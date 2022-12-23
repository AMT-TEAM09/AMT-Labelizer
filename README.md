# Microservices

### Auteurs : Stéphane Marengo & Géraud Silvestri


Ce repository contient 3 sous-projets:

- [Labelizer](./Labelizer/) - microservice permettant d'analyser des images
- [DataObject](./DataObject/) - microservice permettant de gérer des objets
- [SimpleClient](./SimpleClient/) - permet de tester le fonctionnement des deux microservices

## Introduction

Ce projet permet de gérer des objets (fichiers) et d'analyser des images. Il est composé de deux microservices : [DataObject](./DataObject/) et [Labelizer](./Labelizer/).

Les microservices sont dockerisés et peuvent être lancés avec docker-compose.

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

## :roller_coaster: Lancement avec build d'image

La première étape est de créer un fichier `.env` à la racine du projet avec les informations suivantes :

```bash
AWS_ACCESS_KEY_ID= ...
AWS_SECRET_ACCESS_KEY= ...
AWS_BUCKET_NAME= ...
AWS_REGION= ...
```

La region doit être définie selon la colonne `Region` du tableau présenté dans [la documentation](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Concepts.RegionsAndAvailabilityZones.html#Concepts.RegionsAndAvailabilityZones.Regions).

Un paramètre `AWS_URL_DURATION` peut être ajouté pour définir la durée de validité des urls générées (en minutes) par le service `DataObject`. La valeur par défaut est décrite dans le [README](./DataObject/README.md) du service en question.

Pour lancer les microservices, il suffit de lancer la commande suivante :

```bash
docker compose -f docker-compose.local.yml up
```

Cette commande va construire les images des microservices localement et les lancer. Le paramètre `-d` peut être ajouté pour lancer les microservices en arrière-plan.

Les microservices sont ensuite accessibles sur les ports suivants :

- DataObject: 8080
- Labelizer: 8081

Ces ports peuvent être modifiés dans le fichier [docker-compose.local.yml](./docker-compose.local.yml).

Une fois les microservices lancés, il est possible de tester leur fonctionnement avec le [SimpleClient](./SimpleClient/).

## :roller_coaster: Lancement en utilisant les images DockerHub

Un second fichier [docker-compose.yml](./docker-compose.yml) est disponible pour lancer les microservices en utilisant les images qui se trouvent sur DockerHub.

Le reste de la procédure est identique à la précédente.