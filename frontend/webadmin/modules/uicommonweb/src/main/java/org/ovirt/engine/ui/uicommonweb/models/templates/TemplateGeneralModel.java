package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Translator;

@SuppressWarnings("unused")
public class TemplateGeneralModel extends EntityModel
{

    @Override
    public VmTemplate getEntity()
    {
        if (super.getEntity() == null)
        {
            return null;
        }
        if (super.getEntity() instanceof VmTemplate)
        {
            return (VmTemplate) super.getEntity();
        }
        else
        {
            java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>> pair =
                    (java.util.Map.Entry<VmTemplate, java.util.ArrayList<DiskImage>>) super.getEntity();
            return pair.getKey();
        }
    }

    public void setEntity(VmTemplate value)
    {
        super.setEntity(value);
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

    private String description;

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String value)
    {
        if (!StringHelper.stringsEqual(description, value))
        {
            description = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Description"));
        }
    }

    private String hostCluster;

    public String getHostCluster()
    {
        return hostCluster;
    }

    public void setHostCluster(String value)
    {
        if (!StringHelper.stringsEqual(hostCluster, value))
        {
            hostCluster = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HostCluster"));
        }
    }

    private String definedMemory;

    public String getDefinedMemory()
    {
        return definedMemory;
    }

    public void setDefinedMemory(String value)
    {
        if (!StringHelper.stringsEqual(definedMemory, value))
        {
            definedMemory = value;
            OnPropertyChanged(new PropertyChangedEventArgs("DefinedMemory"));
        }
    }

    private String cpuInfo;

    public String getCpuInfo()
    {
        return cpuInfo;
    }

    public void setCpuInfo(String value)
    {
        if (!StringHelper.stringsEqual(cpuInfo, value))
        {
            cpuInfo = value;
            OnPropertyChanged(new PropertyChangedEventArgs("CpuInfo"));
        }
    }

    private String os;

    public String getOS()
    {
        return os;
    }

    public void setOS(String value)
    {
        if (!StringHelper.stringsEqual(os, value))
        {
            os = value;
            OnPropertyChanged(new PropertyChangedEventArgs("OS"));
        }
    }

    private String defaultDisplayType;

    public String getDefaultDisplayType()
    {
        return defaultDisplayType;
    }

    public void setDefaultDisplayType(String value)
    {
        if (!StringHelper.stringsEqual(defaultDisplayType, value))
        {
            defaultDisplayType = value;
            OnPropertyChanged(new PropertyChangedEventArgs("DefaultDisplayType"));
        }
    }

    private String origin;

    public String getOrigin()
    {
        return origin;
    }

    public void setOrigin(String value)
    {
        if (!StringHelper.stringsEqual(origin, value))
        {
            origin = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Origin"));
        }
    }

    private boolean hasHighlyAvailable;

    public boolean getHasHighlyAvailable()
    {
        return hasHighlyAvailable;
    }

    public void setHasHighlyAvailable(boolean value)
    {
        if (hasHighlyAvailable != value)
        {
            hasHighlyAvailable = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasHighlyAvailable"));
        }
    }

    private boolean isHighlyAvailable;

    public boolean getIsHighlyAvailable()
    {
        return isHighlyAvailable;
    }

    public void setIsHighlyAvailable(boolean value)
    {
        if (isHighlyAvailable != value)
        {
            isHighlyAvailable = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsHighlyAvailable"));
        }
    }

    private boolean hasPriority;

    public boolean getHasPriority()
    {
        return hasPriority;
    }

    public void setHasPriority(boolean value)
    {
        if (hasPriority != value)
        {
            hasPriority = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasPriority"));
        }
    }

    private String priority;

    public String getPriority()
    {
        return priority;
    }

    public void setPriority(String value)
    {
        if (!StringHelper.stringsEqual(priority, value))
        {
            priority = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Priority"));
        }
    }

    private boolean hasMonitorCount;

    public boolean getHasMonitorCount()
    {
        return hasMonitorCount;
    }

    public void setHasMonitorCount(boolean value)
    {
        if (hasMonitorCount != value)
        {
            hasMonitorCount = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasMonitorCount"));
        }
    }

    private int monitorCount;

    public int getMonitorCount()
    {
        return monitorCount;
    }

    public void setMonitorCount(int value)
    {
        if (monitorCount != value)
        {
            monitorCount = value;
            OnPropertyChanged(new PropertyChangedEventArgs("MonitorCount"));
        }
    }

    private boolean hasDomain;

    public boolean getHasDomain()
    {
        return hasDomain;
    }

    public void setHasDomain(boolean value)
    {
        if (hasDomain != value)
        {
            hasDomain = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasDomain"));
        }
    }

    private boolean hasTimeZone;

    public boolean getHasTimeZone()
    {
        return hasTimeZone;
    }

    public void setHasTimeZone(boolean value)
    {
        if (hasTimeZone != value)
        {
            hasTimeZone = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasTimeZone"));
        }
    }

    private String domain;

    public String getDomain()
    {
        return domain;
    }

    public void setDomain(String value)
    {
        if (!StringHelper.stringsEqual(domain, value))
        {
            domain = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Domain"));
        }
    }

    private String timeZone;

    public String getTimeZone()
    {
        return timeZone;
    }

    public void setTimeZone(String value)
    {
        if (!StringHelper.stringsEqual(timeZone, value))
        {
            timeZone = value;
            OnPropertyChanged(new PropertyChangedEventArgs("TimeZone"));
        }
    }

    private boolean hasUsbPolicy;

    public boolean getHasUsbPolicy()
    {
        return hasUsbPolicy;
    }

    public void setHasUsbPolicy(boolean value)
    {
        if (hasUsbPolicy != value)
        {
            hasUsbPolicy = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasUsbPolicy"));
        }
    }

    private String usbPolicy;

    public String getUsbPolicy()
    {
        return usbPolicy;
    }

    public void setUsbPolicy(String value)
    {
        if (!StringHelper.stringsEqual(usbPolicy, value))
        {
            usbPolicy = value;
            OnPropertyChanged(new PropertyChangedEventArgs("UsbPolicy"));
        }
    }

    private boolean isStateless;

    public boolean getIsStateless()
    {
        return isStateless;
    }

    public void setIsStateless(boolean value)
    {
        if (isStateless != value)
        {
            isStateless = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsStateless"));
        }
    }

    public TemplateGeneralModel()
    {
        setTitle("General");
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (super.getEntity() != null)
        {
            UpdateProperties();
        }
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        UpdateProperties();
    }

    private void UpdateProperties()
    {
        VmTemplate template = getEntity();

        setName(template.getname());
        setDescription(template.getdescription());
        setHostCluster(template.getvds_group_name());
        setDefinedMemory(template.getmem_size_mb() + " MB");
        setIsHighlyAvailable(template.getauto_startup());
        setPriority(PriorityToString(template.getpriority()));
        setMonitorCount(template.getnum_of_monitors());
        setCpuInfo(StringFormat.format("%1$s (%2$s Socket(s), %3$s Core(s) per Socket)",
                template.getnum_of_cpus(),
                template.getnum_of_sockets(),
                template.getcpu_per_socket()));

        Translator translator = EnumTranslator.Create(VmOsType.class);
        setOS(translator.get(template.getos()));

        translator = EnumTranslator.Create(DisplayType.class);
        setDefaultDisplayType(translator.get(template.getdefault_display_type()));

        translator = EnumTranslator.Create(OriginType.class);
        setOrigin(translator.get(template.getorigin()));

        setHasDomain(DataProvider.IsWindowsOsType(template.getos()));
        setDomain(template.getdomain());

        setHasTimeZone(DataProvider.IsWindowsOsType(template.getos()));
        setTimeZone(template.gettime_zone());

        setHasUsbPolicy(true);
        translator = EnumTranslator.Create(UsbPolicy.class);
        setUsbPolicy(translator.get(template.getusb_policy()));

        setIsStateless(template.getis_stateless());
    }

    public String PriorityToString(int value)
    {
        String priorityStr;
        int highPriority = DataProvider.GetMaxVmPriority();
        int roundedPriority = DataProvider.RoundPriority(value);

        if (roundedPriority == 1)
        {
            priorityStr = "Low";
        }
        else if (roundedPriority == highPriority / 2)
        {
            priorityStr = "Medium";
        }
        else if (roundedPriority == highPriority)
        {
            priorityStr = "High";
        }
        else
        {
            priorityStr = "Unknown";
        }

        return priorityStr;
    }
}
