package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotScheduleRecurrence;
import org.ovirt.engine.core.compat.DayOfWeek;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class GlusterVolumeSnapshotModel extends EntityModel<GlusterVolumeEntity> {
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
    private ListModel<Map.Entry<String, String>> timeZones;
    private EntityModel<Date> startAt;
    private EntityModel<Date> executionTime;
    private ListModel<List<DayOfWeek>> daysOfWeek;
    private ListModel<String> daysOfMonth;
    //Listeners should be registered only once and not initially itself but only after a first validation occurs after user clicks on ok.
    private boolean listenersRegistered = false;
    private EntityModel<Boolean> disableCliSchedule;

    public GlusterVolumeSnapshotModel(boolean generalTabVisible, boolean scheduleTabVisible) {
        init();
        setGeneralTabVisible(generalTabVisible);
        setScheduleTabVisible(scheduleTabVisible);
        disableCliSchedule.setEntity(false);
        disableCliSchedule.setIsAvailable(false);
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
        setTimeZones(new ListModel<Map.Entry<String, String>>());
        setDaysOfMonth(new ListModel<String>());
        setStartAt(new EntityModel<>(new Date()));
        setEndDate(new EntityModel<>(new Date()));
        setExecutionTime(new EntityModel<>(new Date()));
        setDisableCliSchedule(new EntityModel<>(false));
        initIntervals();
        initTimeZones();

        recurrence.setItems(Arrays.asList(GlusterVolumeSnapshotScheduleRecurrence.values()),
                GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN);
        endByOptions.setItems(Arrays.asList(EndDateOptions.values()));

        List<String> values = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            values.add(day.toString().substring(0, 3));
        }
        daysOfWeek = new ListModel<>();
        List<DayOfWeek> daysList = Arrays.asList(DayOfWeek.values());
        List<List<DayOfWeek>> list = new ArrayList<>();
        list.add(daysList);
        daysOfWeek.setItems(list, new ArrayList<DayOfWeek>());
    }

    private void initValueChangeListeners() {
        IEventListener<EventArgs> onPropertyChangeValidate = (ev, sender, args) -> validate(true);
        getSnapshotName().getEntityChangedEvent().addListener(onPropertyChangeValidate);

        getDaysOfTheWeek().getSelectedItemChangedEvent().addListener(onPropertyChangeValidate);

        getDaysOfMonth().getSelectedItemChangedEvent().addListener(onPropertyChangeValidate);

        getEndDate().getEntityChangedEvent().addListener(onPropertyChangeValidate);
    }

    private void validateSnapshotName() {
        getSnapshotName().validateEntity(new IValidation[] { new NotEmptyValidation(), new LengthValidation(128),
                new AsciiNameValidation() });
    }

    private void validateEndDate() {
        if (getRecurrence().getSelectedItem() != GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN
                && getEndByOptions().getSelectedItem() == EndDateOptions.HasEndDate
                && getEndDate().getEntity().compareTo(getStartAt().getEntity()) <= 0) {
            String message = ConstantsManager.getInstance().getConstants().endDateBeforeStartDate();
            getEndDate().setInvalidityReasons(Collections.singletonList(message));
            getEndDate().setIsValid(false);
        } else {
            getEndDate().setInvalidityReasons(new ArrayList<String>());
            getEndDate().setIsValid(true);
        }
    }

    private void validateDaysOfMonth() {
        if (getRecurrence().getSelectedItem() == GlusterVolumeSnapshotScheduleRecurrence.MONTHLY) {
            if (getDaysOfMonth().getSelectedItem() == null || getDaysOfMonth().getSelectedItem().equals("")) {//$NON-NLS-1$
                String message = ConstantsManager.getInstance().getConstants().noMonthDaysSelectedMessage();
                getDaysOfMonth().setInvalidityReasons(Collections.singletonList(message));
                getDaysOfMonth().setIsValid(false);
            } else if (getDaysOfMonth().getSelectedItem().contains(",L") || getDaysOfMonth().getSelectedItem().contains("L,")) {//$NON-NLS-1$//$NON-NLS-2$
                String message = ConstantsManager.getInstance().getConstants().lastDayMonthCanBeSelectedAlone();
                getDaysOfMonth().setInvalidityReasons(Collections.singletonList(message));
                getDaysOfMonth().setIsValid(false);
            } else {
                getDaysOfMonth().setInvalidityReasons(new ArrayList<String>());
                getDaysOfMonth().setIsValid(true);
            }
        }
    }

    private void validateDaysOfWeek() {
        if (getRecurrence().getSelectedItem() == GlusterVolumeSnapshotScheduleRecurrence.WEEKLY) {
            if (getDaysOfTheWeek().getSelectedItem() == null || getDaysOfTheWeek().getSelectedItem().isEmpty()) {
                String message = ConstantsManager.getInstance().getConstants().noWeekDaysSelectedMessage();
                getDaysOfTheWeek().setInvalidityReasons(Collections.singletonList(message));
                getDaysOfTheWeek().setIsValid(false);
            } else {
                getDaysOfTheWeek().setInvalidityReasons(new ArrayList<String>());
                getDaysOfTheWeek().setIsValid(true);
            }
        }
    }

    private void initIntervals() {
        List<String> intervals = new ArrayList<>();
        int mins = 0;
        for (int nThMin = 1; mins < 55; nThMin++) {
            mins = nThMin * 5;
            intervals.add(String.valueOf(mins));
        }
        getInterval().setItems(intervals);
    }

    private void initTimeZones() {
        Map<String, String> timeZones = AsyncDataProvider.getInstance().getTimezones(TimeZoneType.GENERAL_TIMEZONE);
        getTimeZones().setItems(timeZones.entrySet());
        getTimeZones().setSelectedItem(Linq.firstOrNull(timeZones.entrySet(),
                item -> item.getKey().equals("Etc/GMT"))); //$NON-NLS-1$
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

    public ListModel<Map.Entry<String, String>> getTimeZones() {
        return timeZones;
    }

    public void setTimeZones(ListModel<Map.Entry<String, String>> timeZones) {
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

    public EntityModel<Boolean> getDisableCliSchedule() {
        return this.disableCliSchedule;
    }

    public void setDisableCliSchedule(EntityModel<Boolean> value) {
        this.disableCliSchedule = value;
    }

    public boolean validate(boolean inplaceValidate) {
        String propName;

        validateSnapshotName();

        validateDaysOfWeek();

        validateDaysOfMonth();

        validateEndDate();

        if (!listenersRegistered) {
            initValueChangeListeners();
            listenersRegistered = true;
        }

        if(inplaceValidate) {
            propName = "modelPropertiesChanged";//$NON-NLS-1$
        } else {
            propName = "validateAndSwitchAppropriateTab";//$NON-NLS-1$
        }
        onPropertyChanged(new PropertyChangedEventArgs(propName));

        return getSnapshotName().getIsValid() && getDaysOfTheWeek().getIsValid() && getDaysOfMonth().getIsValid()
                && getDaysOfTheWeek().getIsValid() && getDaysOfMonth().getIsValid() && getEndDate().getIsValid();
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

    public static GlusterVolumeSnapshotModel createVolumeSnapshotModel(ICommandTarget commandTarget, GlusterVolumeEntity volumeEntity) {
        final GlusterVolumeSnapshotModel snapshotModel =
                new GlusterVolumeSnapshotModel(true, !volumeEntity.getSnapshotScheduled());

        snapshotModel.setHelpTag(HelpTag.new_volume_snapshot);
        snapshotModel.setHashName("new_volume_snapshot"); //$NON-NLS-1$
        snapshotModel.setTitle(ConstantsManager.getInstance().getConstants().createScheduleVolumeSnapshotTitle());
        return snapshotModel;
    }
}
