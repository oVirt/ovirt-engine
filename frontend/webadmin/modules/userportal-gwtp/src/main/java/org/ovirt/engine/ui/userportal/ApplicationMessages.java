package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.common.CommonApplicationMessages;

public interface ApplicationMessages extends CommonApplicationMessages {

    @DefaultMessage("Select Console for ''{0}''")
    String selectConsoleFor(String name);

    @DefaultMessage("Error Connecting to {0}. This browser does not support {1} protocol")
    String errorConnectingToConsole(String name, String protocol);

    @DefaultMessage("{0} with Smartcard")
    String consoleWithSmartcard(String console);

    @DefaultMessage("Exceeded {0}% / {1}vCPU")
    String exceedingCpus(int percentage, int number);

    @DefaultMessage("Exceeded {0}% / {1}")
    String exceedingMem(int percentage, String mem);

    @DefaultMessage("Exceeded {0}% / {1}GB")
    String exceedingStorage(int percentage, double gb);
}
