package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.VmBase;
import org.ovirt.engine.core.common.businessentities.OriginType;

public class VmBaseMapperTest {

    @Test
    public void testMapOriginTypeRhev() {
        String s = VmBaseMapper.map(OriginType.RHEV, null);
        assertEquals("rhev", s);
        OriginType s2 = VmMapper.map(s, OriginType.RHEV);
        assertEquals(OriginType.RHEV, s2);
    }

    @Test
    public void testMapOriginTypeOvirt() {
        String s = VmBaseMapper.map(OriginType.OVIRT, null);
        assertEquals("ovirt", s);
        OriginType s2 = VmMapper.map(s, OriginType.OVIRT);
        assertEquals(OriginType.OVIRT, s2);
    }

    @Test
    public void testVirtioScsiMultiQueueCustom() {
        VmBase model = new VmBase();
        int numOfQueues = 5;
        model.setVirtioScsiMultiQueuesEnabled(true);
        model.setVirtioScsiMultiQueues(numOfQueues);
        org.ovirt.engine.core.common.businessentities.VmBase entity =
                new org.ovirt.engine.core.common.businessentities.VmBase();

        VmBaseMapper.mapVmBaseModelToEntity(entity, model);
        assertEquals(numOfQueues, entity.getVirtioScsiMultiQueues());
    }

    @Test
    public void testVirtioScsiMultiQueueAutomatic() {
        VmBase model = new VmBase();
        model.setVirtioScsiMultiQueuesEnabled(true);
        org.ovirt.engine.core.common.businessentities.VmBase entity =
                new org.ovirt.engine.core.common.businessentities.VmBase();

        VmBaseMapper.mapVmBaseModelToEntity(entity, model);
        assertEquals(-1, entity.getVirtioScsiMultiQueues());
    }

    @Test
    public void testVirtioScsiMultiQueueDisabled() {
        VmBase model = new VmBase();
        model.setVirtioScsiMultiQueuesEnabled(false);
        org.ovirt.engine.core.common.businessentities.VmBase entity =
                new org.ovirt.engine.core.common.businessentities.VmBase();

        VmBaseMapper.mapVmBaseModelToEntity(entity, model);
        assertEquals(0, entity.getVirtioScsiMultiQueues());
    }

    @Test
    public void testVirtioScsiMultiQueueCustomRestOutput() {
        org.ovirt.engine.core.common.businessentities.VmBase entity =
                new org.ovirt.engine.core.common.businessentities.VmBase();
        int numOfQueues = 5;
        entity.setVirtioScsiMultiQueues(numOfQueues);

        VmBase model = new VmBase();

        VmBaseMapper.mapVmBaseEntityToModel(model, entity);

        assertEquals(true, model.isVirtioScsiMultiQueuesEnabled());
        assertEquals(numOfQueues, model.getVirtioScsiMultiQueues());
    }

    @Test
    public void testVirtioScsiMultiQueueAutomaticRestOutput() {
        org.ovirt.engine.core.common.businessentities.VmBase entity =
                new org.ovirt.engine.core.common.businessentities.VmBase();
        entity.setVirtioScsiMultiQueues(-1);

        VmBase model = new VmBase();

        VmBaseMapper.mapVmBaseEntityToModel(model, entity);

        assertEquals(true, model.isVirtioScsiMultiQueuesEnabled());
        assertEquals(null,
                model.getVirtioScsiMultiQueues(),
                "Automatic value should not be" +
                        " returned in REST API output");
    }

    @Test
    public void testVirtioScsiMultiQueueDisabledRestOutput() {
        org.ovirt.engine.core.common.businessentities.VmBase entity =
                new org.ovirt.engine.core.common.businessentities.VmBase();
        entity.setVirtioScsiMultiQueues(0);

        VmBase model = new VmBase();

        VmBaseMapper.mapVmBaseEntityToModel(model, entity);

        assertEquals(false, model.isVirtioScsiMultiQueuesEnabled());
        assertEquals(null,
                model.getVirtioScsiMultiQueues(),
                "Disabled value should not be " +
                        "returned in REST API output");
    }
}
