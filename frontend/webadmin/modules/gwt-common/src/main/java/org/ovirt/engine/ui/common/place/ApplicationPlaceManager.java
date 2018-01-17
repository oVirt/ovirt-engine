package org.ovirt.engine.ui.common.place;

import java.util.Map;
import java.util.logging.Logger;

import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent;
import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent.UserLoginChangeHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.uicommonweb.models.MainModelSelectionChangeEvent;
import org.ovirt.engine.ui.uicommonweb.models.MainModelSelectionChangeEvent.MainModelSelectionChangeHandler;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;

/**
 * Place manager that handles transitions between different places in the application.
 */
public abstract class ApplicationPlaceManager extends PlaceManagerImpl implements UserLoginChangeHandler, MainModelSelectionChangeHandler {

    private static final Logger logger = Logger.getLogger(ApplicationPlaceManager.class.getName());

    protected final ClientAgentType clientAgentType;

    public ApplicationPlaceManager(EventBus eventBus, TokenFormatter tokenFormatter,
            ClientAgentType clientAgentType) {
        super(eventBus, tokenFormatter);
        this.clientAgentType = clientAgentType;

        eventBus.addHandler(UserLoginChangeEvent.getType(), this);
        eventBus.addHandler(MainModelSelectionChangeEvent.getType(), this);
    }

    /**
     * Work around Firefox bug
     * https://bugzilla.mozilla.org/show_bug.cgi?id=519028
     * (change location.hash leads to location bar icon (favicon) disappearance).
     * Simply detach and re-attach the favicon link tag on hashchange.
     */
    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        super.onValueChange(event);
        if (clientAgentType.isFirefox()) {
            Scheduler.get().scheduleDeferred(() -> {
                Node favicon = DOM.getElementById("id-link-favicon");  //$NON-NLS-1$
                if (favicon != null) {
                    Node parent = favicon.getParentNode();
                    favicon.removeFromParent();
                    parent.appendChild(favicon);
                }
            });
        }
    }

    @Override
    public void revealDefaultPlace() {
        revealPlace(getDefaultPlace());
    }

    protected PlaceRequest getDefaultPlace() {
        return getDefaultMainSectionPlace();
    }

    /**
     * Returns the currently valid default "main" section place.
     */
    protected abstract PlaceRequest getDefaultMainSectionPlace();

    @Override
    public void revealErrorPlace(String invalidHistoryToken) {
        logger.warning("Invalid place request - no presenter proxy mapped to '" //$NON-NLS-1$
                + invalidHistoryToken + "'"); //$NON-NLS-1$
        revealDefaultPlace();
    }

    @Override
    public void revealUnauthorizedPlace(String unauthorizedHistoryToken) {
        logger.warning("Unauthorized place request - the user is not allowed to access '" //$NON-NLS-1$
                + unauthorizedHistoryToken + "'"); //$NON-NLS-1$
        revealDefaultPlace();
    }

    @Override
    public void onUserLoginChange(UserLoginChangeEvent event) {
        revealCurrentPlace();
    }

    /**
     * Update the fragment parameters of the current visible place. This will cause a NEW item to be added to the
     * browser history so the back button will take you back to the previous fragment parameters on the same place.
     * @param params key value pairs containing the key and the value of the parameters. Valid keys are defined in
     * {@link FragmentParams}
     */
    public void setFragmentParameters(Map<String, String> params) {
        setFragmentParameters(params, true);
    }

    /**
     * Update the fragment parameters of the current visible place. This will cause a NEW item to be added to the
     * browser history so the back button will take you back to the previous fragment parameters on the same place.
     * @param params key value pairs containing the key and the value of the parameters. Valid keys are defined in
     * {@link FragmentParams}
     * @param newItem true to insert new item into History, false to simply replace current URL without adding
     * to history
     */
    public void setFragmentParameters(Map<String, String> params, boolean newItem) {
        PlaceRequest request = new PlaceRequest.Builder().nameToken(
                getCurrentPlaceRequest().getNameToken()).with(params).build();
        if (newItem) {
            History.newItem(buildHistoryToken(request), false);
        } else {
            History.replaceItem(buildHistoryToken(request), false);
        }
    }

    @Override
    public void onMainModelSelectionChange(MainModelSelectionChangeEvent event) {
        String nameToken = event.getMainModel().getApplicationPlace();
        PlaceRequest placeRequest;

        if (nameToken != null && event.getMainModel().getIsAvailable()) {
            placeRequest = PlaceRequestFactory.get(nameToken);
        } else {
            placeRequest = getDefaultPlace();
        }

        revealPlace(placeRequest);
    }

}
