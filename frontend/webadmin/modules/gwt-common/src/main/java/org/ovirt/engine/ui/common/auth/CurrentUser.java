package org.ovirt.engine.ui.common.auth;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.utils.FormatUtils;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.inject.Inject;

/**
 * Holds data relevant for the current user.
 * <p>
 * Triggers {@link UserLoginChangeEvent} when the user logs in or out.
 */
public class CurrentUser implements HasHandlers {

    public interface LogoutHandler {

        void onLogout();

        void onSessionExpired();

    }

    private final EventBus eventBus;

    private boolean loggedIn = false;
    private AutoLoginData userInfo;

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

    DbUser getLoggedUser() {
        return Frontend.getInstance().getLoggedInUser();
    }

    /**
     * Returns the user ID of the current user.
     */
    public String getUserId() {
        return getLoggedUser().getId().toString();
    }

    /**
     * Returns full user name ({@code user@domain}) of the current user.
     */
    public String getFullUserName() {
        return FormatUtils.getFullLoginName(getLoggedUser());
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

    public void login() {
        setLoggedIn(true);
    }

    public void logout() {
        if (isLoggedIn() && logoutHandler != null) {
            logoutHandler.onLogout();
        }
    }

    public void sessionExpired() {
        if (logoutHandler != null) {
            logoutHandler.onSessionExpired();
        }
    }

    public void fireLoginChangeEvent() {
        UserLoginChangeEvent.fire(this);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    public void setUserInfo(AutoLoginData userInfo) {
        this.userInfo = userInfo;
    }

    public String getEngineSessionId() {
        return userInfo != null ? userInfo.getEngineSessionId() : null;
    }

    public String getSsoToken() {
        return userInfo != null ? userInfo.getSsoToken() : null;
    }

}
