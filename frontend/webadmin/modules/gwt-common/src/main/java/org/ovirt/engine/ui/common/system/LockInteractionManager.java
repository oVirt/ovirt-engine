package org.ovirt.engine.ui.common.system;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;
import com.gwtplatform.mvp.client.proxy.LockInteractionHandler;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

/**
 * Handles GWTP {@link LockInteractionEvent} by displaying progress indicator when the application is transitioning from
 * one place to another.
 * <p>
 * The progress indicator provides visual feedback that the application is currently busy and any user interaction
 * should be avoided. Note that the progress indicator might have different representations in different sections of the
 * application.
 */
public class LockInteractionManager implements LockInteractionHandler {

    private static final String APP_STATUS_ATTRIBUTE = "data-app-status"; //$NON-NLS-1$
    private static final String APP_STATUS_BUSY = "busy"; //$NON-NLS-1$
    private static final String APP_STATUS_READY = "ready"; //$NON-NLS-1$

    private final PlaceManager placeManager;

    private boolean loadingIndicatorActive = false;

    @Inject
    public LockInteractionManager(EventBus eventBus, PlaceManager placeManager) {
        this.placeManager = placeManager;
        eventBus.addHandler(LockInteractionEvent.getType(), this);
    }

    @Override
    public void onLockInteraction(LockInteractionEvent event) {
        // Allow progress indicator to be hidden regardless of the current (non-empty) place
        boolean emptyPlace = placeManager.getCurrentPlaceRequest().getNameToken() == null;
        if (!emptyPlace && !event.shouldLock()) {
            // Use deferred command because some other initialization might happen
            // right after place transition; therefore we want to hide the loading
            // indicator only after the browser event loop returns
            Scheduler.get().scheduleDeferred(() -> hideLoadingIndicator());
        }

        // Set data attribute on body element to indicate the status of the application
        BodyElement body = Document.get().getBody();
        if (event.shouldLock()) {
            body.setAttribute(APP_STATUS_ATTRIBUTE, APP_STATUS_BUSY);
        } else {
            body.setAttribute(APP_STATUS_ATTRIBUTE, APP_STATUS_READY);
        }
    }

    public void showLoadingIndicator() {
        if (!loadingIndicatorActive) {
            loadingIndicatorActive = true;
            RootPanel.getBodyElement().getStyle().setCursor(Cursor.WAIT);
        }
    }

    public void hideLoadingIndicator() {
        if (loadingIndicatorActive) {
            loadingIndicatorActive = false;
            RootPanel.getBodyElement().getStyle().clearCursor();
        }
    }

}
