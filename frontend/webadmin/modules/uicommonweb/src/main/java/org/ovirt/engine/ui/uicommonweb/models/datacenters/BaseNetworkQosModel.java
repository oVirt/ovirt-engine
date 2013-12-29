package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class BaseNetworkQosModel extends Model {

    private NetworkQosParametersModel inbound;
    private NetworkQosParametersModel outbound;

    protected NetworkQoS networkQoS = new NetworkQoS();

    public NetworkQosParametersModel getInbound() {
        return inbound;
    }

    public void setInbound(NetworkQosParametersModel inbound) {
        this.inbound = inbound;
    }

    public NetworkQosParametersModel getOutbound() {
        return outbound;
    }

    public void setOutbound(NetworkQosParametersModel outbound) {
        this.outbound = outbound;
    }

    public BaseNetworkQosModel() {
        setInbound(new NetworkQosParametersModel());
        setOutbound(new NetworkQosParametersModel());
        getInbound().getAverage()
                .setEntity((Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.QoSInboundAverageDefaultValue));
        getInbound().getPeak()
                .setEntity((Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.QoSInboundPeakDefaultValue));
        getInbound().getBurst()
                .setEntity((Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.QoSInboundBurstDefaultValue));
        getOutbound().getAverage()
                .setEntity((Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.QoSOutboundAverageDefaultValue));
        getOutbound().getPeak()
                .setEntity((Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.QoSOutboundPeakDefaultValue));
        getOutbound().getBurst()
                .setEntity((Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.QoSOutboundBurstDefaultValue));
    }

    protected boolean validate() {
        getInbound().validate();
        getOutbound().validate();

        setIsValid(getInbound().getIsValid() && getOutbound().getIsValid());
        return getIsValid();
    }

    protected void flush() {
        if (getInbound().getEnabled().getEntity()) {
            networkQoS.setInboundAverage(getInbound().getAverage().getEntity());
            networkQoS.setInboundPeak(getInbound().getPeak().getEntity());
            networkQoS.setInboundBurst(getInbound().getBurst().getEntity());
        } else {
            networkQoS.setInboundAverage(null);
            networkQoS.setInboundPeak(null);
            networkQoS.setInboundBurst(null);
        }

        if (getOutbound().getEnabled().getEntity()) {
            networkQoS.setOutboundAverage(getOutbound().getAverage().getEntity());
            networkQoS.setOutboundPeak(getOutbound().getPeak().getEntity());
            networkQoS.setOutboundBurst(getOutbound().getBurst().getEntity());
        } else {
            networkQoS.setOutboundAverage(null);
            networkQoS.setOutboundPeak(null);
            networkQoS.setOutboundBurst(null);
        }
    }

}
