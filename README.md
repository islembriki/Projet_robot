# Système de Robots Intelligents et Connectés avec Mode Écologique

Un système Java de gestion de robots intelligents avec fonctionnalités écologiques intégrées, interface graphique interactive et simulation de livraison automatisée.
## 📝 Auteur

- **Briki Islem RT2/1**  - Année universitaire 2024/2025

## 📋 À propos du projet

Ce projet implémente une application Java avancée pour la gestion de robots autonomes en respectant les principes de la programmation orientée objet (POO). L'accent est mis sur l'intégration de considérations écologiques dans chaque composant du système, offrant une simulation réaliste et interactive via une interface graphique intuitive.

### 🌟 Caractéristiques principales

- **Hiérarchie de robots** : Architecture à plusieurs niveaux avec classes abstraites et spécialisées
- **Système de simulation de livraison** : Interface graphique pour simuler des missions de livraison
- **Technologies écologiques** : 4 mécanismes d'optimisation énergétique intégrés
- **Interface graphique interactive** : Robot avec yeux suivant le curseur, animations et feedback visuel
- **Gestion avancée d'énergie** : Système KERS (Kinetic Energy Recovery System)
- **Installation facile** : Fichier exécutable disponible via launch4j

## 🏗️ Structure du projet

Le projet est structuré selon une hiérarchie orientée objet rigoureuse :

```
├── Robot (Classe abstraite)
│   ├── RobotConnecte (Classe abstraite)
│   │   └── RobotLivraison
├── Connectable (Interface)
├── Exceptions
│   ├── RobotException
│   ├── EnergieInsuffisanteException
│   └── MaintenanceRequiseException
└── RobotDeliveryGUI (Interface graphique)
```

## 🚀 Installation

### Option 1 : Compilation manuelle
1. Clonez le repository
2. Compilez les fichiers Java avec `javac *.java`
3. Exécutez l'application avec `java RobotDeliveryGUI`

### Option 2 : Exécutable (Recommandé)
1. Téléchargez le fichier exécutable généré avec Launch4j
2. Double-cliquez sur l'exécutable pour lancer l'application directement

> **Note** : L'exécutable offre une installation rapide sans nécessiter Java préinstallé

## 🖥️ Interface graphique

L'interface se compose de trois écrans principaux :

### 1. Écran de Configuration
- Visage de robot interactif dont les yeux suivent le curseur
- Champs pour la configuration (colis, destination, réseau)
- Option pour activer le mode éco-numérique
- Indicateurs d'énergie et système KERS

### 2. Écran de Livraison
- Grille interactive pour déplacer le robot
- Marquage de la destination
- Journal des actions en temps réel
- Indicateurs d'état et contrôles du mode énergétique

### 3. Écran de Fin de Livraison
- Confirmation de la livraison réussie
- Options pour une nouvelle livraison ou terminer

## 🌱 Fonctionnalités écologiques

Le projet intègre quatre mécanismes d'optimisation énergétique :

1. **Mode Veille** (Classe Robot)
   - Activation automatique après 30 secondes d'inactivité
   - Réduction de la consommation d'énergie à 1% par minute
   - Réactivation automatique à la reprise d'activité

2. **Mode Économiseur d'Énergie** (Classe Robot)
   - Activation automatique quand le niveau d'énergie est inférieur à 20%
   - Réduction de 10% de la consommation énergétique pour toutes les opérations
   - Peut être activé/désactivé manuellement

3. **Système KERS** (Classe RobotLivraison)
   - Récupération d'énergie cinétique pendant les déplacements
   - Stockage dans un "super condensateur" intégré
   - Utilisation automatique quand l'énergie principale est épuisée
   - Économie d'énergie proportionnelle à la distance parcourue

4. **Mode Éco-Numérique** (Classe RobotConnecte)
   - Compression des données transmises (60% de réduction)
   - Optimisation de la fréquence des communications réseau
   - Suivi des économies de données réalisées

## 🛡️ Mécanismes de sécurité et de robustesse

- Vérifications préalables avant chaque opération
- Exceptions personnalisées pour différents types d'erreurs
- Historique détaillé des actions avec horodatage
- Arrêt automatique en cas d'épuisement d'énergie

## 🛠️ Technologies utilisées

- **Java** : Langage de programmation principal
- **Swing** : Pour l'interface graphique
- **Launch4j** : Pour la création de l'exécutable
- **DateTimeFormatter** : Pour la gestion des horodatages
- **Timer & TimerTask** : Pour les opérations périodiques



