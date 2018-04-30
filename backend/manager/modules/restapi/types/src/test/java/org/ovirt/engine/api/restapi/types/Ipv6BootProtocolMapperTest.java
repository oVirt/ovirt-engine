package org.ovirt.engine.api.restapi.types;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;

public class Ipv6BootProtocolMapperTest {

    @Test
    public void testMapNullFromModelToBe() {
        assertThat(Ipv6BootProtocolMapper.map((BootProtocol) null), is((Ipv6BootProtocol) null));
    }

    @Test
    public void testMapNullFromBeToModel() {
        assertThat(Ipv6BootProtocolMapper.map((Ipv6BootProtocol) null), is((BootProtocol) null));
    }

    @Test
    public void testMapModelToBe() {
        assertThat(Ipv6BootProtocolMapper.map(BootProtocol.DHCP), is(Ipv6BootProtocol.DHCP));
    }

    @Test
    public void testMapBeToModel() {
        assertThat(Ipv6BootProtocolMapper.map(Ipv6BootProtocol.AUTOCONF), is(BootProtocol.AUTOCONF));
    }

    @Test
    public void testUnmappedEntityValue() {
        for (Ipv6BootProtocol value : Ipv6BootProtocol.values()) {
            assertThat(String.format("%s.%s is not mapped", Ipv6BootProtocol.class.getName(), value),
                    Ipv6BootProtocolMapper.map(value),
                    Matchers.notNullValue());
        }
    }

    @Test
    public void testUnmappedModelValue() {
        for (BootProtocol value : BootProtocol.values()) {
            assertThat(String.format("%s.%s is not mapped", BootProtocol.class.getName(), value),
                    Ipv6BootProtocolMapper.map(value),
                    Matchers.notNullValue());
        }
    }
}
