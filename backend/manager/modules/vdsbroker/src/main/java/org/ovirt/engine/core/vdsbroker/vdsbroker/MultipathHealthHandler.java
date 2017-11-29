package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.utils.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MultipathHealthHandler {

    private static final Logger log = LoggerFactory.getLogger(MultipathHealthHandler.class);
    @Inject
    private AuditLogDirector auditLogDirector;

    private Map<Guid, byte[]> multipathHealthHash = new HashMap<>();

    public void handleMultipathHealthReport(VDS vds, Map<String, Object> statsMap) {

        if (!statsMap.containsKey(VdsProperties.MULTIPATH_HEALTH)) {
            return;
        }
        Map<String, Object> multipathHealthMap = (Map<String, Object>)
                statsMap.get(VdsProperties.MULTIPATH_HEALTH);

        // The following mechanism avoids to have the same events generated
        // when there was no changes in the health report.
        // Note that the EventFloodRegulator is solving the problem of not
        // emitting the same event reoccuring in the time frame of AuditLogTimeInterval.
        // In this case, using the EventFloodRegulator would filter events that are needed.
        // For example, having an event of NO_FAULTY_MULTIPATHS_ON_HOST, then
        // an event of FAULTY_MULTIPATHS_ON_HOST, and again NO_FAULTY_MULTIPATHS_ON_HOST.
        // The last event would have been filtered out.
        byte[] hash;
        String json;
        try {
            json = JsonHelper.mapToJson(new TreeMap<>(multipathHealthMap));
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            digest.update(json.getBytes("UTF-8"));
            hash = digest.digest();
        } catch (Exception e) {
            log.error("failed building multipath events: {}", e.getMessage());
            log.debug("Exception", e);
            return;
        }
        byte[] previousHash = multipathHealthHash.get(vds.getId());
        if (Arrays.equals(hash, previousHash)) {
            // No changes in the report
            return;
        }
        multipathHealthHash.put(vds.getId(), hash);
        log.debug("Multipath health report for host {}: {}", vds.getName(), json);

        if (multipathHealthMap.isEmpty()) {
            AuditLogable logable = createAuditLogableForHost(vds);
            auditLogDirector.log(logable, AuditLogType.NO_FAULTY_MULTIPATHS_ON_HOST);
            return;
        }

        Map<Boolean, List<String>> multipathHealthMapPartition =
                multipathHealthMap.entrySet()
                        .stream()
                        .collect(Collectors.partitioningBy(entry -> {
                                    Map<String, Object> internalValue =
                                            (Map<String, Object>) entry.getValue();
                                    return (Integer) internalValue.get(VdsProperties.MULTIPATH_VALID_PATHS) > 0;
                                },
                                Collectors.mapping(Map.Entry::getKey, Collectors.toList())));

        createAuditLog(multipathHealthMapPartition.get(Boolean.FALSE),
                AuditLogType.MULTIPATH_DEVICES_WITHOUT_VALID_PATHS_ON_HOST,
                vds);
        createAuditLog(multipathHealthMapPartition.get(Boolean.TRUE),
                AuditLogType.FAULTY_MULTIPATHS_ON_HOST,
                vds);
    }

    private void createAuditLog(List<String> guids, AuditLogType type, VDS vds) {
        if (guids.isEmpty()) {
            return;
        }
        AuditLogable logable = createAuditLogableForHost(vds);
        logable.addCustomValue("MpathGuids", String.join(", ", guids));
        auditLogDirector.log(logable, type);
    }

    private AuditLogable createAuditLogableForHost(VDS vds) {
        AuditLogable logable = new AuditLogableImpl();
        logable.setVdsId(vds.getId());
        logable.setVdsName(vds.getName());
        return logable;
    }

}
