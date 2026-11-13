package cm.inf563.bdd.support;

import java.util.Optional;

/**
 * Configuration lue dans les variables d'environnement (ou propriétés système -D...).
 *
 * Les identifiants de notre compte ne sont JAMAIS écrits dans le code ni dans le
 * fichier .feature : un dépôt Git garde l'historique, un mot de passe commité est
 * un mot de passe divulgué.
 *
 *   GITHUB_LOGIN     identifiant (ou e-mail) du compte GitHub
 *   GITHUB_PASSWORD  mot de passe du compte
 *   BROWSER          navigateur à utiliser : chrome (défaut), firefox ou edge
 *   HEADLESS         "false" pour voir le navigateur s'ouvrir (défaut : true)
 */
public final class Config {

    public static final String URL_CONNEXION = "https://github.com/login";
    public static final long DELAI_MAX_SECONDES = 15;

    private Config() { }

    public static Optional<String> lire(String cle) {
        String valeur = System.getProperty(cle, System.getenv(cle));
        return (valeur == null || valeur.isBlank()) ? Optional.empty() : Optional.of(valeur);
    }

    public static Optional<String> login() {
        return lire("GITHUB_LOGIN");
    }

    public static Optional<String> motDePasse() {
        return lire("GITHUB_PASSWORD");
    }

    public static boolean headless() {
        return !"false".equalsIgnoreCase(lire("HEADLESS").orElse("true"));
    }

    public static String navigateur() {
        return lire("BROWSER").orElse("chrome").toLowerCase();
    }
}
