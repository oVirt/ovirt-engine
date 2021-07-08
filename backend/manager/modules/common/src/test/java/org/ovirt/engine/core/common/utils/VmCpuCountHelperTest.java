package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;

public class VmCpuCountHelperTest {

    @Test
    public void testCommon() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(null, Integer.valueOf(16), Integer.valueOf(288), 2, 12, null);
        assertEquals(Integer.valueOf(192), maxVCpu);
    }

    @Test
    public void testPowerOf2() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(null, Integer.valueOf(16), Integer.valueOf(288), 2, 4, null);
        assertEquals(Integer.valueOf(128), maxVCpu);
    }

    @Test
    public void test256() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(null, Integer.valueOf(16), Integer.valueOf(288), 2, 8, null);
        assertEquals(Integer.valueOf(240), maxVCpu);
    }

    @Test
    public void testLimit() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(null, Integer.valueOf(16), Integer.valueOf(100), 2, 8, null);
        assertEquals(Integer.valueOf(96), maxVCpu);
    }

    @Test
    public void testCommonX86() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.x86, Integer.valueOf(16), Integer.valueOf(288),
                2, 12, null);
        assertEquals(Integer.valueOf(192), maxVCpu);
    }

    @Test
    public void testPowerOf2X86() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.x86, Integer.valueOf(16), Integer.valueOf(288),
                2, 4, null);
        assertEquals(Integer.valueOf(128), maxVCpu);
    }

    @Test
    public void test256X86() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.x86, Integer.valueOf(16), Integer.valueOf(288),
                2, 8, null);
        assertEquals(Integer.valueOf(240), maxVCpu);
    }

    @Test
    public void testAtLimitX86() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.x86, Integer.valueOf(16), Integer.valueOf(240),
                2, 8, null);
        assertEquals(Integer.valueOf(240), maxVCpu);
    }

    @Test
    public void testLimitX86() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.x86, Integer.valueOf(16), Integer.valueOf(240),
                2, 16, null);
        assertEquals(Integer.valueOf(224), maxVCpu);
    }

    @Test
    public void testTooManyX86APIC() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.x86, Integer.valueOf(32), Integer.valueOf(710),
                2, 16, null);
        assertEquals(Integer.valueOf(224), maxVCpu);
    }

    @Test
    public void testTooManyX86Q35() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.x86, Integer.valueOf(32), Integer.valueOf(710),
                2, 16, BiosType.Q35_OVMF);
        assertEquals(Integer.valueOf(704), maxVCpu);
    }

    @Test
    public void testCommonPpc() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.ppc, Integer.valueOf(16), Integer.valueOf(288),
                2, 12, null);
        assertEquals(Integer.valueOf(288), maxVCpu);
    }

    @Test
    public void testPowerOf2Ppc() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.ppc, Integer.valueOf(16), Integer.valueOf(288),
                2, 4, null);
        assertEquals(Integer.valueOf(128), maxVCpu);
    }

    @Test
    public void test256Ppc() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.ppc, Integer.valueOf(16), Integer.valueOf(288),
                2, 8, null);
        assertEquals(Integer.valueOf(256), maxVCpu);
    }

    @Test
    public void testLimitPpc() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.x86, Integer.valueOf(16), Integer.valueOf(240),
                2, 16, null);
        assertEquals(Integer.valueOf(224), maxVCpu);
    }
}
