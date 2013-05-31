package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class ManageGlusterSwiftModel extends EntityModel {

    private EntityModel swiftStatus;

    private EntityModel startSwift;

    private EntityModel stopSwift;

    private EntityModel restartSwift;

    private EntityModel isManageServerLevel;

    private ListModel hostServicesList;

    public ManageGlusterSwiftModel() {
        setSwiftStatus(new EntityModel());
        setStartSwift(new EntityModel(Boolean.FALSE));
        setStopSwift(new EntityModel(Boolean.FALSE));
        setRestartSwift(new EntityModel(Boolean.FALSE));
        setIsManageServerLevel(new EntityModel());
        setHostServicesList(new ListModel());

        getIsManageServerLevel().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getHostServicesList().setIsChangable((Boolean) getIsManageServerLevel().getEntity());
            }
        });
        getIsManageServerLevel().setEntity(Boolean.FALSE);
    }

    public EntityModel getSwiftStatus() {
        return swiftStatus;
    }

    public void setSwiftStatus(EntityModel swiftStatus) {
        this.swiftStatus = swiftStatus;
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

    public EntityModel getIsManageServerLevel() {
        return isManageServerLevel;
    }

    public void setIsManageServerLevel(EntityModel isManageServerLevel) {
        this.isManageServerLevel = isManageServerLevel;
    }

    public ListModel getHostServicesList() {
        return hostServicesList;
    }

    public void setHostServicesList(ListModel hostServicesList) {
        this.hostServicesList = hostServicesList;
    }
}
