package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol.STATIC_IP;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Arrays;

import javax.validation.Validator;

import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.utils.MockConfigRule;

public class SetupNetworksParametersTest {

    @ClassRule
    public static MockConfigRule mcr =
            new MockConfigRule(mockConfig(ConfigValues.NetworkConnectivityCheckTimeoutInSeconds, 100));

    @Test
    public void validateParameters() {
        SetupNetworksParameters params = new SetupNetworksParameters();
        VdsNetworkInterface nic = new VdsNetworkInterface();
        params.setInterfaces(Arrays.asList(nic));

        nic.setName("nic");
        nic.setNetworkName("otherThenMgmtNetwork");
        nic.setGateway("1.1.1.1");

        Validator validator = ValidationUtils.getValidator();

        assertTrue(validator.validate(params).isEmpty());

        nic.setGateway("");
        nic.setBootProtocol(STATIC_IP);
        nic.setAddress("1.1.1.1");

        assertTrue(validator.validate(params).isEmpty());

        params.setConectivityTimeout(-1);

        assertTrue(validator.validate(params).size() == 1);
    }
}
