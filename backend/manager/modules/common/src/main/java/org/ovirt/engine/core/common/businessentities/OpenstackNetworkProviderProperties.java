package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class OpenstackNetworkProviderProperties extends TenantProviderProperties {

    private static final long serialVersionUID = -7470940167999871534L;

    private String pluginType;

    private AgentConfiguration agentConfiguration;

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
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getPluginType() == null) ? 0 : getPluginType().hashCode());
        result = prime * result + ((getAgentConfiguration() == null) ? 0 : getAgentConfiguration().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OpenstackNetworkProviderProperties)) {
            return false;
        }
        OpenstackNetworkProviderProperties other = (OpenstackNetworkProviderProperties) obj;
        if (!getPluginType().equals(other.getPluginType())) {
            return false;
        }
        if (getAgentConfiguration() == null) {
            if (other.getAgentConfiguration() != null) {
                return false;
            }
        } else if (!getAgentConfiguration().equals(other.getAgentConfiguration())) {
            return false;
        }
        return true;
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
            final int prime = 31;
            int result = 1;
            result = prime * result + ((getAddress() == null) ? 0 : getAddress().hashCode());
            result = prime * result + ((getPort() == null) ? 0 : getPort().hashCode());
            result = prime * result + ((getUsername() == null) ? 0 : getUsername().hashCode());
            result = prime * result + ((getPassword() == null) ? 0 : getPassword().hashCode());
            result = prime * result + ((getBrokerType() == null) ? 0 : getBrokerType().hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof MessagingConfiguration)) {
                return false;
            }
            MessagingConfiguration other = (MessagingConfiguration) obj;
            if (getAddress() == null) {
                if (other.getAddress() != null) {
                    return false;
                }
            } else if (!getAddress().equals(other.getAddress())) {
                return false;
            }
            if (getPassword() == null) {
                if (other.getPassword() != null) {
                    return false;
                }
            } else if (!getPassword().equals(other.getPassword())) {
                return false;
            }
            if (getPort() == null) {
                if (other.getPort() != null) {
                    return false;
                }
            } else if (!getPort().equals(other.getPort())) {
                return false;
            }
            if (getUsername() == null) {
                if (other.getUsername() != null) {
                    return false;
                }
            } else if (!getUsername().equals(other.getUsername())) {
                return false;
            }
            if (getBrokerType() == null) {
                if (other.getBrokerType() != null) {
                    return false;
                }
            } else if (!getBrokerType().equals(other.getBrokerType())) {
                return false;
            }
            return true;
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
            final int prime = 31;
            int result = 1;
            result = prime * result + ((getNetworkMappings() == null) ? 0 : getNetworkMappings().hashCode());
            result = prime * result + ((getMessagingConfiguration() == null) ? 0 : getMessagingConfiguration().hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof AgentConfiguration)) {
                return false;
            }
            AgentConfiguration other = (AgentConfiguration) obj;
            if (getNetworkMappings() == null) {
                if (other.getNetworkMappings() != null) {
                    return false;
                }
            } else if (!getNetworkMappings().equals(other.getNetworkMappings())) {
                return false;
            }
            if (getMessagingConfiguration() == null) {
                if (other.getMessagingConfiguration() != null) {
                    return false;
                }
            } else if (!getMessagingConfiguration().equals(other.getMessagingConfiguration())) {
                return false;
            }
            return true;
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
