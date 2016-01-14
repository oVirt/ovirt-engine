package org.ovirt.engine.ui.common.widget.action;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.shared.EventBus;

/**
 * Button definition that adapts to UiCommon {@linkplain UICommand commands}.
 *
 * @param <T>
 *            Action panel item type.
 */
public abstract class UiCommandButtonDefinition<T> extends AbstractButtonDefinition<T> {

    /**
     * Null object singleton that represents an empty (no-op) command.
     */
    private static final UICommand EMPTY_COMMAND = new UICommand("Empty", null) { //$NON-NLS-1$
        {
            setIsExecutionAllowed(false);
        }
    };

    private UICommand command;
    private IEventListener<PropertyChangedEventArgs> propertyChangeListener;

    public UiCommandButtonDefinition(EventBus eventBus, String title,
            CommandLocation commandLocation, boolean subTitledAction) {
        super(eventBus, title, commandLocation, subTitledAction);
        update();
    }

    public UiCommandButtonDefinition(EventBus eventBus, String title) {
        this(eventBus, title, CommandLocation.ContextAndToolBar, false);
    }

    public UiCommandButtonDefinition(EventBus eventBus, String title,
            CommandLocation commandLocation) {
        this(eventBus, title, commandLocation, false);
    }

    /**
     * Assigns the given command to this button definition.
     * <p>
     * Triggers {@link InitializeEvent} when the provided command or its property changes.
     * <p>
     * If the given {@code command} is {@code null}, an empty command will be used.
     */
    protected void setCommand(UICommand command) {
        UICommand newCommand = command != null ? command : EMPTY_COMMAND;

        if (this.command != newCommand) {
            // Remove property change handler from current command
            removePropertyChangeEventHandler();

            // Update current command
            this.command = newCommand;
            InitializeEvent.fire(UiCommandButtonDefinition.this);

            // Add property change handler to new command
            if (newCommand != EMPTY_COMMAND) {
                addPropertyChangeEventHandler();
            }
        }
    }

    void addPropertyChangeEventHandler() {
        propertyChangeListener = new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                InitializeEvent.fire(UiCommandButtonDefinition.this);
            }
        };
        command.getPropertyChangedEvent().addListener(propertyChangeListener);
    }

    void removePropertyChangeEventHandler() {
        if (command != null && propertyChangeListener != null) {
            command.getPropertyChangedEvent().removeListener(propertyChangeListener);
            propertyChangeListener = null;
        }
    }

    @Override
    public void releaseAllHandlers() {
        super.releaseAllHandlers();
        removePropertyChangeEventHandler();
    }

    /**
     * Returns the command associated with this button definition.
     * <p>
     * Returning {@code null} is equivalent to returning an empty command.
     */
    protected abstract UICommand resolveCommand();

    @Override
    public void update() {
        // Update command associated with this button definition, this
        // triggers InitializeEvent when command or its property changes
        setCommand(resolveCommand());
    }

    @Override
    public String getUniqueId() {
        return command.getName();
    }

    @Override
    public boolean isAccessible(List<T> selectedItems) {
        return command.getIsAvailable();
    }

    @Override
    public boolean isVisible(List<T> selectedItems) {
        return command != null ? command.getIsVisible() : true;
    }

    @Override
    public boolean isEnabled(List<T> selectedItems) {
        return command.getIsExecutionAllowed();
    }

    @Override
    public void onClick(List<T> selectedItems) {
        command.execute();
    }

}
