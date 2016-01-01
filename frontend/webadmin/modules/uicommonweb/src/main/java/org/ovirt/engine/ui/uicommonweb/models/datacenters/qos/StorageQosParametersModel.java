package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;


public class StorageQosParametersModel extends QosParametersModel<StorageQos> {
    private StorageQosMetricParametersModel throughput;
    private StorageQosMetricParametersModel iops;

    public StorageQosParametersModel() {
        setThroughput(new StorageQosMetricParametersModel(ConfigurationValues.MaxThroughputUpperBoundQosValue,
                ConfigurationValues.MaxReadThroughputUpperBoundQosValue,
                ConfigurationValues.MaxWriteThroughputUpperBoundQosValue));
        setIops(new StorageQosMetricParametersModel(ConfigurationValues.MaxIopsUpperBoundQosValue,
                ConfigurationValues.MaxReadIopsUpperBoundQosValue,
                ConfigurationValues.MaxWriteIopsUpperBoundQosValue));

        getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {

            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if ("IsChangable".equals(args.propertyName)) { //$NON-NLS-1$
                    boolean value = getIsChangable();
                    getThroughput().setIsChangeable(value);
                    getIops().setIsChangeable(value);
                }
            }
        });
    }

    @Override
    public void init(StorageQos qos) {
        initStorageParameterModel(qos.getMaxThroughput(),
                qos.getMaxReadThroughput(),
                qos.getMaxWriteThroughput(),
                getThroughput());
        initStorageParameterModel(qos.getMaxIops(), qos.getMaxReadIops(), qos.getMaxWriteIops(), getIops());
    }

    private void initStorageParameterModel(Integer max,
            Integer maxRead,
            Integer maxWrite,
            StorageQosMetricParametersModel parameterModel) {
        boolean noneSelected = false;
        boolean totalSelected = false;
        boolean readWriteSelected = false;
        if (!(max == null &&
                maxRead == null && maxWrite == null)) {
            if (max != null) {
                totalSelected = true;
            } else {
                readWriteSelected = true;
            }
            parameterModel.getTotal().setEntity(max);
            parameterModel.getRead().setEntity(maxRead);
            parameterModel.getWrite().setEntity(maxWrite);
        } else {
            noneSelected = true;
        }

        parameterModel.getChoiceGroupNone().setEntity(noneSelected);
        parameterModel.getChoiceGroupTotal().setEntity(totalSelected);
        parameterModel.getChoiceGroupReadWrite().setEntity(readWriteSelected);
    }

    @Override
    public void flush(StorageQos storageQos) {
        storageQos.setMaxThroughput(null);
        storageQos.setMaxReadThroughput(null);
        storageQos.setMaxWriteThroughput(null);
        if (getThroughput().getChoiceGroupTotal().getEntity()) {
            storageQos.setMaxThroughput(getThroughput().getTotal().getEntity());
        } else if (getThroughput().getChoiceGroupReadWrite().getEntity()) {
            storageQos.setMaxReadThroughput(getThroughput().getRead().getEntity());
            storageQos.setMaxWriteThroughput(getThroughput().getWrite().getEntity());
        }

        storageQos.setMaxIops(null);
        storageQos.setMaxReadIops(null);
        storageQos.setMaxWriteIops(null);
        if (getIops().getChoiceGroupTotal().getEntity()) {
            storageQos.setMaxIops(getIops().getTotal().getEntity());
        }
        else if (getIops().getChoiceGroupReadWrite().getEntity()) {
            storageQos.setMaxReadIops(getIops().getRead().getEntity());
            storageQos.setMaxWriteIops(getIops().getWrite().getEntity());
        }
    }

    @Override
    public boolean validate() {
        if (!getIsAvailable()) {
            return true;
        }

        getThroughput().validate();
        getIops().validate();

        setIsValid(getThroughput().getIsValid() && getIops().getIsValid());
        return getIsValid();
    }

    public StorageQosMetricParametersModel getThroughput() {
        return throughput;
    }

    public void setThroughput(StorageQosMetricParametersModel throughput) {
        this.throughput = throughput;
    }

    public StorageQosMetricParametersModel getIops() {
        return iops;
    }

    public void setIops(StorageQosMetricParametersModel iops) {
        this.iops = iops;
    }
}
