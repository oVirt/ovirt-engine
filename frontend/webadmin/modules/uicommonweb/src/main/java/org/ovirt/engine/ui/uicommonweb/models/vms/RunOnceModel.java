package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.InitializationType;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.dataprovider.ImagesDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.validation.CpuNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.I18NExtraNameOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.MatchFieldsValidator;
import org.ovirt.engine.ui.uicommonweb.validation.NoTrimmingWhitespacesValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

public abstract class RunOnceModel extends Model {

    // Boot Options tab

    public static final String RUN_ONCE_COMMAND = "OnRunOnce"; //$NON-NLS-1$

    /** The VM that is about to run */
    protected final VM vm;

    /** Listener for events that are triggered by this model */
    protected ICommandTarget commandTarget;

    protected final UICommand runOnceCommand;
    protected final UICommand cancelCommand;

    private EntityModel<Boolean> privateAttachFloppy;

    public EntityModel<Boolean> getAttachFloppy() {
        return privateAttachFloppy;
    }

    private void setAttachFloppy(EntityModel<Boolean> value) {
        privateAttachFloppy = value;
    }

    private ListModel<String> privateFloppyImage;

    public ListModel<String> getFloppyImage() {
        return privateFloppyImage;
    }

    private void setFloppyImage(ListModel<String> value) {
        privateFloppyImage = value;
    }

    private EntityModel<Boolean> privateAttachIso;

    public EntityModel<Boolean> getAttachIso() {
        return privateAttachIso;
    }

    private void setAttachIso(EntityModel<Boolean> value) {
        privateAttachIso = value;
    }

    private EntityModel<Boolean> privateAttachWgt;

    public EntityModel<Boolean> getAttachWgt() {
        return privateAttachWgt;
    }

    private void setAttachWgt(EntityModel<Boolean> value) {
        privateAttachWgt = value;
    }

    private SortedListModel<RepoImage> privateIsoImage;

    public ListModel<RepoImage> getIsoImage() {
        return privateIsoImage;
    }

    private void setIsoImage(SortedListModel<RepoImage> value) {
        privateIsoImage = value;
    }

    private EntityModel<Boolean> attachSysprep;

    public EntityModel<Boolean> getAttachSysprep() {
        return attachSysprep;
    }

    private void setAttachSysprep(EntityModel<Boolean> value) {
        attachSysprep = value;
    }

    private ListModel<String> kernelImage;

    public ListModel<String> getKernelImage() {
        return kernelImage;
    }

    public void setKernelImage(ListModel<String> kernelImage) {
        this.kernelImage = kernelImage;
    }

    private ListModel<String> initrdImage;

    public ListModel<String> getInitrdImage() {
        return initrdImage;
    }

    public void setInitrdImage(ListModel<String> initrdImage) {
        this.initrdImage = initrdImage;
    }

    private ListModel<EntityModel<DisplayType>> privateDisplayProtocol;

    public ListModel<EntityModel<DisplayType>> getDisplayProtocol() {
        return privateDisplayProtocol;
    }

    private void setDisplayProtocol(ListModel<EntityModel<DisplayType>> value) {
        privateDisplayProtocol = value;
    }

    // Linux Boot Options tab

    private EntityModel<String> privateKernelParameters;

    public EntityModel<String> getKernelParameters() {
        return privateKernelParameters;
    }

    private void setKernel_parameters(EntityModel<String> value) {
        privateKernelParameters = value;
    }

    // Initial Boot tab - Sysprep

    private ListModel<String> privateSysPrepDomainName;

    public ListModel<String> getSysPrepDomainName() {
        return privateSysPrepDomainName;
    }

    private void setSysPrepDomainName(ListModel<String> value) {
        privateSysPrepDomainName = value;
    }

    private EntityModel<String> privateSysPrepSelectedDomainName;

    public EntityModel<String> getSysPrepSelectedDomainName() {
        return privateSysPrepSelectedDomainName;
    }

    private void setSysPrepSelectedDomainName(EntityModel<String> value) {
        privateSysPrepSelectedDomainName = value;
    }

    private EntityModel<String> privateSysPrepUserName;

    public EntityModel<String> getSysPrepUserName() {
        return privateSysPrepUserName;
    }

    private void setSysPrepUserName(EntityModel<String> value) {
        privateSysPrepUserName = value;
    }

    private EntityModel<String> sysPrepPassword;

    public EntityModel<String> getSysPrepPassword() {
        return sysPrepPassword;
    }

    private void setSysPrepPassword(EntityModel<String> value) {
        sysPrepPassword = value;
    }

    private EntityModel<String> sysPrepPasswordVerification;

    public EntityModel<String> getSysPrepPasswordVerification() {
        return sysPrepPasswordVerification;
    }

    private void setSysPrepPasswordVerification(EntityModel<String> value) {
        sysPrepPasswordVerification = value;
    }

    private EntityModel<Boolean> privateUseAlternateCredentials;

    public EntityModel<Boolean> getUseAlternateCredentials() {
        return privateUseAlternateCredentials;
    }

    private void setUseAlternateCredentials(EntityModel<Boolean> value) {
        privateUseAlternateCredentials = value;
    }

    private EntityModel<Boolean> privateIsSysprepEnabled;

    public EntityModel<Boolean> getIsSysprepEnabled() {
        return privateIsSysprepEnabled;
    }

    private void setIsSysprepEnabled(EntityModel<Boolean> value) {
        privateIsSysprepEnabled = value;
    }

    private EntityModel<Boolean> privateIsSysprepPossible;

    public EntityModel<Boolean> getIsSysprepPossible() {
        return privateIsSysprepPossible;
    }

    private void setIsSysprepPossible(EntityModel<Boolean> value) {
        privateIsSysprepPossible = value;
    }

    // Initialization

    private EntityModel<Boolean> privateIsVmFirstRun;

    public EntityModel<Boolean> getIsVmFirstRun() {
        return privateIsVmFirstRun;
    }

    private void setIsVmFirstRun(EntityModel<Boolean> value) {
        privateIsVmFirstRun = value;
    }

    private EntityModel<Boolean> privateIsLinuxOptionsAvailable;

    public EntityModel<Boolean> getIsLinuxOptionsAvailable() {
        return privateIsLinuxOptionsAvailable;
    }

    private void setIsLinuxOptionsAvailable(EntityModel<Boolean> value) {
        privateIsLinuxOptionsAvailable = value;
    }

    // Initial Boot tab - Cloud-Init

    private EntityModel<Boolean> privateIsCloudInitEnabled;

    public EntityModel<Boolean> getIsCloudInitEnabled() {
        return privateIsCloudInitEnabled;
    }

    private void setIsCloudInitEnabled(EntityModel<Boolean> value) {
        privateIsCloudInitEnabled = value;
    }

    public VmInitModel vmInitModel;

    public VmInitModel getVmInitModel() {
        return vmInitModel;
    }

    public void setVmInitModel(VmInitModel value) {
        vmInitModel = value;
    }

    private EntityModel<Boolean> privateIsCloudInitPossible;

    public EntityModel<Boolean> getIsCloudInitPossible() {
        return privateIsCloudInitPossible;
    }

    private void setIsCloudInitPossible(EntityModel<Boolean> value) {
        privateIsCloudInitPossible = value;
    }

    // Custom Properties tab

    private KeyValueModel customPropertySheet;

    public KeyValueModel getCustomPropertySheet() {
        return customPropertySheet;
    }

    public void setCustomPropertySheet(KeyValueModel customPropertySheet) {
        this.customPropertySheet = customPropertySheet;
    }

    private EntityModel<Boolean> bootMenuEnabled;

    public EntityModel<Boolean> getBootMenuEnabled() {
        return bootMenuEnabled;
    }

    public void setBootMenuEnabled(EntityModel<Boolean> bootMenuEnabled) {
        this.bootMenuEnabled = bootMenuEnabled;
    }

    private EntityModel<Boolean> privateRunAndPause;

    public EntityModel<Boolean> getRunAndPause() {
        return privateRunAndPause;
    }

    public void setRunAndPause(EntityModel<Boolean> value) {
        privateRunAndPause = value;
    }

    private EntityModel<Boolean> privateRunAsStateless;

    public EntityModel<Boolean> getRunAsStateless() {
        return privateRunAsStateless;
    }

    public void setRunAsStateless(EntityModel<Boolean> value) {
        privateRunAsStateless = value;
    }

    private EntityModel<Boolean> privateRunOnceHeadlessModeIsSelected;

    public EntityModel<Boolean> getRunOnceHeadlessModeIsSelected() {
        return privateRunOnceHeadlessModeIsSelected;
    }

    public void setRunOnceHeadlessModeIsSelected(EntityModel<Boolean> value) {
        privateRunOnceHeadlessModeIsSelected = value;
    }

    private EntityModel<Boolean> privateDisplayConsole_Vnc_IsSelected;

    public EntityModel<Boolean> getDisplayConsole_Vnc_IsSelected() {
        return privateDisplayConsole_Vnc_IsSelected;
    }

    public void setDisplayConsole_Vnc_IsSelected(EntityModel<Boolean> value) {
        privateDisplayConsole_Vnc_IsSelected = value;
    }

    private ListModel<String> vncKeyboardLayout;

    public ListModel<String> getVncKeyboardLayout() {
        return vncKeyboardLayout;
    }

    public void setVncKeyboardLayout(ListModel<String> vncKeyboardLayout) {
        this.vncKeyboardLayout = vncKeyboardLayout;
    }

    // Display Protocol tab

    private EntityModel<Boolean> privateDisplayConsole_Spice_IsSelected;

    public EntityModel<Boolean> getDisplayConsole_Spice_IsSelected() {
        return privateDisplayConsole_Spice_IsSelected;
    }

    public void setDisplayConsole_Spice_IsSelected(EntityModel<Boolean> value) {
        privateDisplayConsole_Spice_IsSelected = value;
    }

    private EntityModel<Boolean> spiceFileTransferEnabled;

    public EntityModel<Boolean> getSpiceFileTransferEnabled() {
        return spiceFileTransferEnabled;
    }

    public void setSpiceFileTransferEnabled(EntityModel<Boolean> spiceFileTransferEnabled) {
        this.spiceFileTransferEnabled = spiceFileTransferEnabled;
    }

    private EntityModel<Boolean> spiceCopyPasteEnabled;

    public EntityModel<Boolean> getSpiceCopyPasteEnabled() {
        return spiceCopyPasteEnabled;
    }

    public void setSpiceCopyPasteEnabled(EntityModel<Boolean> spiceCopyPasteEnabled) {
        this.spiceCopyPasteEnabled = spiceCopyPasteEnabled;
    }

    // Misc

    private boolean privateIsLinuxOS;

    public boolean getIsLinuxOS() {
        return privateIsLinuxOS;
    }

    public void setIsLinuxOS(boolean value) {
        privateIsLinuxOS = value;
    }

    private boolean privateIsWindowsOS;

    public boolean getIsWindowsOS() {
        return privateIsWindowsOS;
    }

    public void setIsWindowsOS(boolean value) {
        privateIsWindowsOS = value;
    }

    private boolean privateIsIgnition;

    public boolean getIsIgnition() {
        return privateIsIgnition;
    }

    public void setIsIgnition(boolean value) {
        privateIsIgnition = value;
    }

    public String getIgnitionVersion() {
        return AsyncDataProvider.getInstance().getIgnitionVersion(vm.getVmOsId());
    }

    private BootSequenceModel bootSequence;

    public BootSequenceModel getBootSequence() {
        return bootSequence;
    }

    public void setBootSequence(BootSequenceModel value) {
        if (bootSequence != value) {
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

    // System tab

    private ListModel<String> emulatedMachine;

    public ListModel<String> getEmulatedMachine() {
        return emulatedMachine;
    }

    private void setEmulatedMachine(ListModel<String> emulatedMachine) {
        this.emulatedMachine = emulatedMachine;
    }

    private EntityModel<String> clusterEmulatedMachine;

    public EntityModel<String> getClusterEmulatedMachine() {
        return clusterEmulatedMachine;
    }

    public void setClusterEmulatedMachine(EntityModel<String> clusterEmulatedMachine) {
        this.clusterEmulatedMachine = clusterEmulatedMachine;
    }

    private ListModel<String> customCpu;

    public ListModel<String> getCustomCpu() {
        return customCpu;
    }

    private void setCustomCpu(ListModel<String> customCpu) {
        this.customCpu = customCpu;
    }

    // Host tab

    private ListModel<VDS> defaultHost;

    public ListModel<VDS> getDefaultHost() {
        return defaultHost;
    }

    private void setDefaultHost(ListModel<VDS> value) {
        this.defaultHost = value;
    }

    private EntityModel<Boolean> isAutoAssign;

    public EntityModel<Boolean> getIsAutoAssign() {
        return isAutoAssign;
    }

    public void setIsAutoAssign(EntityModel<Boolean> value) {
        this.isAutoAssign = value;
    }

    // Botton section

    private EntityModel<Boolean> volatileRun;

    public EntityModel<Boolean> getVolatileRun() {
        return volatileRun;
    }

    public void setVolatileRun(EntityModel<Boolean> volatileRun) {
        this.volatileRun = volatileRun;
    }

    // The "sysprep" option was moved from a standalone check box to a
    // pseudo floppy or cdrom disk image. In order not to change the back-end
    // interface, the Reinitialize variable was changed to a read-only
    // property and its value is based on the selected floppy or cdrom image.
    // A similar comparison is done for cloud-init iso images, so the
    // variable was changed from a boolean to an Enum.
    public InitializationType getInitializationType() {
        if (getAttachFloppy().getEntity() != null
                && getAttachFloppy().getEntity()
                && "[sysprep]".equals(getFloppyImage().getSelectedItem())) { //$NON-NLS-1$
            return InitializationType.Sysprep;
        } else if (getAttachSysprep().getEntity() != null
                && getAttachSysprep().getEntity()) {
            return InitializationType.Sysprep;
        } else if (getIsCloudInitEnabled().getEntity() != null
                && getIsCloudInitEnabled().getEntity()) {
            return getIsIgnition() ? InitializationType.Ignition : InitializationType.CloudInit;
        } else {
            return InitializationType.None;
        }
    }

    public String getFloppyImagePath() {
        if (getAttachFloppy().getEntity()) {
            return getInitializationType() == InitializationType.Sysprep
                    ? "" : getFloppyImage().getSelectedItem(); //$NON-NLS-1$
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    public String getIsoImagePath() {
        if (getAttachIso().getEntity()) {
            return getIsoImage().getSelectedItem().getRepoImageId();
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    public void setIsoImagePath(String isoPath) {
        if ("".equals(isoPath)) { //$NON-NLS-1$
            getAttachIso().setEntity(false);
        } else {
            getAttachIso().setEntity(true);
            RepoImage iso = new RepoImage(isoPath);
            getIsoImage().setSelectedItem(iso);
        }
    }

    public RunOnceModel(VM vm, ICommandTarget commandTarget) {
        this.vm = vm;
        this.commandTarget = commandTarget;

        // Boot Options tab
        setAttachFloppy(new EntityModel<Boolean>());
        getAttachFloppy().getEntityChangedEvent().addListener(this);
        setFloppyImage(new ListModel<String>());
        getFloppyImage().getSelectedItemChangedEvent().addListener(this);
        setAttachIso(new EntityModel<Boolean>());
        getAttachIso().getEntityChangedEvent().addListener(this);
        setIsoImage(new SortedListModel(new LexoNumericNameableComparator<Nameable>()));
        getIsoImage().getSelectedItemChangedEvent().addListener(this);
        setAttachWgt(new EntityModel<Boolean>());
        setAttachSysprep(new EntityModel<Boolean>());
        getAttachSysprep().getEntityChangedEvent().addListener(this);
        setDisplayProtocol(new ListModel<EntityModel<DisplayType>>());
        setBootSequence(new BootSequenceModel());

        // Linux Boot Options tab
        setKernel_parameters(new EntityModel<String>());
        setKernelImage(new ListModel<String>());
        setInitrdImage(new ListModel<String>());

        // Initial Boot tab - Sysprep
        setIsCloudInitEnabled(new EntityModel<>(false));

        setSysPrepDomainName(new ListModel<String>());
        setSysPrepSelectedDomainName(new EntityModel<String>());

        setSysPrepUserName(new EntityModel<String>().setIsChangeable(false));
        setSysPrepPassword(new EntityModel<String>().setIsChangeable(false));
        setSysPrepPasswordVerification(new EntityModel<String>().setIsChangeable(false));

        setIsSysprepEnabled(new EntityModel<>(false));
        setIsSysprepPossible(new EntityModel<Boolean>());

        setIsVmFirstRun(new EntityModel<>(false));
        getIsVmFirstRun().getEntityChangedEvent().addListener(this);
        setUseAlternateCredentials(new EntityModel<>(false));
        getUseAlternateCredentials().getEntityChangedEvent().addListener(this);

        // Initial Boot tab - Cloud-Init
        setIsCloudInitPossible(new EntityModel<Boolean>());

        setVmInitModel(new VmInitModel());

        // Custom Properties tab
        setCustomPropertySheet(new KeyValueModel());

        setBootMenuEnabled(new EntityModel<>(false));
        getBootMenuEnabled().setIsAvailable(true);
        setRunAndPause(new EntityModel<>(false));
        setRunAsStateless(new EntityModel<>(false));

        // Display Protocol tab
        setRunOnceHeadlessModeIsSelected(new EntityModel<Boolean>());
        getRunOnceHeadlessModeIsSelected().getEntityChangedEvent().addListener(this);
        setDisplayConsole_Spice_IsSelected(new EntityModel<Boolean>());
        getDisplayConsole_Spice_IsSelected().getEntityChangedEvent().addListener(this);
        setDisplayConsole_Vnc_IsSelected(new EntityModel<Boolean>());
        getDisplayConsole_Vnc_IsSelected().getEntityChangedEvent().addListener(this);

        setVncKeyboardLayout(new ListModel<String>());
        getVncKeyboardLayout().getSelectedItemChangedEvent().addListener(this);
        initVncKeyboardLayout();
        getVncKeyboardLayout().setSelectedItem(vm.getDefaultVncKeyboardLayout());

        setSpiceFileTransferEnabled(new EntityModel<Boolean>());
        getSpiceFileTransferEnabled().setEntity(vm.isSpiceFileTransferEnabled());
        getSpiceFileTransferEnabled().setIsChangeable(true);
        getSpiceFileTransferEnabled().setIsAvailable(true);

        setSpiceCopyPasteEnabled(new EntityModel<Boolean>());
        getSpiceCopyPasteEnabled().setEntity(vm.isSpiceCopyPasteEnabled());
        getSpiceCopyPasteEnabled().setIsChangeable(true);
        getSpiceCopyPasteEnabled().setIsAvailable(true);

        // System tab
        setEmulatedMachine(new ListModel<String>());
        getEmulatedMachine().setSelectedItem(vm.getCustomEmulatedMachine());
        setClusterEmulatedMachine(new EntityModel<>());
        setCustomCpu(new ListModel<String>());
        getCustomCpu().setSelectedItem(vm.getCustomCpuName());

        // Host tab
        setDefaultHost(new ListModel<VDS>());
        getDefaultHost().getSelectedItemChangedEvent().addListener(this);

        setIsAutoAssign(new EntityModel<Boolean>());
        getIsAutoAssign().getEntityChangedEvent().addListener(this);

        // availability/visibility
        setIsLinuxOptionsAvailable(new EntityModel<>(false));

        setIsHostTabVisible(true);

        setIsCustomPropertiesSheetVisible(true);

        setIsLinuxOS(false);
        setIsWindowsOS(false);
        setIsIgnition(false);

        setVolatileRun(new EntityModel<>(false));
        getVolatileRun().setIsChangeable(true);
        if (!getVolatileRun().getIsChangable()) {
            getVolatileRun().setChangeProhibitionReason(
                    ConstantsManager.getInstance().getMessages().optionNotSupportedClusterVersionTooOld(vm.getCompatibilityVersion().toString()));
        }

        runOnceCommand = UICommand.createDefaultOkUiCommand(RUN_ONCE_COMMAND, this);
        cancelCommand = UICommand.createCancelUiCommand(Model.CANCEL_COMMAND, this);

        getCommands().addAll(Arrays.asList(runOnceCommand, cancelCommand));
    }

    public void updateOSs(){
        setIsWindowsOS(AsyncDataProvider.getInstance().isWindowsOsType(vm.getVmOsId()));
        setIsIgnition(AsyncDataProvider.getInstance().isIgnition(vm.getVmOsId()));
    }

    public void init() {
        setTitle(ConstantsManager.getInstance().getConstants().runVirtualMachinesTitle());
        setHelpTag(HelpTag.run_once_virtual_machine);
        setHashName("run_once_virtual_machine"); //$NON-NLS-1$
        setIsoImagePath(vm.getIsoPath()); // needs to be called before iso list is updated
        getAttachFloppy().setEntity(false);
        getAttachSysprep().setEntity(false);
        getAttachWgt().setEntity(false);
        getBootMenuEnabled().setEntity(true);
        getRunAsStateless().setEntity(vm.isStateless());
        getRunAndPause().setEntity(vm.isRunAndPause());

        updateOSs();
        // passing Kernel parameters
        getKernelParameters().setEntity(vm.getKernelParams());

        setIsLinuxOS(AsyncDataProvider.getInstance().isLinuxOsType(vm.getVmOsId()));
        getIsLinuxOptionsAvailable().setEntity(getIsLinuxOS());
        setIsWindowsOS(AsyncDataProvider.getInstance().isWindowsOsType(vm.getVmOsId()));
        getIsVmFirstRun().setEntity(!vm.isInitialized());

        initVmInitEnabled(vm.getVmInit(), vm.isInitialized());
        getVmInitModel().init(vm.getStaticData());
        toggleAutoSetVmHostname();

        updateDomainList();
        updateSystemTabLists();
        updateIsoList();
        updateUnknownTypeImagesList();
        updateFloppyImages();
        updateSysprep();
        updateInitialRunFields();

        // Boot sequence.
        setIsBootFromNetworkAllowedForVm();
        setIsBootFromHardDiskAllowedForVm();

        // Display protocols.
        if (vm.getDefaultDisplayType() != DisplayType.none) {
            updateDisplayProtocols();
        } else {
            getRunOnceHeadlessModeIsSelected().setEntity(true);
        }


        EntityModel<DisplayType> vncProtocol = new EntityModel<>(DisplayType.vga)
           .setTitle(ConstantsManager.getInstance().getConstants().vncTitle());

        EntityModel<DisplayType> qxlProtocol = new EntityModel<>(DisplayType.qxl)
           .setTitle(ConstantsManager.getInstance().getConstants().spiceTitle());

        boolean hasSpiceSupport =
                AsyncDataProvider.getInstance().hasSpiceSupport(vm.getOs(), vm.getCompatibilityVersion());

        if (hasSpiceSupport) {
            getDisplayProtocol().setItems(Arrays.asList(vncProtocol, qxlProtocol));
        } else {
            getDisplayProtocol().setItems(Arrays.asList(vncProtocol));
            getDisplayConsole_Spice_IsSelected().setIsAvailable(false);
        }

        getDisplayProtocol().setSelectedItem(vm.getDefaultDisplayType() == DisplayType.vga ?
                vncProtocol : qxlProtocol);
        getSpiceFileTransferEnabled().setEntity(vm.isSpiceFileTransferEnabled());
        getSpiceCopyPasteEnabled().setEntity(vm.isSpiceCopyPasteEnabled());

        AsyncDataProvider.isFloppySupported(new AsyncQuery<>(isFloppySupported -> {
            if (!isFloppySupported) {
                getAttachFloppy().setIsAvailable(false);
                getFloppyImage().setIsAvailable(false);
            } else {
                getAttachSysprep().setIsAvailable(false);
            }

        }), vm.getOs(), vm.getCompatibilityVersion());
    }

    private void initVmInitEnabled(VmInit vmInit, boolean isInitialized) {
        if (vmInit == null) {
            getIsCloudInitEnabled().setEntity(false);
            getIsSysprepEnabled().setEntity(false);
            getAttachFloppy().setEntity(false);
            getAttachSysprep().setEntity(false);
        } else if (!isInitialized) {
            if (getIsWindowsOS()) {
                getIsSysprepEnabled().setEntity(true);
                getAttachFloppy().setEntity(true);
                getAttachSysprep().setEntity(true);
            } else {
                getIsCloudInitEnabled().setEntity(true);
            }
        }
    }

    protected RunVmOnceParams createRunVmOnceParams() {
        RunVmOnceParams params = new RunVmOnceParams();
        params.setVmId(vm.getId());
        params.setBootSequence(getBootSequence().getSequence());
        params.setDiskPath(getIsoImagePath());
        params.setFloppyPath(getFloppyImagePath());
        params.setAttachWgt(getAttachWgt().getEntity());
        params.setBootMenuEnabled(getBootMenuEnabled().getEntity());
        params.setRunAndPause(getRunAndPause().getEntity());
        params.setRunAsStateless(getRunAsStateless().getEntity());
        params.setInitializationType(getInitializationType());
        params.setCustomProperties(getCustomPropertySheet().serialize());

        // kernel params
        String selectedKernelImage = getKernelImage().getSelectedItem();
        if (StringHelper.isNotNullOrEmpty(selectedKernelImage)) {
            params.setKernelUrl(selectedKernelImage);
        }

        if (getKernelParameters().getEntity() != null) {
            params.setKernelParams(getKernelParameters().getEntity());
        }

        String selectedInitrdImage = getInitrdImage().getSelectedItem();
        if (StringHelper.isNotNullOrEmpty(selectedInitrdImage)) {
            params.setInitrdUrl(selectedInitrdImage);
        }

        // Sysprep params
        if (getSysPrepUserName().getEntity() != null) {
            params.setSysPrepUserName(getSysPrepUserName().getEntity());
        }
        if (getSysPrepPassword().getEntity() != null) {
            params.setSysPrepPassword(getSysPrepPassword().getEntity());
        }

        if (getIsCloudInitEnabled() != null && getIsCloudInitEnabled().getEntity() ||
                getIsSysprepEnabled() != null && getIsSysprepEnabled().getEntity()) {
            params.setVmInit(getVmInitModel().buildCloudInitParameters(this));
        }

        if (getRunOnceHeadlessModeIsSelected().getEntity()) {
            params.getRunOnceGraphics().clear();
            params.setRunOnceDisplayType(DisplayType.none);
        } else {
            params.getRunOnceGraphics().add(Boolean.TRUE.equals(getDisplayConsole_Vnc_IsSelected().getEntity())
                    ? GraphicsType.VNC
                    : GraphicsType.SPICE);

            if (vm.getDefaultDisplayType() == DisplayType.none) {
                params.setRunOnceDisplayType(params.getRunOnceGraphics().contains(GraphicsType.SPICE) ? DisplayType.qxl
                        : DisplayType.vga);
            }
        }

        params.setVncKeyboardLayout(getVncKeyboardLayout().getSelectedItem());

        String selectedDomain = getSysPrepSelectedDomainName().getEntity();
        if (!StringHelper.isNullOrEmpty(selectedDomain)) {
             params.setSysPrepDomainName(selectedDomain);
        }

        String selectedEmulatedMachine = getEmulatedMachine().getSelectedItem();
        if (!StringHelper.isNullOrEmpty(selectedEmulatedMachine)) {
            params.setCustomEmulatedMachine(selectedEmulatedMachine);
        }

        String selectedCustomCpu = getCustomCpu().getSelectedItem();
        if (!StringHelper.isNullOrEmpty(selectedCustomCpu)) {
            params.setCustomCpuName(selectedCustomCpu);
        }

        params.setSpiceFileTransferEnabled(getSpiceFileTransferEnabled().getEntity());

        params.setSpiceCopyPasteEnabled(getSpiceCopyPasteEnabled().getEntity());

        params.setVolatileRun(getVolatileRun().getEntity());

        return params;
    }

    protected void updateFloppyImages() {
        ImagesDataProvider.getFloppyImageList(new AsyncQuery<>(
                        images -> {
                            images.sort(new LexoNumericComparator());

                            VM selectedVM = vm;

                            if (AsyncDataProvider.getInstance().isWindowsOsType(selectedVM.getVmOsId())) {
                                // Add a pseudo floppy disk image used for Windows' sysprep.
                                if (!selectedVM.isInitialized() && vm.getVmInit() != null) {
                                    images.add(0, "[sysprep]"); //$NON-NLS-1$
                                    getAttachFloppy().setEntity(true);
                                } else {
                                    images.add("[sysprep]"); //$NON-NLS-1$
                                }
                            }
                            getFloppyImage().setItems(images);

                            if (getFloppyImage().getIsChangable()
                                    && getFloppyImage().getSelectedItem() == null) {
                                getFloppyImage().setSelectedItem(Linq.firstOrNull(images));
                            }
                        }),
                vm.getStoragePoolId());
    }

    protected void updateSysprep() {
        VM selectedVM = vm;

        if (AsyncDataProvider.getInstance().isWindowsOsType(selectedVM.getVmOsId())) {
            // Add a pseudo CDROM disk image used for Windows' sysprep.
            if (!selectedVM.isInitialized() && vm.getVmInit() != null) {
                getAttachSysprep().setEntity(true);
            }
        } else {
            getAttachSysprep().setIsAvailable(false);
        }
    }

    private void setIsBootFromHardDiskAllowedForVm() {
        Frontend.getInstance().runQuery(QueryType.GetAllDisksByVmId, new IdQueryParameters(vm.getId()),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    ArrayList<Disk> vmDisks = returnValue.getReturnValue();

                    if (vmDisks.isEmpty()) {
                        getRunAsStateless().setIsChangeable(false);
                        getRunAsStateless()
                                .setChangeProhibitionReason(ConstantsManager.getInstance()
                                        .getMessages()
                                        .disklessVmCannotRunAsStateless());
                        getRunAsStateless().setEntity(false);
                    }

                    if (!isDisksContainBootableDisk(vmDisks)) {
                        BootSequenceModel bootSequenceModel = getBootSequence();
                        bootSequenceModel.getHardDiskOption().setIsChangeable(false);
                        bootSequenceModel.getHardDiskOption()
                                .setChangeProhibitionReason(ConstantsManager.getInstance()
                                        .getMessages()
                                        .bootableDiskIsRequiredToBootFromDisk());
                    }
                }));
    }

    private boolean isDisksContainBootableDisk(List<Disk> disks) {
        for (Disk disk : disks) {
            if (disk.getDiskVmElementForVm(vm.getId()).isBoot()) {
                return true;
            }
        }
        return false;
    }

    private void setIsBootFromNetworkAllowedForVm() {
        Frontend.getInstance().runQuery(QueryType.GetVmInterfacesByVmId, new IdQueryParameters(vm.getId()),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    Collection<VmNetworkInterface> nics = returnValue.getReturnValue();
                    boolean hasPluggedNics = nics.stream().anyMatch(VmNetworkInterface::isPlugged);

                    if (!hasPluggedNics) {
                        BootSequenceModel bootSequenceModel = getBootSequence();
                        bootSequenceModel.getNetworkOption().setIsChangeable(false);
                        bootSequenceModel.getNetworkOption()
                                .setChangeProhibitionReason(ConstantsManager.getInstance()
                                        .getMessages()
                                        .interfaceIsRequiredToBootFromNetwork());
                    }
                }));
    }

    private void updateDisplayProtocols() {
        Frontend.getInstance().runQuery(QueryType.GetGraphicsDevices, new IdQueryParameters(vm.getId()),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    boolean selectVnc = false;

                    List<GraphicsDevice> graphicsDevices = returnValue.getReturnValue();
                    if (graphicsDevices.size() == 1 && graphicsDevices.get(0).getGraphicsType() == GraphicsType.VNC) {
                        selectVnc = true;
                    }

                    getDisplayConsole_Vnc_IsSelected().setEntity(selectVnc);
                    getDisplayConsole_Spice_IsSelected().setEntity(!selectVnc);
                }));
    }

    public void updateIsoList() {
        updateIsoList(false);
    }

    public void updateUnknownTypeImagesList() {
        updateUnknownTypeImagesList(false);
    }

    public void updateUnknownTypeImagesList(boolean forceRefresh) {
        ImagesDataProvider.getUnknownImageList(new AsyncQuery<>(images -> {
            getKernelImage().setItems(images);
            getInitrdImage().setItems(images);

            getKernelImage().setSelectedItem(null);
            getInitrdImage().setSelectedItem(null);
        }), vm.getStoragePoolId(), forceRefresh);
    }

    public void updateIsoList(boolean forceRefresh) {
        ImagesDataProvider.getISOImagesList(new AsyncQuery<>(
                        images -> {
                            final RepoImage lastSelectedIso = getIsoImage().getSelectedItem();

                            getIsoImage().setItems(images);

                            if (getIsoImage().getIsChangable()) {
                                // try to preselect last image
                                if (lastSelectedIso != null) {
                                    getIsoImage().setSelectedItem(images.stream()
                                            .filter(i -> i.getRepoImageId().equals(lastSelectedIso.getRepoImageId())).findFirst().orElse(null));
                                } else {
                                    getIsoImage().setSelectedItem(Linq.firstOrNull(images));
                                }
                            }
                        }),
                vm.getStoragePoolId(), forceRefresh);
    }

    private void updateDomainList() {
        // Update Domain list
        AsyncDataProvider.getInstance().getAuthzExtensionsNames(new AsyncQuery<>(domains -> {
            String oldDomain = getSysPrepDomainName().getSelectedItem();
            if (oldDomain != null && !oldDomain.equals("") && !domains.contains(oldDomain)) { //$NON-NLS-1$
                domains.add(0, oldDomain);
            }
            getSysPrepDomainName().setItems(domains);
            String selectedDomain = (oldDomain != null) ? oldDomain : Linq.firstOrNull(domains);
            if (!StringHelper.isNullOrEmpty(selectedDomain)) {
                getSysPrepDomainName().setSelectedItem(selectedDomain);
            }
        }));
    }

    private void updateSystemTabLists() {
        Guid clusterId = vm.getClusterId();

        if (clusterId != null) {
            initEmulatedMachineFields(clusterId);

            AsyncDataProvider.getInstance().getClusterById(new AsyncQuery<>(
                    cluster -> {
                        if (cluster != null) {
                            // update cpu names list
                            if (cluster.getCpuName() != null && !"".equals(cluster.getCpuName())) {
                                AsyncDataProvider.getInstance().getSupportedCpuList(new AsyncQuery<>(
                                        returnValue -> {
                                            if (returnValue != null) {
                                                List<String> cpuList = new ArrayList<>();
                                                for (ServerCpu cpu : returnValue) {
                                                    cpuList.add(cpu.getVdsVerbData());
                                                }
                                                String oldVal = getCustomCpu().getSelectedItem();
                                                getCustomCpu().setItems(cpuList);

                                                // replace 'cluster cpu' with the explicit run-time value
                                                if (StringHelper.isNullOrEmpty(oldVal)
                                                        && !cpuList.isEmpty()) {
                                                    getCustomCpu().setSelectedItem(cpuList.get(
                                                            cpuList.size() - 1));
                                                } else {
                                                    getCustomCpu().setSelectedItem(oldVal);
                                                }
                                            }
                                        }), cluster.getCpuName(), cluster.getCompatibilityVersion());
                            }
                        }
                    }), clusterId);
        }
    }

    public void sysPrepListBoxChanged() {
        getSysPrepSelectedDomainName().setEntity(getSysPrepDomainName().getSelectedItem());
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition)) {
            if (sender == getIsoImage()) {
                IsoImage_SelectedItemChanged();
            } else if (sender == getFloppyImage()) {
                floppyImage_SelectedItemChanged();
            }
        } else if (ev.matchesDefinition(HasEntity.entityChangedEventDefinition)) {
            if (sender == getAttachFloppy()) {
                attachFloppy_EntityChanged();
            } else if (sender == getAttachIso()) {
                attachIso_EntityChanged();
            } else if (sender == getAttachSysprep()) {
                attachSysprep_EntityChanged();
            } else if (sender == getIsVmFirstRun()) {
                isVmFirstRun_EntityChanged();
            } else if (sender == getUseAlternateCredentials()) {
                useAlternateCredentials_EntityChanged();
            } else if (sender == getDisplayConsole_Vnc_IsSelected() && ((EntityModel<Boolean>) sender).getEntity()) {
                getRunOnceHeadlessModeIsSelected().setEntity(false);
                getDisplayConsole_Spice_IsSelected().setEntity(false);
                getVncKeyboardLayout().setIsChangeable(true);
                getSpiceFileTransferEnabled().setIsChangeable(false);
                getSpiceCopyPasteEnabled().setIsChangeable(false);
            } else if (sender == getDisplayConsole_Spice_IsSelected() && ((EntityModel<Boolean>) sender).getEntity()) {
                getRunOnceHeadlessModeIsSelected().setEntity(false);
                getDisplayConsole_Vnc_IsSelected().setEntity(false);
                getVncKeyboardLayout().setIsChangeable(false);
                getSpiceFileTransferEnabled().setIsChangeable(true);
                getSpiceCopyPasteEnabled().setIsChangeable(true);
            } else if (sender == getRunOnceHeadlessModeIsSelected() && ((EntityModel<Boolean>) sender).getEntity()) {
                getDisplayConsole_Vnc_IsSelected().setEntity(false);
                getDisplayConsole_Spice_IsSelected().setEntity(false);

                getVncKeyboardLayout().setIsChangeable(false);
                getSpiceFileTransferEnabled().setIsChangeable(false);
                getSpiceCopyPasteEnabled().setIsChangeable(false);
            } else if (sender == getIsAutoAssign()) {
                isAutoAssign_EntityChanged(sender, args);
            }
        }
    }

    private void attachIso_EntityChanged() {
        getIsoImage().setIsChangeable(getAttachIso().getEntity());
        getBootSequence().getCdromOption().setIsChangeable(getAttachIso().getEntity());
        updateInitialRunFields();
    }

    private void attachFloppy_EntityChanged() {
        getFloppyImage().setIsChangeable(getAttachFloppy().getEntity());
        updateInitialRunFields();
    }

    private void attachSysprep_EntityChanged() {
        updateInitialRunFields();
    }

    private void useAlternateCredentials_EntityChanged() {
        boolean useAlternateCredentials = getUseAlternateCredentials().getEntity();

        getSysPrepUserName().setIsChangeable(getUseAlternateCredentials().getEntity());
        getSysPrepPassword().setIsChangeable(getUseAlternateCredentials().getEntity());
        getSysPrepPasswordVerification().setIsChangeable(getUseAlternateCredentials().getEntity());

        getSysPrepUserName().setEntity(useAlternateCredentials ? "" : null); //$NON-NLS-1$
        getSysPrepPassword().setEntity(useAlternateCredentials ? "" : null); //$NON-NLS-1$
        getSysPrepPasswordVerification().setEntity(useAlternateCredentials ? "" : null); //$NON-NLS-1$
    }

    private void isVmFirstRun_EntityChanged() {
        updateInitialRunFields();
    }

    private void floppyImage_SelectedItemChanged() {
        updateInitialRunFields();
    }

    private void sysprepImage_SelectedItemChanged() {
        updateInitialRunFields();
    }

    private void IsoImage_SelectedItemChanged() {
        updateInitialRunFields();
    }

    private void isAutoAssign_EntityChanged(Object sender, EventArgs args) {
        if (!getIsAutoAssign().getEntity()) {
            getDefaultHost().setIsChangeable(true);
        }
    }

    // Sysprep/cloud-init/ignition sections displayed only with proper OS type (Windows
    // /Linux or core os, respectively) and when proper floppy or CD is attached.
    // Currently vm.isFirstRun() status is not considered.
    public void updateInitialRunFields() {
        getIsSysprepPossible().setEntity(getIsWindowsOS());
        getIsSysprepEnabled().setEntity(getInitializationType() == InitializationType.Sysprep);
        // also other can be cloud inited
        getIsCloudInitPossible().setEntity(!getIsWindowsOS());
        getIsCloudInitEnabled().setEntity(getInitializationType() == InitializationType.CloudInit
                || getInitializationType() == InitializationType.Ignition);
        getIsCloudInitEnabled().setIsAvailable(!getIsWindowsOS());
        getAttachWgt().setIsChangeable(getIsWindowsOS() && getAttachIso().getEntity());

        if (getIsSysprepPossible().getEntity() && getIsSysprepEnabled().getEntity()) {
            getVmInitModel().updateSysprepDomain(getVmInitModel().getSysprepDomain().getSelectedItem());
        }
    }

    public boolean validate() {
        getIsoImage().setIsValid(true);
        if (getAttachIso().getEntity()) {
            getIsoImage().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }

        getFloppyImage().setIsValid(true);
        if (getAttachFloppy().getEntity()) {
            getFloppyImage().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }

        boolean customPropertyValidation = getCustomPropertySheet().validate();

        if (getIsLinuxOS()) {
            getKernelImage().validateSelectedItem(new IValidation[] { new NoTrimmingWhitespacesValidation() });
            getInitrdImage().validateSelectedItem(new IValidation[] { new NoTrimmingWhitespacesValidation() });
            getKernelParameters().validateEntity(new IValidation[] { new NoTrimmingWhitespacesValidation() });

            // initrd path and kernel params require kernel path to be filled
            if (StringHelper.isNullOrEmpty(getKernelImage().getSelectedItem())) {
                final UIConstants constants = ConstantsManager.getInstance().getConstants();

                if (!StringHelper.isNullOrEmpty(getInitrdImage().getSelectedItem())) {
                    getInitrdImage().getInvalidityReasons().add(constants.initrdPathInvalid());
                    getInitrdImage().setIsValid(false);
                    getKernelImage().getInvalidityReasons().add(constants.initrdPathInvalid());
                    getKernelImage().setIsValid(false);
                }

                if (!StringHelper.isNullOrEmpty(getKernelParameters().getEntity())) {
                    getKernelParameters().getInvalidityReasons().add(constants.kernelParamsInvalid());
                    getKernelParameters().setIsValid(false);
                    getKernelImage().getInvalidityReasons().add(constants.kernelParamsInvalid());
                    getKernelImage().setIsValid(false);
                }
            }
        }

        if (getIsAutoAssign().getEntity() != null && !getIsAutoAssign().getEntity()) {
            getDefaultHost().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        } else {
            getDefaultHost().setIsValid(true);
        }

        getSysPrepPassword().setIsValid(true);
        getSysPrepPasswordVerification().setIsValid(true);

        if (getIsWindowsOS() && getIsSysprepEnabled().getEntity()) {
            getSysPrepPassword().validateEntity(new IValidation[] {
                    new NotEmptyValidation(),
                    new MatchFieldsValidator(getSysPrepPassword().getEntity(),
                                             getSysPrepPasswordVerification().getEntity())
            });
        }

        boolean cloudInitIsValid = getVmInitModel().validate();

        getEmulatedMachine().validateSelectedItem(new IValidation[] { new I18NExtraNameOrNoneValidation(), new LengthValidation(BusinessEntitiesDefinitions.VM_EMULATED_MACHINE_SIZE) });
        getCustomCpu().validateSelectedItem(new IValidation[] { new CpuNameValidation(), new LengthValidation(BusinessEntitiesDefinitions.VM_CPU_NAME_SIZE) });

        return getIsoImage().getIsValid()
                && getFloppyImage().getIsValid()
                && getKernelImage().getIsValid()
                && getInitrdImage().getIsValid()
                && getKernelParameters().getIsValid()
                && getDefaultHost().getIsValid()
                && customPropertyValidation
                && cloudInitIsValid
                && getSysPrepPassword().getIsValid()
                && getEmulatedMachine().getIsValid()
                && getCustomCpu().getIsValid();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command == runOnceCommand) {
            if (validate()) {
                onRunOnce();
            }
        } else if (command == cancelCommand) {
            commandTarget.executeCommand(command);
        }
    }

    protected abstract void onRunOnce();

    private void initVncKeyboardLayout() {

        List<String> layouts =
                (List<String>) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.VncKeyboardLayoutValidValues);
        List<String> vncKeyboardLayoutItems = new ArrayList<>();
        vncKeyboardLayoutItems.add(null);
        vncKeyboardLayoutItems.addAll(layouts);
        getVncKeyboardLayout().setItems(vncKeyboardLayoutItems);

        getVncKeyboardLayout().setIsChangeable(false);
    }

    private void initEmulatedMachineFields(Guid clusterId) {
        AsyncDataProvider.getInstance().getClusterById(new AsyncQuery<>(
                cluster -> {
                    if (cluster != null) {
                        getClusterEmulatedMachine().setEntity(cluster.getEmulatedMachine());
                    }
                    loadEmulatedMachineList(clusterId);
                }), clusterId);
    }

    private void loadEmulatedMachineList(Guid clusterId) {
        // update emulated machine list
        AsyncDataProvider.getInstance().getEmulatedMachinesByClusterID(new AsyncQuery<>(
                emulatedSet -> {
                    if (emulatedSet != null) {
                        String oldVal = getEmulatedMachine().getSelectedItem();
                        List<String> items = new ArrayList<>(new TreeSet<>(emulatedSet));
                        items.add(0, "");//$NON-NLS-1$
                        getEmulatedMachine().setItems(items);
                        getEmulatedMachine().setSelectedItem(oldVal);
                    }
                }), clusterId);
    }

    public void autoSetVmHostname() {
        getVmInitModel().autoSetHostname(vm.getName());
    }

    private void toggleAutoSetVmHostname() {
        if (vm.getVmInit() != null && vm.getName() != null
                && !vm.getName().equals(vm.getVmInit().getHostname())) {
            getVmInitModel().disableAutoSetHostname();
        }
    }


}
