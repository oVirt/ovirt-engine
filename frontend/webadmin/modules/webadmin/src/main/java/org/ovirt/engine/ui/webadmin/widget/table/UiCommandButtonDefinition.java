package org.ovirt.engine.ui.webadmin.widget.table;

import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.event.logical.shared.HasInitializeHandlers;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * A Button Definition for UICommands that have a text label
 * 
 * @param <T>
 */
public class UiCommandButtonDefinition<T> implements ActionButtonDefinition<T>, HasInitializeHandlers {
    private UICommand command;

    private final SafeHtml title;

    private boolean implemented = true;

    private boolean implInUserPortal;

    private boolean isAvailableOnlyFromContext;

    private final EventBus bus;

    /**
     * create a new UICommand button, with default title
     */
    public UiCommandButtonDefinition(UICommand command) {
        this(command, command.getName());
    }

    /**
     * TODO: This constructor should be cleaned up when webadmin will be fully implemented
     * 
     * @param command
     * @param implemented
     * @param implInUserPortal
     */
    public UiCommandButtonDefinition(UICommand command, boolean implemented, boolean implInUserPortal) {
        this(command, command.getName());
        this.implemented = implemented;
        this.implInUserPortal = implInUserPortal;
    }

    /**
     * create a new UICommand button, with the provided title
     */
    public UiCommandButtonDefinition(UICommand command, String title) {
        this.title = new SafeHtmlBuilder().appendEscaped(title).toSafeHtml();
        this.bus = ClientGinjectorProvider.instance().getEventBus();
        setCommand(command);
    }

    /**
     * create a new UICommand button, with the provided title and 'isAvailableOnlyFromContext' flag
     */
    public UiCommandButtonDefinition(UICommand command, String title, boolean isAvailableOnlyFromContext) {
        this(command, title);
        this.isAvailableOnlyFromContext = isAvailableOnlyFromContext;
    }

    /**
     * TODO: This constructor should be cleaned up when webadmin will be fully implemented
     * 
     * @param command
     * @param title
     * @param implemented
     * @param implInUserPortal
     */
    public UiCommandButtonDefinition(UICommand command, String title, boolean implemented, boolean implInUserPortal) {
        this(command, title);
        this.implemented = implemented;
        this.implInUserPortal = implInUserPortal;
    }

    @Override
    public HandlerRegistration addInitializeHandler(InitializeHandler handler) {
        return bus.addHandler(InitializeEvent.getType(), handler);
    }

    @Override
    public SafeHtml getDisabledHtml() {
        return title;
    }

    @Override
    public SafeHtml getEnabledHtml() {
        return title;
    }

    @Override
    public String getTitle() {
        return title.asString();
    }

    @Override
    public boolean isAccessible() {
        return command.getIsAvailable();
    }

    @Override
    public boolean isEnabled(List<T> selectedItems) {
        return command.getIsExecutionAllowed();
    }

    @Override
    public void onClick(List<T> selectedItems) {
        command.Execute();
    }

    @Override
    public void setAccessible(boolean accessible) {
        // No-op since UICommand availability is managed by UiCommon models
    }

    protected UICommand getCommand() {
        return command;
    }

    protected void setCommand(UICommand command) {
        if (!command.equals(this.command)) {
            this.command = command;
            InitializeEvent.fire(UiCommandButtonDefinition.this);
            // register init events
            command.getPropertyChangedEvent().addListener(new IEventListener() {
                @Override
                public void eventRaised(Event ev, Object sender, EventArgs args) {
                    InitializeEvent.fire(UiCommandButtonDefinition.this);
                }
            });
        }
    }

    @Override
    public boolean isImplemented() {
        return implemented;
    }

    @Override
    public boolean isImplInUserPortal() {
        return implInUserPortal;
    }

    @Override
    public boolean isAvailableOnlyFromContext() {
        return isAvailableOnlyFromContext;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        bus.fireEvent(event);
    }
}
