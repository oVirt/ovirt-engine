package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.HostnameOrIp;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.PowerManagementCheck;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class VdsStatic implements BusinessEntity<Guid> {

    private static final long serialVersionUID = -1425566208615075937L;
    private static final int HOST_DEFAULT_SPM_PRIORITY = 5;

    private Guid id;

    @Size(min = 1, max = BusinessEntitiesDefinitions.HOST_NAME_SIZE)
    @Pattern(regexp = ValidationUtils.NO_SPECIAL_CHARACTERS_WITH_DOT, message = "VALIDATION_VDS_NAME_INVALID", groups = {
            CreateEntity.class, UpdateEntity.class })
    private String name = ""; // GREGM prevents NPE

    @HostnameOrIp(message = "VALIDATION.VDS.POWER_MGMT.ADDRESS.HOSTNAME_OR_IP", groups = PowerManagementCheck.class)
    @NotNull(groups = PowerManagementCheck.class)
    @Size(max = BusinessEntitiesDefinitions.HOST_IP_SIZE)
    private String ip;

    @HostnameOrIp(message = "VALIDATION.VDS.CONSOLEADDRESSS.HOSTNAME_OR_IP",
            groups = { CreateEntity.class, UpdateEntity.class })
    @Size(max = BusinessEntitiesDefinitions.CONSOLE_ADDRESS_SIZE)
    private String consoleAddress;

    @Size(max = BusinessEntitiesDefinitions.HOST_UNIQUE_ID_SIZE)
    private String uniqueId;

    @HostnameOrIp(message = "VALIDATION.VDS.HOSTNAME.HOSTNAME_OR_IP",
            groups = { CreateEntity.class, UpdateEntity.class })
    @NotNull(groups = { CreateEntity.class, UpdateEntity.class })
    @Size(max = BusinessEntitiesDefinitions.HOST_HOSTNAME_SIZE)
    private String hostname;

    @Range(min = BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT,
            max = BusinessEntitiesDefinitions.NETWORK_MAX_LEGAL_PORT,
            message = "VALIDATION.VDS.PORT.RANGE")
    private int port;

    private Guid vdsGroupId;

    private Boolean serverSslEnabled;

    private VDSType vdsType = VDSType.VDS;

    private Integer vdsStrength;

    @Size(max = BusinessEntitiesDefinitions.HOST_PM_TYPE_SIZE)
    private String pmType;

    @Size(max = BusinessEntitiesDefinitions.HOST_PM_USER_SIZE)
    private String pmUser;

    @Size(max = BusinessEntitiesDefinitions.HOST_PM_PASSWD_SIZE)
    private String pmPassword;

    private Integer pmPort;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String pmOptions;

    private boolean pmEnabled;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    private String pmProxyPreferences;

    @HostnameOrIp(message = "VALIDATION.VDS.POWER_MGMT.ADDRESS.HOSTNAME_OR_IP", groups = PowerManagementCheck.class)
    @Size(max = BusinessEntitiesDefinitions.HOST_IP_SIZE)
    private String pmSecondaryIp;

    @Size(max = BusinessEntitiesDefinitions.HOST_PM_TYPE_SIZE)
    private String pmSecondaryType;

    @Size(max = BusinessEntitiesDefinitions.HOST_PM_USER_SIZE)
    private String pmSecondaryUser;

    @Size(max = BusinessEntitiesDefinitions.HOST_PM_PASSWD_SIZE)
    private String pmSecondaryPassword;

    private Integer pmSecondaryPort;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String pmSecondaryOptions;

    private boolean pmSecondaryConcurrent;

    private ValueObjectMap pmOptionsMap;

    private ValueObjectMap pmSecondaryOptionsMap;

    private long otpValidity;

    @Min(BusinessEntitiesDefinitions.HOST_MIN_SPM_PRIORITY)
    @Max(BusinessEntitiesDefinitions.HOST_MAX_SPM_PRIORITY)
    private int vdsSpmPriority;

    private boolean autoRecoverable = true;

    @Size(max = BusinessEntitiesDefinitions.SSH_KEY_FINGERPRINT_SIZE)
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
        this.setPmOptions("");
        this.setPmSecondaryOptions("");
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
        this.setVdsType(vds_type);
        this.setPmOptions("");
        this.setPmSecondaryOptions("");
        this.vdsSpmPriority = HOST_DEFAULT_SPM_PRIORITY;
    }

    public boolean isServerSslEnabled() {
        return serverSslEnabled;
    }

    public void setServerSslEnabled(boolean value) {
        serverSslEnabled = value;
    }

    public String getHostName() {
        return this.hostname;
    }

    public void setHostName(String value) {
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

    public int getPort() {
        return this.port;
    }

    public void setPort(int value) {
        this.port = value;
    }

    public Guid getVdsGroupId() {
        return this.vdsGroupId;
    }

    public void setVdsGroupId(Guid value) {
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

    public String getName() {
        return this.name;
    }

    public void setVdsName(String value) {
        this.name = value;
    }

    public VDSType getVdsType() {
        return this.vdsType;
    }

    public void setVdsType(VDSType value) {
        this.vdsType = value;
    }

    public int getVdsStrength() {
        return this.vdsStrength;
    }

    public void setVdsStrength(int value) {
        // strength should be between 1 and 100
        this.vdsStrength = value < 1 ? 1 : value > 100 ? 100 : value;
    }

    public String getPmType() {
        return pmType;
    }

    public void setPmType(String value) {
        pmType = value;
    }

    public String getPmUser() {
        return pmUser;
    }

    public void setPmUser(String value) {
        pmUser = value;
    }

    public String getPmPassword() {
        return pmPassword;
    }

    public void setPmPassword(String value) {
        pmPassword = value;
    }

    public Integer getPmPort() {
        return pmPort;
    }

    public void setPmPort(Integer value) {
        pmPort = value;
    }

    public String getPmOptions() {
        return pmOptions;
    }

    public void setPmOptions(String value) {
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

    public boolean isPmEnabled() {
        return pmEnabled;
    }

    public void setPmEnabled(boolean value) {
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

    public String getPmSecondaryUser() {
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

    public String getConsoleAddress() {
        return consoleAddress;
    }

    public void setConsoleAddress(String consoleAddress) {
        this.consoleAddress = consoleAddress;
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
        result = prime * result + ((consoleAddress == null) ? 0 : consoleAddress.hashCode());
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
        if (consoleAddress == null) {
            if (other.consoleAddress != null)
                return false;
        } else if (!consoleAddress.equals(other.consoleAddress))
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
