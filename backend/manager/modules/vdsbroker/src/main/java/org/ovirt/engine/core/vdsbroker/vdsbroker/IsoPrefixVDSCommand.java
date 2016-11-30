package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.vdscommands.VdsAndPoolIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsProperties;
import org.ovirt.engine.core.vdsbroker.irsbroker.StoragePoolInfo;

public class IsoPrefixVDSCommand<T extends VdsAndPoolIDVDSParametersBase> extends VdsBrokerCommand<T> {

    private static final ConcurrentHashMap<Guid, String> storagePoolIdToIsoPrefix = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Guid, Object> storagePoolIdToLockObj = new ConcurrentHashMap<>();

    public IsoPrefixVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        getVDSReturnValue().setReturnValue(getIsoPrefix());
    }

    private String getIsoPrefix() {
        Guid storagePoolId = getParameters().getStoragePoolId();

        String cachedIsoPrefix = storagePoolIdToIsoPrefix.get(storagePoolId);
        if (cachedIsoPrefix != null) {
            return cachedIsoPrefix;
        }

        synchronized(getLockObjForStoragePool(storagePoolId)) {
            cachedIsoPrefix = storagePoolIdToIsoPrefix.get(storagePoolId);
            if (cachedIsoPrefix != null) {
                return cachedIsoPrefix;
            }

            StoragePoolInfo retVal;
            try {
                retVal = getBroker().getStoragePoolInfo(storagePoolId.toString());
            } catch (Exception ex) {
                log.error("IsoPrefix Failed to get storage pool info (vds '{}', pool '{}').",
                        getParameters().getVdsId(), storagePoolId);
                return StringUtils.EMPTY;
            }

            String isoPrefix = getIsoPrefixFromStoragePoolInfoReturnValue(retVal);

            if (!isoPrefix.isEmpty()) {
                storagePoolIdToIsoPrefix.put(storagePoolId, isoPrefix);
            }

            return isoPrefix;
        }
    }

    private String getIsoPrefixFromStoragePoolInfoReturnValue(StoragePoolInfo retVal) {
        return retVal.storagePoolInfo.containsKey(IrsProperties.isoPrefix) ?
                retVal.storagePoolInfo.get(IrsProperties.isoPrefix).toString()
                : StringUtils.EMPTY;
    }

    static void clearCachedIsoPrefix(Guid storagePoolId) {
        storagePoolIdToIsoPrefix.remove(storagePoolId);
    }

    private static Object getLockObjForStoragePool(Guid storagePoolId) {
        Object result = storagePoolIdToLockObj.get(storagePoolId);
        if (result == null) {
            final Object value = new Object();
            result = storagePoolIdToLockObj.putIfAbsent(storagePoolId, value);
            if (result == null) {
                result = value;
            }
        }
        return result;
    }
}
