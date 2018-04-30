package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Rate;
import org.ovirt.engine.api.model.RngDevice;
import org.ovirt.engine.api.model.RngSource;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;


public class RngDeviceMapperTest {

    @Test
    public void testMapFromBackendToRest() {
        VmRngDevice entity = new VmRngDevice();
        entity.setBytes(11);
        entity.setPeriod(10);
        entity.setSource(VmRngDevice.Source.RANDOM);

        RngDevice expected = new RngDevice();
        expected.setRate(new Rate());
        expected.getRate().setBytes(11);
        expected.getRate().setPeriod(10);
        expected.setSource(RngSource.RANDOM);

        assertEquals(expected.getRate().getBytes(), RngDeviceMapper.map(entity, null).getRate().getBytes());
        assertEquals(expected.getRate().getPeriod(), RngDeviceMapper.map(entity, null).getRate().getPeriod());
        assertEquals(expected.getSource(), RngDeviceMapper.map(entity, null).getSource());
    }

    @Test
    public void testMapFromRestToBackend() {
        RngDevice model = new RngDevice();
        model.setSource(RngSource.HWRNG);
        model.setRate(new Rate());
        model.getRate().setBytes(10);
        model.getRate().setPeriod(11);

        VmRngDevice expected = new VmRngDevice();
        expected.setBytes(10);
        expected.setPeriod(11);
        expected.setSource(VmRngDevice.Source.HWRNG);

        assertEquals(expected, RngDeviceMapper.map(model, null));
    }
}
