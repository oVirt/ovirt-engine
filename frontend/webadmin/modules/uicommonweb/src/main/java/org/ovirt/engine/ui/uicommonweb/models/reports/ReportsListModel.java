package org.ovirt.engine.ui.uicommonweb.models.reports;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.ui.uicommonweb.HtmlParameters;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicompat.ReportParser.Dashboard;

import com.google.gwt.core.client.GWT;

public class ReportsListModel extends SearchableListModel {

    HtmlParameters htmlParams = new HtmlParameters();
    private String lastResourceId = "";
    private final String reportUrl;
    private Event reportModelRefreshEvent = new Event(new EventDefinition("ReportModelRefreshed",
            ReportsListModel.class));

    public Event getReportModelRefreshEvent() {
        return reportModelRefreshEvent;
    }

    public ReportsListModel(String baseUrl) {
        reportUrl = baseUrl + "/flow.html" + "?viewAsDashboardFrame=true";

        setFlowId();

        setDefaultSearchString("Reports:");
        setSearchString(getDefaultSearchString());

        getSearchNextPageCommand().setIsAvailable(false);
        getSearchPreviousPageCommand().setIsAvailable(false);

        setIsTimerDisabled(true);
    }

    public Map<String, List<String>> getParams() {
        return htmlParams.getParameters();
    }

    public String getUrl() {
        return reportUrl;
    }

    private void setFlowId() {
        if (ReportInit.getInstance().isCommunityEdition()) {
            htmlParams.setParameter("_flowId", "viewReportFlow");
        } else {
            htmlParams.setParameter("_flowId", "dashboardRuntimeFlow");
        }
    }

    public void setPassword(String password) {
        htmlParams.setParameter("j_password", password);
    }

    public void setUser(String user) {
        htmlParams.setParameter("j_username", user);
    }

    public void setDataCenterID(final String uuid) {
        htmlParams.setParameter(getHiddenParamPrefix() + "P_DataCenter_ID", uuid);
    }

    public void setResourceId(String idParamName, String id) {
        htmlParams.setParameter(getHiddenParamPrefix() + idParamName, id);
        lastResourceId = idParamName;
    }

    public void setUri(String uri) {
        if (ReportInit.getInstance().isCommunityEdition()) {
            htmlParams.setParameter("reportUnit", uri);
        } else {
            htmlParams.setParameter("dashboardResource", uri);
        }
    }

    private void clearTreeSensitiveParams() {
        removeParam("P_DataCenter_ID");
        removeParam(lastResourceId);
    }

    public void removeParam(String paramName) {
        htmlParams.removeParameter(paramName);
    }

    public void refreshReportModel() {
        SystemTreeItemModel treeItemModel =
                ((SystemTreeItemModel) CommonModel.getInstance().getSystemTree().getSelectedItem());
        if (treeItemModel == null) {
            return;
        }

        clearTreeSensitiveParams();
        String title = treeItemModel.getTitle();

        Dashboard dashboard = ReportInit.getInstance().getDashboard(treeItemModel.getType().toString());
        if (dashboard != null) {
            setUri(dashboard.getUri());

            switch (treeItemModel.getType()) {
            case System: {
                break;
            }
            case DataCenter: {
                storage_pool dataCenter = (storage_pool) treeItemModel.getEntity();
                setDataCenterID(dataCenter.getId().toString());
                break;
            }
            case Clusters: {
                storage_pool dataCenter = (storage_pool) treeItemModel.getEntity();
                setDataCenterID(dataCenter.getId().toString());
                break;
            }
            case Cluster: {
                VDSGroup cluster = (VDSGroup) treeItemModel.getEntity();
                setDataCenterID(cluster.getstorage_pool_id().toString());
                setResourceId("P_Cluster_ID", cluster.getQueryableId().toString());
                break;
            }
            case Hosts: {
                VDSGroup cluster = (VDSGroup) treeItemModel.getEntity();
                setDataCenterID(cluster.getstorage_pool_id().toString());
                setResourceId("P_Cluster_ID", cluster.getQueryableId().toString());
                break;
            }
            case Host: {
                VDS host = (VDS) treeItemModel.getEntity();
                setDataCenterID(host.getstorage_pool_id().toString());
                setResourceId("P_Host_ID", host.getQueryableId().toString());
                break;
            }
            case Storages: {
                storage_pool dataCenter = (storage_pool) treeItemModel.getEntity();
                setDataCenterID(dataCenter.getId().toString());
                break;
            }
            case Storage: {
                storage_domains storage = (storage_domains) treeItemModel.getEntity();
                setDataCenterID(storage.getstorage_pool_id().toString());
                setResourceId("P_StorageDomain_ID", storage.getQueryableId().toString());
                break;
            }
            case Templates:
                storage_pool dataCenter = (storage_pool) treeItemModel.getEntity();
                setDataCenterID(dataCenter.getId().toString());
                break;
            case VMs: {
                VM vm = (VM) treeItemModel.getEntity();
                setDataCenterID(vm.getstorage_pool_id().toString());
                setResourceId("P_VM_ID", vm.getQueryableId().toString());
                break;
            }
            default:
                // webadmin: redirect to default tab in case no tab is selected.
            }
        }
        GWT.log("Tree Item changed: " + title);

        reportModelRefreshEvent.raise(this, EventArgs.Empty);
    }

    @Override
    protected String getListName() {
        return "DeashboardReportsListModel";
    }

    private String getHiddenParamPrefix() {
        if (ReportInit.getInstance().isCommunityEdition()) {
            return "";
        } else {
            return "hidden_";
        }
    }

    @Override
    public boolean IsSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("reports");
    }

}
