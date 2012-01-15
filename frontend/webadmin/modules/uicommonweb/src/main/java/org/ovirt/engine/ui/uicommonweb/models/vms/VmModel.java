package org.ovirt.engine.ui.uicommonweb.models.vms;

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
import org.ovirt.engine.core.compat.StringFormat;
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
import org.ovirt.engine.ui.uicommonweb.validation.ByteSizeValidation;
import org.ovirt.engine.ui.uicommonweb.validation.CustomPropertyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;

@SuppressWarnings("unused")
public class VmModel extends Model
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

    private ListModel privateDomain;

    public ListModel getDomain()
    {
        return privateDomain;
    }

    private void setDomain(ListModel value)
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
    private int _maxMemSize32 = 262144;
    private int _maxMemSize64 = 262144;

    public static final int WINDOWS_VM_NAME_MAX_LIMIT = 15;
    public static final int NON_WINDOWS_VM_NAME_MAX_LIMIT = 64;

    public VmModel()
    {
        setDataCenter(new ListModel());
        // DataCenter.ValueChanged += DataCenter_ValueChanged;
        getDataCenter().getSelectedItemChangedEvent().addListener(this);

        setStorageDomain(new ListModel());

        setTemplate(new ListModel());
        // Template.ValueChanged += Template_ValueChanged;
        getTemplate().getSelectedItemChangedEvent().addListener(this);

        setName(new EntityModel());
        setNumOfMonitors(new ListModel());
        setDescription(new EntityModel());
        EntityModel tempVar = new EntityModel();
        tempVar.setEntity(256);
        setMemSize(tempVar);
        getMemSize().getEntityChangedEvent().addListener(this);
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setEntity(256);
        setMinAllocatedMemory(tempVar2);

        setCluster(new ListModel());
        // Cluster.ValueChanged += Cluster_ValueChanged;
        getCluster().getSelectedItemChangedEvent().addListener(this);

        ListModel tempVar3 = new ListModel();
        tempVar3.setItems(DataProvider.GetUsbPolicyList());
        setUsbPolicy(tempVar3);
        ListModel tempVar4 = new ListModel();
        tempVar4.setItems(DataProvider.GetTimeZoneList().entrySet());
        setTimeZone(tempVar4);
        setDomain(new ListModel());
        UpdateDomain();

        setNumOfSockets(new RangeEntityModel());
        // NumOfSockets.ValueChanged += NumOfSockets_ValueChanged;
        getNumOfSockets().getEntityChangedEvent().addListener(this);

        setTotalCPUCores(new RangeEntityModel());

        setDefaultHost(new ListModel());
        // DefaultHost.ValueChanged += DefaultHost_ValueChanged;
        getDefaultHost().getSelectedItemChangedEvent().addListener(this);

        setIsAutoAssign(new EntityModel());
        getIsAutoAssign().setEntity(true);
        getIsAutoAssign().getEntityChangedEvent().addListener(this);

        EntityModel tempVar5 = new EntityModel();
        tempVar5.setEntity(false);
        setIsStateless(tempVar5);
        setCdImage(new ListModel());
        getCdImage().setIsChangable(false);
        EntityModel tempVar6 = new EntityModel();
        tempVar6.setEntity(false);
        setIsHighlyAvailable(tempVar6);

        EntityModel tempVar7 = new EntityModel();
        tempVar7.setEntity(false);
        setRunVMOnSpecificHost(tempVar7);
        getRunVMOnSpecificHost().setIsChangable(false);
        getRunVMOnSpecificHost().getEntityChangedEvent().addListener(this);
        EntityModel tempVar8 = new EntityModel();
        tempVar8.setEntity(false);
        setDontMigrateVM(tempVar8);

        EntityModel tempVar9 = new EntityModel();
        tempVar9.setEntity(true);
        setIsTemplatePublic(tempVar9);

        setKernel_parameters(new EntityModel());
        setKernel_path(new EntityModel());
        setInitrd_path(new EntityModel());

        setCustomProperties(new EntityModel());

        ListModel tempVar10 = new ListModel();
        tempVar10.setItems(DataProvider.GetOSList());
        setOSType(tempVar10);
        getOSType().getSelectedItemChangedEvent().addListener(this);
        getOSType().setSelectedItem(VmOsType.Unassigned);

        // Display protocols.
        setDisplayProtocol(new ListModel());

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

        // DisplayProtocol.ValueChanged += DisplayProtocol_ValueChanged;
        getDisplayProtocol().getSelectedItemChangedEvent().addListener(this);
        if (getVmType() == VmType.Desktop)
        {
            getDisplayProtocol().setSelectedItem(spiceProtocol);
        }

        // Boot devices.
        EntityModel tempVar11 = new EntityModel();
        tempVar11.setTitle("Hard Disk");
        tempVar11.setEntity(BootSequence.C);
        EntityModel hardDiskOption = tempVar11;

        setFirstBootDevice(new ListModel());
        // FirstBootDevice.ValueChanged += FirstBootDevice_ValueChanged;
        getFirstBootDevice().getSelectedItemChangedEvent().addListener(this);

        setSecondBootDevice(new ListModel());

        java.util.ArrayList<EntityModel> firstBootDeviceItems = new java.util.ArrayList<EntityModel>();
        firstBootDeviceItems.add(hardDiskOption);
        EntityModel tempVar12 = new EntityModel();
        tempVar12.setTitle("CD-ROM");
        tempVar12.setEntity(BootSequence.D);
        firstBootDeviceItems.add(tempVar12);
        EntityModel tempVar13 = new EntityModel();
        tempVar13.setTitle("Network (PXE)");
        tempVar13.setEntity(BootSequence.N);
        firstBootDeviceItems.add(tempVar13);
        getFirstBootDevice().setItems(firstBootDeviceItems);
        getFirstBootDevice().setSelectedItem(hardDiskOption);

        // Provisioning
        java.util.ArrayList<EntityModel> provisioningItems = new java.util.ArrayList<EntityModel>();
        EntityModel tempVar14 = new EntityModel();
        tempVar14.setTitle("Thin");
        tempVar14.setEntity(false);
        provisioningItems.add(tempVar14);
        EntityModel tempVar15 = new EntityModel();
        tempVar15.setTitle("Clone");
        tempVar15.setEntity(true);
        provisioningItems.add(tempVar15);
        ListModel tempVar16 = new ListModel();
        tempVar16.setItems(provisioningItems);
        tempVar16.setSelectedItem(false);
        setProvisioning(tempVar16);

        setIsHostTabValid(true);
        setIsCustomPropertiesTabValid(getIsHostTabValid());
        setIsBootSequenceTabValid(getIsCustomPropertiesTabValid());
        setIsAllocationTabValid(getIsBootSequenceTabValid());
        setIsDisplayTabValid(getIsAllocationTabValid());
        setIsFirstRunTabValid(getIsDisplayTabValid());
        setIsGeneralTabValid(getIsFirstRunTabValid());
        // Provisioning.ValueChanged += Provisioning_ValueChanged;
        getProvisioning().getSelectedItemChangedEvent().addListener(this);

        // Priority
        int maxPriority = DataProvider.GetMaxVmPriority();
        EntityModel tempVar17 = new EntityModel();
        tempVar17.setTitle("Low");
        tempVar17.setEntity(1);
        EntityModel lowOption = tempVar17;

        java.util.ArrayList<EntityModel> priorityItems = new java.util.ArrayList<EntityModel>();
        priorityItems.add(lowOption);
        EntityModel tempVar18 = new EntityModel();
        tempVar18.setTitle("Medium");
        tempVar18.setEntity(maxPriority / 2);
        priorityItems.add(tempVar18);
        EntityModel tempVar19 = new EntityModel();
        tempVar19.setTitle("High");
        tempVar19.setEntity(maxPriority);
        priorityItems.add(tempVar19);

        ListModel tempVar20 = new ListModel();
        tempVar20.setItems(priorityItems);
        tempVar20.setSelectedItem(lowOption);
        setPriority(tempVar20);

        // Populate a list of data centers.
        // DataCenter.Options = DataProvider.GetStoragePoolList().Where(a => a.status == StoragePoolStatus.Up);
        java.util.ArrayList<storage_pool> list = new java.util.ArrayList<storage_pool>();
        for (storage_pool a : DataProvider.GetDataCenterList())
        {
            if (a.getstatus() == StoragePoolStatus.Up)
            {
                list.add(a);
            }
        }
        getDataCenter().setItems(list);

        _minMemSize = DataProvider.GetMinimalVmMemSize();

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
                if ((Boolean) getIsAutoAssign().getEntity() == true)
                {
                    getRunVMOnSpecificHost().setEntity(false);
                    getRunVMOnSpecificHost().setIsChangable(false);
                }
                else
                {
                    getRunVMOnSpecificHost().setIsChangable(true);
                }
            }
            else if (sender == getRunVMOnSpecificHost())
            {
                if ((Boolean) getRunVMOnSpecificHost().getEntity() == true)
                {
                    getDontMigrateVM().setEntity(true);
                    getDontMigrateVM().setIsChangable(false);
                }
                else
                {
                    // DontMigrateVM.Entity = false;
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

    private void UpdateDomain()
    {
        AsyncDataProvider.GetDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        VmModel vmModel = (VmModel) target;
                        java.util.List<String> domains = (java.util.List<String>) returnValue;
                        String oldDomain = (String) vmModel.getDomain().getSelectedItem();
                        if (oldDomain != null && !oldDomain.equals("") && !domains.contains(oldDomain))
                        {
                            domains.add(0, oldDomain);
                        }
                        vmModel.getDomain().setItems(domains);
                        vmModel.getDomain().setSelectedItem((oldDomain != null) ? oldDomain
                                : Linq.FirstOrDefault(domains));

                    }
                }),
                true);
    }

    private void UpdateNumOfSockets()
    {
        VDSGroup cluster = (VDSGroup) getCluster().getSelectedItem();
        if (cluster == null)
        {
            return;
        }

        String version = cluster.getcompatibility_version().toString();

        int maxNumOfSockets = DataProvider.GetMaxNumOfVmSockets(version);

        getNumOfSockets().setMin(1);
        getNumOfSockets().setInterval(1);
        getNumOfSockets().setMax(maxNumOfSockets);
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

    private void UpdateTotalCpus()
    {
        VDSGroup cluster = (VDSGroup) getCluster().getSelectedItem();
        if (cluster == null)
        {
            return;
        }

        String version = cluster.getcompatibility_version().toString();

        int maxCpus = DataProvider.GetMaxNumOfVmCpus(version);
        int maxCpusPerSocket = DataProvider.GetMaxNumOfCPUsPerSocket(version);
        int numOfSockets = Integer.parseInt(getNumOfSockets().getEntity().toString());

        int totalCPUCores =
                getTotalCPUCores().getEntity() != null ? Integer.parseInt(getTotalCPUCores().getEntity().toString())
                        : 0;

        int realMaxCpus = maxCpus < numOfSockets * maxCpusPerSocket ? maxCpus : numOfSockets * maxCpusPerSocket;
        getTotalCPUCores().setMin(numOfSockets);
        getTotalCPUCores().setMax(realMaxCpus - (realMaxCpus % numOfSockets));
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
            getTotalCPUCores().setEntity(Integer.parseInt((new Double(getTotalCPUCores().getMin())).toString()));
        }
        // if the value is higher than range update it to max
        else if (totalCPUCores > getTotalCPUCores().getMax())
        {
            getTotalCPUCores().setEntity(Integer.parseInt((new Double(getTotalCPUCores().getMax())).toString()));
        }
    }

    private void Provisioning_SelectedItemChanged()
    {
        UpdateIsDisksAvailable();
        UpdateStorageDomains();
    }

    private void DataCenter_SelectedItemChanged()
    {
        Object tempVar = getDataCenter().getSelectedItem();
        storage_pool dataCenter = (storage_pool) ((tempVar instanceof storage_pool) ? tempVar : null);

        java.util.ArrayList<VDSGroup> clusters =
                dataCenter != null ? DataProvider.GetClusterList(dataCenter.getId()) : DataProvider.GetClusterList();

        getCluster().setItems(clusters);
        // Cluster.Value = clusters.FirstOrDefault();
        getCluster().setSelectedItem(Linq.FirstOrDefault(clusters));

        if (dataCenter != null)
        {
            FillTemplateList(dataCenter.getId());
        }
        UpdateCDImages();

        Object tempVar2 = getTemplate().getSelectedItem();
        VmTemplate template = (VmTemplate) ((tempVar2 instanceof VmTemplate) ? tempVar2 : null);
        if (template == null)
        {
            setIsBlankTemplate(true);
            java.util.ArrayList<VmTemplate> listTemplates = (java.util.ArrayList<VmTemplate>) getTemplate().getItems();
            if (listTemplates.size() > 0)
            {
                getTemplate().setSelectedItem(listTemplates.get(0));
            }
        }

        if (dataCenter.getstorage_pool_type() == StorageType.LOCALFS)
        {
            setIsHostAvailable(false);
        }
        else
        {
            setIsHostAvailable(true);
        }
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

        getOSType().setSelectedItem(template.getos());
        getNumOfSockets().setEntity(template.getnum_of_sockets());
        getTotalCPUCores().setEntity(template.getnum_of_cpus());

        getNumOfMonitors().setSelectedItem(template.getnum_of_monitors());
        getDomain().setSelectedItem(template.getdomain());
        getMemSize().setEntity(template.getmem_size_mb());
        getUsbPolicy().setSelectedItem(template.getusb_policy());
        setBootSequence(template.getdefault_boot_sequence());
        getIsHighlyAvailable().setEntity(template.getauto_startup());
        getIsStateless().setEntity(template.getis_stateless());

        UpdateMinAllocatedMemory(true);

        getCdImage().setIsChangable(!StringHelper.isNullOrEmpty(template.getiso_path()));
        if (getCdImage().getIsChangable())
        {
            getCdImage().setSelectedItem(template.getiso_path());
        }

        getTimeZone().setSelectedItem(StringHelper.isNullOrEmpty(template.gettime_zone()) ? Linq.FirstOrDefault(getTimeZone().getItems(),
                new Linq.TimeZonePredicate(DataProvider.GetDefaultTimeZone()))
                : Linq.FirstOrDefault(getTimeZone().getItems(), new Linq.TimeZonePredicate(template.gettime_zone())));

        // IEnumerable<VDSGroup> clusters = Cluster.Options.Cast<VDSGroup>();
        if (getCluster().getItems() == null)
        {
            return;
        }
        java.util.ArrayList<VDSGroup> clusters = Linq.<VDSGroup> Cast(getCluster().getItems());
        // VDSGroup clusterToSelect = clusters.FirstOrDefault(a => a.ID == template.vds_group_id);
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
            // clusterToSelect = clusters.FirstOrDefault();
            clusterToSelect = Linq.FirstOrDefault(clusters);
        }

        getCluster().setSelectedItem(clusterToSelect);

        // BZ#549348 (SPICE should be the default protocol for new Desktop).
        // Because of this bug, the Display Protocol is read only in case of
        // new Server or in case the template has the SPICE protocol as the
        // default display type:
        if (getVmType() == VmType.Server || template.getdefault_display_type() == DisplayType.qxl)
        {
            // DisplayProtocol.Value = DisplayProtocol.Options
            // .Cast<EntityModel>()
            // .FirstOrDefault(a => (DisplayType)a.Entity == template.default_display_type);
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
        // by default, take kernel params from template
        if (getIsNew())
        {
            getKernel_path().setEntity(template.getkernel_url());
            getKernel_parameters().setEntity(template.getkernel_params());
            getInitrd_path().setEntity(template.getinitrd_url());
        }
        if (!template.getId().equals(NGuid.Empty))
        {
            setIsBlankTemplate(false);
            getProvisioning().setIsChangable(true);
            // Retrieve disks.
            if (getIsNew())
            {
                // Disks = DataProvider.GetTemplateDiskList(template.vmt_guid)
                // .OrderBy(a => a.internal_drive_mapping)
                // .Select(a =>
                // new DiskModel()
                // {
                // IsNew = true,
                // Name = a.internal_drive_mapping,
                // Size = { Entity = a.SizeInGigabytes },
                // VolumeType = { SelectedItem = a.volume_type }
                // }
                // )
                // .ToList();

                java.util.ArrayList<DiskImage> diskList = DataProvider.GetTemplateDiskList(template.getId());
                Collections.sort(diskList, new Linq.DiskByInternalDriveMappingComparer());

                java.util.ArrayList<DiskModel> list = new java.util.ArrayList<DiskModel>();
                for (DiskImage a : diskList)
                {
                    DiskModel model = new DiskModel();
                    model.setIsNew(true);
                    model.setName(a.getinternal_drive_mapping());
                    EntityModel tempVar2 = new EntityModel();
                    tempVar2.setEntity(a.getSizeInGigabytes());
                    model.setSize(tempVar2);
                    ListModel tempVar3 = new ListModel();
                    tempVar3.setItems(DataProvider.GetVolumeTypeList());
                    tempVar3.setSelectedItem(a.getvolume_type());
                    model.setVolumeType(tempVar3);
                    list.add(model);
                }

                setDisks(list);
            }
            UpdateIsDisksAvailable();
        }
        else
        {
            setIsBlankTemplate(true);
            setIsDisksAvailable(false);
            getProvisioning().setIsChangable(false);
            setDisks(null);
        }

        // Set priority
        int priority = DataProvider.RoundPriority(template.getpriority());
        // Priority.SelectedItem = Priority.Items
        // .Cast<EntityModel>()
        // .FirstOrDefault(a => (int)a.Entity == priority);

        Object prioritySelectedItem = null;
        for (Object item : getPriority().getItems())
        {
            EntityModel a = (EntityModel) item;
            if ((Integer) a.getEntity() == priority)
            {
                prioritySelectedItem = a;
                break;
            }
        }
        getPriority().setSelectedItem(prioritySelectedItem);
    }

    private void Cluster_SelectedItemChanged()
    {
        VDSGroup cluster = (VDSGroup) getCluster().getSelectedItem();

        java.util.ArrayList<VDS> hosts =
                cluster != null ? DataProvider.GetHostListByCluster(cluster.getname()) : new java.util.ArrayList<VDS>();
        getDefaultHost().setItems(hosts);
        getDefaultHost().setSelectedItem(Linq.FirstOrDefault(hosts));

        if (cluster != null)
        {
            _maxMemSize32 = DataProvider.GetMaximalVmMemSize32OS();
            _maxMemSize64 = DataProvider.GetMaximalVmMemSize64OS();

            setIsCustomPropertiesAvailable(DataProvider.IsSupportCustomProperties(cluster.getcompatibility_version()
                    .toString()));
        }

        // int numOfSockets = cluster != null
        // ? DataProvider.GetNumOfVmSockets(cluster.compatibility_version.ToString())
        // : 1;

        // NumOfSockets.Entity = NumOfSockets.Min = NumOfSockets.Interval = 1;
        // NumOfSockets.Max = numOfSockets;
        UpdateNumOfSockets();
        UpdateMinAllocatedMemory(false);
        // UpdateTotalCpus();
    }

    private void OSType_SelectedItemChanged()
    {
        setIsWindowsOS(DataProvider.IsWindowsOsType((VmOsType) getOSType().getSelectedItem()));
        setIsLinux_Unassign_UnknownOS(DataProvider.IsLinuxOsType((VmOsType) getOSType().getSelectedItem())
                || ((VmOsType) getOSType().getSelectedItem()) == VmOsType.Unassigned
                || ((VmOsType) getOSType().getSelectedItem()) == VmOsType.Other);

        getInitrd_path().setIsChangable(getIsLinux_Unassign_UnknownOS());
        getInitrd_path().setIsAvailable(getIsLinux_Unassign_UnknownOS());

        getKernel_path().setIsChangable(getIsLinux_Unassign_UnknownOS());
        getKernel_path().setIsAvailable(getIsLinux_Unassign_UnknownOS());

        getKernel_parameters().setIsChangable(getIsLinux_Unassign_UnknownOS());
        getKernel_parameters().setIsAvailable(getIsLinux_Unassign_UnknownOS());

        getDomain().setIsChangable(getIsWindowsOS());
        getDomain().setIsAvailable(getIsWindowsOS());

        getTimeZone().setIsChangable(getIsWindowsOS());
        getTimeZone().setIsAvailable(getIsWindowsOS());

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

        // var list = FirstBootDevice.Items
        // .Cast<EntityModel>()
        // .Where(a => (BootSequence)a.Entity != firstDevice)
        // .ToList();
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

    private void UpdateIsDisksAvailable()
    {
        setIsDisksAvailable(getIsNew() && (Boolean) getProvisioning().getSelectedItem() && (getDisks() != null));
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
        java.util.ArrayList<VmTemplate> templates = DataProvider.GetTemplateList(DataCenterId);

        VmTemplate oldTemplate = (VmTemplate) getTemplate().getSelectedItem();
        getTemplate().setItems(templates);

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
                    all = true;
                    break;
                }
            }
        }

        if (oldTemplate == null || all)
        // || templates.All(a => a.vmt_guid != oldTemplateValue.vmt_guid))
        {
            // Template.SelectedItem = templates.FirstOrDefault(a => a.vmt_guid == Guid.Empty);
            for (VmTemplate a : templates)
            {
                if (a.getId().equals(NGuid.Empty))
                {
                    getTemplate().setSelectedItem(a);
                    break;
                }
            }
        }
    }

    private void SetGUIByVMType()
    {
        Iterable<Integer> numOfMonitors;

        if (getVmType() == VmType.Desktop)
        {
            numOfMonitors = DataProvider.GetNumOfMonitorList();
            // numOfCPUs = DataProvider.GetNumOfCPUList().Where(a => a <= 4);
            getUsbPolicy().setSelectedItem(org.ovirt.engine.core.common.businessentities.UsbPolicy.Enabled);
        }
        else
        {
            numOfMonitors = new java.util.ArrayList<Integer>(java.util.Arrays.asList(new Integer[] { 1 }));
        }

        getNumOfMonitors().setItems(numOfMonitors);
        getNumOfMonitors().setSelectedItem(Linq.FirstOrDefault(numOfMonitors));

        // DisplayProtocol.SelectedItem = DisplayProtocol.Items
        // .Cast<EntityModel>()
        // .First(a => (DisplayType)a.Entity == (VmType == VmType.Desktop ? DisplayType.qxl : DisplayType.vnc));
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

    private void UpdateCDImages()
    {
        Object tempVar = getDataCenter().getSelectedItem();
        storage_pool dataCenter = (storage_pool) ((tempVar instanceof storage_pool) ? tempVar : null);
        if (dataCenter != null)
        {
            NGuid vds_id = null;
            if ((Boolean) getIsAutoAssign().getEntity() == false && (getDefaultHost().getSelectedItem() != null))
            {
                vds_id = ((VDS) getDefaultHost().getSelectedItem()).getvds_id();
                vds_id = vds_id.equals(NGuid.Empty) ? null : vds_id;
            }

            java.util.ArrayList<String> images = DataProvider.GetIrsImageList(dataCenter.getId(), false);
            getCdImage().setItems(images);

            if (getCdImage().getIsChangable() && getCdImage().getSelectedItem() == null)
            {
                getCdImage().setSelectedItem(Linq.FirstOrDefault(images));
            }
        }
    }

    private void UpdateStorageDomains()
    {
        Object tempVar = getTemplate().getSelectedItem();
        VmTemplate template = (VmTemplate) ((tempVar instanceof VmTemplate) ? tempVar : null);

        if (template != null && !template.getId().equals(NGuid.Empty))
        {
            storage_pool dataCenter = (storage_pool) getDataCenter().getSelectedItem();

            // var storageDomains = !(bool)Provisioning.getSelectedItem()
            // ? DataProvider.GetStorageDomainListByTemplate(template.vmt_guid)
            // : DataProvider.GetStorageDomainList(dataCenter.id)
            // .Where(a => a.storage_domain_type == StorageDomainType.Data || a.storage_domain_type ==
            // StorageDomainType.Master);
            java.util.ArrayList<storage_domains> storageDomains;
            if ((Boolean) getProvisioning().getSelectedItem() == false)
            {
                storageDomains = DataProvider.GetStorageDomainListByTemplate(template.getId());
            }
            else
            {
                storageDomains = new java.util.ArrayList<storage_domains>();
                for (storage_domains a : DataProvider.GetStorageDomainList(dataCenter.getId()))
                {
                    if (a.getstorage_domain_type() == StorageDomainType.Data
                            || a.getstorage_domain_type() == StorageDomainType.Master)
                    {
                        storageDomains.add(a);
                    }
                }
            }

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
        else
        {
            getStorageDomain().setItems(new java.util.ArrayList<storage_domains>());
            getStorageDomain().setSelectedItem(null);
            getStorageDomain().setIsChangable(false);
        }
    }

    public boolean Validate()
    {
        VmOsType os = (VmOsType) getOSType().getSelectedItem();
        String nameExpr = StringFormat.format("^[0-9a-zA-Z-_]{1,%1$s}$", WINDOWS_VM_NAME_MAX_LIMIT);
        String nameMsg =
                StringFormat.format("Name must contain only alphanumeric characters. Maximum length: %1$s.",
                        WINDOWS_VM_NAME_MAX_LIMIT);

        if (!DataProvider.IsWindowsOsType(os))
        {
            nameExpr = StringFormat.format("^[-\\w]{1,%1$s}$", NON_WINDOWS_VM_NAME_MAX_LIMIT);
            nameMsg =
                    StringFormat.format("Name cannot contain blanks or special characters. Maximum length: %1$s.",
                            NON_WINDOWS_VM_NAME_MAX_LIMIT);
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

        ValidateMemorySize(getMemSize(), maxMemSize, _minMemSize);

        // Minimum 'Physical Memory Guaranteed' is 1MB
        ValidateMemorySize(getMinAllocatedMemory(), (Integer) getMemSize().getEntity(), 1);

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

        if (this.isLinux_Unassign_UnknownOS
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

        if (getCustomProperties().getEntity() == null)
        {
            getCustomProperties().setEntity("");
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

        if ((Boolean) getIsAutoAssign().getEntity() == false)
        {
            getDefaultHost().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
            setIsHostTabValid(getDefaultHost().getIsValid());
        }
        else
        {
            getDefaultHost().setIsValid(true);
            setIsHostTabValid(true);
        }

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

    private void ValidateMemorySize(EntityModel memorySizeEntityModel, int maxMemSize, int minMemSize)
    {
        memorySizeEntityModel.setIsValid(true);

        int memSize = (Integer) memorySizeEntityModel.getEntity();

        if (memSize == 0)
        {
            memorySizeEntityModel.setIsValid(false);
            memorySizeEntityModel.getInvalidityReasons()
                    .add(StringFormat.format("Memory size is between %1$s MB and %2$s MB", minMemSize, maxMemSize));
        }
        else if (memSize > maxMemSize)
        {
            memorySizeEntityModel.setIsValid(false);
            memorySizeEntityModel.getInvalidityReasons().add(StringFormat.format("Maximum memory size is %1$s MB.",
                    maxMemSize));
        }
        else if (memSize < minMemSize)
        {
            memorySizeEntityModel.setIsValid(false);
            memorySizeEntityModel.getInvalidityReasons().add(StringFormat.format("Minimum memory size is %1$s MB.",
                    minMemSize));
        }
    }

    public BootSequence getBootSequence()
    {
        EntityModel firstSelectedItem = (EntityModel) getFirstBootDevice().getSelectedItem();
        EntityModel secondSelectedItem = (EntityModel) getSecondBootDevice().getSelectedItem();

        return BootSequence.valueOf(StringFormat.format("%1$s%2$s",
                firstSelectedItem.getEntity(),
                secondSelectedItem.getEntity()));
    }

    public void setBootSequence(BootSequence value)
    {
        // var items = value.ToString().Select(a => (BootSequence)Enum.Parse(typeof(BootSequence), a.ToString()));
        java.util.ArrayList<BootSequence> items = new java.util.ArrayList<BootSequence>();
        for (char a : value.toString().toCharArray())
        {
            items.add(BootSequence.valueOf((new Character(a)).toString()));
        }

        // FirstBootDevice.SelectedItem = FirstBootDevice.Items
        // .Cast<EntityModel>()
        // .FirstOrDefault(a => (BootSequence)a.Entity == items.First());
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

        // var secondDeviceOptions = SecondBootDevice.Items
        // .Cast<EntityModel>()
        // .ToList();

        java.util.ArrayList<EntityModel> secondDeviceOptions =
                Linq.<EntityModel> Cast(getSecondBootDevice().getItems());

        // SecondBootDevice.SelectedItem = items.Count() > 1
        // ? secondDeviceOptions.Where(a => a.Entity != null).First(a => (BootSequence)a.Entity == items.Last())
        // : secondDeviceOptions.First(a => a.Entity == null);

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
