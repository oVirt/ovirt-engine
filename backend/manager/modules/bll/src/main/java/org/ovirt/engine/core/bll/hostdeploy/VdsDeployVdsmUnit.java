package org.ovirt.engine.core.bll.hostdeploy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.otopi.dialog.Event;
import org.ovirt.otopi.dialog.SoftError;
import org.ovirt.ovirt_host_deploy.constants.VdsmEnv;
import org.ovirt.ovirt_host_deploy.constants.VirtEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VdsDeployVdsmUnit implements VdsDeployUnit {

    private static final Logger log = LoggerFactory.getLogger(VdsDeployVdsmUnit.class);

    private final List<Callable<Boolean>> CUSTOMIZATION_DIALOG = Arrays.asList(
        new Callable<Boolean>() { public Boolean call() throws Exception {
            if (
                (Boolean)_deploy.getParser().cliEnvironmentGet(
                    VdsmEnv.OVIRT_NODE
                )
            ) {
                _deploy.userVisibleLog(
                    Level.INFO,
                    "Host is hypervisor"
                );
                _setNode();
            }
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            if (_isNode) {
                _isLegacyNode = (Boolean)_deploy.getParser().cliEnvironmentGet(
                    VdsmEnv.OVIRT_NODE_HAS_OWN_BRIDGES
                );
            }
            else {
                _deploy.getParser().cliNoop();
            }
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _setVdsmId((String)_deploy.getParser().cliEnvironmentGet(VdsmEnv.VDSM_ID));
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                String.format(
                    "%svars/ssl",
                    VdsmEnv.CONFIG_PREFIX
                ),
                Config.<Boolean> getValue(ConfigValues.EncryptHostCommunication).toString()
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                String.format(
                    "%saddresses/management_port",
                    VdsmEnv.CONFIG_PREFIX
                ),
                Integer.toString(_deploy.getVds().getPort())
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                VdsmEnv.ENGINE_HOST,
                EngineLocalConfig.getInstance().getHost()
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                VdsmEnv.ENGINE_PORT,
                EngineLocalConfig.getInstance().getExternalHttpPort()
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            if (_managementNetwork != null) {
                _deploy.getParser().cliEnvironmentSet(
                    VdsmEnv.MANAGEMENT_BRIDGE_NAME,
                    _managementNetwork
                );
            }
            else if (_isLegacyNode) {
                final ManagementNetworkUtil managmentNetworkUtil = Injector.get(ManagementNetworkUtil.class);
                final Guid clusterId = _deploy.getVds().getVdsGroupId();
                final Network managementNetwork = managmentNetworkUtil.getManagementNetwork(clusterId);
                _deploy.getParser().cliEnvironmentSet(
                    VdsmEnv.MANAGEMENT_BRIDGE_NAME,
                    managementNetwork.getName());
            }
            else {
                _deploy.getParser().cliNoop();
            }
            return true;
        }},
        new Callable<Boolean>() {
            public Boolean call() throws Exception {
                String minimal = Config.<String> getValue(ConfigValues.BootstrapMinimalVdsmVersion);
                if (minimal.trim().length() == 0) {
                    _deploy.getParser().cliNoop();
            }
            else {
                _deploy.getParser().cliEnvironmentSet(
                    VdsmEnv.VDSM_MINIMUM_VERSION,
                    minimal
                );
            }
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDao().get(
                _deploy.getVds().getVdsGroupId()
            );
            _deploy.getParser().cliEnvironmentSet(
                VdsmEnv.CHECK_VIRT_HARDWARE,
                vdsGroup.supportsVirtService()
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDao().get(
                _deploy.getVds().getVdsGroupId()
            );
            _deploy.getParser().cliEnvironmentSet(
                VirtEnv.ENABLE,
                vdsGroup.supportsVirtService()
            );
            return true;
        }}
    );

    private VdsDeployBase _deploy;
    private String _managementNetwork = null;
    private boolean _isNode = false;
    private boolean _isLegacyNode = false;

    /**
     * set vds object with unique id.
     * Check if vdsmid is unique, if not, halt installation, otherwise
     * update the vds object.
     * @param vdsmid unique id read from host.
     */
    private void _setVdsmId(String vdsmid) {
        if (vdsmid == null) {
            throw new SoftError("Cannot acquire node id");
        }

        log.info(
            "Host {} reports unique id {}",
            _deploy.getVds().getHostName(),
            vdsmid
        );

        final List<VDS> list = LinqUtils.filter(
            DbFacade.getInstance().getVdsDao().getAllWithUniqueId(vdsmid),
            new Predicate<VDS>() {
                @Override
                public boolean eval(VDS vds) {
                    return !vds.getId().equals(_deploy.getVds().getId());
                }
            }
        );

        if (!list.isEmpty()) {
            final StringBuilder hosts = new StringBuilder(1024);
            for (VDS v : list) {
                if (hosts.length() > 0) {
                    hosts.append(", ");
                }
                hosts.append(v.getName());
            }

            log.error(
                "Host {} reports duplicate unique id {} of following hosts {}",
                _deploy.getVds().getHostName(),
                vdsmid,
                hosts
            );
            throw new SoftError(
                String.format(
                    "Host %1$s reports unique id which already registered for %2$s",
                    _deploy.getVds().getHostName(),
                    hosts
                )
            );
        }

        log.info("Assigning unique id {} to Host {}", vdsmid, _deploy.getVds().getHostName());
        _deploy.getVds().setUniqueId(vdsmid);

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                DbFacade.getInstance().getVdsStaticDao().update(_deploy.getVds().getStaticData());
                return null;
            }
        });
    }

    /**
     * Set host to be node.
     */
    private void _setNode() {
        _isNode = true;

        _deploy.getVds().setVdsType(VDSType.oVirtNode);

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                DbFacade.getInstance().getVdsStaticDao().update(_deploy.getVds().getStaticData());
                return null;
            }
        });
    }

    public VdsDeployVdsmUnit(String managementNetwork) {
        _managementNetwork = managementNetwork;
    }

    // VdsDeployUnit interface

    @Override
    public void setVdsDeploy(VdsDeployBase deploy) {
        _deploy = deploy;
    }

    @Override
    public void init() {
        _deploy.addCustomizationDialog(CUSTOMIZATION_DIALOG);
    }

    @Override
    public boolean processEvent(Event.Base bevent) throws IOException {
        return true;
    }
}
