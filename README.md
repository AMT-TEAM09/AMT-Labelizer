# AMT-TestAws

### Auteurs : Stéphane Marengo & Géraud Silvestri

## Pré-requis

[Java 17](https://adoptium.net/temurin/releases/)

[Maven 3.8](https://maven.apache.org/download.cgi)

## Installation

Pour installer toutes les dépendances, faire la commande suivante :

```
mvn install
```

Il faut ensuite créer un fichier `.env` à la racine du projet, contenant les informations suivantes :

```
AWS_PROFILE=...
AWS_BUCKET_NAME=...
```

## Paramétrage

Les droits nécessaires minimums du profil sont les suivants :

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

## Tests

Pour compiler et lancer les tests

```
mvn test
```

Pour lancer une classe de tests spécifique

```
mvn -Dtest=NomDeLaClasseDeTest test
```

Pour lancer un test spécifique

```
mvn -Dtest=NomDeLaClasseDeTest#nomDuTest test
```

[Plus d'infos](https://maven.apache.org/surefire/maven-surefire-plugin/examples/single-test.html)

## Lancer l'application
Utilisez les commandes suivantes pour créer l'exécutable `.jar` et le lancer
```
mvn package

java -jar target/*.jar
```

Il est nécessaire que le fichier `.env` soit au même niveau que l'appel de la 2ème commande.

## Structure du projet

```
AMT-TestAws
    ├─── .idea
    ├─── src
    │     ├─── main
    │     │     └─── java
    │     │           ├─── impl
    │     │           │     └───aws
    │     │           ├─── interfaces
    │     │           ├─── models
    │     │           └─── util
    │     └─── test
    │           ├─── java
    │           │     └─── impl
    │           │           └─── aws
    │           └─── resources
    └─── target
           ├─── classes
           │      ├─── impl
           │      │     └─── aws
           │      └─── interfaces
           ├─── generated-sources
           │      └─── annotations
           ├─── generated-test-sources
           │      └─── test-annotations
           └─── test-classes
```
