package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Range;
import org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.pm.FenceProxySourceTypeHelper;
import org.ovirt.engine.core.common.validation.annotation.HostnameOrIp;
import org.ovirt.engine.core.common.validation.annotation.ValidNameWithDot;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

@Entity
@Table(name = "vds_static")
@Cacheable(true)
@NamedQueries({
        @NamedQuery(name = "VdsStatic.getByHostName",
                query = "select v from VdsStatic v where v.hostName = :hostName"),
        @NamedQuery(name = "VdsStatic.getAllForVdsGroup",
                query = "select v from VdsStatic v where v.vdsGroupId = :vdsGroupId"),
        @NamedQuery(name = "VdsStatic.getByVdsName",
                query = "select v.name from VdsStatic v where v.name = :name")
})
public class VdsStatic implements BusinessEntity<Guid>, Commented {

    private static final long serialVersionUID = -1425566208615075937L;
    private static final int HOST_DEFAULT_SPM_PRIORITY = 5;
    private static final int DEFAULT_SSH_PORT = 22;
    private static final String DEFAULT_SSH_USERNAME = "root";

    @Id
    @Column(name = "vds_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid id;

    @EditableField
    @Size(min = 1, max = BusinessEntitiesDefinitions.HOST_NAME_SIZE)
    @ValidNameWithDot(message = "VALIDATION_VDS_NAME_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    @Column(name = "vds_name")
    private String name;

    @EditableField
    @Column(name = "free_text_comment")
    private String comment;

    @EditableField
    @HostnameOrIp(message = "VALIDATION.VDS.CONSOLEADDRESSS.HOSTNAME_OR_IP",
            groups = { CreateEntity.class, UpdateEntity.class })
    @Size(max = BusinessEntitiesDefinitions.CONSOLE_ADDRESS_SIZE)
    @Column(name = "console_address")
    private String consoleAddress;

    @Size(max = BusinessEntitiesDefinitions.HOST_UNIQUE_ID_SIZE)
    @Column(name = "vds_unique_id")
    private String uniqueId;

    @EditableOnVdsStatus
    @HostnameOrIp(message = "VALIDATION.VDS.HOSTNAME.HOSTNAME_OR_IP",
            groups = { CreateEntity.class, UpdateEntity.class })
    @NotNull(groups = { CreateEntity.class, UpdateEntity.class })
    @Size(max = BusinessEntitiesDefinitions.HOST_HOSTNAME_SIZE)
    @Column(name = "host_name")
    private String hostName;

    @EditableField
    @Range(min = BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT,
            max = BusinessEntitiesDefinitions.NETWORK_MAX_LEGAL_PORT,
            message = "VALIDATION.VDS.PORT.RANGE")
    @Column(name = "port")
    private int port;

    @EditableField
    @Column(name = "protocol")
    @Enumerated(EnumType.ORDINAL)
    private VdsProtocol protocol;

    @EditableOnVdsStatus
    @Range(min = BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT,
            max = BusinessEntitiesDefinitions.NETWORK_MAX_LEGAL_PORT,
            message = "VALIDATION.VDS.SSH_PORT.RANGE")
    @Column(name = "ssh_port")
    private int sshPort;

    @EditableField
    @Size(min = 1, max = BusinessEntitiesDefinitions.HOST_NAME_SIZE)
    @ValidNameWithDot(message = "VALIDATION_VDS_SSH_USERNAME_INVALID", groups = { CreateEntity.class,
            UpdateEntity.class })
    @Column(name = "ssh_username")
    private String sshUsername;

    @EditableOnVdsStatus
    @Column(name = "vds_group_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid vdsGroupId;

    @Column(name = "server_ssl_enabled")
    private Boolean serverSslEnabled;

    @Column(name = "vds_type")
    @Enumerated(EnumType.ORDINAL)
    private VDSType vdsType;

    @Column(name = "vds_strength")
    private Integer vdsStrength;

    @EditableField
    @Column(name = "pm_enabled")
    private boolean pmEnabled;

    @EditableField
    private transient List<FenceProxySourceType> fenceProxySources;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    @Column(name = "pm_proxy_preferences")
    private String pmProxyPreferences;

    @EditableField
    @Column(name = "pm_detect_kdump")
    private boolean pmKdumpDetection;

    /**
     * When this flag is true, the automatic power management
     * is not allowed to touch this host.
     */
    @EditableField
    @Column(name = "disable_auto_pm")
    private boolean disablePowerManagementPolicy;

    @EditableField
    @Column(name = "otp_validity")
    private Long otpValidity;

    @EditableField
    @Min(BusinessEntitiesDefinitions.HOST_MIN_SPM_PRIORITY)
    @Max(BusinessEntitiesDefinitions.HOST_MAX_SPM_PRIORITY)
    @Column(name = "vds_spm_priority")
    private int vdsSpmPriority;

    @Column(name = "recoverable")
    private boolean autoRecoverable;

    @EditableField
    @Size(max = BusinessEntitiesDefinitions.SSH_KEY_FINGERPRINT_SIZE)
    @Column(name = "sshkeyfingerprint")
    private String sshKeyFingerprint;

    @EditableField
    @Column(name = "host_provider_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid hostProviderId;

    public boolean isAutoRecoverable() {
        return autoRecoverable;
    }

    public void setAutoRecoverable(boolean autoRecoverable) {
        this.autoRecoverable = autoRecoverable;
    }

    @PostLoad
    protected void afterLoad() {
        fenceProxySources = FenceProxySourceTypeHelper.parseFromString(pmProxyPreferences);
    }

    @PrePersist
    protected void beforeStore() {
        pmProxyPreferences = FenceProxySourceTypeHelper.saveAsString(fenceProxySources);
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
        protocol = VdsProtocol.STOMP;
    }

    public VdsStatic(String hostName, String uniqueId, int port, int sshPort, String sshUsername, Guid vdsGroupId,
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
        this.vdsGroupId = vdsGroupId;
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

    public Guid getVdsGroupId() {
        return vdsGroupId;
    }

    public void setVdsGroupId(Guid value) {
        vdsGroupId = value;
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

    public Long getOtpValidity() {
        return otpValidity;
    }

    public void setOtpValidity(Long otpValidity) {
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

    /**
     * Converts a PM Options string to a map.
     *
     * <b<Note:</b> A {@link HashMap} is used instead of the interface
     * {@link Map}, as this method is used by the frontend, and requires
     * GWT compilation.
     *
     * @param pmOptions String representation of the map
     * @return A parsed map
     */
    public static HashMap<String, String> pmOptionsStringToMap(String pmOptions) {
        HashMap<String, String> map = new HashMap<>();
        if (pmOptions == null || pmOptions.equals("")) {
            return map;
        }
        String[] tokens = pmOptions.split(",");
        for (String token : tokens) {
            String[] pair = token.split("=");
            if (pair.length == 2) { // key=value setting
                pair[1] = (pair[1] == null ? "" : pair[1]);
                // ignore illegal settings
                if (pair[0].trim().length() > 0 && pair[1].trim().length() > 0)
                    map.put(pair[0], pair[1]);
            } else { // only key setting
                map.put(pair[0], "");
            }
        }
        return map;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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
        return ObjectUtils.objectsEqual(id, other.id);
    }
}
