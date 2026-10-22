package org.ovirt.engine.core.bll.exportimport;

import java.util.LinkedHashMap;
import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

/**
 * Helpers shared by the OVA import commands (VM and template).
 */
public final class OvaImportUtils {

    private OvaImportUtils() {
    }

    /**
     * Converts the disk id to OVA tar member name mapping of the import parameters into the form passed to the
     * Convert/Extract OVA child commands.
     */
    public static Map<Guid, String> buildDiskIdToTarName(Map<Guid, Guid> tarNameByDiskId) {
        if (tarNameByDiskId == null || tarNameByDiskId.isEmpty()) {
            return null;
        }
        Map<Guid, String> result = new LinkedHashMap<>();
        for (Map.Entry<Guid, Guid> entry : tarNameByDiskId.entrySet()) {
            if (entry.getValue() != null) {
                result.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return result.isEmpty() ? null : result;
    }
}
