package org.ovirt.engine.core.vdsbroker.monitoring;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.LunDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LunDisksMonitoring {

    @Inject
    private DiskDao diskDao;
    @Inject
    private LunDao lunDao;

    private static final Logger log = LoggerFactory.getLogger(LunDisksMonitoring.class);

    void process(Map<Guid, Map<String, LUNs>> vmIdToLunsMap) {
        if (vmIdToLunsMap.isEmpty()) {
            return;
        }
        List<LUNs> lunsToSave = vmIdToLunsMap.entrySet().stream()
                .map(entry -> getVmLunDisksToSave(entry.getKey(), entry.getValue()))
                .flatMap(List::stream)
                .collect(toList());
        saveVmLunDisks(lunsToSave);
    }

    List<LUNs> getVmLunDisksToSave(Guid vmId, Map<String, LUNs> lunsMap) {
        if (lunsMap.isEmpty()) {
            // LUNs list from getVmStats hasn't been updated yet or VDSM doesn't support LUNs list retrieval.
            return Collections.emptyList();
        }

        List<LUNs> vmLunDisksToSave = new ArrayList<>();
        getVmPluggedLunsFromDb(vmId).forEach(lunFromDB -> {
            LUNs lunFromMap = lunsMap.get(lunFromDB.getId());
            // LUN's device size might be returned as zero in case of an error in VDSM;
            // Hence, verify before updating.
            if (lunFromMap.getDeviceSize() != 0 && lunFromMap.getDeviceSize() != lunFromDB.getDeviceSize()) {
                // Found a mismatch - set LUN for update
                log.info("Updated LUN device size - ID: '{}', previous size: '{}', new size: '{}'.",
                        lunFromDB.getLUNId(), lunFromDB.getDeviceSize(), lunFromMap.getDeviceSize());

                lunFromDB.setDeviceSize(lunFromMap.getDeviceSize());
                vmLunDisksToSave.add(lunFromDB);
            }
        });
        return vmLunDisksToSave;
    }

    List<LUNs> getVmPluggedLunsFromDb(Guid vmId) {
        return diskDao.getAllForVm(vmId, true).stream()
                .filter(disk -> disk.getDiskStorageType() == DiskStorageType.LUN)
                .map(disk -> ((LunDisk) disk).getLun())
                .collect(Collectors.toList());
    }

    void saveVmLunDisks(List<LUNs> luns) {
        lunDao.updateAllInBatch(luns);
    }
}
