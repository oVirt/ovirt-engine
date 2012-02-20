package org.ovirt.engine.ui.webadmin.section.main.view.tab.dashboard;

import org.ovirt.engine.ui.webadmin.section.main.view.tab.ReportsPanel;
import org.ovirt.engine.ui.webadmin.widget.report.ReportPostableFrame;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;

public class ClusterReportPanel extends ReportsPanel {

    interface ViewUiBinder extends UiBinder<SimplePanel, ClusterReportPanel> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    SimplePanel reportsPanel;

    @UiField
    ReportPostableFrame frame1;

    @UiField
    ReportPostableFrame frame2;

    public ClusterReportPanel() {
        super();
        setWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        frames.add(frame1);
        frames.add(frame2);
    }
}
