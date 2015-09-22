package org.ovirt.engine.core.bll.hostedengine;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.EngineLocalConfig;

public class HostedEngineHelper {

    private static final String CA_CERT_PATH = "/etc/pki/vdsm/libvirt-spice/ca-cert.pem";

    private DbFacade dbFacade;
    private VM hostedEngineVm;
    private StorageDomainStatic sd;
    private StorageServerConnections sdConnection;

    @Inject
    private HostedEngineHelper(DbFacade dbFacade) {
        this.dbFacade = dbFacade;
    }

    @PostConstruct
    private void init() {
        List<VmStatic> byName = dbFacade.getVmStaticDao().getAllByName(
                Config.<String>getValue(ConfigValues.HostedEngineVmName));
        if (byName != null && !byName.isEmpty()) {
            VmStatic vmStatic = byName.get(0);
            hostedEngineVm = dbFacade.getVmDao().get(vmStatic.getId());
            VmHandler.updateDisksFromDb(hostedEngineVm);
            sd = dbFacade.getStorageDomainStaticDao().getByName(
                    Config.<String>getValue(ConfigValues.HostedEngineStorageDomainName));
            if (sd != null) {
                sdConnection = dbFacade.getStorageServerConnectionDao()
                        .getAllForDomain(getStorageDomainStatic().getId()).get(0);
            }
        }
    }

    public Map<String, String> createVdsDeployParams(String hostname, String heAgentGateway) {
        if (hostedEngineVm == null) {
            return Collections.emptyMap();
        }

        HashMap params = new HashMap<>();
        params.put(
                "fqdn",
                EngineLocalConfig.getInstance().getHost());
        params.put(
                "vmid",
                hostedEngineVm.getId().toString());
        params.put(
                "storage",
                sd.getConnection());
        params.put(
                "conf",
                getBackupConfPath());
        params.put(
                "host_id",
                offerHostId(hostedEngineVm.getVdsGroupId()));
        params.put(
                "domainType",
                getStorageType());
        params.put(
                "spUUID",
                hostedEngineVm.getStoragePoolId().toString());
        params.put(
                "sdUUID",
                getStorageDomainStatic().getId().toString());
        params.put(
                "connectionUUID",
                sdConnection.getid().toString());
        params.put(
                "iqn",
                sdConnection.getiqn());
        params.put(
                "portal",
                sdConnection.getport());
        params.put(
                "user",
                sdConnection.getuser_name());
        params.put(
                "password",
                sdConnection.getpassword());
        params.put(
                "port",
                sdConnection.getport());
        params.put(
                "ca_cert",
                getCaCertPath());
        params.put(
                "ca_subject",
                hostname);
        params.put(
                "vdsm_use_ssl",
                Config.<Boolean>getValue(ConfigValues.EncryptHostCommunication).toString());
        params.put(
                "gateway",
                heAgentGateway);
        if (hostedEngineVm.getDiskList() != null) {
            for (int i = 0; i < hostedEngineVm.getDiskList().size(); i++) {
                params.put(
                        "vm_disk_id[" + i + "]",
                        hostedEngineVm.getDiskList().get(i).getImageId().toString());
                params.put(
                        "vm_disk_vol_id[" + i + "]",
                        hostedEngineVm.getDiskList().get(i).getImage().getDiskId().toString());
            }
        }
        return params;
    }

    private StorageDomainStatic getStorageDomainStatic() {
        return sd;
    }

    /**
     * Offer a host id to the underlying agent. It is a best effort currently and the agent might use
     * a different, free one.
     *
     * The reason it is a best effort is because 2 concurrent host deploy may run, competing on the id
     *
     * @param vdsGroupId
     * @return
     */
    private String offerHostId(Guid vdsGroupId) {
        int i = 0;
        for (VDS host : dbFacade.getVdsDao().getAllForVdsGroup(vdsGroupId)) {
            if (host.getHighlyAvailableScore() > 0) {
                // count as HE Host
                i++;
            }
        }
        return String.valueOf(i + 1);
    }

    private String getBackupConfPath() {
        return "/var/run/ovirt-hosted-engine-ha/vm.conf";
    }

    private String getStorageType() {
        switch (sdConnection.getstorage_type()) {
        case NFS:
            if (sdConnection.getNfsVersion() == NfsVersion.V4) {
                return "nfs4";
            } else {
                return "nfs3";
            }
        case ISCSI:
            return "iscsi";
        case GLUSTERFS:
            return "glusterfs";
        case FCP:
            return "fc";
        }
        throw new IllegalArgumentException(
                "There is no legal storage type for the connection " + sdConnection.toString());
    }

    private String getCaCertPath() {
        return CA_CERT_PATH;
    }

    public boolean isVmManaged() {
        return hostedEngineVm != null && hostedEngineVm.isManagedVm();
    }

    public StorageDomainStatic getStorageDomain() {
        return sd;
    }
}
