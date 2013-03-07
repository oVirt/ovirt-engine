package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.validation.ByteSizeValidation;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NoTrimmingWhitespacesValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyQuotaValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.PoolNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class UnitVmModel extends Model {

    public static final int WINDOWS_VM_NAME_MAX_LIMIT = 15;
    public static final int NON_WINDOWS_VM_NAME_MAX_LIMIT = 64;
    public static final int VM_TEMPLATE_NAME_MAX_LIMIT = 40;
    public static final int DESCRIPTION_MAX_LIMIT = 255;

    private boolean privateIsNew;

    public boolean getIsNew()
    {
        return privateIsNew;
    }

    public void setIsNew(boolean value)
    {
        privateIsNew = value;
    }

    private boolean vmAttachedToPool;

    public boolean isVmAttachedToPool() {
        return vmAttachedToPool;
    }

    /**
     * Note: We assume that this method is called only once, on the creation stage
     * of the model. if this assumption is changed (i.e the VM can attached/detached
     * from a pool after the model is created), this method should be modified
     */
    public void setVmAttachedToPool(boolean value) {
        if (value) {
            // ==General Tab==
            getDataCenter().setIsChangable(false);
            getCluster().setIsChangable(!value);
            getQuota().setIsChangable(false);
            getDescription().setIsChangable(false);

            getNumOfDesktops().setIsChangable(false);
            getPrestartedVms().setIsChangable(false);

            getTemplate().setIsChangable(false);
            getMemSize().setIsChangable(false);
            getTotalCPUCores().setIsChangable(false);

            getCoresPerSocket().setIsChangable(false);
            getNumOfSockets().setIsChangable(false);

            getOSType().setIsChangable(false);
            getIsStateless().setIsChangable(false);
            getIsDeleteProtected().setIsChangable(false);

            // ==Initial run Tab==
            getTimeZone().setIsChangable(false);
            getDomain().setIsChangable(false);

            // ==Console Tab==
            getDisplayProtocol().setIsChangable(false);
            getUsbPolicy().setIsChangable(false);
            getNumOfMonitors().setIsChangable(false);
            getIsSmartcardEnabled().setIsChangable(false);
            getAllowConsoleReconnect().setIsChangable(false);

            // ==Host Tab==
            getIsAutoAssign().setIsChangable(false);
            getDefaultHost().setIsChangable(false);
            getHostCpu().setIsChangable(false);
            getMigrationMode().setIsChangable(false);
            getCpuPinning().setIsChangable(false);

            // ==Resource Allocation Tab==
            getMinAllocatedMemory().setIsChangable(false);
            getProvisioning().setIsChangable(false);
            getProvisioningThin_IsSelected().setIsChangable(false);
            getProvisioningClone_IsSelected().setIsChangable(false);
            getDisksAllocationModel().setIsChangable(false);

            // ==Boot Options Tab==
            getFirstBootDevice().setIsChangable(false);
            getSecondBootDevice().setIsChangable(false);
            getCdAttached().setIsChangable(false);
            getCdImage().setIsChangable(false);
            getKernel_path().setIsChangable(false);
            getInitrd_path().setIsChangable(false);
            getKernel_parameters().setIsChangable(false);

            // ==Custom Properties Tab==
            getCustomProperties().setIsChangable(false);

            vmAttachedToPool = true;
        }
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

    private String privateHash;

    public String getHash()
    {
        return privateHash;
    }

    public void setHash(String value)
    {
        privateHash = value;
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsBlankTemplate")); //$NON-NLS-1$
        }
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsWindowsOS")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsLinux_Unassign_UnknownOS")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("CPUNotification")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsCPUsAmountValid")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsGeneralTabValid")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsFirstRunTabValid")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsDisplayTabValid")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsAllocationTabValid")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsHostTabValid")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsBootSequenceTabValid")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsCustomPropertiesTabValid")); //$NON-NLS-1$
        }
    }

    private NotChangableForVmInPoolListModel privateDataCenter;

    public ListModel getDataCenter()
    {
        return privateDataCenter;
    }

    private void setDataCenter(NotChangableForVmInPoolListModel value)
    {
        privateDataCenter = value;
    }

    private NotChangableForVmInPoolListModel privateStorageDomain;

    public ListModel getStorageDomain()
    {
        return privateStorageDomain;
    }

    private void setStorageDomain(NotChangableForVmInPoolListModel value)
    {
        privateStorageDomain = value;
    }

    private NotChangableForVmInPoolListModel privateTemplate;

    public ListModel getTemplate()
    {
        return privateTemplate;
    }

    private void setTemplate(NotChangableForVmInPoolListModel value)
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

    private NotChangableForVmInPoolListModel privateOSType;

    public ListModel getOSType()
    {
        return privateOSType;
    }

    private void setOSType(NotChangableForVmInPoolListModel value)
    {
        privateOSType = value;
    }

    private NotChangableForVmInPoolListModel privateNumOfMonitors;

    public ListModel getNumOfMonitors()
    {
        return privateNumOfMonitors;
    }

    private void setNumOfMonitors(NotChangableForVmInPoolListModel value)
    {
        privateNumOfMonitors = value;
    }

    private NotChangableForVmInPoolEntityModel privateAllowConsoleReconnect;

    public EntityModel getAllowConsoleReconnect()
    {
        return privateAllowConsoleReconnect;
    }

    private void setAllowConsoleReconnect(NotChangableForVmInPoolEntityModel value)
    {
        privateAllowConsoleReconnect = value;
    }

    private NotChangableForVmInPoolEntityModel privateDescription;

    public EntityModel getDescription()
    {
        return privateDescription;
    }

    private void setDescription(NotChangableForVmInPoolEntityModel value)
    {
        privateDescription = value;
    }

    private NotChangableForVmInPoolListModel privateDomain;

    public ListModel getDomain()
    {
        return privateDomain;
    }

    private void setDomain(NotChangableForVmInPoolListModel value)
    {
        privateDomain = value;
    }

    private NotChangableForVmInPoolEntityModel privateMemSize;

    public EntityModel getMemSize()
    {
        return privateMemSize;
    }

    private void setMemSize(NotChangableForVmInPoolEntityModel value)
    {
        privateMemSize = value;
    }

    private NotChangableForVmInPoolEntityModel privateMinAllocatedMemory;

    public EntityModel getMinAllocatedMemory()
    {
        return privateMinAllocatedMemory;
    }

    private void setMinAllocatedMemory(NotChangableForVmInPoolEntityModel value)
    {
        privateMinAllocatedMemory = value;
    }

    private NotChangableForVmInPoolListModel privateQuota;

    public ListModel getQuota()
    {
        return privateQuota;
    }

    private void setQuota(NotChangableForVmInPoolListModel value)
    {
        privateQuota = value;
    }

    private NotChangableForVmInPoolListModel privateCluster;

    public ListModel getCluster()
    {
        return privateCluster;
    }

    private void setCluster(NotChangableForVmInPoolListModel value)
    {
        privateCluster = value;
    }

    private NotChangableForVmInPoolListModel privateUsbPolicy;

    public ListModel getUsbPolicy()
    {
        return privateUsbPolicy;
    }

    private void setUsbPolicy(NotChangableForVmInPoolListModel value)
    {
        privateUsbPolicy = value;
    }

    private NotChangableForVmInPoolListModel privateTimeZone;

    public ListModel getTimeZone()
    {
        return privateTimeZone;
    }

    private void setTimeZone(NotChangableForVmInPoolListModel value)
    {
        privateTimeZone = value;
    }

    private NotChangableForVmInPoolListModel privateNumOfSockets;

    public ListModel getNumOfSockets()
    {
        return privateNumOfSockets;
    }

    private void setNumOfSockets(NotChangableForVmInPoolListModel value)
    {
        privateNumOfSockets = value;
    }

    private NotChangableForVmInPoolEntityModel privateTotalCPUCores;

    public EntityModel getTotalCPUCores()
    {
        return privateTotalCPUCores;
    }

    private void setTotalCPUCores(NotChangableForVmInPoolEntityModel value)
    {
        privateTotalCPUCores = value;
    }

    private NotChangableForVmInPoolListModel privateCoresPerSocket;

    public ListModel getCoresPerSocket()
    {
        return privateCoresPerSocket;
    }

    private void setCoresPerSocket(NotChangableForVmInPoolListModel value)
    {
        privateCoresPerSocket = value;
    }

    private NotChangableForVmInPoolListModel privateDefaultHost;

    public ListModel getDefaultHost()
    {
        return privateDefaultHost;
    }

    private void setDefaultHost(NotChangableForVmInPoolListModel value)
    {
        privateDefaultHost = value;
    }

    private NotChangableForVmInPoolEntityModel privateisSmartcardEnabled;

    public EntityModel getIsSmartcardEnabled()
    {
        return privateisSmartcardEnabled;
    }

    private void setIsSmartcardEnabled(NotChangableForVmInPoolEntityModel value)
    {
        privateisSmartcardEnabled = value;
    }

    private NotChangableForVmInPoolEntityModel privateIsStateless;

    public EntityModel getIsStateless()
    {
        return privateIsStateless;
    }

    private void setIsStateless(NotChangableForVmInPoolEntityModel value)
    {
        privateIsStateless = value;
    }

    private NotChangableForVmInPoolEntityModel privateIsDeleteProtected;

    public EntityModel getIsDeleteProtected() {
        return privateIsDeleteProtected;
    }

    public void setIsDeleteProtected(NotChangableForVmInPoolEntityModel deleteProtected) {
        this.privateIsDeleteProtected = deleteProtected;
    }

    private NotChangableForVmInPoolListModel privateDisplayProtocol;

    public ListModel getDisplayProtocol()
    {
        return privateDisplayProtocol;
    }

    private void setDisplayProtocol(NotChangableForVmInPoolListModel value)
    {
        privateDisplayProtocol = value;
    }

    private NotChangableForVmInPoolEntityModel privateProvisioning;

    public EntityModel getProvisioning()
    {
        return privateProvisioning;
    }

    private void setProvisioning(NotChangableForVmInPoolEntityModel value)
    {
        privateProvisioning = value;
    }

    private NotChangableForVmInPoolEntityModel privateProvisioningThin_IsSelected;

    public EntityModel getProvisioningThin_IsSelected()
    {
        return privateProvisioningThin_IsSelected;
    }

    public void setProvisioningThin_IsSelected(NotChangableForVmInPoolEntityModel value)
    {
        privateProvisioningThin_IsSelected = value;
    }

    private NotChangableForVmInPoolEntityModel privateProvisioningClone_IsSelected;

    public EntityModel getProvisioningClone_IsSelected()
    {
        return privateProvisioningClone_IsSelected;
    }

    public void setProvisioningClone_IsSelected(NotChangableForVmInPoolEntityModel value)
    {
        privateProvisioningClone_IsSelected = value;
    }

    private NotChangableForVmInPoolListModel privatePriority;

    public ListModel getPriority()
    {
        return privatePriority;
    }

    private void setPriority(NotChangableForVmInPoolListModel value)
    {
        privatePriority = value;
    }

    private NotChangableForVmInPoolEntityModel privateIsHighlyAvailable;

    public EntityModel getIsHighlyAvailable()
    {
        return privateIsHighlyAvailable;
    }

    private void setIsHighlyAvailable(NotChangableForVmInPoolEntityModel value)
    {
        privateIsHighlyAvailable = value;
    }

    private NotChangableForVmInPoolListModel privateFirstBootDevice;

    public ListModel getFirstBootDevice()
    {
        return privateFirstBootDevice;
    }

    private void setFirstBootDevice(NotChangableForVmInPoolListModel value)
    {
        privateFirstBootDevice = value;
    }

    private NotChangableForVmInPoolListModel privateSecondBootDevice;

    public ListModel getSecondBootDevice()
    {
        return privateSecondBootDevice;
    }

    private void setSecondBootDevice(NotChangableForVmInPoolListModel value)
    {
        privateSecondBootDevice = value;
    }

    private NotChangableForVmInPoolListModel privateCdImage;

    public ListModel getCdImage()
    {
        return privateCdImage;
    }

    private void setCdImage(NotChangableForVmInPoolListModel value)
    {
        privateCdImage = value;
    }

    private NotChangableForVmInPoolEntityModel cdAttached;

    public EntityModel getCdAttached() {
        return cdAttached;
    }

    public void setCdAttached(NotChangableForVmInPoolEntityModel value) {
        cdAttached = value;
    }

    private NotChangableForVmInPoolEntityModel privateInitrd_path;

    public EntityModel getInitrd_path()
    {
        return privateInitrd_path;
    }

    private void setInitrd_path(NotChangableForVmInPoolEntityModel value)
    {
        privateInitrd_path = value;
    }

    private NotChangableForVmInPoolEntityModel privateKernel_path;

    public EntityModel getKernel_path()
    {
        return privateKernel_path;
    }

    private void setKernel_path(NotChangableForVmInPoolEntityModel value)
    {
        privateKernel_path = value;
    }

    private NotChangableForVmInPoolEntityModel privateKernel_parameters;

    public EntityModel getKernel_parameters()
    {
        return privateKernel_parameters;
    }

    private void setKernel_parameters(NotChangableForVmInPoolEntityModel value)
    {
        privateKernel_parameters = value;
    }

    private NotChangableForVmInPoolEntityModel privateCustomProperties;

    public EntityModel getCustomProperties()
    {
        return privateCustomProperties;
    }

    private void setCustomProperties(NotChangableForVmInPoolEntityModel value)
    {
        privateCustomProperties = value;
    }

    private NotChangableForVmInPoolKeyValueModel customPropertySheet;

    public KeyValueModel getCustomPropertySheet() {
        return customPropertySheet;
    }

    public void setCustomPropertySheet(NotChangableForVmInPoolKeyValueModel customPropertySheet) {
        this.customPropertySheet = customPropertySheet;
    }

    private HashMap<Version, ArrayList<String>> privateCustomPropertiesKeysList;

    public HashMap<Version, ArrayList<String>> getCustomPropertiesKeysList()
    {
        return privateCustomPropertiesKeysList;
    }

    public void setCustomPropertiesKeysList(HashMap<Version, ArrayList<String>> value)
    {
        privateCustomPropertiesKeysList = value;
    }

    private NotChangableForVmInPoolEntityModel privateIsAutoAssign;

    public EntityModel getIsAutoAssign()
    {
        return privateIsAutoAssign;
    }

    public void setIsAutoAssign(NotChangableForVmInPoolEntityModel value)
    {
        privateIsAutoAssign = value;
    }

    private NotChangableForVmInPoolEntityModel hostCpu;

    public EntityModel getHostCpu() {
        return hostCpu;
    }

    public void setHostCpu(NotChangableForVmInPoolEntityModel hostCpu) {
        this.hostCpu = hostCpu;
    }

    private NotChangableForVmInPoolListModel migrationMode;

    public ListModel getMigrationMode()
    {
        return migrationMode;
    }

    public void setMigrationMode(NotChangableForVmInPoolListModel value)
    {
        migrationMode = value;
    }

    private NotChangableForVmInPoolEntityModel privateIsTemplatePublic;

    public EntityModel getIsTemplatePublic()
    {
        return privateIsTemplatePublic;
    }

    private void setIsTemplatePublic(NotChangableForVmInPoolEntityModel value)
    {
        privateIsTemplatePublic = value;
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

    private List<DiskModel> disks;

    public List<DiskModel> getDisks()
    {
        return disks;
    }

    public void setDisks(List<DiskModel> value)
    {
        if (disks != value)
        {
            disks = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Disks")); //$NON-NLS-1$
        }
    }

    private DisksAllocationModel disksAllocationModel;

    public DisksAllocationModel getDisksAllocationModel()
    {
        return disksAllocationModel;
    }

    private void setDisksAllocationModel(DisksAllocationModel value)
    {
        disksAllocationModel = value;
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsDisksAvailable")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsHostAvailable")); //$NON-NLS-1$
        }
    }

    private boolean isCustomPropertiesTabAvailable;

    public boolean getIsCustomPropertiesTabAvailable()
    {
        return isCustomPropertiesTabAvailable;
    }

    public void setIsCustomPropertiesTabAvailable(boolean value)
    {
        if (isCustomPropertiesTabAvailable != value)
        {
            isCustomPropertiesTabAvailable = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsCustomPropertiesTabAvailable")); //$NON-NLS-1$
        }
    }

    private Boolean isDatacenterAvailable;

    public Boolean getIsDatacenterAvailable()
    {
        return isDatacenterAvailable;
    }

    public void setIsDatacenterAvailable(Boolean value)
    {
        if (isDatacenterAvailable == null && value == null)
        {
            return;
        }
        if (isDatacenterAvailable == null || !isDatacenterAvailable.equals(value))
        {
            isDatacenterAvailable = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsDatacenterAvailable")); //$NON-NLS-1$
        }
    }

    private final VmModelBehaviorBase behavior;

    public VmModelBehaviorBase getBehavior() {
        return behavior;
    }

    private void setBehavior(VmModelBehaviorBase value) {
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

    private int _maxMemSize32 = 20480;

    public int get_MaxMemSize32()
    {
        return _maxMemSize32;
    }

    public void set_MaxMemSize32(int value)
    {
        _maxMemSize32 = value;
    }

    private int _maxMemSize64 = 262144;

    public int get_MaxMemSize64()
    {
        return _maxMemSize64;
    }

    public void set_MaxMemSize64(int value)
    {
        _maxMemSize64 = value;
    }

    private NotChangableForVmInPoolEntityModel cpuPinning;

    public EntityModel getCpuPinning() {
        return cpuPinning;
    }

    public void setCpuPinning(NotChangableForVmInPoolEntityModel cpuPinning) {
        this.cpuPinning = cpuPinning;
    }

    public UnitVmModel(VmModelBehaviorBase behavior)
    {
        Frontend.getQueryStartedEvent().addListener(this);
        Frontend.getQueryCompleteEvent().addListener(this);

        Frontend.Subscribe(new VdcQueryType[] { VdcQueryType.GetStorageDomainsByStoragePoolId,
                VdcQueryType.GetAllIsoImagesListByStoragePoolId, VdcQueryType.GetTimeZones,
                VdcQueryType.GetDefualtTimeZone, VdcQueryType.GetStoragePoolsByClusterService,
                VdcQueryType.GetDomainList, VdcQueryType.GetConfigurationValue,
                VdcQueryType.GetVdsGroupsByStoragePoolId, VdcQueryType.GetVmTemplatesByStoragePoolId,
                VdcQueryType.GetVmTemplatesDisks, VdcQueryType.GetStorageDomainsByVmTemplateId,
                VdcQueryType.GetStorageDomainById, VdcQueryType.GetDataCentersWithPermittedActionOnClusters,
                VdcQueryType.GetClustersWithPermittedAction, VdcQueryType.GetVmTemplatesWithPermittedAction,
                VdcQueryType.GetVdsGroupById, VdcQueryType.GetStoragePoolById, VdcQueryType.GetAllDisksByVmId,
                VdcQueryType.GetVmTemplate, VdcQueryType.GetVmConfigurationBySnapshot,
                VdcQueryType.GetPermittedStorageDomainsByStoragePoolId, VdcQueryType.Search });

        this.behavior = behavior;
        this.behavior.setModel(this);

        setStorageDomain(new NotChangableForVmInPoolListModel());
        setName(new NotChangableForVmInPoolEntityModel());
        setNumOfMonitors(new NotChangableForVmInPoolListModel());
        setAllowConsoleReconnect(new NotChangableForVmInPoolEntityModel());
        setDescription(new NotChangableForVmInPoolEntityModel());
        setDomain(new NotChangableForVmInPoolListModel());
        setMinAllocatedMemory(new NotChangableForVmInPoolEntityModel());
        setUsbPolicy(new NotChangableForVmInPoolListModel());
        setIsStateless(new NotChangableForVmInPoolEntityModel());
        setIsSmartcardEnabled(new NotChangableForVmInPoolEntityModel());
        setIsDeleteProtected(new NotChangableForVmInPoolEntityModel());

        setCdImage(new NotChangableForVmInPoolListModel());
        getCdImage().setIsChangable(false);

        setCdAttached(new NotChangableForVmInPoolEntityModel());
        getCdAttached().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {

                getCdImage().setIsChangable((Boolean) getCdAttached().getEntity());
            }
        });
        getCdAttached().setEntity(false);

        setIsHighlyAvailable(new NotChangableForVmInPoolEntityModel());
        setIsTemplatePublic(new NotChangableForVmInPoolEntityModel());
        setKernel_parameters(new NotChangableForVmInPoolEntityModel());
        setKernel_path(new NotChangableForVmInPoolEntityModel());
        setInitrd_path(new NotChangableForVmInPoolEntityModel());
        setCustomProperties(new NotChangableForVmInPoolEntityModel());
        setCustomPropertySheet(new NotChangableForVmInPoolKeyValueModel());
        setDisplayProtocol(new NotChangableForVmInPoolListModel());
        setSecondBootDevice(new NotChangableForVmInPoolListModel());
        setPriority(new NotChangableForVmInPoolListModel());

        setDataCenter(new NotChangableForVmInPoolListModel());
        getDataCenter().getSelectedItemChangedEvent().addListener(this);

        setTemplate(new NotChangableForVmInPoolListModel());
        getTemplate().getSelectedItemChangedEvent().addListener(this);

        setQuota(new NotChangableForVmInPoolListModel());
        getQuota().setIsAvailable(false);

        setCluster(new NotChangableForVmInPoolListModel());
        getCluster().getSelectedItemChangedEvent().addListener(this);

        setTimeZone(new NotChangableForVmInPoolListModel());
        getTimeZone().getSelectedItemChangedEvent().addListener(this);

        setDefaultHost(new NotChangableForVmInPoolListModel());
        getDefaultHost().getSelectedItemChangedEvent().addListener(this);

        setOSType(new NotChangableForVmInPoolListModel());
        getOSType().getSelectedItemChangedEvent().addListener(this);

        setFirstBootDevice(new NotChangableForVmInPoolListModel());
        getFirstBootDevice().getSelectedItemChangedEvent().addListener(this);

        setProvisioning(new NotChangableForVmInPoolEntityModel());
        getProvisioning().getEntityChangedEvent().addListener(this);

        setMemSize(new NotChangableForVmInPoolEntityModel());
        getMemSize().getEntityChangedEvent().addListener(this);

        setTotalCPUCores(new NotChangableForVmInPoolEntityModel());
        getTotalCPUCores().getEntityChangedEvent().addListener(this);

        setNumOfSockets(new NotChangableForVmInPoolListModel());
        getNumOfSockets().getSelectedItemChangedEvent().addListener(this);

        setCoresPerSocket(new NotChangableForVmInPoolListModel());
        getCoresPerSocket().getSelectedItemChangedEvent().addListener(this);

        setMigrationMode(new NotChangableForVmInPoolListModel());
        getMigrationMode().getSelectedItemChangedEvent().addListener(this);

        setHostCpu(new NotChangableForVmInPoolEntityModel());
        getHostCpu().getEntityChangedEvent().addListener(this);

        setIsAutoAssign(new NotChangableForVmInPoolEntityModel());
        getIsAutoAssign().getEntityChangedEvent().addListener(this);

        setIsTemplatePublic(new NotChangableForVmInPoolEntityModel());
        getIsTemplatePublic().getEntityChangedEvent().addListener(this);

        setIsHostTabValid(true);
        setIsCustomPropertiesTabAvailable(true);
        setIsCustomPropertiesTabValid(getIsHostTabValid());
        setIsBootSequenceTabValid(getIsCustomPropertiesTabValid());
        setIsAllocationTabValid(getIsBootSequenceTabValid());
        setIsDisplayTabValid(getIsAllocationTabValid());
        setIsFirstRunTabValid(getIsDisplayTabValid());
        setIsGeneralTabValid(getIsFirstRunTabValid());

        // NOTE: This is because currently the auto generated view code tries to register events of pooltype for
        // VM/Template views as this model is shared across VM/Template/Pool models
        setPoolType(new NotChangableForVmInPoolListModel());

        setNumOfDesktops(new NotChangableForVmInPoolEntityModel());
        getNumOfDesktops().setEntity(0);
        getNumOfDesktops().setIsAvailable(false);

        setAssignedVms(new NotChangableForVmInPoolEntityModel());
        getAssignedVms().setEntity(0);
        getAssignedVms().setIsAvailable(false);
        // Assigned VMs count is always read-only.
        getAssignedVms().setIsChangable(false);

        setPrestartedVms(new NotChangableForVmInPoolEntityModel());
        getPrestartedVms().setEntity(0);
        getPrestartedVms().setIsAvailable(false);

        setDisksAllocationModel(new DisksAllocationModel());

        setProvisioningClone_IsSelected(new NotChangableForVmInPoolEntityModel());
        getProvisioningClone_IsSelected().getEntityChangedEvent().addListener(this);

        setProvisioningThin_IsSelected(new NotChangableForVmInPoolEntityModel());
        getProvisioningThin_IsSelected().getEntityChangedEvent().addListener(this);

        setCpuPinning(new NotChangableForVmInPoolEntityModel());
        getCpuPinning().setEntity("");
        getCpuPinning().setIsAvailable(false);

        initTimeZones();
    }

    private void initTimeZones() {
        getOSType().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getBehavior().updateTimeZone();
            }
        });

    }

    public void Initialize(SystemTreeItemModel SystemTreeSelectedItem)
    {
        super.Initialize();

        setHash(getHashName() + new Date());

        getMemSize().setEntity(256);
        getMinAllocatedMemory().setEntity(256);
        getIsStateless().setEntity(false);
        getIsSmartcardEnabled().setEntity(false);
        getIsHighlyAvailable().setEntity(false);
        getIsAutoAssign().setEntity(true);
        getIsTemplatePublic().setEntity(true);

        getHostCpu().setEntity(false);
        getMigrationMode().setIsChangable(true);

        getCdImage().setIsChangable(false);

        InitOSType();
        InitDisplayProtocol();
        InitFirstBootDevice();
        InitNumOfMonitors();
        InitAllowConsoleReconnect();
        InitMinimalVmMemSize();
        InitMaximalVmMemSize32OS();
        initMigrationMode();

        behavior.Initialize(SystemTreeSelectedItem);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(Frontend.QueryStartedEventDefinition)
                && StringHelper.stringsEqual(Frontend.getCurrentContext(), getHash()))
        {
            Frontend_QueryStarted();
        }
        else if (ev.matchesDefinition(Frontend.QueryCompleteEventDefinition)
                && StringHelper.stringsEqual(Frontend.getCurrentContext(), getHash()))
        {
            Frontend_QueryComplete();
        }
        else if (ev.matchesDefinition(ListModel.SelectedItemChangedEventDefinition))
        {
            if (sender == getDataCenter())
            {
                DataCenter_SelectedItemChanged(sender, args);
            }
            else if (sender == getTemplate())
            {
                Template_SelectedItemChanged(sender, args);
            }
            else if (sender == getCluster())
            {
                Cluster_SelectedItemChanged(sender, args);
                InitUsbPolicy();
            }
            else if (sender == getTimeZone())
            {
                TimeZone_SelectedItemChanged(sender, args);
            }
            else if (sender == getDefaultHost())
            {
                DefaultHost_SelectedItemChanged(sender, args);
            }
            else if (sender == getOSType())
            {
                OSType_SelectedItemChanged(sender, args);
                InitUsbPolicy();
            }
            else if (sender == getFirstBootDevice())
            {
                FirstBootDevice_SelectedItemChanged(sender, args);
            }
            else if (sender == getDisplayProtocol())
            {
                DisplayProtocol_SelectedItemChanged(sender, args);
                InitUsbPolicy();
            }
            else if (sender == getNumOfSockets())
            {
                NumOfSockets_EntityChanged(sender, args);
            }
            else if (sender == getCoresPerSocket())
            {
                CoresPerSocket_EntityChanged(sender, args);
            }
            else if (sender == getMigrationMode())
            {
                MigrationMode_EntityChanged(sender, args);
            }
        }
        else if (ev.matchesDefinition(EntityModel.EntityChangedEventDefinition))
        {
            if (sender == getMemSize())
            {
                MemSize_EntityChanged(sender, args);
            }
            else if (sender == getTotalCPUCores())
            {
                TotalCPUCores_EntityChanged(sender, args);
            }
            else if (sender == getHostCpu())
            {
                behavior.updateCpuPinningVisibility();
            }
            else if (sender == getProvisioning())
            {
                Provisioning_SelectedItemChanged(sender, args);
            }
            else if (sender == getProvisioningThin_IsSelected())
            {
                if ((Boolean) getProvisioningThin_IsSelected().getEntity()) {
                    getProvisioning().setEntity(false);
                }
            }
            else if (sender == getProvisioningClone_IsSelected())
            {
                if ((Boolean) getProvisioningClone_IsSelected().getEntity()) {
                    getProvisioning().setEntity(true);
                }
            }
        }
    }

    private int queryCounter;

    private void Frontend_QueryStarted()
    {
        queryCounter++;
        if (getProgress() == null)
        {
            StartProgress(null);
        }
    }

    private void Frontend_QueryComplete()
    {
        queryCounter--;
        if (queryCounter == 0)
        {
            StopProgress();
        }
    }

    protected void InitNumOfMonitors()
    {
        if (getVmType() == VmType.Desktop)
        {
            AsyncDataProvider.GetNumOfMonitorList(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            UnitVmModel model = (UnitVmModel) target;
                            Integer oldNumOfMonitors = null;
                            if (model.getNumOfMonitors().getSelectedItem() != null)
                            {
                                oldNumOfMonitors = (Integer) model.getNumOfMonitors().getSelectedItem();
                            }
                            ArrayList<Integer> numOfMonitors = (ArrayList<Integer>) returnValue;
                            model.getNumOfMonitors().setItems(numOfMonitors);
                            if (oldNumOfMonitors != null)
                            {
                                model.getNumOfMonitors().setSelectedItem(oldNumOfMonitors);
                            }

                        }
                    }, getHash()));
        }
        else
        {
            getNumOfMonitors().setItems(new ArrayList<Integer>(Arrays.asList(new Integer[] { 1 })));
            getNumOfMonitors().setSelectedItem(1);
        }
    }

    protected void InitAllowConsoleReconnect()
    {
        getAllowConsoleReconnect().setEntity(getVmType() == VmType.Server);
    }

    private void InitOSType()
    {
        List<VmOsType> osList = Arrays.asList(VmOsType.values());
        Collections.sort(osList, new Comparator<VmOsType>() {

            @Override
            public int compare(VmOsType o1, VmOsType o2) {
                // moving Unassigned to the head of the list
                if (o1.name().equals(VmOsType.Unassigned.name())) {
                    return -1;
                }

                if (o2.name().equals(VmOsType.Unassigned.name())) {
                    return 1;
                }

                return o1.name().compareTo(o2.name());
            }
        });

        getOSType().setItems(osList);
        getOSType().setSelectedItem(VmOsType.Unassigned);
    }

    private void InitUsbPolicy() {
        VDSGroup cluster = (VDSGroup) getCluster().getSelectedItem();
        VmOsType osType = (VmOsType) getOSType().getSelectedItem();
        DisplayType displayType = (DisplayType) (getDisplayProtocol().getSelectedItem() != null ?
                ((EntityModel) getDisplayProtocol().getSelectedItem()).getEntity() : null);

        if (osType == null || cluster == null || displayType == null) {
            return;
        }

        getUsbPolicy().setIsChangable(true);
        if (Version.v3_1.compareTo(cluster.getcompatibility_version()) > 0) {
            if (osType.isWindows()) {
                getUsbPolicy().setItems(Arrays.asList(
                        UsbPolicy.DISABLED,
                        UsbPolicy.ENABLED_LEGACY
                        ));
            } else {
                getUsbPolicy().setItems(Arrays.asList(UsbPolicy.DISABLED));
                getUsbPolicy().setSelectedItem(UsbPolicy.DISABLED);
                getUsbPolicy().setIsChangable(false);
            }
        }

        if (Version.v3_1.compareTo(cluster.getcompatibility_version()) <= 0) {
            if (osType.isLinux()) {
                getUsbPolicy().setItems(Arrays.asList(
                        UsbPolicy.DISABLED,
                        UsbPolicy.ENABLED_NATIVE
                        ));
            } else {
                getUsbPolicy().setItems(
                        Arrays.asList(
                                UsbPolicy.DISABLED,
                                UsbPolicy.ENABLED_LEGACY,
                                UsbPolicy.ENABLED_NATIVE
                                ));
            }
        }

        if (displayType != DisplayType.qxl) {
            getUsbPolicy().setIsChangable(false);
        }

        getUsbPolicy().setSelectedItem(UsbPolicy.DISABLED);
    }

    private void InitMinimalVmMemSize()
    {
        AsyncDataProvider.GetMinimalVmMemSize(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        UnitVmModel vmModel = (UnitVmModel) target;
                        vmModel.set_MinMemSize((Integer) returnValue);

                    }
                }, getHash()));
    }

    private void InitMaximalVmMemSize32OS()
    {
        AsyncDataProvider.GetMaximalVmMemSize32OS(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        UnitVmModel vmModel = (UnitVmModel) target;
                        vmModel.set_MaxMemSize32((Integer) returnValue);

                    }
                }, getHash()));
    }

    private void UpdateMaximalVmMemSize()
    {
        VDSGroup cluster = (VDSGroup) getCluster().getSelectedItem();

        if (cluster != null)
        {
            AsyncDataProvider.GetMaximalVmMemSize64OS(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            UnitVmModel vmModel = (UnitVmModel) target;
                            vmModel.set_MaxMemSize64((Integer) returnValue);

                        }
                    }, getHash()), cluster.getcompatibility_version().toString());
        }
    }

    private void InitDisplayProtocol()
    {
        ArrayList<EntityModel> displayProtocolOptions = new ArrayList<EntityModel>();

        EntityModel spiceProtocol = new EntityModel();
        spiceProtocol.setTitle(ConstantsManager.getInstance().getConstants().spiceTitle());
        spiceProtocol.setEntity(DisplayType.qxl);

        EntityModel vncProtocol = new EntityModel();
        vncProtocol.setTitle(ConstantsManager.getInstance().getConstants().VNCTitle());
        vncProtocol.setEntity(DisplayType.vnc);

        displayProtocolOptions.add(spiceProtocol);
        displayProtocolOptions.add(vncProtocol);
        getDisplayProtocol().setItems(displayProtocolOptions);

        getDisplayProtocol().getSelectedItemChangedEvent().addListener(this);
    }

    private void InitFirstBootDevice()
    {
        EntityModel tempVar = new EntityModel();
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().hardDiskTitle());
        tempVar.setEntity(BootSequence.C);
        EntityModel hardDiskOption = tempVar;

        ArrayList<EntityModel> firstBootDeviceItems = new ArrayList<EntityModel>();
        firstBootDeviceItems.add(hardDiskOption);
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cdromTitle());
        tempVar2.setEntity(BootSequence.D);
        firstBootDeviceItems.add(tempVar2);
        EntityModel tempVar3 = new EntityModel();
        tempVar3.setTitle(ConstantsManager.getInstance().getConstants().networkPXETitle());
        tempVar3.setEntity(BootSequence.N);
        firstBootDeviceItems.add(tempVar3);
        getFirstBootDevice().setItems(firstBootDeviceItems);
        getFirstBootDevice().setSelectedItem(hardDiskOption);
    }

    private void initMigrationMode() {
        getMigrationMode().setItems(Arrays.asList(MigrationSupport.values()));
    }

    private void DataCenter_SelectedItemChanged(Object sender, EventArgs args)
    {
        behavior.DataCenter_SelectedItemChanged();

        storage_pool dataCenter = (storage_pool) getDataCenter().getSelectedItem();
        if (dataCenter != null) {
            getDisksAllocationModel().setQuotaEnforcementType(dataCenter.getQuotaEnforcementType());
        }
    }

    private void Template_SelectedItemChanged(Object sender, EventArgs args)
    {
        behavior.Template_SelectedItemChanged();
    }

    private void Cluster_SelectedItemChanged(Object sender, EventArgs args)
    {
        behavior.Cluster_SelectedItemChanged();
    }

    private void TimeZone_SelectedItemChanged(Object sender, EventArgs args)
    {
    }

    private void DefaultHost_SelectedItemChanged(Object sender, EventArgs args)
    {
        behavior.DefaultHost_SelectedItemChanged();
    }

    private void OSType_SelectedItemChanged(Object sender, EventArgs args)
    {
        VmOsType osType = (VmOsType) getOSType().getSelectedItem();

        setIsWindowsOS(AsyncDataProvider.IsWindowsOsType(osType));
        setIsLinux_Unassign_UnknownOS(AsyncDataProvider.IsLinuxOsType(osType) || osType == VmOsType.Unassigned
                || osType == VmOsType.Other);

        getInitrd_path().setIsChangable(getIsLinux_Unassign_UnknownOS());
        getInitrd_path().setIsAvailable(getIsLinux_Unassign_UnknownOS());

        getKernel_path().setIsChangable(getIsLinux_Unassign_UnknownOS());
        getKernel_path().setIsAvailable(getIsLinux_Unassign_UnknownOS());

        getKernel_parameters().setIsChangable(getIsLinux_Unassign_UnknownOS());
        getKernel_parameters().setIsAvailable(getIsLinux_Unassign_UnknownOS());

        getDomain().setIsChangable(getIsWindowsOS());
        if (getTimeZone().getSelectedItem() == null) {
            getBehavior().updateTimeZone();
        } else {
            getBehavior().updateTimeZone(((Entry<String, String>)getTimeZone().getSelectedItem()).getKey());
        }

    }

    private void FirstBootDevice_SelectedItemChanged(Object sender, EventArgs args)
    {
        EntityModel entityModel = (EntityModel) getFirstBootDevice().getSelectedItem();
        BootSequence firstDevice = (BootSequence) entityModel.getEntity();

        ArrayList<EntityModel> list = new ArrayList<EntityModel>();
        for (Object item : getFirstBootDevice().getItems())
        {
            EntityModel a = (EntityModel) item;
            if ((BootSequence) a.getEntity() != firstDevice)
            {
                list.add(a);
            }
        }

        EntityModel tempVar = new EntityModel();
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().noneTitle());
        EntityModel noneOption = tempVar;

        list.add(0, noneOption);

        getSecondBootDevice().setItems(list);
        getSecondBootDevice().setSelectedItem(noneOption);
    }

    private void Provisioning_SelectedItemChanged(Object sender, EventArgs args)
    {
        behavior.Provisioning_SelectedItemChanged();
    }

    private void DisplayProtocol_SelectedItemChanged(Object sender, EventArgs args)
    {
        EntityModel entityModel = (EntityModel) getDisplayProtocol().getSelectedItem();
        if (entityModel == null)
        {
            return;
        }
        DisplayType type = (DisplayType) entityModel.getEntity();

        if (type == DisplayType.vnc)
        {
            getUsbPolicy().setSelectedItem(org.ovirt.engine.core.common.businessentities.UsbPolicy.DISABLED);
            getIsSmartcardEnabled().setEntity(false);
        }

        getUsbPolicy().setIsChangable(type == DisplayType.qxl);
        getIsSmartcardEnabled().setIsChangable(type == DisplayType.qxl);

        UpdateNumOfMonitors();
    }

    private void MemSize_EntityChanged(Object sender, EventArgs args)
    {
        behavior.UpdateMinAllocatedMemory();
    }

    private void NumOfSockets_EntityChanged(Object sender, EventArgs args)
    {
        behavior.numOfSocketChanged();
    }

    private void TotalCPUCores_EntityChanged(Object sender, EventArgs args) {
        // do not listen on changes while the totalCpuCoresChanged is adjusting them
        getNumOfSockets().getSelectedItemChangedEvent().removeListener(this);
        getTotalCPUCores().getEntityChangedEvent().removeListener(this);
        getCoresPerSocket().getSelectedItemChangedEvent().removeListener(this);

        behavior.totalCpuCoresChanged();

        // start listening again
        getTotalCPUCores().getEntityChangedEvent().addListener(this);
        getNumOfSockets().getSelectedItemChangedEvent().addListener(this);
        getCoresPerSocket().getSelectedItemChangedEvent().addListener(this);
    }

    private void CoresPerSocket_EntityChanged(Object sender, EventArgs args) {
        behavior.coresPerSocketChanged();
    }

    private void MigrationMode_EntityChanged(Object sender, EventArgs args)
    {
        if (MigrationSupport.PINNED_TO_HOST == getMigrationMode().getSelectedItem()) {
            getHostCpu().setIsChangable(true);
        } else {
            getHostCpu().setEntity(false);
            getHostCpu().setIsChangable(false);
        }

        behavior.updateCpuPinningVisibility();
    }

    private void UpdateNumOfMonitors()
    {
        boolean isVnc = false;

        if (getDisplayProtocol().getSelectedItem() != null)
        {
            DisplayType displayType = (DisplayType) ((EntityModel) getDisplayProtocol().getSelectedItem()).getEntity();
            isVnc = displayType == DisplayType.vnc;
        }

        if (isVnc)
        {
            getNumOfMonitors().setSelectedItem(1);
            getNumOfMonitors().setIsChangable(false);
        } else {
            getNumOfMonitors().setIsChangable(true);
        }
    }

    public BootSequence getBootSequence()
    {
        EntityModel firstSelectedItem = (EntityModel) getFirstBootDevice().getSelectedItem();
        EntityModel secondSelectedItem = (EntityModel) getSecondBootDevice().getSelectedItem();

        String firstSelectedString =
                firstSelectedItem.getEntity() == null ? "" : firstSelectedItem.getEntity().toString(); //$NON-NLS-1$
        String secondSelectedString =
                secondSelectedItem.getEntity() == null ? "" : secondSelectedItem.getEntity().toString(); //$NON-NLS-1$

        return BootSequence.valueOf(firstSelectedString + secondSelectedString);
    }

    public void setBootSequence(BootSequence value)
    {
        ArrayList<BootSequence> items = new ArrayList<BootSequence>();
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

        ArrayList<EntityModel> secondDeviceOptions =
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

    public void SetDataCenter(UnitVmModel model, ArrayList<storage_pool> list)
    {
        if (model.getBehavior().getSystemTreeSelectedItem() != null
                && model.getBehavior().getSystemTreeSelectedItem().getType() != SystemTreeItemType.System)
        {
            switch (model.getBehavior().getSystemTreeSelectedItem().getType())
            {
            case Templates:
            case DataCenter:
                storage_pool selectDataCenter =
                        (storage_pool) model.getBehavior().getSystemTreeSelectedItem().getEntity();
                for (storage_pool dc : list) {
                    if (selectDataCenter.getId().equals(dc.getId())) {
                        selectDataCenter = dc;
                        break;
                    }
                }
                model.getDataCenter()
                        .setItems(new ArrayList<storage_pool>(Arrays.asList(new storage_pool[] { selectDataCenter })));
                model.getDataCenter().setSelectedItem(selectDataCenter);
                model.getDataCenter().setIsChangable(false);
                model.getDataCenter().setInfo("Cannot choose Data Center in tree context"); //$NON-NLS-1$
                break;
            case Cluster:
            case VMs:
                VDSGroup cluster = (VDSGroup) model.getBehavior().getSystemTreeSelectedItem().getEntity();
                for (storage_pool dc : list)
                {
                    if (dc.getId().equals(cluster.getStoragePoolId()))
                    {
                        model.getDataCenter()
                                .setItems(new ArrayList<storage_pool>(Arrays.asList(new storage_pool[] { dc })));
                        model.getDataCenter().setSelectedItem(dc);
                        break;
                    }
                }
                model.getDataCenter().setIsChangable(false);
                model.getDataCenter().setInfo("Cannot choose Data Center in tree context"); //$NON-NLS-1$
                break;
            case Host:
                VDS host = (VDS) model.getBehavior().getSystemTreeSelectedItem().getEntity();
                for (storage_pool dc : list)
                {
                    if (dc.getId().equals(host.getStoragePoolId()))
                    {
                        model.getDataCenter()
                                .setItems(new ArrayList<storage_pool>(Arrays.asList(new storage_pool[] { dc })));
                        model.getDataCenter().setSelectedItem(dc);
                        model.getDataCenter().setIsChangable(false);
                        model.getDataCenter().setInfo("Cannot choose Data Center in tree context"); //$NON-NLS-1$
                        break;
                    }
                }
                break;
            case Storage:
                StorageDomain storage = (StorageDomain) model.getBehavior().getSystemTreeSelectedItem().getEntity();
                for (storage_pool dc : list)
                {
                    if (dc.getId().equals(storage.getStoragePoolId()))
                    {
                        model.getDataCenter()
                                .setItems(new ArrayList<storage_pool>(Arrays.asList(new storage_pool[] { dc })));
                        model.getDataCenter().setSelectedItem(dc);
                        model.getDataCenter().setIsChangable(false);
                        model.getDataCenter().setInfo("Cannot choose Data Center in tree context"); //$NON-NLS-1$
                        break;
                    }
                }
                break;
            default:
                break;
            }
        }
        else
        {
            model.getDataCenter().setItems(list);
            model.getDataCenter().setSelectedItem(Linq.FirstOrDefault(list));
        }
    }

    public void SetClusters(UnitVmModel model, ArrayList<VDSGroup> clusters, NGuid clusterGuid)
    {
        VmModelBehaviorBase behavior = model.getBehavior();
        if (behavior.getSystemTreeSelectedItem() != null
                && behavior.getSystemTreeSelectedItem().getType() != SystemTreeItemType.System)
        {
            switch (model.getBehavior().getSystemTreeSelectedItem().getType())
            {
            case Cluster:
            case VMs:
                VDSGroup cluster = (VDSGroup) behavior.getSystemTreeSelectedItem().getEntity();
                model.getCluster().setItems(new ArrayList<VDSGroup>(Arrays.asList(new VDSGroup[] { cluster })));
                model.getCluster().setSelectedItem(cluster);
                model.getCluster().setIsChangable(false);
                model.getCluster().setInfo("Cannot choose Cluster in tree context"); //$NON-NLS-1$
                break;
            case Host:
                VDS host = (VDS) behavior.getSystemTreeSelectedItem().getEntity();
                for (VDSGroup iterCluster : clusters)
                {
                    if (iterCluster.getId().equals(host.getVdsGroupId()))
                    {
                        model.getCluster()
                                .setItems(new ArrayList<VDSGroup>(Arrays.asList(new VDSGroup[] { iterCluster })));
                        model.getCluster().setSelectedItem(iterCluster);
                        model.getCluster().setIsChangable(false);
                        model.getCluster().setInfo("Cannot choose Cluster in tree context"); //$NON-NLS-1$
                        break;
                    }
                }
                break;
            default:
                model.getCluster().setItems(clusters);
                if (clusterGuid == null)
                {
                    model.getCluster().setSelectedItem(Linq.FirstOrDefault(clusters));
                }
                else
                {
                    model.getCluster().setSelectedItem(Linq.FirstOrDefault(clusters,
                            new Linq.ClusterPredicate((Guid) clusterGuid)));
                }
                break;
            }
        }
        else
        {

            model.getCluster().setItems(clusters);
            if (clusterGuid == null)
            {
                model.getCluster().setSelectedItem(Linq.FirstOrDefault(clusters));
            }
            else
            {
                model.getCluster().setSelectedItem(Linq.FirstOrDefault(clusters,
                        new Linq.ClusterPredicate((Guid) clusterGuid)));
            }
        }
    }

    public boolean Validate()
    {
        getDataCenter().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getCluster().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getMemSize().ValidateEntity(new IValidation[] { new ByteSizeValidation() });
        getMinAllocatedMemory().ValidateEntity(new IValidation[] { new ByteSizeValidation() });
        getOSType().ValidateSelectedItem(new NotEmptyValidation[] { new NotEmptyValidation() });

        storage_pool dataCenter = (storage_pool) getDataCenter().getSelectedItem();
        if (dataCenter != null && dataCenter.getQuotaEnforcementType() == QuotaEnforcementTypeEnum.HARD_ENFORCEMENT) {
            getQuota().ValidateSelectedItem(new IValidation[] { new NotEmptyQuotaValidation() });
        }

        getTotalCPUCores().ValidateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new IntegerValidation(1, behavior.maxCpus),
                new TotalCpuCoresComposableValidation() });

        if (getOSType().getIsValid())
        {
            VmOsType osType = (VmOsType) getOSType().getSelectedItem();
            getName().ValidateEntity(
                    new IValidation[] {
                            new NotEmptyValidation(),
                            new LengthValidation(
                                (getBehavior() instanceof TemplateVmModelBehavior || getBehavior() instanceof NewTemplateVmModelBehavior)
                                    ? VM_TEMPLATE_NAME_MAX_LIMIT : AsyncDataProvider.IsWindowsOsType(osType) ? WINDOWS_VM_NAME_MAX_LIMIT : NON_WINDOWS_VM_NAME_MAX_LIMIT),
                            isPoolTabValid ? new PoolNameValidation() : new I18NNameValidation()
                    });

            getDescription().ValidateEntity(
                    new IValidation[] {
                            new LengthValidation(DESCRIPTION_MAX_LIMIT),
                            new SpecialAsciiI18NOrNoneValidation()
                    });

            boolean is64OsType =
                    (osType == VmOsType.Other || osType == VmOsType.OtherLinux || AsyncDataProvider.Is64bitOsType(osType));
            int maxMemSize = is64OsType ? get_MaxMemSize64() : get_MaxMemSize32();

            ValidateMemorySize(getMemSize(), maxMemSize, _minMemSize);
            if (!(this.getBehavior() instanceof TemplateVmModelBehavior)) {
                // Minimum 'Physical Memory Guaranteed' is 1MB
                ValidateMemorySize(getMinAllocatedMemory(), (Integer) getMemSize().getEntity(), 1);
            }
        }

        if (getIsAutoAssign().getEntity() != null && ((Boolean) getIsAutoAssign().getEntity()) == false) {
            getDefaultHost().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        } else {
            getDefaultHost().setIsValid(true);
        }

        getTemplate().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getDisksAllocationModel().ValidateEntity(new IValidation[] {});

        getCdImage().setIsValid(true);
        if (getCdImage().getIsChangable()) {
            getCdImage().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }

        getKernel_path().setIsValid(true);
        getKernel_parameters().setIsValid(true);
        getInitrd_path().setIsValid(true);
        if (getKernel_path().getEntity() == null) {
            getKernel_path().setEntity(""); //$NON-NLS-1$
        }
        if (getKernel_parameters().getEntity() == null) {
            getKernel_parameters().setEntity(""); //$NON-NLS-1$
        }
        if (getInitrd_path().getEntity() == null) {
            getInitrd_path().setEntity(""); //$NON-NLS-1$
        }

        if (isLinux_Unassign_UnknownOS) {
            getKernel_path().ValidateEntity(new IValidation[] { new NoTrimmingWhitespacesValidation() });
            getInitrd_path().ValidateEntity(new IValidation[] { new NoTrimmingWhitespacesValidation() });
            getKernel_parameters().ValidateEntity(new IValidation[] { new NoTrimmingWhitespacesValidation() });

            String kernelPath = (String) getKernel_path().getEntity();
            String initrdPath = (String) getInitrd_path().getEntity();
            String kernelParams = (String) getKernel_parameters().getEntity();

            if ((kernelParams.length() > 0 || initrdPath.length() > 0) && kernelPath.length() == 0) {
                boolean kernelParamInvalid = false;
                boolean inetdPathInvalid = false;

                if (kernelParams.length() > 0) {
                    getKernel_parameters().setIsValid(false);
                    kernelParamInvalid = true;
                }
                if (initrdPath.length() > 0) {
                    getInitrd_path().setIsValid(false);
                    inetdPathInvalid = true;
                }

                String msg =
                        ConstantsManager.getInstance()
                                .getMessages()
                                .invalidPath(kernelParamInvalid ? ConstantsManager.getInstance()
                                        .getConstants()
                                        .kernelInvalid() : "", //$NON-NLS-1$
                                        kernelParamInvalid && inetdPathInvalid ? ConstantsManager.getInstance()
                                                .getConstants()
                                                .or() : "", //$NON-NLS-1$
                                        inetdPathInvalid ? ConstantsManager.getInstance()
                                                .getConstants()
                                                .inetdInvalid() : ""); //$NON-NLS-1$

                getKernel_path().setIsValid(false);
                getInitrd_path().getInvalidityReasons().add(msg);
                getKernel_parameters().getInvalidityReasons().add(msg);
                getKernel_path().getInvalidityReasons().add(msg);
            }
        }

        boolean customPropertySheetValid = getCustomPropertySheet().validate();

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
        setIsAllocationTabValid(getDisksAllocationModel().getIsValid() && getMinAllocatedMemory().getIsValid());
        setIsBootSequenceTabValid(getCdImage().getIsValid() && getKernel_path().getIsValid());
        setIsCustomPropertiesTabValid(customPropertySheetValid);

        return getName().getIsValid() && getDescription().getIsValid() && getDataCenter().getIsValid()
                && getDisksAllocationModel().getIsValid() && getTemplate().getIsValid() && getCluster().getIsValid()
                && getDefaultHost().getIsValid() && getMemSize().getIsValid() && getMinAllocatedMemory().getIsValid()
                && getNumOfMonitors().getIsValid() && getDomain().getIsValid() && getUsbPolicy().getIsValid()
                && getTimeZone().getIsValid() && getOSType().getIsValid() && getCdImage().getIsValid()
                && getKernel_path().getIsValid()
                && getKernel_parameters().getIsValid() && getInitrd_path().getIsValid()
                && behavior.Validate()
                && customPropertySheetValid && getQuota().getIsValid();

    }

    class TotalCpuCoresComposableValidation implements IValidation {

        @Override
        public ValidationResult validate(Object value) {
            boolean isOk = behavior.isNumOfSocketsCorrect(Integer.parseInt(getTotalCPUCores().getEntity().toString()));
            ValidationResult res = new ValidationResult();
            res.setSuccess(isOk);
            res.setReasons(Arrays.asList(ConstantsManager.getInstance()
                    .getMessages()
                    .incorrectVCPUNumber()));
            return res;

        }

    }

    private void ValidateMemorySize(EntityModel model, int maxMemSize, int minMemSize)
    {
        boolean isValid = false;

        int memSize = (Integer) model.getEntity();

        if (memSize == 0)
        {
            model.getInvalidityReasons().add(ConstantsManager.getInstance()
                    .getMessages()
                    .memSizeBetween(minMemSize, maxMemSize));
        }
        else if (memSize > maxMemSize)
        {
            model.getInvalidityReasons().add(ConstantsManager.getInstance()
                    .getMessages()
                    .maxMemSizeIs(maxMemSize));
        }
        else if (memSize < minMemSize)
        {
            model.getInvalidityReasons().add(ConstantsManager.getInstance()
                    .getMessages()
                    .minMemSizeIs(minMemSize));
        }
        else
        {
            isValid = true;
        }

        model.setIsValid(isValid);
    }

    private NotChangableForVmInPoolListModel poolType;

    public ListModel getPoolType()
    {
        return poolType;
    }

    protected void setPoolType(NotChangableForVmInPoolListModel value)
    {
        poolType = value;
    }

    private NotChangableForVmInPoolEntityModel numOfDesktops;

    public EntityModel getNumOfDesktops()
    {
        return numOfDesktops;
    }

    protected void setNumOfDesktops(NotChangableForVmInPoolEntityModel value)
    {
        numOfDesktops = value;
    }

    private NotChangableForVmInPoolEntityModel assignedVms;

    public EntityModel getAssignedVms()
    {
        return assignedVms;
    }

    public void setAssignedVms(NotChangableForVmInPoolEntityModel value)
    {
        assignedVms = value;
    }

    private boolean isPoolTabValid;

    public boolean getIsPoolTabValid()
    {
        return isPoolTabValid;
    }

    public void setIsPoolTabValid(boolean value)
    {
        if (isPoolTabValid != value)
        {
            isPoolTabValid = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsPoolTabValid")); //$NON-NLS-1$
        }
    }

    private NotChangableForVmInPoolEntityModel prestartedVms;

    public EntityModel getPrestartedVms() {
        return prestartedVms;
    }

    protected void setPrestartedVms(NotChangableForVmInPoolEntityModel value) {
        prestartedVms = value;
    }

    private String prestartedVmsHint;

    public String getPrestartedVmsHint() {
        return prestartedVmsHint;
    }

    public void setPrestartedVmsHint(String value) {
        if (prestartedVmsHint != value) {
            prestartedVmsHint = value;
            OnPropertyChanged(new PropertyChangedEventArgs("PrestartedVmsHint")); //$NON-NLS-1$
        }
    }

    private class NotChangableForVmInPoolListModel extends ListModel {
        @Override
        public void setIsChangable(boolean value) {
            if (!isVmAttachedToPool())
                super.setIsChangable(value);
        }
    }

    private class NotChangableForVmInPoolEntityModel extends EntityModel {
        @Override
        public void setIsChangable(boolean value) {
            if (!isVmAttachedToPool())
                super.setIsChangable(value);
        }
    }

    private class NotChangableForVmInPoolKeyValueModel extends KeyValueModel {
        @Override
        public void setIsChangable(boolean value) {
            if (!isVmAttachedToPool())
                super.setIsChangable(value);
        }
    }
}
