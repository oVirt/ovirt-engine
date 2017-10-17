package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;

/**
 * Class takes list of preexisting db records and list of records from user request, calculate difference, and update
 * MacPool, so that correct number of MACs are registered in MAC Pool. There might arise need to reallocate MAC of
 * some NIC from user request; in that case this class have side effect and updates passed {@link VmNetworkInterface}
 * from user request.
 */
public class SyncMacsOfDbNicsWithSnapshot {
    private final MacPool macPool;
    private final AuditLogDirector auditLogDirector;
    private final boolean macsInSnapshotAreExpectedToBeAlreadyAllocated;

    public SyncMacsOfDbNicsWithSnapshot(MacPool macPool,
            AuditLogDirector auditLogDirector,
            boolean macsInSnapshotAreExpectedToBeAlreadyAllocated) {
        this.macPool = macPool;
        this.auditLogDirector = auditLogDirector;
        this.macsInSnapshotAreExpectedToBeAlreadyAllocated = macsInSnapshotAreExpectedToBeAlreadyAllocated;
    }

    private Stream<String> vmNicsToMacAddresses(List<? extends NetworkInterface> nics) {
        return nics.stream().map(NetworkInterface::getMacAddress).filter(Objects::nonNull);
    }

    public boolean canSyncNics(List<? extends NetworkInterface> currentDbNics,
           List<? extends NetworkInterface> snapshotedNics) {
        CountMacUsageDifference macUsageDifference = new CountMacUsageDifference(vmNicsToMacAddresses(currentDbNics),
                vmNicsToMacAddresses(snapshotedNics),
                macsInSnapshotAreExpectedToBeAlreadyAllocated);

        List<String> macsToBeAdded = macUsageDifference.getMissingMacs();

        if (macsToBeAdded.isEmpty()) {
            return true;
        }

        List<String> macsToBeRemoved = macUsageDifference.getExtraMacs();
        int numOfMacsToAdd = macsToBeAdded.size() - macsToBeRemoved.size();
        return numOfMacsToAdd > 0 ? macPool.canAllocateMacAddresses(numOfMacsToAdd) : true;
    }

    public void sync(List<? extends NetworkInterface> currentDbNics, List<? extends NetworkInterface> snapshotedNics) {
        CountMacUsageDifference macUsageDifference = new CountMacUsageDifference(vmNicsToMacAddresses(currentDbNics),
                vmNicsToMacAddresses(snapshotedNics),
                macsInSnapshotAreExpectedToBeAlreadyAllocated);

        //release extra macs.
        macPool.freeMacs(macUsageDifference.getExtraMacs());

        /*
         * This will calculate, which macs are missing in {@link MacPool} and adds them into it. Some macs might fail to
         * be added because of duplicity. These macs will be reallocated and respective {@link VmNetworkInterface} will
         * be updated.
         */
        List<String> macsFailedToBeAdded = macPool.addMacs(macUsageDifference.getMissingMacs());
        reallocateMacsWhichCouldntBeAddedToMacPool(macsFailedToBeAdded, snapshotedNics);
    }

    private void reallocateMacsWhichCouldntBeAddedToMacPool(List<String> macsFailedToBeAdded,
            List<? extends NetworkInterface> snapshotedNics) {

        if (macsFailedToBeAdded.isEmpty()) {
            return;
        }

        //not to ruin original for error reporting.
        List<String> macsFailedToBeAddedCopy = new ArrayList<>(macsFailedToBeAdded);
        try {
            List<Pair<String, String>> macReplacements = new ArrayList<>();
            for (NetworkInterface vmInterface : snapshotedNics) {
                String originalMacAddress = vmInterface.getMacAddress();
                if (macsFailedToBeAddedCopy.contains(originalMacAddress)) {
                    macsFailedToBeAddedCopy.remove(originalMacAddress);
                    String replacingMac = macPool.allocateNewMac();
                    vmInterface.setMacAddress(replacingMac);
                    macReplacements.add(new Pair<>(originalMacAddress, replacingMac));
                }
            }

            auditLogPerformedReplacements(macReplacements);
        } catch (EngineException ex) {
            if (EngineError.MAC_POOL_NO_MACS_LEFT.equals(ex.getErrorCode())) {
                auditLogImpossibilityToReplaceDuplicateMacs(macsFailedToBeAdded);
            }
            throw ex;
        }
    }

    private void auditLogPerformedReplacements(List<Pair<String, String>> macReplacements) {
        AuditLogable event = new AuditLogableImpl();
        List<String> replacementsString = macReplacements.stream()
                .map(e -> e.getFirst() + "->" + e.getSecond())
                .collect(Collectors.toList());
        String auditLogMessage = "Following MACs had to be reallocated: " + replacementsString;
        auditLogDirector.log(event, AuditLogType.MAC_ADDRESS_HAD_TO_BE_REALLOCATED, auditLogMessage);
    }

    private void auditLogImpossibilityToReplaceDuplicateMacs(List<String> macs) {
        AuditLogable event = new AuditLogableImpl();
        String auditLogMessage = "Following MACs had to be reallocated, but we was unable to replace them because of insufficient amount of free macs: " + macs;
        auditLogDirector.log(event, AuditLogType.MAC_ADDRESS_COULDNT_BE_REALLOCATED, auditLogMessage);
    }
}
