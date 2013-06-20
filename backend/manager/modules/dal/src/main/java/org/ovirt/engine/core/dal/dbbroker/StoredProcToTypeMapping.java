package org.ovirt.engine.core.dal.dbbroker;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class StoredProcToTypeMapping {
    private static Map<String, Class> procToType = new HashMap<>();

    public static Class getTypeFromProcName(String procName) {
        return procToType.get(procName);
    }
}
