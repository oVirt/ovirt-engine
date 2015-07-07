package org.ovirt.engine.core.config.entity.helper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RemoteViewerSupportedVersionsValueHelperTest {

    private RemoteViewerSupportedVersionsValueHelper helper = new RemoteViewerSupportedVersionsValueHelper();

    @Test
    public void emptyIsNotValid() {
        assertEquals(helper.validate(null, "").isOk(), false);
    }

    @Test
    public void onePairIsValid() {
        assertEquals(helper.validate(null, "a:b").isOk(), true);
    }

    @Test
    public void oneWithMissingVersionIsNotValid() {
        assertEquals(helper.validate(null, "a").isOk(), false);
    }

    @Test
    public void oneWithMissingVersionButWithColonIsNotValid() {
        assertEquals(helper.validate(null, "a:").isOk(), false);
        assertEquals(helper.validate(null, ":a").isOk(), false);
    }

    @Test
    public void moreWithMissingVersionButWithColonIsNotValid() {
        assertEquals(helper.validate(null, "a:;b:").isOk(), false);
        assertEquals(helper.validate(null, ":a;b:c").isOk(), false);
    }

    @Test
    public void morevalid() {
        assertEquals(helper.validate(null, "linux:3.0;windows:2.5").isOk(), true);
    }
}
