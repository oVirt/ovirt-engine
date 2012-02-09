package org.ovirt.engine.ui.webadmin.section.main.view.tab.dashboard;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.ReportsPanel;

public class ReportsPanelManager {

    private static ReportsPanelManager INSTANCE = new ReportsPanelManager();

    Map<SystemTreeItemType, ReportsPanel> reportsPanelsMap = new HashMap<SystemTreeItemType, ReportsPanel>();

    public static ReportsPanelManager getInstance() {
        return INSTANCE;
    }

    private ReportsPanelManager() {
        reportsPanelsMap.put(SystemTreeItemType.System, new SystemReportsPanel());
        reportsPanelsMap.put(SystemTreeItemType.DataCenter, new DataCenterReportsPanel());
        // reportsPanelsMap.put( Storages,);
        // reportsPanelsMap.put(Storage,);
        // reportsPanelsMap.put(Templates,);
        reportsPanelsMap.put(SystemTreeItemType.Clusters, new ClusterReportPanel());
        // reportsPanelsMap.put(Cluster,);
        // reportsPanelsMap.put(VMs,);
        // reportsPanelsMap.put(Hosts,);
        // reportsPanelsMap.put(SystemTreeItemType.Host, new HostReportsPanel());
    }

    public ReportsPanel getReportsPanel(SystemTreeItemType type) {
        return reportsPanelsMap.get(type);
    }

}
