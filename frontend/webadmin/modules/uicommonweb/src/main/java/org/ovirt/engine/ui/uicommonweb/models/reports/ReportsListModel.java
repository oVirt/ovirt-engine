package org.ovirt.engine.ui.uicommonweb.models.reports;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.uicommonweb.HtmlParameters;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;

import com.google.gwt.core.client.GWT;

public class ReportsListModel extends SearchableListModel {

    HtmlParameters htmlParams = new HtmlParameters();
    private String lastResourceId = "";
    private final String reportUrl;

    public ReportsListModel(String baseUrl) {
        reportUrl = baseUrl + "/flow.html" + "?viewAsDashboardFrame=true";

        htmlParams.setParameter("_flowId", "viewReportFlow");
        htmlParams.setParameter(" active_hosts_select", "AND+delete_date+IS+NULL");

        setDefaultSearchString("Reports:");
        setSearchString(getDefaultSearchString());

        getSearchNextPageCommand().setIsAvailable(false);
        getSearchPreviousPageCommand().setIsAvailable(false);

        setIsTimerDisabled(true);
    }

    public Map<String, List<String>> getCommonParams() {
        return htmlParams.getParameters();
    }

    public String getCommonUrl() {
        return reportUrl;
    }

    public void setDataCenterID(final String uuid) {
        htmlParams.setParameter("P_DataCenter_ID", uuid);
    }

    public void setPassword(String password) {
        htmlParams.setParameter("j_password", password);
    }

    public void setReportEndDate(String date) {
        htmlParams.setParameter("P_End_Date", date);
    }

    public void setReportStartDate(String date) {
        htmlParams.setParameter("P_Start_Date", date);
    }

    public void setUser(String user) {
        htmlParams.setParameter("j_username", user);
    }

    public void setReportUnit(String uri) {
        htmlParams.setParameter("reportUnit", uri);
    }

    public void setResourceId(String idParamName, String id) {
        htmlParams.setParameter(idParamName, id);
        lastResourceId = idParamName;
    }

    public void addResourceId(String idParamName, String id) {
        htmlParams.addParameter(idParamName, id);
    }

    private void clearTreeSensitiveParams() {
        removeParam("P_DataCenter_ID");
        removeParam(lastResourceId);
    }

    public void removeParam(String paramName) {
        htmlParams.removeParameter(paramName);
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

        setEntity(model);
    }

    @Override
    protected String getListName() {
        return "DeashboardReportsListModel";
    }
}
