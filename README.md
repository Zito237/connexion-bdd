# INF563 — BDD : test de la connexion à notre compte GitHub

Le comportement « se connecter à GitHub » est décrit en **Gherkin français**, puis exécuté par
**Cucumber** (Java) qui pilote **Google Chrome** (par défaut), Firefox ou Microsoft Edge avec
**Selenium WebDriver**.
Cours de référence : Séance 9, « BDD : la spécification exécutable » (Gherkin, Cucumber,
définitions d'étapes, organisation Maven).

## Prérequis

- Java 17 ou plus, Maven 3.8 ou plus, Google Chrome installé (ou Firefox / Edge si choisi)
- VS Code : extensions *Extension Pack for Java* et *Cucumber (Gherkin) Full Support*
- Aucun pilote à installer : Selenium Manager télécharge le pilote correspondant au navigateur choisi.

## Lancer les tests

```bash
# 1. Scénarios négatifs seulement (aucun identifiant nécessaire)
mvn test '-Dcucumber.filter.tags=@negatif'

# 2. Tous les scénarios, y compris la connexion réelle à un compte de test.
#    Les identifiants sont OBLIGATOIRES : le scénario positif n'est jamais sauté,
#    il échoue s'ils manquent.
#    Linux / macOS
export GITHUB_LOGIN="votre-login"
export GITHUB_PASSWORD="votre-mot-de-passe"
#    Windows PowerShell
#    $env:GITHUB_LOGIN="notre-login"; $env:GITHUB_PASSWORD="notre-mot-de-passe"
mvn test

# Choisir explicitement le navigateur (Chrome par défaut)
$env:BROWSER="chrome"
# Alternatives : $env:BROWSER="firefox" ou $env:BROWSER="edge"

# Voir le navigateur travailler (conseillé pour la démo)
mvn test -DHEADLESS=false

# N'exécuter qu'une partie des scénarios grâce aux tags
mvn test '-Dcucumber.filter.tags=@negatif'
mvn test '-Dcucumber.filter.tags=@positif'
```

Rapport HTML : `target/cucumber-rapport.html`. En cas d'échec, il contient une capture d'écran.

Le scénario positif s'exécute **toujours** : il n'est jamais ignoré (skipped). Les identifiants
`GITHUB_LOGIN` et `GITHUB_PASSWORD` doivent donc être définis avant de le lancer ; sinon il
**échoue** avec un message explicite demandant de les fournir.

## Arborescence (convention Maven du cours)

```
connexion-bdd/
├── pom.xml
└── src/test/
    ├── java/cm/inf563/bdd/
    │   ├── RunCucumberTest.java        lanceur JUnit 5 (@Suite) : mvn test / VS Code
    │   ├── steps/ConnexionSteps.java   définitions d'étapes (@Soit, @Quand, @Alors)
    │   ├── pages/PageConnexion.java    Page Object : sélecteurs et actions sur github.com/login
    │   └── support/
    │           ├── Hooks.java              @Before / @After : ouvre et ferme le navigateur choisi
    │       ├── Contexte.java           navigateur du scénario courant
    │       └── Config.java             identifiants lus dans les variables d'environnement
    └── resources/features/
        └── connexion_github.feature    la spécification exécutable
```

## Les scénarios (5 exécutions)

| Scénario | Données | Résultat attendu |
| --- | --- | --- |
| Identifiants acceptés (`@positif`) | nos vrais identifiants | GitHub nous connecte **ou** réclame un second facteur (preuve que le couple identifiant/mot de passe est bon), plus sur /login |
| Identifiants refusés, exemple 1 (`@negatif`) | compte inexistant | « Incorrect username or password. » |
| Identifiants refusés, exemple 2 (`@negatif`) | e-mail inexistant | « Incorrect username or password. » |
| Champ obligatoire, exemple 1 | identifiant vide | envoi bloqué, champ identifiant signalé |
| Champ obligatoire, exemple 2 | mot de passe vide | envoi bloqué, champ mot de passe signalé |

Les scénarios négatifs utilisent un **compte inexistant**, jamais notre compte avec un faux mot de
passe : des échecs répétés sur notre vrai compte peuvent le faire bloquer temporairement par GitHub.

## Lien avec le cours

| Notion de la séance 9 | Où elle apparaît |
| --- | --- |
| `# language: fr`, Fonctionnalité, récit « Afin de / En tant que / Je veux » | en-tête du `.feature` |
| Contexte (Background) | `Soit je suis sur la page de connexion de GitHub`, commun à tous les scénarios |
| Soit / Quand / Alors = schéma AAA | chaque scénario |
| Plan du scénario + Exemples = test paramétré | les deux plans négatifs |
| Définition d'étape par phrase type, expressions `{string}` | `ConnexionSteps.java` |
| Le mot-clé ne compte pas (`Et` relié à `@Quand`) | `Et je valide le formulaire de connexion` |
| Niveau de test | test **système / acceptation** de bout en bout (vraie application, vrai réseau) |
| Tests instables : attente bornée, pas de `Thread.sleep` | `WebDriverWait` dans `PageConnexion` |
| Tests instables : environnement fixé | langue `en-US` et taille de fenêtre fixées dans `Hooks` |
| Isolement (F.I.R.S.T.) | un navigateur neuf par scénario, fermé dans `@After` |

## Ce que le test a révélé

En écrivant le premier jet, on supposait qu'un champ vide produisait le message serveur
« Incorrect username or password. ». Sur la vraie page, les deux champs portent l'attribut HTML
`required` : **le navigateur bloque l'envoi** et le serveur ne répond rien. Le comportement a donc
été décrit dans un plan de scénario séparé. C'est l'intérêt du BDD vu en cours : écrire l'exemple
oblige à préciser le comportement attendu.

## Limites à connaître

- **Double authentification (2FA) ou code envoyé par e-mail** : si notre compte les exige, le
  scénario positif ne va pas jusqu'au bureau connecté — mais il **reste vert**, et à juste titre.
  GitHub ne réclame un second facteur qu'après avoir validé l'identifiant et le mot de passe ;
  arriver sur la page 2FA **prouve** donc que nos identifiants sont corrects, ce qui est tout ce
  que ce scénario affirme (« GitHub accepte nos identifiants »). Exiger la connexion complète
  produisait un **faux négatif** : le test échouait alors que le logiciel fonctionnait. Pour
  tester aussi l'étape du second facteur, il faudrait un compte sans 2FA ou automatiser le code
  TOTP — un scénario distinct, non couvert ici.
- **Protection anti-robot** : en mode headless, GitHub peut afficher « What‽ Your browser did
  something unexpected ». Relancer avec `-DHEADLESS=false` depuis votre poste.
- Un test qui dépend d'un site externe reste fragile par nature (le cours le classe parmi les
  causes de tests instables). Il a sa place au sommet de la pyramide des tests, en petit nombre.
- **Facebook** n'a pas été retenu : sa détection d'automatisation bloque presque toujours Selenium,
  ce qui rendrait le test non reproductible.
