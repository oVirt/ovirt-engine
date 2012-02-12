package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.vdscommands.GetStoragePoolInfoVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.vdsbroker.vdsbroker.GetStorageDomainStatsVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

@Logged(executionLevel = LogLevel.DEBUG)
public class GetStoragePoolInfoVDSCommand<P extends GetStoragePoolInfoVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    private StoragePoolInfoReturnForXmlRpc _result;

    public GetStoragePoolInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        _result = getIrsProxy().getStoragePoolInfo(getParameters().getStoragePoolId().toString());
        ProceedProxyReturnValue();
        storage_pool sp = ParseStoragePoolInfoResult(_result.mStoragePoolInfo);
        Guid masterId = Guid.Empty;
        if (_result.mStoragePoolInfo.containsKey("master_uuid")) {
            masterId = new Guid(_result.mStoragePoolInfo.getItem("master_uuid").toString());
        }
        sp.setId(getParameters().getStoragePoolId());
        java.util.ArrayList<storage_domains> domList = ParseStorageDomainList(_result.mDomainsList, masterId);

        KeyValuePairCompat<storage_pool, java.util.List<storage_domains>> list =
                new KeyValuePairCompat<storage_pool, java.util.List<storage_domains>>(
                        sp, domList);
        setReturnValue(list);
    }

    private java.util.ArrayList<storage_domains> ParseStorageDomainList(XmlRpcStruct xmlRpcStruct, Guid masterId) {
        java.util.ArrayList<storage_domains> domainsList = new java.util.ArrayList<storage_domains>(
                xmlRpcStruct.getCount());
        for (String domain : xmlRpcStruct.getKeys()) {
            XmlRpcStruct domainAsStruct = new XmlRpcStruct((java.util.Map) xmlRpcStruct.getItem(domain));
            storage_domains sd = GetStorageDomainStatsVDSCommand.BuildStorageDynamicFromXmlRpcStruct(domainAsStruct);
            sd.setstorage_pool_id(getParameters().getStoragePoolId());
            sd.setid(new Guid(domain));
            if (!masterId.equals(Guid.Empty) && masterId.equals(sd.getid())) {
                sd.setstorage_domain_type(StorageDomainType.Master);
            } else if (!masterId.equals(Guid.Empty)) {
                sd.setstorage_domain_type(StorageDomainType.Data);
            } else {
                sd.setstorage_domain_type(StorageDomainType.Unknown);
            }
            domainsList.add(sd);
        }
        return domainsList;
    }

    private storage_pool ParseStoragePoolInfoResult(XmlRpcStruct xmlRpcStruct) {
        try {

            return VdsBrokerObjectsBuilder.buildStoragePool(xmlRpcStruct);

        } catch (RuntimeException ex) {
            log.errorFormat(
                    "irsBroker::BuildStorageDynamicFromXmlRpcStruct::Failed building Storage dynamic, xmlRpcStruct = {0}",
                    xmlRpcStruct.toString());
            IRSErrorException outEx = new IRSErrorException(ex);
            log.error(outEx);
            throw outEx;
        }
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    private static Log log = LogFactory.getLog(GetStoragePoolInfoVDSCommand.class);
}
