# Analisi dell'APK — Column 1.3.7 (com.columnapp.column)

Metodo: download APK (apkeep, sorgente APKPure) → estrazione XAPK → decompilazione con jadx 1.5.6
→ ispezione del bundle web in `assets/public/`.

## Identità del pacchetto

| Campo | Valore |
|---|---|
| package | `com.columnapp.column` |
| versionName | `1.3.7` |
| versionCode | `327` |
| minSdk / targetSdk | 29 / 36 |
| developer | Column srl |
| formato | XAPK (base + `config.arm64_v8a`, `config.en`, `config.mdpi`) |

## Permessi rilevanti

```
com.google.android.gms.permission.AD_ID
android.permission.ACCESS_ADSERVICES_ATTRIBUTION
android.permission.ACCESS_ADSERVICES_AD_ID
android.permission.ACCESS_ADSERVICES_CUSTOM_AUDIENCE
com.android.vending.BILLING
```

I permessi `ACCESS_ADSERVICES_*` + `AD_ID` indicano la presenza dell'Advertising ID e di una
componente ads, ma **il dex non contiene il Mobile Ads SDK** (nessuna `AdView`, nessun `MobileAds`).

## Layer nativo (tutto qui)

```
com/columnapp/column/MainActivity            (Capacitor BridgeActivity)
com/columnapp/column/ColumnWebView           (menu "✨ Chiedi a ColumnAI" sul testo selezionato)
com/columnapp/column/BuildConfig
com/columnapp/plugins/metapixel/MetaPixel         (stub: "Method not implemented")
com/columnapp/plugins/metapixel/MetaPixelPlugin
```

`ColumnWebView` inietta JS nativo:

```java
evaluateJavascript("...window.__columnAskWithSelection(t)...")
```

→ la feature ColumnAI vive nel web layer, non nel nativo.

## Bundle web (266 file .js, ~4.9 MB)

Il gating dei contenuti è nel web layer:

```js
this.PERKS = {
  premium:  ["Riassunti AI illimitati", "Podcast quotidiani personalizzati", "Zero pubblicità"],
  freemium: ["Riassunti AI illimitati", "Podcast quotidiani personalizzati"]
}
buildReward(n){
  let e = n.entitlement === 1;
  return { badge: e ? "Premium" : "Freemium", expiry: this.formatDate(n.entitlement_expire_ts), ... }
}
```

- `entitlement` / `entitlement_expire_ts` / `duration_days` → forniti dal backend.
- `redemptionService` + `promoService` → flusso **codici promo/referral** che concede `entitlement = 1`
  per una durata. È la via legittima per ottenere Premium gratuitamente.

### Host contattati dal web layer

```
api.column.news        auth.column.news       checkout.column.news
share.column.news      web.column.news        www.column.news
api.revenuecat.com     e.revenue.cat          errors.rev.cat
app.posthog.com        us.i.posthog.com       us.posthog.com      (analytics)
www.googletagmanager.com                                          (tag manager)
js.stripe.com          cdn.paddle.com                            (pagamenti)
bundles-pics.s3.eu-south-1.amazonaws.com
```

Nessun host pubblicitario statico nel bundle: i tag ads vengono caricati a runtime dal container
Google Tag Manager. Da qui la scelta di bloccare per host noti (DoubleClick/Google Ads + reti
programmatiche) al livello della WebView.

## Conclusione tecnica

1. Non esiste codice client che decida l'accesso Premium: la decisione è del backend.
2. L'unica feature premium esclusiva implementabile lato client è la rimozione degli ads.
3. Gli ads sono richieste HTTP della WebView → intercettabili in
   `com.getcapacitor.BridgeWebViewClient.shouldInterceptRequest`.

## Punto d'innesto della patch

`com/getcapacitor/BridgeWebViewClient;->shouldInterceptRequest(Landroid/webkit/WebView;Landroid/webkit/WebResourceRequest;)Landroid/webkit/WebResourceResponse;`

Istruzioni iniettate all'indice 0 (p0 = this, p1 = WebView, p2 = WebResourceRequest):

```smali
invoke-static {p2}, Lapp/columnpatches/extension/AdBlocker;->isBlockedRequest(Landroid/webkit/WebResourceRequest;)Z
move-result v0
if-eqz v0, :column_adblock_continue
invoke-static {}, Lapp/columnpatches/extension/AdBlocker;->blockedResponse()Landroid/webkit/WebResourceResponse;
move-result-object v0
return-object v0
:column_adblock_continue
# ... codice originale: bridge.getLocalServer().shouldInterceptRequest(request)
```

`v0` è il registro temporaneo già usato dal metodo originale, quindi non si aumenta il numero di registri.
