package org.ovirt.engine.core.common.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.asynctasks.EndedTaskInfo;
import org.ovirt.engine.core.common.asynctasks.IEndedTaskVisitor;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.queries.RegisterQueryParameters;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TransactionScopeOption;

@XmlSeeAlso({ VdsActionParameters.class, AddVdsActionParameters.class,
        VdsOperationActionParameters.class, UpdateVdsActionParameters.class,
        MaintananceNumberOfVdssParameters.class, AddVmTemplateParameters.class,
        UpdateVmTemplateParameters.class, VmTemplateParametersBase.class,
        VmPoolParametersBase.class, AddVmToPoolParameters.class, VmPoolOperationParameters.class,
        AddVmPoolWithVmsParameters.class, RemoveVmFromPoolParameters.class,
        VmPoolSimpleUserParameters.class, DetachUserFromTimeLeasedPoolParameters.class,
        UpdateUserVmPoolParameters.class, VmPoolUserParameters.class,
        AttachUserToTimeLeasedPoolParameters.class, TagsActionParametersBase.class,
        TagsOperationParameters.class, MoveTagParameters.class, AttachEntityToTagParameters.class,
        AttachVdsToTagParameters.class, TagsVmMapParameters.class, AdElementParametersBase.class,
        AdGroupElementParametersBase.class, VmToAdGroupParameters.class,
        VmToAdElementParameters.class, VmPoolToAdElementParameters.class,
        VmPoolToAdGroupParameters.class, DetachAdGroupFromTimeLeasedPoolParameters.class,
        ADElementTimeLeasedVmPoolParametersBase.class,
        AttachAdGroupTimeLeasedPoolCommandParameters.class, SetAdGroupRoleParameters.class,
        VmOperationParameterBase.class, RemoveVmFromImportExportParamenters.class,
        ShutdownVmParameters.class, StopVmParameters.class, RunVmParams.class, RunVmOnceParams.class,
        LogoffVmParameters.class, ChangeDiskCommandParameters.class,
        MonitorCommandParameters.class, SetVmTicketParameters.class,
        VmManagementParametersBase.class, AddVmFromTemplateParameters.class,
        AddVmFromScratchParameters.class, AddVmAndAttachToUserParameters.class,
        ExportVmParameters.class, ExportVmTemplateParameters.class,
        VmTemplateImportExportParameters.class, AddVMFromImportCandidateParameters.class,
        AddVmTemplateFromImportParameters.class, VmToUserParameters.class,
        SetUserRoleParameters.class, LoginUserParameters.class, LogoutUserParameters.class,
        BookmarksParametersBase.class, BookmarksOperationParameters.class,
        VdsGroupParametersBase.class, VdsGroupOperationParameters.class,
        VdsShutdownParameters.class, CreateAllSnapshotsFromVmParameters.class,
        ImagesActionsParametersBase.class, ImagesContainterParametersBase.class,
        MergeSnapshotParamenters.class, RemoveImageParameters.class,
        CreateImageTemplateParameters.class, AddImageFromScratchParameters.class,
        AddImageFromImportParameters.class, CreateSnapshotFromTemplateParameters.class,
        PowerClientMigrateOnConnectCheckParameters.class, ChangeUserPasswordParameters.class,
        SetDedicatedVmParameters.class, RegisterQueryParameters.class, InstallVdsParameters.class,
        ApproveVdsParameters.class, PermissionsOperationsParametes.class, RolesParameterBase.class,
        RolesOperationsParameters.class, RolesActionMapParameters.class,
        ActionGroupsToRoleParameter.class, RoleWithActionGroupsParameters.class,
        CreateComputerAccountParameters.class, StoragePoolParametersBase.class,
        StoragePoolManagementParameter.class, StoragePoolWithStoragesParameter.class,
        StorageDomainParametersBase.class, StorageDomainPoolParametersBase.class,
        DetachStorageDomainFromPoolParameters.class, StorageDomainManagementParameter.class,
        StorageServerConnectionParametersBase.class, AttachNetworkToVdsParameters.class,
        UpdateNetworkToVdsParameters.class, AddBondParameters.class, RemoveBondParameters.class,
        AddNetworkStoragePoolParameters.class, AttachNetworkToVdsGroupParameter.class,
        DisplayNetworkToVdsGroupParameters.class, AddVmInterfaceParameters.class,
        AddVmTemplateInterfaceParameters.class, RemoveVmInterfaceParameters.class,
        RemoveVmTemplateInterfaceParameters.class,
        VdsClusterParameters.class, AddSANStorageDomainParameters.class,
        TryBackToAllSnapshotsOfVmParameters.class, RestoreAllSnapshotsParameters.class,
        AddDiskToVmParameters.class, RemoveDisksFromVmParameters.class,
        MoveOrCopyImageGroupParameters.class, MoveOrCopyParameters.class, MoveVmParameters.class,
        ImportVmParameters.class, ImprotVmTemplateParameters.class,
        EventSubscriptionParametesBase.class, UpdateVmDiskParameters.class, DiskImage.class,
        DiskImageBase.class, ExtendSANStorageDomainParameters.class,
        MigrateVmToServerParameters.class, FenceVdsActionParameters.class,
        SetStoragePoolStatusParameters.class,
        SetConfigurationValueParametes.class,
        VdcActionType.class,
        AddVmAndAttachToPoolParameters.class,
        MaintananceVdsParameters.class,
        // java.util.ArrayList<VdcActionType>.class,
        HibernateVmParameters.class, NetworkBootProtocol.class, RemoveVmParameters.class,
        CreateCloneOfTemplateParameters.class, MoveMultipleImageGroupsParameters.class,
        FenceVdsManualyParameters.class, VolumeFormat.class, ImageOperation.class,
        RemoveStorageDomainParameters.class, RecoveryStoragePoolParameters.class, AddUserParameters.class,
        ChangeVDSClusterParameters.class, ChangeVMClusterParameters.class, SetupNetworksParameters.class })
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdcActionParametersBase")
public class VdcActionParametersBase implements java.io.Serializable {

    private static final long serialVersionUID = 3436680315595922758L;

    public VdcActionParametersBase() {
        // _sessionId = string.Empty;
        _shouldBeLogged = true;
        _transctionOption = TransactionScopeOption.Required;

        setTaskGroupSuccess(true);

        /**
         * Patch for web commands
         */
        /*
         * VITALY? if (HttpContext.Current != null) { setHttpSessionId("JTODO: HttpContext.Current.Session.SessionID");
         * //ParametersCurrentUser = HttpContext.Current.Session["VdcUser"] as VdcUser; }
         */

        _taskStartTime = System.currentTimeMillis();
        setParentCommand(VdcActionType.Unknown);
    }

    private transient String _sessionId;

    private boolean _shouldBeLogged;

    @XmlElement(name = "SessionId")
    public String getSessionId() {
        if (StringHelper.isNullOrEmpty(_sessionId)) {
            if (getHttpSessionId() != null) {
                _sessionId = getHttpSessionId();
            } else {
                _sessionId = "";
            }
        }
        return _sessionId;
    }

    public void setSessionId(String value) {
        _sessionId = value;
    }

    @XmlElement(name = "HttpSessionId")
    private String privateHttpSessionId;

    public String getHttpSessionId() {
        return privateHttpSessionId;
    }

    private void setHttpSessionId(String value) {
        privateHttpSessionId = value;
    }

    @XmlElement(name = "ParametersCurrentUser")
    private VdcUser privateParametersCurrentUser;

    public VdcUser getParametersCurrentUser() {
        return privateParametersCurrentUser;
    }

    public void setParametersCurrentUser(VdcUser value) {
        privateParametersCurrentUser = value;
    }

    public boolean getShouldBeLogged() {
        return _shouldBeLogged;
    }

    public void setShouldBeLogged(boolean value) {
        _shouldBeLogged = value;
    }

    private TransactionScopeOption _transctionOption;

    public TransactionScopeOption getTransactionScopeOption() {
        return _transctionOption;
    }

    public void setTransactionScopeOption(TransactionScopeOption value) {
        _transctionOption = value;
    }

    /**
     * Indicates if the command should use the compensation mechanism or not.
     */
    private boolean compensationEnabled = false;

    /**
     * @return the compensationEnabled
     */
    public boolean isCompensationEnabled() {
        return compensationEnabled;
    }

    /**
     * @param compensationEnabled the compensationEnabled to set
     */
    public void setCompensationEnabled(boolean compensationEnabled) {
        this.compensationEnabled = compensationEnabled;
    }

    @XmlElement(name = "ParentCommand")
    private VdcActionType privateParentCommand = VdcActionType.forValue(0);

    private transient VdcActionParametersBase parentParameters;

    public VdcActionType getParentCommand() {
        return privateParentCommand;
    }

    public void setParentCommand(VdcActionType value) {
        privateParentCommand = value;
    }

    @XmlTransient
    public VdcActionParametersBase getParentParameters() {
        return parentParameters;
    }

    public void setParentParemeters (VdcActionParametersBase parameters) {
        parentParameters = parameters;
    }

    // this flag marks if the command runned with MultipleAction for
    // ProcessExceptionToClient
    @XmlElement(name = "MultipleAction")
    private boolean privateMultipleAction;

    public boolean getMultipleAction() {
        return privateMultipleAction;
    }

    public void setMultipleAction(boolean value) {
        privateMultipleAction = value;
    }

    private java.util.ArrayList<VdcActionParametersBase> _imagesParameters;

    public java.util.ArrayList<VdcActionParametersBase> getImagesParameters() {
        if (_imagesParameters == null) {
            _imagesParameters = new java.util.ArrayList<VdcActionParametersBase>();
        }
        return _imagesParameters;
    }

    public void setImagesParameters(java.util.ArrayList<VdcActionParametersBase> value) {
        _imagesParameters = value;
    }

    private boolean privateTaskGroupSuccess;

    public boolean getTaskGroupSuccess() {
        return privateTaskGroupSuccess;
    }

    public void setTaskGroupSuccess(boolean value) {
        privateTaskGroupSuccess = value;
    }

    public boolean Accept(EndedTaskInfo taskInfo, IEndedTaskVisitor visitor) {
        boolean retVal = visitor.Visit(taskInfo, this);
        if (!retVal) {
            for (VdcActionParametersBase parameters : getImagesParameters()) {
                retVal = parameters.Accept(taskInfo, visitor);
                if (retVal) {
                    break;
                }
            }
        }
        return retVal;
    }

    private Object privateEntityId;

    public Object getEntityId() {
        return privateEntityId;
    }

    public void setEntityId(Object value) {
        privateEntityId = value;
    }

    @XmlElement(name = "TaskStartTime")
    private long _taskStartTime;

    public long getTaskStartTime() {
        return _taskStartTime;
    }

    public void setTaskStartTime(long value) {
        _taskStartTime = value;
    }

    @XmlElement
    private java.util.ArrayList<Guid> privateTaskIds;

    public java.util.ArrayList<Guid> getTaskIds() {
        return privateTaskIds;
    }

    public void setTaskIds(java.util.ArrayList<Guid> value) {
        privateTaskIds = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_imagesParameters == null) ? 0 : _imagesParameters.hashCode());
        result = prime * result + (_shouldBeLogged ? 1231 : 1237);
        result = prime * result + (int) (_taskStartTime ^ (_taskStartTime >>> 32));
        result = prime * result + ((_transctionOption == null) ? 0 : _transctionOption.hashCode());
        result = prime * result + ((privateEntityId == null) ? 0 : privateEntityId.hashCode());
        result = prime * result + ((privateHttpSessionId == null) ? 0 : privateHttpSessionId.hashCode());
        result = prime * result + (privateMultipleAction ? 1231 : 1237);
        result =
                prime * result + ((privateParametersCurrentUser == null) ? 0 : privateParametersCurrentUser.hashCode());
        result = prime * result + ((privateParentCommand == null) ? 0 : privateParentCommand.hashCode());
        result = prime * result + (privateTaskGroupSuccess ? 1231 : 1237);
        result = prime * result + ((privateTaskIds == null) ? 0 : privateTaskIds.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VdcActionParametersBase other = (VdcActionParametersBase) obj;
        if (_imagesParameters == null) {
            if (other._imagesParameters != null)
                return false;
        } else if (!_imagesParameters.equals(other._imagesParameters))
            return false;
        if (_shouldBeLogged != other._shouldBeLogged)
            return false;
        if (_taskStartTime != other._taskStartTime)
            return false;
        if (_transctionOption != other._transctionOption)
            return false;
        if (privateEntityId == null) {
            if (other.privateEntityId != null)
                return false;
        } else if (!privateEntityId.equals(other.privateEntityId))
            return false;
        if (privateHttpSessionId == null) {
            if (other.privateHttpSessionId != null)
                return false;
        } else if (!privateHttpSessionId.equals(other.privateHttpSessionId))
            return false;
        if (privateMultipleAction != other.privateMultipleAction)
            return false;
        if (privateParametersCurrentUser == null) {
            if (other.privateParametersCurrentUser != null)
                return false;
        } else if (!privateParametersCurrentUser.equals(other.privateParametersCurrentUser))
            return false;
        if (privateParentCommand != other.privateParentCommand)
            return false;
        if (privateTaskGroupSuccess != other.privateTaskGroupSuccess)
            return false;
        if (privateTaskIds == null) {
            if (other.privateTaskIds != null)
                return false;
        } else if (!privateTaskIds.equals(other.privateTaskIds))
            return false;
        return true;
    }
}
