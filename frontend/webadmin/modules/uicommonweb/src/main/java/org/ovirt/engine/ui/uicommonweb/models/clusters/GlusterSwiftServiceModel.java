package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class GlusterSwiftServiceModel extends EntityModel<GlusterServerService> {

    private List<GlusterServerService> internalServiceList;

    private EntityModel<Boolean> startSwift;

    private EntityModel<Boolean> stopSwift;

    private EntityModel<Boolean> restartSwift;

    public GlusterSwiftServiceModel() {
        this(null);
    }

    public GlusterSwiftServiceModel(GlusterServerService service) {
        this(service, new ArrayList<GlusterServerService>());
    }

    public GlusterSwiftServiceModel(GlusterServerService service, List<GlusterServerService> internalServiceList) {
        setStartSwift(new EntityModel<>(Boolean.FALSE));
        setStopSwift(new EntityModel<>(Boolean.FALSE));
        setRestartSwift(new EntityModel<>(Boolean.FALSE));
        setEntity(service);
        setInternalServiceList(internalServiceList);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        GlusterServerService service = getEntity();
        if (service == null || service.getStatus() == GlusterServiceStatus.NOT_AVAILABLE) {
            getStartSwift().setIsChangeable(false);
            getStopSwift().setIsChangeable(false);
            getRestartSwift().setIsChangeable(false);
        } else {
            getStartSwift().setIsChangeable(service.getStatus() != GlusterServiceStatus.RUNNING);
            getStopSwift().setIsChangeable(service.getStatus() != GlusterServiceStatus.STOPPED);
            getRestartSwift().setIsChangeable(true);
        }
    }

    public List<GlusterServerService> getInternalServiceList() {
        return internalServiceList;
    }

    public void setInternalServiceList(List<GlusterServerService> internalServiceList) {
        this.internalServiceList = internalServiceList;
    }

    public EntityModel<Boolean> getStartSwift() {
        return startSwift;
    }

    public void setStartSwift(EntityModel<Boolean> startSwift) {
        this.startSwift = startSwift;
    }

    public EntityModel<Boolean> getStopSwift() {
        return stopSwift;
    }

    public void setStopSwift(EntityModel<Boolean> stopSwift) {
        this.stopSwift = stopSwift;
    }

    public EntityModel<Boolean> getRestartSwift() {
        return restartSwift;
    }

    public void setRestartSwift(EntityModel<Boolean> restartSwift) {
        this.restartSwift = restartSwift;
    }
}
