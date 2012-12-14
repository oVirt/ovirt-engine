package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.SANState;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class HSMGetStorageDomainInfoVDSCommand<P extends HSMGetStorageDomainInfoVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    public HSMGetStorageDomainInfoVDSCommand(P parameters) {
        super(parameters);
    }

    private OneStorageDomainInfoReturnForXmlRpc _result;

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().getStorageDomainInfo(getParameters().getStorageDomainId().toString());
        ProceedProxyReturnValue();
        Pair<StorageDomainStatic, SANState> pairSdStatic = BuildStorageStaticFromXmlRpcStruct(_result.mStorageInfo);
        pairSdStatic.getFirst().setId(getParameters().getStorageDomainId());
        setReturnValue(pairSdStatic);
    }

    private static Pair<StorageDomainStatic, SANState> BuildStorageStaticFromXmlRpcStruct(XmlRpcStruct xmlRpcStruct) {
        Pair<StorageDomainStatic, SANState> returnValue = new Pair<StorageDomainStatic, SANState>();
        StorageDomainStatic sdStatic = new StorageDomainStatic();
        if (xmlRpcStruct.contains("name")) {
            sdStatic.setstorage_name(xmlRpcStruct.getItem("name").toString());
        }
        if (xmlRpcStruct.contains("type")) {
            sdStatic.setstorage_type(EnumUtils.valueOf(StorageType.class, xmlRpcStruct.getItem("type").toString(),
                    true));
        }
        if (xmlRpcStruct.contains("class")) {
            String domainType = xmlRpcStruct.getItem("class").toString();
            if ("backup".equalsIgnoreCase(domainType)) {
                sdStatic.setstorage_domain_type(StorageDomainType.ImportExport);
            } else {
                sdStatic.setstorage_domain_type(EnumUtils.valueOf(StorageDomainType.class, domainType, true));
            }
        }
        if (xmlRpcStruct.contains("version")) {
            sdStatic.setStorageFormat(
                    StorageFormatType.forValue(xmlRpcStruct.getItem("version").toString()));
        }
        if (sdStatic.getstorage_type() != StorageType.UNKNOWN) {
            if (sdStatic.getstorage_type() == StorageType.NFS && xmlRpcStruct.contains("remotePath")) {
                String path = xmlRpcStruct.getItem("remotePath").toString();
                List<storage_server_connections> connections = DbFacade.getInstance()
                        .getStorageServerConnectionDao().getAllForStorage(path);
                if (connections.isEmpty()) {
                    sdStatic.setConnection(new storage_server_connections());
                    sdStatic.getConnection().setconnection(path);
                    sdStatic.getConnection().setstorage_type(StorageType.NFS);
                } else {
                    sdStatic.setstorage(connections.get(0).getid());
                    sdStatic.setConnection(connections.get(0));
                }
            } else if (sdStatic.getstorage_type() != StorageType.NFS && (xmlRpcStruct.contains("vguuid"))) {
                sdStatic.setstorage(xmlRpcStruct.getItem("vguuid").toString());
            }
        }
        if (xmlRpcStruct.contains("state")) {
            returnValue.setSecond(EnumUtils.valueOf(SANState.class, xmlRpcStruct.getItem("state")
                    .toString()
                    .toUpperCase(),
                    false));
        }
        returnValue.setFirst(sdStatic);
        return returnValue;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
