package org.ovirt.engine.core.vdsbroker.proxy;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.ovirt.engine.core.common.backendinterfaces.IResourceManager;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@Stateless(name = "VdsBroker")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@Local(IResourceManager.class)
public class ResourceManagerProxy implements IResourceManager {

    @Override
    public VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters) {
        return ResourceManager.getInstance().runVdsCommand(commandType, parameters);
    }

    @Override
    public void setup() {
        ResourceManager.getInstance();
    }
}
