package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VmWithStatusForExclusiveLock;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister.ConsoleContext;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.TagsEqualityComparer;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.template.UnitToAddVmTemplateParametersBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.template.VmBaseToVmBaseForTemplateCompositeBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CommonUnitToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.UnitToGraphicsDeviceParamsBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.VmIconUnitAndVmToParameterBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ConsolePopupModel;
import org.ovirt.engine.ui.uicommonweb.models.ConsolesFactory;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.VmConsoles;
import org.ovirt.engine.ui.uicommonweb.models.configure.ChangeCDModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list.VmAffinityGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.VmBaseListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.AttachCdModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.VmHostDeviceListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.ICancelable;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class VmListModel<E> extends VmBaseListModel<E, VM> implements ISupportSystemTreeContext, ICancelable, HasDiskWindow {

    public static final String CMD_CONFIGURE_VMS_TO_IMPORT = "ConfigureVmsToImport"; //$NON-NLS-1$
    public static final String CMD_CANCEL = "Cancel"; //$NON-NLS-1$
    private static final String CMD_BACK = "Back"; //$NON-NLS-1$
    private static final String CMD_IMPORT = "Import"; //$NON-NLS-1$

    private final UIConstants constants = ConstantsManager.getInstance().getConstants();

    final Provider<ImportVmsModel> importVmsModelProvider;

    private UICommand importVmCommand;

    public UICommand getImportVmCommand() {
        return importVmCommand;
    }

    public void setImportVmCommand(UICommand importVmCommand) {
        this.importVmCommand = importVmCommand;
    }

    private UICommand cloneVmCommand;

    public UICommand getCloneVmCommand() {
        return cloneVmCommand;
    }

    public void setCloneVmCommand(UICommand cloneVmCommand) {
        this.cloneVmCommand = cloneVmCommand;
    }

    private UICommand newVMCommand;

    private static final String SHUTDOWN = "Shutdown"; //$NON-NLS-1$
    private static final String STOP     = "Stop"; //$NON-NLS-1$
    private static final String REBOOT   = "Reboot"; //$NON-NLS-1$

    public UICommand getNewVmCommand() {
        return newVMCommand;
    }

    private void setNewVmCommand(UICommand newVMCommand) {
        this.newVMCommand = newVMCommand;
    }

    private UICommand privateEditCommand;

    @Override
    public UICommand getEditCommand() {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value) {
        privateEditCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand() {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value) {
        privateRemoveCommand = value;
    }

    private UICommand privateRunCommand;

    public UICommand getRunCommand() {
        return privateRunCommand;
    }

    private void setRunCommand(UICommand value) {
        privateRunCommand = value;
    }

    private UICommand privatePauseCommand;

    public UICommand getPauseCommand() {
        return privatePauseCommand;
    }

    private void setPauseCommand(UICommand value) {
        privatePauseCommand = value;
    }

    private UICommand privateStopCommand;

    public UICommand getStopCommand() {
        return privateStopCommand;
    }

    private void setStopCommand(UICommand value) {
        privateStopCommand = value;
    }

    private UICommand privateShutdownCommand;

    public UICommand getShutdownCommand() {
        return privateShutdownCommand;
    }

    private void setShutdownCommand(UICommand value) {
        privateShutdownCommand = value;
    }

    private UICommand privateRebootCommand;

    public UICommand getRebootCommand() {
        return privateRebootCommand;
    }

    public void setRebootCommand(UICommand value) {
        privateRebootCommand = value;
    }

    private UICommand privateCancelMigrateCommand;

    public UICommand getCancelMigrateCommand() {
        return privateCancelMigrateCommand;
    }

    private void setCancelMigrateCommand(UICommand value) {
        privateCancelMigrateCommand = value;
    }

    private UICommand cancelConvertCommand;

    public UICommand getCancelConvertCommand() {
        return cancelConvertCommand;
    }

    private void setCancelConvertCommand(UICommand value) {
        cancelConvertCommand = value;
    }

    private UICommand privateMigrateCommand;

    public UICommand getMigrateCommand() {
        return privateMigrateCommand;
    }

    private void setMigrateCommand(UICommand value) {
        privateMigrateCommand = value;
    }

    private UICommand privateNewTemplateCommand;

    public UICommand getNewTemplateCommand() {
        return privateNewTemplateCommand;
    }

    private void setNewTemplateCommand(UICommand value) {
        privateNewTemplateCommand = value;
    }

    private UICommand privateRunOnceCommand;

    public UICommand getRunOnceCommand() {
        return privateRunOnceCommand;
    }

    private void setRunOnceCommand(UICommand value) {
        privateRunOnceCommand = value;
    }

    private UICommand privateExportCommand;

    public UICommand getExportCommand() {
        return privateExportCommand;
    }

    private void setExportCommand(UICommand value) {
        privateExportCommand = value;
    }

    private UICommand privateCreateSnapshotCommand;

    public UICommand getCreateSnapshotCommand() {
        return privateCreateSnapshotCommand;
    }

    private void setCreateSnapshotCommand(UICommand value) {
        privateCreateSnapshotCommand = value;
    }

    private UICommand privateRetrieveIsoImagesCommand;

    public UICommand getRetrieveIsoImagesCommand() {
        return privateRetrieveIsoImagesCommand;
    }

    private void setRetrieveIsoImagesCommand(UICommand value) {
        privateRetrieveIsoImagesCommand = value;
    }

    private UICommand privateGuideCommand;

    public UICommand getGuideCommand() {
        return privateGuideCommand;
    }

    private void setGuideCommand(UICommand value) {
        privateGuideCommand = value;
    }

    private UICommand privateChangeCdCommand;

    public UICommand getChangeCdCommand() {
        return privateChangeCdCommand;
    }

    private void setChangeCdCommand(UICommand value) {
        privateChangeCdCommand = value;
    }

    private UICommand privateAssignTagsCommand;

    public UICommand getAssignTagsCommand() {
        return privateAssignTagsCommand;
    }

    private void setAssignTagsCommand(UICommand value) {
        privateAssignTagsCommand = value;
    }

    UICommand editConsoleCommand;

    public void setEditConsoleCommand(UICommand editConsoleCommand) {
        this.editConsoleCommand = editConsoleCommand;
    }

    public UICommand getEditConsoleCommand() {
        return editConsoleCommand;
    }

    public UICommand consoleConnectCommand;

    public UICommand getConsoleConnectCommand() {
        return consoleConnectCommand;
    }

    public void setConsoleConnectCommand(UICommand consoleConnectCommand) {
        this.consoleConnectCommand = consoleConnectCommand;
    }

    public ObservableCollection<ChangeCDModel> isoImages;

    public ObservableCollection<ChangeCDModel> getIsoImages() {
        return isoImages;
    }

    private void setIsoImages(ObservableCollection<ChangeCDModel> value) {
        if ((isoImages == null && value != null) || (isoImages != null && !isoImages.equals(value))) {
            isoImages = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsoImages")); //$NON-NLS-1$
        }
    }

    private Object privateGuideContext;

    public Object getGuideContext() {
        return privateGuideContext;
    }

    public void setGuideContext(Object value) {
        privateGuideContext = value;
    }

    private final ConsolesFactory consolesFactory;

    private ErrorPopupManager errorPopupManager;

    /** The edited VM could be different than the selected VM in the grid
     *  when the VM has next-run configuration */
    private VM editedVm;

    @Inject
    public VmListModel(final VmGeneralModel vmGeneralModel, final VmInterfaceListModel vmInterfaceListModel,
            final VmDiskListModel vmDiskListModel, final VmSnapshotListModel vmSnapshotListModel,
            final VmEventListModel vmEventListModel, final VmAppListModel<VM> vmAppListModel,
            final PermissionListModel<VM> permissionListModel, final VmAffinityGroupListModel vmAffinityGroupListModel,
            final VmGuestInfoModel vmGuestInfoModel, final Provider<ImportVmsModel> importVmsModelProvider,
            final VmHostDeviceListModel vmHostDeviceListModel, final VmDevicesListModel vmDevicesListModel) {
        setDetailList(vmGeneralModel, vmInterfaceListModel, vmDiskListModel, vmSnapshotListModel, vmEventListModel,
                vmAppListModel, permissionListModel, vmAffinityGroupListModel, vmGuestInfoModel, vmHostDeviceListModel,
                vmDevicesListModel);
        this.importVmsModelProvider = importVmsModelProvider;
        setTitle(ConstantsManager.getInstance().getConstants().virtualMachinesTitle());
        setHelpTag(HelpTag.virtual_machines);
        setApplicationPlace(WebAdminApplicationPlaces.virtualMachineMainTabPlace);
        setHashName("virtual_machines"); //$NON-NLS-1$

        setDefaultSearchString("Vms:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.VM_OBJ_NAME, SearchObjects.VM_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        consolesFactory = new ConsolesFactory(ConsoleContext.WA, this);
        setConsoleHelpers();

        setNewVmCommand(new UICommand("NewVm", this)); //$NON-NLS-1$
        setImportVmCommand(new UICommand("ImportVm", this)); //$NON-NLS-1$
        setCloneVmCommand(new UICommand("CloneVm", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setRunCommand(new UICommand("Run", this, true)); //$NON-NLS-1$
        setPauseCommand(new UICommand("Pause", this)); //$NON-NLS-1$
        setStopCommand(new UICommand("Stop", this)); //$NON-NLS-1$
        setShutdownCommand(new UICommand("Shutdown", this)); //$NON-NLS-1$
        setRebootCommand(new UICommand("Reboot", this)); //$NON-NLS-1$
        setEditConsoleCommand(new UICommand("EditConsoleCommand", this)); //$NON-NLS-1$
        setConsoleConnectCommand(new UICommand("ConsoleConnectCommand", this)); //$NON-NLS-1$
        setMigrateCommand(new UICommand("Migrate", this)); //$NON-NLS-1$
        setCancelMigrateCommand(new UICommand("CancelMigration", this)); //$NON-NLS-1$
        setCancelConvertCommand(new UICommand("CancelConversion", this)); //$NON-NLS-1$
        setNewTemplateCommand(new UICommand("NewTemplate", this)); //$NON-NLS-1$
        setRunOnceCommand(new UICommand("RunOnce", this)); //$NON-NLS-1$
        setExportCommand(new UICommand("Export", this)); //$NON-NLS-1$
        setCreateSnapshotCommand(new UICommand("CreateSnapshot", this)); //$NON-NLS-1$
        setGuideCommand(new UICommand("Guide", this)); //$NON-NLS-1$
        setRetrieveIsoImagesCommand(new UICommand("RetrieveIsoImages", this)); //$NON-NLS-1$
        setChangeCdCommand(new UICommand("ChangeCD", this)); //$NON-NLS-1$
        setAssignTagsCommand(new UICommand("AssignTags", this)); //$NON-NLS-1$

        setIsoImages(new ObservableCollection<ChangeCDModel>());
        ChangeCDModel tempVar = new ChangeCDModel();
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().retrievingCDsTitle());
        getIsoImages().add(tempVar);

        updateActionsAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    private void setDetailList(final VmGeneralModel vmGeneralModel, final VmInterfaceListModel vmInterfaceListModel,
            final VmDiskListModel vmDiskListModel, final VmSnapshotListModel vmSnapshotListModel,
            final VmEventListModel vmEventListModel, final VmAppListModel<VM> vmAppListModel,
            final PermissionListModel<VM> permissionListModel, final VmAffinityGroupListModel vmAffinityGroupListModel,
            final VmGuestInfoModel vmGuestInfoModel, final VmHostDeviceListModel vmHostDeviceListModel,
            final VmDevicesListModel vmDevicesListModel) {
        List<HasEntity<VM>> list = new ArrayList<>();
        list.add(vmGeneralModel);
        list.add(vmInterfaceListModel);
        vmDiskListModel.setSystemTreeContext(this);
        list.add(vmDiskListModel);
        list.add(vmSnapshotListModel);
        list.add(vmEventListModel);
        list.add(vmAppListModel);
        list.add(vmDevicesListModel);
        list.add(permissionListModel);
        list.add(vmAffinityGroupListModel);
        list.add(vmGuestInfoModel);
        list.add(vmHostDeviceListModel);
        setDetailModels(list);
    }

    private void setConsoleHelpers() {
        this.errorPopupManager = (ErrorPopupManager) TypeResolver.getInstance().resolve(ErrorPopupManager.class);
    }

    private void assignTags() {
        if (getWindow() != null) {
            return;
        }

        TagListModel model = new TagListModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().assignTagsTitle());
        model.setHelpTag(HelpTag.assign_tags_vms);
        model.setHashName("assign_tags_vms"); //$NON-NLS-1$

        getAttachedTagsToSelectedVMs(model);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnAssignTags", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public Map<Guid, Boolean> attachedTagsToEntities;
    public ArrayList<Tags> allAttachedTags;
    public int selectedItemsCounter;

    private void getAttachedTagsToSelectedVMs(final TagListModel model) {
        ArrayList<Guid> vmIds = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VM vm = (VM) item;
            vmIds.add(vm.getId());
        }

        attachedTagsToEntities = new HashMap<>();
        allAttachedTags = new ArrayList<>();
        selectedItemsCounter = 0;

        for (Guid id : vmIds) {
            AsyncDataProvider.getInstance().getAttachedTagsToVm(new AsyncQuery<>(
                    new AsyncCallback<List<Tags>>() {
                        @Override
                        public void onSuccess(List<Tags> returnValue) {

                            allAttachedTags.addAll(returnValue);
                            selectedItemsCounter++;
                            if (selectedItemsCounter == getSelectedItems().size()) {
                                postGetAttachedTags(model);
                            }

                        }
                    }),
                    id);
        }
    }

    private void postGetAttachedTags(TagListModel tagListModel) {
        if (getLastExecutedCommand() == getAssignTagsCommand()) {
            ArrayList<Tags> attachedTags =
                    Linq.distinct(allAttachedTags, new TagsEqualityComparer());
            for (Tags tag : attachedTags) {
                int count = 0;
                for (Tags tag2 : allAttachedTags) {
                    if (tag2.getTagId().equals(tag.getTagId())) {
                        count++;
                    }
                }
                attachedTagsToEntities.put(tag.getTagId(), count == getSelectedItems().size());
            }
            tagListModel.setAttachedTagsToEntities(attachedTagsToEntities);
        }
        else if ("OnAssignTags".equals(getLastExecutedCommand().getName())) { //$NON-NLS-1$
            postOnAssignTags(tagListModel.getAttachedTagsToEntities());
        }
    }

    private void onAssignTags() {
        TagListModel model = (TagListModel) getWindow();

        getAttachedTagsToSelectedVMs(model);
    }

    public void postOnAssignTags(Map<Guid, Boolean> attachedTags) {
        TagListModel model = (TagListModel) getWindow();
        ArrayList<Guid> vmIds = new ArrayList<>();

        for (Object item : getSelectedItems()) {
            VM vm = (VM) item;
            vmIds.add(vm.getId());
        }

        // prepare attach/detach lists
        ArrayList<Guid> tagsToAttach = new ArrayList<>();
        ArrayList<Guid> tagsToDetach = new ArrayList<>();

        if (model.getItems() != null && model.getItems().size() > 0) {
            ArrayList<TagModel> tags = (ArrayList<TagModel>) model.getItems();
            TagModel rootTag = tags.get(0);
            TagModel.recursiveEditAttachDetachLists(rootTag, attachedTags, tagsToAttach, tagsToDetach);
        }

        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
        for (Guid a : tagsToAttach) {
            parameters.add(new AttachEntityToTagParameters(a, vmIds));
        }
        Frontend.getInstance().runMultipleAction(VdcActionType.AttachVmsToTag, parameters);

        parameters = new ArrayList<>();
        for (Guid a : tagsToDetach) {
            parameters.add(new AttachEntityToTagParameters(a, vmIds));
        }
        Frontend.getInstance().runMultipleAction(VdcActionType.DetachVmFromTag, parameters);

        cancel();
    }

    private void guide() {
        VmGuideModel model = new VmGuideModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newVirtualMachineGuideMeTitle());
        model.setHelpTag(HelpTag.new_virtual_machine___guide_me);
        model.setHashName("new_virtual_machine_-_guide_me"); //$NON-NLS-1$

        if (getGuideContext() == null) {
            VM vm = getSelectedItem();
            setGuideContext(vm.getId());
        }

        AsyncDataProvider.getInstance().getVmById(new AsyncQuery<>(
                                                   new AsyncCallback<VM>() {
                                                       @Override
                                                       public void onSuccess(VM returnValue) {
                                                           VmGuideModel model = (VmGuideModel) getWindow();
                                                           model.setEntity(returnValue);

                                                           UICommand tempVar = new UICommand("Cancel", VmListModel.this); //$NON-NLS-1$
                                                           tempVar.setTitle(ConstantsManager.getInstance().getConstants().configureLaterTitle());
                                                           tempVar.setIsDefault(true);
                                                           tempVar.setIsCancel(true);
                                                           model.getCommands().add(tempVar);
                                                       }
                                                   }), (Guid) getGuideContext());
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("vm"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        SearchParameters tempVar = new SearchParameters(applySortOptions(getSearchString()), SearchType.VM, isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, tempVar);
    }

    private void newVm() {
        if (getWindow() != null) {
            return;
        }

        List<UICommand> commands = new ArrayList<>();
        commands.add(UICommand.createDefaultOkUiCommand("OnSave", this)); //$NON-NLS-1$
        commands.add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
        UnitVmModel model = new UnitVmModel(new NewVmModelBehavior(), this);
        setupNewVmModel(model, VmType.Server, getSystemTreeSelectedItem(), commands);
    }

    private void editConsole() {
        if (getWindow() != null || getSelectedItem() == null) {
            return;
        }

        final VmConsoles activeVmConsoles = consolesFactory.getVmConsolesForVm(getSelectedItem());

        final ConsolePopupModel model = new ConsolePopupModel();
        model.setVmConsoles(activeVmConsoles);
        model.setHelpTag(HelpTag.editConsole);
        model.setHashName("editConsole"); //$NON-NLS-1$
        setWindow(model);

        final UICommand saveCommand = UICommand.createDefaultOkUiCommand("OnEditConsoleSave", this); //$NON-NLS-1$
        model.getCommands().add(saveCommand);
        final UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    private void edit() {
        VM vm = getSelectedItem();
        if (vm == null) {
            return;
        }

        if (getWindow() != null) {
            return;
        }

        // populating VMInit
        AsyncQuery<VM> getVmInitQuery = new AsyncQuery<>(new AsyncCallback<VM>() {
            @Override
            public void onSuccess(VM result) {
                editedVm = result;
                vmInitLoaded(editedVm);
            }
        });
        if (vm.isNextRunConfigurationExists()) {
            AsyncDataProvider.getInstance().getVmNextRunConfiguration(getVmInitQuery, vm.getId());
        } else {
            AsyncDataProvider.getInstance().getVmById(getVmInitQuery, vm.getId());
        }

    }

    private void vmInitLoaded(VM vm) {
        UnitVmModel model = new UnitVmModel(new ExistingVmModelBehavior(vm), this);
        model.getVmType().setSelectedItem(vm.getVmType());
        model.setVmAttachedToPool(vm.getVmPoolId() != null);
        model.setIsAdvancedModeLocalStorageKey("wa_vm_dialog");  //$NON-NLS-1$
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance()
                .getConstants().editVmTitle());
        model.setHelpTag(HelpTag.edit_vm);
        model.setHashName("edit_vm"); //$NON-NLS-1$
        model.setCustomPropertiesKeysList(AsyncDataProvider.getInstance().getCustomPropertiesList());

        model.initialize(this.getSystemTreeSelectedItem());
        model.initForemanProviders(vm.getProviderId());

        VmBasedWidgetSwitchModeCommand switchModeCommand = new VmBasedWidgetSwitchModeCommand();
        switchModeCommand.init(model);
        model.getCommands().add(switchModeCommand);

        model.getCommands().add(UICommand.createDefaultOkUiCommand("OnSave", this)); //$NON-NLS-1$

        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    private Map<Guid, EntityModel> vmsRemoveMap;

    private void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel window = new ConfirmationModel();
        setWindow(window);
        window.setTitle(ConstantsManager.getInstance().getConstants().removeVirtualMachinesTitle());
        window.setHelpTag(HelpTag.remove_virtual_machine);
        window.setHashName("remove_virtual_machine"); //$NON-NLS-1$

        vmsRemoveMap = new HashMap<>();

        for (Object selectedItem : getSelectedItems()) {
            VM vm = (VM) selectedItem;
            if (VdcActionUtils.canExecute(Arrays.asList(vm), VM.class, VdcActionType.RemoveVm)) {
                EntityModel removeDisksCheckbox = new EntityModel(true);
                removeDisksCheckbox.setTitle(ConstantsManager.getInstance().getConstants().removeDisksTitle());
                removeDisksCheckbox.setMessage(vm.getName());
                if (!Guid.Empty.equals(vm.getVmtGuid())) {
                    updateRemoveDisksCheckBox(removeDisksCheckbox, true, false, ConstantsManager.getInstance()
                            .getConstants()
                            .removeVmDisksTemplateMsg());
                }
                vmsRemoveMap.put(vm.getId(), removeDisksCheckbox);
            }
        }
        window.setItems(vmsRemoveMap.entrySet());
        initRemoveDisksCheckboxes(vmsRemoveMap);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        window.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        window.getCommands().add(tempVar2);
    }

    private void updateRemoveDisksCheckBox(EntityModel model,
            boolean deleteDisks,
            boolean isChangable,
            String changeProhibitionReason) {

        model.setEntity(deleteDisks);
        if (!isChangable && changeProhibitionReason != null) {
            model.setChangeProhibitionReason(changeProhibitionReason);
        }
        model.setIsChangeable(isChangable);
    }

    private void initRemoveDisksCheckboxes(final Map<Guid, EntityModel> vmsMap) {
        ArrayList<VdcQueryParametersBase> params = new ArrayList<>();
        ArrayList<VdcQueryType> queries = new ArrayList<>();

        for (Entry<Guid, EntityModel> entry : vmsMap.entrySet()) {
            if (entry.getValue().getIsChangable()) { // No point in fetching VM disks from ones that already determined
                                                     // is unchangeable since they are already initialized
                params.add(new IdQueryParameters(entry.getKey()));
                queries.add(VdcQueryType.GetAllDisksByVmId);
            }
        }

        // TODO: There's no point in creating a VdcQueryType list when you wanna run the same query for all parameters,
        // revise when refactoring org.ovirt.engine.ui.Frontend to support runMultipleQuery with a single query
        if (!params.isEmpty()) {
            Frontend.getInstance().runMultipleQueries(queries, params, new IFrontendMultipleQueryAsyncCallback() {
                @Override
                public void executed(FrontendMultipleQueryAsyncResult result) {
                    for (int i = 0; i < result.getReturnValues().size(); i++) {
                        if (result.getReturnValues().get(i).getSucceeded()) {
                            Guid vmId = ((IdQueryParameters) result.getParameters().get(i)).getId();
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

    private void createSnapshot() {
        VM vm = getSelectedItem();
        if (vm == null || getWindow() != null) {
            return;
        }

        SnapshotModel model = SnapshotModel.createNewSnapshotModel(this);
        model.setValidateByVmSnapshots(true);
        setWindow(model);
        model.setVm(vm);
        model.initialize();
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
    protected boolean entitiesSelectedOnDifferentDataCenters() {
        ArrayList<VM> vms = new ArrayList<>();
        for (Object selectedItem : getSelectedItems()) {
            VM a = (VM) selectedItem;
            vms.add(a);
        }

        Map<Guid, ArrayList<VM>> t = new HashMap<>();
        for (VM a : vms) {
            if (!t.containsKey(a.getStoragePoolId())) {
                t.put(a.getStoragePoolId(), new ArrayList<VM>());
            }

            ArrayList<VM> list = t.get(a.getStoragePoolId());
            list.add(a);
        }

        return t.size() > 1;
    }

    @Override
    protected String extractNameFromEntity(VM entity) {
        return entity.getName();
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

    private void getTemplatesNotPresentOnExportDomain() {
        ExportVmModel model = (ExportVmModel) getWindow();
        Guid storageDomainId = model.getStorage().getSelectedItem().getId();

        AsyncDataProvider.getInstance().getDataCentersByStorageDomain(new AsyncQuery<>(
                new AsyncCallback<List<StoragePool>>() {
                    @Override
                    public void onSuccess(List<StoragePool> storagePools) {
                        StoragePool storagePool = storagePools.size() > 0 ? storagePools.get(0) : null;

                        postGetTemplatesNotPresentOnExportDomain(storagePool);
                    }
                }), storageDomainId);
    }

    private void postGetTemplatesNotPresentOnExportDomain(StoragePool storagePool) {
        ExportVmModel model = (ExportVmModel) getWindow();
        Guid storageDomainId = model.getStorage().getSelectedItem().getId();

        if (storagePool != null) {
            AsyncDataProvider.getInstance().getAllTemplatesFromExportDomain(new AsyncQuery<>(
                            new AsyncCallback<Map<VmTemplate, ArrayList<DiskImage>>>() {
                                @Override
                                public void onSuccess(Map<VmTemplate, ArrayList<DiskImage>> templatesDiskSet) {
                                    HashMap<String, ArrayList<String>> templateDic =
                                            new HashMap<>();

                                    // check if relevant templates are already there
                                    for (VM vm : getSelectedItems()) {
                                        boolean hasMatch = false;
                                        for (VmTemplate a : templatesDiskSet.keySet()) {
                                            if (vm.getVmtGuid().equals(a.getId())) {
                                                hasMatch = true;
                                                break;
                                            }
                                        }

                                        if (!vm.getVmtGuid().equals(Guid.Empty) && !hasMatch) {
                                            if (!templateDic.containsKey(vm.getVmtName())) {
                                                templateDic.put(vm.getVmtName(), new ArrayList<String>());
                                            }
                                            templateDic.get(vm.getVmtName()).add(vm.getName());
                                        }
                                    }

                                    ArrayList<String> tempList;
                                    ArrayList<String> missingTemplates = new ArrayList<>();
                                    for (Map.Entry<String, ArrayList<String>> keyValuePair : templateDic.entrySet()) {
                                        tempList = keyValuePair.getValue();
                                        StringBuilder sb = new StringBuilder("Template " + keyValuePair.getKey() + " (for "); //$NON-NLS-1$ //$NON-NLS-2$
                                        int i;
                                        for (i = 0; i < tempList.size() - 1; i++) {
                                            sb.append(tempList.get(i));
                                            sb.append(", "); //$NON-NLS-1$
                                        }
                                        sb.append(tempList.get(i));
                                        sb.append(")"); //$NON-NLS-1$
                                        missingTemplates.add(sb.toString());
                                    }

                                    postExportGetMissingTemplates(missingTemplates);
                                }
                            }),
                    storagePool.getId(),
                    storageDomainId);
        }
    }

    private void postExportGetMissingTemplates(ArrayList<String> missingTemplatesFromVms) {
        ExportVmModel model = (ExportVmModel) getWindow();
        Guid storageDomainId = model.getStorage().getSelectedItem().getId();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();

        model.stopProgress();

        for (Object a : getSelectedItems()) {
            VM vm = (VM) a;
            MoveOrCopyParameters parameter = new MoveOrCopyParameters(vm.getId(), storageDomainId);
            parameter.setForceOverride(model.getForceOverride().getEntity());
            parameter.setCopyCollapse(model.getCollapseSnapshots().getEntity());
            parameter.setTemplateMustExists(true);

            parameters.add(parameter);
        }

        if (!model.getCollapseSnapshots().getEntity()) {
            if (missingTemplatesFromVms == null || missingTemplatesFromVms.size() > 0) {
                ConfirmationModel confirmModel = new ConfirmationModel();
                setConfirmWindow(confirmModel);
                confirmModel.setTitle(ConstantsManager.getInstance()
                        .getConstants()
                        .templatesNotFoundOnExportDomainTitle());
                confirmModel.setHelpTag(HelpTag.template_not_found_on_export_domain);
                confirmModel.setHashName("template_not_found_on_export_domain"); //$NON-NLS-1$

                confirmModel.setMessage(missingTemplatesFromVms == null ? ConstantsManager.getInstance()
                        .getConstants()
                        .couldNotReadTemplatesFromExportDomainMsg()
                        : ConstantsManager.getInstance()
                                .getConstants()
                                .theFollowingTemplatesAreMissingOnTargetExportDomainMsg());
                confirmModel.setItems(missingTemplatesFromVms);

                UICommand tempVar = UICommand.createDefaultOkUiCommand("OnExportNoTemplates", this); //$NON-NLS-1$
                confirmModel.getCommands().add(tempVar);
                UICommand tempVar2 = UICommand.createCancelUiCommand("CancelConfirmation", this); //$NON-NLS-1$
                confirmModel.getCommands().add(tempVar2);
            }
            else {
                if (model.getProgress() != null) {
                    return;
                }

                model.startProgress();

                Frontend.getInstance().runMultipleAction(VdcActionType.ExportVm, parameters,
                        new IFrontendMultipleActionAsyncCallback() {
                            @Override
                            public void executed(FrontendMultipleActionAsyncResult result) {
                                ExportVmModel localModel = (ExportVmModel) result.getState();
                                localModel.stopProgress();
                                cancel();
                            }
                        }, model);
            }
        }
        else {
            if (model.getProgress() != null) {
                return;
            }

            for (VdcActionParametersBase item : parameters) {
                MoveOrCopyParameters parameter = (MoveOrCopyParameters) item;
                parameter.setTemplateMustExists(false);
            }

            model.startProgress();

            Frontend.getInstance().runMultipleAction(VdcActionType.ExportVm, parameters,
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void executed(FrontendMultipleActionAsyncResult result) {
                            ExportVmModel localModel = (ExportVmModel) result.getState();
                            localModel.stopProgress();
                            cancel();
                        }
                    }, model);
        }
    }

    @Override
    protected void setupExportModel(ExportVmModel model) {
        super.setupExportModel(model);
        model.setTitle(constants.exportVirtualMachineTitle());
        model.setHelpTag(HelpTag.export_virtual_machine);
        model.setHashName("export_virtual_machine"); //$NON-NLS-1$
    }

    public void onExport() {
        ExportVmModel model = (ExportVmModel) getWindow();
        if (!model.validate()) {
            return;
        }

        model.startProgress();

        getTemplatesNotPresentOnExportDomain();
    }

    private void onExportNoTemplates() {
        ExportVmModel model = (ExportVmModel) getWindow();
        Guid storageDomainId = model.getStorage().getSelectedItem().getId();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VM a = (VM) item;
            MoveOrCopyParameters parameters = new MoveOrCopyParameters(a.getId(), storageDomainId);
            parameters.setForceOverride(model.getForceOverride().getEntity());
            parameters.setCopyCollapse(model.getCollapseSnapshots().getEntity());
            parameters.setTemplateMustExists(false);

            list.add(parameters);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(VdcActionType.ExportVm, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        ExportVmModel localModel = (ExportVmModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }
                }, model);
    }

    @Override
    protected void sendWarningForNonExportableDisks(VM entity) {
        // load VM disks and check if there is one which doesn't allow snapshot
        AsyncDataProvider.getInstance().getVmDiskList(new AsyncQuery<>(
                        new AsyncCallback<List<Disk>>() {
                            @Override
                            public void onSuccess(List<Disk> vmDisks) {
                                final ExportVmModel model = (ExportVmModel) getWindow();
                                VmModelHelper.sendWarningForNonExportableDisks(model,
                                        vmDisks,
                                        VmModelHelper.WarningType.VM_EXPORT);
                            }
                        }),
                entity.getId());
    }

    private void runOnce() {
        VM vm = getSelectedItem();
        // populating VMInit
        AsyncDataProvider.getInstance().getVmById(new AsyncQuery<>(new AsyncCallback<VM>() {
            @Override
            public void onSuccess(VM result) {
                RunOnceModel runOnceModel = new WebadminRunOnceModel(result, VmListModel.this);
                setWindow(runOnceModel);
                runOnceModel.init();
            }
        }), vm.getId());


    }

    private void newTemplate() {
        VM vm = getSelectedItem();
        if (vm == null || getWindow() != null) {
            return;
        }

        UnitVmModel model = new UnitVmModel(new NewTemplateVmModelBehavior(vm), this);

        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newTemplateTitle());
        model.setHelpTag(HelpTag.new_template);
        model.setHashName("new_template"); //$NON-NLS-1$
        model.setIsNew(true);
        model.getVmType().setSelectedItem(vm.getVmType());
        model.setCustomPropertiesKeysList(AsyncDataProvider.getInstance().getCustomPropertiesList());

        model.initialize(getSystemTreeSelectedItem());

        model.getCommands().add(
                new UICommand("OnNewTemplate", this) //$NON-NLS-1$
                        .setTitle(ConstantsManager.getInstance().getConstants().ok())
                        .setIsDefault(true));

        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$

        model.getIsHighlyAvailable().setEntity(vm.getStaticData().isAutoStartup());
        if (vm.getDefaultDisplayType() == DisplayType.none) {
            model.getIsHeadlessModeEnabled().setEntity(true);
        }
    }

    private void onNewTemplate() {
        UnitVmModel model = (UnitVmModel) getWindow();
        VM vm = getSelectedItem();
        if (vm == null) {
            cancel();
            return;
        }

        if (model.getProgress() != null) {
            return;
        }

        if (!model.validate(false)) {
            model.setIsValid(false);
        }
        else  if (model.getIsSubTemplate().getEntity()) {
            postNameUniqueCheck();
        }
        else {
            String name = model.getName().getEntity();

            // Check name unicitate.
            AsyncDataProvider.getInstance().isTemplateNameUnique(new AsyncQuery<>(
                    new AsyncCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean isNameUnique) {

                            if (!isNameUnique) {
                                UnitVmModel VmModel = (UnitVmModel) getWindow();
                                VmModel.getInvalidityReasons().clear();
                                VmModel.getName()
                                        .getInvalidityReasons()
                                        .add(ConstantsManager.getInstance()
                                                .getConstants()
                                                .nameMustBeUniqueInvalidReason());
                                VmModel.getName().setIsValid(false);
                                VmModel.setIsValid(false);
                                VmModel.fireValidationCompleteEvent();
                            }
                            else {
                                postNameUniqueCheck();
                            }

                        }
                    }),
                    name, model.getSelectedDataCenter() == null ? null : model.getSelectedDataCenter().getId());
        }
    }

    private void postNameUniqueCheck() {
        UnitVmModel model = (UnitVmModel) getWindow();
        VM vm = getSelectedItem();

        VM newVm = buildVmOnNewTemplate(model, vm);

        AddVmTemplateParameters addVmTemplateParameters =
                new AddVmTemplateParameters(newVm,
                        model.getName().getEntity(),
                        model.getDescription().getEntity());
        BuilderExecutor.build(model, addVmTemplateParameters, new UnitToAddVmTemplateParametersBuilder());
        model.startProgress();
        Frontend.getInstance().runAction(VdcActionType.AddVmTemplate, addVmTemplateParameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        getWindow().stopProgress();
                        VdcReturnValueBase returnValueBase = result.getReturnValue();
                        if (returnValueBase != null && returnValueBase.getSucceeded()) {
                            cancel();
                        }

                    }
                }, this);
    }

    protected static VM buildVmOnNewTemplate(UnitVmModel model, VM vm) {
        VM resultVm = new VM();
        resultVm.setId(vm.getId());
        BuilderExecutor.build(model, resultVm.getStaticData(), new CommonUnitToVmBaseBuilder());
        BuilderExecutor.build(vm.getStaticData(), resultVm.getStaticData(), new VmBaseToVmBaseForTemplateCompositeBaseBuilder());
        return resultVm;
    }

    private void migrate() {
        VM vm = getSelectedItem();
        if (vm == null) {
            return;
        }

        if (getWindow() != null) {
            return;
        }

        MigrateModel model = new MigrateModel(this);
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().migrateVirtualMachinesTitle());
        model.setHelpTag(HelpTag.migrate_virtual_machine);
        model.setHashName("migrate_virtual_machine"); //$NON-NLS-1$
        model.setVmsOnSameCluster(true);
        model.setIsAutoSelect(true);
        model.setVmList(Linq.<VM> cast(getSelectedItems()));
        model.setVm(vm);
        model.initializeModel();
    }

    private void cancelMigration() {
        ArrayList<VdcActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VM a = (VM) item;
            list.add(new VmOperationParameterBase(a.getId()));
        }

        Frontend.getInstance().runMultipleAction(VdcActionType.CancelMigrateVm, list,
                                                 new IFrontendMultipleActionAsyncCallback() {
                                                     @Override
                                                     public void executed(
                                                             FrontendMultipleActionAsyncResult result) {
                                                     }
                                                 }, null);
    }

    private void cancelConversion() {
        List<VdcActionParametersBase> parameters = new ArrayList<>();
        for (VM vm : getSelectedItems()) {
            parameters.add(new VmOperationParameterBase(vm.getId()));
        }

        Frontend.getInstance().runMultipleAction(VdcActionType.CancelConvertVm, parameters);
    }

    private void onMigrate() {
        MigrateModel model = (MigrateModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        model.startProgress();

        Guid targetClusterId = model.getClusters().getSelectedItem() != null ? model.getClusters().getSelectedItem().getId() : null;

        if (model.getIsAutoSelect()) {
            ArrayList<VdcActionParametersBase> list = new ArrayList<>();
            for (Object item : getSelectedItems()) {
                VM a = (VM) item;
                list.add(new MigrateVmParameters(true, a.getId(), targetClusterId));
            }

            Frontend.getInstance().runMultipleAction(VdcActionType.MigrateVm, list,
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void executed(FrontendMultipleActionAsyncResult result) {

                            MigrateModel localModel = (MigrateModel) result.getState();
                            localModel.stopProgress();
                            cancel();

                        }
                    }, model);
        }
        else {
            ArrayList<VdcActionParametersBase> list = new ArrayList<>();
            for (Object item : getSelectedItems()) {
                VM a = (VM) item;

                if (a.getRunOnVds().equals(model.getHosts().getSelectedItem().getId())) {
                    continue;
                }

                list.add(new MigrateVmToServerParameters(true, a.getId(), model.getHosts()
                        .getSelectedItem().getId(), targetClusterId));
            }

            Frontend.getInstance().runMultipleAction(VdcActionType.MigrateVmToServer, list,
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void executed(FrontendMultipleActionAsyncResult result) {

                            MigrateModel localModel = (MigrateModel) result.getState();
                            localModel.stopProgress();
                            cancel();

                        }
                    }, model);
        }
    }

    private void powerAction(final String actionName, final String title, final String message) {
        Guid clusterId = getClusterIdOfSelectedVms();
        if (clusterId == null) {
            powerAction(actionName, title, message, false);
        } else {
            AsyncDataProvider.getInstance().getClusterById(new AsyncQuery<>(
                new AsyncCallback<Cluster>() {
                    @Override
                    public void onSuccess(Cluster cluster) {
                        if (cluster != null) {
                            powerAction(actionName, title, message, cluster.isOptionalReasonRequired());
                        }
                    }
                }), clusterId);
        }
    }

    /**
     * Returns the cluster id if all vms are from the same cluster else returns null.
     */
    private Guid getClusterIdOfSelectedVms() {
        Guid clusterId = null;
        for (Object item : getSelectedItems()) {
            VM a = (VM) item;
            if (clusterId == null) {
                clusterId = a.getClusterId();
            } else if (!clusterId.equals(a.getClusterId())) {
                clusterId = null;
                break;
            }
        }
        return clusterId;
    }

    private void powerAction(String actionName, String title, String message, boolean reasonVisible) {
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(title);
        model.setReasonVisible(reasonVisible);

        if (actionName.equals(SHUTDOWN)) {
            model.setHelpTag(HelpTag.shutdown_virtual_machine);
            model.setHashName("shutdown_virtual_machine"); //$NON-NLS-1$
        }
        else if (actionName.equals(STOP)) {
            model.setHelpTag(HelpTag.stop_virtual_machine);
            model.setHashName("stop_virtual_machine"); //$NON-NLS-1$
        }
        else if (actionName.equals(REBOOT)) {
            model.setHelpTag(HelpTag.reboot_virtual_machine);
            model.setHashName("reboot_virtual_machine"); //$NON-NLS-1$
        }

        model.setMessage(message);
        ArrayList<String> items = new ArrayList<>();
        boolean stoppingSingleVM = getSelectedItems().size() == 1 &&
                (actionName.equals(SHUTDOWN) || actionName.equals(STOP));
        for (Object item : getSelectedItems()) {
            VM vm = (VM) item;
            items.add(vm.getName());
            // If a single VM in status PoweringDown is being stopped the reason field
            // is populated with the current reason so the user can edit it.
            if (stoppingSingleVM && reasonVisible && VMStatus.PoweringDown.equals(vm.getStatus())) {
                model.getReason().setEntity(vm.getStopReason());
            }
        }
        model.setItems(items);

        UICommand tempVar = new UICommand("On" + actionName, this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    private interface PowerActionParametersFactory<P extends VdcActionParametersBase> {
        P createActionParameters(VM vm);
    }

    private void onPowerAction(VdcActionType actionType, PowerActionParametersFactory<?> parametersFactory) {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }


        ArrayList<VdcActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VM vm = (VM) item;
            list.add(parametersFactory.createActionParameters(vm));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(actionType, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }
                }, model);

    }

    private void shutdown() {
        UIConstants constants = ConstantsManager.getInstance().getConstants();
        powerAction(SHUTDOWN,
                constants.shutdownVirtualMachinesTitle(),
                constants.areYouSureYouWantToShutDownTheFollowingVirtualMachinesMsg());
    }

    private void onShutdown() {
        final ConfirmationModel model = (ConfirmationModel) getWindow();
        onPowerAction(VdcActionType.ShutdownVm, new PowerActionParametersFactory<VdcActionParametersBase>() {
            @Override
            public VdcActionParametersBase createActionParameters(VM vm) {
                return new ShutdownVmParameters(vm.getId(), true, model.getReason().getEntity());
            }
        });
    }

    private void stop() {
        UIConstants constants = ConstantsManager.getInstance().getConstants();
        powerAction(STOP,
                constants.stopVirtualMachinesTitle(),
                constants.areYouSureYouWantToStopTheFollowingVirtualMachinesMsg());
    }

    private void onStop() {
        final ConfirmationModel model = (ConfirmationModel) getWindow();
        onPowerAction(VdcActionType.StopVm, new PowerActionParametersFactory<VdcActionParametersBase>() {
            @Override
            public VdcActionParametersBase createActionParameters(VM vm) {
                return new StopVmParameters(vm.getId(), StopVmTypeEnum.NORMAL, model.getReason().getEntity());
            }
        });
    }

    private void reboot() {
        UIConstants constants = ConstantsManager.getInstance().getConstants();
        powerAction(REBOOT,
                constants.rebootVirtualMachinesTitle(),
                constants.areYouSureYouWantToRebootTheFollowingVirtualMachinesMsg(),
                false);
    }

    private void onReboot() {
        onPowerAction(VdcActionType.RebootVm, new PowerActionParametersFactory<VdcActionParametersBase>() {
            @Override
            public VdcActionParametersBase createActionParameters(VM vm) {
                return new VmOperationParameterBase(vm.getId());
            }
        });
    }

    private void pause() {
        ArrayList<VdcActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VM a = (VM) item;
            list.add(new VmOperationParameterBase(a.getId()));
        }

        Frontend.getInstance().runMultipleAction(VdcActionType.HibernateVm, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                    }
                }, null);
    }

    private void run() {
        ArrayList<VdcActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VM a = (VM) item;
            list.add(new RunVmParams(a.getId()));
        }

        Frontend.getInstance().runMultipleAction(VdcActionType.RunVm, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                    }
                }, null);
    }

    private void onRemove() {
        final ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        final ArrayList<VdcActionParametersBase> list = new ArrayList<>();
        for (Entry<Guid, EntityModel> entry : vmsRemoveMap.entrySet()) {
            list.add(new RemoveVmParameters(entry.getKey(), false, (Boolean) entry.getValue().getEntity()));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(VdcActionType.RemoveVm, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();
                    }
                }, model);
    }

    private void changeCD() {
        final VM vm = getSelectedItem();
        if (vm == null) {
            return;
        }

        AttachCdModel model = new AttachCdModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().changeCDTitle());
        model.setHelpTag(HelpTag.change_cd);
        model.setHashName("change_cd"); //$NON-NLS-1$

        AttachCdModel attachCdModel = (AttachCdModel) getWindow();
        ArrayList<String> images1 =
                new ArrayList<>(Arrays.asList(new String[] { ConstantsManager.getInstance()
                        .getConstants()
                        .noCds() }));
        attachCdModel.getIsoImage().setItems(images1);
        attachCdModel.getIsoImage().setSelectedItem(Linq.firstOrNull(images1));

        AsyncDataProvider.getInstance().getIrsImageList(new AsyncQuery<>(new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> images) {
                AttachCdModel _attachCdModel = (AttachCdModel) getWindow();
                images.add(0, ConsoleModel.getEjectLabel());
                _attachCdModel.getIsoImage().setItems(images);
                if (_attachCdModel.getIsoImage().getIsChangable()) {
                    String selectedIso = Linq.firstOrNull(images, new Linq.IPredicate<String>() {
                        @Override
                        public boolean match(String s) {
                            return vm.getCurrentCd().equals(s);
                        }
                    });
                    _attachCdModel.getIsoImage().setSelectedItem(selectedIso == null ? ConsoleModel.getEjectLabel() : selectedIso);
                }
            }
        }), vm.getStoragePoolId());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnChangeCD", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    private void onChangeCD() {
        VM vm = getSelectedItem();
        if (vm == null) {
            cancel();
            return;
        }

        AttachCdModel model = (AttachCdModel) getWindow();
        if (model.getProgress() != null) {
            return;
        }

        if (Objects.equals(model.getIsoImage().getSelectedItem(), vm.getCurrentCd())) {
            cancel();
            return;
        }

        String isoName =
                Objects.equals(model.getIsoImage().getSelectedItem(), ConsoleModel.getEjectLabel()) ? "" //$NON-NLS-1$
                        : model.getIsoImage().getSelectedItem();

        model.startProgress();

        Frontend.getInstance().runAction(VdcActionType.ChangeDisk, new ChangeDiskCommandParameters(vm.getId(), isoName),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {

                        AttachCdModel attachCdModel = (AttachCdModel) result.getState();
                        attachCdModel.stopProgress();
                        cancel();

                    }
                }, model);
    }

    private void preSave() {
        final UnitVmModel model = (UnitVmModel) getWindow();

        if (model.getIsNew() == false && selectedItem == null) {
            cancel();
            return;
        }

        setcurrentVm(model.getIsNew() ? new VM() : (VM) Cloner.clone(selectedItem));

        String selectedCpu = model.getCustomCpu().getSelectedItem();
        if (selectedCpu != null && !selectedCpu.isEmpty()  && !model.getCustomCpu().getItems().contains(selectedCpu)) {
            confirmCustomCpu("PreSavePhase2"); //$NON-NLS-1$
        } else {
            preSavePhase2();
        }
    }

    private void confirmCustomCpu(String phase2UiCommand) {
        ConfirmationModel confirmModel = new ConfirmationModel();
        confirmModel.setTitle(ConstantsManager.getInstance().getConstants().vmUnsupportedCpuTitle());
        confirmModel.setMessage(ConstantsManager.getInstance().getConstants().vmUnsupportedCpuMessage());
        confirmModel.setHelpTag(HelpTag.edit_unsupported_cpu);
        confirmModel.setHashName("edit_unsupported_cpu"); //$NON-NLS-1$

        confirmModel.getCommands().add(new UICommand(phase2UiCommand, VmListModel.this)
                .setTitle(ConstantsManager.getInstance().getConstants().ok())
                .setIsDefault(true));

        confirmModel.getCommands().add(UICommand.createCancelUiCommand("CancelConfirmation", VmListModel.this)); //$NON-NLS-1$

        setConfirmWindow(confirmModel);
    }

    private void preSavePhase2() {
        final UnitVmModel model = (UnitVmModel) getWindow();

        EntityModel<String> cpuPinning = model.getCpuPinning();
        if (!cpuPinning.getIsChangable() && cpuPinning.getEntity() != null
                && !cpuPinning.getEntity().isEmpty()) {
            confirmCpuPinningLost();
        } else {
            preSavePhase3();
        }
    }

    private void confirmCpuPinningLost() {
        ConfirmationModel confirmModel = new ConfirmationModel();
        confirmModel.setTitle(ConstantsManager.getInstance().getConstants().vmCpuPinningClearTitle());
        confirmModel.setMessage(ConstantsManager.getInstance().getConstants().vmCpuPinningClearMessage());

        confirmModel.setHelpTag(HelpTag.edit_unsupported_cpu);
        confirmModel.setHashName("edit_clear_cpu_pinning"); //$NON-NLS-1$

        confirmModel.getCommands().add(UICommand.createDefaultOkUiCommand("ClearCpuPinning", VmListModel.this)); //$NON-NLS-1$
        confirmModel.getCommands().add(UICommand.createCancelUiCommand("CancelConfirmation", VmListModel.this)); //$NON-NLS-1$

        setConfirmWindow(confirmModel);
    }

    private void clearCpuPinning() {
        final UnitVmModel model = (UnitVmModel) getWindow();
        model.getCpuPinning().setEntity("");

        preSavePhase3();
    }

    private void preSavePhase3() {
        final UnitVmModel model = (UnitVmModel) getWindow();
        final String name = model.getName().getEntity();

        if (!model.getIsNew() && model.getNumaEnabled().getEntity() &&
                (!model.getMemSize().getEntity().equals(getcurrentVm().getMemSizeMb()) ||
                !model.getTotalCPUCores().getEntity().equals(Integer.toString(getcurrentVm().getNumOfCpus())) )) {
            model.setNumaChanged(true);
        }

        validateVm(model, name);
    }

    @Override
    protected void updateVM (final UnitVmModel model){
        final VM selectedItem = getSelectedItem();
        // explicitly pass non-editable field from the original VM
        getcurrentVm().setCreatedByUserId(selectedItem.getCreatedByUserId());
        getcurrentVm().setUseLatestVersion(model.getTemplateWithVersion().getSelectedItem().isLatest());

        if (selectedItem.isRunningOrPaused() && !selectedItem.isHostedEngine()) {
            AsyncDataProvider.getInstance().getVmChangedFieldsForNextRun(editedVm, getcurrentVm(), getUpdateVmParameters(false), new AsyncQuery<>(
                    new AsyncCallback<VdcQueryReturnValue>() {
                @Override
                public void onSuccess(VdcQueryReturnValue returnValue) {
                    List<String> changedFields = returnValue.getReturnValue();
                    final boolean cpuHotPluggable = VmCommonUtils.isCpusToBeHotplugged(selectedItem, getcurrentVm());
                    final boolean isHeadlessModeChanged = isHeadlessModeChanged(editedVm, getUpdateVmParameters(false));
                    final boolean isMemoryHotUnplugSupported =
                            AsyncDataProvider.getInstance().isMemoryHotUnplugSupported(getcurrentVm());
                    final boolean memoryHotPluggable =
                            VmCommonUtils.isMemoryToBeHotplugged(selectedItem, getcurrentVm(), isMemoryHotUnplugSupported);
                    if (isHeadlessModeChanged) {
                        changedFields.add(constants.headlessMode());
                    }

                    // provide warnings if isVmUnpinned()
                    if (!changedFields.isEmpty() || isVmUnpinned() || memoryHotPluggable || cpuHotPluggable) {
                        VmNextRunConfigurationModel confirmModel = new VmNextRunConfigurationModel();
                        if (isVmUnpinned()) {
                            confirmModel.setVmUnpinned();
                        }
                        confirmModel.setTitle(ConstantsManager.getInstance().getConstants().editNextRunConfigurationTitle());
                        confirmModel.setHelpTag(HelpTag.edit_next_run_configuration);
                        confirmModel.setHashName("edit_next_run_configuration"); //$NON-NLS-1$
                        confirmModel.setChangedFields(changedFields);
                        confirmModel.setCpuPluggable(cpuHotPluggable);
                        confirmModel.setMemoryPluggable(memoryHotPluggable);

                        confirmModel.getCommands().add(new UICommand("updateExistingVm", VmListModel.this) //$NON-NLS-1$
                        .setTitle(ConstantsManager.getInstance().getConstants().ok())
                        .setIsDefault(true));

                        confirmModel.getCommands().add(UICommand.createCancelUiCommand("CancelConfirmation", VmListModel.this)); //$NON-NLS-1$

                        setConfirmWindow(confirmModel);
                    }
                    else {
                        updateExistingVm(false);
                    }
                }

                private boolean isVmUnpinned() {
                    if (selectedItem.isRunning()) {
                        if (selectedItem.getMigrationSupport() == MigrationSupport.PINNED_TO_HOST
                            && getcurrentVm().getMigrationSupport() != MigrationSupport.PINNED_TO_HOST) {
                            return true;
                        }
                    }
                    return false;
                }
            }));
        }
        else {
            updateExistingVm(false);
        }
    }

    private boolean isHeadlessModeChanged(VM source, VmManagementParametersBase updateVmParameters) {
        return source.getDefaultDisplayType() != updateVmParameters.getVmStaticData().getDefaultDisplayType()
                && (source.getDefaultDisplayType() == DisplayType.none
                || updateVmParameters.getVmStaticData().getDefaultDisplayType() == DisplayType.none);
    }

    private void updateExistingVm(final boolean applyCpuChangesLater) {
        final UnitVmModel model = (UnitVmModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        // runEditVM: should be true if Cluster hasn't changed or if
        // Cluster has changed and Editing it in the Backend has succeeded:
        VM selectedItem = getSelectedItem();
        Guid oldClusterID = selectedItem.getClusterId();
        Guid newClusterID = model.getSelectedCluster().getId();
        if (oldClusterID.equals(newClusterID) == false) {
            ChangeVMClusterParameters parameters =
                    new ChangeVMClusterParameters(
                            newClusterID,
                            getcurrentVm().getId(),
                            model.getCustomCompatibilityVersion().getSelectedItem());

            model.startProgress();

            Frontend.getInstance().runAction(VdcActionType.ChangeVMCluster, parameters,
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void executed(FrontendActionAsyncResult result) {

                            final VmListModel<Void> vmListModel = (VmListModel<Void>) result.getState();
                            VdcReturnValueBase returnValueBase = result.getReturnValue();
                            if (returnValueBase != null && returnValueBase.getSucceeded()) {
                                VM vm = vmListModel.getcurrentVm();
                                VmManagementParametersBase updateVmParams = vmListModel.getUpdateVmParameters(applyCpuChangesLater);
                                Frontend.getInstance().runAction(VdcActionType.UpdateVm,
                                        updateVmParams, new UnitVmModelNetworkAsyncCallback(model, defaultNetworkCreatingManager, vm.getId()), vmListModel);
                            }
                            else {
                                vmListModel.getWindow().stopProgress();
                            }

                        }
                    },
                    this);
        }
        else {
            model.startProgress();
            VmManagementParametersBase updateVmParams = getUpdateVmParameters(applyCpuChangesLater);
            Frontend.getInstance().runAction(VdcActionType.UpdateVm, updateVmParams, new UnitVmModelNetworkAsyncCallback(model, defaultNetworkCreatingManager, getcurrentVm().getId()), this);
        }
    }

    public VmManagementParametersBase getUpdateVmParameters(boolean applyCpuChangesLater) {
        UnitVmModel model = (UnitVmModel) getWindow();
        VmManagementParametersBase updateVmParams = new VmManagementParametersBase(getcurrentVm());

        setVmWatchdogToParams(model, updateVmParams);
        updateVmParams.setSoundDeviceEnabled(model.getIsSoundcardEnabled().getEntity());
        updateVmParams.setConsoleEnabled(model.getIsConsoleDeviceEnabled().getEntity());
        updateVmParams.setBalloonEnabled(balloonEnabled(model));
        updateVmParams.setVirtioScsiEnabled(model.getIsVirtioScsiEnabled().getEntity());
        updateVmParams.setApplyChangesLater(applyCpuChangesLater);
        updateVmParams.setUpdateNuma(model.isNumaChanged());
        if (model.getIsHeadlessModeEnabled().getEntity()) {
            updateVmParams.getVmStaticData().setDefaultDisplayType(DisplayType.none);
        }
        BuilderExecutor.build(
                new Pair<>((UnitVmModel) getWindow(), getSelectedItem()),
                updateVmParams,
                new VmIconUnitAndVmToParameterBuilder());
        setRngDeviceToParams(model, updateVmParams);
        BuilderExecutor.build(model, updateVmParams, new UnitToGraphicsDeviceParamsBuilder());

        return updateVmParams;
    }

    private void retrieveIsoImages() {
        Object tempVar = getSelectedItem();
        VM vm = (VM) ((tempVar instanceof VM) ? tempVar : null);
        if (vm == null) {
            return;
        }

        getIsoImages().clear();

        ChangeCDModel tempVar2 = new ChangeCDModel();
        tempVar2.setTitle(ConsoleModel.getEjectLabel());
        ChangeCDModel ejectModel = tempVar2;
        ejectModel.getExecutedEvent().addListener(this);
        getIsoImages().add(ejectModel);

        ChangeCDModel tempVar4 = new ChangeCDModel();
        tempVar4.setTitle(ConstantsManager.getInstance().getConstants().noCds());
        getIsoImages().add(tempVar4);
    }

    private void changeCD(Object sender, EventArgs e) {
        ChangeCDModel model = (ChangeCDModel) sender;

        // TODO: Patch!
        String isoName = model.getTitle();
        if (Objects.equals(isoName, ConstantsManager.getInstance()
                .getConstants()
                .noCds())) {
            return;
        }

        Object tempVar = getSelectedItem();
        VM vm = (VM) ((tempVar instanceof VM) ? tempVar : null);
        if (vm == null) {
            return;
        }

        Frontend.getInstance().runMultipleAction(VdcActionType.ChangeDisk,
                new ArrayList<>(Arrays.asList(new VdcActionParametersBase[] { new ChangeDiskCommandParameters(vm.getId(),
                        Objects.equals(isoName, ConsoleModel.getEjectLabel()) ? "" : isoName) })), //$NON-NLS-1$
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                    }
                },
                null);
    }

    @Override
    public void cancel() {
        cancelConfirmation();

        setGuideContext(null);
        setWindow(null);

        updateActionsAvailability();
    }

    private void cancelConfirmation() {
        setConfirmWindow(null);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();

        updateActionsAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();

        updateActionsAvailability();
    }

    @Override
    protected void selectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.selectedItemPropertyChanged(sender, e);

        if (e.propertyName.equals("status")) { //$NON-NLS-1$
            updateActionsAvailability();
        }
    }

    @Override
    protected void updateActionsAvailability() {
        List items = getSelectedItems() != null && getSelectedItem() != null ? getSelectedItemsWithStatusForExclusiveLock() : new ArrayList();

        boolean singleVmSelected = items.size() == 1;
        boolean vmsSelected = items.size() > 0;

        getEditCommand().setIsExecutionAllowed(isEditCommandExecutionAllowed(items));
        getRemoveCommand().setIsExecutionAllowed(vmsSelected
                && VdcActionUtils.canExecutePartially(items, VmWithStatusForExclusiveLock.class, VdcActionType.RemoveVm));
        getRunCommand().setIsExecutionAllowed(vmsSelected
                && VdcActionUtils.canExecutePartially(items, VmWithStatusForExclusiveLock.class, VdcActionType.RunVm));
        getCloneVmCommand().setIsExecutionAllowed(singleVmSelected
                && VdcActionUtils.canExecute(items, VmWithStatusForExclusiveLock.class, VdcActionType.CloneVm));
        getPauseCommand().setIsExecutionAllowed(vmsSelected
                && VdcActionUtils.canExecutePartially(items, VmWithStatusForExclusiveLock.class, VdcActionType.HibernateVm));
        getShutdownCommand().setIsExecutionAllowed(vmsSelected
                && VdcActionUtils.canExecutePartially(items, VmWithStatusForExclusiveLock.class, VdcActionType.ShutdownVm));
        getStopCommand().setIsExecutionAllowed(vmsSelected
                && VdcActionUtils.canExecutePartially(items, VmWithStatusForExclusiveLock.class, VdcActionType.StopVm));
        getRebootCommand().setIsExecutionAllowed(AsyncDataProvider.getInstance().isRebootCommandExecutionAllowed(items));
        getMigrateCommand().setIsExecutionAllowed(vmsSelected
                && VdcActionUtils.canExecutePartially(items, VmWithStatusForExclusiveLock.class, VdcActionType.MigrateVm));
        getCancelMigrateCommand().setIsExecutionAllowed(vmsSelected
                && VdcActionUtils.canExecutePartially(items, VmWithStatusForExclusiveLock.class, VdcActionType.CancelMigrateVm));
        getNewTemplateCommand().setIsExecutionAllowed(singleVmSelected
                && VdcActionUtils.canExecute(items, VmWithStatusForExclusiveLock.class, VdcActionType.AddVmTemplate));
        getRunOnceCommand().setIsExecutionAllowed(singleVmSelected
                && VdcActionUtils.canExecute(items, VmWithStatusForExclusiveLock.class, VdcActionType.RunVmOnce));
        getExportCommand().setIsExecutionAllowed(vmsSelected
                && VdcActionUtils.canExecute(items, VmWithStatusForExclusiveLock.class, VdcActionType.ExportVm));
        getCreateSnapshotCommand().setIsExecutionAllowed(singleVmSelected
                && !getSelectedItem().isStateless() && !getSelectedItem().isPreviewSnapshot()
                && VdcActionUtils.canExecute(items, VmWithStatusForExclusiveLock.class, VdcActionType.CreateAllSnapshotsFromVm));
        getRetrieveIsoImagesCommand().setIsExecutionAllowed(singleVmSelected
                && VdcActionUtils.canExecute(items, VmWithStatusForExclusiveLock.class, VdcActionType.ChangeDisk));
        getChangeCdCommand().setIsExecutionAllowed(singleVmSelected
                && VdcActionUtils.canExecute(items, VmWithStatusForExclusiveLock.class, VdcActionType.ChangeDisk));
        getAssignTagsCommand().setIsExecutionAllowed(vmsSelected);

        getGuideCommand().setIsExecutionAllowed(getGuideContext() != null || singleVmSelected);

        getConsoleConnectCommand().setIsExecutionAllowed(isConsoleCommandsExecutionAllowed());
        getEditConsoleCommand().setIsExecutionAllowed(singleVmSelected && isConsoleEditEnabled());
        getCancelConvertCommand().setIsExecutionAllowed(isSelectedVmBeingConverted());
    }

    private List<VmWithStatusForExclusiveLock> getSelectedItemsWithStatusForExclusiveLock() {
        List<VmWithStatusForExclusiveLock> vmsWithStatusForExclusive = new ArrayList<>();
        for (VM vm : getSelectedItems()) {
            vmsWithStatusForExclusive.add(new VmWithStatusForExclusiveLock(vm));
        }
        return vmsWithStatusForExclusive;
    }

    private boolean isSelectedVmBeingConverted() {
        List<VM> vms = getSelectedItems();
        if (vms != null) {
            for (VM vm : vms) {
                int conversionProgress = vm.getBackgroundOperationProgress();
                if (conversionProgress >= 0 && conversionProgress < 100) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isConsoleEditEnabled() {
        return getSelectedItem() != null && getSelectedItem().isRunningOrPaused();
    }

    private boolean isConsoleCommandsExecutionAllowed() {
        final List<VM> list = getSelectedItem() == null ? null : getSelectedItems();
        if (list == null) {
            return false;
        }

        // return true, if at least one console is available
        for (VM vm : list) {
            if (consolesFactory.getVmConsolesForVm(vm).canConnectToConsole()) {
                return true;
            }
        }

        return false;
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
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ChangeCDModel.executedEventDefinition)) {
            changeCD(sender, args);
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewVmCommand()) {
            newVm();
        } else if (command == getImportVmCommand()) {
            importVms();
        } else if (command == getCloneVmCommand()) {
            cloneVm();
        } else if (command == getEditCommand()) {
            edit();
        }
        else if (command == getEditConsoleCommand()) {
            editConsole();
        }
        else if (command == getConsoleConnectCommand()) {
            connectToConsoles();
        }
        else if (command == getRemoveCommand()) {
            remove();
        }
        else if (command == getRunCommand()) {
            run();
        }
        else if (command == getPauseCommand()) {
            pause();
        } else if (command == getStopCommand()) {
            stop();
        } else if (command == getShutdownCommand()) {
            shutdown();
        }
        else if (command == getRebootCommand()) {
            reboot();
        } else if (command == getMigrateCommand()) {
            migrate();
        }
        else if (command == getNewTemplateCommand()) {
            newTemplate();
        } else if (command == getRunOnceCommand()) {
            runOnce();
        } else if (command == getExportCommand()) {
            export();
        }
        else if (command == getCreateSnapshotCommand()) {
            createSnapshot();
        }
        else if (command == getGuideCommand()) {
            guide();
        }
        else if (command == getRetrieveIsoImagesCommand()) {
            retrieveIsoImages();
        }
        else if (command == getChangeCdCommand()) {
            changeCD();
        }
        else if (command == getAssignTagsCommand()) {
            assignTags();
        }
        else if ("OnAssignTags".equals(command.getName())) { //$NON-NLS-1$
            onAssignTags();
        }
        else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
        else if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            preSave();
        }
        else if ("PreSavePhase2".equals(command.getName())) { //$NON-NLS-1$
            preSavePhase2();
        }
        else if ("PreSavePhase3".equals(command.getName())) { //$NON-NLS-1$
            preSavePhase3();
            cancelConfirmation();
        }
        else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        }
        else if ("OnClone".equals(command.getName())) { //$NON-NLS-1$
            onClone();
        }
        else if ("OnExport".equals(command.getName())) { //$NON-NLS-1$
            onExport();
        }
        else if ("OnExportNoTemplates".equals(command.getName())) { //$NON-NLS-1$
            onExportNoTemplates();
        }
        else if ("CancelConfirmation".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirmation();
        }
        else if ("OnRunOnce".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
        else if ("OnNewTemplate".equals(command.getName())) { //$NON-NLS-1$

            onNewTemplate();
        }
        else if ("OnMigrate".equals(command.getName())) { //$NON-NLS-1$
            onMigrate();
        }
        else if (command == getCancelMigrateCommand()) {
            cancelMigration();
        }
        else if (command == getCancelConvertCommand()) {
            cancelConversion();
        }
        else if ("OnShutdown".equals(command.getName())) { //$NON-NLS-1$
            onShutdown();
        }
        else if ("OnStop".equals(command.getName())) { //$NON-NLS-1$
            onStop();
        }
        else if ("OnReboot".equals(command.getName())) { //$NON-NLS-1$
            onReboot();
        }
        else if ("OnChangeCD".equals(command.getName())) { //$NON-NLS-1$
            onChangeCD();
        }
        else if (command.getName().equals("closeVncInfo") || // $NON-NLS-1$
                "OnEditConsoleSave".equals(command.getName())) { //$NON-NLS-1$
            setWindow(null);
        }
        else if ("updateExistingVm".equals(command.getName())) { // $NON-NLS-1$
            VmNextRunConfigurationModel model = (VmNextRunConfigurationModel) getConfirmWindow();
            if (!model.validate()) {
                return;
            }

            updateExistingVm(model.getApplyLater().getEntity());
            cancelConfirmation();
        }
        else if ("ClearCpuPinning".equals(command.getName())) { // $NON-NLS-1$
            clearCpuPinning();
        }
        else if (CMD_CONFIGURE_VMS_TO_IMPORT.equals(command.getName())) {
            onConfigureVmsToImport();
        }
    }

    private void importVms() {
        if (getWindow() != null) {
            return;
        }

        final ImportVmsModel model = importVmsModelProvider.get();
        model.init();
        setWindow(model);

        model.getCommands().add(new UICommand(CMD_CONFIGURE_VMS_TO_IMPORT, this)
        .setIsExecutionAllowed(false)
        .setTitle(ConstantsManager.getInstance().getConstants().next())
        .setIsDefault(true)
        );

        model.getCommands().add(new UICommand(CMD_CANCEL, this)
        .setTitle(ConstantsManager.getInstance().getConstants().cancel())
        .setIsCancel(true)
        );

        model.initImportModels(
                new UICommand(CMD_IMPORT, new BaseCommandTarget() {
                    @Override
                    public void executeCommand(UICommand uiCommand) {
                        model.onRestoreVms(
                                new IFrontendMultipleActionAsyncCallback() {
                                    @Override
                                    public void executed(FrontendMultipleActionAsyncResult result) {
                                        boolean isAllValidatePassed = true;
                                        for (VdcReturnValueBase returnValueBase : result.getReturnValue()) {
                                            if (!returnValueBase.isValid()) {
                                                isAllValidatePassed = false;
                                                break;
                                            }
                                        }
                                        if (isAllValidatePassed) {
                                            setWindow(null);
                                        }
                                    }
                                });
                    }
                }).setTitle(ConstantsManager.getInstance().getConstants().ok())
                  .setIsDefault(true)
                ,
                new UICommand(CMD_BACK, new BaseCommandTarget() {
                    @Override
                    public void executeCommand(UICommand uiCommand) {
                        setWindow(null); // remove current window first
                        model.clearVmModelsExceptItems();
                        setWindow(model);
                    }
                }).setTitle(ConstantsManager.getInstance().getConstants().back())
                ,
                new UICommand(CMD_CANCEL, this).setIsCancel(true)
                    .setTitle(ConstantsManager.getInstance().getConstants().cancel())
                );
    }

    private void cloneVm() {
        final VM vm = getSelectedItem();
        if (vm == null) {
            return;
        }

        CloneVmModel model = new CloneVmModel(vm, constants);
        setWindow(model);

        model.initialize();
        model.setTitle(ConstantsManager.getInstance()
                .getConstants().cloneVmTitle());

        model.setHelpTag(HelpTag.clone_vm);
        model.setHashName("clone_vm"); //$NON-NLS-1$

        UICommand okCommand = UICommand.createDefaultOkUiCommand("OnClone", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    private void onClone() {
        ((CloneVmModel) getWindow()).onClone(this, false);
    }

    private void onConfigureVmsToImport() {
        final ImportVmsModel importVmsModel = (ImportVmsModel) getWindow();
        if (importVmsModel == null) {
            return;
        }

        boolean vmsToImportHaveFullInfo = importVmsModel.vmsToImportHaveFullInfo();


        if (vmsToImportHaveFullInfo && !importVmsModel.validateArchitectures(importVmsModel.getVmsToImport())) {
            return;
        }

        final ImportVmModel model = importVmsModel.getSpecificImportModel(vmsToImportHaveFullInfo);

        if (vmsToImportHaveFullInfo) {
            setWindow(null); // remove import-vms window first
            setWindow(model);
        } else {
            initImportModelForVmsToImportNamesOnly(importVmsModel, model);
        }
    }

    private void initImportModelForVmsToImportNamesOnly(final ImportVmsModel importVmsModel, final ImportVmModel importVmModel) {
        final UIMessages messages = ConstantsManager.getInstance().getMessages();
        final UIConstants constants = ConstantsManager.getInstance().getConstants();
        final List<String> vmsToImport = new ArrayList<>();
        OriginType originType = convertImportSourceToOriginType(importVmsModel.getImportSources().getSelectedItem());

        final List<VM> externalVms = importVmsModel.getVmsToImport();
        for (VM vm : externalVms) {
            vmsToImport.add(vm.getName());
        }

        importVmsModel.clearProblem();
        importVmsModel.startProgress();
        importVmModel.setMessage("");

        AsyncQuery query = new AsyncQuery(new AsyncCallback() {
            @Override
            public void onSuccess(Object returnValue) {
                if (returnValue instanceof VdcQueryReturnValue) {
                    importVmsModel.setError(messages.providerFailure());
                    importVmsModel.stopProgress();
                }
                else {
                    List<VM> remoteVms = (List<VM>) returnValue;
                    List<VM> remoteDownVms = new ArrayList<>();
                    List<VM> nonRetrievedVms = new ArrayList<>();
                    // find vms with status=down
                    for (VM vm : remoteVms) {
                        if (vm.isDown()) {
                            remoteDownVms.add(vm);
                        }
                    }
                    // find vms which have some kind of a problem retrieving them with their full info
                    // i.e. they were retrieved with their names only but not with their full info
                    if (remoteVms.size() != externalVms.size()) {
                        for (VM vm : externalVms) {
                            if (!remoteVms.contains(vm)) {
                                nonRetrievedVms.add(vm);
                            }
                        }
                    }

                    importVmsModel.stopProgress();

                    // prepare error message to be displayed in one of the models
                    String messageForImportVm = null;
                    String messageForImportVms = null;
                    if (remoteVms.size() != remoteDownVms.size()) {
                        if (!nonRetrievedVms.isEmpty()) {
                            messageForImportVm = constants.nonRetrievedAndRunningVmsWereFilteredOnImportVm();
                            messageForImportVms = constants.nonRetrievedAndRunningVmsWereAllFilteredOnImportVm();
                        } else {
                            messageForImportVm = constants.runningVmsWereFilteredOnImportVm();
                            messageForImportVms = constants.runningVmsWereAllFilteredOnImportVm();
                        }
                    } else if (!nonRetrievedVms.isEmpty()) {
                        messageForImportVm = constants.nonRetrievedVmsWereFilteredOnImportVm();
                        messageForImportVms = constants.nonRetrievedVmsWereAllFilteredOnImportVm();
                    }

                    if (remoteDownVms.isEmpty() && messageForImportVms != null) {
                        importVmsModel.setError(messageForImportVms);
                    }

                    if (!importVmsModel.validateArchitectures(remoteDownVms)) {
                        return;
                    }

                    // init and display next dialog - the importVmsModel model
                    importVmModel.init(remoteDownVms, importVmsModel.getDataCenters().getSelectedItem().getId());
                    setWindow(null);
                    setWindow(importVmModel);
                    if (messageForImportVm != null) {
                        importVmModel.setMessage(messageForImportVm);
                    }
                }
            }
        });

        if (!(importVmModel instanceof ImportVmFromExternalSourceModel)) {
            importVmsModel.setError(messages.providerImportFailure());
            importVmsModel.stopProgress();
            return;
        }
        ImportVmFromExternalSourceModel importVmsFromExternalSource = (ImportVmFromExternalSourceModel) importVmModel;

        query.setHandleFailure(true);
        AsyncDataProvider.getInstance().getVmsFromExternalServer(
                query,
                importVmsModel.getDataCenters().getSelectedItem().getId(),
                importVmsFromExternalSource.getProxyHostId(),
                importVmsFromExternalSource.getUrl(),
                importVmsFromExternalSource.getUsername(),
                importVmsFromExternalSource.getPassword(),
                originType,
                vmsToImport
        );
    }

    private OriginType convertImportSourceToOriginType(ImportSource importSource) {
        OriginType originType;
        switch(importSource) {
            case VMWARE:
                originType = OriginType.VMWARE;
                break;
            case KVM:
                originType = OriginType.KVM;
                break;
            case XEN:
                originType = OriginType.XEN;
                break;
            default:
                originType = OriginType.EXTERNAL;
        }
        return originType;
    }

    private void connectToConsoles() {
        StringBuilder errorMessages = null;

        final List<VM> list = getSelectedItems();
        if (list == null || list.isEmpty()) {
            return;
        }

        for (VM vm : list) {
            try {
                consolesFactory.getVmConsolesForVm(vm).connect();
            } catch (VmConsoles.ConsoleConnectException e) {
                final String errorMessage = e.getLocalizedErrorMessage();
                if (errorMessage != null) {
                    if (errorMessages == null) {
                        errorMessages = new StringBuilder();
                    } else {
                        errorMessages.append("\r\n"); //$NON-NLS-1$
                    }

                    errorMessages
                            .append(vm.getName())
                            .append(" - ") //$NON-NLS-1$
                            .append(errorMessage);
                }
            }
        }

        if (errorMessages != null) {
            errorPopupManager.show(errorMessages.toString());
        }
    }

    private SystemTreeItemModel systemTreeSelectedItem;

    @Override
    public SystemTreeItemModel getSystemTreeSelectedItem() {
        return systemTreeSelectedItem;
    }

    @Override
    public void setSystemTreeSelectedItem(SystemTreeItemModel value) {
        systemTreeSelectedItem = value;
        onPropertyChanged(new PropertyChangedEventArgs("SystemTreeSelectedItem")); //$NON-NLS-1$
    }

    @Override
    protected String getListName() {
        return "VmListModel"; //$NON-NLS-1$
    }

    @Override
    public boolean supportsServerSideSorting() {
        return true;
    }

    @Override
    protected Guid extractStoragePoolIdNullSafe(VM entity) {
        return entity.getStoragePoolId();
    }

}
