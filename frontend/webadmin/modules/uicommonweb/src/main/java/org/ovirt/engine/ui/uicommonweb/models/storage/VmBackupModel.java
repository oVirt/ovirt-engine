package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;

import org.ovirt.engine.core.common.action.RemoveVmFromImportExportParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmFromExportDomainModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class VmBackupModel extends ManageBackupModel<VM> {

    private VmAppListModel appListModel;
    protected ImportVmFromExportDomainModel importModel;

    protected Provider<? extends ImportVmFromExportDomainModel> importModelProvider;

    public VmAppListModel getAppListModel() {
        return appListModel;
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

        appListModel = new VmAppListModel();
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
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeBackedUpVMsTitle());
        model.setHelpTag(HelpTag.remove_backed_up_vm);
        model.setHashName("remove_backed_up_vm"); //$NON-NLS-1$

        ArrayList<String> items = new ArrayList<>();
        for (VM vm : getSelectedItems()) {
            items.add(vm.getName());
        }
        model.setItems(items);

        model.setNote(ConstantsManager.getInstance().getConstants().noteTheDeletedItemsMightStillAppearOntheSubTab());

        model.getCommands().add(UICommand.createDefaultOkUiCommand("OnRemove", this)); //$NON-NLS-1$
        model.getCommands().add(UICommand.createCancelUiCommand(CANCEL_COMMAND, this)); //$NON-NLS-1$
    }

    private void onRemove() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        model.startProgress();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.model = this;
        _asyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                ArrayList<StoragePool> pools = (ArrayList<StoragePool>) returnValue;
                if (pools != null && pools.size() > 0) {
                    StoragePool pool = pools.get(0);
                    VmBackupModel backupModel = (VmBackupModel) model;
                    ArrayList<VdcActionParametersBase> list = new ArrayList<>();
                    for (VM vm : backupModel.getSelectedItems()) {
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

    protected ArchitectureType getArchitectureFromItem(VM vm) {
        return vm.getClusterArch();
    }

    @Override
    protected void restore() {
        if (getWindow() != null) {
            return;
        }

        if (!validateSingleArchitecture()) {
            return;
        }

        ImportVmFromExportDomainModel model = importModelProvider.get();
        model.setEntity(getEntity().getId());
        setWindow(model);
        model.startProgress();
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
    }

    public void onRestore() {
        importModel = (ImportVmFromExportDomainModel) getWindow();

        if (importModel.getProgress() != null) {
            return;
        }

        if (!importModel.validate()) {
            return;
        }

        executeImport();
    }

    protected String getObjectName(Object object) {
        return ((ImportVmData) object).getVm().getName();
    }

    protected void setObjectName(Object object, String name) {
        ((ImportVmData) object).getVm().setName(name);
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
                            for (VM vm : getSelectedItems()) {
                                if (retVals.get(counter) != null
                                        && retVals.get(counter).isValid()) {
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
                                confirmModel.getCommands().add(new UICommand(CANCEL_CONFIRMATION_COMMAND, VmBackupModel.this)
                                .setTitle(ConstantsManager.getInstance().getConstants().close())
                                .setIsDefault(true)
                                .setIsCancel(true)
                                );
                            }
                        }

                    }
                });
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
            setItems(Collections.<VM>emptyList());
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
                                Collections.sort(vms, new LexoNumericNameableComparator<>());
                                setItems((ArrayList) vms);
                            }
                        };
                        Frontend.getInstance().runQuery(VdcQueryType.GetVmsFromExportDomain,
                                new GetAllFromExportDomainQueryParameters(dataCenter.getId(),
                                        backupModel.getEntity().getId()), _asyncQuery1);
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
        } else if (command.getName().equals("multipleArchsOK")) { //$NON-NLS-1$
            multipleArchsOK();
        }
    }

    @Override
    protected String getListName() {
        return "VmBackupModel"; //$NON-NLS-1$
    }

    protected String getImportConflictTitle() {
        return ConstantsManager.getInstance().getConstants().importVmConflictTitle();
    }
}
