# Labelizer

### Auteurs : Stéphane Marengo & Géraud Silvestri

## :warning: Pré-requis

[Java 17](https://adoptium.net/temurin/releases/)

[Maven 3.8](https://maven.apache.org/download.cgi) avec la variable d'environnement `JAVA_HOME` pointant sur le jdk 17.

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

## :wrench: Installation

Pour installer le projet, il suffit de cloner le projet et de lancer la commande suivante depuis sa racine :

```
mvn install
```

Un fichier `.env` doit ensuite être créé à la racine du projet avec les informations suivantes :

```
AWS_ACCESS_KEY_ID= ...
AWS_SECRET_ACCESS_KEY= ...
AWS_BUCKET_NAME= ...
AWS_REGION= ...
```

La region doit être définie selon la colonne `Region` du tableau présenté dans  
[la documentation](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Concepts.RegionsAndAvailabilityZones.html#Concepts.RegionsAndAvailabilityZones.Regions).

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

## :rocket: Lancement

Utilisez les commandes suivantes pour créer le `.jar` et le lancer :

```
mvn package

cd target
java -jar app.jar
```

> :warning: **Il est nécessaire que le fichier `.env` se situe dans le même dossier.**

## Structure du projet

```
Labelizer
│   .env                    # Fichier de configuration
│   .gitignore
│   pom.xml
│   README.md
│
├───.github
│   └───workflows
│           pipeline.yaml   # Défini une action GitHub pour lancer les tests
│
├───target                  # Contient le projet compilé, notamment le .jar
│
└───src
    ├───main
    │   └───java            # Contient les classes du projet
    │
    └───test
        ├───java            # Contient les classes de tests
        └───resources
```

## :roller_coaster: Lancement depuis la machine EC2

Après s'être connecté à l'instance EC2, il faut se rendre dans le dossier du projet :

```
cd Labelizer
```

et lancer la commande suivante pour afficher l'aide :

```
java -jar app.jar -h
```

Exemple de lancement pour tester facilement :

```
java -jar app.jar -m 2 https://upload.wikimedia.org/wikipedia/commons/thumb/4/4d/Cat_November_2010-1a.jpg/1200px-Cat_November_2010-1a.jpg
```