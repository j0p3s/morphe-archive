# Proprietà intellettuale e limiti di questo progetto

Questo repository contiene solo codice di patch. Non contiene e non deve contenere:

- l'APK di Column né sue parti (dex, risorse, bundle JS) — materiale protetto da copyright;
- credenziali, token o chiavi.

## Cosa fa la patch

Intercetta le richieste di rete degli annunci (`shouldInterceptRequest`) e le annulla. È la stessa
categoria di patch pubblicata da progetti come ReVanced / Morphe / AdGuard. Non tocca lo stato
di abbonamento né alcun entitlement, e non modifica gli altri contenuti dell'app.
