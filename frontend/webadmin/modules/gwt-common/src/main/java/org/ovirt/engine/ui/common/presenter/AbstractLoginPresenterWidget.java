package org.ovirt.engine.ui.common.presenter;

import java.util.logging.Logger;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.system.LockInteractionManager;
import org.ovirt.engine.ui.common.uicommon.model.DeferredModelCommandInvoker;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.UriUtils;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 * Base class for login presenter widgets.
 *
 * @param <T>
 *            Login model type.
 * @param <V>
 *            View type.
 */
public abstract class AbstractLoginPresenterWidget<T extends LoginModel, V extends AbstractLoginPresenterWidget.ViewDef<T>> extends PresenterWidget<V> {

    public interface ViewDef<T extends LoginModel> extends View, HasEditorDriver<T> {

        void resetAndFocus();

        void setErrorMessageHtml(SafeHtml text);

        void clearErrorMessage();

        HasUiCommandClickHandlers getLoginButton();

        HasKeyPressHandlers getLoginForm();

        String getMotdAnchorHtml(String url);
    }

    private static final Logger logger = Logger.getLogger(AbstractLoginPresenterWidget.class.getName());

    private final ClientStorage clientStorage;
    private final LockInteractionManager lockInteractionManager;

    private DeferredModelCommandInvoker modelCommandInvoker;

    public AbstractLoginPresenterWidget(EventBus eventBus, V view, T loginModel,
            ClientStorage clientStorage, LockInteractionManager lockInteractionManager) {
        super(eventBus, view);
        this.clientStorage = clientStorage;
        this.lockInteractionManager = lockInteractionManager;
        getView().edit(loginModel);
    }

    @Override
    protected void onBind() {
        super.onBind();

        final T loginModel = getView().flush();

        // Set up model command invoker
        this.modelCommandInvoker = new DeferredModelCommandInvoker(loginModel) {
            @Override
            protected void executeCommand(UICommand command) {
                if (command == loginModel.getLoginCommand()) {
                    beforeLoginCommandExecuted(loginModel);
                }
                super.executeCommand(command);
            }
        };

        loginModel.getLoggedInEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                onLoggedInEvent(loginModel);
            }
        });

        loginModel.getLoginFailedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                lockInteractionManager.hideLoadingIndicator();
                formatAndSetErrorMessage(loginModel.getMessage());
                logger.warning("Login failed for user [" + loginModel.getUserName().getEntity() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            }

        });

        getView().getLoginButton().setCommand(loginModel.getLoginCommand());
        registerHandler(getView().getLoginButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                modelCommandInvoker.invokeDefaultCommand();
            }
        }));

        registerHandler(getView().getLoginForm().addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    modelCommandInvoker.invokeDefaultCommand();
                }
            }
        }));

        // Update selected domain after domain items have been set
        loginModel.getProfile().getPropertyChangedEvent().addListener(new IEventListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (args instanceof PropertyChangedEventArgs &&
                        !"Items".equals(((PropertyChangedEventArgs) args).propertyName)) {//$NON-NLS-1$
                    return;
                }

                String previouslySelectedItem = clientStorage.getLocalItem(getSelectedDomainKey());
                if (previouslySelectedItem == null || "".equals(previouslySelectedItem)) { //$NON-NLS-1$
                    return;
                }

                for (String item : loginModel.getProfile().getItems()) {
                    if (previouslySelectedItem.equals(item)) {
                        loginModel.getProfile().setSelectedItem(item);
                        break;
                    }
                }
            }
        });
    }

    /**
     * Actions taken before executing 'Login' command that initiates UiCommon {@linkplain LoginModel#Login login
     * operation}.
     */
    void beforeLoginCommandExecuted(T loginModel) {
        lockInteractionManager.showLoadingIndicator();
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
        String selectedItem = loginModel.getProfile().getSelectedItem();
        if (selectedItem == null || "".equals(selectedItem)) { //$NON-NLS-1$
            return;
        }

        clientStorage.setLocalItem(getSelectedDomainKey(), selectedItem);
    }

    private void formatAndSetErrorMessage(String message) {
        SafeHtml safeMessage = null;
        if (message != null) {
            SafeHtmlBuilder builder = new SafeHtmlBuilder();
            int urlIndex = message.indexOf("http"); //$NON-NLS-1$
            if (urlIndex != -1) {
                String beforeURL = message.substring(0, urlIndex);
                int afterUrlMessageIndex = message.indexOf(" ", urlIndex); //$NON-NLS-1$
                int endIndex = afterUrlMessageIndex > -1 ? afterUrlMessageIndex : message.length();
                //Sanitize the URL, returns # if it is not safe.
                String url = UriUtils.sanitizeUri(message.substring(urlIndex, endIndex));
                String motdAnchor = getView().getMotdAnchorHtml(url);
                builder.appendEscaped(beforeURL).append(SafeHtmlUtils.fromTrustedString(motdAnchor));
                if (endIndex < message.length()) {
                    //There was a string after the URL append it as well.
                    builder.appendEscaped(message.substring(endIndex));
                }
            } else {
                builder.appendEscaped(message);
            }
            safeMessage = builder.toSafeHtml();
        }
        getView().setErrorMessageHtml(safeMessage);
    }

    @Override
    protected void onReset() {
        super.onReset();

        getView().resetAndFocus();
    }

}
