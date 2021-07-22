package org.ovirt.engine.ui.uicommonweb.models.hosts;

import static org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel.createUpgradeModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.ActionUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.ForceSelectSPMParameters;
import org.ovirt.engine.core.common.action.MaintenanceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.action.SetHaMaintenanceParameters;
import org.ovirt.engine.core.common.action.SshHostRebootParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.VdsPowerDownParameters;
import org.ovirt.engine.core.common.action.hostdeploy.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.ApproveVdsParameters;
import org.ovirt.engine.core.common.action.hostdeploy.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.HaMaintenanceMode;
import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.ReplaceHostConfiguration;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.VgpuPlacement;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.utils.pm.FenceProxySourceTypeHelper;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.utils.FrontendUrlUtils;
import org.ovirt.engine.ui.frontend.utils.JsSingleValueStringObject;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.TagAssigningModel;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.Uri;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.AddVdsActionParametersMapper;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.HostErrataCountModel;
import org.ovirt.engine.ui.uicommonweb.models.HostMaintenanceConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchStringMapping;
import org.ovirt.engine.ui.uicommonweb.models.VDSMapper;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.list.HostAffinityLabelListModel;
import org.ovirt.engine.ui.uicommonweb.models.events.TaskListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.HostGlusterStorageDevicesListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.HostGlusterSwiftListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.NumaSupportModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.HostDeviceListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.ReversibleFlow;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

@SuppressWarnings("unchecked")
public class HostListModel<E> extends ListWithSimpleDetailsModel<E, VDS> implements TagAssigningModel<VDS> {

    private final HostGeneralModel generalModel;

    public HostGeneralModel getGeneralModel() {
        return generalModel;
    }

    private UICommand privateNewCommand;

    public UICommand getNewCommand() {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value) {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    @Override
    public UICommand getEditCommand() {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value) {
        privateEditCommand = value;
    }

    private UICommand privateEditWithPMemphasisCommand;

    public UICommand getEditWithPMemphasisCommand() {
        return privateEditWithPMemphasisCommand;
    }

    private void setEditWithPMemphasisCommand(UICommand value) {
        privateEditWithPMemphasisCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand() {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value) {
        privateRemoveCommand = value;
    }

    private UICommand privateHostConsoleCommand;

    public UICommand getHostConsoleCommand() {
        return privateHostConsoleCommand;
    }

    private void setHostConsoleCommand(UICommand value) {
        privateHostConsoleCommand = value;
    }

    private UICommand selectAsSpmCommand;

    public UICommand getSelectAsSpmCommand() {
        return selectAsSpmCommand;
    }

    private void setSelectAsSpmCommand(UICommand value) {
        selectAsSpmCommand = value;
    }

    private UICommand privateActivateCommand;

    public UICommand getActivateCommand() {
        return privateActivateCommand;
    }

    private void setActivateCommand(UICommand value) {
        privateActivateCommand = value;
    }

    private UICommand privateMaintenanceCommand;

    public UICommand getMaintenanceCommand() {
        return privateMaintenanceCommand;
    }

    private void setMaintenanceCommand(UICommand value) {
        privateMaintenanceCommand = value;
    }

    private UICommand privateApproveCommand;

    public UICommand getApproveCommand() {
        return privateApproveCommand;
    }

    private void setApproveCommand(UICommand value) {
        privateApproveCommand = value;
    }

    private UICommand privateInstallCommand;

    public UICommand getInstallCommand() {
        return privateInstallCommand;
    }

    private void setInstallCommand(UICommand value) {
        privateInstallCommand = value;
    }

    private UICommand privateCheckForUpgradeCommand;

    public UICommand getCheckForUpgradeCommand() {
        return privateCheckForUpgradeCommand;
    }

    private void setCheckForUpgradeCommand(UICommand value) {
        privateCheckForUpgradeCommand = value;
    }

    private UICommand privateUpgradeCommand;

    public UICommand getUpgradeCommand() {
        return privateUpgradeCommand;
    }

    private void setUpgradeCommand(UICommand value) {
        privateUpgradeCommand = value;
    }

    private UICommand privateSshRestartCommand;

    public UICommand getSshRestartCommand() {
        return privateSshRestartCommand;
    }

    private void setSshRestartCommand(UICommand value) {
        privateSshRestartCommand = value;
    }

    private UICommand privateSshStopCommand;

    public UICommand getSshStopCommand() {
        return privateSshStopCommand;
    }

    private void setSshStopCommand(UICommand value) {
        privateSshStopCommand = value;
    }

    private UICommand privateRestartCommand;

    public UICommand getRestartCommand() {
        return privateRestartCommand;
    }

    private void setRestartCommand(UICommand value) {
        privateRestartCommand = value;
    }

    private UICommand privateStartCommand;

    public UICommand getStartCommand() {
        return privateStartCommand;
    }

    private void setStartCommand(UICommand value) {
        privateStartCommand = value;
    }

    private UICommand privateStopCommand;

    public UICommand getStopCommand() {
        return privateStopCommand;
    }

    private void setStopCommand(UICommand value) {
        privateStopCommand = value;
    }

    private UICommand privateManualFenceCommand;

    public UICommand getManualFenceCommand() {
        return privateManualFenceCommand;
    }

    private void setManualFenceCommand(UICommand value) {
        privateManualFenceCommand = value;
    }

    private UICommand privateAssignTagsCommand;

    @Override
    public UICommand getAssignTagsCommand() {
        return privateAssignTagsCommand;
    }

    private void setAssignTagsCommand(UICommand value) {
        privateAssignTagsCommand = value;
    }

    private UICommand privateConfigureLocalStorageCommand;

    public UICommand getConfigureLocalStorageCommand() {
        return privateConfigureLocalStorageCommand;
    }

    private void setConfigureLocalStorageCommand(UICommand value) {
        privateConfigureLocalStorageCommand = value;
    }

    private UICommand refreshCapabilitiesCommand;

    public UICommand getRefreshCapabilitiesCommand() {
        return refreshCapabilitiesCommand;
    }

    private void setRefreshCapabilitiesCommand(UICommand value) {
        refreshCapabilitiesCommand = value;
    }

    private UICommand enrollCertificateCommand;

    public UICommand getEnrollCertificateCommand() {
        return enrollCertificateCommand;
    }

    private void setEnrollCertificateCommand(UICommand value) {
        enrollCertificateCommand = value;
    }

    private UICommand numaSupportCommand;

    public UICommand getNumaSupportCommand() {
        return numaSupportCommand;
    }

    public void setNumaSupportCommand(UICommand numaSupportCommand) {
        this.numaSupportCommand = numaSupportCommand;
    }

    private UICommand privateEnableGlobalHaMaintenanceCommand;

    public UICommand getEnableGlobalHaMaintenanceCommand() {
        return privateEnableGlobalHaMaintenanceCommand;
    }

    private void setEnableGlobalHaMaintenanceCommand(UICommand value) {
        privateEnableGlobalHaMaintenanceCommand = value;
    }

    private UICommand privateDisableGlobalHaMaintenanceCommand;

    public UICommand getDisableGlobalHaMaintenanceCommand() {
        return privateDisableGlobalHaMaintenanceCommand;
    }

    private void setDisableGlobalHaMaintenanceCommand(UICommand value) {
        privateDisableGlobalHaMaintenanceCommand = value;
    }

    private final HostEventListModel privateHostEventListModel;

    public HostEventListModel getEventListModel() {
        return privateHostEventListModel;
    }

    private boolean isPowerManagementEnabled;

    public boolean getIsPowerManagementEnabled() {
        return isPowerManagementEnabled;
    }

    public void setIsPowerManagementEnabled(boolean value) {
        if (isPowerManagementEnabled != value) {
            isPowerManagementEnabled = value;
            onPropertyChanged(new PropertyChangedEventArgs("isPowerManagementEnabled")); //$NON-NLS-1$
        }
    }

    private final HostGlusterSwiftListModel glusterSwiftModel;

    public HostGlusterSwiftListModel getGlusterSwiftModel() {
        return glusterSwiftModel;
    }

    private final HostBricksListModel bricksListModel;

    public HostBricksListModel getBricksListModel() {
        return bricksListModel;
    }

    private final HostGlusterStorageDevicesListModel glusterStorageDeviceListModel;

    public HostGlusterStorageDevicesListModel getGlusterStorageDeviceListModel() {
        return glusterStorageDeviceListModel;
    }

    private final HostVmListModel vmListModel;

    public HostVmListModel getVmListModel() {
        return this.vmListModel;
    }

    private final HostInterfaceListModel interfaceListModel;

    public HostInterfaceListModel getInterfaceListModel() {
        return interfaceListModel;
    }

    private final HostDeviceListModel deviceListModel;

    public HostDeviceListModel getDeviceListModel() {
        return deviceListModel;
    }

    private final HostHooksListModel hooksListModel;

    public HostHooksListModel getHooksListModel() {
        return hooksListModel;
    }

    private final PermissionListModel<VDS> permissionListModel;

    public PermissionListModel<VDS> getPermissionListModel() {
        return permissionListModel;
    }

    private final HostAffinityLabelListModel affinityLabelListModel;

    public HostAffinityLabelListModel getAffinityLabelListModel() {
        return affinityLabelListModel;
    }

    private final HostErrataCountModel errataCountModel;

    public HostErrataCountModel getErrataCountModel() {
        return errataCountModel;
    }

    @Inject
    public HostListModel(final HostGeneralModel hostGeneralModel,
            final HostGlusterSwiftListModel hostGlusterSwiftListModel,
            final HostBricksListModel hostBricksListModel,
            final HostVmListModel hostVmListModel,
            final HostEventListModel hostEventListModel,
            final HostInterfaceListModel hostInterfaceListModel,
            final HostDeviceListModel hostDeviceListModel,
            final HostHardwareGeneralModel hostHardwareGeneralModel,
            final HostHooksListModel hostHooksListModel,
            final PermissionListModel<VDS> permissionListModel,
            final HostGlusterStorageDevicesListModel glusterStorageDeviceListModel,
            final HostAffinityLabelListModel hostAffinityLabelListModel,
            final HostErrataCountModel hostErrataCountModel) {
        this.generalModel = hostGeneralModel;
        this.glusterSwiftModel = hostGlusterSwiftListModel;
        this.bricksListModel = hostBricksListModel;
        this.vmListModel = hostVmListModel;
        this.privateHostEventListModel = hostEventListModel;
        this.glusterStorageDeviceListModel = glusterStorageDeviceListModel;
        this.interfaceListModel = hostInterfaceListModel;
        this.deviceListModel = hostDeviceListModel;
        this.hooksListModel = hostHooksListModel;
        this.permissionListModel = permissionListModel;
        this.affinityLabelListModel = hostAffinityLabelListModel;
        this.errataCountModel = hostErrataCountModel;

        setDetailList(hostHardwareGeneralModel);

        setTitle(ConstantsManager.getInstance().getConstants().hostsTitle());
        setHelpTag(HelpTag.hosts);
        setApplicationPlace(WebAdminApplicationPlaces.hostMainPlace);
        setHashName("hosts"); //$NON-NLS-1$

        setDefaultSearchString(SearchStringMapping.HOSTS_DEFAULT_SEARCH + ":"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.VDS_OBJ_NAME, SearchObjects.VDS_PLU_OBJ_NAME });

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setEditWithPMemphasisCommand(new UICommand("EditWithPMemphasis", this)); //$NON-NLS-1$
        setSelectAsSpmCommand(new UICommand("SelectAsSpm", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setHostConsoleCommand(new UICommand("HostConsole", this)); //$NON-NLS-1$
        setActivateCommand(new UICommand("Activate", this, true)); //$NON-NLS-1$
        setMaintenanceCommand(new UICommand("Maintenance", this, true)); //$NON-NLS-1$
        setApproveCommand(new UICommand("Approve", this)); //$NON-NLS-1$
        setInstallCommand(new UICommand("Install", this)); //$NON-NLS-1$
        setCheckForUpgradeCommand(new UICommand("CheckForUpgrade", this)); //$NON-NLS-1$
        setUpgradeCommand(new UICommand("Upgrade", this)); //$NON-NLS-1$
        setSshRestartCommand(new UICommand("Restart", this, true)); //$NON-NLS-1$
        setSshStopCommand(new UICommand("Stop", this, true)); //$NON-NLS-1$
        setRestartCommand(new UICommand("Restart", this, true)); //$NON-NLS-1$
        setStartCommand(new UICommand("Start", this, true)); //$NON-NLS-1$
        setStopCommand(new UICommand("Stop", this, true)); //$NON-NLS-1$
        setManualFenceCommand(new UICommand("ManualFence", this)); //$NON-NLS-1$
        setAssignTagsCommand(new UICommand("AssignTags", this)); //$NON-NLS-1$
        setConfigureLocalStorageCommand(new UICommand("ConfigureLocalStorage", this)); //$NON-NLS-1$
        setRefreshCapabilitiesCommand(new UICommand("GetCapabilities", this)); //$NON-NLS-1$
        setEnrollCertificateCommand(new UICommand("EnrollCertificate", this)); //$NON-NLS-1$
        setNumaSupportCommand(new UICommand("NumaSupport", this)); //$NON-NLS-1$
        getConfigureLocalStorageCommand().setAvailableInModes(ApplicationMode.VirtOnly);
        getSelectAsSpmCommand().setAvailableInModes(ApplicationMode.VirtOnly);
        setEnableGlobalHaMaintenanceCommand(new UICommand("EnableGlobalHaMaintenance", this)); //$NON-NLS-1$
        setDisableGlobalHaMaintenanceCommand(new UICommand("DisableGlobalHaMaintenance", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);

        getItemsChangedEvent().addListener((ev, sender, args) -> hostAffinityLabelListModel.loadEntitiesNameMap());
    }

    @Override
    public void setItems(Collection<VDS> value) {
        AsyncDataProvider.getInstance().updateVDSDefaultRouteRole(new ArrayList<>(value), () -> super.setItems(value));
    }

    private void setDetailList(final HostHardwareGeneralModel hostHardwareGeneralModel) {
        generalModel.getRequestEditEvent().addListener(this);
        generalModel.getRequestGOToEventsTabEvent().addListener(this);

        List<HasEntity<VDS>> list = new ArrayList<>();
        list.add(generalModel);
        list.add(hostHardwareGeneralModel);
        list.add(getVmListModel());
        list.add(interfaceListModel);
        list.add(deviceListModel);
        list.add(getEventListModel());
        list.add(hooksListModel);
        list.add(getGlusterSwiftModel());
        list.add(getBricksListModel());
        list.add(getGlusterStorageDeviceListModel());
        list.add(permissionListModel);
        list.add(affinityLabelListModel);
        setDetailModels(list);
    }

    public void assignTags() {
        if (getWindow() != null) {
            return;
        }

        TagListModel model = new TagListModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().assignTagsTitle());
        model.setHelpTag(HelpTag.assign_tags_hosts);
        model.setHashName("assign_tags_hosts"); //$NON-NLS-1$

        getAttachedTagsToSelectedHosts(model);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnAssignTags", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }
    public Map<Guid, Boolean> attachedTagsToEntities;

    @Override
    public Map<Guid, Boolean> getAttachedTagsToEntities() {
        return attachedTagsToEntities;
    }

    public ArrayList<Tags> allAttachedTags;

    @Override
    public List<Tags> getAllAttachedTags() {
        return allAttachedTags;
    }

    public int selectedItemsCounter;

    private void getAttachedTagsToSelectedHosts(final TagListModel model) {
        ArrayList<Guid> hostIds = new ArrayList<>();

        for (VDS vds : getSelectedItems()) {
            hostIds.add(vds.getId());
        }

        attachedTagsToEntities = new HashMap<>();
        allAttachedTags = new ArrayList<>();
        selectedItemsCounter = 0;

        for (Guid hostId : hostIds) {
            AsyncDataProvider.getInstance().getAttachedTagsToHost(new AsyncQuery<>(
                            returnValue -> {

                                allAttachedTags.addAll(returnValue);
                                selectedItemsCounter++;
                                if (selectedItemsCounter == getSelectedItems().size()) {
                                    postGetAttachedTags(model);
                                }

                            }),
                    hostId);
        }
    }

    public void onAssignTags() {
        TagListModel model = (TagListModel) getWindow();

        getAttachedTagsToSelectedHosts(model);
    }

    @Override
    public void postOnAssignTags(Map<Guid, Boolean> attachedTags) {
        TagListModel model = (TagListModel) getWindow();
        ArrayList<Guid> hostIds = new ArrayList<>();

        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            hostIds.add(vds.getId());
        }

        // prepare attach/detach lists
        ArrayList<Guid> tagsToAttach = new ArrayList<>();
        ArrayList<Guid> tagsToDetach = new ArrayList<>();

        if (model.getItems() != null && model.getItems().size() > 0) {
            ArrayList<TagModel> tags = (ArrayList<TagModel>) model.getItems();
            TagModel rootTag = tags.get(0);
            TagModel.recursiveEditAttachDetachLists(rootTag, attachedTags, tagsToAttach, tagsToDetach);
        }

        ArrayList<ActionParametersBase> prmsToAttach = new ArrayList<>();
        for (Guid tag_id : tagsToAttach) {
            prmsToAttach.add(new AttachEntityToTagParameters(tag_id, hostIds));
        }
        Frontend.getInstance().runMultipleAction(ActionType.AttachVdsToTag, prmsToAttach);

        ArrayList<ActionParametersBase> prmsToDetach = new ArrayList<>();
        for (Guid tag_id : tagsToDetach) {
            prmsToDetach.add(new AttachEntityToTagParameters(tag_id, hostIds));
        }
        Frontend.getInstance().runMultipleAction(ActionType.DetachVdsFromTag, prmsToDetach);

        cancel();
    }

    public void manualFence() {
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().areYouSureTitle());
        model.setHelpTag(HelpTag.manual_fence_are_you_sure);
        model.setHashName("manual_fence_are_you_sure"); //$NON-NLS-1$
        ArrayList<VDS> items = new ArrayList<>();
        items.add(getSelectedItem());
        model.setItems(items);

        model.getLatch().setIsAvailable(true);
        model.getLatch().setIsChangeable(true);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnManualFence", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onManualFence() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (!model.validate()) {
            return;
        }

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            FenceVdsManualyParameters parameters = new FenceVdsManualyParameters(true);
            parameters.setStoragePoolId(vds.getStoragePoolId());
            parameters.setVdsId(vds.getId());
            list.add(parameters);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.FenceVdsManualy, list,
                result -> {

                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();

                }, model);
    }

    boolean updateOverrideIpTables = true;
    boolean clusterChanging = false;

    public void newEntity() {
        if (getWindow() != null) {
            return;
        }

        final NewHostModel hostModel = new NewHostModel();

        // isHeSystem must be set before setWindow() because the view edit is called before it finishes
        hostModel.setIsHeSystem(isHeSystem());

        setWindow(hostModel);
        hostModel.setTitle(ConstantsManager.getInstance().getConstants().newHostTitle());
        hostModel.setHelpTag(HelpTag.new_host);
        hostModel.setHashName("new_host"); //$NON-NLS-1$
        hostModel.getPort().setEntity(54321);
        hostModel.getOverrideIpTables().setIsAvailable(false);
        hostModel.setSpmPriorityValue(null);
        hostModel.getConsoleAddressEnabled().setEntity(false);
        hostModel.getConsoleAddress().setIsChangeable(false);
        hostModel.setVgpuPlacement(VgpuPlacement.CONSOLIDATED);

        AsyncDataProvider.getInstance().getDefaultPmProxyPreferences(new AsyncQuery<>(returnValue -> hostModel.setPmProxyPreferences(returnValue)));

        // Make sure not to set override IP tables flag back true when it was set false once.
        hostModel.getOverrideIpTables().getEntityChangedEvent().addListener((ev, sender, args) -> {

            if (!clusterChanging) {
                updateOverrideIpTables = hostModel.getOverrideIpTables().getEntity();
            }
        });

        hostModel.getCluster().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {

            clusterChanging = true;
            ListModel<Cluster> clusterModel = hostModel.getCluster();

            if (clusterModel.getSelectedItem() != null) {
                hostModel.getOverrideIpTables().setIsAvailable(true);
                hostModel.getOverrideIpTables().setEntity(updateOverrideIpTables);
            }

            clusterChanging = false;
        });

        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery<>(dataCenters -> {
            HostModel innerHostModel = (HostModel) getWindow();

            innerHostModel.getDataCenter().setItems(dataCenters);
            innerHostModel.getDataCenter().setSelectedItem(Linq.firstOrNull(dataCenters));
            innerHostModel.onDataInitialized();

            UICommand onSaveFalseCommand = UICommand.createDefaultOkUiCommand("OnSaveFalse", HostListModel.this); //$NON-NLS-1$
            innerHostModel.getCommands().add(onSaveFalseCommand);

            UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", HostListModel.this); //$NON-NLS-1$
            innerHostModel.getCommands().add(cancelCommand);
        }));
    }

    private void goToEventsTab() {
        setActiveDetailModel(getEventListModel());
    }

    public void edit(final boolean isEditWithPMemphasis) {
        if (getWindow() != null) {
            return;
        }

        VDS host = getSelectedItem();
        EditHostModel hostModel = new EditHostModel();
        hostModel.setSelectedCluster(host);
        AsyncDataProvider.getInstance().getAllFenceAgentsByHostId(new AsyncQuery<>(retValue -> {
            ArrayList<FenceAgent> fenceAgents = new ArrayList<>();
            for (FenceAgent fenceAgent : retValue) {
                fenceAgents.add(fenceAgent);
            }
            host.setFenceAgents(fenceAgents);
            hostModel.getFenceAgentListModel().setItems(hostModel.getFenceAgentModelList(host));
        }), getSelectedItem().getId());

        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery<>(dataCenters -> {

            hostModel.updateModelFromVds(host, dataCenters, isEditWithPMemphasis);
            hostModel.onDataInitialized();
            hostModel.setTitle(ConstantsManager.getInstance().getConstants().editHostTitle());
            hostModel.setHelpTag(HelpTag.edit_host);
            hostModel.setHashName("edit_host"); //$NON-NLS-1$
            hostModel.setIsHeSystem(isHeSystem());
            hostModel.setHostsWithHeDeployed(getHostsWithHeDeployed());
            hostModel.setHostedEngineHostModel(new HostedEngineHostModel());

            setWindow(hostModel);

            if (host.getFenceProxySources() != null && !host.getFenceProxySources().isEmpty()) {
                hostModel.setPmProxyPreferences(
                        FenceProxySourceTypeHelper.saveAsString(host.getFenceProxySources()));
            } else {
                AsyncDataProvider.getInstance().getDefaultPmProxyPreferences(new AsyncQuery<>(returnValue -> hostModel.setPmProxyPreferences(returnValue)));
            }

            UICommand onSaveFalseCommand = UICommand.createDefaultOkUiCommand("OnSaveFalse", HostListModel.this); //$NON-NLS-1$
            hostModel.getCommands().add(onSaveFalseCommand);

            UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", HostListModel.this); //$NON-NLS-1$
            hostModel.getCommands().add(cancelCommand);
        }));

    }

    public void onSaveFalse() {
        onSave(false);
    }

    public void onSave(boolean approveInitiated) {
        HostModel model = (HostModel) getWindow();

        if (!model.validate()) {
            return;
        }

        if (!model.getIsPm().getEntity() && model.getFencingEnabled().getEntity()) {
            if (model.getCluster().getSelectedItem().supportsVirtService()) {
                ConfirmationModel confirmModel = new ConfirmationModel();
                setConfirmWindow(confirmModel);
                confirmModel.setTitle(ConstantsManager.getInstance().getConstants().powerManagementConfigurationTitle());
                confirmModel.setHelpTag(HelpTag.power_management_configuration);
                confirmModel.setHashName("power_management_configuration"); //$NON-NLS-1$
                confirmModel.setMessage(ConstantsManager.getInstance().getConstants().youHavntConfigPmMsg());


                UICommand approveCommand = UICommand.createDefaultOkUiCommand(approveInitiated ? "OnSaveInternalFromApprove" : "OnSaveInternalNotFromApprove", this); //$NON-NLS-1$ //$NON-NLS-2$
                confirmModel.getCommands().add(approveCommand);

                UICommand cancelCommand = new UICommand("CancelConfirmFocusPM", this); //$NON-NLS-1$
                cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().configurePowerManagement());
                cancelCommand.setIsCancel(true);
                confirmModel.getCommands().add(cancelCommand);
            } else {
                if (approveInitiated) {
                    onSaveInternalFromApprove();
                } else {
                    onSaveInternalNotFromApprove();
                }
            }
        } else {
            onSaveInternal(approveInitiated);
        }

    }

    public void cancelConfirmFocusPM() {
        HostModel hostModel = (HostModel) getWindow();
        hostModel.setIsPowerManagementTabSelected(true);

        setConfirmWindow(null);
    }

    public void onSaveInternalNotFromApprove() {
        onSaveInternal(false);
    }

    public void onSaveInternalFromApprove() {
        onSaveInternal(true);
    }

    public void onSaveInternal(boolean approveInitiated) {
        HostModel model = (HostModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        VDS initHost = model.getIsNew()
                ? new VDS()
                : (VDS) Cloner.clone(getSelectedItem());
        Guid oldClusterId = initHost.getClusterId();

        VDS host = VDSMapper.INSTANCE.apply(initHost, model);

        cancelConfirm();
        model.startProgress();

        if (model.getIsNew()) {
            AddVdsActionParameters parameters = AddVdsActionParametersMapper.INSTANCE.apply(host, model);

            Frontend.getInstance().runAction(ActionType.AddVds, parameters,
                    result -> {

                        Object[] array = (Object[]) result.getState();
                        HostListModel<Void> localModel = (HostListModel<Void>) array[0];
                        boolean localApproveInitiated = (Boolean) array[1];
                        localModel.postOnSaveInternal(result.getReturnValue(), localApproveInitiated);

                    }, new Object[] { this, approveInitiated });
        } else { // Update VDS -> consists of changing VDS cluster first and then updating rest of VDS properties:
            UpdateVdsActionParameters parameters = new UpdateVdsActionParameters();
            parameters.setvds(host);
            parameters.setVdsId(host.getId());
            parameters.setPassword(""); //$NON-NLS-1$
            parameters.setInstallHost(false);
            parameters.setRebootHost(model.getRebootHostAfterInstall().getEntity());
            parameters.setAuthMethod(model.getAuthenticationMethod());
            parameters.setFenceAgents(model.getFenceAgentListModel().getFenceAgents());
            parameters.setAffinityGroups(model.getAffinityGroupList().getSelectedItems());
            parameters.setAffinityLabels(model.getLabelList().getSelectedItems());
            if (model.getExternalHostProviderEnabled().getEntity() && model.getProviders().getSelectedItem() != null) {
                host.setHostProviderId(model.getProviders().getSelectedItem().getId());
            }

            if (!oldClusterId.equals(host.getClusterId())) {
                Frontend.getInstance().runAction(ActionType.ChangeVDSCluster,
                        new ChangeVDSClusterParameters(host.getClusterId(), host.getId()),
                        result -> {

                            Object[] array = (Object[]) result.getState();
                            HostListModel<Void> localModel = (HostListModel<Void>) array[0];
                            UpdateVdsActionParameters localParameters = (UpdateVdsActionParameters) array[1];
                            boolean localApproveInitiated = (Boolean) array[2];
                            ActionReturnValue localReturnValue = result.getReturnValue();
                            if (localReturnValue != null && localReturnValue.getSucceeded()) {
                                localModel.postOnSaveInternalChangeCluster(localParameters, localApproveInitiated);
                            } else {
                                localModel.getWindow().stopProgress();
                            }

                        },
                        new Object[] { this, parameters, approveInitiated });
            } else {
                postOnSaveInternalChangeCluster(parameters, approveInitiated);
            }
        }
    }

    public void postOnSaveInternalChangeCluster(UpdateVdsActionParameters parameters, boolean approveInitiated) {
        Frontend.getInstance().runAction(ActionType.UpdateVds, parameters,
                result -> {
                    Object[] array = (Object[]) result.getState();
                    HostListModel<Void> localModel = (HostListModel<Void>) array[0];
                    boolean localApproveInitiated = (Boolean) array[1];
                    localModel.postOnSaveInternal(result.getReturnValue(), localApproveInitiated);

                }, new Object[] { this, approveInitiated });
    }

    public void postOnSaveInternal(ActionReturnValue returnValue, boolean approveInitiated) {
        HostModel model = (HostModel) getWindow();

        model.stopProgress();

        if (returnValue != null && returnValue.getSucceeded()) {
            if (approveInitiated) {
                onApproveInternal();
            }
            cancel();
        }
    }

    private void onApproveInternal() {
        HostModel model = (HostModel) getWindow();
        VDS vds = getSelectedItem();
        ApproveVdsParameters params = new ApproveVdsParameters(vds.getId());
        if (model.getUserPassword().getEntity() != null) {
            params.setPassword(model.getUserPassword().getEntity());
        }
        params.setAuthMethod(model.getAuthenticationMethod());
        params.setActivateHost(model.getActivateHostAfterInstall().getEntity());
        params.setRebootHost(model.getRebootHostAfterInstall().getEntity());

        Frontend.getInstance().runMultipleAction(ActionType.ApproveVds,
                new ArrayList<>(Arrays.asList(params)),
                result -> {

                },
                null);
    }

    public void remove() {
        if (getWindow() != null) {
            return;
        }

        final ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeHostsTitle());
        model.setHelpTag(HelpTag.remove_host);
        model.setHashName("remove_host"); //$NON-NLS-1$

        Set<Guid> clusters = new HashSet<>();
        ArrayList<String> list = new ArrayList<>();
        boolean heOnHosts = false;
        for (VDS item : getSelectedItems()) {
            clusters.add(item.getClusterId());
            String name = item.getName();
            if (item.isHostedEngineDeployed()) {
                name = name + " *"; //$NON-NLS-1$
                heOnHosts = true;
            }
            list.add(name);
        }
        model.setItems(list);

        if (heOnHosts) {
            model.setNote(ConstantsManager.getInstance().getConstants().heHostRemovalWarning());
        }

        // Remove Force option will be shown only if
        // - All the selected hosts belongs to same cluster
        // - the cluster should have  gluster service enabled
        if (clusters.size() == 1) {
            model.startProgress();
            AsyncDataProvider.getInstance().getClusterById(new AsyncQuery<>(cluster -> {
                if (cluster != null && cluster.supportsGlusterService()) {
                    model.getForce().setIsAvailable(true);
                }
                model.stopProgress();
            }), clusters.iterator().next());
        }

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onRemove() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        boolean force = model.getForce().getEntity();
        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            list.add(new RemoveVdsParameters(vds.getId(), force));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.RemoveVds, list,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();
                }, model);
    }

    public void onHostConsole() {
        AsyncDataProvider.getInstance().isOvirtCockpitSSOStarted(new AsyncQuery<>(
                isStarted -> {
                    if (isStarted) {
                        openCockpitWithSSO();
                    } else {
                        openCockpitWithoutSSO();
                    }
                }));
    }

    private String getSsoToken() {
        return JsSingleValueStringObject.getProperty("userInfo", "ssoToken");//$NON-NLS-1$ //$NON-NLS-2$
    }

    private String getEngineSSORootUrl() {
        String protocolSeparator = "://"; //$NON-NLS-1$
        String engineSSOUrl = FrontendUrlUtils.getRootURL(); // like: https://engine.fqdn:8080/

        if (engineSSOUrl.endsWith("/")) {//$NON-NLS-1$
            engineSSOUrl = engineSSOUrl.substring(0, engineSSOUrl.lastIndexOf("/"));//$NON-NLS-1$
        }

        int index = engineSSOUrl.indexOf(protocolSeparator);
        index = (index >= 0) ? index + protocolSeparator.length() : 0; // start of hostname
        index = engineSSOUrl.indexOf(':', index); // start of port
        if (index >= 0) {// port found
            engineSSOUrl = engineSSOUrl.substring(0, index); // remove port number since SSO is served from different one
        }

        return engineSSOUrl;
    }

    private void openCockpitWithSSO() {
        // https://[ENGINE_FQDN]:9986/=[OVIRT_HOST_UUID]/machines#access_token=[VALID_OVIRT_ACCESS_TOKEN]
        String cockpitSSOPort = (String) AsyncDataProvider.getInstance()
                .getConfigValuePreConverted(ConfigValues.CockpitSSOPort);

        String ssoToken = getSsoToken();
        String engineSSOUrl = getEngineSSORootUrl();

        for (VDS item : getSelectedItems()) { // open new browser-tab for every selected host
            StringBuilder cockpitUrl = new StringBuilder();
            cockpitUrl.append(engineSSOUrl); // like: https://[ENGINE_FQDN]
            if (StringHelper.isNotNullOrEmpty(cockpitSSOPort)) {
                cockpitUrl.append(':');
                cockpitUrl.append(cockpitSSOPort);
            }
            cockpitUrl.append("/=");//$NON-NLS-1$
            cockpitUrl.append(item.getId());
            cockpitUrl.append("/machines#access_token=");//$NON-NLS-1$
            cockpitUrl.append(ssoToken);

            getLogger().info("About to open: " + cockpitUrl.toString());//$NON-NLS-1$
            Window.open(cockpitUrl.toString(), "_blank", "");//$NON-NLS-1$
        }
    }

    private void openCockpitWithoutSSO() {
        // https://[HOST_FQDN]:9000
        String cockpitPort = (String) AsyncDataProvider.getInstance()
                .getConfigValuePreConverted(ConfigValues.CockpitPort);
        for (VDS item : getSelectedItems()) { // open new browser-tab for every selected host
            StringBuilder cockpitUrl = new StringBuilder();
            cockpitUrl.append(Uri.SCHEME_HTTPS);
            cockpitUrl.append("://"); //$NON-NLS-1$
            cockpitUrl.append(item.getHostName());
            if (StringHelper.isNotNullOrEmpty(cockpitPort)) {
                cockpitUrl.append(':');
                cockpitUrl.append(cockpitPort);
            }

            getLogger().info("About to open: " + cockpitUrl.toString());//$NON-NLS-1$
            Window.open(cockpitUrl.toString(), "_blank", "");//$NON-NLS-1$
        }
    }

    public void activate() {
        ArrayList<ActionParametersBase> list = new ArrayList<>();

        getSelectedItems().sort(Comparator.comparing(VDS::getVdsSpmPriority).reversed());

        for (VDS vds : getSelectedItems()) {
            list.add(new VdsActionParameters(vds.getId()));
        }

        Frontend.getInstance().runMultipleAction(ActionType.ActivateVds, list,
                result -> {

                }, null);
    }

    private Guid getClusterIdOfSelectedHosts() {
        Guid clusterId = null;
        for (Object item : getSelectedItems()) {
            VDS host = (VDS) item;
            if (clusterId == null) {
                clusterId = host.getClusterId();
            } else if (!clusterId.equals(host.getClusterId())) {
                clusterId = null;
                break;
            }
        }
        return clusterId;
    }

    public void maintenance() {
        Guid clusterId = getClusterIdOfSelectedHosts();
        if (clusterId == null) {
            maintenance(false, null);
        } else {
            AsyncDataProvider.getInstance().getClusterById(new AsyncQuery<>(
                    cluster -> {
                        if (cluster != null) {
                            maintenance(cluster.supportsGlusterService(), clusterId);
                        }
                    }), clusterId);
        }
    }

    private void maintenance(boolean supportsGlusterService, Guid clusterId) {
        if (getConfirmWindow() != null) {
            return;
        }

        HostMaintenanceConfirmationModel model = new HostMaintenanceConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().maintenanceHostsTitle());
        model.setHelpTag(HelpTag.maintenance_host);
        model.setHashName("maintenance_host"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance()
                .getConstants()
                .areYouSureYouWantToPlaceFollowingHostsIntoMaintenanceModeMsg());
        if (supportsGlusterService) {
            model.getStopGlusterServices().setIsAvailable(true);
            model.getStopGlusterServices().setEntity(false);

            model.getForce().setIsAvailable(true);
            model.getForce().setEntity(false);
            model.setForceLabel(ConstantsManager.getInstance().getConstants().ignoreGlusterQuorumChecks());
        }
        // model.Items = SelectedItems.Cast<VDS>().Select(a => a.vds_name);
        List<String> vdssNames = getSelectedItems().stream().map(VDS::getName).collect(Collectors.toList());
        List<Guid> vdssIds = getSelectedItems().stream().map(VDS::getId).collect(Collectors.toList());
        model.setItems(vdssNames);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnMaintenance", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("CancelConfirm", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);

        // Display existence of pinned/hp VMs warning notification
        if (clusterId != null) {
            AsyncDataProvider.getInstance().getAllVmsRunningForMultipleVds(new AsyncQuery<>(vdsToVmsMap -> {
                if (!vdsToVmsMap.isEmpty()) {
                    displayPinnedVmsInfoMsg(model, clusterId, vdsToVmsMap);
                }
            }), vdssIds);
        }
    }

    public void onMaintenance() {
        HostMaintenanceConfirmationModel model = (HostMaintenanceConfirmationModel) getConfirmWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<Guid> vdss = new ArrayList<>();

        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            vdss.add(vds.getId());
        }
        MaintenanceNumberOfVdssParameters params = new MaintenanceNumberOfVdssParameters(vdss,
                false,
                model.getReason().getEntity(),
                model.getStopGlusterServices().getEntity(),
                model.getForce().getEntity());

        cancelConfirm();

        Frontend.getInstance().runAction(ActionType.MaintenanceNumberOfVdss, params);
    }

    public void approve() {
        EditHostModel hostModel = new EditHostModel();
        setWindow(hostModel);
        VDS host = getSelectedItem();

        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery<>(dataCenters -> {
            hostModel.setSelectedCluster(host);
            hostModel.updateModelFromVds(host, dataCenters, false);
            hostModel.setTitle(ConstantsManager.getInstance().getConstants().editAndApproveHostTitle());
            hostModel.setHelpTag(HelpTag.edit_and_approve_host);
            hostModel.setHashName("edit_and_approve_host"); //$NON-NLS-1$

            UICommand tempVar = UICommand.createDefaultOkUiCommand("OnApprove", HostListModel.this); //$NON-NLS-1$
            hostModel.getCommands().add(tempVar);
            UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", HostListModel.this); //$NON-NLS-1$
            hostModel.getCommands().add(tempVar2);
        }));
    }

    public void onApprove() {
        onSave(true);
    }

    public void install() {
        final VDS host = getSelectedItem();
        InstallModel model = new InstallModel();
        model.setVds(host);
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().installHostTitle());
        model.setHelpTag(HelpTag.install_host);
        model.setHashName("install_host"); //$NON-NLS-1$
        model.getOVirtISO().setIsAvailable(false);

        model.getOverrideIpTables().setIsAvailable(false);

        model.getHostVersion().setEntity(host.getHostOs());
        model.getHostVersion().setIsAvailable(false);

        getWindow().startProgress();
        model.getUserPassword().setIsAvailable(true);
        model.getUserPassword().setIsChangeable(true);

        model.getOverrideIpTables().setIsAvailable(true);
        model.getOverrideIpTables().setEntity(true);
        model.getActivateHostAfterInstall().setEntity(true);
        model.getReconfigureGluster().setIsAvailable(false);
        addInstallCommands(model, host);
        getWindow().stopProgress();
    }

    private void addInstallCommands(InstallModel model, VDS host) {
        model.getCommands()
                .add(UICommand.createDefaultOkUiCommand("OnInstall", this)); //$NON-NLS-1$
        model.getUserName().setEntity(host.getSshUsername());
        model.getCommands()
                .add(new UICommand("Cancel", this) //$NON-NLS-1$
                        .setTitle(ConstantsManager.getInstance().getConstants().cancel())
                        .setIsCancel(true));
    }

    public void onInstall() {
        final VDS host = getSelectedItem();
        InstallModel model = (InstallModel) getWindow();

        final UpdateVdsActionParameters param = new UpdateVdsActionParameters();
        param.setvds(host);
        param.setVdsId(host.getId());
        param.setPassword(model.getUserPassword().getEntity());
        param.setReinstallOrUpgrade(true);
        param.setInstallHost(true);
        param.setoVirtIsoFile(null);
        param.setOverrideFirewall(model.getOverrideIpTables().getEntity());
        param.setReconfigureGluster(model.getReconfigureGluster().getEntity());
        param.setFqdnBox(model.getFqdnBox().getEntity());
        param.setActivateHost(model.getActivateHostAfterInstall().getEntity());
        param.setRebootHost(model.getRebootHostAfterInstall().getEntity());
        param.setAuthMethod(model.getAuthenticationMethod());
        param.setFenceAgents(null);  // Explicitly set null, to be clear we don't want to update fence agents.
        param.setHostedEngineDeployConfiguration(
                new HostedEngineDeployConfiguration(model.getHostedEngineHostModel().getSelectedItem()));
        param.setReplaceHostConfiguration(
                new ReplaceHostConfiguration(model.getReplaceHostModel().getSelectedItem()));
        AsyncDataProvider.getInstance().getClusterById(new AsyncQuery<>(returnValue -> Frontend.getInstance().runAction(
                ActionType.InstallVds,
                param,
                result -> {
                    ActionReturnValue returnValue1 = result.getReturnValue();
                    if (returnValue1 != null && returnValue1.getSucceeded()) {
                        cancel();
                    }
                }
        )), host.getClusterId());


    }

    public void checkForUpgrade() {
        final VDS host = getSelectedItem();
        Model model = new HostUpgradeCheckConfirmationModel(host);
        setWindow(model);
        model.initialize();
        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); // $NON-NLS-1$
    }

    public void upgrade() {
        final VDS host = getSelectedItem();
        Model model = createUpgradeModel(host);
        setWindow(model);
        model.initialize();
        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); // $NON-NLS-1$
    }

    public void sshRestart() {
        restart("OnSshRestart");  //$NON-NLS-1$
    }

    public void restart() {
        restart("OnRestart");  //$NON-NLS-1$
    }

    public void stop() {
        stop("OnStop");  //$NON-NLS-1$
    }

    public void sshStop() {
        stop("OnSshStop");  //$NON-NLS-1$
    }

    public void restart(String uiCommand) {
        final UIConstants constants = ConstantsManager.getInstance().getConstants();
        final UIMessages messages = ConstantsManager.getInstance().getMessages();
        HostRestartConfirmationModel model = new HostRestartConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(constants.restartHostsTitle());
        model.setHelpTag(HelpTag.restart_host);
        model.setHashName("restart_host"); //$NON-NLS-1$
        model.setMessage(constants.areYouSureYouWantToRestartTheFollowingHostsMsg());
        ArrayList<String> items = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            int runningVms = vds.getVmCount();
            if (runningVms > 0) {
                items.add(messages.hostNumberOfRunningVms(vds.getName(), runningVms));
            } else {
                items.add(vds.getName());
            }
        }
        model.setItems(items);

        UICommand tempVar = new UICommand(uiCommand, this); //$NON-NLS-1$
        tempVar.setTitle(constants.ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(constants.cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void onSshStop() {
        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();
        if (model.getProgress() != null) {
            return;
        }
        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            VdsPowerDownParameters param = new VdsPowerDownParameters(vds.getId());
            param.setFallbackToPowerManagement(false);
            param.setKeepPolicyPMEnabled(true);
            list.add(param);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(
            ActionType.VdsPowerDown, list,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancelConfirm();
                },
            model
        );
    }

    public void onSshRestart() {
        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();
        if (model.getProgress() != null) {
            return;
        }
        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            SshHostRebootParameters params = new SshHostRebootParameters(vds.getId());
            params.setPrevVdsStatus(vds.getStatus());
            list.add(params);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(
            ActionType.SshHostReboot, list,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancelConfirm();
                },
                model
        );
    }

    public void onRestart() {
        HostRestartConfirmationModel model = (HostRestartConfirmationModel) getConfirmWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            FenceVdsActionParameters parameters = new FenceVdsActionParameters(vds.getId());
            parameters.setChangeHostToMaintenanceOnStart(model.getForceToMaintenance().getEntity());
            list.add(parameters);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.RestartVds, list,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancelConfirm();

                }, model);
    }

    public void start() {
        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            list.add(new FenceVdsActionParameters(vds.getId()));
        }

        Frontend.getInstance().runMultipleAction(ActionType.StartVds, list,
                result -> {

                }, null);
    }

    public void stop(String uiCommand) {
        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().stopHostsTitle());
        model.setHelpTag(HelpTag.stop_host);
        model.setHashName("stop_host"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().areYouSureYouWantToStopTheFollowingHostsMsg());
        // model.Items = SelectedItems.Cast<VDS>().Select(a => a.vds_name);
        ArrayList<String> items = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            items.add(vds.getName());
        }
        model.setItems(items);

        UICommand tempVar = UICommand.createDefaultOkUiCommand(uiCommand, this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onStop() {
        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            list.add(new FenceVdsActionParameters(vds.getId()));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.StopVds, list,
                result -> {

                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancelConfirm();

                }, model);
    }

    private void configureLocalStorage() {
        if (getWindow() != null) {
            return;
        }

        ConfigureLocalStorageModel model = new ConfigureLocalStorageModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().configureLocalStorageTitle());
        model.setHelpTag(HelpTag.configure_local_storage);
        model.setHashName("configure_local_storage"); //$NON-NLS-1$

        configureLocalStorage3(model);
    }

    private void configureLocalStorage3(ConfigureLocalStorageModel model) {
        VDS host = getSelectedItem();
        model.setDefaultNames(host);

        UICommand onConfigureLocalStorageCommand = UICommand.createDefaultOkUiCommand("OnConfigureLocalStorage", this); //$NON-NLS-1$
        model.getCommands().add(onConfigureLocalStorageCommand);

        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    private void onConfigureLocalStorage() {

        ConfigureLocalStorageModel model = (ConfigureLocalStorageModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (!model.validate()) {
            return;
        }

        model.startProgress(ConstantsManager.getInstance().getConstants().configuringLocalStorageHost());

        ReversibleFlow flow = new ReversibleFlow();
        flow.getCompleteEvent().addListener(
                (ev, sender, args) -> {

                    ConfigureLocalStorageModel model1 = (ConfigureLocalStorageModel) ev.getContext();

                    model1.stopProgress();
                    cancel();
                },
                model);

        String correlationId = TaskListModel.createCorrelationId("Configure Local Storage"); //$NON-NLS-1$
        flow.enlist(new AddDataCenterRM(correlationId));
        flow.enlist(new AddClusterRM(correlationId));
        flow.enlist(new ChangeHostClusterRM(correlationId));
        flow.enlist(new AddStorageDomainRM(correlationId));

        flow.run(new EnlistmentContext(this));
    }

    private void refreshCapabilities() {
        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            list.add(new VdsActionParameters(vds.getId()));
        }

        Frontend.getInstance().runMultipleAction(ActionType.RefreshHost, list,
                result -> {

                }, null);
    }

    private void enrollCertificate() {
        final VDS host = getSelectedItem();
        Frontend.getInstance().runAction(ActionType.HostEnrollCertificate, new VdsActionParameters(host.getId()));
    }

    private void updateHaMaintenanceAvailability() {
        VDS vds = getSelectedItem();
        boolean singleVdsSelected = singleHostSelected(getSelectedItems());
        boolean haConfigured = vds != null && vds.getHighlyAvailableIsConfigured();
        boolean inGlobalMaintenance = vds != null && vds.getHighlyAvailableGlobalMaintenance();

        getEnableGlobalHaMaintenanceCommand().setIsExecutionAllowed(!inGlobalMaintenance
                && haConfigured && singleVdsSelected);
        getDisableGlobalHaMaintenanceCommand().setIsExecutionAllowed(inGlobalMaintenance
                && haConfigured && singleVdsSelected);
    }

    private void setGlobalHaMaintenance(boolean enabled) {
        SetHaMaintenanceParameters params = new SetHaMaintenanceParameters(HaMaintenanceMode.GLOBAL, enabled);
        Frontend.getInstance().runAction(ActionType.SetHaMaintenance, params);
    }

    @Override
    protected void updateDetailsAvailability() {
        super.updateDetailsAvailability();
        VDS vds = getSelectedItem();
        getGlusterSwiftModel().setIsAvailable(false);
        getBricksListModel().setIsAvailable(vds != null && vds.getClusterSupportsGlusterService());
        getVmListModel().setIsAvailable(vds != null && vds.getClusterSupportsVirtService());
        getGlusterStorageDeviceListModel().setIsAvailable(vds != null && vds.getClusterSupportsGlusterService());
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(HostGeneralModel.requestEditEventDefinition)) {
            getEditWithPMemphasisCommand().execute();
        }
        if (ev.matchesDefinition(HostGeneralModel.requestGOToEventsTabEventDefinition)) {
            goToEventsTab();
        }
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("host"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        SearchParameters tempVar = new SearchParameters(applySortOptions(getModifiedSearchString()), SearchType.VDS,
                isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(QueryType.Search, tempVar);
    }

    @Override
    public boolean supportsServerSideSorting() {
        return true;
    }

    public void cancel() {
        cancelConfirm();
        setWindow(null);
        fireModelChangeRelevantForActionsEvent();
    }

    public void cancelConfirm() {
        setConfirmWindow(null);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.selectedItemPropertyChanged(sender, e);

        if (e.propertyName.equals("status") || e.propertyName.equals("pm_enabled")) { //$NON-NLS-1$ //$NON-NLS-2$
            updateActionAvailability();
        }
    }

    private void updateActionAvailability() {
        List<VDS> items = getSelectedItems() != null ? getSelectedItems() : new ArrayList<VDS>();

        boolean isAllPMEnabled = items.stream().allMatch(VDS::isPmEnabled);

        getEditCommand().setIsExecutionAllowed(items.size() == 1
                && ActionUtils.canExecute(items, VDS.class, ActionType.UpdateVds));

        getEditWithPMemphasisCommand().setIsExecutionAllowed(items.size() == 1
                && ActionUtils.canExecute(items, VDS.class, ActionType.UpdateVds));

        getRemoveCommand().setIsExecutionAllowed(items.size() > 0
                && ActionUtils.canExecute(items, VDS.class, ActionType.RemoveVds));

        getActivateCommand().setIsExecutionAllowed(items.size() > 0
                && ActionUtils.canExecute(items, VDS.class, ActionType.ActivateVds));

        boolean approveAvailability =
                items.size() == 1
                        && (ActionUtils.canExecute(items, VDS.class, ActionType.ApproveVds));
        getApproveCommand().setIsExecutionAllowed(approveAvailability);

        boolean installAvailability = false;
        if (singleHostSelected(items)) {
            VDS host = items.get(0);
            installAvailability = host.getStatus() == VDSStatus.InstallFailed ||
                    host.getStatus() == VDSStatus.Maintenance;
        }
        getInstallCommand().setIsExecutionAllowed(installAvailability);

        boolean webConsoleAvailability = singleHostSelected(items);
        getHostConsoleCommand().setIsExecutionAllowed(webConsoleAvailability);

        boolean checkForUpgradeAvailability = false;
        if (singleHostSelected(items)) {
            VDS host = items.get(0);
            checkForUpgradeAvailability = canCheckForHostUpgrade(host);
        }
        getCheckForUpgradeCommand().setIsExecutionAllowed(checkForUpgradeAvailability);

        boolean upgradeAvailability = false;
        if (singleHostSelected(items)) {
            VDS host = items.get(0);
            upgradeAvailability = canUpgradeHost(host);
        }
        getUpgradeCommand().setIsExecutionAllowed(upgradeAvailability);
        getEnrollCertificateCommand().setIsExecutionAllowed(installAvailability);

        getMaintenanceCommand().setIsExecutionAllowed(items.size() > 0
                && ActionUtils.canExecute(items, VDS.class, ActionType.MaintenanceVds));

        getSshRestartCommand().setIsExecutionAllowed(items.size() > 0
            && ActionUtils.canExecute(items, VDS.class, ActionType.SshHostReboot));

        getSshStopCommand().setIsExecutionAllowed(items.size() > 0
            && ActionUtils.canExecute(items, VDS.class, ActionType.VdsPowerDown));

        getRestartCommand().setIsExecutionAllowed(items.size() > 0
                && ActionUtils.canExecute(items, VDS.class, ActionType.RestartVds) && isAllPMEnabled);

        getStartCommand().setIsExecutionAllowed(items.size() > 0
                && ActionUtils.canExecute(items, VDS.class, ActionType.StartVds) && isAllPMEnabled);

        getStopCommand().setIsExecutionAllowed(items.size() > 0
                && ActionUtils.canExecute(items, VDS.class, ActionType.StopVds) && isAllPMEnabled);

        setIsPowerManagementEnabled(getRestartCommand().getIsExecutionAllowed()
                || getStartCommand().getIsExecutionAllowed() || getStopCommand().getIsExecutionAllowed());

        getManualFenceCommand().setIsExecutionAllowed(
                items.size() == 1 && items.get(0) != null && items.get(0).isManaged());

        getAssignTagsCommand().setIsExecutionAllowed(items.size() > 0);

        getSelectAsSpmCommand().setIsExecutionAllowed(isSelectAsSpmCommandAllowed(items));

        updateConfigureLocalStorageCommandAvailability();

        getRefreshCapabilitiesCommand().setIsExecutionAllowed(items.size() > 0 && ActionUtils.canExecute(items,
                VDS.class,
                ActionType.RefreshHostCapabilities));

        boolean numaVisible = false;
        if (getSelectedItem() != null) {
            numaVisible = getSelectedItem().isNumaSupport();
        }
        getNumaSupportCommand().setIsVisible(numaVisible);

        updateHaMaintenanceAvailability();
        fireModelChangeRelevantForActionsEvent();
    }

    private boolean canCheckForHostUpgrade(VDS host) {
        return ActionUtils.canExecute(Arrays.asList(host), VDS.class, ActionType.HostUpgradeCheck);
    }

    private boolean canUpgradeHost(VDS host) {
        return host.isUpdateAvailable()
                && ActionUtils.canExecute(Arrays.asList(host), VDS.class, ActionType.UpgradeHost);
    }

    private boolean singleHostSelected(List<VDS> items) {
        return items != null && items.size() == 1 && items.get(0) != null;
    }

    private Boolean hasAdminSystemPermission = null;

    public void updateConfigureLocalStorageCommandAvailability() {

        if (hasAdminSystemPermission == null) {

            DbUser dbUser = Frontend.getInstance().getLoggedInUser();

            if (dbUser == null) {
                hasAdminSystemPermission = false;
                updateConfigureLocalStorageCommandAvailability1();
                return;
            }

            Frontend.getInstance().runQuery(QueryType.GetPermissionsByAdElementId,
                    new IdQueryParameters(dbUser.getId()),
                    new AsyncQuery<QueryReturnValue>(response -> {
                        if (response == null || !response.getSucceeded()) {
                            hasAdminSystemPermission = false;
                            updateConfigureLocalStorageCommandAvailability1();
                        } else {
                            ArrayList<Permission> permissions =
                                    response.getReturnValue();
                            for (Permission permission : permissions) {

                                if (permission.getObjectType() == VdcObjectType.System
                                        && permission.getRoleType() == RoleType.ADMIN) {
                                    hasAdminSystemPermission = true;
                                    break;
                                }
                            }

                            updateConfigureLocalStorageCommandAvailability1();
                        }

                    }, true));
        } else {
            updateConfigureLocalStorageCommandAvailability1();
        }
    }

    private void updateConfigureLocalStorageCommandAvailability1() {

        List<VDS> items = getSelectedItems() != null ? getSelectedItems() : new ArrayList<VDS>();

        getConfigureLocalStorageCommand().setIsExecutionAllowed(items.size() == 1
                && items.get(0).getStatus() == VDSStatus.Maintenance);

        if (!Boolean.TRUE.equals(hasAdminSystemPermission) && getConfigureLocalStorageCommand().getIsExecutionAllowed()) {

            getConfigureLocalStorageCommand().getExecuteProhibitionReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .configuringLocalStoragePermittedOnlyAdministratorsWithSystemLevelPermissionsReason());
            getConfigureLocalStorageCommand().setIsExecutionAllowed(false);
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newEntity();
        } else if (command == getEditCommand()) {
            edit(false);
        } else if (command == getEditWithPMemphasisCommand()) {
            edit(true);
        } else if (command == getRemoveCommand()) {
            remove();
        } else if (command == getSelectAsSpmCommand()) {
            selectAsSPM();
        } else if (command == getActivateCommand()) {
            activate();
        } else if (command == getMaintenanceCommand()) {
            maintenance();
        } else if (command == getApproveCommand()) {
            approve();
        } else if (command == getInstallCommand()) {
            install();
        } else if (command == getCheckForUpgradeCommand()) {
            checkForUpgrade();
        } else if (command == getUpgradeCommand()) {
            upgrade();
        } else if (command == getSshRestartCommand()) {
            sshRestart();
        } else if (command == getSshStopCommand()) {
            sshStop();
        } else if (command == getRestartCommand()) {
            restart();
        } else if (command == getStartCommand()) {
            start();
        } else if (command == getStopCommand()) {
            stop();
        } else if (command == getManualFenceCommand()) {
            manualFence();
        } else if (command == getAssignTagsCommand()) {
            assignTags();
        } else if (command == getConfigureLocalStorageCommand()) {
            configureLocalStorage();
        } else if (command == getRefreshCapabilitiesCommand()) {
            refreshCapabilities();
        } else if (command == getEnrollCertificateCommand()) {
            enrollCertificate();
        } else if (command == getNumaSupportCommand()) {
            numaSupport();
        } else if (command == getEnableGlobalHaMaintenanceCommand()) {
            setGlobalHaMaintenance(true);
        } else if (command == getDisableGlobalHaMaintenanceCommand()) {
            setGlobalHaMaintenance(false);
        } else if (command == getHostConsoleCommand()) {
            onHostConsole();
        } else if ("OnAssignTags".equals(command.getName())) { //$NON-NLS-1$
            onAssignTags();
        } else if ("OnManualFence".equals(command.getName())) { //$NON-NLS-1$
            onManualFence();
        } else if ("OnSaveFalse".equals(command.getName())) { //$NON-NLS-1$
            onSaveFalse();
        } else if ("OnSaveInternalFromApprove".equals(command.getName())) { //$NON-NLS-1$
            onSaveInternalFromApprove();
        } else if ("OnSaveInternalNotFromApprove".equals(command.getName())) { //$NON-NLS-1$
            onSaveInternalNotFromApprove();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if ("CancelConfirm".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirm();
        } else if ("CancelConfirmFocusPM".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirmFocusPM();
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        } else if ("OnMaintenance".equals(command.getName())) { //$NON-NLS-1$
            onMaintenance();
        } else if ("OnApprove".equals(command.getName())) { //$NON-NLS-1$
            onApprove();
        } else if ("OnInstall".equals(command.getName())) { //$NON-NLS-1$
            onInstall();
        } else if ("OnSshRestart".equals(command.getName())) { //$NON-NLS-1$
            onSshRestart();
        } else if ("OnSshStop".equals(command.getName())) { //$NON-NLS-1$
            onSshStop();
        } else if ("OnRestart".equals(command.getName())) { //$NON-NLS-1$
            onRestart();
        } else if ("OnStop".equals(command.getName())) { //$NON-NLS-1$
            onStop();
        } else if ("OnConfigureLocalStorage".equals(command.getName())) { //$NON-NLS-1$
            onConfigureLocalStorage();
        } else if (NumaSupportModel.SUBMIT_NUMA_SUPPORT.equals(command.getName())) {
            onNumaSupport();
        }
    }

    private void numaSupport() {
        if (getWindow() != null) {
            return;
        }

        VDS host = getSelectedItem();
        List<VDS> hosts = getSelectedItems();

        NumaSupportModel model = new NumaSupportModel(hosts, host, this);
        setWindow(model);
    }

    private void onNumaSupport() {
        if (getWindow() == null) {
            return;
        }
        NumaSupportModel model = (NumaSupportModel) getWindow();
        ArrayList<ActionParametersBase> updateParamsList = model.getUpdateParameters();
        if (!updateParamsList.isEmpty()) {
            Frontend.getInstance().runMultipleAction(ActionType.UpdateVmNumaNodes, updateParamsList);
        }
        setWindow(null);
    }

    private void selectAsSPM() {
        ForceSelectSPMParameters params = new ForceSelectSPMParameters(getSelectedItem().getId());
        Frontend.getInstance().runAction(ActionType.ForceSelectSPM, params);

    }

    private boolean isSelectAsSpmCommandAllowed(List<VDS> selectedItems) {
        if (selectedItems.size() != 1) {
            return false;
        }

        VDS vds = selectedItems.get(0);

        if (vds.getStatus() != VDSStatus.Up || !vds.getClusterSupportsVirtService()
                || vds.getSpmStatus() != VdsSpmStatus.None) {
            return false;
        }

        return vds.getVdsSpmPriority() != BusinessEntitiesDefinitions.HOST_MIN_SPM_PRIORITY;
    }

    @Override
    protected String getListName() {
        return "HostListModel"; //$NON-NLS-1$
    }

    private boolean isHeSystem() {
        boolean isRunningHeVm = false;

        Collection<VDS> items = getItems();
        if (items != null) {
            for (VDS host : items) {
                if (host.isHostedEngineHost()) {
                    isRunningHeVm = true;
                    break;
                }
            }
        }

        return isRunningHeVm;
    }

    private List<Guid> getHostsWithHeDeployed() {
        List<Guid> hostsWithHeDeployed = new ArrayList<>();

        for (VDS host : getItems()) {
            if (host.isHostedEngineDeployed()) {
                hostsWithHeDeployed.add(host.getId());
            }
        }

        return hostsWithHeDeployed;
    }

    private void displayPinnedVmsInfoMsg(HostMaintenanceConfirmationModel model, Guid clusterId, Map<Guid, List<VM>> vdsToVmsMap) {
        AsyncDataProvider.getInstance().getVMsWithVNumaNodesByClusterId(new AsyncQuery<>(returnValue -> {
            List<String> pinnedVmsNames = new ArrayList<>();
            List<Guid> hostsIdsForPinnedVms = new ArrayList<>();

            findHpOrPinnedVms(vdsToVmsMap, returnValue, pinnedVmsNames, hostsIdsForPinnedVms);
            formatAndDisplayPinnedVmsMsg(pinnedVmsNames, hostsIdsForPinnedVms, model);
        }), clusterId);
    }

    /**
     * Find pinned/hp VMs running on selected hosts to maintenance and the hosts that those VMs are running on
     *
     * @param pinnedVmsNames output parameter that will include list of pinned/Hp vms names identified on selected hosts to maintenance
     * @param hostsIdsForPinnedVms output parameter that will include list of hosts ids for hosts that pinnedVmsNames are running on
     */
    private void findHpOrPinnedVms(Map<Guid, List<VM>> vdsToVmsMap, List<VM> vmsWithvNumaNodesConf, List<String> pinnedVmsNames, List<Guid> hostsIdsForPinnedVms) {
        Set<Guid> vmsWithPinnedvNumaNodes = getOnlyVmsWithPinnedvNumaNodes(vmsWithvNumaNodesConf);

        for (Map.Entry<Guid, List<VM>> entry : vdsToVmsMap.entrySet()) {
            List<String> currHostPinnedVmsNames = entry.getValue().stream().filter(v -> isVmHpOrPinningConfigurationEnabled(v, vmsWithPinnedvNumaNodes)).map(v -> v.getName()).collect(Collectors.toList());
            pinnedVmsNames.addAll(currHostPinnedVmsNames);
            if (!currHostPinnedVmsNames.isEmpty()) {
                hostsIdsForPinnedVms.add(entry.getKey());
            }
        }
    }

    /**
     * Format the message of pinned/high performance VMs existed on selected hosts to maintenance and display it on HostMaintenanceConfirmationModel dialog
     *
     */
    private void formatAndDisplayPinnedVmsMsg(List<String> pinnedVmsNames, List<Guid> hostsIdsForPinnedVms, HostMaintenanceConfirmationModel model) {
        if (!pinnedVmsNames.isEmpty()) {
            List<String> hostsNamesForPinnedVms = getSelectedItems().stream()
                    .filter(d -> hostsIdsForPinnedVms.contains(d.getId()))
                    .map(VDS::getName)
                    .collect(Collectors.toList());

            String formatedPinnedVMsInfoMsg = ConstantsManager.getInstance()
                    .getConstants()
                    .areYouSureYouWantToPlaceFollowingHostsIntoMaintenanceModeDueToPinnedVmsMsg()
                    + "VM(s): " //$NON-NLS-1$
                    + String.join(", ", pinnedVmsNames) //$NON-NLS-1$
                    + "\nHost(s): " //$NON-NLS-1$
                    + String.join(", ", hostsNamesForPinnedVms); //$NON-NLS-1$
            model.setPinnedVMsInfoPanelVisible(true);
            model.setPinnedVMsInfoMessage(formatedPinnedVMsInfoMsg);
        }
    }

    /**
     * Return VMs that include vNUMA pinning to hosts NUMAs
     */
    private Set<Guid> getOnlyVmsWithPinnedvNumaNodes(List<VM> vmsWithvNumaNodesConf) {
        Set<Guid> vmsWithPinnedvNumaNodes = new HashSet<>();

        for (final VM vm : vmsWithvNumaNodesConf) {
            if (vm.getvNumaNodeList() != null
                    && vm.getvNumaNodeList().stream().anyMatch(node -> !node.getVdsNumaNodeList().isEmpty())) {
                vmsWithPinnedvNumaNodes.add(vm.getId());
            }
        }
        return vmsWithPinnedvNumaNodes;
    }

    /**
     * Return true if VM includes pinning configuration or if it is a high performance vm type
     */
    private boolean isVmHpOrPinningConfigurationEnabled(VM vm, Set<Guid> vmsWithPinnedvNumaNodes) {
        return vm.getVmType() == VmType.HighPerformance
                || vm.isUsingCpuPassthrough()
                || (vm.getCpuPinning() != null && !vm.getCpuPinning().isEmpty())
                || vm.getCpuPinningPolicy() == CpuPinningPolicy.RESIZE_AND_PIN_NUMA
                || vmsWithPinnedvNumaNodes.contains(vm.getId());
    }
}
