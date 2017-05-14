package org.ovirt.engine.core.searchbackend;

public class SyntaxCheckerFactory {

    private static String ConfigAuthenticationMethod;
    private static final ISyntaxChecker uiSyntaxChecker = new SyntaxChecker();
    private static ISyntaxChecker backendSyntaxChecker = null;
    private static final ISyntaxChecker adSyntaxChecker = new ADSyntaxChecker();

    // This method is for used in client side by uicommon, which cannot access
    // server side config, so needs to get the auth method as a parameter.

    public static ISyntaxChecker createUISyntaxChecker(String AuthenticationMethod) {
        ConfigAuthenticationMethod = AuthenticationMethod;
        return uiSyntaxChecker;
    }

    public static ISyntaxChecker createBackendSyntaxChecker(String AuthenticationMethod) {
        ConfigAuthenticationMethod = AuthenticationMethod;
        if(backendSyntaxChecker == null) {
            backendSyntaxChecker = new SyntaxChecker();
        }
        return backendSyntaxChecker;
    }

    public static ISyntaxChecker createADSyntaxChecker(String AuthenticationMethod) {
        ConfigAuthenticationMethod = AuthenticationMethod;
        return adSyntaxChecker;
    }

    public static String getConfigAuthenticationMethod() {
        return ConfigAuthenticationMethod;
    }

}
