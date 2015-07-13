package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewPoolNameLengthValidation extends PoolNameLengthValidation {

    public NewPoolNameLengthValidation(String poolName, int numOfVmsInPool, int osType) {
        super(poolName, numOfVmsInPool, osType);
    }

    @Override
    protected String getReason() {
        return getQuestionMarksCount() == 0
                ? ConstantsManager.getInstance()
                    .getMessages()
                    .poolNameLengthInvalid(generateMaxLengthNoQuestionMarks(), getNumOfVmsInPool())
                : ConstantsManager.getInstance()
                    .getMessages()
                    .poolNameWithQuestionMarksLengthInvalid(generateMaxLengthQuestionMarksPresent(),
                            getNumOfVmsInPool(),
                            getQuestionMarksCount());
    }

    private int generateMaxLengthNoQuestionMarks() {
        return getMaxNameLength() - getNumOfVmsInPoolLength() - 1;
    }

    private int generateMaxLengthQuestionMarksPresent() {
        return getMaxNameLength() - Math.max(getNumOfVmsInPoolLength() - getQuestionMarksCount(), 0);
    }

}
