package org.ovirt.engine.core.common.backendinterfaces;

import javax.ejb.Local;

import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//[ServiceContract(SessionMode = SessionMode.Required, CallbackContract = typeof(IVdsEventListener))]
@Local
public interface IResourceManager {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters);

    void setup();

}
