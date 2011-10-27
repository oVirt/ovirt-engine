package org.ovirt.engine.core.common.interfaces;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//[ServiceContract]
public interface IImagesHandler {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [OperationContract(IsInitiating = false),
    // FaultContract(typeof(VdcFault))]
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    void MigrateIrsSnapshotsToVdc();
}
