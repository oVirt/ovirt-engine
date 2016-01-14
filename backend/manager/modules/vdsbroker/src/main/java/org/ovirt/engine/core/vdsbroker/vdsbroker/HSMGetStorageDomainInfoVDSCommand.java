package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.SANState;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class HSMGetStorageDomainInfoVDSCommand<P extends HSMGetStorageDomainInfoVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    public HSMGetStorageDomainInfoVDSCommand(P parameters) {
        super(parameters);
    }

    private OneStorageDomainInfoReturnForXmlRpc result;

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().getStorageDomainInfo(getParameters().getStorageDomainId().toString());
        proceedProxyReturnValue();
        Pair<StorageDomainStatic, Guid> pairSdStatic = buildStorageStaticFromXmlRpcStruct(result.storageInfo);
        pairSdStatic.getFirst().setId(getParameters().getStorageDomainId());
        setReturnValue(pairSdStatic);
    }

    private static Pair<StorageDomainStatic, Guid> buildStorageStaticFromXmlRpcStruct(Map<String, Object> xmlRpcStruct) {
        Pair<StorageDomainStatic, Guid> returnValue = new Pair<>();
        StorageDomainStatic sdStatic = new StorageDomainStatic();
        if (xmlRpcStruct.containsKey("name")) {
            sdStatic.setStorageName(xmlRpcStruct.get("name").toString());
        }
        if (xmlRpcStruct.containsKey("type")) {
            sdStatic.setStorageType(EnumUtils.valueOf(StorageType.class, xmlRpcStruct.get("type").toString(),
                    true));
        }
        if (xmlRpcStruct.containsKey("class")) {
            String domainType = xmlRpcStruct.get("class").toString();
            if ("backup".equalsIgnoreCase(domainType)) {
                sdStatic.setStorageDomainType(StorageDomainType.ImportExport);
            } else {
                sdStatic.setStorageDomainType(EnumUtils.valueOf(StorageDomainType.class, domainType, true));
            }
        }
        if (xmlRpcStruct.containsKey("version")) {
            sdStatic.setStorageFormat(
                    StorageFormatType.forValue(xmlRpcStruct.get("version").toString()));
        }
        if (sdStatic.getStorageType() != StorageType.UNKNOWN) {
            if (sdStatic.getStorageType().isFileDomain() && xmlRpcStruct.containsKey("remotePath")) {
                String path = xmlRpcStruct.get("remotePath").toString();
                List<StorageServerConnections> connections = DbFacade.getInstance()
                        .getStorageServerConnectionDao().getAllForStorage(path);
                if (connections.isEmpty()) {
                    sdStatic.setConnection(new StorageServerConnections());
                    sdStatic.getConnection().setConnection(path);
                    sdStatic.getConnection().setStorageType(sdStatic.getStorageType());
                } else {
                    sdStatic.setStorage(connections.get(0).getId());
                    sdStatic.setConnection(connections.get(0));
                }
            } else if (sdStatic.getStorageType() != StorageType.NFS && xmlRpcStruct.containsKey("vguuid")) {
                sdStatic.setStorage(xmlRpcStruct.get("vguuid").toString());
            }
        }
        if (xmlRpcStruct.containsKey("state")) {
            sdStatic.setSanState(EnumUtils.valueOf(SANState.class, xmlRpcStruct.get("state")
                    .toString()
                    .toUpperCase(),
                    false));
        }
        returnValue.setFirst(sdStatic);
        Object[] poolUUIDs = (Object[])xmlRpcStruct.get("pool");
        if (poolUUIDs.length != 0) {
            returnValue.setSecond(Guid.createGuidFromString(poolUUIDs[0].toString()));
        }

        return returnValue;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return result.getXmlRpcStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }
}
