package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Arrays;
import java.util.Map;

public class Status {

    private static final String CODE = "code";
    private static final String MESSAGE = "message";

    public int code;
    public String message;

    public Status(Map<String, Object> innerMap) {
        code = (Integer) innerMap.get(CODE);
        Object obj = innerMap.get(MESSAGE);
        if (obj == null) {
            message = "";
        } else if (obj instanceof Object[]) {
            message = Arrays.toString((Object[]) obj);
        } else {
            message = obj.toString();
        }
    }

    public Status() {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [code=" + code + ", message=" + message + "]";
    }
}
