package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class HostNetworkQosParametersModel extends QosParametersModel<HostNetworkQos> {

    private EntityModel<Integer> outAverageLinkshare = new EntityModel<Integer>();
    private EntityModel<Integer> outAverageUpperlimit = new EntityModel<Integer>();
    private EntityModel<Integer> outAverageRealtime = new EntityModel<Integer>();

    public EntityModel<Integer> getOutAverageLinkshare() {
        return outAverageLinkshare;
    }

    public EntityModel<Integer> getOutAverageUpperlimit() {
        return outAverageUpperlimit;
    }

    public EntityModel<Integer> getOutAverageRealtime() {
        return outAverageRealtime;
    }

    public HostNetworkQosParametersModel() {
        getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {

            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev,
                    Object sender,
                    PropertyChangedEventArgs args) {
                boolean value = getIsChangable();
                getOutAverageLinkshare().setIsChangable(value);
                getOutAverageUpperlimit().setIsChangable(value);
                getOutAverageRealtime().setIsChangable(value);
            }
        });
    }

    @Override
    public void init(HostNetworkQos qos) {
        if (qos == null) {
            qos = new HostNetworkQos();
        }

        getOutAverageLinkshare().setEntity(qos.getOutAverageLinkshare());
        getOutAverageUpperlimit().setEntity(qos.getOutAverageUpperlimit());
        getOutAverageRealtime().setEntity(qos.getOutAverageRealtime());
    }

    @Override
    public void flush(HostNetworkQos qos) {
        qos.setOutAverageLinkshare(getOutAverageLinkshare().getEntity());
        qos.setOutAverageUpperlimit(getOutAverageUpperlimit().getEntity());
        qos.setOutAverageRealtime(getOutAverageRealtime().getEntity());
    }

    @Override
    public boolean validate() {
        if (!getIsChangable() || !getIsAvailable()) {
            return true;
        }

        getOutAverageLinkshare().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new IntegerValidation(1, (Integer) AsyncDataProvider.getInstance()
                        .getConfigValuePreConverted(ConfigurationValues.MaxHostNetworkQosShares)) });

        IValidation[] rateRangeValidation =
                new IValidation[] { new IntegerValidation(0, (Integer) AsyncDataProvider.getInstance()
                        .getConfigValuePreConverted(ConfigurationValues.MaxAverageNetworkQoSValue)) };
        getOutAverageUpperlimit().validateEntity(rateRangeValidation);
        getOutAverageRealtime().validateEntity(rateRangeValidation);

        setIsValid(getOutAverageLinkshare().getIsValid() && getOutAverageUpperlimit().getIsValid()
                && getOutAverageRealtime().getIsValid());
        return getIsValid();
    }

}
