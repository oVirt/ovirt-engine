package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExportVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.TemplateVmModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.TimeZoneModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmBasedWidgetSwitchModeCommand;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class TemplateListModel extends VmBaseListModel<VmTemplate> implements ISupportSystemTreeContext
{

    private UICommand privateEditCommand;

    @Override
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

    private UICommand privateExportCommand;

    public UICommand getExportCommand()
    {
        return privateExportCommand;
    }

    private void setExportCommand(UICommand value)
    {
        privateExportCommand = value;
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
        if (systemTreeSelectedItem != value)
        {
            systemTreeSelectedItem = value;
        }
    }

    public TemplateListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().templatesTitle());

        setDefaultSearchString("Template:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.TEMPLATE_OBJ_NAME, SearchObjects.TEMPLATE_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setExportCommand(new UICommand("Export", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    @Override
    protected void setupExportModel(ExportVmModel model) {
        super.setupExportModel(model);
        model.getCollapseSnapshots().setIsAvailable(false);
    }

    @Override
    protected String thereIsNoExportDomainBackupEntityAttachExportDomainToVmsDcMsg() {
        return ConstantsManager.getInstance()
                .getConstants()
                .thereIsNoExportDomainToExportTheTemplateIntoMsg();
    }

    @Override
    protected String entityResideOnSeveralDCsMakeSureTheExportedVMResideOnSameDcMsg() {
        return ConstantsManager.getInstance()
                .getConstants()
                .templatesResideOnSeveralDcsMakeSureExportedTemplatesResideOnSameDcMsg();
    }

    @Override
    protected VdcQueryType getEntityExportDomain() {
        return VdcQueryType.GetTemplatesFromExportDomain;
    }

    @Override
    protected Guid extractStoragePoolIdNullSafe(VmTemplate entity) {
        return entity.getStoragePoolId();
    }

    @Override
    protected boolean entitiesSelectedOnDifferentDataCenters()
    {
        ArrayList<VmTemplate> templates = Linq.<VmTemplate> cast(getSelectedItems());

        Map<Guid, ArrayList<VmTemplate>> t =
                new HashMap<Guid, ArrayList<VmTemplate>>();
        for (VmTemplate a : templates)
        {
            if (!a.getId().equals(Guid.Empty))
            {
                if (!t.containsKey(a.getStoragePoolId()))
                {
                    t.put(a.getStoragePoolId(), new ArrayList<VmTemplate>());
                }

                ArrayList<VmTemplate> list = t.get(a.getStoragePoolId());
                list.add(a);
            }
        }

        return t.size() > 1 ? true : false;
    }

    @Override
    protected String composeEntityOnStorage(String entities) {
        return ConstantsManager.getInstance()
                .getMessages()
                .templatesAlreadyExistonTargetExportDomain(entities);
    }

    @Override
    protected boolean entititesEqualsNullSafe(VmTemplate e1, VmTemplate e2) {
        return e1.getId().equals(e2.getId());
    }

    @Override
    protected Iterable<VmTemplate> asIterableReturnValue(Object returnValue) {
        return ((Map<VmTemplate, ?>) returnValue).keySet();
    }

    private void onExport()
    {
        ExportVmModel model = (ExportVmModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.validate())
        {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VmTemplate a = (VmTemplate) item;
            if (a.getId().equals(Guid.Empty))
            {
                continue;
            }
            MoveOrCopyParameters tempVar =
                    new MoveOrCopyParameters(a.getId(),
                            ((StorageDomain) model.getStorage().getSelectedItem()).getId());
            tempVar.setForceOverride((Boolean) model.getForceOverride().getEntity());
            list.add(tempVar);
        }

        model.startProgress(null);

        Frontend.RunMultipleAction(VdcActionType.ExportVmTemplate, list,
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
    protected void initDetailModels()
    {
        super.initDetailModels();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new TemplateGeneralModel());
        list.add(new TemplateVmListModel());
        list.add(new TemplateInterfaceListModel());
        list.add(new TemplateStorageListModel());
        addCustomModelsDetailModelList(list);
        setDetailModels(list);
    }

    protected void addCustomModelsDetailModelList(ObservableCollection<EntityModel> list) {
        TemplateDiskListModel diskListModel = new TemplateDiskListModel();
        diskListModel.setSystemTreeContext(this);
        list.add(3, diskListModel);
        list.add(new TemplateEventListModel());
        list.add(new PermissionListModel());
    }

    @Override
    public boolean isSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("template"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch()
    {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.VmTemplate);
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, tempVar);
    }

    private void edit()
    {
        VmTemplate template = (VmTemplate) getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        UnitVmModel model = new UnitVmModel(createBehavior(template));
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().editTemplateTitle());
        model.setHashName("edit_template"); //$NON-NLS-1$
        model.getVmType().setSelectedItem(template.getVmType());

        model.initialize(this.getSystemTreeSelectedItem());

        VmBasedWidgetSwitchModeCommand switchModeCommand = new VmBasedWidgetSwitchModeCommand();
        switchModeCommand.init(model);
        model.getCommands().add(switchModeCommand);

        UICommand command;

        command = new UICommand("OnSave", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        model.getCommands().add(command);

        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        model.getCommands().add(command);
    }

    protected TemplateVmModelBehavior createBehavior(VmTemplate template) {
        return new TemplateVmModelBehavior(template);
    }

    private void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeTemplatesTitle());
        model.setHashName("remove_template"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().templatesMsg());

        ArrayList<String> items = new ArrayList<String>();
        ArrayList<VmTemplate> templates = Linq.<VmTemplate> cast(getSelectedItems());
        for (VmTemplate template : templates)
        {
            if (!template.getId().equals(Guid.Empty))
            {
                items.add(template.getName());
            }
        }

        model.setItems(items);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void onRemove()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VmTemplate a = (VmTemplate) item;
            list.add(new VmTemplateParametersBase(a.getId()));
        }

        model.startProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveVmTemplate, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }
                }, model);
    }

    private void onSave()
    {
        UnitVmModel model = (UnitVmModel) getWindow();

        if (!model.validate())
        {
            return;
        }

        String name = (String) model.getName().getEntity();

        AsyncDataProvider.isTemplateNameUnique(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        TemplateListModel templateListModel = (TemplateListModel) target;
                        boolean isNameUnique = (Boolean) returnValue;
                        templateListModel.postNameUniqueCheck(isNameUnique);

                    }
                }), name);
    }

    public void postNameUniqueCheck(boolean isNameUnique)
    {
        final UnitVmModel model = (UnitVmModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        VmTemplate selectedItem = (VmTemplate) getSelectedItem();
        final VmTemplate template = (VmTemplate) Cloner.clone(selectedItem);

        String name = (String) model.getName().getEntity();

        // Check name unicitate.
        if (!isNameUnique && name.compareToIgnoreCase(template.getName()) != 0)
        {
            model.getName()
                    .getInvalidityReasons()
                    .add(ConstantsManager.getInstance().getConstants().nameMustBeUniqueInvalidReason());
            model.getName().setIsValid(false);
            model.setIsGeneralTabValid(false);
            return;
        }

        // Save changes.
        template.setVmType((VmType) model.getVmType().getSelectedItem());
        template.setName(name);
        template.setOsId((Integer) model.getOSType().getSelectedItem());
        template.setNumOfMonitors((Integer) model.getNumOfMonitors().getSelectedItem());
        template.setAllowConsoleReconnect((Boolean) model.getAllowConsoleReconnect().getEntity());
        template.setDescription((String) model.getDescription().getEntity());
        template.setDomain(model.getDomain().getIsAvailable() ? (String) model.getDomain().getSelectedItem() : ""); //$NON-NLS-1$
        template.setMemSizeMb((Integer) model.getMemSize().getEntity());
        template.setMinAllocatedMem(((Integer) model.getMinAllocatedMemory().getEntity()));

        template.setVdsGroupId((model.getSelectedCluster()).getId());
        template.setTimeZone((model.getTimeZone().getIsAvailable() && model.getTimeZone().getSelectedItem() != null) ? ((TimeZoneModel) model.getTimeZone()
                .getSelectedItem()).getTimeZoneKey()
                : ""); //$NON-NLS-1$
        template.setNumOfSockets((Integer) model.getNumOfSockets().getSelectedItem());
        template.setCpuPerSocket(Integer.parseInt(model.getTotalCPUCores().getEntity().toString())
                / (Integer) model.getNumOfSockets().getSelectedItem());
        template.setUsbPolicy((UsbPolicy) model.getUsbPolicy().getSelectedItem());
        template.setStateless((Boolean) model.getIsStateless().getEntity());
        template.setRunAndPause((Boolean) model.getIsRunAndPause().getEntity());
        template.setDeleteProtected((Boolean) model.getIsDeleteProtected().getEntity());
        template.setSmartcardEnabled((Boolean) model.getIsSmartcardEnabled().getEntity());
        template.setDefaultBootSequence(model.getBootSequence());
        template.setIsoPath(model.getCdImage().getIsChangable() ? (String) model.getCdImage().getSelectedItem() : ""); //$NON-NLS-1$
        template.setAutoStartup((Boolean) model.getIsHighlyAvailable().getEntity());
        template.setKernelUrl((String) model.getKernel_path().getEntity());
        template.setKernelParams((String) model.getKernel_parameters().getEntity());
        template.setInitrdUrl((String) model.getInitrd_path().getEntity());
        template.setVncKeyboardLayout((String) model.getVncKeyboardLayout().getSelectedItem());

        if (model.getQuota().getIsAvailable() && model.getQuota().getSelectedItem() != null) {
            template.setQuotaId(((Quota) model.getQuota().getSelectedItem()).getId());
        }

        EntityModel displayProtocolSelectedItem = (EntityModel) model.getDisplayProtocol().getSelectedItem();
        template.setDefaultDisplayType((DisplayType) displayProtocolSelectedItem.getEntity());

        EntityModel prioritySelectedItem = (EntityModel) model.getPriority().getSelectedItem();
        template.setPriority((Integer) prioritySelectedItem.getEntity());

        // host migration configuration
        VDS defaultHost = (VDS) model.getDefaultHost().getSelectedItem();
        if ((Boolean) model.getIsAutoAssign().getEntity())
        {
            template.setDedicatedVmForVds(null);
        }
        else
        {
            template.setDedicatedVmForVds(defaultHost.getId());
        }

        template.setMigrationSupport((MigrationSupport) model.getMigrationMode().getSelectedItem());

        model.startProgress(null);

        UpdateVmTemplateParameters parameters = new UpdateVmTemplateParameters(template);
        parameters.setConsoleEnabled((Boolean) model.getIsConsoleDeviceEnabled().getEntity());
        setVmWatchdogToParams(model, parameters);
        parameters.setSoundDeviceEnabled((Boolean) model.getIsSoundcardEnabled().getEntity());

        Frontend.RunAction(VdcActionType.UpdateVmTemplate, parameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {

                        TemplateListModel localModel = (TemplateListModel) result.getState();
                        localModel.postUpdateVmTemplate(result.getReturnValue());

                    }
                }, this);
    }

    private void setVmWatchdogToParams(final UnitVmModel model, UpdateVmTemplateParameters updateVmParams) {
        VmWatchdogType wdModel = VmWatchdogType.getByName((String) model.getWatchdogModel()
                .getSelectedItem());
        updateVmParams.setUpdateWatchdog(true);
        if(wdModel != null) {
            VmWatchdog vmWatchdog = new VmWatchdog();
            vmWatchdog.setAction(VmWatchdogAction.getByName((String) model.getWatchdogAction()
                    .getSelectedItem()));
            vmWatchdog.setModel(wdModel);
            updateVmParams.setWatchdog(vmWatchdog);
        }
    }


    public void postUpdateVmTemplate(VdcReturnValueBase returnValue)
    {
        UnitVmModel model = (UnitVmModel) getWindow();

        model.stopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            cancel();
        }
    }

    private void cancel()
    {
        Frontend.Unsubscribe();

        setWindow(null);
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.selectedItemPropertyChanged(sender, e);

        if (e.PropertyName.equals("status")) //$NON-NLS-1$
        {
            updateActionAvailability();
        }
    }

    private boolean selectedItemsContainBlankTemplate()
    {
        if (getSelectedItems() != null)
        {
            ArrayList<VmTemplate> templates = Linq.<VmTemplate> cast(getSelectedItems());
            for (VmTemplate template : templates)
            {
                if (template != null && template.getId().equals(Guid.Empty))
                {
                    return true;
                }
            }
        }

        return false;
    }

    protected void updateActionAvailability()
    {
        VmTemplate item = (VmTemplate) getSelectedItem();
        ArrayList items =
                (((ArrayList) getSelectedItems()) != null) ? (ArrayList) getSelectedItems()
                        : new ArrayList();

        boolean blankSelected = isBlankTemplateSelected();

        getEditCommand().setIsExecutionAllowed(items.size() == 1 && item != null
                && item.getStatus() != VmTemplateStatus.Locked);
        if (getEditCommand().getIsExecutionAllowed() && blankSelected)
        {
            getEditCommand().getExecuteProhibitionReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .blankTemplateCannotBeEdited());
            getEditCommand().setIsExecutionAllowed(false);
        }

        getRemoveCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VmTemplate.class, VdcActionType.RemoveVmTemplate));
        if (getRemoveCommand().getIsExecutionAllowed() && blankSelected)
        {
            getRemoveCommand().getExecuteProhibitionReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .blankTemplateCannotBeRemoved());
            getRemoveCommand().setIsExecutionAllowed(false);
        }

        getExportCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VmTemplate.class, VdcActionType.ExportVmTemplate));

        if (getExportCommand().getIsExecutionAllowed() && blankSelected)
        {
            getExportCommand().getExecuteProhibitionReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .blankTemplateCannotBeExported());
            getExportCommand().setIsExecutionAllowed(false);
        }
    }

    /**
     * Returns true if at least one of the selected items is the blank template
     */
    protected boolean isBlankTemplateSelected() {
        if (isBlankTemplate(getSelectedItem())) {
            return true;
        }

        if (getSelectedItems() == null) {
            return false;
        }

        for (Object selectedItem : getSelectedItems()) {
            if (isBlankTemplate(selectedItem)) {
                return true;
            }
        }

        return false;
    }

    private boolean isBlankTemplate(Object selectedItem) {
        return selectedItem != null &&
                selectedItem instanceof VmTemplate &&
                Guid.Empty.equals(((VmTemplate) selectedItem).getId());
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getEditCommand())
        {
            edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (command == getExportCommand())
        {
            export(ConstantsManager.getInstance().getConstants().exportTemplateTitle());
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnExport")) //$NON-NLS-1$
        {
            onExport();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            onSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            onRemove();
        }
    }

    @Override
    protected String getListName() {
        return "TemplateListModel"; //$NON-NLS-1$
    }

    @Override
    protected String extractNameFromEntity(VmTemplate entity) {
        return entity.getName();
    }

    @Override
    protected void sendWarningForNonExportableDisks(VmTemplate entity) {
        // no op
    }
}
