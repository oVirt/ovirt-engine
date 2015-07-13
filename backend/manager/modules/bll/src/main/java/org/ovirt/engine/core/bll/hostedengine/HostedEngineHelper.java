package org.ovirt.engine.core.bll.hostedengine;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.ovirt_host_deploy.constants.Const;
import org.ovirt.ovirt_host_deploy.constants.HostedEngineEnv;

public class HostedEngineHelper {

    private static final String HE_CONF_HOST_ID = "host_id";

    private DbFacade dbFacade;
    private VM hostedEngineVm;
    private StorageDomainStatic sd;

    @Inject
    private HostedEngineHelper(DbFacade dbFacade) {
        this.dbFacade = dbFacade;
    }

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private HostedEngineConfigFetcher hostedEngineConfigFetcher;

    @PostConstruct
    private void init() {
        List<VmStatic> byName = dbFacade.getVmStaticDao().getAllByName(
                Config.<String> getValue(ConfigValues.HostedEngineVmName));
        if (byName != null && !byName.isEmpty()) {
            VmStatic vmStatic = byName.get(0);
            hostedEngineVm = dbFacade.getVmDao().get(vmStatic.getId());
            VmHandler.updateDisksFromDb(hostedEngineVm);
        }

        sd = dbFacade.getStorageDomainStaticDao().getByName(
                Config.<String> getValue(ConfigValues.HostedEngineStorageDomainName));
    }

    public Map<String, String> createVdsDeployParams(Guid vdsId, HostedEngineDeployConfiguration.Action deployAction) {
        if (hostedEngineVm == null) {
            return Collections.emptyMap();
        }

        Map<String, String> params = new HashMap<>();
        params.put(HostedEngineEnv.ACTION, fromDeployAction(deployAction));
        if (HostedEngineDeployConfiguration.Action.DEPLOY == deployAction) {
            params.putAll(hostedEngineConfigFetcher.fetch());
            // This installation method will generate the host id for this HE host. This MUST be
            // set in the configuration other wise the agent won't be able to register itself to
            // the whiteboard and become a part of the cluster.
            params.put(HE_CONF_HOST_ID, String.valueOf(offerHostId(vdsId)));
        }
        return params;
    }

    protected String fromDeployAction(HostedEngineDeployConfiguration.Action deployAction) {
        switch (deployAction) {
        case DEPLOY:
            return Const.HOSTED_ENGINE_ACTION_DEPLOY;
        case UNDEPLOY:
            return Const.HOSTED_ENGINE_ACTION_REMOVE;
        }
        return Const.HOSTED_ENGINE_ACTION_NONE;
    }

    public boolean isVmManaged() {
        return hostedEngineVm != null && hostedEngineVm.isManagedVm();
    }

    /**
     * Offer the host id this data center allocated for this host in vds_spm_map. This effectively syncs
     * between the hosted engine HA identifier and vdsm's host ids that are used when locking storage domain for
     * monitoring.
     *
     * @return a numeric host id which identifies this host as part of hosted engine cluster
     */
    private int offerHostId(Guid vdsId) {
            return  dbFacade.getVdsSpmIdMapDao().get(vdsId).getVdsSpmId();
    }

    public StorageDomainStatic getStorageDomain() {
        return sd;
    }

    public static boolean isHostedEngineDomain(final StorageDomain storageDomain) {
        return Config.<String> getValue(ConfigValues.HostedEngineStorageDomainName).equals(storageDomain.getName());
    }

    /**
     * @return The Guid of the DC the engine VM is running under
     */
    public Guid getStoragePoolId() {
        return hostedEngineVm.getStoragePoolId();
    }

    /**
     * @return The Guid of Storage Domain of the engine VM
     */
    public Guid getStorageDomainId() {
        return getStorageDomain().getId();
    }

    /**
     * @return The Guid of the host running the engine VM
     */
    public Guid getRunningHostId() {
        return hostedEngineVm.getRunOnVds();
    }
}
