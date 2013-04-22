package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class HostInterface extends Model
{

    private VdsNetworkInterface privateInterface;

    public VdsNetworkInterface getInterface()
    {
        return privateInterface;
    }

    public void setInterface(VdsNetworkInterface value)
    {
        privateInterface = value;
    }

    private InterfaceStatus status = InterfaceStatus.values()[0];

    public InterfaceStatus getStatus()
    {
        return status;
    }

    public void setStatus(InterfaceStatus value)
    {
        if (status != value)
        {
            status = value;
            onPropertyChanged(new PropertyChangedEventArgs("Status")); //$NON-NLS-1$
        }
    }

    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(String value)
    {
        if (!StringHelper.stringsEqual(name, value))
        {
            name = value;
            onPropertyChanged(new PropertyChangedEventArgs("Name")); //$NON-NLS-1$
        }
    }

    private String address;

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String value)
    {
        if (!StringHelper.stringsEqual(address, value))
        {
            address = value;
            onPropertyChanged(new PropertyChangedEventArgs("Address")); //$NON-NLS-1$
        }
    }

    private String mac;

    public String getMAC()
    {
        return mac;
    }

    public void setMAC(String value)
    {
        if (!StringHelper.stringsEqual(mac, value))
        {
            mac = value;
            onPropertyChanged(new PropertyChangedEventArgs("MAC")); //$NON-NLS-1$
        }
    }

    private Integer speed;

    public Integer getSpeed()
    {
        return speed;
    }

    public void setSpeed(Integer value)
    {
        if (speed == null && value == null)
        {
            return;
        }
        if (speed == null || !speed.equals(value))
        {
            speed = value;
            onPropertyChanged(new PropertyChangedEventArgs("Speed")); //$NON-NLS-1$
        }
    }

    private Double rxRate;

    public Double getRxRate()
    {
        return rxRate;
    }

    public void setRxRate(Double value)
    {
        if (rxRate == null && value == null)
        {
            return;
        }
        if (rxRate == null || !rxRate.equals(value))
        {
            rxRate = value;
            onPropertyChanged(new PropertyChangedEventArgs("RxRate")); //$NON-NLS-1$
        }
    }

    private Double rxDrop;

    public Double getRxDrop()
    {
        return rxDrop;
    }

    public void setRxDrop(Double value)
    {
        if (rxDrop == null && value == null)
        {
            return;
        }
        if (rxDrop == null || !rxDrop.equals(value))
        {
            rxDrop = value;
            onPropertyChanged(new PropertyChangedEventArgs("RxDrop")); //$NON-NLS-1$
        }
    }

    private Double txRate;

    public Double getTxRate()
    {
        return txRate;
    }

    public void setTxRate(Double value)
    {
        if (txRate == null && value == null)
        {
            return;
        }
        if (txRate == null || !txRate.equals(value))
        {
            txRate = value;
            onPropertyChanged(new PropertyChangedEventArgs("TxRate")); //$NON-NLS-1$
        }
    }

    private Double txDrop;

    public Double getTxDrop()
    {
        return txDrop;
    }

    public void setTxDrop(Double value)
    {
        if (txDrop == null && value == null)
        {
            return;
        }
        if (txDrop == null || !txDrop.equals(value))
        {
            txDrop = value;
            onPropertyChanged(new PropertyChangedEventArgs("TxDrop")); //$NON-NLS-1$
        }
    }

}
