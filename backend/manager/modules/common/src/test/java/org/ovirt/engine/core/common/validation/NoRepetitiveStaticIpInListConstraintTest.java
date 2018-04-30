package org.ovirt.engine.core.common.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.NoRepetitiveStaticIpInList;

public class NoRepetitiveStaticIpInListConstraintTest {

    private static final String IP_1 = "10.10.10.10";
    private static final String IP_2 = "11.11.11.11";

    private Validator validator;

    @BeforeEach
    public void initValidator() {
        validator = ValidationUtils.getValidator();
    }

    @Test
    public void twoNetworkInterfacesWithSameIp() {
        List<VdsNetworkInterface> listOfInterfaces = new ArrayList<>();
        listOfInterfaces.add(createVdsNetworkInterfaceWithStaticIp(IP_1));
        listOfInterfaces.add(createVdsNetworkInterfaceWithStaticIp(IP_1));
        validateAndAssertResult(listOfInterfaces, false);
    }

    @Test
    public void twoNetworkInterfacesWithDifferentIp() {
        List<VdsNetworkInterface> listOfInterfaces = new ArrayList<>();
        listOfInterfaces.add(createVdsNetworkInterfaceWithStaticIp(IP_1));
        listOfInterfaces.add(createVdsNetworkInterfaceWithStaticIp(IP_2));
        validateAndAssertResult(listOfInterfaces, true);
    }

    @Test
    public void twoNetworkInterfacesWithEmptyIp() {
        List<VdsNetworkInterface> listOfInterfaces = new ArrayList<>();
        listOfInterfaces.add(createVdsNetworkInterfaceWithStaticIp(""));
        listOfInterfaces.add(createVdsNetworkInterfaceWithStaticIp(""));
        validateAndAssertResult(listOfInterfaces, true);
    }

    @Test
    public void twoNetworkInterfacesWithNullIp() {
        List<VdsNetworkInterface> listOfInterfaces = new ArrayList<>();
        listOfInterfaces.add(createVdsNetworkInterfaceWithStaticIp(null));
        listOfInterfaces.add(createVdsNetworkInterfaceWithStaticIp(null));
        validateAndAssertResult(listOfInterfaces, true);
    }

    private void validateAndAssertResult(List<VdsNetworkInterface> listOfInterfaces, boolean isValid) {
        NoRepetitiveStaticIpInListContainer container = new NoRepetitiveStaticIpInListContainer(listOfInterfaces);
        Set<ConstraintViolation<NoRepetitiveStaticIpInListContainer>> validate = validator.validate(container);
        assertEquals(isValid, validate.isEmpty());
    }

    private static VdsNetworkInterface createVdsNetworkInterfaceWithStaticIp(String ip) {
        VdsNetworkInterface networkInterface = new VdsNetworkInterface();
        networkInterface.setIpv4Address(ip);
        networkInterface.setIpv4BootProtocol(Ipv4BootProtocol.STATIC_IP);
        return networkInterface;
    }

    private static class NoRepetitiveStaticIpInListContainer {
        @NoRepetitiveStaticIpInList
        private List<VdsNetworkInterface> value;

        public NoRepetitiveStaticIpInListContainer(List<VdsNetworkInterface> value) {
            this.value = value;
        }
    }
}
