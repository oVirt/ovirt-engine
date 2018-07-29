package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmTemplateImportExportParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.ImportTemplateFromExportDomainModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateImportDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportTemplateData;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotInCollectionValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class TemplateBackupModel extends ManageBackupModel<VmTemplate> {
    private List<Map.Entry<VmTemplate, List<DiskImage>>> extendedItems;
    private StoragePool pool;
    protected ImportTemplateFromExportDomainModel importModel;
    protected Provider<? extends ImportTemplateFromExportDomainModel> importModelProvider;

    /** used to save the names that were assigned for VMs which are going
     *  to be created using import in case of choosing multiple VM imports */
    protected Set<String> assignedVmNames = new HashSet<>();
    protected Map<Guid, Object> cloneObjectMap;
    protected List<ImportTemplateData> objectsToClone;

    private static UIConstants constants = ConstantsManager.getInstance().getConstants();
    private static UIMessages messages = ConstantsManager.getInstance().getMessages();

    @Inject
    public TemplateBackupModel(Provider<ImportTemplateFromExportDomainModel> importModelProvider) {
        this.importModelProvider = importModelProvider;
        setTitle(constants.templateImportTitle());
        setHelpTag(HelpTag.template_import);
        setHashName("template_import"); //$NON-NLS-1$
    }

    @Override
    protected void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(constants.removeBackedUpTemplatesTitle());
        model.setHelpTag(HelpTag.remove_backed_up_template);
        model.setHashName("remove_backed_up_template"); //$NON-NLS-1$
        List<String> items = getSelectedItems()
                .stream()
                .map(template -> template.getName() + getTemplateVersionNameAndNumber(template))
                .collect(Collectors.toList());

        model.setItems(items);

        model.setNote(constants.noteTheDeletedItemsMightStillAppearOntheSubTab());

        model.getCommands().add(UICommand.createDefaultOkUiCommand("OnRemove", this)); //$NON-NLS-1$
        model.getCommands().add(UICommand.createCancelUiCommand(CANCEL_COMMAND, this));
    }

    private void onRemove() {
        AsyncDataProvider.getInstance().getDataCentersByStorageDomain(new AsyncQuery<>(pools -> {
            if (pools != null && !pools.isEmpty()) {
                pool = pools.get(0);
                checkVmsDependentOnTemplate(pool.getId(), getEntity().getId());
            }
        }),
                getEntity().getId());
    }

    private void checkVmsDependentOnTemplate(Guid dataCenterId, Guid storageDomainId) {
        Frontend.getInstance().runQuery(QueryType.GetVmsFromExportDomain,
                new GetAllFromExportDomainQueryParameters(dataCenterId, storageDomainId),
                new AsyncQuery<>(createGetVmsFromExportDomainCallback()));
    }

    private AsyncCallback<QueryReturnValue> createGetVmsFromExportDomainCallback() {
        return returnValue -> {
            if (returnValue == null || returnValue.getReturnValue() == null || !returnValue.getSucceeded()) {
                return;
            }
            List<VM> vmsInExportDomain = returnValue.getReturnValue();
            Map<String, List<String>> problematicVmNames =
                    getDependentVMsForTemplates(vmsInExportDomain, getSelectedItems());
            if (!problematicVmNames.isEmpty()) {
                showRemoveTemplateWithDependentVMConfirmationWindow(problematicVmNames);
            } else {
                removeTemplateBackup();
            }
        };
    }

    private void showRemoveTemplateWithDependentVMConfirmationWindow(Map<String, List<String>> problematicVmNames) {
        List<String> missingTemplatesFromVms = problematicVmNames
                .entrySet()
                .stream()
                .map(e -> messages.templatesWithDependentVMs(e.getKey(), String.join(", ", e.getValue())))  //$NON-NLS-1$
                .collect(Collectors.toList());

        setConfirmWindow(null);
        ConfirmationModel confirmModel = new ConfirmationModel();
        setConfirmWindow(confirmModel);
        confirmModel.setTitle(constants.removeBackedUpTemplatesWithDependentsVMTitle());
        confirmModel.setHelpTag(HelpTag.remove_backed_up_template);
        confirmModel.setHashName("remove_backed_up_template"); //$NON-NLS-1$

        confirmModel.setMessage(constants.theFollowingTemplatesHaveDependentVmsBackupOnExportDomainMsg());
        confirmModel.setItems(missingTemplatesFromVms);

        confirmModel.getCommands().add(UICommand.createDefaultOkUiCommand("RemoveVmTemplates", this)); //$NON-NLS-1$
        confirmModel.getCommands().add(UICommand.createCancelUiCommand(CANCEL_CONFIRMATION_COMMAND, this));
    }

    private Map<String, List<String>> getDependentVMsForTemplates(List<VM> vmsInExportDomain,
            List<VmTemplate> templates) {
        // Build a map between the template ID and the template instance
        Map<Guid, VmTemplate> templateMap = Entities.businessEntitiesById(templates);
        // Build a map between the template name  and a list of dependent VMs names
        return vmsInExportDomain
                .stream()
                .filter(vm -> templateMap.containsKey(vm.getVmtGuid()))
                .collect(Collectors.groupingBy(vm -> templateMap.get(vm.getVmtGuid()).getName(),
                        Collectors.mapping(VM::getName, Collectors.toList())));
    }

    private void removeTemplateBackup() {
        List<ActionParametersBase> prms =  getSelectedItems()
                .stream()
                .map(item -> new VmTemplateImportExportParameters(item.getId(), getEntity().getId(), pool.getId()))
                .collect(Collectors.toList());
        Frontend.getInstance().runMultipleAction(ActionType.RemoveVmTemplateFromImportExport, prms);
        cancel();
    }

    @Override
    protected ArchitectureType getArchitectureFromItem(VmTemplate template) {
        return template.getClusterArch();
    }

    protected String getObjectName(ImportTemplateData templateData) {
        return templateData.getTemplate().getName();
    }

    protected void setObjectName(ImportTemplateData templateData, String name) {
        templateData.getTemplate().setName(name);
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

    protected int getMaxClonedNameLength() {
        return UnitVmModel.VM_TEMPLATE_AND_INSTANCE_TYPE_NAME_MAX_LIMIT;
    }

    protected String getAlreadyAssignedClonedNameMessage() {
        return messages.alreadyAssignedClonedTemplateName();
    }

    protected String getSuffixCauseToClonedNameCollisionMessage(String existingName) {
        return messages.suffixCauseToClonedTemplateNameCollision(existingName);
    }

    protected void executeImport() {
        ImportTemplateFromExportDomainModel model = (ImportTemplateFromExportDomainModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (!model.validate()) {
            return;
        }

        model.setCloneObjectMap(cloneObjectMap);
        model.executeImport(result -> {
            TemplateBackupModel templateBackupModel = TemplateBackupModel.this;
            templateBackupModel.getWindow().stopProgress();
            templateBackupModel.cancel();
            List<ActionReturnValue> retVals = result.getReturnValue();
            if (retVals != null && templateBackupModel.getSelectedItems().size() == retVals.size()) {

                StringBuilder importedTemplates = new StringBuilder();
                int counter = 0;
                boolean toShowConfirmWindow = false;
                for (VmTemplate template : templateBackupModel.getSelectedItems()) {
                    if (retVals.get(counter) != null && retVals.get(counter).isValid()) {
                        importedTemplates.append(template.getName()).append(", "); //$NON-NLS-1$
                        toShowConfirmWindow = true;
                    }
                    counter++;
                }
                if (toShowConfirmWindow) {
                    ConfirmationModel confirmModel = new ConfirmationModel();
                    templateBackupModel.setConfirmWindow(confirmModel);
                    confirmModel.setTitle(constants.importTemplatesTitle());
                    confirmModel.setHelpTag(HelpTag.import_template);
                    confirmModel.setHashName("import_template"); //$NON-NLS-1$
                    confirmModel.setMessage(messages.importProcessHasBegunForTemplates(StringHelper.trimEnd(importedTemplates.toString().trim(), ',')));
                    confirmModel.getCommands().add(new UICommand(CANCEL_CONFIRMATION_COMMAND, templateBackupModel) //$NON-NLS-1$
                            .setTitle(constants.close())
                            .setIsDefault(true)
                            .setIsCancel(true)
                            );
                }
            }

        });
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("storage_domain_shared_status")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null || getEntity().getStorageDomainType() != StorageDomainType.ImportExport
                || getEntity().getStorageDomainSharedStatus() != StorageDomainSharedStatus.Active) {
            setItems(Collections.emptyList());
        } else {
            AsyncDataProvider.getInstance().getDataCentersByStorageDomain(new AsyncQuery<>(list -> {
                if (list != null && list.size() > 0 && getEntity() != null) {
                    StoragePool dataCenter = list.get(0);
                    Frontend.getInstance().runQuery(QueryType.GetTemplatesFromExportDomain,
                            new GetAllFromExportDomainQueryParameters(dataCenter.getId(),
                                    getEntity().getId()), new AsyncQuery<>((AsyncCallback<QueryReturnValue>) returnValue -> {
                                        List<Map.Entry<VmTemplate, List<DiskImage>>> items1 = new ArrayList<>();
                                        Map<VmTemplate, List<DiskImage>> dictionary = returnValue.getReturnValue();
                                        List<VmTemplate> list1 = new ArrayList<>();
                                        for (Map.Entry<VmTemplate, List<DiskImage>> item : dictionary.entrySet()) {
                                            items1.add(item);
                                            VmTemplate template = item.getKey();
                                            template.setDiskList(new ArrayList<>());
                                            template.getDiskList().addAll(item.getValue());
                                            list1.add(template);
                                        }
                                        list1.sort(new LexoNumericNameableComparator<>());
                                        setItems(list1);
                                        TemplateBackupModel.this.extendedItems = items1;
                                    }));
                }
            }), getEntity().getId());
        }
    }

    @Override
    protected void restore() {
        if (getWindow() != null) {
            return;
        }

        if (!validateSingleArchitecture()) {
            return;
        }

        ImportTemplateFromExportDomainModel model = importModelProvider.get();
        model.setEntity(getEntity().getId());
        setWindow(model);
        model.startProgress();
        model.setTitle(ConstantsManager.getInstance().getConstants().importTemplatesTitle());
        model.getCommands().add(UICommand.createDefaultOkUiCommand("OnRestore", this)); //$NON-NLS-1$
        model.getCommands().add(UICommand.createCancelUiCommand(CANCEL_COMMAND, this)); //$NON-NLS-1$);
        model.init(getSelectedItems(), getEntity().getId());
        model.setTargetArchitecture(getArchitectureFromItem(getSelectedItems().get(0)));

        // Add 'Close' command
        model.setCloseCommand(new UICommand(CANCEL_COMMAND, this) //$NON-NLS-1$
        .setTitle(ConstantsManager.getInstance().getConstants().close())
        .setIsDefault(true)
        .setIsCancel(true)
        );
        ((TemplateImportDiskListModel) ((ImportTemplateFromExportDomainModel) getWindow()).getImportDiskListModel()).setExtendedItems(extendedItems);
    }

    @Override
    public void executeCommand(UICommand command) {
        switch (command.getName()) {
        case "OnRemove": //$NON-NLS-1$
            onRemove();
            break;
        case "OnRestore": //$NON-NLS-1$
            onRestore();
            break;
        case "RemoveVmTemplates": //$NON-NLS-1$
            removeTemplateBackup();
            break;
        case "onClone": //$NON-NLS-1$
            onClone();
            break;
        case "closeClone": //$NON-NLS-1$
            closeClone();
            break;
        case "multipleArchsOK": //$NON-NLS-1$
            multipleArchsOK();
            break;
        default:
            super.executeCommand(command);
        }
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
        executeImportClone();
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

    private void setObjectName(ImportTemplateData templateData, String input, boolean isSuffix) {
        String nameForTheClonedVm = isSuffix ? getObjectName(templateData) + input : input;
        setObjectName(templateData, nameForTheClonedVm);
        assignedVmNames.add(nameForTheClonedVm);
    }

    private void closeClone() {
        setConfirmWindow(null);
        clearCachedAssignedVmNames();
    }

    private void multipleArchsOK() {
        setConfirmWindow(null);
    }

    public void onRestore() {
        importModel = (ImportTemplateFromExportDomainModel) getWindow();

        if (importModel.getProgress() != null) {
            return;
        }

        if (!importModel.validate()) {
            return;
        }
        cloneObjectMap = new HashMap<>();

        objectsToClone = new ArrayList<>();
        for (Object object : importModel.getItems()) {
            ImportTemplateData item = (ImportTemplateData) object;
            if (item.getClone().getEntity()) {
                objectsToClone.add(item);
            }
        }
        executeImportClone();
    }

    @Override
    protected String getListName() {
        return "TemplateBackupModel"; //$NON-NLS-1$
    }

    protected String getImportConflictTitle() {
        return constants.importTemplateConflictTitle();
    }

    private void executeImportClone() {
        // TODO: support running numbers (for suffix)
        if (objectsToClone.size() == 0) {
            clearCachedAssignedVmNames();
            executeImport();
            return;
        }
        ImportCloneModel entity = new ImportCloneModel();
        Object object = objectsToClone.iterator().next();
        entity.setEntity(object);
        entity.setTitle(getImportConflictTitle());
        entity.setHelpTag(HelpTag.import_conflict);
        entity.setHashName("import_conflict"); //$NON-NLS-1$
        entity.getCommands().add(UICommand.createDefaultOkUiCommand("onClone", this)); //$NON-NLS-1$
        entity.getCommands().add(UICommand.createCancelUiCommand("closeClone", this)); //$NON-NLS-1$

        setConfirmWindow(entity);
    }

    private void clearCachedAssignedVmNames() {
        assignedVmNames.clear();
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

    private class UniqueClonedAppendedNameValidator extends NotInCollectionValidation {

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

    private class UniqueClonedNameValidator extends NotInCollectionValidation {

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

    /**
     * In case the template is not base, return template's version name and version number in the format:
     * " (Version: version-name (version-number))"
     */
    private String getTemplateVersionNameAndNumber(VmTemplate template) {
        if (template.isBaseTemplate()) {
            return ""; //$NON-NLS-1$
        }

        return messages.templateVersionNameAndNumber(template.getTemplateVersionName() != null ? template.getTemplateVersionName() : "", //$NON-NLS-1$
                template.getTemplateVersionNumber());
    }
}
