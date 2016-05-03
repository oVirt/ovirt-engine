package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmInitNetwork;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.IPredicate;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.HostAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.HostnameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.Ipv4AddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.MatchFieldsValidator;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SubnetMaskValidation;
import org.ovirt.engine.ui.uicommonweb.validation.VmInitNetworkNameValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public class VmInitModel extends Model {

    private boolean isWindowsOS = false;
    public boolean getHostnameEnabled() {
        if (isWindowsOS) {
            return !StringHelper.isNullOrEmpty(getWindowsHostname().getEntity());
        } else {
            return !StringHelper.isNullOrEmpty(getHostname().getEntity());
        }
    }
    public boolean getDomainEnabled() {
        if (isWindowsOS) {
            return !StringHelper.isNullOrEmpty(getSysprepDomain().getSelectedItem());
        }
        return false;
    }

    private ListModel<Map.Entry<String, String>> windowsSysprepTimeZone;

    public ListModel<Map.Entry<String, String>> getWindowsSysprepTimeZone() {
        return windowsSysprepTimeZone;
    }

    public void setWindowsSysprepTimeZone(ListModel<Map.Entry<String, String>> windowsSysprepTimeZone) {
        this.windowsSysprepTimeZone = windowsSysprepTimeZone;
    }

    private EntityModel<Boolean> windowsSysprepTimeZoneEnabled;

    public EntityModel<Boolean> getWindowsSysprepTimeZoneEnabled() {
        return windowsSysprepTimeZoneEnabled;
    }

    public void setWindowsSysprepTimeZoneEnabled(EntityModel<Boolean> windowsSysprepTimeZoneEnabled) {
        this.windowsSysprepTimeZoneEnabled = windowsSysprepTimeZoneEnabled;
    }

    private EntityModel<String> privateWindowsHostname;
    public EntityModel<String> getWindowsHostname() {
        return privateWindowsHostname;
    }
    private void setWindowsHostname(EntityModel<String> value) {
        privateWindowsHostname = value;
    }

    private EntityModel<String> privateSysprepOrgName;
    public EntityModel<String> getSysprepOrgName() {
        return privateSysprepOrgName;
    }
    private void setSysprepOrgName(EntityModel<String> value) {
        privateSysprepOrgName = value;
    }

    private EntityModel<String> privateHostname;
    public EntityModel<String> getHostname() {
        return privateHostname;
    }

    private void setHostname(EntityModel<String> value) {
        privateHostname = value;
    }

    private ListModel<String> privateSysprepDomain;
    public ListModel<String> getSysprepDomain() {
        return privateSysprepDomain;
    }

    private void setSysprepDomain(ListModel<String> value) {
        privateSysprepDomain = value;
    }

    private EntityModel<String> privateInputLocale;
    public EntityModel<String> getInputLocale() {
        return privateInputLocale;
    }
    private void setInputLocale(EntityModel<String> value) {
        privateInputLocale = value;
    }

    private EntityModel<String> privateUiLanguage;
    public EntityModel<String> getUiLanguage() {
        return privateUiLanguage;
    }
    private void setUiLanguage(EntityModel<String> value) {
        privateUiLanguage = value;
    }

    private EntityModel<String> privateSystemLocale;
    public EntityModel<String> getSystemLocale() {
        return privateSystemLocale;
    }
    private void setSystemLocale(EntityModel<String> value) {
        privateSystemLocale = value;
    }

    private EntityModel<String> privateUserLocale;
    public EntityModel<String> getUserLocale() {
        return privateUserLocale;
    }
    private void setUserLocale(EntityModel<String> value) {
        privateUserLocale = value;
    }

    private EntityModel<String> privateUserName;
    public EntityModel<String> getUserName() {
        return privateUserName;
    }
    private void setUserName(EntityModel<String> value) {
        privateUserName = value;
    }

    private EntityModel<String> privateActiveDirectoryOU;
    public EntityModel<String> getActiveDirectoryOU() {
        return privateActiveDirectoryOU;
    }

    private void setActiveDirectoryOU(EntityModel<String> value) {
        privateActiveDirectoryOU = value;
    }

    private EntityModel<String> privateCustomScript;
    public EntityModel<String> getCustomScript() {
        return privateCustomScript;
    }
    private void setCustomScript(EntityModel<String> value) {
        privateCustomScript = value;
    }

    private EntityModel<String> privateSysprepScript;
    public EntityModel<String> getSysprepScript() {
        return privateSysprepScript;
    }
    private void setSysprepScript(EntityModel<String> value) {
        privateSysprepScript = value;
    }

    public boolean getAuthorizedKeysEnabled() {
        return !StringHelper.isNullOrEmpty(getCloudInitRootPassword().getEntity());
    }

    private EntityModel<String> privateAuthorizedKeys;
    public EntityModel<String> getAuthorizedKeys() {
        return privateAuthorizedKeys;
    }
    private void setAuthorizedKeys(EntityModel<String> value) {
        privateAuthorizedKeys = value;
    }


    private EntityModel<Boolean> privateRegenerateKeysEnabled;
    public EntityModel<Boolean> getRegenerateKeysEnabled() {
        return privateRegenerateKeysEnabled;
    }
    private void setRegenerateKeysEnabled(EntityModel<Boolean> value) {
        privateRegenerateKeysEnabled = value;
    }


    private EntityModel<Boolean> privateTimeZoneEnabled;
    public EntityModel<Boolean> getTimeZoneEnabled() {
        return privateTimeZoneEnabled;
    }
    private void setTimeZoneEnabled(EntityModel<Boolean> value) {
        privateTimeZoneEnabled = value;
    }

    private ListModel<Map.Entry<String, String>>  privateTimeZoneList;
    public ListModel<Map.Entry<String, String>>  getTimeZoneList() {
        return privateTimeZoneList;
    }
    private void setTimeZoneList(ListModel<Map.Entry<String, String>>  value) {
        privateTimeZoneList = value;
    }

    public boolean getSysprepPasswordEnabled() {
        return !StringHelper.isNullOrEmpty(getSysprepAdminPassword().getEntity());
    }

    public boolean getRootPasswordEnabled() {
        return !StringHelper.isNullOrEmpty(getCloudInitRootPassword().getEntity());
    }

    private EntityModel<String> privateCloudInitRootPassword;
    public EntityModel<String> getCloudInitRootPassword() {
        return privateCloudInitRootPassword;
    }
    private void setCloudInitRootPassword(EntityModel<String> value) {
        privateCloudInitRootPassword = value;
    }

    private EntityModel<String> privateCloudInitRootPasswordVerification;
    public EntityModel<String> getCloudInitRootPasswordVerification() {
        return privateCloudInitRootPasswordVerification;
    }
    private void setCloudInitRootPasswordVerification(EntityModel<String> value) {
        privateCloudInitRootPasswordVerification = value;
    }

    private EntityModel<Boolean> privateCloudInitPasswordSet;
    public EntityModel<Boolean> getCloudInitPasswordSet() {
        return privateCloudInitPasswordSet;
    }

    private void setCloudInitPasswordSet(EntityModel<Boolean> value) {
        privateCloudInitPasswordSet = value;
    }


    private EntityModel<String> privateSysprepAdminPassword;
    public EntityModel<String> getSysprepAdminPassword() {
        return privateSysprepAdminPassword;
    }
    private void setSysprepAdminPassword(EntityModel<String> value) {
        privateSysprepAdminPassword = value;
    }


    private EntityModel<String> privateSysprepAdminPasswordVerification;
    public EntityModel<String> getSysprepAdminPasswordVerification() {
        return privateSysprepAdminPasswordVerification;
    }
    private void setSysprepAdminPasswordVerification(EntityModel<String> value) {
        privateSysprepAdminPasswordVerification = value;
    }

    private EntityModel<Boolean> privateSysprepPasswordSet;
    public EntityModel<Boolean> getSysprepPasswordSet() {
        return privateSysprepPasswordSet;
    }

    private void setSysprepPasswordSet(EntityModel<Boolean> value) {
        privateSysprepPasswordSet = value;
    }


    private EntityModel<Boolean> privateNetworkEnabled;
    public EntityModel<Boolean> getNetworkEnabled() {
        return privateNetworkEnabled;
    }
    private void setNetworkEnabled(EntityModel<Boolean> value) {
        privateNetworkEnabled = value;
    }

    private EntityModel<String> privateNetworkSelectedName;
    public EntityModel<String> getNetworkSelectedName() {
        return privateNetworkSelectedName;
    }
    private void setNetworkSelectedName(EntityModel<String> value) {
        privateNetworkSelectedName = value;
    }

    private ListModel<String> privateNetworkList;
    public ListModel<String> getNetworkList() {
        return privateNetworkList;
    }
    private void setNetworkList(ListModel<String> value) {
        privateNetworkList = value;
    }

    private UICommand addNetworkCommand;
    public UICommand getAddNetworkCommand() {
        return addNetworkCommand;
    }
    private void setAddNetworkCommand(UICommand value) {
        addNetworkCommand = value;
    }

    private UICommand removeNetworkCommand;
    public UICommand getRemoveNetworkCommand() {
        return removeNetworkCommand;
    }
    private void setRemoveNetworkCommand(UICommand value) {
        removeNetworkCommand = value;
    }

    private ListModel<Ipv4BootProtocol> ipv4BootProtocolList;

    public ListModel<Ipv4BootProtocol> getIpv4BootProtocolList() {
        return ipv4BootProtocolList;
    }

    private void setIpv4BootProtocolList(ListModel<Ipv4BootProtocol> value) {
        ipv4BootProtocolList = value;
    }

    private EntityModel<String> privateNetworkIpAddress;
    public EntityModel<String> getNetworkIpAddress() {
        return privateNetworkIpAddress;
    }
    private void setNetworkIpAddress(EntityModel<String> value) {
        privateNetworkIpAddress = value;
    }

    private EntityModel<String> privateNetworkNetmask;
    public EntityModel<String> getNetworkNetmask() {
        return privateNetworkNetmask;
    }
    private void setNetworkNetmask(EntityModel<String> value) {
        privateNetworkNetmask = value;
    }

    private EntityModel<String> privateNetworkGateway;
    public EntityModel<String> getNetworkGateway() {
        return privateNetworkGateway;
    }
    private void setNetworkGateway(EntityModel<String> value) {
        privateNetworkGateway = value;
    }

    private EntityModel<Boolean> privateNetworkStartOnBoot;
    public EntityModel<Boolean> getNetworkStartOnBoot() {
        return privateNetworkStartOnBoot;
    }
    private void setNetworkStartOnBoot(EntityModel<Boolean> value) {
        privateNetworkStartOnBoot = value;
    }

    private EntityModel<String> privateDnsServers;
    public EntityModel<String> getDnsServers() {
        return privateDnsServers;
    }
    public void setDnsServers(EntityModel<String> dnsServers) {
        privateDnsServers = dnsServers;
    }

    private EntityModel<String> privateDnsSearchDomains;
    public EntityModel<String> getDnsSearchDomains() {
        return privateDnsSearchDomains;
    }
    public void setDnsSearchDomains(EntityModel<String> dnsSearchDomains) {
        privateDnsSearchDomains = dnsSearchDomains;
    }


    private EntityModel<Boolean> privateAttachmentEnabled;
    public EntityModel<Boolean> getAttachmentEnabled() {
        return privateAttachmentEnabled;
    }
    private void setAttachmentEnabled(EntityModel<Boolean> value) {
        privateAttachmentEnabled = value;
    }

    private EntityModel<? extends Object> privateAttachmentSelectedPath;
    public EntityModel getAttachmentSelectedPath() {
        return privateAttachmentSelectedPath;
    }
    private void setAttachmentSelectedPath(EntityModel value) {
        privateAttachmentSelectedPath = value;
    }

    private ListModel<? extends Object> privateAttachmentList;
    public ListModel getAttachmentList() {

        return privateAttachmentList;
    }
    private void setAttachmentList(ListModel value) {
        privateAttachmentList = value;
    }

    private UICommand addAttachmentCommand;
    public UICommand getAddAttachmentCommand() {
        return addAttachmentCommand;
    }
    private void setAddAttachmentCommand(UICommand value) {
        addAttachmentCommand = value;
    }

    private UICommand removeAttachmentCommand;
    public UICommand getRemoveAttachmentCommand() {
        return removeAttachmentCommand;
    }
    private void setRemoveAttachmentCommand(UICommand value) {
        removeAttachmentCommand = value;
    }

    private ListModel privateAttachmentType;
    public ListModel getAttachmentType() {
        return privateAttachmentType;
    }
    private void setAttachmentType(ListModel value) {
        privateAttachmentType = value;
    }

    private EntityModel<? extends Object> privateAttachmentContent;
    public EntityModel getAttachmentContent() {
        return privateAttachmentContent;
    }
    private void setAttachmentContent(EntityModel value) {
        privateAttachmentContent = value;
    }


    private static final String rootPasswordMatchMessage;
    private static final String dnsServerListMessage;
    private static final String newNetworkText;
    private static final String newAttachmentText;
    private static final String base64Message;
    private static final String base64Regex;

    private SortedMap<String, VmInitNetwork> networkMap;
    private Set<String> networkStartOnBoot;
    private String lastSelectedNetworkName;

    static {
        rootPasswordMatchMessage = ConstantsManager.getInstance().getConstants().cloudInitRootPasswordMatchMessage();
        dnsServerListMessage = ConstantsManager.getInstance().getConstants().cloudInitDnsServerListMessage();
        newNetworkText = ""; //$NON-NLS-1$
        newAttachmentText = ConstantsManager.getInstance().getConstants().cloudInitNewAttachmentItem();
        base64Message = ConstantsManager.getInstance().getConstants().cloudInitBase64Message();
        base64Regex = "^[a-zA-Z0-9+/_\\r\\n-](=){0,2}$"; //$NON-NLS-1$
    }

    public VmInitModel() {

        setWindowsSysprepTimeZone(new ListModel<Map.Entry<String, String>>());
        setWindowsSysprepTimeZoneEnabled(new EntityModel<Boolean>());
        setWindowsHostname(new EntityModel<String>());
        setSysprepOrgName(new EntityModel<String>());
        setSysprepDomain(new ListModel<String>());
        setInputLocale(new EntityModel<String>());
        setUiLanguage(new EntityModel<String>());
        setSystemLocale(new EntityModel<String>());
        setUserLocale(new EntityModel<String>());
        setSysprepScript(new EntityModel<String>());
        setActiveDirectoryOU(new EntityModel());

        setHostname(new EntityModel<String>());
        setAuthorizedKeys(new EntityModel<String>());
        setCustomScript(new EntityModel<String>());
        setRegenerateKeysEnabled(new EntityModel<Boolean>());
        setTimeZoneEnabled(new EntityModel<Boolean>());
        setTimeZoneList(new ListModel<Map.Entry<String, String>>());
        setUserName(new EntityModel<String>());
        setCloudInitRootPassword(new EntityModel<String>());
        setCloudInitRootPasswordVerification(new EntityModel<String>());
        setCloudInitPasswordSet(new EntityModel<Boolean>());
        getCloudInitPasswordSet().getEntityChangedEvent().addListener(this);
        setSysprepAdminPassword(new EntityModel<String>());
        setSysprepAdminPasswordVerification(new EntityModel<String>());
        setSysprepPasswordSet(new EntityModel<Boolean>());
        getSysprepPasswordSet().getEntityChangedEvent().addListener(this);


        setNetworkEnabled(new EntityModel<Boolean>());
        setNetworkSelectedName(new EntityModel<String>());
        setNetworkList(new ListModel<String>());
        setIpv4BootProtocolList(new ListModel<Ipv4BootProtocol>());
        setNetworkIpAddress(new EntityModel<String>());
        setNetworkNetmask(new EntityModel<String>());
        setNetworkGateway(new EntityModel<String>());
        setNetworkStartOnBoot(new EntityModel<Boolean>());

        setDnsServers(new EntityModel<String>());
        setDnsSearchDomains(new EntityModel<String>());

        setAddNetworkCommand(new UICommand("addNetwork", this)); //$NON-NLS-1$
        setRemoveNetworkCommand(new UICommand("removeNetwork", this)); //$NON-NLS-1$

        networkMap = new TreeMap<>();
        networkStartOnBoot = new HashSet<>();
        lastSelectedNetworkName = null;
        getNetworkList().setItems(new ArrayList<>(networkMap.keySet()));
        getNetworkList().setSelectedItem(lastSelectedNetworkName);

        getNetworkList().getSelectedItemChangedEvent().addListener(this);
        getNetworkSelectedName().getEntityChangedEvent().addListener(this);

        setAttachmentEnabled(new EntityModel<Boolean>());
        setAttachmentSelectedPath(new EntityModel());
        setAttachmentList(new ListModel());
        setAttachmentType(new ListModel());
        setAttachmentContent(new EntityModel());

        setAddAttachmentCommand(new UICommand("addAttachment", this)); //$NON-NLS-1$
        setRemoveAttachmentCommand(new UICommand("removeAttachment", this)); //$NON-NLS-1$

        getAttachmentList().getSelectedItemChangedEvent().addListener(this);
        getAttachmentSelectedPath().getEntityChangedEvent().addListener(this);
    }

    public void init(final VmBase vm) {
        getWindowsSysprepTimeZoneEnabled().setEntity(false);
        getRegenerateKeysEnabled().setEntity(false);
        getTimeZoneEnabled().setEntity(false);
        getNetworkEnabled().setEntity(false);
        getAttachmentEnabled().setEntity(false);

        getCloudInitPasswordSet().setEntity(false);
        getCloudInitPasswordSet().setIsChangeable(false);
        getSysprepPasswordSet().setEntity(false);
        getSysprepPasswordSet().setIsChangeable(false);

        getWindowsHostname().setEntity("");
        getSysprepOrgName().setEntity("");
        getInputLocale().setEntity("");
        getUiLanguage().setEntity("");
        getSystemLocale().setEntity("");
        getUserLocale().setEntity("");
        getSysprepScript().setEntity("");
        getHostname().setEntity("");
        getUserName().setEntity("");
        getCloudInitRootPassword().setEntity("");
        getCloudInitRootPasswordVerification().setEntity("");
        getSysprepAdminPassword().setEntity("");
        getSysprepAdminPasswordVerification().setEntity("");
        getAuthorizedKeys().setEntity("");
        getRegenerateKeysEnabled().setEntity(false);
        getCustomScript().setEntity("");
        getActiveDirectoryOU().setEntity("");

        Map<String, String> timezones = TimeZoneType.GENERAL_TIMEZONE.getTimeZoneList();
        getTimeZoneList().setItems(timezones.entrySet());
        getTimeZoneList().setSelectedItem(Linq.firstOrNull(timezones.entrySet(),
                new IPredicate<Map.Entry<String, String>>() {
                    @Override
                    public boolean match(Map.Entry<String, String> item) {
                        return item.getValue().startsWith("(GMT) Greenwich"); //$NON-NLS-1$
                    }
                }));

        Map<String, String> windowsTimezones = TimeZoneType.WINDOWS_TIMEZONE.getTimeZoneList();
        getWindowsSysprepTimeZone().setItems(windowsTimezones.entrySet());
        getWindowsSysprepTimeZone().setSelectedItem(Linq.firstOrNull(windowsTimezones.entrySet(),
                new IPredicate<Map.Entry<String, String>>() {
                    @Override
                    public boolean match(Map.Entry<String, String> item) {
                        return item.getValue().startsWith("(GMT) Greenwich"); //$NON-NLS-1$
                    }
                }));

        isWindowsOS = vm != null ? AsyncDataProvider.getInstance().isWindowsOsType(vm.getOsId()) : true;

        getIpv4BootProtocolList().setItems(Arrays.asList(Ipv4BootProtocol.values()));
        getIpv4BootProtocolList().setSelectedItem(Ipv4BootProtocol.NONE);

        VmInit vmInit = (vm != null) ? vm.getVmInit() : null;
        if (vmInit != null) {
            if (!StringHelper.isNullOrEmpty(vmInit.getHostname())) {
                getHostname().setEntity(vmInit.getHostname());
                getWindowsHostname().setEntity(vmInit.getHostname());
            }
            if (!StringHelper.isNullOrEmpty(vmInit.getOrgName())) {
                getSysprepOrgName().setEntity(vmInit.getOrgName());
            }
            updateSysprepDomain(vmInit.getDomain());
            if (!StringHelper.isNullOrEmpty(vmInit.getInputLocale())) {
                getInputLocale().setEntity(vmInit.getInputLocale());
            }
            if (!StringHelper.isNullOrEmpty(vmInit.getUiLanguage())) {
                getUiLanguage().setEntity(vmInit.getUiLanguage());
            }
            if (!StringHelper.isNullOrEmpty(vmInit.getSystemLocale())) {
                getSystemLocale().setEntity(vmInit.getSystemLocale());
            }
            if (!StringHelper.isNullOrEmpty(vmInit.getUserLocale())) {
                getUserLocale().setEntity(vmInit.getUserLocale());
            }

            final String tz = vmInit.getTimeZone();
            if (!StringHelper.isNullOrEmpty(tz)) {
                if (AsyncDataProvider.getInstance().isWindowsOsType(vm.getOsId())) {
                    getWindowsSysprepTimeZoneEnabled().setEntity(true);
                    selectTimeZone(getWindowsSysprepTimeZone(), windowsTimezones, tz);
                } else {
                    getTimeZoneEnabled().setEntity(true);
                    selectTimeZone(getTimeZoneList(), timezones, tz);
                }
            }

            if (!StringHelper.isNullOrEmpty(vmInit.getUserName())) {
                getUserName().setEntity(vmInit.getUserName());
            }

            if (!StringHelper.isNullOrEmpty(vmInit.getRootPassword())) {
                getCloudInitRootPassword().setEntity(vmInit.getRootPassword());
                getCloudInitRootPasswordVerification().setEntity(vmInit.getRootPassword());
                getSysprepAdminPassword().setEntity(vmInit.getRootPassword());
                getSysprepAdminPasswordVerification().setEntity(vmInit.getRootPassword());
            }
            getCloudInitPasswordSet().setEntity(vmInit.isPasswordAlreadyStored());
            getCloudInitPasswordSet().setIsChangeable(vmInit.isPasswordAlreadyStored());
            getSysprepPasswordSet().setEntity(vmInit.isPasswordAlreadyStored());
            getSysprepPasswordSet().setIsChangeable(vmInit.isPasswordAlreadyStored());


            if (!StringHelper.isNullOrEmpty(vmInit.getAuthorizedKeys())) {
                getAuthorizedKeys().setEntity(vmInit.getAuthorizedKeys());
            }
            if (vmInit.getRegenerateKeys() != null) {
                getRegenerateKeysEnabled().setEntity(vmInit.getRegenerateKeys());
            }

            if (!StringHelper.isNullOrEmpty(vmInit.getCustomScript())) {
                if (isWindowsOS) {
                    getSysprepScript().setEntity(vmInit.getCustomScript());
                } else {
                    getCustomScript().setEntity(vmInit.getCustomScript());
                }
            }

            if (!StringHelper.isNullOrEmpty(vmInit.getActiveDirectoryOU())) {
                getActiveDirectoryOU().setEntity(vmInit.getActiveDirectoryOU());
            }

            initNetworks(vmInit);
        }

        addHostnameListeners();
    }

    private void initNetworks(VmInit vmInit) {
        if (vmInit.getDnsServers() != null) {
            getDnsServers().setEntity(vmInit.getDnsServers());
        }

        if (vmInit.getDnsSearch() != null) {
            getDnsSearchDomains().setEntity(vmInit.getDnsSearch());
        }

        if (vmInit.getNetworks() == null || vmInit.getNetworks().size() == 0) {
            return;
        }

        networkMap = new TreeMap<>();
        networkStartOnBoot = new HashSet<>();
        lastSelectedNetworkName = null;

        for (VmInitNetwork network : vmInit.getNetworks()) {
            if (network.getName() == null) {
                continue;
            }

            networkMap.put(network.getName(), network);
            if (network.getStartOnBoot() != null && network.getStartOnBoot()) {
                networkStartOnBoot.add(network.getName());
            }
        }

        if (networkMap.size() != 0) {
            lastSelectedNetworkName =  networkMap.keySet().iterator().next();
            getNetworkEnabled().setEntity(true);
        } else {
            getNetworkEnabled().setEntity(false);
        }

        // update silently - do not listen to events
        getNetworkList().getSelectedItemChangedEvent().removeListener(this);
        getNetworkList().setItems(new ArrayList<>(networkMap.keySet()));
        getNetworkList().setSelectedItem(lastSelectedNetworkName);
        getNetworkList().getSelectedItemChangedEvent().addListener(this);

        getNetworkSelectedName().getEntityChangedEvent().removeListener(this);
        getNetworkSelectedName().setEntity(getNetworkList().getSelectedItem());
        getNetworkSelectedName().getEntityChangedEvent().addListener(this);

        updateNetworkDisplay();
    }


    private void selectTimeZone(ListModel<Map.Entry<String, String>> specificTimeZoneModel, Map<String, String> timezones, final String tz) {
        specificTimeZoneModel.setSelectedItem(Linq.firstOrNull(timezones.entrySet(),
                new IPredicate<Map.Entry<String, String>>() {
                    @Override
                    public boolean match(Map.Entry<String, String> item) {
                        return item.getKey().equals(tz);
                    }
                }));
    }

    public boolean validate() {
        getHostname().setIsValid(true);
        getWindowsHostname().setIsValid(true);
        getSysprepAdminPassword().setIsValid(true);
        getSysprepAdminPasswordVerification().setIsValid(true);
        getCloudInitRootPassword().setIsValid(true);
        getCloudInitRootPasswordVerification().setIsValid(true);

        if (this.isWindowsOS) {
            if (getSysprepPasswordEnabled()) {
                getSysprepAdminPassword().validateEntity(new IValidation[] { new NotEmptyValidation(), new MatchFieldsValidator(getSysprepAdminPassword().getEntity(),
                        getSysprepAdminPasswordVerification().getEntity()) });
            }
        } else {
            if (getRootPasswordEnabled()) {
                getCloudInitRootPassword().validateEntity(new IValidation[] { new NotEmptyValidation(), new MatchFieldsValidator(getCloudInitRootPassword().getEntity(),
                        getCloudInitRootPasswordVerification().getEntity()) });
            }
        }

        if (getHostnameEnabled()) {
            if (this.isWindowsOS) {
                getWindowsHostname().validateEntity(new IValidation[] { new HostnameValidation(), new  LengthValidation(AsyncDataProvider.getInstance().getMaxVmNameLengthSysprep())});

            } else {
                getHostname().validateEntity(new IValidation[] { new HostnameValidation(), new  LengthValidation(AsyncDataProvider.getInstance().getMaxVmNameLength())});
            }
        }
        getSysprepDomain().setIsValid(true);
        if (getDomainEnabled()) {
            getSysprepDomain().setIsValid(new HostAddressValidation().validate(getSysprepDomain().getSelectedItem()).getSuccess());
        }

        getAuthorizedKeys().setIsValid(true);

        getTimeZoneList().setIsValid(true);
        if (getTimeZoneEnabled().getEntity()) {
            getTimeZoneList().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }



        boolean networkIsValid = true;
        getNetworkList().setIsValid(true);
        getNetworkIpAddress().setIsValid(true);
        getNetworkNetmask().setIsValid(true);
        getNetworkGateway().setIsValid(true);
        boolean dnsIsValid = true;
        getDnsServers().setIsValid(true);
        getDnsSearchDomains().setIsValid(true);
        if (getNetworkEnabled().getEntity()) {
            saveNetworkFields();

            for (Map.Entry<String, VmInitNetwork> entry : networkMap.entrySet()) {
                String name = entry.getKey();
                VmInitNetwork params = entry.getValue();

                if (params.getBootProtocol() == Ipv4BootProtocol.STATIC_IP) {
                    if (!validateHidden(getNetworkList(), name, null,
                                    new IValidation[] { new VmInitNetworkNameValidation(), new NotEmptyValidation()})
                            || !validateHidden(getNetworkIpAddress(), params.getIp(), null,
                                    new IValidation[] { new Ipv4AddressValidation() })
                            || !validateHidden(getNetworkNetmask(), params.getNetmask(), null,
                                    new IValidation[] { new SubnetMaskValidation() })
                            || !validateHidden(getNetworkGateway(), params.getGateway(), null,
                                    new IValidation[] { new Ipv4AddressValidation(true) })) {
                        getNetworkList().setSelectedItem(name);
                        networkIsValid = false;
                        break;
                    }
                }
            }

            if (!networkMap.isEmpty()) {
                if (getDnsServers().getEntity() != null) {
                    for (String server : tokenizeString(getDnsServers().getEntity())) {
                        if (!validateHidden(getDnsServers(), server, dnsServerListMessage,
                                new IValidation[] { new Ipv4AddressValidation() })) {
                            dnsIsValid = false;
                            break;
                        }
                    }
                }
                if (getDnsSearchDomains().getEntity() != null) {
                    for (String domain : tokenizeString(getDnsSearchDomains().getEntity())) {
                        if (!validateHidden(getDnsSearchDomains(), domain, null,
                                new IValidation[] { new HostnameValidation() })) {
                            dnsIsValid = false;
                            break;
                        }
                    }
                }
            }
        }

        return getHostname().getIsValid()
                && getWindowsHostname().getIsValid()
                && getSysprepDomain().getIsValid()
                && getAuthorizedKeys().getIsValid()
                && getTimeZoneList().getIsValid()
                && getCloudInitRootPassword().getIsValid()
                && getSysprepAdminPassword().getIsValid()
                && networkIsValid
                && dnsIsValid;
    }

    /* Validate a shared display element, without having to display each shared value */
    private boolean validateHidden(Model entity, final String value, final String message, final IValidation[] validations) {
        EntityModel<String> tmp = new EntityModel<>(value);
        tmp.setIsValid(true);
        tmp.validateEntity(validations);
        if (!tmp.getIsValid()) {
            if (message != null) {
                List<String> reasons = new ArrayList<>();
                reasons.add(message);
                entity.setInvalidityReasons(reasons);
            } else {
                entity.setInvalidityReasons(tmp.getInvalidityReasons());
            }
            entity.setIsValid(false);
        } else {
            entity.setIsValid(true);
        }
        return tmp.getIsValid();
    }

    public VmInit buildCloudInitParameters(UnitVmModel model) {
        if (model.getVmInitEnabled().getEntity() ||
                model.getSysprepEnabled().getEntity()) {
            return buildModelSpecificParameters(model.getIsWindowsOS());
        } else {
            return null;
        }
    }

    public VmInit buildCloudInitParameters(RunOnceModel model) {
        if (model.getIsSysprepEnabled().getEntity() ||
                model.getIsCloudInitEnabled().getEntity()) {
            return buildModelSpecificParameters(model.getIsWindowsOS());
        } else {
            return null;
        }
    }

    private VmInit buildModelSpecificParameters(boolean isWindows) {
        VmInit vmInit = buildCloudInitParameters();
        if (isWindows && getWindowsSysprepTimeZoneEnabled().getEntity()) {
            Map.Entry<String, String> entry = getWindowsSysprepTimeZone().getSelectedItem();
            vmInit.setTimeZone(entry.getKey());
        } else if (!isWindows && getTimeZoneEnabled().getEntity()) {
            Map.Entry<String, String> entry = getTimeZoneList().getSelectedItem();
            vmInit.setTimeZone(entry.getKey());
        }

        if (isWindows) {
            vmInit.setDomain(getSysprepDomain().getSelectedItem());
        }

        return vmInit;
    }

    public VmInit buildCloudInitParameters() {
        VmInit vmInit = new VmInit();

        if (getHostnameEnabled()) {
            vmInit.setHostname(isWindowsOS ? getWindowsHostname().getEntity() :
                                       getHostname().getEntity());
        }
        if (isWindowsOS) {
            vmInit.setInputLocale(getInputLocale().getEntity());
            vmInit.setUiLanguage(getUiLanguage().getEntity());
            vmInit.setSystemLocale(getSystemLocale().getEntity());
            vmInit.setUserLocale(getUserLocale().getEntity());
            vmInit.setCustomScript(getSysprepScript().getEntity());
            vmInit.setActiveDirectoryOU(getActiveDirectoryOU().getEntity());
            if (getSysprepPasswordEnabled()) {
                vmInit.setRootPassword(getSysprepAdminPassword().getEntity());
            }
            vmInit.setPasswordAlreadyStored(getSysprepPasswordSet().getEntity());
            vmInit.setOrgName(getSysprepOrgName().getEntity());
        } else {
            vmInit.setCustomScript(getCustomScript().getEntity());
            if (getRootPasswordEnabled()) {
                vmInit.setRootPassword(getCloudInitRootPassword().getEntity());
            }
            vmInit.setPasswordAlreadyStored(getCloudInitPasswordSet().getEntity());
        }

        vmInit.setUserName(getUserName().getEntity());

        vmInit.setAuthorizedKeys(getAuthorizedKeys().getEntity());
        if (getRegenerateKeysEnabled().getEntity()) {
            vmInit.setRegenerateKeys(Boolean.TRUE);
        }
        if (getNetworkEnabled().getEntity()) {
            saveNetworkFields();
            if (!networkMap.isEmpty()) {
                for (Map.Entry<String, VmInitNetwork> entry : networkMap.entrySet()) {
                    VmInitNetwork params = entry.getValue();
                    if (params.getBootProtocol() != Ipv4BootProtocol.STATIC_IP) {
                        params.setIp(null);
                        params.setNetmask(null);
                        params.setGateway(null);
                    }
                    params.setStartOnBoot(networkStartOnBoot.contains(entry.getKey()));
                    params.setName(entry.getKey());
                }
                vmInit.setNetworks(new ArrayList<>(networkMap.values()));
            }
        }
        vmInit.setDnsServers(getDnsServers().getEntity());
        vmInit.setDnsSearch(getDnsSearchDomains().getEntity());

        return vmInit;
    }

    private List<String> tokenizeString(String spaceDelimitedString) {
        if (spaceDelimitedString != null) {
            return new ArrayList<>(Arrays.asList(spaceDelimitedString.split("\\s+"))); //$NON-NLS-1$
        } else {
            return null;
        }
    }


    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition)) {
            if (sender == getNetworkList()) {
                networkList_SelectedItemChanged();
            }
        }
        else if (ev.matchesDefinition(HasEntity.entityChangedEventDefinition)) {
            if (sender == getNetworkSelectedName()) {
                networkSelectedName_SelectionChanged();
            } else if (sender == getCloudInitPasswordSet()) {
                cloudInitPasswordSetChanged();
            } else if (sender == getSysprepPasswordSet()) {
                sysprepPasswordSetChanged();
            } else if (sender == getHostname()) {
                disableAutoSetHostname();
            } else if (sender == getWindowsHostname()) {
                disableAutoSetHostname();
            }
        }
    }

    private void cloudInitPasswordSetChanged() {
        Boolean passwordChangable = !getCloudInitPasswordSet().getEntity();
        getCloudInitRootPassword().setIsChangeable(passwordChangable);
        getCloudInitRootPasswordVerification().setIsChangeable(passwordChangable);
    }

    private void sysprepPasswordSetChanged() {
        Boolean passwordChangable = !getSysprepPasswordSet().getEntity();
        getSysprepAdminPassword().setIsChangeable(passwordChangable);
        getSysprepAdminPasswordVerification().setIsChangeable(passwordChangable);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command.equals(getAddNetworkCommand())) {
            addNetwork();
        }
        else if (command.equals(getRemoveNetworkCommand())) {
            removeNetwork();
        }
    }


    /* === Network === */

    private void networkList_SelectedItemChanged() {
        saveNetworkFields();

        // The networkSelectedName EntityChangedEvent is really only
        // to catch user updates; don't trigger it programmatically.
        // Suppressing events locally works better than setEntity(, false).
        getNetworkSelectedName().getEntityChangedEvent().removeListener(this);
        getNetworkSelectedName().setEntity(getNetworkList().getSelectedItem());
        getNetworkSelectedName().getEntityChangedEvent().addListener(this);

        updateNetworkDisplay();
        // lastSelectedNetworkName can be used throughout update process to see prior name
        lastSelectedNetworkName = getNetworkList().getSelectedItem();
    }

    private void networkSelectedName_SelectionChanged() {
        String oldName = getNetworkList().getSelectedItem();
        String newName = getNetworkSelectedName().getEntity();

        if (oldName != null && newName != null && !newName.trim().equals(oldName)) {
            VmInitNetwork obj = networkMap.get(oldName);
            newName = newName.trim();
            if (newName.isEmpty() || networkMap.containsKey(newName)) {
                getNetworkSelectedName().setEntity(oldName);
            } else {
                networkMap.remove(oldName);
                networkMap.put(newName, obj);
                getNetworkList().setItems(new ArrayList<>(networkMap.keySet()));
                getNetworkList().setSelectedItem(newName);
            }
        }
    }

    private void addNetwork() {
        if (!networkMap.containsKey(newNetworkText)) {
            networkMap.put(newNetworkText, new VmInitNetwork());
            getNetworkList().setItems(new ArrayList<>(networkMap.keySet()));
        }
        getNetworkList().setSelectedItem(newNetworkText);
    }

    private void removeNetwork() {
        networkMap.remove(getNetworkList().getSelectedItem());
        getNetworkList().setItems(new ArrayList<>(networkMap.keySet()));
        getNetworkList().setSelectedItem(Linq.firstOrNull(networkMap.keySet()));
    }

    /* Save displayed network properties */
    private void saveNetworkFields() {
        if (lastSelectedNetworkName != null) {
            VmInitNetwork obj = networkMap.get(lastSelectedNetworkName);
            if (obj != null) {
                obj.setBootProtocol(getIpv4BootProtocolList().getSelectedItem());
                obj.setIp(getNetworkIpAddress().getEntity());
                obj.setNetmask(getNetworkNetmask().getEntity());
                obj.setGateway(getNetworkGateway().getEntity());
                if (getNetworkStartOnBoot().getEntity() != null && getNetworkStartOnBoot().getEntity()) {
                    networkStartOnBoot.add(lastSelectedNetworkName);
                } else {
                    networkStartOnBoot.remove(lastSelectedNetworkName);
                }
            }
        }
    }

    /* Update displayed network properties to reflect currently-selected item */
    private void updateNetworkDisplay() {
        String networkName = null;
        VmInitNetwork obj = null;
        if (getNetworkList().getSelectedItem() != null) {
            networkName = getNetworkList().getSelectedItem();
            obj = networkMap.get(networkName);
        }

        if (obj == null || obj.getBootProtocol() == null) {
            getIpv4BootProtocolList().setSelectedItem(Ipv4BootProtocol.NONE);
        } else {
            getIpv4BootProtocolList().setSelectedItem(obj.getBootProtocol());
        }

        getNetworkIpAddress().setEntity(obj == null ? null : obj.getIp());
        getNetworkNetmask().setEntity(obj == null ? null : obj.getNetmask());
        getNetworkGateway().setEntity(obj == null ? null : obj.getGateway());
        getNetworkStartOnBoot().setEntity(networkName == null ? null : networkStartOnBoot.contains(networkName));
    }

    public void osTypeChanged(Integer selectedItem) {
        isWindowsOS = AsyncDataProvider.getInstance().isWindowsOsType(selectedItem);
    }

    private String currentDomain = null;
    protected void updateSysprepDomain(String domain) {
        // Can't use domain since onSuccess is async call and it have
        // a different stack call.
        currentDomain = domain;
        AsyncDataProvider.getInstance().getAAAProfilesList(new AsyncQuery(this,
                                                                          new INewAsyncCallback() {
                                                                              @Override
                                                                              public void onSuccess(Object target, Object returnValue) {
                                                                                  @SuppressWarnings("unchecked")
                                                                                  List<String> domains = (List<String>) returnValue;
                                                                                  getSysprepDomain().setItems(domains);
                                                                                  if (!StringHelper.isNullOrEmpty(currentDomain)) {
                                                                                      if (!domains.contains(currentDomain)) {
                                                                                          domains.add(currentDomain);
                                                                                      }
                                                                                      getSysprepDomain().setSelectedItem(currentDomain);
                                                                                  }
                                                                              }
                                                                          }));
    }

    /**
     * Do not automatically change guest's hostname when the user already did manually
     */
    private boolean canAutoSetHostname = true;
    private boolean disableOnHostnameChanged = false;

    public void autoSetHostname(String hostName) {
        if (canAutoSetHostname) {
            disableOnHostnameChanged = true;
            getWindowsHostname().setEntity(hostName);
            getHostname().setEntity(hostName);
            disableOnHostnameChanged = false;
        }
    }

    public void disableAutoSetHostname() {
        if (!disableOnHostnameChanged) {
            canAutoSetHostname = false;
        }
    }

    private void addHostnameListeners() {
        getHostname().getEntityChangedEvent().addListener(this);
        getWindowsHostname().getEntityChangedEvent().addListener(this);
    }
}
