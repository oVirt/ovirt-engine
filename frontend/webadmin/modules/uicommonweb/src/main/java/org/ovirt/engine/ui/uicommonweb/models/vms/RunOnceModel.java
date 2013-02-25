package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.compat.StringHelper;
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
public class RunOnceModel extends Model
{

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
            OnPropertyChanged(new PropertyChangedEventArgs("HwAcceleration")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("BootSequence")); //$NON-NLS-1$
        }
    }

    private boolean isHostTabVisible = false;

    public boolean getIsHostTabVisible() {
        return isHostTabVisible;
    }

    public void setIsHostTabVisible(boolean value) {
        if (isHostTabVisible != value) {
            isHostTabVisible = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsHostTabVisible")); //$NON-NLS-1$
        }
    }

    private boolean isCustomPropertiesSheetVisible = false;

    public boolean getIsCustomPropertiesSheetVisible() {
        return isCustomPropertiesSheetVisible;
    }

    public void setIsCustomPropertiesSheetVisible(boolean value) {
        if (isCustomPropertiesSheetVisible != value) {
            isCustomPropertiesSheetVisible = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsCustomPropertiesSheetVisible")); //$NON-NLS-1$
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

    public RunOnceModel()
    {
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

        EntityModel tempVar = new EntityModel();
        tempVar.setIsChangable(false);
        setSysPrepUserName(tempVar);
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setIsChangable(false);
        setSysPrepPassword(tempVar2);

        setIsSysprepEnabled(new EntityModel());
        EntityModel tempVar3 = new EntityModel();
        tempVar3.setEntity(false);
        setIsVmFirstRun(tempVar3);
        getIsVmFirstRun().getEntityChangedEvent().addListener(this);
        EntityModel tempVar4 = new EntityModel();
        tempVar4.setEntity(false);
        setUseAlternateCredentials(tempVar4);
        getUseAlternateCredentials().getEntityChangedEvent().addListener(this);

        setCustomProperties(new EntityModel());
        setCustomPropertySheet(new KeyValueModel());

        EntityModel tempVar5 = new EntityModel();
        tempVar5.setEntity(false);
        setRunAndPause(tempVar5);
        EntityModel tempVar6 = new EntityModel();
        tempVar6.setEntity(false);
        setRunAsStateless(tempVar6);

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
            getKernel_path().ValidateEntity(new IValidation[] { new NoTrimmingWhitespacesValidation() });
            getInitrd_path().ValidateEntity(new IValidation[] { new NoTrimmingWhitespacesValidation() });
            getKernel_parameters().ValidateEntity(new IValidation[] { new NoTrimmingWhitespacesValidation() });

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

}
