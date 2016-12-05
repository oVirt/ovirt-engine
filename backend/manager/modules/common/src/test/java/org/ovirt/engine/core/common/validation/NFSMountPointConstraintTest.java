package org.ovirt.engine.core.common.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NFSMountPointConstraintTest {
    @Test
    public void isValid() {
        assertTrue(new NfsMountPointConstraint().isValid("192.168.0.1:/tmp/kakukk", null));
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
