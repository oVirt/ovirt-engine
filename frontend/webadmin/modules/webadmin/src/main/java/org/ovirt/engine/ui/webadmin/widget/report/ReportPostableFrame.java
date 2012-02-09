package org.ovirt.engine.ui.webadmin.widget.report;

import org.ovirt.engine.ui.webadmin.widget.form.PostableFrame;

import com.google.gwt.uibinder.client.UiConstructor;

public class ReportPostableFrame extends PostableFrame {
    public @UiConstructor
    ReportPostableFrame(String frameName) {
        super(frameName);
    }

    String uri;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
