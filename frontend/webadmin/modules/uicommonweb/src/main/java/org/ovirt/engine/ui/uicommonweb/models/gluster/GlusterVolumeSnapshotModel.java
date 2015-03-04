package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotScheduleRecurrence;
import org.ovirt.engine.core.compat.DayOfWeek;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class GlusterVolumeSnapshotModel extends Model {
    private EntityModel<String> dataCenter;
    private EntityModel<String> clusterName;
    private EntityModel<String> volumeName;
    private EntityModel<String> snapshotName;
    private EntityModel<String> description;
    private ListModel<GlusterVolumeSnapshotScheduleRecurrence> recurrence;
    private ListModel<String> interval;
    private ListModel<EndDateOptions> endByOptions;
    private EntityModel<Date> endDate;
    private boolean generalTabVisible;
    private boolean scheduleTabVisible;
    private ListModel<String> timeZones;
    private EntityModel<Date> startAt;
    private EntityModel<Date> executionTime;
    private ListModel<List<DayOfWeek>> daysOfWeek;
    private ListModel<String> daysOfMonth;

    public GlusterVolumeSnapshotModel(boolean generalTabVisible, boolean scheduleTabVisible) {
        init();
        setGeneralTabVisible(generalTabVisible);
        setScheduleTabVisible(scheduleTabVisible);
    }

    private void init() {
        setDataCenter(new EntityModel<String>());
        setClusterName(new EntityModel<String>());
        setVolumeName(new EntityModel<String>());
        setSnapshotName(new EntityModel<String>());
        setDescription(new EntityModel<String>());
        setRecurrence(new ListModel<GlusterVolumeSnapshotScheduleRecurrence>());
        setInterval(new ListModel<String>());
        setEndByOptions(new ListModel<EndDateOptions>());
        setTimeZones(new ListModel<String>());
        setDaysOfMonth(new ListModel<String>());
        setStartAt(new EntityModel<Date>(new Date()));
        setEndDate(new EntityModel<Date>(new Date()));
        setExecutionTime(new EntityModel<Date>(new Date()));
        initIntervals();
        initTimeZones();

        recurrence.setItems(Arrays.asList(GlusterVolumeSnapshotScheduleRecurrence.values()),
                GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN);
        endByOptions.setItems(Arrays.asList(EndDateOptions.values()));

        List<String> values = new ArrayList<String>();
        for (DayOfWeek day : DayOfWeek.values()) {
            values.add(day.toString().substring(0, 3));
        }
        daysOfWeek = new ListModel<List<DayOfWeek>>();
        List<DayOfWeek> daysList = Arrays.asList(DayOfWeek.values());
        List<List<DayOfWeek>> list = new ArrayList<List<DayOfWeek>>();
        list.add(daysList);
        daysOfWeek.setItems(list, new ArrayList<DayOfWeek>());
    }

    private void initIntervals() {
        List<String> intervals = new ArrayList<String>();
        int mins = 0;
        for (int nThMin = 1; mins < 55; nThMin++) {
            mins = nThMin * 5;
            intervals.add(String.valueOf(mins));
        }
        getInterval().setItems(intervals);
    }

    private void initTimeZones() {
        Set<String> timeZoneTypes = TimeZoneType.GENERAL_TIMEZONE.getTimeZoneList().keySet();
        getTimeZones().setItems(timeZoneTypes);
    }

    public EntityModel<String> getDataCenter() {
        return this.dataCenter;
    }

    public void setDataCenter(EntityModel<String> dataCenter) {
        this.dataCenter = dataCenter;
    }

    public EntityModel<String> getClusterName() {
        return clusterName;
    }

    public void setClusterName(EntityModel<String> clusterName) {
        this.clusterName = clusterName;
    }

    public EntityModel<String> getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(EntityModel<String> volumeName) {
        this.volumeName = volumeName;
    }

    public EntityModel<String> getSnapshotName() {
        return snapshotName;
    }

    public void setSnapshotName(EntityModel<String> snapshotName) {
        this.snapshotName = snapshotName;
    }

    public EntityModel<String> getDescription() {
        return description;
    }

    public void setDescription(EntityModel<String> description) {
        this.description = description;
    }

    public ListModel<GlusterVolumeSnapshotScheduleRecurrence> getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(ListModel<GlusterVolumeSnapshotScheduleRecurrence> recurrence) {
        this.recurrence = recurrence;
    }

    public ListModel<String> getInterval() {
        return interval;
    }

    public void setInterval(ListModel<String> interval) {
        this.interval = interval;
    }

    public ListModel<EndDateOptions> getEndByOptions() {
        return endByOptions;
    }

    public void setEndByOptions(ListModel<EndDateOptions> endByOptions) {
        this.endByOptions = endByOptions;
    }

    public EntityModel<Date> getEndDate() {
        return endDate;
    }

    public void setEndDate(EntityModel<Date> endDate) {
        this.endDate = endDate;
    }

    public boolean isGeneralTabVisible() {
        return generalTabVisible;
    }

    public void setGeneralTabVisible(boolean generalTabVisible) {
        this.generalTabVisible = generalTabVisible;
    }

    public boolean isScheduleTabVisible() {
        return scheduleTabVisible;
    }

    public void setScheduleTabVisible(boolean scheduleTabVisible) {
        this.scheduleTabVisible = scheduleTabVisible;
    }

    public ListModel<String> getTimeZones() {
        return timeZones;
    }

    public void setTimeZones(ListModel<String> timeZones) {
        this.timeZones = timeZones;
    }

    public EntityModel<Date> getStartAt() {
        return this.startAt;
    }

    public void setStartAt(EntityModel<Date> value) {
        this.startAt = value;
    }

    public EntityModel<Date> getExecutionTime() {
        return this.executionTime;
    }

    public void setExecutionTime(EntityModel<Date> value) {
        this.executionTime = value;
    }

    public ListModel<List<DayOfWeek>> getDaysOfTheWeek() {
        return daysOfWeek;
    }

    public void setDaysOfTheWeek(ListModel<List<DayOfWeek>> daysOfTheWeek) {
        this.daysOfWeek = daysOfTheWeek;
    }

    public ListModel<String> getDaysOfMonth() {
        return daysOfMonth;
    }

    public void setDaysOfMonth(ListModel<String> daysOfMonth) {
        this.daysOfMonth = daysOfMonth;
    }

    public boolean validate() {
        boolean validWeekDays = true;
        boolean validMonthDays = true;
        boolean validEndDate = true;
        getSnapshotName().validateEntity(new IValidation[] { new NotEmptyValidation(), new LengthValidation(128),
                new AsciiNameValidation() });

        if (getRecurrence().getSelectedItem() == GlusterVolumeSnapshotScheduleRecurrence.WEEKLY
                && (getDaysOfTheWeek().getSelectedItem() == null || getDaysOfTheWeek().getSelectedItem().isEmpty())) {
            setMessage(ConstantsManager.getInstance().getConstants().noWeekDaysSelectedMessage());
            validWeekDays = false;
        }

        if (getRecurrence().getSelectedItem() == GlusterVolumeSnapshotScheduleRecurrence.MONTHLY) {
            if (getDaysOfMonth().getSelectedItem() == null || getDaysOfMonth().getSelectedItem().equals("")) {//$NON-NLS-1$
                setMessage(ConstantsManager.getInstance().getConstants().noMonthDaysSelectedMessage());
                validMonthDays = false;
            } else if (getDaysOfMonth().getSelectedItem().contains(",L") || getDaysOfMonth().getSelectedItem().contains("L,")) {//$NON-NLS-1$//$NON-NLS-2$
                setMessage(ConstantsManager.getInstance().getConstants().lastDayMonthCanBeSelectedAlone());
                validMonthDays = false;
            }
        }

        if (getEndByOptions().getSelectedItem() == EndDateOptions.HasEndDate
                && getEndDate().getEntity().compareTo(getStartAt().getEntity()) <= 0) {
                setMessage(ConstantsManager.getInstance().getConstants().endDateBeforeStartDate());
                validEndDate = false;
        }

        return getSnapshotName().getIsValid() && getDaysOfTheWeek().getIsValid() && getDaysOfMonth().getIsValid()
                && validWeekDays && validMonthDays && validEndDate;
    }

    public enum EndDateOptions {
        HasEndDate(ConstantsManager.getInstance().getConstants().endDateOptionText()),
        NoEndDate(ConstantsManager.getInstance().getConstants().noEndDateOptionText());

        private String description;

        private EndDateOptions(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
