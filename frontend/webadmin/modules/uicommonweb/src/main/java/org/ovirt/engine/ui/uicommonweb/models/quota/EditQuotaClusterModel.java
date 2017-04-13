package org.ovirt.engine.ui.uicommonweb.models.quota;

import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LongValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public class EditQuotaClusterModel extends EntityModel<QuotaCluster> {
    EntityModel<Boolean> unlimitedMem;
    EntityModel<Boolean> unlimitedCpu;

    EntityModel<Boolean> specificMem;
    EntityModel<Boolean> specificCpu;

    EntityModel<Long> specificMemValue;
    EntityModel<Integer> specificCpuValue;

    public EntityModel<Boolean> getUnlimitedMem() {
        return unlimitedMem;
    }

    public void setUnlimitedMem(EntityModel<Boolean> unlimitedMem) {
        this.unlimitedMem = unlimitedMem;
    }

    public EntityModel<Boolean> getUnlimitedCpu() {
        return unlimitedCpu;
    }

    public void setUnlimitedCpu(EntityModel<Boolean> unlimitedCpu) {
        this.unlimitedCpu = unlimitedCpu;
    }

    public EntityModel<Boolean> getSpecificMem() {
        return specificMem;
    }

    public void setSpecificMem(EntityModel<Boolean> specificMem) {
        this.specificMem = specificMem;
    }

    public EntityModel<Boolean> getSpecificCpu() {
        return specificCpu;
    }

    public void setSpecificCpu(EntityModel<Boolean> specificCpu) {
        this.specificCpu = specificCpu;
    }

    public EntityModel<Long> getSpecificMemValue() {
        return specificMemValue;
    }

    public void setSpecificMemValue(EntityModel<Long> specificMemValue) {
        this.specificMemValue = specificMemValue;
    }

    public EntityModel<Integer> getSpecificCpuValue() {
        return specificCpuValue;
    }

    public void setSpecificCpuValue(EntityModel<Integer> specificCpuValue) {
        this.specificCpuValue = specificCpuValue;
    }

    public EditQuotaClusterModel() {
        setSpecificMem(new EntityModel<Boolean>());
        getSpecificMem().setEntity(true);
        setUnlimitedMem(new EntityModel<Boolean>());
        getUnlimitedMem().setEntity(false);
        setSpecificMemValue(new EntityModel<Long>());
        getUnlimitedMem().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getUnlimitedMem().getEntity()) {
                getSpecificMem().setEntity(false);
                getSpecificMemValue().setIsChangeable(false);
            }
        });

        getSpecificMem().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getSpecificMem().getEntity()) {
                getUnlimitedMem().setEntity(false);
                getSpecificMemValue().setIsChangeable(true);
            }
        });

        setSpecificCpu(new EntityModel<Boolean>());
        setUnlimitedCpu(new EntityModel<Boolean>());
        setSpecificCpuValue(new EntityModel<Integer>());
        getUnlimitedCpu().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getUnlimitedCpu().getEntity()) {
                getSpecificCpu().setEntity(false);
                getSpecificCpuValue().setIsChangeable(false);
            }
        });

        getSpecificCpu().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getSpecificCpu().getEntity()) {
                getUnlimitedCpu().setEntity(false);
                getSpecificCpuValue().setIsChangeable(true);
            }
        });
    }

    public boolean validate() {
        IntegerValidation intValidation = new IntegerValidation();
        intValidation.setMinimum(1);
        LongValidation longValidation = new LongValidation();
        longValidation.setMinimum(1);
        getSpecificMemValue().setIsValid(true);
        getSpecificCpuValue().setIsValid(true);
        if (getSpecificMem().getEntity()) {
            getSpecificMemValue().validateEntity(new IValidation[] { longValidation, new NotEmptyValidation() });
        }
        if (getSpecificCpu().getEntity()) {
            getSpecificCpuValue().validateEntity(new IValidation[] { intValidation, new NotEmptyValidation() });
        }
        return getSpecificMemValue().getIsValid() && getSpecificCpuValue().getIsValid();
    }
}
