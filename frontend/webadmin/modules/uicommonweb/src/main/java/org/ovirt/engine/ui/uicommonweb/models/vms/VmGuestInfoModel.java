package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.OsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIMessages;
import org.ovirt.engine.ui.uicompat.external.StringUtils;
import com.google.gwt.i18n.client.NumberFormat;

public class VmGuestInfoModel extends EntityModel<VM> {

    private final static UIMessages messages = ConstantsManager.getInstance().getMessages();
    private String guestUserName;
    private OsType guestOsType;
    private ArchitectureType guestOsArch;
    private String guestOsCodename;
    private String guestOsDistribution;
    private String guestOsKernelVersion;
    private String guestOsVersion;
    private String guestOs;
    private String guestOsTimezoneName;
    private Integer guestOsTimezoneOffset;
    private String guestOsNamedVersion;
    private String guestOsTimezone;
    private String clientIp;
    private String consoleUserName;

    public VmGuestInfoModel() {
        setTitle(ConstantsManager.getInstance().getConstants().guestInformationTitle());
        setHelpTag(HelpTag.guest_info);
        setHashName("guest_info"); //$NON-NLS-1$

        guestOsType = OsType.Other;
        guestOsArch = ArchitectureType.undefined;
        guestOsCodename = "";
        guestOsDistribution = "";
        guestOsKernelVersion = "";
        guestOsVersion = "";
        guestOs = "";
        guestOsTimezoneName = "";
        guestOsTimezoneOffset = 0;
        guestOsNamedVersion = "";
        guestOsTimezone = "";
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            updateProperties();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        updateProperties();
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        if (!ObjectUtils.objectsEqual(this.clientIp, clientIp)) {
            this.clientIp = clientIp;
            onPropertyChanged(new PropertyChangedEventArgs("ClientIp")); //$NON-NLS-1$
        }
    }

    public String getConsoleUserName() {
        return consoleUserName;
    }

    public void setConsoleUserName(String consoleUserName) {
        if (!ObjectUtils.objectsEqual(this.consoleUserName, consoleUserName)) {
            this.consoleUserName = consoleUserName;
            onPropertyChanged(new PropertyChangedEventArgs("ConsoleUserName")); //$NON-NLS-1$
        }
    }

    private void updateProperties() {
        VM vm = getEntity();

        setClientIp(vm.getClientIp());
        setConsoleUserName(vm.getConsoleCurentUserName());
        setGuestUserName(vm.getGuestCurentUserName());
        setGuestOs(vm.getGuestOs());
        setGuestOsArch(vm.getGuestOsArch());
        setGuestOsCodename(vm.getGuestOsCodename());
        setGuestOsDistribution(vm.getGuestOsDistribution());
        setGuestOsKernelVersion(vm.getGuestOsKernelVersion());
        setGuestOsType(vm.getGuestOsType());
        setGuestOsVersion(vm.getGuestOsVersion());
        setGuestOsTimezoneName(vm.getGuestOsTimezoneName());
        setGuestOsTimezoneOffset(vm.getGuestOsTimezoneOffset());
        setGuestOsNamedVersion();

        String hours = NumberFormat.getFormat("00").format(guestOsTimezoneOffset / 60.); //$NON-NLS-1$
        String minutes = NumberFormat.getFormat("00").format(guestOsTimezoneOffset % 60); //$NON-NLS-1$
        if (guestOsTimezoneOffset >= 0) {
            guestOsTimezone = messages.positiveTimezoneOffset(guestOsTimezoneName, hours, minutes);
        } else {
            guestOsTimezone = messages.negativeTimezoneOffset(guestOsTimezoneName, hours, minutes);
        }
    }

    public String getGuestOs() {
        return guestOs;
    }

    public void setGuestOs(String guestOs) {
        if (!ObjectUtils.objectsEqual(this.guestOs, guestOs)) {
            this.guestOs = guestOs;
            onPropertyChanged(new PropertyChangedEventArgs("GuestOs")); //$NON-NLS-1$
        }
    }

    public String getGuestOsTimezone() {
        return guestOsTimezone;
    }

    public String getGuestUserName() {
        return guestUserName;
    }

    public void setGuestUserName(String guestUserName) {
        if (!ObjectUtils.objectsEqual(this.guestUserName, guestUserName)) {
            this.guestUserName = guestUserName;
            onPropertyChanged(new PropertyChangedEventArgs("GuestUserName")); //$NON-NLS-1$
        }
    }

    public String getGuestOsType() {
        return guestOsType.name();
    }

    public void setGuestOsType(OsType guestOsType) {
        if (!ObjectUtils.objectsEqual(this.guestOsType, guestOsType)) {
            this.guestOsType = guestOsType;
            onPropertyChanged(new PropertyChangedEventArgs("GuestOsType")); //$NON-NLS-1$
        }
    }

    public String getGuestOsArch() {
        return guestOsArch.name();
    }

    public void setGuestOsArch(ArchitectureType guestOsArch) {
        if (!ObjectUtils.objectsEqual(this.guestOsArch, guestOsArch)) {
            this.guestOsArch = guestOsArch;
            onPropertyChanged(new PropertyChangedEventArgs("GuestOsArch")); //$NON-NLS-1$
        }
    }

    public String getGuestOsCodename() {
        return guestOsCodename;
    }

    public void setGuestOsCodename(String guestOsCodename) {
        if (!ObjectUtils.objectsEqual(this.guestOsCodename, guestOsCodename)) {
            this.guestOsCodename = guestOsCodename;
            onPropertyChanged(new PropertyChangedEventArgs("GuestOsCodename")); //$NON-NLS-1$
        }
    }

    public String getGuestOsDistribution() {
        return guestOsDistribution;
    }

    public void setGuestOsDistribution(String guestOsDistribution) {
        if (!ObjectUtils.objectsEqual(this.guestOsDistribution, guestOsDistribution)) {
            this.guestOsDistribution = guestOsDistribution;
            onPropertyChanged(new PropertyChangedEventArgs("GuestOsDistribution")); //$NON-NLS-1$
        }
    }

    public String getGuestOsKernelVersion() {
        return guestOsKernelVersion;
    }

    public void setGuestOsKernelVersion(String guestOsKernelVersion) {
        if (!ObjectUtils.objectsEqual(this.guestOsKernelVersion, guestOsKernelVersion)) {
            this.guestOsKernelVersion = guestOsKernelVersion;
            onPropertyChanged(new PropertyChangedEventArgs("GuestOsKernelVersion")); //$NON-NLS-1$
        }
    }

    public String getGuestOsVersion() {
        return guestOsVersion;
    }

    public void setGuestOsVersion(String guestOsVersion) {
        if (!ObjectUtils.objectsEqual(this.guestOsVersion, guestOsVersion)) {
            this.guestOsVersion = guestOsVersion;
            onPropertyChanged(new PropertyChangedEventArgs("GuestOsVersion")); //$NON-NLS-1$
        }
    }

    public String getGuestOsTimezoneName() {
        return guestOsTimezoneName;
    }

    public void setGuestOsTimezoneName(String guestOsTimezoneName) {
        if (!ObjectUtils.objectsEqual(this.guestOsTimezoneName, guestOsTimezoneName)) {
            this.guestOsTimezoneName = guestOsTimezoneName;
            onPropertyChanged(new PropertyChangedEventArgs("TimezoneName")); //$NON-NLS-1$
        }
    }

    public Integer getGuestOsTimezoneOffset() {
        return guestOsTimezoneOffset;
    }

    public void setGuestOsTimezoneOffset(Integer guestOsTimezoneOffset) {
        if (!ObjectUtils.objectsEqual(this.guestOsTimezoneOffset, guestOsTimezoneOffset)) {
            this.guestOsTimezoneOffset = guestOsTimezoneOffset;
            onPropertyChanged(new PropertyChangedEventArgs("TimezoneOffset")); //$NON-NLS-1$
        }
    }

    public String getGuestOsNamedVersion() {
        return guestOsNamedVersion;
    }

    private void setGuestOsNamedVersion() {
        if (guestOsType == OsType.Linux) {
            String optional = ""; // $NON-NLS-1$
            if (!StringUtils.isEmpty(guestOsCodename)) {
                optional = messages.guestOSVersionOptional(guestOsCodename);
            }
            guestOsNamedVersion = messages.guestOSVersionLinux(guestOsDistribution, guestOsVersion, optional);
        } else if (guestOsType == OsType.Windows && guestOs.startsWith("Win ")) { //$NON-NLS-1$
            if (guestOs.startsWith("Win 20")) { //$NON-NLS-1$
                guestOsNamedVersion = messages.guestOSVersionWindowsServer(guestOs.substring(4), guestOsVersion);
            } else {
                guestOsNamedVersion = messages.guestOSVersionWindows(guestOs.substring(4), guestOsVersion);
            }
        }
    }
}
