package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({ VM.class, VdsIdVDSCommandParametersBase.class, ActivateVdsVDSCommandParameters.class,
        AddVdsVDSCommandParameters.class, RemoveVdsVDSCommandParameters.class, CreateVmVDSCommandParameters.class,
        VdsAndVmIDVDSParametersBase.class, DestroyVmVDSCommandParameters.class, PauseVDSCommandParameters.class,
        HibernateVDSCommandParameters.class, ResumeVDSCommandParameters.class, MigrateVDSCommandParameters.class,
        ChangeDiskVDSCommandParameters.class, VmLogonVDSCommandParameters.class, VmLogoffVDSCommandParameters.class,
        VmLockVDSCommandParameters.class, VmMonitorCommandVDSCommandParameters.class,
        SetVmTicketVDSCommandParameters.class, SetVdsStatusVDSCommandParameters.class,
        SetVmStatusVDSCommandParameters.class, FailedToRunVmVDSCommandParameters.class,
        IsVmDuringInitiatingVDSCommandParameters.class, CreateImageVDSCommandParameters.class,
        DestroyImageVDSCommandParameters.class,
        GetImageInfoVDSCommandParameters.class, CopyImageVDSCommandParameters.class,
        CreateSnapshotVDSCommandParameters.class, MergeSnapshotsVDSCommandParameters.class,
        SetImageDescriptionVDSCommandParameters.class, SetImageLegalityVDSCommandParameters.class,
        ResetIrsVDSCommandParameters.class, GetImportCandidatesVDSCommandParameters.class,
        GetCandidateInfoVDSCommandParameters.class, ImportCandidateVDSCommandParameters.class,
        ExportCandidateVDSCommandParameters.class, StartSpiceVDSCommandParameters.class,
        RunVmHyperChannelCommandVDSCommandParameters.class, ShutdownVdsVDSCommandParameters.class,
        IrsBaseVDSCommandParameters.class, UpdateVdsVMsClearedVDSCommandParameters.class,
        NetworkVdsmVDSCommandParameters.class, String[].class, ConnectStorageServerVDSCommandParameters.class,
        GetStorageConnectionsListVDSCommandParameters.class, ValidateStorageDomainVDSCommandParameters.class,
        CreateStorageDomainVDSCommandParameters.class, ActivateStorageDomainVDSCommandParameters.class,
        DeactivateStorageDomainVDSCommandParameters.class, DetachStorageDomainVDSCommandParameters.class,
        AttachStorageDomainVDSCommandParameters.class, FormatStorageDomainVDSCommandParameters.class,
        SetStorageDomainDescriptionVDSCommandParameters.class, GetStorageDomainInfoVDSCommandParameters.class,
        GetStorageDomainStatsVDSCommandParameters.class, GetStorageDomainsListVDSCommandParameters.class,
        CreateStoragePoolVDSCommandParameters.class, SetStoragePoolDescriptionVDSCommandParameters.class,
        ConnectStoragePoolVDSCommandParameters.class, DisconnectStoragePoolVDSCommandParameters.class,
        GetStoragePoolInfoVDSCommandParameters.class, DeleteImageGroupVDSCommandParameters.class,
        MoveImageGroupVDSCommandParameters.class, GetImageDomainsListVDSCommandParameters.class,
        CreateVGVDSCommandParameters.class, RemoveVGVDSCommandParameters.class, GetVGInfoVDSCommandParameters.class,
        GetDeviceInfoVDSCommandParameters.class,
        DiscoverSendTargetsVDSCommandParameters.class, RefreshStoragePoolVDSCommandParameters.class,
        SpmStartVDSCommandParameters.class, SpmStopVDSCommandParameters.class, SpmStatusVDSCommandParameters.class,
        FenceSpmStorageVDSCommandParameters.class, HSMTaskGuidBaseVDSCommandParameters.class,
        SPMTaskGuidBaseVDSCommandParameters.class, UpdateVMVDSCommandParameters.class,
        RemoveVMVDSCommandParameters.class, FenceVdsVDSCommandParameters.class,
        UpdateVmDynamicDataVDSCommandParameters.class, UpdateVdsDynamicDataVDSCommandParameters.class,
        GetVmsInfoVDSCommandParameters.class, GetDeviceListVDSCommandParameters.class,
        ExtendStorageDomainVDSCommandParameters.class })
@XmlAccessorType(XmlAccessType.NONE)
public class VDSParametersBase {
    @XmlElement
    private boolean _runAsync = true;

    public boolean getRunAsync() {
        return _runAsync;
    }

    public void setRunAsync(boolean value) {
        _runAsync = value;
    }

    public VDSParametersBase() {
    }
}
