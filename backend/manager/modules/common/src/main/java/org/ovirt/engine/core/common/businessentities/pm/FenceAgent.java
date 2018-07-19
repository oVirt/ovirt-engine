package org.ovirt.engine.core.common.businessentities.pm;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.EditableVdsField;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.utils.pm.PowerManagementUtils;
import org.ovirt.engine.core.common.validation.annotation.HostnameOrIp;
import org.ovirt.engine.core.common.validation.group.PowerManagementCheck;
import org.ovirt.engine.core.compat.Guid;

public class FenceAgent implements BusinessEntity<Guid> {

    private static final long serialVersionUID = -5910560758520427911L;
    private Guid id;
    private Guid hostId;
    private int order;

    @EditableVdsField
    private Map<String, String> optionsMap;

    @EditableVdsField
    @Size(max = BusinessEntitiesDefinitions.HOST_IP_SIZE)
    @HostnameOrIp(message = "VALIDATION_VDS_POWER_MGMT.ADDRESS_HOSTNAME_OR_IP", groups = PowerManagementCheck.class)
    private String ip;

    @EditableVdsField
    @Size(max = BusinessEntitiesDefinitions.HOST_PM_TYPE_SIZE)
    private String type;

    @EditableVdsField
    @Size(max = BusinessEntitiesDefinitions.HOST_PM_USER_SIZE)
    private String user;

    @EditableVdsField
    @Size(max = BusinessEntitiesDefinitions.HOST_PM_PASSWD_SIZE)
    private String password;

    @EditableVdsField
    @Range(min = BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT,
            max = BusinessEntitiesDefinitions.NETWORK_MAX_LEGAL_PORT,
            message = "VALIDATION_VDS_PORT_RANGE")
    private Integer port;

    @EditableVdsField
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String options;

    @EditableVdsField
    private boolean encryptOptions;

    public FenceAgent() {
        this(null);
    }

    public FenceAgent(FenceAgent other) {
        if (other != null) {
            this.id = other.id;
            this.hostId = other.hostId;
            this.order = other.order;
            this.type = other.type;
            this.ip = other.ip;
            this.port = other.port;
            this.user = other.user;
            this.password = other.password;
            this.encryptOptions = other.encryptOptions;
            this.options = other.options;
            if (other.optionsMap != null) {
                this.optionsMap = new HashMap<>();
                for (Map.Entry<String, String> entry : other.optionsMap.entrySet()) {
                    this.optionsMap.put(entry.getKey(), entry.getValue());
                }
            }

        }
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public Guid getHostId() {
        return hostId;
    }

    public void setHostId(Guid hostId) {
        this.hostId = hostId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
        optionsMap = PowerManagementUtils.pmOptionsStringToMap(options);
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Map<String, String> getOptionsMap() {
        return optionsMap;
    }

    public void setOptionsMap(Map<String, String> value) {
        optionsMap = value;
        options = optionsMapToString(value);
    }

    public boolean getEncryptOptions() {
        return encryptOptions;
    }

    public void setEncryptOptions(boolean value) {
        encryptOptions = value;
    }

    /**
     * Converts a PM Options map to string
     */
    public static String optionsMapToString(Map<String, String> map) {
        String result = "";
        String seperator = "";
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

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("hostId", getHostId())
                .append("order", getOrder())
                .append("type", getType())
                .append("ip", getIp())
                .append("port", getPort())
                .append("user", getUser())
                .appendFiltered("password", getPassword())
                .append("encryptOptions", getEncryptOptions())
                // options are separated by NEWLINE when sent to VDSM, but NEWLINE shouldn't be used in logs
                .append("options", getOptions() == null ? "" : getOptions().replace("\n", ", "))
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FenceAgent)) {
            return false;
        }
        FenceAgent other = (FenceAgent) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(hostId, other.hostId)
                && order == other.order
                && Objects.equals(type, other.type)
                && Objects.equals(ip, other.ip)
                && Objects.equals(port, other.port)
                && Objects.equals(user, other.user)
                && Objects.equals(password, other.password)
                && encryptOptions == other.encryptOptions
                && Objects.equals(options, other.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                hostId,
                order,
                type,
                ip,
                port,
                user,
                password,
                encryptOptions,
                options
        );
    }

    public static class FenceAgentOrderComparator implements Comparator<FenceAgent> {

        @Override
        public int compare(FenceAgent agent1, FenceAgent agent2) {
            return Integer.compare(agent1.getOrder(), agent2.getOrder());
        }
    }
}
