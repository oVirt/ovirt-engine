package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.common.CommonApplicationMessages;

public interface ApplicationMessages extends CommonApplicationMessages {

    @DefaultMessage("{0} with Smartcard")
    String consoleWithSmartcard(String console);

    @DefaultMessage("Exceeded {0}% / {1}vCPU")
    String exceedingCpus(int percentage, int number);

    @DefaultMessage("Exceeded {0}% / {1}")
    String exceedingMem(int percentage, String mem);

    @DefaultMessage("Exceeded {0}% / {1}GB")
    String exceedingStorage(int percentage, double gb);

    @DefaultMessage("Host {0} was filtered out by {1} filter {2}")
    String schedulingHostFilteredOutReason(String host, String filterLocation, String filterName);
}
