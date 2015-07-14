package org.ovirt.engine.api.restapi.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.OriginType;

public class VmBaseMapperTest {

    @Test
    public void testMapOriginTypeRhev() {
        String s = VmBaseMapper.map(OriginType.RHEV, null);
        assertEquals(s, "rhev");
        OriginType s2 = VmMapper.map(s, OriginType.RHEV);
        assertEquals(s2, OriginType.RHEV);
    }

    @Test
    public void testMapOriginTypeOvirt() {
        String s = VmBaseMapper.map(OriginType.OVIRT, null);
        assertEquals(s, "ovirt");
        OriginType s2 = VmMapper.map(s, OriginType.OVIRT);
        assertEquals(s2, OriginType.OVIRT);
    }

}
