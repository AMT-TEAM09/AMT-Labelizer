# Labelizer

### Auteurs : Stéphane Marengo & Géraud Silvestri

## :warning: Pré-requis

[Docker](https://www.docker.com/)

Un utilisateur [AWS IAM](https://aws.amazon.com/iam/)

Si vous souhaitez lancer le service ou les tests sans passer par Docker il faut également les dépendances suivantes :

[Java 17](https://adoptium.net/temurin/releases/)

[Maven 3.8](https://maven.apache.org/download.cgi) avec la variable d'environnement `JAVA_HOME` pointant sur le jdk 17.

## Récupération des dépendances

Pour récupérer la liste des dépendances, il faut lancer la commande suivante à la racine du projet :

```
mvn dependency:resolve
```

## :wrench: Installation

Pour installer le projet, il suffit de cloner le projet et de lancer la commande suivante depuis sa racine :

```
mvn install
```

Il est possible d'éviter de lancer les tests en ajoutant l'option `-DskipTests` à la commande précédente.

```
mvn install -DskipTests
```

Un fichier `.env` doit ensuite être créé à la racine du projet avec les informations suivantes :

```
AWS_ACCESS_KEY_ID= ...
AWS_SECRET_ACCESS_KEY= ...
AWS_REGION= ...
```

La region doit être définie selon la colonne `Region` du tableau présenté dans [la documentation](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Concepts.RegionsAndAvailabilityZones.html#Concepts.RegionsAndAvailabilityZones.Regions).

## Tests

Pour lancer les tests, il suffit de lancer la commande suivante :

```
mvn test
```

Pour lancer une classe de tests spécifique:

```
mvn -Dtest=NomDeLaClasseDeTest test
```

Pour lancer un test spécifique :

```
mvn -Dtest=NomDeLaClasseDeTest#nomDuTest test
```

Plus d'infos, consultez la [documentation](https://maven.apache.org/surefire/maven-surefire-plugin/examples/single-test.html)

Les tests peuvent également être lancés à l'intérieur d'un container en construisant l'image avec la `target` `tests`:

```bash
docker build . --target tests -t labelizer:tests
```

Puis en la lançant:

```bash
docker run --env-file .env -it -t labelizer:tests
```

## :rocket: Lancement du service

### Manuellement

Utilisez les commandes suivantes pour créer le `.jar` et le lancer :

```
mvn package

java -jar --enable-preview target\Labelizer-0.0.1-SNAPSHOT.jar
```

> :warning: **Il est nécessaire que le fichier `.env` se situe dans le même dossier.**

### Avec Docker

Pour lancer le service avec Docker, il suffit de construire l'image avec la `target` `production`:

```bash
docker build . --target production -t labelizer:production
```

Puis la lancer:

```bash
docker run --env-file .env -p 8081:8081 -it -t labelizer:production
```

> :grey_question: Le paramètre `-p <port>:8081` permet de rediriger le port 8081 du container vers le port `<port>` de la machine hôte.

## Structure du projet

```
labelizer
    ├───advice      # Traduction des exceptions en réponse HTTP
    ├───assembler   # Gestion des liens HATEOAS
    ├───controller
    │   ├───api     # API REST
    │   └───request # DTOs pour les requêtes
    ├───dto
    ├───exception
    └───service
        ├───impl
        └───interfaces
```

# API

## POST /analyzer/v1/url

Retourne les labels détectés sur l'image à l'URL `source` avec une confiance supérieure à `minConfidence` et un nombre de labels ne dépassant pas `maxLabels`.

+ Requête (application/json)

    ```json
    {
        "source": "https://...",
        "minConfidence": 80.5,  // optionnel (défaut: 90)
        "maxLabels": 10         // optionnel (défaut: 10)
    }
    ```

+ Réponse OK (200)

    ```json
    {
        "labels": [
            {
                "name": "label1",
                "confidence": 82.5
            },
            {
                "name": "labelN",
                "confidence": 85
            }
        ],
        "_links": {
            "self": {
                "href": "http://.../analyzer/v1/url"
            },
            "base64": {
                "href": "http://.../analyzer/v1/url/base64"
            }
        }
    }
    ```

Code retourné en cas d'erreur:
+ URL invalide: 422
+ Erreurs dans les paramètres de la requête: 422
+ JSON mal formé ou champ inconnu: 400

## POST /analyzer/v1/base64

Retourne les labels détectés sur l'image en base64 `source` avec une confiance supérieure à `minConfidence` et un nombre de labels ne dépassant pas `maxLabels`.

+ Requête (application/json)

    ```json
    {
        "source": "<string en base64>",
        "minConfidence": 80.5,  // optionnel (défaut: 90)
        "maxLabels": 10         // optionnel (défaut: 10)
    }
    ```

+ Réponse OK (200)

    ```json
    {
        "labels": [
            {
                "name": "label1",
                "confidence": 82.5
            },
            {
                "name": "labelN",
                "confidence": 85
            }
        ],
        "_links": {
            "url": {
                "href": "http://.../analyzer/v1/url"
            },
            "self": {
                "href": "http://.../analyzer/v1/url/base64"
            }
        }
    }
    ```

Code retourné en cas d'erreur:
+ Base64 invalide: 422
+ Erreurs dans les paramètres de la requête: 422
+ JSON mal formé ou champ inconnu: 400