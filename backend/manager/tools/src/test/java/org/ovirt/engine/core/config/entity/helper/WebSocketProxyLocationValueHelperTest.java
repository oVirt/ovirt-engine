package org.ovirt.engine.core.config.entity.helper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class WebSocketProxyLocationValueHelperTest {

    @Test
    public void testValidateNull() {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertFalse(helper.validate(null, null).isOk());
    }

    @Test
    public void testValidateEmpty() {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertFalse(helper.validate(null, "").isOk());
    }

    @Test
    public void testValidateIncomplete() {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertFalse(helper.validate(null, "myengine:").isOk());
    }

    @Test
    public void testNoColons() {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertFalse(helper.validate(null, "myengine-blah").isOk());
    }

    @Test
    public void testValidateNegativePort() {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertFalse(helper.validate(null, "myengine.com:-6100").isOk());
    }


    @Test
    public void testValidateNonIntPort() {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertFalse(helper.validate(null, "myengine.com:3.14156").isOk());
    }

    @Test
    public void testValidatePortTooBig() {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertFalse(helper.validate(null, "myengine.com:314156").isOk());
    }

    @Test
    public void testValidateHostPort() {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertTrue(helper.validate(null, "myengine.com:6100").isOk());
    }

    @Test
    public void testValidateIp4() {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertTrue(helper.validate(null, "192.168.1.1:6100").isOk());
    }

    @Test
    public void testValidateIp6Localhost() {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertTrue(helper.validate(null, "[::1]:6100").isOk());
    }

    @Test
    public void testValidateIp6() {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertTrue(helper.validate(null, "[2001:0db8:85a3:08d3:1319:8a2e:0370:7344]:1234").isOk());
    }

    @Test
    public void testValidateOff() {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertTrue(helper.validate(null, "Off").isOk());
    }

    @Test
    public void testValidateEngine() {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertTrue(helper.validate(null, "Engine:6100").isOk());
    }

    @Test
    public void testValidateHost() {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertTrue(helper.validate(null, "Host:6100").isOk());
    }

}
