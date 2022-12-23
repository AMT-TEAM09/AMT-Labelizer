# DataObject

### Auteurs : Stéphane Marengo & Géraud Silvestri

## :warning: Pré-requis

[Docker](https://www.docker.com/)

Un utilisateur [AWS IAM](https://aws.amazon.com/iam/) avec les droits suivants :

```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            //Permission de lister tous les data objects de notre organisation
            "Effect": "Allow",
            "Action": "s3:ListAllMyBuckets",
            "Resource": "arn:aws:s3:::*"
        },
        {   
            //Permission totale sur les data objects respectant cette nomenclature
            //[XX] Etant la référence de votre équipe
            "Effect": "Allow",
            "Action": "s3:*",
            "Resource": [
                "arn:aws:s3:::amt.team[XX].diduno.education",
                "arn:aws:s3:::amt.team[XX].diduno.education/*"
            ]
        }
    ]
}
```

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
AWS_BUCKET_NAME= ...
AWS_REGION= ...
```

La region doit être définie selon la colonne `Region` du tableau présenté
dans [la documentation](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Concepts.RegionsAndAvailabilityZones.html#Concepts.RegionsAndAvailabilityZones.Regions).

Un paramètre `AWS_URL_DURATION` peut être ajouté pour définir la durée de validité des urls générées (en minutes) par le
service. Par défaut, cette durée est de 90 minutes.

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

Plus d'infos, consultez
la [documentation](https://maven.apache.org/surefire/maven-surefire-plugin/examples/single-test.html)

Les tests peuvent également être lancés à l'intérieur d'un container en construisant l'image avec la `target` `tests`:

```bash
docker build . --target tests -t dataobject:tests
```

Puis en la lançant:

```bash
docker run --env-file .env -it -t dataobject:tests
```

## :rocket: Lancement du service

### Manuellement

Utilisez les commandes suivantes pour créer le `.jar` et le lancer :

```
mvn package

java -jar --enable-preview target\DataObject-0.0.1-SNAPSHOT.jar
```

> :warning: **Il est nécessaire que le fichier `.env` se situe dans le même dossier.**

### Avec Docker

Pour lancer le service avec Docker, il suffit de construire l'image avec la `target` `production`:

```bash
docker build . --target production -t dataobject:production
```

Puis la lancer:

```bash
docker run --env-file .env -p 8080:8080 -it -t dataobject:production
```

> :grey_question: Le paramètre `-p <port>:8080` permet de rediriger le port 8080 du container vers le port `<port>` de
> la machine hôte.

## Structure du projet

```
dataobject
    ├───advice      # Traduction des exceptions en réponse HTTP
    ├───assembler   # Gestion des liens HATEOAS
    ├───controller
    │   └───api     # API REST
    ├───dto
    ├───exception
    └───service
        ├───impl
        └───interfaces
```

# API

## GET /data-object/v1/objects

Retourne une URL vers l'objet possédant le nom `objectName`. Le lien est valide pendant `duration` secondes.

+ Paramètres
    + objectName (required) - Nom du fichier à récupérer
    + duration (optionnel, défaut 90 minutes) - Durée de validité du lien en secondes

+ Réponse OK (200)

    ```json
    {
        "objectName": "...",
        "url": "...",
        "duration": 123,
        "_links": {
            // ...
        }
    }
    ```

Code retourné en cas d'erreur:

+ Objet non trouvé: 404
+ Erreurs dans les paramètres de la requête: 400

## POST /data-object/v1/objects

Upload un fichier.

+ Requête (application/multipart-form-data)
    + objectName - Nom du fichier
    + file - Fichier à uploader

+ Réponse OK (200)

    ```json
    {
        "objectName": "...",
        "_links": {
            // ...
        }
    }
    ```

Code retourné en cas d'erreur:

+ Fichier vide ou impossible à lire: 422
+ Objet avec le même nom déjà existant: 409
+ Erreurs dans les paramètres de la requête: 400

## DELETE /data-object/v1/objects

Supprime un objet. Si `objectName` n'est pas spécifié, supprime l'objet racine.

+ Paramètres
    + objectName (optionnel) - Nom de l'objet à supprimer
    + recursive (optionnel, défaut `false`) - Supprime récursivement

+ Réponse OK (204)

Code retourné en cas d'erreur:

+ Objet non trouvé: 404
+ Suppression échouée: 422
+ Erreurs dans les paramètres de la requête: 422
