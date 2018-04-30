package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collections;
import java.util.Set;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.InjectorExtension;

@ExtendWith({MockitoExtension.class, InjectorExtension.class })
public class VmWatchdogValidatorTest {

    private static final Set<VmWatchdogType> WATCHDOG_MODELS = Collections.singleton(VmWatchdogType.i6300esb);

    @Test
    public void i6300esbVmWatchdogTypeWhenIsCompatibleWithOs() {
        isModelCompatibleWithOsTest(isValid(), VmWatchdogType.i6300esb);
    }

    private void isModelCompatibleWithOsTest(Matcher<ValidationResult> matcher, VmWatchdogType watchDogModel) {
        Version version = new Version();
        VmWatchdog vmWatchdog = new VmWatchdog();
        vmWatchdog.setModel(watchDogModel);
        VmWatchdogValidator.VmWatchdogClusterDependentValidator validator = spy(new VmWatchdogValidator.VmWatchdogClusterDependentValidator(0, vmWatchdog, version));
        OsRepository osRepository = mock(OsRepository.class);

        when(validator.getOsRepository()).thenReturn(osRepository);
        when(osRepository.getVmWatchdogTypes(anyInt(), any())).thenReturn(WATCHDOG_MODELS);

        assertThat(validator.isValid(), matcher);
    }

}
