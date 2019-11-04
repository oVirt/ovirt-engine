package org.ovirt.engine.core.bll;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.MacRange;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.utils.InjectedMock;

public class MacPoolValidatorTest extends BaseCommandTest {

    private final MacPool macPool = new MacPool();

    private MacPoolValidator macPoolValidator;

    @Mock
    @InjectedMock
    public ClusterDao clusterDao;

    @BeforeEach
    public void setUp() {
        this.macPoolValidator = createMacPoolValidator(macPool);
    }

    private MacPoolValidator createMacPoolValidator(MacPool macPool) {
        macPoolValidator = spy(new MacPoolValidator(Collections.singletonList(new MacPool()), macPool));
        return macPoolValidator;
    }

    @Test
    public void testDefaultPoolFlagIsNotSetValidUsage() {
        macPool.setDefaultPool(false);
        assertThat(macPoolValidator.defaultPoolFlagIsNotSet(),
                isValid());
    }

    @Test
    public void testDefaultPoolFlagIsNotSetInvalidUsage() {
        macPool.setDefaultPool(true);
        assertThat(macPoolValidator.defaultPoolFlagIsNotSet(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_SETTING_DEFAULT_MAC_POOL_IS_NOT_SUPPORTED));
    }

    @Test
    public void testHasUniqueNameUpdateNotChangingName() {
        final Guid macPoolId = Guid.newGuid();
        final String poolName = "macPool1";

        assertThat(callHasUniqueName(macPoolId, macPoolId, poolName, poolName),
                isValid());
    }

    @Test
    public void testHasUniqueNameRenamingPool() {
        final Guid macPoolId = Guid.newGuid();

        assertThat(callHasUniqueName(macPoolId, macPoolId, "macPool1", "macPool2"),
                isValid());
    }

    @Test
    public void testHasUniqueNameUsingExistingName() {
        final String macPoolName = "macPool1";

        assertThat(callHasUniqueName(Guid.newGuid(), Guid.newGuid(), macPoolName, macPoolName),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED));
    }

    @Test
    public void testHasUniqueNamePersistingNewRecord() {
        assertThat(callHasUniqueName(Guid.newGuid(), Guid.newGuid(), "macPool1", "macPool2"),
                isValid());
    }

    @Test
    public void testHasUniqueNamePersistingNewRecordWithNullId() {
        assertThat(callHasUniqueName(Guid.newGuid(), null, "macPool1", "whatever"),
                isValid());
    }

    @Test
    public void testHasUniqueNamePersistingNewRecordWithNullIdAndSameName() {
        final String macPoolName = "macPool1";

        assertThat(callHasUniqueName(Guid.newGuid(), null, macPoolName, macPoolName),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED));
    }

    private ValidationResult callHasUniqueName(Guid macPool1Id,
            Guid macPool2Id,
            String macPool1Name,
            String macPool2Name) {

        final MacPool existingMacPool = new MacPool();
        existingMacPool.setId(macPool1Id);
        existingMacPool.setName(macPool1Name);
        MacPoolValidator macPoolValidator = new MacPoolValidator(Collections.singletonList(existingMacPool), macPool);

        macPool.setId(macPool2Id);
        macPool.setName(macPool2Name);
        return macPoolValidator.hasUniqueName();
    }

    @Test
    public void testNotRemovingDefaultPool() {
        macPool.setDefaultPool(true);
        assertThat(macPoolValidator.notRemovingDefaultPool(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_DEFAULT_MAC_POOL));
    }

    @Test
    public void testNotRemovingDefaultPoolNonDefaultIsRemoved() {
        assertThat(macPoolValidator.notRemovingDefaultPool(), isValid());
    }

    @Test
    public void testNotRemovingUsedPoolRecordIsUsed() {
        macPool.setId(Guid.newGuid());
        final Cluster cluster = new Cluster();
        cluster.setName("cluster");
        when(clusterDao.getAllClustersByMacPoolId(macPool.getId()))
                .thenReturn(Collections.singletonList(cluster));

        assertThat(macPoolValidator.notRemovingUsedPool(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_STILL_USED_MAC_POOL));
    }

    @Test
    public void testNotRemovingUsedPoolRecordNotUsed() {
        macPool.setId(Guid.newGuid());

        assertThat(macPoolValidator.notRemovingUsedPool(), isValid());
    }

    @Test
    public void testMacPoolExistsEntityNotExist() {
        assertThat(createMacPoolValidator(null).macPoolExists(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MAC_POOL_DOES_NOT_EXIST));
    }

    @Test
    public void testMacPoolExistsEntityDoesExist() {
        assertThat(macPoolValidator.macPoolExists(), isValid());
    }


    /**
     * @return data stream for test with same name
     */
    static Stream<Object[]> testOverlappingRangesInPool() {
        return parameterStreamForOverlapTests();
    }

    @ParameterizedTest
    @MethodSource
    void testOverlappingRangesInPool(
            String range1From, String range1To,
            String range2From, String range2To, boolean overlapping) {
        MacRange range1 = new MacRange();
        range1.setMacFrom(range1From);
        range1.setMacTo(range1To);

        MacRange range2 = new MacRange();
        range2.setMacFrom(range2From);
        range2.setMacTo(range2To);

        MacPool macPool = new MacPool();
        macPool.setRanges(Arrays.asList(range1, range2));

        assertFalse(overlapping && ValidationResult.VALID.equals(macPoolValidator.validateOverlappingRanges(macPool)));
    }

    /**
     * @return data stream for test with same name
     */
    static Stream<Object[]> testValidateOverlapWithOtherPools() {
        return parameterStreamForOverlapTests();
    }

    @ParameterizedTest
    @MethodSource
    void testValidateOverlapWithOtherPools(
        String range1From, String range1To,
        String range2From, String range2To, boolean overlapping) {

        MacRange range1 = new MacRange();
        range1.setMacFrom(range1From);
        range1.setMacTo(range1To);

        MacRange range2 = new MacRange();
        range2.setMacFrom(range2From);
        range2.setMacTo(range2To);

        MacPool macPool1 = new MacPool();
        macPool1.setId(Guid.newGuid());
        macPool1.setRanges(Collections.singletonList(range1));

        MacPool macPool2 = new MacPool();
        macPool2.setId(Guid.newGuid());
        macPool2.setRanges(Collections.singletonList(range1));

        assertFalse(overlapping && ValidationResult.VALID.equals(
                macPoolValidator.validateOverlapWithOtherPools(Collections.singletonList(macPool2), macPool1)
            )
        );

        assertFalse(overlapping && ValidationResult.VALID.equals(
                macPoolValidator.validateOverlapWithOtherPools(Arrays.asList(macPool1, macPool2), macPool1)
            )
        );

    }

    private static Stream<Object[]> parameterStreamForOverlapTests() {
        return Stream.of(
                // array of [range1 from, range1 to, range2 from, range2 to, are overlapping]
                new Object[] { "ff::05", "ff::08", "ff::05", "ff::08", true },
                new Object[] { "ff::05", "ff::08", "ff::05", "ff::10", true },
                new Object[] { "ff::05", "ff::08", "ff::06", "ff::08", true },
                new Object[] { "ff::05", "ff::08", "ff::05", "ff::07", true },
                new Object[] { "ff::05", "ff::08", "ff::06", "ff::10", true },
                new Object[] { "ff::05", "ff::08", "ff::06", "ff::07", true },
                new Object[] { "ff::05", "ff::08", "ff::04", "ff::10", true },
                new Object[] { "ff::05", "ff::08", "ff::00", "ff::05", true },
                new Object[] { "ff::05", "ff::08", "ff::08", "ff::10", true },

                new Object[] { "ff::05", "ff::08", "ff::00", "ff::04", false },
                new Object[] { "ff::05", "ff::08", "ff::09", "ff::10", false }
        );
    }
}
