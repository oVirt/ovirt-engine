package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Arrays;
import java.util.Map;

public class StatusForXmlRpc {

    private static final String CODE = "code";
    private static final String MESSAGE = "message";

    public int mCode;
    public String mMessage;

    public StatusForXmlRpc(Map<String, Object> innerMap) {
        mCode = (Integer) innerMap.get(CODE);
        if (innerMap.get(MESSAGE) instanceof Object[]) {
            mMessage = Arrays.toString((Object[])innerMap.get(MESSAGE));
        }
        else {
            mMessage = innerMap.get(MESSAGE).toString();
        }
    }

    public StatusForXmlRpc() {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [mCode=" + mCode + ", mMessage=" + mMessage + "]";
    }
}
