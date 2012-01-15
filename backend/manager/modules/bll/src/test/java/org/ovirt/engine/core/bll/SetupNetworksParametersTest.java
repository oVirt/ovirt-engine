package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.businessentities.NetworkBootProtocol.StaticIp;

import java.util.Arrays;

import javax.validation.Validator;

import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.ValidationUtils;

public class SetupNetworksParametersTest extends BaseMockitoTest {

    @Test
    public void validateParameters() {
        SetupNetworksParameters params = new SetupNetworksParameters();
        VdsNetworkInterface nic = new VdsNetworkInterface();
        params.setInterfaces(Arrays.asList(nic));

        nic.setNetworkName("otherThenMgmtNetwork");
        nic.setGateway("1.1.1.1");

        Validator validator = ValidationUtils.getValidator();

        Assert.assertFalse(validator.validate(params).isEmpty());

        nic.setGateway("");
        nic.setBootProtocol(StaticIp);
        nic.setAddress("1.1.1.1");

        Assert.assertTrue(validator.validate(params).isEmpty());

        params.setConectivityTimeout(-1);

        assertTrue(validator.validate(params).size() == 1);
    }
}
