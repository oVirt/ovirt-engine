package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class OpenstackNetworkProviderProperties extends TenantProviderProperties {

    private static final long serialVersionUID = -7470940167999871534L;

    private String pluginType;

    private AgentConfiguration agentConfiguration;

    private boolean readOnly = true;

    public boolean getReadOnly(){
        return readOnly;
    }

    public void setReadOnly(boolean entity) {
        this.readOnly = entity;
    }

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    /**
     * Convenience method to know if the plugin represented is Linux Bridge.
     *
     * @return <code>false</code> the Linux Bridge plugin is deprecated and replaced by ml2.
     */
    public final boolean isLinuxBridge() {
        return false;
    }

    /**
     * Convenience method to know if the plugin represented is Open vSwitch.
     *
     * @return <code>true</code> iff the plugin type represents a Open vSwitch plugin.
     */
    public final boolean isOpenVSwitch() {
        return OpenstackNetworkPluginType.OPEN_VSWITCH.name().equals(getPluginType());
    }

    public AgentConfiguration getAgentConfiguration() {
        return agentConfiguration;
    }

    public void setAgentConfiguration(AgentConfiguration agentConfiguration) {
        this.agentConfiguration = agentConfiguration;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                pluginType,
                agentConfiguration
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OpenstackNetworkProviderProperties)) {
            return false;
        }
        OpenstackNetworkProviderProperties other = (OpenstackNetworkProviderProperties) obj;
        return super.equals(obj)
                && Objects.equals(pluginType, other.pluginType)
                && Objects.equals(agentConfiguration, other.agentConfiguration);
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("pluginType", getPluginType());
    }

    public static class MessagingConfiguration implements Serializable {
        private static final long serialVersionUID = -8072430559946539586L;
        private BrokerType brokerType;
        private String address;
        private Integer port;
        private String username;
        private String password;

        public BrokerType getBrokerType() {
            return brokerType;
        }

        public void setBrokerType(BrokerType brokerType) {
            this.brokerType = brokerType;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    address,
                    port,
                    username,
                    password,
                    brokerType
            );
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof MessagingConfiguration)) {
                return false;
            }
            MessagingConfiguration other = (MessagingConfiguration) obj;
            return Objects.equals(address, other.address)
                    && Objects.equals(password, other.password)
                    && Objects.equals(port, other.port)
                    && Objects.equals(username, other.username)
                    && Objects.equals(brokerType, other.brokerType);
        }
    }

    public static class AgentConfiguration implements Serializable {
        private static final long serialVersionUID = -3588687921167640459L;
        private MessagingConfiguration messagingConfiguration;
        private String networkMappings;

        public MessagingConfiguration getMessagingConfiguration() {
            return messagingConfiguration;
        }

        public void setMessagingConfiguration(MessagingConfiguration messagingConfiguration) {
            this.messagingConfiguration = messagingConfiguration;
        }

        public String getNetworkMappings() {
            return networkMappings;
        }

        public void setNetworkMappings(String networkMappings) {
            this.networkMappings = networkMappings;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    networkMappings,
                    messagingConfiguration
            );
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AgentConfiguration)) {
                return false;
            }
            AgentConfiguration other = (AgentConfiguration) obj;
            return Objects.equals(networkMappings, other.networkMappings)
                    && Objects.equals(messagingConfiguration, other.messagingConfiguration);
        }
    }

    public enum BrokerType {
        QPID(
                "qpid_hostname",
                "qpid_port",
                "qpid_username",
                "qpid_password",
                "neutron.openstack.common.rpc.impl_qpid"),
        RABBIT_MQ(
                "rabbit_host",
                "rabbit_port",
                "rabbit_userid",
                "rabbit_password",
                "neutron.openstack.common.rpc.impl_kombu");

        private String hostKey;
        private String portKey;
        private String usernameKey;
        private String passwordKey;
        private String rpcBackendValue;

        private BrokerType(String hostKey, String portKey, String usernameKey, String passwordKey, String rpcBackend) {
            this.hostKey = hostKey;
            this.portKey = portKey;
            this.usernameKey = usernameKey;
            this.passwordKey = passwordKey;
            this.rpcBackendValue = rpcBackend;
        }

        public String getHostKey() {
            return hostKey;
        }

        public String getUsernameKey() {
            return usernameKey;
        }

        public String getPasswordKey() {
            return passwordKey;
        }

        public String getRpcBackendValue() {
            return rpcBackendValue;
        }

        public String getPortKey() {
            return portKey;
        }
    }
}
