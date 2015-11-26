package org.ovirt.engine.ui.common.auth;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type for {@code userInfo} global JS object.
 */
public final class AutoLoginData extends JavaScriptObject {

    protected AutoLoginData() {
    }

    public static native AutoLoginData instance() /*-{
        return $wnd.userInfo;
    }-*/;

    private native String getId() /*-{
        return this.id;
    }-*/;

    private native String getUserName() /*-{
        return this.userName;
    }-*/;

    private native String getDomain() /*-{
        return this.domain;
    }-*/;

    private native boolean isAdmin() /*-{
        return this.isAdmin;
    }-*/;

    public DbUser getDbUser() {
        DbUser user = new DbUser();
        user.setId(Guid.createGuidFromStringDefaultEmpty(getId()));
        user.setDomain(getDomain());
        user.setLoginName(getUserName());
        user.setAdmin(isAdmin());
        return user;
    }

    public native String getEngineSessionId() /*-{
        return this.engineSessionId;
    }-*/;

    public native String getSsoToken() /*-{
        return this.ssoToken;
    }-*/;

}
