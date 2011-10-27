package org.ovirt.engine.ui.uicompat;

public class UriValidator {
    public static boolean IsValid(String uri) {
        // TODO: Implement URL validation via regexp or find a GWT supported method if there is one
        // throw new RuntimeException("org.ovirt.engine.ui.uicompat.UriValidator is not supported!");
        return true;
        /*
         * try { URI newUri = new URI("http://" + uri); if (newUri.getHost().equals(uri)){ return true; } return false;
         * } catch (URISyntaxException e) { return false; }
         */
    }
}
