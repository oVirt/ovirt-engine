package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;


public class CpuQosParametersModel extends QosParametersModel<CpuQos> {
    private EntityModel<Integer> cpuLimit;

    public CpuQosParametersModel() {
        setCpuLimit(new EntityModel<Integer>());
    }

    @Override
    public void init(CpuQos qos) {
        getCpuLimit().setEntity(qos.getCpuLimit());
    }

    @Override
    public void flush(CpuQos cpuQos) {
        cpuQos.setCpuLimit(getCpuLimit().getEntity());
    }

    @Override
    public boolean validate() {
        if (!getIsAvailable()) {
            return true;
        }

        getCpuLimit().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new IntegerValidation(0, (Integer) AsyncDataProvider.getInstance()
                        .getConfigValuePreConverted(ConfigValues.MaxCpuLimitQosValue)) });

        setIsValid(getCpuLimit().getIsValid());
        return getIsValid();
    }

    public EntityModel<Integer> getCpuLimit() {
        return cpuLimit;
    }

    public void setCpuLimit(EntityModel<Integer> cpuLimit) {
        this.cpuLimit = cpuLimit;
    }
}
