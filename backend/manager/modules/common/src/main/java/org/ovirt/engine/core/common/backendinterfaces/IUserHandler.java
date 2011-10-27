package org.ovirt.engine.core.common.backendinterfaces;

import org.ovirt.engine.core.common.action.*;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//[ServiceContract]
public interface IUserHandler {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsInitiating = true), FaultContract(typeof(VdcFault))]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    VdcReturnValueBase Login(LoginUserParameters parameters);

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsInitiating = false,IsTerminating = true),
    // FaultContract(typeof(VdcFault))]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    VdcReturnValueBase Logoff(LogoutUserParameters parameters);
}
