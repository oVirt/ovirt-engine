package org.ovirt.engine.ui.uicommonweb;

import com.google.gwt.core.client.JavaScriptObject;

public final class ReportsUrls extends JavaScriptObject {

    protected ReportsUrls() {
    }

    public static native ReportsUrls instance() /*-{
        return $wnd.engineReportsUrls;
    }-*/;

    public native String getReportUrl() /*-{
        return this.reportUrl;
    }-*/;

    public native String getRightClickUrl() /*-{
        return this.rightClickUrl;
    }-*/;
}
