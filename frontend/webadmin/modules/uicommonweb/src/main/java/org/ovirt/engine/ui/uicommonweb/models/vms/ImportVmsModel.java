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
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
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
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ImportVmsModel extends ListWithSimpleDetailsModel {

    private ListModel<StoragePool> dataCenters;
    private ListModel<ImportSource> importSources;
    private ListModel<EntityModel<VM>> externalVmModels;
    private ListModel<EntityModel<VM>> importedVmModels;

    private EntityModel<String> vCenter;
    private EntityModel<String> esx;
    private EntityModel<String> vmwareDatacenter;
    private EntityModel<Boolean> verify;
    private EntityModel<String> username;
    private EntityModel<String> password;
    private ListModel<VDS> proxyHosts;

    private StorageDomain exportDomain;
    private String exportPath;
    private String exportName;
    private String exportDescription;

    private UICommand addImportCommand = new UICommand(null, this);
    private UICommand cancelImportCommand = new UICommand(null, this);

    private Provider<ImportVmFromExternalProviderModel> importFromExternalProviderModelProvider;
    private Provider<ImportVmFromExportDomainModel> importFromExportDomainModelProvider;

    private ImportVmFromExternalProviderModel importFromExternalProviderModel;
    private ImportVmFromExportDomainModel importFromExportDomainModel;
    private ImportVmModel selectedImportVmModel;

    private EntityModel<Boolean> importSourceValid;

    @Inject
    public ImportVmsModel(Provider<ImportVmFromExportDomainModel> importFromExportDomainModelProvider,
            Provider<ImportVmFromExternalProviderModel> importFromExternalProviderModelProvider) {
        this.importFromExportDomainModelProvider = importFromExportDomainModelProvider;
        this.importFromExternalProviderModelProvider = importFromExternalProviderModelProvider;

        // General
        setDataCenters(new ListModel<StoragePool>());
        setImportSources(new ListModel<ImportSource>());
        setExternalVmModels(new ListModel<EntityModel<VM>>());
        setImportedVmModels(new ListModel<EntityModel<VM>>());

        // VMWARE
        setProxyHosts(new ListModel<VDS>());
        setUsername(new EntityModel<String>());
        setPassword(new EntityModel<String>());
        setvCenter(new EntityModel<String>());
        setEsx(new EntityModel<String>());
        setVmwareDatacenter(new EntityModel<String>());
        setVerify(new EntityModel<Boolean>(false));

        setImportSourceValid(new EntityModel<Boolean>(true));

        initImportSources();
    }

    public void initImportModels(UICommand ... commands) {
        importFromExportDomainModel = importFromExportDomainModelProvider.get();
        importFromExportDomainModel.setTitle(ConstantsManager.getInstance().getConstants().importVirtualMachinesTitle());
        importFromExportDomainModel.setHelpTag(HelpTag.import_virtual_machine);
        importFromExportDomainModel.setHashName("import_virtual_machine"); //$NON-NLS-1$
        for (UICommand command : commands) {
            importFromExportDomainModel.getCommands().add(command);
        }

        importFromExternalProviderModel = importFromExternalProviderModelProvider.get();
        importFromExternalProviderModel.setTitle(ConstantsManager.getInstance().getConstants().importVirtualMachinesTitle());
        importFromExternalProviderModel.setHelpTag(HelpTag.import_virtual_machine);
        importFromExternalProviderModel.setHashName("import_virtual_machine"); //$NON-NLS-1$
        for (UICommand command : commands) {
            importFromExternalProviderModel.getCommands().add(command);
        }
    }

    public ImportVmModel getSpecificImportModel() {
        selectedImportVmModel = null;
        switch(importSources.getSelectedItem()) {
        case EXPORT_DOMAIN:
            importFromExportDomainModel.setEntity(null);
            importFromExportDomainModel.init(getVmsToImport(), exportDomain.getId());
            importFromExportDomainModel.setEntity(exportDomain.getId());
            selectedImportVmModel = importFromExportDomainModel;
            break;
        case VMWARE:
            importFromExternalProviderModel.init(getVmsToImport(), getDataCenters().getSelectedItem().getId());
            importFromExternalProviderModel.setUrl(getUrl());
            importFromExternalProviderModel.setUsername(getUsername().getEntity());
            importFromExternalProviderModel.setPassword(getPassword().getEntity());
            importFromExternalProviderModel.setProxyHostId(getProxyHosts().getSelectedItem() != null ? getProxyHosts().getSelectedItem().getId() : null);
            selectedImportVmModel = importFromExternalProviderModel;
            break;
        default:
        }
        return selectedImportVmModel;
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

        dataCenters.getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                AsyncQuery hostsQuery = new AsyncQuery();
                hostsQuery.setModel(ImportVmsModel.this);
                hostsQuery.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        ImportVmsModel.this.stopProgress();
                        List<VDS> hosts = new ArrayList<VDS>();
                        hosts.add(0, null); // Any host in the cluster
                        for (VDS host : (List<VDS>) returnValue) {
                            if (host.getStatus() == VDSStatus.Up) {
                                hosts.add(host);
                            }
                        }
                        proxyHosts.setItems(hosts);
                    }
                };

                AsyncDataProvider.getInstance().getHostListByDataCenter(hostsQuery, dataCenters.getSelectedItem().getId());
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

    public void loadVmsFromVmware() {
        startProgress(null);
        AsyncDataProvider.getInstance().getVmsFromExternalServer(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                updateVms((List<VM>) returnValue);
            }
        }),
        getDataCenters().getSelectedItem().getId(),
        getProxyHosts().getSelectedItem() != null ? getProxyHosts().getSelectedItem().getId() : null,
        getUrl(),
        getUsername().getEntity(),
        getPassword().getEntity());
    }

    private String getUrl() {
        return getVmwareUrl(getUsername().getEntity(), getvCenter().getEntity(),
                getVmwareDatacenter().getEntity(), getEsx().getEntity(), getVerify().getEntity());
    }

    public static String getVmwareUrl(String username, String vcenter,
            String dataCenter, String esx, boolean verify) {
        return "vpx://" + //$NON-NLS-1$
                (StringUtils.isEmpty(username) ? "" : username + "@") + //$NON-NLS-1$ //$NON-NLS-2$
                vcenter +
                "/" + //$NON-NLS-1$
                dataCenter +
                "/" + //$NON-NLS-1$
                esx +
                (verify ? "" : "?no_verify=1"); //$NON-NLS-1$ //$NON-NLS-2$
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

    public EntityModel<String> getUsername() {
        return username;
    }

    public void setUsername(EntityModel<String> username) {
        this.username = username;
    }

    public EntityModel<String> getPassword() {
        return password;
    }

    public void setPassword(EntityModel<String> password) {
        this.password = password;
    }

    public ListModel<VDS> getProxyHosts() {
        return proxyHosts;
    }

    public void setProxyHosts(ListModel<VDS> proxyHosts) {
        this.proxyHosts = proxyHosts;
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

    public void onRestoreVms(IFrontendMultipleActionAsyncCallback callback) {
        if (selectedImportVmModel.getProgress() != null) {
            return;
        }

        if (!selectedImportVmModel.validate()) {
            return;
        }

        selectedImportVmModel.importVms(callback);
    }

    public EntityModel<String> getEsx() {
        return esx;
    }

    public void setEsx(EntityModel<String> esx) {
        this.esx = esx;
    }

    public EntityModel<String> getVmwareDatacenter() {
        return vmwareDatacenter;
    }

    public void setVmwareDatacenter(EntityModel<String> vmwareDatacenter) {
        this.vmwareDatacenter = vmwareDatacenter;
    }

    public EntityModel<Boolean> getVerify() {
        return verify;
    }

    public void setVerify(EntityModel<Boolean> verify) {
        this.verify = verify;
    }

    public EntityModel<String> getvCenter() {
        return vCenter;
    }

    public void setvCenter(EntityModel<String> vCenter) {
        this.vCenter = vCenter;
    }
}
