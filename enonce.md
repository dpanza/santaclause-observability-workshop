# 🎄 Escape Game Observabilité — Santa's Wish List

## Prologue

*24 décembre, 6h00 du matin — Pôle Nord*

Le Père Noël est en panique. Dans quelques heures, il doit partir pour sa tournée mondiale, mais l'application **Santa's Wish List** — le système qui gère tous les vœux des enfants — montre des signes inquiétants.

Les lutins Jingle, Twinkle, Snowflake et Sparkle travaillent sans relâche, mais quelque chose ne va pas. Les commandes s'accumulent, certains jouets semblent introuvables, et le tableau de bord clignote de partout.

Le Père Noël a fait appel à vous, experts en observabilité, pour sauver Noël. Vous avez **3 heures** pour diagnostiquer et résoudre les problèmes avant le grand départ.

> *"Ho ho ho... J'ai confiance en vous. Les enfants du monde entier comptent sur votre expertise !"*
> — Père Noël

---

## Votre mission

Résolvez les **6 énigmes** en utilisant Grafana pour explorer les métriques, traces et logs de l'application. Chaque énigme résolue vous donnera un indice pour la suivante.

### Accès aux outils

| Outil          | URL                     | Identifiants  |
|----------------|-------------------------|---------------|
| 🎯 Grafana     | http://localhost:3000   | admin / santa |
| 🎁 Application | http://localhost:8080   | —             |

### Rappels Grafana

- **Dashboards** : visualisations pré-construites
- **Explore** : investigation libre (métriques, traces, logs)
- **Alerting** : création de règles d'alerte

---

## 🎅 Énigme 1 — Le tableau de bord du Père Noël

### Contexte

Le Père Noël a un magnifique tableau de bord, mais il avoue ne pas tout comprendre. Avant de plonger dans les problèmes, il a besoin que vous l'aidiez à lire les informations essentielles.

> *"Ces graphiques sont très jolis, mais... qu'est-ce qu'ils me disent exactement ?"*

### Votre mission

Ouvrez le dashboard **"Santa's Workshop Overview"** et répondez aux questions suivantes :

| # | Question                                                    | Votre réponse |
|---|-------------------------------------------------------------|---------------|
| 1 | Combien de vœux ont été reçus dans la **dernière heure** ?  |               |
| 2 | Quel est le **jouet le plus demandé** aujourd'hui ?         |               |
| 3 | Combien de **lutins** sont actuellement actifs ?            |               |
| 4 | Quelle **catégorie** de jouets génère le plus de demandes ? |               |

### Indices

<details>
<summary>💡 Indice 1</summary>
Regardez le sélecteur de temps en haut à droite de Grafana. Vous pouvez choisir "Last 1 hour".
</details>

<details>
<summary>💡 Indice 2</summary>
Survolez les barres ou portions des graphiques pour voir les détails.
</details>

<details>
<summary>💡 Indice 3</summary>
Les légendes sous les graphiques contiennent des informations précieuses.
</details>

### Validation

Une fois vos réponses complétées, observez attentivement le panel des lutins. L'un d'entre eux semble en difficulté... Notez son nom : **____________**

C'est votre indice pour l'énigme suivante !

---

## 🧝 Énigme 2 — Le lutin épuisé

### Contexte

Vous avez identifié qu'un lutin semble avoir des difficultés. En regardant de plus près, vous remarquez qu'il traite beaucoup moins de commandes que ses collègues, et celles qu'il traite prennent beaucoup plus de temps.

> *"Pauvre Sparkle... Il travaille si dur mais n'arrive pas à suivre le rythme des autres !"*

### Votre mission

Découvrez **pourquoi** ce lutin est plus lent que les autres.

**Étape 1** : Trouvez la métrique qui mesure le temps de traitement des vœux par lutin.

<details>
<summary>💡 Aide</summary>
Allez dans Explore → sélectionnez Prometheus → cherchez une métrique contenant "duration" et "wish"
</details>

**Étape 2** : Créez un nouveau panel dans le dashboard qui compare le **temps moyen de traitement** de chaque lutin.

| Élément             | Valeur |
|---------------------|--------|
| Métrique utilisée   |        |
| Requête PromQL      |        |

**Étape 3** : Analysez les résultats. Qu'est-ce qui différencie ce lutin des autres ?

<details>
<summary>💡 Indice</summary>
Regardez les labels de la métrique. Un label en particulier montre une différence...
</details>

### Questions

| # | Question                                                         | Votre réponse |
|---|------------------------------------------------------------------|---------------|
| 1 | Quel est le temps moyen de traitement de Sparkle vs les autres ? |               |
| 2 | Quel **label** différencie Sparkle des autres lutins ?           |               |
| 3 | Quelle est la **valeur** de ce label pour Sparkle ?              |               |

### Validation

La catégorie problématique est : **____________**

Notez cette information, elle sera cruciale pour la suite !

---

## 📜 Énigme 3 — La liste interminable

### Contexte

Ce matin, tout allait bien. Mais au fil des heures, l'application devient de plus en plus lente. Le Père Noël est perplexe : rien n'a changé dans la configuration, pourtant les performances se dégradent continuellement.

> *"C'est étrange... Ce matin, les vœux étaient traités en quelques millisecondes. Maintenant, ça prend plusieurs secondes !"*

Un développeur lutin mentionne qu'une nouvelle fonctionnalité a été déployée hier soir : la **détection des doublons**. Un enfant ne peut plus demander le même jouet plusieurs fois.

### Votre mission

Identifiez pourquoi cette fonctionnalité cause un ralentissement progressif.

**Étape 1** : Trouvez les métriques liées à la détection de doublons.

<details>
<summary>💡 Aide</summary>
Cherchez des métriques contenant "duplicate"
</details>

**Étape 2** : Créez un panel montrant l'évolution de `duplicate_check_duration_seconds` sur les dernières heures.

**Étape 3** : Sur le même graphique (ou un autre), ajoutez la métrique `wishes_in_database`.

### Questions

| # | Question                                                                 | Votre réponse |
|---|--------------------------------------------------------------------------|---------------|
| 1 | Comment évolue la durée du duplicate check au fil du temps ?             |               |
| 2 | Y a-t-il une corrélation avec le nombre de vœux en base ?                |               |
| 3 | Quelle est la complexité algorithmique probable ? (O(1), O(n), O(n²)...) |               |

**Étape 4** : Confirmez votre hypothèse avec le tracing.

- Allez dans **Explore → Tempo**
- Cherchez un span nommé `duplicate.check`
- Examinez l'attribut `iterations_count`

| Moment                     | iterations_count | existing_count |
|----------------------------|------------------|----------------|
| Ce matin (ancienne trace)  |                  |                |
| Maintenant (trace récente) |                  |                |

### La solution

Vous avez compris le problème ! L'algorithme parcourt **toute la liste** des vœux existants pour chaque nouveau vœu. C'est du O(n) par vœu, soit O(n²) au total.

**Proposez une solution** (en une phrase) :

_______________________________________________

### Validation

Pour vérifier votre diagnostic, activez l'optimisation :

```bash
curl -X POST "http://localhost:8080/admin/enable-optimization?type=INDEX"
```

Observez les métriques pendant 2-3 minutes. La durée du duplicate check devrait chuter drastiquement.

✅ **Énigme résolue !** Passez à la suivante.

---

## 🔍 Énigme 4 — La commande perdue

### Contexte

Un message urgent arrive du service client du Pôle Nord :

> *"Cher support, mon fils Théo a demandé une Nintendo Switch il y a 2 heures. Le statut indique toujours 'En traitement'. Il est très inquiet que le Père Noël ne reçoive pas sa demande à temps ! Pouvez-vous vérifier ?"*

Le Père Noël vous demande de retrouver cette commande et de comprendre pourquoi elle est bloquée.

### Votre mission

Utilisez le **tracing distribué** pour suivre le parcours de cette commande.

**Étape 1** : Allez dans **Explore → Tempo**

**Étape 2** : Construisez une requête TraceQL pour trouver des traces :
- De catégorie `ELECTRONIC`
- Avec une durée supérieure à 1 seconde

<details>
<summary>💡 Aide TraceQL</summary>

```
{ span.category = "ELECTRONIC" && duration > 1s }
```

ou cherchez par nom de span :

```
{ name = "wish.process" && span.category = "ELECTRONIC" }
```
</details>

**Étape 3** : Sélectionnez une trace et explorez sa structure.

### Questions

| # | Question | Votre réponse |
|---|----------|---------------|
| 1 | Combien de spans composent cette trace ? | |
| 2 | Quel span prend le plus de temps ? | |
| 3 | Quelle est la durée de ce span ? | |
| 4 | Quel attribut indique la cause du ralentissement ? | |

**Étape 4** : Dessinez la structure de la trace (ou décrivez-la)

```
wish.receive (Xms)
└── wish.route (Xms)
    └── wish.process (Xms)
        └── ??? (Xms)        <-- Le coupable !
            └── ???
```

### Validation

Le span problématique est : **____________**

Ce span révèle qu'un appel externe est effectué pour les jouets électroniques. Mais ce n'est pas le seul problème...

---

## 🏭 Énigme 5 — Le mystère de l'entrepôt

### Contexte

Vous avez découvert qu'un appel externe ralentit le traitement des jouets électroniques. Mais en creusant davantage, vous remarquez que même sans cet appel, quelque chose d'autre cause des lenteurs.

> *"Les lutins me disent que l'entrepôt répond lentement... Mais seulement pour certains jouets !"*

### Votre mission

Utilisez la **corrélation logs-traces-métriques** pour résoudre ce mystère.

**Étape 1** : Depuis une trace de jouet ELECTRONIC, cliquez sur **"Logs for this span"** (ou le bouton équivalent pour voir les logs associés).

**Étape 2** : Cherchez un message de log suspect.

| Niveau | Message trouvé |
|--------|----------------|
| WARN   |                |

<details>
<summary>💡 Indice</summary>
Cherchez un message contenant "cache" ou "fallback"
</details>

**Étape 3** : Allez dans **Explore → Loki** et recherchez ce type de message sur les dernières heures.

Requête suggérée :
```
{service_name="santa-wishlist"} |= "cache" | logfmt
```

**Étape 4** : Analysez les métriques de cache.

| Métrique     | Catégorie PLUSH | Catégorie ELECTRONIC |
|--------------|-----------------|----------------------|
| Cache hits   |                 |                      |
| Cache misses |                 |                      |
| Hit rate (%) |                 |                      |

### Questions

| # | Question                                                        | Votre réponse |
|---|-----------------------------------------------------------------|---------------|
| 1 | Quel message de warning apparaît pour les jouets ELECTRONIC ?   |               |
| 2 | Pourquoi le cache ne fonctionne-t-il pas pour cette catégorie ? |               |
| 3 | Quelle conséquence cela a-t-il sur la base de données ?         |               |
| 4 | Quelle table est impactée ?                                     |               |

### Corrélation temporelle

Créez un panel avec deux métriques superposées :
- `inventory_cache_misses_total{category="ELECTRONIC"}`
- Latence des requêtes DB (si disponible) ou `wish_processing_duration_seconds{category="ELECTRONIC"}`

Les pics correspondent-ils ? **OUI / NON**

### Validation

Le problème est double :
1. _________________________________ (cache)
2. _________________________________ (base de données)

---

## 🚨 Énigme 6 — Sauver Noël

### Contexte

Félicitations ! Vous avez diagnostiqué tous les problèmes :

1. ✅ Routing déséquilibré → Sparkle surchargé
2. ✅ Algorithme O(n²) → Détection de doublons lente
3. ✅ Appel externe lent → Fournisseur de jouets électroniques
4. ✅ Cache miss → Jouets ELECTRONIC non mis en cache

Maintenant, le Père Noël veut s'assurer que ces problèmes ne se reproduiront plus jamais. Il vous demande de créer un **système d'alerte** pour surveiller l'application.

> *"Je ne veux plus jamais être surpris la veille de Noël ! Prévenez-moi AVANT que les problèmes n'arrivent !"*

### Votre mission

Créez un dashboard de synthèse et des alertes pour protéger Noël.

**Partie 1 : Dashboard de monitoring**

Créez un nouveau dashboard "Santa's Alert Center" avec les panels suivants :

| Panel              | Description                        | Métrique/Requête |
|--------------------|------------------------------------|------------------|
| 🔴 Cache Health    | Taux de cache miss par catégorie   |                  |
| 🟡 Processing Time | Temps de traitement par lutin      |                  |
| 🟢 Duplicate Check | Durée du check vs nombre de wishes |                  |
| 📊 System Overview | Vue globale des KPIs               |                  |

**Partie 2 : Création d'une alerte**

Créez une alerte qui se déclenche si le taux de cache miss dépasse 50% pendant plus de 5 minutes.

**Étapes :**
1. Allez dans **Alerting → Alert rules → New alert rule**
2. Configurez la condition

| Paramètre  | Valeur |
|------------|--------|
| Métrique   |        |
| Condition  |        |
| Seuil      |        |
| Durée      |        |
| Message    |        |

**Partie 3 : Diagnostic final**

Rédigez un **rapport de diagnostic** en 5 lignes maximum pour le Père Noël :

```
RAPPORT DE DIAGNOSTIC — Santa's Wish List
==========================================
Problème 1 : 
Problème 2 : 
Problème 3 : 
Problème 4 : 
Recommandation : 
```

---

## 🎉 Épilogue

*24 décembre, 17h00 — Pôle Nord*

Le Père Noël rayonne de joie. Grâce à votre expertise en observabilité, tous les problèmes ont été identifiés et les systèmes d'alerte sont en place.

> *"Ho ho ho ! Vous avez sauvé Noël ! Les millions d'enfants du monde entier recevront leurs cadeaux à temps. Merci, chers experts !"*

Les lutins Jingle, Twinkle, Snowflake et même Sparkle (maintenant soulagé de sa surcharge de travail) vous applaudissent.

Le traîneau est chargé, les rennes sont prêts. Noël est sauvé ! 🎅🦌🎁

---

## 🏆 Code secret de victoire

Une fois toutes les énigmes résolues, assemblez vos réponses pour former le code secret :

| Énigme | Élément clé trouvé         | Réponse        |
|--------|----------------------------|----------------|
| 1      | Nom du lutin en difficulté | Sparkle        |
| 2      | Catégorie problématique    | ELECTRONIC     |
| 3      | Type d'optimisation        | REPOSITORY     |
| 4      | Nom du span lent           | ENTREPOT       |
| 5      | Nom de la table DB         | toy_inventory  |
| 6      | Seuil de votre alerte      |                |

**Code secret : 🎄 NOEL-2024-SAVED 🎄**

---

## 📚 Ressources

### Aide-mémoire PromQL

```promql
# Compteur simple
sum(wishes_received_total)

# Taux par seconde
rate(wishes_received_total[5m])

# Moyenne par label
avg by (elf) (wish_processing_duration_seconds)

# Histogramme - percentile 95
histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))
```

### Aide-mémoire TraceQL

```
# Par nom de span
{ name = "wish.process" }

# Par attribut
{ span.category = "ELECTRONIC" }

# Par durée
{ duration > 1s }

# Combinaison
{ name = "inventory.check_stock" && span.cache_hit = false }
```

### Aide-mémoire LogQL

```logql
# Recherche simple
{service_name="santa-wishlist"} |= "error"

# Avec parsing
{service_name="santa-wishlist"} | json | level = "WARN"

# Pattern
{service_name="santa-wishlist"} |~ "cache.*miss"
```

---

*Joyeux Noël et bonne observabilité ! 🎄*
