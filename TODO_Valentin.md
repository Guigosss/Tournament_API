# Personne 1 — Utilisateurs, joueurs, équipes et authentification

## 1. Gestion des utilisateurs

- [ ] Créer un utilisateur
    - [ ] Username
    - [ ] Email
    - [ ] Mot de passe
    - [ ] Rôle
- [ ] Consulter un utilisateur
- [ ] Lister les utilisateurs
- [ ] Rechercher un utilisateur
- [ ] Filtrer les utilisateurs par rôle

## 2. Authentification avec Spring Security

- [ ] Mettre en place Spring Security
- [ ] Créer l'inscription
- [ ] Vérifier l'unicité du username
- [ ] Vérifier l'unicité de l'email
- [ ] Valider les données d'inscription
- [ ] Hasher les mots de passe avec BCrypt
- [ ] Créer la connexion
- [ ] Créer la déconnexion
- [ ] Récupérer l'utilisateur connecté
- [ ] Gérer les sessions d'authentification

## 3. Gestion de UserDetails

- [ ] Faire implémenter UserDetails à l'utilisateur
- [ ] Créer UserDetailsService
- [ ] Charger un utilisateur depuis la base de données
- [ ] Gérer les utilisateurs inexistants
- [ ] Transformer les rôles en GrantedAuthority
- [ ] Récupérer l'utilisateur authentifié dans les controllers

## 4. Gestion des rôles et autorisations

- [ ] Créer les rôles
    - [ ] USER
    - [ ] ADMIN
- [ ] Associer un rôle à un utilisateur
- [ ] Modifier le rôle d'un utilisateur
- [ ] Vérifier les permissions
- [ ] Protéger les endpoints selon les rôles
- [ ] Empêcher l'accès aux ressources non autorisées
- [ ] Vérifier les droits du propriétaire d'une ressource

## 5. Configuration de la sécurité

- [ ] Créer SecurityConfig
- [ ] Configurer SecurityFilterChain
- [ ] Définir les routes publiques
- [ ] Définir les routes authentifiées
- [ ] Définir les routes réservées aux rôles
- [ ] Configurer PasswordEncoder
- [ ] Configurer BCrypt
- [ ] Configurer le mécanisme de session
- [ ] Configurer la gestion des erreurs d'authentification
- [ ] Configurer la gestion des accès refusés

## 6. Validation des données d'inscription

- [ ] Valider le username
- [ ] Valider l'email
- [ ] Valider le mot de passe
- [ ] Vérifier la confirmation du mot de passe
- [ ] Vérifier l'unicité du username
- [ ] Vérifier l'unicité de l'email
- [ ] Créer les validators nécessaires

## 7. Gestion des joueurs

- [ ] Créer un profil joueur
- [ ] Modifier un profil joueur
- [ ] Supprimer un profil joueur
- [ ] Consulter un profil joueur
- [ ] Lister les joueurs
- [ ] Rechercher un joueur
- [ ] Rechercher un joueur par pseudo
- [ ] Associer un joueur à un utilisateur

## 8. Gestion des équipes

- [ ] Créer une équipe
- [ ] Modifier une équipe
- [ ] Supprimer une équipe
- [ ] Consulter une équipe
- [ ] Lister les équipes
- [ ] Rechercher une équipe
- [ ] Gérer le capitaine d'une équipe

## 9. Gestion des membres d'une équipe

- [ ] Ajouter un joueur à une équipe
- [ ] Retirer un joueur d'une équipe
- [ ] Consulter les membres d'une équipe
- [ ] Vérifier qu'un joueur n'est pas déjà membre
- [ ] Changer le capitaine
- [ ] Vérifier les permissions du capitaine
- [ ] Vérifier les droits d'administration

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
