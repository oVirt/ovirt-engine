package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.console.ConsoleOptions;

public interface ConsoleClient {
    ConsoleOptions getOptions();
    void invokeClient();
}
