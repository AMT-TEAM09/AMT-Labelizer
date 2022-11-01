# AMT-TestAws

### Auteurs : Stéphane Marengo & Géraud Silvestri

## Pré-requis

La version 19 de Java est requise pour lancer le projet. Il faut aussi que la variable d'environnement JAVA_HOME soit
définie.

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

## Structure

```
AMT-TestAws
    ├─── .idea
    ├─── src
    │     ├─── main
    │     │     └─── java
    │     │           ├─── impl
    │     │           │     └───aws
    │     │           ├─── interfaces
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
           └─── generated-sources
                  └─── annotations
```