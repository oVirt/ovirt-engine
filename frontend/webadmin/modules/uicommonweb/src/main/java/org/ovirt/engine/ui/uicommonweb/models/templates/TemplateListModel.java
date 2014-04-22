package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.template.VersionNameUnitToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.FullUnitToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExportVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.TemplateVmModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmBasedWidgetSwitchModeCommand;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

        model.startProgress(null);

        getTemplatesNotPresentOnExportDomain();
    }

    private void getTemplatesNotPresentOnExportDomain()
    {
        ExportVmModel model = (ExportVmModel) getWindow();
        Guid storageDomainId = model.getStorage().getSelectedItem().getId();

        AsyncDataProvider.getInstance().getDataCentersByStorageDomain(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        TemplateListModel templateListModel = (TemplateListModel) target;
                        ArrayList<StoragePool> storagePools =
                                (ArrayList<StoragePool>) returnValue;
                        StoragePool storagePool = storagePools.size() > 0 ? storagePools.get(0) : null;

                        templateListModel.postGetTemplatesNotPresentOnExportDomain(storagePool);
                    }
                }), storageDomainId);
    }

    private void postGetTemplatesNotPresentOnExportDomain(StoragePool storagePool)
    {
        ExportVmModel model = (ExportVmModel) getWindow();
        Guid storageDomainId = model.getStorage().getSelectedItem().getId();

        if (storagePool != null)
        {
            AsyncDataProvider.getInstance().getAllTemplatesFromExportDomain(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {
                            TemplateListModel templateListModel = (TemplateListModel) target;
                            HashMap<VmTemplate, ArrayList<DiskImage>> templatesDiskSet =
                                    (HashMap<VmTemplate, ArrayList<DiskImage>>) returnValue;
                            ArrayList<String> verTempMissingBase = new ArrayList<String>();

                            // check if relevant templates are already there
                            for (Object selectedItem : templateListModel.getSelectedItems()) {
                                VmTemplate template = (VmTemplate) selectedItem;
                                // only relevant for template versions
                                if (!template.isBaseTemplate()) {
                                    boolean hasMatch = false;
                                    for (VmTemplate a : templatesDiskSet.keySet()) {
                                        if (template.getBaseTemplateId().equals(a.getId())) {
                                            hasMatch = true;
                                            break;
                                        }
                                    }

                                    if (!template.getBaseTemplateId().equals(Guid.Empty) && !hasMatch) {
                                        verTempMissingBase.add(template.getName());
                                    }
                                }
                            }

                            templateListModel.postExportGetMissingTemplates(verTempMissingBase);
                        }
                    }),
                    storagePool.getId(),
                    storageDomainId);
        }
    }

    private void postExportGetMissingTemplates(ArrayList<String> missingTemplatesFromVms)
    {
        ExportVmModel model = (ExportVmModel) getWindow();

        if (!missingTemplatesFromVms.isEmpty())
        {
            model.stopProgress();

            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance()
                    .getConstants()
                    .baseTemplatesNotFoundOnExportDomainTitle());
            confirmModel.setHelpTag(HelpTag.base_template_not_found_on_export_domain);
            confirmModel.setHashName("base_template_not_found_on_export_domain"); //$NON-NLS-1$

            confirmModel.setMessage(ConstantsManager.getInstance()
                            .getConstants()
                            .theFollowingTemplatesAreMissingOnTargetExportDomainForTemplateVersionsMsg());
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
            doExport();
        }
    }

    private void doExport()
    {
        ExportVmModel model = (ExportVmModel) getWindow();

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
                            model.getStorage().getSelectedItem().getId());
            tempVar.setForceOverride(model.getForceOverride().getEntity());
            list.add(tempVar);
        }

        model.startProgress(null);

        Frontend.getInstance().runMultipleAction(VdcActionType.ExportVmTemplate, list,
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
        SearchParameters tempVar = new SearchParameters(applySortOptions(getSearchString()), SearchType.VmTemplate, isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    public boolean supportsServerSideSorting() {
        return true;
    }

    @Override
    public void setItems(Collection value) {
        genVersionToBaseTemplate(value);
        super.setItems(value);
    }

    private Map<Guid, String> templateIdToBaseTemplateName;

    private void genVersionToBaseTemplate(Iterable<VmTemplate> items) {
        if (items == null) {
            templateIdToBaseTemplateName = null;
            return;
        }

        Map<Guid, VmTemplate> templateIdToTemplate = new HashMap<Guid, VmTemplate>();
        for (VmTemplate template : items) {
            templateIdToTemplate.put(template.getId(), template);
        }

        templateIdToBaseTemplateName = new HashMap<Guid, String>();
        for (VmTemplate template : items) {
            VmTemplate baseTemplate = templateIdToTemplate.get(template.getBaseTemplateId());
            templateIdToBaseTemplateName.put(template.getId(),
                    baseTemplate != null ? baseTemplate.getName() : "");  //$NON-NLS-1$
        }
    }

    public String resolveBaseTemplateNameForTemplate(Guid templateId) {
        if (templateIdToBaseTemplateName == null) {
            return ""; //$NON-NLS-1$
        }

        return templateIdToBaseTemplateName.get(templateId);
    }

    private void edit()
    {
        VmTemplate template = (VmTemplate) getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        // populating VMInit
        AsyncQuery getVmInitQuery = new AsyncQuery();
        getVmInitQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                vmInitLoaded((VmTemplate) result);
            }
        };
        AsyncDataProvider.getInstance().getTemplateById(getVmInitQuery, template.getId());

    }

    private void vmInitLoaded(VmTemplate template) {
        UnitVmModel model = new UnitVmModel(createBehavior(template));
        model.setIsAdvancedModeLocalStorageKey(getEditTemplateAdvancedModelKey());
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().editTemplateTitle());
        model.setHelpTag(HelpTag.edit_template);
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

    protected String getEditTemplateAdvancedModelKey() {
        return "wa_template_dialog"; //$NON-NLS-1$
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
        model.setHelpTag(HelpTag.remove_template);
        model.setHashName("remove_template"); //$NON-NLS-1$

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

        Frontend.getInstance().runMultipleAction(VdcActionType.RemoveVmTemplate, list,
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

        String name = model.getName().getEntity();

        if (((TemplateVmModelBehavior) model.getBehavior()).getVmTemplate().isBaseTemplate()) {

            AsyncDataProvider.getInstance().isTemplateNameUnique(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            TemplateListModel templateListModel = (TemplateListModel) target;
                            boolean isNameUnique = (Boolean) returnValue;
                            templateListModel.postNameUniqueCheck(isNameUnique);

                        }
                    }), name);
        } else {
            postNameUniqueCheck(true);
        }
    }

    public void postNameUniqueCheck(boolean isNameUnique)
    {
        final UnitVmModel model = (UnitVmModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        VmTemplate selectedItem = ((TemplateVmModelBehavior) model.getBehavior()).getVmTemplate();
        final VmTemplate template = (VmTemplate) Cloner.clone(selectedItem);

        String name = model.getName().getEntity();

        // Check name unicitate.
        if (!isNameUnique && name.compareToIgnoreCase(template.getName()) != 0)
        {
            model.getName()
                    .getInvalidityReasons()
                    .add(ConstantsManager.getInstance().getConstants().nameMustBeUniqueInvalidReason());
            model.getName().setIsValid(false);
            model.setValidTab(TabName.GENERAL_TAB, false);
            return;
        }

        // Save changes.
        buildTemplateOnSave(model, template);
        template.setCreatedByUserId(selectedItem.getCreatedByUserId());

        model.startProgress(null);

        template.setVmInit(model.getVmInitModel().buildCloudInitParameters(model));
        UpdateVmTemplateParameters parameters = new UpdateVmTemplateParameters(template);
        parameters.setConsoleEnabled(model.getIsConsoleDeviceEnabled().getEntity());
        setVmWatchdogToParams(model, parameters);
        parameters.setSoundDeviceEnabled(model.getIsSoundcardEnabled().getEntity());
        setVmRngDeviceToParams(model, parameters);

        Frontend.getInstance().runAction(VdcActionType.UpdateVmTemplate, parameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {

                        TemplateListModel localModel = (TemplateListModel) result.getState();
                        localModel.postUpdateVmTemplate(result.getReturnValue());

                    }
                }, this);
    }

    @SuppressWarnings("unchecked")
    protected static void buildTemplateOnSave(UnitVmModel model, VmTemplate template) {
        BuilderExecutor.build(model, template, new FullUnitToVmBaseBuilder<VmTemplate>(), new VersionNameUnitToVmBaseBuilder());
    }

    private void setVmWatchdogToParams(final UnitVmModel model, UpdateVmTemplateParameters updateVmParams) {
        VmWatchdogType wdModel = model.getWatchdogModel().getSelectedItem();
        updateVmParams.setUpdateWatchdog(true);
        if(wdModel != null) {
            VmWatchdog vmWatchdog = new VmWatchdog();
            vmWatchdog.setAction(model.getWatchdogAction().getSelectedItem());
            vmWatchdog.setModel(wdModel);
            updateVmParams.setWatchdog(vmWatchdog);
        }
    }

    private void setVmRngDeviceToParams(UnitVmModel model, UpdateVmTemplateParameters parameters) {
        parameters.setUpdateRngDevice(true);
        parameters.setRngDevice(model.getIsRngEnabled().getEntity() ? model.generateRngDevice() : null);
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
        Frontend.getInstance().unsubscribe();

        cancelConfirmation();

        setWindow(null);

        updateActionAvailability();
    }

    private void cancelConfirmation()
    {
        setConfirmWindow(null);
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

        if (e.propertyName.equals("status")) //$NON-NLS-1$
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
                ((getSelectedItems()) != null) ? (ArrayList) getSelectedItems()
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
                && VdcActionUtils.canExecute(items, VmTemplate.class, VdcActionType.RemoveVmTemplate));
        if (getRemoveCommand().getIsExecutionAllowed() && blankSelected)
        {
            getRemoveCommand().getExecuteProhibitionReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .blankTemplateCannotBeRemoved());
            getRemoveCommand().setIsExecutionAllowed(false);
        }

        getExportCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.canExecute(items, VmTemplate.class, VdcActionType.ExportVmTemplate));

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
        else if ("Cancel".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }
        else if ("OnExport".equals(command.getName())) //$NON-NLS-1$
        {
            onExport();
        }
        else if ("OnSave".equals(command.getName())) //$NON-NLS-1$
        {
            onSave();
        }
        else if ("OnRemove".equals(command.getName())) //$NON-NLS-1$
        {
            onRemove();
        }
        else if ("OnExportNoTemplates".equals(command.getName())) //$NON-NLS-1$
        {
            doExport();
        }
        else if ("CancelConfirmation".equals(command.getName())) //$NON-NLS-1$
        {
            cancelConfirmation();
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
