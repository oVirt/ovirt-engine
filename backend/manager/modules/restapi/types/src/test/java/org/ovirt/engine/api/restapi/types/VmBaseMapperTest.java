package org.ovirt.engine.api.restapi.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.OriginType;

public class VmBaseMapperTest {

    @Test
    public void testMapOriginTypeRhev() {
        String s = VmBaseMapper.map(OriginType.RHEV, null);
        assertEquals("rhev", s);
        OriginType s2 = VmMapper.map(s, OriginType.RHEV);
        assertEquals(OriginType.RHEV, s2);
    }

    @Test
    public void testMapOriginTypeOvirt() {
        String s = VmBaseMapper.map(OriginType.OVIRT, null);
        assertEquals("ovirt", s);
        OriginType s2 = VmMapper.map(s, OriginType.OVIRT);
        assertEquals(OriginType.OVIRT, s2);
    }

}
