package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class BaseNetworkQosModel extends Model {

    private NetworkQosParametersModel inbound;
    private NetworkQosParametersModel outbound;

    protected NetworkQoS networkQoS;

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
        getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("IsChangable".equals(args.propertyName)) { //$NON-NLS-1$
                boolean value = getIsChangable();
                getInbound().setIsChangeable(value);
                getOutbound().setIsChangeable(value);
            }
        });
    }

    public void init(NetworkQoS qos) {
        if (qos == null) {
            networkQoS = new NetworkQoS();
        } else {
            networkQoS = qos;
        }

        if (networkQoS.getInboundAverage() == null
                || networkQoS.getInboundPeak() == null
                || networkQoS.getInboundBurst() == null) {
            getInbound().getEnabled().setEntity(false);
        } else {
            getInbound().getAverage().setEntity(networkQoS.getInboundAverage());
            getInbound().getPeak().setEntity(networkQoS.getInboundPeak());
            getInbound().getBurst().setEntity(networkQoS.getInboundBurst());
        }

        if (networkQoS.getOutboundAverage() == null
                || networkQoS.getOutboundPeak() == null
                || networkQoS.getOutboundBurst() == null) {
            getOutbound().getEnabled().setEntity(false);
        } else {
            getOutbound().getAverage().setEntity(networkQoS.getOutboundAverage());
            getOutbound().getPeak().setEntity(networkQoS.getOutboundPeak());
            getOutbound().getBurst().setEntity(networkQoS.getOutboundBurst());
        }
    }

    public boolean validate() {
        if (!getIsAvailable()) {
            return true;
        }

        getInbound().validate();
        getOutbound().validate();

        setIsValid(getInbound().getIsValid() && getOutbound().getIsValid());
        return getIsValid();
    }

    public NetworkQoS flush() {
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

        return networkQoS;
    }

}
