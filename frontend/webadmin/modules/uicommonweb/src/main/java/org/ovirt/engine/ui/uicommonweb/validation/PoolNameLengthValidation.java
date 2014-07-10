package org.ovirt.engine.ui.uicommonweb.validation;

import java.util.Arrays;

import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;

public class PoolNameLengthValidation implements IValidation {

    private String poolName;

    private int numOfVmsInPool;

    private int osId;

    public PoolNameLengthValidation(String poolName, int numOfVmsInPool, int osId) {
        this.poolName = poolName;
        this.numOfVmsInPool = numOfVmsInPool;
        this.osId = osId;
    }

    @Override
    public ValidationResult validate(Object value) {
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
        return isWindows() ?
                AsyncDataProvider.getInstance().getMaxVmNameLengthWin() :
                AsyncDataProvider.getInstance().getMaxVmNameLengthNonWin();
    }

    /**
     * convenience method, best used for test cases
     *
     * @return true if this osId is of Windows type
     */
    protected boolean isWindows() {
        return AsyncDataProvider.getInstance().isWindowsOsType(osId);
    }

    protected String getReason() {
        return "";
    }

}
