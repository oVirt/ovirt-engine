package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.hostinstall.IVdsInstallCallBack;
import org.ovirt.engine.core.utils.hostinstall.IVdsInstallWrapper;
import org.ovirt.engine.core.utils.hostinstall.VdsInstallerFactory;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

/**
 * Class designed to provide connectivity to host and retrieval of its unique-id in order to validate host status before
 * starting the installation flow.
 */
public class VdsInstallHelper{

    private IVdsInstallWrapper wrapper;
    private SimpleCallback callback;

    public VdsInstallHelper() {
        callback = new SimpleCallback();
        wrapper = VdsInstallerFactory.CreateVdsInstallWrapper();
        wrapper.InitCallback(callback);
    }

    public boolean connectToServer(String server, String passwd, long timeout) {
        return wrapper.ConnectToServer(server, passwd, timeout);
    }

    public String getServerUniqueId() {
        wrapper.RunSSHCommand(VdsInstaller._getUniqueIdCommand);
        return callback.serverUniqueId;
    }

    public void wrapperShutdown() {
        wrapper.wrapperShutdown();
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

    private class SimpleCallback implements IVdsInstallCallBack {

        String serverUniqueId;

        @Override
        public void AddError(String error) {
        }

        @Override
        public void AddMessage(String message) {
            serverUniqueId = message;
        }

        @Override
        public void Connected() {
        }

        @Override
        public void EndTransfer() {
        }

        @Override
        public void Failed(String error) {
        }

    }
}
