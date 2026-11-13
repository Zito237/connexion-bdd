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
        // Le hook @Before("@identifiants-reels") a déjà vérifié leur présence.
        page.saisir(Config.login().orElseThrow(), Config.motDePasse().orElseThrow());
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

    @Alors("je suis connecté au compte de notre équipe")
    public void jeSuisConnecte() {
        if (page.verificationSupplementaireDemandee()) {
            fail("Mot de passe accepté, mais GitHub demande une vérification supplémentaire "
                    + "(2FA ou code envoyé par e-mail) : " + page.urlCourante()
                    + ". Utiliser un compte de test sans 2FA, ou automatiser cette étape.");
        }
        String attendu = Config.login().orElseThrow();
        String connecte = page.utilisateurConnecte()
                .orElseThrow(() -> new AssertionError("Aucun utilisateur connecté sur " + page.urlCourante()));
        // GitHub accepte l'e-mail comme identifiant : on ne compare au login que si ce n'en est pas un.
        if (!attendu.contains("@")) {
            assertTrue(connecte.equalsIgnoreCase(attendu),
                    "Connecté en tant que « " + connecte + " » au lieu de « " + attendu + " »");
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
