package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;

@RunWith(MockitoJUnitRunner.class)
public class MetadataDiskDescriptionHandlerTest {

    private static int DISK_ALIAS_MAX_LENGTH = 194;

    private DiskImage disk;
    private MetadataDiskDescriptionHandler metadataDiskDescriptionHandler;

    @Rule
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    @Before
    public void setUp() {
        disk = new DiskImage();
        metadataDiskDescriptionHandler = spy(MetadataDiskDescriptionHandler.getInstance());
        doNothing().when(metadataDiskDescriptionHandler).auditLog(any(AuditLogableBase.class), any(AuditLogType.class));
    }

    @Test
    public void testJsonDiskDescription() throws IOException {
        disk.setDiskAlias("DiskAlias");
        disk.setDiskDescription("DiskDescription");
        assertDiskDescriptionMap(disk, generateDiskAliasJsonEntry("DiskAlias"),
                generateDiskDescriptionJsonEntry("DiskDescription"));
    }

    @Test
    public void testJsonNullDiskDescription() throws IOException {
        disk.setDiskAlias("DiskAlias");
        disk.setDiskDescription(null);
        assertDiskDescriptionMap(disk, generateDiskAliasJsonEntry("DiskAlias"), generateDiskDescriptionJsonEntry(""));
    }

    @Test
    public void testJsonEmptyDiskDescription() throws IOException {
        disk.setDiskAlias("DiskAlias");
        disk.setDiskDescription("");
        assertDiskDescriptionMap(disk, generateDiskAliasJsonEntry("DiskAlias"), generateDiskDescriptionJsonEntry(""));
    }

    @Test
    public void generateDiskDescriptionWhenAliasIsTruncated() throws IOException {
        String alias = generateRandomString(200);
        disk.setDiskAlias(alias); // Only 194 can be stored.
        disk.setDiskDescription("DiskDescription");
        // Only first 194 bytes were stored from the alias. The description was lost.
        assertDiskDescriptionMap(disk, generateDiskAliasJsonEntry(alias.substring(0, DISK_ALIAS_MAX_LENGTH)));
    }

    @Test
    public void generateDiskDescriptionWhenDescriptionIsCompletelyTruncated() throws IOException {
        String alias = generateRandomString(DISK_ALIAS_MAX_LENGTH);
        disk.setDiskAlias(alias); // Exactly the limit.
        disk.setDiskDescription("DiskDescription");
        // Alias was completely stored. The description was lost.
        assertDiskDescriptionMap(disk, generateDiskAliasJsonEntry(alias));
    }

    @Test
    public void generateDiskDescriptionWhenDescriptionIsTruncated() throws IOException {
        disk.setDiskAlias("DiskAlias"); // diskAlias.length == 9 characters.
        String diskDescription = generateRandomString(170);
        disk.setDiskDescription(diskDescription); // Only 164 can be stored.
        // Only first 164 bytes were stored from the description. The alias was completely stored.
        assertDiskDescriptionMap(disk, generateDiskAliasJsonEntry("DiskAlias"),
                generateDiskDescriptionJsonEntry(diskDescription.substring(0, 164)));
    }

    private static String generateDiskAliasJsonEntry(String diskAlias) {
        return generateJsonEntry("DiskAlias", diskAlias);
    }

    private static String generateDiskDescriptionJsonEntry(String diskDescription) {
        return generateJsonEntry("DiskDescription", diskDescription);
    }

    private static String generateJsonEntry(String key, String value) {
        return String.format("\"%s\":\"%s\"", key, value);
    }

    private static String generateRandomString(int stringLength) {
        return RandomUtils.instance().nextPropertyString(stringLength);
    }

    private void assertDiskDescriptionMap(Disk disk, String... jsonEntries)
            throws IOException {
        assertEquals(String.format("{%s}", StringUtils.join(jsonEntries, ",", 0, jsonEntries.length)),
                metadataDiskDescriptionHandler.generateJsonDiskDescription(disk));
    }
}
