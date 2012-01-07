package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.common.util.DetailHelper.Detail;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.model.Tags;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VMs;
import org.ovirt.engine.api.resource.VmResource;
import org.ovirt.engine.api.resource.VmsResource;

import org.ovirt.engine.core.common.action.AddVmFromScratchParameters;
import org.ovirt.engine.core.common.action.AddVmFromTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByVmTemplateIdQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesDisksParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.api.restapi.types.DiskMapper;


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
        if (vm.isSetDisks() && vm.getDisks().isSetClone() && vm.getDisks().isClone()){
            //disks are always cloned on the storage-domain, which contains the disk from which they are cloned.
            //therefore, even if user passed storage-domain, it is ignored in this context.
            response = cloneVmFromTemplate(staticVm, vm.getDisks(), templateId);
        } else if (templateId.equals(Guid.Empty)) {
            response = addVmFromScratch(staticVm, storageDomainId, vm.getDisks());
        } else {
            response = addVm(staticVm, storageDomainId.equals(Guid.Empty) ? getTemplateStorageDomain(templateId) : storageDomainId);
        }
        return response;
    }

    private String getHostId(String hostName) {
        return getEntity(VDS.class, SearchType.VDS, "Hosts: name=" + hostName).getvds_id().toString();
    }

    private Response cloneVmFromTemplate(VmStatic staticVm, Disks disks, Guid templateId) {
        return performCreation(VdcActionType.AddVmFromTemplate,
                               new AddVmFromTemplateParameters(staticVm, getDisksToClone(disks, templateId), getTemplateStorageDomain(templateId)),
                               new QueryIdResolver(VdcQueryType.GetVmByVmId, GetVmByVmIdParameters.class));
    }

    private HashMap<String, DiskImageBase> getDisksToClone(Disks disks, Guid templateId) {
        HashMap<String, DiskImageBase> disksMap = new HashMap<String, DiskImageBase>();

        if (disks.isSetDisks() && disks.getDisks().size() > 0){
            HashMap<Guid, DiskImage> templatesDisksMap = getTemplateDisks(templateId);
            for(Disk disk : disks.getDisks()){
                DiskImage templateDisk = templatesDisksMap.get(Guid.createGuidFromString(disk.getId()));
                if( templateDisk != null ){
                    disksMap.put(templateDisk.getinternal_drive_mapping(), map(disk, templateDisk));
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
            templatesDisksMap.put(di.getId(), di);
        }
        return templatesDisksMap;
    }

    private DiskImage map(Disk entity, DiskImage template) {
        return getMapper(Disk.class, DiskImage.class).map(entity, template);
    }

    protected Response addVm(VmStatic staticVm, Guid storageDomainId) {
        VmManagementParametersBase params = new VmManagementParametersBase(staticVm);
        params.setStorageDomainId(storageDomainId);
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

    protected Guid getTemplateStorageDomain(Guid templateId) {
        Guid domainId = Guid.Empty;
        try {
            VdcQueryReturnValue queryReturn = backend.RunQuery(
                    VdcQueryType.GetStorageDomainsByVmTemplateId,
                    sessionize(new GetStorageDomainsByVmTemplateIdQueryParameters(templateId)));
            if (queryReturn.getSucceeded()) {
                storage_domains domain = (asCollection(storage_domains.class, queryReturn.getReturnValue())).get(0);
                if (domain != null) {
                    domainId = domain.getid();
                }
            }
        } catch (Exception e) {
            // best effort semantics
        }
        return domainId;
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
                "Cluster: name=" + vm.getCluster().getName()).getID();
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
