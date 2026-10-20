# Alternativa senza patching: blocco ads a livello DNS

Il 100% del blocco pubblicitario si ottiene senza toccare l'APK, in modo del tutto lecito, con un
resolver DNS filtrante. Vale per Column e per tutte le altre app del telefono.

## Perché funziona su Column

Gli ads di Column sono richieste HTTP del web layer. Se il DNS non risolve gli host pubblicitari, le
richieste falliscono e nessun annuncio viene mostrato. Non serve root, non serve modificare l'app.

## Opzione A — NextDNS (nessun hardware, ~5 minuti)

1. Crea un profilo su <https://nextdns.io>.
2. Abilita le blocklist *AdGuard DNS filter* + *OISD*.
3. In *Denylist* incolla le voci di `blocklists/ads-hosts.txt`.
4. Sul telefono: **Impostazioni → Rete → DNS privato** → inserisci l'URL del profilo (`https://dns.nextdns.io/xxxxxx`).

## Opzione B — AdGuard DNS / dns0.eu

Stesso principio: metti il DNS filtrante in *DNS privato* di Android. Zero installazioni.

## Opzione C — Pi-hole o AdGuard Home (rete domestica)

1. Installa Pi-hole/AdGuard Home su un Raspberry Pi o un NAS.
2. Aggiungi `blocklists/ads-hosts.txt` come *Local list*.
3. Imposta il router (o il telefono) per usare quel DNS.

## Opzione D — blocca solo sul telefono, senza DNS

App come AdGuard (versione local-VPN) o RethinkDNS filtrano in locale. Anche qui: nessuna modifica all'APK.

## Verifica

Apri Column e controlla il log DNS: gli host in `blocklists/ads-hosts.txt` non devono comparire come
risolti. Se un annuncio passa ancora, individua l'host dalla query log, aggiungilo alla deny list e
riprova — oppure aggiungilo a `AdBlocker.java` nel progetto Morphe e ricompila.
