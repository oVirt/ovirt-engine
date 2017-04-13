package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.StorageSyncSchedule;
import org.ovirt.engine.core.common.businessentities.gluster.StorageSyncSchedule.Frequency;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

/*
 * Disaster Recovery configuration for Storage Domain that setups up
 * syncing of data to remote site
 */
public class StorageDRModel extends Model {

    private static final int MAX_MINUTE = 60;
    private static final int MAX_HOUR = 24;
    ListModel<StorageSyncSchedule.Frequency> frequency;
    ListModel<List<StorageSyncSchedule.Day>> days;
    ListModel<Integer> hour;
    ListModel<Integer> mins;

    ListModel<GlusterGeoRepSession> geoRepSession;
    EntityModel<Boolean> noSync;
    EntityModel<StorageDomain> storageDomain;

    public ListModel<StorageSyncSchedule.Frequency> getFrequency() {
        return frequency;
    }

    public void setFrequency(ListModel<StorageSyncSchedule.Frequency> frequency) {
        this.frequency = frequency;
    }

    public ListModel<Integer> getHour() {
        return hour;
    }

    public void setHour(ListModel<Integer> hour) {
        this.hour = hour;
    }

    public ListModel<Integer> getMins() {
        return mins;
    }

    public void setMins(ListModel<Integer> mins) {
        this.mins = mins;
    }

    public ListModel<GlusterGeoRepSession> getGeoRepSession() {
        return geoRepSession;
    }

    public void setGeoRepSession(ListModel<GlusterGeoRepSession> geoRepSession) {
        this.geoRepSession = geoRepSession;
    }

    public EntityModel<Boolean> getNoSync() {
        return noSync;
    }

    public void setNoSync(EntityModel<Boolean> noSync) {
        this.noSync = noSync;
    }

    public EntityModel<StorageDomain> getStorageDomain() {
        return storageDomain;
    }

    public void setStorageDomain(EntityModel<StorageDomain> storageDomain) {
        this.storageDomain = storageDomain;
    }

    public ListModel<List<StorageSyncSchedule.Day>> getDays() {
        return days;
    }

    public void setDays(ListModel<List<StorageSyncSchedule.Day>> days) {
        this.days = days;
    }

    public StorageDRModel() {
        setFrequency(new ListModel<StorageSyncSchedule.Frequency>());
        setHour(new ListModel<Integer>());
        setMins(new ListModel<Integer>());
        setGeoRepSession(new ListModel<GlusterGeoRepSession>());
        setNoSync(new EntityModel<Boolean>());
        setStorageDomain(new EntityModel<StorageDomain>());

        getNoSync().setEntity(false);
        frequency.setItems(Arrays.asList(StorageSyncSchedule.Frequency.values()));
        days = new ListModel<>();
        List<StorageSyncSchedule.Day> daysList = Arrays.asList(StorageSyncSchedule.Day.values());
        List<List<StorageSyncSchedule.Day>> list = new ArrayList<>();
        list.add(daysList);
        days.setItems(list, new ArrayList<StorageSyncSchedule.Day>());

        List<Integer> hours = new ArrayList<>();
        for (int i = 0; i < MAX_HOUR; i++) {
            hours.add(i);
        }
        hour.setItems(hours);
        List<Integer> minutes = new ArrayList<>();
        for (int i = 0; i < MAX_MINUTE; i++) {
            minutes.add(i);
        }
        mins.setItems(minutes);

        getFrequency().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            boolean weekly = getFrequency().getSelectedItem() == Frequency.WEEKLY;
            boolean daily = getFrequency().getSelectedItem() == Frequency.DAILY;
            getDays().setIsAvailable(weekly);
            getHour().setIsAvailable(weekly || daily);
            getMins().setIsAvailable(weekly || daily);
        });

    }

    public boolean validate() {
        getGeoRepSession().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getFrequency().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getHour().validateSelectedItem(
                new IValidation[] { new NotEmptyValidation(), new IntegerValidation(0, MAX_HOUR) }
        );
        getMins().validateSelectedItem(
                new IValidation[] { new NotEmptyValidation(), new IntegerValidation(0, MAX_MINUTE) }
        );
        boolean ret = getGeoRepSession().getIsValid() && getFrequency().getIsValid();
        if (!getFrequency().getSelectedItem().equals(Frequency.NONE)) {
            return ret && getHour().getIsValid() && getMins().getIsValid();
        }
        return ret;
    }


}
