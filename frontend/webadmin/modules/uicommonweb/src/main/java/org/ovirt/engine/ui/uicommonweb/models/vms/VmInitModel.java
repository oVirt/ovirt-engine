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
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.IPredicate;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.HostnameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IpAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SubnetMaskValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public class VmInitModel extends Model {

    public boolean getHostnameEnabled() {
        return !StringHelper.isNullOrEmpty((String) getHostname().getEntity());
    }

    private ListModel windowsSysprepTimeZone;

    public ListModel getWindowsSysprepTimeZone() {
        return windowsSysprepTimeZone;
    }

    public void setWindowsSysprepTimeZone(ListModel windowsSysprepTimeZone) {
        this.windowsSysprepTimeZone = windowsSysprepTimeZone;
    }

    private EntityModel windowsSysprepTimeZoneEnabled;

    public EntityModel getWindowsSysprepTimeZoneEnabled() {
        return windowsSysprepTimeZoneEnabled;
    }

    public void setWindowsSysprepTimeZoneEnabled(EntityModel windowsSysprepTimeZoneEnabled) {
        this.windowsSysprepTimeZoneEnabled = windowsSysprepTimeZoneEnabled;
    }

    private EntityModel privateHostname;
    public EntityModel getHostname() {
        return privateHostname;
    }
    private void setHostname(EntityModel value) {
        privateHostname = value;
    }

    private EntityModel privateDomain;
    public EntityModel getDomain() {
        return privateDomain;
    }
    private void setDomain(EntityModel value) {
        privateDomain = value;
    }

    private EntityModel privateCustomScript;
    public EntityModel getCustomScript() {
        return privateCustomScript;
    }
    private void setCustomScript(EntityModel value) {
        privateCustomScript = value;
    }

    public boolean getAuthorizedKeysEnabled() {
        return !StringHelper.isNullOrEmpty((String) getRootPassword().getEntity());
    }

    private EntityModel privateAuthorizedKeys;
    public EntityModel getAuthorizedKeys() {
        return privateAuthorizedKeys;
    }
    private void setAuthorizedKeys(EntityModel value) {
        privateAuthorizedKeys = value;
    }


    private EntityModel privateRegenerateKeysEnabled;
    public EntityModel getRegenerateKeysEnabled() {
        return privateRegenerateKeysEnabled;
    }
    private void setRegenerateKeysEnabled(EntityModel value) {
        privateRegenerateKeysEnabled = value;
    }


    private EntityModel privateTimeZoneEnabled;
    public EntityModel getTimeZoneEnabled() {
        return privateTimeZoneEnabled;
    }
    private void setTimeZoneEnabled(EntityModel value) {
        privateTimeZoneEnabled = value;
    }

    private ListModel privateTimeZoneList;
    public ListModel getTimeZoneList() {
        return privateTimeZoneList;
    }
    private void setTimeZoneList(ListModel value) {
        privateTimeZoneList = value;
    }

    public boolean getRootPasswordEnabled() {
        return !StringHelper.isNullOrEmpty((String) getRootPassword().getEntity());
    }

    private EntityModel privateRootPassword;
    public EntityModel getRootPassword() {
        return privateRootPassword;
    }
    private void setRootPassword(EntityModel value) {
        privateRootPassword = value;
    }

    private EntityModel privateRootPasswordVerification;
    public EntityModel getRootPasswordVerification() {
        return privateRootPasswordVerification;
    }
    private void setRootPasswordVerification(EntityModel value) {
        privateRootPasswordVerification = value;
    }


    private EntityModel privateNetworkEnabled;
    public EntityModel getNetworkEnabled() {
        return privateNetworkEnabled;
    }
    private void setNetworkEnabled(EntityModel value) {
        privateNetworkEnabled = value;
    }

    private EntityModel privateNetworkSelectedName;
    public EntityModel getNetworkSelectedName() {
        return privateNetworkSelectedName;
    }
    private void setNetworkSelectedName(EntityModel value) {
        privateNetworkSelectedName = value;
    }

    private ListModel privateNetworkList;
    public ListModel getNetworkList() {
        return privateNetworkList;
    }
    private void setNetworkList(ListModel value) {
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

    private EntityModel privateNetworkDhcp;
    public EntityModel getNetworkDhcp() {
        return privateNetworkDhcp;
    }
    private void setNetworkDhcp(EntityModel value) {
        privateNetworkDhcp = value;
    }

    private EntityModel privateNetworkIpAddress;
    public EntityModel getNetworkIpAddress() {
        return privateNetworkIpAddress;
    }
    private void setNetworkIpAddress(EntityModel value) {
        privateNetworkIpAddress = value;
    }

    private EntityModel privateNetworkNetmask;
    public EntityModel getNetworkNetmask() {
        return privateNetworkNetmask;
    }
    private void setNetworkNetmask(EntityModel value) {
        privateNetworkNetmask = value;
    }

    private EntityModel privateNetworkGateway;
    public EntityModel getNetworkGateway() {
        return privateNetworkGateway;
    }
    private void setNetworkGateway(EntityModel value) {
        privateNetworkGateway = value;
    }

    private EntityModel privateNetworkStartOnBoot;
    public EntityModel getNetworkStartOnBoot() {
        return privateNetworkStartOnBoot;
    }
    private void setNetworkStartOnBoot(EntityModel value) {
        privateNetworkStartOnBoot = value;
    }

    private EntityModel privateDnsServers;
    public EntityModel getDnsServers() {
        return privateDnsServers;
    }
    public void setDnsServers(EntityModel dnsServers) {
        privateDnsServers = dnsServers;
    }

    private EntityModel privateDnsSearchDomains;
    public EntityModel getDnsSearchDomains() {
        return privateDnsSearchDomains;
    }
    public void setDnsSearchDomains(EntityModel dnsSearchDomains) {
        privateDnsSearchDomains = dnsSearchDomains;
    }


    private EntityModel privateAttachmentEnabled;
    public EntityModel getAttachmentEnabled() {
        return privateAttachmentEnabled;
    }
    private void setAttachmentEnabled(EntityModel value) {
        privateAttachmentEnabled = value;
    }

    private EntityModel privateAttachmentSelectedPath;
    public EntityModel getAttachmentSelectedPath() {
        return privateAttachmentSelectedPath;
    }
    private void setAttachmentSelectedPath(EntityModel value) {
        privateAttachmentSelectedPath = value;
    }

    private ListModel privateAttachmentList;
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

    private EntityModel privateAttachmentContent;
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
        newNetworkText = ConstantsManager.getInstance().getConstants().cloudInitNewNetworkItem();
        newAttachmentText = ConstantsManager.getInstance().getConstants().cloudInitNewAttachmentItem();
        base64Message = ConstantsManager.getInstance().getConstants().cloudInitBase64Message();
        base64Regex = "^[a-zA-Z0-9+/_\\r\\n-](=){0,2}$"; //$NON-NLS-1$
    }

    public VmInitModel() {

        setWindowsSysprepTimeZone(new ListModel());
        setWindowsSysprepTimeZoneEnabled(new EntityModel());

        setHostname(new EntityModel());
        setDomain(new EntityModel());
        setAuthorizedKeys(new EntityModel());
        setCustomScript(new EntityModel());
        setRegenerateKeysEnabled(new EntityModel());
        setTimeZoneEnabled(new EntityModel());
        setTimeZoneList(new ListModel());
        setRootPassword(new EntityModel());
        setRootPasswordVerification(new EntityModel());

        setNetworkEnabled(new EntityModel());
        setNetworkSelectedName(new EntityModel());
        setNetworkList(new ListModel());
        setNetworkDhcp(new EntityModel());
        setNetworkIpAddress(new EntityModel());
        setNetworkNetmask(new EntityModel());
        setNetworkGateway(new EntityModel());
        setNetworkStartOnBoot(new EntityModel());

        setDnsServers(new EntityModel());
        setDnsSearchDomains(new EntityModel());

        setAddNetworkCommand(new UICommand("addNetwork", this)); //$NON-NLS-1$
        setRemoveNetworkCommand(new UICommand("removeNetwork", this)); //$NON-NLS-1$

        networkMap = new TreeMap<String, VmInitNetwork>();
        networkStartOnBoot = new HashSet<String>();
        lastSelectedNetworkName = null;
        getNetworkList().setItems(new ArrayList<String>(networkMap.keySet()));
        getNetworkList().setSelectedItem(lastSelectedNetworkName);

        getNetworkList().getSelectedItemChangedEvent().addListener(this);
        getNetworkSelectedName().getEntityChangedEvent().addListener(this);

        setAttachmentEnabled(new EntityModel());
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

        getHostname().setEntity("");
        getDomain().setEntity("");
        getRootPassword().setEntity("");
        getRootPasswordVerification().setEntity("");
        getAuthorizedKeys().setEntity("");
        getRegenerateKeysEnabled().setEntity(false);
        getCustomScript().setEntity("");

        Map<String, String> timezones = TimeZoneType.GENERAL_TIMEZONE.getTimeZoneList();
        getTimeZoneList().setItems(timezones.entrySet());
        getTimeZoneList().setSelectedItem(Linq.firstOrDefault(timezones.entrySet(),
                new IPredicate<Map.Entry<String, String>>() {
                    @Override
                    public boolean match(Map.Entry<String, String> item) {
                        return item.getValue().startsWith("(GMT) Greenwich"); //$NON-NLS-1$
                    }
                }));

        Map<String, String> windowsTimezones = TimeZoneType.WINDOWS_TIMEZONE.getTimeZoneList();
        getWindowsSysprepTimeZone().setItems(windowsTimezones.entrySet());
        getWindowsSysprepTimeZone().setSelectedItem(Linq.firstOrDefault(windowsTimezones.entrySet(),
                new IPredicate<Map.Entry<String, String>>() {
                    @Override
                    public boolean match(Map.Entry<String, String> item) {
                        return item.getValue().startsWith("(GMT) Greenwich"); //$NON-NLS-1$
                    }
                }));

        // if not proven to be hidden, show it
        boolean isWindows = vm != null ? AsyncDataProvider.isWindowsOsType(vm.getOsId()) : true;
        getDomain().setIsAvailable(isWindows);

        VmInit vmInit = (vm != null) ? vm.getVmInit() : null;
        if (vmInit != null) {
            if (!StringHelper.isNullOrEmpty(vmInit.getHostname())) {
                getHostname().setEntity(vmInit.getHostname());
            }
            getDomain().setEntity(vmInit.getDomain());
            final String tz = vmInit.getTimeZone();
            if (!StringHelper.isNullOrEmpty(tz)) {
                if (AsyncDataProvider.isWindowsOsType(vm.getOsId())) {
                    getWindowsSysprepTimeZoneEnabled().setEntity(true);
                    selectTimeZone(getWindowsSysprepTimeZone(), windowsTimezones, tz);
                } else {
                    getTimeZoneEnabled().setEntity(true);
                    selectTimeZone(getTimeZoneList(), timezones, tz);
                }
            }

            if (!StringHelper.isNullOrEmpty(vmInit.getRootPassword())) {
                getRootPassword().setEntity(vmInit.getRootPassword());
                getRootPasswordVerification().setEntity(vmInit.getRootPassword());
            }
            if (!StringHelper.isNullOrEmpty(vmInit.getAuthorizedKeys())) {
                getAuthorizedKeys().setEntity(vmInit.getAuthorizedKeys());
            }
            if (vmInit.getRegenerateKeys() != null) {
                getRegenerateKeysEnabled().setEntity(vmInit.getRegenerateKeys());
            }

            if (!StringHelper.isNullOrEmpty(vmInit.getCustomScript())) {
                getCustomScript().setEntity(vmInit.getCustomScript());
            }

            initNetworks(vmInit);
        }
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

        networkMap = new TreeMap<String, VmInitNetwork>();
        networkStartOnBoot = new HashSet<String>();
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
        getNetworkList().setItems(new ArrayList<String>(networkMap.keySet()));
        getNetworkList().setSelectedItem(lastSelectedNetworkName);
        getNetworkList().getSelectedItemChangedEvent().addListener(this);

        getNetworkSelectedName().getEntityChangedEvent().removeListener(this);
        getNetworkSelectedName().setEntity(getNetworkList().getSelectedItem());
        getNetworkSelectedName().getEntityChangedEvent().addListener(this);

        updateNetworkDisplay();
    }


    private void selectTimeZone(ListModel specificTimeZoneModel, Map<String, String> timezones, final String tz) {
        specificTimeZoneModel.setSelectedItem(Linq.firstOrDefault(timezones.entrySet(),
                new IPredicate<Map.Entry<String, String>>() {
                    @Override
                    public boolean match(Map.Entry<String, String> item) {
                        return item.getKey().equals(tz);
                    }
                }));
    }

    public boolean validate() {
        getHostname().setIsValid(true);
        if (getHostnameEnabled()) {
            getHostname().validateEntity(new IValidation[] { new HostnameValidation() });
        }
        getDomain().setIsValid(true);

        getAuthorizedKeys().setIsValid(true);

        getTimeZoneList().setIsValid(true);
        if ((Boolean) getTimeZoneEnabled().getEntity()) {
            getTimeZoneList().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }

        getRootPassword().setIsValid(true);
        getRootPasswordVerification().setIsValid(true);
        if (getRootPasswordEnabled()) {
            getRootPassword().validateEntity(new IValidation[] { new NotEmptyValidation() });
            if (getRootPassword().getIsValid()) {
                if (!(getRootPassword().getEntity())
                        .equals(getRootPasswordVerification().getEntity())) {
                    ArrayList<String> reasons = new ArrayList<String>();
                    reasons.add(rootPasswordMatchMessage);
                    getRootPassword().setInvalidityReasons(reasons);
                    getRootPassword().setIsValid(false);
                }
            }
            if (!getRootPassword().getIsValid()) {
                getRootPasswordVerification().setInvalidityReasons(getRootPassword().getInvalidityReasons());
                getRootPasswordVerification().setIsValid(false);
            }
        }

        boolean networkIsValid = true;
        getNetworkList().setIsValid(true);
        getNetworkIpAddress().setIsValid(true);
        getNetworkNetmask().setIsValid(true);
        getNetworkGateway().setIsValid(true);
        boolean dnsIsValid = true;
        getDnsServers().setIsValid(true);
        getDnsSearchDomains().setIsValid(true);
        if ((Boolean) getNetworkEnabled().getEntity()) {
            saveNetworkFields();

            for (Map.Entry<String, VmInitNetwork> entry : networkMap.entrySet()) {
                String name = entry.getKey();
                VmInitNetwork params = entry.getValue();

                if (params.getBootProtocol() != NetworkBootProtocol.DHCP) {
                    if (!validateHidden(getNetworkList(), name, null,
                                    new IValidation[] { new AsciiNameValidation() })
                            || !validateHidden(getNetworkIpAddress(), params.getIp(), null,
                                    new IValidation[] { new IpAddressValidation() })
                            || !validateHidden(getNetworkNetmask(), params.getNetmask(), null,
                                    new IValidation[] { new SubnetMaskValidation() })
                            || !validateHidden(getNetworkGateway(), params.getGateway(), null,
                                    new IValidation[] { new IpAddressValidation() })) {
                        getNetworkList().setSelectedItem(name);
                        networkIsValid = false;
                        break;
                    }
                }
            }

            if (!networkMap.isEmpty()) {
                if (getDnsServers().getEntity() != null) {
                    for (String server : tokenizeString((String) getDnsServers().getEntity())) {
                        if (!validateHidden(getDnsServers(), server, dnsServerListMessage,
                                new IValidation[] { new IpAddressValidation() })) {
                            dnsIsValid = false;
                            break;
                        }
                    }
                }
                if (getDnsSearchDomains().getEntity() != null) {
                    for (String domain : tokenizeString((String) getDnsSearchDomains().getEntity())) {
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
                && getDomain().getIsValid()
                && getAuthorizedKeys().getIsValid()
                && getTimeZoneList().getIsValid()
                && getRootPassword().getIsValid()
                && networkIsValid
                && dnsIsValid;
    }

    /* Validate a shared display element, without having to display each shared value */
    private boolean validateHidden(EntityModel entity, final Object value, final String message, final IValidation[] validations) {
        EntityModel tmp = new EntityModel(value);
        tmp.setIsValid(true);
        tmp.validateEntity(validations);
        if (!tmp.getIsValid()) {
            if (message != null) {
                List<String> reasons = new ArrayList<String>();
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
        if (model.getVmInitEnabled().getEntity()) {
            return buildModelSpecificParameters(model.getIsWindowsOS(), model.getDomain().getEntity());
        } else {
            return null;
        }
    }

    public VmInit buildCloudInitParameters(RunOnceModel model) {
        if ((Boolean) model.getIsSysprepEnabled().getEntity() ||
                (Boolean) model.getIsCloudInitEnabled().getEntity()) {
            return buildModelSpecificParameters(model.getIsWindowsOS(), (String) model.getSysPrepSelectedDomainName().getEntity());
        } else {
            return null;
        }
    }

    private VmInit buildModelSpecificParameters(boolean isWindows, String domainFromModel) {
        VmInit vmInit = buildCloudInitParameters();
        if (isWindows && (Boolean) getWindowsSysprepTimeZoneEnabled().getEntity()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) getWindowsSysprepTimeZone().getSelectedItem();
            vmInit.setTimeZone(entry.getKey());
        } else if (!isWindows && (Boolean) getTimeZoneEnabled().getEntity()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) getTimeZoneList().getSelectedItem();
            vmInit.setTimeZone(entry.getKey());
        }

        if (isWindows) {
            vmInit.setDomain(domainFromModel);
        } else {
            vmInit.setDomain((String) getDomain().getEntity());
        }

        return vmInit;
    }

    public VmInit buildCloudInitParameters() {
        VmInit vmInit = new VmInit();

        if (getHostnameEnabled()) {
            vmInit.setHostname((String) getHostname().getEntity());
        }

        if (getRootPasswordEnabled()) {
            vmInit.setRootPassword((String) getRootPassword().getEntity());
        }
        if (getAuthorizedKeysEnabled()) {
            vmInit.setAuthorizedKeys((String) getAuthorizedKeys().getEntity());
        }
        if ((Boolean) getRegenerateKeysEnabled().getEntity()) {
            vmInit.setRegenerateKeys(Boolean.TRUE);
        }
        if ((Boolean) getNetworkEnabled().getEntity()) {
            saveNetworkFields();
            if (!networkMap.isEmpty()) {
                for (Map.Entry<String, VmInitNetwork> entry : networkMap.entrySet()) {
                    VmInitNetwork params = entry.getValue();
                    if (params.getBootProtocol() == NetworkBootProtocol.DHCP) {
                        params.setIp(null);
                        params.setNetmask(null);
                        params.setGateway(null);
                    }
                    params.setStartOnBoot(networkStartOnBoot.contains(entry.getKey()));
                    params.setName(entry.getKey());
                }
                vmInit.setNetworks(new ArrayList(networkMap.values()));
            }
        }
        vmInit.setDnsServers((String) getDnsServers().getEntity());
        vmInit.setDnsSearch((String) getDnsSearchDomains().getEntity());
        vmInit.setCustomScript((String) getCustomScript().getEntity());

        return vmInit;
    }

    private List<String> tokenizeString(String spaceDelimitedString) {
        if (spaceDelimitedString != null) {
            return new ArrayList<String>(Arrays.asList(spaceDelimitedString.split("\\s+"))); //$NON-NLS-1$
        } else {
            return null;
        }
    }


    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition)) {
            if (sender == getNetworkList()) {
                networkList_SelectedItemChanged();
            }
        }
        else if (ev.matchesDefinition(EntityModel.entityChangedEventDefinition)) {
            if (sender == getNetworkSelectedName()) {
                networkSelectedName_SelectionChanged();
            }
        }
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
        lastSelectedNetworkName = (String) getNetworkList().getSelectedItem();
    }

    private void networkSelectedName_SelectionChanged() {
        String oldName = (String) getNetworkList().getSelectedItem();
        String newName = (String) getNetworkSelectedName().getEntity();

        if (oldName != null && newName != null && !newName.trim().equals(oldName)) {
            VmInitNetwork obj = networkMap.get(oldName);
            newName = newName.trim();
            if (newName.isEmpty() || networkMap.containsKey(newName)) {
                getNetworkSelectedName().setEntity(oldName);
            } else {
                networkMap.remove(oldName);
                networkMap.put(newName, obj);
                getNetworkList().setItems(new ArrayList<String>(networkMap.keySet()));
                getNetworkList().setSelectedItem(newName);
            }
        }
    }

    private void addNetwork() {
        if (!networkMap.containsKey(newNetworkText)) {
            networkMap.put(newNetworkText, new VmInitNetwork());
            getNetworkList().setItems(new ArrayList<String>(networkMap.keySet()));
        }
        getNetworkList().setSelectedItem(newNetworkText);
    }

    private void removeNetwork() {
        networkMap.remove((String) getNetworkList().getSelectedItem());
        getNetworkList().setItems(new ArrayList<String>(networkMap.keySet()));
        getNetworkList().setSelectedItem(Linq.firstOrDefault(networkMap.keySet()));
    }

    /* Save displayed network properties */
    private void saveNetworkFields() {
        if (lastSelectedNetworkName != null) {
            VmInitNetwork obj = networkMap.get(lastSelectedNetworkName);
            if (obj != null) {
                obj.setBootProtocol((getNetworkDhcp().getEntity() != null && (Boolean) getNetworkDhcp().getEntity())
                                    ? NetworkBootProtocol.DHCP : NetworkBootProtocol.NONE);
                obj.setIp((String) getNetworkIpAddress().getEntity());
                obj.setNetmask((String) getNetworkNetmask().getEntity());
                obj.setGateway((String) getNetworkGateway().getEntity());
                if (getNetworkStartOnBoot().getEntity() != null && (Boolean) getNetworkStartOnBoot().getEntity()) {
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
            networkName = (String) getNetworkList().getSelectedItem();
            obj = networkMap.get(networkName);
        }
        getNetworkDhcp().setEntity(obj == null ? null : obj.getBootProtocol() == NetworkBootProtocol.DHCP);
        getNetworkIpAddress().setEntity(obj == null ? null : obj.getIp());
        getNetworkNetmask().setEntity(obj == null ? null : obj.getNetmask());
        getNetworkGateway().setEntity(obj == null ? null : obj.getGateway());
        getNetworkStartOnBoot().setEntity(networkName == null ? null : networkStartOnBoot.contains(networkName));
    }

    public void osTypeChanged(Integer selectedItem) {
        getDomain().setIsAvailable(selectedItem != null && AsyncDataProvider.isWindowsOsType(selectedItem));
    }
}
