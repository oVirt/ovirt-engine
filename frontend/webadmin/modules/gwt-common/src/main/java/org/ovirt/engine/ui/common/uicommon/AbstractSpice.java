package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.ui.common.uicommon.model.AbstractConsoleWithForeignMenu;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public abstract class AbstractSpice extends AbstractConsoleWithForeignMenu {

    protected ConsoleOptions consoleOptions = new ConsoleOptions(GraphicsType.SPICE);

    // long term todo - move these events to plugin impl
    protected Event<EventArgs> disconnectedEvent = new Event<>(SpiceConsoleModel.spiceDisconnectedEventDefinition);
    protected Event<EventArgs> connectedEvent = new Event<>(SpiceConsoleModel.spiceConnectedEventDefinition);
    protected Event<EventArgs> menuItemSelectedEvent = new Event<>(SpiceConsoleModel.spiceMenuItemSelectedEventDefinition);

    public ConsoleOptions getOptions() {
        return consoleOptions;
    }

    public void setOptions(ConsoleOptions consoleOptions) {
        this.consoleOptions = consoleOptions;
    }

    public Event<EventArgs> getDisconnectedEvent() {
        return disconnectedEvent;
    }

    public void setDisconnectedEvent(Event<EventArgs> disconnectedEvent) {
        this.disconnectedEvent = disconnectedEvent;
    }

    public Event<EventArgs> getConnectedEvent() {
        return connectedEvent;
    }

    public void setConnectedEvent(Event<EventArgs> connectedEvent) {
        this.connectedEvent = connectedEvent;
    }

    public Event<EventArgs> getMenuItemSelectedEvent() {
        return menuItemSelectedEvent;
    }

    public void setMenuItemSelectedEvent(Event<EventArgs> menuItemSelectedEvent) {
        this.menuItemSelectedEvent = menuItemSelectedEvent;
    }

}
