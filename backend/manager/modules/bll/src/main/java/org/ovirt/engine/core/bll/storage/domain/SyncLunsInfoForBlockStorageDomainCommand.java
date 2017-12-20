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
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.SyncLunsInfoForBlockStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetVGInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * Synchronize LUN details comprising the storage domain with the DB
 */
@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class SyncLunsInfoForBlockStorageDomainCommand<T extends SyncLunsInfoForBlockStorageDomainParameters>
        extends StorageDomainCommandBase<T> {

    @Inject
    private BlockStorageDiscardFunctionalityHelper discardHelper;
    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;
    @Inject
    private StorageServerConnectionLunMapDao storageServerConnectionLunMapDao;
    @Inject
    private LunDao lunDao;
    @Inject
    private BlockStorageDomainHelper blockStorageDomainHelper;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;

    public SyncLunsInfoForBlockStorageDomainCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setVdsId(parameters.getVdsId());
        setStorageDomainId(parameters.getStorageDomainId());
    }

    @Override
    protected boolean validate() {
        HostValidator hostValidator = getHostValidator();
        return validate(hostValidator.hostExists()) &&
                validate(hostValidator.isUp()) &&
                checkStorageDomain();
    }

    protected HostValidator getHostValidator() {
        return HostValidator.createInstance(getVds());
    }

    @Override
    protected void executeCommand() {
        final List<LUNs> lunsFromVgInfo = getVgInfo();
        final List<LUNs> lunsFromDb = lunDao.getAllForVolumeGroup(getStorageDomain().getStorage());

        Map<LunHandler, List<LUNs>> lunsToUpdateInDb = getLunsToUpdateInDb(lunsFromVgInfo, lunsFromDb);
        boolean dbShouldBeUpdated = lunsToUpdateInDb.keySet().stream().anyMatch(LunHandler::requiresDbUpdate);
        if (dbShouldBeUpdated) {
            TransactionSupport.executeInNewTransaction(() -> {
                updateLunsInDb(lunsToUpdateInDb);
                refreshLunsConnections(lunsFromVgInfo);
                return null;
            });
        }

        refreshMetadataDevicesInfoIfNeeded();
        setSucceeded(true);
    }

    @SuppressWarnings("unchecked")
    private List<LUNs> getVgInfo() {
        if (getParameters().getVgInfo() != null) {
            log.debug("Using the virtual group's information from the command's parameters rather "
                    + "than calling getVgInfo and getting the information directly from vdsm.");
            return getParameters().getVgInfo();
        }
        GetVGInfoVDSCommandParameters params = new GetVGInfoVDSCommandParameters(getParameters().getVdsId(),
                getStorageDomain().getStorage());
        return (List<LUNs>) runVdsCommand(VDSCommandType.GetVGInfo, params).getReturnValue();
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

    private void refreshMetadataDevicesInfoIfNeeded() {
        // When a domain is created it has a vg metadata device and a metadata lv which may be created on multiple
        // devices. On a regular basis, those devices should be never changing.
        // However, in some user environments in case of disaster the block sd may be restored manually in a way that
        // will change its metadata devices - therefore when syncing the luns info we refresh the metadata devices
        // information as well.

        String oldVgMetadataDevice = getStorageDomain().getVgMetadataDevice();
        String oldFirstMetadataDevice = getStorageDomain().getFirstMetadataDevice();
        blockStorageDomainHelper.fillMetadataDevicesInfo(getStorageDomain().getStorageStaticData(),
                getParameters().getVdsId());
        if (!Objects.equals(oldVgMetadataDevice, getStorageDomain().getVgMetadataDevice()) ||
                !Objects.equals(oldFirstMetadataDevice, getStorageDomain().getFirstMetadataDevice())) {
            storageDomainStaticDao.update(getStorageDomain().getStorageStaticData());
        }

        blockStorageDomainHelper.checkDomainMetadataDevices(getStorageDomain());
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
    protected Map<LunHandler, List<LUNs>> getLunsToUpdateInDb(List<LUNs> lunsFromVgInfo, List<LUNs> lunsFromDb) {
        Map<String, LUNs> lunsFromDbMap =
                lunsFromDb.stream().collect(Collectors.toMap(LUNs::getLUNId, Function.identity()));

        Map<LunHandler, List<LUNs>> lunsToUpdateInDb =
                lunsFromVgInfo.stream().collect(Collectors.groupingBy(lunFromVgInfo -> {
                    LUNs lunFromDb = lunsFromDbMap.get(lunFromVgInfo.getLUNId());

                    if (lunFromDb == null) {
                        // One of the following:
                        // 1. There's no lun in the db with the same lun id and pv id -> new lun.
                        // 2. lunFromDb has the same pv id and a different lun id -> using storage from backup.
                        return saveLunsHandler;
                    }
                    boolean lunFromDbHasSamePvId = Objects.equals(
                            lunFromDb.getPhysicalVolumeId(),
                            lunFromVgInfo.getPhysicalVolumeId());
                    if (lunFromDbHasSamePvId) {
                        // Existing lun, check if it should be updated.
                        if (lunFromDb.getDeviceSize() != lunFromVgInfo.getDeviceSize() ||
                                !Objects.equals(lunFromDb.getDiscardMaxSize(), lunFromVgInfo.getDiscardMaxSize())) {
                            return updateLunsHandler;
                        }
                        // Existing lun is up to date.
                        return noOp;
                    }
                    // lunFromDb has the same lun id and a different pv id -> old pv id.
                    return updateLunsHandler;
                }));
        List<LUNs> lunsToRemoveFromDb = getLunsToRemoveFromDb(lunsFromVgInfo, lunsFromDb);
        if (!lunsToRemoveFromDb.isEmpty()) {
            lunsToUpdateInDb.put(removeLunsHandler, lunsToRemoveFromDb);
        }
        return lunsToUpdateInDb;
    }

    protected List<LUNs> getLunsToRemoveFromDb(List<LUNs> lunsFromVgInfo, List<LUNs> lunsFromDb) {
        Set<String> lunIdsFromVgInfo = lunsFromVgInfo.stream().map(LUNs::getLUNId).collect(Collectors.toSet());
        return lunsFromDb.stream()
                .filter(lun -> !lun.getLUNId().startsWith(BusinessEntitiesDefinitions.DUMMY_LUN_ID_PREFIX))
                .filter(lun -> !lunIdsFromVgInfo.contains(lun.getLUNId()))
                .collect(Collectors.toList());
    }

    private static String getLunsIdsList(List<LUNs> luns) {
        return luns.stream().map(LUNs::getLUNId).collect(Collectors.joining(", "));
    }

    /**
     * Saves the new or updates the existing luns in the DB.
     */
    private void updateLunsInDb(Map<LunHandler, List<LUNs>> lunsToUpdateInDbMap) {
        lunsToUpdateInDbMap.entrySet().forEach(entry -> entry.getKey().accept(entry.getValue()));

        if (lunsToUpdateInDbMap.keySet().stream().anyMatch(LunHandler::affectsDiscardFunctionality)) {
            Collection<LUNs> lunsToUpdateInDb = lunsToUpdateInDbMap.entrySet().stream()
                    .filter(entry -> entry.getKey().affectsDiscardFunctionality())
                    .map(Map.Entry::getValue)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            discardHelper.logIfLunsBreakStorageDomainDiscardFunctionality(lunsToUpdateInDb, getStorageDomain());
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getStorageDomainId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    protected final LunHandler updateLunsHandler = new LunHandler() {

        @Override
        public void accept(List<LUNs> luns) {
            lunDao.updateAllInBatch(luns);
            log.info("Updated LUNs information, IDs '{}'.", getLunsIdsList(luns));
        }

        @Override
        public boolean requiresDbUpdate() {
            return true;
        }

        @Override
        public boolean affectsDiscardFunctionality() {
            return true;
        }
    };

    protected final LunHandler saveLunsHandler = new LunHandler() {

        @Override
        public void accept(List<LUNs> luns) {
            lunDao.saveAllInBatch(luns);
            log.info("New LUNs discovered, IDs '{}'", getLunsIdsList(luns));
        }

        @Override
        public boolean requiresDbUpdate() {
            return true;
        }

        @Override
        public boolean affectsDiscardFunctionality() {
            return true;
        }
    };

    protected final LunHandler removeLunsHandler = new LunHandler() {

        @Override
        public void accept(List<LUNs> luns) {
            lunDao.removeAllInBatch(luns);
            log.info("Removed LUNs, IDs '{}'", getLunsIdsList(luns));
        }

        @Override
        public boolean requiresDbUpdate() {
            return true;
        }
    };

    protected final LunHandler noOp = luns -> {};

    protected interface LunHandler extends Consumer<List<LUNs>> {

        /**
         * Indicates whether a lun should be updated in the db.
         */
        default boolean requiresDbUpdate() {
            return false;
        }

        /**
         * Indicates whether updating the lun can affect
         * its storage domain's discard functionality.
         */
        default boolean affectsDiscardFunctionality() {
            return false;
        }
    }
}
