package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public final class FileStatsReturn extends StatusReturn {

    private Map<String, Map<String, Object>> fileStats;

    @SuppressWarnings("unchecked")
    public FileStatsReturn(Map<String, Object> innerMap) {
        super(innerMap);
        // New VDSM returns a map with file names as key and file stats as value.
        // The map contains all files even those without proper permissions.
        fileStats = (Map<String, Map<String, Object>>) innerMap.get(VdsProperties.file_stats);
        if (fileStats != null) {
            removeFilesWithoutPermissions();
        } else {
            fileStats = new HashMap<>();
            // Old VDSM returns only a list of file names but files without proper
            // permissions are already filtered by VDSM.
            Object[] fileNames = (Object[]) innerMap.get(VdsProperties.iso_list);
            if (fileNames != null) {
                // Since returned value is a list we have to bridge a gap and translate it into a map.
                // Caller should not worry about returned type.
                createDefaultFileStats(fileNames);
            }
        }
    }

    private void removeFilesWithoutPermissions() {
        List<String> filesWithoutPermissions = getFilesWithoutPermissions();
        for (String file : filesWithoutPermissions) {
            fileStats.remove(file);
        }
    }

    private List<String> getFilesWithoutPermissions() {
        List<String> filesWithoutPermissions = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> fileEntry : fileStats.entrySet()) {
            if (!isPermittedFile(fileEntry)) {
                filesWithoutPermissions.add(fileEntry.getKey());
            }
        }
        return filesWithoutPermissions;
    }

    private boolean isPermittedFile(Map.Entry<String, Map<String, Object>> fileEntry) {
        return (Integer) fileEntry.getValue().get(VdsProperties.status) == 0;
    }

    private void createDefaultFileStats(Object[] fileNames) {
        for (Object fileName : fileNames) {
            fileStats.put((String) fileName, Collections.emptyMap());
        }
    }

    public Map<String, Map<String, Object>> getFileStats() {
        return fileStats;
    }
}
