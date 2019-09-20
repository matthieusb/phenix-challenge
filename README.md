# phenix-challenge

[![Build Status](https://travis-ci.org/matthieusb/phenix-challenge.svg?branch=master)](https://travis-ci.org/matthieusb/phenix-challenge)
[![Coverage Status](https://coveralls.io/repos/github/matthieusb/phenix-challenge/badge.svg?branch=master)](https://coveralls.io/github/matthieusb/phenix-challenge?branch=master)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fmatthieusb%2Fphenix-challenge.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fmatthieusb%2Fphenix-challenge?ref=badge_shield)

Solution apportée au projet Carrefour https://github.com/Carrefour-Group/phenix-challenge

## Pré-requis

Pour utiliser ce programme vous devez avoir une JVM au minimum en version 8.

## Utilisation

Récupérez le jar fourni en release et éxécutez le comme suit:

```
java -jar votrejar.jar -i chemin/dossier/donnees/entrantes -o chemin/dossier/donnees/produite
```

**Si vous ne souhaitez pas calculer** les 7 derniers jours, lancez l'argument `-s` (comme simple) comme ceci:

```
java -jar votrejar.jar -i chemin/dossier/donnees/entrantes -o chemin/dossier/donnees/produite -s
```

Le dossier en entrée est **obligatoire**, il doit contenir les fichiers de _transactions_ et de _reference produits_ dont vous souhaitez calculés les indicateurs.
Si vous ne mentionnez pas le dossier en sortie, tout sera créé dans le dossier en cours sous le répertoire `phenix-output``.


Pour le calcul des indicateurs sur les 7 derniers jours, la date du fichier le plus récent est prise en compte pour commencer les calculs, puis sont pris les 6 jours suivants disponibles dans le dossier. Si des jours sont manquants, le calcul se fait quand même normalement.

Si un fichier possède une date qui est supérieure aux 6 jours, il n'est pas pris en compte pour ce calcul.

Si vous avez besoin d'aide sur l'utilisation de la CLI/

```
java -jar votrejar.jar -h
```

**NOTE:** Les fichiers de données doivent être conformes aux exemples et ne pas contenir de variations, sous peine d'une erreur fatale.

## Développement

### Pré-requis

* _Java_ en version 8
* _Scala_ en version 2.12.8 
* _Sbt_ en version 1.2.8

Des fichiers sont disponibles dans le dossier data. Attention à créer des nouveaux dossiers si vous souhaitez ajouter des tests.

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
sbt run -i input/folder -o output/folder
```

Packaging:
```
sbt assembly
```

Note: ajouter "~" devant une commande pour la lancer en mode "watch"

### Simulation de contraintes 

#### Limitation de la RAM

Le fichier `.jvmopts` permet d'avoir une consommation de RAM maximale de 512 Mo pour ce process sur la JVM.

#### Limitation du nombre de threads CPU

*Ces instructions fonctionnent sous Linux Ubuntu 18.04.* Il faut installer la commande `cpulimit` (disponible dans les dépôts ubuntu).

Pour exécuter avec seulement deux CPUs, il faut donner un pourcentage, selon le nombre de threads de la machine. Par exemple pour une machine à 8 threads pour laquelle on veut limiter à 2 threads:

```
cpulimit -l 25 sbt run &
```









## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fmatthieusb%2Fphenix-challenge.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fmatthieusb%2Fphenix-challenge?ref=badge_large)