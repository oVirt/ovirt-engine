package org.ovirt.engine.ui.webadmin.auth;

import org.ovirt.engine.ui.webadmin.uicommon.model.CommonModelManager;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.inject.Inject;

/**
 * Holds data relevant for the current user.
 * <p>
 * Triggers following events upon certain actions:
 * <ul>
 * <li>{@link UserLoginChangeEvent} when the user logs in or out
 * </ul>
 */
public class CurrentUser implements HasHandlers {

    private final EventBus eventBus;

    private boolean loggedIn = false;
    private String userName;

    // Indicates that the user should be logged in automatically
    private boolean autoLogin = false;

    @Inject
    public CurrentUser(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Returns {@code true} if the user is currently logged in, {@code false} otherwise.
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    /**
     * Returns the user name if the user is currently logged in, {@code null} otherwise.
     */
    public String getUserName() {
        return userName;
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isAutoLogin() {
        return autoLogin;
    }

    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }

    /**
     * Initiates the sign out operation.
     *
     * @see #onUserLogout()
     */
    public void logout() {
        if (isLoggedIn()) {
            CommonModelManager.instance().SignOut();
        }
    }

    /**
     * User login callback, called after successful user authentication.
     */
    public void onUserLogin(String userName) {
        setUserName(userName);
        setLoggedIn(true);
        fireLoginChangeEvent();
    }

    /**
     * User logout callback, called after the user has successfully signed out.
     */
    public void onUserLogout() {
        setUserName(null);
        setLoggedIn(false);
        fireLoginChangeEvent();
    }

    void fireLoginChangeEvent() {
        UserLoginChangeEvent.fire(this);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

}
