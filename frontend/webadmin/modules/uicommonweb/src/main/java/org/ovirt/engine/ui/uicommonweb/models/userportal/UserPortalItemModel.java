package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConsolesFactory;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.VmConsoles;
import org.ovirt.engine.ui.uicommonweb.models.configure.ChangeCDModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

public class UserPortalItemModel extends EntityModel {
    private UICommand runCommand;
    private final VM poolRepresentative;
    private final ConsolesFactory consolesFactory;

    public UICommand getRunCommand() {
        return runCommand;
    }

    private void setRunCommand(UICommand value) {
        getCommands().remove(runCommand);
        getCommands().add(value);
        runCommand = value;
    }

    private UICommand pauseCommand;

    public UICommand getPauseCommand() {
        return pauseCommand;
    }

    private void setPauseCommand(UICommand value) {
        getCommands().remove(pauseCommand);
        getCommands().add(value);
        pauseCommand = value;
    }

    private UICommand stopCommand;

    public UICommand getStopCommand() {
        return stopCommand;
    }

    private void setStopCommand(UICommand value) {
        getCommands().remove(stopCommand);
        getCommands().add(value);
        stopCommand = value;
    }

    private UICommand shutdownCommand;

    public UICommand getShutdownCommand() {
        return shutdownCommand;
    }

    private void setShutdownCommand(UICommand value) {
        getCommands().remove(shutdownCommand);
        getCommands().add(value);
        shutdownCommand = value;
    }

    private UICommand takeVmCommand;

    public UICommand getTakeVmCommand() {
        return takeVmCommand;
    }

    private void setTakeVmCommand(UICommand value) {
        getCommands().remove(takeVmCommand);
        getCommands().add(value);
        takeVmCommand = value;
    }

    private UICommand returnVmCommand;

    public UICommand getReturnVmCommand() {
        return returnVmCommand;
    }

    private void setReturnVmCommand(UICommand value) {
        getCommands().remove(returnVmCommand);
        getCommands().add(value);
        returnVmCommand = value;
    }

    private UICommand rebootCommand;

    public UICommand getRebootCommand() {
        return rebootCommand;
    }

    public void setRebootCommand(UICommand value) {
        getCommands().remove(rebootCommand);
        getCommands().add(value);
        rebootCommand = value;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        if (!Objects.equals(name, value)) {
            name = value;
            onPropertyChanged(new PropertyChangedEventArgs("Name")); //$NON-NLS-1$
        }
    }

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        if (!Objects.equals(description, value)) {
            description = value;
            onPropertyChanged(new PropertyChangedEventArgs("Description")); //$NON-NLS-1$
        }
    }

    private boolean isPool;

    public boolean isPool() {
        return isPool;
    }

    public void setIsPool(boolean value) {
        if (isPool != value) {
            isPool = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsPool")); //$NON-NLS-1$
        }
    }

    private boolean isServer;

    public boolean getIsServer() {
        return isServer;
    }

    public void setIsServer(boolean value) {
        if (isServer != value) {
            isServer = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsServer")); //$NON-NLS-1$
        }
    }

    private boolean isFromPool;

    public boolean getIsFromPool() {
        return isFromPool;
    }

    public void setIsFromPool(boolean value) {
        if (isFromPool != value) {
            isFromPool = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsFromPool")); //$NON-NLS-1$
        }
    }

    private VmPoolType poolType = VmPoolType.values()[0];

    public VmPoolType getPoolType() {
        return poolType;
    }

    public void setPoolType(VmPoolType value) {
        if (poolType != value) {
            poolType = value;
            onPropertyChanged(new PropertyChangedEventArgs("PoolType")); //$NON-NLS-1$
        }
    }

    private VMStatus status = VMStatus.values()[0];

    public VMStatus getStatus() {
        return status;
    }

    public void setStatus(VMStatus value) {
        if (status != value) {
            status = value;
            onPropertyChanged(new PropertyChangedEventArgs("Status")); //$NON-NLS-1$
        }
    }

    private List<ChangeCDModel> cdImages;

    public List<ChangeCDModel> getCdImages() {
        return cdImages;
    }

    public void setCdImages(List<ChangeCDModel> value) {
        if (cdImages != value) {
            cdImages = value;
            onPropertyChanged(new PropertyChangedEventArgs("CdImages")); //$NON-NLS-1$
        }
    }

    private int osId;

    public int getOsId() {
        return osId;
    }

    public void setOsId(int value) {
        if (osId != value) {
            osId = value;
            onPropertyChanged(new PropertyChangedEventArgs("OsId")); //$NON-NLS-1$
        }
    }

    private Guid smallIconId;

    public Guid getSmallIconId() {
        return smallIconId;
    }

    public void setSmallIconId(Guid smallIconId) {
        if (!Objects.equals(this.smallIconId, smallIconId)) {
            this.smallIconId = smallIconId;
            onPropertyChanged(new PropertyChangedEventArgs("SmallIconId")); //$NON-NLS-1$
        }
    }

    private Guid largeIconId;

    public Guid getLargeIconId() {
        return largeIconId;
    }

    public void setLargeIconId(Guid largeIconId) {
        if (!Objects.equals(this.largeIconId, largeIconId)) {
            this.largeIconId = largeIconId;
            onPropertyChanged(new PropertyChangedEventArgs("LargeIconId")); //$NON-NLS-1$
        }
    }


    private ItemBehavior behavior;

    /**
     *
     * @param vmOrPool instance of either {@link VM} or {@link VmPool} - the wrapped entity
     * @param poolRepresentative if argument {@code vmOrPool} is instance if {@link VM} then {@code null},
     *                           if {@code vmOrPool} is instance if {@link VmPool} then arbitrary VM from
     *                           that pool
     */
    public UserPortalItemModel(Object vmOrPool, VM poolRepresentative, ConsolesFactory consolesFactory) {
        setRunCommand(new UICommand("Run", this)); //$NON-NLS-1$
        setPauseCommand(new UICommand("Pause", this)); //$NON-NLS-1$
        setStopCommand(new UICommand("Stop", this)); //$NON-NLS-1$
        setShutdownCommand(new UICommand("Shutdown", this)); //$NON-NLS-1$
        setTakeVmCommand(new UICommand("TakeVm", this)); //$NON-NLS-1$
        setReturnVmCommand(new UICommand("ReturnVm", this)); //$NON-NLS-1$
        setRebootCommand(new UICommand("RebootVm", this)); //$NON-NLS-1$

        ChangeCDModel tempVar = new ChangeCDModel();
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().retrievingCDsTitle());
        setCdImages(new ArrayList<>(Arrays.asList(new ChangeCDModel[]{tempVar})));

        this.poolRepresentative = poolRepresentative;
        this.consolesFactory = consolesFactory;

        setEntity(vmOrPool);
    }

    @Override
    protected void onEntityChanged() {
        // Change behavior to match entity type.
        if (getEntity() instanceof VM) {
            behavior = new VmItemBehavior(this);
        }
        else if (getEntity() instanceof VmPool) {
            behavior = new PoolItemBehavior(this, poolRepresentative);
        }
        else {
            throw new UnsupportedOperationException();
        }

        behavior.onEntityChanged();
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
        rebootCommand.setTarget(null);
        rebootCommand = null;
        setCommands(null);
    }

    public boolean isVmUp() {
        switch (getStatus()) {
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
        return isVmUp();
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);
        behavior.entityPropertyChanged(e);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);
        behavior.eventRaised(ev, sender, args);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        behavior.executeCommand(command);
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
            boolean consoleUsersEqual = Objects.equals(thisVm.getConsoleCurentUserName(),
                    otherVm.getConsoleCurentUserName());

            return  thisVm.getDynamicData().getStatus().equals(otherVm.getDynamicData().getStatus())
                    && consoleUsersEqual
                    && thisVm.getStaticData().equals(otherVm.getStaticData());
        }
        return false;
    }

    public VM getVM() {
        if (getEntity() instanceof VM) {
            return (VM) getEntity();
        }

        return null;
    }

    public Pair<VMStatus, Boolean> getStatusWithConsoleState() {
        boolean consoleTaken = getVM() != null && !StringUtils.isEmpty(getVM().getClientIp());
        return new Pair<> (getStatus(), consoleTaken);
    }

    public VmConsoles getVmConsoles() {
        return poolRepresentative == null
               ? consolesFactory.getVmConsolesForVm((VM) getEntity())
               : consolesFactory.getVmConsolesForPool(poolRepresentative);
    }
}
