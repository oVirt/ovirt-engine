package org.ovirt.engine.core.dao.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Time;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotScheduleRecurrence;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;

public class GlusterVolumeSnapshotScheduleDaoTest extends BaseDaoTestCase<GlusterVolumeSnapshotScheduleDao> {

    private static final Guid CLUSTER_ID = new Guid("ae956031-6be2-43d6-bb8f-5191c9253314");
    private static final Guid VOLUME_ID_1 = new Guid("0c3f45f6-3fe9-4b35-a30c-be0d1a835ea8");
    private static final Guid VOLUME_ID_2 = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");

    private GlusterVolumeSnapshotSchedule existingSchedule;
    private GlusterVolumeSnapshotSchedule newSchedule;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        existingSchedule = dao.getByVolumeId(VOLUME_ID_1);
    }

    @Test
    public void testSaveAndGetByVolumeId() {
        GlusterVolumeSnapshotSchedule schedule = dao.getByVolumeId(VOLUME_ID_2);
        assertNull(schedule);

        newSchedule = insertSnapshotSchedule();
        schedule = dao.getByVolumeId(VOLUME_ID_2);
        assertNotNull(schedule);
        assertEquals(schedule, newSchedule);
    }

    private GlusterVolumeSnapshotSchedule insertSnapshotSchedule() {
        GlusterVolumeSnapshotSchedule schedule = new GlusterVolumeSnapshotSchedule();
        schedule.setClusterId(CLUSTER_ID);
        schedule.setVolumeId(VOLUME_ID_2);
        schedule.setJobId(Guid.createGuidFromString("77569427-9fbe-41db-ae91-fb96fab17141"));
        schedule.setSnapshotNamePrefix("prefix");
        schedule.setSnapshotDescription("desc");
        schedule.setInterval(0);
        schedule.setRecurrence(GlusterVolumeSnapshotScheduleRecurrence.DAILY);
        schedule.setTimeZone("Asia/Calcutta");
        schedule.setExecutionTime(new Time(10, 30, 0));
        schedule.setEndByDate(null);
        dao.save(schedule);
        return schedule;
    }

    @Test
    public void testGetByVolumeId() {
        GlusterVolumeSnapshotSchedule schedule = dao.getByVolumeId(VOLUME_ID_1);
        assertNotNull(schedule);
        assertEquals(schedule, existingSchedule);
    }

    @Test
    public void testRemoveByVolumeId() {
        dao.removeByVolumeId(VOLUME_ID_1);
        GlusterVolumeSnapshotSchedule schedule = dao.getByVolumeId(VOLUME_ID_1);
        assertNull(schedule);
    }

    @Test
    public void testUpdateShceduleByVolumeId() {
        GlusterVolumeSnapshotSchedule schedule = dao.getByVolumeId(VOLUME_ID_1);
        schedule.setRecurrence(GlusterVolumeSnapshotScheduleRecurrence.HOURLY);
        schedule.setStartDate(new Date());
        schedule.setInterval(0);

        dao.updateScheduleByVolumeId(VOLUME_ID_1, schedule);

        GlusterVolumeSnapshotSchedule fetchedSchedule = dao.getByVolumeId(VOLUME_ID_1);
        assertNotNull(fetchedSchedule);
        assertEquals(fetchedSchedule, schedule);
    }
}
