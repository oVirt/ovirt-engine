package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public class StorageQosMetricParametersModel extends Model {
    private EntityModel<Integer> total;
    private EntityModel<Integer> read;
    private EntityModel<Integer> write;
    private EntityModel<Boolean> enabled;
    private final ConfigurationValues maxTotal;
    private final ConfigurationValues maxRead;
    private final ConfigurationValues maxWrite;

    public StorageQosMetricParametersModel(ConfigurationValues maxTotal,
            ConfigurationValues maxRead,
            ConfigurationValues maxWrite) {
        this.maxTotal = maxTotal;
        this.maxRead = maxRead;
        this.maxWrite = maxWrite;
        setTotal(new EntityModel<Integer>());
        setRead(new EntityModel<Integer>());
        setWrite(new EntityModel<Integer>());
        setEnabled(new EntityModel<Boolean>());
        getEnabled().getPropertyChangedEvent().addListener(this);
        getPropertyChangedEvent().addListener(this);
    }

    public EntityModel<Integer> getTotal() {
        return total;
    }

    public void setTotal(EntityModel<Integer> total) {
        this.total = total;
    }

    public EntityModel<Integer> getRead() {
        return read;
    }

    public void setRead(EntityModel<Integer> read) {
        this.read = read;
    }

    public EntityModel<Integer> getWrite() {
        return write;
    }

    public void setWrite(EntityModel<Integer> write) {
        this.write = write;
    }

    public EntityModel<Boolean> getEnabled() {
        return enabled;
    }

    public void setEnabled(EntityModel<Boolean> enabled) {
        this.enabled = enabled;
    }

    public boolean validate() {
        if (!getEnabled().getEntity()) {
            return true;
        }

        validateValue(getTotal(), (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(maxTotal));
        validateValue(getRead(), (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(maxRead));
        validateValue(getWrite(), (Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(maxWrite));

        setIsValid(getTotal().getIsValid() && getRead().getIsValid() && getWrite().getIsValid());
        return getIsValid();
    }

    private void validateValue(EntityModel<Integer> entity, Integer maxValue) {
        entity.validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new IntegerValidation(0, maxValue) });
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (getEnabled().equals(sender)) {
            updateChangeability();
        } else if (this.equals(sender)) {
            getEnabled().setIsChangable(getIsChangable());
        }
    }

    private void updateChangeability() {
        boolean enabled = getIsChangable() && getEnabled().getEntity();
        getTotal().setIsChangable(enabled);
        getRead().setIsChangable(enabled);
        getWrite().setIsChangable(enabled);
    }
}
