package org.ovirt.engine.ui.common.presenter;

import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.ContextSensitiveHelpManager;
import org.ovirt.engine.ui.common.uicommon.model.DeferredModelCommandInvoker;
import org.ovirt.engine.ui.common.uicommon.model.ModelBoundPopupHandler;
import org.ovirt.engine.ui.common.uicommon.model.ModelBoundPopupResolver;
import org.ovirt.engine.ui.common.utils.WebUtils;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.frontend.communication.AsyncOperationCompleteEvent;
import org.ovirt.engine.ui.frontend.communication.AsyncOperationCompleteEvent.AsyncOperationCompleteHandler;
import org.ovirt.engine.ui.frontend.communication.AsyncOperationStartedEvent;
import org.ovirt.engine.ui.frontend.communication.AsyncOperationStartedEvent.AsyncOperationStartedHandler;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Provider;

/**
 * Base class for popup presenter widgets bound to a UiCommon Window model.
 * <p>
 * It is assumed that each popup presenter widget is bound as non-singleton.
 *
 * @param <T>
 *            Window model type.
 * @param <V>
 *            View type.
 */
public abstract class AbstractModelBoundPopupPresenterWidget<T extends Model, V extends AbstractModelBoundPopupPresenterWidget.ViewDef<T>>
        extends AbstractPopupPresenterWidget<V> implements ModelBoundPopupResolver<T> {

    public interface ViewDef<T extends Model> extends AbstractPopupPresenterWidget.ViewDef, HasEditorDriver<T> {

        void setTitle(String title);

        void setMessage(String message);

        void setItems(Iterable<?> items);

        void setHashName(String name);

        HasUiCommandClickHandlers addFooterButton(String label, String uniqueId);

        void setHelpCommand(UICommand command);

        void removeButtons();

        void startProgress(String progressMessage);

        void stopProgress();

        void focusInput();

        void updateTabIndexes();

        void init(T model);

    }

    private final ModelBoundPopupHandler<T> popupHandler;

    private T model;
    private DeferredModelCommandInvoker modelCommandInvoker;

    private int asyncOperationCounter;

    public AbstractModelBoundPopupPresenterWidget(EventBus eventBus, V view) {
        super(eventBus, view);
        this.popupHandler = new ModelBoundPopupHandler<>(this, eventBus);
    }

    public AbstractModelBoundPopupPresenterWidget(EventBus eventBus, V view,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        this(eventBus, view);
        this.popupHandler.setDefaultConfirmPopupProvider(defaultConfirmPopupProvider);
    }

    @Override
    public String[] getWindowPropertyNames() {
        return new String[] { "Window" }; //$NON-NLS-1$
    }

    @Override
    public Model getWindowModel(T source, String propertyName) {
        return source.getWindow();
    }

    @Override
    public void clearWindowModel(T source, String propertyName) {
        source.setWindow(null);
    }

    @Override
    public String[] getConfirmWindowPropertyNames() {
        return new String[] { "ConfirmWindow" }; //$NON-NLS-1$
    }

    @Override
    public Model getConfirmWindowModel(T source, String propertyName) {
        return source.getConfirmWindow();
    }

    @Override
    public void clearConfirmWindowModel(T source, String propertyName) {
        source.setConfirmWindow(null);
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(T source,
            UICommand lastExecutedCommand, Model windowModel) {
        // No-op, override as necessary
        return null;
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(T source,
            UICommand lastExecutedCommand) {
        // No-op, override as necessary
        return null;
    }

    /**
     * Initialize the view from the given model.
     */
    public void init(final T model) {
        this.model = model;

        getView().init(model);

        // Set up async operation listeners to automatically display/hide progress bar
        asyncOperationCounter = 0;
        addRegisteredHandler(AsyncOperationStartedEvent.getType(), new AsyncOperationStartedHandler() {
            @Override
            public void onAsyncOperationStarted(AsyncOperationStartedEvent event) {
                if (event.getTarget() != getModel() || getModel().getProgress() != null) {
                    return;
                }

                if (asyncOperationCounter == 0) {
                    startProgress(null);
                }
                asyncOperationCounter++;
            }
        });
        addRegisteredHandler(AsyncOperationCompleteEvent.getType(), new AsyncOperationCompleteHandler() {
            @Override
            public void onAsyncOperationComplete(AsyncOperationCompleteEvent event) {
                if (event.getTarget() != getModel() || getModel().getProgress() != null) {
                    return;
                }

                asyncOperationCounter--;
                if (asyncOperationCounter == 0) {
                    stopProgress();
                }
            }
        });

        // Set up model command invoker
        this.modelCommandInvoker = new DeferredModelCommandInvoker(model) {
            @Override
            protected void commandFailed(UICommand command) {
                // Clear Window and ConfirmWindow models when "Cancel" command execution fails
                if (command.getIsCancel() && command.getTarget() instanceof Model) {
                    Model source = (Model) command.getTarget();
                    source.setWindow(null);
                    source.setConfirmWindow(null);
                }
            }

            @Override
            protected void commandFinished(UICommand command) {
                // Enforce popup close after executing "Cancel" command
                if (command.getIsCancel()) {
                    hideAndUnbind();
                }
            }
        };

        // Set common popup properties
        updateTitle(model);
        updateMessage(model);
        updateItems(model);
        updateHashName(model);
        updateHelpTag(model);
        model.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                String propName = args.propertyName;

                if ("Title".equals(propName)) { //$NON-NLS-1$
                    updateTitle(model);
                } else if ("Message".equals(propName)) { //$NON-NLS-1$
                    updateMessage(model);
                } else if ("Items".equals(propName)) { //$NON-NLS-1$
                    updateItems(model);
                } else if ("HashName".equals(propName)) { //$NON-NLS-1$
                    updateHashName(model);
                } else if ("HelpTag".equals(propName)) { //$NON-NLS-1$
                    updateHelpTag(model);
                } else if ("OpenDocumentation".equals(propName)) { //$NON-NLS-1$
                    openDocumentation(model);
                }
            }
        });

        // Add popup footer buttons
        addFooterButtons(model);
        if (model.getCommands() instanceof ObservableCollection) {
            ObservableCollection<UICommand> commands = (ObservableCollection<UICommand>) model.getCommands();
            commands.getCollectionChangedEvent().addListener(new IEventListener<EventArgs>() {
                @Override
                public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                    getView().removeButtons();
                    addFooterButtons(model);
                    getView().updateTabIndexes();
                }
            });
        }

        // Register dialog model property change listener
        popupHandler.addDialogModelListener(model);

        // Initialize popup contents from the model
        getView().edit(model);
        getView().updateTabIndexes();

        if (!model.hasEventBusSet()) {
            model.setEventBus((EventBus) getEventBus());
        }
    }

    @Override
    protected void onClose() {
        // Close button behaves as if the user pressed the Escape key
        handleEscapeKey();
    }

    @Override
    protected void handleEnterKey() {
        getView().flush();
        beforeCommandExecuted(model.getDefaultCommand());
        modelCommandInvoker.invokeDefaultCommand();
    }

    @Override
    protected void handleEscapeKey() {
        getView().flush();
        beforeCommandExecuted(model.getCancelCommand());
        modelCommandInvoker.invokeCancelCommand();
    }

    /**
     * Callback right before any command is executed on the popup.
     */
    protected void beforeCommandExecuted(UICommand command) {
        // No-op, override as necessary
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        // Try to focus some popup input widget
        getView().focusInput();
    }

    void updateTitle(T model) {
        getView().setTitle(model.getTitle());
    }

    protected void updateMessage(T model) {
        getView().setMessage(model.getMessage());
    }

    protected void updateHashName(T model) {
        String hashName = model.getHashName();
        getView().setHashName(hashName);
    }

    protected void updateHelpTag(T model) {
        HelpTag helpTag = model.getHelpTag();
        UICommand command = model.getOpenDocumentationCommand();

        if (command != null && helpTag != null && ContextSensitiveHelpManager.getPath(helpTag.name()) != null) {
            command.setIsAvailable(true);
            getView().setHelpCommand(command); // also sets the help icon visible
        }
    }

    void updateItems(T model) {
        if (model instanceof ListModel) {
            getView().setItems(((ListModel) model).getItems());
        }
    }

    void addFooterButtons(T model) {
        for (int i = model.getCommands().size() - 1; i >= 0; i--) {
            UICommand command = model.getCommands().get(i);
            final HasUiCommandClickHandlers button = getView().addFooterButton(
                    command.getTitle(), command.getName());
            button.setCommand(command);

            // Register command execution handler
            registerHandler(button.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    getView().flush();
                    beforeCommandExecuted(button.getCommand());
                    button.getCommand().execute();
                }
            }));
        }
    }

    /**
     * Shows the popup progress indicator.
     */
    public void startProgress(String progressMessage) {
        getView().startProgress(progressMessage);
    }

    /**
     * Hides the popup progress indicator.
     */
    public void stopProgress() {
        getView().stopProgress();
    }

    /**
     * Open the context-sensitive help for this model. Opens in a new window.
     * ContextSensitiveHelpManager locates the proper URL via helptag/csh mapping file.
     */
    protected void openDocumentation(T model) {
        String helpTag = model.getHelpTag().name();
        String docPath = ContextSensitiveHelpManager.getPath(helpTag);
        String docBase = model.getConfigurator().getDocsBaseUrl();

        WebUtils.openUrlInNewWindow("_blank", docBase + docPath, WebUtils.OPTION_SCROLLBARS); //$NON-NLS-1$
    }

    public T getModel() {
        return model;
    }
}
