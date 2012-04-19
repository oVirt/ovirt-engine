package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.common.util.DetailHelper.Detail;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Snapshots;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.model.Tags;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VMs;
import org.ovirt.engine.api.resource.VmResource;
import org.ovirt.engine.api.resource.VmsResource;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.core.common.action.AddVmFromScratchParameters;
import org.ovirt.engine.core.common.action.AddVmFromSnapshotParameters;
import org.ovirt.engine.core.common.action.AddVmFromTemplateParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetVmConfigurationBySnapshotQueryParams;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesDisksParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;


public class BackendVmsResource extends
        AbstractBackendCollectionResource<VM, org.ovirt.engine.core.common.businessentities.VM>
        implements VmsResource {

    static final String[] SUB_COLLECTIONS = { "disks", "nics", "cdroms", "snapshots", "tags", "permissions", "statistics" };

    public BackendVmsResource() {
        super(VM.class, org.ovirt.engine.core.common.businessentities.VM.class, SUB_COLLECTIONS);
    }

    @Override
    public VMs list() {
         return mapCollection(getBackendCollection(SearchType.VM));
    }

    @Override
    @SingleEntityResource
    public VmResource getVmSubResource(String id) {
        return inject(new BackendVmResource(id, this));
    }

    @Override
    public Response add(VM vm) {
        validateParameters(vm, "name", "template.id|name", "cluster.id|name");

        Guid templateId = getTemplateId(vm);
        VmStatic staticVm = getMapper(VM.class, VmStatic.class).map(vm,
                                                                    getMapper(VmTemplate.class, VmStatic.class).map(lookupTemplate(templateId), null));

        if (namedCluster(vm)) {
            staticVm.setvds_group_id(getClusterId(vm));
        }

        //if the user set the host-name within placement-policy, rather than the host-id (legal) -
        //resolve the host's ID, because it will be needed down the line
        if (vm.isSetPlacementPolicy() && vm.getPlacementPolicy().isSetHost()
                && vm.getPlacementPolicy().getHost().isSetName() && !vm.getPlacementPolicy().getHost().isSetId()) {
            vm.getPlacementPolicy().getHost().setId(getHostId(vm.getPlacementPolicy().getHost().getName()));
        }

        Response response = null;
        Guid storageDomainId = ( vm.isSetStorageDomain() && vm.getStorageDomain().isSetId() ) ? asGuid(vm.getStorageDomain().getId()) : Guid.Empty;
        if (vm.isSetSnapshots() && vm.getSnapshots().getSnapshots() != null
                && !vm.getSnapshots().getSnapshots().isEmpty()) {
            // If Vm has snapshots collection - this is a clone vm from snapshot operation
            String snapshotId = getSnapshotId(vm.getSnapshots());
            org.ovirt.engine.core.common.businessentities.VM vmConfiguration = getVmConfiguration(snapshotId);
            getMapper(VM.class, VmStatic.class).map(vm, vmConfiguration.getStaticData());
            // If vm passed in the call has disks attached on them,
            // merge their data with the data of the disks on the configuration
            // The parameters to AddVmFromSnapshot hold an array list of Disks
            // and not List of Disks, as this is a GWT serialization limitation,
            // and this parameter class serves GWT clients as well.
            HashMap<Guid, DiskImage> diskImagesById = getDiskImagesByIdMap(vmConfiguration.getDiskMap().values());
            if (vm.isSetDisks()) {
                prepareImagesForCloneFromSnapshotParams(vm.getDisks(), diskImagesById);
            }
            response =
                        cloneVmFromSnapshot(vmConfiguration.getStaticData(),
                                snapshotId,
                                diskImagesById);
        } else if (vm.isSetDisks() && vm.getDisks().isSetClone() && vm.getDisks().isClone()) {
            response = cloneVmFromTemplate(staticVm, vm.getDisks(), templateId);
        } else if (Guid.Empty.equals(templateId)) {
            response = addVmFromScratch(staticVm, storageDomainId, vm.getDisks());
        } else {
            response = addVm(staticVm, storageDomainId, vm.getDisks(), templateId);
        }
        return response;
    }

    protected org.ovirt.engine.core.common.businessentities.VM getVmConfiguration(String snapshotId) {
        org.ovirt.engine.core.common.businessentities.VM vmConfiguration =
                getEntity(org.ovirt.engine.core.common.businessentities.VM.class,
                        VdcQueryType.GetVmConfigurationBySnapshot,
                        new GetVmConfigurationBySnapshotQueryParams(asGuid(snapshotId)),
                        "");
        return vmConfiguration;
    }

    private void prepareImagesForCloneFromSnapshotParams(Disks disks,
            Map<Guid, DiskImage> imagesFromConfiguration) {
        if (disks.getDisks() != null) {
            for (Disk disk : disks.getDisks()) {
                DiskImage diskImageFromConfig = imagesFromConfiguration.get(asGuid(disk.getId()));
                DiskImage diskImage = getMapper(Disk.class, DiskImage.class).map(disk, diskImageFromConfig);
                imagesFromConfiguration.put(diskImage.getImageId(), diskImage);
            }
        }
    }

    private HashMap<Guid, DiskImage> getDiskImagesByIdMap(Collection<DiskImage> values) {
        HashMap<Guid, DiskImage> result = new HashMap<Guid, DiskImage>();
        for (DiskImage diskImage : values) {
            result.put(diskImage.getImageId(), diskImage);
        }
        return result;
    }

    private String getSnapshotId(Snapshots snapshots) {
        return (snapshots.getSnapshots() != null && !snapshots.getSnapshots().isEmpty()) ? snapshots.getSnapshots()
                .get(0)
                .getId() : Guid.Empty.toString();
    }

    private String getHostId(String hostName) {
        return getEntity(VDS.class, SearchType.VDS, "Hosts: name=" + hostName).getId().toString();
    }

    private Response cloneVmFromSnapshot(VmStatic staticVm,
            String snapshotId,
            HashMap<Guid, DiskImage> images) {
        Guid sourceSnapshotId = asGuid(snapshotId);
        AddVmFromSnapshotParameters params =
                new AddVmFromSnapshotParameters(staticVm, sourceSnapshotId);
        params.setDiskInfoDestinationMap(images);
        return performCreation(VdcActionType.AddVmFromSnapshot,
                                params,
                                new QueryIdResolver(VdcQueryType.GetVmByVmId, GetVmByVmIdParameters.class));
    }

    private Response cloneVmFromTemplate(VmStatic staticVm, Disks disks, Guid templateId) {
        return performCreation(VdcActionType.AddVmFromTemplate,
                               new AddVmFromTemplateParameters(staticVm, getDisksToClone(disks, templateId), Guid.Empty),
                               new QueryIdResolver(VdcQueryType.GetVmByVmId, GetVmByVmIdParameters.class));
    }

    private HashMap<Guid, DiskImage> getDisksToClone(Disks disks, Guid templateId) {
        HashMap<Guid, DiskImage> disksMap = new HashMap<Guid, DiskImage>();

        if (disks != null && disks.isSetDisks() && disks.getDisks().size() > 0){
            HashMap<Guid, DiskImage> templatesDisksMap = getTemplateDisks(templateId);
            for (Disk disk : disks.getDisks()) {
                DiskImage templateDisk = templatesDisksMap.get(Guid.createGuidFromString(disk.getId()));
                if( templateDisk != null ) {
                    disksMap.put(templateDisk.getImageId(), map(disk, templateDisk));
                } else {
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }
            }
        }
        return disksMap;
    }

    @SuppressWarnings("unchecked")
    private HashMap<Guid, DiskImage> getTemplateDisks(Guid templateId) {
        HashMap<Guid, DiskImage> templatesDisksMap = new HashMap<Guid, DiskImage>();
        for(DiskImage di : (List<DiskImage>)getEntity(List.class,
                                                      VdcQueryType.GetVmTemplatesDisks,
                                                      new GetVmTemplatesDisksParameters(templateId),
                                                      "Disks")){
            templatesDisksMap.put(di.getImageId(), di);
        }
        return templatesDisksMap;
    }

    private DiskImage map(Disk entity, DiskImage template) {
        return getMapper(Disk.class, DiskImage.class).map(entity, template);
    }

    protected Response addVm(VmStatic staticVm, Guid storageDomainId, Disks disks, Guid templateId) {
        VmManagementParametersBase params = new VmManagementParametersBase(staticVm);
        params.setStorageDomainId(storageDomainId);
        params.setDiskInfoDestinationMap(getDisksToClone(disks, templateId));
        return performCreation(VdcActionType.AddVm,
                               params,
                               new QueryIdResolver(VdcQueryType.GetVmByVmId, GetVmByVmIdParameters.class));
    }

    protected Response addVmFromScratch(VmStatic staticVm, Guid storageDomainId, Disks disks) {
        AddVmFromScratchParameters params = new AddVmFromScratchParameters(staticVm, mapDisks(disks), Guid.Empty);
        params.setStorageDomainId(storageDomainId);
        return performCreation(VdcActionType.AddVmFromScratch,
                               params,
                               new QueryIdResolver(VdcQueryType.GetVmByVmId, GetVmByVmIdParameters.class));
    }

    private ArrayList<DiskImageBase> mapDisks(Disks disks) {
        ArrayList<DiskImageBase> diskImages = null;
        if (disks!=null && disks.isSetDisks()) {
            diskImages = new ArrayList<DiskImageBase>();
            for (Disk disk : disks.getDisks()) {
                DiskImage diskImage = DiskMapper.map(disk, null);
                diskImages.add(diskImage);
            }
        }
        return diskImages;
    }

    protected VM addInlineDetails(Set<Detail> details, VM vm) {
        if (details.contains(Detail.DISKS)) {
            addInlineDisks(vm);
        }
        if (details.contains(Detail.NICS)) {
            addInlineNics(vm);
        }
        if (details.contains(Detail.TAGS)) {
            addInlineTags(vm);
        }
        return vm;
    }

    private void addInlineStatistics(VM vm) {
        EntityIdResolver resolver = new QueryIdResolver(VdcQueryType.GetVmByVmId, GetVmByVmIdParameters.class);
        VmStatisticalQuery query = new VmStatisticalQuery(resolver, newModel(vm.getId()));
        BackendStatisticsResource<VM, org.ovirt.engine.core.common.businessentities.VM> statisticsResource = inject(new BackendStatisticsResource<VM, org.ovirt.engine.core.common.businessentities.VM>(entityType, Guid.createGuidFromString(vm.getId()), query));
        Statistics statistics = statisticsResource.list();
        vm.setStatistics(statistics);
    }

    private void addInlineTags(VM vm) {
        BackendVmTagsResource tagsResource = inject(new BackendVmTagsResource(vm.getId()));
        Tags tags = tagsResource.list();
        vm.setTags(tags);
    }

    private void addInlineNics(VM vm) {
        BackendVmNicsResource nicsResource = inject(new BackendVmNicsResource(asGuid(vm.getId())));
        Nics nics = nicsResource.list();
        vm.setNics(nics);
    }

    private void addInlineDisks(VM vm) {
        BackendDisksResource disksResource = inject(new BackendDisksResource(Guid.createGuidFromString(vm.getId()),
                VdcQueryType.GetAllDisksByVmId,
                new GetAllDisksByVmIdParameters(Guid.createGuidFromString(vm.getId()))));
        Disks disks = disksResource.list();
        vm.setDisks(disks);
    }

    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemoveVm, new RemoveVmParameters(asGuid(id), false));
    }

    @Override
    public Response remove(String id, Action action) {
        getEntity(id);
        return performAction(VdcActionType.RemoveVm,
                             new RemoveVmParameters(asGuid(id),
                                                    action != null && action.isSetForce() ? action.isForce()
                                                                                     :
                                                                                     false));

    }

    protected VMs mapCollection(List<org.ovirt.engine.core.common.businessentities.VM> entities) {
        VMs collection = new VMs();
        for (org.ovirt.engine.core.common.businessentities.VM entity : entities) {
            collection.getVMs().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    protected boolean templated(VM vm) {
        return vm.isSetTemplate() && (vm.getTemplate().isSetId() || vm.getTemplate().isSetName());
    }

    protected Guid getTemplateId(VM vm) {
        return vm.getTemplate().isSetId() ? asGuid(vm.getTemplate().getId()) : getEntity(
                VmTemplate.class, SearchType.VmTemplate,
                "Template: name=" + vm.getTemplate().getName()).getId();
    }

    public VmTemplate lookupTemplate(Guid id) {
        return getEntity(VmTemplate.class, VdcQueryType.GetVmTemplate, new GetVmTemplateParameters(id), "GetVmTemplate");
    }

    protected boolean namedCluster(VM vm) {
        return vm.isSetCluster() && vm.getCluster().isSetName() && !vm.getCluster().isSetId();
    }

    protected Guid getClusterId(VM vm) {
        return getEntity(VDSGroup.class, SearchType.Cluster,
                "Cluster: name=" + vm.getCluster().getName()).getId();
    }

    @Override
    protected VM populate(VM model, org.ovirt.engine.core.common.businessentities.VM entity) {
        Set<Detail> details = DetailHelper.getDetails(getHttpHeaders());
        model = addInlineDetails(details, model);
        if (details.contains(Detail.STATISTICS)) {
            addInlineStatistics(model);
        }
        return model;
    }
}
