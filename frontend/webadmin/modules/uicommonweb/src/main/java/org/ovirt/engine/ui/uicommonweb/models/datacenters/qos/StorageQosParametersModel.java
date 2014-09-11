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
                    getThroughput().setIsChangable(value);
                    getIops().setIsChangable(value);
                }
            }
        });
    }

    @Override
    public void init(StorageQos qos) {
        if (qos.getMaxThroughput() == null
                && qos.getMaxReadThroughput() == null
                && qos.getMaxWriteThroughput() == null) {
            getThroughput().getEnabled().setEntity(false);
        } else {
            getThroughput().getTotal().setEntity(qos.getMaxThroughput());
            getThroughput().getRead().setEntity(qos.getMaxReadThroughput());
            getThroughput().getWrite().setEntity(qos.getMaxWriteThroughput());
            getThroughput().getEnabled().setEntity(true);
        }

        if (qos.getMaxIops() == null
                && qos.getMaxReadIops() == null
                && qos.getMaxWriteIops() == null) {
            getIops().getEnabled().setEntity(false);
        } else {
            getIops().getTotal().setEntity(qos.getMaxIops());
            getIops().getRead().setEntity(qos.getMaxReadIops());
            getIops().getWrite().setEntity(qos.getMaxWriteIops());
            getIops().getEnabled().setEntity(true);
        }
    }

    @Override
    public void flush(StorageQos storageQos) {
        if (getThroughput().getEnabled().getEntity()) {
            storageQos.setMaxThroughput(getThroughput().getTotal().getEntity());
            storageQos.setMaxReadThroughput(getThroughput().getRead().getEntity());
            storageQos.setMaxWriteThroughput(getThroughput().getWrite().getEntity());
        } else {
            storageQos.setMaxThroughput(null);
            storageQos.setMaxReadThroughput(null);
            storageQos.setMaxWriteThroughput(null);
        }

        if (getIops().getEnabled().getEntity()) {
            storageQos.setMaxIops(getIops().getTotal().getEntity());
            storageQos.setMaxReadIops(getIops().getRead().getEntity());
            storageQos.setMaxWriteIops(getIops().getWrite().getEntity());
        } else {
            storageQos.setMaxIops(null);
            storageQos.setMaxReadIops(null);
            storageQos.setMaxWriteIops(null);
        }
    }

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
