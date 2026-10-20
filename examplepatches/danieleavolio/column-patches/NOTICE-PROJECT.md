# Proprietà intellettuale e limiti di questo progetto

Questo repository contiene solo codice di patch. Non contiene e non deve contenere:

- l'APK di Column né sue parti (dex, risorse, bundle JS) — materiale protetto da copyright;
- credenziali, token o chiavi;
- codice o dati che aggirano l'abbonamento Premium.

## Cosa fa la patch

Intercetta le richieste di rete degli annunci (`shouldInterceptRequest`) e le annulla. È la stessa
categoria di patch pubblicata da progetti come ReVanced / Morphe / AdGuard. Non legge né falsifica
lo stato di abbonamento, non modifica contenuti, non aggira paywall.

## Cosa non fa

Non sblocca il tier Premium. L'accesso Premium di Column è concesso e verificato lato server
(`api.column.news` + RevenueCat entitlement), quindi non è modificabile dal client. Tentare di
forgiare tale entitlement violerebbe la Direttiva UE 2019/790 art. 6, la L. 248/2016 in Italia e il
DMCA §1201 negli USA, anche per uso personale.

Per ottenere Premium in modo legittimo: usare il flusso di codici promo/referral dell'app
(`redemptionService`) o l'abbonamento in-app.
