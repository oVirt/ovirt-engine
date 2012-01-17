package org.ovirt.engine.core.common.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.Serializable;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RunVmParams")
public class RunVmParams extends VmOperationParameterBase implements INotifyPropertyChanged, Serializable {
    private static final long serialVersionUID = 3311307677963231320L;

    @XmlElement(name = "BootSequence", nillable = true)
    private BootSequence _bootSequence;

    @XmlElement(name = "DiskPath")
    private String _diskPath;

    @XmlElement(name = "KvmEnable")
    private boolean _kvmEnable;

    @XmlElement(name = "RunAndPause")
    private boolean _runAndPause;

    @XmlElement(name = "UseVnc", nillable = true)
    private Boolean _useVnc;

    @XmlElement(name = "AcpiEnable")
    private boolean _acpiEnable;

    @XmlElement(name = "Win2kHackEnable", nillable = true)
    private Boolean _win2kHackEnable;

    @XmlElement(name = "CustomProperties", nillable = true)
    private String customProperties;

    @XmlElement(name = "FloppyPath")
    private String privateFloppyPath;

    @XmlElement(name = "ClientIp")
    private String privateClientIp;

    @XmlElement(name = "RequestingUser")
    private VdcUser privateRequestingUser;

    @XmlElement
    private boolean _internal;

    @XmlElement(name = "DestinationVdsId", nillable = true)
    private Guid _destinationVdsId;

    @XmlElement(name = "Reinitialize")
    private boolean privateReinitialize;

    @XmlElement(name = "RunAsStateless", nillable = true)
    private Boolean privateRunAsStateless;

    @XmlElement(name = "initrd_url", nillable = true)
    private String initrd_url;

    @XmlElement(name = "kernel_url", nillable = true)
    private String kernel_url;

    @XmlElement(name = "kernel_params", nillable = true)
    private String kernel_params;

    public RunVmParams() {
    }

    public RunVmParams(BootSequence bootSequence, Guid vmId, String diskPath, boolean kvmEnable,
            boolean runAndPause, Boolean useVnc, boolean acpiEnable, boolean win2kHackEnable,
            String clientIp, boolean runAsStateless) {
        super(vmId);
        _internal = false;
        _bootSequence = bootSequence;
        _diskPath = diskPath;
        _kvmEnable = kvmEnable;
        _runAndPause = runAndPause;
        _useVnc = useVnc;
        _acpiEnable = acpiEnable;
        _win2kHackEnable = win2kHackEnable;
        setClientIp(clientIp);
        setRunAsStateless(runAsStateless);
    }

    public RunVmParams(Guid vmId) {
        super(vmId);
        _internal = false;
        _diskPath = "";
        _kvmEnable = true;
        _acpiEnable = true;
    }

    public RunVmParams(Guid vmId, String clientIp) {
        this(vmId);
        setClientIp(clientIp);
    }

    public RunVmParams(Guid vmId, boolean isInternal) {
        this(vmId);
        _internal = isInternal;
    }

    public RunVmParams(Guid vmId, Guid powerClientId) {
        this(vmId);
        _destinationVdsId = powerClientId;
    }

    public Guid getDestinationVdsId() {
        return _destinationVdsId;
    }

    public void setDestinationVdsId(Guid value) {
        _destinationVdsId = value;
    }

    public boolean getIsInternal() {
        return _internal;
    }

    public void setIsInternal(boolean value) {
        _internal = value;
    }

    public BootSequence getBootSequence() {
        return _bootSequence;
    }

    public void setBootSequence(BootSequence value) {
        _bootSequence = value;
    }

    public String getFloppyPath() {
        return privateFloppyPath;
    }

    public void setFloppyPath(String value) {
        privateFloppyPath = value;
    }

    public String getDiskPath() {
        return _diskPath;
    }

    public void setDiskPath(String value) {
        _diskPath = value;
    }

    public boolean getKvmEnable() {
        return _kvmEnable;
    }

    public void setKvmEnable(boolean value) {
        _kvmEnable = value;
    }

    public boolean getRunAndPause() {
        return _runAndPause;
    }

    public void setRunAndPause(boolean value) {
        _runAndPause = value;
    }

    public Boolean getUseVnc() {
        return _useVnc;
    }

    public void setUseVnc(Boolean value) {
        _useVnc = value;
    }

    public boolean getAcpiEnable() {
        return _acpiEnable;
    }

    public void setAcpiEnable(boolean value) {
        _acpiEnable = value;
    }

    public Boolean getWin2kHackEnable() {
        return _win2kHackEnable;
    }

    public void setWin2kHackEnable(Boolean value) {
        _win2kHackEnable = value;
        OnPropertyChanged(new PropertyChangedEventArgs("Win2kHackEnable"));
    }

    public String getClientIp() {
        return privateClientIp;
    }

    public void setClientIp(String value) {
        privateClientIp = value;
    }

    public VdcUser getRequestingUser() {
        return privateRequestingUser;
    }

    public void setRequestingUser(VdcUser value) {
        privateRequestingUser = value;
    }

    public boolean getReinitialize() {
        return privateReinitialize;
    }

    public void setReinitialize(boolean value) {
        privateReinitialize = value;
    }

    public Boolean getRunAsStateless() {
        return privateRunAsStateless;
    }

    public void setRunAsStateless(Boolean value) {
        privateRunAsStateless = value;
    }

    protected void OnPropertyChanged(PropertyChangedEventArgs e) {
        // if (PropertyChanged != null)
        // {
        // PropertyChanged(this, e);
        // }
    }

    public String getinitrd_url() {
        return this.initrd_url;
    }

    public void setinitrd_url(String value) {
        this.initrd_url = value;
    }

    public String getkernel_url() {
        return this.kernel_url;
    }

    public void setkernel_url(String value) {
        this.kernel_url = value;
    }

    public String getkernel_params() {
        return this.kernel_params;
    }

    public void setkernel_params(String value) {
        this.kernel_params = value;
    }

    public String getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(String customProperties) {
        this.customProperties = customProperties;
    }

}
