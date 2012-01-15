package org.ovirt.engine.ui.webadmin.system;

import java.util.logging.Logger;

import org.ovirt.engine.ui.webadmin.ApplicationMessages;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.AsyncCallFailEvent;
import com.gwtplatform.mvp.client.proxy.AsyncCallFailHandler;

/**
 * Handles cases when GWTP MVP fails to call the server asynchronously.
 * <p>
 * This typically happens when a presenter sitting behind a split point fails to load asynchronously.
 *
 * @see AsyncCallFailEvent
 */
public class AsyncCallFailureHandler implements AsyncCallFailHandler {

    private static final Logger logger = Logger.getLogger(AsyncCallFailureHandler.class.getName());

    private final ErrorPopupManager errorPopupManager;
    private final ApplicationMessages messages;

    @Inject
    public AsyncCallFailureHandler(EventBus eventBus,
            ErrorPopupManager errorPopupManager, ApplicationMessages messages) {
        this.errorPopupManager = errorPopupManager;
        this.messages = messages;
        eventBus.addHandler(AsyncCallFailEvent.getType(), this);
    }

    @Override
    public void onAsyncCallFail(AsyncCallFailEvent event) {
        Throwable caught = event.getCaught();
        logger.warning("Error while performing async call: " + caught.getLocalizedMessage());
        errorPopupManager.show(messages.asyncCallFailure(caught.getLocalizedMessage()));
    }

}
