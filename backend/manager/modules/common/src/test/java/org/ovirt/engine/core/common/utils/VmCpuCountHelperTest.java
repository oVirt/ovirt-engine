package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;

public class VmCpuCountHelperTest {

    @Test
    public void testCommon() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(null, new Integer(16), new Integer(288), 2, 12);
        assertEquals(new Integer(192), maxVCpu);
    }

    @Test
    public void testPowerOf2() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(null, new Integer(16), new Integer(288), 2, 4);
        assertEquals(new Integer(128), maxVCpu);
    }

    @Test
    public void test256() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(null, new Integer(16), new Integer(288), 2, 8);
        assertEquals(new Integer(240), maxVCpu);
    }

    @Test
    public void testLimit() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(null, new Integer(16), new Integer(100), 2, 8);
        assertEquals(new Integer(96), maxVCpu);
    }

    @Test
    public void testCommonX86() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.x86, new Integer(16), new Integer(288), 2, 12);
        assertEquals(new Integer(192), maxVCpu);
    }

    @Test
    public void testPowerOf2X86() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.x86, new Integer(16), new Integer(288), 2, 4);
        assertEquals(new Integer(128), maxVCpu);
    }

    @Test
    public void test256X86() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.x86, new Integer(16), new Integer(288), 2, 8);
        assertEquals(new Integer(240), maxVCpu);
    }

    @Test
    public void testAtLimitX86() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.x86, new Integer(16), new Integer(240), 2, 8);
        assertEquals(new Integer(240), maxVCpu);
    }

    @Test
    public void testLimitX86() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.x86, new Integer(16), new Integer(240), 2, 16);
        assertEquals(new Integer(224), maxVCpu);
    }

    @Test
    public void testCommonPpc() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.ppc, new Integer(16), new Integer(288), 2, 12);
        assertEquals(new Integer(288), maxVCpu);
    }

    @Test
    public void testPowerOf2Ppc() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.ppc, new Integer(16), new Integer(288), 2, 4);
        assertEquals(new Integer(128), maxVCpu);
    }

    @Test
    public void test256Ppc() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.ppc, new Integer(16), new Integer(288), 2, 8);
        assertEquals(new Integer(256), maxVCpu);
    }

    @Test
    public void testLimitPpc() {
        Integer maxVCpu = VmCpuCountHelper.calcMaxVCpu(ArchitectureType.x86, new Integer(16), new Integer(240), 2, 16);
        assertEquals(new Integer(224), maxVCpu);
    }
}
