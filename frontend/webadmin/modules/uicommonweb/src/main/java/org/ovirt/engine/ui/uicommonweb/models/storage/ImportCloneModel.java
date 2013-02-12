package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class ImportCloneModel extends ConfirmationModel {

    EntityModel noClone;
    EntityModel clone;
    EntityModel applyToAll;

    EntityModel suffix;
    EntityModel name;

    public EntityModel getNoClone() {
        return noClone;
    }

    public void setNoClone(EntityModel noClone) {
        this.noClone = noClone;
    }

    public EntityModel getClone() {
        return clone;
    }

    public void setClone(EntityModel clone) {
        this.clone = clone;
    }

    public EntityModel getApplyToAll() {
        return applyToAll;
    }

    public void setApplyToAll(EntityModel applyToAll) {
        this.applyToAll = applyToAll;
    }

    public EntityModel getName() {
        return name;
    }

    public void setName(EntityModel name) {
        this.name = name;
    }

    public EntityModel getSuffix() {
        return suffix;
    }

    public void setSuffix(EntityModel suffix) {
        this.suffix = suffix;
    }

    public ImportCloneModel() {
        setNoClone(new EntityModel());
        getNoClone().setEntity(false);
        setClone(new EntityModel());
        getClone().setEntity(true);
        setName(new EntityModel());
        setApplyToAll(new EntityModel());
        getApplyToAll().setEntity(false);
        setSuffix(new EntityModel());
        getSuffix().setIsChangable(false);
        getSuffix().setEntity("_Copy"); //$NON-NLS-1$
        getClone().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean value = (Boolean) getClone().getEntity();
                if (value) {
                    getNoClone().setEntity(false);
                    if ((Boolean) getApplyToAll().getEntity()) {
                        getSuffix().setIsChangable(true);
                    } else {
                        getName().setIsChangable(true);
                    }
                }
            }
        });
        getNoClone().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean value = (Boolean) getNoClone().getEntity();
                if (value) {
                    getClone().setEntity(false);
                    getName().setIsChangable(false);
                    getSuffix().setIsChangable(false);
                }
            }
        });
        getApplyToAll().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (!((Boolean) getNoClone().getEntity())) {
                    Boolean value = (Boolean) getApplyToAll().getEntity();
                    getSuffix().setIsChangable(value);
                    getName().setIsChangable(!value);
                }
            }
        });
    }
}
