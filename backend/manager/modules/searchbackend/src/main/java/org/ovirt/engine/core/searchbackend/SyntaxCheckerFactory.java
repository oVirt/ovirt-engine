package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public class SyntaxCheckerFactory {
    private static final String UISyntaxChecker = "UISyntaxChecker";
    private static final String BackendSyntaxChecker = "BackendSyntaxChecker";
    private static final String ADSyntaxChecker = "ADSyntaxChecker";
    private static final java.util.HashMap<String, ISyntaxChecker> syntaxCheckers =
            new java.util.HashMap<String, ISyntaxChecker>();

    private static String ConfigAuthenticationMethod;

    // This method is for used in client side by uicommon, which cannot access
    // server side config, so needs to get the auth method as a parameter.

    public static ISyntaxChecker CreateUISyntaxChecker(String AuthenticationMethod) {
        ConfigAuthenticationMethod = AuthenticationMethod;
        synchronized (syntaxCheckers) {
            if (!syntaxCheckers.containsKey(UISyntaxChecker)) {
                SyntaxChecker checker = new SyntaxChecker(100, true);
                syntaxCheckers.put(UISyntaxChecker, checker);
            }
            return syntaxCheckers.get(UISyntaxChecker);
        }
    }

    public static ISyntaxChecker CreateBackendSyntaxChecker(String AuthenticationMethod) {
        ConfigAuthenticationMethod = AuthenticationMethod;
        synchronized (syntaxCheckers) {
            if (!syntaxCheckers.containsKey(BackendSyntaxChecker)) {
                SyntaxChecker checker = new SyntaxChecker(Config.<Integer> GetValue(ConfigValues.SearchResultsLimit),
                        true);
                syntaxCheckers.put(BackendSyntaxChecker, checker);
            }
            return syntaxCheckers.get(BackendSyntaxChecker);
        }
    }

    public static ISyntaxChecker CreateADSyntaxChecker(String AuthenticationMethod) {
        ConfigAuthenticationMethod = AuthenticationMethod;
        synchronized (syntaxCheckers) {
            if (!syntaxCheckers.containsKey(ADSyntaxChecker)) {
                ADSyntaxChecker checker = new ADSyntaxChecker();
                syntaxCheckers.put(ADSyntaxChecker, checker);
            }
            return syntaxCheckers.get(ADSyntaxChecker);
        }
    }

    public static String getConfigAuthenticationMethod() {
        return ConfigAuthenticationMethod;
    }
}
