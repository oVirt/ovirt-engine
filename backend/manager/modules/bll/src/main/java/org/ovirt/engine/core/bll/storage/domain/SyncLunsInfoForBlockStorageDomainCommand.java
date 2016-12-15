package org.ovirt.engine.core.bll.storage.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.utils.BlockStorageDiscardFunctionalityHelper;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetVGInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * Synchronize LUN details comprising the storage domain with the DB
 */
@InternalCommandAttribute
@NonTransactiveCommandAttribute(forceCompensation = true)
public class SyncLunsInfoForBlockStorageDomainCommand<T extends StorageDomainParametersBase> extends StorageDomainCommandBase<T> {

    @Inject
    private BlockStorageDiscardFunctionalityHelper discardHelper;

    protected final Consumer<List<LUNs>> updateExistingLuns = luns -> {
        lunDao.updateAll(luns);
        log.info("Updated LUNs information, IDs '{}'.", getLunsIdsList(luns));
    };

    protected final Consumer<List<LUNs>> saveNewLuns = luns -> {
        lunDao.saveAll(luns);
        log.info("New LUNs discovered, IDs '{}'", getLunsIdsList(luns));
    };

    protected final Consumer<List<LUNs>> noOp = luns -> {};

    public SyncLunsInfoForBlockStorageDomainCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setVdsId(parameters.getVdsId());
    }

    public SyncLunsInfoForBlockStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        final List<LUNs> lunsFromVgInfo = (List<LUNs>) runVdsCommand(VDSCommandType.GetVGInfo,
                new GetVGInfoVDSCommandParameters(getVds().getId(), getStorageDomain().getStorage())).getReturnValue();
        final List<LUNs> lunsFromDb = lunDao.getAllForVolumeGroup(getStorageDomain().getStorage());

        Map<Consumer<List<LUNs>>, List<LUNs>> lunsToUpdateInDb = getLunsToUpdateInDb(lunsFromVgInfo, lunsFromDb);
        boolean dbShouldBeUpdated =
                lunsToUpdateInDb.containsKey(updateExistingLuns) || // There are existing luns that should be updated.
                        lunsToUpdateInDb.containsKey(saveNewLuns); // There are new luns that should be saved.
        if (dbShouldBeUpdated) {
            TransactionSupport.executeInNewTransaction(() -> {
                updateLunsInDb(lunsToUpdateInDb);
                refreshLunsConnections(lunsFromVgInfo);
                cleanupLunsFromDb(lunsFromVgInfo, lunsFromDb);
                return null;
            });
        }

        setSucceeded(true);
    }

    protected void refreshLunsConnections(List<LUNs> lunsFromVgInfo) {
        for (LUNs lunFromVgInfo : lunsFromVgInfo) {
            // Update lun connections map
            for (StorageServerConnections connection : lunFromVgInfo.getLunConnections()) {
                StorageServerConnections connectionFromDb =
                        storageServerConnectionDao.getForIqn(connection.getIqn());
                if (connectionFromDb == null) {
                    // Shouldn't happen
                    continue;
                }

                LUNStorageServerConnectionMap lunConnection = new LUNStorageServerConnectionMap(
                        lunFromVgInfo.getLUNId(), connectionFromDb.getId());
                if (storageServerConnectionLunMapDao.get(lunConnection.getId()) == null) {
                    storageServerConnectionLunMapDao.save(lunConnection);
                }
            }
        }
    }

    private void cleanupLunsFromDb(List<LUNs> lunsFromVgInfo, List<LUNs> lunsFromDb) {
        Set<String> lunIdsFromVgInfo = lunsFromVgInfo.stream().map(LUNs::getLUNId).collect(Collectors.toSet());
        List<LUNs> lunsToRemove = lunsFromDb.stream()
                .filter(lun -> !lun.getLUNId().startsWith(BusinessEntitiesDefinitions.DUMMY_LUN_ID_PREFIX))
                .filter(lun -> !lunIdsFromVgInfo.contains(lun.getLUNId()))
                .peek(lun -> log.info("Removing LUN ID '{}'", lun.getLUNId()))
                .collect(Collectors.toList());
        lunDao.removeAllInBatch(lunsToRemove);
    }

    /**
     * Gets a list of up to date luns from vdsm and a list of the existing luns from the db,
     * and returns the luns from vdsm separated into three groups:
     * 1. Luns that should be saved (new luns) to the db.
     * 2. Luns that should be updated in the db.
     * 3. Up to date luns that we should not do anything with.
     * The return value is a map from the consumer of the luns to the luns themselves.
     * The consumer takes the list of luns and saves/updates/does nothing with them.
     */
    protected Map<Consumer<List<LUNs>>, List<LUNs>> getLunsToUpdateInDb(List<LUNs> lunsFromVgInfo, List<LUNs> lunsFromDb) {
        Map<String, LUNs> lunsFromDbMap =
                lunsFromDb.stream().collect(Collectors.toMap(LUNs::getLUNId, Function.identity()));

        return lunsFromVgInfo.stream().collect(Collectors.groupingBy(lunFromVgInfo -> {
            LUNs lunFromDb = lunsFromDbMap.get(lunFromVgInfo.getLUNId());

            if (lunFromDb == null) {
                // One of the following:
                // 1. There's no lun in the db with the same lun id and pv id -> new lun.
                // 2. lunFromDb has the same pv id and a different lun id -> using storage from backup.
                return saveNewLuns;
            }
            boolean lunFromDbHasSamePvId = Objects.equals(
                    lunFromDb.getPhysicalVolumeId(),
                    lunFromVgInfo.getPhysicalVolumeId());
            if (lunFromDbHasSamePvId) {
                // Existing lun, check if it should be updated.
                if (lunFromDb.getDeviceSize() != lunFromVgInfo.getDeviceSize() ||
                        !Objects.equals(lunFromDb.getDiscardMaxSize(), lunFromVgInfo.getDiscardMaxSize()) ||
                        !Objects.equals(lunFromDb.getDiscardZeroesData(), lunFromVgInfo.getDiscardZeroesData())) {
                    return updateExistingLuns;
                }
                // Existing lun is up to date.
                return noOp;
            }
            // lunFromDb has the same lun id and a different pv id -> old pv id.
            return updateExistingLuns;
        }));
    }

    private static String getLunsIdsList(List<LUNs> luns) {
        return luns.stream().map(LUNs::getLUNId).collect(Collectors.joining(", "));
    }

    /**
     * Saves the new or updates the existing luns in the DB.
     */
    protected void updateLunsInDb(Map<Consumer<List<LUNs>>, List<LUNs>> lunsToUpdateInDbMap) {
        lunsToUpdateInDbMap.entrySet().forEach(entry -> entry.getKey().accept(entry.getValue()));

        if (lunsToUpdateInDbMap.containsKey(saveNewLuns) || lunsToUpdateInDbMap.containsKey(updateExistingLuns)) {
            Collection<LUNs> lunsToUpdateInDb = lunsToUpdateInDbMap.entrySet().stream()
                    .filter(entry -> entry.getKey().equals(saveNewLuns) ||
                            entry.getKey().equals(updateExistingLuns))
                    .map(Map.Entry::getValue)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            discardHelper.logIfLunsBreakStorageDomainDiscardFunctionality(lunsToUpdateInDb, getStorageDomainId());
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getStorageDomainId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.SYNC_LUNS,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }
}
