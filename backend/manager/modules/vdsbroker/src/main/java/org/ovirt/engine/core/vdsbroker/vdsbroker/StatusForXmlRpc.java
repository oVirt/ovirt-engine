package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Arrays;
import java.util.Map;

public class StatusForXmlRpc {

    private static final String CODE = "code";
    private static final String MESSAGE = "message";

    public int code;
    public String message;

    public StatusForXmlRpc(Map<String, Object> innerMap) {
        code = (Integer) innerMap.get(CODE);
        if (innerMap.get(MESSAGE) instanceof Object[]) {
            message = Arrays.toString((Object[])innerMap.get(MESSAGE));
        }
        else {
            message = innerMap.get(MESSAGE).toString();
        }
    }

    public StatusForXmlRpc() {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [code=" + code + ", message=" + message + "]";
    }
}
