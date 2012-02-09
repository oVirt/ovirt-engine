package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.ui.webadmin.widget.form.PostableFrame;
import org.ovirt.engine.ui.webadmin.widget.report.ReportPostableFrame;

import com.google.gwt.user.client.ui.SimplePanel;

public abstract class ReportsPanel extends SimplePanel {

    public abstract int getNumOfFrames();

    protected List<ReportPostableFrame> frames = new LinkedList<ReportPostableFrame>();

    public void setCommonUrl(String url) {
        for (PostableFrame frame : frames) {
            frame.setUrl(url);
        }
    }

    public void setParams(Map<String, List<String>> params) {
        for (ReportPostableFrame frame : frames) {
            frame.removeOldParams();
            for (Entry<String, List<String>> entry : params.entrySet()) {
                for (String param : entry.getValue()) {
                    frame.addParameter(entry.getKey(), param);
                }
            }
            frame.addParameter("reportUnit", frame.getUri());
        }
    }

    public void post() {
        for (ReportPostableFrame frame : frames) {
            frame.post();
        }
    }
}
