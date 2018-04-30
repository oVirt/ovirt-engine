package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;

public class RngDeviceSpecParamsTest {

    @Test
    public void testGenerateFullSpecParams() {
        VmRngDevice dev = new VmRngDevice();
        dev.setBytes(12);
        dev.setPeriod(34);
        dev.setSource(VmRngDevice.Source.RANDOM);

        Map<String, Object> expectedParams = new HashMap<>();
        expectedParams.put("bytes", "12");
        expectedParams.put("period", "34");
        expectedParams.put("source", "random");

        assertEquals(expectedParams, dev.getSpecParams());
    }

    @Test
    public void testGenerateSpecParams() {
        VmRngDevice dev = new VmRngDevice();
        dev.setSource(VmRngDevice.Source.HWRNG);

        Map<String, Object> expectedParams = new HashMap<>();
        expectedParams.put("source", "hwrng");

        assertEquals(expectedParams, dev.getSpecParams());
    }

}
