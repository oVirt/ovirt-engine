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
import org.ovirt.engine.api.model.Console;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Templates;
import org.ovirt.engine.api.model.VirtioScsi;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.TemplateResource;
import org.ovirt.engine.api.resource.TemplatesResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.api.restapi.types.RngDeviceMapper;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.restapi.util.IconHelper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.api.restapi.util.VmHelper;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
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
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplatesResource
    extends AbstractBackendCollectionResource<Template, VmTemplate>
    implements TemplatesResource {

    static final String[] SUB_COLLECTIONS = {
        "cdroms",
        "diskattachments",
        "graphicsconsoles",
        "nics",
        "permissions",
        "tags",
        "watchdogs",
    };

    public static final String CLONE_PERMISSIONS = "clone_permissions";

    public BackendTemplatesResource() {
        super(Template.class, VmTemplate.class, SUB_COLLECTIONS);
    }

    @Override
    public Templates list() {
        if (isFiltered()) {
            return mapCollection(getBackendCollection(VdcQueryType.GetAllVmTemplates,
                    new VdcQueryParametersBase(), SearchType.VmTemplate));
        } else {
            return mapCollection(getBackendCollection(SearchType.VmTemplate));
        }
    }

    @Override
    public TemplateResource getTemplateResource(String id) {
        return inject(new BackendTemplateResource(id));
    }

    @Override
    public Response add(Template template) {
        validateParameters(template, "name", "vm.id|name");
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
        VmStatic staticVm = getMapper(Template.class, VmStatic.class).map(template, getVm(cluster, template));
        if (namedCluster(template)) {
            staticVm.setClusterId(clusterId);
        }

        staticVm.setUsbPolicy(VmMapper.getUsbPolicyOnCreate(template.getUsb()));

        // REVISIT: powershell has a IsVmTemlateWithSameNameExist safety check
        AddVmTemplateParameters params = new AddVmTemplateParameters(staticVm,
                                       template.getName(),
                                       template.getDescription());
        if (template.getVersion() != null) {
            params.setBaseTemplateId(Guid.createGuidFromString(template.getVersion().getBaseTemplate().getId()));
            params.setTemplateVersionName(template.getVersion().getVersionName());
        }
        params.setConsoleEnabled(template.getConsole() != null && template.getConsole().isSetEnabled() ?
                        template.getConsole().isEnabled() :
                        !getConsoleDevicesForEntity(staticVm.getId()).isEmpty());
        params.setVirtioScsiEnabled(template.isSetVirtioScsi() && template.getVirtioScsi().isSetEnabled() ?
                template.getVirtioScsi().isEnabled() : null);
        if(template.isSetSoundcardEnabled()) {
            params.setSoundDeviceEnabled(template.isSoundcardEnabled());
        } else {
            params.setSoundDeviceEnabled(!VmHelper.getSoundDevicesForEntity(this, staticVm.getId()).isEmpty());
        }
        if (template.isSetRngDevice()) {
            params.setUpdateRngDevice(true);
            params.setRngDevice(RngDeviceMapper.map(template.getRngDevice(), null));
        }

        DisplayHelper.setGraphicsToParams(template.getDisplay(), params);

        boolean isDomainSet = false;
        if (template.isSetStorageDomain() && template.getStorageDomain().isSetId()) {
            params.setDestinationStorageDomainId(asGuid(template.getStorageDomain().getId()));
            isDomainSet = true;
        }
        params.setDiskInfoDestinationMap(getDestinationTemplateDiskMap(template.getVm(),
            params.getDestinationStorageDomainId(),
            isDomainSet));

        setupCloneVmPermissions(params);
        IconHelper.setIconToParams(template, params);

        Response response = performCreate(
            VdcActionType.AddVmTemplate,
            params,
            new QueryIdResolver<Guid>(VdcQueryType.GetVmTemplate, GetVmTemplateParameters.class)
        );

        Template result = (Template) response.getEntity();
        if (result != null) {
            DisplayHelper.adjustDisplayData(this, result);
        }

        return response;
    }

    private void validateIconParameters(Template incoming) {
        if (!IconHelper.validateIconParameters(incoming)) {
            throw new BaseBackendResource.WebFaultException(null,
                    localize(Messages.INVALID_ICON_PARAMETERS),
                    Response.Status.BAD_REQUEST);
        }
    }

    void setupCloneVmPermissions(AddVmTemplateParameters params) {
        boolean clonePermissions = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, CLONE_PERMISSIONS, true, false);
        if (clonePermissions) {
            params.setCopyVmPermissions(clonePermissions);
        }
    }

    private Cluster lookupCluster(Guid id) {
        return getEntity(Cluster.class, VdcQueryType.GetClusterByClusterId, new IdQueryParameters(id), "GetClusterByClusterId");
    }

    protected HashMap<Guid, DiskImage> getDestinationTemplateDiskMap(Vm vm, Guid storageDomainId, boolean isTemplateGeneralStorageDomainSet) {
        HashMap<Guid, DiskImage> destinationTemplateDiskMap = null;
        if (vm.isSetDiskAttachments() && vm.getDiskAttachments().isSetDiskAttachments()) {
            destinationTemplateDiskMap = new HashMap<>();
            Map<Guid, org.ovirt.engine.core.common.businessentities.storage.Disk> vmSourceDisks = queryVmDisksMap(vm);

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

    private Map<Guid, org.ovirt.engine.core.common.businessentities.storage.Disk> queryVmDisksMap(Vm vm) {
        List<org.ovirt.engine.core.common.businessentities.storage.Disk> vmDisks =
                getBackendCollection(org.ovirt.engine.core.common.businessentities.storage.Disk.class,
                        VdcQueryType.GetAllDisksByVmId,
                        new IdQueryParameters(asGuid(vm.getId())));
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
            VdcQueryReturnValue queryReturnValue = runQuery(VdcQueryType.GetVmsInit, params);
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
                           VdcQueryType.GetVmByVmId,
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
                           VdcQueryType.GetVmByVmNameForDataCenter,
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
                VdcQueryType.GetClusterByName,
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
        List<VmRngDevice> rngDevices = getRngDevices(entity.getId());
        if (rngDevices != null && !rngDevices.isEmpty()) {
            model.setRngDevice(RngDeviceMapper.map(rngDevices.get(0), null));
        }
        MemoryPolicyHelper.setupMemoryBalloon(model, this);
        return model;
    }

    private List<VmRngDevice> getRngDevices(Guid id) {
        return getEntity(List.class,
            VdcQueryType.GetRngDevice,
            new IdQueryParameters(id),
            "GetRngDevice", true);
    }

    private List<String> getConsoleDevicesForEntity(Guid id) {
        return getEntity(List.class,
                VdcQueryType.GetConsoleDevices,
                new IdQueryParameters(id),
                "GetConsoleDevices", true);
    }

}
