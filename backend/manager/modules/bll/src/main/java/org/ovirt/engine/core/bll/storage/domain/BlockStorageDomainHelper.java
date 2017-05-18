package org.ovirt.engine.core.bll.storage.domain;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetVGInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BlockStorageDomainHelper {
    private static final Logger log = LoggerFactory.getLogger(BlockStorageDomainHelper.class);

    @Inject
    private ResourceManager resourceManager;

    @Inject
    protected AuditLogDirector auditLogDirector;

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
            storageDomainStatic.setFirstMetadataDevice(null);
            storageDomainStatic.setVgMetadataDevice(null);
            log.info("Failed to get the domain info");
        }
    }

    public List<String> findMetadataDevices(StorageDomain storageDomain, Collection<String> devices) {
        return devices.stream()
                .filter(x -> x.equals(storageDomain.getVgMetadataDevice())
                        || x.equals(storageDomain.getFirstMetadataDevice()))
                .collect(toList());
    }

    @SuppressWarnings("unchecked")
    public List<LUNs> getVgLUNsInfo(StorageDomainStatic storageDomain, Guid vdsId) {
        try {
            return (List<LUNs>) resourceManager.runVdsCommand(VDSCommandType.GetVGInfo,
                    new GetVGInfoVDSCommandParameters(vdsId, storageDomain.getStorage()))
                    .getReturnValue();
        } catch (Exception e) {
            log.info("Failed to get the domain info, ignoring");
        }

        return null;
    }

    public boolean checkDomainMetadataDevices(StorageDomain domain) {
        if (domain.getVgMetadataDevice() == null || domain.getFirstMetadataDevice() == null) {
            AuditLogable logable = new AuditLogableImpl();
            logable.setStorageDomainName(domain.getName());
            logable.setStorageDomainId(domain.getId());
            auditLogDirector.log(logable, AuditLogType.FAILED_DETERMINE_STORAGE_DOMAIN_METADATA_DEVICES);
            return false;
        }

        return true;
    }
}
