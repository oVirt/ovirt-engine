package org.ovirt.engine.core.dao.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.Time;
import java.util.Date;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotScheduleRecurrence;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;

public class GlusterVolumeSnapshotScheduleDaoTest extends BaseDaoTestCase {

    private static final Guid CLUSTER_ID = new Guid("ae956031-6be2-43d6-bb8f-5191c9253314");
    private static final Guid VOLUME_ID_1 = new Guid("0c3f45f6-3fe9-4b35-a30c-be0d1a835ea8");
    private static final Guid VOLUME_ID_2 = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");

    private GlusterVolumeSnapshotScheduleDao dao;
    private GlusterVolumeSnapshotSchedule existingSchedule;
    private GlusterVolumeSnapshotSchedule newSchedule;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getGlusterVolumeSnapshotScheduleDao();
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
        schedule.setJobId("test_job_id");
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
