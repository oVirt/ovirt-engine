package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public class SyntaxCheckerFactory {

    private static String ConfigAuthenticationMethod;
    private static final ISyntaxChecker uiSyntaxChecker = new SyntaxChecker(100, true);
    private static ISyntaxChecker backendSyntaxChecker = null;
    private static final ISyntaxChecker adSyntaxChecker = new ADSyntaxChecker();

    // This method is for used in client side by uicommon, which cannot access
    // server side config, so needs to get the auth method as a parameter.

    public static ISyntaxChecker CreateUISyntaxChecker(String AuthenticationMethod) {
        ConfigAuthenticationMethod = AuthenticationMethod;
        return uiSyntaxChecker;
    }

    public static ISyntaxChecker CreateBackendSyntaxChecker(String AuthenticationMethod) {
        ConfigAuthenticationMethod = AuthenticationMethod;
        if(backendSyntaxChecker == null) {
            backendSyntaxChecker = new SyntaxChecker(Config.<Integer> GetValue(ConfigValues.SearchResultsLimit),
                    true);
        }
        return backendSyntaxChecker;
    }

    public static ISyntaxChecker CreateADSyntaxChecker(String AuthenticationMethod) {
        ConfigAuthenticationMethod = AuthenticationMethod;
        return adSyntaxChecker;
    }

    public static String getConfigAuthenticationMethod() {
        return ConfigAuthenticationMethod;
    }
}
