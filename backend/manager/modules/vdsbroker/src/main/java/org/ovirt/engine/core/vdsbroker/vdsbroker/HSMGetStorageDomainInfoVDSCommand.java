package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.SANState;
import org.ovirt.engine.core.common.businessentities.StorageBlockSize;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;

public class HSMGetStorageDomainInfoVDSCommand<P extends HSMGetStorageDomainInfoVDSCommandParameters>
        extends VdsBrokerCommand<P> {

    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;

    public HSMGetStorageDomainInfoVDSCommand(P parameters) {
        super(parameters);
    }

    private OneStorageDomainInfoReturn result;

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().getStorageDomainInfo(getParameters().getStorageDomainId().toString());
        proceedProxyReturnValue();
        Pair<StorageDomainStatic, Guid> pairSdStatic = buildStorageStaticFromStruct(result.storageInfo);
        pairSdStatic.getFirst().setId(getParameters().getStorageDomainId());
        setReturnValue(pairSdStatic);
    }

    private Pair<StorageDomainStatic, Guid> buildStorageStaticFromStruct(Map<String, Object> struct) {
        Pair<StorageDomainStatic, Guid> returnValue = new Pair<>();
        StorageDomainStatic sdStatic = new StorageDomainStatic();
        if (struct.containsKey("name")) {
            sdStatic.setStorageName(struct.get("name").toString());
        }
        if (struct.containsKey("type")) {
            sdStatic.setStorageType(EnumUtils.valueOf(StorageType.class, struct.get("type").toString(),
                    true));
        }
        if (struct.containsKey("class")) {
            String domainType = struct.get("class").toString();
            if ("backup".equalsIgnoreCase(domainType)) {
                sdStatic.setStorageDomainType(StorageDomainType.ImportExport);
            } else {
                sdStatic.setStorageDomainType(EnumUtils.valueOf(StorageDomainType.class, domainType, true));
            }
        }
        if (struct.containsKey("version")) {
            sdStatic.setStorageFormat(
                    StorageFormatType.forValue(struct.get("version").toString()));
        }
        if (sdStatic.getStorageType() != StorageType.UNKNOWN) {
            if (sdStatic.getStorageType().isFileDomain() && struct.containsKey("remotePath")) {
                String path = struct.get("remotePath").toString();
                List<StorageServerConnections> connections = storageServerConnectionDao.getAllForStorage(path);
                if (connections.isEmpty()) {
                    sdStatic.setConnection(new StorageServerConnections());
                    sdStatic.getConnection().setConnection(path);
                    sdStatic.getConnection().setStorageType(sdStatic.getStorageType());
                } else {
                    sdStatic.setStorage(connections.get(0).getId());
                    sdStatic.setConnection(connections.get(0));
                }
            } else if (sdStatic.getStorageType() != StorageType.NFS) {
                if (struct.containsKey("vguuid")) {
                    sdStatic.setStorage(struct.get("vguuid").toString());
                }

                if (struct.containsKey("metadataDevice")) {
                    sdStatic.setFirstMetadataDevice(Objects.toString(struct.get("metadataDevice")));
                }

                if (struct.containsKey("vgMetadataDevice")) {
                    sdStatic.setVgMetadataDevice(Objects.toString(struct.get("vgMetadataDevice")));
                }
            }
            if (struct.containsKey("block_size")) {
                sdStatic.setBlockSize(StorageBlockSize.forValue(
                        Integer.parseInt(Objects.toString(struct.get("block_size")))));
            }
        }
        if (struct.containsKey("state")) {
            sdStatic.setSanState(EnumUtils.valueOf(SANState.class, struct.get("state")
                    .toString()
                    .toUpperCase(),
                    false));
        }
        returnValue.setFirst(sdStatic);
        Object[] poolUUIDs = (Object[])struct.get("pool");
        if (poolUUIDs.length != 0) {
            returnValue.setSecond(Guid.createGuidFromString(poolUUIDs[0].toString()));
        }

        return returnValue;
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }
}
