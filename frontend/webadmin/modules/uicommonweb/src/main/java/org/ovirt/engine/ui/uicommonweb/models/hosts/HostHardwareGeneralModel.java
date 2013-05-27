package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class HostHardwareGeneralModel extends EntityModel
{
    @Override
    public VDS getEntity()
    {
        return (VDS) super.getEntity();
    }

    private String hardwareManufacturer;

    public String getHardwareManufacturer()
    {
        return hardwareManufacturer;
    }

    public void setHardwareManufacturer(String value)
    {
        if (!StringHelper.stringsEqual(hardwareManufacturer, value))
        {
            hardwareManufacturer = value;
            onPropertyChanged(new PropertyChangedEventArgs("manufacturer")); //$NON-NLS-1$
        }
    }

    private String hardwareProductName;

    public String getHardwareProductName()
    {
        return hardwareProductName;
    }

    public void setHardwareProductName(String value)
    {
        if (!StringHelper.stringsEqual(hardwareProductName, value))
        {
            hardwareProductName = value;
            onPropertyChanged(new PropertyChangedEventArgs("productName")); //$NON-NLS-1$
        }
    }

    private String hardwareSerialNumber;

    public String getHardwareSerialNumber()
    {
        return hardwareSerialNumber;
    }

    public void setHardwareSerialNumber(String value)
    {
        if (!StringHelper.stringsEqual(hardwareSerialNumber, value))
        {
            hardwareSerialNumber = value;
            onPropertyChanged(new PropertyChangedEventArgs("serialNumber")); //$NON-NLS-1$
        }
    }

    private String hardwareVersion;

    public String getHardwareVersion()
    {
        return hardwareVersion;
    }

    public void setHardwareVersion(String value)
    {
        if (!StringHelper.stringsEqual(hardwareVersion, value))
        {
            hardwareVersion = value;
            onPropertyChanged(new PropertyChangedEventArgs("hardwareVersion")); //$NON-NLS-1$
        }
    }

    private String hardwareUUID;

    public String getHardwareUUID()
    {
        return hardwareUUID;
    }

    public void setHardwareUUID(String value)
    {
        if (!StringHelper.stringsEqual(hardwareUUID, value))
        {
            hardwareUUID = value;
            onPropertyChanged(new PropertyChangedEventArgs("uuid")); //$NON-NLS-1$
        }
    }

    private String hardwareFamily;

    public String getHardwareFamily()
    {
        return hardwareFamily;
    }

    @SuppressWarnings("deprecation")
    public void setHardwareFamily(String value)
    {
        if (!StringHelper.stringsEqual(hardwareFamily, value))
        {
            hardwareFamily = value;
            onPropertyChanged(new PropertyChangedEventArgs("family")); //$NON-NLS-1$
        }
    }

    public enum HbaDeviceKeys { MODEL_NAME, // Model name field
                                TYPE,       // Device type
                                WWNN,       // WWNN of the NIC
                                WWNPS       // Comma separated list of WWNPs (port ids)
    };

    private List<EnumMap<HbaDeviceKeys, String>> hbaDevices;

    public List<EnumMap<HbaDeviceKeys, String>> getHbaDevices() {
        return hbaDevices;
    }

    public void setHbaDevices(List<EnumMap<HbaDeviceKeys, String>> hbaDevices) {
        this.hbaDevices = hbaDevices;
    }

    public HostHardwareGeneralModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHashName("hardware"); //$NON-NLS-1$
        setAvailableInModes(ApplicationMode.VirtOnly);
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);
    }

    private void updateProperties()
    {
        VDS vds = getEntity();
        setHardwareManufacturer(vds.getHardwareManufacturer());
        setHardwareVersion(vds.getHardwareVersion());
        setHardwareProductName(vds.getHardwareProductName());
        setHardwareUUID(vds.getHardwareUUID());
        setHardwareSerialNumber(vds.getHardwareSerialNumber());
        setHardwareFamily(vds.getHardwareFamily());

        /* Go through the list of HBA devices and transfer the necessary info
           to the GWT host hardware model */
        List<EnumMap<HbaDeviceKeys, String>> hbaDevices = new ArrayList<EnumMap<HbaDeviceKeys, String>>();
        List<Map<String, String>> fcDevices = vds.getHBAs().get("FC"); //$NON-NLS-1$

        if (fcDevices != null) {
            for (Map<String, String> device: fcDevices) {
                EnumMap<HbaDeviceKeys, String> deviceModel = new EnumMap<HbaDeviceKeys, String>(HbaDeviceKeys.class);
                deviceModel.put(HbaDeviceKeys.MODEL_NAME, device.get("model")); //$NON-NLS-1$
                deviceModel.put(HbaDeviceKeys.WWNN, device.get("wwnn")); //$NON-NLS-1$
                deviceModel.put(HbaDeviceKeys.WWNPS, device.get("wwpn")); //$NON-NLS-1$
                deviceModel.put(HbaDeviceKeys.TYPE, "FC"); //$NON-NLS-1$

                hbaDevices.add(deviceModel);
            }
        }

        setHbaDevices(hbaDevices);
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        if (getEntity() != null)
        {
            updateProperties();
        }
    }
}
