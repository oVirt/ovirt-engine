package org.ovirt.engine.ui.common.presenter;

import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent;
import org.ovirt.engine.ui.common.utils.ElementTooltipUtils;
import org.ovirt.engine.ui.common.widget.dialog.PopupNativeKeyPressHandler;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
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
         * @return Button used to hide the pop-up view (can be {@code null}).
         */
        HasClickHandlers getCloseButton();

        /**
         * @return The header button (X) that can close the pop-up view.
         */
        HasClickHandlers getCloseIconButton();

        /**
         * Used to handle native key press events that occur within pop-ups.
         */
        HandlerRegistration setPopupKeyPressHandler(PopupNativeKeyPressHandler handler);

        /**
         * @return The handler used to re-position the dialog on Window resize.
         */
        HandlerRegistration getRepositionOnWindowResizeHandler();

    }

    // Number of popups currently active (visible)
    private static int activePopups = 0;

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
            registerHandler(closeButton.addClickHandler(event -> AbstractPopupPresenterWidget.this.onClose()));
        }

        HasClickHandlers closeIconButton = getView().getCloseIconButton();
        if (closeIconButton != null) {
            registerHandler(closeIconButton.addClickHandler(event -> AbstractPopupPresenterWidget.this.onClose()));
        }

        registerHandler(getView().setPopupKeyPressHandler(event -> AbstractPopupPresenterWidget.this.onKeyPress(event)));

        registerHandler(getEventBus().addHandler(UserLoginChangeEvent.getType(), event -> {
            if (isVisible()) {
                getView().hide();
            }
        }));

        registerHandler(getView().getRepositionOnWindowResizeHandler());
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        activePopups++;
    }

    @Override
    protected void onHide() {
        super.onHide();
        activePopups--;

        if (activePopups == 0) {
            // No popups active, reap tooltips attached to popup content.
            Scheduler.get().scheduleDeferred(() -> ElementTooltipUtils.reapPopupContentTooltips());
        }
    }

    public static int getActivePopupCount() {
        return activePopups;
    }

    /**
     * By default, closing the popup will release its registered handlers.
     * <p>
     * Alternatively, override this method to return {@code false} and call
     * {@link #hideAndUnbind} to manually hide and dispose of the popup.
     */
    protected boolean shouldDestroyOnClose() {
        return true;
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
        // Use deferred command to allow GWT Editor framework to update dialog model
        // in case of "Enter" pressed on a specific field; technically this is needed
        // since OVirtBootstrapModal previews native events (happens before GWT event
        // callbacks are executed)
        if (KeyCodes.KEY_ENTER == event.getKeyCode()) {
            Scheduler.get().scheduleDeferred(this::handleEnterKey);
        } else if (KeyCodes.KEY_ESCAPE == event.getKeyCode()) {
            Scheduler.get().scheduleDeferred(this::handleEscapeKey);
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
