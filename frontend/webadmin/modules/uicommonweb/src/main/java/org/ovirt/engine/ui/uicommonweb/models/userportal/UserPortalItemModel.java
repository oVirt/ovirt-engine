package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister.ConsoleContext;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.ChangeCDModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class UserPortalItemModel extends EntityModel implements HasConsoleModel
{

    @Override
    public ConsoleProtocol getUserSelectedProtocol() {
        return userSelectedDisplayProtocolManager.resolveSelectedProtocol(this);
    }

    @Override
    public void setSelectedProtocol(ConsoleProtocol selectedProtocol) {
        userSelectedDisplayProtocolManager.setSelectedProtocol(selectedProtocol, this);
    }

    private UICommand runCommand;

    public UICommand getRunCommand()
    {
        return runCommand;
    }

    private void setRunCommand(UICommand value)
    {
        getCommands().remove(runCommand);
        getCommands().add(value);
        runCommand = value;
    }

    private UICommand pauseCommand;

    public UICommand getPauseCommand()
    {
        return pauseCommand;
    }

    private void setPauseCommand(UICommand value)
    {
        getCommands().remove(pauseCommand);
        getCommands().add(value);
        pauseCommand = value;
    }

    private UICommand stopCommand;

    public UICommand getStopCommand()
    {
        return stopCommand;
    }

    private void setStopCommand(UICommand value)
    {
        getCommands().remove(stopCommand);
        getCommands().add(value);
        stopCommand = value;
    }

    private UICommand shutdownCommand;

    public UICommand getShutdownCommand()
    {
        return shutdownCommand;
    }

    private void setShutdownCommand(UICommand value)
    {
        getCommands().remove(shutdownCommand);
        getCommands().add(value);
        shutdownCommand = value;
    }

    private UICommand takeVmCommand;

    public UICommand getTakeVmCommand()
    {
        return takeVmCommand;
    }

    private void setTakeVmCommand(UICommand value)
    {
        getCommands().remove(takeVmCommand);
        getCommands().add(value);
        takeVmCommand = value;
    }

    private UICommand returnVmCommand;

    public UICommand getReturnVmCommand()
    {
        return returnVmCommand;
    }

    private void setReturnVmCommand(UICommand value)
    {
        getCommands().remove(returnVmCommand);
        getCommands().add(value);
        returnVmCommand = value;
    }

    private IVmPoolResolutionService privateResolutionService;

    public IVmPoolResolutionService getResolutionService()
    {
        return privateResolutionService;
    }

    private void setResolutionService(IVmPoolResolutionService value)
    {
        privateResolutionService = value;
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
            onPropertyChanged(new PropertyChangedEventArgs("Description")); //$NON-NLS-1$
        }
    }

    private boolean isPool;

    @Override
    public boolean isPool()
    {
        return isPool;
    }

    public void setIsPool(boolean value)
    {
        if (isPool != value)
        {
            isPool = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsPool")); //$NON-NLS-1$
        }
    }

    private boolean isServer;

    public boolean getIsServer()
    {
        return isServer;
    }

    public void setIsServer(boolean value)
    {
        if (isServer != value)
        {
            isServer = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsServer")); //$NON-NLS-1$
        }
    }

    private boolean isFromPool;

    public boolean getIsFromPool()
    {
        return isFromPool;
    }

    public void setIsFromPool(boolean value)
    {
        if (isFromPool != value)
        {
            isFromPool = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsFromPool")); //$NON-NLS-1$
        }
    }

    private VmPoolType poolType = VmPoolType.values()[0];

    public VmPoolType getPoolType()
    {
        return poolType;
    }

    public void setPoolType(VmPoolType value)
    {
        if (poolType != value)
        {
            poolType = value;
            onPropertyChanged(new PropertyChangedEventArgs("PoolType")); //$NON-NLS-1$
        }
    }

    private VMStatus status = VMStatus.values()[0];

    public VMStatus getStatus()
    {
        return status;
    }

    public void setStatus(VMStatus value)
    {
        if (status != value)
        {
            status = value;
            onPropertyChanged(new PropertyChangedEventArgs("Status")); //$NON-NLS-1$
        }
    }

    private ConsoleModel defaultConsole;

    @Override
    public ConsoleModel getDefaultConsoleModel()
    {
        return defaultConsole;
    }

    public void setDefaultConsole(ConsoleModel value)
    {
        if (defaultConsole != value)
        {
            defaultConsole = value;
            onPropertyChanged(new PropertyChangedEventArgs("DefaultConsole")); //$NON-NLS-1$
        }
    }

    private ConsoleModel additionalConsole;

    @Override
    public ConsoleModel getAdditionalConsoleModel()
    {
        return additionalConsole;
    }

    public void setAdditionalConsole(ConsoleModel value)
    {
        if (additionalConsole != value)
        {
            additionalConsole = value;
            onPropertyChanged(new PropertyChangedEventArgs("AdditionalConsole")); //$NON-NLS-1$
        }
    }

    private List<ChangeCDModel> cdImages;

    public List<ChangeCDModel> getCdImages()
    {
        return cdImages;
    }

    public void setCdImages(List<ChangeCDModel> value)
    {
        if (cdImages != value)
        {
            cdImages = value;
            onPropertyChanged(new PropertyChangedEventArgs("CdImages")); //$NON-NLS-1$
        }
    }

    private VmOsType osType = VmOsType.values()[0];

    public VmOsType getOsType()
    {
        return osType;
    }

    public void setOsType(VmOsType value)
    {
        if (osType != value)
        {
            osType = value;
            onPropertyChanged(new PropertyChangedEventArgs("OsType")); //$NON-NLS-1$
        }
    }

    private Version spiceDriverVersion;

    public Version getSpiceDriverVersion() {
        return spiceDriverVersion;
    }

    public void setSpiceDriverVersion(Version spiceDriverVersion) {
        if (this.spiceDriverVersion != spiceDriverVersion) {
            this.spiceDriverVersion = spiceDriverVersion;
            onPropertyChanged(new PropertyChangedEventArgs("spiceDriverVersion")); //$NON-NLS-1$
        }
    }

    private ItemBehavior behavior;
    private final UserSelectedDisplayProtocolManager userSelectedDisplayProtocolManager;
    private final ConsoleContext consoleContext;

    public UserPortalItemModel(IVmPoolResolutionService resolutionService,
            UserSelectedDisplayProtocolManager userSelectedDisplayManager,
            ConsoleContext consoleContext) {
        this.userSelectedDisplayProtocolManager = userSelectedDisplayManager;
        this.consoleContext = consoleContext;
        setResolutionService(resolutionService);

        setRunCommand(new UICommand("Run", this)); //$NON-NLS-1$
        setPauseCommand(new UICommand("Pause", this)); //$NON-NLS-1$
        setStopCommand(new UICommand("Stop", this)); //$NON-NLS-1$
        setShutdownCommand(new UICommand("Shutdown", this)); //$NON-NLS-1$
        setTakeVmCommand(new UICommand("TakeVm", this)); //$NON-NLS-1$
        setReturnVmCommand(new UICommand("ReturnVm", this)); //$NON-NLS-1$

        ChangeCDModel tempVar = new ChangeCDModel();
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().retrievingCDsTitle());
        setCdImages(new ArrayList<ChangeCDModel>(Arrays.asList(new ChangeCDModel[] { tempVar })));
    }

    @Override
    protected void onEntityChanged()
    {
        // Change behavior to match entity type.
        if (getEntity() instanceof VM)
        {
            behavior = new VmItemBehavior(this);
        }
        else if (getEntity() instanceof VmPool)
        {
            behavior = new PoolItemBehavior(this);
        }
        else
        {
            throw new UnsupportedOperationException();
        }

        behavior.OnEntityChanged();
    }

    public void clearReferences() {
        clearBehavior();
        clearCommands();
    }

    private void clearBehavior() {
        behavior = null;
    }

    private void clearCommands() {
        pauseCommand.setTarget(null);
        pauseCommand = null;
        returnVmCommand.setTarget(null);
        returnVmCommand = null;
        runCommand.setTarget(null);
        runCommand = null;
        shutdownCommand.setTarget(null);
        shutdownCommand = null;
        stopCommand.setTarget(null);
        stopCommand = null;
        takeVmCommand.setTarget(null);
        takeVmCommand = null;
        setCommands(null);
    }

    public boolean IsVmUp()
    {
        switch (getStatus())
        {
        case WaitForLaunch:
        case PoweringUp:
        case RebootInProgress:
        case RestoringState:
        case MigratingFrom:
        case MigratingTo:
        case Up:
            return true;

        default:
            return false;
        }
    }

    // to simpler integration with the editor framework
    public boolean getIsVmUp() {
        return IsVmUp();
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);
        behavior.EntityPropertyChanged(e);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);
        behavior.eventRaised(ev, sender, args);
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);
        behavior.ExecuteCommand(command);
    }

    public Guid getId() {
        return (Guid) ((IVdcQueryable) getEntity()).getQueryableId();
    }

    public boolean entityStateEqualTo(UserPortalItemModel other) {
        // Compare pools
        if (getEntity() instanceof VmPool && other.getEntity() instanceof VmPool) {
            return getEntity().equals(other.getEntity());
        }
        // Compare VMs
        else if (getEntity() instanceof VM && other.getEntity() instanceof VM) {
            VM thisVm = (VM) getEntity();
            VM otherVm = (VM) other.getEntity();
            return thisVm.getDynamicData().getStatus().equals(otherVm.getDynamicData().getStatus())
                            && thisVm.getStaticData().equals(otherVm.getStaticData());
        }
        return false;
    }

    @Override
    public VM getVM() {
        if (getEntity() instanceof VM) {
            return (VM) getEntity();
        }

        return null;
    }

    @Override
    public ConsoleContext getConsoleContext() {
        return consoleContext;
    }
}
