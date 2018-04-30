package org.ovirt.engine.core.bll.scheduling.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ovirt.engine.core.bll.scheduling.utils.CpuPinningHelper.getAllPinnedPCpus;
import static org.ovirt.engine.core.bll.scheduling.utils.CpuPinningHelper.parseCpuPinning;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.scheduling.utils.CpuPinningHelper.PinnedCpu;

public class CpuPinningHelperTest {

    @Test
    public void shouldFilterExcludes() {
        List<PinnedCpu> pinnedCpus = parseCpuPinning("0#1-7,^3,^5");
        assertThat(pinnedCpus.get(0).getpCpus()).containsExactly(1, 2, 4, 6, 7);
    }

    @Test
    public void shouldNotCreateSingleElementRange() {
        List<PinnedCpu> pinnedCpus = parseCpuPinning("0#2-2");
        assertThat(pinnedCpus.get(0).getpCpus()).isEmpty();
    }

    @Test
    public void shouldNotCreateInvertedRange() {
        List<PinnedCpu> pinnedCpus = parseCpuPinning("0#3-1");
        assertThat(pinnedCpus.get(0).getpCpus()).isEmpty();
    }

    @Test
    public void shouldCreateIndividalCpus() {
        List<PinnedCpu> pinnedCpus = parseCpuPinning("0#2,4,7,^4");
        assertThat(pinnedCpus.get(0).getpCpus()).containsExactly(2, 7);
    }

    @Test
    public void shouldCreateMultiplePinnedCpus() {
        List<PinnedCpu> pinnedCpus = parseCpuPinning("0#2_2#1-2_5#3");
        assertThat(pinnedCpus.get(0).getpCpus()).containsExactly(2);
        assertThat(pinnedCpus.get(0).getvCpu()).isEqualTo(0);
        assertThat(pinnedCpus.get(1).getpCpus()).containsExactly(1, 2);
        assertThat(pinnedCpus.get(1).getvCpu()).isEqualTo(2);
        assertThat(pinnedCpus.get(2).getpCpus()).containsExactly(3);
        assertThat(pinnedCpus.get(2).getvCpu()).isEqualTo(5);
    }

    @Test
    public void shouldGetAllPinnedHostCpus() {
        Collection<Integer> pinnedHostCpus = getAllPinnedPCpus("0#3_2#1-2,12_5#3,4,10,^10_6#6-9,^8_9#13-15");
        assertThat(pinnedHostCpus).containsOnly(1, 2, 3, 4, 6, 7, 9, 12, 13, 14, 15);

    }
}
