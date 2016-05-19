package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.common.CommonApplicationMessages;

public interface ApplicationMessages extends CommonApplicationMessages {
    String consoleWithSmartcard(String console);

    String exceedingCpus(int percentage, int number);

    String exceedingMem(int percentage, String mem);

    String exceedingStorage(int percentage, double gb);
}

