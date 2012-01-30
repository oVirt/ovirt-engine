package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Translator;

@SuppressWarnings("unused")
public class VmGeneralModel extends EntityModel
{

    public static EventDefinition UpdateCompleteEventDefinition;
    private Event privateUpdateCompleteEvent;

    public Event getUpdateCompleteEvent()
    {
        return privateUpdateCompleteEvent;
    }

    private void setUpdateCompleteEvent(Event value)
    {
        privateUpdateCompleteEvent = value;
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

    private String template;

    public String getTemplate()
    {
        return template;
    }

    public void setTemplate(String value)
    {
        if (!StringHelper.stringsEqual(template, value))
        {
            template = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Template"));
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

    private String minAllocatedMemory;

    public String getMinAllocatedMemory()
    {
        return minAllocatedMemory;
    }

    public void setMinAllocatedMemory(String value)
    {
        if (!StringHelper.stringsEqual(minAllocatedMemory, value))
        {
            minAllocatedMemory = value;
            OnPropertyChanged(new PropertyChangedEventArgs("MinAllocatedMemory"));
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

    private String quotaName;

    public String getQuotaName() {
        return quotaName;
    }

    public void setQuotaName(String quotaName) {
        this.quotaName = quotaName;
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

    private boolean hasStorageDomain;

    public boolean getHasStorageDomain()
    {
        return hasStorageDomain;
    }

    public void setHasStorageDomain(boolean value)
    {
        if (hasStorageDomain != value)
        {
            hasStorageDomain = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasStorageDomain"));
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

    private boolean hasAlert;

    public boolean getHasAlert()
    {
        return hasAlert;
    }

    public void setHasAlert(boolean value)
    {
        if (hasAlert != value)
        {
            hasAlert = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasAlert"));
        }
    }

    private String alert;

    public String getAlert()
    {
        return alert;
    }

    public void setAlert(String value)
    {
        if (!StringHelper.stringsEqual(alert, value))
        {
            alert = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Alert"));
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

    private String storageDomain;

    public String getStorageDomain()
    {
        return storageDomain;
    }

    public void setStorageDomain(String value)
    {
        if (!StringHelper.stringsEqual(storageDomain, value))
        {
            storageDomain = value;
            OnPropertyChanged(new PropertyChangedEventArgs("StorageDomain"));
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

    private boolean hasDefaultHost;

    public boolean getHasDefaultHost()
    {
        return hasDefaultHost;
    }

    public void setHasDefaultHost(boolean value)
    {
        if (hasDefaultHost != value)
        {
            hasDefaultHost = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasDefaultHost"));
        }
    }

    private String defaultHost;

    public String getDefaultHost()
    {
        return defaultHost;
    }

    public void setDefaultHost(String value)
    {
        if (!StringHelper.stringsEqual(defaultHost, value))
        {
            defaultHost = value;
            OnPropertyChanged(new PropertyChangedEventArgs("DefaultHost"));
        }
    }

    private boolean hasCustomProperties;

    public boolean getHasCustomProperties()
    {
        return hasCustomProperties;
    }

    public void setHasCustomProperties(boolean value)
    {
        if (hasCustomProperties != value)
        {
            hasCustomProperties = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasCustomProperties"));
        }
    }

    private String customProperties;

    public String getCustomProperties()
    {
        return customProperties;
    }

    public void setCustomProperties(String value)
    {
        if (!StringHelper.stringsEqual(customProperties, value))
        {
            customProperties = value;
            OnPropertyChanged(new PropertyChangedEventArgs("CustomProperties"));
        }
    }

    static
    {
        UpdateCompleteEventDefinition = new EventDefinition("UpdateComplete", VmGeneralModel.class);
    }

    public VmGeneralModel()
    {
        setUpdateCompleteEvent(new Event(UpdateCompleteEventDefinition));

        setTitle("General");
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (getEntity() != null)
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
        VM vm = (VM) getEntity();

        setName(vm.getvm_name());
        setDescription(vm.getvm_description());
        setQuotaName(vm.getStaticData().getQuotaId() != null ? vm.getStaticData().getQuotaId().toString() : "");
        setTemplate(vm.getvmt_name());
        setDefinedMemory(vm.getvm_mem_size_mb() + " MB");
        setMinAllocatedMemory(vm.getMinAllocatedMem() + " MB");

        Translator translator = EnumTranslator.Create(VmOsType.class);
        setOS(translator.get(vm.getvm_os()));

        translator = EnumTranslator.Create(DisplayType.class);
        setDefaultDisplayType(translator.get(vm.getdefault_display_type()));

        translator = EnumTranslator.Create(OriginType.class);
        setOrigin(translator.get(vm.getorigin()));

        setHasHighlyAvailable(vm.getvm_type() == VmType.Server);
        setIsHighlyAvailable(vm.getauto_startup());

        setHasPriority(vm.getvm_type() == VmType.Server);
        setPriority(PriorityToString(vm.getpriority()));

        setHasMonitorCount(vm.getvm_type() == VmType.Desktop);
        setMonitorCount(vm.getnum_of_monitors());

        setHasUsbPolicy(true);
        translator = EnumTranslator.Create(UsbPolicy.class);
        setUsbPolicy(translator.get(vm.getusb_policy()));

        setCpuInfo(vm.getnum_of_cpus() + " " + "(" + vm.getnum_of_sockets() + " Socket(s), " + vm.getcpu_per_socket()
                + " Core(s) per Socket)");

        setHasDomain(DataProvider.IsWindowsOsType(vm.getvm_os()));
        setDomain(vm.getvm_domain());

        setHasTimeZone(DataProvider.IsWindowsOsType(vm.getvm_os()));
        setTimeZone(vm.gettime_zone());

        setHasCustomProperties(!StringHelper.stringsEqual(vm.getCustomProperties(), ""));
        setCustomProperties(getHasCustomProperties() ? "Configured" : "Not-Configured");

        setHasAlert(vm.getVmPauseStatus() != VmPauseStatus.NONE && vm.getVmPauseStatus() != VmPauseStatus.NOERR);
        if (getHasAlert())
        {
            translator = EnumTranslator.Create(VmPauseStatus.class);
            setAlert(translator.get(vm.getVmPauseStatus()));
        }
        else
        {
            setAlert(null);
        }

        setHasDefaultHost(vm.getdedicated_vm_for_vds() != null);
        if (getHasDefaultHost())
        {
            Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Host: cluster = " + vm.getvds_group_name()
                    + " sortby name", SearchType.VDS), new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            VmGeneralModel model = (VmGeneralModel) target;
                            VM localVm = (VM) model.getEntity();
                            if (localVm == null)
                            {
                                return;
                            }
                            java.util.ArrayList<VDS> hosts =
                                    (java.util.ArrayList<VDS>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                            for (VDS host : hosts)
                            {
                                if (localVm.getdedicated_vm_for_vds() != null
                                        && host.getId().equals(localVm.getdedicated_vm_for_vds()))
                                {
                                    model.setDefaultHost(host.getvds_name());
                                    break;
                                }
                            }
                            model.UpdateStorageDomain();

                        }
                    }));
        }
        else
        {
            setDefaultHost("Any Host in Cluster");

            UpdateStorageDomain();
        }
    }

    public void UpdateStorageDomain()
    {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                VmGeneralModel vmGeneralModel = (VmGeneralModel) model;
                Iterable disks = (Iterable) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                java.util.Iterator disksIterator = disks.iterator();
                if (disksIterator.hasNext())
                {
                    vmGeneralModel.setHasStorageDomain(true);

                    AsyncQuery _asyncQuery1 = new AsyncQuery();
                    _asyncQuery1.setModel(vmGeneralModel);
                    _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object model1, Object ReturnValue1)
                        {
                            VmGeneralModel vmGeneralModel1 = (VmGeneralModel) model1;
                            storage_domains storage =
                                    (storage_domains) ((VdcQueryReturnValue) ReturnValue1).getReturnValue();
                            vmGeneralModel1.setStorageDomain(storage.getstorage_name());

                            vmGeneralModel1.getUpdateCompleteEvent().raise(this, EventArgs.Empty);
                        }
                    };

                    DiskImage firstDisk = (DiskImage) disksIterator.next();
                    StorageDomainQueryParametersBase params =
                            new StorageDomainQueryParametersBase(firstDisk.getstorage_ids().get(0));
                    params.setRefresh(false);
                    Frontend.RunQuery(VdcQueryType.GetStorageDomainById, params, _asyncQuery1);
                }
                else
                {
                    vmGeneralModel.setHasStorageDomain(false);

                    vmGeneralModel.getUpdateCompleteEvent().raise(this, EventArgs.Empty);
                }
            }
        };

        VM vm = (VM) getEntity();

        GetAllDisksByVmIdParameters params = new GetAllDisksByVmIdParameters(vm.getId());
        params.setRefresh(false);
        Frontend.RunQuery(VdcQueryType.GetAllDisksByVmId, params, _asyncQuery);
    }

    // TODO: Find a better place for this code. It must be something common,
    // allowing using of converters' code available in UICommon.
    public String PriorityToString(int value)
    {
        int highPriority = DataProvider.GetMaxVmPriority();
        int roundedPriority = DataProvider.RoundPriority(value);

        if (roundedPriority == 1)
        {
            return "Low";
        }
        else if (roundedPriority == DataProvider.GetMaxVmPriority() / 2)
        {
            return "Medium";
        }
        else if (roundedPriority == DataProvider.GetMaxVmPriority())
        {
            return "High";
        }
        else
        {
            return "Unknown";
        }
    }
}
