package org.ovirt.engine.ui.uicommonweb.models.quota;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class EditQuotaClusterModel extends EntityModel {
    EntityModel unlimitedMem;
    EntityModel unlimitedCpu;

    EntityModel specificMem;
    EntityModel specificCpu;

    EntityModel specificMemValue;
    EntityModel specificCpuValue;

    public EntityModel getUnlimitedMem() {
        return unlimitedMem;
    }

    public void setUnlimitedMem(EntityModel unlimitedMem) {
        this.unlimitedMem = unlimitedMem;
    }

    public EntityModel getUnlimitedCpu() {
        return unlimitedCpu;
    }

    public void setUnlimitedCpu(EntityModel unlimitedCpu) {
        this.unlimitedCpu = unlimitedCpu;
    }

    public EntityModel getSpecificMem() {
        return specificMem;
    }

    public void setSpecificMem(EntityModel specificMem) {
        this.specificMem = specificMem;
    }

    public EntityModel getSpecificCpu() {
        return specificCpu;
    }

    public void setSpecificCpu(EntityModel specificCpu) {
        this.specificCpu = specificCpu;
    }

    public EntityModel getSpecificMemValue() {
        return specificMemValue;
    }

    public void setSpecificMemValue(EntityModel specificMemValue) {
        this.specificMemValue = specificMemValue;
    }

    public EntityModel getSpecificCpuValue() {
        return specificCpuValue;
    }

    public void setSpecificCpuValue(EntityModel specificCpuValue) {
        this.specificCpuValue = specificCpuValue;
    }

    public EditQuotaClusterModel() {
        setSpecificMem(new EntityModel());
        getSpecificMem().setEntity(true);
        setUnlimitedMem(new EntityModel());
        getUnlimitedMem().setEntity(false);
        setSpecificMemValue(new EntityModel());
        getUnlimitedMem().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) getUnlimitedMem().getEntity()) {
                    getSpecificMem().setEntity(false);
                    getSpecificMemValue().setIsChangable(false);
                }
            }
        });

        getSpecificMem().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) getSpecificMem().getEntity()) {
                    getUnlimitedMem().setEntity(false);
                    getSpecificMemValue().setIsChangable(true);
                }
            }
        });

        setSpecificCpu(new EntityModel());
        setUnlimitedCpu(new EntityModel());
        setSpecificCpuValue(new EntityModel());
        getUnlimitedCpu().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) getUnlimitedCpu().getEntity()) {
                    getSpecificCpu().setEntity(false);
                    getSpecificCpuValue().setIsChangable(false);
                }
            }
        });

        getSpecificCpu().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) getSpecificCpu().getEntity()) {
                    getUnlimitedCpu().setEntity(false);
                    getSpecificCpuValue().setIsChangable(true);
                }
            }
        });
    }

    public boolean Validate() {
        IntegerValidation intValidation = new IntegerValidation();
        intValidation.setMinimum(1);
        getSpecificMemValue().setIsValid(true);
        getSpecificCpuValue().setIsValid(true);
        if ((Boolean) getSpecificMem().getEntity()) {
            getSpecificMemValue().ValidateEntity(new IValidation[] { intValidation, new NotEmptyValidation() });
        }
        if ((Boolean) getSpecificCpu().getEntity()) {
            getSpecificCpuValue().ValidateEntity(new IValidation[] { intValidation, new NotEmptyValidation() });
        }
        return getSpecificMemValue().getIsValid() && getSpecificCpuValue().getIsValid();
    }
}
