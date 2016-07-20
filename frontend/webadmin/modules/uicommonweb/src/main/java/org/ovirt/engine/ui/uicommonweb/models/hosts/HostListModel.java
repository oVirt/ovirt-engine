package org.ovirt.engine.ui.uicommonweb.models.hosts;

import static org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel.createUpgradeModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.ForceSelectSPMParameters;
import org.ovirt.engine.core.common.action.MaintenanceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.hostdeploy.ApproveVdsParameters;
import org.ovirt.engine.core.common.action.hostdeploy.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.ExternalComputeResource;
import org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost;
import org.ovirt.engine.core.common.businessentities.ExternalHostGroup;
import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.pm.FenceProxySourceTypeHelper;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.TagsEqualityComparer;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsAndReportsModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
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
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.NotifyCollectionChangedEventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.ReversibleFlow;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.inject.Inject;

@SuppressWarnings("unchecked")
public class HostListModel<E> extends ListWithDetailsAndReportsModel<E, VDS> implements ISupportSystemTreeContext {
    private final HostGeneralModel generalModel;

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

    private UICommand privateUpgradeCommand;

    public UICommand getUpgradeCommand() {
        return privateUpgradeCommand;
    }

    private void setUpgradeCommand(UICommand value) {
        privateUpgradeCommand = value;
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

    private final HostEventListModel privateHostEventListModel;

    private HostEventListModel getHostEventListModel() {
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

    private final HostBricksListModel hostBricksListModel;

    public HostBricksListModel getHostBricksListModel() {
        return hostBricksListModel;
    }

    private HostGlusterStorageDevicesListModel glusterStorageDeviceListModel;

    public HostGlusterStorageDevicesListModel getGlusterStorageDeviceListModel() {
        return glusterStorageDeviceListModel;
    }

    public void setGlusterStorageDeviceListModel(HostGlusterStorageDevicesListModel storageDeviceListModel) {
        this.glusterStorageDeviceListModel = storageDeviceListModel;
    }

    private final HostVmListModel hostVmListModel;

    public HostVmListModel getHostVmListModel() {
        return this.hostVmListModel;
    }

    protected Object[] getSelectedKeys() {
        if (getSelectedItems() == null) {
            return new Object[0];
        }
        else {
            Object[] keys = new Object[getSelectedItems().size()];
            for (int i = 0; i < getSelectedItems().size(); i++) {
                keys[i] = getSelectedItems().get(i).getId();
            }
            return keys;
        }
    }

    @Inject
    public HostListModel(final HostGeneralModel hostGeneralModel,
            final HostGlusterSwiftListModel hostGlusterSwiftListModel, final HostBricksListModel hostBricksListModel,
            final HostVmListModel hostVmListModel, final HostEventListModel hostEventListModel,
            final HostInterfaceListModel hostInterfaceListModel, final HostDeviceListModel hostDeviceListModel,
            final HostHardwareGeneralModel hostHardwareGeneralModel, final HostHooksListModel hostHooksListModel,
            final PermissionListModel<VDS> permissionListModel,
            final HostGlusterStorageDevicesListModel glusterStorageDeviceListModel) {
        this.generalModel = hostGeneralModel;
        this.glusterSwiftModel = hostGlusterSwiftListModel;
        this.hostBricksListModel = hostBricksListModel;
        this.hostVmListModel = hostVmListModel;
        this.privateHostEventListModel = hostEventListModel;
        this.glusterStorageDeviceListModel = glusterStorageDeviceListModel;

        setDetailList(hostInterfaceListModel, hostHardwareGeneralModel, hostHooksListModel, permissionListModel, hostDeviceListModel);

        setTitle(ConstantsManager.getInstance().getConstants().hostsTitle());
        setHelpTag(HelpTag.hosts);
        setApplicationPlace(WebAdminApplicationPlaces.hostMainTabPlace);
        setHashName("hosts"); //$NON-NLS-1$

        setDefaultSearchString("Host:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.VDS_OBJ_NAME, SearchObjects.VDS_PLU_OBJ_NAME });

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setEditWithPMemphasisCommand(new UICommand("EditWithPMemphasis", this)); //$NON-NLS-1$
        setSelectAsSpmCommand(new UICommand("SelectAsSpm", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setActivateCommand(new UICommand("Activate", this, true)); //$NON-NLS-1$
        setMaintenanceCommand(new UICommand("Maintenance", this, true)); //$NON-NLS-1$
        setApproveCommand(new UICommand("Approve", this)); //$NON-NLS-1$
        setInstallCommand(new UICommand("Install", this)); //$NON-NLS-1$
        setUpgradeCommand(new UICommand("Upgrade", this)); //$NON-NLS-1$
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

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);

        getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateAvailableOvirtNodeUpgrades();
            }
        });

    }

    private void setDetailList(final HostInterfaceListModel hostInterfaceListModel,
                               final HostHardwareGeneralModel hostHardwareGeneralModel, final HostHooksListModel hostHooksListModel,
                               final PermissionListModel<VDS> permissionListModel, final HostDeviceListModel hostDeviceListModel) {
        generalModel.getRequestEditEvent().addListener(this);
        generalModel.getRequestGOToEventsTabEvent().addListener(this);

        List<HasEntity<VDS>> list = new ArrayList<>();
        list.add(generalModel);
        list.add(hostHardwareGeneralModel);
        list.add(getHostVmListModel());
        list.add(hostInterfaceListModel);
        list.add(hostDeviceListModel);
        list.add(getHostEventListModel());
        list.add(hostHooksListModel);
        list.add(getGlusterSwiftModel());
        list.add(getHostBricksListModel());
        list.add(getGlusterStorageDeviceListModel());
        list.add(permissionListModel);
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
    public ArrayList<Tags> allAttachedTags;
    public int selectedItemsCounter;

    private void getAttachedTagsToSelectedHosts(TagListModel model) {
        ArrayList<Guid> hostIds = new ArrayList<>();

        for (VDS vds : getSelectedItems()) {
            hostIds.add(vds.getId());
        }

        attachedTagsToEntities = new HashMap<>();
        allAttachedTags = new ArrayList<>();
        selectedItemsCounter = 0;

        for (Guid hostId : hostIds) {
            AsyncDataProvider.getInstance().getAttachedTagsToHost(new AsyncQuery(new Object[] { this, model },
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            Object[] array = (Object[]) target;
                            HostListModel<Void> hostListModel = (HostListModel<Void>) array[0];
                            TagListModel tagListModel = (TagListModel) array[1];
                            hostListModel.allAttachedTags.addAll((ArrayList<Tags>) returnValue);
                            hostListModel.selectedItemsCounter++;
                            if (hostListModel.selectedItemsCounter == hostListModel.getSelectedItems().size()) {
                                postGetAttachedTags(hostListModel, tagListModel);
                            }

                        }
                    }),
                    hostId);
        }
    }

    private void postGetAttachedTags(HostListModel<Void> hostListModel, TagListModel tagListModel) {
        if (hostListModel.getLastExecutedCommand() == getAssignTagsCommand()) {
            ArrayList<Tags> attachedTags =
                    Linq.distinct(hostListModel.allAttachedTags, new TagsEqualityComparer());
            for (Tags tag : attachedTags) {
                int count = 0;
                for (Tags tag2 : hostListModel.allAttachedTags) {
                    if (tag2.getTagId().equals(tag.getTagId())) {
                        count++;
                    }
                }
                hostListModel.attachedTagsToEntities.put(tag.getTagId(), count == hostListModel.getSelectedItems()
                        .size());
            }
            tagListModel.setAttachedTagsToEntities(hostListModel.attachedTagsToEntities);
        }
        else if ("OnAssignTags".equals(hostListModel.getLastExecutedCommand().getName())) { //$NON-NLS-1$
            hostListModel.postOnAssignTags(tagListModel.getAttachedTagsToEntities());
        }
    }

    public void onAssignTags() {
        TagListModel model = (TagListModel) getWindow();

        getAttachedTagsToSelectedHosts(model);
    }

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

        ArrayList<VdcActionParametersBase> prmsToAttach = new ArrayList<>();
        for (Guid tag_id : tagsToAttach) {
            prmsToAttach.add(new AttachEntityToTagParameters(tag_id, hostIds));
        }
        Frontend.getInstance().runMultipleAction(VdcActionType.AttachVdsToTag, prmsToAttach);

        ArrayList<VdcActionParametersBase> prmsToDetach = new ArrayList<>();
        for (Guid tag_id : tagsToDetach) {
            prmsToDetach.add(new AttachEntityToTagParameters(tag_id, hostIds));
        }
        Frontend.getInstance().runMultipleAction(VdcActionType.DetachVdsFromTag, prmsToDetach);

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

        ArrayList<VdcActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            FenceVdsManualyParameters parameters = new FenceVdsManualyParameters(true);
            parameters.setStoragePoolId(vds.getStoragePoolId());
            parameters.setVdsId(vds.getId());
            list.add(parameters);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(VdcActionType.FenceVdsManualy, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }
                }, model);
    }

    boolean updateOverrideIpTables = true;
    boolean clusterChanging = false;

    public void newEntity() {
        if (getWindow() != null) {
            return;
        }

        final NewHostModel hostModel = new NewHostModel();
        setWindow(hostModel);
        hostModel.setTitle(ConstantsManager.getInstance().getConstants().newHostTitle());
        hostModel.setHelpTag(HelpTag.new_host);
        hostModel.setHashName("new_host"); //$NON-NLS-1$
        hostModel.getPort().setEntity(54321);
        hostModel.getOverrideIpTables().setIsAvailable(false);
        hostModel.setSpmPriorityValue(null);
        hostModel.getConsoleAddressEnabled().setEntity(false);
        hostModel.getConsoleAddress().setIsChangeable(false);

        AsyncDataProvider.getInstance().getDefaultPmProxyPreferences(new AsyncQuery(null, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {

                hostModel.setPmProxyPreferences((String) returnValue);
            }
        }));

        // Make sure not to set override IP tables flag back true when it was set false once.
        hostModel.getOverrideIpTables().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {

                if (!clusterChanging) {
                    updateOverrideIpTables = hostModel.getOverrideIpTables().getEntity();
                }
            }
        });

        hostModel.getCluster().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {

                clusterChanging = true;
                ListModel<Cluster> clusterModel = hostModel.getCluster();

                if (clusterModel.getSelectedItem() != null) {
                    hostModel.getOverrideIpTables().setIsAvailable(true);
                    hostModel.getOverrideIpTables().setEntity(updateOverrideIpTables);
                }

                clusterChanging = false;
            }
        });

        hostModel.getCluster().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                ListModel<Cluster> clusterModel = hostModel.getCluster();
                if (clusterModel.getSelectedItem() != null) {
                    Cluster cluster = clusterModel.getSelectedItem();
                    if (Version.v3_6.compareTo(cluster.getCompatibilityVersion()) <= 0) {
                        hostModel.getProtocol().setIsAvailable(false);
                        hostModel.getProtocol().setIsChangeable(false);
                    } else {
                        hostModel.getProtocol().setIsAvailable(true);
                        hostModel.getProtocol().setIsChangeable(true);
                    }
                    hostModel.getProtocol().setEntity(true);
                    hostModel.getProtocol().setIsChangeable(true);
                }
            }
        });

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                HostListModel<Void> hostListModel = (HostListModel<Void>) model;
                HostModel innerHostModel = (HostModel) hostListModel.getWindow();
                ArrayList<StoragePool> dataCenters = (ArrayList<StoragePool>) result;
                final UIConstants constants = ConstantsManager.getInstance().getConstants();

                if (hostListModel.getSystemTreeSelectedItem() != null) {
                    switch (hostListModel.getSystemTreeSelectedItem().getType()) {
                    case Host:
                        innerHostModel.getName().setIsChangeable(false);
                        innerHostModel.getName().setChangeProhibitionReason(constants.cannotEditNameInTreeContext());
                        break;
                    case Hosts:
                    case Cluster:
                    case Cluster_Gluster:
                        Cluster cluster = (Cluster) hostListModel.getSystemTreeSelectedItem().getEntity();
                        for (StoragePool dc : dataCenters) {
                            if (dc.getId().equals(cluster.getStoragePoolId())) {
                                innerHostModel.getDataCenter()
                                        .setItems(new ArrayList<>(Arrays.asList(new StoragePool[]{dc})));
                                innerHostModel.getDataCenter().setSelectedItem(dc);
                                break;
                            }
                        }
                        innerHostModel.getDataCenter().setIsChangeable(false);
                        innerHostModel.getDataCenter().setChangeProhibitionReason(constants.cannotChangeDCInTreeContext());
                        innerHostModel.getCluster().setItems(Arrays.asList(cluster));
                        innerHostModel.getCluster().setSelectedItem(cluster);
                        innerHostModel.getCluster().setIsChangeable(false);
                        innerHostModel.getCluster().setChangeProhibitionReason(constants.cannotChangeClusterInTreeContext());
                        break;
                    case DataCenter:
                        StoragePool selectDataCenter =
                                (StoragePool) hostListModel.getSystemTreeSelectedItem().getEntity();
                        innerHostModel.getDataCenter()
                                .setItems(new ArrayList<>(Arrays.asList(new StoragePool[]{selectDataCenter})));
                        innerHostModel.getDataCenter().setSelectedItem(selectDataCenter);
                        innerHostModel.getDataCenter().setIsChangeable(false);
                        innerHostModel.getDataCenter().setChangeProhibitionReason(constants.cannotChangeDCInTreeContext());
                        break;
                    default:
                        innerHostModel.getDataCenter().setItems(dataCenters);
                        innerHostModel.getDataCenter().setSelectedItem(Linq.firstOrNull(dataCenters));
                        break;
                    }
                }
                else {
                    innerHostModel.getDataCenter().setItems(dataCenters);
                    innerHostModel.getDataCenter().setSelectedItem(Linq.firstOrNull(dataCenters));
                }

                innerHostModel.onDataInitialized();

                UICommand onSaveFalseCommand = UICommand.createDefaultOkUiCommand("OnSaveFalse", hostListModel); //$NON-NLS-1$
                innerHostModel.getCommands().add(onSaveFalseCommand);

                UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", hostListModel); //$NON-NLS-1$
                innerHostModel.getCommands().add(cancelCommand);
            }
        };
        AsyncDataProvider.getInstance().getDataCenterList(_asyncQuery);
    }

    private void goToEventsTab() {
        setActiveDetailModel(getHostEventListModel());
    }

    public void edit(final boolean isEditWithPMemphasis) {
        if (getWindow() != null) {
            return;
        }

        final UIConstants constants = ConstantsManager.getInstance().getConstants();
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                HostListModel<Void> hostListModel = (HostListModel<Void>) model;
                ArrayList<StoragePool> dataCenters = (ArrayList<StoragePool>) result;
                VDS host = hostListModel.getSelectedItem();

                final EditHostModel hostModel = new EditHostModel();
                hostModel.updateModelFromVds(host, dataCenters, isEditWithPMemphasis, getSystemTreeSelectedItem());
                hostModel.setSelectedCluster(host);
                hostModel.onDataInitialized();
                hostModel.setTitle(ConstantsManager.getInstance().getConstants().editHostTitle());
                hostModel.setHelpTag(HelpTag.edit_host);
                hostModel.setHashName("edit_host"); //$NON-NLS-1$

                hostListModel.setWindow(hostModel);

                if (host.getFenceProxySources() != null && !host.getFenceProxySources().isEmpty()) {
                    hostModel.setPmProxyPreferences(
                            FenceProxySourceTypeHelper.saveAsString(host.getFenceProxySources()));
                } else {
                    AsyncDataProvider.getInstance().getDefaultPmProxyPreferences(new AsyncQuery(null, new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object model, Object returnValue) {

                            hostModel.setPmProxyPreferences((String) returnValue);
                        }
                    }));
                }

                UICommand onSaveFalseCommand = UICommand.createDefaultOkUiCommand("OnSaveFalse", hostListModel); //$NON-NLS-1$
                hostModel.getCommands().add(onSaveFalseCommand);

                UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", hostListModel); //$NON-NLS-1$
                hostModel.getCommands().add(cancelCommand);

                if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Host) {
                    hostModel.getName().setIsChangeable(false);
                    hostModel.getName().setChangeProhibitionReason(constants.cannotEditNameInTreeContext());
                }

            }
        };
        AsyncDataProvider.getInstance().getDataCenterList(_asyncQuery);


    }

    public void onSaveFalse() {
        onSave(false);
    }

    public void onSave(boolean approveInitiated) {
        HostModel model = (HostModel) getWindow();

        if (!model.validate()) {
            return;
        }

        if (!model.getIsPm().getEntity()) {
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
            }
            else {
                if (approveInitiated) {
                    onSaveInternalFromApprove();
                }
                else {
                    onSaveInternalNotFromApprove();
                }
            }
        }
        else {
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

        VDS host = model.getIsNew() ? new VDS() : (VDS) Cloner.clone(getSelectedItem());

        // Save changes.
        host.setVdsName(model.getName().getEntity());
        host.setComment(model.getComment().getEntity());
        host.setHostName(model.getHost().getEntity().trim());
        host.setPort(Integer.parseInt(model.getPort().getEntity().toString()));
        host.setProtocol(model.getProtocol().getEntity() ? VdsProtocol.STOMP : VdsProtocol.XML);
        host.setSshPort(Integer.parseInt(model.getAuthSshPort().getEntity().toString()));
        boolean sshUsernameSet = model.getUserName().getEntity() != null;
        host.setSshUsername(sshUsernameSet ? model.getUserName().getEntity() : null);
        boolean sshFpSet = model.getFetchSshFingerprint().getEntity() != null;
        host.setSshKeyFingerprint(!sshFpSet ? null : model.getFetchSshFingerprint().getEntity());
        host.setVdsSpmPriority(model.getSpmPriorityValue());
        boolean consoleAddressSet = model.getConsoleAddressEnabled().getEntity();
        host.setConsoleAddress(!consoleAddressSet ? null : model.getConsoleAddress().getEntity());
        Guid oldClusterId = host.getClusterId();
        Guid newClusterId = model.getCluster().getSelectedItem().getId();
        host.setClusterId(newClusterId);
        host.setVdsSpmPriority(model.getSpmPriorityValue());
        host.setFenceProxySources(FenceProxySourceTypeHelper.parseFromString(model.getPmProxyPreferences()));


        // Save other PM parameters.
        host.setPmEnabled(model.getIsPm().getEntity());
        host.setDisablePowerManagementPolicy(model.getDisableAutomaticPowerManagement().getEntity());
        host.setPmKdumpDetection(model.getPmKdumpDetection().getEntity());

        host.setCurrentKernelCmdline(model.getKernelCmdline().getEntity());
        host.setKernelCmdlineParsable(model.isKernelCmdlineParsable());
        host.setKernelCmdlineIommu(model.getKernelCmdlineIommu().getEntity());
        host.setKernelCmdlineKvmNested(model.getKernelCmdlineKvmNested().getEntity());
        host.setKernelCmdlineUnsafeInterrupts(model.getKernelCmdlineUnsafeInterrupts().getEntity());
        host.setKernelCmdlinePciRealloc(model.getKernelCmdlinePciRealloc().getEntity());


        cancelConfirm();
        model.startProgress();

        if (model.getIsNew()) {
            AddVdsActionParameters parameters = new AddVdsActionParameters();
            parameters.setVdsId(host.getId());
            parameters.setvds(host);
            parameters.setFenceAgents(model.getFenceAgentListModel().getFenceAgents());
            if (model.getUserPassword().getEntity() != null) {
                parameters.setPassword(model.getUserPassword().getEntity());
            }
            parameters.setOverrideFirewall(model.getOverrideIpTables().getEntity());
            parameters.setAuthMethod(model.getAuthenticationMethod());

            Provider<?> networkProvider = model.getNetworkProviders().getSelectedItem();
            if (networkProvider != null) {
                parameters.getVdsStaticData().setOpenstackNetworkProviderId(networkProvider.getId());
                parameters.setNetworkMappings(model.getInterfaceMappings().getEntity());
            }

            if (model.getProviders().getSelectedItem() != null) {
                parameters.getVdsStaticData().setHostProviderId(model.getProviders().getSelectedItem().getId());
            }
            if (Boolean.TRUE.equals(model.getIsDiscoveredHosts().getEntity())) {
                Provider<?> provider = model.getProviders().getSelectedItem();
                ExternalHostGroup hostGroup = (ExternalHostGroup) model.getExternalHostGroups().getSelectedItem();
                ExternalComputeResource computeResource = (ExternalComputeResource) model.getExternalComputeResource().getSelectedItem();
                ExternalDiscoveredHost discoveredHost = (ExternalDiscoveredHost) model.getExternalDiscoveredHosts().getSelectedItem();
                parameters.initVdsActionParametersForProvision(
                        provider.getId(),
                        hostGroup,
                        computeResource,
                        discoveredHost.getMac(),
                        discoveredHost.getName(),
                        discoveredHost.getIp());
            }

            parameters.setHostedEngineDeployConfiguration(
                    new HostedEngineDeployConfiguration(model.getHostedEngineHostModel().getActions().getSelectedItem()));

            Frontend.getInstance().runAction(VdcActionType.AddVds, parameters,
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void executed(FrontendActionAsyncResult result) {

                            Object[] array = (Object[]) result.getState();
                            HostListModel<Void> localModel = (HostListModel<Void>) array[0];
                            boolean localApproveInitiated = (Boolean) array[1];
                            localModel.postOnSaveInternal(result.getReturnValue(), localApproveInitiated);

                        }
                    }, new Object[] { this, approveInitiated });
        }
        else { // Update VDS -> consists of changing VDS cluster first and then updating rest of VDS properties:
            UpdateVdsActionParameters parameters = new UpdateVdsActionParameters();
            parameters.setvds(host);
            parameters.setVdsId(host.getId());
            parameters.setPassword(""); //$NON-NLS-1$
            parameters.setInstallHost(false);
            parameters.setAuthMethod(model.getAuthenticationMethod());
            parameters.setFenceAgents(model.getFenceAgentListModel().getFenceAgents());
            if (model.getExternalHostProviderEnabled().getEntity() && model.getProviders().getSelectedItem() != null) {
                host.setHostProviderId(model.getProviders().getSelectedItem().getId());
            }

            if (!oldClusterId.equals(newClusterId)) {
                Frontend.getInstance().runAction(VdcActionType.ChangeVDSCluster,
                        new ChangeVDSClusterParameters(newClusterId, host.getId()),
                        new IFrontendActionAsyncCallback() {
                            @Override
                            public void executed(FrontendActionAsyncResult result) {

                                Object[] array = (Object[]) result.getState();
                                HostListModel<Void> localModel = (HostListModel<Void>) array[0];
                                UpdateVdsActionParameters localParameters = (UpdateVdsActionParameters) array[1];
                                boolean localApproveInitiated = (Boolean) array[2];
                                VdcReturnValueBase localReturnValue = result.getReturnValue();
                                if (localReturnValue != null && localReturnValue.getSucceeded()) {
                                    localModel.postOnSaveInternalChangeCluster(localParameters, localApproveInitiated);
                                }
                                else {
                                    localModel.getWindow().stopProgress();
                                }

                            }
                        },
                        new Object[] { this, parameters, approveInitiated });
            }
            else {
                postOnSaveInternalChangeCluster(parameters, approveInitiated);
            }
        }
    }

    public void postOnSaveInternalChangeCluster(UpdateVdsActionParameters parameters, boolean approveInitiated) {
        Frontend.getInstance().runAction(VdcActionType.UpdateVds, parameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {

                        Object[] array = (Object[]) result.getState();
                        HostListModel<Void> localModel = (HostListModel<Void>) array[0];
                        boolean localApproveInitiated = (Boolean) array[1];
                        localModel.postOnSaveInternal(result.getReturnValue(), localApproveInitiated);

                    }
                }, new Object[] { this, approveInitiated });
    }

    public void postOnSaveInternal(VdcReturnValueBase returnValue, boolean approveInitiated) {
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
            params.setPassword(model.getUserPassword().getEntity().toString());
        }
        params.setAuthMethod(model.getAuthenticationMethod());

        Frontend.getInstance().runMultipleAction(VdcActionType.ApproveVds,
                new ArrayList<>(Arrays.asList(new VdcActionParametersBase[]{params})),
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                    }
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
        for (VDS item : Linq.<VDS> cast(getSelectedItems())) {
            list.add(item.getName());
            clusters.add(item.getClusterId());
        }
        model.setItems(list);

        // Remove Force option will be shown only if
        // - All the selected hosts belongs to same cluster
        // - the cluster should have  gluster service enabled
        if (clusters.size() == 1) {
            model.startProgress();
            AsyncDataProvider.getInstance().getClusterById(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {
                    Cluster cluster = (Cluster) returnValue;
                    if (cluster != null && cluster.supportsGlusterService()) {
                        model.getForce().setIsAvailable(true);
                    }
                    model.stopProgress();
                }
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
        ArrayList<VdcActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            list.add(new RemoveVdsParameters(vds.getId(), force));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(VdcActionType.RemoveVds, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }
                }, model);
    }

    public void activate() {
        ArrayList<VdcActionParametersBase> list = new ArrayList<>();

        Collections.sort(getSelectedItems(), new Linq.VdsSPMPriorityComparer());

        for (VDS vds : getSelectedItems()) {
            list.add(new VdsActionParameters(vds.getId()));
        }

        Frontend.getInstance().runMultipleAction(VdcActionType.ActivateVds, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                    }
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
            maintenance(false, false);
        } else {
            AsyncDataProvider.getInstance().getClusterById(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {
                            Cluster cluster = (Cluster) returnValue;
                            if (cluster != null) {
                                maintenance(cluster.isMaintenanceReasonRequired(), cluster.supportsGlusterService());
                            }
                        }
                    }), clusterId);
        }
    }

    private void maintenance(boolean isMaintenanceReasonVisible, boolean isStopGlusterServiceRequired) {
        if (getConfirmWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().maintenanceHostsTitle());
        model.setHelpTag(HelpTag.maintenance_host);
        model.setHashName("maintenance_host"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance()
                .getConstants()
                .areYouSureYouWantToPlaceFollowingHostsIntoMaintenanceModeMsg());
        model.setReasonVisible(isMaintenanceReasonVisible);
        if (isStopGlusterServiceRequired) {
            model.getForce().setIsAvailable(true);
            model.setForceLabel(ConstantsManager.getInstance().getConstants().stopGlusterServices());
            model.getForce().setEntity(false);
        }
        // model.Items = SelectedItems.Cast<VDS>().Select(a => a.vds_name);
        ArrayList<String> vdss = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            vdss.add(vds.getName());
        }
        model.setItems(vdss);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnMaintenance", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("CancelConfirm", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onMaintenance() {
        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<>();
        ArrayList<Guid> vdss = new ArrayList<>();

        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            vdss.add(vds.getId());
        }
        list.add(new MaintenanceNumberOfVdssParameters(vdss,
                false,
                model.getReason().getEntity(),
                model.getForce().getEntity()));

        model.startProgress();

        Frontend.getInstance().runMultipleAction(VdcActionType.MaintenanceNumberOfVdss, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancelConfirm();

                    }
                }, model);
    }

    public void approve() {
        HostModel hostModel = new EditHostModel();
        setWindow(hostModel);
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                HostListModel<Void> hostListModel = (HostListModel<Void>) model;
                HostModel innerHostModel = (HostModel) hostListModel.getWindow();
                ArrayList<StoragePool> dataCenters = (ArrayList<StoragePool>) result;
                VDS host = hostListModel.getSelectedItem();
                innerHostModel.updateModelFromVds(host, dataCenters, false, getSystemTreeSelectedItem());
                innerHostModel.setTitle(ConstantsManager.getInstance().getConstants().editAndApproveHostTitle());
                innerHostModel.setHelpTag(HelpTag.edit_and_approve_host);
                innerHostModel.setHashName("edit_and_approve_host"); //$NON-NLS-1$

                UICommand tempVar = UICommand.createDefaultOkUiCommand("OnApprove", hostListModel); //$NON-NLS-1$
                innerHostModel.getCommands().add(tempVar);
                UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", hostListModel); //$NON-NLS-1$
                innerHostModel.getCommands().add(tempVar2);
            }
        };
        AsyncDataProvider.getInstance().getDataCenterList(_asyncQuery);
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
        addInstallCommands(model, host, false);
        getWindow().stopProgress();
    }

    private void addInstallCommands(InstallModel model, VDS host, boolean isOnlyClose) {

        if (!isOnlyClose) {
            UICommand command = UICommand.createDefaultOkUiCommand("OnInstall", this); //$NON-NLS-1$
            model.getCommands().add(command);
        }
        model.getUserName().setEntity(host.getSshUsername());
        UICommand command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(isOnlyClose ? ConstantsManager.getInstance().getConstants().close()
                : ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        model.getCommands().add(command);
    }

    public void onInstall() {
        final VDS host = getSelectedItem();
        InstallModel model = (InstallModel) getWindow();

        if (!model.validate(host.isOvirtVintageNode())) {
            model.setValidationFailed(new EntityModel<>(true));
            return;
        }

        UpdateVdsActionParameters param = new UpdateVdsActionParameters();
        param.setvds(host);
        param.setVdsId(host.getId());
        param.setPassword(model.getUserPassword().getEntity());
        param.setReinstallOrUpgrade(true);
        param.setInstallHost(true);
        param.setoVirtIsoFile(null);
        param.setOverrideFirewall(model.getOverrideIpTables().getEntity());
        param.setActivateHost(model.getActivateHostAfterInstall().getEntity());
        param.setAuthMethod(model.getAuthenticationMethod());
        param.setFenceAgents(null);  // Explicitly set null, to be clear we don't want to update fence agents.
        param.setHostedEngineDeployConfiguration(
                new HostedEngineDeployConfiguration(model.getHostedEngineHostModel().getActions().getSelectedItem()));

        Provider<?> networkProvider = (Provider<?>) model.getNetworkProviders().getSelectedItem();
        if (networkProvider != null) {
            param.getVdsStaticData().setOpenstackNetworkProviderId(networkProvider.getId());
            param.setNetworkMappings((String) model.getInterfaceMappings().getEntity());
        }

        AsyncDataProvider.getInstance().getClusterById(new AsyncQuery(param, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                UpdateVdsActionParameters internalParam = (UpdateVdsActionParameters) model;

                Frontend.getInstance().runAction(
                        VdcActionType.InstallVds,
                        internalParam,
                        new IFrontendActionAsyncCallback() {
                            @Override
                            public void executed(FrontendActionAsyncResult result) {
                                VdcReturnValueBase returnValue = result.getReturnValue();
                                if (returnValue != null && returnValue.getSucceeded()) {
                                    cancel();
                                }
                            }
                        }
                );
            }
        }), host.getClusterId());


    }

    public void upgrade() {
        final VDS host = getSelectedItem();
        Model model = createUpgradeModel(host);
        setWindow(model);
        model.initialize();
        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); // $NON-NLS-1$
    }

    public void restart() {
        final UIConstants constants = ConstantsManager.getInstance().getConstants();
        final UIMessages messages = ConstantsManager.getInstance().getMessages();
        ConfirmationModel model = new ConfirmationModel();
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

        UICommand tempVar = new UICommand("OnRestart", this); //$NON-NLS-1$
        tempVar.setTitle(constants.ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(constants.cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void onRestart() {
        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            list.add(new FenceVdsActionParameters(vds.getId()));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(VdcActionType.RestartVds, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancelConfirm();

                    }
                }, model);
    }

    public void start() {
        ArrayList<VdcActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            list.add(new FenceVdsActionParameters(vds.getId()));
        }

        Frontend.getInstance().runMultipleAction(VdcActionType.StartVds, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                    }
                }, null);
    }

    public void stop() {
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

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnStop", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onStop() {
        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            list.add(new FenceVdsActionParameters(vds.getId()));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(VdcActionType.StopVds, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancelConfirm();

                    }
                }, model);
    }

    private void configureLocalStorage() {

        VDS host = getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        ConfigureLocalStorageModel model = new ConfigureLocalStorageModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().configureLocalStorageTitle());
        model.setHelpTag(HelpTag.configure_local_storage);
        model.setHashName("configure_local_storage"); //$NON-NLS-1$

        if (host.isOvirtVintageNode()) {
            configureLocalStorage2(model);
        } else {
            configureLocalStorage3(model);
        }
    }

    private void configureLocalStorage2(ConfigureLocalStorageModel model) {
        String prefix = (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.RhevhLocalFSPath);
        if (!StringUtils.isEmpty(prefix)) {
            EntityModel<String> pathModel = model.getStorage().getPath();
            pathModel.setEntity(prefix);
            pathModel.setIsChangeable(false);
        }

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
                new IEventListener<EventArgs>() {
                    @Override
                    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {

                        ConfigureLocalStorageModel model = (ConfigureLocalStorageModel) ev.getContext();

                        model.stopProgress();
                        cancel();
                    }
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
        ArrayList<VdcActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            list.add(new VdsActionParameters(vds.getId()));
        }

        Frontend.getInstance().runMultipleAction(VdcActionType.RefreshHost, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                    }
                }, null);
    }

    private void enrollCertificate() {
        final VDS host = getSelectedItem();
        Frontend.getInstance().runAction(VdcActionType.HostEnrollCertificate, new VdsActionParameters(host.getId()));
    }

    @Override
    protected void updateDetailsAvailability() {
        super.updateDetailsAvailability();
        VDS vds = getSelectedItem();
        getGlusterSwiftModel().setIsAvailable(false);
        getHostBricksListModel().setIsAvailable(vds != null && vds.getClusterSupportsGlusterService());
        getHostVmListModel().setIsAvailable(vds != null && vds.getClusterSupportsVirtService());
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
        SearchParameters tempVar = new SearchParameters(applySortOptions(getSearchString()), SearchType.VDS,
                isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    public boolean supportsServerSideSorting() {
        return true;
    }

    public void cancel() {
        cancelConfirm();
        setWindow(null);
    }

    public void cancelConfirm() {
        setConfirmWindow(null);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    private void updateAvailableOvirtNodeUpgrades() {
        final VDS host = getSelectedItem();
        if (host == null) {
            return;
        }

        if (!host.isOvirtVintageNode()) {
            return;
        }

        AsyncDataProvider.getInstance().getoVirtISOsList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                    }
                }),
                host.getId());
    }

    private void setUpgradeActionStatus(final VDS vds) {
        final boolean executionAllowed = canUpgradeHost(vds);
        if (!executionAllowed) {
            final UIConstants constants = ConstantsManager.getInstance().getConstants();
            getUpgradeCommand().getExecuteProhibitionReasons()
                    .add(constants.switchToMaintenanceModeToEnableUpgradeReason());
        }

        getUpgradeCommand().setIsExecutionAllowed(executionAllowed);
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    protected void itemsCollectionChanged(Object sender, NotifyCollectionChangedEventArgs<VDS> e) {
        super.itemsCollectionChanged(sender, e);

        // Try to select an item corresponding to the system tree selection.
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Host) {
            VDS host = (VDS) getSystemTreeSelectedItem().getEntity();

            setSelectedItem(Linq.firstOrNull(Linq.<VDS> cast(getItems()), new Linq.IdPredicate<>(host.getId())));
        }
    }

    @Override
    protected void selectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.selectedItemPropertyChanged(sender, e);

        if (e.propertyName.equals("status") || e.propertyName.equals("pm_enabled")) { //$NON-NLS-1$ //$NON-NLS-2$
            updateActionAvailability();
        }
    }

    private void updateActionAvailability() {
        ArrayList<VDS> items =
                getSelectedItems() != null ? Linq.<VDS> cast(getSelectedItems()) : new ArrayList<VDS>();

        boolean isAllPMEnabled = Linq.findAllVDSByPmEnabled(items).size() == items.size();

        getEditCommand().setIsExecutionAllowed(items.size() == 1
                && VdcActionUtils.canExecute(items, VDS.class, VdcActionType.UpdateVds));

        getEditWithPMemphasisCommand().setIsExecutionAllowed(items.size() == 1
                && VdcActionUtils.canExecute(items, VDS.class, VdcActionType.UpdateVds));

        getRemoveCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.canExecute(items, VDS.class, VdcActionType.RemoveVds));

        getActivateCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.canExecute(items, VDS.class, VdcActionType.ActivateVds));

        // or special case where its installation failed but its oVirt node
        boolean approveAvailability =
                items.size() == 1
                        && (VdcActionUtils.canExecute(items, VDS.class, VdcActionType.ApproveVds) || (items.get(0)
                                .getStatus() == VDSStatus.InstallFailed && items.get(0).isOvirtVintageNode()));
        getApproveCommand().setIsExecutionAllowed(approveAvailability);

        boolean installAvailability = false;
        if (singleHostSelected(items)) {
            VDS host = items.get(0);
            installAvailability = host.getStatus() == VDSStatus.InstallFailed ||
                    host.getStatus() == VDSStatus.Maintenance;
        }
        getInstallCommand().setIsExecutionAllowed(installAvailability);

        boolean upgradeAvailability = false;
        if (singleHostSelected(items)) {
            VDS host = items.get(0);
            upgradeAvailability = canUpgradeHost(host);
        }
        getUpgradeCommand().setIsExecutionAllowed(upgradeAvailability);
        getEnrollCertificateCommand().setIsExecutionAllowed(installAvailability);

        getMaintenanceCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.canExecute(items, VDS.class, VdcActionType.MaintenanceVds));

        getRestartCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.canExecute(items, VDS.class, VdcActionType.RestartVds) && isAllPMEnabled);

        getStartCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.canExecute(items, VDS.class, VdcActionType.StartVds) && isAllPMEnabled);

        getStopCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.canExecute(items, VDS.class, VdcActionType.StopVds) && isAllPMEnabled);

        setIsPowerManagementEnabled(getRestartCommand().getIsExecutionAllowed()
                || getStartCommand().getIsExecutionAllowed() || getStopCommand().getIsExecutionAllowed());

        getManualFenceCommand().setIsExecutionAllowed(items.size() == 1);

        getAssignTagsCommand().setIsExecutionAllowed(items.size() > 0);

        getSelectAsSpmCommand().setIsExecutionAllowed(isSelectAsSpmCommandAllowed(items));

        updateConfigureLocalStorageCommandAvailability();

        getRefreshCapabilitiesCommand().setIsExecutionAllowed(items.size() > 0 && VdcActionUtils.canExecute(items,
                VDS.class,
                VdcActionType.RefreshHostCapabilities));

        boolean numaVisible = false;
        if (getSelectedItem() != null) {
            numaVisible = getSelectedItem().isNumaSupport();
        }
        getNumaSupportCommand().setIsVisible(numaVisible);

    }

    private boolean canUpgradeHost(VDS host) {
        return host.isUpdateAvailable()
                && VdcActionUtils.canExecute(Arrays.asList(host), VDS.class, VdcActionType.UpgradeHost);
    }

    private boolean singleHostSelected(List<VDS> items) {
        return items.size() == 1 && items.get(0) instanceof VDS;
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

            Frontend.getInstance().runQuery(VdcQueryType.GetPermissionsByAdElementId,
                    new IdQueryParameters(dbUser.getId()),
                    new AsyncQuery(this, new INewAsyncCallback() {

                        @Override
                        public void onSuccess(Object model, Object returnValue) {
                            VdcQueryReturnValue response = (VdcQueryReturnValue) returnValue;
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

                        }
                    }, true));
        } else {
            updateConfigureLocalStorageCommandAvailability1();
        }
    }

    private void updateConfigureLocalStorageCommandAvailability1() {

        ArrayList<VDS> items = getSelectedItems() != null ? Linq.<VDS> cast(getSelectedItems()) : new ArrayList<VDS>();

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
        }
        else if (command == getEditCommand()) {
            edit(false);
        }
        else if (command == getEditWithPMemphasisCommand()) {
            edit(true);
        }
        else if (command == getRemoveCommand()) {
            remove();
        }
        else if (command == getSelectAsSpmCommand()) {
            selectAsSPM();
        }
        else if (command == getActivateCommand()) {
            activate();
        }
        else if (command == getMaintenanceCommand()) {
            maintenance();
        }
        else if (command == getApproveCommand()) {
            approve();
        }
        else if (command == getInstallCommand()) {
            install();
        }
        else if (command == getUpgradeCommand()) {
            upgrade();
        }
        else if (command == getRestartCommand()) {
            restart();
        }
        else if (command == getStartCommand()) {
            start();
        }
        else if (command == getStopCommand()) {
            stop();
        }
        else if (command == getManualFenceCommand()) {
            manualFence();
        }
        else if (command == getAssignTagsCommand()) {
            assignTags();
        }
        else if (command == getConfigureLocalStorageCommand()) {
            configureLocalStorage();
        }
        else if (command == getRefreshCapabilitiesCommand()) {
            refreshCapabilities();
        }
        else if (command == getEnrollCertificateCommand()) {
            enrollCertificate();
        }
        else if (command == getNumaSupportCommand()) {
            numaSupport();
        }
        else if ("OnAssignTags".equals(command.getName())) { //$NON-NLS-1$
            onAssignTags();
        }
        else if ("OnManualFence".equals(command.getName())) { //$NON-NLS-1$
            onManualFence();
        }
        else if ("OnSaveFalse".equals(command.getName())) { //$NON-NLS-1$
            onSaveFalse();
        }
        else if ("OnSaveInternalFromApprove".equals(command.getName())) { //$NON-NLS-1$
            onSaveInternalFromApprove();
        }
        else if ("OnSaveInternalNotFromApprove".equals(command.getName())) { //$NON-NLS-1$
            onSaveInternalNotFromApprove();
        }
        else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
        else if ("CancelConfirm".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirm();
        }
        else if ("CancelConfirmFocusPM".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirmFocusPM();
        }
        else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        }
        else if ("OnMaintenance".equals(command.getName())) { //$NON-NLS-1$
            onMaintenance();
        }
        else if ("OnApprove".equals(command.getName())) { //$NON-NLS-1$
            onApprove();
        }
        else if ("OnInstall".equals(command.getName())) { //$NON-NLS-1$
            onInstall();
        }
        else if ("OnRestart".equals(command.getName())) { //$NON-NLS-1$
            onRestart();
        }
        else if ("OnStop".equals(command.getName())) { //$NON-NLS-1$
            onStop();
        }
        else if ("OnConfigureLocalStorage".equals(command.getName())) { //$NON-NLS-1$
            onConfigureLocalStorage();
        }
        else if (NumaSupportModel.SUBMIT_NUMA_SUPPORT.equals(command.getName())) {
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
        ArrayList<VdcActionParametersBase> updateParamsList = model.getUpdateParameters();
        if (!updateParamsList.isEmpty()) {
            Frontend.getInstance().runMultipleAction(VdcActionType.UpdateVmNumaNodes, updateParamsList);
        }
        setWindow(null);
    }

    private void updateVNodesMap(VM vm, Map<Guid, VmNumaNode> map) {
        List<VmNumaNode> list = new ArrayList<>();
        for (VmNumaNode node : vm.getvNumaNodeList()) {
            list.add(node);
        }
        for (VmNumaNode node : list) {
            map.put(node.getId(), node);
        }
    }

    private void selectAsSPM() {
        ForceSelectSPMParameters params = new ForceSelectSPMParameters(getSelectedItem().getId());
        Frontend.getInstance().runAction(VdcActionType.ForceSelectSPM, params);

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

        if (vds.getVdsSpmPriority() == BusinessEntitiesDefinitions.HOST_MIN_SPM_PRIORITY) {
            return false;
        }

        return true;
    }

    private SystemTreeItemModel systemTreeSelectedItem;

    @Override
    public SystemTreeItemModel getSystemTreeSelectedItem() {
        return systemTreeSelectedItem;
    }

    @Override
    public void setSystemTreeSelectedItem(SystemTreeItemModel value) {
        if (systemTreeSelectedItem != value) {
            systemTreeSelectedItem = value;
            onSystemTreeSelectedItemChanged();
        }
    }

    private void onSystemTreeSelectedItemChanged() {
        updateActionAvailability();
    }

    @Override
    protected String getListName() {
        return "HostListModel"; //$NON-NLS-1$
    }
}
