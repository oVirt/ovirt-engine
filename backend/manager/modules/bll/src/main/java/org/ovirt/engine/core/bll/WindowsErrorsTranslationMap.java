package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class WindowsErrorsTranslationMap {
    private static Map<Integer,VdcBllMessages> errorCodes = new HashMap<Integer,VdcBllMessages>();

    static {
        errorCodes.put(1326,VdcBllMessages.USER_FAILED_TO_AUTHENTICATE_WRONG_USERNAME_OR_PASSWORD);
    }

    public static VdcBllMessages getError(int errorCode) {
        return errorCodes.get(errorCode);
    }

}

