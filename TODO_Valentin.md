# Personne 1 — Utilisateurs, joueurs, équipes et authentification

## 1. Gestion des utilisateurs

- [X] Créer un utilisateur
    - [X] Username
    - [X] Email
    - [X] Mot de passe
    - [X] Rôle
- [ ] Consulter un utilisateur
- [ ] Lister les utilisateurs
- [X] Rechercher un utilisateur
- [ ] Filtrer les utilisateurs par rôle

## 2. Authentification avec Spring Security

- [X] Mettre en place Spring Security
- [X] Créer l'inscription
- [X] Vérifier l'unicité du username
- [X] Vérifier l'unicité de l'email
- [X] Valider les données d'inscription
- [X] Hasher les mots de passe avec BCrypt
- [X] Créer la connexion
- [X] Créer la déconnexion
- [X] Récupérer l'utilisateur connecté
- [X] Gérer les sessions d'authentification

## 3. Gestion de UserDetails

- [X] Faire implémenter UserDetails à l'utilisateur
- [X] Charger un utilisateur depuis la base de données
- [ ] Gérer les utilisateurs inexistants
- [X] Transformer les rôles en GrantedAuthority
- [X] Récupérer l'utilisateur authentifié dans les controllers

## 4. Gestion des rôles et autorisations

- [X] Créer les rôles
    - [X] USER
    - [X] ADMIN
- [X] Associer un rôle à un utilisateur
- [ ] Modifier le rôle d'un utilisateur
- [X] Vérifier les permissions
- [X] Protéger les endpoints selon les rôles
- [ ] Empêcher l'accès aux ressources non autorisées
- [ ] Vérifier les droits du propriétaire d'une ressource

## 5. Configuration de la sécurité

- [X] Créer SecurityConfig
- [ ] Définir les routes publiques
- [ ] Définir les routes authentifiées
- [ ] Définir les routes réservées aux rôles
- [X] Configurer PasswordEncoder
- [X] Configurer BCrypt
- [X] Configurer le mécanisme de session
- [X] Configurer la gestion des erreurs d'authentification
- [X] Configurer la gestion des accès refusés

## 6. Validation des données d'inscription

- [X] Valider le username
- [X] Valider l'email
- [ ] Valider le mot de passe
- [ ] Vérifier la confirmation du mot de passe
- [X] Vérifier l'unicité du username
- [X] Vérifier l'unicité de l'email

## 7. Gestion des joueurs

- [ ] Modifier un profil joueur (username et email)
- [ ] Supprimer un profil joueur (supprime de l'equipe également + message pour prevenir le capitaine)
- [ ] Consulter un profil joueur (nb de win et username + liste participation des tournois et classements)
- [ ] Rechercher un joueur par pseudo

## 8. Gestion des équipes

- [ ] Créer une équipe (le createur est capitaine)
- [ ] Modifier une équipe (changement du nom et donner le role de capitaine)
- [ ] Supprimer une équipe (! aux equipes inscrites dans des tournois)
- [ ] Profil d'une équipe (lister les joueurs, nb de win ect)
- [ ] Rechercher une équipe
- [ ] Demande pour rejoindre une équipe

## 9. Gestion des membres d'une équipe

- [ ] Envoyer une demande d'ajout à un joueur
- [ ] Retirer un joueur d'une équipe (message au joueur exclu)
- [ ] Consulter les membres d'une équipe (afficher le capitaine)
- [ ] Vérifier qu'un joueur n'est pas déjà membre
- [ ] Vérifier les permissions du capitaine

## 10. Invitations aux équipes

- [ ] Inviter un joueur
- [ ] Accepter une invitation
- [ ] Refuser une invitation
- [ ] Annuler une invitation
- [ ] Consulter ses invitations
- [ ] Gérer le statut des invitations

## 11. Sécurité des équipes

- [ ] Protéger la création d'équipe
- [ ] Protéger la modification d'équipe
- [ ] Protéger la suppression d'équipe
- [ ] Protéger la gestion des membres
- [ ] Vérifier que le capitaine est autorisé
- [ ] Vérifier que l'utilisateur possède les droits nécessaires
- [ ] Empêcher la modification d'une équipe par un utilisateur non autorisé

## 12. Tests de sécurité et d'authentification

- [ ] Tester l'inscription
- [ ] Tester la connexion
- [ ] Tester la déconnexion
- [ ] Tester les mauvais identifiants
- [ ] Tester les utilisateurs inexistants
- [ ] Tester le hashage des mots de passe
- [ ] Tester les rôles
- [ ] Tester les permissions
- [ ] Tester les routes protégées
- [ ] Tester les accès refusés
- [ ] Tester les droits du capitaine
- [ ] Tester les droits administrateur

## 13. Tests utilisateurs, joueurs et équipes

- [ ] Tester la création d'un utilisateur
- [ ] Tester la modification d'un utilisateur
- [ ] Tester la suppression d'un utilisateur
- [ ] Tester la création d'un joueur
- [ ] Tester la modification d'un joueur
- [ ] Tester la création d'une équipe
- [ ] Tester la modification d'une équipe
- [ ] Tester la suppression d'une équipe
- [ ] Tester l'ajout d'un membre
- [ ] Tester la suppression d'un membre
- [ ] Tester les invitations
