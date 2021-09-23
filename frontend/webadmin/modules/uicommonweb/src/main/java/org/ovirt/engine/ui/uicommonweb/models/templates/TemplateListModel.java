package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.ActionUtils;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ExportOvaParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VmTemplateManagementParameters;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.IconUtils;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.template.VersionNameUnitToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.FullUnitToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.UnitToGraphicsDeviceParamsBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModelChain;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.SearchStringMapping;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.ImportCloneModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExportOvaModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExportVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.IconCache;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportTemplateData;
import org.ovirt.engine.ui.uicommonweb.models.vms.NewVmFromTemplateModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.TemplateVmModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmBasedWidgetSwitchModeCommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmModelBehaviorBase;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotInCollectionValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class TemplateListModel extends VmBaseListModel<Void, VmTemplate> {

    public static final String CMD_CONFIGURE_TEMPLATES_TO_IMPORT = "ConfigureTemplatesToImport"; //$NON-NLS-1$
    public static final String CMD_CANCEL = "Cancel"; //$NON-NLS-1$
    private static final String CMD_BACK = "Back"; //$NON-NLS-1$
    private static final String CMD_IMPORT = "Import"; //$NON-NLS-1$

    protected Set<String> assignedVmNames = new HashSet<>();
    protected Map<Guid, Object> cloneObjectMap;
    protected List<ImportTemplateData> objectsToClone;
    private UIConstants constants;
    private UIMessages messages;

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

    final Provider<ImportTemplatesModel> importTemplatesModelProvider;

    private UICommand importTemplatesCommand;

    public UICommand getImportTemplateCommand() {
        return importTemplatesCommand;
    }

    public void setImportTemplateCommand(UICommand importVmCommand) {
        this.importTemplatesCommand = importVmCommand;
    }

    private UICommand privateCreateVmfromTemplateCommand;

    public UICommand getCreateVmFromTemplateCommand() {
        return privateCreateVmfromTemplateCommand;
    }

    private void setCreateVmFromTemplateCommand(UICommand value) {
        privateCreateVmfromTemplateCommand = value;
    }

    private final TemplateGeneralModel generalModel;

    public TemplateGeneralModel getGeneralModel() {
        return generalModel;
    }

    private final TemplateVmListModel vmListModel;

    public TemplateVmListModel getVmListModel() {
        return vmListModel;
    }

    private final TemplateInterfaceListModel interfaceListModel;

    public TemplateInterfaceListModel getInterfaceListModel() {
        return interfaceListModel;
    }

    private final TemplateDiskListModel diskListModel;

    public TemplateDiskListModel getDiskListModel() {
        return diskListModel;
    }

    private final TemplateStorageListModel storageListModel;

    public TemplateStorageListModel getStorageListModel() {
        return storageListModel;
    }

    private final PermissionListModel<VmTemplate> permissionListModel;

    public PermissionListModel<VmTemplate> getPermissionListModel() {
        return permissionListModel;
    }

    private final TemplateEventListModel eventListModel;

    public TemplateEventListModel getEventListModel() {
        return eventListModel;
    }

    @Inject
    public TemplateListModel(final TemplateGeneralModel templateGeneralModel,
            final TemplateVmListModel templateVmListModel,
            final TemplateInterfaceListModel templateInterfaceListModel,
            final TemplateStorageListModel templateStorageListModel,
            final TemplateDiskListModel templateDiskListModel,
            final TemplateEventListModel templateEventListModel,
            final Provider<ImportTemplatesModel> importTemplatesModelProvider,
            final PermissionListModel<VmTemplate> permissionListModel) {
        this.generalModel = templateGeneralModel;
        this.vmListModel = templateVmListModel;
        this.interfaceListModel = templateInterfaceListModel;
        this.diskListModel = templateDiskListModel;
        this.storageListModel = templateStorageListModel;
        this.permissionListModel = permissionListModel;
        this.eventListModel = templateEventListModel;
        this.importTemplatesModelProvider = importTemplatesModelProvider;

        constants = ConstantsManager.getInstance().getConstants();
        messages = ConstantsManager.getInstance().getMessages();
        List<HasEntity<VmTemplate>> list = new ArrayList<>();
        setDetailList(list);
        addCustomModelsDetailModelList(list, 3);
        setDetailModels(list);
        setTitle(ConstantsManager.getInstance().getConstants().templatesTitle());
        setApplicationPlace(WebAdminApplicationPlaces.templateMainPlace);

        setDefaultSearchString(SearchStringMapping.TEMPLATE_DEFAULT_SEARCH + ":"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.TEMPLATE_OBJ_NAME, SearchObjects.TEMPLATE_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setExportCommand(new UICommand("Export", this)); //$NON-NLS-1$
        setExportOvaCommand(new UICommand("ExportOva", this)); //$NON-NLS-1$
        setImportTemplateCommand(new UICommand("ImportTemplate", this)); //$NON-NLS-1$
        setCreateVmFromTemplateCommand(new UICommand("CreateVM", this)); //$NON-NLS-1$

        updateActionsAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    private void setDetailList(final List<HasEntity<VmTemplate>> list) {
        list.add(generalModel);
        list.add(vmListModel);
        list.add(interfaceListModel);
        list.add(storageListModel);
    }

    @Override
    protected void setupExportModel(ExportVmModel model) {
        super.setupExportModel(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().exportTemplateTitle());
        model.setHelpTag(HelpTag.export_template);
        model.setHashName("export_template"); //$NON-NLS-1$
        model.getCollapseSnapshots().setIsAvailable(false);
    }

    @Override
    protected void setupExportOvaModel(ExportOvaModel model) {
        super.setupExportOvaModel(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().exportTemplateAsOvaTitle());
        model.setHelpTag(HelpTag.export_template);
        model.setHashName("export_template"); //$NON-NLS-1$
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
    protected QueryType getEntityExportDomain() {
        return QueryType.GetTemplatesFromExportDomain;
    }

    @Override
    protected Guid extractStoragePoolIdNullSafe(VmTemplate entity) {
        return entity.getStoragePoolId();
    }

    @Override
    protected boolean entitiesSelectedOnDifferentDataCenters() {
        List<VmTemplate> templates = getSelectedItems();

        Map<Guid, ArrayList<VmTemplate>> t = new HashMap<>();
        for (VmTemplate a : templates) {
            if (!a.getId().equals(Guid.Empty)) {
                if (!t.containsKey(a.getStoragePoolId())) {
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

    private void onExport() {
        ExportVmModel model = (ExportVmModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (!model.validate()) {
            return;
        }

        model.startProgress();

        getTemplatesNotPresentOnExportDomain();
    }

    public void onExportOva() {
        ExportOvaModel model = (ExportOvaModel) getWindow();
        if (!model.validate()) {
            return;
        }

        model.startProgress();

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VmTemplate template = (VmTemplate) item;
            ExportOvaParameters parameters = new ExportOvaParameters();
            parameters.setEntityId(template.getId());
            parameters.setEntityType(VmEntityType.TEMPLATE);
            parameters.setProxyHostId(model.getProxy().getSelectedItem().getId());
            parameters.setDirectory(model.getPath().getEntity());
            parameters.setName(model.getName().getEntity());

            list.add(parameters);
        }

        Frontend.getInstance().runMultipleAction(ActionType.ExportVmTemplateToOva, list,
                result -> {
                    ExportOvaModel localModel = (ExportOvaModel) result.getState();
                    localModel.stopProgress();
                    cancel();
                }, model);
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
                                ArrayList<String> verTempMissingBase = new ArrayList<>();

                                // check if relevant templates are already there
                                for (Object selectedItem : getSelectedItems()) {
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

                                postExportGetMissingTemplates(verTempMissingBase);
                            }),
                    storagePool.getId(),
                    storageDomainId);
        }
    }

    private void postExportGetMissingTemplates(ArrayList<String> missingTemplatesFromVms) {
        ExportVmModel model = (ExportVmModel) getWindow();

        if (!missingTemplatesFromVms.isEmpty()) {
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

            UICommand tempVar = UICommand.createDefaultOkUiCommand("OnExportNoTemplates", this); //$NON-NLS-1$
            confirmModel.getCommands().add(tempVar);
            UICommand tempVar2 = UICommand.createCancelUiCommand("CancelConfirmation", this); //$NON-NLS-1$
            confirmModel.getCommands().add(tempVar2);
        } else {
            doExport();
        }
    }

    private void doExport() {
        ExportVmModel model = (ExportVmModel) getWindow();

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VmTemplate a = (VmTemplate) item;
            if (a.getId().equals(Guid.Empty)) {
                continue;
            }
            MoveOrCopyParameters tempVar =
                    new MoveOrCopyParameters(a.getId(),
                            model.getStorage().getSelectedItem().getId());
            tempVar.setForceOverride(model.getForceOverride().getEntity());
            list.add(tempVar);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.ExportVmTemplate, list,
                result -> {

                    ExportVmModel localModel = (ExportVmModel) result.getState();
                    localModel.stopProgress();
                    cancel();

                }, model);
    }

    protected void addCustomModelsDetailModelList(final List<HasEntity<VmTemplate>> list,
            int customPosition) {
        list.add(customPosition, diskListModel);
        list.add(eventListModel);
        list.add(permissionListModel);
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("template"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        SearchParameters tempVar = new SearchParameters(applySortOptions(getSearchString()), SearchType.VmTemplate, isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(QueryType.Search, tempVar);
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

        Map<Guid, VmTemplate> templateIdToTemplate = new HashMap<>();
        for (VmTemplate template : items) {
            templateIdToTemplate.put(template.getId(), template);
        }

        templateIdToBaseTemplateName = new HashMap<>();
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

    private void edit() {
        VmTemplate template = getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        // populating VMInit
        AsyncDataProvider.getInstance().getTemplateById(new AsyncQuery<>(result -> vmInitLoaded(result)), template.getId());

    }

    private void vmInitLoaded(VmTemplate template) {
        UnitVmModel model = createModel(createBehavior(template));
        model.setIsAdvancedModeLocalStorageKey(getEditTemplateAdvancedModelKey());
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().editTemplateTitle());
        model.setHelpTag(HelpTag.edit_template);
        model.setHashName("edit_template"); //$NON-NLS-1$
        model.setCustomPropertiesKeysList(AsyncDataProvider.getInstance().getCustomPropertiesList());

        model.initialize();

        VmBasedWidgetSwitchModeCommand switchModeCommand = new VmBasedWidgetSwitchModeCommand();
        switchModeCommand.init(model);
        model.getCommands().add(switchModeCommand);


        UICommand onSaveCommand = UICommand.createDefaultOkUiCommand("OnSaveConfirm", this); //$NON-NLS-1$
        model.getCommands().add(onSaveCommand);

        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);

        model.getIsHighlyAvailable().setEntity(template.isAutoStartup());
        if (template.getDefaultDisplayType() == DisplayType.none) {
            model.getIsHeadlessModeEnabled().setEntity(true);
        }

    }

    protected String getEditTemplateAdvancedModelKey() {
        return "wa_template_dialog"; //$NON-NLS-1$
    }

    private UnitVmModel createModel(VmModelBehaviorBase behavior) {
        if (behavior.isBlankTemplateBehavior()) {
            return new BlankTemplateModel(behavior, this);
        }

        return new UnitVmModel(behavior, this);
    }

    protected VmModelBehaviorBase createBehavior(VmTemplate template) {
        if (!template.isBlank()) {
            return new TemplateVmModelBehavior(template);
        }

        return new ExistingBlankTemplateModelBehavior(template);
    }

    private void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeTemplatesTitle());
        model.setHelpTag(HelpTag.remove_template);
        model.setHashName("remove_template"); //$NON-NLS-1$

        ArrayList<String> items = new ArrayList<>();
        List<VmTemplate> templates = getSelectedItems();
        for (VmTemplate template : templates) {
            if (!template.getId().equals(Guid.Empty)) {
                items.add(template.getName() + getTemplateVersionNameAndNumber(template));
            }
        }

        model.setItems(items);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    private void onRemove() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VmTemplate a = (VmTemplate) item;
            list.add(new VmTemplateManagementParameters(a.getId()));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.RemoveVmTemplate, list,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();

                }, model);
    }

    /**
     * In case the template is not base, return template's version name and version number in the format:
     * " (Version: version-name (version-number))"
     */
    private String getTemplateVersionNameAndNumber(VmTemplate template) {
        if (template.isBaseTemplate()) {
            return ""; //$NON-NLS-1$
        }

        return ConstantsManager.getInstance().getMessages().templateVersionNameAndNumber(template.getTemplateVersionName() != null ? template.getTemplateVersionName() : "", //$NON-NLS-1$
                template.getTemplateVersionNumber());
    };

    private void onSaveConfirm() {
        UnitVmModel model = (UnitVmModel) getWindow();

        if (!model.validate()) {
            return;
        }

        Guid templateId;

        if (model.getBehavior().isBlankTemplateBehavior()) {
            templateId = ((ExistingBlankTemplateModelBehavior) model.getBehavior()).getVmTemplate().getId();
        } else {
            templateId = ((TemplateVmModelBehavior) model.getBehavior()).getVmTemplate().getId();
        }

        ConfirmationModelChain chain = new ConfirmationModelChain();
        chain.addConfirmation(new TpmDataRemovalConfirmation(model, templateId));
        chain.addConfirmation(new NvramDataRemovalConfirmation(model, templateId));
        chain.execute(this, () -> onSave());
    }

    private void onSave() {
        UnitVmModel model = (UnitVmModel) getWindow();

        final String name = model.getName().getEntity();

        boolean isBaseTemplate = false;

        if (model.getBehavior().isExistingTemplateBehavior()) {
            isBaseTemplate = ((TemplateVmModelBehavior) model.getBehavior()).getVmTemplate().isBaseTemplate();
        } else if (model.getBehavior().isBlankTemplateBehavior()) {
            isBaseTemplate = true;
        }

        if (isBaseTemplate) {
            AsyncDataProvider.getInstance().isTemplateNameUnique(new AsyncQuery<>(
                    isNameUnique -> {

                        if (model.getBehavior().isExistingTemplateBehavior()) {
                            selectedItem = ((TemplateVmModelBehavior) model.getBehavior()).getVmTemplate();
                        } else {
                            selectedItem = ((ExistingBlankTemplateModelBehavior) model.getBehavior()).getVmTemplate();
                        }

                        if (!isNameUnique && name.compareToIgnoreCase(selectedItem.getName()) != 0) {
                            model.getName()
                                    .getInvalidityReasons()
                                    .add(ConstantsManager.getInstance()
                                            .getConstants()
                                            .nameMustBeUniqueInvalidReason());
                            model.getName().setIsValid(false);
                            model.setValidTab(TabName.GENERAL_TAB, false);
                            model.fireValidationCompleteEvent();
                            return;
                        }

                        String selectedCpu = model.getCustomCpu().getSelectedItem();
                        if (selectedCpu != null && !selectedCpu.isEmpty()  && !model.getCustomCpu().getItems().contains(selectedCpu)) {
                            ConfirmationModel confirmModel = new ConfirmationModel();
                            confirmModel.setTitle(ConstantsManager.getInstance().getConstants().vmUnsupportedCpuTitle());
                            confirmModel.setMessage(ConstantsManager.getInstance().getConstants().vmUnsupportedCpuMessage());
                            confirmModel.setHelpTag(HelpTag.edit_unsupported_cpu);
                            confirmModel.setHashName("edit_unsupported_cpu"); //$NON-NLS-1$

                            confirmModel.getCommands().add(new UICommand("postNameUniqueCheck", TemplateListModel.this) //$NON-NLS-1$
                                    .setTitle(ConstantsManager.getInstance().getConstants().ok())
                                    .setIsDefault(true));

                            confirmModel.getCommands()
                                    .add(UICommand.createCancelUiCommand("CancelConfirmation", TemplateListModel.this)); //$NON-NLS-1$

                            setConfirmWindow(confirmModel);
                        } else {
                            postNameUniqueCheck();
                        }

                    }), name, model.getSelectedDataCenter() == null ? null : model.getSelectedDataCenter().getId());
        } else {
            postNameUniqueCheck();
        }
    }

    private void createVMFromTemplate() {
        VmTemplate template = getSelectedItem();

        final List<UICommand> commands = new ArrayList<>();
        commands.add(UICommand.createDefaultOkUiCommand("OnSaveVm", this)); //$NON-NLS-1$
        commands.add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$

        AsyncDataProvider.getInstance().getTemplateById(new AsyncQuery<>(withVmInit -> setupNewVmModel(new UnitVmModel(new NewVmFromTemplateModelBehavior(withVmInit), TemplateListModel.this),
                withVmInit.getVmType(), commands)), template.getId());
    }

    private void onSaveVm() {
        UnitVmModel model = (UnitVmModel) getWindow();
        String name = model.getName().getEntity();
        setcurrentVm(new VM());
        validateVm(model, name);
    }

    public void postNameUniqueCheck() {
        final UnitVmModel model = (UnitVmModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }


        VmTemplate selectedItem;
        if (model.getBehavior().isExistingTemplateBehavior()) {
            selectedItem = ((TemplateVmModelBehavior) model.getBehavior()).getVmTemplate();
        } else {
            selectedItem = ((ExistingBlankTemplateModelBehavior) model.getBehavior()).getVmTemplate();
        }

        final VmTemplate template = (VmTemplate) Cloner.clone(selectedItem);

        final String iconForParameters = IconCache.getInstance().getIcon(selectedItem.getLargeIconId()).equals(
                model.getIcon().getEntity().getIcon())
                ? null
                : IconUtils.filterPredefinedIcons(model.getIcon().getEntity().getIcon());


        // Save changes.
        buildTemplateOnSave(model, template);
        template.setCreatedByUserId(selectedItem.getCreatedByUserId());

        model.startProgress();

        template.setVmInit(model.getVmInitModel().buildCloudInitParameters(model));
        UpdateVmTemplateParameters parameters = new UpdateVmTemplateParameters(template);
        parameters.setVmLargeIcon(iconForParameters);
        parameters.setConsoleEnabled(model.getIsConsoleDeviceEnabled().getEntity());
        setVmWatchdogToParams(model, parameters);
        BuilderExecutor.build(model, parameters, new UnitToGraphicsDeviceParamsBuilder());
        parameters.setSoundDeviceEnabled(model.getIsSoundcardEnabled().getEntity());
        parameters.setTpmEnabled(model.getTpmEnabled().getEntity());
        setVmRngDeviceToParams(model, parameters);
        parameters.setVirtioScsiEnabled(model.getIsVirtioScsiEnabled().getEntity());

        if (model.getIsHeadlessModeEnabled().getEntity()) {
            parameters.getVmTemplateData().setDefaultDisplayType(DisplayType.none);
        }

        Frontend.getInstance().runAction(ActionType.UpdateVmTemplate, parameters,
                result -> {
                    TemplateListModel localModel = (TemplateListModel) result.getState();
                    localModel.postUpdateVmTemplate(result.getReturnValue());

                }, this);
    }

    @SuppressWarnings("unchecked")
    protected static void buildTemplateOnSave(UnitVmModel model, VmTemplate template) {
        BuilderExecutor.build(model, template,
                new FullUnitToVmBaseBuilder<VmTemplate>(),
                new VersionNameUnitToVmBaseBuilder());
        template.setSealed(model.getIsSealed().getEntity());
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

    public void postUpdateVmTemplate(ActionReturnValue returnValue) {
        UnitVmModel model = (UnitVmModel) getWindow();

        model.stopProgress();

        if (returnValue != null && returnValue.getSucceeded()) {
            cancel();
        }
    }

    @Override
    protected void cancel() {
        cancelConfirmation();

        setWindow(null);

        fireModelChangeRelevantForActionsEvent();
    }

    private boolean selectedItemsContainBlankTemplate() {
        if (getSelectedItems() != null) {
            List<VmTemplate> templates = getSelectedItems();
            for (VmTemplate template : templates) {
                if (template != null && template.getId().equals(Guid.Empty)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void onModelChangeRelevantForActions() {
        super.onModelChangeRelevantForActions();
        updateActionsAvailability();
    }

    private void updateActionsAvailability() {
        VmTemplate item = getSelectedItem();
        ArrayList items =
                (getSelectedItems() != null) ? (ArrayList) getSelectedItems()
                        : new ArrayList();

        boolean blankSelected = isBlankTemplateSelected();

        getEditCommand().setIsExecutionAllowed(items.size() == 1 && item != null
                && ActionUtils.canExecute(items, VmTemplate.class, ActionType.UpdateVmTemplate));

        getRemoveCommand().setIsExecutionAllowed(items.size() > 0
                && ActionUtils.canExecute(items, VmTemplate.class, ActionType.RemoveVmTemplate));
        if (getRemoveCommand().getIsExecutionAllowed() && blankSelected) {
            getRemoveCommand().getExecuteProhibitionReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .blankTemplateCannotBeRemoved());
            getRemoveCommand().setIsExecutionAllowed(false);
        }

        getExportCommand().setIsExecutionAllowed(items.size() > 0
                && ActionUtils.canExecute(items, VmTemplate.class, ActionType.ExportVmTemplate));

        if (getExportCommand().getIsExecutionAllowed() && blankSelected) {
            getExportCommand().getExecuteProhibitionReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .blankTemplateCannotBeExported());
            getExportCommand().setIsExecutionAllowed(false);
        }

        getExportOvaCommand().setIsExecutionAllowed(items.size() > 0
                && ActionUtils.canExecute(items, VmTemplate.class, ActionType.ExportVmTemplate));

        if (getExportOvaCommand().getIsExecutionAllowed() && blankSelected) {
            getExportOvaCommand().getExecuteProhibitionReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .blankTemplateCannotBeExported());
            getExportOvaCommand().setIsExecutionAllowed(false);
        }

        getCreateVmFromTemplateCommand().setIsExecutionAllowed(items.size() == 1 && item != null
                && item.getStatus() != VmTemplateStatus.Locked);
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
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getEditCommand()) {
            edit();
        } else if (command == getRemoveCommand()) {
            remove();
        } else if (command == getExportCommand()) {
            export();
        } else if (command == getExportOvaCommand()) {
            exportOva();
        } else if (command == getImportTemplateCommand()) {
            importTemplates();
        } else if (command == getCreateVmFromTemplateCommand()) {
            createVMFromTemplate();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if ("OnExport".equals(command.getName())) { //$NON-NLS-1$
            onExport();
        } else if ("OnExportOva".equals(command.getName())) { //$NON-NLS-1$
            onExportOva();
        } else if ("OnSaveConfirm".equals(command.getName())) { //$NON-NLS-1$
            onSaveConfirm();
        } else if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        } else if ("OnSaveVm".equals(command.getName())) { //$NON-NLS-1$
            onSaveVm();
        } else if ("postNameUniqueCheck".equals(command.getName())) { //$NON-NLS-1$
            postNameUniqueCheck();
            setConfirmWindow(null);
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        } else if ("OnExportNoTemplates".equals(command.getName())) { //$NON-NLS-1$
            doExport();
        } else if ("CancelConfirmation".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirmation();
        } else if (CMD_CONFIGURE_TEMPLATES_TO_IMPORT.equals(command.getName())) { // $NON-NLS-1$
            onConfigureTemplatesToImport();
        } else if ("onClone".equals(command.getName())){ //$NON-NLS-1$
            onClone();
        } else if ("closeClone".equals(command.getName())) { //$NON-NLS-1$
            closeClone();
        }
    }

    private void onConfigureTemplatesToImport() {
        final ImportTemplatesModel importTemplatesModel = (ImportTemplatesModel) getWindow();
        if (importTemplatesModel == null || !importTemplatesModel.validateSelectedTemplates()) {
            return;
        }

        setWindow(null); // remove import-templates window first
        setWindow(importTemplatesModel.getSpecificImportModel());
    }

    private void importTemplates() {
        if (getWindow() != null) {
            return;
        }

        final ImportTemplatesModel model = importTemplatesModelProvider.get();
        model.init();
        setWindow(model);

        model.getCommands().add(new UICommand(CMD_CONFIGURE_TEMPLATES_TO_IMPORT, this)
                .setIsExecutionAllowed(false)
                .setTitle(ConstantsManager.getInstance().getConstants().next())
                .setIsDefault(true));

        model.getCommands().add(new UICommand(CMD_CANCEL, this)
                .setTitle(ConstantsManager.getInstance().getConstants().cancel())
                .setIsCancel(true));

        model.initImportModels(
                new UICommand(CMD_IMPORT, new BaseCommandTarget() {
                    @Override
                    public void executeCommand(UICommand uiCommand) {
                        cloneObjectMap = new HashMap<>();
                        objectsToClone = model.onRestoreTemplates();
                        if (objectsToClone == null) {
                            return;
                        }
                        executeImportClone(model);
                    }
                }).setTitle(ConstantsManager.getInstance().getConstants().ok())
                .setIsDefault(true)
                ,
                new UICommand(CMD_BACK, new BaseCommandTarget() {
                    @Override
                    public void executeCommand(UICommand uiCommand) {
                        setWindow(null); // remove current window first
                        model.clearTemplateModelsExceptItems();
                        setWindow(model);
                    }
                }).setTitle(ConstantsManager.getInstance().getConstants().back())
                ,
                new UICommand(CMD_CANCEL, this).setIsCancel(true)
                .setTitle(ConstantsManager.getInstance().getConstants().cancel())
                );
    }

    private void executeImportClone(ImportTemplatesModel model) {
        // TODO: support running numbers (for suffix)
        if (objectsToClone.size() == 0) {
            clearCachedAssignedVmNames();
            executeImport(model);
            return;
        }
        ImportCloneModel entity = new ImportCloneModel();
        Object object = objectsToClone.iterator().next();
        entity.setImportTemplatesModel(model);
        entity.setEntity(object);
        entity.setTitle(getImportConflictTitle());
        entity.setHelpTag(HelpTag.import_conflict);
        entity.setHashName("import_conflict"); //$NON-NLS-1$
        entity.getCommands().add(UICommand.createDefaultOkUiCommand("onClone", this)); //$NON-NLS-1$
        entity.getCommands().add(UICommand.createCancelUiCommand("closeClone", this)); //$NON-NLS-1$

        setConfirmWindow(entity);
    }

    protected IValidation[] getClonedAppendedNameValidators() {
        final int maxClonedNameLength = getMaxClonedNameLength();
        return new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(maxClonedNameLength),
                new I18NNameValidation(ConstantsManager.getInstance()
                        .getMessages()
                        .newNameWithSuffixCannotContainBlankOrSpecialChars(maxClonedNameLength)),
                new UniqueClonedAppendedNameValidator(assignedVmNames)
        };
    }

    protected int getMaxClonedNameLength() {
        return UnitVmModel.VM_TEMPLATE_AND_INSTANCE_TYPE_NAME_MAX_LIMIT;
    }

    protected void executeImport(ImportTemplatesModel model) {
        model.executeImport(cloneObjectMap, result -> {
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

    protected String getImportConflictTitle() {
        return constants.importTemplateConflictTitle();
    }

    private void setObjectName(ImportTemplateData templateData, String input, boolean isSuffix) {
        String nameForTheClonedVm = isSuffix ? getObjectName(templateData) + input : input;
        setObjectName(templateData, nameForTheClonedVm);
        assignedVmNames.add(nameForTheClonedVm);
    }

    protected String getObjectName(ImportTemplateData templateData) {
        return templateData.getTemplate().getName();
    }

    protected void setObjectName(ImportTemplateData templateData, String name) {
        templateData.getTemplate().setName(name);
    }

    class UniqueClonedNameValidator extends NotInCollectionValidation {

        public UniqueClonedNameValidator(Collection<?> collection) {
            super(collection);
        }

        @Override
        public ValidationResult validate(Object value) {
            ValidationResult result = super.validate(value);
            if (!result.getSuccess()) {
                result.getReasons().add(getAlreadyAssignedClonedNameMessage());
            }
            return result;
        }
    }

    class UniqueClonedAppendedNameValidator extends NotInCollectionValidation {

        public UniqueClonedAppendedNameValidator(Collection<?> collection) {
            super(collection);
        }

        @Override
        public ValidationResult validate(Object value) {
            ValidationResult result = super.validate(value);
            if (!result.getSuccess()) {
                result.getReasons().add(getSuffixCauseToClonedNameCollisionMessage((String) value));
            }
            return result;
        }
    }

    protected String getAlreadyAssignedClonedNameMessage() {
        return messages.alreadyAssignedClonedTemplateName();
    }

    protected String getSuffixCauseToClonedNameCollisionMessage(String existingName) {
        return messages.suffixCauseToClonedTemplateNameCollision(existingName);
    }

    private void onClone() {
        ImportCloneModel cloneModel = (ImportCloneModel) getConfirmWindow();
        if (cloneModel.getApplyToAll().getEntity()) {
            if (!cloneModel.getNoClone().getEntity()) {
                String suffix = cloneModel.getSuffix().getEntity();
                if (!validateSuffix(suffix, cloneModel.getSuffix())) {
                    return;
                }
                for (ImportTemplateData object : objectsToClone) {
                    setObjectName(object, suffix, true);
                    cloneObjectMap.put((Guid) object.getEntity().getQueryableId(),
                            object);
                }
            }
            objectsToClone.clear();
        } else {
            ImportTemplateData object = (ImportTemplateData) cloneModel.getEntity();
            if (!cloneModel.getNoClone().getEntity()) {
                String vmName = cloneModel.getName().getEntity();
                if (!validateName(vmName, cloneModel.getName(), getClonedNameValidators())) {
                    return;
                }
                setObjectName(object, vmName, false);
                cloneObjectMap.put((Guid) object.getEntity().getQueryableId(),
                        object);
            }
            objectsToClone.remove(object);
        }

        setConfirmWindow(null);
        executeImportClone(cloneModel.getImportTemplatesModel());
    }

    protected boolean validateSuffix(String suffix, EntityModel entityModel) {
        return objectsToClone.stream()
                .map(itd -> itd.getTemplate().getName())
                .allMatch(n -> validateName(n + suffix, entityModel, getClonedAppendedNameValidators()));
    }

    protected boolean validateName(String newVmName, EntityModel<String> entity, IValidation[] validators) {
        EntityModel<String> temp = new EntityModel<>();
        temp.setIsValid(true);
        temp.setEntity(newVmName);
        temp.validateEntity(validators);
        if (!temp.getIsValid()) {
            entity.setInvalidityReasons(temp.getInvalidityReasons());
            entity.setIsValid(false);
        }

        return temp.getIsValid();
    }

    protected IValidation[] getClonedNameValidators() {
        final int maxClonedNameLength = getMaxClonedNameLength();
        return new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(maxClonedNameLength),
                new I18NNameValidation(ConstantsManager.getInstance()
                        .getMessages()
                        .nameMustConataionOnlyAlphanumericChars(maxClonedNameLength)),
                new UniqueClonedNameValidator(assignedVmNames)
        };
    }

    private void closeClone() {
        setConfirmWindow(null);
        clearCachedAssignedVmNames();
    }

    private void clearCachedAssignedVmNames() {
        assignedVmNames.clear();
    }

    protected void exportOva() {
        VmTemplate selectedEntity = getSelectedItem();
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

    @Override
    protected void setupNewVmModel(UnitVmModel model,
            VmType vmType,
            List<UICommand> uiCommands) {
        super.setupNewVmModel(model, vmType, uiCommands);
        model.getProvisioning().setEntity(vmType == VmType.Server || vmType == VmType.HighPerformance);
    }

}
