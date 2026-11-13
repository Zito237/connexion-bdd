# language: fr
@connexion
Fonctionnalité: Connexion à notre compte GitHub
  Afin d'accéder à nos dépôts et à nos projets
  En tant que membre de l'équipe
  Je veux me connecter à notre compte GitHub avec notre identifiant et notre mot de passe

  Contexte:
    Soit je suis sur la page de connexion de GitHub

  @positif @identifiants-reels
  Scénario: Connexion réussie avec les identifiants de notre compte
    Quand je saisis l'identifiant et le mot de passe de notre compte
    Et je valide le formulaire de connexion
    Alors je suis connecté au compte de notre équipe
    Et je ne suis plus sur la page de connexion

  @negatif
  Plan du scénario: Connexion refusée par GitHub avec des identifiants invalides
    Quand je saisis l'identifiant "<identifiant>" et le mot de passe "<mot_de_passe>"
    Et je valide le formulaire de connexion
    Alors le message d'erreur "Incorrect username or password." est affiché
    Et je reste sur la page de connexion

    Exemples:
      | cas                    | identifiant                   | mot_de_passe        |
      | compte inexistant      | inf563-compte-inexistant-xq7  | MotDePasse#2026     |
      | e-mail inexistant      | inexistant-xq7@inf563.invalid | AutreMotDePasse!99  |

  @negatif @champ-obligatoire
  Plan du scénario: Le formulaire n'est pas envoyé si un champ obligatoire est vide
    Quand je saisis l'identifiant "<identifiant>" et le mot de passe "<mot_de_passe>"
    Et je valide le formulaire de connexion
    Alors le champ "<champ_vide>" est signalé comme obligatoire
    Et je reste sur la page de connexion

    Exemples:
      | identifiant                  | mot_de_passe    | champ_vide   |
      |                              | MotDePasse#2026 | identifiant  |
      | inf563-compte-inexistant-xq7 |                 | mot de passe |
