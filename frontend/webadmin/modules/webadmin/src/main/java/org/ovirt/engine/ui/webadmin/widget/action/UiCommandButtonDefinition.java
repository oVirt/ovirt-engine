package org.ovirt.engine.ui.webadmin.widget.action;

import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.CommonModelChangeEvent;
import org.ovirt.engine.ui.webadmin.uicommon.model.CommonModelChangeEvent.CommonModelChangeHandler;

import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Button definition that adapts to UiCommon {@link UICommand commands}.
 *
 * @param <T>
 *            Action panel item type.
 */
public abstract class UiCommandButtonDefinition<T> implements ActionButtonDefinition<T> {

    /**
     * Null object singleton that represents an empty (no-op) command.
     */
    private static final UICommand EMPTY_COMMAND = new UICommand("Empty", null) {
        {
            setIsAvailable(false);
            setIsExecutionAllowed(false);
        }
    };

    private UICommand command;

    private final SafeHtml title;
    private final EventBus eventBus;

    // Indicates whether the given feature is implemented in WebAdmin
    private final boolean implInWebAdmin;

    // Indicates whether the given feature is implemented in UserPortal
    private final boolean implInUserPortal;

    // Indicates whether the given feature is available only from a context menu
    private final boolean availableOnlyFromContext;

    protected UiCommandButtonDefinition(String title, boolean implInWebAdmin,
            boolean implInUserPortal, boolean availableOnlyFromContext) {
        this.title = SafeHtmlUtils.fromSafeConstant(title);
        this.eventBus = ClientGinjectorProvider.instance().getEventBus();
        this.implInWebAdmin = implInWebAdmin;
        this.implInUserPortal = implInUserPortal;
        this.availableOnlyFromContext = availableOnlyFromContext;
        update();

        // Add handler to be notified when the CommonModel instance changes
        eventBus.addHandler(CommonModelChangeEvent.getType(), new CommonModelChangeHandler() {
            @Override
            public void onCommonModelChange(CommonModelChangeEvent event) {
                update();
            }
        });
    }

    /**
     * Creates a new button with the given title.
     */
    public UiCommandButtonDefinition(String title) {
        this(title, true, false, false);
    }

    /**
     * Creates a new button with the given title.
     * <p>
     * If {@code availableOnlyFromContext} is {@code true}, the button will only be available from the corresponding
     * context menu.
     */
    public UiCommandButtonDefinition(String title, boolean availableOnlyFromContext) {
        this(title, true, false, availableOnlyFromContext);
    }

    /**
     * TODO This constructor will be removed when all WebAdmin features are implemented.
     */
    public UiCommandButtonDefinition(String title, boolean implInWebAdmin, boolean implInUserPortal) {
        this(title, implInWebAdmin, implInUserPortal, false);
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
            this.command = newCommand;
            InitializeEvent.fire(UiCommandButtonDefinition.this);

            // Register property change handler
            if (newCommand != EMPTY_COMMAND) {
                newCommand.getPropertyChangedEvent().addListener(new IEventListener() {
                    @Override
                    public void eventRaised(Event ev, Object sender, EventArgs args) {
                        InitializeEvent.fire(UiCommandButtonDefinition.this);
                    }
                });
            }
        }
    }

    /**
     * Returns the command associated with this button definition.
     * <p>
     * Returning {@code null} is equivalent to returning an empty command.
     */
    protected abstract UICommand resolveCommand();

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
        return eventBus.addHandler(InitializeEvent.getType(), handler);
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
        command.Execute();
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
    public boolean isAvailableOnlyFromContext() {
        return availableOnlyFromContext;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

}
