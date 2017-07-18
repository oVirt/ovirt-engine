package org.ovirt.engine.core.common.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmBase;


public class HugePageUtilsTest {

    @Test
    public void isBackedByHudepagesEmptyCustomProperties() {
        testIsBackedByHudepagesHugePage("", false);
    }

    @Test
    public void isBackedByHudepagesCustomPropertySetButNotTheHugepage() {
        testIsBackedByHudepagesHugePage("sap_agent=true", false);
    }

    @Test
    public void isBackedByHudepagesHugePagePropertySet() {
        testIsBackedByHudepagesHugePage("hugepages=1024", true);
    }

    @Test
    public void isBackedByHudepagesHugePagePropertySetToEmpty() {
        testIsBackedByHudepagesHugePage("hugepages=undefined", false);
    }

    @Test
    public void isBackedByHudepagesHugePagePropertySetToZero() {
        testIsBackedByHudepagesHugePage("hugepages=0", false);
    }

    @Test
    public void isBackedByHudepagesHugePagePropertySetToNegative() {
        testIsBackedByHudepagesHugePage("hugepages=-5", false);
    }

    @Test
    public void getHugePagesNoHugePagesDefined() {
        HugePageUtils utils = new HugePageUtils();
        VmBase base = new VmBase();
        base.setMemSizeMb(1025);

        assertThat(utils.getHugePages(base).size(), is(0));
    }

    @Test
    public void getHugeMemoryFitsIntoOneHugePage() {
        HugePageUtils utils = new HugePageUtils();
        VmBase base = new VmBase();
        base.setCustomProperties("hugepages=1024");
        base.setMemSizeMb(1024);

        assertThat(utils.getHugePages(base).size(), is(1));
        assertThat(utils.getHugePages(base).get(1024), is(1));
    }

    @Test
    public void getHugePagesMemoryDoesNotFitIntoOne() {
        HugePageUtils utils = new HugePageUtils();
        VmBase base = new VmBase();
        base.setCustomProperties("hugepages=1024");
        base.setMemSizeMb(1025);

        assertThat(utils.getHugePages(base).size(), is(1));
        assertThat(utils.getHugePages(base).get(1024), is(2));
    }

    private void testIsBackedByHudepagesHugePage(String customProperties, boolean expected) {
        HugePageUtils utils = new HugePageUtils();
        VmBase base = new VmBase();
        base.setCustomProperties(customProperties);
        assertThat(utils.isBackedByHudepages(base), is(expected));
    }
}
