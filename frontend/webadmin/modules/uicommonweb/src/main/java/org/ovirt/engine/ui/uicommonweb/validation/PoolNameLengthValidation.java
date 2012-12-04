package org.ovirt.engine.ui.uicommonweb.validation;

import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class PoolNameLengthValidation implements IValidation {

    private String poolName;

    private int numOfVmsInPool;

    private VmOsType osType;

    public PoolNameLengthValidation(String poolName, int numOfVmsInPool, VmOsType osType) {
        this.poolName = poolName;
        this.numOfVmsInPool = numOfVmsInPool;
        this.osType = osType;
    }

    @Override
    public ValidationResult Validate(Object value) {
        int numOfVmsInPoolLengt = getNumOfVmsInPoolLength();

        // the +1 is the '-' sign between the name of pool and the ID of the VM
        boolean isOk = poolName.length() + numOfVmsInPoolLengt + 1 <= getMaxNameLength();

        ValidationResult res = new ValidationResult();
        res.setSuccess(isOk);
        if (!isOk) {
            res.setReasons(Arrays.asList(getReason()));
        }

        return res;
    }

    protected int getNumOfVmsInPoolLength() {
        return Integer.toString(numOfVmsInPool).length();
    }

    protected String getPoolName() {
        return poolName;
    }

    protected int getNumOfVmsInPool() {
        return numOfVmsInPool;
    }

    protected int getMaxNameLength() {
        return AsyncDataProvider.IsWindowsOsType(osType) ?
                UnitVmModel.WINDOWS_VM_NAME_MAX_LIMIT :
                UnitVmModel.NON_WINDOWS_VM_NAME_MAX_LIMIT;
    }

    protected String getReason() {
        return "";
    }

}
