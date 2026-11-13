package cm.inf563.bdd.support;

import org.openqa.selenium.WebDriver;

/**
 * État partagé par les définitions d'étapes et les hooks D'UN SEUL scénario.
 *
 * Cucumber crée une nouvelle instance des classes d'étapes pour chaque scénario.
 * Le hook @Before ouvre un navigateur neuf et le range ici ; le hook @After le
 * ferme et vide ce holder. Chaque scénario a donc son propre navigateur (cookies
 * vierges) : pas d'état partagé -> qualité « I » (Isolated) de F.I.R.S.T.
 */
public final class Contexte {

    private static final ThreadLocal<WebDriver> NAVIGATEUR = new ThreadLocal<>();

    private Contexte() { }

    public static WebDriver navigateur() {
        WebDriver driver = NAVIGATEUR.get();
        if (driver == null) {
            throw new IllegalStateException("Aucun navigateur ouvert : le hook @Before n'a pas été exécuté");
        }
        return driver;
    }

    static void ouvrir(WebDriver driver) {
        NAVIGATEUR.set(driver);
    }

    static WebDriver fermer() {
        WebDriver driver = NAVIGATEUR.get();
        NAVIGATEUR.remove();
        return driver;
    }

    // ----- scénario courant : sert à écrire des messages dans le rapport Cucumber -----

    private static final ThreadLocal<io.cucumber.java.Scenario> SCENARIO = new ThreadLocal<>();

    public static io.cucumber.java.Scenario scenario() {
        return SCENARIO.get();
    }

    static void definirScenario(io.cucumber.java.Scenario scenario) {
        SCENARIO.set(scenario);
    }

    static void oublierScenario() {
        SCENARIO.remove();
    }
}
