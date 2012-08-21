package org.ovirt.engine.ui.common.presenter;

import java.util.logging.Logger;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.DeferredModelCommandInvoker;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.common.widget.dialog.PopupNativeKeyPressHandler;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.RootPanel;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 * Base class for login popup presenter widgets.
 *
 * @param <T>
 *            Login model type.
 * @param <V>
 *            View type.
 */
public abstract class AbstractLoginPopupPresenterWidget<T extends LoginModel, V extends AbstractLoginPopupPresenterWidget.ViewDef<T>> extends PresenterWidget<V> {

    public interface ViewDef<T extends LoginModel> extends PopupView, HasEditorDriver<T> {

        void resetAndFocus();

        void setErrorMessage(String text);

        void clearErrorMessage();

        HasUiCommandClickHandlers getLoginButton();

        void setPopupKeyPressHandler(PopupNativeKeyPressHandler keyPressHandler);

    }

    private static final Logger logger = Logger.getLogger(AbstractLoginPopupPresenterWidget.class.getName());

    private final ClientStorage clientStorage;

    public AbstractLoginPopupPresenterWidget(EventBus eventBus, V view, T loginModel, ClientStorage clientStorage) {
        super(eventBus, view);
        this.clientStorage = clientStorage;
        getView().edit(loginModel);
    }

    @Override
    protected void onBind() {
        super.onBind();

        final T loginModel = getView().flush();

        loginModel.getLoggedInEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                onLoggedInEvent(loginModel);
            }
        });

        loginModel.getLoginFailedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getView().setErrorMessage(loginModel.getMessage());
                logger.warning("Login failed for user [" + loginModel.getUserName().getEntity() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        });

        getView().getLoginButton().setCommand(loginModel.getLoginCommand());
        registerHandler(getView().getLoginButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getView().flush();
                getView().getLoginButton().getCommand().Execute();
            }
        }));

        final DeferredModelCommandInvoker commandInvoker = new DeferredModelCommandInvoker(loginModel);
        getView().setPopupKeyPressHandler(new PopupNativeKeyPressHandler() {
            @Override
            public void onKeyPress(NativeEvent event) {
                if (KeyCodes.KEY_ENTER == event.getKeyCode()) {
                    commandInvoker.invokeDefaultCommand();
                }
            }
        });

        // Update selected domain after domain items have been set
        loginModel.getDomain().getPropertyChangedEvent().addListener(new IEventListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (!"Items".equals(((PropertyChangedEventArgs) args).PropertyName)) {//$NON-NLS-1$
                    return;
                }

                String previouslySelectedItem = clientStorage.getLocalItem(getSelectedDomainKey());
                if (previouslySelectedItem == null || "".equals(previouslySelectedItem)) { //$NON-NLS-1$
                    return;
                }

                for (String item : (Iterable<String>) loginModel.getDomain().getItems()) {
                    if (previouslySelectedItem.equals(item)) {
                        loginModel.getDomain().setSelectedItem(item);
                        break;
                    }
                }
            }
        });

        loginModel.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                PropertyChangedEventArgs pcArgs = (PropertyChangedEventArgs) args;

                if ("Progress".equals(pcArgs.PropertyName)) { //$NON-NLS-1$
                    if (loginModel.getProgress() != null) {
                        RootPanel.getBodyElement().getStyle().setCursor(Cursor.WAIT);
                    } else {
                        RootPanel.getBodyElement().getStyle().clearCursor();
                    }
                }
            }
        });
    }



    /**
     * Returns the key used to store and retrieve selected domain from {@link ClientStorage}.
     */
    protected abstract String getSelectedDomainKey();

    protected void onLoggedInEvent(T loginModel) {
        getView().clearErrorMessage();
        saveSelectedDomain(loginModel);
    }

    void saveSelectedDomain(T loginModel) {
        String selectedItem = (String) loginModel.getDomain().getSelectedItem();
        if (selectedItem == null || "".equals(selectedItem)) { //$NON-NLS-1$
            return;
        }

        clientStorage.setLocalItem(getSelectedDomainKey(), selectedItem);
    }

    @Override
    protected void onReset() {
        super.onReset();

        getView().resetAndFocus();
    }

}
