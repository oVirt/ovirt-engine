package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class GlusterSwiftServiceModel extends EntityModel {

    private List<GlusterServerService> internalServiceList;

    private EntityModel startSwift;

    private EntityModel stopSwift;

    private EntityModel restartSwift;

    public GlusterSwiftServiceModel() {
        this(null);
    }

    public GlusterSwiftServiceModel(GlusterServerService service) {
        this(service, new ArrayList<GlusterServerService>());
    }

    public GlusterSwiftServiceModel(GlusterServerService service, List<GlusterServerService> internalServiceList) {
        setStartSwift(new EntityModel(Boolean.FALSE));
        setStopSwift(new EntityModel(Boolean.FALSE));
        setRestartSwift(new EntityModel(Boolean.FALSE));
        setEntity(service);
        setInternalServiceList(internalServiceList);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        GlusterServerService service = getEntity();
        if (service == null || service.getStatus() == GlusterServiceStatus.NOT_AVAILABLE) {
            getStartSwift().setIsChangable(false);
            getStopSwift().setIsChangable(false);
            getRestartSwift().setIsChangable(false);
        }
        else {
            getStartSwift().setIsChangable(service.getStatus() != GlusterServiceStatus.RUNNING);
            getStopSwift().setIsChangable(service.getStatus() != GlusterServiceStatus.STOPPED);
            getRestartSwift().setIsChangable(true);
        }
    }

    @Override
    public GlusterServerService getEntity() {
        return (GlusterServerService) super.getEntity();
    }

    public List<GlusterServerService> getInternalServiceList() {
        return internalServiceList;
    }

    public void setInternalServiceList(List<GlusterServerService> internalServiceList) {
        this.internalServiceList = internalServiceList;
    }

    public EntityModel getStartSwift() {
        return startSwift;
    }

    public void setStartSwift(EntityModel startSwift) {
        this.startSwift = startSwift;
    }

    public EntityModel getStopSwift() {
        return stopSwift;
    }

    public void setStopSwift(EntityModel stopSwift) {
        this.stopSwift = stopSwift;
    }

    public EntityModel getRestartSwift() {
        return restartSwift;
    }

    public void setRestartSwift(EntityModel restartSwift) {
        this.restartSwift = restartSwift;
    }
}
