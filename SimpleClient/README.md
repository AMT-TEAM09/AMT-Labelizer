# SimpleClient

### Auteurs : Stéphane Marengo & Géraud Silvestri

## :warning: Pré-requis

[Java 17](https://adoptium.net/temurin/releases/)

[Maven 3.8](https://maven.apache.org/download.cgi) avec la variable d'environnement `JAVA_HOME` pointant sur le jdk 17.

## Récupération les dépendances

Pour récupérer la liste des dépendances, il faut lancer la commande suivante à la racine du projet :

```
mvn dependency:resolve
```

## :wrench: Installation

Pour installer le projet, il suffit de cloner le projet et de lancer la commande suivante depuis sa racine :

```
mvn install
```

Un fichier `.env` doit ensuite être créé à la racine du projet avec les informations suivantes :

```
DATA_OBJECT_URI= ...
ANALYZER_URI= ...
```

Le fichier [`.env.example`](./.env.example) peut être utilisé comme modèle.

## :rocket: Lancement

Lancez les microservices [DataObject](../DataObject) et [Labelizer](../Labelizer) comme indiqué dans le [README racine](../README.md).

Utilisez les commandes suivantes pour créer le `.jar` et le lancer :

```
mvn package

java -jar target\SimpleClient.jar
```

Par défaut, tous les scénarios de tests sont lancés. Pour lancer un scénario en particulier, il suffit de lancer la commande suivante :

```
java -jar SimpleClient.jar <scenario>
```

Scénarios disponibles :

1. tout est nouveau
2. seul le bucket existe
3. tout existe


> :warning: **Les microservices doivent être en cours d'exécution**