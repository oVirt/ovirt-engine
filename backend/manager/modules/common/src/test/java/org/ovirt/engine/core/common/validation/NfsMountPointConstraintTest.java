package org.ovirt.engine.core.common.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class NfsMountPointConstraintTest {
    @Test
    public void isValid() {
        assertTrue(new NfsMountPointConstraint().isValid("192.168.0.1:/tmp/kakukk", null));
        assertTrue(new NfsMountPointConstraint().isValid("192.168.0.1:/tmp/kakukk/", null));
        assertTrue(new NfsMountPointConstraint().isValid("192.168.0.1:/", null));
        assertTrue(new NfsMountPointConstraint().isValid("[1:2:3:4:5:6:7:8]:/tmp/kakukk", null));
        assertTrue(new NfsMountPointConstraint().isValid("[1::2]:/tmp/kakukk", null));
        assertTrue(new NfsMountPointConstraint().isValid("foo.example.com:/tmp/kakukk", null));
        assertTrue(new NfsMountPointConstraint().isValid("f0o.example.com:/tmp/kakukk", null));
        assertTrue(new NfsMountPointConstraint().isValid("storageserver:/tmp/kakukk", null));
        assertTrue(new NfsMountPointConstraint().isValid("st0rageserver:/tmp/kakukk", null));
        assertTrue(new NfsMountPointConstraint().isValid("s:/tmp/kakukk", null));
        assertTrue(new NfsMountPointConstraint().isValid("s.foobar:/tmp/kakukk", null));
        assertTrue(new NfsMountPointConstraint().isValid("0s:/tmp/kakukk", null));
        assertTrue(new NfsMountPointConstraint().isValid("f0o.example.f4:/tmp/kakukk", null));
        assertTrue(new NfsMountPointConstraint().isValid("f0o.example.f4:/tmp/kakukk", null));
        assertTrue(new NfsMountPointConstraint().isValid("f0o.example.f4:/", null));

        assertFalse(new NfsMountPointConstraint().isValid("", null));
        assertFalse(new NfsMountPointConstraint().isValid("/tmp/kakukk", null));
        assertFalse(new NfsMountPointConstraint().isValid(":/tmp/kakukk", null));
        assertFalse(new NfsMountPointConstraint().isValid(".:/tmp/kakukk", null));
        assertFalse(new NfsMountPointConstraint().isValid(".example.com:/tmp/kakukk", null));
        assertFalse(new NfsMountPointConstraint().isValid("-example.com:/tmp/kakukk", null));
        assertFalse(new NfsMountPointConstraint().isValid("192.168.1.1:/h ome", null));
        assertFalse(new NfsMountPointConstraint().isValid("192.168.1.1:/home ", null));
        assertFalse(new NfsMountPointConstraint().isValid(" 192.168.1.1:/home", null));
    }
}
