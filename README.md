# AutiCare - Plateforme Éducative

🌟 **Plateforme de gestion pour l'apprentissage des enfants autistes**

## 📋 Description

AutiCare est une application JavaFX moderne pour gérer les séances d'apprentissage, les rendez-vous avec les psychologues, et les emplois du temps pour les enfants autistes.

## ✨ Fonctionnalités

### 🎯 Modules Principaux

1. **Gestion des Séances** 📚
   - Créer, modifier, supprimer des séances
   - Filtrer par statut (planifiée, confirmé, terminé, annulé, reporté)
   - Recherche avancée
   - Affichage en cartes colorées

2. **Gestion des RDV** 📅
   - Gérer les rendez-vous avec les psychologues
   - Types de consultation : première consultation, suivi, urgence, familiale, bilan
   - Planning détaillé avec durée et statut
   - Interface intuitive avec cartes

3. **Emploi du Temps** 🕐
   - Vue hebdomadaire des activités
   - Liaison entre séances et RDV
   - Organisation par tranche horaire (matin, après-midi, soir)
   - Année scolaire

### 🎨 Interface Moderne

- **Design coloré et enfantin** adapté pour une plateforme éducative
- **Animations fluides** pour une meilleure expérience utilisateur
- **Navigation intuitive** avec sidebar violet
- **Cartes visuelles** au lieu de tableaux classiques
- **Filtres et recherche** en temps réel

## 🚀 Installation

### Prérequis

- Java 17 ou supérieur
- MySQL 8.0 ou supérieur
- Maven 3.6+

### Étapes d'installation

1. **Cloner le projet**
   ```bash
   cd C:\Users\jizel\OneDrive\Bureau\PIDEV-Arwa
   ```

2. **Créer la base de données**
   - Ouvrir MySQL Workbench ou phpMyAdmin
   - Créer une base de données nommée `PIDEV`
   - Exécuter le script `database_schema.sql`
   
   ```sql
   CREATE DATABASE IF NOT EXISTS PIDEV;
   USE PIDEV;
   source database_schema.sql;
   ```

3. **Configurer la connexion**
   - Ouvrir `src/main/java/Utils/Mydatabase.java`
   - Vérifier les paramètres de connexion :
     - URL: `jdbc:mysql://localhost:3306/PIDEV`
     - Username: `root`
     - Password: `` (vide par défaut)

4. **Compiler et exécuter**
   ```bash
   mvn clean install
   mvn javafx:run
   ```
   
   Ou exécuter directement la classe `test.MainFX`

## 📁 Structure du Projet

```
PIDEV-Arwa/
├── src/main/java/
│   ├── Controller/
│   │   ├── HomeController.java
│   │   ├── SeanceManagementController.java
│   │   ├── SeanceFormController.java
│   │   ├── RDVManagementController.java
│   │   └── RDVFormController.java
│   ├── Entites/
│   │   ├── Seance.java
│   │   ├── RDV.java
│   │   └── EmploiDuTemps.java
│   ├── IServices/
│   │   ├── ISeanceServices.java
│   │   ├── IRDVServices.java
│   │   └── IEmploiDuTempsServices.java
│   ├── Services/
│   │   ├── SeanceServices.java
│   │   ├── RDVServices.java
│   │   └── EmploiDuTempsServices.java
│   ├── Utils/
│   │   └── Mydatabase.java
│   └── test/
│       └── MainFX.java
├── src/main/resources/
│   ├── Home.fxml
│   ├── SeanceManagement.fxml
│   ├── SeanceForm.fxml
│   ├── RDVManagement.fxml
│   ├── RDVForm.fxml
│   ├── EmploiManagement.fxml
│   └── styles/
│       └── app.css
├── database_schema.sql
└── pom.xml
```

## 🎨 Palette de Couleurs

- **Violet** (#8b5cf6) - Séances / Principal
- **Bleu** (#2db7f5) - RDV
- **Vert** (#6cda95) - Emploi du temps
- **Rose** (#ff77aa) - Boutons d'édition
- **Rouge** (#ff4757) - Suppression

## 🔧 Technologies Utilisées

- **JavaFX 21** - Interface graphique
- **MySQL** - Base de données
- **Maven** - Gestion des dépendances
- **JDBC** - Connexion à la base de données
- **CSS** - Styling moderne

## 📊 Schéma de Base de Données

### Table `seance`
- id_seance (PK, AUTO_INCREMENT)
- titre_seance
- description
- date_seance
- jours_semaine
- duree (minutes)
- statut_seance (ENUM)
- id_autiste (FK)
- id_professeur (FK)
- id_cours (FK)

### Table `rdv`
- id_rdv (PK, AUTO_INCREMENT)
- type_consultation (ENUM)
- date_heure_rdv
- statut_rdv (ENUM)
- duree_rdv_minutes
- id_psychologue (FK)
- id_autiste (FK)

### Table `emploi_du_temps`
- id_emploi (PK, AUTO_INCREMENT)
- annee_scolaire
- jour_semaine (ENUM)
- tranche_horaire (ENUM)
- id_rdv (FK, nullable)
- id_seance (FK, nullable)

## 👥 Rôles & Permissions

### Admin
- Consulter, ajouter, modifier, supprimer : Séances, RDV, Emploi du temps

### Professeur
- Consulter : Séances, Emploi du temps
- Gérer : Séances (ajouter, modifier, supprimer)

### Psychologue
- Consulter : RDV, Emploi du temps
- Gérer : RDV (ajouter, modifier, supprimer)

### Autiste
- Consulter : Séances, RDV, Emploi du temps (lecture seule)

### Parent
- Consulter : Séances, RDV, Emploi du temps (lecture seule)

## 🐛 Résolution des Problèmes

### Erreur de connexion à la base de données
- Vérifier que MySQL est démarré
- Vérifier les identifiants dans `Mydatabase.java`
- Vérifier que la base `PIDEV` existe

### Erreur de compilation
- Vérifier que Java 17+ est installé : `java -version`
- Nettoyer et recompiler : `mvn clean install`

### Interface ne s'affiche pas correctement
- Vérifier que JavaFX est bien configuré dans le pom.xml
- Vérifier que les fichiers FXML sont dans `src/main/resources/`

## 📝 Validation des Formulaires

Tous les formulaires incluent :
- ✅ Validation des champs obligatoires
- ✅ Validation des formats (numérique, dates)
- ✅ Messages d'erreur clairs
- ✅ Désactivation du bouton "Sauvegarder" si erreurs

## 🎯 Prochaines Étapes

- [ ] Implémenter le système d'authentification
- [ ] Ajouter les permissions par rôle
- [ ] Développer le module Emploi du Temps complet
- [ ] Ajouter des graphiques et statistiques
- [ ] Export PDF des emplois du temps
- [ ] Notifications et rappels

## 📞 Support

Pour toute question ou problème, veuillez créer une issue dans le repository.

## 📄 Licence

Ce projet est développé dans le cadre du PIDEV.

---

**Fait avec ❤️ pour les enfants autistes**

