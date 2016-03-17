package org.ovirt.engine.ui.common.presenter;

import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent;
import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent.UserLoginChangeHandler;
import org.ovirt.engine.ui.common.widget.dialog.PopupNativeKeyPressHandler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 * Base class for presenter widgets meant to be revealed as popups.
 *
 * @param <V>
 *            Popup view type.
 */
public abstract class AbstractPopupPresenterWidget<V extends AbstractPopupPresenterWidget.ViewDef> extends PresenterWidget<V> {

    public interface ViewDef extends PopupView {

        /**
         * @return Button used to hide the popup view (can be {@code null}).
         */
        HasClickHandlers getCloseButton();

        /**
         * @return Icon button used to hide the popup view (can be {@code null}).
         */
        HasClickHandlers getCloseIconButton();

        /**
         * Used to handle native key press events that occur within popups.
         */
        void setPopupKeyPressHandler(PopupNativeKeyPressHandler handler);

    }

    // Indicates whether the handlers registered by the popup have been released
    private boolean destroyed = false;

    public AbstractPopupPresenterWidget(EventBus eventBus, V view) {
        super(eventBus, view);
    }

    @Override
    protected void onBind() {
        super.onBind();

        HasClickHandlers closeButton = getView().getCloseButton();
        if (closeButton != null) {
            registerHandler(closeButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    AbstractPopupPresenterWidget.this.onClose();
                }
            }));
        }

        HasClickHandlers closeIconButton = getView().getCloseIconButton();
        if (closeIconButton != null) {
            registerHandler(closeIconButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    AbstractPopupPresenterWidget.this.onClose();
                }
            }));
        }

        getView().setPopupKeyPressHandler(new PopupNativeKeyPressHandler() {
            @Override
            public void onKeyPress(NativeEvent event) {
                AbstractPopupPresenterWidget.this.onKeyPress(event);
            }
        });

        registerHandler(getEventBus().addHandler(UserLoginChangeEvent.getType(), new UserLoginChangeHandler() {
            @Override
            public void onUserLoginChange(UserLoginChangeEvent event) {
                if (isVisible()) {
                    getView().hide();
                }
            }
        }));
    }

    /**
     * By default, closing the popup doesn't release its registered handlers.
     * <p>
     * Non-singleton popup presenter widgets <em>should</em> override this method
     * and return {@code true} to ensure proper handler disposal. Alternatively,
     * use {@link #hideAndUnbind} to manually hide and dispose of the popup.
     */
    protected boolean shouldDestroyOnClose() {
        return false;
    }

    /**
     * Close action callback, hides the popup view by default.
     */
    protected void onClose() {
        if (shouldDestroyOnClose()) {
            hideAndUnbind();
        } else {
            getView().hide();
        }
    }

    /**
     * Hides the popup view and releases all handlers registered via {@link #registerHandler}.
     * <p>
     * This method is applicable to non-singleton presenter widgets only.
     */
    public void hideAndUnbind() {
        if (!destroyed) {
            getView().hide();
            unbind();
            destroyed = true;
        }
    }

    protected void onKeyPress(NativeEvent event) {
        if (KeyCodes.KEY_ENTER == event.getKeyCode()) {
            handleEnterKey();
        } else if (KeyCodes.KEY_ESCAPE == event.getKeyCode()) {
            handleEscapeKey();
            event.preventDefault();
        }
    }

    /**
     * Does nothing by default.
     */
    protected void handleEnterKey() {
        // No-op, override as necessary
    }

    /**
     * Calls {@link #onClose} by default.
     */
    protected void handleEscapeKey() {
        onClose();
    }

}
