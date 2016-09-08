package org.ovirt.engine.core.config.entity.helper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RemoteViewerSupportedVersionsValueHelperTest {

    private RemoteViewerSupportedVersionsValueHelper helper = new RemoteViewerSupportedVersionsValueHelper();

    @Test
    public void emptyIsNotValid() {
        assertFalse(helper.validate(null, "").isOk());
    }

    @Test
    public void onePairIsValid() {
        assertTrue(helper.validate(null, "a:b").isOk());
    }

    @Test
    public void oneWithMissingVersionIsNotValid() {
        assertFalse(helper.validate(null, "a").isOk());
    }

    @Test
    public void oneWithMissingVersionButWithColonIsNotValid() {
        assertFalse(helper.validate(null, "a:").isOk());
        assertFalse(helper.validate(null, ":a").isOk());
    }

    @Test
    public void moreWithMissingVersionButWithColonIsNotValid() {
        assertFalse(helper.validate(null, "a:;b:").isOk());
        assertFalse(helper.validate(null, ":a;b:c").isOk());
    }

    @Test
    public void morevalid() {
        assertTrue(helper.validate(null, "linux:3.0;windows:2.5").isOk());
    }
}
