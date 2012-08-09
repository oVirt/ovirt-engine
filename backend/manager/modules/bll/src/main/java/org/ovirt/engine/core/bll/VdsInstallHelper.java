package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.hostinstall.IVdsInstallerCallback;
import org.ovirt.engine.core.utils.hostinstall.VdsInstallerSSH;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

/**
 * Class designed to provide connectivity to host and retrieval of its unique-id in order to validate host status before
 * starting the installation flow.
 */
public class VdsInstallHelper{

    private VdsInstallerSSH wrapper;
    private SimpleCallback callback;

    public VdsInstallHelper() {
        callback = new SimpleCallback();
        wrapper = new VdsInstallerSSH();
        wrapper.setCallback(callback);
    }

    public boolean connectToServer(String server, String passwd, long timeout) {
        return wrapper.connect(server, passwd, timeout);
    }

    public String getServerUniqueId() {
        wrapper.executeCommand(
            Config.<String> GetValue(ConfigValues.BootstrapNodeIDCommand)
        );
        return callback.serverUniqueId;
    }

    public void shutdown() {
        wrapper.shutdown();
        wrapper = null;
        callback = null;
    }

    public static List<VDS> getVdssByUniqueId(final Guid vdsId, String uniqueIdToCheck) {
        List<VDS> list = DbFacade.getInstance().getVdsDAO().getAllWithUniqueId(uniqueIdToCheck);
        return LinqUtils.filter(list, new Predicate<VDS>() {
            @Override
            public boolean eval(VDS vds) {
                return !vds.getId().equals(vdsId);
            }
        });
    }

    public static boolean isVdsUnique(final Guid vdsId, String uniqueIdToCheck) {
        return getVdssByUniqueId(vdsId, uniqueIdToCheck).isEmpty();
    }

    private class SimpleCallback implements IVdsInstallerCallback {

        String serverUniqueId;

        @Override
        public void addError(String error) {
        }

        @Override
        public void addMessage(String message) {
            serverUniqueId = message;
        }

        @Override
        public void connected() {
        }

        @Override
        public void endTransfer() {
        }

        @Override
        public void failed(String error) {
        }

    }
}
