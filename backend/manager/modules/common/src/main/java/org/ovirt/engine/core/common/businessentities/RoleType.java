package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

public enum RoleType {

    ADMIN(1),
    USER(2);

    private int id;
    private static final Map<Integer, RoleType> map = new HashMap<>(RoleType.values().length);

    static {
        for (RoleType t : RoleType.values()) {
            map.put(t.id, t);
        }
    }

    private RoleType(Integer val) {
        id = val;
    }

    public int getId() {
        return id;
    }

    public static RoleType getById(Integer id) {
        return map.get(id);
    }

}
