package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.ActionUtils;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.CloneVmParameters;
import org.ovirt.engine.core.common.action.ExportVmToOvaParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.RebootVmParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.action.VmInterfacesModifyParameters;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VmWithStatusForExclusiveLock;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
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
import org.ovirt.engine.ui.uicommonweb.TagAssigningModel;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.template.UnitToAddVmTemplateParametersBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.template.VmBaseToVmBaseForTemplateCompositeBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CommonUnitToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.UnitToGraphicsDeviceParamsBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.VmIconUnitAndVmToParameterBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.dataprovider.ImagesDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModelChain;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModelChain.ConfirmationModelChainItem;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModelSettingsManager;
import org.ovirt.engine.ui.uicommonweb.models.ConsolePopupModel;
import org.ovirt.engine.ui.uicommonweb.models.ConsolesFactory;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.SearchStringMapping;
import org.ovirt.engine.ui.uicommonweb.models.VmConsoles;
import org.ovirt.engine.ui.uicommonweb.models.VmErrataCountModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.ChangeCDModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.list.VmAffinityLabelListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list.VmAffinityGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.VmBaseListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.VmHostDeviceListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.ICancelable;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class VmListModel<E> extends VmBaseListModel<E, VM>
        implements ICancelable, HasDiskWindow, TagAssigningModel<VM> {

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

    private static final String SUSPEND = "Suspend"; //$NON-NLS-1$
    private static final String SHUTDOWN = "Shutdown"; //$NON-NLS-1$
    private static final String STOP     = "Stop"; //$NON-NLS-1$
    private static final String REBOOT   = "Reboot"; //$NON-NLS-1$
    private static final String RESET    = "Reset"; //$NON-NLS-1$

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

    private UICommand privateSuspendCommand;

    public UICommand getSuspendCommand() {
        return privateSuspendCommand;
    }

    private void setSuspendCommand(UICommand value) {
        privateSuspendCommand = value;
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

    private UICommand privateResetCommand;

    public UICommand getResetCommand() {
        return privateResetCommand;
    }

    public void setResetCommand(UICommand value) {
        privateResetCommand = value;
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

    private UICommand privateExportOvaCommand;

    public UICommand getExportOvaCommand() {
        return privateExportOvaCommand;
    }

    private void setExportOvaCommand(UICommand value) {
        privateExportOvaCommand = value;
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

    @Override
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

    private final VmGeneralModel generalModel;

    public VmGeneralModel getGeneralModel() {
        return generalModel;
    }

    private final VmInterfaceListModel interfaceListModel;

    public VmInterfaceListModel getInterfaceListModel() {
        return interfaceListModel;
    }

    private final VmEventListModel eventListModel;

    public VmEventListModel getEventListModel() {
        return eventListModel;
    }

    private final VmDiskListModel diskListModel;

    public VmDiskListModel getDiskListModel() {
        return diskListModel;
    }

    private final VmSnapshotListModel snapshotListModel;

    public VmSnapshotListModel getSnapshotListModel() {
        return snapshotListModel;
    }

    private final VmAppListModel<VM> appListModel;

    public VmAppListModel<VM> getAppListModel() {
        return appListModel;
    }

    private final VmHostDeviceListModel hostDeviceListModel;

    public VmHostDeviceListModel getHostDeviceListModel() {
        return hostDeviceListModel;
    }

    private final VmDevicesListModel<VM> vmDevicesListModel;

    public VmDevicesListModel<VM> getVmDevicesListModel() {
        return vmDevicesListModel;
    }

    private final VmAffinityGroupListModel affinityGroupListModel;

    public VmAffinityGroupListModel getAffinityGroupListModel() {
        return affinityGroupListModel;
    }

    private final VmAffinityLabelListModel affinityLabelListModel;

    public VmAffinityLabelListModel getAffinityLabelListModel() {
        return affinityLabelListModel;
    }

    private final PermissionListModel<VM> permissionListModel;

    public PermissionListModel<VM> getPermissionListModel() {
        return permissionListModel;
    }

    private final VmGuestInfoModel guestInfoModel;

    public VmGuestInfoModel getGuestInfoModel() {
        return guestInfoModel;
    }

    private final VmErrataCountModel errataCountModel;

    public VmErrataCountModel getErrataCountModel() {
        return errataCountModel;
    }

    private final VmGuestContainerListModel guestContainerListModel;

    public VmGuestContainerListModel getGuestContainerListModel() {
        return guestContainerListModel;
    }

    private final ConfirmationModelSettingsManager confirmationModelSettingsManager;

    @Inject
    public VmListModel(final VmGeneralModel vmGeneralModel,
            final VmInterfaceListModel vmInterfaceListModel,
            final VmDiskListModel vmDiskListModel,
            final VmSnapshotListModel vmSnapshotListModel,
            final VmEventListModel vmEventListModel,
            final VmAppListModel<VM> vmAppListModel,
            final PermissionListModel<VM> permissionListModel,
            final VmAffinityGroupListModel vmAffinityGroupListModel,
            final VmGuestInfoModel vmGuestInfoModel,
            final Provider<ImportVmsModel> importVmsModelProvider,
            final VmHostDeviceListModel vmHostDeviceListModel,
            final VmDevicesListModel<VM> vmDevicesListModel,
            final VmAffinityLabelListModel vmAffinityLabelListModel,
            final VmErrataCountModel vmErrataCountModel,
            final VmGuestContainerListModel vmGuestContainerListModel,
            final ConfirmationModelSettingsManager confirmationModelSettingsManager) {
        this.importVmsModelProvider = importVmsModelProvider;
        this.generalModel = vmGeneralModel;
        this.interfaceListModel = vmInterfaceListModel;
        this.eventListModel = vmEventListModel;
        this.diskListModel = vmDiskListModel;
        this.snapshotListModel = vmSnapshotListModel;
        this.appListModel = vmAppListModel;
        this.hostDeviceListModel = vmHostDeviceListModel;
        this.vmDevicesListModel = vmDevicesListModel;
        this.affinityGroupListModel = vmAffinityGroupListModel;
        this.affinityLabelListModel = vmAffinityLabelListModel;
        this.permissionListModel = permissionListModel;
        this.guestInfoModel = vmGuestInfoModel;
        this.errataCountModel = vmErrataCountModel;
        this.guestContainerListModel = vmGuestContainerListModel;
        this.confirmationModelSettingsManager = confirmationModelSettingsManager;

        setDetailList();
        setTitle(ConstantsManager.getInstance().getConstants().virtualMachinesTitle());
        setHelpTag(HelpTag.virtual_machines);
        setApplicationPlace(WebAdminApplicationPlaces.virtualMachineMainPlace);
        setHashName("virtual_machines"); //$NON-NLS-1$

        setDefaultSearchString(SearchStringMapping.VMS_DEFAULT_SEARCH + ":"); //$NON-NLS-1$
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
        setSuspendCommand(new UICommand("Suspend", this)); //$NON-NLS-1$
        setStopCommand(new UICommand("Stop", this)); //$NON-NLS-1$
        setShutdownCommand(new UICommand("Shutdown", this)); //$NON-NLS-1$
        setRebootCommand(new UICommand("Reboot", this)); //$NON-NLS-1$
        setResetCommand(new UICommand("Reset", this)); //$NON-NLS-1$
        setEditConsoleCommand(new UICommand("EditConsoleCommand", this)); //$NON-NLS-1$
        setConsoleConnectCommand(new UICommand("ConsoleConnectCommand", this)); //$NON-NLS-1$
        setCancelMigrateCommand(new UICommand("CancelMigration", this)); //$NON-NLS-1$
        setCancelConvertCommand(new UICommand("CancelConversion", this)); //$NON-NLS-1$
        setNewTemplateCommand(new UICommand("NewTemplate", this)); //$NON-NLS-1$
        setRunOnceCommand(new UICommand("RunOnce", this)); //$NON-NLS-1$
        setExportCommand(new UICommand("Export", this)); //$NON-NLS-1$
        setExportOvaCommand(new UICommand("ExportOva", this)); //$NON-NLS-1$
        setCreateSnapshotCommand(new UICommand("CreateSnapshot", this)); //$NON-NLS-1$
        setGuideCommand(new UICommand("Guide", this)); //$NON-NLS-1$
        setRetrieveIsoImagesCommand(new UICommand("RetrieveIsoImages", this)); //$NON-NLS-1$
        setChangeCdCommand(new UICommand("ChangeCD", this)); //$NON-NLS-1$
        setAssignTagsCommand(new UICommand("AssignTags", this)); //$NON-NLS-1$

        setIsoImages(new ObservableCollection<ChangeCDModel>());
        ChangeCDModel tempVar = new ChangeCDModel();
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().retrievingCDsTitle());
        getIsoImages().add(tempVar);

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);

        getItemsChangedEvent().addListener((ev, sender, args) -> vmAffinityLabelListModel.loadEntitiesNameMap());

        updateActionsAvailability();
    }

    private void setDetailList() {
        List<HasEntity<VM>> list = new ArrayList<>();
        list.add(generalModel);
        list.add(interfaceListModel);
        list.add(diskListModel);
        list.add(snapshotListModel);
        list.add(eventListModel);
        list.add(appListModel);
        list.add(vmDevicesListModel);
        list.add(permissionListModel);
        list.add(affinityGroupListModel);
        list.add(guestInfoModel);
        list.add(hostDeviceListModel);
        list.add(affinityLabelListModel);
        list.add(guestContainerListModel);
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
                            returnValue -> {

                                allAttachedTags.addAll(returnValue);
                                selectedItemsCounter++;
                                if (selectedItemsCounter == getSelectedItems().size()) {
                                    postGetAttachedTags(model);
                                }

                            }),
                    id);
        }
    }

    private void onAssignTags() {
        TagListModel model = (TagListModel) getWindow();

        getAttachedTagsToSelectedVMs(model);
    }

    @Override
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

        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        for (Guid a : tagsToAttach) {
            parameters.add(new AttachEntityToTagParameters(a, vmIds));
        }
        Frontend.getInstance().runMultipleAction(ActionType.AttachVmsToTag, parameters);

        parameters = new ArrayList<>();
        for (Guid a : tagsToDetach) {
            parameters.add(new AttachEntityToTagParameters(a, vmIds));
        }
        Frontend.getInstance().runMultipleAction(ActionType.DetachVmFromTag, parameters);

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
                returnValue -> {
                    VmGuideModel vmGuideModel = (VmGuideModel) getWindow();
                    vmGuideModel.setEntity(returnValue);

                    UICommand tempVar = new UICommand("Cancel", VmListModel.this); //$NON-NLS-1$
                    tempVar.setTitle(ConstantsManager.getInstance().getConstants().configureLaterTitle());
                    tempVar.setIsDefault(true);
                    tempVar.setIsCancel(true);
                    vmGuideModel.getCommands().add(tempVar);
                }), (Guid) getGuideContext());
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("vm"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        SearchParameters tempVar = new SearchParameters(applySortOptions(getModifiedSearchString()),
                SearchType.VM, isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(QueryType.Search, tempVar);
    }

    private void newVm() {
        if (getWindow() != null) {
            return;
        }

        List<UICommand> commands = new ArrayList<>();
        commands.add(UICommand.createDefaultOkUiCommand("OnSave", this)); //$NON-NLS-1$
        commands.add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
        UnitVmModel model = new UnitVmModel(new NewVmModelBehavior(), this);
        setupNewVmModel(model, VmType.Server, commands);
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
        AsyncQuery<VM> getVmInitQuery = new AsyncQuery<>(result -> {
            editedVm = result;
            vmInitLoaded(editedVm);
        });
        if (vm.isNextRunConfigurationExists()) {
            AsyncDataProvider.getInstance().getVmNextRunConfiguration(getVmInitQuery, vm.getId());
        } else {
            AsyncDataProvider.getInstance().getVmById(getVmInitQuery, vm.getId());
        }

    }

    private void vmInitLoaded(VM vm) {
        UnitVmModel model = new UnitVmModel(new ExistingVmModelBehavior(vm), this);
        model.setVmAttachedToPool(vm.getVmPoolId() != null);
        model.setIsAdvancedModeLocalStorageKey(IS_ADVANCED_MODEL_LOCAL_STORAGE_KEY);
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance()
                .getConstants().editVmTitle());
        model.setHelpTag(HelpTag.edit_vm);
        model.setHashName("edit_vm"); //$NON-NLS-1$
        model.setCustomPropertiesKeysList(AsyncDataProvider.getInstance().getCustomPropertiesList());

        model.initialize();
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
            if (ActionUtils.canExecute(Arrays.asList(vm), VM.class, ActionType.RemoveVm)) {
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
        model.setIsChangeable(isChangable);
        if (!isChangable && changeProhibitionReason != null) {
            model.setChangeProhibitionReason(changeProhibitionReason);
        }
    }

    private void initRemoveDisksCheckboxes(final Map<Guid, EntityModel> vmsMap) {
        ArrayList<QueryParametersBase> params = new ArrayList<>();
        ArrayList<QueryType> queries = new ArrayList<>();

        for (Entry<Guid, EntityModel> entry : vmsMap.entrySet()) {
            if (entry.getValue().getIsChangable()) { // No point in fetching VM disks from ones that already determined
                                                     // is unchangeable since they are already initialized
                params.add(new IdQueryParameters(entry.getKey()));
                queries.add(QueryType.GetAllDisksByVmId);
            }
        }

        // TODO: There's no point in creating a QueryType list when you wanna run the same query for all parameters,
        // revise when refactoring org.ovirt.engine.ui.Frontend to support runMultipleQuery with a single query
        if (!params.isEmpty()) {
            Frontend.getInstance().runMultipleQueries(queries, params, result -> {
                for (int i = 0; i < result.getReturnValues().size(); i++) {
                    if (result.getReturnValues().get(i).getSucceeded()) {
                        Guid vmId = ((IdQueryParameters) result.getParameters().get(i)).getId();
                        initRemoveDisksChecboxesPost(vmId, (List<Disk>) result.getReturnValues()
                                .get(i)
                                .getReturnValue());
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
    protected QueryType getEntityExportDomain() {
        return QueryType.GetVmsFromExportDomain;
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
                storagePools -> {
                    StoragePool storagePool = storagePools.size() > 0 ? storagePools.get(0) : null;

                    postGetTemplatesNotPresentOnExportDomain(storagePool);
                }), storageDomainId);
    }

    private void postGetTemplatesNotPresentOnExportDomain(StoragePool storagePool) {
        ExportVmModel model = (ExportVmModel) getWindow();
        Guid storageDomainId = model.getStorage().getSelectedItem().getId();

        if (storagePool != null) {
            AsyncDataProvider.getInstance().getAllTemplatesFromExportDomain(new AsyncQuery<>(
                            templatesDiskSet -> {
                                Map<String, ArrayList<String>> templateDic = new HashMap<>();

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
                                for (Entry<String, ArrayList<String>> keyValuePair : templateDic.entrySet()) {
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
                            }),
                    storagePool.getId(),
                    storageDomainId);
        }
    }

    private void postExportGetMissingTemplates(ArrayList<String> missingTemplatesFromVms) {
        ExportVmModel model = (ExportVmModel) getWindow();
        Guid storageDomainId = model.getStorage().getSelectedItem().getId();
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();

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
            } else {
                if (model.getProgress() != null) {
                    return;
                }

                model.startProgress();

                Frontend.getInstance().runMultipleAction(ActionType.ExportVm, parameters,
                        result -> {
                            ExportVmModel localModel = (ExportVmModel) result.getState();
                            localModel.stopProgress();
                            cancel();
                        }, model);
            }
        } else {
            if (model.getProgress() != null) {
                return;
            }

            for (ActionParametersBase item : parameters) {
                MoveOrCopyParameters parameter = (MoveOrCopyParameters) item;
                parameter.setTemplateMustExists(false);
            }

            model.startProgress();

            Frontend.getInstance().runMultipleAction(ActionType.ExportVm, parameters,
                    result -> {
                        ExportVmModel localModel = (ExportVmModel) result.getState();
                        localModel.stopProgress();
                        cancel();
                    }, model);
        }
    }

    @Override
    protected void postExportGetSnapshots(VM selectedEntity) {
        AsyncDataProvider.getInstance().getVmSnapshotList(
            new AsyncQuery<>(snapshots -> {
                if (snapshots.size() == 1 && getSelectedItems().size() == 1) {
                    ExportVmModel model = (ExportVmModel) getWindow();
                    model.getCollapseSnapshots().setIsChangeable(false);
                }
                postExportInitStorageDomains(selectedEntity);
            }),
        selectedEntity.getId());
    }

    @Override
    protected void setupExportModel(ExportVmModel model) {
        super.setupExportModel(model);
        model.setTitle(constants.exportVirtualMachineTitle());
        model.setHelpTag(HelpTag.export_virtual_machine);
        model.setHashName("export_virtual_machine"); //$NON-NLS-1$
    }

    @Override
    protected void setupExportOvaModel(ExportOvaModel model) {
        super.setupExportOvaModel(model);
        model.setTitle(constants.exportVirtualMachineAsOvaTitle());
        model.setHelpTag(HelpTag.export_virtual_machine);
        model.setHashName("export_virtual_machine"); //$NON-NLS-1$
    }

    public void onExportOva() {
        ExportOvaModel model = (ExportOvaModel) getWindow();
        if (!model.validate()) {
            return;
        }

        model.startProgress();

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VM vm = (VM) item;
            ExportVmToOvaParameters parameters = new ExportVmToOvaParameters();
            parameters.setEntityId(vm.getId());
            parameters.setProxyHostId(model.getProxy().getSelectedItem().getId());
            parameters.setDirectory(model.getPath().getEntity());
            parameters.setName(model.getName().getEntity());

            list.add(parameters);
        }

        Frontend.getInstance().runMultipleAction(ActionType.ExportVmToOva, list,
                result -> {
                    ExportOvaModel localModel = (ExportOvaModel) result.getState();
                    localModel.stopProgress();
                    cancel();
                }, model);
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

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VM a = (VM) item;
            MoveOrCopyParameters parameters = new MoveOrCopyParameters(a.getId(), storageDomainId);
            parameters.setForceOverride(model.getForceOverride().getEntity());
            parameters.setCopyCollapse(model.getCollapseSnapshots().getEntity());
            parameters.setTemplateMustExists(false);

            list.add(parameters);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.ExportVm, list,
                result -> {

                    ExportVmModel localModel = (ExportVmModel) result.getState();
                    localModel.stopProgress();
                    cancel();

                }, model);
    }

    @Override
    protected void sendWarningForNonExportableDisks(VM entity) {
        // load VM disks and check if there is one which doesn't allow snapshot
        AsyncDataProvider.getInstance().getVmDiskList(new AsyncQuery<>(
                        vmDisks -> {
                            final ExportVmModel model = (ExportVmModel) getWindow();
                            VmModelHelper.sendWarningForNonExportableDisks(model,
                                    vmDisks,
                                    VmModelHelper.WarningType.VM_EXPORT);
                        }),
                entity.getId());
    }

    private void runOnce() {
        VM vm = getSelectedItem();
        // populating VMInit
        AsyncDataProvider.getInstance().getVmById(new AsyncQuery<>(result -> {
            RunOnceModel runOnceModel = new WebadminRunOnceModel(result, VmListModel.this);
            setWindow(runOnceModel);
            runOnceModel.init();
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

        model.initialize();

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
        } else  if (model.getIsSubTemplate().getEntity()) {
            postNameUniqueCheck();
        } else {
            String name = model.getName().getEntity();

            // Check name unicitate.
            AsyncDataProvider.getInstance().isTemplateNameUnique(new AsyncQuery<>(
                            isNameUnique -> {

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
                                } else {
                                    postNameUniqueCheck();
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
        Frontend.getInstance().runAction(ActionType.AddVmTemplate, addVmTemplateParameters,
                result -> {
                    getWindow().stopProgress();
                    ActionReturnValue returnValueBase = result.getReturnValue();
                    if (returnValueBase != null && returnValueBase.getSucceeded()) {
                        cancel();
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

    private void cancelMigration() {
        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VM a = (VM) item;
            list.add(new VmOperationParameterBase(a.getId()));
        }

        Frontend.getInstance().runMultipleAction(ActionType.CancelMigrateVm, list,
                result -> {
                }, null);
    }

    private void cancelConversion() {
        List<ActionParametersBase> parameters = new ArrayList<>();
        for (VM vm : getSelectedItems()) {
            parameters.add(new VmOperationParameterBase(vm.getId()));
        }

        Frontend.getInstance().runMultipleAction(ActionType.CancelConvertVm, parameters);
    }

    private void powerActionBase(final String actionName, final String title, final String message) {
        Guid clusterId = getClusterIdOfSelectedVms();
        if (clusterId == null) {
            powerAction(actionName, title, message);
        } else {
            AsyncDataProvider.getInstance().getClusterById(new AsyncQuery<>(
                    cluster -> {
                        if (cluster != null) {
                            powerAction(actionName, title, message);
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

    private void powerAction(String actionName, String title, String message) {
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(title);

        if (actionName.equals(SHUTDOWN)) {
            model.setHelpTag(HelpTag.shutdown_virtual_machine);
            model.setHashName("shutdown_virtual_machine"); //$NON-NLS-1$
        } else if (actionName.equals(STOP)) {
            model.setHelpTag(HelpTag.stop_virtual_machine);
            model.setHashName("stop_virtual_machine"); //$NON-NLS-1$
        } else if (actionName.equals(REBOOT)) {
            model.setHelpTag(HelpTag.reboot_virtual_machine);
            model.setHashName("reboot_virtual_machine"); //$NON-NLS-1$
        } else if (actionName.equals(RESET)) {
            model.setHelpTag(HelpTag.reset_virtual_machine);
            model.setHashName("reset_virtual_machine"); //$NON-NLS-1$
        } else if (actionName.equals(SUSPEND)) {
            model.setHelpTag(HelpTag.suspend_virtual_machine);
            model.setHashName(HelpTag.suspend_virtual_machine.name());
            model.getDoNotShowAgain().setIsAvailable(true);
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
            if (stoppingSingleVM && VMStatus.PoweringDown.equals(vm.getStatus())) {
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

    private interface PowerActionParametersFactory<P extends ActionParametersBase> {
        P createActionParameters(VM vm);
    }

    private void onPowerAction(ActionType actionType, PowerActionParametersFactory<?> parametersFactory) {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }


        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VM vm = (VM) item;
            list.add(parametersFactory.createActionParameters(vm));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(actionType, list,
                result -> {

                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();

                }, model);

    }

    private void shutdown() {
        UIConstants constants = ConstantsManager.getInstance().getConstants();
        powerActionBase(SHUTDOWN,
                constants.shutdownVirtualMachinesTitle(),
                constants.areYouSureYouWantToShutDownTheFollowingVirtualMachinesMsg());
    }

    private void onShutdown() {
        final ConfirmationModel model = (ConfirmationModel) getWindow();
        onPowerAction(ActionType.ShutdownVm,
                vm -> new ShutdownVmParameters(vm.getId(), true, model.getReason().getEntity()));
    }

    private void stop() {
        UIConstants constants = ConstantsManager.getInstance().getConstants();
        powerActionBase(STOP,
                constants.stopVirtualMachinesTitle(),
                constants.areYouSureYouWantToStopTheFollowingVirtualMachinesMsg());
    }

    private void onStop() {
        final ConfirmationModel model = (ConfirmationModel) getWindow();
        onPowerAction(ActionType.StopVm,
                vm -> new StopVmParameters(vm.getId(), StopVmTypeEnum.NORMAL, model.getReason().getEntity()));
    }

    private void reboot() {
        UIConstants constants = ConstantsManager.getInstance().getConstants();
        powerAction(REBOOT,
                constants.rebootVirtualMachinesTitle(),
                constants.areYouSureYouWantToRebootTheFollowingVirtualMachinesMsg());
    }

    private void onReboot() {
        onPowerAction(ActionType.RebootVm, vm -> new RebootVmParameters(vm.getId()));
    }

    private void reset() {
        UIConstants constants = ConstantsManager.getInstance().getConstants();
        powerAction(RESET,
                constants.resetVirtualMachinesTitle(),
                constants.areYouSureYouWantToResetTheFollowingVirtualMachinesMsg());
    }

    private void onReset() {
        onPowerAction(ActionType.ResetVm, vm -> new VmOperationParameterBase(vm.getId()));
    }

    private void suspend() {
        if (confirmationModelSettingsManager.isConfirmSuspendingVm()) {
            UIConstants constants = ConstantsManager.getInstance().getConstants();
            powerAction(SUSPEND,
                    constants.suspendVirtualMachinesTitle(),
                    constants.areYouSureYouWantToSuspendTheFollowingVirtualMachinesMsg());
        } else {
            ArrayList<ActionParametersBase> paramsList = new ArrayList<>();
            for (Object item : getSelectedItems()) {
                VM a = (VM) item;
                paramsList.add(new VmOperationParameterBase(a.getId()));
            }
            Frontend.getInstance().runMultipleAction(ActionType.HibernateVm, paramsList, result -> {}, null);
        }
    }

    private void onSuspend() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (model.getDoNotShowAgain().getEntity()) {
            confirmationModelSettingsManager.setConfirmSuspendingVm(!model.getDoNotShowAgain().getEntity());
        }

        onPowerAction(ActionType.HibernateVm, vm -> new VmOperationParameterBase(vm.getId()));
    }

    private void run() {
        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VM a = (VM) item;
            list.add(new RunVmParams(a.getId()));
        }

        Frontend.getInstance().runMultipleAction(ActionType.RunVm, list, result -> {}, null);
    }

    private void onRemove() {
        final ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        final ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Entry<Guid, EntityModel> entry : vmsRemoveMap.entrySet()) {
            list.add(new RemoveVmParameters(entry.getKey(), false, (Boolean) entry.getValue().getEntity()));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.RemoveVm, list,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();
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
        List<RepoImage> images1 = new ArrayList<>(
                Arrays.asList(new RepoImage(ConstantsManager.getInstance().getConstants().noCds())));
        attachCdModel.getIsoImage().setItems(images1);
        attachCdModel.getIsoImage().setSelectedItem(Linq.firstOrNull(images1));

        ImagesDataProvider.getISOImagesList(new AsyncQuery<>(images -> {
            AttachCdModel _attachCdModel = (AttachCdModel) getWindow();
            RepoImage eject = new RepoImage(ConsoleModel.getEjectLabel());
            images.add(0, eject);
            _attachCdModel.getIsoImage().setItems(images);
            if (_attachCdModel.getIsoImage().getIsChangable()) {
                RepoImage selectedIso =
                        Linq.firstOrNull(images, s -> vm.getCurrentCd() != null && vm.getCurrentCd().endsWith(s.getRepoImageId()));
                _attachCdModel.getIsoImage().setSelectedItem(selectedIso == null ? eject : selectedIso);
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

        if (Objects.equals(model.getIsoImage().getSelectedItem().getRepoImageId(), vm.getCurrentCd())) {
            cancel();
            return;
        }

        String isoName =
                Objects.equals(model.getIsoImage().getSelectedItem().getRepoImageId(), ConsoleModel.getEjectLabel()) ? "" //$NON-NLS-1$
                        : model.getIsoImage().getSelectedItem().getRepoImageId();

        model.startProgress();

        Frontend.getInstance().runAction(ActionType.ChangeDisk, new ChangeDiskCommandParameters(vm.getId(), isoName),
                result -> {

                    AttachCdModel attachCdModel = (AttachCdModel) result.getState();
                    attachCdModel.stopProgress();
                    cancel();

                }, model);
    }

    private void preSave() {
        final UnitVmModel model = (UnitVmModel) getWindow();

        if (!model.getIsNew() && selectedItem == null) {
            cancel();
            return;
        }

        setcurrentVm(model.getIsNew() ? new VM() : (VM) Cloner.clone(selectedItem));

        confirmPreSaveWarnings();
    }

    private void confirmPreSaveWarnings() {
        UnitVmModel model = (UnitVmModel) getWindow();

        ConfirmationModelChain chain = new ConfirmationModelChain();
        chain.addConfirmation(createConfirmCustomCpu(model));
        chain.addConfirmation(createConfirmCpuPinningLost(model));
        chain.execute(this, this::preSaveUpdateAndValidateModel);
    }

    private ConfirmationModelChainItem createConfirmCustomCpu(UnitVmModel model) {
        return new ConfirmationModelChainItem() {

            @Override
            public boolean isRequired() {
                final String selectedCpu = model.getCustomCpu().getSelectedItem();
                return selectedCpu != null && !selectedCpu.isEmpty() && !model.getCustomCpu().getItems().contains(selectedCpu);
            }

            @Override
            public ConfirmationModel getConfirmation() {
                ConfirmationModel confirmModel = new ConfirmationModel();
                confirmModel.setTitle(ConstantsManager.getInstance().getConstants().vmUnsupportedCpuTitle());
                confirmModel.setMessage(ConstantsManager.getInstance().getConstants().vmUnsupportedCpuMessage());
                confirmModel.setHelpTag(HelpTag.edit_unsupported_cpu);
                confirmModel.setHashName("edit_unsupported_cpu"); //$NON-NLS-1$
                return confirmModel;
            }
        };
    }

    private ConfirmationModelChainItem createConfirmCpuPinningLost(UnitVmModel model) {
        return new ConfirmationModelChainItem() {

            @Override
            public boolean isRequired() {
                final EntityModel<String> cpuPinning = model.getCpuPinning();
                return !cpuPinning.getIsChangable() && !model.isVmAttachedToPool() && cpuPinning.getEntity() != null
                        && !cpuPinning.getEntity().isEmpty();
            }

            @Override
            public ConfirmationModel getConfirmation() {
                ConfirmationModel confirmModel = new ConfirmationModel();
                confirmModel.setTitle(ConstantsManager.getInstance().getConstants().vmCpuPinningClearTitle());
                confirmModel.setMessage(ConstantsManager.getInstance().getConstants().vmCpuPinningClearMessage());

                confirmModel.setHelpTag(HelpTag.edit_unsupported_cpu);
                confirmModel.setHashName("edit_clear_cpu_pinning"); //$NON-NLS-1$

                return confirmModel;
            }

            @Override
            public void onConfirm() {
                model.getCpuPinning().setEntity("");
            }
        };
    }

    private void preSaveUpdateAndValidateModel() {
        final UnitVmModel model = (UnitVmModel) getWindow();
        final String name = model.getName().getEntity();

        if (!model.getIsNew() && !model.getIsClone() && model.getNumaEnabled().getEntity() &&
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

        ConfirmationModelChain chain = new ConfirmationModelChain();
        NextRunConfigurationConfirmation confirmationChainItem = new NextRunConfigurationConfirmation();
        chain.addConfirmation(confirmationChainItem);
        chain.execute(this, () -> updateExistingVm(confirmationChainItem.applyChangesLater()));
    }

    protected void setupCloneVmModel(UnitVmModel model, List<UICommand> uiCommands) {
        model.setTitle(ConstantsManager.getInstance().getConstants().cloneVmTitle());
        model.setHelpTag(HelpTag.clone_vm);
        model.setHashName(HelpTag.clone_vm.name());
        model.setIsClone(true);
        model.setCustomPropertiesKeysList(AsyncDataProvider.getInstance().getCustomPropertiesList());
        model.setIsAdvancedModeLocalStorageKey(IS_ADVANCED_MODEL_LOCAL_STORAGE_KEY);

        setWindow(model);
        model.initialize();

        VmBasedWidgetSwitchModeCommand switchModeCommand = new VmBasedWidgetSwitchModeCommand();
        switchModeCommand.init(model);
        model.getCommands().add(switchModeCommand);

        model.getProvisioning().setEntity(true);

        model.getCommands().addAll(uiCommands);

        model.initForemanProviders(null);
    }

    @Override
    protected void cloneVM(final UnitVmModel model) {
        if (model.getProgress() != null) {
            return;
        }

        model.startProgress();

        VM vm = getcurrentVm();

        CloneVmParameters parameters = getCloneVmParameters(vm, vm.getName(), true);
        parameters.setDiskInfoDestinationMap(model.getDisksAllocationModel().getImageToDestinationDomainMap());
        List<VmInterfacesModifyParameters.VnicWithProfile> vnicsWithProfiles =
                model.getNicsWithLogicalNetworks().getItems().stream()
                    .map(vnic -> new VmInterfacesModifyParameters.VnicWithProfile(
                            vnic.getNetworkInterface(), vnic.getSelectedItem()))
                    .collect(Collectors.toList());
        parameters.setVnicsWithProfiles(vnicsWithProfiles);

        IFrontendActionAsyncCallback callback = result -> {
            model.stopProgress();
            cancel();
        };
        Frontend.getInstance().runAction(ActionType.CloneVm, parameters, callback, this);
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
        if (!oldClusterID.equals(newClusterID)) {
            ChangeVMClusterParameters parameters =
                    new ChangeVMClusterParameters(
                            newClusterID,
                            getcurrentVm().getId(),
                            model.getCustomCompatibilityVersion().getSelectedItem());

            model.startProgress();

            Frontend.getInstance().runAction(ActionType.ChangeVMCluster, parameters,
                    result -> {

                        final VmListModel<Void> vmListModel = (VmListModel<Void>) result.getState();
                        ActionReturnValue returnValueBase = result.getReturnValue();
                        if (returnValueBase != null && returnValueBase.getSucceeded()) {
                            VM vm = vmListModel.getcurrentVm();
                            VmManagementParametersBase updateVmParams = vmListModel.getUpdateVmParameters(applyCpuChangesLater);
                            Frontend.getInstance().runAction(ActionType.UpdateVm,
                                    updateVmParams, new UnitVmModelNetworkAsyncCallback(model, defaultNetworkCreatingManager, vm.getId()), vmListModel);
                        } else {
                            vmListModel.getWindow().stopProgress();
                        }

                    },
                    this);
        } else {
            model.startProgress();
            VmManagementParametersBase updateVmParams = getUpdateVmParameters(applyCpuChangesLater);
            Frontend.getInstance().runAction(ActionType.UpdateVm, updateVmParams, new UnitVmModelNetworkAsyncCallback(model, defaultNetworkCreatingManager, getcurrentVm().getId()), this);
        }
    }

    public VmManagementParametersBase getUpdateVmParameters(boolean applyCpuChangesLater) {
        VmManagementParametersBase updateVmParams = new VmManagementParametersBase(getcurrentVm());
        fillVmManagementParameters(updateVmParams);
        updateVmParams.setApplyChangesLater(applyCpuChangesLater);
        return updateVmParams;
    }

    public CloneVmParameters getCloneVmParameters(VM vm, String newName, boolean isEdited) {
        CloneVmParameters params = new CloneVmParameters(vm, newName, isEdited);
        fillVmManagementParameters(params);
        return params;
    }

    public VmManagementParametersBase fillVmManagementParameters(VmManagementParametersBase params) {
        UnitVmModel model = (UnitVmModel) getWindow();

        setVmWatchdogToParams(model, params);
        params.setSoundDeviceEnabled(model.getIsSoundcardEnabled().getEntity());
        params.setTpmEnabled(model.getTpmEnabled().getEntity());
        params.setConsoleEnabled(model.getIsConsoleDeviceEnabled().getEntity());
        params.setVirtioScsiEnabled(model.getIsVirtioScsiEnabled().getEntity());
        params.setUpdateNuma(model.isNumaChanged());
        params.setAffinityGroups(model.getAffinityGroupList().getSelectedItems());
        params.setAffinityLabels(model.getLabelList().getSelectedItems());
        if (model.getIsHeadlessModeEnabled().getEntity()) {
            params.getVmStaticData().setDefaultDisplayType(DisplayType.none);
        }
        BuilderExecutor.build(
                new Pair<>((UnitVmModel) getWindow(), getSelectedItem()),
                params,
                new VmIconUnitAndVmToParameterBuilder());
        setRngDeviceToParams(model, params);
        BuilderExecutor.build(model, params, new UnitToGraphicsDeviceParamsBuilder());

        return params;
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

        Frontend.getInstance().runMultipleAction(ActionType.ChangeDisk,
                new ArrayList<>(Arrays.asList(new ActionParametersBase[] { new ChangeDiskCommandParameters(vm.getId(),
                        Objects.equals(isoName, ConsoleModel.getEjectLabel()) ? "" : isoName) })), //$NON-NLS-1$
                result -> {

                },
                null);
    }

    @Override
    public void cancel() {
        cancelConfirmation();

        setGuideContext(null);
        setWindow(null);

        fireModelChangeRelevantForActionsEvent();
    }

    @Override
    protected void onModelChangeRelevantForActions() {
        super.onModelChangeRelevantForActions();
        updateActionsAvailability();
    }

    private void updateActionsAvailability() {
        List<VmWithStatusForExclusiveLock> items = getSelectedItems() != null && getSelectedItem() != null ? getSelectedItemsWithStatusForExclusiveLock() : new ArrayList<>();

        boolean singleVmSelected = items.size() == 1;
        boolean vmsSelected = items.size() > 0;

        getEditCommand().setIsExecutionAllowed(singleVmSelected
                && ActionUtils.canExecute(items, VmWithStatusForExclusiveLock.class, ActionType.UpdateVm));
        getRemoveCommand().setIsExecutionAllowed(vmsSelected && isRemovalEnabled()
                && ActionUtils.canExecutePartially(items, VmWithStatusForExclusiveLock.class, ActionType.RemoveVm));
        getRunCommand().setIsExecutionAllowed(vmsSelected
                && ActionUtils.canExecutePartially(items, VmWithStatusForExclusiveLock.class, ActionType.RunVm));
        getCloneVmCommand().setIsExecutionAllowed(singleVmSelected
                && ActionUtils.canExecute(items, VmWithStatusForExclusiveLock.class, ActionType.CloneVm));
        getSuspendCommand().setIsExecutionAllowed(vmsSelected
                && ActionUtils.canExecutePartially(items, VmWithStatusForExclusiveLock.class, ActionType.HibernateVm));
        getShutdownCommand().setIsExecutionAllowed(vmsSelected
                && ActionUtils.canExecutePartially(items, VmWithStatusForExclusiveLock.class, ActionType.ShutdownVm));
        getStopCommand().setIsExecutionAllowed(vmsSelected
                && ActionUtils.canExecutePartially(items, VmWithStatusForExclusiveLock.class, ActionType.StopVm));
        getRebootCommand().setIsExecutionAllowed(AsyncDataProvider.getInstance().isRebootCommandExecutionAllowed(items));
        getResetCommand().setIsExecutionAllowed(vmsSelected
                && ActionUtils.canExecutePartially(items, VmWithStatusForExclusiveLock.class, ActionType.ResetVm));
        getCancelMigrateCommand().setIsExecutionAllowed(vmsSelected
                && ActionUtils.canExecutePartially(items, VmWithStatusForExclusiveLock.class, ActionType.CancelMigrateVm));
        getNewTemplateCommand().setIsExecutionAllowed(singleVmSelected
                && ActionUtils.canExecute(items, VmWithStatusForExclusiveLock.class, ActionType.AddVmTemplate));
        getRunOnceCommand().setIsExecutionAllowed(singleVmSelected
                && ActionUtils.canExecute(items, VmWithStatusForExclusiveLock.class, ActionType.RunVmOnce));
        getExportCommand().setIsExecutionAllowed(vmsSelected
                && ActionUtils.canExecute(items, VmWithStatusForExclusiveLock.class, ActionType.ExportVm));
        getExportOvaCommand().setIsExecutionAllowed(vmsSelected
                && ActionUtils.canExecute(items, VmWithStatusForExclusiveLock.class, ActionType.CreateSnapshotForVm));
        getCreateSnapshotCommand().setIsExecutionAllowed(singleVmSelected
                && !getSelectedItem().isStateless() && !getSelectedItem().isPreviewSnapshot()
                && ActionUtils.canExecute(items, VmWithStatusForExclusiveLock.class, ActionType.CreateSnapshotForVm));
        getRetrieveIsoImagesCommand().setIsExecutionAllowed(singleVmSelected
                && ActionUtils.canExecute(items, VmWithStatusForExclusiveLock.class, ActionType.ChangeDisk));
        getChangeCdCommand().setIsExecutionAllowed(singleVmSelected
                && ActionUtils.canExecute(items, VmWithStatusForExclusiveLock.class, ActionType.ChangeDisk));
        getAssignTagsCommand().setIsExecutionAllowed(vmsSelected
                && items.stream().allMatch(VM::isManaged));

        getGuideCommand().setIsExecutionAllowed(getGuideContext() != null || singleVmSelected
                && items.get(0).isManaged());

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

    private boolean isRemovalEnabled() {
        Predicate<VM> p = v -> v.isDeleteProtected();
        return getSelectedItems()
                .stream()
                .noneMatch(p);
    }

    private boolean isConsoleEditEnabled() {
        return getSelectedItem() != null && getSelectedItem().isRunningOrPaused();
    }

    private boolean isConsoleCommandsExecutionAllowed() {
        if (getSelectedItems() == null) {
            return false;
        }

        // return true, if at least one console is available
        return getSelectedItems()
                .stream()
                .map(consolesFactory::getVmConsolesForVm)
                .anyMatch(VmConsoles::canConnectToConsole);
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
        } else if (command == getEditConsoleCommand()) {
            editConsole();
        } else if (command == getConsoleConnectCommand()) {
            connectToConsoles();
        } else if (command == getRemoveCommand()) {
            remove();
        } else if (command == getRunCommand()) {
            run();
        } else if (command == getSuspendCommand()) {
            suspend();
        } else if (command == getStopCommand()) {
            stop();
        } else if (command == getShutdownCommand()) {
            shutdown();
        } else if (command == getRebootCommand()) {
            reboot();
        } else if (command == getResetCommand()) {
            reset();
        } else if (command == getNewTemplateCommand()) {
            newTemplate();
        } else if (command == getRunOnceCommand()) {
            runOnce();
        } else if (command == getExportCommand()) {
            export();
        } else if (command == getExportOvaCommand()) {
            exportOva();
        } else if (command == getCreateSnapshotCommand()) {
            createSnapshot();
        } else if (command == getGuideCommand()) {
            guide();
        } else if (command == getRetrieveIsoImagesCommand()) {
            retrieveIsoImages();
        } else if (command == getChangeCdCommand()) {
            changeCD();
        } else if (command == getAssignTagsCommand()) {
            assignTags();
        } else if ("OnAssignTags".equals(command.getName())) { //$NON-NLS-1$
            onAssignTags();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            preSave();
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        } else if ("OnExport".equals(command.getName())) { //$NON-NLS-1$
            onExport();
        } else if ("OnExportOva".equals(command.getName())) { //$NON-NLS-1$
            onExportOva();
        } else if ("OnExportNoTemplates".equals(command.getName())) { //$NON-NLS-1$
            onExportNoTemplates();
        } else if ("CancelConfirmation".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirmation();
        } else if ("OnRunOnce".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if ("OnNewTemplate".equals(command.getName())) { //$NON-NLS-1$
            onNewTemplate();
        } else if (command == getCancelMigrateCommand()) {
            cancelMigration();
        } else if (command == getCancelConvertCommand()) {
            cancelConversion();
        } else if ("OnShutdown".equals(command.getName())) { //$NON-NLS-1$
            onShutdown();
        } else if ("OnStop".equals(command.getName())) { //$NON-NLS-1$
            onStop();
        } else if ("OnReboot".equals(command.getName())) { //$NON-NLS-1$
            onReboot();
        } else if ("OnReset".equals(command.getName())) { //$NON-NLS-1$
            onReset();
        } else if ("OnSuspend".equals(command.getName())) { //$NON-NLS-1$
            onSuspend();
        } else if ("OnChangeCD".equals(command.getName())) { //$NON-NLS-1$
            onChangeCD();
        } else if (command.getName().equals("closeVncInfo") || // $NON-NLS-1$
                "OnEditConsoleSave".equals(command.getName())) { //$NON-NLS-1$
            setWindow(null);
        } else if (CMD_CONFIGURE_VMS_TO_IMPORT.equals(command.getName())) {
            onConfigureVmsToImport();
        }
    }

    protected void exportOva() {
        VM selectedEntity = (VM) getSelectedItem();
        if (selectedEntity == null) {
            return;
        }

        if (getWindow() != null) {
            return;
        }

        ExportOvaModel model = getSelectedItems().size() == 1 ? new ExportOvaModel(selectedEntity.getName())
                : new ExportOvaModel();
        setWindow(model);
        model.startProgress();
        setupExportOvaModel(model);
        AsyncDataProvider.getInstance().getHostListByDataCenter(new AsyncQuery<>(
                hosts -> postExportOvaGetHosts(hosts.stream()
                        .filter(host -> host.getStatus() == VDSStatus.Up)
                        .collect(Collectors.toList()))
                ), extractStoragePoolIdNullSafe(selectedEntity));
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
                                result -> {
                                    boolean isAllValidatePassed = true;
                                    for (ActionReturnValue returnValueBase : result.getReturnValue()) {
                                        if (!returnValueBase.isValid()) {
                                            isAllValidatePassed = false;
                                            break;
                                        }
                                    }
                                    if (isAllValidatePassed) {
                                        setWindow(null);
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
        if (getWindow() != null) {
            return;
        }

        final VM vm = getSelectedItem();
        if (vm == null) {
            return;
        }

        List<UICommand> commands = new ArrayList<>();
        commands.add(UICommand.createDefaultOkUiCommand("OnSave", this)); //$NON-NLS-1$
        commands.add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$

        UnitVmModel model = new UnitVmModel(new CloneVmModelBehavior(vm), this);
        setupCloneVmModel(model, commands);
    }

    private void onConfigureVmsToImport() {
        final ImportVmsModel importVmsModel = (ImportVmsModel) getWindow();
        if (importVmsModel == null || !importVmsModel.validate()) {
            return;
        }

        boolean vmsToImportHaveFullInfo = importVmsModel.vmsToImportHaveFullInfo();
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

        AsyncQuery query = new AsyncQuery(returnValue -> {
            if (returnValue instanceof QueryReturnValue) {
                importVmsModel.setError(messages.providerFailure());
                importVmsModel.stopProgress();
            } else {
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

    class NextRunConfigurationConfirmation implements ConfirmationModelChainItem {

        private VmNextRunConfigurationModel confirmModel;

        private boolean required;

        @Override
        public void init(Runnable callback) {
            required = selectedItem.isRunningOrPaused() && !selectedItem.isHostedEngine();

            if (!required) {
                callback.run();
                return;
            }

            AsyncDataProvider.getInstance().getVmChangedFieldsForNextRun(editedVm, getcurrentVm(), getUpdateVmParameters(false), new AsyncQuery<>(
                    new AsyncCallback<QueryReturnValue>() {
                @Override
                public void onSuccess(QueryReturnValue returnValue) {
                    List<String> changedFields = returnValue.getReturnValue();
                    final boolean cpuHotPluggable = VmCommonUtils.isCpusToBeHotpluggedOrUnplugged(selectedItem, getcurrentVm());
                    final boolean isHeadlessModeChanged = isHeadlessModeChanged(editedVm, getUpdateVmParameters(false));
                    final boolean memoryHotPluggable =
                            VmCommonUtils.isMemoryToBeHotplugged(selectedItem, getcurrentVm());
                    final boolean minAllocatedMemoryChanged = selectedItem.getMinAllocatedMem() != getcurrentVm().getMinAllocatedMem();
                    final boolean vmLeaseUpdated = VmCommonUtils.isVmLeaseToBeHotPluggedOrUnplugged(selectedItem, getcurrentVm());
                    if (isHeadlessModeChanged) {
                        changedFields.add(constants.headlessMode());
                    }

                    // provide warnings if isVmUnpinned()
                    if (!changedFields.isEmpty() || isVmUnpinned() || memoryHotPluggable || cpuHotPluggable || vmLeaseUpdated) {
                        confirmModel = new VmNextRunConfigurationModel();
                        if (isVmUnpinned()) {
                            confirmModel.setVmUnpinned();
                        }
                        confirmModel.setTitle(ConstantsManager.getInstance().getConstants().editNextRunConfigurationTitle());
                        confirmModel.setHelpTag(HelpTag.edit_next_run_configuration);
                        confirmModel.setHashName("edit_next_run_configuration"); //$NON-NLS-1$
                        confirmModel.setChangedFields(changedFields);
                        confirmModel.setCpuPluggable(cpuHotPluggable);
                        confirmModel.setMemoryPluggable(memoryHotPluggable);
                        // it can be plugged only together with the memory, never alone
                        confirmModel.setMinAllocatedMemoryPluggable(memoryHotPluggable && minAllocatedMemoryChanged);
                        confirmModel.setVmLeaseUpdated(vmLeaseUpdated);
                    } else {
                        required = false;
                    }
                    callback.run();
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

        @Override
        public boolean isRequired() {
            return required;
        }

        @Override
        public VmNextRunConfigurationModel getConfirmation() {
            return confirmModel;
        }

        public boolean applyChangesLater() {
            if (confirmModel == null) {
                return false;
            }
            return !confirmModel.isAnythingPluggable() || confirmModel.getApplyLater().getEntity();
        }
    }
}
