package cm.inf563.bdd.pages;

import cm.inf563.bdd.support.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Optional;

/**
 * Page Object de https://github.com/login.
 *
 * Toute la connaissance de la structure HTML (sélecteurs) est ici. Les étapes
 * Gherkin parlent métier (« je saisis l'identifiant… ») ; si GitHub change un id,
 * on ne corrige qu'un seul endroit, pas tous les scénarios.
 *
 * Sélecteurs vérifiés sur la page réelle le 7 novembre 2026.
 */
public class PageConnexion {

    private static final By CHAMP_IDENTIFIANT = By.id("login_field");
    private static final By CHAMP_MOT_DE_PASSE = By.id("password");
    private static final By BOUTON_SIGN_IN = By.cssSelector("input[type='submit'][name='commit']");
    /*
     * Attention : la page contient TOUJOURS un bandeau caché « .flash.flash-error »
     * (message d'erreur AJAX générique). On cible donc uniquement le conteneur des
     * messages du formulaire, et on ne retient qu'un bandeau visible.
     */
    private static final By MESSAGE_ERREUR = By.cssSelector("#js-flash-container .flash-error");
    private static final By META_UTILISATEUR = By.cssSelector("meta[name='user-login']");

    private final WebDriver driver;
    private final WebDriverWait attente;

    public PageConnexion(WebDriver driver) {
        this.driver = driver;
        // Attente BORNÉE sur une condition, jamais Thread.sleep (source de tests instables).
        this.attente = new WebDriverWait(driver, Duration.ofSeconds(Config.DELAI_MAX_SECONDES));
    }

    public PageConnexion ouvrir() {
        driver.get(Config.URL_CONNEXION);
        attente.until(d -> d.findElement(CHAMP_IDENTIFIANT).isDisplayed());
        return this;
    }

    public PageConnexion saisir(String identifiant, String motDePasse) {
        remplir(CHAMP_IDENTIFIANT, identifiant);
        remplir(CHAMP_MOT_DE_PASSE, motDePasse);
        return this;
    }

    /**
     * Clique sur « Sign in » puis attend l'une des trois issues possibles :
     * la page a changé (connexion ou erreur serveur), un message d'erreur est visible,
     * ou le navigateur a bloqué l'envoi parce qu'un champ obligatoire est vide.
     */
    public void valider() {
        String urlAvant = driver.getCurrentUrl();
        driver.findElement(BOUTON_SIGN_IN).click();
        attente.until(d -> !d.getCurrentUrl().equals(urlAvant)
                || messageErreurVisible().isPresent()
                || champInvalide(CHAMP_IDENTIFIANT) || champInvalide(CHAMP_MOT_DE_PASSE));
    }

    /** Texte du bandeau d'erreur, attendu au plus DELAI_MAX_SECONDES. */
    public Optional<String> messageErreur() {
        try {
            return Optional.of(attente.until(d -> messageErreurVisible().orElse(null)));
        } catch (org.openqa.selenium.TimeoutException e) {
            return Optional.empty();
        }
    }

    /** Vrai si le navigateur signale le champ comme obligatoire et vide (validation HTML5 « required »). */
    public boolean champObligatoireVide(String nomChamp) {
        By champ = switch (nomChamp) {
            case "identifiant" -> CHAMP_IDENTIFIANT;
            case "mot de passe" -> CHAMP_MOT_DE_PASSE;
            default -> throw new IllegalArgumentException("Champ inconnu : " + nomChamp);
        };
        return champInvalide(champ);
    }

    /**
     * Identifiant du compte connecté. GitHub l'expose dans
     * &lt;meta name="user-login" content="..."&gt; ; content est vide si personne n'est connecté.
     */
    public Optional<String> utilisateurConnecte() {
        return driver.findElements(META_UTILISATEUR).stream()
                .map(meta -> meta.getAttribute("content"))
                .filter(login -> login != null && !login.isBlank())
                .findFirst();
    }

    /** GitHub peut exiger une 2FA ou une vérification d'appareil après le mot de passe. */
    public boolean verificationSupplementaireDemandee() {
        String url = driver.getCurrentUrl();
        return url.contains("/sessions/two-factor") || url.contains("/sessions/verified-device");
    }

    /**
     * Vrai si GitHub a accepté le couple identifiant / mot de passe.
     *
     * Deux preuves possibles, toutes deux concluantes :
     *  - un utilisateur est connecté (compte sans second facteur) ;
     *  - GitHub réclame un second facteur (2FA ou code appareil) : il ne le fait
     *    QUE lorsque l'identifiant et le mot de passe sont corrects ; un couple
     *    erroné renverrait « Incorrect username or password. ».
     *
     * Traiter le second facteur comme une réussite des identifiants corrige le
     * faux négatif : le logiciel fonctionne (identifiants validés), le test ne
     * doit donc pas échouer parce qu'une étape hors de son ressort reste à faire.
     */
    public boolean identifiantsAcceptes() {
        return utilisateurConnecte().isPresent() || verificationSupplementaireDemandee();
    }

    /** Page « What‽ Your browser did something unexpected » : GitHub a soupçonné un robot. */
    public boolean bloqueParLaProtectionAntiRobot() {
        return driver.getPageSource().contains("Your browser did something unexpected");
    }

    public boolean estSurLaPageDeConnexion() {
        String url = driver.getCurrentUrl();
        return url.contains("/login") || url.endsWith("/session");
    }

    public String urlCourante() {
        return driver.getCurrentUrl();
    }

    // ----- détails techniques -----

    private Optional<String> messageErreurVisible() {
        return driver.findElements(MESSAGE_ERREUR).stream()
                .filter(WebElement::isDisplayed)
                .map(e -> e.getText().trim())
                .filter(texte -> !texte.isEmpty())
                // le bandeau contient aussi le bouton « Dismiss » : on garde la première ligne
                .map(texte -> texte.lines().findFirst().orElse(texte).trim())
                .findFirst();
    }

    private boolean champInvalide(By champ) {
        WebElement element = driver.findElement(champ);
        Object vide = ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].validity.valueMissing;", element);
        return Boolean.TRUE.equals(vide);
    }

    private void remplir(By champ, String valeur) {
        WebElement element = driver.findElement(champ);
        element.clear();
        if (!valeur.isEmpty()) {
            element.sendKeys(valeur);
        }
    }
}
