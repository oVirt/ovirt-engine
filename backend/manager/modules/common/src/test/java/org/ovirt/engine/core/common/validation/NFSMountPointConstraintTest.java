package org.ovirt.engine.core.common.validation;

import org.junit.Assert;
import org.junit.Test;

public class NFSMountPointConstraintTest {
    @Test
    public void isValid() {
        Assert.assertTrue(new NfsMountPointConstraint().isValid("192.168.0.1:/tmp/kakukk", null));
        Assert.assertTrue(new NfsMountPointConstraint().isValid("[1:2:3:4:5:6:7:8]:/tmp/kakukk", null));
        Assert.assertTrue(new NfsMountPointConstraint().isValid("[1::2]:/tmp/kakukk", null));
        Assert.assertTrue(new NfsMountPointConstraint().isValid("foo.example.com:/tmp/kakukk", null));
        Assert.assertTrue(new NfsMountPointConstraint().isValid("f0o.example.com:/tmp/kakukk", null));
        Assert.assertTrue(new NfsMountPointConstraint().isValid("storageserver:/tmp/kakukk", null));
        Assert.assertTrue(new NfsMountPointConstraint().isValid("st0rageserver:/tmp/kakukk", null));
        Assert.assertTrue(new NfsMountPointConstraint().isValid("s:/tmp/kakukk", null));
        Assert.assertTrue(new NfsMountPointConstraint().isValid("s.foobar:/tmp/kakukk", null));
        Assert.assertTrue(new NfsMountPointConstraint().isValid("0s:/tmp/kakukk", null));
        Assert.assertTrue(new NfsMountPointConstraint().isValid("f0o.example.f4:/tmp/kakukk", null));

        Assert.assertFalse(new NfsMountPointConstraint().isValid("", null));
        Assert.assertFalse(new NfsMountPointConstraint().isValid("/tmp/kakukk", null));
        Assert.assertFalse(new NfsMountPointConstraint().isValid(":/tmp/kakukk", null));
        Assert.assertFalse(new NfsMountPointConstraint().isValid(".:/tmp/kakukk", null));
        Assert.assertFalse(new NfsMountPointConstraint().isValid(".example.com:/tmp/kakukk", null));
        Assert.assertFalse(new NfsMountPointConstraint().isValid("-example.com:/tmp/kakukk", null));
    }
}
