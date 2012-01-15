package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.RangeEntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.validation.ByteSizeValidation;
import org.ovirt.engine.ui.uicommonweb.validation.CustomPropertyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;

@SuppressWarnings("unused")
public class UserVmModel extends Model
{

    private boolean privateIsNew;

    public boolean getIsNew()
    {
        return privateIsNew;
    }

    public void setIsNew(boolean value)
    {
        privateIsNew = value;
    }

    private VmType privateVmType = getVmType().values()[0];

    public VmType getVmType()
    {
        return privateVmType;
    }

    public void setVmType(VmType value)
    {
        privateVmType = value;
    }

    private boolean isWindowsOS;

    public boolean getIsWindowsOS()
    {
        return isWindowsOS;
    }

    public void setIsWindowsOS(boolean value)
    {
        if (isWindowsOS != value)
        {
            isWindowsOS = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsWindowsOS"));
        }
    }

    private boolean isLinux_Unassign_UnknownOS;

    public boolean getIsLinux_Unassign_UnknownOS()
    {
        return isLinux_Unassign_UnknownOS;
    }

    public void setIsLinux_Unassign_UnknownOS(boolean value)
    {
        if (isLinux_Unassign_UnknownOS != value)
        {
            isLinux_Unassign_UnknownOS = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsLinux_Unassign_UnknownOS"));
        }
    }

    private boolean isBlankTemplate;

    public boolean getIsBlankTemplate()
    {
        return isBlankTemplate;
    }

    public void setIsBlankTemplate(boolean value)
    {
        if (isBlankTemplate != value)
        {
            isBlankTemplate = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsBlankTemplate"));
        }
    }

    private String cpuNotification;

    public String getCPUNotification()
    {
        return cpuNotification;
    }

    public void setCPUNotification(String value)
    {
        if (!StringHelper.stringsEqual(cpuNotification, value))
        {
            cpuNotification = value;
            OnPropertyChanged(new PropertyChangedEventArgs("CPUNotification"));
        }
    }

    public boolean isCPUsAmountValid;

    public boolean getIsCPUsAmountValid()
    {
        return isCPUsAmountValid;
    }

    public void setIsCPUsAmountValid(boolean value)
    {
        if (isCPUsAmountValid != value)
        {
            isCPUsAmountValid = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsCPUsAmountValid"));
        }
    }

    private boolean isGeneralTabValid;

    public boolean getIsGeneralTabValid()
    {
        return isGeneralTabValid;
    }

    public void setIsGeneralTabValid(boolean value)
    {
        if (isGeneralTabValid != value)
        {
            isGeneralTabValid = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsGeneralTabValid"));
        }
    }

    private boolean isFirstRunTabValid;

    public boolean getIsFirstRunTabValid()
    {
        return isFirstRunTabValid;
    }

    public void setIsFirstRunTabValid(boolean value)
    {
        if (isFirstRunTabValid != value)
        {
            isFirstRunTabValid = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsFirstRunTabValid"));
        }
    }

    private boolean isDisplayTabValid;

    public boolean getIsDisplayTabValid()
    {
        return isDisplayTabValid;
    }

    public void setIsDisplayTabValid(boolean value)
    {
        if (isDisplayTabValid != value)
        {
            isDisplayTabValid = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsDisplayTabValid"));
        }
    }

    private boolean isAllocationTabValid;

    public boolean getIsAllocationTabValid()
    {
        return isAllocationTabValid;
    }

    public void setIsAllocationTabValid(boolean value)
    {
        if (isAllocationTabValid != value)
        {
            isAllocationTabValid = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsAllocationTabValid"));
        }
    }

    private boolean isHostTabValid;

    public boolean getIsHostTabValid()
    {
        return isHostTabValid;
    }

    public void setIsHostTabValid(boolean value)
    {
        if (isHostTabValid != value)
        {
            isHostTabValid = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsHostTabValid"));
        }
    }

    private boolean isBootSequenceTabValid;

    public boolean getIsBootSequenceTabValid()
    {
        return isBootSequenceTabValid;
    }

    public void setIsBootSequenceTabValid(boolean value)
    {
        if (isBootSequenceTabValid != value)
        {
            isBootSequenceTabValid = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsBootSequenceTabValid"));
        }
    }

    private boolean isCustomPropertiesTabValid;

    public boolean getIsCustomPropertiesTabValid()
    {
        return isCustomPropertiesTabValid;
    }

    public void setIsCustomPropertiesTabValid(boolean value)
    {
        if (isCustomPropertiesTabValid != value)
        {
            isCustomPropertiesTabValid = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsCustomPropertiesTabValid"));
        }
    }

    private ListModel privateDataCenter;

    public ListModel getDataCenter()
    {
        return privateDataCenter;
    }

    private void setDataCenter(ListModel value)
    {
        privateDataCenter = value;
    }

    private ListModel privateStorageDomain;

    public ListModel getStorageDomain()
    {
        return privateStorageDomain;
    }

    private void setStorageDomain(ListModel value)
    {
        privateStorageDomain = value;
    }

    private ListModel privateTemplate;

    public ListModel getTemplate()
    {
        return privateTemplate;
    }

    private void setTemplate(ListModel value)
    {
        privateTemplate = value;
    }

    private EntityModel privateName;

    public EntityModel getName()
    {
        return privateName;
    }

    private void setName(EntityModel value)
    {
        privateName = value;
    }

    private ListModel privateOSType;

    public ListModel getOSType()
    {
        return privateOSType;
    }

    private void setOSType(ListModel value)
    {
        privateOSType = value;
    }

    private ListModel privateNumOfMonitors;

    public ListModel getNumOfMonitors()
    {
        return privateNumOfMonitors;
    }

    private void setNumOfMonitors(ListModel value)
    {
        privateNumOfMonitors = value;
    }

    private EntityModel privateDescription;

    public EntityModel getDescription()
    {
        return privateDescription;
    }

    private void setDescription(EntityModel value)
    {
        privateDescription = value;
    }

    private EntityModel privateDomain;

    public EntityModel getDomain()
    {
        return privateDomain;
    }

    private void setDomain(EntityModel value)
    {
        privateDomain = value;
    }

    private EntityModel privateMemSize;

    public EntityModel getMemSize()
    {
        return privateMemSize;
    }

    private void setMemSize(EntityModel value)
    {
        privateMemSize = value;
    }

    private EntityModel privateMinAllocatedMemory;

    public EntityModel getMinAllocatedMemory()
    {
        return privateMinAllocatedMemory;
    }

    private void setMinAllocatedMemory(EntityModel value)
    {
        privateMinAllocatedMemory = value;
    }

    private ListModel privateCluster;

    public ListModel getCluster()
    {
        return privateCluster;
    }

    private void setCluster(ListModel value)
    {
        privateCluster = value;
    }

    private ListModel privateUsbPolicy;

    public ListModel getUsbPolicy()
    {
        return privateUsbPolicy;
    }

    private void setUsbPolicy(ListModel value)
    {
        privateUsbPolicy = value;
    }

    private ListModel privateTimeZone;

    public ListModel getTimeZone()
    {
        return privateTimeZone;
    }

    private void setTimeZone(ListModel value)
    {
        privateTimeZone = value;
    }

    private RangeEntityModel privateNumOfSockets;

    public RangeEntityModel getNumOfSockets()
    {
        return privateNumOfSockets;
    }

    private void setNumOfSockets(RangeEntityModel value)
    {
        privateNumOfSockets = value;
    }

    private RangeEntityModel privateTotalCPUCores;

    public RangeEntityModel getTotalCPUCores()
    {
        return privateTotalCPUCores;
    }

    private void setTotalCPUCores(RangeEntityModel value)
    {
        privateTotalCPUCores = value;
    }

    private ListModel privateDefaultHost;

    public ListModel getDefaultHost()
    {
        return privateDefaultHost;
    }

    private void setDefaultHost(ListModel value)
    {
        privateDefaultHost = value;
    }

    private EntityModel privateIsStateless;

    public EntityModel getIsStateless()
    {
        return privateIsStateless;
    }

    private void setIsStateless(EntityModel value)
    {
        privateIsStateless = value;
    }

    private ListModel privateDisplayProtocol;

    public ListModel getDisplayProtocol()
    {
        return privateDisplayProtocol;
    }

    private void setDisplayProtocol(ListModel value)
    {
        privateDisplayProtocol = value;
    }

    private ListModel privateProvisioning;

    public ListModel getProvisioning()
    {
        return privateProvisioning;
    }

    private void setProvisioning(ListModel value)
    {
        privateProvisioning = value;
    }

    private ListModel privatePriority;

    public ListModel getPriority()
    {
        return privatePriority;
    }

    private void setPriority(ListModel value)
    {
        privatePriority = value;
    }

    private EntityModel privateIsHighlyAvailable;

    public EntityModel getIsHighlyAvailable()
    {
        return privateIsHighlyAvailable;
    }

    private void setIsHighlyAvailable(EntityModel value)
    {
        privateIsHighlyAvailable = value;
    }

    private ListModel privateFirstBootDevice;

    public ListModel getFirstBootDevice()
    {
        return privateFirstBootDevice;
    }

    private void setFirstBootDevice(ListModel value)
    {
        privateFirstBootDevice = value;
    }

    private ListModel privateSecondBootDevice;

    public ListModel getSecondBootDevice()
    {
        return privateSecondBootDevice;
    }

    private void setSecondBootDevice(ListModel value)
    {
        privateSecondBootDevice = value;
    }

    private ListModel privateCdImage;

    public ListModel getCdImage()
    {
        return privateCdImage;
    }

    private void setCdImage(ListModel value)
    {
        privateCdImage = value;
    }

    private EntityModel privateInitrd_path;

    public EntityModel getInitrd_path()
    {
        return privateInitrd_path;
    }

    private void setInitrd_path(EntityModel value)
    {
        privateInitrd_path = value;
    }

    private EntityModel privateKernel_path;

    public EntityModel getKernel_path()
    {
        return privateKernel_path;
    }

    private void setKernel_path(EntityModel value)
    {
        privateKernel_path = value;
    }

    private EntityModel privateKernel_parameters;

    public EntityModel getKernel_parameters()
    {
        return privateKernel_parameters;
    }

    private void setKernel_parameters(EntityModel value)
    {
        privateKernel_parameters = value;
    }

    private EntityModel privateCustomProperties;

    public EntityModel getCustomProperties()
    {
        return privateCustomProperties;
    }

    private void setCustomProperties(EntityModel value)
    {
        privateCustomProperties = value;
    }

    private java.util.ArrayList<String> privateCustomPropertiesKeysList;

    public java.util.ArrayList<String> getCustomPropertiesKeysList()
    {
        return privateCustomPropertiesKeysList;
    }

    public void setCustomPropertiesKeysList(java.util.ArrayList<String> value)
    {
        privateCustomPropertiesKeysList = value;
    }

    private EntityModel privateIsAutoAssign;

    public EntityModel getIsAutoAssign()
    {
        return privateIsAutoAssign;
    }

    public void setIsAutoAssign(EntityModel value)
    {
        privateIsAutoAssign = value;
    }

    private EntityModel privateRunVMOnSpecificHost;

    public EntityModel getRunVMOnSpecificHost()
    {
        return privateRunVMOnSpecificHost;
    }

    public void setRunVMOnSpecificHost(EntityModel value)
    {
        privateRunVMOnSpecificHost = value;
    }

    private EntityModel privateDontMigrateVM;

    public EntityModel getDontMigrateVM()
    {
        return privateDontMigrateVM;
    }

    public void setDontMigrateVM(EntityModel value)
    {
        privateDontMigrateVM = value;
    }

    private EntityModel privateIsTemplatePublic;

    public EntityModel getIsTemplatePublic()
    {
        return privateIsTemplatePublic;
    }

    private void setIsTemplatePublic(EntityModel value)
    {
        privateIsTemplatePublic = value;
    }

    private String privateIsoPath;

    public String getIsoPath()
    {
        return privateIsoPath;
    }

    public void setIsoPath(String value)
    {
        privateIsoPath = value;
    }

    private boolean privateIsFirstRun;

    public boolean getIsFirstRun()
    {
        return privateIsFirstRun;
    }

    public void setIsFirstRun(boolean value)
    {
        privateIsFirstRun = value;
    }

    private java.util.List<DiskModel> disks;

    public java.util.List<DiskModel> getDisks()
    {
        return disks;
    }

    public void setDisks(java.util.List<DiskModel> value)
    {
        if (disks != value)
        {
            disks = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Disks"));
        }
    }

    private boolean isDisksAvailable;

    public boolean getIsDisksAvailable()
    {
        return isDisksAvailable;
    }

    public void setIsDisksAvailable(boolean value)
    {
        if (isDisksAvailable != value)
        {
            isDisksAvailable = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsDisksAvailable"));
        }
    }

    private boolean isCustomPropertiesAvailable;

    public boolean getIsCustomPropertiesAvailable()
    {
        return isCustomPropertiesAvailable;
    }

    public void setIsCustomPropertiesAvailable(boolean value)
    {
        if (isCustomPropertiesAvailable != value)
        {
            isCustomPropertiesAvailable = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsCustomPropertiesAvailable"));

            if (value == false)
            {
                getCustomProperties().setEntity("");
            }
        }
    }

    private boolean isHostAvailable;

    public boolean getIsHostAvailable()
    {
        return isHostAvailable;
    }

    public void setIsHostAvailable(boolean value)
    {
        if (isHostAvailable != value)
        {
            isHostAvailable = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsHostAvailable"));
        }
    }

    private int _minMemSize = 1;

    public int get_MinMemSize()
    {
        return _minMemSize;
    }

    public void set_MinMemSize(int value)
    {
        _minMemSize = value;
    }

    private int _maxMemSize32 = 262144;
    private int _maxMemSize64 = 262144;
    private int _maxCpus = 0;

    public int get_MaxCpus()
    {
        return _maxCpus;
    }

    public void set_MaxCpus(int value)
    {
        _maxCpus = value;
    }

    private int _maxCpusPerSocket = 0;

    public int get_MaxCpusPerSocket()
    {
        return _maxCpusPerSocket;
    }

    public void set_MaxCpusPerSocket(int value)
    {
        _maxCpusPerSocket = value;
    }

    private Guid dataCenterId = new Guid();

    public static final int WINDOWS_VM_NAME_MAX_LIMIT = 15;
    public static final int NON_WINDOWS_VM_NAME_MAX_LIMIT = 64;

    public UserVmModel()
    {
        DataProvider.GetVolumeTypeList();

        setDataCenter(new ListModel());
        getDataCenter().getSelectedItemChangedEvent().addListener(this);

        setStorageDomain(new ListModel());

        setTemplate(new ListModel());
        getTemplate().getSelectedItemChangedEvent().addListener(this);

        setName(new EntityModel());
        setNumOfMonitors(new ListModel());
        setDescription(new EntityModel());
        setDomain(new EntityModel());
        setMemSize(new EntityModel());
        getMemSize().getEntityChangedEvent().addListener(this);
        setMinAllocatedMemory(new EntityModel());

        setCluster(new ListModel());
        getCluster().getSelectedItemChangedEvent().addListener(this);

        setUsbPolicy(new ListModel());

        setTimeZone(new ListModel());
        getTimeZone().getSelectedItemChangedEvent().addListener(this);

        setNumOfSockets(new RangeEntityModel());
        getNumOfSockets().getEntityChangedEvent().addListener(this);

        setTotalCPUCores(new RangeEntityModel());

        setDefaultHost(new ListModel());
        getDefaultHost().getSelectedItemChangedEvent().addListener(this);

        setIsStateless(new EntityModel());

        setCdImage(new ListModel());

        setIsHighlyAvailable(new EntityModel());

        setDontMigrateVM(new EntityModel());

        setRunVMOnSpecificHost(new EntityModel());
        getRunVMOnSpecificHost().getEntityChangedEvent().addListener(this);

        setIsAutoAssign(new EntityModel());
        getIsAutoAssign().getEntityChangedEvent().addListener(this);

        setIsTemplatePublic(new EntityModel());

        setKernel_parameters(new EntityModel());
        setKernel_path(new EntityModel());
        setInitrd_path(new EntityModel());

        setCustomProperties(new EntityModel());

        setOSType(new ListModel());
        getOSType().getSelectedItemChangedEvent().addListener(this);

        setDisplayProtocol(new ListModel());

        setFirstBootDevice(new ListModel());
        getFirstBootDevice().getSelectedItemChangedEvent().addListener(this);

        setSecondBootDevice(new ListModel());

        setProvisioning(new ListModel());

        setIsHostTabValid(true);
        setIsCustomPropertiesTabValid(getIsHostTabValid());
        setIsBootSequenceTabValid(getIsCustomPropertiesTabValid());
        setIsAllocationTabValid(getIsBootSequenceTabValid());
        setIsDisplayTabValid(getIsAllocationTabValid());
        setIsFirstRunTabValid(getIsDisplayTabValid());
        setIsGeneralTabValid(getIsFirstRunTabValid());
        getProvisioning().getSelectedItemChangedEvent().addListener(this);

        setPriority(new ListModel());
    }

    @Override
    public void Initialize()
    {
        super.Initialize();

        getMemSize().setEntity(256);
        getMinAllocatedMemory().setEntity(256);
        getUsbPolicy().setItems(DataProvider.GetUsbPolicyList());
        getIsStateless().setEntity(false);

        getCdImage().setIsChangable(false);

        getIsHighlyAvailable().setEntity(false);
        getDontMigrateVM().setEntity(false);

        getRunVMOnSpecificHost().setEntity(false);
        getRunVMOnSpecificHost().setIsChangable(false);

        getIsAutoAssign().setEntity(true);
        getIsTemplatePublic().setEntity(false);

        getOSType().setItems(DataProvider.GetOSList());
        getOSType().setSelectedItem(VmOsType.Unassigned);

        // Display protocols.
        java.util.ArrayList<EntityModel> displayProtocolOptions = new java.util.ArrayList<EntityModel>();

        EntityModel spiceProtocol = new EntityModel();
        spiceProtocol.setTitle("Spice");
        spiceProtocol.setEntity(DisplayType.qxl);

        EntityModel vncProtocol = new EntityModel();
        vncProtocol.setTitle("VNC");
        vncProtocol.setEntity(DisplayType.vnc);

        displayProtocolOptions.add(spiceProtocol);
        displayProtocolOptions.add(vncProtocol);
        getDisplayProtocol().setItems(displayProtocolOptions);

        getDisplayProtocol().getSelectedItemChangedEvent().addListener(this);
        if (getVmType() == VmType.Desktop)
        {
            getDisplayProtocol().setSelectedItem(spiceProtocol);
        }

        // Boot devices.
        EntityModel tempVar = new EntityModel();
        tempVar.setTitle("Hard Disk");
        tempVar.setEntity(BootSequence.C);
        EntityModel hardDiskOption = tempVar;

        java.util.ArrayList<EntityModel> firstBootDeviceItems = new java.util.ArrayList<EntityModel>();
        firstBootDeviceItems.add(hardDiskOption);
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setTitle("CD-ROM");
        tempVar2.setEntity(BootSequence.D);
        firstBootDeviceItems.add(tempVar2);
        EntityModel tempVar3 = new EntityModel();
        tempVar3.setTitle("Network (PXE)");
        tempVar3.setEntity(BootSequence.N);
        firstBootDeviceItems.add(tempVar3);
        getFirstBootDevice().setItems(firstBootDeviceItems);
        getFirstBootDevice().setSelectedItem(hardDiskOption);

        // Provisioning
        java.util.ArrayList<EntityModel> provisioningItems = new java.util.ArrayList<EntityModel>();
        EntityModel tempVar4 = new EntityModel();
        tempVar4.setTitle("Thin");
        tempVar4.setEntity(false);
        provisioningItems.add(tempVar4);
        EntityModel tempVar5 = new EntityModel();
        tempVar5.setTitle("Clone");
        tempVar5.setEntity(true);
        provisioningItems.add(tempVar5);
        getProvisioning().setItems(provisioningItems);
        getProvisioning().setSelectedItem(provisioningItems.get(0));

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                UserVmModel userVmModel = (UserVmModel) model;
                int maxPriority = (Integer) result;

                EntityModel tempVar6 = new EntityModel();
                tempVar6.setTitle("Low");
                tempVar6.setEntity(1);
                EntityModel lowOption = tempVar6;

                java.util.ArrayList<EntityModel> priorityItems = new java.util.ArrayList<EntityModel>();
                priorityItems.add(lowOption);
                EntityModel tempVar7 = new EntityModel();
                tempVar7.setTitle("Medium");
                tempVar7.setEntity(maxPriority / 2);
                priorityItems.add(tempVar7);
                EntityModel tempVar8 = new EntityModel();
                tempVar8.setTitle("High");
                tempVar8.setEntity(maxPriority);
                priorityItems.add(tempVar8);

                userVmModel.getPriority().setItems(priorityItems);
                userVmModel.getPriority().setSelectedItem(lowOption);
            }
        };
        AsyncDataProvider.GetMaxVmPriority(_asyncQuery);
        _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                UserVmModel userVmModel = (UserVmModel) model;
                String oldSelectedItem = null;
                if (userVmModel.getTimeZone().getSelectedItem() != null)
                {
                    oldSelectedItem =
                            ((java.util.Map.Entry<String, String>) userVmModel.getTimeZone().getSelectedItem()).getKey();
                }

                userVmModel.getTimeZone().setItems(((java.util.HashMap<String, String>) result).entrySet());

                userVmModel.getTimeZone()
                        .setSelectedItem(!StringHelper.isNullOrEmpty(oldSelectedItem) ? Linq.FirstOrDefault(userVmModel.getTimeZone()
                                .getItems(),
                                new Linq.TimeZonePredicate(oldSelectedItem))
                                : Linq.FirstOrDefault((Iterable<java.util.Map.Entry<String, String>>) userVmModel.getTimeZone()
                                        .getItems()));
            }
        };
        AsyncDataProvider.GetTimeZoneList(_asyncQuery);
        _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                UserVmModel userVmModel = (UserVmModel) model;
                java.util.ArrayList<storage_pool> list = new java.util.ArrayList<storage_pool>();
                for (storage_pool a : (java.util.ArrayList<storage_pool>) result)
                {
                    if (a.getstatus() == StoragePoolStatus.Up)
                    {
                        list.add(a);
                    }
                }
                userVmModel.getDataCenter().setItems(list);

            }
        };
        AsyncDataProvider.GetDataCenterList(_asyncQuery);
        _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                UserVmModel userVmModel = (UserVmModel) model;
                userVmModel.set_MinMemSize((Integer) result);

            }
        };
        AsyncDataProvider.GetMinimalVmMemSize(_asyncQuery);
        SetGUIByVMType();
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.equals(EntityModel.EntityChangedEventDefinition))
        {
            if (sender == getNumOfSockets())
            {
                NumOfSockets_EntityChanged();
            }
            else if (sender == getIsAutoAssign())
            {
                if ((Boolean) getIsAutoAssign().getEntity())
                {
                    getRunVMOnSpecificHost().setEntity(false);
                    getRunVMOnSpecificHost().setIsChangable(false);
                    getDefaultHost().setIsChangable(false);
                }
                else
                {
                    getRunVMOnSpecificHost().setIsChangable(true);
                    getDefaultHost().setIsChangable(true);
                }
            }
            else if (sender == getRunVMOnSpecificHost())
            {
                if ((Boolean) getRunVMOnSpecificHost().getEntity())
                {
                    getDontMigrateVM().setEntity(true);
                    getDontMigrateVM().setIsChangable(false);
                }
                else
                {
                    getDontMigrateVM().setIsChangable(true);
                }
            }
            else if (sender == getMemSize())
            {
                UpdateMinAllocatedMemory(true);
            }
        }

        if (ev.equals(ListModel.SelectedItemChangedEventDefinition))
        {
            if (sender == getDataCenter())
            {
                DataCenter_SelectedItemChanged();
            }
            else if (sender == getTemplate())
            {
                Template_SelectedItemChanged();
            }
            else if (sender == getCluster())
            {
                Cluster_SelectedItemChanged();
            }
            else if (sender == getDefaultHost())
            {
                DefaultHost_SelectedItemChanged();
            }
            else if (sender == getOSType())
            {
                OSType_SelectedItemChanged();
            }
            else if (sender == getDisplayProtocol())
            {
                DisplayProtocol_SelectedItemChanged();
            }
            else if (sender == getFirstBootDevice())
            {
                FirstBootDevice_SelectedItemChanged();
            }
            else if (sender == getProvisioning())
            {
                Provisioning_SelectedItemChanged();
            }
        }
    }

    private void DefaultHost_SelectedItemChanged()
    {
        UpdateCDImages();
    }

    private void NumOfSockets_EntityChanged()
    {
        UpdateTotalCpus();
    }

    private void UpdateNumOfSockets()
    {
        VDSGroup cluster = (VDSGroup) getCluster().getSelectedItem();
        if (cluster == null)
        {
            return;
        }

        String version = cluster.getcompatibility_version().toString();
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                UserVmModel userVmModel = (UserVmModel) model;
                userVmModel.getNumOfSockets().setMax((Integer) result);
                userVmModel.getNumOfSockets().setMin(1);
                userVmModel.getNumOfSockets().setInterval(1);
                userVmModel.getNumOfSockets().setIsAllValuesSet(true);

                if (userVmModel.getIsNew())
                {
                    userVmModel.getNumOfSockets().setEntity(1);
                }

                VDSGroup cluster1 = (VDSGroup) userVmModel.getCluster().getSelectedItem();
                String version1 = cluster1.getcompatibility_version().toString();

                AsyncQuery _asyncQuery1 = new AsyncQuery();
                _asyncQuery1.setModel(userVmModel);
                _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model1, Object result1)
                    {
                        UserVmModel userVmModel1 = (UserVmModel) model1;
                        userVmModel1._maxCpus = (Integer) result1;

                        VDSGroup cluster2 = (VDSGroup) userVmModel1.getCluster().getSelectedItem();
                        String version2 = cluster2.getcompatibility_version().toString();

                        AsyncQuery _asyncQuery2 = new AsyncQuery();
                        _asyncQuery2.setModel(userVmModel1);
                        _asyncQuery2.asyncCallback = new INewAsyncCallback() {
                            @Override
                            public void OnSuccess(Object model2, Object result2)
                            {
                                UserVmModel userVmModel2 = (UserVmModel) model2;
                                userVmModel2._maxCpusPerSocket = (Integer) result2;
                                userVmModel2.UpdateTotalCpus();
                            }
                        };
                        AsyncDataProvider.GetMaxNumOfCPUsPerSocket(_asyncQuery2, version2);

                    }
                };
                AsyncDataProvider.GetMaxNumOfVmCpus(_asyncQuery1, version1);

            }
        };
        AsyncDataProvider.GetMaxNumOfVmSockets(_asyncQuery, version);
    }

    private void UpdateMinAllocatedMemory(boolean forceUpdate)
    {
        VDSGroup cluster = (VDSGroup) getCluster().getSelectedItem();
        if (cluster == null)
        {
            return;
        }

        double overCommitFactor = 100.0 / cluster.getmax_vds_memory_over_commit();
        if (getIsNew() || forceUpdate)
        {
            getMinAllocatedMemory().setEntity((int) ((Integer) getMemSize().getEntity() * overCommitFactor));
        }
    }

    public void UpdateTotalCpus()
    {
        int numOfSockets = Integer.parseInt(getNumOfSockets().getEntity().toString());

        int totalCPUCores =
                getTotalCPUCores().getEntity() != null ? Integer.parseInt(getTotalCPUCores().getEntity().toString())
                        : 0;

        int realMaxCpus = _maxCpus < numOfSockets * _maxCpusPerSocket ? _maxCpus : numOfSockets * _maxCpusPerSocket;

        if (_maxCpus == 0 || _maxCpusPerSocket == 0)
        {
            return;
        }

        getTotalCPUCores().setMax(realMaxCpus - (realMaxCpus % numOfSockets));
        getTotalCPUCores().setMin(numOfSockets);
        getTotalCPUCores().setInterval(numOfSockets);
        // update value if needed
        // if the slider in the range but not on tick update it to lowest tick
        if ((totalCPUCores % numOfSockets != 0) && totalCPUCores < getTotalCPUCores().getMax()
                && totalCPUCores > getTotalCPUCores().getMin())
        {
            getTotalCPUCores().setEntity(totalCPUCores - (totalCPUCores % numOfSockets));
        }
        // if the value is lower than range update it to min
        else if (totalCPUCores < getTotalCPUCores().getMin())
        {
            getTotalCPUCores().setEntity(new Double(getTotalCPUCores().getMin()).intValue());
        }
        // if the value is higher than range update it to max
        else if (totalCPUCores > getTotalCPUCores().getMax())
        {
            getTotalCPUCores().setEntity(new Double(getTotalCPUCores().getMax()).intValue());
        }

        getTotalCPUCores().setIsAllValuesSet(true);
    }

    private void Provisioning_SelectedItemChanged()
    {
        UpdateIsDisksAvailable();
        UpdateStorageDomains();
    }

    private void DataCenter_SelectedItemChanged()
    {
        storage_pool dataCenter = (storage_pool) getDataCenter().getSelectedItem();
        dataCenterId = dataCenter.getId();
        if (dataCenter.getstorage_pool_type() == StorageType.LOCALFS)
        {
            setIsHostAvailable(false);
        }
        else
        {
            setIsHostAvailable(true);
        }
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                UserVmModel userVmModel = (UserVmModel) model;
                java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>) result;
                userVmModel.getCluster().setItems(clusters);
                Object tempVar = getCluster().getSelectedItem();
                userVmModel.getCluster().setSelectedItem((tempVar != null) ? tempVar : Linq.FirstOrDefault(clusters));

                userVmModel.FillTemplateList(dataCenterId);
                userVmModel.UpdateCDImages();

            }
        };
        AsyncDataProvider.GetClusterList(_asyncQuery, dataCenterId);
    }

    private void Template_SelectedItemChanged()
    {
        Object tempVar = getTemplate().getSelectedItem();
        VmTemplate template = (VmTemplate) ((tempVar instanceof VmTemplate) ? tempVar : null);

        UpdateStorageDomains();

        if (template == null)
        {
            return;
        }

        setIsBlankTemplate(template.getId().equals(NGuid.Empty));

        if (getIsNew())
        {
            getOSType().setSelectedItem(template.getos());
            getNumOfSockets().setEntity(template.getnum_of_sockets());
            getTotalCPUCores().setEntity(template.getnum_of_cpus());

            getNumOfMonitors().setSelectedItem(template.getnum_of_monitors());
            getDomain().setEntity(template.getdomain());
            getMemSize().setEntity(template.getmem_size_mb());
            getUsbPolicy().setSelectedItem(template.getusb_policy());
            setBootSequence(template.getdefault_boot_sequence());
            getIsHighlyAvailable().setEntity(template.getauto_startup());

            UpdateMinAllocatedMemory(false);

            getCdImage().setIsChangable(!StringHelper.isNullOrEmpty(template.getiso_path()));
            if (getCdImage().getIsChangable())
            {
                getCdImage().setSelectedItem(template.getiso_path());
            }

            if (StringHelper.isNullOrEmpty(template.gettime_zone()))
            {
                AsyncQuery _asyncQuery = new AsyncQuery();
                _asyncQuery.setModel(this);
                _asyncQuery.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model, Object result)
                    {
                        UserVmModel userVmModel = (UserVmModel) model;
                        userVmModel.getTimeZone().setSelectedItem(Linq.FirstOrDefault(getTimeZone().getItems(),
                                new Linq.TimeZonePredicate((String) result)));
                    }
                };
                AsyncDataProvider.GetDefaultTimeZone(_asyncQuery);
            }
            else
            {
                getTimeZone().setSelectedItem(Linq.FirstOrDefault(getTimeZone().getItems(),
                        new Linq.TimeZonePredicate(template.gettime_zone())));
            }

            if (getCluster().getItems() == null)
            {
                return;
            }
            java.util.ArrayList<VDSGroup> clusters = Linq.<VDSGroup> Cast(getCluster().getItems());
            VDSGroup clusterToSelect = null;
            for (VDSGroup a : clusters)
            {
                if (a.getID().equals(template.getvds_group_id()))
                {
                    clusterToSelect = a;
                    break;
                }
            }

            if (clusterToSelect == null)
            {
                clusterToSelect = Linq.FirstOrDefault(clusters);
            }

            getCluster().setSelectedItem(clusterToSelect);

            // BZ#549348 (SPICE should be the default protocol for new Desktop).
            // Because of this bug, the Display Protocol is read only in case of
            // new Server or in case the template has the SPICE protocol as the
            // default display type:
            if (getVmType() == VmType.Server || template.getdefault_display_type() == DisplayType.qxl)
            {
                EntityModel displayProtocol = null;
                boolean isFirst = true;
                for (Object item : getDisplayProtocol().getItems())
                {
                    EntityModel a = (EntityModel) item;
                    if (isFirst)
                    {
                        displayProtocol = a;
                        isFirst = false;
                    }
                    DisplayType dt = (DisplayType) a.getEntity();
                    if (dt == template.getdefault_display_type())
                    {
                        displayProtocol = a;
                        break;
                    }
                }
                getDisplayProtocol().setSelectedItem(displayProtocol);
            }

            // By default, take kernel params from template.
            getKernel_path().setEntity(template.getkernel_url());
            getKernel_parameters().setEntity(template.getkernel_params());
            getInitrd_path().setEntity(template.getinitrd_url());

            if (!template.getId().equals(NGuid.Empty))
            {
                setIsBlankTemplate(false);
                getProvisioning().setIsChangable(true);
                // Retrieve disks.
                if (getIsNew())
                {
                    AsyncQuery _asyncQuery = new AsyncQuery();
                    _asyncQuery.setModel(this);
                    _asyncQuery.asyncCallback = new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object model, Object result)
                        {
                            UserVmModel userVmModel = (UserVmModel) model;
                            java.util.ArrayList<DiskImage> diskList = (java.util.ArrayList<DiskImage>) result;
                            Collections.sort(diskList, new Linq.DiskByInternalDriveMappingComparer());

                            java.util.ArrayList<DiskModel> list = new java.util.ArrayList<DiskModel>();
                            for (DiskImage a : diskList)
                            {
                                DiskModel diskModel = new DiskModel();
                                diskModel.setIsNew(true);
                                diskModel.setName(a.getinternal_drive_mapping());
                                EntityModel tempVar2 = new EntityModel();
                                tempVar2.setEntity(a.getSizeInGigabytes());
                                diskModel.setSize(tempVar2);
                                ListModel tempVar3 = new ListModel();
                                tempVar3.setItems(DataProvider.GetVolumeTypeList());
                                tempVar3.setSelectedItem(a.getvolume_type());
                                diskModel.setVolumeType(tempVar3);
                                list.add(diskModel);
                            }

                            userVmModel.setDisks(list);
                            userVmModel.UpdateIsDisksAvailable();

                        }
                    };
                    AsyncDataProvider.GetTemplateDiskList(_asyncQuery, template.getId());
                }
            }
            else
            {
                setIsBlankTemplate(true);
                setIsDisksAvailable(false);
                getProvisioning().setIsChangable(false);
                setDisks(null);
            }

            // Set priority
            AsyncQuery _asyncQuery1 = new AsyncQuery();
            _asyncQuery1.setModel(this);
            _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object result)
                {
                    UserVmModel userVmModel = (UserVmModel) model;
                    int priority = (Integer) result;

                    Object prioritySelectedItem = null;
                    for (Object item : userVmModel.getPriority().getItems())
                    {
                        EntityModel a = (EntityModel) item;
                        if ((Integer) a.getEntity() == priority)
                        {
                            prioritySelectedItem = a;
                            break;
                        }
                    }
                    userVmModel.getPriority().setSelectedItem(prioritySelectedItem);
                }
            };
            AsyncDataProvider.GetRoundedPriority(_asyncQuery1, template.getpriority());
        }
    }

    private void Cluster_SelectedItemChanged()
    {
        VDSGroup cluster = (VDSGroup) getCluster().getSelectedItem();

        if (cluster != null)
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object result)
                {
                    UserVmModel userVmModel = (UserVmModel) model;
                    java.util.ArrayList<VDS> hosts = (java.util.ArrayList<VDS>) result;
                    userVmModel.getDefaultHost().setItems(hosts);
                    userVmModel.getDefaultHost().setSelectedItem(Linq.FirstOrDefault(hosts));

                    // VDSGroup cluster = (VDSGroup)Cluster.getSelectedItem();
                    AsyncQuery _asyncQuery1 = new AsyncQuery();
                    _asyncQuery1.setModel(userVmModel);
                    _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object model1, Object result1)
                        {
                            UserVmModel userVmModel1 = (UserVmModel) model1;
                            userVmModel1.UpdateNumOfSockets();

                        }
                    };
                    AsyncDataProvider.GetMaxVmMemSize(_asyncQuery1, false);
                    AsyncDataProvider.GetMaxVmMemSize(_asyncQuery1, true);

                }
            };
            AsyncDataProvider.GetHostListByCluster(_asyncQuery, cluster.getname());
            _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object result)
                {
                    UserVmModel userVmModel = (UserVmModel) model;
                    setIsCustomPropertiesAvailable((Boolean) result);
                }
            };
            AsyncDataProvider.IsCustomPropertiesAvailable(_asyncQuery, cluster.getcompatibility_version().toString());

            UpdateMinAllocatedMemory(false);
        }
        else
        {
            java.util.ArrayList<VDS> hosts = new java.util.ArrayList<VDS>();
            getDefaultHost().setItems(hosts);
            getDefaultHost().setSelectedItem(Linq.FirstOrDefault(hosts));

            UpdateNumOfSockets();
        }
    }

    private void OSType_SelectedItemChanged()
    {
        VmOsType osType = (VmOsType) getOSType().getSelectedItem();

        setIsWindowsOS(DataProvider.IsWindowsOsType(osType));
        setIsLinux_Unassign_UnknownOS(DataProvider.IsLinuxOsType(osType) || osType == VmOsType.Unassigned
                || osType == VmOsType.Other);

        getInitrd_path().setIsChangable(getIsLinux_Unassign_UnknownOS());
        getInitrd_path().setIsAvailable(getIsLinux_Unassign_UnknownOS());

        getKernel_path().setIsChangable(getIsLinux_Unassign_UnknownOS());
        getKernel_path().setIsAvailable(getIsLinux_Unassign_UnknownOS());

        getKernel_parameters().setIsChangable(getIsLinux_Unassign_UnknownOS());
        getKernel_parameters().setIsAvailable(getIsLinux_Unassign_UnknownOS());

        getDomain().setIsChangable(getIsWindowsOS());
        getDomain().setIsAvailable(getIsWindowsOS());

        if (getIsFirstRun())
        {
            getTimeZone().setIsChangable(getIsWindowsOS());
            getTimeZone().setIsAvailable(getIsWindowsOS());
        }

        DefineNumOfMonitorsAvailabilityAccoringToSelectedOs();
    }

    private void DisplayProtocol_SelectedItemChanged()
    {
        EntityModel entityModel = (EntityModel) getDisplayProtocol().getSelectedItem();
        DisplayType type = (DisplayType) entityModel.getEntity();

        if (type == DisplayType.vnc)
        {
            getUsbPolicy().setSelectedItem(org.ovirt.engine.core.common.businessentities.UsbPolicy.Disabled);
            getNumOfMonitors().setSelectedItem(1);
        }

        getUsbPolicy().setIsChangable(type == DisplayType.qxl);
        getNumOfMonitors().setIsChangable(type == DisplayType.qxl);
    }

    private void FirstBootDevice_SelectedItemChanged()
    {
        EntityModel entityModel = (EntityModel) getFirstBootDevice().getSelectedItem();
        BootSequence firstDevice = (BootSequence) entityModel.getEntity();

        java.util.ArrayList<EntityModel> list = new java.util.ArrayList<EntityModel>();
        for (Object item : getFirstBootDevice().getItems())
        {
            EntityModel a = (EntityModel) item;
            if ((BootSequence) a.getEntity() != firstDevice)
            {
                list.add(a);
            }
        }

        EntityModel tempVar = new EntityModel();
        tempVar.setTitle("[None]");
        EntityModel noneOption = tempVar;

        list.add(0, noneOption);

        getSecondBootDevice().setItems(list);
        getSecondBootDevice().setSelectedItem(noneOption);
    }

    public void UpdateIsDisksAvailable()
    {
        setIsDisksAvailable(getIsNew() && (Boolean) ((EntityModel) getProvisioning().getSelectedItem()).getEntity()
                && (getDisks() != null));
    }

    private void DefineNumOfMonitorsAvailabilityAccoringToSelectedOs()
    {
        if (getVmType() == VmType.Desktop)
        {
            if (DataProvider.IsLinuxOsType((VmOsType) getOSType().getSelectedItem()))
            {
                getNumOfMonitors().setSelectedItem(1);
                getNumOfMonitors().setIsAvailable(false);
                getNumOfMonitors().setIsChangable(false);
            }
            else if (!getNumOfMonitors().getIsAvailable())
            {
                getNumOfMonitors().setIsAvailable(true);
                getNumOfMonitors().setIsChangable(true);
            }
        }
    }

    protected void FillTemplateList(Guid DataCenterId)
    {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                UserVmModel userVmModel = (UserVmModel) model;
                java.util.ArrayList<VmTemplate> templates = (java.util.ArrayList<VmTemplate>) result;

                VmTemplate oldTemplate = (VmTemplate) userVmModel.getTemplate().getSelectedItem();
                userVmModel.getTemplate().setItems(templates);

                // If there was no selected template or the old selected template doesn't
                // exist in the new list of available templates - re-select a template.
                // Otherwise, leave the template selected value as is.
                boolean all = false;
                if (oldTemplate != null)
                {
                    for (VmTemplate a : templates)
                    {
                        if (a.getId().equals(oldTemplate.getId()))
                        {
                            userVmModel.getTemplate().setSelectedItem(a);
                            all = true;
                            break;
                        }
                    }
                }

                if (oldTemplate == null || !all)
                {
                    for (VmTemplate a : templates)
                    {
                        if (a.getId().equals(NGuid.Empty))
                        {
                            userVmModel.getTemplate().setSelectedItem(a);
                            break;
                        }
                    }
                }
                userVmModel.UpdateIsDisksAvailable();
            }
        };
        AsyncDataProvider.GetTemplateListByDataCenter(_asyncQuery, DataCenterId);
    }

    private void SetGUIByVMType()
    {
        if (getVmType() == VmType.Desktop)
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object result)
                {
                    UserVmModel userVmModel = (UserVmModel) model;
                    java.util.ArrayList<Integer> numOfMonitors = (java.util.ArrayList<Integer>) result;
                    userVmModel.getNumOfMonitors().setItems(numOfMonitors);
                    userVmModel.getNumOfMonitors().setSelectedItem(Linq.FirstOrDefault(numOfMonitors));
                }
            };
            AsyncDataProvider.GetNumOfMonitorList(_asyncQuery);
            getUsbPolicy().setSelectedItem(org.ovirt.engine.core.common.businessentities.UsbPolicy.Enabled);
        }
        else
        {
            java.util.ArrayList<Integer> numOfMonitors =
                    new java.util.ArrayList<Integer>(java.util.Arrays.asList(new Integer[] { 1 }));
            getNumOfMonitors().setItems(numOfMonitors);
            getNumOfMonitors().setSelectedItem(Linq.FirstOrDefault(numOfMonitors));
        }

        for (Object item : getDisplayProtocol().getItems())
        {
            EntityModel a = (EntityModel) item;
            if ((DisplayType) a.getEntity() == (getVmType() == VmType.Desktop ? DisplayType.qxl : DisplayType.vnc))
            {
                getDisplayProtocol().setSelectedItem(a);
                break;
            }
        }
    }

    public void UpdateCDImages()
    {
        Object tempVar = getDataCenter().getSelectedItem();
        storage_pool dataCenter = (storage_pool) ((tempVar instanceof storage_pool) ? tempVar : null);
        if (dataCenter != null)
        {
            NGuid vds_id = null;
            if (getDefaultHost().getSelectedItem() != null)
            {
                vds_id = ((VDS) getDefaultHost().getSelectedItem()).getvds_id();
                vds_id = vds_id.equals(NGuid.Empty) ? null : vds_id;
            }
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object result)
                {
                    UserVmModel userVmModel = (UserVmModel) model;
                    java.util.ArrayList<String> images = (java.util.ArrayList<String>) result;
                    userVmModel.getCdImage().setItems(images);

                    if (!userVmModel.getIsNew() && !StringHelper.isNullOrEmpty(userVmModel.getIsoPath()))
                    {
                        userVmModel.getCdImage().setIsChangable(true);
                        userVmModel.getCdImage().setSelectedItem(userVmModel.getIsoPath());
                    }
                    else if (userVmModel.getCdImage().getIsChangable()
                            && userVmModel.getCdImage().getSelectedItem() == null)
                    {
                        userVmModel.getCdImage().setSelectedItem(Linq.FirstOrDefault(images));
                    }
                }
            };
            AsyncDataProvider.GetIrsImageList(_asyncQuery, NGuid.Empty, true);
        }
    }

    private void UpdateStorageDomains()
    {
        Object tempVar = getTemplate().getSelectedItem();
        VmTemplate template = (VmTemplate) ((tempVar instanceof VmTemplate) ? tempVar : null);

        if (template != null && !template.getId().equals(NGuid.Empty))
        {
            storage_pool dataCenter = (storage_pool) getDataCenter().getSelectedItem();

            if ((Boolean) ((EntityModel) getProvisioning().getSelectedItem()).getEntity())
            {
                AsyncQuery _asyncQuery = new AsyncQuery();
                _asyncQuery.setModel(this);
                _asyncQuery.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model, Object result)
                    {
                        UserVmModel userVmModel = (UserVmModel) model;
                        java.util.ArrayList<storage_domains> storageDomains =
                                (java.util.ArrayList<storage_domains>) result;
                        userVmModel.PostUpdateStorageDomains(storageDomains);
                    }
                };
                AsyncDataProvider.GetStorageDomainListByTemplate(_asyncQuery, template.getId());
            }
            else
            {
                AsyncQuery _asyncQuery = new AsyncQuery();
                _asyncQuery.setModel(this);
                _asyncQuery.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model, Object result)
                    {
                        UserVmModel userVmModel = (UserVmModel) model;
                        java.util.ArrayList<storage_domains> list = (java.util.ArrayList<storage_domains>) result;

                        java.util.ArrayList<storage_domains> storageDomains =
                                new java.util.ArrayList<storage_domains>();
                        for (storage_domains a : list)
                        {
                            if (a.getstorage_domain_type() == StorageDomainType.Data
                                    || a.getstorage_domain_type() == StorageDomainType.Master)
                            {
                                storageDomains.add(a);
                            }
                        }

                        userVmModel.PostUpdateStorageDomains(storageDomains);
                    }
                };
                AsyncDataProvider.GetStorageDomainList(_asyncQuery, dataCenter.getId());
            }
        }
        else
        {
            getStorageDomain().setItems(new java.util.ArrayList<storage_domains>());
            getStorageDomain().setSelectedItem(null);
            getStorageDomain().setIsChangable(false);
        }
    }

    public void PostUpdateStorageDomains(java.util.ArrayList<storage_domains> storageDomains)
    {
        // filter only the Active storage domains (Active regarding the relevant storage pool).
        // storageDomains = storageDomains.Where(a => a.status.HasValue && a.status.Value ==
        // StorageDomainStatus.Active);
        java.util.ArrayList<storage_domains> list = new java.util.ArrayList<storage_domains>();
        for (storage_domains a : storageDomains)
        {
            if (a.getstatus() != null && a.getstatus() == StorageDomainStatus.Active)
            {
                list.add(a);
            }
        }

        getStorageDomain().setItems(list);
        getStorageDomain().setSelectedItem(Linq.FirstOrDefault(list));
        getStorageDomain().setIsChangable(true);
    }

    public boolean Validate()
    {
        VmOsType os = (VmOsType) getOSType().getSelectedItem();

        String nameExpr;
        String nameMsg;
        if (DataProvider.IsWindowsOsType(os))
        {
            nameExpr = "^[0-9a-zA-Z-_]{1," + WINDOWS_VM_NAME_MAX_LIMIT + "}$";
            nameMsg =
                    "Name must contain only alphanumeric characters. Maximum length: " + WINDOWS_VM_NAME_MAX_LIMIT
                            + ".";
        }
        else
        {
            nameExpr = "^[-\\w]{1," + NON_WINDOWS_VM_NAME_MAX_LIMIT + "}$";
            nameMsg =
                    "Name cannot contain blanks or special characters. Maximum length: "
                            + NON_WINDOWS_VM_NAME_MAX_LIMIT + ".";
        }

        RegexValidation tempVar = new RegexValidation();
        tempVar.setExpression(nameExpr);
        tempVar.setMessage(nameMsg);
        getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar });

        getDataCenter().setIsValid(true);
        // In case of Edit the only scenario in which it will be null - editing of Blank template
        if (getIsNew())
        {
            getDataCenter().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }

        getCluster().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        if ((Boolean) getIsAutoAssign().getEntity() == false)
        {
            getDefaultHost().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }
        else
        {
            getDefaultHost().setIsValid(true);
        }

        getMemSize().ValidateEntity(new IValidation[] { new ByteSizeValidation() });
        getMinAllocatedMemory().ValidateEntity(new IValidation[] { new ByteSizeValidation() });

        VmTemplate template = (VmTemplate) getTemplate().getSelectedItem();
        storage_domains storageDomain = (storage_domains) getStorageDomain().getSelectedItem();

        getTemplate().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        getStorageDomain().setIsValid(true);
        if (template != null && !template.getId().equals(NGuid.Empty) && storageDomain == null)
        {
            getStorageDomain().setIsValid(false);
            getStorageDomain().getInvalidityReasons().add("Storage Domain must be specified.");
        }

        VmOsType osType = (VmOsType) getOSType().getSelectedItem();
        boolean is64OsType =
                (osType == VmOsType.Other || osType == VmOsType.OtherLinux || DataProvider.Is64bitOsType(osType));
        int maxMemSize = is64OsType ? _maxMemSize64 : _maxMemSize32;

        ValidateMemorySize(getMemSize(), maxMemSize);
        ValidateMemorySize(getMinAllocatedMemory(), (Integer) getMemSize().getEntity());

        getCdImage().setIsValid(true);
        if (getCdImage().getIsChangable())
        {
            getCdImage().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }

        getKernel_path().setIsValid(true);
        getKernel_parameters().setIsValid(true);
        getInitrd_path().setIsValid(true);
        if (getKernel_path().getEntity() == null)
        {
            getKernel_path().setEntity("");
        }
        if (getKernel_parameters().getEntity() == null)
        {
            getKernel_parameters().setEntity("");
        }
        if (getInitrd_path().getEntity() == null)
        {
            getInitrd_path().setEntity("");
        }

        if (isLinux_Unassign_UnknownOS
                && ((((String) getKernel_parameters().getEntity()).length() > 0 || ((String) getInitrd_path().getEntity()).length() > 0) && ((String) getKernel_path().getEntity()).length() == 0))
        {
            int count = 0;
            String msg = "When ";
            if (((String) getKernel_parameters().getEntity()).length() > 0)
            {
                getKernel_parameters().setIsValid(false);
                msg += "a kernel parameter argument ";
                count++;
            }
            if (((String) getInitrd_path().getEntity()).length() > 0)
            {
                getInitrd_path().setIsValid(false);
                if (count == 1)
                {
                    msg += "or ";
                }
                msg += "an initrd path ";
            }
            msg += "is used, kernel path must be non-empty";

            getKernel_path().setIsValid(false);
            getInitrd_path().getInvalidityReasons().add(msg);
            getKernel_parameters().getInvalidityReasons().add(msg);
            getKernel_path().getInvalidityReasons().add(msg);
        }

        getCustomProperties().ValidateEntity(new IValidation[] { new CustomPropertyValidation(getCustomPropertiesKeysList()) });

        setIsBootSequenceTabValid(true);
        setIsAllocationTabValid(getIsBootSequenceTabValid());
        setIsDisplayTabValid(getIsAllocationTabValid());
        setIsFirstRunTabValid(getIsDisplayTabValid());
        setIsGeneralTabValid(getIsFirstRunTabValid());

        setIsGeneralTabValid(getName().getIsValid() && getDescription().getIsValid() && getDataCenter().getIsValid()
                && getTemplate().getIsValid() && getCluster().getIsValid() && getMemSize().getIsValid()
                && getMinAllocatedMemory().getIsValid());

        setIsFirstRunTabValid(getDomain().getIsValid() && getTimeZone().getIsValid());
        setIsDisplayTabValid(getUsbPolicy().getIsValid() && getNumOfMonitors().getIsValid());
        setIsHostTabValid(getDefaultHost().getIsValid());
        setIsAllocationTabValid(getStorageDomain().getIsValid() && getMinAllocatedMemory().getIsValid());
        setIsBootSequenceTabValid(getCdImage().getIsValid() && getKernel_path().getIsValid());
        setIsCustomPropertiesTabValid(getCustomProperties().getIsValid());

        return getName().getIsValid() && getDescription().getIsValid() && getDataCenter().getIsValid()
                && getStorageDomain().getIsValid() && getTemplate().getIsValid() && getCluster().getIsValid()
                && getDefaultHost().getIsValid() && getMemSize().getIsValid() && getMinAllocatedMemory().getIsValid()
                && getNumOfMonitors().getIsValid() && getDomain().getIsValid() && getUsbPolicy().getIsValid()
                && getTimeZone().getIsValid() && getOSType().getIsValid() && getCdImage().getIsValid()
                && getKernel_path().getIsValid() && getCustomProperties().getIsValid();
    }

    private void ValidateMemorySize(EntityModel memorySizeEntityModel, int maxMemSize)
    {
        memorySizeEntityModel.setIsValid(true);

        int memSize = (Integer) memorySizeEntityModel.getEntity();

        if (memSize == 0)
        {
            memorySizeEntityModel.setIsValid(false);
            memorySizeEntityModel.getInvalidityReasons().add("Memory size is between " + _minMemSize + " MB and "
                    + maxMemSize + " MB");
        }
        else if (memSize > maxMemSize)
        {
            memorySizeEntityModel.setIsValid(false);
            memorySizeEntityModel.getInvalidityReasons().add("Maximum memory size is " + maxMemSize + " MB.");
        }
        else if (memSize < _minMemSize)
        {
            memorySizeEntityModel.setIsValid(false);
            memorySizeEntityModel.getInvalidityReasons().add("Minimum memory size is " + _minMemSize + " MB.");
        }
    }

    public BootSequence getBootSequence()
    {
        EntityModel firstSelectedItem = (EntityModel) getFirstBootDevice().getSelectedItem();
        EntityModel secondSelectedItem = (EntityModel) getSecondBootDevice().getSelectedItem();

        String firstSelectedString =
                firstSelectedItem.getEntity() == null ? "" : firstSelectedItem.getEntity().toString();
        String secondSelectedString =
                secondSelectedItem.getEntity() == null ? "" : secondSelectedItem.getEntity().toString();

        return BootSequence.valueOf(firstSelectedString + secondSelectedString);
    }

    public void setBootSequence(BootSequence value)
    {
        java.util.ArrayList<BootSequence> items = new java.util.ArrayList<BootSequence>();
        for (char a : value.toString().toCharArray())
        {
            items.add(BootSequence.valueOf((new Character(a)).toString()));
        }

        Object firstBootDevice = null;
        for (Object item : getFirstBootDevice().getItems())
        {
            EntityModel a = (EntityModel) item;
            if ((BootSequence) a.getEntity() == Linq.FirstOrDefault(items))
            {
                firstBootDevice = a;
            }
        }
        getFirstBootDevice().setSelectedItem(firstBootDevice);

        java.util.ArrayList<EntityModel> secondDeviceOptions =
                Linq.<EntityModel> Cast(getSecondBootDevice().getItems());

        if (items.size() > 1)
        {
            BootSequence last = items.get(items.size() - 1);
            for (EntityModel a : secondDeviceOptions)
            {
                if (a.getEntity() != null && (BootSequence) a.getEntity() == last)
                {
                    getSecondBootDevice().setSelectedItem(a);
                    break;
                }
            }
        }
        else
        {
            for (EntityModel a : secondDeviceOptions)
            {
                if (a.getEntity() == null)
                {
                    getSecondBootDevice().setSelectedItem(a);
                    break;
                }
            }
        }
    }
}
