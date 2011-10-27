package org.ovirt.engine.core.vdsbroker.proxy;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.ovirt.engine.core.common.backendinterfaces.IResourceManager;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//[ServiceBehavior(ConcurrencyMode = ConcurrencyMode.Multiple)]
@Stateless(name = "VdsBroker")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
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
