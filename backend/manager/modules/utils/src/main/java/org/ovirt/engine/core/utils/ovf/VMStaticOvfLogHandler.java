package org.ovirt.engine.core.utils.ovf;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmStatic;

public class VMStaticOvfLogHandler extends OvfLogEventHandler<VmStatic> {

    private static Map<String, TypeConverter> typeConvertersMap = new HashMap<>();

    static {
        typeConvertersMap.put("migrationSupport", new MigrationSupportConverter());
    }

    public VMStaticOvfLogHandler(VmStatic entity) {
        super(entity);
    }

    @Override
    protected Map<String, TypeConverter> getTypeConvertersMap() {
        return typeConvertersMap;
    }

}
