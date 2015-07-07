package org.ovirt.engine.ui.userportal.section.login.presenter;

import org.ovirt.engine.ui.common.system.ClientStorage;
import com.google.inject.Inject;

/**
 * Encapsulates the access to the connect automatically property
 */
public class ConnectAutomaticallyProvider {

    // Determines if the application should automatically open console for a running VM upon login
    private static final String LOGIN_AUTOCONNECT = "Login_ConnectAutomaticallyChecked"; //$NON-NLS-1$

    private final ClientStorage clientStorage;

    @Inject
    public ConnectAutomaticallyProvider(ClientStorage clientStorage) {
        this.clientStorage = clientStorage;
    }

    public void storeConnectAutomatically(boolean connectAutomatically) {
        clientStorage.setLocalItem(LOGIN_AUTOCONNECT, Boolean.toString(connectAutomatically));
    }

    public boolean readConnectAutomatically() {
        String storedConnectAutomatically = clientStorage.getLocalItem(LOGIN_AUTOCONNECT);

        // Default value is true
        boolean connectAutomatically = true;
        if (storedConnectAutomatically != null && !"".equals(storedConnectAutomatically)) { //$NON-NLS-1$
            connectAutomatically = Boolean.parseBoolean(storedConnectAutomatically);
        }

        return connectAutomatically;
    }

}
