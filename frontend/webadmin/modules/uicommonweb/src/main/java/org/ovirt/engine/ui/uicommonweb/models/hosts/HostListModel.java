package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.ApproveVdsParameters;
import org.ovirt.engine.core.common.action.AttachVdsToTagParameters;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.FenceVdsManualyParameters;
import org.ovirt.engine.core.common.action.MaintananceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.action.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByAdElementIdParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
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
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.events.TaskListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.NotifyCollectionChangedEventArgs;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.ReversibleFlow;

@SuppressWarnings("unused")
public class HostListModel extends ListWithDetailsModel implements ISupportSystemTreeContext
{
    private UICommand privateNewCommand;

    public UICommand getNewCommand()
    {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value)
    {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    public UICommand getEditCommand()
    {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value)
    {
        privateEditCommand = value;
    }

    private UICommand privateEditWithPMemphasisCommand;

    public UICommand getEditWithPMemphasisCommand()
    {
        return privateEditWithPMemphasisCommand;
    }

    private void setEditWithPMemphasisCommand(UICommand value)
    {
        privateEditWithPMemphasisCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand()
    {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value)
    {
        privateRemoveCommand = value;
    }

    private UICommand privateActivateCommand;

    public UICommand getActivateCommand()
    {
        return privateActivateCommand;
    }

    private void setActivateCommand(UICommand value)
    {
        privateActivateCommand = value;
    }

    private UICommand privateMaintenanceCommand;

    public UICommand getMaintenanceCommand()
    {
        return privateMaintenanceCommand;
    }

    private void setMaintenanceCommand(UICommand value)
    {
        privateMaintenanceCommand = value;
    }

    private UICommand privateApproveCommand;

    public UICommand getApproveCommand()
    {
        return privateApproveCommand;
    }

    private void setApproveCommand(UICommand value)
    {
        privateApproveCommand = value;
    }

    private UICommand privateRestartCommand;

    public UICommand getRestartCommand()
    {
        return privateRestartCommand;
    }

    private void setRestartCommand(UICommand value)
    {
        privateRestartCommand = value;
    }

    private UICommand privateStartCommand;

    public UICommand getStartCommand()
    {
        return privateStartCommand;
    }

    private void setStartCommand(UICommand value)
    {
        privateStartCommand = value;
    }

    private UICommand privateStopCommand;

    public UICommand getStopCommand()
    {
        return privateStopCommand;
    }

    private void setStopCommand(UICommand value)
    {
        privateStopCommand = value;
    }

    private UICommand privateManualFenceCommand;

    public UICommand getManualFenceCommand()
    {
        return privateManualFenceCommand;
    }

    private void setManualFenceCommand(UICommand value)
    {
        privateManualFenceCommand = value;
    }

    private UICommand privateAssignTagsCommand;

    public UICommand getAssignTagsCommand()
    {
        return privateAssignTagsCommand;
    }

    private void setAssignTagsCommand(UICommand value)
    {
        privateAssignTagsCommand = value;
    }

    private UICommand privateConfigureLocalStorageCommand;

    public UICommand getConfigureLocalStorageCommand()
    {
        return privateConfigureLocalStorageCommand;
    }

    private void setConfigureLocalStorageCommand(UICommand value)
    {
        privateConfigureLocalStorageCommand = value;
    }

    private HostEventListModel privateHostEventListModel;

    private HostEventListModel getHostEventListModel()
    {
        return privateHostEventListModel;
    }

    private void setHostEventListModel(HostEventListModel value)
    {
        privateHostEventListModel = value;
    }

    private boolean isPowerManagementEnabled;

    public boolean getIsPowerManagementEnabled()
    {
        return isPowerManagementEnabled;
    }

    public void setIsPowerManagementEnabled(boolean value)
    {
        if (isPowerManagementEnabled != value)
        {
            isPowerManagementEnabled = value;
            OnPropertyChanged(new PropertyChangedEventArgs("isPowerManagementEnabled")); //$NON-NLS-1$
        }
    }

    protected Object[] getSelectedKeys()
    {
        if (getSelectedItems() == null)
        {
            return new Object[0];
        }
        else
        {
            Object[] keys = new Object[getSelectedItems().size()];
            for (int i = 0; i < getSelectedItems().size(); i++)
            {
                keys[i] = ((VDS) getSelectedItems().get(i)).getId();
            }
            return keys;
        }
    }

    public HostListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().hostsTitle());
        setHashName("hosts"); //$NON-NLS-1$

        setDefaultSearchString("Host:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.VDS_OBJ_NAME, SearchObjects.VDS_PLU_OBJ_NAME });

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setEditWithPMemphasisCommand(new UICommand("EditWithPMemphasis", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setActivateCommand(new UICommand("Activate", this, true)); //$NON-NLS-1$
        setMaintenanceCommand(new UICommand("Maintenance", this, true)); //$NON-NLS-1$
        setApproveCommand(new UICommand("Approve", this)); //$NON-NLS-1$
        setRestartCommand(new UICommand("Restart", this, true)); //$NON-NLS-1$
        setStartCommand(new UICommand("Start", this, true)); //$NON-NLS-1$
        setStopCommand(new UICommand("Stop", this, true)); //$NON-NLS-1$
        setManualFenceCommand(new UICommand("ManualFence", this)); //$NON-NLS-1$
        setAssignTagsCommand(new UICommand("AssignTags", this)); //$NON-NLS-1$
        setConfigureLocalStorageCommand(new UICommand("ConfigureLocalStorage", this)); //$NON-NLS-1$

        getConfigureLocalStorageCommand().setAvailableInModes(ApplicationMode.VirtOnly);
        UpdateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    public void AssignTags()
    {
        if (getWindow() != null)
        {
            return;
        }

        TagListModel model = new TagListModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().assignTagsTitle());
        model.setHashName("assign_tags_hosts"); //$NON-NLS-1$

        GetAttachedTagsToSelectedHosts(model);

        UICommand tempVar = new UICommand("OnAssignTags", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public Map<Guid, Boolean> attachedTagsToEntities;
    public ArrayList<org.ovirt.engine.core.common.businessentities.tags> allAttachedTags;
    public int selectedItemsCounter;

    private void GetAttachedTagsToSelectedHosts(TagListModel model)
    {
        HashMap<Guid, Boolean> tags = new HashMap<Guid, Boolean>();

        ArrayList<Guid> hostIds = new ArrayList<Guid>();

        for (Object item : getSelectedItems())
        {
            VDS vds = (VDS) item;
            hostIds.add(vds.getId());
        }

        attachedTagsToEntities = new HashMap<Guid, Boolean>();
        allAttachedTags = new ArrayList<org.ovirt.engine.core.common.businessentities.tags>();
        selectedItemsCounter = 0;

        for (Guid hostId : hostIds)
        {
            AsyncDataProvider.GetAttachedTagsToHost(new AsyncQuery(new Object[] { this, model },
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            Object[] array = (Object[]) target;
                            HostListModel hostListModel = (HostListModel) array[0];
                            TagListModel tagListModel = (TagListModel) array[1];
                            hostListModel.allAttachedTags.addAll((ArrayList<org.ovirt.engine.core.common.businessentities.tags>) returnValue);
                            hostListModel.selectedItemsCounter++;
                            if (hostListModel.selectedItemsCounter == hostListModel.getSelectedItems().size())
                            {
                                PostGetAttachedTags(hostListModel, tagListModel);
                            }

                        }
                    }),
                    hostId);
        }
    }

    private void PostGetAttachedTags(HostListModel hostListModel, TagListModel tagListModel)
    {
        if (hostListModel.getLastExecutedCommand() == getAssignTagsCommand())
        {
            ArrayList<org.ovirt.engine.core.common.businessentities.tags> attachedTags =
                    Linq.Distinct(hostListModel.allAttachedTags, new TagsEqualityComparer());
            for (org.ovirt.engine.core.common.businessentities.tags tag : attachedTags)
            {
                int count = 0;
                for (org.ovirt.engine.core.common.businessentities.tags tag2 : hostListModel.allAttachedTags)
                {
                    if (tag2.gettag_id().equals(tag.gettag_id()))
                    {
                        count++;
                    }
                }
                hostListModel.attachedTagsToEntities.put(tag.gettag_id(), count == hostListModel.getSelectedItems()
                        .size());
            }
            tagListModel.setAttachedTagsToEntities(hostListModel.attachedTagsToEntities);
        }
        else if (StringHelper.stringsEqual(hostListModel.getLastExecutedCommand().getName(), "OnAssignTags")) //$NON-NLS-1$
        {
            hostListModel.PostOnAssignTags(tagListModel.getAttachedTagsToEntities());
        }
    }

    public void OnAssignTags()
    {
        TagListModel model = (TagListModel) getWindow();

        GetAttachedTagsToSelectedHosts(model);
    }

    public void PostOnAssignTags(Map<Guid, Boolean> attachedTags)
    {
        TagListModel model = (TagListModel) getWindow();
        ArrayList<Guid> hostIds = new ArrayList<Guid>();

        for (Object item : getSelectedItems())
        {
            VDS vds = (VDS) item;
            hostIds.add(vds.getId());
        }

        // prepare attach/detach lists
        ArrayList<Guid> tagsToAttach = new ArrayList<Guid>();
        ArrayList<Guid> tagsToDetach = new ArrayList<Guid>();

        if (model.getItems() != null && ((ArrayList<TagModel>) model.getItems()).size() > 0)
        {
            ArrayList<TagModel> tags = (ArrayList<TagModel>) model.getItems();
            TagModel rootTag = tags.get(0);
            TagModel.RecursiveEditAttachDetachLists(rootTag, attachedTags, tagsToAttach, tagsToDetach);
        }

        ArrayList<VdcActionParametersBase> prmsToAttach = new ArrayList<VdcActionParametersBase>();
        for (Guid tag_id : tagsToAttach)
        {
            prmsToAttach.add(new AttachVdsToTagParameters(tag_id, hostIds));
        }
        Frontend.RunMultipleAction(VdcActionType.AttachVdsToTag, prmsToAttach);

        ArrayList<VdcActionParametersBase> prmsToDetach = new ArrayList<VdcActionParametersBase>();
        for (Guid tag_id : tagsToDetach)
        {
            prmsToDetach.add(new AttachVdsToTagParameters(tag_id, hostIds));
        }
        Frontend.RunMultipleAction(VdcActionType.DetachVdsFromTag, prmsToDetach);

        Cancel();
    }

    public void ManualFence()
    {
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().areYouSureTitle());
        model.setHashName("manual_fence_are_you_sure"); //$NON-NLS-1$
        ArrayList<VDS> items = new ArrayList<VDS>();
        items.add((VDS) getSelectedItem());
        model.setItems(items);

        model.getLatch().setIsAvailable(true);
        model.getLatch().setIsChangable(true);

        UICommand tempVar = new UICommand("OnManualFence", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnManualFence()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VDS vds = (VDS) item;
            FenceVdsManualyParameters parameters = new FenceVdsManualyParameters(true);
            parameters.setStoragePoolId(vds.getStoragePoolId());
            parameters.setVdsId(vds.getId());
            list.add(parameters);
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.FenceVdsManualy, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    boolean updateOverrideIpTables = true;
    boolean clusterChanging = false;

    public void New()
    {
        if (getWindow() != null)
        {
            return;
        }

        final HostModel hostModel = new HostModel();
        setWindow(hostModel);
        hostModel.setTitle(ConstantsManager.getInstance().getConstants().newHostTitle());
        hostModel.setHashName("new_host"); //$NON-NLS-1$
        hostModel.getPort().setEntity(54321);
        hostModel.getOverrideIpTables().setIsAvailable(false);
        hostModel.setSpmPriorityValue(null);
        hostModel.getConsoleAddressEnabled().setEntity(false);
        hostModel.getConsoleAddress().setIsChangable(false);

        AsyncDataProvider.GetDefaultPmProxyPreferences(new AsyncQuery(null, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object returnValue) {

                hostModel.setPmProxyPreferences((String) returnValue);
            }
        }));

        // Make sure not to set override IP tables flag back true when it was set false once.
        hostModel.getOverrideIpTables().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {

                if (!clusterChanging) {
                    updateOverrideIpTables = (Boolean) hostModel.getOverrideIpTables().getEntity();
                }
            }
        });

        // Set override IP tables flag true for v3.0 clusters.
        hostModel.getCluster().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {

                clusterChanging = true;
                ListModel clusterModel = hostModel.getCluster();

                if (clusterModel.getSelectedItem() != null) {

                    Version v3 = new Version(3, 0);
                    VDSGroup cluster = (VDSGroup) clusterModel.getSelectedItem();

                    boolean isLessThan3 = cluster.getcompatibility_version().compareTo(v3) < 0;

                    hostModel.getOverrideIpTables().setIsAvailable(!isLessThan3);
                    hostModel.getOverrideIpTables().setEntity(!isLessThan3 && updateOverrideIpTables);
                }

                clusterChanging = false;
            }
        });

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                HostListModel hostListModel = (HostListModel) model;
                HostModel innerHostModel = (HostModel) hostListModel.getWindow();
                ArrayList<storage_pool> dataCenters = (ArrayList<storage_pool>) result;

                if (hostListModel.getSystemTreeSelectedItem() != null)
                {
                    switch (hostListModel.getSystemTreeSelectedItem().getType())
                    {
                    case Host:
                        innerHostModel.getName().setIsChangable(false);
                        innerHostModel.getName().setInfo("Cannot edit Host's Name in this tree context"); //$NON-NLS-1$
                        break;
                    case Hosts:
                    case Cluster:
                        VDSGroup cluster = (VDSGroup) hostListModel.getSystemTreeSelectedItem().getEntity();
                        for (storage_pool dc : dataCenters)
                        {
                            if (dc.getId().equals(cluster.getStoragePoolId()))
                            {
                                innerHostModel.getDataCenter()
                                        .setItems(new ArrayList<storage_pool>(Arrays.asList(new storage_pool[] { dc })));
                                innerHostModel.getDataCenter().setSelectedItem(dc);
                                break;
                            }
                        }
                        innerHostModel.getDataCenter().setIsChangable(false);
                        innerHostModel.getDataCenter().setInfo("Cannot choose Host's Data Center in tree context"); //$NON-NLS-1$
                        innerHostModel.getCluster().setItems(Arrays.asList(cluster));
                        innerHostModel.getCluster().setSelectedItem(cluster);
                        innerHostModel.getCluster().setIsChangable(false);
                        innerHostModel.getCluster().setInfo("Cannot choose Host's Cluster in tree context"); //$NON-NLS-1$
                        break;
                    case DataCenter:
                        storage_pool selectDataCenter =
                                (storage_pool) hostListModel.getSystemTreeSelectedItem().getEntity();
                        innerHostModel.getDataCenter()
                                .setItems(new ArrayList<storage_pool>(Arrays.asList(new storage_pool[] { selectDataCenter })));
                        innerHostModel.getDataCenter().setSelectedItem(selectDataCenter);
                        innerHostModel.getDataCenter().setIsChangable(false);
                        innerHostModel.getDataCenter().setInfo("Cannot choose Host's Data Center in tree context"); //$NON-NLS-1$
                        break;
                    default:
                        innerHostModel.getDataCenter().setItems(dataCenters);
                        innerHostModel.getDataCenter().setSelectedItem(Linq.FirstOrDefault(dataCenters));
                        break;
                    }
                }
                else
                {
                    innerHostModel.getDataCenter().setItems(dataCenters);
                    innerHostModel.getDataCenter().setSelectedItem(Linq.FirstOrDefault(dataCenters));
                }


                UICommand command;

                command = new UICommand("OnSaveFalse", hostListModel); //$NON-NLS-1$
                command.setTitle(ConstantsManager.getInstance().getConstants().ok());
                command.setIsDefault(true);
                innerHostModel.getCommands().add(command);

                command = new UICommand("Cancel", hostListModel); //$NON-NLS-1$
                command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                command.setIsCancel(true);
                innerHostModel.getCommands().add(command);
            }
        };
        AsyncDataProvider.GetDataCenterList(_asyncQuery);
    }

    private void GoToEventsTab()
    {
        setActiveDetailModel(getHostEventListModel());
    }

    public void Edit(final boolean isEditWithPMemphasis)
    {
        if (getWindow() != null)
        {
            return;
        }

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                HostListModel hostListModel = (HostListModel) model;
                ArrayList<storage_pool> dataCenters = (ArrayList<storage_pool>) result;
                VDS host = (VDS) hostListModel.getSelectedItem();

                final HostModel hostModel = new HostModel();
                hostListModel.setWindow(hostModel);
                PrepareModelForApproveEdit(host, hostModel, dataCenters, isEditWithPMemphasis);
                hostModel.setTitle(ConstantsManager.getInstance().getConstants().editHostTitle());
                hostModel.setHashName("edit_host"); //$NON-NLS-1$

                if (host.getPmProxyPreferences() != null) {
                    hostModel.setPmProxyPreferences(host.getPmProxyPreferences());
                } else {
                    AsyncDataProvider.GetDefaultPmProxyPreferences(new AsyncQuery(null, new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object model, Object returnValue) {

                            hostModel.setPmProxyPreferences((String) returnValue);
                        }
                    }));
                }


                UICommand command;
                command = new UICommand("OnSaveFalse", hostListModel); //$NON-NLS-1$
                command.setTitle(ConstantsManager.getInstance().getConstants().ok());
                command.setIsDefault(true);
                hostModel.getCommands().add(command);

                command = new UICommand("Cancel", hostListModel); //$NON-NLS-1$
                command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                command.setIsCancel(true);
                hostModel.getCommands().add(command);
            }
        };
        AsyncDataProvider.GetDataCenterList(_asyncQuery);
    }

    public void OnSaveFalse()
    {
        OnSave(false);
    }

    public void OnSave(boolean approveInitiated)
    {
        HostModel model = (HostModel) getWindow();

        if (!model.Validate())
        {
            return;
        }

        if (!((Boolean) model.getIsPm().getEntity()))
        {
            if (((VDSGroup) model.getCluster().getSelectedItem()).supportsVirtService())
            {
                ConfirmationModel confirmModel = new ConfirmationModel();
                setConfirmWindow(confirmModel);
                confirmModel.setTitle(ConstantsManager.getInstance().getConstants().powerManagementConfigurationTitle());
                confirmModel.setHashName("power_management_configuration"); //$NON-NLS-1$
                confirmModel.setMessage(ConstantsManager.getInstance().getConstants().youHavntConfigPmMsg());


                UICommand command;

                command = new UICommand(approveInitiated ? "OnSaveInternalFromApprove" : "OnSaveInternalNotFromApprove", this); //$NON-NLS-1$ //$NON-NLS-2$
                command.setTitle(ConstantsManager.getInstance().getConstants().ok());
                command.setIsDefault(true);
                confirmModel.getCommands().add(command);

                command = new UICommand("CancelConfirmFocusPM", this); //$NON-NLS-1$
                command.setTitle(ConstantsManager.getInstance().getConstants().configurePowerManagement());
                command.setIsCancel(true);
                confirmModel.getCommands().add(command);
            }
            else
            {
                if(approveInitiated)
                {
                    OnSaveInternalFromApprove();
                }
                else
                {
                    OnSaveInternalNotFromApprove();
                }
            }
        }
        else
        {
            OnSaveInternal(approveInitiated);
        }

    }

    public void CancelConfirmFocusPM()
    {
        HostModel hostModel = (HostModel) getWindow();
        hostModel.setIsPowerManagementTabSelected(true);

        setConfirmWindow(null);
    }

    public void OnSaveInternalNotFromApprove()
    {
        OnSaveInternal(false);
    }

    public void OnSaveInternalFromApprove()
    {
        OnSaveInternal(true);
    }

    public void OnSaveInternal(boolean approveInitiated)
    {
        HostModel model = (HostModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        VDS host = model.getIsNew() ? new VDS() : (VDS) Cloner.clone(getSelectedItem());

        // Save changes.
        host.setVdsName((String) model.getName().getEntity());
        host.setHostName((String) model.getHost().getEntity());
        host.setPort(Integer.parseInt(model.getPort().getEntity().toString()));
        host.setVdsSpmPriority(model.getSpmPriorityValue());
        boolean consoleAddressSet = (Boolean) model.getConsoleAddressEnabled().getEntity();
        host.setConsoleAddress(!consoleAddressSet ? null : (String) model.getConsoleAddress().getEntity());
        Guid oldClusterId = host.getVdsGroupId();
        Guid newClusterId = ((VDSGroup) model.getCluster().getSelectedItem()).getId();
        host.setVdsGroupId(newClusterId);
        host.setVdsSpmPriority(model.getSpmPriorityValue());
        host.setPmProxyPreferences(model.getPmProxyPreferences());

        // Save primary PM parameters.
        host.setManagementIp((String) model.getManagementIp().getEntity());
        host.setPmUser((String) model.getPmUserName().getEntity());
        host.setPmPassword((String) model.getPmPassword().getEntity());
        host.setPmType((String) model.getPmType().getSelectedItem());
        host.setPmOptionsMap(new ValueObjectMap(model.getPmOptionsMap(), false));

        // Save secondary PM parameters.
        host.setPmSecondaryIp((String) model.getPmSecondaryIp().getEntity());
        host.setPmSecondaryUser((String) model.getPmSecondaryUserName().getEntity());
        host.setPmSecondaryPassword((String) model.getPmSecondaryPassword().getEntity());
        host.setPmSecondaryType((String) model.getPmSecondaryType().getSelectedItem());
        host.setPmSecondaryOptionsMap(new ValueObjectMap(model.getPmSecondaryOptionsMap(), false));

        // Save other PM parameters.
        host.setpm_enabled((Boolean) model.getIsPm().getEntity());
        host.setPmSecondaryConcurrent((Boolean) model.getPmSecondaryConcurrent().getEntity());


        CancelConfirm();
        model.StartProgress(null);

        if (model.getIsNew())
        {
            AddVdsActionParameters parameters = new AddVdsActionParameters();
            parameters.setVdsId(host.getId());
            parameters.setvds(host);
            parameters.setRootPassword((String) model.getRootPassword().getEntity());
            parameters.setOverrideFirewall((Boolean) model.getOverrideIpTables().getEntity());

            Frontend.RunAction(VdcActionType.AddVds, parameters,
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                            Object[] array = (Object[]) result.getState();
                            HostListModel localModel = (HostListModel) array[0];
                            boolean localApproveInitiated = (Boolean) array[1];
                            localModel.PostOnSaveInternal(result.getReturnValue(), localApproveInitiated);

                        }
                    }, new Object[] { this, approveInitiated });
        }
        else // Update VDS -> consists of changing VDS cluster first and then updating rest of VDS properties:
        {
            UpdateVdsActionParameters parameters = new UpdateVdsActionParameters();
            parameters.setvds(host);
            parameters.setVdsId(host.getId());
            parameters.setRootPassword(""); //$NON-NLS-1$
            parameters.setInstallVds(false);

            if (!oldClusterId.equals(newClusterId))
            {
                Frontend.RunAction(VdcActionType.ChangeVDSCluster,
                        new ChangeVDSClusterParameters(newClusterId, host.getId()),
                        new IFrontendActionAsyncCallback() {
                            @Override
                            public void Executed(FrontendActionAsyncResult result) {

                                Object[] array = (Object[]) result.getState();
                                HostListModel localModel = (HostListModel) array[0];
                                UpdateVdsActionParameters localParameters = (UpdateVdsActionParameters) array[1];
                                boolean localApproveInitiated = (Boolean) array[2];
                                VdcReturnValueBase localReturnValue = result.getReturnValue();
                                if (localReturnValue != null && localReturnValue.getSucceeded())
                                {
                                    localModel.PostOnSaveInternalChangeCluster(localParameters, localApproveInitiated);
                                }
                                else
                                {
                                    localModel.getWindow().StopProgress();
                                }

                            }
                        },
                        new Object[] { this, parameters, approveInitiated });
            }
            else
            {
                PostOnSaveInternalChangeCluster(parameters, approveInitiated);
            }
        }
    }

    public void PostOnSaveInternalChangeCluster(UpdateVdsActionParameters parameters, boolean approveInitiated)
    {
        Frontend.RunAction(VdcActionType.UpdateVds, parameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        Object[] array = (Object[]) result.getState();
                        HostListModel localModel = (HostListModel) array[0];
                        boolean localApproveInitiated = (Boolean) array[1];
                        localModel.PostOnSaveInternal(result.getReturnValue(), localApproveInitiated);

                    }
                }, new Object[] { this, approveInitiated });
    }

    public void PostOnSaveInternal(VdcReturnValueBase returnValue, boolean approveInitiated)
    {
        HostModel model = (HostModel) getWindow();

        model.StopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            if (approveInitiated)
            {
                OnApproveInternal();
            }
            Cancel();
        }
    }

    private void OnApproveInternal()
    {
        VDS vds = (VDS) getSelectedItem();

        Frontend.RunMultipleAction(VdcActionType.ApproveVds,
                new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] { new ApproveVdsParameters(vds.getId()) })),
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                    }
                },
                null);
    }

    public void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        final ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeHostsTitle());
        model.setHashName("remove_host"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().hostsMsg());

        Set<Guid> clusters = new HashSet<Guid>();
        ArrayList<String> list = new ArrayList<String>();
        for (VDS item : Linq.<VDS> Cast(getSelectedItems()))
        {
            list.add(item.getName());
            clusters.add(item.getVdsGroupId());
        }
        model.setItems(list);

        // Remove Force option will be shown only if
        // - All the selected hosts belongs to same cluster
        // - the cluster should be a gluster only cluster
        if (clusters.size() == 1) {
            model.StartProgress(null);
            AsyncDataProvider.GetClusterById(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {
                    VDSGroup cluster = (VDSGroup) returnValue;
                    if (cluster != null && cluster.supportsGlusterService() && !cluster.supportsVirtService()) {
                        model.getForce().setIsAvailable(true);
                    }
                    model.StopProgress();
                }
            }), clusters.iterator().next());
        }

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnRemove()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        boolean force = (Boolean) model.getForce().getEntity();
        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VDS vds = (VDS) item;
            list.add(new RemoveVdsParameters(vds.getId(), force));
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveVds, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    public void Activate()
    {
        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VDS vds = (VDS) item;
            list.add(new VdsActionParameters(vds.getId()));
        }

        Frontend.RunMultipleAction(VdcActionType.ActivateVds, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                    }
                }, null);
    }

    public void Maintenance()
    {
        if (getConfirmWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().maintenanceHostsTitle());
        model.setHashName("maintenance_host"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance()
                .getConstants()
                .areYouSureYouWantToPlaceFollowingHostsIntoMaintenanceModeMsg());
        // model.Items = SelectedItems.Cast<VDS>().Select(a => a.vds_name);
        ArrayList<String> vdss = new ArrayList<String>();
        for (Object item : getSelectedItems())
        {
            VDS vds = (VDS) item;
            vdss.add(vds.getName());
        }
        model.setItems(vdss);

        UICommand tempVar = new UICommand("OnMaintenance", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("CancelConfirm", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnMaintenance()
    {
        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        ArrayList<Guid> vdss = new ArrayList<Guid>();

        for (Object item : getSelectedItems())
        {
            VDS vds = (VDS) item;
            vdss.add(vds.getId());
        }
        list.add(new MaintananceNumberOfVdssParameters(vdss, false));

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.MaintananceNumberOfVdss, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        CancelConfirm();

                    }
                }, model);
    }

    public void Approve()
    {
        VDS host = (VDS) getSelectedItem();
        HostModel hostModel = new HostModel();
        setWindow(hostModel);
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                HostListModel hostListModel = (HostListModel) model;
                HostModel innerHostModel = (HostModel) hostListModel.getWindow();
                ArrayList<storage_pool> dataCenters = (ArrayList<storage_pool>) result;
                VDS host = (VDS) hostListModel.getSelectedItem();
                hostListModel.PrepareModelForApproveEdit(host, innerHostModel, dataCenters, false);
                innerHostModel.setTitle(ConstantsManager.getInstance().getConstants().editAndApproveHostTitle());
                innerHostModel.setHashName("edit_and_approve_host"); //$NON-NLS-1$

                UICommand tempVar = new UICommand("OnApprove", hostListModel); //$NON-NLS-1$
                tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
                tempVar.setIsDefault(true);
                innerHostModel.getCommands().add(tempVar);
                UICommand tempVar2 = new UICommand("Cancel", hostListModel); //$NON-NLS-1$
                tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                tempVar2.setIsCancel(true);
                innerHostModel.getCommands().add(tempVar2);
            }
        };
        AsyncDataProvider.GetDataCenterList(_asyncQuery);
    }

    private void PrepareModelForApproveEdit(VDS vds,
            HostModel model,
            ArrayList<storage_pool> dataCenters,
            boolean isEditWithPMemphasis)
    {
        model.setHostId(vds.getId());
        model.getRootPassword().setIsAvailable(false);
        model.getOverrideIpTables().setIsAvailable(false);
        model.setSpmPriorityValue(vds.getVdsSpmPriority());
        model.setOriginalName(vds.getName());
        model.getName().setEntity(vds.getName());
        model.getHost().setEntity(vds.getHostName());
        model.getPort().setEntity(vds.getPort());

        boolean consoleAddressEnabled = vds.getConsoleAddress() != null;
        model.getConsoleAddressEnabled().setEntity(consoleAddressEnabled);
        model.getConsoleAddress().setEntity(vds.getConsoleAddress());
        model.getConsoleAddress().setIsChangable(consoleAddressEnabled);

        if (vds.getStatus() != VDSStatus.InstallFailed)
        {
            model.getHost().setIsChangable(false);
        }

        // Set primary PM parameters.
        model.getManagementIp().setEntity(vds.getManagementIp());
        model.getPmUserName().setEntity(vds.getPmUser());
        model.getPmPassword().setEntity(vds.getPmPassword());
        model.getPmType().setSelectedItem(vds.getPmType());
        model.setPmOptionsMap(VdsStatic.PmOptionsStringToMap(vds.getPmOptions()).asMap());

        // Set secondary PM parameters.
        model.getPmSecondaryIp().setEntity(vds.getPmSecondaryIp());
        model.getPmSecondaryUserName().setEntity(vds.getPmSecondaryUser());
        model.getPmSecondaryPassword().setEntity(vds.getPmSecondaryPassword());
        model.getPmSecondaryType().setSelectedItem(vds.getPmSecondaryType());
        model.setPmSecondaryOptionsMap(vds.getPmSecondaryOptionsMap().asMap());

        // Set other PM parameters.
        if (isEditWithPMemphasis) {
            model.setIsPowerManagementTabSelected(true);
            model.getIsPm().setEntity(true);
            model.getIsPm().setIsChangable(false);
        } else {
            model.getIsPm().setEntity(vds.getpm_enabled());
        }

        model.getPmSecondaryConcurrent().setEntity(vds.isPmSecondaryConcurrent());


        if (dataCenters != null)
        {
            model.getDataCenter().setItems(dataCenters);
            model.getDataCenter().setSelectedItem(Linq.FirstOrDefault(dataCenters,
                    new Linq.DataCenterPredicate(vds.getStoragePoolId())));
            if (model.getDataCenter().getSelectedItem() == null) {
                model.getDataCenter().setSelectedItem(Linq.FirstOrDefault(dataCenters));
            }
        }

        ArrayList<VDSGroup> clusters;
        if (model.getCluster().getItems() == null)
        {
            VDSGroup tempVar = new VDSGroup();
            tempVar.setname(vds.getVdsGroupName());
            tempVar.setId(vds.getVdsGroupId());
            tempVar.setcompatibility_version(vds.getVdsGroupCompatibilityVersion());
            model.getCluster()
                    .setItems(new ArrayList<VDSGroup>(Arrays.asList(new VDSGroup[] { tempVar })));
        }
        clusters = (ArrayList<VDSGroup>) model.getCluster().getItems();
        model.getCluster().setSelectedItem(Linq.FirstOrDefault(clusters,
                new Linq.ClusterPredicate(vds.getVdsGroupId())));
        if (model.getCluster().getSelectedItem() == null)
        {
            model.getCluster().setSelectedItem(Linq.FirstOrDefault(clusters));
        }

        if (vds.getStatus() != VDSStatus.Maintenance && vds.getStatus() != VDSStatus.PendingApproval)
        {
            model.getDataCenter()
                    .setChangeProhibitionReason("Data Center can be changed only when the Host is in Maintenance mode."); //$NON-NLS-1$
            model.getDataCenter().setIsChangable(false);
            model.getCluster()
                    .setChangeProhibitionReason("Cluster can be changed only when the Host is in Maintenance mode."); //$NON-NLS-1$
            model.getCluster().setIsChangable(false);
        }
        else if (getSystemTreeSelectedItem() != null)
        {
            switch (getSystemTreeSelectedItem().getType())
            {
            case Host:
                model.getName().setIsChangable(false);
                model.getName().setInfo("Cannot edit Host's Name in this tree context"); //$NON-NLS-1$
                break;
            case Hosts:
            case Cluster:
                model.getCluster().setIsChangable(false);
                model.getCluster().setInfo("Cannot change Host's Cluster in tree context"); //$NON-NLS-1$
                model.getDataCenter().setIsChangable(false);
                break;
            case DataCenter:
                storage_pool selectDataCenter = (storage_pool) getSystemTreeSelectedItem().getEntity();
                model.getDataCenter()
                        .setItems(new ArrayList<storage_pool>(Arrays.asList(new storage_pool[] { selectDataCenter })));
                model.getDataCenter().setSelectedItem(selectDataCenter);
                model.getDataCenter().setIsChangable(false);
                break;
            default:
                break;
            }
        }
    }

    public void OnApprove()
    {
        OnSave(true);
    }

    public void Restart()
    {
        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().restartHostsTitle());
        model.setHashName("restart_host"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().areYouSureYouWantToRestartTheFollowingHostsMsg());
        // model.Items = SelectedItems.Cast<VDS>().Select(a => a.vds_name);
        ArrayList<String> items = new ArrayList<String>();
        for (Object item : getSelectedItems())
        {
            VDS vds = (VDS) item;
            items.add(vds.getName());
        }
        model.setItems(items);

        UICommand tempVar = new UICommand("OnRestart", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnRestart()
    {
        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VDS vds = (VDS) item;
            list.add(new FenceVdsActionParameters(vds.getId(), FenceActionType.Restart));
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RestartVds, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        CancelConfirm();

                    }
                }, model);
    }

    public void start()
    {
        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VDS vds = (VDS) item;
            list.add(new FenceVdsActionParameters(vds.getId(), FenceActionType.Start));
        }

        Frontend.RunMultipleAction(VdcActionType.StartVds, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                    }
                }, null);
    }

    public void stop()
    {
        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().stopHostsTitle());
        model.setHashName("stop_host"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().areYouSureYouWantToStopTheFollowingHostsMsg());
        // model.Items = SelectedItems.Cast<VDS>().Select(a => a.vds_name);
        ArrayList<String> items = new ArrayList<String>();
        for (Object item : getSelectedItems())
        {
            VDS vds = (VDS) item;
            items.add(vds.getName());
        }
        model.setItems(items);

        UICommand tempVar = new UICommand("OnStop", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnStop()
    {
        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VDS vds = (VDS) item;
            list.add(new FenceVdsActionParameters(vds.getId(), FenceActionType.Stop));
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.StopVds, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        CancelConfirm();

                    }
                }, model);
    }

    private void ConfigureLocalStorage() {

        VDS host = (VDS) getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        ConfigureLocalStorageModel model = new ConfigureLocalStorageModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().configureLocalStorageTitle());
        model.setHashName("configure_local_storage"); //$NON-NLS-1$

        if (host.getVdsType() == VDSType.oVirtNode) {
            configureLocalStorage2(model);
        } else {
            configureLocalStorage3(model);
        }
    }

    private void configureLocalStorage2(ConfigureLocalStorageModel model) {

        AsyncDataProvider.GetLocalFSPath(new AsyncQuery(model,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        String prefix = (String) returnValue;
                        ConfigureLocalStorageModel model = (ConfigureLocalStorageModel) target;

                        if (!StringHelper.isNullOrEmpty(prefix)) {

                            EntityModel pathModel = model.getStorage().getPath();
                            pathModel.setEntity(prefix);
                            pathModel.setIsChangable(false);
                        }

                        configureLocalStorage3(model);
                    }
                })
                );
    }

    private void configureLocalStorage3(ConfigureLocalStorageModel model) {

        VDS host = (VDS) getSelectedItem();

        boolean hostSupportLocalStorage = false;
        Version version3_0 = new Version(3, 0);

        if (host.getSupportedClusterLevels() != null) {

            String[] array = host.getSupportedClusterLevels().split("[,]", -1); //$NON-NLS-1$

            for (int i = 0; i < array.length; i++) {
                if (version3_0.compareTo(new Version(array[i])) <= 0) {
                    hostSupportLocalStorage = true;
                    break;
                }
            }
        }

        UICommand command;

        if (hostSupportLocalStorage) {

            model.SetDefaultNames(host);

            command = new UICommand("OnConfigureLocalStorage", this); //$NON-NLS-1$
            command.setTitle(ConstantsManager.getInstance().getConstants().ok());
            command.setIsDefault(true);
            model.getCommands().add(command);

            command = new UICommand("Cancel", this); //$NON-NLS-1$
            command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            command.setIsCancel(true);
            model.getCommands().add(command);
        } else {

            model.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .hostDoesntSupportLocalStorageConfigurationMsg());

            command = new UICommand("Cancel", this); //$NON-NLS-1$
            command.setTitle(ConstantsManager.getInstance().getConstants().close());
            command.setIsCancel(true);
            command.setIsDefault(true);
            model.getCommands().add(command);
        }
    }

    private void OnConfigureLocalStorage() {

        ConfigureLocalStorageModel model = (ConfigureLocalStorageModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (!model.Validate()) {
            return;
        }

        model.StartProgress(ConstantsManager.getInstance().getConstants().configuringLocalStorageHost());

        ReversibleFlow flow = new ReversibleFlow();
        flow.getCompleteEvent().addListener(
                new IEventListener() {
                    @Override
                    public void eventRaised(Event ev, Object sender, EventArgs args) {

                        ConfigureLocalStorageModel model = (ConfigureLocalStorageModel) ev.getContext();

                        model.StopProgress();
                        Cancel();
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

    @Override
    protected void InitDetailModels()
    {
        super.InitDetailModels();

        HostGeneralModel generalModel = new HostGeneralModel();
        generalModel.getRequestEditEvent().addListener(this);
        generalModel.getRequestGOToEventsTabEvent().addListener(this);
        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(generalModel);
        list.add(new HostHardwareGeneralModel());
        list.add(new HostVmListModel());
        list.add(new HostInterfaceListModel());
        setHostEventListModel(new HostEventListModel());
        list.add(getHostEventListModel());
        list.add(new HostHooksListModel());
        list.add(new PermissionListModel());
        setDetailModels(list);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(HostGeneralModel.RequestEditEventDefinition))
        {
            getEditWithPMemphasisCommand().Execute();
        }
        if (ev.matchesDefinition(HostGeneralModel.RequestGOToEventsTabEventDefinition))
        {
            GoToEventsTab();
        }
    }

    @Override
    public boolean IsSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("host"); //$NON-NLS-1$
    }

    @Override
    protected void SyncSearch()
    {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.VDS);
        tempVar.setMaxCount(getSearchPageSize());
        super.SyncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterSearch(getSearchString(), SearchType.VDS, getSearchPageSize()));
        setItems(getAsyncResult().getData());
    }

    public void Cancel()
    {
        CancelConfirm();
        setWindow(null);
    }

    public void CancelConfirm()
    {
        setConfirmWindow(null);
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void SelectedItemsChanged()
    {
        super.SelectedItemsChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void ItemsCollectionChanged(Object sender, NotifyCollectionChangedEventArgs e)
    {
        super.ItemsCollectionChanged(sender, e);

        // Try to select an item corresponding to the system tree selection.
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Host)
        {
            VDS host = (VDS) getSystemTreeSelectedItem().getEntity();

            setSelectedItem(Linq.FirstOrDefault(Linq.<VDS> Cast(getItems()), new Linq.HostPredicate(host.getId())));
        }
    }

    @Override
    protected void SelectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.SelectedItemPropertyChanged(sender, e);

        if (e.PropertyName.equals("status") || e.PropertyName.equals("pm_enabled")) //$NON-NLS-1$ //$NON-NLS-2$
        {
            UpdateActionAvailability();
        }
    }

    private void UpdateActionAvailability()
    {
        ArrayList<VDS> items =
                getSelectedItems() != null ? Linq.<VDS> Cast(getSelectedItems()) : new ArrayList<VDS>();

        boolean isAllPMEnabled = Linq.FindAllVDSByPmEnabled(items).size() == items.size();

        getEditCommand().setIsExecutionAllowed(items.size() == 1
                && VdcActionUtils.CanExecute(items, VDS.class, VdcActionType.UpdateVds));

        getEditWithPMemphasisCommand().setIsExecutionAllowed(items.size() == 1
                && VdcActionUtils.CanExecute(items, VDS.class, VdcActionType.UpdateVds));

        getRemoveCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VDS.class, VdcActionType.RemoveVds));

        getActivateCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VDS.class, VdcActionType.ActivateVds));

        // or special case where its installation failed but its oVirt node
        boolean approveAvailability =
                items.size() == 1
                        && (VdcActionUtils.CanExecute(items, VDS.class, VdcActionType.ApproveVds) || (items.get(0)
                                .getStatus() == VDSStatus.InstallFailed && items.get(0).getVdsType() == VDSType.oVirtNode));
        getApproveCommand().setIsExecutionAllowed(approveAvailability);
        getApproveCommand().setIsAvailable(approveAvailability);

        getMaintenanceCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VDS.class, VdcActionType.MaintananceVds));

        getRestartCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VDS.class, VdcActionType.RestartVds) && isAllPMEnabled);

        getStartCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VDS.class, VdcActionType.StartVds) && isAllPMEnabled);

        getStopCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VDS.class, VdcActionType.StopVds) && isAllPMEnabled);

        setIsPowerManagementEnabled(getRestartCommand().getIsExecutionAllowed()
                || getStartCommand().getIsExecutionAllowed() || getStopCommand().getIsExecutionAllowed());

        getManualFenceCommand().setIsExecutionAllowed(items.size() == 1);

        getAssignTagsCommand().setIsExecutionAllowed(items.size() > 0);

        // System tree dependent actions.
        boolean isAvailable =
                !(getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Host);

        getNewCommand().setIsAvailable(isAvailable);
        getRemoveCommand().setIsAvailable(isAvailable);

        UpdateConfigureLocalStorageCommandAvailability();
    }

    private Boolean hasAdminSystemPermission = null;

    public void UpdateConfigureLocalStorageCommandAvailability() {

        if (hasAdminSystemPermission == null) {

            VdcUser vdcUser = Frontend.getLoggedInUser();

            if (vdcUser == null) {
                hasAdminSystemPermission = false;
                UpdateConfigureLocalStorageCommandAvailability1();
                return;
            }

            Frontend.RunQuery(VdcQueryType.GetPermissionsByAdElementId,
                    new MultilevelAdministrationByAdElementIdParameters(vdcUser.getUserId()),
                    new AsyncQuery(this, new INewAsyncCallback() {

                        @Override
                        public void OnSuccess(Object model, Object returnValue) {
                            VdcQueryReturnValue response = (VdcQueryReturnValue) returnValue;
                            if (response == null || !response.getSucceeded()) {
                                hasAdminSystemPermission = false;
                                UpdateConfigureLocalStorageCommandAvailability1();
                            } else {
                                ArrayList<permissions> permissions =
                                        (ArrayList<permissions>) response.getReturnValue();
                                for (permissions permission : permissions) {

                                    if (permission.getObjectType() == VdcObjectType.System
                                            && permission.getRoleType() == RoleType.ADMIN) {
                                        hasAdminSystemPermission = true;
                                        break;
                                    }
                                }

                                UpdateConfigureLocalStorageCommandAvailability1();
                            }

                        }
                    }, true));
        } else {
            UpdateConfigureLocalStorageCommandAvailability1();
        }
    }

    private void UpdateConfigureLocalStorageCommandAvailability1() {

        ArrayList<VDS> items = getSelectedItems() != null ? Linq.<VDS> Cast(getSelectedItems()) : new ArrayList<VDS>();

        getConfigureLocalStorageCommand().setIsExecutionAllowed(items.size() == 1
                && items.get(0).getStatus() == VDSStatus.Maintenance);

        if (!hasAdminSystemPermission && getConfigureLocalStorageCommand().getIsExecutionAllowed()) {

            getConfigureLocalStorageCommand().getExecuteProhibitionReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .configuringLocalStoragePermittedOnlyAdministratorsWithSystemLevelPermissionsReason());
            getConfigureLocalStorageCommand().setIsExecutionAllowed(false);
        }
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getNewCommand())
        {
            New();
        }
        else if (command == getEditCommand())
        {
            Edit(false);
        }
        else if (command == getEditWithPMemphasisCommand())
        {
            Edit(true);
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (command == getActivateCommand())
        {
            Activate();
        }
        else if (command == getMaintenanceCommand())
        {
            Maintenance();
        }
        else if (command == getApproveCommand())
        {
            Approve();
        }
        else if (command == getRestartCommand())
        {
            Restart();
        }
        else if (command == getStartCommand())
        {
            start();
        }
        else if (command == getStopCommand())
        {
            stop();
        }
        else if (command == getManualFenceCommand())
        {
            ManualFence();
        }
        else if (command == getAssignTagsCommand())
        {
            AssignTags();
        }
        else if (command == getConfigureLocalStorageCommand())
        {
            ConfigureLocalStorage();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnAssignTags")) //$NON-NLS-1$
        {
            OnAssignTags();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnManualFence")) //$NON-NLS-1$
        {
            OnManualFence();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSaveFalse")) //$NON-NLS-1$
        {
            OnSaveFalse();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSaveInternalFromApprove")) //$NON-NLS-1$
        {
            OnSaveInternalFromApprove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSaveInternalNotFromApprove")) //$NON-NLS-1$
        {
            OnSaveInternalNotFromApprove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirm")) //$NON-NLS-1$
        {
            CancelConfirm();
        }
        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirmFocusPM")) //$NON-NLS-1$
        {
            CancelConfirmFocusPM();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            OnRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnMaintenance")) //$NON-NLS-1$
        {
            OnMaintenance();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnApprove")) //$NON-NLS-1$
        {
            OnApprove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRestart")) //$NON-NLS-1$
        {
            OnRestart();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnStop")) //$NON-NLS-1$
        {
            OnStop();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnConfigureLocalStorage")) //$NON-NLS-1$
        {
            OnConfigureLocalStorage();
        }
    }

    // @Override
    // public void run(TaskContext context)
    // {
    // switch ((Integer) context.getState())
    // {
    // case 1:
    // try
    // {
    // // override default timeout (60 sec) with 10 minutes
    // TransactionScope scope = new TransactionScope(TransactionScopeOption.Required, new TimeSpan(0, 10, 0));
    // try
    // {
    // new AddDataCenterRM(this);
    // scope.Complete();
    // } finally
    // {
    // scope.dispose();
    // }
    // } catch (TransactionAbortedException e)
    // {
    // // Do nothing.
    // } finally
    // {
    // context.InvokeUIThread(this, 2);
    // }
    // break;
    //
    // case 2:
    // StopProgress();
    //
    // Cancel();
    // break;
    // }
    // }

    private SystemTreeItemModel systemTreeSelectedItem;

    @Override
    public SystemTreeItemModel getSystemTreeSelectedItem()
    {
        return systemTreeSelectedItem;
    }

    @Override
    public void setSystemTreeSelectedItem(SystemTreeItemModel value)
    {
        if (systemTreeSelectedItem != value)
        {
            systemTreeSelectedItem = value;
            OnSystemTreeSelectedItemChanged();
        }
    }

    private void OnSystemTreeSelectedItemChanged()
    {
        UpdateActionAvailability();
    }

    @Override
    protected String getListName() {
        return "HostListModel"; //$NON-NLS-1$
    }
}
