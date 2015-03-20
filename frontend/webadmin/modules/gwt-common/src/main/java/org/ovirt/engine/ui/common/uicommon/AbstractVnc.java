package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.ui.common.uicommon.model.AbstractConsoleWithForeignMenu;

public abstract class AbstractVnc extends AbstractConsoleWithForeignMenu {

    private ConsoleOptions consoleOptions = new ConsoleOptions(GraphicsType.VNC);

    public ConsoleOptions getOptions() {
        return consoleOptions;
    }

    public void setOptions(ConsoleOptions consoleOptions) {
        this.consoleOptions = consoleOptions;
    }

}
