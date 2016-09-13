package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.console.ConsoleOptions;

public abstract class AbstractSpice extends AbstractConsole {

    protected ConsoleOptions consoleOptions = new ConsoleOptions(GraphicsType.SPICE);

    public ConsoleOptions getOptions() {
        return consoleOptions;
    }

    public void setOptions(ConsoleOptions consoleOptions) {
        this.consoleOptions = consoleOptions;
    }

}
