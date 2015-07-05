package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.gluster.GlusterVolumeValidator;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class GlusterVolumeValidatorTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.GlusterSupportForceCreateVolume, Version.v3_1.getValue(), false),
            mockConfig(ConfigValues.GlusterSupportForceCreateVolume, Version.v3_3.getValue(), true));

    GlusterVolumeValidator validator;

    @Test
    public void withoutForceSucceedsIn31Cluster() {
        validator = spy(new GlusterVolumeValidator());
        ValidationResult result = validator.isForceCreateVolumeAllowed(Version.v3_1, false);
        assertEquals(result, ValidationResult.VALID);
    }

    @Test
    public void withForceFailsIn31Cluster() {
        validator = spy(new GlusterVolumeValidator());
        ValidationResult result = validator.isForceCreateVolumeAllowed(Version.v3_1, true);
        assertEquals(result,
                new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_ADD_BRICK_FORCE_NOT_SUPPORTED));
    }

    @Test
    public void withoutForceSucceedsIn33Cluster() {
        validator = spy(new GlusterVolumeValidator());
        ValidationResult result = validator.isForceCreateVolumeAllowed(Version.v3_3, false);
        assertEquals(result, ValidationResult.VALID);
    }

    @Test
    public void withForceFailsIn33Cluster() {
        validator = spy(new GlusterVolumeValidator());
        ValidationResult result = validator.isForceCreateVolumeAllowed(Version.v3_3, true);
        assertEquals(result, ValidationResult.VALID);
    }
}
