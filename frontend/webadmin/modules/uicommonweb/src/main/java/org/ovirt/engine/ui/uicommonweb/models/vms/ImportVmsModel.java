package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ImportVmsModel extends ListWithDetailsModel {

    private ListModel<StoragePool> dataCenters;
    private ListModel<ImportSource> importSources;
    private ListModel<EntityModel<VM>> externalVmModels;
    private ListModel<EntityModel<VM>> importedVmModels;

    private StorageDomain exportDomain;

    private UICommand addImportCommand = new UICommand(null, this);
    private UICommand cancelImportCommand = new UICommand(null, this);

    private Provider<ImportVmFromExportDomainModel> importFromExportDomainModelProvider;
    private ImportVmFromExportDomainModel importVmFromExportDomainModel;

    private EntityModel<Boolean> importSourceValid;

    @Inject
    public ImportVmsModel(Provider<ImportVmFromExportDomainModel> importFromExportDomainModelProvider) {
        this.importFromExportDomainModelProvider = importFromExportDomainModelProvider;

        setDataCenters(new ListModel<StoragePool>());
        setImportSources(new ListModel<ImportSource>());
        setExternalVmModels(new ListModel<EntityModel<VM>>());
        setImportedVmModels(new ListModel<EntityModel<VM>>());
        setImportSourceValid(new EntityModel<Boolean>(true));
    }

    public void initImportFromExportDomainModel(UICommand ... commands) {
        importVmFromExportDomainModel = importFromExportDomainModelProvider.get();
        importVmFromExportDomainModel.setTitle(ConstantsManager.getInstance().getConstants().importVirtualMachinesTitle());
        importVmFromExportDomainModel.setHelpTag(HelpTag.import_virtual_machine);
        importVmFromExportDomainModel.setHashName("import_virtual_machine"); //$NON-NLS-1$
        for (UICommand command : commands) {
            importVmFromExportDomainModel.getCommands().add(command);
        }
    }

    @SuppressWarnings("unchecked")
    public ImportVmFromExportDomainModel getSpecificImportModel() {
        importVmFromExportDomainModel.setEntity(null);
        importVmFromExportDomainModel.init(getVmsToImport(), exportDomain.getId());
        importVmFromExportDomainModel.setEntity(exportDomain.getId());
        return importVmFromExportDomainModel;
    }

    public void init() {
        startProgress(null);

        setTitle(ConstantsManager.getInstance().getConstants().importVirtualMachinesTitle());
        setHelpTag(HelpTag.import_virtual_machine);
        setHashName("import_virtual_machine"); //$NON-NLS-1$

        initDataCenters();
    }

    private void onImportSourceChanged() {
        final ImportSource source = importSources.getSelectedItem();
        if (source == null) {
            return;
        }

        importedVmModels.setItems(null);
        externalVmModels.setItems(null);

        switch(source) {
        case EXPORT_DOMAIN:
            onExportDomainChosen();
            break;
        default:
        }
    }

    private void onExportDomainChosen() {
        StoragePool dataCenter = dataCenters.getSelectedItem();

        startProgress(null);
        Frontend.getInstance().runQuery(
                VdcQueryType.GetStorageDomainsByStoragePoolId,
                new IdQueryParameters(dataCenter.getId()),
                new AsyncQuery(this, createGetStorageDomainsByStoragePoolIdCallback(dataCenter)));
    }

    private INewAsyncCallback createGetStorageDomainsByStoragePoolIdCallback(final StoragePool dataCenter) {
        return new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue) {
                List<StorageDomain> storageDomains = (List<StorageDomain>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();

               Guid exportDomainId = findExportDomainId(storageDomains);
               if (exportDomainId != null) {
                   getVmsFromExportDomain(dataCenter.getId(), exportDomainId);
               }
               else {
                   setErrorToFetchData(ConstantsManager.getInstance().getConstants().notAvailableWithNoActiveExportDomain());
                   stopProgress();
               }
            }
        };
    }

    private void getVmsFromExportDomain(Guid dataCenterId, Guid storageDomainId) {
        Frontend.getInstance().runQuery(VdcQueryType.GetVmsFromExportDomain,
                new GetAllFromExportDomainQueryParameters(dataCenterId, storageDomainId),
                new AsyncQuery(this, createGetVmsFromExportDomainCallback()));
    }

    private INewAsyncCallback createGetVmsFromExportDomainCallback() {
        return new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                List<EntityModel<VM>> externalVms = new ArrayList<EntityModel<VM>>();
                for (VM vm : ((VdcQueryReturnValue) returnValue).<List<VM>>getReturnValue()) {
                    externalVms.add(new EntityModel<VM>(vm));
                }

                ImportVmsModel.this.externalVmModels.setItems(externalVms);
                stopProgress();
            }
        };
    }

    private Guid findExportDomainId(List<StorageDomain> storageDomains) {
        for (StorageDomain storageDomain : storageDomains) {
            if (storageDomain.getStorageDomainType() == StorageDomainType.ImportExport
                    && storageDomain.getStatus() == StorageDomainStatus.Active) {
                return (exportDomain = storageDomain).getId();
            }
        }

        return null;
    }

    private void initDataCenters() {
        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery(new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                final List<StoragePool> dataCenters = new ArrayList<StoragePool>();
                for (StoragePool a : (ArrayList<StoragePool>) returnValue) {
                    if (a.getStatus() == StoragePoolStatus.Up) {
                        dataCenters.add(a);
                    }
                }
                if (dataCenters == null || dataCenters.isEmpty()) {
                    getDataCenters().setIsChangable(false);
                    getImportSources().setIsChangable(false);
                    setErrorToFetchData(ConstantsManager.getInstance().getConstants().notAvailableWithNoUpDC());
                    stopProgress();
                    return;
                }

                Collections.sort(dataCenters, new NameableComparator());
                ImportVmsModel.this.dataCenters.setItems(dataCenters);
                initImportSources();
            }
        }));
    }

    private void initImportSources() {
        importSources.setItems(Arrays.asList(ImportSource.values()));
        onImportSourceChanged();
    }

    @Override
    public void executeCommand(UICommand command) {
        if (getAddImportCommand().equals(command)) {
            addImport();
        } else if (getCancelImportCommand().equals(command)) {
            cancelImport();
        } else {
            super.executeCommand(command);
        }
    }

    public List<VM> getVmsToImport() {
        List<VM> vmsToImport = new ArrayList<VM>();
        for (EntityModel<VM> externalVm : importedVmModels.getItems()) {
            vmsToImport.add(externalVm.getEntity());
        }
        return vmsToImport;
    }

    private void addImport() {
        getDefaultCommand().setIsExecutionAllowed(true);
    }

    private void cancelImport() {
        Collection<EntityModel<VM>> selectedVms = getImportedVmModels().getSelectedItems();
        Collection<EntityModel<VM>> totalVmsSetToImport = getImportedVmModels().getItems();
        getDefaultCommand().setIsExecutionAllowed(selectedVms.size() < totalVmsSetToImport.size());
    }

    public UICommand getAddImportCommand() {
        return addImportCommand;
    }

    public UICommand getCancelImportCommand() {
        return cancelImportCommand;
    }

    public ListModel<StoragePool> getDataCenters() {
        return dataCenters;
    }

    private void setDataCenters(ListModel<StoragePool> storage) {
        this.dataCenters = storage;
    }

    @Override
    protected String getListName() {
        return "ImportVmsModel"; //$NON-NLS-1$
    }

    public ListModel<ImportSource> getImportSources() {
        return importSources;
    }

    private void setImportSources(ListModel<ImportSource> importSources) {
        this.importSources = importSources;
    }

    public ListModel<EntityModel<VM>> getExternalVmModels() {
        return externalVmModels;
    }

    private void setExternalVmModels(ListModel<EntityModel<VM>> externalVmModels) {
        this.externalVmModels = externalVmModels;
    }

    public ListModel<EntityModel<VM>> getImportedVmModels() {
        return importedVmModels;
    }

    private void setImportedVmModels(ListModel<EntityModel<VM>> importedVmModels) {
        this.importedVmModels = importedVmModels;
    }

    public void reload() {
        onImportSourceChanged();
    }

    public EntityModel<Boolean> getImportSourceValid() {
        return importSourceValid;
    }

    public void setImportSourceValid(EntityModel<Boolean> editingEnabled) {
        this.importSourceValid = editingEnabled;
    }

    public void setErrorToFetchData(String msg) {
        getImportSourceValid().setMessage(msg);
        getImportSourceValid().setEntity(false);
    }
}
