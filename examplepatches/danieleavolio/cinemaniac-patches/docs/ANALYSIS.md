# Analisi — Cinemaniac (`it.papalillo.moviestowatch`)

Analisi statica dell'APK scaricato da APKPure (arm64-v8a, 107 MB, versionName **5.0.7**,
versionCode **172**, minSdk 24, targetSdk 36).

## Identità

| Campo | Valore |
|---|---|
| Package | `it.papalillo.moviestowatch` |
| Nome | Cinemaniac: Movie & TV Tracker |
| Sviluppatore | Antonio Papalillo (sviluppatore indipendente singolo) |
| Installazioni | 1.000.000+ |
| Prezzo | Gratis, con acquisti in-app |

## Stack tecnico

- **React Native** (nuova architettura) con **Hermes**: tutta la logica applicativa è in
  `assets/index.android.bundle` (5,3 MB di bytecode Hermes, non JS leggibile).
- `libNitroIap.so` + `react-native-nitro-iap` → acquisti in-app.
- `io.invertase.googlemobileads` (`react-native-google-mobile-ads`) → **annunci AdMob**.
- `@react-native-firebase/*` (analytics, crashlytics, messaging).
- `react-native-reanimated`, `react-native-screens`, `react-native-svg`, Nitro Modules.

Il livello nativo (`classes*.dex`) contiene solo le librerie: **non c'è codice applicativo
Java/Kotlin da patchare**. La logica sta nel bundle Hermes, che i patcher Morphe/ReVanced non
gestiscono (lavorano su smali/dex).

## Esiste un tier PRO

Dalle chiavi di localizzazione presenti nel bundle:

| Chiave | Significato |
|---|---|
| `pro_unlock_price` | prezzo dell'acquisto |
| `unlock_pro_button` | pulsante di acquisto |
| `pro_remove_ads` | rimozione pubblicità |
| `pro_unlock_categories` | categorie personalizzate |
| `pro_unlock_favorites` | preferiti |
| `pro_unlock_streaming_providers` | provider di streaming |
| `pro_unlock_themes` | temi |
| `pro_unlock_random_select` | selezione casuale |
| `pro_cancel_anytime` | (testo) cancellazione |
| `pro_read_conditions` | termini |

Stringhe collegate: `Unlock PRO account unlocked. Enjoy!`, `pro_unlocked-openAppStoreIfInAppFails`.

## Il PRO è validato lato server

Stringhe rilevanti nel bundle:

```
purchase verified via validate/user, user is effectively PRO
PRO_LAST_VALIDATED
Failed to get active subscriptions
Network Error checking active subscriptions
```

- Backend: `https://api.cinemaniac.app/v1/...` (endpoint `validate/user`).
- L'app invia la ricevuta dell'acquisto al backend, che risponde con lo stato PRO.
- `PRO_LAST_VALIDATED` è solo una cache locale con scadenza (grace period offline):
  **la fonte di verità è il server**.

Di conseguenza un flag client-side non sbloccherebbe nulla: le funzioni PRO vengono abilitate
in base alla risposta del backend.

## Pubblicità

- SDK: Google Mobile Ads (AdMob), publisher `ca-app-pub-2990132554136404`, ~40 unità
  pubblicitarie (`banner_protected`, interstitial, rewarded, app-open).
- Bridge React Native: `io.invertase.googlemobileads.*`
  (`ReactNativeGoogleMobileAdsModule`, `...InterstitialModule`, `...RewardedModule`,
  `...RewardedInterstitialModule`, `...AppOpenModule`, `...BannerAdViewManager`).
- Permesso `com.google.android.gms.permission.AD_ID` +
  `ACCESS_ADSERVICES_AD_ID` / `_ATTRIBUTION` / `_TOPICS` nel manifest.

### Entry point nativi neutralizzati dalla patch

Verificati con `dexdump -d classes.dex` (tutti `void`, nessuno con `try/catch`):

```
com.google.android.gms.ads.BaseAdView.loadAd(AdRequest)
com.google.android.gms.ads.admanager.AdManagerAdView.loadAd(AdManagerAdRequest)
com.google.android.gms.ads.interstitial.InterstitialAd.load(Context, String, AdRequest, InterstitialAdLoadCallback)
com.google.android.gms.ads.admanager.AdManagerInterstitialAd.load(Context, String, AdManagerAdRequest, AdManagerInterstitialAdLoadCallback)
com.google.android.gms.ads.rewarded.RewardedAd.load(Context, String, AdRequest, RewardedAdLoadCallback)
com.google.android.gms.ads.rewarded.RewardedAd.load(Context, String, AdManagerAdRequest, RewardedAdLoadCallback)
com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd.load(Context, String, AdRequest, RewardedInterstitialAdLoadCallback)
com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd.load(Context, String, AdManagerAdRequest, RewardedInterstitialAdLoadCallback)
com.google.android.gms.ads.appopen.AppOpenAd.load(Context, String, AdRequest, AppOpenAdLoadCallback)
com.google.android.gms.ads.appopen.AppOpenAd.load(Context, String, AdRequest, int, AppOpenAdLoadCallback)
com.google.android.gms.ads.appopen.AppOpenAd.load(Context, String, AdManagerAdRequest, int, AppOpenAdLoadCallback)
com.google.android.gms.ads.AdLoader.loadAd(AdRequest)
com.google.android.gms.ads.AdLoader.loadAd(AdManagerAdRequest)
```

`MobileAds.initialize(...)` **non** viene toccato di proposito: neutralizzarlo lascerebbe la
Promise "ads initialised" del bridge React Native appesa per sempre e l'app si bloccherebbe
all'avvio.

## Advertising ID

`com.google.android.gms.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(Context)`:

```
public static AdvertisingIdClient.Info getAdvertisingIdInfo(Context);   registers = 8, ins = 1
AdvertisingIdClient$Info.<init>(String, boolean)                          PUBLIC CONSTRUCTOR
```

Il metodo viene sostituito con una `Info` sintetica: UUID fisso (uguale su ogni
installazione, quindi non identifica nessuno) e `isLimitAdTrackingEnabled() = true`.

## Perché non viene creata una patch di sblocco PRO

1. **Tecnicamente inutile**: lo stato PRO è deciso da `api.cinemaniac.app`, non dal client.
   Un flag locale o un `isPro = true` non cambierebbe le risposte del server.
2. **Elusione di misura di protezione**: forzare l'entitlement equivale a bypassare un
   paywall (Direttiva UE 2019/790 art. 6, L. 248/2016, DMCA §1201), anche per uso personale.
3. **Superficie sbagliata**: il codice da modificare è bytecode Hermes, non smali; non è
   un target sensato per Morphe.

## Conclusione

Per Cinemaniac non esiste un insieme di "funzioni a pagamento" sbloccabili client-side:
l'app è un guscio React Native con logica in bytecode Hermes e stato PRO sul server.
Non è stato prodotto alcun bypass. L'unica patch prodotta rimuove gli annunci e l'identificatore
pubblicitario. Per ottenere PRO in modo legittimo: usare il flusso di acquisto in-app, il
ripristino dell'acquisto (`restore`) già presente nell'app, o chiedere un codice promo a chi
sviluppa l'app.
