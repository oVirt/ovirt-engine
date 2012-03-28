package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.common.util.LinkHelper;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.resource.DeviceResource;
import org.ovirt.engine.api.resource.DevicesResource;
import org.ovirt.engine.core.common.action.AddDiskToVmParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;

public class BackendDisksResource
        extends AbstractBackendDevicesResource<Disk, Disks, DiskImage>
        implements DevicesResource<Disk, Disks> {

    private static final String SUB_COLLECTIONS = "statistics";

    public BackendDisksResource(Guid parentId,
                                VdcQueryType queryType,
                                VdcQueryParametersBase queryParams) {
        super(Disk.class,
              Disks.class,
              DiskImage.class,
              parentId,
              queryType,
              queryParams,
              VdcActionType.AddDiskToVm,
              VdcActionType.RemoveDisk,
              VdcActionType.UpdateVmDisk,
              SUB_COLLECTIONS);
    }

    @Override
    @SingleEntityResource
    public DeviceResource<Disk> getDeviceSubResource(String id) {
        return inject(new BackendDiskResource(id,
                                              this,
                                              updateType,
                                              getUpdateParametersProvider(),
                                              getRequiredUpdateFields(),
                                              subCollections));
    }

    @Override
    protected boolean matchEntity(DiskImage entity, Guid id) {
        return id != null && (id.equals(entity.getId()) || id.equals(entity.getvm_snapshot_id()));
    }

    @Override
    protected boolean matchEntity(DiskImage entity, String name) {
        return false;
    }

    @Override
    protected String[] getRequiredAddFields() {
        return new String[] { "size" };
    }

    @Override
    protected String[] getRequiredUpdateFields() {
        return new String[0];
    }

    @Override
    protected VdcActionParametersBase getAddParameters(DiskImage entity, Disk disk) {
        AddDiskToVmParameters parameters = new AddDiskToVmParameters(parentId, entity);
        if (disk.isSetStorageDomains() && disk.getStorageDomains().getStorageDomains().get(0).isSetId()) {
            parameters.setStorageDomainId(new Guid(disk.getStorageDomains().getStorageDomains().get(0).getId()));
        }
        return parameters;
    }

    @Override
    protected VdcActionParametersBase getRemoveParameters(String id) {
        return new RemoveDiskParameters(asGuid(id));
    }

    @Override
    protected ParametersProvider<Disk, DiskImage> getUpdateParametersProvider() {
        return new UpdateParametersProvider();
    }

    protected class UpdateParametersProvider implements ParametersProvider<Disk, DiskImage> {
        @Override
        public VdcActionParametersBase getParameters(Disk incoming, DiskImage entity) {
            return new UpdateVmDiskParameters(parentId, entity.getId(), map(incoming, entity));
        }
    }

    @Override
    protected Disk populate(Disk model, DiskImage entity) {
        return addStatistics(model, entity, uriInfo, httpHeaders);
    }

    Disk addStatistics(Disk model, DiskImage entity, UriInfo ui, HttpHeaders httpHeaders) {
        if (DetailHelper.include(httpHeaders, "statistics")) {
            model.setStatistics(new Statistics());
            DiskStatisticalQuery query = new DiskStatisticalQuery(newModel(model.getId()));
            List<Statistic> statistics = query.getStatistics(entity);
            for (Statistic statistic : statistics) {
                LinkHelper.addLinks(ui, statistic, query.getParentType());
            }
            model.getStatistics().getStatistics().addAll(statistics);
        }
        return model;
    }
}
