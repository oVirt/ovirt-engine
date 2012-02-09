package org.ovirt.engine.ui.uicommonweb.models.reports;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.UrlBuilder;

public class ReportsListModel extends SearchableListModel {

    private final UrlBuilder builder;
    private final Map<String, List<String>> reportParams = new HashMap<String, List<String>>();
    private String lastResourceId = "";

    public ReportsListModel(String host, int port, String context) {
        builder = new UrlBuilder();
        builder.setHost(host);
        builder.setPort(port);
        builder.setPath(context + "/flow.html");
        reportParams.put("_flowId", new LinkedList<String>(Collections.singletonList("viewReportFlow")));
        reportParams.put(" active_hosts_select",
                new LinkedList<String>(Collections.singletonList("AND+delete_date+IS+NULL")));

        setDefaultSearchString("Reports:");
        setSearchString(getDefaultSearchString());

        getSearchNextPageCommand().setIsAvailable(false);
        getSearchPreviousPageCommand().setIsAvailable(false);

        setIsTimerDisabled(true);

        setViewAsDashboard(true);
    }

    public Map<String, List<String>> getCommonParams() {
        return Collections.unmodifiableMap(reportParams);
    }

    public String getCommonUrl() {
        return builder.buildString();
    }

    public void setDataCenterID(final String uuid) {
        reportParams.put("P_DataCenter_ID", new LinkedList<String>(Collections.singletonList(uuid)));
    }

    public void setPassword(String password) {
        reportParams.put("j_password", new LinkedList<String>(Collections.singletonList(password)));
    }

    public void setReportEndDate(Date date) {
        reportParams.put("P_End_Date", new LinkedList<String>(Collections.singletonList(dateStr(date))));
    }

    public void setReportStartDate(Date date) {
        reportParams.put("P_Start_Date", new LinkedList<String>(Collections.singletonList(dateStr(date))));
    }

    public void setUser(String user) {
        reportParams.put("j_username", new LinkedList<String>(Collections.singletonList(user)));
    }

    public void setViewAsDashboard(boolean viewAsDashboard) {
        builder.setParameter("viewAsDashboardFrame", String.valueOf(viewAsDashboard));
    }

    public void setReportUnit(String uri) {
        reportParams.put("reportUnit", new LinkedList<String>(Collections.singletonList(uri)));
    }

    public void setResourceId(String idParamName, String id) {
        reportParams.put(idParamName, new LinkedList<String>(Collections.singletonList(id)));
        lastResourceId = idParamName;
    }

    public void addResourceId(String idParamName, String id) {
        List<String> ids = reportParams.get(idParamName);

        if (ids == null) {
            setResourceId(idParamName, id);
        } else {
            ids.add(id);
        }
    }

    private void clearTreeSensitiveParams() {
        removeParam("P_DataCenter_ID");
        removeParam(lastResourceId);
    }

    public void removeParam(String paramName) {
        reportParams.remove(paramName);
    }

    public void OnSystemTreeChanged(SystemTreeItemModel model) {
        clearTreeSensitiveParams();
        String title = model.getTitle();
        switch (model.getType()) {
        case System: {
            break;
        }
        case DataCenter: {
            storage_pool dataCenter = (storage_pool) model.getEntity();
            setDataCenterID(dataCenter.getId().toString());
            break;
        }
        case Clusters: {
            storage_pool dataCenter = (storage_pool) model.getEntity();
            setDataCenterID(dataCenter.getId().toString());
            break;
        }
        case Cluster: {
            break;
        }
        case Hosts: {
            break;
        }
        case Host: {
            VDS host = (VDS) model.getEntity();
            setDataCenterID(host.getstorage_pool_id().toString());
            setResourceId("P_Host_ID", host.getQueryableId().toString());
            break;
        }
        case Storages: {
            break;
        }
        case Storage: {
            break;
        }
        case Templates:

            break;
        case VMs: {
            break;
        }
        default:
            // webadmin: redirect to default tab in case no tab is selected.
        }
        GWT.log("Tree Item changed: " + title);

        setEntity(model.getType());
    }

    @Override
    protected String getListName() {
        return "DeashboardReportsListModel";
    }

    private String dateStr(Date date) {
        return date.getYear() + "-" + date.getMonth() + "-" + date.getDate();
    }
}
