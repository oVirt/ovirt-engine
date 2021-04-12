package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.model.Configuration;
import org.ovirt.engine.api.model.Console;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.Initialization;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.Snapshots;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Templates;
import org.ovirt.engine.api.model.VirtioScsi;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.TemplateResource;
import org.ovirt.engine.api.resource.TemplatesResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.api.restapi.types.RngDeviceMapper;
import org.ovirt.engine.api.restapi.types.TemplateMapper;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.restapi.util.IconHelper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.api.restapi.util.VmHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmTemplateFromSnapshotParameters;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.ImportVmTemplateFromConfParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVmByVmNameForDataCenterParameters;
import org.ovirt.engine.core.common.queries.GetVmFromConfigurationQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplatesResource
    extends AbstractBackendCollectionResource<Template, VmTemplate>
    implements TemplatesResource {

    public static final String CLONE_PERMISSIONS = "clone_permissions";
    public static final String SEAL = "seal";

    public BackendTemplatesResource() {
        super(Template.class, VmTemplate.class);
    }

    @Override
    public Templates list() {
        if (isFiltered()) {
            return mapCollection(getBackendCollection(QueryType.GetAllVmTemplates,
                    new QueryParametersBase(), SearchType.VmTemplate));
        } else {
            return mapCollection(getBackendCollection(SearchType.VmTemplate));
        }
    }

    @Override
    public TemplateResource getTemplateResource(String id) {
        return inject(new BackendTemplateResource(id));
    }

    @Override
    public Response addFromVm(Template template) {
        validateIconParameters(template);
        Guid clusterId = null;
        Cluster cluster = null;
        if (namedCluster(template)) {
            clusterId = getClusterId(template);
            cluster = lookupCluster(clusterId);
        }

        if (template.getVersion() != null) {
            validateParameters(template.getVersion(), "baseTemplate");
        }
        VmStatic originalVm = getVm(cluster, template);
        VmStatic staticVm = getMapper(Template.class, VmStatic.class).map(template, originalVm);

        if (namedCluster(template)) {
            staticVm.setClusterId(clusterId);
        }
        AddVmTemplateParameters params = new AddVmTemplateParameters(staticVm,
                template.getName(),
                template.getDescription());
        return addTemplate(template, originalVm, null, params, ActionType.AddVmTemplate);
    }

    @Override
    public Response addFromVmSnapshot(Template template) {
        validateIconParameters(template);
        validateSnapshotExistence(template.getVm());

        Guid snapshotId = getSnapshotId(template.getVm().getSnapshots());
        org.ovirt.engine.core.common.businessentities.VM vmConfiguration = getVmConfiguration(snapshotId);

        if (template.getVersion() != null) {
            validateParameters(template.getVersion(), "baseTemplate");
        }
        VmStatic originalVm = vmConfiguration.getStaticData();
        VmStatic staticVm = getMapper(Template.class, VmStatic.class).map(template, originalVm);

        if (namedCluster(template)) {
            Guid clusterId = getClusterId(template);
            staticVm.setClusterId(clusterId);
        }

        AddVmTemplateFromSnapshotParameters params = new AddVmTemplateFromSnapshotParameters(
                staticVm,
                template.getName(),
                template.getDescription(),
                snapshotId);
        return addTemplate(template,
                originalVm,
                vmConfiguration.getDiskMap().keySet(),
                params,
                ActionType.AddVmTemplateFromSnapshot);
    }

    private Response addTemplate(Template template,
            VmStatic originalVm,
            Set<Guid> snapshotDisksIds,
            AddVmTemplateParameters params,
            ActionType actionType) {
        if (template.getVersion() != null) {
            params.setBaseTemplateId(Guid.createGuidFromString(template.getVersion().getBaseTemplate().getId()));
            params.setTemplateVersionName(template.getVersion().getVersionName());
        }
        params.setConsoleEnabled(template.getConsole() != null && template.getConsole().isSetEnabled() ?
                template.getConsole().isEnabled()
                : !getConsoleDevicesForEntity(originalVm.getId()).isEmpty());
        params.setVirtioScsiEnabled(template.isSetVirtioScsi() && template.getVirtioScsi().isSetEnabled() ?
                template.getVirtioScsi().isEnabled() : null);
        params.setSoundDeviceEnabled(template.isSetSoundcardEnabled() ?
                template.isSoundcardEnabled()
                : !VmHelper.getSoundDevicesForEntity(this, originalVm.getId()).isEmpty());
        if (template.isSetRngDevice()) {
            params.setUpdateRngDevice(true);
            params.setRngDevice(RngDeviceMapper.map(template.getRngDevice(), null));
        }
        params.setTpmEnabled(template.isSetTpmEnabled() ? template.isTpmEnabled() : null);

        DisplayHelper.setGraphicsToParams(template.getDisplay(), params);
        boolean domainSet = template.isSetStorageDomain() && template.getStorageDomain().isSetId();
        if (domainSet) {
            params.setDestinationStorageDomainId(asGuid(template.getStorageDomain().getId()));
        }
        params.setDiskInfoDestinationMap(
                getDestinationTemplateDiskMap(
                        template.getVm(),
                        originalVm.getId(),
                        params.getDestinationStorageDomainId(),
                        domainSet,
                        snapshotDisksIds
                )
        );

        setupOptionalParameters(params);
        IconHelper.setIconToParams(template, params);

        Response response = performCreate(
                actionType,
                params,
                new QueryIdResolver<Guid>(QueryType.GetVmTemplate, GetVmTemplateParameters.class)
        );

        Template result = (Template) response.getEntity();
        if (result != null) {
            DisplayHelper.adjustDisplayData(this, result);
        }

        return response;
    }

    private void validateSnapshotExistence(Vm vm) {
        Snapshot snapshot = vm.getSnapshots().getSnapshots().get(0);
        validateParameters(snapshot, "id");
    }

    private Guid getSnapshotId(Snapshots snapshots) {
        return asGuid(snapshots.getSnapshots().get(0).getId());
    }

    private org.ovirt.engine.core.common.businessentities.VM getVmConfiguration(Guid snapshotId) {
        org.ovirt.engine.core.common.businessentities.VM vmConfiguration =
                getEntity(org.ovirt.engine.core.common.businessentities.VM.class,
                        QueryType.GetVmConfigurationBySnapshot,
                        new IdQueryParameters(snapshotId),
                        "");
        return vmConfiguration;
    }

    @Override
    public Response addFromConfiguration(Template template) {
        Initialization initialization = template.getInitialization();
        Configuration config = initialization.getConfiguration();
        org.ovirt.engine.core.common.businessentities.VmTemplate templateConfiguration =
                getEntity(org.ovirt.engine.core.common.businessentities.VmTemplate.class,
                        QueryType.GetVmTemplateFromConfiguration,
                        new GetVmFromConfigurationQueryParameters(VmMapper.map(config.getType(), null), config.getData().trim()),
                        "");

        template.setInitialization(null); // if configuration is provided, the initialization parameters cannot be overridden
        TemplateMapper.map(template, templateConfiguration);

        Guid clusterId = namedCluster(template) ? getClusterId(template) : asGuid(template.getCluster().getId());
        ImportVmTemplateFromConfParameters parameters = new ImportVmTemplateFromConfParameters();
        parameters.setVmTemplate(templateConfiguration);
        parameters.setClusterId(clusterId);
        if (initialization.isSetRegenerateIds()) {
            parameters.setImportAsNewEntity(initialization.isRegenerateIds());
        }
        return performCreate(ActionType.ImportVmTemplateFromConfiguration,
                parameters,
                new QueryIdResolver<Guid>(QueryType.GetVmTemplate, GetVmTemplateParameters.class));
    }

    private void validateIconParameters(Template incoming) {
        if (!IconHelper.validateIconParameters(incoming)) {
            throw new BaseBackendResource.WebFaultException(null,
                    localize(Messages.INVALID_ICON_PARAMETERS),
                    Response.Status.BAD_REQUEST);
        }
    }

    void setupOptionalParameters(AddVmTemplateParameters params) {
        boolean clonePermissions = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, CLONE_PERMISSIONS, true, false);
        boolean seal = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, SEAL, true, false);
        if (clonePermissions) {
            params.setCopyVmPermissions(clonePermissions);
        }
        if (seal) {
            params.setSealTemplate(seal);
        }
    }

    private Cluster lookupCluster(Guid id) {
        return getEntity(Cluster.class, QueryType.GetClusterById, new IdQueryParameters(id), "GetClusterById");
    }

    protected Map<Guid, DiskImage> getDestinationTemplateDiskMap(Vm vm, Guid vmId, Guid storageDomainId,
            boolean isTemplateGeneralStorageDomainSet, Set<Guid> snapshotDisksIds) {
        Map<Guid, DiskImage> destinationTemplateDiskMap = null;
        if (vm.isSetDiskAttachments() && vm.getDiskAttachments().isSetDiskAttachments()) {
            destinationTemplateDiskMap = new HashMap<>();
            Map<Guid, org.ovirt.engine.core.common.businessentities.storage.Disk> vmSourceDisks = queryVmDisksMap(vmId);

            for (DiskAttachment diskAttachment : vm.getDiskAttachments().getDiskAttachments()) {
                Disk disk = diskAttachment.getDisk();
                if (disk == null || !disk.isSetId()) {
                    continue;
                }

                Guid currDiskID = asGuid(disk.getId());
                org.ovirt.engine.core.common.businessentities.storage.Disk sourceDisk = vmSourceDisks.get(currDiskID);

                // VM template can only have disk images
                if (sourceDisk == null || !isDiskImage(sourceDisk)) {
                    continue;
                }

                if (snapshotDisksIds != null && !snapshotDisksIds.contains(sourceDisk.getId())) {
                    continue;
                }
                DiskImage destinationDisk = (DiskImage) DiskMapper.map(disk, sourceDisk);
                if (isTemplateGeneralStorageDomainSet) {
                    destinationDisk.setStorageIds(new ArrayList<>(Arrays.asList(storageDomainId)));
                }

                // Since domain can be changed, do not set profile and quota for this disk.
                destinationDisk.setDiskProfileId(null);
                destinationDisk.setQuotaId(null);

                destinationTemplateDiskMap.put(destinationDisk.getId(), destinationDisk);
            }
        }
        return destinationTemplateDiskMap;
    }

    private boolean isDiskImage(org.ovirt.engine.core.common.businessentities.storage.Disk disk) {
        return disk.getDiskStorageType() == DiskStorageType.IMAGE;
    }

    private Map<Guid, org.ovirt.engine.core.common.businessentities.storage.Disk> queryVmDisksMap(Guid vmId) {
        List<org.ovirt.engine.core.common.businessentities.storage.Disk> vmDisks = getBackendCollection(
            org.ovirt.engine.core.common.businessentities.storage.Disk.class,
            QueryType.GetAllDisksByVmId,
            new IdQueryParameters(vmId)
        );
        return Entities.businessEntitiesById(vmDisks);
    }

    protected Templates mapCollection(List<VmTemplate> entities) {
        Set<String> details = DetailHelper.getDetails(httpHeaders, uriInfo);
        boolean includeData = details.contains(DetailHelper.MAIN);
        boolean includeSize = details.contains("size");

        if (includeData) {
            // Fill VmInit for entities - the search query no join the VmInit to Templates
            IdsQueryParameters params = new IdsQueryParameters();
            List<Guid> ids = entities.stream().map(VmTemplate::getId).collect(Collectors.toList());
            params.setId(ids);
            QueryReturnValue queryReturnValue = runQuery(QueryType.GetVmsInit, params);
            if (queryReturnValue.getSucceeded() && queryReturnValue.getReturnValue() != null) {
                List<VmInit> vmInits = queryReturnValue.getReturnValue();
                Map<Guid, VmInit> initMap = Entities.businessEntitiesById(vmInits);
                for (VmTemplate template : entities) {
                    template.setVmInit(initMap.get(template.getId()));
                }
            }
        }

        Templates collection = new Templates();
        if (includeData) {
            for (VmTemplate entity : entities) {
                Template template = map(entity);
                collection.getTemplates().add(addLinks(populate(template, entity)));
                DisplayHelper.adjustDisplayData(this, template);
            }
        }
        if (includeSize) {
            collection.setSize((long) entities.size());
        }
        return collection;
    }

    protected VmStatic getVm(Cluster cluster, Template template) {
        org.ovirt.engine.core.common.businessentities.VM vm;
        if (template.getVm().isSetId()) {
            vm = getEntity(org.ovirt.engine.core.common.businessentities.VM.class,
                           QueryType.GetVmByVmId,
                           new IdQueryParameters(asGuid(template.getVm().getId())),
                           template.getVm().getId());
        } else {
            Guid dataCenterId = null;
            if (cluster != null && cluster.getStoragePoolId() != null) {
                dataCenterId = cluster.getStoragePoolId();
            }
            GetVmByVmNameForDataCenterParameters params =
                    new GetVmByVmNameForDataCenterParameters(dataCenterId, template.getVm().getName());
            params.setFiltered(isFiltered());
            vm = getEntity(org.ovirt.engine.core.common.businessentities.VM.class,
                           QueryType.GetVmByVmNameForDataCenter,
                    params,
                           template.getVm().getName());
        }
        return vm.getStaticData();
    }

    protected boolean namedCluster(Template template) {
        return template.isSetCluster() && template.getCluster().isSetName() && !template.getCluster().isSetId();
    }

    protected Guid getClusterId(Template template) {
        return getEntity(Cluster.class,
                QueryType.GetClusterByName,
                new NameQueryParameters(template.getCluster().getName()),
                "Cluster: name=" + template.getCluster().getName()).getId();
    }

    @Override
    protected Template doPopulate(Template model, VmTemplate entity) {
        if (!model.isSetConsole()) {
            model.setConsole(new Console());
        }
        model.getConsole().setEnabled(!getConsoleDevicesForEntity(entity.getId()).isEmpty());
        if (!model.isSetVirtioScsi()) {
            model.setVirtioScsi(new VirtioScsi());
        }
        model.getVirtioScsi().setEnabled(!VmHelper.getVirtioScsiControllersForEntity(this, entity.getId()).isEmpty());
        model.setSoundcardEnabled(!VmHelper.getSoundDevicesForEntity(this, entity.getId()).isEmpty());
        model.setTpmEnabled(!VmHelper.getTpmDevicesForEntity(this, entity.getId()).isEmpty());
        List<VmRngDevice> rngDevices = getRngDevices(entity.getId());
        if (rngDevices != null && !rngDevices.isEmpty()) {
            model.setRngDevice(RngDeviceMapper.map(rngDevices.get(0), null));
        }
        return model;
    }

    private List<VmRngDevice> getRngDevices(Guid id) {
        return getEntity(List.class,
            QueryType.GetRngDevice,
            new IdQueryParameters(id),
            "GetRngDevice", true);
    }

    private List<String> getConsoleDevicesForEntity(Guid id) {
        return getEntity(List.class,
                QueryType.GetConsoleDevices,
                new IdQueryParameters(id),
                "GetConsoleDevices", true);
    }

}
