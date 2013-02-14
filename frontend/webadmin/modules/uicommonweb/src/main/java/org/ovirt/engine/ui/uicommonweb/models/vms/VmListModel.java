package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.AddVmFromScratchParameters;
import org.ovirt.engine.core.common.action.AddVmFromTemplateParameters;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.HibernateVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.action.MoveVmParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
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
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.ChangeCDModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.VmBaseListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.AttachCdModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

public class VmListModel extends VmBaseListModel<VM> implements ISupportSystemTreeContext
{
    private UICommand privateNewServerCommand;

    public UICommand getNewServerCommand()
    {
        return privateNewServerCommand;
    }

    private void setNewServerCommand(UICommand value)
    {
        privateNewServerCommand = value;
    }

    private UICommand privateNewDesktopCommand;

    public UICommand getNewDesktopCommand()
    {
        return privateNewDesktopCommand;
    }

    private void setNewDesktopCommand(UICommand value)
    {
        privateNewDesktopCommand = value;
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

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand()
    {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value)
    {
        privateRemoveCommand = value;
    }

    private UICommand privateRunCommand;

    public UICommand getRunCommand()
    {
        return privateRunCommand;
    }

    private void setRunCommand(UICommand value)
    {
        privateRunCommand = value;
    }

    private UICommand privatePauseCommand;

    public UICommand getPauseCommand()
    {
        return privatePauseCommand;
    }

    private void setPauseCommand(UICommand value)
    {
        privatePauseCommand = value;
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

    private UICommand privateShutdownCommand;

    public UICommand getShutdownCommand()
    {
        return privateShutdownCommand;
    }

    private void setShutdownCommand(UICommand value)
    {
        privateShutdownCommand = value;
    }

    private UICommand privateCancelMigrateCommand;

    public UICommand getCancelMigrateCommand() {
        return privateCancelMigrateCommand;
    }

    private void setCancelMigrateCommand(UICommand value) {
        privateCancelMigrateCommand = value;
    }

    private UICommand privateMigrateCommand;

    public UICommand getMigrateCommand()
    {
        return privateMigrateCommand;
    }

    private void setMigrateCommand(UICommand value)
    {
        privateMigrateCommand = value;
    }

    private UICommand privateNewTemplateCommand;

    public UICommand getNewTemplateCommand()
    {
        return privateNewTemplateCommand;
    }

    private void setNewTemplateCommand(UICommand value)
    {
        privateNewTemplateCommand = value;
    }

    private UICommand privateRunOnceCommand;

    public UICommand getRunOnceCommand()
    {
        return privateRunOnceCommand;
    }

    private void setRunOnceCommand(UICommand value)
    {
        privateRunOnceCommand = value;
    }

    private UICommand privateExportCommand;

    public UICommand getExportCommand()
    {
        return privateExportCommand;
    }

    private void setExportCommand(UICommand value)
    {
        privateExportCommand = value;
    }

    private UICommand privateCreateSnapshotCommand;

    public UICommand getCreateSnapshotCommand()
    {
        return privateCreateSnapshotCommand;
    }

    private void setCreateSnapshotCommand(UICommand value)
    {
        privateCreateSnapshotCommand = value;
    }

    private UICommand privateMoveCommand;

    public UICommand getMoveCommand()
    {
        return privateMoveCommand;
    }

    private void setMoveCommand(UICommand value)
    {
        privateMoveCommand = value;
    }

    private UICommand privateRetrieveIsoImagesCommand;

    public UICommand getRetrieveIsoImagesCommand()
    {
        return privateRetrieveIsoImagesCommand;
    }

    private void setRetrieveIsoImagesCommand(UICommand value)
    {
        privateRetrieveIsoImagesCommand = value;
    }

    private UICommand privateGuideCommand;

    public UICommand getGuideCommand()
    {
        return privateGuideCommand;
    }

    private void setGuideCommand(UICommand value)
    {
        privateGuideCommand = value;
    }

    private UICommand privateChangeCdCommand;

    public UICommand getChangeCdCommand()
    {
        return privateChangeCdCommand;
    }

    private void setChangeCdCommand(UICommand value)
    {
        privateChangeCdCommand = value;
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

    private ConsoleModel defaultConsoleModel;

    public ConsoleModel getDefaultConsoleModel()
    {
        return defaultConsoleModel;
    }

    public void setDefaultConsoleModel(ConsoleModel value)
    {
        if (defaultConsoleModel != value)
        {
            defaultConsoleModel = value;
            OnPropertyChanged(new PropertyChangedEventArgs("DefaultConsoleModel")); //$NON-NLS-1$
        }
    }

    private ConsoleModel additionalConsoleModel;

    public ConsoleModel getAdditionalConsoleModel()
    {
        return additionalConsoleModel;
    }

    public void setAdditionalConsoleModel(ConsoleModel value)
    {
        if (additionalConsoleModel != value)
        {
            additionalConsoleModel = value;
            OnPropertyChanged(new PropertyChangedEventArgs("AdditionalConsoleModel")); //$NON-NLS-1$
        }
    }

    private boolean hasAdditionalConsoleModel;

    public boolean getHasAdditionalConsoleModel()
    {
        return hasAdditionalConsoleModel;
    }

    public void setHasAdditionalConsoleModel(boolean value)
    {
        if (hasAdditionalConsoleModel != value)
        {
            hasAdditionalConsoleModel = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasAdditionalConsoleModel")); //$NON-NLS-1$
        }
    }

    public ObservableCollection<ChangeCDModel> isoImages;

    public ObservableCollection<ChangeCDModel> getIsoImages()
    {
        return isoImages;
    }

    private void setIsoImages(ObservableCollection<ChangeCDModel> value)
    {
        if ((isoImages == null && value != null) || (isoImages != null && !isoImages.equals(value)))
        {
            isoImages = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsoImages")); //$NON-NLS-1$
        }
    }

    // get { return SelectedItems == null ? new object[0] : SelectedItems.Cast<VM>().Select(a =>
    // a.vm_guid).Cast<object>().ToArray(); }
    protected Object[] getSelectedKeys()
    {
        if (getSelectedItems() == null)
        {
            return new Object[0];
        }

        Object[] keys = new Object[getSelectedItems().size()];
        for (int i = 0; i < getSelectedItems().size(); i++)
        {
            keys[i] = ((VM) getSelectedItems().get(i)).getId();
        }

        return keys;
    }

    private Object privateGuideContext;

    public Object getGuideContext()
    {
        return privateGuideContext;
    }

    public void setGuideContext(Object value)
    {
        privateGuideContext = value;
    }

    private VM privatecurrentVm;

    public VM getcurrentVm()
    {
        return privatecurrentVm;
    }

    public void setcurrentVm(VM value)
    {
        privatecurrentVm = value;
    }

    private final HashMap<Guid, ArrayList<ConsoleModel>> cachedConsoleModels;

    private HashMap<Version, ArrayList<String>> privateCustomPropertiesKeysList;

    private HashMap<Version, ArrayList<String>> getCustomPropertiesKeysList() {
        return privateCustomPropertiesKeysList;
    }

    private void setCustomPropertiesKeysList(HashMap<Version, ArrayList<String>> value) {
        privateCustomPropertiesKeysList = value;
    }

    public VmListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().virtualMachinesTitle());
        setHashName("virtual_machines"); //$NON-NLS-1$

        setDefaultSearchString("Vms:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.VM_OBJ_NAME, SearchObjects.VM_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        cachedConsoleModels = new HashMap<Guid, ArrayList<ConsoleModel>>();

        setNewServerCommand(new UICommand("NewServer", this)); //$NON-NLS-1$
        setNewDesktopCommand(new UICommand("NewDesktop", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setRunCommand(new UICommand("Run", this, true)); //$NON-NLS-1$
        setPauseCommand(new UICommand("Pause", this)); //$NON-NLS-1$
        setStopCommand(new UICommand("Stop", this)); //$NON-NLS-1$
        setShutdownCommand(new UICommand("Shutdown", this)); //$NON-NLS-1$
        setMigrateCommand(new UICommand("Migrate", this)); //$NON-NLS-1$
        setCancelMigrateCommand(new UICommand("CancelMigration", this)); //$NON-NLS-1$
        setNewTemplateCommand(new UICommand("NewTemplate", this)); //$NON-NLS-1$
        setRunOnceCommand(new UICommand("RunOnce", this)); //$NON-NLS-1$
        setExportCommand(new UICommand("Export", this)); //$NON-NLS-1$
        setCreateSnapshotCommand(new UICommand("CreateSnapshot", this)); //$NON-NLS-1$
        setMoveCommand(new UICommand("Move", this)); //$NON-NLS-1$
        setGuideCommand(new UICommand("Guide", this)); //$NON-NLS-1$
        setRetrieveIsoImagesCommand(new UICommand("RetrieveIsoImages", this)); //$NON-NLS-1$
        setChangeCdCommand(new UICommand("ChangeCD", this)); //$NON-NLS-1$
        setAssignTagsCommand(new UICommand("AssignTags", this)); //$NON-NLS-1$

        setIsoImages(new ObservableCollection<ChangeCDModel>());
        ChangeCDModel tempVar = new ChangeCDModel();
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().retrievingCDsTitle());
        getIsoImages().add(tempVar);

        UpdateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
        if (getCustomPropertiesKeysList() == null) {
            AsyncDataProvider.GetCustomPropertiesList(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            VmListModel model = (VmListModel) target;
                            if (returnValue != null)
                            {
                                model.setCustomPropertiesKeysList(new HashMap<Version, ArrayList<String>>());
                                HashMap<Version, String> dictionary = (HashMap<Version, String>) returnValue;
                                for (Map.Entry<Version, String> keyValuePair : dictionary.entrySet())
                                {
                                    model.getCustomPropertiesKeysList().put(keyValuePair.getKey(),
                                            new ArrayList<String>());
                                    for (String s : keyValuePair.getValue().split("[;]", -1)) //$NON-NLS-1$
                                    {
                                        model.getCustomPropertiesKeysList().get(keyValuePair.getKey()).add(s);
                                    }
                                }
                            }
                        }
                    }));
        }

        // Call 'IsCommandCompatible' for precaching
        AsyncDataProvider.IsCommandCompatible(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                    }
                }), null, null, null);
    }

    private void AssignTags()
    {
        if (getWindow() != null)
        {
            return;
        }

        TagListModel model = new TagListModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().assignTagsTitle());
        model.setHashName("assign_tags_vms"); //$NON-NLS-1$

        GetAttachedTagsToSelectedVMs(model);

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

    private void GetAttachedTagsToSelectedVMs(TagListModel model)
    {
        ArrayList<Guid> vmIds = new ArrayList<Guid>();
        for (Object item : getSelectedItems())
        {
            VM vm = (VM) item;
            vmIds.add(vm.getId());
        }

        attachedTagsToEntities = new HashMap<Guid, Boolean>();
        allAttachedTags = new ArrayList<org.ovirt.engine.core.common.businessentities.tags>();
        selectedItemsCounter = 0;

        for (Guid id : vmIds)
        {
            AsyncDataProvider.GetAttachedTagsToVm(new AsyncQuery(new Object[] { this, model },
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            Object[] array = (Object[]) target;
                            VmListModel vmListModel = (VmListModel) array[0];
                            TagListModel tagListModel = (TagListModel) array[1];
                            vmListModel.allAttachedTags.addAll((ArrayList<org.ovirt.engine.core.common.businessentities.tags>) returnValue);
                            vmListModel.selectedItemsCounter++;
                            if (vmListModel.selectedItemsCounter == vmListModel.getSelectedItems().size())
                            {
                                PostGetAttachedTags(vmListModel, tagListModel);
                            }

                        }
                    }),
                    id);
        }
    }

    private void PostGetAttachedTags(VmListModel vmListModel, TagListModel tagListModel)
    {
        if (vmListModel.getLastExecutedCommand() == getAssignTagsCommand())
        {
            ArrayList<org.ovirt.engine.core.common.businessentities.tags> attachedTags =
                    Linq.Distinct(vmListModel.allAttachedTags, new TagsEqualityComparer());
            for (org.ovirt.engine.core.common.businessentities.tags tag : attachedTags)
            {
                int count = 0;
                for (org.ovirt.engine.core.common.businessentities.tags tag2 : vmListModel.allAttachedTags)
                {
                    if (tag2.gettag_id().equals(tag.gettag_id()))
                    {
                        count++;
                    }
                }
                vmListModel.attachedTagsToEntities.put(tag.gettag_id(), count == vmListModel.getSelectedItems().size());
            }
            tagListModel.setAttachedTagsToEntities(vmListModel.attachedTagsToEntities);
        }
        else if (StringHelper.stringsEqual(vmListModel.getLastExecutedCommand().getName(), "OnAssignTags")) //$NON-NLS-1$
        {
            vmListModel.PostOnAssignTags(tagListModel.getAttachedTagsToEntities());
        }
    }

    private void OnAssignTags()
    {
        TagListModel model = (TagListModel) getWindow();

        GetAttachedTagsToSelectedVMs(model);
    }

    public void PostOnAssignTags(Map<Guid, Boolean> attachedTags)
    {
        TagListModel model = (TagListModel) getWindow();
        ArrayList<Guid> vmIds = new ArrayList<Guid>();

        for (Object item : getSelectedItems())
        {
            VM vm = (VM) item;
            vmIds.add(vm.getId());
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

        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
        for (Guid a : tagsToAttach)
        {
            parameters.add(new AttachEntityToTagParameters(a, vmIds));
        }
        Frontend.RunMultipleAction(VdcActionType.AttachVmsToTag, parameters);

        parameters = new ArrayList<VdcActionParametersBase>();
        for (Guid a : tagsToDetach)
        {
            parameters.add(new AttachEntityToTagParameters(a, vmIds));
        }
        Frontend.RunMultipleAction(VdcActionType.DetachVmFromTag, parameters);

        Cancel();
    }

    private void Guide()
    {
        VmGuideModel model = new VmGuideModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newVirtualMachineGuideMeTitle());
        model.setHashName("new_virtual_machine_-_guide_me"); //$NON-NLS-1$

        if (getGuideContext() == null) {
            VM vm = (VM) getSelectedItem();
            setGuideContext(vm.getId());
        }

        AsyncDataProvider.GetVmById(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmListModel vmListModel = (VmListModel) target;
                        VmGuideModel model = (VmGuideModel) vmListModel.getWindow();
                        model.setEntity(returnValue);

                        UICommand tempVar = new UICommand("Cancel", vmListModel); //$NON-NLS-1$
                        tempVar.setTitle(ConstantsManager.getInstance().getConstants().configureLaterTitle());
                        tempVar.setIsDefault(true);
                        tempVar.setIsCancel(true);
                        model.getCommands().add(tempVar);
                    }
                }), (Guid) getGuideContext());
    }

    @Override
    protected void InitDetailModels()
    {
        super.InitDetailModels();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new VmGeneralModel());
        list.add(new VmInterfaceListModel());
        VmDiskListModel diskListModel = new VmDiskListModel();
        diskListModel.setSystemTreeContext(this);
        list.add(diskListModel);
        list.add(new VmSnapshotListModel());
        list.add(new VmEventListModel());
        list.add(new VmAppListModel());
        list.add(new PermissionListModel());
        list.add(new VmSessionsModel());
        setDetailModels(list);
    }

    @Override
    public boolean IsSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("vm"); //$NON-NLS-1$
    }

    @Override
    protected void SyncSearch()
    {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.VM);
        tempVar.setMaxCount(getSearchPageSize());
        super.SyncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterSearch(getSearchString(), SearchType.VM, getSearchPageSize()));
        setItems(getAsyncResult().getData());
    }

    private void UpdateConsoleModels()
    {
        List tempVar = getSelectedItems();
        List selectedItems = (tempVar != null) ? tempVar : new ArrayList();
        Object tempVar2 = getSelectedItem();
        VM vm = (VM) ((tempVar2 instanceof VM) ? tempVar2 : null);

        if (vm == null || selectedItems.size() > 1)
        {
            setDefaultConsoleModel(null);
            setAdditionalConsoleModel(null);
            setHasAdditionalConsoleModel(false);
        }
        else
        {
            if (!cachedConsoleModels.containsKey(vm.getId()))
            {
                SpiceConsoleModel spiceConsoleModel = new SpiceConsoleModel();
                spiceConsoleModel.setParentModel(this);
                spiceConsoleModel.getErrorEvent().addListener(new ConsoleModelErrorEventListener(this));
                VncConsoleModel vncConsoleModel = new VncConsoleModel();
                vncConsoleModel.setParentModel(this);
                RdpConsoleModel rdpConsoleModel = new RdpConsoleModel();
                rdpConsoleModel.getErrorEvent().addListener(new ConsoleModelErrorEventListener(this));

                cachedConsoleModels.put(vm.getId(),
                        new ArrayList<ConsoleModel>(Arrays.asList(new ConsoleModel[] {
                                spiceConsoleModel, vncConsoleModel, rdpConsoleModel })));
            }

            ArrayList<ConsoleModel> cachedModels = cachedConsoleModels.get(vm.getId());
            for (ConsoleModel a : cachedModels)
            {
                a.setEntity(null);
                a.setEntity(vm);
            }

            setDefaultConsoleModel(vm.getDisplayType() == DisplayType.vnc ? cachedModels.get(1) : cachedModels.get(0));

            if (AsyncDataProvider.IsWindowsOsType(vm.getVmOs()))
            {
                for (ConsoleModel a : cachedModels)
                {
                    if (a instanceof RdpConsoleModel)
                    {
                        setAdditionalConsoleModel(a);
                        break;
                    }
                }
                setHasAdditionalConsoleModel(true);
            }
            else
            {
                setAdditionalConsoleModel(null);
                setHasAdditionalConsoleModel(false);
            }
        }
    }

    public ArrayList<ConsoleModel> GetConsoleModelsByVmGuid(Guid vmGuid)
    {
        if (cachedConsoleModels != null && cachedConsoleModels.containsKey(vmGuid))
        {
            return cachedConsoleModels.get(vmGuid);
        }

        return null;
    }

    private void NewDesktop()
    {
        NewInternal(VmType.Desktop);
    }

    private void NewServer()
    {
        NewInternal(VmType.Server);
    }

    private void NewInternal(VmType vmType)
    {
        if (getWindow() != null)
        {
            return;
        }

        UnitVmModel model = new UnitVmModel(new NewVmModelBehavior());
        model.setTitle(ConstantsManager.getInstance()
                .getMessages()
                .newVmTitle(vmType == VmType.Server ? ConstantsManager.getInstance().getConstants().serverVmType()
                        : ConstantsManager.getInstance().getConstants().desktopVmType()));
        model.setHashName("new_" + (vmType == VmType.Server ? "server" : "desktop")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        model.setIsNew(true);
        model.setVmType(vmType);
        model.setCustomPropertiesKeysList(getCustomPropertiesKeysList());

        setWindow(model);

        model.Initialize(getSystemTreeSelectedItem());

        // Ensures that the default provisioning is "Clone" for a new server and "Thin" for a new desktop.
        boolean selectValue = model.getVmType() == VmType.Server;
        model.getProvisioning().setEntity(selectValue);

        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void Edit()
    {
        VM vm = (VM) getSelectedItem();
        if (vm == null)
        {
            return;
        }

        if (getWindow() != null)
        {
            return;
        }

        UnitVmModel model = new UnitVmModel(new ExistingVmModelBehavior(vm));
        model.setVmType(vm.getVmType());
        model.setVmAttachedToPool(vm.getVmPoolId() != null);
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance()
                .getMessages()
                .editVmTitle(vm.getVmType() == VmType.Server ? ConstantsManager.getInstance()
                        .getConstants()
                        .serverVmType()
                        : ConstantsManager.getInstance().getConstants().desktopVmType()));
        model.setHashName("edit_" + (vm.getVmType() == VmType.Server ? "server" : "desktop")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        model.setCustomPropertiesKeysList(getCustomPropertiesKeysList());

        model.Initialize(this.getSystemTreeSelectedItem());

        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private Map<Guid, EntityModel> vmsRemoveMap;

    private void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel window = new ConfirmationModel();
        setWindow(window);
        window.setTitle(ConstantsManager.getInstance().getConstants().removeVirtualMachinesTitle());
        window.setHashName("remove_virtual_machine"); //$NON-NLS-1$
        window.setMessage(ConstantsManager.getInstance().getConstants().virtualMachinesMsg());

        vmsRemoveMap = new HashMap<Guid, EntityModel>();

        for (Object selectedItem : getSelectedItems())
        {
            VM vm = (VM) selectedItem;
            EntityModel removeDisksCheckbox = new EntityModel(true);
            removeDisksCheckbox.setTitle(ConstantsManager.getInstance().getConstants().removeDisksTitle());
            removeDisksCheckbox.setMessage(vm.getVmName());
            if (!NGuid.Empty.equals(vm.getVmtGuid())) {
                updateRemoveDisksCheckBox(removeDisksCheckbox, true, false, ConstantsManager.getInstance()
                        .getConstants()
                        .removeVmDisksTemplateMsg());
            }
            vmsRemoveMap.put(vm.getId(), removeDisksCheckbox);
        }
        window.setItems(vmsRemoveMap.entrySet());
        initRemoveDisksCheckboxes(vmsRemoveMap);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        window.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        window.getCommands().add(tempVar2);
    }

    private void updateRemoveDisksCheckBox(EntityModel model,
            boolean deleteDisks,
            boolean isChangable,
            String changeProhibitionReason) {

        model.setEntity(deleteDisks);
        if (!isChangable && changeProhibitionReason != null) {
            model.getChangeProhibitionReasons().add(changeProhibitionReason);
        }
        model.setIsChangable(isChangable);
    }

    private void initRemoveDisksCheckboxes(final Map<Guid, EntityModel> vmsMap) {
        ArrayList<VdcQueryParametersBase> params = new ArrayList<VdcQueryParametersBase>();
        ArrayList<VdcQueryType> queries = new ArrayList<VdcQueryType>();

        for (Entry<Guid, EntityModel> entry : vmsMap.entrySet()) {
            if (entry.getValue().getIsChangable()) { // No point in fetching VM disks from ones that already determined
                                                     // is unchangeable since they are already initialized
                params.add(new GetAllDisksByVmIdParameters(entry.getKey()));
                queries.add(VdcQueryType.GetAllDisksByVmId);
            }
        }

        // TODO: There's no point in creating a VdcQueryType list when you wanna run the same query for all parameters,
        // revise when refactoring org.ovirt.engine.ui.Frontend to support runMultipleQuery with a single query
        if (!params.isEmpty()) {
            Frontend.RunMultipleQueries(queries, params, new IFrontendMultipleQueryAsyncCallback() {
                @Override
                public void Executed(FrontendMultipleQueryAsyncResult result) {
                    for (int i = 0; i < result.getReturnValues().size(); i++) {
                        if (result.getReturnValues().get(i).getSucceeded()) {
                            Guid vmId = ((GetAllDisksByVmIdParameters) result.getParameters().get(i)).getVmId();
                            initRemoveDisksChecboxesPost(vmId, (List<Disk>) result.getReturnValues()
                                    .get(i)
                                    .getReturnValue());
                        }
                    }
                }
            });
        }
    }

    private void initRemoveDisksChecboxesPost(Guid vmId, List<Disk> disks) {
        EntityModel model = vmsRemoveMap.get(vmId);
        if (disks.isEmpty()) {
            updateRemoveDisksCheckBox(model, false, false, ConstantsManager.getInstance()
                    .getConstants()
                    .removeVmDisksNoDisksMsg());
            return;
        }

        boolean isOnlySharedDisks = true;
        boolean isSnapshotExists = false;
        for (Disk disk : disks) {
            if (!disk.isShareable()) {
                isOnlySharedDisks = false;
                if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    if (((DiskImage) disk).getSnapshots().size() > 1) {
                        isSnapshotExists = true;
                        break;
                    }
                }
            }
        }

        if (isSnapshotExists) {
            updateRemoveDisksCheckBox(model, true, false, ConstantsManager.getInstance()
                    .getConstants()
                    .removeVmDisksSnapshotsMsg());
            return;
        }

        if (isOnlySharedDisks) {
            updateRemoveDisksCheckBox(model, false, false, ConstantsManager.getInstance()
                    .getConstants()
                    .removeVmDisksAllSharedMsg());
            return;
        }
    }

    private void CreateSnapshot() {
        VM vm = (VM) getSelectedItem();
        if (vm == null || getWindow() != null) {
            return;
        }

        SnapshotModel model = new SnapshotModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().createSnapshotTitle());
        model.setHashName("create_snapshot"); //$NON-NLS-1$

        model.setVm(vm);
        model.setValidateByVmSnapshots(true);
        model.Initialize();

        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        UICommand closeCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        closeCommand.setTitle(ConstantsManager.getInstance().getConstants().close());
        closeCommand.setIsCancel(true);

        model.setCancelCommand(cancelCommand);
        model.setCloseCommand(closeCommand);
    }

    private void Move()
    {
        VM vm = (VM) getSelectedItem();
        if (vm == null)
        {
            return;
        }

        if (getWindow() != null)
        {
            return;
        }

        MoveDiskModel model = new MoveDiskModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().moveVirtualMachineTitle());
        model.setHashName("move_virtual_machine"); //$NON-NLS-1$
        model.setEntity(this);
        model.StartProgress(null);

        AsyncDataProvider.GetVmDiskList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                VmListModel vmListModel = (VmListModel) target;
                MoveDiskModel moveDiskModel = (MoveDiskModel) vmListModel.getWindow();
                LinkedList<DiskImage> disks = (LinkedList<DiskImage>) returnValue;
                ArrayList<DiskImage> diskImages = new ArrayList<DiskImage>();
                diskImages.addAll(disks);

                moveDiskModel.init(diskImages);
            }
        }), vm.getId(), true);
    }

    @Override
    protected String thereIsNoExportDomainBackupEntityAttachExportDomainToVmsDcMsg() {
        return ConstantsManager.getInstance()
                .getConstants()
                .thereIsNoExportDomainBackupVmAttachExportDomainToVmsDcMsg();
    }

    @Override
    protected VdcQueryType getEntityExportDomain() {
        return VdcQueryType.GetVmsFromExportDomain;
    }

    @Override
    protected String entityResideOnSeveralDCsMakeSureTheExportedVMResideOnSameDcMsg() {
        return ConstantsManager.getInstance()
                .getConstants()
                .vmsResideOnSeveralDCsMakeSureTheExportedVMResideOnSameDcMsg();
    }

    @Override
    protected boolean entitiesSelectedOnDifferentDataCenters()
    {
        ArrayList<VM> vms = new ArrayList<VM>();
        for (Object selectedItem : getSelectedItems())
        {
            VM a = (VM) selectedItem;
            vms.add(a);
        }

        Map<NGuid, ArrayList<VM>> t = new HashMap<NGuid, ArrayList<VM>>();
        for (VM a : vms)
        {
            if (!t.containsKey(a.getStoragePoolId()))
            {
                t.put(a.getStoragePoolId(), new ArrayList<VM>());
            }

            ArrayList<VM> list = t.get(a.getStoragePoolId());
            list.add(a);
        }

        return t.size() > 1;
    }

    @Override
    protected String extractNameFromEntity(VM entity) {
        return entity.getVmName();
    }

    @Override
    protected boolean entititesEqualsNullSafe(VM e1, VM e2) {
        return e1.getId().equals(e2.getId());
    }

    @Override
    protected String composeEntityOnStorage(String entities) {
        return ConstantsManager.getInstance()
                .getMessages()
                .vmsAlreadyExistOnTargetExportDomain(entities);
    }

    @Override
    protected Iterable<VM> asIterableReturnValue(Object returnValue) {
        return (List<VM>) returnValue;
    }

    private void GetTemplatesNotPresentOnExportDomain()
    {
        ExportVmModel model = (ExportVmModel) getWindow();
        Guid storageDomainId = ((StorageDomain) model.getStorage().getSelectedItem()).getId();

        AsyncDataProvider.GetDataCentersByStorageDomain(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmListModel vmListModel = (VmListModel) target;
                        ArrayList<storage_pool> storagePools =
                                (ArrayList<storage_pool>) returnValue;
                        storage_pool storagePool = storagePools.size() > 0 ? storagePools.get(0) : null;

                        vmListModel.PostGetTemplatesNotPresentOnExportDomain(storagePool);
                    }
                }), storageDomainId);
    }

    private void PostGetTemplatesNotPresentOnExportDomain(storage_pool storagePool)
    {
        ExportVmModel model = (ExportVmModel) getWindow();
        Guid storageDomainId = ((StorageDomain) model.getStorage().getSelectedItem()).getId();

        if (storagePool != null)
        {
            AsyncDataProvider.GetAllTemplatesFromExportDomain(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {
                            VmListModel vmListModel = (VmListModel) target;
                            HashMap<VmTemplate, ArrayList<DiskImage>> templatesDiskSet =
                                    (HashMap<VmTemplate, ArrayList<DiskImage>>) returnValue;
                            HashMap<String, ArrayList<String>> templateDic =
                                    new HashMap<String, ArrayList<String>>();

                            // check if relevant templates are already there
                            for (Object selectedItem : vmListModel.getSelectedItems())
                            {
                                VM vm = (VM) selectedItem;
                                boolean hasMatch = false;
                                for (VmTemplate a : templatesDiskSet.keySet())
                                {
                                    if (vm.getVmtGuid().equals(a.getId()))
                                    {
                                        hasMatch = true;
                                        break;
                                    }
                                }

                                if (!vm.getVmtGuid().equals(NGuid.Empty) && !hasMatch)
                                {
                                    if (!templateDic.containsKey(vm.getVmtName()))
                                    {
                                        templateDic.put(vm.getVmtName(), new ArrayList<String>());
                                    }
                                    templateDic.get(vm.getVmtName()).add(vm.getVmName());
                                }
                            }

                            String tempStr;
                            ArrayList<String> tempList;
                            ArrayList<String> missingTemplates = new ArrayList<String>();
                            for (Map.Entry<String, ArrayList<String>> keyValuePair : templateDic.entrySet())
                            {
                                tempList = keyValuePair.getValue();
                                tempStr = "Template " + keyValuePair.getKey() + " (for "; //$NON-NLS-1$ //$NON-NLS-2$
                                int i;
                                for (i = 0; i < tempList.size() - 1; i++)
                                {
                                    tempStr += tempList.get(i) + ", "; //$NON-NLS-1$
                                }
                                tempStr += tempList.get(i) + ")"; //$NON-NLS-1$
                                missingTemplates.add(tempStr);
                            }

                            vmListModel.PostExportGetMissingTemplates(missingTemplates);
                        }
                    }),
                    storagePool.getId(),
                    storageDomainId);
        }
    }

    private void PostExportGetMissingTemplates(ArrayList<String> missingTemplatesFromVms)
    {
        ExportVmModel model = (ExportVmModel) getWindow();
        Guid storageDomainId = ((StorageDomain) model.getStorage().getSelectedItem()).getId();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();

        model.StopProgress();

        for (Object a : getSelectedItems())
        {
            VM vm = (VM) a;
            MoveVmParameters parameter = new MoveVmParameters(vm.getId(), storageDomainId);
            parameter.setForceOverride((Boolean) model.getForceOverride().getEntity());
            parameter.setCopyCollapse((Boolean) model.getCollapseSnapshots().getEntity());
            parameter.setTemplateMustExists(true);

            parameters.add(parameter);
        }

        if (!(Boolean) model.getCollapseSnapshots().getEntity())
        {
            if ((missingTemplatesFromVms == null || missingTemplatesFromVms.size() > 0))
            {
                ConfirmationModel confirmModel = new ConfirmationModel();
                setConfirmWindow(confirmModel);
                confirmModel.setTitle(ConstantsManager.getInstance()
                        .getConstants()
                        .templatesNotFoundOnExportDomainTitle());
                confirmModel.setHashName("template_not_found_on_export_domain"); //$NON-NLS-1$

                confirmModel.setMessage(missingTemplatesFromVms == null ? ConstantsManager.getInstance()
                        .getConstants()
                        .couldNotReadTemplatesFromExportDomainMsg()
                        : ConstantsManager.getInstance()
                                .getConstants()
                                .theFollowingTemplatesAreMissingOnTargetExportDomainMsg());
                confirmModel.setItems(missingTemplatesFromVms);

                UICommand tempVar = new UICommand("OnExportNoTemplates", this); //$NON-NLS-1$
                tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
                tempVar.setIsDefault(true);
                confirmModel.getCommands().add(tempVar);
                UICommand tempVar2 = new UICommand("CancelConfirmation", this); //$NON-NLS-1$
                tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                tempVar2.setIsCancel(true);
                confirmModel.getCommands().add(tempVar2);
            }
            else
            {
                if (model.getProgress() != null)
                {
                    return;
                }

                model.StartProgress(null);

                Frontend.RunMultipleAction(VdcActionType.ExportVm, parameters,
                        new IFrontendMultipleActionAsyncCallback() {
                            @Override
                            public void Executed(FrontendMultipleActionAsyncResult result) {
                                ExportVmModel localModel = (ExportVmModel) result.getState();
                                localModel.StopProgress();
                                Cancel();
                            }
                        }, model);
            }
        }
        else
        {
            if (model.getProgress() != null)
            {
                return;
            }

            for (VdcActionParametersBase item : parameters)
            {
                MoveVmParameters parameter = (MoveVmParameters) item;
                parameter.setTemplateMustExists(false);
            }

            model.StartProgress(null);

            Frontend.RunMultipleAction(VdcActionType.ExportVm, parameters,
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendMultipleActionAsyncResult result) {
                            ExportVmModel localModel = (ExportVmModel) result.getState();
                            localModel.StopProgress();
                            Cancel();
                        }
                    }, model);
        }
    }

    public void OnExport()
    {
        ExportVmModel model = (ExportVmModel) getWindow();
        Guid storageDomainId = ((StorageDomain) model.getStorage().getSelectedItem()).getId();
        if (!model.Validate())
        {
            return;
        }

        model.StartProgress(null);

        GetTemplatesNotPresentOnExportDomain();
    }

    private void OnExportNoTemplates()
    {
        ExportVmModel model = (ExportVmModel) getWindow();
        Guid storageDomainId = ((StorageDomain) model.getStorage().getSelectedItem()).getId();

        if (model.getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VM a = (VM) item;
            MoveVmParameters parameters = new MoveVmParameters(a.getId(), storageDomainId);
            parameters.setForceOverride((Boolean) model.getForceOverride().getEntity());
            parameters.setCopyCollapse((Boolean) model.getCollapseSnapshots().getEntity());
            parameters.setTemplateMustExists(false);

            list.add(parameters);
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.ExportVm, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ExportVmModel localModel = (ExportVmModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    @Override
    protected void sendWarningForNonExportableDisks(VM entity) {
        // load VM disks and check if there is one which doesn't allow snapshot
        AsyncDataProvider.GetVmDiskList(new AsyncQuery((ExportVmModel) getWindow(),
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        final ExportVmModel model = (ExportVmModel) target;
                        @SuppressWarnings("unchecked")
                        final ArrayList<Disk> diskList = (ArrayList<Disk>) returnValue;
                        // filter non-exportable disks
                        final List<String> list = new ArrayList<String>();
                        for(Disk disk : diskList) {
                            if (!disk.isAllowSnapshot()) {
                                list.add(disk.getDiskAlias());
                            }
                        }

                        if (!list.isEmpty()) {
                            final String s = StringUtils.join(list, ", "); //$NON-NLS-1$
                            // append warning message
                            model.setMessage(ConstantsManager.getInstance().getMessages().disksWillNotBePartOfTheExportedVM(s));
                        }
                    }
                }), entity.getId());
    }

    private void RunOnce()
    {
        VM vm = (VM) getSelectedItem();
        RunOnceModel model = new RunOnceModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().runVirtualMachinesTitle());
        model.setHashName("run_virtual_machine"); //$NON-NLS-1$
        model.getAttachIso().setEntity(false);
        model.getAttachFloppy().setEntity(false);
        model.getRunAsStateless().setEntity(vm.isStateless());
        model.getRunAndPause().setEntity(false);
        model.setHwAcceleration(true);

        // passing Kernel parameters
        model.getKernel_parameters().setEntity(vm.getKernelParams());
        model.getKernel_path().setEntity(vm.getKernelUrl());
        model.getInitrd_path().setEntity(vm.getInitrdUrl());

        // Custom Properties
        model.getCustomPropertySheet()
                .setKeyValueString(this.getCustomPropertiesKeysList()
                        .get(vm.getVdsGroupCompatibilityVersion()));
        model.getCustomPropertySheet().setEntity(vm.getCustomProperties());
        model.setCustomPropertiesKeysList(this.getCustomPropertiesKeysList()
                .get(vm.getVdsGroupCompatibilityVersion()));

        model.setIsLinux_Unassign_UnknownOS(AsyncDataProvider.IsLinuxOsType(vm.getVmOs())
                || vm.getVmOs() == VmOsType.Unassigned || vm.getVmOs() == VmOsType.Other);
        model.getIsLinuxOptionsAvailable().setEntity(model.getIsLinux_Unassign_UnknownOS());
        model.setIsWindowsOS(AsyncDataProvider.IsWindowsOsType(vm.getVmOs()));
        model.getIsVmFirstRun().setEntity(!vm.isInitialized());
        model.getSysPrepDomainName().setSelectedItem(vm.getVmDomain());

        RunOnceUpdateDisplayProtocols(vm);
        RunOnceUpdateFloppy(vm, new ArrayList<String>());
        RunOnceUpdateImages(vm);
        RunOnceUpdateDomains();
        RunOnceUpdateBootSequence(vm);
        RunOnceLoadHosts(vm);

        UICommand tempVar = new UICommand("OnRunOnce", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);

        model.getIsAutoAssign().setEntity(true);
    }

    private void RunOnceUpdateDisplayProtocols(VM vm)
    {
        RunOnceModel model = (RunOnceModel) getWindow();

        EntityModel tempVar = new EntityModel();
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().VNCTitle());
        tempVar.setEntity(DisplayType.vnc);
        EntityModel vncProtocol = tempVar;

        EntityModel tempVar2 = new EntityModel();
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().spiceTitle());
        tempVar2.setEntity(DisplayType.qxl);
        EntityModel qxlProtocol = tempVar2;

        boolean isVncSelected = vm.getDefaultDisplayType() == DisplayType.vnc;
        model.getDisplayConsole_Vnc_IsSelected().setEntity(isVncSelected);
        model.getDisplayConsole_Spice_IsSelected().setEntity(!isVncSelected);

        ArrayList<EntityModel> items = new ArrayList<EntityModel>();
        items.add(vncProtocol);
        items.add(qxlProtocol);
        model.getDisplayProtocol().setItems(items);
        model.getDisplayProtocol().setSelectedItem(isVncSelected ? vncProtocol : qxlProtocol);
    }

    private void RunOnceUpdateBootSequence(VM vm)
    {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);

        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                VmListModel vmListModel = (VmListModel) model;
                RunOnceModel runOnceModel = (RunOnceModel) vmListModel.getWindow();
                boolean hasNics =
                        ((ArrayList<VmNetworkInterface>) ((VdcQueryReturnValue) ReturnValue).getReturnValue()).size() > 0;

                if (!hasNics)
                {
                    BootSequenceModel bootSequenceModel = runOnceModel.getBootSequence();
                    bootSequenceModel.getNetworkOption().setIsChangable(false);
                    bootSequenceModel.getNetworkOption()
                            .getChangeProhibitionReasons()
                            .add("Virtual Machine must have at least one network interface defined to boot from network."); //$NON-NLS-1$
                }
            }
        };

        Frontend.RunQuery(VdcQueryType.GetVmInterfacesByVmId, new IdQueryParameters(vm.getId()), _asyncQuery);

        setIsBootFromHardDiskAllowedForVm(vm);
    }

    private void setIsBootFromHardDiskAllowedForVm(VM vm) {
        AsyncQuery vmDisksQuery = new AsyncQuery();
        vmDisksQuery.setModel(this);

        vmDisksQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object returnValue)
            {
                VmListModel userPortalListModel = (VmListModel) model;
                ArrayList<Disk> vmDisks = (ArrayList<Disk>) ((VdcQueryReturnValue) returnValue).getReturnValue();

                boolean hasBootableDisk = false;
                for (Disk disk : vmDisks) {
                    if (disk.isBoot()) {
                        hasBootableDisk = true;
                        break;
                    }
                }

                if (!hasBootableDisk)
                {
                    BootSequenceModel bootSequenceModel =
                            ((RunOnceModel) userPortalListModel.getWindow()).getBootSequence();
                    bootSequenceModel.getHardDiskOption().setIsChangable(false);
                    bootSequenceModel.getHardDiskOption()
                            .getChangeProhibitionReasons()
                            .add("Virtual Machine must have at least one bootable disk defined to boot from hard disk."); //$NON-NLS-1$
                }
            }
        };

        Frontend.RunQuery(VdcQueryType.GetAllDisksByVmId, new GetAllDisksByVmIdParameters(vm.getId()), vmDisksQuery);
    }

    private void RunOnceUpdateDomains()
    {
        RunOnceModel model = (RunOnceModel) getWindow();

        // Update Domain list
        AsyncDataProvider.GetDomainList(new AsyncQuery(model,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        RunOnceModel runOnceModel = (RunOnceModel) target;
                        List<String> domains = (List<String>) returnValue;
                        String oldDomain = (String) runOnceModel.getSysPrepDomainName().getSelectedItem();
                        if (oldDomain != null && !oldDomain.equals("") && !domains.contains(oldDomain)) //$NON-NLS-1$
                        {
                            domains.add(0, oldDomain);
                        }
                        runOnceModel.getSysPrepDomainName().setItems(domains);
                        String selectedDomain = (oldDomain != null) ? oldDomain : Linq.FirstOrDefault(domains);
                        if (!StringHelper.stringsEqual(selectedDomain, "")) //$NON-NLS-1$
                        {
                            runOnceModel.getSysPrepDomainName().setSelectedItem(selectedDomain);
                        }

                    }
                }), true);
    }

    public void RunOnceUpdateFloppy(VM vm, ArrayList<String> images)
    {
        RunOnceModel model = (RunOnceModel) getWindow();

        if (AsyncDataProvider.IsWindowsOsType(vm.getVmOs()))
        {
            // Add a pseudo floppy disk image used for Windows' sysprep.
            if (!vm.isInitialized())
            {
                images.add(0, "[sysprep]"); //$NON-NLS-1$
                model.getAttachFloppy().setEntity(true);
            }
            else
            {
                images.add("[sysprep]"); //$NON-NLS-1$
            }
        }

        model.getFloppyImage().setItems(images);

        if (model.getFloppyImage().getIsChangable() && model.getFloppyImage().getSelectedItem() == null)
        {
            model.getFloppyImage().setSelectedItem(Linq.FirstOrDefault(images));
        }
    }

    private void RunOnceUpdateImages(VM vm) {

        AsyncQuery _asyncQuery2 = new AsyncQuery();
        _asyncQuery2.setModel(this);

        _asyncQuery2.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model2, Object result)
            {
                VmListModel vmListModel2 = (VmListModel) model2;
                VM selectedVM = (VM) vmListModel2.getSelectedItem();
                ArrayList<String> images = (ArrayList<String>) result;

                vmListModel2.RunOnceUpdateFloppy(selectedVM, images);
            }
        };
        AsyncDataProvider.GetFloppyImageList(_asyncQuery2, vm.getStoragePoolId());

        AsyncQuery getImageListQuery = new AsyncQuery();
        getImageListQuery.setModel(this);

        getImageListQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model1, Object result)
            {
                VmListModel vmListModel1 = (VmListModel) model1;
                RunOnceModel runOnceModel = (RunOnceModel) vmListModel1.getWindow();
                ArrayList<String> images = (ArrayList<String>) result;

                runOnceModel.getIsoImage().setItems(images);
                if (runOnceModel.getIsoImage().getIsChangable()
                        && runOnceModel.getIsoImage().getSelectedItem() == null)
                {
                    runOnceModel.getIsoImage().setSelectedItem(Linq.FirstOrDefault(images));
                }
            }
        };
        AsyncDataProvider.GetIrsImageList(getImageListQuery, vm.getStoragePoolId());

    }

    /**
     * Load active hosts bound to active cluster.
     *
     */
    private void RunOnceLoadHosts(VM vm) {
        RunOnceModel model = (RunOnceModel) getWindow();

        // append just active hosts
        AsyncDataProvider.GetHostListByCluster(new AsyncQuery(model,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        final RunOnceModel model = (RunOnceModel) target;
                        final List<VDS> hosts = (ArrayList<VDS>) returnValue;
                        final List<VDS> activeHosts = new ArrayList<VDS>();
                        for(VDS host : hosts) {
                            if(VDSStatus.Up.equals(host.getStatus())) {
                                activeHosts.add(host);
                            }
                        }

                        model.getDefaultHost().setItems(activeHosts);

                        // hide host tab when no active host is available
                        if(activeHosts.isEmpty()) {
                            model.setIsHostTabVisible(false);
                        }
                    }
                }), vm.getVdsGroupName());
    }

    private void OnRunOnce()
    {
        VM vm = (VM) getSelectedItem();
        if (vm == null)
        {
            Cancel();
            return;
        }

        RunOnceModel model = (RunOnceModel) getWindow();

        if (!model.Validate())
        {
            return;
        }

        BootSequenceModel bootSequenceModel = model.getBootSequence();

        RunVmOnceParams tempVar = new RunVmOnceParams();
        tempVar.setVmId(vm.getId());
        tempVar.setBootSequence(bootSequenceModel.getSequence());
        tempVar.setDiskPath((Boolean) model.getAttachIso().getEntity() ? (String) model.getIsoImage().getSelectedItem()
                : ""); //$NON-NLS-1$
        tempVar.setFloppyPath(model.getFloppyImagePath());
        tempVar.setKvmEnable(model.getHwAcceleration());
        tempVar.setRunAndPause((Boolean) model.getRunAndPause().getEntity());
        tempVar.setAcpiEnable(true);
        tempVar.setRunAsStateless((Boolean) model.getRunAsStateless().getEntity());
        tempVar.setReinitialize(model.getReinitialize());
        tempVar.setCustomProperties(model.getCustomPropertySheet().getEntity());

        // set destination host if specified
        VDS defaultHost = (VDS) model.getDefaultHost().getSelectedItem();

        if ((Boolean) model.getIsAutoAssign().getEntity()) {
            tempVar.setDestinationVdsId(null);
        } else {
            tempVar.setDestinationVdsId(defaultHost != null ? defaultHost.getId() : null);
        }

        RunVmOnceParams param = tempVar;

        // kernel params
        if (model.getKernel_path().getEntity() != null)
        {
            param.setkernel_url((String) model.getKernel_path().getEntity());
        }
        if (model.getKernel_parameters().getEntity() != null)
        {
            param.setkernel_params((String) model.getKernel_parameters().getEntity());
        }
        if (model.getInitrd_path().getEntity() != null)
        {
            param.setinitrd_url((String) model.getInitrd_path().getEntity());
        }

        // Sysprep params
        if (model.getSysPrepDomainName().getSelectedItem() != null)
        {
            param.setSysPrepDomainName(model.getSysPrepSelectedDomainName().getEntity().equals("") ? (String) model.getSysPrepSelectedDomainName() //$NON-NLS-1$
                    .getEntity()
                    : (String) model.getSysPrepDomainName().getSelectedItem());
        }
        if (model.getSysPrepUserName().getEntity() != null)
        {
            param.setSysPrepUserName((String) model.getSysPrepUserName().getEntity());
        }
        if (model.getSysPrepPassword().getEntity() != null)
        {
            param.setSysPrepPassword((String) model.getSysPrepPassword().getEntity());
        }

        EntityModel displayProtocolSelectedItem = (EntityModel) model.getDisplayProtocol().getSelectedItem();
        param.setUseVnc((DisplayType) displayProtocolSelectedItem.getEntity() == DisplayType.vnc);
        if ((Boolean) model.getDisplayConsole_Vnc_IsSelected().getEntity()
                || (Boolean) model.getDisplayConsole_Spice_IsSelected().getEntity())
        {
            param.setUseVnc((Boolean) model.getDisplayConsole_Vnc_IsSelected().getEntity());
        }

        Frontend.RunAction(VdcActionType.RunVmOnce, param, null, this);

        Cancel();
    }

    private void NewTemplate()
    {
        VM vm = (VM) getSelectedItem();
        if (vm == null)
        {
            return;
        }

        if (getWindow() != null)
        {
            return;
        }

        UnitVmModel model = new UnitVmModel(new NewTemplateVmModelBehavior(vm));
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newTemplateTitle());
        model.setHashName("new_template"); //$NON-NLS-1$
        model.setIsNew(true);
        model.setVmType(vm.getVmType());

        model.Initialize(getSystemTreeSelectedItem());

        UICommand tempVar = new UICommand("OnNewTemplate", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);

        model.getIsHighlyAvailable().setEntity(vm.getStaticData().isAutoStartup());
    }

    private void OnNewTemplate()
    {
        UnitVmModel model = (UnitVmModel) getWindow();
        VM vm = (VM) getSelectedItem();
        if (vm == null)
        {
            Cancel();
            return;
        }

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.Validate())
        {
            model.setIsValid(false);
        }
        else
        {
            String name = (String) model.getName().getEntity();

            // Check name unicitate.
            AsyncDataProvider.IsTemplateNameUnique(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            VmListModel vmListModel = (VmListModel) target;
                            boolean isNameUnique = (Boolean) returnValue;
                            if (!isNameUnique)
                            {
                                UnitVmModel VmModel = (UnitVmModel) vmListModel.getWindow();
                                VmModel.getInvalidityReasons().clear();
                                VmModel.getName()
                                        .getInvalidityReasons()
                                        .add(ConstantsManager.getInstance()
                                                .getConstants()
                                                .nameMustBeUniqueInvalidReason());
                                VmModel.getName().setIsValid(false);
                                VmModel.setIsValid(false);
                            }
                            else
                            {
                                vmListModel.PostNameUniqueCheck();
                            }

                        }
                    }),
                    name);
        }
    }

    public void PostNameUniqueCheck()
    {
        UnitVmModel model = (UnitVmModel) getWindow();
        VM vm = (VM) getSelectedItem();

        VM tempVar = new VM();
        tempVar.setId(vm.getId());
        tempVar.setVmType(model.getVmType());
        if (model.getQuota().getSelectedItem() != null) {
            tempVar.setQuotaId(((Quota) model.getQuota().getSelectedItem()).getId());
        }
        tempVar.setVmOs((VmOsType) model.getOSType().getSelectedItem());
        tempVar.setNumOfMonitors((Integer) model.getNumOfMonitors().getSelectedItem());
        tempVar.setAllowConsoleReconnect((Boolean) model.getAllowConsoleReconnect().getEntity());
        tempVar.setVmDomain(model.getDomain().getIsAvailable() ? (String) model.getDomain().getSelectedItem() : ""); //$NON-NLS-1$
        tempVar.setVmMemSizeMb((Integer) model.getMemSize().getEntity());
        tempVar.setMinAllocatedMem((Integer) model.getMinAllocatedMemory().getEntity());
        tempVar.setVdsGroupId(((VDSGroup) model.getCluster().getSelectedItem()).getId());
        tempVar.setTimeZone(model.getTimeZone().getIsAvailable() && model.getTimeZone().getSelectedItem() != null ? ((Map.Entry<String, String>) model.getTimeZone()
                .getSelectedItem()).getKey()
                : ""); //$NON-NLS-1$
        tempVar.setNumOfSockets((Integer) model.getNumOfSockets().getSelectedItem());
        tempVar.setCpuPerSocket(Integer.parseInt(model.getTotalCPUCores().getEntity().toString())
                / (Integer) model.getNumOfSockets().getSelectedItem());
        tempVar.setAutoSuspend(false);
        tempVar.setStateless((Boolean) model.getIsStateless().getEntity());
        tempVar.setSmartcardEnabled((Boolean) model.getIsSmartcardEnabled().getEntity());
        tempVar.setDeleteProtected((Boolean) model.getIsDeleteProtected().getEntity());
        tempVar.setDefaultBootSequence(model.getBootSequence());
        tempVar.setAutoStartup((Boolean) model.getIsHighlyAvailable().getEntity());
        tempVar.setIsoPath(model.getCdImage().getIsChangable() ? (String) model.getCdImage().getSelectedItem() : ""); //$NON-NLS-1$
        tempVar.setUsbPolicy(vm.getUsbPolicy());
        tempVar.setInitrdUrl(vm.getInitrdUrl());
        tempVar.setKernelUrl(vm.getKernelUrl());
        tempVar.setKernelParams(vm.getKernelParams());
        tempVar.setDedicatedVmForVds(vm.getDedicatedVmForVds());
        tempVar.setMigrationSupport(vm.getMigrationSupport());

        VM newvm = tempVar;

        EntityModel displayProtocolSelectedItem = (EntityModel) model.getDisplayProtocol().getSelectedItem();
        newvm.setDefaultDisplayType((DisplayType) displayProtocolSelectedItem.getEntity());

        EntityModel prioritySelectedItem = (EntityModel) model.getPriority().getSelectedItem();
        newvm.setPriority((Integer) prioritySelectedItem.getEntity());

        AddVmTemplateParameters addVmTemplateParameters =
                new AddVmTemplateParameters(newvm,
                        (String) model.getName().getEntity(),
                        (String) model.getDescription().getEntity());
        addVmTemplateParameters.setPublicUse((Boolean) model.getIsTemplatePublic().getEntity());

        addVmTemplateParameters.setDiskInfoDestinationMap(
                model.getDisksAllocationModel().getImageToDestinationDomainMap());

        model.StartProgress(null);

        Frontend.RunAction(VdcActionType.AddVmTemplate, addVmTemplateParameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        VmListModel vmListModel = (VmListModel) result.getState();
                        vmListModel.getWindow().StopProgress();
                        VdcReturnValueBase returnValueBase = result.getReturnValue();
                        if (returnValueBase != null && returnValueBase.getSucceeded())
                        {
                            vmListModel.Cancel();
                        }

                    }
                }, this);
    }

    private void Migrate()
    {
        VM vm = (VM) getSelectedItem();
        if (vm == null)
        {
            return;
        }

        if (getWindow() != null)
        {
            return;
        }

        MigrateModel model = new MigrateModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().migrateVirtualMachinesTitle());
        model.setHashName("migrate_virtual_machine"); //$NON-NLS-1$
        model.setVmsOnSameCluster(true);
        model.setIsAutoSelect(true);
        model.setVmList(Linq.<VM> Cast(getSelectedItems()));

        AsyncDataProvider.GetUpHostListByCluster(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmListModel vmListModel = (VmListModel) target;
                        vmListModel.PostMigrateGetUpHosts((ArrayList<VDS>) returnValue);
                    }
                }), vm.getVdsGroupName());
    }

    private void CancelMigration()
    {
        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems()) {
            VM a = (VM) item;
            list.add(new VmOperationParameterBase(a.getId()));
        }

        Frontend.RunMultipleAction(VdcActionType.CancelMigrateVm, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(
                            FrontendMultipleActionAsyncResult result) {
                    }
                }, null);
    }

    private void PostMigrateGetUpHosts(ArrayList<VDS> hosts)
    {
        MigrateModel model = (MigrateModel) getWindow();
        NGuid run_on_vds = null;
        boolean allRunOnSameVds = true;

        for (Object item : getSelectedItems())
        {
            VM a = (VM) item;
            if (!a.getVdsGroupId().equals(((VM) getSelectedItems().get(0)).getVdsGroupId()))
            {
                model.setVmsOnSameCluster(false);
            }
            if (run_on_vds == null)
            {
                run_on_vds = a.getRunOnVds().getValue();
            }
            else if (allRunOnSameVds && !run_on_vds.equals(a.getRunOnVds().getValue()))
            {
                allRunOnSameVds = false;
            }
        }

        model.setIsHostSelAvailable(model.getVmsOnSameCluster() && hosts.size() > 0);

        if (model.getVmsOnSameCluster() && allRunOnSameVds)
        {
            VDS runOnSameVDS = null;
            for (VDS host : hosts)
            {
                if (host.getId().equals(run_on_vds))
                {
                    runOnSameVDS = host;
                }
            }
            hosts.remove(runOnSameVDS);
        }
        if (hosts.isEmpty())
        {
            model.setIsHostSelAvailable(false);

            if (allRunOnSameVds)
            {
                model.setNoSelAvailable(true);

                UICommand tempVar = new UICommand("Cancel", this); //$NON-NLS-1$
                tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
                tempVar.setIsDefault(true);
                tempVar.setIsCancel(true);
                model.getCommands().add(tempVar);
            }
        }
        else
        {
            model.getHosts().setItems(hosts);
            model.getHosts().setSelectedItem(Linq.FirstOrDefault(hosts));

            UICommand tempVar2 = new UICommand("OnMigrate", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar2.setIsDefault(true);
            model.getCommands().add(tempVar2);
            UICommand tempVar3 = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            tempVar3.setIsCancel(true);
            model.getCommands().add(tempVar3);
        }
    }

    private void OnMigrate()
    {
        MigrateModel model = (MigrateModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        model.StartProgress(null);

        if (model.getIsAutoSelect())
        {
            ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
            for (Object item : getSelectedItems())
            {
                VM a = (VM) item;
                list.add(new MigrateVmParameters(true, a.getId()));
            }

            Frontend.RunMultipleAction(VdcActionType.MigrateVm, list,
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendMultipleActionAsyncResult result) {

                            MigrateModel localModel = (MigrateModel) result.getState();
                            localModel.StopProgress();
                            Cancel();

                        }
                    }, model);
        }
        else
        {
            ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
            for (Object item : getSelectedItems())
            {
                VM a = (VM) item;

                if (a.getRunOnVds().getValue().equals(((VDS) model.getHosts().getSelectedItem()).getId()))
                {
                    continue;
                }

                list.add(new MigrateVmToServerParameters(true, a.getId(), ((VDS) model.getHosts()
                        .getSelectedItem()).getId()));
            }

            Frontend.RunMultipleAction(VdcActionType.MigrateVmToServer, list,
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendMultipleActionAsyncResult result) {

                            MigrateModel localModel = (MigrateModel) result.getState();
                            localModel.StopProgress();
                            Cancel();

                        }
                    }, model);
        }
    }

    private void Shutdown()
    {
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().shutdownVirtualMachinesTitle());
        model.setHashName("shut_down_virtual_machine"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance()
                .getConstants()
                .areYouSureYouWantToShutDownTheFollowingVirtualMachinesMsg());
        // model.Items = SelectedItems.Cast<VM>().Select(a => a.vm_name);
        ArrayList<String> items = new ArrayList<String>();
        for (Object item : getSelectedItems())
        {
            VM a = (VM) item;
            items.add(a.getVmName());
        }
        model.setItems(items);

        UICommand tempVar = new UICommand("OnShutdown", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void OnShutdown()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VM a = (VM) item;
            list.add(new ShutdownVmParameters(a.getId(), true));
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.ShutdownVm, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    private void stop()
    {
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().stopVirtualMachinesTitle());
        model.setHashName("stop_virtual_machine"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance()
                .getConstants()
                .areYouSureYouWantToStopTheFollowingVirtualMachinesMsg());
        // model.Items = SelectedItems.Cast<VM>().Select(a => a.vm_name);
        ArrayList<String> items = new ArrayList<String>();
        for (Object item : getSelectedItems())
        {
            VM a = (VM) item;
            items.add(a.getVmName());
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

    private void OnStop()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VM a = (VM) item;
            list.add(new StopVmParameters(a.getId(), StopVmTypeEnum.NORMAL));
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.StopVm, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    private void Pause()
    {
        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VM a = (VM) item;
            list.add(new HibernateVmParameters(a.getId()));
        }

        Frontend.RunMultipleAction(VdcActionType.HibernateVm, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                    }
                }, null);
    }

    private void Run()
    {
        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VM a = (VM) item;
            // use sysprep iff the vm is not initialized and vm has Win OS
            boolean reinitialize = !a.isInitialized() && AsyncDataProvider.IsWindowsOsType(a.getVmOs());
            RunVmParams tempVar = new RunVmParams(a.getId());
            tempVar.setReinitialize(reinitialize);
            list.add(tempVar);
        }

        Frontend.RunMultipleAction(VdcActionType.RunVm, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                    }
                }, null);
    }

    private void OnRemove()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Entry<Guid, EntityModel> entry : vmsRemoveMap.entrySet())
        {
            list.add(new RemoveVmParameters(entry.getKey(), false, (Boolean) entry.getValue().getEntity()));
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveVm, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    private void ChangeCD()
    {
        VM vm = (VM) getSelectedItem();
        if (vm == null)
        {
            return;
        }

        AttachCdModel model = new AttachCdModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().changeCDTitle());
        model.setHashName("change_cd"); //$NON-NLS-1$

        AttachCdModel attachCdModel = (AttachCdModel) getWindow();
        ArrayList<String> images1 =
                new ArrayList<String>(Arrays.asList(new String[] { "No CDs" })); //$NON-NLS-1$
        attachCdModel.getIsoImage().setItems(images1);
        attachCdModel.getIsoImage().setSelectedItem(Linq.FirstOrDefault(images1));

        AsyncQuery getIrsImageListCallback = new AsyncQuery();
        getIrsImageListCallback.setModel(this);

        getIrsImageListCallback.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                VmListModel vmListModel2 = (VmListModel) model;
                AttachCdModel _attachCdModel = (AttachCdModel) vmListModel2.getWindow();
                ArrayList<String> images = (ArrayList<String>) result;
                if (images.size() > 0)
                {
                    images.add(0, ConsoleModel.EjectLabel);
                    _attachCdModel.getIsoImage().setItems(images);
                }
                if (_attachCdModel.getIsoImage().getIsChangable())
                {
                    _attachCdModel.getIsoImage().setSelectedItem(Linq.FirstOrDefault(images));
                }
            }
        };
        AsyncDataProvider.GetIrsImageList(getIrsImageListCallback, vm.getStoragePoolId());

        UICommand tempVar = new UICommand("OnChangeCD", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void OnChangeCD()
    {
        VM vm = (VM) getSelectedItem();
        if (vm == null)
        {
            Cancel();
            return;
        }

        AttachCdModel model = (AttachCdModel) getWindow();
        if (model.getProgress() != null)
        {
            return;
        }

        String isoName =
                (StringHelper.stringsEqual(model.getIsoImage().getSelectedItem().toString(), ConsoleModel.EjectLabel)) ? "" //$NON-NLS-1$
                        : model.getIsoImage().getSelectedItem().toString();

        model.StartProgress(null);

        Frontend.RunAction(VdcActionType.ChangeDisk, new ChangeDiskCommandParameters(vm.getId(), isoName),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        AttachCdModel attachCdModel = (AttachCdModel) result.getState();
                        attachCdModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    private void preSave()
    {
        final UnitVmModel model = (UnitVmModel) getWindow();
        final String name = (String) model.getName().getEntity();

        if (model.getIsNew() == false && selectedItem == null)
        {
            Cancel();
            return;
        }

        setcurrentVm(model.getIsNew() ? new VM() : (VM) Cloner.clone(selectedItem));

        if (!model.Validate())
        {
            return;
        }

        AsyncDataProvider.IsVmNameUnique(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object target, Object returnValue) {
                if (!(Boolean) returnValue && name.compareToIgnoreCase(getcurrentVm().getVmName()) != 0) {
                    model.getName()
                            .getInvalidityReasons()
                            .add(ConstantsManager.getInstance().getConstants().nameMustBeUniqueInvalidReason());
                    model.getName().setIsValid(false);
                    model.setIsGeneralTabValid(false);
                } else {
                    model.getName()
                            .getInvalidityReasons().clear();
                    model.getName().setIsValid(true);
                    model.setIsGeneralTabValid(true);
                    onSave();
                }
            }
        }), name);

    }

    private void onSave()
    {
        UnitVmModel model = (UnitVmModel) getWindow();
        VM selectedItem = (VM) getSelectedItem();

        String name = (String) model.getName().getEntity();

        // Save changes.
        VmTemplate template = (VmTemplate) model.getTemplate().getSelectedItem();

        getcurrentVm().setVmType(model.getVmType());
        getcurrentVm().setVmtGuid(template.getId());
        getcurrentVm().setVmName(name);
        if (model.getQuota().getSelectedItem() != null) {
            getcurrentVm().setQuotaId(((Quota) model.getQuota().getSelectedItem()).getId());
        }
        getcurrentVm().setVmOs((VmOsType) model.getOSType().getSelectedItem());
        getcurrentVm().setNumOfMonitors((Integer) model.getNumOfMonitors().getSelectedItem());
        getcurrentVm().setAllowConsoleReconnect((Boolean) model.getAllowConsoleReconnect().getEntity());
        getcurrentVm().setVmDescription((String) model.getDescription().getEntity());
        getcurrentVm().setVmDomain(model.getDomain().getIsAvailable() ? (String) model.getDomain().getSelectedItem()
                : ""); //$NON-NLS-1$
        getcurrentVm().setVmMemSizeMb((Integer) model.getMemSize().getEntity());
        getcurrentVm().setMinAllocatedMem((Integer) model.getMinAllocatedMemory().getEntity());
        Guid newClusterID = ((VDSGroup) model.getCluster().getSelectedItem()).getId();
        getcurrentVm().setVdsGroupId(newClusterID);
        getcurrentVm().setTimeZone((model.getTimeZone().getIsAvailable() && model.getTimeZone().getSelectedItem() != null) ? ((Map.Entry<String, String>) model.getTimeZone()
                .getSelectedItem()).getKey()
                : ""); //$NON-NLS-1$
        getcurrentVm().setNumOfSockets((Integer) model.getNumOfSockets().getSelectedItem());
        getcurrentVm().setCpuPerSocket(Integer.parseInt(model.getTotalCPUCores().getEntity().toString())
                / (Integer) model.getNumOfSockets().getSelectedItem());
        getcurrentVm().setUsbPolicy((UsbPolicy) model.getUsbPolicy().getSelectedItem());
        getcurrentVm().setAutoSuspend(false);
        getcurrentVm().setStateless((Boolean) model.getIsStateless().getEntity());
        getcurrentVm().setSmartcardEnabled((Boolean) model.getIsSmartcardEnabled().getEntity());
        getcurrentVm().setDeleteProtected((Boolean) model.getIsDeleteProtected().getEntity());
        getcurrentVm().setDefaultBootSequence(model.getBootSequence());
        getcurrentVm().setIsoPath(model.getCdImage().getIsChangable() ? (String) model.getCdImage().getSelectedItem()
                : ""); //$NON-NLS-1$
        getcurrentVm().setAutoStartup((Boolean) model.getIsHighlyAvailable().getEntity());

        getcurrentVm().setInitrdUrl((String) model.getInitrd_path().getEntity());
        getcurrentVm().setKernelUrl((String) model.getKernel_path().getEntity());
        getcurrentVm().setKernelParams((String) model.getKernel_parameters().getEntity());

        getcurrentVm().setCustomProperties(model.getCustomPropertySheet().getEntity());

        EntityModel displayProtocolSelectedItem = (EntityModel) model.getDisplayProtocol().getSelectedItem();
        getcurrentVm().setDefaultDisplayType((DisplayType) displayProtocolSelectedItem.getEntity());

        EntityModel prioritySelectedItem = (EntityModel) model.getPriority().getSelectedItem();
        getcurrentVm().setPriority((Integer) prioritySelectedItem.getEntity());

        getcurrentVm().setCpuPinning((String) model.getCpuPinning()
                .getEntity());

        VDS defaultHost = (VDS) model.getDefaultHost().getSelectedItem();
        if ((Boolean) model.getIsAutoAssign().getEntity())
        {
            getcurrentVm().setDedicatedVmForVds(null);
        }
        else
        {
            getcurrentVm().setDedicatedVmForVds(defaultHost.getId());
        }

        getcurrentVm().setMigrationSupport(MigrationSupport.MIGRATABLE);
        if ((Boolean) model.getRunVMOnSpecificHost().getEntity())
        {
            getcurrentVm().setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
            getcurrentVm().setUseHostCpuFlags((Boolean) model.getHostCpu().getEntity());
        }
        else if ((Boolean) model.getDontMigrateVM().getEntity())
        {
            getcurrentVm().setMigrationSupport(MigrationSupport.IMPLICITLY_NON_MIGRATABLE);
            getcurrentVm().setUseHostCpuFlags((Boolean) model.getHostCpu().getEntity());
        }
        else
        {
            getcurrentVm().setUseHostCpuFlags(Boolean.FALSE);
        }

        if (model.getIsNew())
        {
            if (getcurrentVm().getVmtGuid().equals(NGuid.Empty))
            {
                if (model.getProgress() != null)
                {
                    return;
                }

                model.StartProgress(null);

                Frontend.RunAction(VdcActionType.AddVmFromScratch, new AddVmFromScratchParameters(getcurrentVm(),
                        new ArrayList<DiskImage>(),
                        NGuid.Empty),
                        new IFrontendActionAsyncCallback() {
                            @Override
                            public void Executed(FrontendActionAsyncResult result) {

                                VmListModel vmListModel = (VmListModel) result.getState();
                                vmListModel.getWindow().StopProgress();
                                VdcReturnValueBase returnValueBase = result.getReturnValue();
                                if (returnValueBase != null && returnValueBase.getSucceeded())
                                {
                                    vmListModel.Cancel();
                                    vmListModel.setGuideContext(returnValueBase.getActionReturnValue());
                                    vmListModel.UpdateActionAvailability();
                                    vmListModel.getGuideCommand().Execute();
                                }

                            }
                        }, this);
            }
            else
            {
                if (model.getProgress() != null)
                {
                    return;
                }

                if ((Boolean) model.getProvisioning().getEntity())
                {
                    model.StartProgress(null);

                    AsyncQuery _asyncQuery = new AsyncQuery();
                    _asyncQuery.setModel(this);
                    _asyncQuery.asyncCallback = new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object model1, Object result1)
                        {
                            VmListModel vmListModel = (VmListModel) model1;
                            ArrayList<DiskImage> templateDisks = (ArrayList<DiskImage>) result1;

                            UnitVmModel unitVmModel = (UnitVmModel) vmListModel.getWindow();

                            HashMap<Guid, DiskImage> imageToDestinationDomainMap =
                                    unitVmModel.getDisksAllocationModel().getImageToDestinationDomainMap();

                            ArrayList<StorageDomain> activeStorageDomains =
                                    unitVmModel.getDisksAllocationModel().getActiveStorageDomains();

                            HashMap<Guid, DiskImage> dict =
                                    unitVmModel.getDisksAllocationModel().getImageToDestinationDomainMap();
                            for (DiskImage templateDisk : templateDisks)
                            {
                                DiskModel disk = null;
                                for (DiskModel a : unitVmModel.getDisksAllocationModel().getDisks())
                                {
                                    if (templateDisk.getId().equals(a.getDisk().getId()))
                                    {
                                        disk = a;
                                        break;
                                    }
                                }

                                StorageDomain storageDomain =
                                        Linq.getStorageById(
                                                imageToDestinationDomainMap.get(templateDisk.getId())
                                                        .getstorage_ids()
                                                        .get(0), activeStorageDomains);

                                if (disk != null) {
                                    dict.get(templateDisk.getId())
                                            .setvolume_type((VolumeType) disk.getVolumeType()
                                                    .getSelectedItem());
                                    dict.get(templateDisk.getId())
                                            .setvolume_format(AsyncDataProvider.GetDiskVolumeFormat(
                                                    (VolumeType) disk.getVolumeType().getSelectedItem(),
                                                    storageDomain.getstorage_type()));
                                    if (disk.getQuota().getSelectedItem() != null) {
                                        dict.get(templateDisk.getId()).setQuotaId(((Quota) disk.getQuota()
                                                .getSelectedItem()).getId());
                                    }
                                }
                            }

                            AddVmFromTemplateParameters param =
                                    new AddVmFromTemplateParameters(vmListModel.getcurrentVm(), dict, Guid.Empty);

                            ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
                            parameters.add(param);

                            Frontend.RunMultipleAction(VdcActionType.AddVmFromTemplate, parameters,
                                    new IFrontendMultipleActionAsyncCallback() {
                                        @Override
                                        public void Executed(FrontendMultipleActionAsyncResult result) {
                                            VmListModel vmListModel1 = (VmListModel) result.getState();
                                            vmListModel1.getWindow().StopProgress();
                                            vmListModel1.Cancel();
                                        }
                                    },
                                    vmListModel);
                        }
                    };
                    AsyncDataProvider.GetTemplateDiskList(_asyncQuery, template.getId());
                }
                else
                {
                    if (model.getProgress() != null)
                    {
                        return;
                    }

                    model.StartProgress(null);

                    VmManagementParametersBase params = new VmManagementParametersBase(getcurrentVm());
                    params.setDiskInfoDestinationMap(model.getDisksAllocationModel().getImageToDestinationDomainMap());

                    ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
                    parameters.add(params);

                    Frontend.RunMultipleAction(VdcActionType.AddVm, parameters,
                            new IFrontendMultipleActionAsyncCallback() {
                                @Override
                                public void Executed(FrontendMultipleActionAsyncResult result) {
                                    VmListModel vmListModel1 = (VmListModel) result.getState();
                                    vmListModel1.getWindow().StopProgress();
                                    vmListModel1.Cancel();
                                }
                            },
                            this);
                }
            }
        }
        else // Update existing VM -> consists of editing VM cluster, and if succeeds - editing VM:
        {
            if (model.getProgress() != null)
            {
                return;
            }

            // runEditVM: should be true if Cluster hasn't changed or if
            // Cluster has changed and Editing it in the Backend has succeeded:
            Guid oldClusterID = selectedItem.getVdsGroupId();
            if (oldClusterID.equals(newClusterID) == false)
            {
                ChangeVMClusterParameters parameters =
                        new ChangeVMClusterParameters(newClusterID, getcurrentVm().getId());

                model.StartProgress(null);

                Frontend.RunAction(VdcActionType.ChangeVMCluster, parameters,
                        new IFrontendActionAsyncCallback() {
                            @Override
                            public void Executed(FrontendActionAsyncResult result) {

                                VmListModel vmListModel = (VmListModel) result.getState();
                                VdcReturnValueBase returnValueBase = result.getReturnValue();
                                if (returnValueBase != null && returnValueBase.getSucceeded())
                                {
                                    Frontend.RunAction(VdcActionType.UpdateVm,
                                            new VmManagementParametersBase(vmListModel.getcurrentVm()),
                                            new IFrontendActionAsyncCallback() {
                                                @Override
                                                public void Executed(FrontendActionAsyncResult result1) {

                                                    VmListModel vmListModel1 = (VmListModel) result1.getState();
                                                    vmListModel1.getWindow().StopProgress();
                                                    VdcReturnValueBase retVal = result1.getReturnValue();
                                                    boolean isSucceeded = retVal.getSucceeded();
                                                    if (retVal != null && isSucceeded)
                                                    {
                                                        vmListModel1.Cancel();
                                                    }

                                                }
                                            },
                                            vmListModel);
                                }
                                else
                                {
                                    vmListModel.getWindow().StopProgress();
                                }

                            }
                        }, this);
            }
            else
            {
                if (model.getProgress() != null)
                {
                    return;
                }

                model.StartProgress(null);

                Frontend.RunAction(VdcActionType.UpdateVm, new VmManagementParametersBase(getcurrentVm()),
                        new IFrontendActionAsyncCallback() {
                            @Override
                            public void Executed(FrontendActionAsyncResult result) {

                                VmListModel vmListModel = (VmListModel) result.getState();
                                vmListModel.getWindow().StopProgress();
                                VdcReturnValueBase returnValueBase = result.getReturnValue();
                                if (returnValueBase != null && returnValueBase.getSucceeded())
                                {
                                    vmListModel.Cancel();
                                }

                            }
                        }, this);
            }
        }
    }

    private void RetrieveIsoImages()
    {
        Object tempVar = getSelectedItem();
        VM vm = (VM) ((tempVar instanceof VM) ? tempVar : null);
        if (vm == null)
        {
            return;
        }

        Guid storagePoolId = vm.getStoragePoolId();

        getIsoImages().clear();

        ChangeCDModel tempVar2 = new ChangeCDModel();
        tempVar2.setTitle(ConsoleModel.EjectLabel);
        ChangeCDModel ejectModel = tempVar2;
        ejectModel.getExecutedEvent().addListener(this);
        getIsoImages().add(ejectModel);

        ArrayList<String> list = new ArrayList<String>();
        ChangeCDModel tempVar4 = new ChangeCDModel();
        tempVar4.setTitle(ConstantsManager.getInstance().getConstants().noCDsTitle());
        getIsoImages().add(tempVar4);
    }

    private void changeCD(Object sender, EventArgs e)
    {
        ChangeCDModel model = (ChangeCDModel) sender;

        // TODO: Patch!
        String isoName = model.getTitle();
        if (StringHelper.stringsEqual(isoName, "No CDs")) //$NON-NLS-1$
        {
            return;
        }

        Object tempVar = getSelectedItem();
        VM vm = (VM) ((tempVar instanceof VM) ? tempVar : null);
        if (vm == null)
        {
            return;
        }

        Frontend.RunMultipleAction(VdcActionType.ChangeDisk,
                new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] { new ChangeDiskCommandParameters(vm.getId(),
                        StringHelper.stringsEqual(isoName, ConsoleModel.EjectLabel) ? "" : isoName) })), //$NON-NLS-1$
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                    }
                },
                null);
    }

    public void Cancel()
    {
        Frontend.Unsubscribe();

        CancelConfirmation();

        setGuideContext(null);
        setWindow(null);

        UpdateActionAvailability();
    }

    private void CancelConfirmation()
    {
        setConfirmWindow(null);
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();

        UpdateActionAvailability();
        UpdateConsoleModels();
    }

    @Override
    protected void SelectedItemsChanged()
    {
        super.SelectedItemsChanged();

        UpdateActionAvailability();
        UpdateConsoleModels();
    }

    @Override
    protected void SelectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.SelectedItemPropertyChanged(sender, e);

        if (e.PropertyName.equals("status")) //$NON-NLS-1$
        {
            UpdateActionAvailability();

        }
        else if (e.PropertyName.equals("display_type")) //$NON-NLS-1$
        {
            UpdateConsoleModels();
        }
    }

    private void UpdateActionAvailability()
    {
        List items =
                getSelectedItems() != null && getSelectedItem() != null ? getSelectedItems()
                        : new ArrayList();

        getEditCommand().setIsExecutionAllowed(isEditCommandExecutionAllowed(items));
        getRemoveCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.RemoveVm));
        getRunCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.RunVm));
        getPauseCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.HibernateVm));
        getShutdownCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.ShutdownVm));
        getStopCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.StopVm));
        getMigrateCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.MigrateVm));
        getCancelMigrateCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.CancelMigrateVm));
        getNewTemplateCommand().setIsExecutionAllowed(items.size() == 1
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.AddVmTemplate));
        getRunOnceCommand().setIsExecutionAllowed(items.size() == 1
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.RunVmOnce));
        getExportCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.ExportVm));
        getCreateSnapshotCommand().setIsExecutionAllowed(items.size() == 1
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.CreateAllSnapshotsFromVm));
        getMoveCommand().setIsExecutionAllowed(items.size() == 1
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.MoveVm));
        getRetrieveIsoImagesCommand().setIsExecutionAllowed(items.size() == 1
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.ChangeDisk));
        getChangeCdCommand().setIsExecutionAllowed(items.size() == 1
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.ChangeDisk));
        getAssignTagsCommand().setIsExecutionAllowed(items.size() > 0);

        getGuideCommand().setIsExecutionAllowed(getGuideContext() != null
                || (getSelectedItem() != null && getSelectedItems() != null && getSelectedItems().size() == 1));
    }

    /**
     * Return true if and only if one element is selected.
     */
    private boolean isEditCommandExecutionAllowed(List items) {
        if (items == null) {
            return false;
        }
        if (items.size() != 1) {
            return false;
        }
        return true;
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.equals(ChangeCDModel.ExecutedEventDefinition))
        {
            changeCD(sender, args);
        }
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getNewServerCommand())
        {
            NewServer();
        }
        else if (command == getNewDesktopCommand())
        {
            NewDesktop();
        }
        else if (command == getEditCommand())
        {
            Edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (command == getRunCommand())
        {
            Run();
        }
        else if (command == getPauseCommand())
        {
            Pause();
        }
        else if (command == getStopCommand())
        {
            stop();
        }
        else if (command == getShutdownCommand())
        {
            Shutdown();
        }
        else if (command == getMigrateCommand())
        {
            Migrate();
        }
        else if (command == getNewTemplateCommand())
        {
            NewTemplate();
        }
        else if (command == getRunOnceCommand())
        {
            RunOnce();
        }
        else if (command == getExportCommand())
        {
            Export(ConstantsManager.getInstance().getConstants().exportVirtualMachineTitle());
        }
        else if (command == getCreateSnapshotCommand())
        {
            CreateSnapshot();
        }
        else if (command == getMoveCommand())
        {
            Move();
        }
        else if (command == getGuideCommand())
        {
            Guide();
        }
        else if (command == getRetrieveIsoImagesCommand())
        {
            RetrieveIsoImages();
        }
        else if (command == getChangeCdCommand())
        {
            ChangeCD();
        }
        else if (command == getAssignTagsCommand())
        {
            AssignTags();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnAssignTags")) //$NON-NLS-1$
        {
            OnAssignTags();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            preSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            OnRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnExport")) //$NON-NLS-1$
        {
            OnExport();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnExportNoTemplates")) //$NON-NLS-1$
        {
            OnExportNoTemplates();
        }
        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirmation")) //$NON-NLS-1$
        {
            CancelConfirmation();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRunOnce")) //$NON-NLS-1$
        {
            OnRunOnce();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnNewTemplate")) //$NON-NLS-1$
        {
            OnNewTemplate();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnMigrate")) //$NON-NLS-1$
        {
            OnMigrate();
        }
        else if (command == getCancelMigrateCommand())
        {
            CancelMigration();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnShutdown")) //$NON-NLS-1$
        {
            OnShutdown();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnStop")) //$NON-NLS-1$
        {
            OnStop();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnChangeCD")) //$NON-NLS-1$
        {
            OnChangeCD();
        }
        else if (command.getName().equals("closeVncInfo")) { //$NON-NLS-1$
            setWindow(null);
        }
    }

    private SystemTreeItemModel systemTreeSelectedItem;

    @Override
    public SystemTreeItemModel getSystemTreeSelectedItem()
    {
        return systemTreeSelectedItem;
    }

    @Override
    public void setSystemTreeSelectedItem(SystemTreeItemModel value)
    {
        systemTreeSelectedItem = value;
        OnPropertyChanged(new PropertyChangedEventArgs("SystemTreeSelectedItem")); //$NON-NLS-1$
    }

    @Override
    protected String getListName() {
        return "VmListModel"; //$NON-NLS-1$
    }

    @Override
    protected Guid extractStoragePoolIdNullSafe(VM entity) {
        return entity.getStoragePoolId();
    }


}
