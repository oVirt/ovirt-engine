package org.ovirt.engine.core.common.businessentities.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.gluster.StorageSyncSchedule.Day;
import org.ovirt.engine.core.common.businessentities.gluster.StorageSyncSchedule.Frequency;
import org.ovirt.engine.core.common.utils.ValidationUtils;

public class StorageSyncScheduleTest {

    @Test
    public void testCronExpressionForDaily() {
        StorageSyncSchedule schedule = new StorageSyncSchedule();
        schedule.setFrequency(Frequency.DAILY);
        schedule.setHour(12);
        schedule.setMins(50);
        String expectedExpression = "0 50 12 * * ? *";
        assertEquals(expectedExpression, schedule.toCronExpression());
    }

    @Test
    public void testCronExpressionForWeekly() {
        StorageSyncSchedule schedule = new StorageSyncSchedule();
        schedule.setFrequency(Frequency.WEEKLY);
        schedule.setHour(12);
        schedule.setMins(50);
        Day[] days = {Day.SAT, Day.FRI};
        schedule.setDays(days);
        String expectedExpression = "0 50 12 ? * FRI,SAT *";
        assertEquals(expectedExpression, schedule.toCronExpression());
        List<Class<?>> validationGroup = new ArrayList<>();
        List<String> result = ValidationUtils.validateInputs(validationGroup, schedule);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCronExpressionForInvalidMinsDaily() {
        StorageSyncSchedule schedule = new StorageSyncSchedule();
        schedule.setFrequency(Frequency.DAILY);
        schedule.setHour(23);
        schedule.setMins(60);
        List<Class<?>> validationGroup = new ArrayList<>();
        List<String> result = ValidationUtils.validateInputs(validationGroup, schedule);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("SCHEDULE_MINS_OUT_OF_RANGE"));
        assertFalse(result.contains("SCHEDULE_HOUR_OUT_OF_RANGE"));
    }

    @Test
    public void testCronExpressionForInvalidHourDaily() {
        StorageSyncSchedule schedule = new StorageSyncSchedule();
        schedule.setFrequency(Frequency.DAILY);
        schedule.setHour(24);
        schedule.setMins(59);
        List<Class<?>> validationGroup = new ArrayList<>();
        List<String> result = ValidationUtils.validateInputs(validationGroup, schedule);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("SCHEDULE_HOUR_OUT_OF_RANGE"));
    }

    @Test
    public void testCronExpressionForWeeklyNoDays() {
        StorageSyncSchedule schedule = new StorageSyncSchedule();
        schedule.setFrequency(Frequency.WEEKLY);
        schedule.setHour(12);
        schedule.setMins(50);
        String expectedExpression = "";
        assertEquals(expectedExpression, schedule.toCronExpression());
        List<Class<?>> validationGroup = new ArrayList<>();
        List<String> result = ValidationUtils.validateInputs(validationGroup, schedule);
        assertTrue(result.isEmpty());
    }

}
