package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.OvaTemplateModel;
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.EntityModelLexoNumericNameableComparator;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportSource;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportTemplateData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

import com.google.inject.Inject;

public class ImportTemplatesModel extends ListWithSimpleDetailsModel {

    private ListModel<StoragePool> dataCenters;
    private ListModel<ImportSource> importSources;
    private SortedListModel<EntityModel<VmTemplate>> externalTemplatesModels;
    private SortedListModel<EntityModel<VmTemplate>> importedTemplatesModels;

    private EntityModel<StorageDomain> exportDomain;
    private String exportPath;
    private String exportName;
    private String exportDescription;

    private ListModel<VDS> hosts;
    private EntityModel<String> ovaPath;

    private UICommand addImportCommand = new UICommand(null, this);
    private UICommand cancelImportCommand = new UICommand(null, this);

    private com.google.inject.Provider<ImportTemplateFromExportDomainModel> importFromExportDomainModelProvider;
    private com.google.inject.Provider<ImportTemplateFromOvaModel> importFromOvaModelProvider;

    private ImportTemplateFromExportDomainModel importFromExportDomainModel;
    private ImportTemplateFromOvaModel importFromOvaModel;
    private ImportTemplateFromExportDomainModel selectedImportVmModel;

    private EntityModel<String> problemDescription;
    private UIConstants constants;
    private UIMessages messages;

    /** Data Center Id -> Architectures that are supported by at least one virt cluster */
    private Map<Guid, Set<ArchitectureType>> clusterArchitecturesInDataCenters;

    @Inject
    public ImportTemplatesModel(
            com.google.inject.Provider<ImportTemplateFromExportDomainModel> importFromExportDomainModelProvider,
            com.google.inject.Provider<ImportTemplateFromOvaModel> importFromOvaModelProvider) {
        this.importFromExportDomainModelProvider = importFromExportDomainModelProvider;
        this.importFromOvaModelProvider = importFromOvaModelProvider;

        constants = ConstantsManager.getInstance().getConstants();
        messages = ConstantsManager.getInstance().getMessages();

        // General
        setDataCenters(new ListModel<StoragePool>());
        setImportSources(new ListModel<ImportSource>());

        setExternalTemplatesModels(new SortedListModel<>(new EntityModelLexoNumericNameableComparator<EntityModel<VmTemplate>, VmTemplate>()));
        setImportedTemplatesModels(new SortedListModel<>(new EntityModelLexoNumericNameableComparator<EntityModel<VmTemplate>, VmTemplate>()));

        // Export domain
        setExportDomain(new EntityModel<StorageDomain>());

        // OVA
        setHosts(new ListModel<VDS>());
        setOvaPath(new EntityModel<String>());

        setInfoMessage(new EntityModel<String>());
        initImportSources();
    }

    private void initImportSources() {
        importSources.setItems(Arrays.asList(ImportSource.EXPORT_DOMAIN, ImportSource.OVA));
        importSources.getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            validateSource();
            clearTemplates();
        });
        importSources.setSelectedItem(ImportSource.EXPORT_DOMAIN);
    }

    public void init() {
        startProgress();
        setTitle(constants.importTemplatesTitle());
        setHelpTag(HelpTag.import_template);
        setHashName("import_template"); //$NON-NLS-1$

        initDataCenters();
        initDataCenterCpuArchitectureMap();
    }

    @Override
    protected String getListName() {
        return "ImportTemplatesModel"; //$NON-NLS-1$
    }

    public ListModel<StoragePool> getDataCenters() {
        return dataCenters;
    }

    private void setDataCenters(ListModel<StoragePool> storage) {
        this.dataCenters = storage;
    }

    public ListModel<ImportSource> getImportSources() {
        return importSources;
    }

    private void setImportSources(ListModel<ImportSource> importSources) {
        this.importSources = importSources;
    }

    public SortedListModel<EntityModel<VmTemplate>> getImportedTemplatesModels() {
        return importedTemplatesModels;
    }

    public void setImportedTemplatesModels(SortedListModel<EntityModel<VmTemplate>> importedTemplatesModels) {
        this.importedTemplatesModels = importedTemplatesModels;
        this.importedTemplatesModels.getItemsChangedEvent().addListener((ev, sender, args) -> {
            validateImportedTemplates();
        });
    }

    public SortedListModel<EntityModel<VmTemplate>> getExternalTemplatesModels() {
        return externalTemplatesModels;
    }

    public void setExternalTemplatesModels(SortedListModel<EntityModel<VmTemplate>> externalTemplatesModels) {
        this.externalTemplatesModels = externalTemplatesModels;
        this.externalTemplatesModels.getItemsChangedEvent().addListener((ev, sender, args) -> {
            validateImportedTemplates();
        });
    }

    public void setExportDomain(EntityModel<StorageDomain> exportDomain) {
        this.exportDomain = exportDomain;
    }

    private static StorageDomain getExportDomain(List<StorageDomain> storageDomains) {
        return storageDomains.stream()
                .filter(storageDomain -> storageDomain.getStorageDomainType() == StorageDomainType.ImportExport)
                .filter(storageDomain -> storageDomain.getStatus() == StorageDomainStatus.Active)
                .findAny()
                .orElse(null);
    }

    public EntityModel<StorageDomain> getExportDomain() {
        return exportDomain;
    }

    private AsyncCallback<QueryReturnValue> createGetStorageDomainsByStoragePoolIdCallback() {
        return returnValue -> {
            List<StorageDomain> storageDomains = returnValue.getReturnValue();

           exportDomain.setEntity(getExportDomain(storageDomains));
           if (exportDomain.getEntity() == null) {
               stopProgress();
           } else {
               setExportName(exportDomain.getEntity().getName());
               setExportDescription(exportDomain.getEntity().getDescription());
               // get export-path
               AsyncDataProvider.getInstance().getStorageConnectionById(new AsyncQuery<>(connection -> {
                   setExportPath(connection == null ? null : connection.getConnection());
                   stopProgress();
               }), exportDomain.getEntity().getStorage(), true);
           }
           validateSource();
        };
    }

    public String getExportPath() {
        return exportPath;
    }

    public void setExportPath(String exportPath) {
        if (!Objects.equals(this.exportPath, exportPath)) {
            this.exportPath = exportPath;
            onPropertyChanged(new PropertyChangedEventArgs("ExportPath")); //$NON-NLS-1$
        }
    }

    public String getExportName() {
        return exportName;
    }

    public void setExportName(String exportName) {
        if (!Objects.equals(this.exportName, exportName)) {
            this.exportName = exportName;
            onPropertyChanged(new PropertyChangedEventArgs("ExportName")); //$NON-NLS-1$
        }
    }

    public String getExportDescription() {
        return exportDescription != null ? exportDescription : ""; //$NON-NLS-1$
    }

    public void setExportDescription(String exportDescription) {
        if (!Objects.equals(this.exportDescription, exportDescription)) {
            this.exportDescription = exportDescription;
            onPropertyChanged(new PropertyChangedEventArgs("ExportDescription")); //$NON-NLS-1$
        }
    }

    private void validateSource() {
        clearProblem();
        if (importSources.getSelectedItem() == ImportSource.EXPORT_DOMAIN && exportDomain.getEntity() == null) {
            setError(constants.notAvailableWithNoActiveExportDomain());
        }
    }

    public boolean validateSelectedTemplates() {
        clearProblem();
        if (importedTemplatesModels.getIsEmpty() || importedTemplatesModels.getItems() == null) {
            setError(constants.emptyImagePath());
            return false;
        }
        return true;
    }

    public void validateImportedTemplates() {
        if (importedTemplatesModels == null || importedTemplatesModels.getItems() == null) {
            return;
        }
        clearProblem();
        int setSize = importedTemplatesModels.getItems().stream().map(e -> e.getEntity()).collect(Collectors.toSet()).size();
        int listSize = importedTemplatesModels.getItems().size();
        if (setSize != listSize) {
            setError(messages.noTemplateNameDuplicatesAllowed());
        }
    }

    public void setError(String problem) {
        getProblemDescription().setIsValid(false);
        getProblemDescription().setEntity(problem);
    }

    public void clearProblem() {
        getProblemDescription().setIsValid(true);
        getProblemDescription().setEntity(null);
    }

    public EntityModel<String> getProblemDescription() {
        return problemDescription;
    }

    public void setInfoMessage(EntityModel<String> problemDescription) {
        this.problemDescription = problemDescription;
    }

    public void initImportModels(UICommand ... commands) {
        importFromExportDomainModel = importFromExportDomainModelProvider.get();
        initImportModel(importFromExportDomainModel, commands);

        importFromOvaModel = importFromOvaModelProvider.get();
        initImportModel(importFromOvaModel, commands);
    }

    private void initImportModel(ImportVmModel importVmModel, UICommand ... commands) {
        importVmModel.setTitle(constants.importTemplatesTitle());
        importVmModel.setHelpTag(HelpTag.import_template);
        importVmModel.setHashName("import_template"); //$NON-NLS-1$
        for (UICommand command : commands) {
            importVmModel.getCommands().add(command);
        }
    }

    private void initDataCenters() {
        getDataCenters().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            clearTemplates();
            exportDomain.setEntity(null);

            StoragePool dataCenter = dataCenters.getSelectedItem();
            Frontend.getInstance().runQuery(
                    QueryType.GetStorageDomainsByStoragePoolId,
                    new IdQueryParameters(dataCenter.getId()),
                    new AsyncQuery<>(createGetStorageDomainsByStoragePoolIdCallback()));
        });

        dataCenters.getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {

                AsyncDataProvider.getInstance().getHostListByDataCenter(new AsyncQuery<>(hosts -> {
                    List<VDS> upHosts = filterUpHosts(hosts);
                    ImportTemplatesModel.this.hosts.setItems(upHosts);
                    stopProgress();
                }), dataCenters.getSelectedItem().getId());
            }

            private List<VDS> filterUpHosts(List<VDS> hosts) {
                List<VDS> result = new ArrayList<>();
                for (VDS host : hosts) {
                    if (host.getStatus() == VDSStatus.Up) {
                        result.add(host);
                    }
                }
                return result;
            }
        });

        AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery<>(returnValue -> {
            List<StoragePool> dataCenters = new ArrayList<>();
            for (StoragePool a : returnValue) {
                if (a.getStatus() == StoragePoolStatus.Up) {
                    dataCenters.add(a);
                }
            }
            if (dataCenters.isEmpty()) {
                getDataCenters().setIsChangeable(false);
                getImportSources().setIsChangeable(false);
                setError(constants.notAvailableWithNoUpDC());
                stopProgress();
                return;
            }

            Collections.sort(dataCenters, new NameableComparator());
            ImportTemplatesModel.this.dataCenters.setItems(dataCenters);
        }));
    }

    private void initDataCenterCpuArchitectureMap() {
        final AsyncQuery<QueryReturnValue> callback = new AsyncQuery<>(new AsyncCallback<QueryReturnValue>() {
            @Override
            public void onSuccess(QueryReturnValue returnValue) {
                List<Cluster> allClusters = returnValue.getReturnValue();
                clusterArchitecturesInDataCenters = new HashMap<>();
                for (Cluster cluster : allClusters) {
                    if (cluster.supportsVirtService() && cluster.getArchitecture() != null) {
                        addArchitecture(cluster.getStoragePoolId(), cluster.getArchitecture());
                    }
                }
            }

            private void addArchitecture(Guid dataCenterId, ArchitectureType architecture) {
                Set<ArchitectureType> architectures = clusterArchitecturesInDataCenters.get(dataCenterId);
                if (architectures == null) {
                    architectures = new HashSet<>();
                    clusterArchitecturesInDataCenters.put(dataCenterId, architectures);
                }
                architectures.add(architecture);
            }
        });
        Frontend.getInstance().runQuery(QueryType.GetAllClusters, new QueryParametersBase(), callback);
    }

    private void clearTemplates() {
        importedTemplatesModels.setItems(null);
        externalTemplatesModels.setItems(null);
    }

    private void clearForLoad() {
        clearProblem();
        clearTemplates();
    }

    public ImportVmModel getSpecificImportModel() {
        List<VmTemplate> templates = getTemplatesToImport();
        Map<VmTemplate, List<DiskImage>> templateToDisks = templates.stream()
                .collect(Collectors.toMap(
                        template -> template,
                        template -> new ArrayList<>(template.getDiskTemplateMap().values())));
        List<Map.Entry<VmTemplate, List<DiskImage>>> items1 = new ArrayList<>();
        for (Map.Entry<VmTemplate, List<DiskImage>> item : templateToDisks.entrySet()) {
            items1.add(item);
            VmTemplate template = item.getKey();
            template.setDiskList(new ArrayList<>());
            template.getDiskList().addAll(item.getValue());
        }
        templates.sort(new LexoNumericNameableComparator<>());

        selectedImportVmModel = null;

        switch(importSources.getSelectedItem()) {
        case EXPORT_DOMAIN:
            importFromExportDomainModel.setEntity(null);
            importFromExportDomainModel.init(templates, exportDomain.getEntity().getId());
            importFromExportDomainModel.setEntity(exportDomain.getEntity().getId());
            TemplateImportDiskListModel templateImportDiskListModel = (TemplateImportDiskListModel)
                    importFromExportDomainModel.getImportDiskListModel();
            templateImportDiskListModel.setExtendedItems(items1);
            selectedImportVmModel = importFromExportDomainModel;
            break;
        case OVA:
            importFromOvaModel.init(templates, getDataCenters().getSelectedItem().getId());
            importFromOvaModel.setIsoName(getOvaPath().getEntity());
            importFromOvaModel.setHostId(getHosts().getSelectedItem().getId());
            importFromOvaModel.setTemplateNameToOva(getTemplateNameToOva());
            selectedImportVmModel = importFromOvaModel;
            break;
        default:
        }
        return selectedImportVmModel;
    }

    public void clearTemplateModelsExceptItems() {
        Collection<EntityModel<VmTemplate>> savedTemplates;

        savedTemplates = getImportedTemplatesModels().getItems();
        setImportedTemplatesModels(new SortedListModel<>(new EntityModelLexoNumericNameableComparator<EntityModel<VmTemplate>, VmTemplate>()));
        getImportedTemplatesModels().setItems(savedTemplates);

        savedTemplates = getExternalTemplatesModels().getItems();
        setExternalTemplatesModels(new SortedListModel<>(new EntityModelLexoNumericNameableComparator<EntityModel<VmTemplate>, VmTemplate>()));
        getExternalTemplatesModels().setItems(savedTemplates);
    }

    public List<ImportTemplateData> onRestoreTemplates() {
        if (selectedImportVmModel.getProgress() != null) {
            return null;
        }

        if (!selectedImportVmModel.validate()) {
            return null;
        }

        return (List<ImportTemplateData>) selectedImportVmModel.getItems().stream()
                .filter(item -> ((ImportTemplateData) item).getClone().getEntity())
                .collect(Collectors.toList());
    }

    protected void executeImport(Map<Guid, Object> cloneObjectMap, IFrontendMultipleActionAsyncCallback callback) {
        if (selectedImportVmModel.getProgress() != null) {
            return;
        }

        if (!selectedImportVmModel.validate()) {
            return;
        }

        selectedImportVmModel.setCloneObjectMap(cloneObjectMap);
        selectedImportVmModel.executeImport(callback);
    }

    public ListModel<VDS> getHosts() {
        return hosts;
    }

    public void setHosts(ListModel<VDS> hosts) {
        this.hosts = hosts;
    }

    public EntityModel<String> getOvaPath() {
        return ovaPath;
    }

    public void setOvaPath(EntityModel<String> ovaPath) {
        this.ovaPath = ovaPath;
    }

    public List<VmTemplate> getTemplatesToImport() {
        return importedTemplatesModels.getItems()
                .stream()
                .map(EntityModel::getEntity)
                .collect(Collectors.toList());
    }

    public Map<String, String> getTemplateNameToOva() {
        Map<String, String> templateNameToOva = new HashMap<>();
        for (EntityModel<VmTemplate> item : importedTemplatesModels.getItems()) {
            if (item instanceof OvaTemplateModel) {
                OvaTemplateModel ovaTemplate = (OvaTemplateModel) item;
                templateNameToOva.put(item.getEntity().getName(), ovaTemplate.getOvaFileName());
            }
        }
        return templateNameToOva;
    }

    @Override
    public void executeCommand(UICommand command) {
        if (getAddImportCommand().equals(command)) {
            addImport();
        } else if (getCancelImportCommand().equals(command)) {
            cancelImport();
        }
    }

    private void addImport() {
        getDefaultCommand().setIsExecutionAllowed(true);
    }

    private void cancelImport() {
        Collection<EntityModel<VmTemplate>> selectedtemplates = getImportedTemplatesModels().getSelectedItems();
        Collection<EntityModel<VmTemplate>> totalTemplatesSetToImport = getImportedTemplatesModels().getItems();
        getDefaultCommand().setIsExecutionAllowed(selectedtemplates.size() < totalTemplatesSetToImport.size());
    }

    public UICommand getAddImportCommand() {
        return addImportCommand;
    }

    public UICommand getCancelImportCommand() {
        return cancelImportCommand;
    }

    public void loadTemplatesFromExportDomain() {
        clearProblem();
        startProgress();
        Frontend.getInstance().runQuery(QueryType.GetTemplatesFromExportDomain,
                new GetAllFromExportDomainQueryParameters(getDataCenters().getSelectedItem().getId(), exportDomain.getEntity().getId()),
                new AsyncQuery<QueryReturnValue>(returnValue -> updateTemplates(returnValue.<Map<VmTemplate, ?>>getReturnValue().keySet())));
    }

    private void updateTemplates(Collection<VmTemplate> templates) {
        clearTemplates();
        externalTemplatesModels.setItems(templates.stream().map(EntityModel::new).collect(Collectors.toList()));
        stopProgress();
    }

    private void updateTemplatesForOva(Map<String, VmTemplate> templates) {
        clearTemplates();
        externalTemplatesModels.setItems(
            templates.entrySet()
                .stream()
                .map(e -> new OvaTemplateModel(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));
        stopProgress();
    }

    public void loadVmFromOva() {
        clearForLoad();
        if (!validateOvaConfiguration()) {
            return;
        }

        startProgress();
        AsyncDataProvider.getInstance().getTemplateFromOva(new AsyncQuery<>(returnValue -> {
            if (returnValue.getSucceeded()) {
                Map<String, VmTemplate> templateToOva = returnValue.getReturnValue();
                updateTemplatesForOva(templateToOva);
            } else {
                setError(messages.failedToLoadOva(getOvaPath().getEntity()));
            }
            stopProgress();
        }),
                getHosts().getSelectedItem().getId(),
                getOvaPath().getEntity());
    }

    private boolean validateOvaConfiguration() {
        getOvaPath().validateEntity(new IValidation[]{
                new NotEmptyValidation()});

        return getOvaPath().getIsValid();
    }

}
