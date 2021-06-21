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
import org.ovirt.engine.core.common.businessentities.network.CloudInitNetworkProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.HostAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.HostnameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IpAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.Ipv4AddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.Ipv6AddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.MatchFieldsValidator;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SubnetMaskValidation;
import org.ovirt.engine.ui.uicommonweb.validation.VmInitNetworkNameValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public class VmInitModel extends Model {

    private static final String dnsServerListMessage;
    private static final String newNetworkText;

    static {
        dnsServerListMessage = ConstantsManager.getInstance().getConstants().cloudInitDnsServerListMessage();
        newNetworkText = ""; //$NON-NLS-1$
    }

    private ListModel<Map.Entry<String, String>> windowsSysprepTimeZone;
    private EntityModel<Boolean> windowsSysprepTimeZoneEnabled;
    private EntityModel<String> windowsHostname;
    private EntityModel<String> sysprepOrgName;
    private ListModel<String> sysprepDomain;
    private EntityModel<String> hostname;
    private EntityModel<String> inputLocale;
    private EntityModel<String> uiLanguage;
    private EntityModel<String> systemLocale;
    private EntityModel<String> userLocale;
    private EntityModel<String> userName;
    private EntityModel<String> activeDirectoryOU;
    private EntityModel<String> customScript;
    private EntityModel<String> sysprepScript;
    private EntityModel<String> authorizedKeys;
    private EntityModel<Boolean> regenerateKeysEnabled;
    private EntityModel<Boolean> timeZoneEnabled;
    private ListModel<Map.Entry<String, String>> timeZoneList;
    private EntityModel<String> cloudInitRootPassword;
    private EntityModel<String> cloudInitRootPasswordVerification;
    private EntityModel<Boolean> cloudInitPasswordSet;
    private EntityModel<String> sysprepAdminPassword;
    private EntityModel<String> sysprepAdminPasswordVerification;
    private EntityModel<Boolean> sysprepPasswordSet;
    private EntityModel<Boolean> networkEnabled;
    private EntityModel<String> networkSelectedName;
    private ListModel<String> networkList;
    private UICommand addNetworkCommand;
    private UICommand removeNetworkCommand;
    private ListModel<Ipv4BootProtocol> ipv4BootProtocolList;
    private EntityModel<String> networkIpAddress;
    private EntityModel<String> networkNetmask;
    private EntityModel<String> networkGateway;
    private ListModel<Ipv6BootProtocol> ipv6BootProtocolList;
    private EntityModel<String> networkIpv6Address;
    private EntityModel<Integer> networkIpv6Prefix;
    private EntityModel<String> networkIpv6Gateway;
    private EntityModel<Boolean> networkStartOnBoot;
    private EntityModel<String> dnsServers;
    private EntityModel<String> dnsSearchDomains;
    private EntityModel<Boolean> attachmentEnabled;
    private EntityModel<? extends Object> attachmentSelectedPath;
    private ListModel<? extends Object> attachmentList;
    private UICommand addAttachmentCommand;
    private UICommand removeAttachmentCommand;
    private ListModel attachmentType;
    private EntityModel<? extends Object> attachmentContent;
    private ListModel<CloudInitNetworkProtocol> cloudInitProtocol;

    private boolean isWindowsOS = false;
    private SortedMap<String, VmInitNetwork> networkMap;
    private Set<String> startOnBootNetworkNames;
    private String lastSelectedNetworkName;
    private String currentDomain = null;
    /**
     * Do not automatically change guest's hostname when the user already did manually
     */
    private boolean canAutoSetHostname = true;
    private boolean disableOnHostnameChanged = false;

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
        setActiveDirectoryOU(new EntityModel<String>());

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

        setIpv6BootProtocolList(new ListModel<Ipv6BootProtocol>());
        setNetworkIpv6Address(new EntityModel<String>());
        setNetworkIpv6Prefix(new EntityModel<Integer>());
        setNetworkIpv6Gateway(new EntityModel<String>());

        setNetworkStartOnBoot(new EntityModel<Boolean>());

        setDnsServers(new EntityModel<String>());
        setDnsSearchDomains(new EntityModel<String>());

        setAddNetworkCommand(new UICommand("addNetwork", this)); //$NON-NLS-1$
        setRemoveNetworkCommand(new UICommand("removeNetwork", this)); //$NON-NLS-1$

        networkMap = new TreeMap<>();
        startOnBootNetworkNames = new HashSet<>();
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
        setCloudInitProtocolList(new ListModel<>());

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

        Map<String, String> timezones = AsyncDataProvider.getInstance().getTimezones(TimeZoneType.GENERAL_TIMEZONE);
        getTimeZoneList().setItems(timezones.entrySet());
        getTimeZoneList().setSelectedItem(Linq.firstOrNull(timezones.entrySet(),
                item -> item.getKey().equals("Etc/GMT"))); //$NON-NLS-1$

        Map<String, String> windowsTimezones = AsyncDataProvider.getInstance().getTimezones(TimeZoneType.WINDOWS_TIMEZONE);
        getWindowsSysprepTimeZone().setItems(windowsTimezones.entrySet());
        getWindowsSysprepTimeZone().setSelectedItem(Linq.firstOrNull(windowsTimezones.entrySet(),
                item -> item.getKey().equals("GMT Standard Time"))); //$NON-NLS-1$

        isWindowsOS = vm != null ? AsyncDataProvider.getInstance().isWindowsOsType(vm.getOsId()) : true;

        getIpv4BootProtocolList().setItems(Arrays.asList(Ipv4BootProtocol.values()));
        getIpv4BootProtocolList().setSelectedItem(Ipv4BootProtocol.NONE);
        // only add values which are supported by cloud-init. autoconf ('stateless address autconfiguration') is not supported by cloud-init 0.7.9
        getIpv6BootProtocolList().setItems(Arrays.asList(Ipv6BootProtocol.NONE, Ipv6BootProtocol.DHCP, Ipv6BootProtocol.STATIC_IP));
        getIpv6BootProtocolList().setSelectedItem(Ipv6BootProtocol.NONE);
        getCloudInitProtocolList().setItems(Arrays.asList(CloudInitNetworkProtocol.values()));
        getCloudInitProtocolList().setSelectedItem(CloudInitNetworkProtocol.OPENSTACK_METADATA);

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

            if (vmInit.getCloudInitNetworkProtocol() != null) {
                getCloudInitProtocolList().setSelectedItem(vmInit.getCloudInitNetworkProtocol());
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
        startOnBootNetworkNames = new HashSet<>();
        lastSelectedNetworkName = null;

        for (VmInitNetwork network : vmInit.getNetworks()) {
            if (network.getName() == null) {
                continue;
            }

            networkMap.put(network.getName(), network);
            if (network.getStartOnBoot() != null && network.getStartOnBoot()) {
                startOnBootNetworkNames.add(network.getName());
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
        specificTimeZoneModel.setSelectedItem(Linq.firstOrNull(timezones.entrySet(), item -> item.getKey().equals(tz)));
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
        getNetworkIpv6Address().setIsValid(true);
        getNetworkIpv6Prefix().setIsValid(true);
        getNetworkIpv6Gateway().setIsValid(true);
        boolean dnsIsValid = true;
        getDnsServers().setIsValid(true);
        getDnsSearchDomains().setIsValid(true);
        if (getNetworkEnabled().getEntity()) {
            saveNetworkFields();

            for (Map.Entry<String, VmInitNetwork> entry : networkMap.entrySet()) {
                String name = entry.getKey();
                VmInitNetwork vmInitNetwork = entry.getValue();

                if (vmInitNetwork.getBootProtocol() == Ipv4BootProtocol.STATIC_IP) {
                    if (!validateHidden(getNetworkList(), name, null,
                                    new IValidation[] { new VmInitNetworkNameValidation(), new NotEmptyValidation()})
                            || !validateHidden(getNetworkIpAddress(), vmInitNetwork.getIp(), null,
                                    new IValidation[] { new Ipv4AddressValidation() })
                            || !validateHidden(getNetworkNetmask(), vmInitNetwork.getNetmask(), null,
                                    new IValidation[] { new SubnetMaskValidation() })
                            || !validateHidden(getNetworkGateway(), vmInitNetwork.getGateway(), null,
                                    new IValidation[] { new Ipv4AddressValidation(true) })) {
                        getNetworkList().setSelectedItem(name);
                        networkIsValid = false;
                        break;
                    }
                }

                if (vmInitNetwork.getIpv6BootProtocol() == Ipv6BootProtocol.STATIC_IP) {
                    if (!validateHidden(getNetworkList(), name, null,
                                    new IValidation[] { new VmInitNetworkNameValidation(), new NotEmptyValidation()})
                            || !validateHidden(getNetworkIpv6Address(), vmInitNetwork.getIpv6Address(), null,
                                    new IValidation[] { new Ipv6AddressValidation() })
                            || !validateHidden(getNetworkIpv6Prefix(), vmInitNetwork.getIpv6Prefix(), null,
                                    new IValidation[] { new IntegerValidation(0, 128) })
                            || !validateHidden(getNetworkIpv6Gateway(), vmInitNetwork.getIpv6Gateway(), null,
                                    new IValidation[] { new Ipv6AddressValidation(true) })) {
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
                                new IValidation[] { new IpAddressValidation() })) {
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
    private <T> boolean validateHidden(Model entity, final T value, final String message, final IValidation[] validations) {
        EntityModel<T> tmp = new EntityModel<>(value);
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
            vmInit.setCloudInitNetworkProtocol(getCloudInitProtocolList().getSelectedItem());
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
                    VmInitNetwork vmInitNetwork = entry.getValue();
                    if (vmInitNetwork.getBootProtocol() != Ipv4BootProtocol.STATIC_IP) {
                        vmInitNetwork.setIp(null);
                        vmInitNetwork.setNetmask(null);
                        vmInitNetwork.setGateway(null);
                    }
                    if (vmInitNetwork.getIpv6BootProtocol() != Ipv6BootProtocol.STATIC_IP) {
                        vmInitNetwork.setIpv6Address(null);
                        vmInitNetwork.setIpv6Prefix(null);
                        vmInitNetwork.setIpv6Gateway(null);
                    }
                    vmInitNetwork.setStartOnBoot(startOnBootNetworkNames.contains(entry.getKey()));
                    vmInitNetwork.setName(entry.getKey());
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
        } else if (ev.matchesDefinition(HasEntity.entityChangedEventDefinition)) {
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
        } else if (command.equals(getRemoveNetworkCommand())) {
            removeNetwork();
        }
    }

    /**
     * hard-code startOnBoot to true and disable its modification, because cloud-init 0.7.9 does not support false
     */
    private void hardCodeStartOnBoot() {
        if (getNetworkStartOnBoot() == null) {
            setNetworkStartOnBoot(new EntityModel<>(true));
        } else {
            getNetworkStartOnBoot().setEntity(true);
        }
        getNetworkStartOnBoot().setIsChangeable(false);
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
            VmInitNetwork vmInitNetwork = networkMap.get(oldName);
            newName = newName.trim();
            if (newName.isEmpty() || networkMap.containsKey(newName)) {
                getNetworkSelectedName().setEntity(oldName);
            } else {
                networkMap.remove(oldName);
                networkMap.put(newName, vmInitNetwork);
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
            VmInitNetwork vmInitNetwork = networkMap.get(lastSelectedNetworkName);
            if (vmInitNetwork != null) {
                vmInitNetwork.setBootProtocol(getIpv4BootProtocolList().getSelectedItem());
                vmInitNetwork.setIp(getNetworkIpAddress().getEntity());
                vmInitNetwork.setNetmask(getNetworkNetmask().getEntity());
                vmInitNetwork.setGateway(getNetworkGateway().getEntity());
                vmInitNetwork.setIpv6BootProtocol(getIpv6BootProtocolList().getSelectedItem());
                vmInitNetwork.setIpv6Address(getNetworkIpv6Address().getEntity());
                vmInitNetwork.setIpv6Prefix(getNetworkIpv6Prefix().getEntity());
                vmInitNetwork.setIpv6Gateway(getNetworkIpv6Gateway().getEntity());
                if (getNetworkStartOnBoot().getEntity() != null && getNetworkStartOnBoot().getEntity()) {
                    startOnBootNetworkNames.add(lastSelectedNetworkName);
                } else {
                    startOnBootNetworkNames.remove(lastSelectedNetworkName);
                }
            }
        }
    }

    /* Update displayed network properties to reflect currently-selected item */
    private void updateNetworkDisplay() {
        String networkName = null;
        VmInitNetwork vmInitNetwork = null;
        if (getNetworkList().getSelectedItem() != null) {
            networkName = getNetworkList().getSelectedItem();
            vmInitNetwork = networkMap.get(networkName);
        }

        final Ipv4BootProtocol ipv4bootProtocol =
                vmInitNetwork == null || vmInitNetwork.getBootProtocol() == null
                        ? Ipv4BootProtocol.NONE
                        : vmInitNetwork.getBootProtocol();
        getIpv4BootProtocolList().setSelectedItem(ipv4bootProtocol);
        final Ipv6BootProtocol ipv6bootProtocol =
                vmInitNetwork == null || vmInitNetwork.getIpv6BootProtocol() == null
                        ? Ipv6BootProtocol.NONE
                        : vmInitNetwork.getIpv6BootProtocol();
        getIpv6BootProtocolList().setSelectedItem(ipv6bootProtocol);

        getNetworkIpAddress().setEntity(vmInitNetwork == null ? null : vmInitNetwork.getIp());
        getNetworkNetmask().setEntity(vmInitNetwork == null ? null : vmInitNetwork.getNetmask());
        getNetworkGateway().setEntity(vmInitNetwork == null ? null : vmInitNetwork.getGateway());

        getNetworkIpv6Address().setEntity(vmInitNetwork == null ? null : vmInitNetwork.getIpv6Address());
        getNetworkIpv6Prefix().setEntity(vmInitNetwork == null ? null : vmInitNetwork.getIpv6Prefix());
        getNetworkIpv6Gateway().setEntity(vmInitNetwork == null ? null : vmInitNetwork.getIpv6Gateway());

        hardCodeStartOnBoot();
    }

    public void osTypeChanged(Integer selectedItem) {
        isWindowsOS = AsyncDataProvider.getInstance().isWindowsOsType(selectedItem);
    }

    protected void updateSysprepDomain(String domain) {
        // Can't use domain since onSuccess is async call and it have
        // a different stack call.
        currentDomain = domain;
        AsyncDataProvider.getInstance().getAuthzExtensionsNames(new AsyncQuery<>(domains -> {
            getSysprepDomain().setItems(domains);
            if (!StringHelper.isNullOrEmpty(currentDomain)) {
                if (!domains.contains(currentDomain)) {
                    domains.add(currentDomain);
                }
                getSysprepDomain().setSelectedItem(currentDomain);
            }
        }));
    }

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

    public ListModel<Map.Entry<String, String>> getWindowsSysprepTimeZone() {
        return windowsSysprepTimeZone;
    }

    public void setWindowsSysprepTimeZone(ListModel<Map.Entry<String, String>> windowsSysprepTimeZone) {
        this.windowsSysprepTimeZone = windowsSysprepTimeZone;
    }

    public EntityModel<Boolean> getWindowsSysprepTimeZoneEnabled() {
        return windowsSysprepTimeZoneEnabled;
    }

    public void setWindowsSysprepTimeZoneEnabled(EntityModel<Boolean> windowsSysprepTimeZoneEnabled) {
        this.windowsSysprepTimeZoneEnabled = windowsSysprepTimeZoneEnabled;
    }

    public EntityModel<String> getWindowsHostname() {
        return windowsHostname;
    }

    private void setWindowsHostname(EntityModel<String> value) {
        windowsHostname = value;
    }

    public EntityModel<String> getSysprepOrgName() {
        return sysprepOrgName;
    }

    private void setSysprepOrgName(EntityModel<String> value) {
        sysprepOrgName = value;
    }

    public EntityModel<String> getHostname() {
        return hostname;
    }

    private void setHostname(EntityModel<String> value) {
        hostname = value;
    }

    public ListModel<String> getSysprepDomain() {
        return sysprepDomain;
    }

    private void setSysprepDomain(ListModel<String> value) {
        sysprepDomain = value;
    }

    public EntityModel<String> getInputLocale() {
        return inputLocale;
    }

    private void setInputLocale(EntityModel<String> value) {
        inputLocale = value;
    }

    public EntityModel<String> getUiLanguage() {
        return uiLanguage;
    }

    private void setUiLanguage(EntityModel<String> value) {
        uiLanguage = value;
    }

    public EntityModel<String> getSystemLocale() {
        return systemLocale;
    }

    private void setSystemLocale(EntityModel<String> value) {
        systemLocale = value;
    }

    public EntityModel<String> getUserLocale() {
        return userLocale;
    }

    private void setUserLocale(EntityModel<String> value) {
        userLocale = value;
    }

    public EntityModel<String> getUserName() {
        return userName;
    }

    private void setUserName(EntityModel<String> value) {
        userName = value;
    }

    public EntityModel<String> getActiveDirectoryOU() {
        return activeDirectoryOU;
    }

    private void setActiveDirectoryOU(EntityModel<String> value) {
        activeDirectoryOU = value;
    }

    public EntityModel<String> getCustomScript() {
        return customScript;
    }
    private void setCustomScript(EntityModel<String> value) {
        customScript = value;
    }

    public EntityModel<String> getSysprepScript() {
        return sysprepScript;
    }

    private void setSysprepScript(EntityModel<String> value) {
        sysprepScript = value;
    }

    public boolean getAuthorizedKeysEnabled() {
        return !StringHelper.isNullOrEmpty(getCloudInitRootPassword().getEntity());
    }

    public EntityModel<String> getAuthorizedKeys() {
        return authorizedKeys;
    }

    private void setAuthorizedKeys(EntityModel<String> value) {
        authorizedKeys = value;
    }

    public EntityModel<Boolean> getRegenerateKeysEnabled() {
        return regenerateKeysEnabled;
    }

    private void setRegenerateKeysEnabled(EntityModel<Boolean> value) {
        regenerateKeysEnabled = value;
    }

    public EntityModel<Boolean> getTimeZoneEnabled() {
        return timeZoneEnabled;
    }

    private void setTimeZoneEnabled(EntityModel<Boolean> value) {
        timeZoneEnabled = value;
    }

    public ListModel<Map.Entry<String, String>>  getTimeZoneList() {
        return timeZoneList;
    }

    private void setTimeZoneList(ListModel<Map.Entry<String, String>>  value) {
        timeZoneList = value;
    }

    public boolean getSysprepPasswordEnabled() {
        return !StringHelper.isNullOrEmpty(getSysprepAdminPassword().getEntity());
    }

    public boolean getRootPasswordEnabled() {
        return !StringHelper.isNullOrEmpty(getCloudInitRootPassword().getEntity());
    }

    public EntityModel<String> getCloudInitRootPassword() {
        return cloudInitRootPassword;
    }

    private void setCloudInitRootPassword(EntityModel<String> value) {
        cloudInitRootPassword = value;
    }

    public EntityModel<String> getCloudInitRootPasswordVerification() {
        return cloudInitRootPasswordVerification;
    }

    private void setCloudInitRootPasswordVerification(EntityModel<String> value) {
        cloudInitRootPasswordVerification = value;
    }

    public EntityModel<Boolean> getCloudInitPasswordSet() {
        return cloudInitPasswordSet;
    }

    private void setCloudInitPasswordSet(EntityModel<Boolean> value) {
        cloudInitPasswordSet = value;
    }

    public EntityModel<String> getSysprepAdminPassword() {
        return sysprepAdminPassword;
    }

    private void setSysprepAdminPassword(EntityModel<String> value) {
        sysprepAdminPassword = value;
    }

    public EntityModel<String> getSysprepAdminPasswordVerification() {
        return sysprepAdminPasswordVerification;
    }

    private void setSysprepAdminPasswordVerification(EntityModel<String> value) {
        sysprepAdminPasswordVerification = value;
    }

    public EntityModel<Boolean> getSysprepPasswordSet() {
        return sysprepPasswordSet;
    }

    private void setSysprepPasswordSet(EntityModel<Boolean> value) {
        sysprepPasswordSet = value;
    }

    public EntityModel<Boolean> getNetworkEnabled() {
        return networkEnabled;
    }

    private void setNetworkEnabled(EntityModel<Boolean> value) {
        networkEnabled = value;
    }

    public EntityModel<String> getNetworkSelectedName() {
        return networkSelectedName;
    }

    private void setNetworkSelectedName(EntityModel<String> value) {
        networkSelectedName = value;
    }

    public ListModel<String> getNetworkList() {
        return networkList;
    }

    private void setNetworkList(ListModel<String> value) {
        networkList = value;
    }

    public UICommand getAddNetworkCommand() {
        return addNetworkCommand;
    }

    private void setAddNetworkCommand(UICommand value) {
        addNetworkCommand = value;
    }

    public UICommand getRemoveNetworkCommand() {
        return removeNetworkCommand;
    }

    private void setRemoveNetworkCommand(UICommand value) {
        removeNetworkCommand = value;
    }

    public ListModel<Ipv4BootProtocol> getIpv4BootProtocolList() {
        return ipv4BootProtocolList;
    }

    private void setIpv4BootProtocolList(ListModel<Ipv4BootProtocol> value) {
        ipv4BootProtocolList = value;
    }

    public EntityModel<String> getNetworkIpAddress() {
        return networkIpAddress;
    }

    private void setNetworkIpAddress(EntityModel<String> value) {
        networkIpAddress = value;
    }

    public EntityModel<String> getNetworkNetmask() {
        return networkNetmask;
    }

    private void setNetworkNetmask(EntityModel<String> value) {
        networkNetmask = value;
    }

    public EntityModel<String> getNetworkGateway() {
        return networkGateway;
    }

    private void setNetworkGateway(EntityModel<String> value) {
        networkGateway = value;
    }

    public EntityModel<Boolean> getNetworkStartOnBoot() {
        return networkStartOnBoot;
    }

    private void setNetworkStartOnBoot(EntityModel<Boolean> value) {
        networkStartOnBoot = value;
    }

    public EntityModel<String> getDnsServers() {
        return dnsServers;
    }

    public void setDnsServers(EntityModel<String> dnsServers) {
        this.dnsServers = dnsServers;
    }

    public EntityModel<String> getDnsSearchDomains() {
        return dnsSearchDomains;
    }

    public void setDnsSearchDomains(EntityModel<String> dnsSearchDomains) {
        this.dnsSearchDomains = dnsSearchDomains;
    }


    public EntityModel<Boolean> getAttachmentEnabled() {
        return attachmentEnabled;
    }

    private void setAttachmentEnabled(EntityModel<Boolean> value) {
        attachmentEnabled = value;
    }

    public EntityModel getAttachmentSelectedPath() {
        return attachmentSelectedPath;
    }

    private void setAttachmentSelectedPath(EntityModel value) {
        attachmentSelectedPath = value;
    }

    public ListModel getAttachmentList() {

        return attachmentList;
    }

    private void setAttachmentList(ListModel value) {
        attachmentList = value;
    }

    public UICommand getAddAttachmentCommand() {
        return addAttachmentCommand;
    }

    private void setAddAttachmentCommand(UICommand value) {
        addAttachmentCommand = value;
    }

    public UICommand getRemoveAttachmentCommand() {
        return removeAttachmentCommand;
    }

    private void setRemoveAttachmentCommand(UICommand value) {
        removeAttachmentCommand = value;
    }

    public ListModel getAttachmentType() {
        return attachmentType;
    }

    private void setAttachmentType(ListModel value) {
        attachmentType = value;
    }

    public EntityModel getAttachmentContent() {
        return attachmentContent;
    }

    private void setAttachmentContent(EntityModel value) {
        attachmentContent = value;
    }

    public ListModel<Ipv6BootProtocol> getIpv6BootProtocolList() {
        return ipv6BootProtocolList;
    }

    public void setIpv6BootProtocolList(ListModel<Ipv6BootProtocol> ipv6BootProtocolList) {
        this.ipv6BootProtocolList = ipv6BootProtocolList;
    }

    public EntityModel<String> getNetworkIpv6Address() {
        return networkIpv6Address;
    }

    public void setNetworkIpv6Address(EntityModel<String> networkIpv6Address) {
        this.networkIpv6Address = networkIpv6Address;
    }

    public EntityModel<Integer> getNetworkIpv6Prefix() {
        return networkIpv6Prefix;
    }

    public void setNetworkIpv6Prefix(EntityModel<Integer> networkIpv6Prefix) {
        this.networkIpv6Prefix = networkIpv6Prefix;
    }

    public EntityModel<String> getNetworkIpv6Gateway() {
        return networkIpv6Gateway;
    }

    public void setNetworkIpv6Gateway(EntityModel<String> networkIpv6Gateway) {
        this.networkIpv6Gateway = networkIpv6Gateway;
    }

    public SortedMap<String, VmInitNetwork> getNetworkMap() {
        return networkMap;
    }

    public void setNetworkMap(SortedMap<String, VmInitNetwork> networkMap) {
        this.networkMap = networkMap;
    }

    public ListModel<CloudInitNetworkProtocol> getCloudInitProtocolList() {
        return cloudInitProtocol;
    }

    public void setCloudInitProtocolList(ListModel<CloudInitNetworkProtocol> cloudInitProtocol) {
        this.cloudInitProtocol = cloudInitProtocol;
    }
}
