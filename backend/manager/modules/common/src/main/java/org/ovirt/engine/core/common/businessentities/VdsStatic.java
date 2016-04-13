package org.ovirt.engine.core.common.businessentities;

import java.util.List;
import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;
import org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType;
import org.ovirt.engine.core.common.utils.CertificateSubjectHelper;
import org.ovirt.engine.core.common.validation.annotation.HostnameOrIp;
import org.ovirt.engine.core.common.validation.annotation.ValidNameWithDot;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class VdsStatic implements BusinessEntity<Guid>, Commented {

    private static final long serialVersionUID = -1425566208615075937L;
    private static final int HOST_DEFAULT_SPM_PRIORITY = 5;
    private static final int DEFAULT_SSH_PORT = 22;
    private static final String DEFAULT_SSH_USERNAME = "root";


    private Guid id;

    @EditableField
    @Size(min = 1, max = BusinessEntitiesDefinitions.HOST_NAME_SIZE)
    @ValidNameWithDot(message = "VALIDATION_VDS_NAME_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String name;

    @EditableField
    private String comment;

    @EditableField
    @HostnameOrIp(message = "VALIDATION_VDS_CONSOLEADDRESSS_HOSTNAME_OR_IP",
            groups = { CreateEntity.class, UpdateEntity.class })
    @Size(max = BusinessEntitiesDefinitions.CONSOLE_ADDRESS_SIZE)
    private String consoleAddress;

    @Size(max = BusinessEntitiesDefinitions.HOST_UNIQUE_ID_SIZE)
    private String uniqueId;

    @EditableOnVdsStatus
    @HostnameOrIp(message = "VALIDATION_VDS_HOSTNAME_HOSTNAME_OR_IP",
            groups = { CreateEntity.class, UpdateEntity.class })
    @NotNull(groups = { CreateEntity.class, UpdateEntity.class })
    @Size(max = BusinessEntitiesDefinitions.HOST_HOSTNAME_SIZE)
    private String hostName;

    @EditableField
    @Range(min = BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT,
            max = BusinessEntitiesDefinitions.NETWORK_MAX_LEGAL_PORT,
            message = "VALIDATION_VDS_PORT_RANGE")
    private int port;

    @EditableField
    private VdsProtocol protocol;

    @EditableOnVdsStatus
    @Range(min = BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT,
            max = BusinessEntitiesDefinitions.NETWORK_MAX_LEGAL_PORT,
            message = "VALIDATION.VDS.SSH_PORT.RANGE")
    private int sshPort;

    @EditableField
    @Size(min = 1, max = BusinessEntitiesDefinitions.HOST_NAME_SIZE)
    @ValidNameWithDot(message = "VALIDATION_VDS_SSH_USERNAME_INVALID", groups = { CreateEntity.class,
            UpdateEntity.class })
    private String sshUsername;

    @EditableOnVdsStatus
    private Guid clusterId;

    private Boolean serverSslEnabled;

    private VDSType vdsType;

    private Integer vdsStrength;

    @EditableField
    private boolean pmEnabled;

    @EditableField
    private List<FenceProxySourceType> fenceProxySources;

    @EditableField
    private boolean pmKdumpDetection;

    /**
     * When this flag is true, the automatic power management
     * is not allowed to touch this host.
     */
    @EditableField
    private boolean disablePowerManagementPolicy;

    @EditableField
    private long otpValidity;

    @EditableField
    @Min(BusinessEntitiesDefinitions.HOST_MIN_SPM_PRIORITY)
    @Max(BusinessEntitiesDefinitions.HOST_MAX_SPM_PRIORITY)
    private int vdsSpmPriority;

    private boolean autoRecoverable;

    @EditableField
    @Size(max = BusinessEntitiesDefinitions.SSH_KEY_FINGERPRINT_SIZE)
    private String sshKeyFingerprint;

    @EditableField
    private Guid hostProviderId;

    @EditableField
    private Guid openstackNetworkProviderId;

    private String certificateSubject;

    /**
     * Current kernel cmdline
     *
     * <p>`null` ~ ""</p>
     */
    @EditableField
    private String currentKernelCmdline;

    /**
     * True iff current {@link #currentKernelCmdline} can be build purely based on kernleCmdline*
     * options.
     */
    @EditableField
    private boolean kernelCmdlineParsable;

    /**
     * Kernel cmdline used during last successful host-deploy set
     *
     * <p>`null` ~ ""</p>
     * <p>This shouldn't be updated by users. Only host deploy updates it.</p>
     */
    private String lastStoredKernelCmdline;

    @EditableField
    private boolean kernelCmdlineIommu;

    @EditableField
    private boolean kernelCmdlineKvmNested;

    @EditableField
    private boolean kernelCmdlineUnsafeInterrupts;

    @EditableField
    private boolean kernelCmdlinePciRealloc;

    public boolean isAutoRecoverable() {
        return autoRecoverable;
    }

    public void setAutoRecoverable(boolean autoRecoverable) {
        this.autoRecoverable = autoRecoverable;
    }

    public VdsStatic() {
        serverSslEnabled = false;
        vdsStrength = 100;
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
        comment = value;
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

    public VdsProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(VdsProtocol value) {
        protocol = value;
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

    public int getVdsStrength() {
        return vdsStrength;
    }

    public void setVdsStrength(int value) {
        // strength should be between 1 and 100
        vdsStrength = value < 1 ? 1 : value > 100 ? 100 : value;
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

    public Guid getOpenstackNetworkProviderId() {
        return openstackNetworkProviderId;
    }

    public void setOpenstackNetworkProviderId(Guid openstackNetworkProviderId) {
        this.openstackNetworkProviderId = openstackNetworkProviderId;
    }

    public String getCertificateSubject() {
        if (certificateSubject == null && getHostName() != null) {
            setCertificateSubject(CertificateSubjectHelper.getCertificateSubject(getHostName()));
        }
        return certificateSubject;
    }

    public void setCertificateSubject(String certificateSubject) {
        this.certificateSubject = certificateSubject;
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
                protocol,
                sshPort,
                sshUsername,
                serverSslEnabled,
                uniqueId,
                clusterId,
                vdsStrength,
                vdsType,
                disablePowerManagementPolicy,
                hostProviderId,
                openstackNetworkProviderId,
                currentKernelCmdline,
                kernelCmdlineParsable,
                lastStoredKernelCmdline,
                kernelCmdlineIommu,
                kernelCmdlineKvmNested,
                kernelCmdlinePciRealloc,
                kernelCmdlineUnsafeInterrupts
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
                && protocol == other.protocol
                && sshPort == other.sshPort
                && Objects.equals(sshUsername, other.sshUsername)
                && Objects.equals(serverSslEnabled, other.serverSslEnabled)
                && Objects.equals(uniqueId, other.uniqueId)
                && Objects.equals(clusterId, other.clusterId)
                && Objects.equals(vdsStrength, other.vdsStrength)
                && vdsType == other.vdsType
                && Objects.equals(sshKeyFingerprint, other.sshKeyFingerprint)
                && disablePowerManagementPolicy == other.disablePowerManagementPolicy
                && Objects.equals(hostProviderId, other.hostProviderId)
                && Objects.equals(openstackNetworkProviderId, other.openstackNetworkProviderId)
                && Objects.equals(currentKernelCmdline, other.currentKernelCmdline)
                && Objects.equals(kernelCmdlineParsable, other.kernelCmdlineParsable)
                && Objects.equals(lastStoredKernelCmdline, other.lastStoredKernelCmdline)
                && Objects.equals(kernelCmdlineIommu, other.kernelCmdlineIommu)
                && Objects.equals(kernelCmdlineKvmNested, other.kernelCmdlineKvmNested)
                && Objects.equals(kernelCmdlinePciRealloc, other.kernelCmdlinePciRealloc)
                && Objects.equals(kernelCmdlineUnsafeInterrupts, other.kernelCmdlineUnsafeInterrupts);
    }
}
