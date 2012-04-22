package org.ovirt.engine.ui.common.presenter;

import java.util.logging.Logger;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.uicommon.model.DeferredModelCommandInvoker;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.dialog.PopupNativeKeyPressHandler;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.EventBus;
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

        HasClickHandlers getLoginButton();

        void setPopupKeyPressHandler(PopupNativeKeyPressHandler keyPressHandler);

    }

    private static final Logger logger = Logger.getLogger(AbstractLoginPopupPresenterWidget.class.getName());

    public AbstractLoginPopupPresenterWidget(EventBus eventBus, V view, T loginModel) {
        super(eventBus, view);
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
                logger.warning("Login failed for user [" + loginModel.getUserName().getEntity() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        });

        registerHandler(getView().getLoginButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                loginModel.getLoginCommand().Execute();
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
    }

    protected void onLoggedInEvent(T loginModel) {
        getView().clearErrorMessage();
    }

    @Override
    protected void onReset() {
        super.onReset();

        getView().resetAndFocus();
    }

}
