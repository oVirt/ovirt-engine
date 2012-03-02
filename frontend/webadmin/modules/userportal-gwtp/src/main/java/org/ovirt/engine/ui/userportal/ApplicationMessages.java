package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.common.CommonApplicationMessages;

public interface ApplicationMessages extends CommonApplicationMessages {

    @DefaultMessage("Select Console for ''{0}''")
    String selectConsoleFor(String name);

    @DefaultMessage("Error Connecting to {0}. This browser does not support {1} protocol")
    String errorConnectingToConsole(String name, String protocol);
}
