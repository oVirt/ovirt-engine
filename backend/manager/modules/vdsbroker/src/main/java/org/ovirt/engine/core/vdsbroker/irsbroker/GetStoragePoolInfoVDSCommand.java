package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.vdscommands.GetStoragePoolInfoVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.vdsbroker.vdsbroker.GetStorageDomainStatsVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder;

@Logged(executionLevel = LogLevel.DEBUG)
public class GetStoragePoolInfoVDSCommand<P extends GetStoragePoolInfoVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    private StoragePoolInfoReturnForXmlRpc _result;

    public GetStoragePoolInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        _result = getIrsProxy().getStoragePoolInfo(getParameters().getStoragePoolId().toString());
        proceedProxyReturnValue();
        StoragePool sp = VdsBrokerObjectsBuilder.buildStoragePool(_result.mStoragePoolInfo);
        Guid masterId = Guid.Empty;
        if (_result.mStoragePoolInfo.containsKey("master_uuid")) {
            masterId = new Guid(_result.mStoragePoolInfo.get("master_uuid").toString());
        }
        sp.setId(getParameters().getStoragePoolId());
        ArrayList<StorageDomain> domList = ParseStorageDomainList(_result.mDomainsList, masterId);

        KeyValuePairCompat<StoragePool, List<StorageDomain>> list =
                new KeyValuePairCompat<StoragePool, List<StorageDomain>>(
                        sp, domList);
        setReturnValue(list);
    }

    @SuppressWarnings("unchecked")
    private ArrayList<StorageDomain> ParseStorageDomainList(Map<String, Object> xmlRpcStruct, Guid masterId) {
        ArrayList<StorageDomain> domainsList = new ArrayList<StorageDomain>(xmlRpcStruct.size());
        for (Entry<String, Object> entry : xmlRpcStruct.entrySet()) {
            Map<String, Object> domainAsStruct = (Map<String, Object>) entry.getValue();
            StorageDomain sd = GetStorageDomainStatsVDSCommand.buildStorageDynamicFromXmlRpcStruct(domainAsStruct);
            sd.setStoragePoolId(getParameters().getStoragePoolId());
            sd.setId(new Guid(entry.getKey()));
            if (!masterId.equals(Guid.Empty) && masterId.equals(sd.getId())) {
                sd.setStorageDomainType(StorageDomainType.Master);
            } else if (!masterId.equals(Guid.Empty)) {
                sd.setStorageDomainType(StorageDomainType.Data);
            } else {
                sd.setStorageDomainType(StorageDomainType.Unknown);
            }
            domainsList.add(sd);
        }
        return domainsList;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.getXmlRpcStatus();
    }
}
