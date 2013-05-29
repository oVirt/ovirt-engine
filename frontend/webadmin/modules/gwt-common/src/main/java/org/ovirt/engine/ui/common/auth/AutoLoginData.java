package org.ovirt.engine.ui.common.auth;

import org.ovirt.engine.core.common.users.VdcUser;
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

    public VdcUser getVdcUser() {
        return new VdcUser(Guid.createGuidFromStringDefaultEmpty(getId()), getUserName(), getDomain());
    }

}
