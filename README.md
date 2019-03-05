# phenix-challenge

[![Build Status](https://travis-ci.org/matthieusb/phenix-challenge.svg?branch=master)](https://travis-ci.org/matthieusb/phenix-challenge)

Solution apportée au projet Carrefour https://github.com/Carrefour-Group/phenix-challenge

## Pré-requis

TODO

## Utilisation

TODO

## Développement

### Commandes sbt

Rappel des commandes sbt pour le développement de l'application.

Compilation:
```
sbt compile test:compile
```

Lancement des tests:
```
sbt test
```

Lancement de l'application:
```
sbt run
```

Packaging:
```
sbt assembly
```

Note: ajouter "~" devant une commande pour la lancer en mode "watch"

### Simulation de contraintes 

#### Limitation de la RAM

Le fichier `.jvmopts` permet d'avoir une consommation de RAM maximale de 512 Mo pour ce process sur le JVM.

#### Limitation du nombre de threads CPU

*Ces instructions fonctionnent sous Linux Ubuntu 18.04.* Il faut installer la commande `cpulimit` (disponible dans les dépôts ubuntu).

Pour exécuter avec seulement deux CPUs, il faut donner un pourcentage, selon le nombre de threads de la machine. Par exemple pour une machine à 8 threads pour laquelle on veut limiter à 2 threads:

```
cpulimit -l 25 sbt run &
```







