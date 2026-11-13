package cm.inf563.bdd.support;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.junit.jupiter.api.Assumptions;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Hooks Cucumber : équivalent de @BeforeEach / @AfterEach de JUnit.
 * Ils préparent et nettoient l'environnement autour de CHAQUE scénario.
 */
public class Hooks {

    /**
     * Le scénario positif a besoin de nos vrais identifiants. S'ils ne sont pas
     * fournis, le scénario est IGNORÉ (skipped), pas échoué : ce n'est pas un
     * défaut du logiciel, c'est l'environnement qui n'est pas configuré.
     * order = 0 : exécuté avant l'ouverture du navigateur.
     */
    @Before(value = "@identifiants-reels", order = 0)
    public void exigerIdentifiants() {
        Assumptions.assumeTrue(Config.login().isPresent() && Config.motDePasse().isPresent(),
                "Variables GITHUB_LOGIN et GITHUB_PASSWORD absentes : scénario ignoré");
    }

    @Before(order = 1)
    public void ouvrirNavigateur(Scenario scenario) {
        if (scenario.getSourceTagNames().contains("@identifiants-reels")
                && (Config.login().isEmpty() || Config.motDePasse().isEmpty())) {
            return;
        }

        WebDriver navigateur = switch (Config.navigateur()) {
            case "chrome" -> {
                ChromeOptions options = new ChromeOptions();
                if (Config.headless()) {
                    options.addArguments("--headless=new");
                }
                options.addArguments("--lang=en-US", "--window-size=1280,900",
                        "--no-sandbox", "--disable-dev-shm-usage");
                yield new ChromeDriver(options);
            }
            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions();
                if (Config.headless()) {
                    options.addArguments("--headless");
                }
                options.addArguments("--width=1280", "--height=900");
                options.addPreference("intl.accept_languages", "en-US,en");
                yield new FirefoxDriver(options);
            }
            case "edge" -> {
                EdgeOptions options = new EdgeOptions();
                if (Config.headless()) {
                    options.addArguments("--headless=new");
                }
                options.addArguments("--lang=en-US", "--window-size=1280,900",
                        "--no-sandbox", "--disable-dev-shm-usage");
                options.setExperimentalOption("prefs",
                        java.util.Map.of("intl.accept_languages", "en-US,en"));
                yield new EdgeDriver(options);
            }
            default -> throw new IllegalArgumentException(
                    "Navigateur non pris en charge : " + Config.navigateur()
                            + ". Choisir chrome, firefox ou edge.");
        };
        Contexte.ouvrir(navigateur);
    }

    @After
    public void fermerNavigateur(Scenario scenario) {
        WebDriver driver = Contexte.fermer();
        if (driver == null) {
            return;
        }
        try {
            // Capture d'écran jointe au rapport Cucumber en cas d'échec : aide au diagnostic.
            if (scenario.isFailed()) {
                byte[] capture = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(capture, "image/png", "page au moment de l'échec");
            }
        } finally {
            driver.quit();
        }
    }
}
