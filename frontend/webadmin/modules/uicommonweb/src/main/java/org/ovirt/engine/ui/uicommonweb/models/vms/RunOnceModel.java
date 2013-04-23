package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NoTrimmingWhitespacesValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Constants;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public abstract class RunOnceModel extends Model
{

    public static final String RUN_ONCE_COMMAND = "OnRunOnce"; //$NON-NLS-1$

    /** The VM that is about to run */
    protected final VM vm;
    /** Custom properties for the running */
    protected final ArrayList<String> customPropertiesKeysList;
    /** Listener for events that are triggered by this model */
    protected ICommandTarget commandTarget;

    protected final UICommand runOnceCommand;
    protected final UICommand cancelCommand;

    private EntityModel privateAttachFloppy;

    public EntityModel getAttachFloppy()
    {
        return privateAttachFloppy;
    }

    private void setAttachFloppy(EntityModel value)
    {
        privateAttachFloppy = value;
    }

    private ListModel privateFloppyImage;

    public ListModel getFloppyImage()
    {
        return privateFloppyImage;
    }

    private void setFloppyImage(ListModel value)
    {
        privateFloppyImage = value;
    }

    private EntityModel privateAttachIso;

    public EntityModel getAttachIso()
    {
        return privateAttachIso;
    }

    private void setAttachIso(EntityModel value)
    {
        privateAttachIso = value;
    }

    private ListModel privateIsoImage;

    public ListModel getIsoImage()
    {
        return privateIsoImage;
    }

    private void setIsoImage(ListModel value)
    {
        privateIsoImage = value;
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

    private ListModel privateSysPrepDomainName;

    public ListModel getSysPrepDomainName()
    {
        return privateSysPrepDomainName;
    }

    private void setSysPrepDomainName(ListModel value)
    {
        privateSysPrepDomainName = value;
    }

    private EntityModel privateSysPrepSelectedDomainName;

    public EntityModel getSysPrepSelectedDomainName()
    {
        return privateSysPrepSelectedDomainName;
    }

    private void setSysPrepSelectedDomainName(EntityModel value)
    {
        privateSysPrepSelectedDomainName = value;
    }

    private EntityModel privateSysPrepUserName;

    public EntityModel getSysPrepUserName()
    {
        return privateSysPrepUserName;
    }

    private void setSysPrepUserName(EntityModel value)
    {
        privateSysPrepUserName = value;
    }

    private EntityModel privateSysPrepPassword;

    public EntityModel getSysPrepPassword()
    {
        return privateSysPrepPassword;
    }

    private void setSysPrepPassword(EntityModel value)
    {
        privateSysPrepPassword = value;
    }

    private EntityModel privateUseAlternateCredentials;

    public EntityModel getUseAlternateCredentials()
    {
        return privateUseAlternateCredentials;
    }

    private void setUseAlternateCredentials(EntityModel value)
    {
        privateUseAlternateCredentials = value;
    }

    private EntityModel privateIsSysprepEnabled;

    public EntityModel getIsSysprepEnabled()
    {
        return privateIsSysprepEnabled;
    }

    private void setIsSysprepEnabled(EntityModel value)
    {
        privateIsSysprepEnabled = value;
    }

    private EntityModel privateIsVmFirstRun;

    public EntityModel getIsVmFirstRun()
    {
        return privateIsVmFirstRun;
    }

    private void setIsVmFirstRun(EntityModel value)
    {
        privateIsVmFirstRun = value;
    }

    private EntityModel privateIsLinuxOptionsAvailable;

    public EntityModel getIsLinuxOptionsAvailable()
    {
        return privateIsLinuxOptionsAvailable;
    }

    private void setIsLinuxOptionsAvailable(EntityModel value)
    {
        privateIsLinuxOptionsAvailable = value;
    }

    private KeyValueModel customPropertySheet;

    public KeyValueModel getCustomPropertySheet() {
        return customPropertySheet;
    }

    public void setCustomPropertySheet(KeyValueModel customPropertySheet) {
        this.customPropertySheet = customPropertySheet;
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

    private EntityModel privateRunAndPause;

    public EntityModel getRunAndPause()
    {
        return privateRunAndPause;
    }

    public void setRunAndPause(EntityModel value)
    {
        privateRunAndPause = value;
    }

    private EntityModel privateRunAsStateless;

    public EntityModel getRunAsStateless()
    {
        return privateRunAsStateless;
    }

    public void setRunAsStateless(EntityModel value)
    {
        privateRunAsStateless = value;
    }

    private EntityModel privateDisplayConsole_Vnc_IsSelected;

    public EntityModel getDisplayConsole_Vnc_IsSelected()
    {
        return privateDisplayConsole_Vnc_IsSelected;
    }

    public void setDisplayConsole_Vnc_IsSelected(EntityModel value)
    {
        privateDisplayConsole_Vnc_IsSelected = value;
    }

    private EntityModel privateDisplayConsole_Spice_IsSelected;

    public EntityModel getDisplayConsole_Spice_IsSelected()
    {
        return privateDisplayConsole_Spice_IsSelected;
    }

    public void setDisplayConsole_Spice_IsSelected(EntityModel value)
    {
        privateDisplayConsole_Spice_IsSelected = value;
    }

    private boolean privateIsLinuxOS;

    public boolean getIsLinuxOS()
    {
        return privateIsLinuxOS;
    }

    public void setIsLinuxOS(boolean value)
    {
        privateIsLinuxOS = value;
    }

    private boolean privateIsWindowsOS;

    public boolean getIsWindowsOS()
    {
        return privateIsWindowsOS;
    }

    public void setIsWindowsOS(boolean value)
    {
        privateIsWindowsOS = value;
    }

    private boolean hwAcceleration;

    public boolean getHwAcceleration()
    {
        return hwAcceleration;
    }

    public void setHwAcceleration(boolean value)
    {
        if (hwAcceleration != value)
        {
            hwAcceleration = value;
            onPropertyChanged(new PropertyChangedEventArgs("HwAcceleration")); //$NON-NLS-1$
        }
    }

    private BootSequenceModel bootSequence;

    public BootSequenceModel getBootSequence()
    {
        return bootSequence;
    }

    public void setBootSequence(BootSequenceModel value)
    {
        if (bootSequence != value)
        {
            bootSequence = value;
            onPropertyChanged(new PropertyChangedEventArgs("BootSequence")); //$NON-NLS-1$
        }
    }

    private boolean isHostTabVisible = false;

    public boolean getIsHostTabVisible() {
        return isHostTabVisible;
    }

    public void setIsHostTabVisible(boolean value) {
        if (isHostTabVisible != value) {
            isHostTabVisible = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsHostTabVisible")); //$NON-NLS-1$
        }
    }

    private boolean isCustomPropertiesSheetVisible = false;

    public boolean getIsCustomPropertiesSheetVisible() {
        return isCustomPropertiesSheetVisible;
    }

    public void setIsCustomPropertiesSheetVisible(boolean value) {
        if (isCustomPropertiesSheetVisible != value) {
            isCustomPropertiesSheetVisible = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsCustomPropertiesSheetVisible")); //$NON-NLS-1$
        }
    }

    // host tab
    private ListModel defaultHost;

    public ListModel getDefaultHost() {
        return defaultHost;
    }

    private void setDefaultHost(ListModel value) {
        this.defaultHost = value;
    }

    private EntityModel isAutoAssign;

    public EntityModel getIsAutoAssign() {
        return isAutoAssign;
    }

    public void setIsAutoAssign(EntityModel value) {
        this.isAutoAssign = value;
    }

    // The "sysprep" option was moved from a standalone check box to a
    // pseudo floppy disk image. In order not to change the back-end
    // interface, the Reinitialize variable was changed to a read-only
    // property and its value is based on the selected floppy image.
    public boolean getReinitialize()
    {
        return ((Boolean) getAttachFloppy().getEntity() && getFloppyImage().getSelectedItem() != null && getFloppyImage().getSelectedItem()
                .equals("[sysprep]")); //$NON-NLS-1$
    }

    public String getFloppyImagePath()
    {
        if ((Boolean) getAttachFloppy().getEntity())
        {
            return getReinitialize() ? "" : (String) getFloppyImage().getSelectedItem(); //$NON-NLS-1$
        }
        else
        {
            return ""; //$NON-NLS-1$
        }
    }

    private ArrayList<String> privateCustomPropertiesKeysList;

    public ArrayList<String> getCustomPropertiesKeysList()
    {
        return privateCustomPropertiesKeysList;
    }

    public void setCustomPropertiesKeysList(ArrayList<String> value)
    {
        privateCustomPropertiesKeysList = value;
    }

    public RunOnceModel(VM vm, ArrayList<String> customPropertiesKeysList, ICommandTarget commandTarget)
    {
        this.vm = vm;
        this.customPropertiesKeysList = customPropertiesKeysList;
        this.commandTarget = commandTarget;

        setAttachFloppy(new EntityModel());
        getAttachFloppy().getEntityChangedEvent().addListener(this);
        setFloppyImage(new ListModel());
        getFloppyImage().getSelectedItemChangedEvent().addListener(this);
        setAttachIso(new EntityModel());
        getAttachIso().getEntityChangedEvent().addListener(this);
        setIsoImage(new ListModel());
        setDisplayProtocol(new ListModel());
        setBootSequence(new BootSequenceModel());

        setKernel_parameters(new EntityModel());
        setKernel_path(new EntityModel());
        setInitrd_path(new EntityModel());

        setSysPrepDomainName(new ListModel());
        getSysPrepDomainName().getSelectedItemChangedEvent().addListener(this);
        setSysPrepSelectedDomainName(new EntityModel());

        setSysPrepUserName(new EntityModel().setIsChangable(false));
        setSysPrepPassword(new EntityModel().setIsChangable(false));

        setIsSysprepEnabled(new EntityModel());
        setIsVmFirstRun(new EntityModel(false));
        getIsVmFirstRun().getEntityChangedEvent().addListener(this);
        setUseAlternateCredentials(new EntityModel(false));
        getUseAlternateCredentials().getEntityChangedEvent().addListener(this);

        setCustomProperties(new EntityModel());
        setCustomPropertySheet(new KeyValueModel());

        setRunAndPause(new EntityModel(false));
        setRunAsStateless(new EntityModel(false));

        setDisplayConsole_Spice_IsSelected(new EntityModel());
        getDisplayConsole_Spice_IsSelected().getEntityChangedEvent().addListener(this);
        setDisplayConsole_Vnc_IsSelected(new EntityModel());
        getDisplayConsole_Vnc_IsSelected().getEntityChangedEvent().addListener(this);

        setIsLinuxOptionsAvailable(new EntityModel());

        // host tab
        setDefaultHost(new ListModel());
        getDefaultHost().getSelectedItemChangedEvent().addListener(this);

        setIsAutoAssign(new EntityModel());
        getIsAutoAssign().getEntityChangedEvent().addListener(this);

        setIsHostTabVisible(true);

        setIsCustomPropertiesSheetVisible(true);

        runOnceCommand = new UICommand(RunOnceModel.RUN_ONCE_COMMAND, this)
         .setTitle(ConstantsManager.getInstance().getConstants().ok())
         .setIsDefault(true);

        cancelCommand = new UICommand(Model.CANCEL_COMMAND, this)
         .setTitle(ConstantsManager.getInstance().getConstants().cancel())
         .setIsCancel(true);

        getCommands().addAll(Arrays.asList(runOnceCommand, cancelCommand));
    }

    public void init() {
        setTitle(ConstantsManager.getInstance().getConstants().runVirtualMachinesTitle());
        setHashName("run_virtual_machine"); //$NON-NLS-1$
        getAttachIso().setEntity(false);
        getAttachFloppy().setEntity(false);
        getRunAsStateless().setEntity(vm.isStateless());
        getRunAndPause().setEntity(vm.isRunAndPause());
        setHwAcceleration(true);

        // passing Kernel parameters
        getKernel_parameters().setEntity(vm.getKernelParams());
        getKernel_path().setEntity(vm.getKernelUrl());
        getInitrd_path().setEntity(vm.getInitrdUrl());

        setIsLinuxOS(AsyncDataProvider.IsLinuxOsType(vm.getVmOs()));
        getIsLinuxOptionsAvailable().setEntity(getIsLinuxOS());
        setIsWindowsOS(AsyncDataProvider.IsWindowsOsType(vm.getVmOs()));
        getIsVmFirstRun().setEntity(!vm.isInitialized());
        getSysPrepDomainName().setSelectedItem(vm.getVmDomain());

        setCustomPropertiesKeysList(customPropertiesKeysList);

        updateDomainList();
        updateIsoList();
        updateDisplayProtocols();
        updateFloppyImages();

        // Boot sequence.
        setIsBootFromNetworkAllowedForVm();
        setIsBootFromHardDiskAllowedForVm();

        // Display protocols.
        EntityModel vncProtocol = new EntityModel(DisplayType.vnc)
           .setTitle(ConstantsManager.getInstance().getConstants().VNCTitle());

        EntityModel qxlProtocol = new EntityModel(DisplayType.qxl)
           .setTitle(ConstantsManager.getInstance().getConstants().spiceTitle());

        getDisplayProtocol().setItems(Arrays.asList(vncProtocol, qxlProtocol));
        getDisplayProtocol().setSelectedItem(vm.getDefaultDisplayType() == DisplayType.vnc ?
                vncProtocol : qxlProtocol);
    }

    protected RunVmOnceParams createRunVmOnceParams() {
        RunVmOnceParams params = new RunVmOnceParams();
        params.setVmId(vm.getId());
        params.setBootSequence(getBootSequence().getSequence());
        params.setDiskPath((Boolean) getAttachIso().getEntity() ? (String) getIsoImage().getSelectedItem()
                : ""); //$NON-NLS-1$
        params.setFloppyPath(getFloppyImagePath());
        params.setKvmEnable(getHwAcceleration());
        params.setRunAndPause((Boolean) getRunAndPause().getEntity());
        params.setAcpiEnable(true);
        params.setRunAsStateless((Boolean) getRunAsStateless().getEntity());
        params.setReinitialize(getReinitialize());
        params.setCustomProperties((String) getCustomProperties().getEntity());

        // kernel params
        if (getKernel_path().getEntity() != null)
        {
            params.setkernel_url((String) getKernel_path().getEntity());
        }
        if (getKernel_parameters().getEntity() != null)
        {
            params.setkernel_params((String) getKernel_parameters().getEntity());
        }
        if (getInitrd_path().getEntity() != null)
        {
            params.setinitrd_url((String) getInitrd_path().getEntity());
        }

        // Sysprep params
        if (getSysPrepUserName().getEntity() != null)
        {
            params.setSysPrepUserName((String) getSysPrepUserName().getEntity());
        }
        if (getSysPrepPassword().getEntity() != null)
        {
            params.setSysPrepPassword((String) getSysPrepPassword().getEntity());
        }

        EntityModel displayProtocolSelectedItem = (EntityModel) getDisplayProtocol().getSelectedItem();
        params.setUseVnc((DisplayType) displayProtocolSelectedItem.getEntity() == DisplayType.vnc);
        if ((Boolean) getDisplayConsole_Vnc_IsSelected().getEntity()
                || (Boolean) getDisplayConsole_Spice_IsSelected().getEntity())
        {
            params.setUseVnc((Boolean) getDisplayConsole_Vnc_IsSelected().getEntity());
        }

        return params;
    }

    protected void updateFloppyImages() {
        AsyncDataProvider.GetFloppyImageList(new AsyncQuery(this,
                new INewAsyncCallback() {
                 @Override
                 public void onSuccess(Object model, Object returnValue) {
                     VM selectedVM = (VM) vm;
                     List<String> images = (List<String>) returnValue;

                     if (AsyncDataProvider.IsWindowsOsType(selectedVM.getVmOs()))
                     {
                         // Add a pseudo floppy disk image used for Windows' sysprep.
                         if (!selectedVM.isInitialized())
                         {
                             images.add(0, "[sysprep]"); //$NON-NLS-1$
                             getAttachFloppy().setEntity(true);
                         }
                         else
                         {
                             images.add("[sysprep]"); //$NON-NLS-1$
                         }
                     }
                     getFloppyImage().setItems(images);

                     if (getFloppyImage().getIsChangable()
                             && getFloppyImage().getSelectedItem() == null)
                     {
                         getFloppyImage().setSelectedItem(Linq.FirstOrDefault(images));
                     }
                 }
             }),
             vm.getStoragePoolId());
    }

    private void setIsBootFromHardDiskAllowedForVm() {
        Frontend.RunQuery(VdcQueryType.GetAllDisksByVmId, new GetAllDisksByVmIdParameters(vm.getId()),
                new AsyncQuery(this, new INewAsyncCallback() {

                 @Override
                 public void onSuccess(Object model, Object returnValue) {
                     ArrayList<Disk> vmDisks = (ArrayList<Disk>) ((VdcQueryReturnValue) returnValue).getReturnValue();

                     if (vmDisks.isEmpty()) {
                         getRunAsStateless().setIsChangable(false);
                         getRunAsStateless()
                                .setChangeProhibitionReason(ConstantsManager.getInstance()
                                        .getMessages()
                                        .disklessVmCannotRunAsStateless());
                         getRunAsStateless().setEntity(false);
                     }

                     if (!isDisksContainBootableDisk(vmDisks))
                     {
                         BootSequenceModel bootSequenceModel = getBootSequence();
                         bootSequenceModel.getHardDiskOption().setIsChangable(false);
                         bootSequenceModel.getHardDiskOption()
                                 .setChangeProhibitionReason(ConstantsManager.getInstance()
                                         .getMessages()
                                         .bootableDiskIsRequiredToBootFromDisk());
                     }
                 }
             }));
    }

    private boolean isDisksContainBootableDisk(List<Disk> disks) {
        for (Disk disk : disks) {
            if (disk.isBoot()) {
                return true;
            }
        }
        return false;
    }

    private void setIsBootFromNetworkAllowedForVm() {
        Frontend.RunQuery(VdcQueryType.GetVmInterfacesByVmId, new IdQueryParameters(vm.getId()),
                new AsyncQuery(this, new INewAsyncCallback() {

                 @Override
                 public void onSuccess(Object model, Object returnValue) {
                     boolean hasNics =
                             ((ArrayList<VmNetworkInterface>) ((VdcQueryReturnValue) returnValue).getReturnValue()).size() > 0;

                     if (!hasNics)
                     {
                         BootSequenceModel bootSequenceModel = getBootSequence();
                         bootSequenceModel.getNetworkOption().setIsChangable(false);
                         bootSequenceModel.getNetworkOption()
                                 .setChangeProhibitionReason(ConstantsManager.getInstance()
                                         .getMessages()
                                         .interfaceIsRequiredToBootFromNetwork());
                     }
                 }
             }));
    }

    private void updateDisplayProtocols() {
        boolean isVncSelected = vm.getDefaultDisplayType() == DisplayType.vnc;
        getDisplayConsole_Vnc_IsSelected().setEntity(isVncSelected);
        getDisplayConsole_Spice_IsSelected().setEntity(!isVncSelected);
    }

    private void updateIsoList() {
        AsyncDataProvider.GetIrsImageList(new AsyncQuery(this,
                new INewAsyncCallback() {
                 @Override
                 public void onSuccess(Object model, Object returnValue) {
                     List<String> images = (List<String>) returnValue;
                     getIsoImage().setItems(images);

                     if (getIsoImage().getIsChangable()
                             && getIsoImage().getSelectedItem() == null)
                     {
                         getIsoImage().setSelectedItem(Linq.FirstOrDefault(images));
                     }

                 }
             }),
             vm.getStoragePoolId());
    }

    private void updateDomainList() {
        // Update Domain list
        AsyncDataProvider.GetDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                List<String> domains = (List<String>) returnValue;
                String oldDomain = (String) getSysPrepDomainName().getSelectedItem();
                if (oldDomain != null && !oldDomain.equals("") && !domains.contains(oldDomain)) { //$NON-NLS-1$
                    domains.add(0, oldDomain);
                }
                getSysPrepDomainName().setItems(domains);
                String selectedDomain = (oldDomain != null) ? oldDomain : Linq.FirstOrDefault(domains);
                if (!StringHelper.stringsEqual(selectedDomain, "")) { //$NON-NLS-1$
                    getSysPrepDomainName().setSelectedItem(selectedDomain);
                }
            }
        }), true);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ListModel.SelectedItemChangedEventDefinition))
        {
            if (sender == getFloppyImage())
            {
                FloppyImage_SelectedItemChanged();
            }
            else if (sender == getSysPrepDomainName())
            {
                SysPrepDomainName_SelectedItemChanged();
            }
        }
        else if (ev.matchesDefinition(EntityModel.EntityChangedEventDefinition))
        {
            if (sender == getAttachFloppy())
            {
                AttachFloppy_EntityChanged();
            }
            else if (sender == getAttachIso())
            {
                AttachIso_EntityChanged();
            }
            else if (sender == getIsVmFirstRun())
            {
                IsVmFirstRun_EntityChanged();
            }
            else if (sender == getUseAlternateCredentials())
            {
                UseAlternateCredentials_EntityChanged();
            }
            else if (sender == getDisplayConsole_Vnc_IsSelected() && (Boolean) ((EntityModel) sender).getEntity())
            {
                getDisplayConsole_Spice_IsSelected().setEntity(false);
            }
            else if (sender == getDisplayConsole_Spice_IsSelected() && (Boolean) ((EntityModel) sender).getEntity())
            {
                getDisplayConsole_Vnc_IsSelected().setEntity(false);
            }
            else if (sender == getIsAutoAssign())
            {
                IsAutoAssign_EntityChanged(sender, args);
            }
        }
    }

    private void AttachIso_EntityChanged()
    {
        getIsoImage().setIsChangable((Boolean) getAttachIso().getEntity());
        getBootSequence().getCdromOption().setIsChangable((Boolean) getAttachIso().getEntity());
    }

    private void AttachFloppy_EntityChanged()
    {
        getFloppyImage().setIsChangable((Boolean) getAttachFloppy().getEntity());
        UpdateIsSysprepEnabled();
    }

    private void UseAlternateCredentials_EntityChanged()
    {
        boolean useAlternateCredentials = (Boolean) getUseAlternateCredentials().getEntity();

        getSysPrepUserName().setIsChangable((Boolean) getUseAlternateCredentials().getEntity());
        getSysPrepPassword().setIsChangable((Boolean) getUseAlternateCredentials().getEntity());

        getSysPrepUserName().setEntity(useAlternateCredentials ? "" : null); //$NON-NLS-1$
        getSysPrepPassword().setEntity(useAlternateCredentials ? "" : null); //$NON-NLS-1$
    }

    private void IsVmFirstRun_EntityChanged()
    {
        UpdateIsSysprepEnabled();
    }

    private void FloppyImage_SelectedItemChanged()
    {
        UpdateIsSysprepEnabled();
    }

    private void SysPrepDomainName_SelectedItemChanged()
    {
        getSysPrepSelectedDomainName().setEntity(getSysPrepDomainName().getSelectedItem());
    }

    private void IsAutoAssign_EntityChanged(Object sender, EventArgs args) {
        if ((Boolean) getIsAutoAssign().getEntity() == false) {
            getDefaultHost().setIsChangable(true);
        }
    }

    // Sysprep section is displayed only when VM's OS-type is 'Windows'
    // and [Reinitialize-sysprep == true || IsVmFirstRun == true (IsVmFirstRun == !VM.is_initialized) and no attached
    // floppy]
    private void UpdateIsSysprepEnabled()
    {
        boolean isFloppyAttached = (Boolean) getAttachFloppy().getEntity();
        boolean isVmFirstRun = (Boolean) getIsVmFirstRun().getEntity();

        getIsSysprepEnabled().setEntity(getIsWindowsOS() && getReinitialize());
    }

    public boolean Validate() {
        getIsoImage().setIsValid(true);
        if ((Boolean) getAttachIso().getEntity()) {
            getIsoImage().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }

        getFloppyImage().setIsValid(true);
        if ((Boolean) getAttachFloppy().getEntity()) {
            getFloppyImage().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }

        boolean customPropertyValidation = getCustomPropertySheet().validate();

        if (getIsLinuxOS()) {
            getKernel_path().validateEntity(new IValidation[] { new NoTrimmingWhitespacesValidation() });
            getInitrd_path().validateEntity(new IValidation[] { new NoTrimmingWhitespacesValidation() });
            getKernel_parameters().validateEntity(new IValidation[] { new NoTrimmingWhitespacesValidation() });

            // initrd path and kernel params require kernel path to be filled
            if (StringHelper.isNullOrEmpty((String) getKernel_path().getEntity())) {
                final Constants constants = ConstantsManager.getInstance().getConstants();

                if (!StringHelper.isNullOrEmpty((String) getInitrd_path().getEntity())) {
                    getInitrd_path().getInvalidityReasons().add(constants.initrdPathInvalid());
                    getInitrd_path().setIsValid(false);
                    getKernel_path().getInvalidityReasons().add(constants.initrdPathInvalid());
                    getKernel_path().setIsValid(false);
                }

                if (!StringHelper.isNullOrEmpty((String) getKernel_parameters().getEntity())) {
                    getKernel_parameters().getInvalidityReasons().add(constants.kernelParamsInvalid());
                    getKernel_parameters().setIsValid(false);
                    getKernel_path().getInvalidityReasons().add(constants.kernelParamsInvalid());
                    getKernel_path().setIsValid(false);
                }
            }
        }

        if (getIsAutoAssign().getEntity() != null && (Boolean) getIsAutoAssign().getEntity() == false) {
            getDefaultHost().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }
        else {
            getDefaultHost().setIsValid(true);
        }

        return getIsoImage().getIsValid()
                && getFloppyImage().getIsValid()
                && getKernel_path().getIsValid()
                && getInitrd_path().getIsValid()
                && getKernel_parameters().getIsValid()
                && getDefaultHost().getIsValid()
                && customPropertyValidation;
    }

    @Override
    public void executeCommand(UICommand command)
    {
        if (command == runOnceCommand)
        {
            if (Validate()) {
                onRunOnce();
            }
        }
        else if (command == cancelCommand)
        {
            commandTarget.executeCommand(command);
        }
    }
    protected abstract void onRunOnce();
}
