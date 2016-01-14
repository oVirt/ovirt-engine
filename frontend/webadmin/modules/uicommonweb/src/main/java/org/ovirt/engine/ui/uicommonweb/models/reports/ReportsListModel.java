package org.ovirt.engine.ui.uicommonweb.models.reports;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.frontend.utils.FrontendUrlUtils;
import org.ovirt.engine.ui.uicommonweb.HtmlParameters;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListWithReportsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.ReportParser.Dashboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.inject.Provider;

public class ReportsListModel extends SearchableListWithReportsModel {

    HtmlParameters htmlParams = new HtmlParameters();
    private String lastResourceId = ""; //$NON-NLS-1$
    private final String reportUrl;
    private String uri;
    private boolean reportsTabSelected = false;

    private final Provider<CommonModel> commonModelProvider;

    public ReportsListModel(String baseUrl, String ssoToken, final Provider<CommonModel> commonModelProvider) {
        reportUrl = FrontendUrlUtils.stripParameters(baseUrl);
        htmlParams.parseUrlParams(baseUrl);
        htmlParams.setParameter("sessionID", ssoToken); //$NON-NLS-1$
        this.commonModelProvider = commonModelProvider;

        String currentLocale = LocaleInfo.getCurrentLocale().getLocaleName();
        htmlParams.setParameter("userLocale", currentLocale.equals("default") ? "en_US" : currentLocale); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        setApplicationPlace(WebAdminApplicationPlaces.reportsMainTabPlace);

        setDefaultSearchString("Reports:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setAvailableInModes(ApplicationMode.VirtOnly);

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

    public void setPassword(String password) {
        htmlParams.setParameter("j_password", password); //$NON-NLS-1$
    }

    public void setUser(String user) {
        htmlParams.setParameter("j_username", user); //$NON-NLS-1$
    }

    public void setDataCenterID(final String uuid) {
        htmlParams.setParameter(getHiddenParamPrefix() + "P_DataCenter_ID", uuid); //$NON-NLS-1$
    }

    public void setResourceId(String idParamName, String id) {
        htmlParams.setParameter(getHiddenParamPrefix() + idParamName, id);
        lastResourceId = idParamName;
    }

    public void setUri(String uri) {
        this.uri = uri;
        if (ReportInit.getInstance().isCommunityEdition()) {
            htmlParams.setParameter("reportUnit", uri); //$NON-NLS-1$
        } else {
            htmlParams.setParameter("dashboardResource", uri); //$NON-NLS-1$
        }
    }

    public String getUri() {
        return uri;
    }

    private void clearTreeSensitiveParams() {
        removeParam("P_DataCenter_ID"); //$NON-NLS-1$
        removeParam(lastResourceId);
    }

    public void removeParam(String paramName) {
        htmlParams.removeParameter(paramName);
    }

    public void refreshReportModel() {
        SystemTreeItemModel treeItemModel = commonModelProvider.get().getSystemTree().getSelectedItem();
        if (treeItemModel == null) {
            return;
        }

        clearTreeSensitiveParams();
        String title = treeItemModel.getTitle();

        Dashboard dashboard = ReportInit.getInstance().getDashboard(treeItemModel.getType().toString());
        if (dashboard != null) {
            setUri(dashboard.getUri());

            StoragePool dataCenter;
            Cluster cluster;
            switch (treeItemModel.getType()) {
            case System:
                break;
            case DataCenter:
                dataCenter = (StoragePool) treeItemModel.getEntity();
                setDataCenterID(dataCenter.getId().toString());
                break;
            case Clusters:
                dataCenter = (StoragePool) treeItemModel.getEntity();
                setDataCenterID(dataCenter.getId().toString());
                break;
            case Cluster:
                cluster = (Cluster) treeItemModel.getEntity();
                setDataCenterID(cluster.getStoragePoolId().toString());
                setResourceId("P_Cluster_ID", cluster.getQueryableId().toString()); //$NON-NLS-1$
                break;
            case Hosts:
                cluster = (Cluster) treeItemModel.getEntity();
                setDataCenterID(cluster.getStoragePoolId().toString());
                setResourceId("P_Cluster_ID", cluster.getQueryableId().toString()); //$NON-NLS-1$
                break;
            case Host:
                VDS host = (VDS) treeItemModel.getEntity();
                setDataCenterID(host.getStoragePoolId().toString());
                setResourceId("P_Host_ID", host.getQueryableId().toString()); //$NON-NLS-1$
                break;
            case Storages:
                dataCenter = (StoragePool) treeItemModel.getEntity();
                setDataCenterID(dataCenter.getId().toString());
                break;
            case Storage:
                StorageDomain storage = (StorageDomain) treeItemModel.getEntity();
                setDataCenterID(storage.getStoragePoolId().toString());
                setResourceId("P_StorageDomain_ID", storage.getQueryableId().toString()); //$NON-NLS-1$
                break;
            case Templates:
                dataCenter = (StoragePool) treeItemModel.getEntity();
                setDataCenterID(dataCenter.getId().toString());
                break;
            case VMs:
                VM vm = (VM) treeItemModel.getEntity();
                setDataCenterID(vm.getStoragePoolId().toString());
                setResourceId("P_VM_ID", vm.getQueryableId().toString()); //$NON-NLS-1$
                break;
            case Networks:
                dataCenter = (StoragePool) treeItemModel.getEntity();
                setDataCenterID(dataCenter.getId().toString());
                break;
            case Network:
                Network network = (Network) treeItemModel.getEntity();
                setDataCenterID(network.getDataCenterId().toString());
                setResourceId("P_Network_ID", network.getQueryableId().toString()); //$NON-NLS-1$
                break;
            default:
                // webadmin: redirect to default tab in case no tab is selected.
            }
        }
        GWT.log("Tree Item changed: " + title); //$NON-NLS-1$

        getReportsAvailabilityEvent().raise(this, EventArgs.EMPTY);
    }

    @Override
    protected String getListName() {
        return "DeashboardReportsListModel"; //$NON-NLS-1$
    }

    private String getHiddenParamPrefix() {
        if (ReportInit.getInstance().isCommunityEdition()) {
            return ""; //$NON-NLS-1$
        } else {
            return "hidden_"; //$NON-NLS-1$
        }
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("reports"); //$NON-NLS-1$
    }

    public boolean isReportsTabSelected() {
        return reportsTabSelected;
    }

    public void setReportsTabSelected(boolean reportsTabSelected) {
        this.reportsTabSelected = reportsTabSelected;
    }
}
