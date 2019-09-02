package org.ovirt.engine.core.common.utils.ansible;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AnsibleCommandConfigTest {

    @ParameterizedTest
    @CsvSource({
            "1.2.3.4, 22, 1.2.3.4:22",
            "1::33:44, 22, [1::33:44]:22",
            "www.ovirt.org, 22, www.ovirt.org:22"
    })
    public void testFormatHostPort(String host, int port, String result) {
        assertEquals(result, AnsibleCommandConfig.Builder.formatHostPort(host, port));
    }
}
