package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

public class StatusForXmlRpc {

    private static final String CODE = "code";
    private static final String MESSAGE = "message";

    public int mCode;
    public String mMessage;

    public StatusForXmlRpc(Map<String, Object> innerMap) {
        mCode = (Integer) innerMap.get(CODE);
        mMessage = (String) innerMap.get(MESSAGE);
    }

    public StatusForXmlRpc() {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [mCode=" + mCode + ", mMessage=" + mMessage + "]";
    }
}
