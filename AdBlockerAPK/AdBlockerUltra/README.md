# AdBlocker Ultra 3.0

Projet Android/Kotlin plus complet pour un bloqueur DNS local sans root.

## Architecture

Applications / navigateur
        ↓
Android DNS
        ↓
VPN local (VpnService)
        ↓
DNS parser
   ┌────┴────┐
bloqué     autorisé
NXDOMAIN   1.1.1.1
   ↓          ↓
Android reçoit la réponse

## Fonctionnalités

- VPN Android réel via `VpnService`
- filtrage DNS local
- blocklist de plusieurs domaines publicitaires/tracking
- blocage des sous-domaines
- relais des domaines autorisés vers Cloudflare 1.1.1.1
- compteur des requêtes et blocages
- notification permanente pendant la protection
- interface Compose ON/OFF
- pas de root
- le trafic Internet normal n'est pas routé dans le VPN : seul le sous-réseau DNS virtuel l'est

## Important

Ce n'est pas un bloqueur HTTPS. Une publicité peut encore passer si :
- elle utilise le même domaine que le contenu;
- l'application utilise son propre DNS/DoH/DoT;
- elle contourne le DNS système;
- le domaine publicitaire n'est pas dans la liste.

IPv6 et certains transports DNS alternatifs doivent encore être traités pour une couverture maximale.

## Compiler

1. Ouvre `AdBlockerUltra` avec Android Studio.
2. Synchronise Gradle.
3. Branche un téléphone Android ou utilise un émulateur.
4. Build > Build APK(s).
5. Installe l'APK.
6. Ouvre l'app et active le switch VPN.
7. Accepte la fenêtre d'autorisation Android.

Pour un vrai produit public, ajoute une blocklist régulièrement mise à jour, une interface
pour ajouter/supprimer des domaines, du cache DNS, plusieurs DNS upstream et des tests
IPv4/IPv6/DoH/DoT. Ne présente pas cette version comme garantissant 100 % des pubs.
