package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.SANState;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.Pair;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSErrorException;
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
        Pair<storage_domain_static, SANState> pairSdStatic = BuildStorageStaticFromXmlRpcStruct(_result.mStorageInfo);
        pairSdStatic.getFirst().setId(getParameters().getStorageDomainId());
        setReturnValue(pairSdStatic);
    }

    private static Pair<storage_domain_static, SANState> BuildStorageStaticFromXmlRpcStruct(XmlRpcStruct xmlRpcStruct) {
        try {
            Pair<storage_domain_static, SANState> returnValue = new Pair<storage_domain_static, SANState>();
            storage_domain_static sdStatic = new storage_domain_static();
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
                            .getStorageServerConnectionDAO().getAllForStorage(path);
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
        } catch (RuntimeException ex) {
            log.errorFormat(
                    "vdsBroker::BuildStorageStaticFromXmlRpcStruct::Failed building Storage static, xmlRpcStruct = {0}",
                    xmlRpcStruct.toString());
            IRSErrorException outEx = new IRSErrorException(ex);
            log.error(outEx);
            if (log.isDebugEnabled()) {
                log.debug("vdsBroker::BuildStorageStaticFromXmlRpcStruct::Failed building Storage static stack:"
                        + ex.getStackTrace(),
                        ex);
            }
            throw outEx;
        }
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }

    private static final Log log = LogFactory.getLog(HSMGetStorageDomainInfoVDSCommand.class);
}
