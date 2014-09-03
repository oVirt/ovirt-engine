package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.CdRoms;
import org.ovirt.engine.api.resource.DeviceResource;
import org.ovirt.engine.api.resource.DevicesResource;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;

import java.util.List;

import static org.ovirt.engine.api.restapi.types.CdRomMapper.CDROM_ID;

public class BackendCdRomsResource
        extends AbstractBackendDevicesResource<CdRom, CdRoms, VM>
        implements DevicesResource<CdRom, CdRoms> {

    public BackendCdRomsResource(Guid parentId,
                                 VdcQueryType queryType,
                                 VdcQueryParametersBase queryParams) {
        super(CdRom.class,
              CdRoms.class,
              VM.class,
              parentId,
              queryType,
              queryParams,
              VdcActionType.UpdateVm,
              VdcActionType.UpdateVm,
              VdcActionType.UpdateVm);
    }

    @Override
    protected <T> boolean matchEntity(VM entity, T id) {
        return (id == null || id.equals(CDROM_ID)) && parentId.equals(entity.getQueryableId());
    }

    @Override
    protected boolean matchEntity(VM entity, String name) {
        return false;
    }

    @Override
    protected String[] getRequiredAddFields() {
        return new String[] { "file.id" };
    }

    @Override
    protected String[] getRequiredUpdateFields() {
        return new String[] { "file" };
    }

    @Override
    protected VdcActionParametersBase getAddParameters(VM mapped, CdRom cdrom) {
        return new VmManagementParametersBase(getUpdatable(mapped.getStaticData().getIsoPath()));
    }

    @Override
    protected VdcActionParametersBase getRemoveParameters(String id) {
        return new VmManagementParametersBase(getUpdatable(null));
    }

    @Override
    protected CdRoms mapCollection(List<VM> entities) {
        if (QueryHelper.hasCurrentConstraint(getUriInfo())) {
            for (VM entity : entities) {
                // change the iso path so the result of 'map' will contain current cd instead of the
                // persistent configuration
                entity.setIsoPath(entity.getCurrentCd());
            }
        }
        return super.mapCollection(entities);
    }

    protected VmStatic getUpdatable(String isoPath) {
        VmStatic updatable = getEntity(VM.class,
                                       VdcQueryType.GetVmByVmId,
                                       new IdQueryParameters(parentId),
                                       parentId.toString()).getStaticData();
        updatable.setIsoPath(isoPath);
        return updatable;
    }

    @Override
    protected ParametersProvider<CdRom, VM> getUpdateParametersProvider() {
        return new UpdateParametersProvider();
    }

    protected class UpdateParametersProvider implements ParametersProvider<CdRom, VM> {
        @Override
        public VdcActionParametersBase getParameters(CdRom incoming, VM entity) {
            return new VmManagementParametersBase(getUpdatable(incoming.getFile().getId()));
        }
    }

    @Override
    @SingleEntityResource
    public DeviceResource<CdRom> getDeviceSubResource(String id) {
        return inject(new BackendCdRomResource(modelType,
                                               entityType,
                                               asGuidOr404(id),
                                               this,
                                               updateType,
                                               getUpdateParametersProvider(),
                                               getRequiredUpdateFields()));
    }

    @Override
    protected CdRom doPopulate(CdRom model, VM entity) {
        return model;
    }
}
