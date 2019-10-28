package org.ovirt.engine.core.bll;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.common.action.MacPoolParameters;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.MacRange;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.MacPoolDao;
import org.ovirt.engine.core.utils.InjectorExtension;

@ExtendWith({MockitoExtension.class, InjectorExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateMacPoolCommandTest {

    @Mock
    private MacPoolDao macPoolDao;
    @Mock
    private MacPoolPerCluster macPoolPerCluster;
    @InjectMocks
    UpdateMacPoolCommand command = new UpdateMacPoolCommand(new MacPoolParameters(new MacPool()), CommandContext.createContext(""));

    @Test
    public void testFirstParameterIsNotNull() {
        assertThrows(IllegalArgumentException.class,
                () -> UpdateMacPoolCommand.validateDefaultFlagIsNotChanged(null, new MacPool()));
    }

    @Test
    public void testSecondParameterIsNotNull() {
        assertThrows(IllegalArgumentException.class,
                () -> UpdateMacPoolCommand.validateDefaultFlagIsNotChanged(new MacPool(), null));
    }

    @Test
    public void testValidateDefaultFlagIsNotChangedWhenFlagChanged() {
        final MacPool macPool1 = new MacPool();
        final MacPool macPool2 = new MacPool();
        macPool2.setDefaultPool(!macPool1.isDefaultPool());

        assertThat(UpdateMacPoolCommand.validateDefaultFlagIsNotChanged(macPool1, macPool2),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_CHANGING_DEFAULT_MAC_POOL_IS_NOT_SUPPORTED));
    }

    @Test
    public void testValidateDefaultFlagIsNotChangedWhenFlagNotChanged() {
        final MacPool macPool1 = new MacPool();
        final MacPool macPool2 = new MacPool();
        assertThat(UpdateMacPoolCommand.validateDefaultFlagIsNotChanged(macPool1, macPool2), isValid());
    }

    /**
     * @return data stream for test with same name
     */
    static Stream<Object[]> testValidateOverlapWithOtherPools() {
        return parameterStreamForOverlapTests();
    }

    @ParameterizedTest
    @MethodSource
    public void testValidateOverlapWithOtherPools(
            String range1From, String range1To,
            String range2From, String range2To, boolean overlapping) {

        MacRange range1 = new MacRange();
        range1.setMacFrom(range1From);
        range1.setMacTo(range1To);

        MacRange range2 = new MacRange();
        range2.setMacFrom(range2From);
        range2.setMacTo(range2To);

        // set ranges in pool under validation
        command.getParameters().getMacPool().setRanges(Collections.singletonList(range1));
        // set ranges in cached pools
        MacPool macPool2 = new MacPool();
        macPool2.setId(Guid.newGuid());
        macPool2.setName("p2");
        macPool2.setRanges(Collections.singletonList(range2));
        when(macPoolDao.getAll()).thenReturn(Collections.singletonList(macPool2));

        // needed for some of the command validations that are not related to the overlap validation
        MacPool oldMacPool = new MacPool();
        oldMacPool.setId(Guid.newGuid());
        when(macPoolDao.get(any())).thenReturn(oldMacPool);
        when(macPoolPerCluster.isDuplicateMacAddressesAllowed(any())).thenReturn(false);

        assertEquals(!overlapping, command.validate());
    }

    /**
     * @return data stream for test with same name
     */
    static Stream<Object[]> testValidateOverlapWithinPool() {
        return parameterStreamForOverlapTests();
    }

    @ParameterizedTest
    @MethodSource
    public void testValidateOverlapWithinPool(
            String range1From, String range1To,
            String range2From, String range2To, boolean overlapping) {

        MacRange range1 = new MacRange();
        range1.setMacFrom(range1From);
        range1.setMacTo(range1To);

        MacRange range2 = new MacRange();
        range2.setMacFrom(range2From);
        range2.setMacTo(range2To);

        // set ranges in pool under validation
        command.getParameters().getMacPool().setRanges(Arrays.asList(range1, range2));

        // needed for some of the command validations that are not related to the overlap validation
        MacPool oldMacPool = new MacPool();
        oldMacPool.setId(Guid.newGuid());
        when(macPoolDao.get(any())).thenReturn(oldMacPool);
        when(macPoolPerCluster.isDuplicateMacAddressesAllowed(any())).thenReturn(false);
        when(macPoolDao.getAll()).thenReturn(Collections.emptyList());

        assertEquals(!overlapping, command.validate());
    }

    /**
     * @return data stream for test with same name
     */
    static Stream<Object[]> parameterStreamForOverlapTests() {
        return Stream.of(
                // array of [range1 from, range1 to, range2 from, range2 to, are overlapping]
                new Object[] { "ff::05", "ff::08", "ff::05", "ff::08", true },
                new Object[] { "ff::05", "ff::08", "ff::09", "ff::10", false }
        );
    }
}
