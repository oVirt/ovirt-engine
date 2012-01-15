package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.Model;

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
            OnPropertyChanged(new PropertyChangedEventArgs("Status"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("Name"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("Address"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("MAC"));
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
        // C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value
        // logic:
        if (speed == null || !speed.equals(value))
        {
            speed = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Speed"));
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
        // C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value
        // logic:
        if (rxRate == null || !rxRate.equals(value))
        {
            rxRate = value;
            OnPropertyChanged(new PropertyChangedEventArgs("RxRate"));
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
        // C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value
        // logic:
        if (rxDrop == null || !rxDrop.equals(value))
        {
            rxDrop = value;
            OnPropertyChanged(new PropertyChangedEventArgs("RxDrop"));
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
        // C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value
        // logic:
        if (txRate == null || !txRate.equals(value))
        {
            txRate = value;
            OnPropertyChanged(new PropertyChangedEventArgs("TxRate"));
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
        // C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value
        // logic:
        if (txDrop == null || !txDrop.equals(value))
        {
            txDrop = value;
            OnPropertyChanged(new PropertyChangedEventArgs("TxDrop"));
        }
    }

}
