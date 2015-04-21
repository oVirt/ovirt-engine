package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.action.RemoveVmFromImportExportParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmFromExportDomainModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotInCollectionValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class VmBackupModel extends ManageBackupModel {

    private VmAppListModel privateAppListModel;
    protected List<Object> objectsToClone;
    /** used to save the names that were assigned for VMs which are going
     *  to be created using import in case of choosing multiple VM imports */
    protected Set<String> assignedVmNames = new HashSet<String>();
    protected Map<Guid, Object> cloneObjectMap;
    protected ImportVmFromExportDomainModel importModel;

    protected Provider<? extends ImportVmFromExportDomainModel> importModelProvider;

    public VmAppListModel getAppListModel() {
        return privateAppListModel;
    }

    protected void setAppListModel(VmAppListModel value) {
        privateAppListModel = value;
    }

    protected void setModelProvider(Provider<? extends ImportVmFromExportDomainModel> importModelProvider) {
        this.importModelProvider = importModelProvider;
    }

    @Inject
    public VmBackupModel(Provider<ImportVmFromExportDomainModel> importModelProvider) {
        this();
        setModelProvider(importModelProvider);
    }

    public VmBackupModel() {
        setTitle(ConstantsManager.getInstance().getConstants().vmImportTitle());
        setHelpTag(HelpTag.vm_import);
        setHashName("vm_import"); // //$NON-NLS-1$

        setAppListModel(new VmAppListModel());
        setIsTimerDisabled(true);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        if (getAppListModel() != null) {
            getAppListModel().setEntity(getSelectedItem());
        }
    }

    @Override
    protected void remove() {
        super.remove();

        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeBackedUpVMsTitle());
        model.setHelpTag(HelpTag.remove_backed_up_vm);
        model.setHashName("remove_backed_up_vm"); //$NON-NLS-1$

        ArrayList<String> items = new ArrayList<String>();
        for (Object item : getSelectedItems()) {
            VM vm = (VM) item;
            items.add(vm.getName());
        }
        model.setItems(items);

        model.setNote(ConstantsManager.getInstance().getConstants().noteTheDeletedItemsMightStillAppearOntheSubTab());

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

        model.startProgress(null);

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.model = this;
        _asyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                ArrayList<StoragePool> pools = (ArrayList<StoragePool>) returnValue;
                if (pools != null && pools.size() > 0) {
                    StoragePool pool = pools.get(0);
                    VmBackupModel backupModel = (VmBackupModel) model;
                    ArrayList<VdcActionParametersBase> list =
                            new ArrayList<VdcActionParametersBase>();
                    for (Object item : backupModel.getSelectedItems()) {
                        VM vm = (VM) item;
                        list.add(new RemoveVmFromImportExportParameters(vm.getId(),
                                backupModel.getEntity().getId(), pool.getId()));
                    }

                    Frontend.getInstance().runMultipleAction(
                            VdcActionType.RemoveVmFromImportExport, list,
                            new IFrontendMultipleActionAsyncCallback() {
                                @Override
                                public void executed(
                                        FrontendMultipleActionAsyncResult result) {

                                    ConfirmationModel localModel = (ConfirmationModel) result
                                            .getState();
                                    localModel.stopProgress();
                                    cancel();
                                    onEntityChanged();

                                }
                            }, backupModel.getWindow());
                }
            }
        };
        AsyncDataProvider.getInstance().getDataCentersByStorageDomain(_asyncQuery,
                getEntity().getId());
    }

    protected ArchitectureType getArchitectureFromItem(Object item) {
        VM vm = (VM) item;

        return vm.getClusterArch();
    }

    @Override
    protected void restore() {
        super.restore();

        if (getWindow() != null) {
            return;
        }

        // Checks if there are selected VMs of multiple architectures
        ArchitectureType firstArch = null;
        boolean multipleArchs = false;

        for (Object item : getSelectedItems()) {
            ArchitectureType arch = getArchitectureFromItem(item);

            if (firstArch == null) {
                firstArch = arch;
            } else {
                if (!firstArch.equals(arch)) {
                    multipleArchs = true;
                    break;
                }
            }
        }

        if (multipleArchs) {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance().getConstants().invalidImportTitle());
            confirmModel.setHelpTag(HelpTag.multiple_archs_dialog);
            confirmModel.setHashName("multiple_archs_dialog"); //$NON-NLS-1$
            confirmModel.setMessage(ConstantsManager.getInstance().getConstants().invalidImportMsg());

            UICommand command = UICommand.createDefaultOkUiCommand("multipleArchsOK", this); //$NON-NLS-1$
            confirmModel.getCommands().add(command);

            setConfirmWindow(confirmModel);

            return;
        }

        ImportVmFromExportDomainModel model = importModelProvider.get();
        model.setEntity(getEntity().getId());
        setWindow(model);
        model.startProgress(null);
        UICommand restoreCommand;
        restoreCommand = UICommand.createDefaultOkUiCommand("OnRestore", this); //$NON-NLS-1$
        model.getCommands().add(restoreCommand);
        UICommand tempVar3 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar3);
        model.init(getSelectedItems(), getEntity().getId());
        model.setTargetArchitecture(firstArch);

        // Add 'Close' command
        UICommand closeCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        closeCommand.setTitle(ConstantsManager.getInstance().getConstants().close());
        closeCommand.setIsDefault(true);
        closeCommand.setIsCancel(true);
        model.setCloseCommand(closeCommand);
    }

    public void onRestore() {
        importModel = (ImportVmFromExportDomainModel) getWindow();

        if (importModel.getProgress() != null) {
            return;
        }

        if (!importModel.validate()) {
            return;
        }
        cloneObjectMap = new HashMap<Guid, Object>();

        objectsToClone = new ArrayList<Object>();
        for (Object object : (ArrayList<Object>) importModel.getItems()) {
            ImportEntityData<Object> item = (ImportEntityData<Object>) object;
            if (item.getClone().getEntity()) {
                objectsToClone.add(object);
            }
        }
        executeImportClone();
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

    private void onClone() {
        ImportCloneModel cloneModel = (ImportCloneModel) getConfirmWindow();
        if (cloneModel.getApplyToAll().getEntity()) {
            if (!cloneModel.getNoClone().getEntity()) {
                String suffix = cloneModel.getSuffix().getEntity();
                if (!validateSuffix(suffix, cloneModel.getSuffix())) {
                    return;
                }
                for (Object object : objectsToClone) {
                    setObjectName(object, suffix, true);
                    cloneObjectMap.put((Guid) ((IVdcQueryable) (((ImportEntityData<Object>) object).getEntity())).getQueryableId(),
                            object);
                }
            }
            objectsToClone.clear();
        } else {
            Object object = cloneModel.getEntity();
            if (!cloneModel.getNoClone().getEntity()) {
                String vmName = cloneModel.getName().getEntity();
                if (!validateName(vmName, cloneModel.getName(), getClonedNameValidators(object))) {
                    return;
                }
                setObjectName(object, vmName, false);
                cloneObjectMap.put((Guid) ((IVdcQueryable) ((ImportEntityData<Object>) object).getEntity()).getQueryableId(),
                        object);
            }
            objectsToClone.remove(object);
        }

        setConfirmWindow(null);
        executeImportClone();
    }

    private void setObjectName(Object object, String input, boolean isSuffix) {
        String nameForTheClonedVm = isSuffix ? getObjectName(object) + input : input;
        setObjectName(object, nameForTheClonedVm);
        assignedVmNames.add(nameForTheClonedVm);
    }

    protected String getObjectName(Object object) {
        return ((ImportVmData) object).getVm().getName();
    }

    protected void setObjectName(Object object, String name) {
        ((ImportVmData) object).getVm().setName(name);
    }

    protected boolean validateSuffix(String suffix, EntityModel entityModel) {
        for (Object object : objectsToClone) {
            VM vm = ((ImportVmData) object).getVm();
            if (!validateName(vm.getName() + suffix, entityModel, getClonedAppendedNameValidators(object))) {
                return false;
            }
        }
        return true;
    }

    protected IValidation[] getClonedNameValidators(Object object) {
        final int maxClonedNameLength = getMaxClonedNameLength(object);
        return new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(maxClonedNameLength),
                new I18NNameValidation() {
                    @Override
                    protected String composeMessage() {
                        return ConstantsManager.getInstance()
                                .getMessages()
                                .nameMustConataionOnlyAlphanumericChars(maxClonedNameLength);
                    };
                },
                new UniqueClonedNameValidator(assignedVmNames)
        };
    }

    protected IValidation[] getClonedAppendedNameValidators(Object object) {
        final int maxClonedNameLength = getMaxClonedNameLength(object);
        return new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(maxClonedNameLength),
                new I18NNameValidation() {
                    @Override
                    protected String composeMessage() {
                        return ConstantsManager.getInstance()
                                .getMessages()
                                .newNameWithSuffixCannotContainBlankOrSpecialChars(maxClonedNameLength);
                    };
                },
                new UniqueClonedAppendedNameValidator(assignedVmNames)
        };
    }

    protected String getAlreadyAssignedClonedNameMessage() {
        return ConstantsManager.getInstance()
                .getMessages()
                .alreadyAssignedClonedVmName();
    }

    protected String getSuffixCauseToClonedNameCollisionMessage(String existingName) {
        return ConstantsManager.getInstance()
                .getMessages()
                .suffixCauseToClonedVmNameCollision(existingName);
    }

    protected int getMaxClonedNameLength(Object object) {
        VM vm = ((ImportVmData) object).getVm();
        return AsyncDataProvider.getInstance().isWindowsOsType(vm.getOs()) ? AsyncDataProvider.getInstance().getMaxVmNameLengthWin()
                : AsyncDataProvider.getInstance().getMaxVmNameLengthNonWin();
    }

    protected boolean validateName(String newVmName, EntityModel entity, IValidation[] validators) {
        EntityModel temp = new EntityModel();
        temp.setIsValid(true);
        temp.setEntity(newVmName);
        temp.validateEntity(validators);
        if (!temp.getIsValid()) {
            entity.setInvalidityReasons(temp.getInvalidityReasons());
            entity.setIsValid(false);
        }

        return temp.getIsValid();
    }

    private void closeClone() {
        setConfirmWindow(null);
        clearCachedAssignedVmNames();
    }

    protected void executeImport() {
        importModel.importVms(
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(
                            FrontendMultipleActionAsyncResult result) {

                        getWindow().stopProgress();
                        cancel();
                        ArrayList<VdcReturnValueBase> retVals =
                                (ArrayList<VdcReturnValueBase>) result
                                        .getReturnValue();
                        if (retVals != null
                                && getSelectedItems().size() == retVals
                                        .size()) {
                            StringBuilder importedVms = new StringBuilder();
                            int counter = 0;
                            boolean toShowConfirmWindow = false;
                            for (Object item : getSelectedItems()) {
                                VM vm = (VM) item;
                                if (retVals.get(counter) != null
                                        && retVals.get(counter).getCanDoAction()) {
                                    importedVms.append(vm.getName()).append(", "); //$NON-NLS-1$
                                    toShowConfirmWindow = true;
                                }
                                counter++;
                            }
                            // show the confirm window only if the import has been successfully started for at least one
                            // VM
                            if (toShowConfirmWindow) {
                                ConfirmationModel confirmModel = new ConfirmationModel();
                                setConfirmWindow(confirmModel);
                                confirmModel.setTitle(ConstantsManager.getInstance()
                                        .getConstants()
                                        .importVirtualMachinesTitle());
                                confirmModel.setHelpTag(HelpTag.import_virtual_machine);
                                confirmModel.setHashName("import_virtual_machine"); //$NON-NLS-1$
                                confirmModel.setMessage(ConstantsManager.getInstance()
                                        .getMessages()
                                        .importProcessHasBegunForVms(StringHelper.trimEnd(importedVms.toString().trim(), ',')));
                                UICommand tempVar2 = new UICommand("CancelConfirm", //$NON-NLS-1$
                                        VmBackupModel.this);
                                tempVar2.setTitle(ConstantsManager.getInstance().getConstants().close());
                                tempVar2.setIsDefault(true);
                                tempVar2.setIsCancel(true);
                                confirmModel.getCommands().add(tempVar2);
                            }
                        }

                    }
                },
                cloneObjectMap);
    }

    @Override
    protected void entityPropertyChanged(Object sender,
            PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("storage_domain_shared_status")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }

    @Override
    protected void syncSearch() {
        super.syncSearch();

        if (getEntity() == null
                || getEntity().getStorageDomainType() != StorageDomainType.ImportExport
                || getEntity().getStorageDomainSharedStatus() != StorageDomainSharedStatus.Active) {
            setItems(Collections.emptyList());
        } else {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object ReturnValue) {
                    VmBackupModel backupModel = (VmBackupModel) model;
                    ArrayList<StoragePool> list = (ArrayList<StoragePool>) ReturnValue;
                    if (list != null && list.size() > 0) {
                        StoragePool dataCenter = list.get(0);
                        AsyncQuery _asyncQuery1 = new AsyncQuery();
                        _asyncQuery1.setModel(backupModel);
                        _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object model1,
                                    Object ReturnValue1) {
                                ArrayList<VM> vms = ((VdcQueryReturnValue) ReturnValue1).getReturnValue();
                                Collections.sort(vms, new Linq.VmComparator());
                                setItems((ArrayList) vms);
                            }
                        };
                        GetAllFromExportDomainQueryParameters tempVar = new GetAllFromExportDomainQueryParameters(
                                dataCenter.getId(), backupModel.getEntity()
                                        .getId());
                        Frontend.getInstance().runQuery(VdcQueryType.GetVmsFromExportDomain,
                                tempVar, _asyncQuery1);
                    }
                }
            };
            AsyncDataProvider.getInstance().getDataCentersByStorageDomain(_asyncQuery,
                    getEntity().getId());
        }
    }

    private void multipleArchsOK() {
        setConfirmWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command.getName().equals("OnRemove")) { //$NON-NLS-1$
            onRemove();
        } else if (command.getName().equals("OnRestore")) { //$NON-NLS-1$
            onRestore();
        } else if (command.getName().equals("onClone")) { //$NON-NLS-1$
            onClone();
        } else if (command.getName().equals("closeClone")) { //$NON-NLS-1$
            closeClone();
        } else if (command.getName().equals("multipleArchsOK")) { //$NON-NLS-1$
            multipleArchsOK();
        }
    }

    @Override
    protected final void cancel() {
        super.cancel();
        clearCachedAssignedVmNames();
    }

    @Override
    protected String getListName() {
        return "VmBackupModel"; //$NON-NLS-1$
    }

    private void clearCachedAssignedVmNames() {
        assignedVmNames.clear();
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

    protected String getImportConflictTitle() {
        return ConstantsManager.getInstance().getConstants().importVmConflictTitle();
    }
}
