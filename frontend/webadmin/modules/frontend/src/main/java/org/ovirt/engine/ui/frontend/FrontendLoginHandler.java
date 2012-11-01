package org.ovirt.engine.ui.frontend;

public interface FrontendLoginHandler {

    void onLoginSuccess(String userName, String password, String domain);

    void onLogout();

}
