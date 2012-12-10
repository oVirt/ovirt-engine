package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.validator.constraints.Range;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.HostnameOrIp;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.PowerManagementCheck;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;

@Entity
@Table(name = "vds_static")
@TypeDef(name = "guid", typeClass = GuidType.class)
@NamedQueries(
        value = {
                @NamedQuery(name = "all_vds_static_for_vds_group_without_migration",
                        query = "select s from VdsStatic s, VdsDynamic d, VmDynamic v where " +
                                "(s.vdsGroupId = :vds_group_id) and " +
                                "(d.id = s.id) and " +
                                "(d.status = 3) and " +
                                "(v.status in (5, 6, 11, 12)) and " +
                                "(s.id != v.run_on_vds)")
        })
public class VdsStatic implements INotifyPropertyChanged, BusinessEntity<Guid> {

    private static final long serialVersionUID = -1425566208615075937L;
    private final int HOST_DEFAULT_SPM_PRIORITY = 5;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "Id")
    @Type(type = "guid")
    private Guid id;

    @Size(min = 1, max = BusinessEntitiesDefinitions.HOST_NAME_SIZE)
    @Pattern(regexp = ValidationUtils.NO_SPECIAL_CHARACTERS_WITH_DOT, message = "VALIDATION_VDS_NAME_INVALID", groups = {
            CreateEntity.class, UpdateEntity.class })
    @Column(name = "vds_name")
    private String name = ""; // GREGM prevents NPE

    @HostnameOrIp(message = "VALIDATION.VDS.POWER_MGMT.ADDRESS.HOSTNAME_OR_IP", groups = PowerManagementCheck.class)
    @NotNull(groups = PowerManagementCheck.class)
    @Size(max = BusinessEntitiesDefinitions.HOST_IP_SIZE)
    @Column(name = "ip")
    private String ip;

    @Size(max = BusinessEntitiesDefinitions.HOST_UNIQUE_ID_SIZE)
    @Column(name = "vds_unique_id")
    private String uniqueId;

    @HostnameOrIp(message = "VALIDATION.VDS.HOSTNAME.HOSTNAME_OR_IP",
            groups = { CreateEntity.class, UpdateEntity.class })
    @NotNull(groups = { CreateEntity.class, UpdateEntity.class })
    @Size(max = BusinessEntitiesDefinitions.HOST_HOSTNAME_SIZE)
    @Column(name = "host_name", length = BusinessEntitiesDefinitions.HOST_HOSTNAME_SIZE)
    private String hostname;

    @Range(min = BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT,
            max = BusinessEntitiesDefinitions.NETWORK_MAX_LEGAL_PORT,
            message = "VALIDATION.VDS.PORT.RANGE")
    @Column(name = "port")
    private int port;

    @Column(name = "vds_group_id")
    @Type(type = "guid")
    private Guid vdsGroupId;

    @Column(name = "server_ssl_enabled")
    private Boolean serverSslEnabled;

    @Column(name = "vds_type")
    private VDSType vdsType = VDSType.VDS;

    @Column(name = "vds_strength")
    private Integer vdsStrength;

    @Size(max = BusinessEntitiesDefinitions.HOST_PM_TYPE_SIZE)
    @Column(name = "pm_type")
    private String pmType;

    @Size(max = BusinessEntitiesDefinitions.HOST_PM_USER_SIZE)
    @Column(name = "pm_user")
    private String pmUser;

    @Size(max = BusinessEntitiesDefinitions.HOST_PM_PASSWD_SIZE)
    @Column(name = "pm_password")
    private String pmPassword;

    @Column(name = "pm_port")
    private Integer pmPort;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "pm_options", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String pmOptions;

    @Column(name = "pm_enabled")
    private boolean pmEnabled;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    @Column(name = "pm_proxy_preferences")
    private String pmProxyPreferences;

    @HostnameOrIp(message = "VALIDATION.VDS.POWER_MGMT.ADDRESS.HOSTNAME_OR_IP", groups = PowerManagementCheck.class)
    @Size(max = BusinessEntitiesDefinitions.HOST_IP_SIZE)
    @Column(name = "pm_secondary_ip")
    private String pmSecondaryIp;

    @Size(max = BusinessEntitiesDefinitions.HOST_PM_TYPE_SIZE)
    @Column(name = "pm_secondary_type")
    private String pmSecondaryType;

    @Size(max = BusinessEntitiesDefinitions.HOST_PM_USER_SIZE)
    @Column(name = "pm_secondary_user")
    private String pmSecondaryUser;

    @Size(max = BusinessEntitiesDefinitions.HOST_PM_PASSWD_SIZE)
    @Column(name = "pm_secondary_password")
    private String pmSecondaryPassword;

    @Column(name = "pm_secondary_port")
    private Integer pmSecondaryPort;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "pm_secondary_options", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String pmSecondaryOptions;

    @Column(name = "pm_secondary_concurrent")
    private boolean pmSecondaryConcurrent;

    @Transient
    private ValueObjectMap pmOptionsMap;

    @Transient
    private ValueObjectMap pmSecondaryOptionsMap;

    @Column(name = "otp_validity")
    private long otpValidity;

    @Min(BusinessEntitiesDefinitions.HOST_MIN_SPM_PRIORITY)
    @Max(BusinessEntitiesDefinitions.HOST_MAX_SPM_PRIORITY)
    @Column(name = "vds_spm_priority")
    private int vdsSpmPriority;

    private boolean autoRecoverable = true;

    @Size(max = BusinessEntitiesDefinitions.SSH_KEY_FINGERPRINT_SIZE)
    @Column(name = "sshKeyFingerprint")
    private String sshKeyFingerprint;

    public boolean isAutoRecoverable() {
        return autoRecoverable;
    }

    public void setAutoRecoverable(boolean autoRecoverable) {
        this.autoRecoverable = autoRecoverable;
    }

    public VdsStatic() {
        serverSslEnabled = false;
        vdsStrength = 100;
        this.setpm_options("");
        this.vdsSpmPriority = HOST_DEFAULT_SPM_PRIORITY;
    }

    public VdsStatic(String host_name, String ip, String uniqueId, int port, Guid vds_group_id, Guid vds_id,
            String vds_name, boolean server_SSL_enabled, VDSType vds_type) {
        serverSslEnabled = false;
        vdsStrength = 100;
        this.hostname = host_name;
        this.ip = ip;
        this.uniqueId = uniqueId;
        this.port = port;
        this.vdsGroupId = vds_group_id;
        this.id = vds_id;
        this.name = vds_name;
        this.serverSslEnabled = server_SSL_enabled;
        this.setvds_type(vds_type);
        this.setpm_options("");
        this.setPmSecondaryOptions("");
        this.vdsSpmPriority = HOST_DEFAULT_SPM_PRIORITY;
    }

    public boolean getserver_SSL_enabled() {
        return serverSslEnabled;
    }

    public void setserver_SSL_enabled(boolean value) {
        serverSslEnabled = value;
    }

    public String gethost_name() {
        return this.hostname;
    }

    public void sethost_name(String value) {
        this.hostname = value;
    }

    public String getManagmentIp() {
        return this.ip;
    }

    public void setManagmentIp(String value) {
        this.ip = value;
    }

    public String getUniqueID() {
        return uniqueId;
    }

    public void setUniqueID(String value) {
        uniqueId = value;
    }

    public int getport() {
        return this.port;
    }

    public void setport(int value) {
        this.port = value;
    }

    public Guid getvds_group_id() {
        return this.vdsGroupId;
    }

    public void setvds_group_id(Guid value) {
        this.vdsGroupId = value;
    }

    @Override
    public Guid getId() {
        return this.id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public String getvds_name() {
        return this.name;
    }

    public void setvds_name(String value) {
        this.name = value;
    }

    public VDSType getvds_type() {
        return this.vdsType;
    }

    public void setvds_type(VDSType value) {
        this.vdsType = value;
    }

    public int getvds_strength() {
        return this.vdsStrength;
    }

    public void setvds_strength(int value) {
        // strength should be between 1 and 100
        this.vdsStrength = value < 1 ? 1 : value > 100 ? 100 : value;
    }

    public String getpm_type() {
        return pmType;
    }

    public void setpm_type(String value) {
        pmType = value;
    }

    public String getpm_user() {
        return pmUser;
    }

    public void setpm_user(String value) {
        pmUser = value;
    }

    public String getpm_password() {
        return pmPassword;
    }

    public void setpm_password(String value) {
        pmPassword = value;
    }

    public Integer getpm_port() {
        return pmPort;
    }

    public void setpm_port(Integer value) {
        pmPort = value;
    }

    public String getpm_options() {
        return pmOptions;
    }

    public void setpm_options(String value) {
        pmOptions = value;
        // set pmOptionsMap value content to match the given string.
        pmOptionsMap = PmOptionsStringToMap(value);
    }

    public ValueObjectMap getPmOptionsMap() {
        return pmOptionsMap;
    }

    public void setPmOptionsMap(ValueObjectMap value) {
        pmOptionsMap = value;
        pmOptions = PmOptionsMapToString(value);
    }

    public boolean getpm_enabled() {
        return pmEnabled;
    }

    public void setpm_enabled(boolean value) {
        pmEnabled = value;
    }

    public String getPmProxyPreferences() {
        return pmProxyPreferences;
    }

    public void setPmProxyPreferences(String pmProxyPreferences) {
        this.pmProxyPreferences = pmProxyPreferences;
    }
    public String getPmSecondaryIp() {
        return this.pmSecondaryIp;
    }

    public void setPmSecondaryIp(String value) {
        this.pmSecondaryIp = value;
    }

    public String getPmSecondaryType() {
        return pmSecondaryType;
    }

    public void setPmSecondaryType(String value) {
        pmSecondaryType = value;
    }

    public String getPmSecondaryuser() {
        return pmSecondaryUser;
    }

    public void setPmSecondaryUser(String value) {
        pmSecondaryUser = value;
    }

    public String getPmSecondaryPassword() {
        return pmSecondaryPassword;
    }

    public void setPmSecondaryPassword(String value) {
        pmSecondaryPassword = value;
    }

    public Integer getPmSecondaryPort() {
        return pmSecondaryPort;
    }

    public void setPmSecondaryPort(Integer value) {
        pmSecondaryPort = value;
    }

    public String getPmSecondaryOptions() {
        return pmSecondaryOptions;
    }

    public void setPmSecondaryOptions(String value) {
        pmSecondaryOptions = value;
        // set pmSecondaryOptionsMap value content to match the given string.
        pmSecondaryOptionsMap = PmOptionsStringToMap(value);
    }

    public boolean isPmSecondaryConcurrent() {
        return pmSecondaryConcurrent;
    }

    public void setPmSecondaryConcurrent(boolean value) {
        pmSecondaryConcurrent = value;
    }

    public ValueObjectMap getPmSecondaryOptionsMap() {
        return pmSecondaryOptionsMap;
    }

    public void setPmSecondaryOptionsMap(ValueObjectMap value) {
        pmSecondaryOptionsMap = value;
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
        this.vdsSpmPriority = value;
    }

    public String getSSHKeyFingerprint() {
        return sshKeyFingerprint;
    }

    public void setSSHKeyFingerprint(String sshKeyFingerprint) {
        this.sshKeyFingerprint = sshKeyFingerprint;
    }

    /**
     * Converts a PM Options map to string
     *
     * @param map
     * @return
     */
    public static String PmOptionsMapToString(ValueObjectMap optionsMap) {
        String result = "";
        String seperator = "";
        Map map = optionsMap.asMap();
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pairs = it.next();
            String value = pairs.getValue();
            result +=
                    seperator + pairs.getKey()
                            + ((value != null && value.length() > 0) ? "=" + value : "");
            seperator = ",";
        }
        return result;
    }

    /**
     * Converts a PM Options string to a map
     *
     * @param pmOptions
     * @return
     */
    public static ValueObjectMap PmOptionsStringToMap(String pmOptions) {
        if (pmOptions == null || pmOptions.equals("")) {
            return new ValueObjectMap();
        }
        HashMap<String, String> map = new HashMap<String, String>();
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
        return new ValueObjectMap(map, false);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (int) (otpValidity ^ (otpValidity >>> 32));
        result = prime * result + (pmEnabled ? 1231 : 1237);
        result = prime * result + ((pmOptions == null) ? 0 : pmOptions.hashCode());
        result = prime * result + ((pmOptionsMap == null) ? 0 : pmOptionsMap.hashCode());
        result = prime * result + ((pmPassword == null) ? 0 : pmPassword.hashCode());
        result = prime * result + ((pmPort == null) ? 0 : pmPort.hashCode());
        result = prime * result + ((pmType == null) ? 0 : pmType.hashCode());
        result = prime * result + ((pmUser == null) ? 0 : pmUser.hashCode());
        result = prime * result + ((pmSecondaryIp == null) ? 0 : ip.hashCode());
        result = prime * result + (pmSecondaryConcurrent ? 1231 : 1237);
        result = prime * result + ((pmSecondaryOptions == null) ? 0 : pmOptions.hashCode());
        result = prime * result + ((pmSecondaryOptionsMap == null) ? 0 : pmSecondaryOptionsMap.hashCode());
        result = prime * result + ((pmSecondaryPassword == null) ? 0 : pmSecondaryPassword.hashCode());
        result = prime * result + ((pmSecondaryPort == null) ? 0 : pmSecondaryPort.hashCode());
        result = prime * result + ((pmSecondaryType == null) ? 0 : pmSecondaryType.hashCode());
        result = prime * result + ((pmSecondaryUser == null) ? 0 : pmSecondaryUser.hashCode());
        result = prime * result + port;
        result = prime * result + ((serverSslEnabled == null) ? 0 : serverSslEnabled.hashCode());
        result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
        result = prime * result + ((vdsGroupId == null) ? 0 : vdsGroupId.hashCode());
        result = prime * result + ((vdsStrength == null) ? 0 : vdsStrength.hashCode());
        result = prime * result + ((vdsType == null) ? 0 : vdsType.hashCode());
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
        VdsStatic other = (VdsStatic) obj;
        if (hostname == null) {
            if (other.hostname != null)
                return false;
        } else if (!hostname.equals(other.hostname))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (ip == null) {
            if (other.ip != null)
                return false;
        } else if (!ip.equals(other.ip))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (otpValidity != other.otpValidity)
            return false;
        if (pmEnabled != other.pmEnabled)
            return false;
        if (pmOptions == null) {
            if (other.pmOptions != null)
                return false;
        } else if (!pmOptions.equals(other.pmOptions))
            return false;
        if (pmOptionsMap == null) {
            if (other.pmOptionsMap != null)
                return false;
        } else if (!pmOptionsMap.equals(other.pmOptionsMap))
            return false;
        if (pmPassword == null) {
            if (other.pmPassword != null)
                return false;
        } else if (!pmPassword.equals(other.pmPassword))
            return false;
        if (pmPort == null) {
            if (other.pmPort != null)
                return false;
        } else if (!pmPort.equals(other.pmPort))
            return false;
        if (pmType == null) {
            if (other.pmType != null)
                return false;
        } else if (!pmType.equals(other.pmType))
            return false;
        if (pmUser == null) {
            if (other.pmUser != null)
                return false;
        } else if (!pmUser.equals(other.pmUser))
            return false;
        if (port != other.port)
            return false;
        if (serverSslEnabled == null) {
            if (other.serverSslEnabled != null)
                return false;
        } else if (!serverSslEnabled.equals(other.serverSslEnabled))
            return false;
        if (uniqueId == null) {
            if (other.uniqueId != null)
                return false;
        } else if (!uniqueId.equals(other.uniqueId))
            return false;
        if (vdsGroupId == null) {
            if (other.vdsGroupId != null)
                return false;
        } else if (!vdsGroupId.equals(other.vdsGroupId))
            return false;
        if (vdsStrength == null) {
            if (other.vdsStrength != null)
                return false;
        } else if (!vdsStrength.equals(other.vdsStrength))
            return false;
        if (vdsType != other.vdsType)
            return false;
        if (sshKeyFingerprint == null) {
            if (other.sshKeyFingerprint != null)
                return false;
        } else if (!sshKeyFingerprint.equals(other.sshKeyFingerprint))
            return false;
        if (pmSecondaryIp == null) {
            if (other.pmSecondaryIp != null)
                return false;
        } else if (!pmSecondaryIp.equals(other.pmSecondaryIp))
            return false;
        if (pmSecondaryConcurrent != other.pmSecondaryConcurrent)
            return false;
        if (pmSecondaryOptions == null) {
            if (other.pmSecondaryOptions != null)
                return false;
        } else if (!pmSecondaryOptions.equals(other.pmSecondaryOptions))
            return false;
        if (pmSecondaryOptionsMap == null) {
            if (other.pmSecondaryOptionsMap != null)
                return false;
        } else if (!pmSecondaryOptionsMap.equals(other.pmSecondaryOptionsMap))
            return false;
        if (pmSecondaryPassword == null) {
            if (other.pmSecondaryPassword != null)
                return false;
        } else if (!pmSecondaryPassword.equals(other.pmSecondaryPassword))
            return false;
        if (pmSecondaryPort == null) {
            if (other.pmSecondaryPort != null)
                return false;
        } else if (!pmSecondaryPort.equals(other.pmSecondaryPort))
            return false;
        if (pmSecondaryType == null) {
            if (other.pmSecondaryType != null)
                return false;
        } else if (!pmSecondaryType.equals(other.pmSecondaryType))
            return false;
        if (pmSecondaryUser == null) {
            if (other.pmSecondaryUser != null)
                return false;
        } else if (!pmSecondaryUser.equals(other.pmSecondaryUser))
            return false;
        if (port != other.port)
            return false;

        return true;
    }

}
