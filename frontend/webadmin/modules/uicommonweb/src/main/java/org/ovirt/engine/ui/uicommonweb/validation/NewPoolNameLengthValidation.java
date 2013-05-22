package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewPoolNameLengthValidation extends PoolNameLengthValidation {

    public NewPoolNameLengthValidation(String poolName, int numOfVmsInPool, int osType) {
        super(poolName, numOfVmsInPool, osType);
    }

    @Override
    protected String getReason() {
        return ConstantsManager.getInstance()
                .getMessages()
                .poolNameLengthInvalid(generateMaxLength(), getNumOfVmsInPool());
    }

    private int generateMaxLength() {
        return getMaxNameLength() - getNumOfVmsInPoolLength() - 1;
    }

}
