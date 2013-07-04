package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

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
     * @return <code>true</code> iff the plugin type represents a Linux Bridge plugin.
     */
    public final boolean isLinuxBridge() {
        return OpenstackNetworkPluginType.LINUX_BRIDGE.name().equals(getPluginType());
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OpenstackNetworkProviderProperties [pluginType=")
                .append(getPluginType())
                .append(", tenantName=")
                .append(getTenantName())
                .append("]");
        return builder.toString();
    }

    public static class QpidConfiguration implements Serializable {
        private static final long serialVersionUID = -8072430559946539586L;
        private String address;
        private int port;
        private String username;
        private String password;

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
            if (!(obj instanceof QpidConfiguration)) {
                return false;
            }
            QpidConfiguration other = (QpidConfiguration) obj;
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
            return true;
        }
    }

    public static class AgentConfiguration implements Serializable {
        private static final long serialVersionUID = -3588687921167640459L;
        private QpidConfiguration qpidConfiguration;
        private String networkMappings;

        public QpidConfiguration getQpidConfiguration() {
            return qpidConfiguration;
        }

        public void setQpidConfiguration(QpidConfiguration qpidConfiguration) {
            this.qpidConfiguration = qpidConfiguration;
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
            result = prime * result + ((getQpidConfiguration() == null) ? 0 : getQpidConfiguration().hashCode());
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
            if (getQpidConfiguration() == null) {
                if (other.getQpidConfiguration() != null) {
                    return false;
                }
            } else if (!getQpidConfiguration().equals(other.getQpidConfiguration())) {
                return false;
            }
            return true;
        }
    }
}
