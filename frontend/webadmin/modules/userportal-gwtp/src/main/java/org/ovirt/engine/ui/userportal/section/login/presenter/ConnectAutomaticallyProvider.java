package org.ovirt.engine.ui.userportal.section.login.presenter;

import org.ovirt.engine.ui.common.system.ClientStorage;

import com.google.inject.Inject;

/**
 * Encapsulates the access to the connect automatically property
 */
public class ConnectAutomaticallyProvider {

    private static final String LOGIN_AUTOCONNECT_COOKIE_NAME = "Login_ConnectAutomaticallyChecked";

    private final ClientStorage clientStorage;

    @Inject
    public ConnectAutomaticallyProvider(ClientStorage clientStorage) {
        this.clientStorage = clientStorage;
    }

    public void storeConnectAutomatically(boolean connectAutomatically) {
        clientStorage.setLocalItem(LOGIN_AUTOCONNECT_COOKIE_NAME, Boolean.toString(connectAutomatically));
    }

    public boolean readConnectAutomatically() {
        String storedConnectAutomatically = clientStorage.getLocalItem(LOGIN_AUTOCONNECT_COOKIE_NAME);

        // default value is true
        boolean connectAutomatically = true;
        if (storedConnectAutomatically != null && !"".equals(storedConnectAutomatically)) {
            connectAutomatically = Boolean.parseBoolean(storedConnectAutomatically);
        }

        return connectAutomatically;
    }

}
