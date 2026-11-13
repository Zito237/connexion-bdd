package cm.inf563.bdd.steps;

import cm.inf563.bdd.pages.PageConnexion;
import cm.inf563.bdd.support.Config;
import cm.inf563.bdd.support.Contexte;
import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Définitions d'étapes : une méthode Java annotée par phrase type du fichier .feature.
 *
 * Rappel du cours : le mot-clé (Soit / Quand / Et / Alors) ne compte pas pour la
 * correspondance. « Et je valide le formulaire de connexion » est relié à la
 * méthode annotée @Quand, car seul le texte après le mot-clé est comparé.
 */
public class ConnexionSteps {

    private PageConnexion page;

    // ----- Soit : le contexte (Arrange) -----

    @Soit("je suis sur la page de connexion de GitHub")
    public void jeSuisSurLaPageDeConnexion() {
        page = new PageConnexion(Contexte.navigateur()).ouvrir();
    }

    // ----- Quand : l'action (Act) -----

    @Quand("je saisis l'identifiant et le mot de passe de notre compte")
    public void jeSaisisNosIdentifiants() {
        String login = Config.login().orElseThrow(() -> new AssertionError(
                "Variables GITHUB_LOGIN et GITHUB_PASSWORD absentes : définir ces identifiants "
                        + "avant de lancer le scénario positif."));
        String motDePasse = Config.motDePasse().orElseThrow(() -> new AssertionError(
                "Variable GITHUB_PASSWORD absente : définir le mot de passe du compte."));
        page.saisir(login, motDePasse);
    }

    /** {string} est une expression Cucumber : elle extrait le texte entre guillemets (éventuellement vide). */
    @Quand("je saisis l'identifiant {string} et le mot de passe {string}")
    public void jeSaisis(String identifiant, String motDePasse) {
        page.saisir(identifiant, motDePasse);
    }

    @Quand("je valide le formulaire de connexion")
    public void jeValide() {
        page.valider();
    }

    // ----- Alors : le résultat attendu (Assert) -----

    @Alors("GitHub accepte nos identifiants")
    public void gitHubAccepteNosIdentifiants() {
        if (page.bloqueParLaProtectionAntiRobot()) {
            fail("GitHub a affiché sa page anti-robot (« What‽ ») au lieu de traiter la connexion : "
                    + "relancer avec HEADLESS=false depuis votre poste.");
        }
        // Oracle juste : GitHub a accepté le couple identifiant / mot de passe s'il
        // nous connecte OU s'il réclame un second facteur (qu'il n'exige jamais sur
        // des identifiants erronés). Exiger la connexion complète produisait un faux
        // négatif quand la 2FA est active alors que le logiciel fonctionne.
        assertTrue(page.identifiantsAcceptes(),
                "GitHub n'a pas accepté nos identifiants (ni connexion, ni demande de second facteur) sur "
                        + page.urlCourante());

        // Dès que GitHub réclame un second facteur, on l'AFFICHE dans le rapport :
        // identifiants validés, mais la connexion complète exige une étape 2FA / code
        // appareil hors du périmètre de ce test. Le scénario reste vert (pas de faux négatif).
        if (page.verificationSupplementaireDemandee()) {
            io.cucumber.java.Scenario scenario = Contexte.scenario();
            String message = "⚠ Identifiants acceptés par GitHub, mais un second facteur est "
                    + "demandé après l'identifiant et le mot de passe (2FA ou code appareil) : "
                    + page.urlCourante()
                    + ". La connexion complète nécessiterait de saisir ce code (hors périmètre de ce test).";
            if (scenario != null) {
                scenario.log(message);
            } else {
                System.out.println(message);
            }
        }

        // Si nous sommes effectivement connectés, vérifier que c'est bien notre compte.
        String attendu = Config.login().orElseThrow();
        if (!attendu.contains("@")) {
            page.utilisateurConnecte().ifPresent(connecte ->
                    assertTrue(connecte.equalsIgnoreCase(attendu),
                            "Connecté en tant que « " + connecte + " » au lieu de « " + attendu + " »"));
        }
    }

    @Alors("je ne suis plus sur la page de connexion")
    public void jeNeSuisPlusSurLaPageDeConnexion() {
        assertFalse(page.estSurLaPageDeConnexion(), "Toujours sur " + page.urlCourante());
    }

    @Alors("le message d'erreur {string} est affiché")
    public void leMessageDErreurEstAffiche(String attendu) {
        if (page.bloqueParLaProtectionAntiRobot()) {
            fail("GitHub a affiché sa page anti-robot (« What‽ ») au lieu du formulaire : "
                    + "relancer avec HEADLESS=false depuis votre poste.");
        }
        String obtenu = page.messageErreur()
                .orElseThrow(() -> new AssertionError("Aucun message d'erreur affiché sur " + page.urlCourante()));
        assertEquals(attendu, obtenu);
    }

    @Alors("le champ {string} est signalé comme obligatoire")
    public void leChampEstSignaleObligatoire(String champ) {
        assertTrue(page.champObligatoireVide(champ),
                "Le navigateur ne signale pas le champ « " + champ + " » comme obligatoire");
    }

    @Alors("je reste sur la page de connexion")
    public void jeResteSurLaPageDeConnexion() {
        assertTrue(page.estSurLaPageDeConnexion(), "On a quitté la page de connexion : " + page.urlCourante());
        assertTrue(page.utilisateurConnecte().isEmpty(), "Un utilisateur est connecté alors qu'il ne devrait pas l'être");
    }
}
