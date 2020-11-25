package org.ovirt.engine.core.common.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;

public class NumaUtilsTest {
    private static List<VmNumaNode> nodeList;

    @BeforeAll
    public static void setUp() {
        nodeList = new ArrayList<>();
        VmNumaNode vmNumaNode = new VmNumaNode();
        vmNumaNode.setIndex(0);
        VmNumaNode vmNumaNode1 = new VmNumaNode();
        vmNumaNode1.setIndex(1);
        nodeList.add(vmNumaNode);
        nodeList.add(vmNumaNode1);
    }

    @Test
    public void testNumaConfigurationSingle() {
        NumaUtils.setNumaListConfiguration(nodeList, 1024, Optional.empty(), 2, NumaTuneMode.STRICT);

        assertThat(nodeList.get(0).getCpuIds().size(), is(1));
        assertThat(nodeList.get(0).getMemTotal(), is((long)512));
        assertThat(nodeList.get(0).getNumaTuneMode(), is(NumaTuneMode.STRICT));
    }

    @Test
    public void testNumaConfigurationDual() {
        NumaUtils.setNumaListConfiguration(nodeList, 1024, Optional.empty(), 4, NumaTuneMode.STRICT);

        assertThat(nodeList.get(0).getCpuIds().size(), is(2));
        assertThat(nodeList.get(0).getMemTotal(), is((long)512));
        assertThat(nodeList.get(0).getNumaTuneMode(), is(NumaTuneMode.STRICT));
    }

    @Test
    public void testNumaConfigurationHugePages() {
        NumaUtils.setNumaListConfiguration(nodeList, 1024, Optional.of(1048576), 2, NumaTuneMode.STRICT);

        assertThat(nodeList.get(0).getCpuIds().size(), is(1));
        assertThat(nodeList.get(0).getMemTotal(), is((long)1024));
        assertThat(nodeList.get(0).getNumaTuneMode(), is(NumaTuneMode.STRICT));
    }

    @Test
    public void testNumaConfigurationHugePagesBigMemory() {
        NumaUtils.setNumaListConfiguration(nodeList, 8192, Optional.of(1048576), 2, NumaTuneMode.STRICT);

        assertThat(nodeList.get(0).getCpuIds().size(), is(1));
        assertThat(nodeList.get(0).getMemTotal(), is((long)4096));
        assertThat(nodeList.get(0).getNumaTuneMode(), is(NumaTuneMode.STRICT));
    }
}
