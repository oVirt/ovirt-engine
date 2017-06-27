package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public class NetworkQosParametersModel extends Model {

    private EntityModel<Integer> average;
    private EntityModel<Integer> peak;
    private EntityModel<Integer> burst;
    private EntityModel<Boolean> enabled;

    public EntityModel<Integer> getAverage() {
        return average;
    }

    private void setAverage(EntityModel<Integer> average) {
        this.average = average;
    }

    public EntityModel<Integer> getPeak() {
        return peak;
    }

    private void setPeak(EntityModel<Integer> peak) {
        this.peak = peak;
    }

    public EntityModel<Integer> getBurst() {
        return burst;
    }

    private void setBurst(EntityModel<Integer> burst) {
        this.burst = burst;
    }

    public EntityModel<Boolean> getEnabled() {
        return enabled;
    }

    private void setEnabled(EntityModel<Boolean> enabled) {
        this.enabled = enabled;
    }

    public NetworkQosParametersModel() {
        setAverage(new EntityModel<Integer>());
        setPeak(new EntityModel<Integer>());
        setBurst(new EntityModel<Integer>());
        setEnabled(new EntityModel<>(true));
        getEnabled().getPropertyChangedEvent().addListener(this);
        getPropertyChangedEvent().addListener(this);
    }

    public boolean validate() {
        if (!getEnabled().getEntity()) {
            return true;
        }

        getAverage().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new IntegerValidation(0,
                                      (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.MaxAverageNetworkQoSValue)) });
        getPeak().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new IntegerValidation(0,
                                      (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.MaxPeakNetworkQoSValue)) });
        getBurst().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new IntegerValidation(0,
                                      (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.MaxBurstNetworkQoSValue)) });

        setIsValid(getAverage().getIsValid() && getPeak().getIsValid() && getBurst().getIsValid());
        return getIsValid();
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (getEnabled().equals(sender)) {
            updateChangeability();
        } else if (this.equals(sender)) {
            getEnabled().setIsChangeable(getIsChangable());
        }
    }

    private void updateChangeability() {
        boolean enabled = getIsChangable() && getEnabled().getEntity();
        getAverage().setIsChangeable(enabled);
        getPeak().setIsChangeable(enabled);
        getBurst().setIsChangeable(enabled);
    }

}
