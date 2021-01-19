package org.ovirt.engine.core.common.businessentities;

import java.util.List;
import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;
import org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType;
import org.ovirt.engine.core.common.validation.annotation.HostnameOrIp;
import org.ovirt.engine.core.common.validation.annotation.ValidNameWithDot;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class VdsStatic implements BusinessEntity<Guid>, Commented {

    private static final long serialVersionUID = -1425566208615075937L;
    private static final int HOST_DEFAULT_SPM_PRIORITY = 5;
    public static final int DEFAULT_SSH_PORT = 22;
    private static final String DEFAULT_SSH_USERNAME = "root";


    private Guid id;

    @EditableVdsField
    @Size(min = 1, max = BusinessEntitiesDefinitions.HOST_NAME_SIZE)
    @ValidNameWithDot(message = "VALIDATION_VDS_NAME_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String name;

    @EditableVdsField
    @NotNull
    private String comment;

    @EditableVdsField
    @HostnameOrIp(message = "VALIDATION_VDS_CONSOLEADDRESSS_HOSTNAME_OR_IP",
            groups = { CreateEntity.class, UpdateEntity.class })
    @Size(max = BusinessEntitiesDefinitions.CONSOLE_ADDRESS_SIZE)
    private String consoleAddress;

    @Size(max = BusinessEntitiesDefinitions.HOST_UNIQUE_ID_SIZE)
    private String uniqueId;

    @EditableVdsField(
            onStatuses = { VDSStatus.NonResponsive, VDSStatus.Maintenance, VDSStatus.Down,
                    VDSStatus.Unassigned, VDSStatus.InstallFailed, VDSStatus.PendingApproval, VDSStatus.InstallingOS })
    @HostnameOrIp(message = "VALIDATION_VDS_HOSTNAME_HOSTNAME_OR_IP",
            groups = { CreateEntity.class, UpdateEntity.class })
    @NotNull(groups = { CreateEntity.class, UpdateEntity.class })
    @Size(max = BusinessEntitiesDefinitions.HOST_HOSTNAME_SIZE)
    private String hostName;

    @EditableVdsField
    @Range(min = BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT,
            max = BusinessEntitiesDefinitions.NETWORK_MAX_LEGAL_PORT,
            message = "VALIDATION_VDS_PORT_RANGE")
    private int port;

    @EditableVdsField(
            onStatuses = { VDSStatus.NonResponsive, VDSStatus.Maintenance, VDSStatus.Down,
                    VDSStatus.Unassigned, VDSStatus.InstallFailed, VDSStatus.PendingApproval, VDSStatus.InstallingOS })
    @Range(min = BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT,
            max = BusinessEntitiesDefinitions.NETWORK_MAX_LEGAL_PORT,
            message = "VALIDATION.VDS.SSH_PORT.RANGE")
    private int sshPort;

    @EditableVdsField
    @Size(min = 1, max = BusinessEntitiesDefinitions.HOST_NAME_SIZE)
    @ValidNameWithDot(message = "VALIDATION_VDS_SSH_USERNAME_INVALID", groups = { CreateEntity.class,
            UpdateEntity.class })
    private String sshUsername;

    @EditableVdsField(onStatuses = { VDSStatus.Maintenance, VDSStatus.PendingApproval })
    private Guid clusterId;

    private Boolean serverSslEnabled;

    private VDSType vdsType;

    @EditableVdsField
    private boolean pmEnabled;

    @EditableVdsField
    private List<FenceProxySourceType> fenceProxySources;

    @EditableVdsField
    private boolean pmKdumpDetection;

    /**
     * When this flag is true, the automatic power management
     * is not allowed to touch this host.
     */
    @EditableVdsField
    private boolean disablePowerManagementPolicy;

    @EditableVdsField
    private long otpValidity;

    @EditableVdsField
    @Min(BusinessEntitiesDefinitions.HOST_MIN_SPM_PRIORITY)
    @Max(BusinessEntitiesDefinitions.HOST_MAX_SPM_PRIORITY)
    private int vdsSpmPriority;

    private boolean autoRecoverable;

    @EditableVdsField
    @Size(max = BusinessEntitiesDefinitions.SSH_KEY_FINGERPRINT_SIZE)
    private String sshKeyFingerprint;

    @EditableVdsField
    @Size(max = BusinessEntitiesDefinitions.SSH_PUBLIC_KEY_SIZE)
    private String sshPublicKey;

    @EditableVdsField
    private Guid hostProviderId;

    /**
     * Current kernel cmdline
     *
     * <p>`null` ~ ""</p>
     */
    @EditableVdsField
    private String currentKernelCmdline;

    /**
     * True iff current {@link #currentKernelCmdline} can be build purely based on kernleCmdline*
     * options.
     */
    @EditableVdsField
    private boolean kernelCmdlineParsable;

    /**
     * Kernel cmdline used during last successful host-deploy set
     *
     * <p>`null` ~ ""</p>
     * <p>This shouldn't be updated by users. Only host deploy updates it.</p>
     */
    private String lastStoredKernelCmdline;

    @EditableVdsField
    private boolean kernelCmdlineBlacklistNouveau;

    @EditableVdsField
    private boolean kernelCmdlineIommu;

    @EditableVdsField
    private boolean kernelCmdlineKvmNested;

    @EditableVdsField
    private boolean kernelCmdlineUnsafeInterrupts;

    @EditableVdsField
    private boolean kernelCmdlinePciRealloc;

    @EditableVdsField
    private boolean kernelCmdlineFips;

    @EditableVdsField
    private boolean kernelCmdlineSmtDisabled;

    @EditableVdsField
    private boolean reinstallRequired;

    @EditableVdsField
    @Range(min = VgpuPlacement.MIN_VALUE, max = VgpuPlacement.MAX_VALUE)
    private int vgpuPlacement;

    public boolean isAutoRecoverable() {
        return autoRecoverable;
    }

    public void setAutoRecoverable(boolean autoRecoverable) {
        this.autoRecoverable = autoRecoverable;
    }

    public VdsStatic() {
        serverSslEnabled = false;
        vdsSpmPriority = HOST_DEFAULT_SPM_PRIORITY;
        sshPort = DEFAULT_SSH_PORT;
        sshUsername = DEFAULT_SSH_USERNAME;
        name = "";
        comment = "";
        vdsType = VDSType.VDS;
        autoRecoverable = true;
        disablePowerManagementPolicy = false;
        pmKdumpDetection = true;
        hostProviderId = null;
        vgpuPlacement = VgpuPlacement.CONSOLIDATED.getValue();
    }

    public VdsStatic(String hostName, String uniqueId, int port, int sshPort, String sshUsername, Guid clusterId,
            Guid vdsId, String vdsName, boolean serverSslEnabled, VDSType vdsType, Guid hostProviderId) {
        this();
        this.hostName = hostName;
        this.uniqueId = uniqueId;
        this.port = port;
        if (sshPort > 0) {
            this.sshPort = sshPort;
        }
        if (sshUsername != null) {
            this.sshUsername = sshUsername;
        }
        this.clusterId = clusterId;
        this.id = vdsId;
        this.name = vdsName;
        this.serverSslEnabled = serverSslEnabled;
        this.setVdsType(vdsType);
        this.hostProviderId = hostProviderId;
        comment = "";
    }

    public boolean isServerSslEnabled() {
        return serverSslEnabled;
    }

    public void setServerSslEnabled(boolean value) {
        serverSslEnabled = value;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String value) {
        hostName = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String value) {
        comment = value == null ? "" : value;
    }

    public String getUniqueID() {
        return uniqueId;
    }

    public void setUniqueID(String value) {
        uniqueId = value;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int value) {
        port = value;
    }

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int value) {
        sshPort = value;
    }

    public String getSshUsername() {
        return sshUsername;
    }

    public void setSshUsername(String value) {
        sshUsername = value;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid value) {
        clusterId = value;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public VDSType getVdsType() {
        return vdsType;
    }

    public void setVdsType(VDSType value) {
        vdsType = value;
    }

    public boolean isPmEnabled() {
        return pmEnabled;
    }

    public void setPmEnabled(boolean value) {
        pmEnabled = value;
    }

    public boolean isPmKdumpDetection() {
        return pmKdumpDetection;
    }

    public void setPmKdumpDetection(boolean pmKdumpDetection) {
        this.pmKdumpDetection = pmKdumpDetection;
    }

    public List<FenceProxySourceType> getFenceProxySources() {
        return fenceProxySources;
    }

    public void setFenceProxySources(List<FenceProxySourceType> fenceProxySources) {
        this.fenceProxySources = fenceProxySources;
    }

    public boolean isDisablePowerManagementPolicy() {
        return disablePowerManagementPolicy;
    }

    public void setDisablePowerManagementPolicy(boolean disablePowerManagementPolicy) {
        this.disablePowerManagementPolicy = disablePowerManagementPolicy;
    }

    public long getOtpValidity() {
        return otpValidity;
    }

    public void setOtpValidity(long otpValidity) {
        this.otpValidity = otpValidity;
    }

    public int getVdsSpmPriority() {
        return vdsSpmPriority;
    }

    public void setVdsSpmPriority(int value) {
        vdsSpmPriority = value;
    }

    public String getSshKeyFingerprint() {
        return sshKeyFingerprint;
    }

    public void setSshKeyFingerprint(String sshKeyFingerprint) {
        this.sshKeyFingerprint = sshKeyFingerprint;
    }

    public String getSshPublicKey(){
        return sshPublicKey;
    }

    public void setSshPublicKey(String sshPublicKey){
        this.sshPublicKey = sshPublicKey;
    }

    public String getConsoleAddress() {
        return consoleAddress;
    }

    public void setConsoleAddress(String consoleAddress) {
        this.consoleAddress = consoleAddress;
    }

    public void setHostProviderId (Guid hostProviderId) {
        this.hostProviderId = hostProviderId;
    }

    public Guid getHostProviderId () {
        return hostProviderId;
    }

    public String getCurrentKernelCmdline() {
        return currentKernelCmdline;
    }

    public void setCurrentKernelCmdline(String currentKernelCmdline) {
        this.currentKernelCmdline = currentKernelCmdline;
    }

    public boolean isKernelCmdlineParsable() {
        return kernelCmdlineParsable;
    }

    public void setKernelCmdlineParsable(boolean kernelCmdlineParsable) {
        this.kernelCmdlineParsable = kernelCmdlineParsable;
    }

    public String getLastStoredKernelCmdline() {
        return lastStoredKernelCmdline;
    }

    public void setLastStoredKernelCmdline(String lastStoredKernelCmdline) {
        this.lastStoredKernelCmdline = lastStoredKernelCmdline;
    }

    public boolean isKernelCmdlineBlacklistNouveau() {
        return kernelCmdlineBlacklistNouveau;
    }

    public void setKernelCmdlineBlacklistNouveau(boolean kernelCmdlineBlacklistNouveau) {
        this.kernelCmdlineBlacklistNouveau = kernelCmdlineBlacklistNouveau;
    }

    public boolean isKernelCmdlineIommu() {
        return kernelCmdlineIommu;
    }

    public void setKernelCmdlineIommu(boolean kernelCmdlineIommu) {
        this.kernelCmdlineIommu = kernelCmdlineIommu;
    }

    public boolean isKernelCmdlineKvmNested() {
        return kernelCmdlineKvmNested;
    }

    public void setKernelCmdlineKvmNested(boolean kernelCmdlineKvmNested) {
        this.kernelCmdlineKvmNested = kernelCmdlineKvmNested;
    }

    public boolean isKernelCmdlineUnsafeInterrupts() {
        return kernelCmdlineUnsafeInterrupts;
    }

    public void setKernelCmdlineUnsafeInterrupts(boolean kernelCmdlineUnsafeInterrupts) {
        this.kernelCmdlineUnsafeInterrupts = kernelCmdlineUnsafeInterrupts;
    }

    public boolean isKernelCmdlinePciRealloc() {
        return kernelCmdlinePciRealloc;
    }

    public void setKernelCmdlinePciRealloc(boolean kernelCmdlinePciRealloc) {
        this.kernelCmdlinePciRealloc = kernelCmdlinePciRealloc;
    }

    public boolean isKernelCmdlineFips() {
        return kernelCmdlineFips;
    }

    public void setKernelCmdlineFips(boolean kernelCmdlineFips) {
        this.kernelCmdlineFips = kernelCmdlineFips;
    }

    public boolean isReinstallRequired() {
        return reinstallRequired;
    }

    public void setReinstallRequired(boolean reinstallRequired) {
        this.reinstallRequired = reinstallRequired;
    }

    public boolean isKernelCmdlineSmtDisabled() {
        return kernelCmdlineSmtDisabled;
    }

    public void setKernelCmdlineSmtDisabled(boolean kernelCmdlineSmtDisabled) {
        this.kernelCmdlineSmtDisabled = kernelCmdlineSmtDisabled;
    }

    public int getVgpuPlacement() {
        return vgpuPlacement;
    }

    public void setVgpuPlacement(int vgpuPlacement) {
        this.vgpuPlacement = vgpuPlacement;
    }

    public boolean isManaged() {
        return vdsType != VDSType.KubevirtNode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                hostName,
                consoleAddress,
                name,
                otpValidity,
                pmEnabled,
                pmKdumpDetection,
                port,
                sshPort,
                sshUsername,
                serverSslEnabled,
                uniqueId,
                clusterId,
                vdsType,
                sshKeyFingerprint,
                sshPublicKey,
                disablePowerManagementPolicy,
                hostProviderId,
                currentKernelCmdline,
                kernelCmdlineParsable,
                lastStoredKernelCmdline,
                kernelCmdlineBlacklistNouveau,
                kernelCmdlineIommu,
                kernelCmdlineKvmNested,
                kernelCmdlinePciRealloc,
                kernelCmdlineUnsafeInterrupts,
                reinstallRequired,
                vgpuPlacement
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VdsStatic)) {
            return false;
        }
        VdsStatic other = (VdsStatic) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(hostName, other.hostName)
                && Objects.equals(consoleAddress, other.consoleAddress)
                && Objects.equals(name, other.name)
                && otpValidity == other.otpValidity
                && pmEnabled == other.pmEnabled
                && pmKdumpDetection == other.isPmKdumpDetection()
                && port == other.port
                && sshPort == other.sshPort
                && Objects.equals(sshUsername, other.sshUsername)
                && Objects.equals(serverSslEnabled, other.serverSslEnabled)
                && Objects.equals(uniqueId, other.uniqueId)
                && Objects.equals(clusterId, other.clusterId)
                && vdsType == other.vdsType
                && Objects.equals(sshKeyFingerprint, other.sshKeyFingerprint)
                && Objects.equals(sshPublicKey, other.sshPublicKey)
                && disablePowerManagementPolicy == other.disablePowerManagementPolicy
                && Objects.equals(hostProviderId, other.hostProviderId)
                && Objects.equals(currentKernelCmdline, other.currentKernelCmdline)
                && Objects.equals(kernelCmdlineParsable, other.kernelCmdlineParsable)
                && Objects.equals(lastStoredKernelCmdline, other.lastStoredKernelCmdline)
                && Objects.equals(kernelCmdlineBlacklistNouveau, other.kernelCmdlineBlacklistNouveau)
                && Objects.equals(kernelCmdlineIommu, other.kernelCmdlineIommu)
                && Objects.equals(kernelCmdlineKvmNested, other.kernelCmdlineKvmNested)
                && Objects.equals(kernelCmdlinePciRealloc, other.kernelCmdlinePciRealloc)
                && Objects.equals(kernelCmdlineUnsafeInterrupts, other.kernelCmdlineUnsafeInterrupts)
                && Objects.equals(kernelCmdlineFips, other.kernelCmdlineFips)
                && Objects.equals(kernelCmdlineSmtDisabled, other.kernelCmdlineSmtDisabled)
                && reinstallRequired == other.reinstallRequired
                && vgpuPlacement == other.vgpuPlacement;
    }
}
