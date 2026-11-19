package org.ovirt.engine.core.bll.exportimport;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;

/**
 * Helpers shared by the OVA import commands (VM and template).
 */
public final class OvaImportUtils {

    private OvaImportUtils() {
    }

    /**
     * Pairs the ids of the disks created by the import with the OVF image ids (the OVA tar member names), by
     * position. Fails fast when the lists do not line up, instead of letting the Convert/Extract OVA child
     * command fail later with a generic error.
     */
    public static Map<Guid, Guid> mapTarNamesByDiskId(List<Guid> diskIds, List<Guid> ovfTarImageIds) {
        if (diskIds == null || diskIds.size() != ovfTarImageIds.size()) {
            throw new EngineException(EngineError.GeneralException,
                    String.format("OVA import: number of created disks (%d) does not match number of OVF disk"
                                    + " images (%d)",
                            diskIds == null ? 0 : diskIds.size(),
                            ovfTarImageIds.size()));
        }
        Map<Guid, Guid> tarNameByDiskId = new LinkedHashMap<>();
        for (int i = 0; i < diskIds.size(); i++) {
            Guid diskId = diskIds.get(i);
            Guid tarName = ovfTarImageIds.get(i);
            if (diskId == null || tarName == null) {
                throw new EngineException(EngineError.GeneralException,
                        String.format("OVA import: missing %s for disk #%d",
                                diskId == null ? "created disk id" : "OVF image id",
                                i));
            }
            tarNameByDiskId.put(diskId, tarName);
        }
        return tarNameByDiskId;
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
