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
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ImportVmsModel extends ListWithSimpleDetailsModel {

    private ListModel<StoragePool> dataCenters;
    private ListModel<ImportSource> importSources;
    private ListModel<EntityModel<VM>> externalVmModels;
    private ListModel<EntityModel<VM>> importedVmModels;

    private StorageDomain exportDomain;
    private String exportPath;
    private String exportName;
    private String exportDescription;

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

    private INewAsyncCallback createGetStorageDomainsByStoragePoolIdCallback(final StoragePool dataCenter) {
        return new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue) {
                List<StorageDomain> storageDomains = ((VdcQueryReturnValue) ReturnValue).getReturnValue();

               exportDomain = getExportDomain(storageDomains);
               if (exportDomain == null) {
                   setErrorToFetchData(ConstantsManager.getInstance().getConstants().notAvailableWithNoActiveExportDomain());
                   stopProgress();
               } else {
                   setExportName(exportDomain.getName());
                   setExportDescription(exportDomain.getDescription());
                   // get export-path
                   AsyncQuery _asyncQuery = new AsyncQuery();
                   _asyncQuery.setModel(this);
                   _asyncQuery.asyncCallback = new INewAsyncCallback() {
                       @Override
                       public void onSuccess(Object model, Object ReturnValue)
                       {
                           StorageServerConnections connection = (StorageServerConnections) ReturnValue;
                           setExportPath(connection == null ? null : connection.getconnection());
                           stopProgress();
                       }
                   };
                   AsyncDataProvider.getInstance().getStorageConnectionById(_asyncQuery, exportDomain.getStorage(), true);
               }
            }
        };
    }

    private static StorageDomain getExportDomain(List<StorageDomain> storageDomains) {
        for (StorageDomain storageDomain : storageDomains) {
            if (storageDomain.getStorageDomainType() == StorageDomainType.ImportExport
                    && storageDomain.getStatus() == StorageDomainStatus.Active) {
                return storageDomain;
            }
        }
        return null;
    }

    private void initDataCenters() {
        getDataCenters().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                StoragePool dataCenter = dataCenters.getSelectedItem();
                Frontend.getInstance().runQuery(
                        VdcQueryType.GetStorageDomainsByStoragePoolId,
                        new IdQueryParameters(dataCenter.getId()),
                        new AsyncQuery(this, createGetStorageDomainsByStoragePoolIdCallback(dataCenter)));
            }
        });

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
                    getDataCenters().setIsChangeable(false);
                    getImportSources().setIsChangeable(false);
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
        importSources.setSelectedItem(ImportSource.EXPORT_DOMAIN);
        importSources.getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                clearVms();
            }
        });
    }

    private void clearVms() {
        importedVmModels.setItems(null);
        externalVmModels.setItems(null);
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

    public void loadVmsFromExportDomain() {
        startProgress(null);
        Frontend.getInstance().runQuery(VdcQueryType.GetVmsFromExportDomain,
                new GetAllFromExportDomainQueryParameters(getDataCenters().getSelectedItem().getId(), exportDomain.getId()),
                new AsyncQuery(this, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        updateVms(((VdcQueryReturnValue) returnValue).<List<VM>>getReturnValue());
                    }
                }));
    }

    private void updateVms(List<VM> vms) {
        clearVms();
        List<EntityModel<VM>> externalVms = new ArrayList<>();
        for (VM vm : vms) {
            externalVms.add(new EntityModel<VM>(vm));
        }

        externalVmModels.setItems(externalVms);
        stopProgress();
    }

    public List<VM> getVmsToImport() {
        List<VM> vmsToImport = new ArrayList<>();
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

    public String getExportPath() {
        return exportPath;
    }

    public void setExportPath(String exportPath) {
        if (!ObjectUtils.objectsEqual(this.exportPath, exportPath)) {
            this.exportPath = exportPath;
            onPropertyChanged(new PropertyChangedEventArgs("ExportPath")); //$NON-NLS-1$
        }
    }

    public String getExportName() {
        return exportName;
    }

    public void setExportName(String exportName) {
        if (!ObjectUtils.objectsEqual(this.exportName, exportName)) {
            this.exportName = exportName;
            onPropertyChanged(new PropertyChangedEventArgs("ExportName")); //$NON-NLS-1$
        }
    }

    public String getExportDescription() {
        return exportDescription != null ? exportDescription : ""; //$NON-NLS-1$
    }

    public void setExportDescription(String exportDescription) {
        if (!ObjectUtils.objectsEqual(this.exportDescription, exportDescription)) {
            this.exportDescription = exportDescription;
            onPropertyChanged(new PropertyChangedEventArgs("ExportDescription")); //$NON-NLS-1$
        }
    }
}
