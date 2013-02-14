package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;

/** A test case for the {@link GetDiskConfigurationListQuery} class. */
@SuppressWarnings("unchecked")
public class GetDiskConfigurationListQueryTest extends AbstractUserQueryTest<VdcQueryParametersBase, GetDiskConfigurationListQuery<VdcQueryParametersBase>> {

    @Rule
    public MockConfigRule mcr = new MockConfigRule();

    @Test
    public void testExecuteQueryValidConfigurationMultipleDisks() {
        assertValidConfig(nextDiskImageBase(), nextDiskImageBase());
    }

    @Test
    public void testExecuteQueryValidConfigurationSingleDisk() {
        assertValidConfig(nextDiskImageBase());
    }

    /**
     * Tests the {@link GetDiskConfigurationListQuery#executeQueryCommand()} with an expected output of the given configurations.
     * @param expectedDisks The expected result for the query.
     */
    private void assertValidConfig(DiskImageBase... expectedDisks) {
        List<String> disksConfigurtaion = new ArrayList<String>(expectedDisks.length);

        // Mock the config
        for (int i = 0; i < expectedDisks.length; ++i) {
            DiskImageBase expectedDisk = expectedDisks[i];
            disksConfigurtaion.add(
                    StringUtils.join(Arrays.asList("label" + i,
                            expectedDisk.getVolumeType(),
                            expectedDisk.getVolumeFormat(),
                            expectedDisk.isWipeAfterDelete()),
                            ',')
                    );
        }
        String confStringValue = StringUtils.join(disksConfigurtaion, ';');
        mcr.mockConfigValue(ConfigValues.DiskConfigurationList, confStringValue);

        // Execute the query
        getQuery().executeQueryCommand();

        // Assert the results
        List<DiskImageBase> result = (List<DiskImageBase>) getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong number of disk configurations", expectedDisks.length, result.size());
        for (int i = 0; i < expectedDisks.length; ++i) {
            DiskImageBase expectedDisk = expectedDisks[i];
            DiskImageBase actualDisk = result.get(i);

            assertEquals("Wrong volume type", expectedDisk.getVolumeType(), actualDisk.getVolumeType());
            assertEquals("Wrong volume format", expectedDisk.getVolumeFormat(), actualDisk.getVolumeFormat());
            assertEquals("Wrong wipe after delete flag",
                    expectedDisk.isWipeAfterDelete(),
                    actualDisk.isWipeAfterDelete());
        }
    }

    /** @return A randomly generate {@link DiskImageBase} */
    private static DiskImageBase nextDiskImageBase() {
        DiskImageBase disk = new DiskImageBase();
        disk.setVolumeType(RandomUtils.instance().nextEnum(VolumeType.class));
        disk.setvolumeFormat(RandomUtils.instance().nextEnum(VolumeFormat.class));
        disk.setWipeAfterDelete(RandomUtils.instance().nextBoolean());
        return disk;
    }

    @Test
    public void testExecuteQueryInvalidMissingParameters() {
        assertInvalidConfig("label,Sparse,COW");
    }

    @Test
    public void testExecuteQueryInvalidExtraParameters() {
        assertInvalidConfig("label,Sparse,COW,true,EasterBunny");
    }

    @Test
    public void testExecuteQueryInvalidMalformedParameters() {
        assertInvalidConfig("label,NOT-Sparse,COW,true");
    }

    @Test
    public void testExecuteQueryInvalidConfigurationEmpty() {
        assertInvalidConfig("");
    }

    private void assertInvalidConfig(String config) {
        mcr.mockConfigValue(ConfigValues.DiskConfigurationList, config);

        // Execute the query
        getQuery().executeQueryCommand();

        // Assert the results
        List<DiskImageBase> result = (List<DiskImageBase>) getQuery().getQueryReturnValue().getReturnValue();

        assertTrue("No configurations should be returned", result.isEmpty());
    }
}
