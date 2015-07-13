package org.ovirt.engine.core.common.action;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatic;

public class VdsOperationActionParameters extends VdsActionParameters {
    private static final long serialVersionUID = 4156122527623908516L;

    @Valid
    private VdsStatic vdsStatic;
    private String password;
    private boolean overrideFirewall;
    private boolean activateHost;
    private boolean enableSerialConsole;
    private AuthenticationMethod authMethod;
    private String networkMappings;
    private HostedEngineDeployConfiguration hostedEngineDeployConfiguration;

    public enum AuthenticationMethod {
        Password(0),
        PublicKey(1);

        private int intValue;
        private static Map<Integer, AuthenticationMethod> mappings;

        static {
            mappings = new HashMap<>();
            for (AuthenticationMethod error : values()) {
                mappings.put(error.getValue(), error);
            }
        }

        private AuthenticationMethod(int value) {
            intValue = value;
        }

        public int getValue() {
            return intValue;
        }

        public static AuthenticationMethod forValue(int value) {
            return mappings.get(value);
        }
    }

    public VdsOperationActionParameters(VdsStatic vdsStaticVal, String passwordVal) {
        super(vdsStaticVal.getId());
        vdsStatic = vdsStaticVal;
        password = passwordVal;
        authMethod = AuthenticationMethod.Password;
        enableSerialConsole = true;
    }

    public VdsOperationActionParameters(VdsStatic vdsStatic) {
        this(vdsStatic, null);
    }

    public VdsOperationActionParameters() {
        authMethod = AuthenticationMethod.Password;
        enableSerialConsole = true;
    }

    public VdsStatic getVdsStaticData() {
        return vdsStatic;
    }

    @ShouldNotBeLogged
    public String getPassword() {
        return password;
    }

    public void setPassword(String value) {
        password = value;
    }

    public void setAuthMethod(AuthenticationMethod value) {
        authMethod = value;
    }

    public AuthenticationMethod getAuthMethod() {
        return authMethod;
    }

    // Deprecated to keep old api with root password
    @ShouldNotBeLogged
    public String getRootPassword() {
        return password;
    }

    public void setRootPassword(String value) {
        password = value;
    }

    public VDS getvds() {
        VDS vds = new VDS();
        vds.setStaticData(vdsStatic);
        return vds;
    }

    public void setvds(VDS value) {
        vdsStatic = value.getStaticData();
    }

    public void setOverrideFirewall(boolean overrideFirewall) {
        this.overrideFirewall = overrideFirewall;
    }

    public boolean getOverrideFirewall() {
        return overrideFirewall;
    }

    public void setActivateHost(boolean activateHost) {
        this.activateHost = activateHost;
    }

    public boolean getActivateHost() {
        return activateHost;
    }

    public String getNetworkMappings() {
        return networkMappings;
    }

    public void setNetworkMappings(String networkMappings) {
        this.networkMappings = networkMappings;
    }

    public void setEnableSerialConsole(boolean enableSerialConsole) {
        this.enableSerialConsole = enableSerialConsole;
    }

    public boolean getEnableSerialConsole() {
        return enableSerialConsole;
    }

    public HostedEngineDeployConfiguration getHostedEngineDeployConfiguration() {
        return hostedEngineDeployConfiguration;
    }

    public void setHostedEngineDeployConfiguration(HostedEngineDeployConfiguration hostedEngineDeployConfiguration) {
        this.hostedEngineDeployConfiguration = hostedEngineDeployConfiguration;
    }
}
