# Proprietà intellettuale e limiti di questo progetto

Questo repository contiene solo codice di patch. Non contiene e non deve contenere:

- l'APK di Cinemaniac né sue parti (dex, risorse, bundle Hermes) — materiale protetto da copyright;
- credenziali, token o chiavi;
- codice o dati che aggirano l'abbonamento PRO.

## Cosa fanno le patch

Bloccano le richieste di annunci del SDK Google Mobile Ads e sostituiscono l'identificatore
pubblicitario (GAID) con un valore fisso. È la stessa categoria di patch pubblicata da progetti
come ReVanced / Morphe / AdGuard. Non leggono né falsificano lo stato di abbonamento, non
modificano contenuti, non aggirano paywall.

## Cosa non fanno

Non sbloccano il tier PRO. L'accesso PRO di Cinemaniac è concesso e verificato lato server
(`api.cinemaniac.app/v1/validate/user`), quindi non è modificabile dal client. Tentare di
forgiare tale entitlement violerebbe la Direttiva UE 2019/790 art. 6, la L. 248/2016 in Italia
e il DMCA §1201 negli USA, anche per uso personale.

Per ottenere PRO in modo legittimo: usare l'acquisto in-app dell'app oppure chiedere un codice
promo a chi la sviluppa.

## Nota sul progetto originale

Cinemaniac è un'app gratuita di uno sviluppatore indipendente, senza pubblicità a
schermo pieno ma con banner e interstitial. Se ti piace e la usi, valuta di sostenerla
tramite l'acquisto in-app o con una donazione.
