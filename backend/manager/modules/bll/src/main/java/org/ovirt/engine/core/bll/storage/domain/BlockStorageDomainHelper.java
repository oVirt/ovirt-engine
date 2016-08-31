package org.ovirt.engine.core.bll.storage.domain;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BlockStorageDomainHelper {
    private static final Logger log = LoggerFactory.getLogger(BlockStorageDomainHelper.class);

    @Inject
    private ResourceManager resourceManager;

    private BlockStorageDomainHelper() {
    }

    public void fillMetadataDevicesInfo(StorageDomainStatic storageDomainStatic, Guid vdsId) {
        try {
            @SuppressWarnings("unchecked")
            StorageDomainStatic domainFromIrs =
                    ((Pair<StorageDomainStatic, Guid>) resourceManager.runVdsCommand(
                            VDSCommandType.HSMGetStorageDomainInfo,
                            new HSMGetStorageDomainInfoVDSCommandParameters(vdsId,
                                    storageDomainStatic.getId()))
                            .getReturnValue()).getFirst();
            storageDomainStatic.setFirstMetadataDevice(domainFromIrs.getFirstMetadataDevice());
            storageDomainStatic.setVgMetadataDevice(domainFromIrs.getVgMetadataDevice());
        } catch (Exception e) {
            log.info("Failed to get the domain info, ignoring");
        }
    }
}
