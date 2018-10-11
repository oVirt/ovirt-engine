package org.ovirt.engine.core.common.businessentities.gluster;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GlusterLocalVolumeInfoTest {
    private GlusterLocalVolumeInfo volumeInfo;

    /*
        This test uses following disk layout:
        sdb
         |-sdb1 (PV)
            |- vg0 (VG)
                |- engine (LV)
                |- pool (Thin Pool)
                    |- iso (Thin LV)
                    |- vdobase (Thin LV)
                         |- vdodata (VDO, PV)
                              |- INTERNAL (VG)
                                   |- internal_pool (Thin Pool)
                                        |- vdoreplica (Thin LV)
                    |- vdosecond (Thin LV)
                         |- vdonext (VDO)
        sdc
         |- sdc1
             |- vdophysical (VDO)
                 |- vg(1) (VG)
                     |- vmstore (LV)

     */

    @BeforeEach
    public void setUp() {
        volumeInfo = new GlusterLocalVolumeInfo();
        volumeInfo.setLogicalVolumes(getLogicalVolumes());
        volumeInfo.setPhysicalVolumes(getPhysicalVolumes());
        volumeInfo.setVdoVolumes(getVDOVolumes());
    }

    @Test
    public void testEmpty() {
        assertFalse(new GlusterLocalVolumeInfo().getAvailableThinSizeForDevice("/dev/sdc").isPresent());
    }

    @Test
    public void testNonLvm() {
        assertFalse(volumeInfo.getAvailableThinSizeForDevice("/dev/sdc").isPresent());
    }

    @Test
    public void testNonThinLvm() {
        assertFalse(volumeInfo.getAvailableThinSizeForDevice("/dev/mapper/vg0-engine").isPresent());
    }

    @Test
    public void testThinLvm() {
        assertTrue(volumeInfo.getAvailableThinSizeForDevice("/dev/mapper/vg0-iso").isPresent());
        assertThat(volumeInfo.getAvailableThinSizeForDevice("/dev/mapper/vg0-iso").get(), is(19391777341L));
        assertThat(volumeInfo.getSavingsForDevice("/dev/mapper/vg0-iso").get(), is(0));
    }

    @Test
    public void testVdo() {
        assertTrue(volumeInfo.getAvailableThinSizeForDevice("/dev/mapper/vdophysical").isPresent());
        assertThat(volumeInfo.getAvailableThinSizeForDevice("/dev/mapper/vdophysical").get(), is(6483109092L));
        assertThat(volumeInfo.getSavingsForDevice("/dev/mapper/vdophysical").get(), is(64));
    }

    @Test
    public void testVdoLvm() {
        assertTrue(volumeInfo.getAvailableThinSizeForDevice("/dev/mapper/vg1-vmstore").isPresent());
        assertThat(volumeInfo.getAvailableThinSizeForDevice("/dev/mapper/vg1-vmstore").get(), is(6483109092L));
        assertThat(volumeInfo.getSavingsForDevice("/dev/mapper/vg1-vmstore").get(), is(64));
    }

    @Test
    public void testThinLvmVdo() {
        assertTrue(volumeInfo.getAvailableThinSizeForDevice("/dev/mapper/vdonext").isPresent());
        assertThat(volumeInfo.getAvailableThinSizeForDevice("/dev/mapper/vdonext").get(), is(6438100992L));
        assertThat(volumeInfo.getSavingsForDevice("/dev/mapper/vdonext").get(), is(66));
    }

    @Test
    public void testThinLvmVdoThinLvm() {
        assertTrue(volumeInfo.getAvailableThinSizeForDevice("/dev/mapper/INTERNAL-vdoreplica").isPresent());
        assertThat(volumeInfo.getAvailableThinSizeForDevice("/dev/mapper/INTERNAL-vdoreplica").get(), is(6415818752L));
        assertThat(volumeInfo.getSavingsForDevice("/dev/mapper/INTERNAL-vdoreplica").get(), is(50));
    }

    private List<GlusterLocalLogicalVolume> getLogicalVolumes() {
        GlusterLocalLogicalVolume internalPool = new GlusterLocalLogicalVolume();
        internalPool.setLogicalVolumeName("internal_pool");
        internalPool.setVolumeGroupName("INTERNAL");
        internalPool.setPoolName("");
        internalPool.setSize(52613349376L);
        internalPool.setFree(52581781366L);

        GlusterLocalLogicalVolume vdoreplica = new GlusterLocalLogicalVolume();
        vdoreplica.setLogicalVolumeName("vdoreplica");
        vdoreplica.setVolumeGroupName("INTERNAL");
        vdoreplica.setPoolName("internal_pool");
        vdoreplica.setSize(10737418240L);
        vdoreplica.setFree(0);

        GlusterLocalLogicalVolume engine = new GlusterLocalLogicalVolume();
        engine.setLogicalVolumeName("engine");
        engine.setVolumeGroupName("vg0");
        engine.setPoolName("");
        engine.setSize(53687091200L);
        engine.setFree(0);

        GlusterLocalLogicalVolume iso = new GlusterLocalLogicalVolume();
        iso.setLogicalVolumeName("iso");
        iso.setVolumeGroupName("vg0");
        iso.setPoolName("pool");
        iso.setSize(53687091200L);
        iso.setFree(0);

        GlusterLocalLogicalVolume pool = new GlusterLocalLogicalVolume();
        pool.setLogicalVolumeName("pool");
        pool.setVolumeGroupName("vg0");
        pool.setPoolName("");
        pool.setSize(80530636800L);
        pool.setFree(19391777341L);

        GlusterLocalLogicalVolume vdobase = new GlusterLocalLogicalVolume();
        vdobase.setLogicalVolumeName("vdobase");
        vdobase.setVolumeGroupName("vg0");
        vdobase.setPoolName("pool");
        vdobase.setSize(10737418240L);
        vdobase.setFree(0);

        GlusterLocalLogicalVolume vdosecond = new GlusterLocalLogicalVolume();
        vdosecond.setLogicalVolumeName("vdosecond");
        vdosecond.setVolumeGroupName("vg0");
        vdosecond.setPoolName("pool");
        vdosecond.setSize(10737418240L);
        vdosecond.setFree(0);

        GlusterLocalLogicalVolume vmstore = new GlusterLocalLogicalVolume();
        vmstore.setLogicalVolumeName("vmstore");
        vmstore.setVolumeGroupName("vg1");
        vmstore.setPoolName("");
        vmstore.setSize(53687091200L);
        vmstore.setFree(0);

        List<GlusterLocalLogicalVolume> logicalVolumeList = new ArrayList<>();
        logicalVolumeList.add(internalPool);
        logicalVolumeList.add(vdoreplica);
        logicalVolumeList.add(engine);
        logicalVolumeList.add(iso);
        logicalVolumeList.add(pool);
        logicalVolumeList.add(vdobase);
        logicalVolumeList.add(vdosecond);
        logicalVolumeList.add(vmstore);
        return logicalVolumeList;
    }

    private List<GlusterLocalPhysicalVolume> getPhysicalVolumes() {
        GlusterLocalPhysicalVolume vdoDataVolume = new GlusterLocalPhysicalVolume();
        vdoDataVolume.setPhysicalVolumeName("/dev/mapper/vdodata");
        vdoDataVolume.setVolumeGroupName("INTERNAL");

        GlusterLocalPhysicalVolume sdbDataVolume = new GlusterLocalPhysicalVolume();
        sdbDataVolume.setPhysicalVolumeName("/dev/sdb1");
        sdbDataVolume.setVolumeGroupName("vg0");

        GlusterLocalPhysicalVolume sdcDataVolume = new GlusterLocalPhysicalVolume();
        sdcDataVolume.setPhysicalVolumeName("/dev/mapper/vdophysical");
        sdcDataVolume.setVolumeGroupName("vg1");

        List<GlusterLocalPhysicalVolume> physicalVolumeList = new ArrayList<>();
        physicalVolumeList.add(vdoDataVolume);
        physicalVolumeList.add(sdbDataVolume);
        physicalVolumeList.add(sdcDataVolume);
        return physicalVolumeList;
    }

    private List<GlusterVDOVolume> getVDOVolumes() {
        GlusterVDOVolume dataVdo = new GlusterVDOVolume();
        dataVdo.setName("/dev/mapper/vdodata");
        dataVdo.setDevice("/dev/vg0/vdobase");
        dataVdo.setSize(10737428240L);
        dataVdo.setFree(6415818752L);
        dataVdo.setPhysicalBlocks(37);
        dataVdo.setLogicalBlocks(75);

        GlusterVDOVolume nextVdo = new GlusterVDOVolume();
        nextVdo.setName("/dev/mapper/vdonext");
        nextVdo.setDevice("/dev/vg0/vdosecond");
        nextVdo.setSize(10737418240L);
        nextVdo.setFree(6438100992L);
        nextVdo.setPhysicalBlocks(33);
        nextVdo.setLogicalBlocks(99);

        GlusterVDOVolume physicalVdo = new GlusterVDOVolume();
        physicalVdo.setName("/dev/mapper/vdophysical");
        physicalVdo.setDevice("/dev/sdc1");
        physicalVdo.setSize(10737418240L);
        physicalVdo.setFree(6483109092L);
        physicalVdo.setPhysicalBlocks(14);
        physicalVdo.setLogicalBlocks(39);

        List<GlusterVDOVolume> vdoVolumeList = new ArrayList<>();
        vdoVolumeList.add(dataVdo);
        vdoVolumeList.add(nextVdo);
        vdoVolumeList.add(physicalVdo);
        return vdoVolumeList;
    }
}
