package org.ovirt.engine.ui.uicommonweb.validation;

import java.util.Arrays;

import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

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
        final int questionMarksCount = getQuestionMarksCount();
        int numOfVmsInPoolLengt = getNumOfVmsInPoolLength();

        // if there are no questionmarks placeholders for vm numbers, vms in pool are named like <pool_name>-<numer>
        int dashLenght = questionMarksCount == 0 ? 1 : 0;
        boolean isOk = poolName.length() - questionMarksCount + numOfVmsInPoolLengt + dashLenght <= getMaxNameLength();

        ValidationResult res = new ValidationResult();
        res.setSuccess(isOk);
        if (!isOk) {
            res.setReasons(Arrays.asList(getReason()));
        }

        return res;
    }

    protected int getQuestionMarksCount() {
        final MatchResult matchResult = RegExp.compile("\\?+").exec(poolName); //$NON-NLS-1$
        if (matchResult == null) {
            return 0;
        }
        return matchResult.getGroup(0).length();
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
        return AsyncDataProvider.getInstance().getMaxVmNameLength();
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
