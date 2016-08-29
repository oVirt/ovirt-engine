package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.console.ConsoleOptions;

public abstract class AbstractVnc extends AbstractConsole {

    private ConsoleOptions consoleOptions = new ConsoleOptions(GraphicsType.VNC);

    public ConsoleOptions getOptions() {
        return consoleOptions;
    }

    public void setOptions(ConsoleOptions consoleOptions) {
        this.consoleOptions = consoleOptions;
    }

}
