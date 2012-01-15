package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.webadmin.uicommon.model.DeferredModelCommandInvoker;
import org.ovirt.engine.ui.webadmin.widget.HasEditorDriver;
import org.ovirt.engine.ui.webadmin.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.webadmin.widget.dialog.PopupNativeKeyPressHandler;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

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
public abstract class AbstractModelBoundPopupPresenterWidget<T extends Model, V extends AbstractModelBoundPopupPresenterWidget.ViewDef<T>> extends PresenterWidget<V> {

    public interface ViewDef<T extends Model> extends PopupView, HasEditorDriver<T> {

        void setTitle(String title);

        void setMessage(String message);

        void setItems(Iterable<?> items);

        void setHashName(String name);

        HasUiCommandClickHandlers addFooterButton(String label, String uniqueId);

        void removeButtons();

        void startProgress(String progressMessage);

        void stopProgress();

        void focusInput();

        void setPopupKeyPressHandler(PopupNativeKeyPressHandler handler);

    }

    @Inject
    public AbstractModelBoundPopupPresenterWidget(EventBus eventBus, V view) {
        super(eventBus, view);
    }

    /**
     * Initialize the view from the given model.
     */
    public void init(final T model) {
        // Set common popup properties
        updateTitle(model);
        updateMessage(model);
        updateItems(model);
        updateHashName(model);
        model.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;

                if ("Title".equals(propName)) {
                    updateTitle(model);
                } else if ("Message".equals(propName)) {
                    updateMessage(model);
                } else if ("Items".equals(propName)) {
                    updateItems(model);
                } else if ("HashName".equals(propName)) {
                    updateHashName(model);
                }
            }
        });

        // Add popup footer buttons
        addFooterButtons(model);
        if (model.getCommands() instanceof ObservableCollection) {
            ObservableCollection<UICommand> commands = (ObservableCollection<UICommand>) model.getCommands();
            commands.getCollectionChangedEvent().addListener(new IEventListener() {
                @Override
                public void eventRaised(Event ev, Object sender, EventArgs args) {
                    getView().removeButtons();
                    addFooterButtons(model);
                }
            });
        }

        // Add popup key handlers
        final DeferredModelCommandInvoker commandInvoker = new DeferredModelCommandInvoker(model);
        getView().setPopupKeyPressHandler(new PopupNativeKeyPressHandler() {
            @Override
            public void onKeyPress(NativeEvent event) {
                if (KeyCodes.KEY_ENTER == event.getKeyCode()) {
                    handleEnterKey(commandInvoker);
                } else if (KeyCodes.KEY_ESCAPE == event.getKeyCode()) {
                    handleEscapeKey(commandInvoker);
                }
            }
        });

        // Initialize popup contents from the model
        getView().edit(model);
    }

    protected void handleEnterKey(DeferredModelCommandInvoker commandInvoker) {
        commandInvoker.invokeDefaultCommand();
    }

    protected void handleEscapeKey(DeferredModelCommandInvoker commandInvoker) {
        commandInvoker.invokeCancelCommand();
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

    void updateMessage(T model) {
        getView().setMessage(model.getMessage());
    }

    void updateHashName(T model) {
        getView().setHashName(model.getHashName());
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
                    button.getCommand().Execute();
                }
            }));

        }
    }

    /**
     * Hides the popup and unbinds the presenter widget, removing all handlers registered via {@link #registerHandler}.
     */
    public void hideAndUnbind() {
        getView().hide();
        unbind();
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

}
