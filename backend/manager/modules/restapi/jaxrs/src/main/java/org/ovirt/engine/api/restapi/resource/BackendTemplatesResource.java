package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Console;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Templates;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VirtIOSCSI;
import org.ovirt.engine.api.resource.TemplateResource;
import org.ovirt.engine.api.resource.TemplatesResource;
import org.ovirt.engine.api.restapi.types.RngDeviceMapper;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.VmHelper;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVmByVmNameForDataCenterParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplatesResource
    extends AbstractBackendCollectionResource<Template, VmTemplate>
    implements TemplatesResource {

    static final String[] SUB_COLLECTIONS = { "disks", "nics", "cdroms", "tags", "permissions", "watchdogs" };

    public BackendTemplatesResource() {
        super(Template.class, VmTemplate.class, SUB_COLLECTIONS);
    }

    @Override
    public Templates list() {
        if (isFiltered())
            return mapCollection(getBackendCollection(VdcQueryType.GetAllVmTemplates,
                    new VdcQueryParametersBase()));
        else
            return mapCollection(getBackendCollection(SearchType.VmTemplate));
    }

    @Override
    @SingleEntityResource
    public TemplateResource getTemplateSubResource(String id) {
        return inject(new BackendTemplateResource(id));
    }

    @Override
    public Response add(Template template) {
        validateParameters(template, "name", "vm.id|name");
        validateEnums(Template.class, template);
        Guid clusterId = null;
        VDSGroup cluster = null;
        if (namedCluster(template)) {
            clusterId = getClusterId(template);
            cluster = lookupCluster(clusterId);
        }
        if (template.getVersion() != null) {
            validateParameters(template.getVersion(), "baseTemplate");
        }
        VmStatic staticVm = getMapper(Template.class, VmStatic.class).map(template, getVm(cluster, template));
        if (namedCluster(template)) {
            staticVm.setVdsGroupId(clusterId);
        }

        staticVm.setUsbPolicy(VmMapper.getUsbPolicyOnCreate(template.getUsb(),
                cluster != null ? cluster.getcompatibility_version() : lookupCluster(staticVm.getVdsGroupId()).getcompatibility_version()));

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
            params.setSoundDeviceEnabled(!VmHelper.getInstance().getSoundDevicesForEntity(staticVm.getId()).isEmpty());
        }
        if (template.isSetRngDevice()) {
            params.setUpdateRngDevice(true);
            params.setRngDevice(RngDeviceMapper.map(template.getRngDevice(), null));
        }

        boolean isDomainSet = false;
        if (template.isSetStorageDomain() && template.getStorageDomain().isSetId()) {
            params.setDestinationStorageDomainId(asGuid(template.getStorageDomain().getId()));
            isDomainSet = true;
        }
        params.setDiskInfoDestinationMap(getDiskToDestinationMap(template.getVm(),
                params.getDestinationStorageDomainId(),
                isDomainSet));

        setupCloneVmPermissions(template, params);

        return performCreate(VdcActionType.AddVmTemplate,
                               params,
                               new QueryIdResolver<Guid>(VdcQueryType.GetVmTemplate,
                                                   GetVmTemplateParameters.class));
    }

    void setupCloneVmPermissions(Template template, AddVmTemplateParameters params) {
        if (template.isSetPermissions() && template.getPermissions().isSetClone()) {
            params.setCopyVmPermissions(template.getPermissions().isClone());
        }
    }

    private VDSGroup lookupCluster(Guid id) {
        return getEntity(VDSGroup.class, VdcQueryType.GetVdsGroupByVdsGroupId, new IdQueryParameters(id), "GetVdsGroupByVdsGroupId");
    }

    protected HashMap<Guid, DiskImage> getDiskToDestinationMap(VM vm, Guid storageDomainId, boolean isDomainSet) {
        HashMap<Guid, DiskImage> diskToDestinationMap = null;
        if (vm.isSetDisks() && vm.getDisks().isSetDisks()) {
            diskToDestinationMap = new HashMap<Guid, DiskImage>();
            for (Disk disk : vm.getDisks().getDisks()) {
                if (disk.isSetId() && disk.isSetStorageDomains() && disk.getStorageDomains().isSetStorageDomains()
                        && disk.getStorageDomains().getStorageDomains().get(0).isSetId()) {
                    DiskImage diskImage = new DiskImage();
                    diskImage.setId(asGuid(disk.getId()));
                    diskImage.setStorageIds(new ArrayList<Guid>());
                    Guid newStorageDomainId = isDomainSet ? storageDomainId : asGuid(disk.getStorageDomains()
                            .getStorageDomains().get(0).getId());
                    diskImage.getStorageIds().add(newStorageDomainId);
                    diskToDestinationMap.put(diskImage.getId(), diskImage);
                }
            }
        }
        return diskToDestinationMap;
    }

    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemoveVmTemplate, new VmTemplateParametersBase(asGuid(id)));
    }

    protected Templates mapCollection(List<VmTemplate> entities) {
        // Fill VmInit for entities - the search query no join the VmInit to Templates
        IdsQueryParameters params = new IdsQueryParameters();
        List<Guid> ids = Entities.getIds(entities);
        params.setId(ids);
        VdcQueryReturnValue queryReturnValue = runQuery(VdcQueryType.GetVmsInit, params);
        if (queryReturnValue.getSucceeded() && queryReturnValue.getReturnValue() != null) {
            List<VmInit> vmInits = queryReturnValue.getReturnValue();
            Map<Guid, VmInit> initMap = Entities.businessEntitiesById(vmInits);
            for (VmTemplate template : entities) {
                template.setVmInit(initMap.get(template.getId()));
            }
        }

        Templates collection = new Templates();
        for (VmTemplate entity : entities) {
            collection.getTemplates().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    protected VmStatic getVm(VDSGroup cluster, Template template) {
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
        return getEntity(VDSGroup.class,
                VdcQueryType.GetVdsGroupByName,
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
            model.setVirtioScsi(new VirtIOSCSI());
        }
        model.getVirtioScsi().setEnabled(!VmHelper.getInstance().getVirtioScsiControllersForEntity(entity.getId()).isEmpty());
        model.setSoundcardEnabled(!VmHelper.getInstance().getSoundDevicesForEntity(entity.getId()).isEmpty());
        List<VmRngDevice> rngDevices = getRngDevices(entity.getId());
        if (rngDevices != null && !rngDevices.isEmpty()) {
            model.setRngDevice(RngDeviceMapper.map(rngDevices.get(0), null));
        }
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
