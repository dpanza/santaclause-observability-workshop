# Santa's Wish List - Escape Game Observabilite

Application de formation pour apprendre Grafana a travers un escape game.

## Contexte

Cette application simule la gestion des voeux de cadeaux de Noel.

### Objectifs pedagogiques

- **Dashboarding** : navigation et creation de panels Grafana
- **Metrics** : comprendre et exploiter les metriques applicatives
- **Tracing** : explorer les traces distribuees
- **Diagnostique** : correler logs, traces et metriques pour resoudre des problemes

## Demarrage rapide

```bash
docker-compose up --build -d
```

Attendre environ 2 minutes pour que l'application demarre et genere des donnees.

## Partez du dashboard du Père noel
Chargez le dashboard du père noel dans votre [grafana local](localhost:3000), Dashboard > New > Import. Puis collez le contenu de
santaclause-observability-workshop/grafana/provisioning/dashboards/santa-workshop-overview.json

## Redemarrage

### Redemarrer l'app (sans rebuild)

```bash
docker-compose restart app -d
```

### Redemarrer avec rebuild (apres modification du code)

```bash
docker-compose up --build app -d
```

### Reset complet (supprime les donnees)

```bash
docker-compose down -v
docker-compose up --build -d
```

## Acces

| Service     | URL                   | Credentials       |
|-------------|-----------------------|-------------------|
| Application | http://localhost:8080 | -                 |
| Grafana     | http://localhost:3000 | admin / santa     |
| PostgreSQL  | localhost:5432        | santa / northpole |

## API

### Soumettre un voeu

```bash
curl -X POST http://localhost:8080/wishes \
  -H "Content-Type: application/json" \
  -d '{"childName": "Emma", "toyName": "Teddy Bear", "category": "PLUSH"}'
```

### Consulter un voeu

```bash
curl http://localhost:8080/wishes/{id}
```

### Consulter les voeux d'un enfant

```bash
curl http://localhost:8080/wishes?child=Emma
```

### Statistiques

```bash
curl http://localhost:8080/admin/stats
```

### Activer l'optimisation

```bash
curl -X POST http://localhost:8080/admin/enable-optimization
```

### Desactiver l'optimisation

```bash
curl -X POST http://localhost:8080/admin/disable-optimization
```

## Metriques disponibles

| Metrique                           | Type    | Labels                | Description                   |
|------------------------------------|---------|-----------------------|-------------------------------|
| `wishes_received_total`            | Counter | category              | Nombre de voeux recus         |
| `wishes_processed_total`           | Counter | elf, category, status | Voeux traites par lutin       |
| `wish_processing_duration_seconds` | Timer   | elf, category         | Temps de traitement           |
| `duplicate_check_duration_seconds` | Timer   | optimization_enabled  | Duree du check doublons       |
| `duplicate_check_iterations_total` | Counter | -                     | Iterations dans l'algo naif   |
| `wishes_in_database`               | Gauge   | -                     | Nombre total de voeux en base |
| `inventory_cache_hits_total`       | Counter | category              | Hits du cache                 |
| `inventory_cache_misses_total`     | Counter | category              | Miss du cache                 |
| `elves_active`                     | Gauge   | elf                   | Lutins actifs                 |

## Spans de tracing

| Span name                     | Attributs                                              | Probleme detectable     |
|-------------------------------|--------------------------------------------------------|-------------------------|
| `wish.receive`                | child_name, toy_name, category                         | -                       |
| `wish.route`                  | assigned_elf                                           | #1 Routing desequilibre |
| `wish.process`                | elf, wish_id, category                                 | -                       |
| `duplicate.check`             | existing_count, iterations_count, optimization_enabled | #2 Algo O(n)            |
| `inventory.check_stock`       | toy_name, category, cache_hit                          | #3 et #4                |
| `inventory.external_supplier` | toy_name, supplier, duration_ms                        | #3 Appel lent           |
| `db.query`                    | table, operation                                       | #4 Requete lente        |

## Arret

```bash
docker-compose down -v
```

## Structure du projet

```
santa-wishlist-escape-game/
├── docker-compose.yml
├── app/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/santa/wishlist/
│       ├── WishlistApplication.java
│       ├── controller/
│       │   ├── WishController.java
│       │   ├── AdminController.java
│       │   └── dto/
│       ├── service/
│       │   ├── WishService.java
│       │   ├── WishRouter.java
│       │   ├── DuplicateDetectionService.java
│       │   ├── InventoryService.java
│       │   ├── ElfWorker.java
│       │   └── DeliveryService.java
│       ├── repository/
│       ├── model/
│       └── simulator/
│           └── TrafficSimulator.java
├── grafana/
│   └── provisioning/
│       ├── datasources/
│       └── dashboards/
├── sql/
│   └── init.sql
└── README.md
```
