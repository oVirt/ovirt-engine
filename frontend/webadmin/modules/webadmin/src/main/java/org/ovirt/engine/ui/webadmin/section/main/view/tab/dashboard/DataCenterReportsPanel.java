package org.ovirt.engine.ui.webadmin.section.main.view.tab.dashboard;

import org.ovirt.engine.ui.webadmin.section.main.view.tab.ReportsPanel;
import org.ovirt.engine.ui.webadmin.widget.report.ReportPostableFrame;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;

public class DataCenterReportsPanel extends ReportsPanel {

    interface ViewUiBinder extends UiBinder<SimplePanel, DataCenterReportsPanel> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    SimplePanel reportsPanel;

    @UiField
    ReportPostableFrame dc_entities_counts_list_dr2;

    @UiField
    ReportPostableFrame dc_host_resources_status_dr11;

    @UiField
    ReportPostableFrame dc_host_uptime_status_dr5;

    @UiField
    ReportPostableFrame dc_storage_space_status_dr8;

    @UiField
    ReportPostableFrame datacenter_top_five_busiest_clusters_dr21;

    @UiField
    ReportPostableFrame dc_top_five_used_storage_domain_dr26;

    @UiField
    ReportPostableFrame datacenter_top_five_least_busy_clusters_dr22;

    @UiField
    ReportPostableFrame dc_summary_of_cluster_hosts_resources_usage_dr27;

    @UiField
    ReportPostableFrame top_five_clusters_host_uptime_dr15;

    @UiField
    ReportPostableFrame dc_vm_over_commit_dr25;

    @UiField
    ReportPostableFrame top_five_clusters_host_downtime_dr16;

    public DataCenterReportsPanel() {
        super();
        setWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        frames.add(dc_entities_counts_list_dr2);
        frames.add(dc_host_resources_status_dr11);
        frames.add(dc_host_uptime_status_dr5);
        frames.add(dc_storage_space_status_dr8);
        frames.add(datacenter_top_five_busiest_clusters_dr21);
        frames.add(dc_top_five_used_storage_domain_dr26);
        frames.add(datacenter_top_five_least_busy_clusters_dr22);
        frames.add(dc_summary_of_cluster_hosts_resources_usage_dr27);
        frames.add(top_five_clusters_host_uptime_dr15);
        frames.add(dc_vm_over_commit_dr25);
        frames.add(top_five_clusters_host_downtime_dr16);
    }
}
