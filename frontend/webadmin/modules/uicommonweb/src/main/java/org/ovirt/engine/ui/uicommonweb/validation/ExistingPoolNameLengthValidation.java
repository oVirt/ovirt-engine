package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ExistingPoolNameLengthValidation extends PoolNameLengthValidation {

    public ExistingPoolNameLengthValidation(String poolName, int numOfVmsInPool, int osType) {
        super(poolName, numOfVmsInPool, osType);
    }

    @Override
    protected String getReason() {
        return ConstantsManager.getInstance()
                .getMessages()
                .numOfVmsInPoolInvalod(generateMaxLength(), getPoolName().length());
    }

    private int generateMaxLength() {
        return doGenerateMaxLength(getMaxNameLength(), getPoolName().length());
    }

    int doGenerateMaxLength(int maxNameLengt, int poolNameLength) {
        int allowedLength = maxNameLengt - poolNameLength - 1;
        StringBuilder sb = new StringBuilder("");

        if (allowedLength == 0) {
            return 0;
        }

        for (int i = 0; i < allowedLength; i++) {
            sb.append("9"); //$NON-NLS-1$
        }

        return Integer.parseInt(sb.toString());
    }
}
