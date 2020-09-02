package org.ovirt.engine.ui.common.auth;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

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

    private native JavaScriptObject getUserOptions() /*-{
        return this.userOptions || {};
    }-*/;

    public DbUser getDbUser() {
        DbUser user = new DbUser();
        user.setId(Guid.createGuidFromStringDefaultEmpty(getId()));
        user.setDomain(getDomain());
        user.setLoginName(getUserName());
        user.setAdmin(isAdmin());
        user.setUserOptions(deserializeOptions(getUserOptions()));
        return user;
    }

    private Map<String, String> deserializeOptions(JavaScriptObject userOptions) {
        JSONObject options = new JSONObject(getUserOptions());
        Map<String, String> parsedOptions = new HashMap<>();
        for(String key : options.keySet()) {
            JSONString value = options.get(key).isString();
            if (value != null) {
                parsedOptions.put(key, value.stringValue());
            }
        }
        return parsedOptions;
    }

    public native String getEngineSessionId() /*-{
        return this.engineSessionId;
    }-*/;

    public native String getSsoToken() /*-{
        return this.ssoToken;
    }-*/;

}
