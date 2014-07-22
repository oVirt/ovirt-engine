package org.ovirt.engine.ui.common.auth;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.inject.Inject;
import org.ovirt.engine.ui.frontend.utils.FormatUtils;

/**
 * Holds data relevant for the current user.
 * <p>
 * Triggers {@link UserLoginChangeEvent} when the user logs in or out.
 */
public class CurrentUser implements HasHandlers {

    public interface LogoutHandler {

        void onLogout();

    }

    private final EventBus eventBus;

    private boolean loggedIn = false;
    private DbUser loggedUser;

    // Indicates that the user should be logged in automatically
    private boolean autoLogin = false;

    private LogoutHandler logoutHandler;

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

    void setLoggedUser(DbUser loggedUser) {
        this.loggedUser = loggedUser;
    }

    /**
     * Returns the user name if the user is currently logged in, {@code null} otherwise.
     */
    public String getUserName() {
        return isLoggedIn() ? loggedUser.getLoginName() : null;
    }

    /**
     * Returns the user authentication domain if the user is currently logged in, {@code null} otherwise.
     */
    public String getDomain() {
        return isLoggedIn() ? loggedUser.getDomain() : null;
    }

    /**
     * Returns the user ID if the user is currently logged in, {@code null} otherwise.
     */
    public String getUserId() {
        return isLoggedIn() ? loggedUser.getId().toString() : null;
    }

    /**
     * Returns full user name ({@code user@domain}) if the user is currently logged in, {@code null} otherwise.
     */
    public String getFullUserName() {
        return isLoggedIn()? FormatUtils.getFullLoginName(loggedUser): null;
    }

    public boolean isAutoLogin() {
        return autoLogin;
    }

    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }

    public void setLogoutHandler(LogoutHandler logoutHandler) {
        this.logoutHandler = logoutHandler;
    }

    /**
     * Initiates the sign out operation.
     *
     * @see #onUserLogout()
     */
    public void logout() {
        if (isLoggedIn() && logoutHandler != null) {
            logoutHandler.onLogout();
        }
    }

    /**
     * User login callback, called after successful user authentication.
     */
    public void onUserLogin(DbUser loggedUser) {
        setLoggedUser(loggedUser);
        setLoggedIn(true);
        fireLoginChangeEvent();
    }

    /**
     * User logout callback, called after the user has successfully signed out.
     */
    public void onUserLogout() {
        setLoggedUser(null);
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
