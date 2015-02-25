package org.ovirt.engine.core.common.businessentities;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.annotation.HostnameOrIp;
import org.ovirt.engine.core.common.validation.group.PowerManagementCheck;
import org.ovirt.engine.core.compat.Guid;

public class FenceAgent implements BusinessEntity<Guid> {

    private static final long serialVersionUID = -5910560758520427911L;
    private Guid id;
    private Guid hostId;
    private int order;

    @EditableField
    private HashMap<String, String> optionsMap;

    @EditableField
    @Size(max = BusinessEntitiesDefinitions.HOST_IP_SIZE)
    @HostnameOrIp(message = "VALIDATION.VDS.POWER_MGMT.ADDRESS.HOSTNAME_OR_IP", groups = PowerManagementCheck.class)
    private String ip;

    @EditableField
    @Size(max = BusinessEntitiesDefinitions.HOST_PM_TYPE_SIZE)
    private String type;

    @EditableField
    @Size(max = BusinessEntitiesDefinitions.HOST_PM_USER_SIZE)
    private String user;

    @EditableField
    @Size(max = BusinessEntitiesDefinitions.HOST_PM_PASSWD_SIZE)
    private String password;

    @EditableField
    @Range(min = BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT,
            max = BusinessEntitiesDefinitions.NETWORK_MAX_LEGAL_PORT,
            message = "VALIDATION.VDS.PORT.RANGE")
    private Integer port;

    @EditableField
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String options;

    @EditableField
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
        optionsMap = pmOptionsStringToMap(options);
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public HashMap<String, String> getOptionsMap() {
        return optionsMap;
    }

    public void setOptionsMap(HashMap<String, String> value) {
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
     *
     * @param map
     * @return
     */
    public static String optionsMapToString(HashMap<String, String> map) {
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

    /**
     * Converts a PM Options string to a map.
     *
     * <b<Note:</b> A {@link HashMap} is used instead of the interface {@link Map}, as this method is used by the
     * frontend, and requires GWT compilation.
     *
     * @param pmOptions
     *            String representation of the map
     * @return A parsed map
     */
    public static HashMap<String, String> pmOptionsStringToMap(String pmOptions) {
        HashMap<String, String> map = new HashMap<String, String>();
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
                .append("options", getOptions())
                .build();
    }

    public static class FenceAgentOrderComparator implements Comparator<FenceAgent> {

        @Override
        public int compare(FenceAgent agent1, FenceAgent agent2) {
            return Integer.compare(agent1.getOrder(), agent2.getOrder());
        }
    }
}
