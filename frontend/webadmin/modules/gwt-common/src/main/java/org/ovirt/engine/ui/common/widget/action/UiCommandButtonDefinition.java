package org.ovirt.engine.ui.common.widget.action;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.uicommon.model.UiCommonInitEvent;
import org.ovirt.engine.ui.common.uicommon.model.UiCommonInitEvent.UiCommonInitHandler;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Button definition that adapts to UiCommon {@linkplain UICommand commands}.
 *
 * @param <T>
 *            Action panel item type.
 */
public abstract class UiCommandButtonDefinition<T> implements ActionButtonDefinition<T> {

    /**
     * Null object singleton that represents an empty (no-op) command.
     */
    private static final UICommand EMPTY_COMMAND = new UICommand("Empty", null) { //$NON-NLS-1$
        {
            setIsExecutionAllowed(false);
        }
    };

    protected final EventBus eventBus;

    private UICommand command;
    private IEventListener propertyChangeListener;

    private final List<HandlerRegistration> handlerRegistrations = new ArrayList<HandlerRegistration>();

    private final SafeHtml title;

    private String customToolTip;

    // Indicates whether the given feature is implemented in WebAdmin
    private final boolean implInWebAdmin;

    // Indicates whether the given feature is implemented in UserPortal
    private final boolean implInUserPortal;

    // Indicates whether the given feature is available only from a context menu
    private final CommandLocation commandLocation;

    // Indicates whether this action button has a title action
    private final boolean subTitledAction;

    private final String toolTip;

    protected UiCommandButtonDefinition(EventBus eventBus,
            String title,
            boolean implInWebAdmin,
            boolean implInUserPortal,
            CommandLocation commandLocation,
            boolean subTitledAction, String toolTip) {
        this.eventBus = eventBus;
        this.title = SafeHtmlUtils.fromSafeConstant(title);
        this.implInWebAdmin = implInWebAdmin;
        this.implInUserPortal = implInUserPortal;
        this.commandLocation = commandLocation;
        this.subTitledAction = subTitledAction;
        this.toolTip = toolTip;
        this.customToolTip = "";
        update();

        // Add handler to be notified when UiCommon models are (re)initialized
        registerHandler(eventBus.addHandler(UiCommonInitEvent.getType(), new UiCommonInitHandler() {
            @Override
            public void onUiCommonInit(UiCommonInitEvent event) {
                update();
            }
        }));
    }

    /**
     * Creates a new button with the given title.
     */
    public UiCommandButtonDefinition(EventBus eventBus, String title) {
        this(eventBus, title, true, false, CommandLocation.ContextAndToolBar, false, null);
    }

    /**
     * Creates a new button with the given title.
     * <p>
     * The button will be available from the top tool bar or the corresponding
     * context menu or both, depends on the {@code commandLocation} value.
     */
    public UiCommandButtonDefinition(EventBus eventBus, String title, CommandLocation commandLocation) {
        this(eventBus, title, true, false, commandLocation, false, null);
    }

    /**
     * TODO This constructor will be removed when all WebAdmin features are implemented.
     */
    public UiCommandButtonDefinition(EventBus eventBus, String title, boolean implInWebAdmin, boolean implInUserPortal) {
        this(eventBus, title, implInWebAdmin, implInUserPortal, CommandLocation.ContextAndToolBar, false, null);
    }

    /**
     * Assigns the given command with this button definition.
     * <p>
     * Triggers {@link InitializeEvent} when the provided command reference or its property changes.
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
        propertyChangeListener = new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
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

    /**
     * Returns the command associated with this button definition.
     * <p>
     * Returning {@code null} is equivalent to returning an empty command.
     */
    protected abstract UICommand resolveCommand();

    /**
     * Releases all handlers associated with this button definition.
     */
    public void releaseAllHandlers() {
        removePropertyChangeEventHandler();

        for (HandlerRegistration reg : handlerRegistrations) {
            reg.removeHandler();
        }
        handlerRegistrations.clear();
    }

    void registerHandler(HandlerRegistration reg) {
        handlerRegistrations.add(reg);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The implementation updates the command associated with this button definition.
     */
    @Override
    public void update() {
        setCommand(resolveCommand());
    }

    @Override
    public HandlerRegistration addInitializeHandler(InitializeHandler handler) {
        HandlerRegistration reg = eventBus.addHandler(InitializeEvent.getType(), handler);
        registerHandler(reg);
        return reg;
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
    public String getUniqueId() {
        return command.getName();
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
        command.execute();
    }

    @Override
    public void setAccessible(boolean accessible) {
        // No-op since UICommand availability is managed by UiCommon models
    }

    @Override
    public boolean isImplemented() {
        return implInWebAdmin;
    }

    @Override
    public boolean isImplInUserPortal() {
        return implInUserPortal;
    }

    @Override
    public CommandLocation getCommandLocation() {
        return commandLocation;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    @Override
    public boolean isSubTitledAction() {
        return subTitledAction;
    }

    @Override
    public String getToolTip() {
        return toolTip;
    }

    @Override
    public boolean isVisible(List<T> selectedItems) {
        return (command != null ? command.getIsVisible() : true);
    }

    @Override
    public String getCustomToolTip() {
        return customToolTip;
    }

}
