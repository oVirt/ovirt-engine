package org.ovirt.engine.core.config.entity.helper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WebSocketProxyLocationValueHelperTest {

    @Test
    public void testValidateNull() throws Exception {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertFalse(helper.validate(null, null).isOk());
    }

    @Test
    public void testValidateEmpty() throws Exception {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertFalse(helper.validate(null, "").isOk());
    }

    @Test
    public void testValidateIncomplete() throws Exception {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertFalse(helper.validate(null, "myengine:").isOk());
    }

    @Test
    public void testNoColons() throws Exception {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertFalse(helper.validate(null, "myengine-blah").isOk());
    }

    @Test
    public void testValidateNegativePort() throws Exception {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertFalse(helper.validate(null, "myengine.com:-6100").isOk());
    }


    @Test
    public void testValidateNonIntPort() throws Exception {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertFalse(helper.validate(null, "myengine.com:3.14156").isOk());
    }

    @Test
    public void testValidatePortTooBig() throws Exception {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertFalse(helper.validate(null, "myengine.com:314156").isOk());
    }

    @Test
    public void testValidateHostPort() throws Exception {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertTrue(helper.validate(null, "myengine.com:6100").isOk());
    }

    @Test
    public void testValidateIp4() throws Exception {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertTrue(helper.validate(null, "192.168.1.1:6100").isOk());
    }

    @Test
    public void testValidateIp6Localhost() throws Exception {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertTrue(helper.validate(null, "[::1]:6100").isOk());
    }

    @Test
    public void testValidateIp6() throws Exception {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertTrue(helper.validate(null, "[2001:0db8:85a3:08d3:1319:8a2e:0370:7344]:1234").isOk());
    }

    @Test
    public void testValidateOff() throws Exception {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertTrue(helper.validate(null, "Off").isOk());
    }

    @Test
    public void testValidateEngine() throws Exception {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertTrue(helper.validate(null, "Engine:6100").isOk());
    }

    @Test
    public void testValidateHost() throws Exception {
        WebSocketProxyLocationValueHelper helper = new WebSocketProxyLocationValueHelper();
        assertTrue(helper.validate(null, "Host:6100").isOk());
    }

}
