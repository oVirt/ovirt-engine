package org.ovirt.engine.ui.common.widget.action;

import static java.util.Collections.emptyList;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.UICommand;
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
public abstract class UiCommandButtonDefinition<E, T> extends AbstractButtonDefinition<E, T> {

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

    public UiCommandButtonDefinition(EventBus eventBus, String title, boolean subTitledAction) {
        this(eventBus, title, subTitledAction, emptyList());
    }

    public UiCommandButtonDefinition(EventBus eventBus, String title) {
        this(eventBus, title, false);
    }

    public UiCommandButtonDefinition(EventBus eventBus,
            String title,
            boolean subTitledAction,
            List<ActionButtonDefinition<E, T>> subActions) {
        super(eventBus, title, subTitledAction, subActions);
        update();
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
        propertyChangeListener = (ev, sender, args) -> InitializeEvent.fire(UiCommandButtonDefinition.this);
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
    public boolean isAccessible(E mainEntity, List<T> selectedItems) {
        return command.getIsAvailable();
    }

    @Override
    public boolean isVisible(E mainEntity, List<T> selectedItems) {
        return command == null || command.getIsVisible();
    }

    @Override
    public boolean isEnabled(E mainEntity, List<T> selectedItems) {
        return command.getIsExecutionAllowed();
    }

    @Override
    public void onClick(E mainEntity, List<T> selectedItems) {
        command.execute();
    }

    @Override
    public int getIndex() {
        // Place this button after all existing buttons
        return Integer.MAX_VALUE;
    }

}
