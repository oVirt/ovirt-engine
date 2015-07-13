package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

/**
 * It validates pair of existing pool name (org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel#getName()) and
 * number of VMs added to the pool (org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel#getNumOfDesktop())
 */
public class ExistingPoolNameLengthValidation extends PoolNameLengthValidation {

    public ExistingPoolNameLengthValidation(String poolName, int numOfVmsInPool, int osType) {
        super(poolName, numOfVmsInPool, osType);
    }

    @Override
    protected String getReason() {
        return getQuestionMarksCount() == 0
            ? ConstantsManager.getInstance()
                .getMessages()
                .numOfVmsInPoolInvalid(generateMaxNumOfVms(), getPoolName().length())
            : ConstantsManager.getInstance()
                .getMessages()
                .numOfVmsInPoolInvalidWithQuestionMarks(generateMaxNumOfVmsWithQuestionMark(),
                        getPoolName().length(), getQuestionMarksCount());
    }

    private int generateMaxNumOfVmsWithQuestionMark() {
        return doGenerateMaxNumOfVmsWithQuestionMark(
                getMaxNameLength(), getPoolName().length(), getQuestionMarksCount());
    }

    int doGenerateMaxNumOfVmsWithQuestionMark(int maxNameLength, int poolNameLength, int questionMarkCount) {
        final int numberOfDigits = maxNameLength - (poolNameLength - questionMarkCount);
        return getMaxNumberInNDigits(numberOfDigits);
    }

    private int generateMaxNumOfVms() {
        return doGenerateMaxNumOfVms(getMaxNameLength(), getPoolName().length());
    }

    int doGenerateMaxNumOfVms(int maxNameLength, int poolNameLength) {
        int numberOfDigits = maxNameLength - poolNameLength - 1;
        if (numberOfDigits == 0) {
            return 0;
        }
        return getMaxNumberInNDigits(numberOfDigits);
    }

    private int getMaxNumberInNDigits(int numberOfDigits) {
        return (int) (Math.pow(10, numberOfDigits) - 1);
    }
}
