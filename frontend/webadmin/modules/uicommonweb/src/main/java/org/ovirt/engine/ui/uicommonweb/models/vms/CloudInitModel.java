package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.core.common.action.CloudInitParameters;
import org.ovirt.engine.core.common.action.CloudInitParameters.Attachment;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
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
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SubnetMaskValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public class CloudInitModel extends Model {

    private EntityModel privateHostnameEnabled;
    public EntityModel getHostnameEnabled() {
        return privateHostnameEnabled;
    }
    private void setHostnameEnabled(EntityModel value) {
        privateHostnameEnabled = value;
    }

    private EntityModel privateHostname;
    public EntityModel getHostname() {
        return privateHostname;
    }
    private void setHostname(EntityModel value) {
        privateHostname = value;
    }


    private EntityModel privateAuthorizedKeysEnabled;
    public EntityModel getAuthorizedKeysEnabled() {
        return privateAuthorizedKeysEnabled;
    }
    private void setAuthorizedKeysEnabled(EntityModel value) {
        privateAuthorizedKeysEnabled = value;
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


    private EntityModel privateRootPasswordEnabled;
    public EntityModel getRootPasswordEnabled() {
        return privateRootPasswordEnabled;
    }
    private void setRootPasswordEnabled(EntityModel value) {
        privateRootPasswordEnabled = value;
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

    private SortedMap<String, VdsNetworkInterface> networkMap;
    private Set<String> networkStartOnBoot;
    private String lastSelectedNetworkName;

    private static Map<Attachment.AttachmentType, String> attachmentTypes;
    private SortedMap<String, Attachment> attachmentMap;
    private String lastSelectedAttachmentPath;


    static {
        rootPasswordMatchMessage = ConstantsManager.getInstance().getConstants().cloudInitRootPasswordMatchMessage();
        dnsServerListMessage = ConstantsManager.getInstance().getConstants().cloudInitDnsServerListMessage();
        newNetworkText = ConstantsManager.getInstance().getConstants().cloudInitNewNetworkItem();
        newAttachmentText = ConstantsManager.getInstance().getConstants().cloudInitNewAttachmentItem();
        base64Message = ConstantsManager.getInstance().getConstants().cloudInitBase64Message();
        base64Regex = "^[a-zA-Z0-9+/_\\r\\n-](=){0,2}$"; //$NON-NLS-1$

        initAttachmentTypes();
    }

    private static void initAttachmentTypes() {
        attachmentTypes = new LinkedHashMap<Attachment.AttachmentType, String>();
        attachmentTypes.put(Attachment.AttachmentType.PLAINTEXT,
                ConstantsManager.getInstance().getConstants().cloudInitAttachmentTypePlainText());
        attachmentTypes.put(Attachment.AttachmentType.BASE64,
                ConstantsManager.getInstance().getConstants().cloudInitAttachmentTypeBase64());
    }

    public CloudInitModel() {
        setHostnameEnabled(new EntityModel());
        setHostname(new EntityModel());
        setAuthorizedKeysEnabled(new EntityModel());
        setAuthorizedKeys(new EntityModel());
        setRegenerateKeysEnabled(new EntityModel());
        setTimeZoneEnabled(new EntityModel());
        setTimeZoneList(new ListModel());
        setRootPasswordEnabled(new EntityModel());
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

        networkMap = new TreeMap<String, VdsNetworkInterface>();
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

        attachmentMap = new TreeMap<String, Attachment>();
        lastSelectedAttachmentPath = null;
        getAttachmentList().setItems(new ArrayList<String>(attachmentMap.keySet()));
        getAttachmentList().setSelectedItem(lastSelectedAttachmentPath);

        getAttachmentList().getSelectedItemChangedEvent().addListener(this);
        getAttachmentSelectedPath().getEntityChangedEvent().addListener(this);
    }

    public void init(final VM vm, final CloudInitParameters ciParams) {
        // if ciParams is null, initialize with default options
        // TODO if not null, set fields from passed-in data (for persisted data, if/when supported)
        getHostnameEnabled().setEntity(false);
        getHostname().setEntity(vm.getName());
        getAuthorizedKeysEnabled().setEntity(false);
        getRegenerateKeysEnabled().setEntity(false);
        getTimeZoneEnabled().setEntity(false);
        getRootPasswordEnabled().setEntity(false);
        getNetworkEnabled().setEntity(false);
        getAttachmentEnabled().setEntity(false);

        AsyncDataProvider.getTimeZoneList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                @SuppressWarnings("unchecked")
                Map<String, String> timezones = (Map<String, String>) returnValue;
                getTimeZoneList().setItems(timezones.entrySet());

                getTimeZoneList().setSelectedItem(Linq.firstOrDefault(timezones.entrySet(),
                        new IPredicate<Map.Entry<String, String>>() {
                            @Override
                            public boolean match(Map.Entry<String, String> item) {
                                return item.getValue().startsWith("(GMT) Greenwich"); //$NON-NLS-1$
                            }
                        }));
            }
        }), TimeZoneType.GENERAL_TIMEZONE);

        getAttachmentType().setItems(attachmentTypes.entrySet());
        getAttachmentType().setSelectedItem(Linq.firstOrDefault(attachmentTypes.entrySet()));
    }

    public boolean validate() {
        getHostname().setIsValid(true);
        if ((Boolean) getHostnameEnabled().getEntity()) {
            getHostname().validateEntity(new IValidation[] { new HostnameValidation() });
        }

        getAuthorizedKeys().setIsValid(true);
        if ((Boolean) getAuthorizedKeysEnabled().getEntity()) {
            getAuthorizedKeys().validateEntity(new IValidation[] { new NotEmptyValidation() });
        }

        getTimeZoneList().setIsValid(true);
        if ((Boolean) getTimeZoneEnabled().getEntity()) {
            getTimeZoneList().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }

        getRootPassword().setIsValid(true);
        getRootPasswordVerification().setIsValid(true);
        if ((Boolean) getRootPasswordEnabled().getEntity()) {
            getRootPassword().validateEntity(new IValidation[] { new NotEmptyValidation() });
            if (getRootPassword().getIsValid()) {
                if (!((String) getRootPassword().getEntity())
                        .equals((String) getRootPasswordVerification().getEntity())) {
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

            for (Map.Entry<String, VdsNetworkInterface> entry : networkMap.entrySet()) {
                String name = entry.getKey();
                VdsNetworkInterface params = entry.getValue();

                if (params.getBootProtocol() != NetworkBootProtocol.DHCP) {
                    if (!validateHidden(getNetworkList(), name, null,
                                    new IValidation[] { new AsciiNameValidation() })
                            || !validateHidden(getNetworkIpAddress(), params.getAddress(), null,
                                    new IValidation[] { new IpAddressValidation() })
                            || !validateHidden(getNetworkNetmask(), params.getSubnet(), null,
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

        boolean attachmentIsValid = true;
        getAttachmentContent().setIsValid(true);
        if ((Boolean) getAttachmentEnabled().getEntity()) {
            saveAttachmentFields();

            for (Map.Entry<String, Attachment> entry : attachmentMap.entrySet()) {
                String name = entry.getKey();
                Attachment params = entry.getValue();

                if (params.getAttachmentType() == Attachment.AttachmentType.BASE64) {
                    if (!validateHidden(getAttachmentContent(), params.getContent(), null,
                            new IValidation[] { new RegexValidation(base64Regex, base64Message)})) {
                        getAttachmentList().setSelectedItem(name);
                        attachmentIsValid = false;
                    }
                }
            }
        }

        return getHostname().getIsValid()
                && getAuthorizedKeys().getIsValid()
                && getTimeZoneList().getIsValid()
                && getRootPassword().getIsValid()
                && networkIsValid
                && dnsIsValid
                && attachmentIsValid;
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

    public CloudInitParameters buildCloudInitParameters() {
        CloudInitParameters ciParams = new CloudInitParameters();

        if ((Boolean) getHostnameEnabled().getEntity()) {
            ciParams.setHostname((String) getHostname().getEntity());
        }
        if ((Boolean) getAuthorizedKeysEnabled().getEntity()) {
            ciParams.setAuthorizedKeys((String) getHostname().getEntity());
        }
        if ((Boolean) getRegenerateKeysEnabled().getEntity()) {
            ciParams.setRegenerateKeys(Boolean.TRUE);
        }
        if ((Boolean) getTimeZoneEnabled().getEntity()) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, String> entry = (Map.Entry<String, String>) getTimeZoneList().getSelectedItem();
            ciParams.setTimeZone(entry.getKey());
        }
        if ((Boolean) getRootPasswordEnabled().getEntity()) {
            ciParams.setRootPassword((String) getRootPassword().getEntity());
        }

        if ((Boolean) getNetworkEnabled().getEntity()) {
            saveNetworkFields();
            if (!networkMap.isEmpty()) {
                for (Map.Entry<String, VdsNetworkInterface> entry : networkMap.entrySet()) {
                    // VdsNetworkInterface is used locally to store UI state, unset
                    // fields that shouldn't be passed down iff boot protocol is DHCP,
                    // then pass the sanitized object down via CloudInitParams
                    VdsNetworkInterface params = entry.getValue();
                    if (params.getBootProtocol() == NetworkBootProtocol.DHCP) {
                        params.setAddress(null);
                        params.setSubnet(null);
                        params.setGateway(null);
                    }
                }
                ciParams.setInterfaces(networkMap);
                ciParams.setStartOnBoot(new ArrayList<String>(networkStartOnBoot));

                ciParams.setDnsServers(tokenizeString((String) getDnsServers().getEntity()));
                ciParams.setDnsSearch(tokenizeString((String) getDnsSearchDomains().getEntity()));
            }
        }

        if ((Boolean) getAttachmentEnabled().getEntity()) {
            saveAttachmentFields();
            ciParams.setAttachments(attachmentMap);
        }

        return ciParams;
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
            else if (sender == getAttachmentList()) {
                attachmentList_SelectedItemChanged();
            }
        }
        else if (ev.matchesDefinition(EntityModel.EntityChangedEventDefinition)) {
            if (sender == getNetworkSelectedName()) {
                networkSelectedName_SelectionChanged();
            }
            else if (sender == getAttachmentSelectedPath()) {
                attachmentSelectedPath_SelectionChanged();
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
        else if (command.equals(getAddAttachmentCommand())) {
            addAttachment();
        }
        else if (command.equals(getRemoveAttachmentCommand())) {
            removeAttachment();
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
            VdsNetworkInterface obj = networkMap.get(oldName);
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
            networkMap.put(newNetworkText, new VdsNetworkInterface());
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
            VdsNetworkInterface obj = networkMap.get(lastSelectedNetworkName);
            if (obj != null) {
                obj.setBootProtocol((getNetworkDhcp().getEntity() != null && (Boolean) getNetworkDhcp().getEntity())
                                    ? NetworkBootProtocol.DHCP : NetworkBootProtocol.NONE);
                obj.setAddress((String) getNetworkIpAddress().getEntity());
                obj.setSubnet((String) getNetworkNetmask().getEntity());
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
        VdsNetworkInterface obj = null;
        if (getNetworkList().getSelectedItem() != null) {
            networkName = (String) getNetworkList().getSelectedItem();
            obj = networkMap.get(networkName);
        }
        getNetworkDhcp().setEntity(obj == null ? null : obj.getBootProtocol() == NetworkBootProtocol.DHCP);
        getNetworkIpAddress().setEntity(obj == null ? null : obj.getAddress());
        getNetworkNetmask().setEntity(obj == null ? null : obj.getSubnet());
        getNetworkGateway().setEntity(obj == null ? null : obj.getGateway());
        getNetworkStartOnBoot().setEntity(networkName == null ? null : networkStartOnBoot.contains(networkName));
    }


    /* === Attachments === */

    private void attachmentList_SelectedItemChanged() {
        saveAttachmentFields();

        // The attachmentSelectedName EntityChangedEvent is really only
        // to catch user updates; don't trigger it programmatically.
        // Suppressing events locally works better than setEntity(, false).
        getAttachmentSelectedPath().getEntityChangedEvent().removeListener(this);
        getAttachmentSelectedPath().setEntity(getAttachmentList().getSelectedItem());
        getAttachmentSelectedPath().getEntityChangedEvent().addListener(this);

        updateAttachmentDisplay();
        // lastSelectedAttachmentPath can be used throughout update process to see prior name
        lastSelectedAttachmentPath = (String) getAttachmentList().getSelectedItem();
    }

    private void attachmentSelectedPath_SelectionChanged() {
        String oldPath = (String) getAttachmentList().getSelectedItem();
        String newPath = (String) getAttachmentSelectedPath().getEntity();

        if (oldPath != null && newPath != null && !newPath.trim().equals(oldPath)) {
            Attachment obj = attachmentMap.get(oldPath);
            newPath = newPath.trim();
            if (newPath.isEmpty() || attachmentMap.containsKey(newPath)) {
                getAttachmentSelectedPath().setEntity(oldPath);
            } else {
                attachmentMap.remove(oldPath);
                attachmentMap.put(newPath, obj);
                getAttachmentList().setItems(new ArrayList<String>(attachmentMap.keySet()));
                getAttachmentList().setSelectedItem(newPath);
            }
        }
    }

    private void addAttachment() {
        if (!attachmentMap.containsKey(newAttachmentText)) {
            attachmentMap.put(newAttachmentText, new Attachment());
            getAttachmentList().setItems(new ArrayList<String>(attachmentMap.keySet()));
        }
        getAttachmentList().setSelectedItem(newAttachmentText);
    }

    private void removeAttachment() {
        attachmentMap.remove((String) getAttachmentList().getSelectedItem());
        getAttachmentList().setItems(new ArrayList<String>(attachmentMap.keySet()));
        getAttachmentList().setSelectedItem(Linq.firstOrDefault(attachmentMap.keySet()));
    }

    /* Save displayed attachment properties */
    private void saveAttachmentFields() {
        if (lastSelectedAttachmentPath != null) {
            Attachment obj = attachmentMap.get(lastSelectedAttachmentPath);
            if (obj != null) {
                @SuppressWarnings("unchecked")
                Map.Entry<Attachment.AttachmentType, String> entry
                        = (Map.Entry<Attachment.AttachmentType, String>) getAttachmentType().getSelectedItem();
                obj.setAttachmentType(entry.getKey());
                obj.setContent((String) getAttachmentContent().getEntity());
            }
        }
    }

    /* Update displayed attachment properties to reflect currently-selected item */
    private void updateAttachmentDisplay() {
        if (getAttachmentList().getSelectedItem() != null) {
            String attachmentName = (String) getAttachmentList().getSelectedItem();
            final Attachment obj = attachmentMap.get(attachmentName);

            getAttachmentType().setSelectedItem(Linq.firstOrDefault(attachmentTypes.entrySet(),
                    new IPredicate<Map.Entry<Attachment.AttachmentType, String>>() {
                        @Override
                        public boolean match(Map.Entry<Attachment.AttachmentType, String> item) {
                            return item.getKey() == obj.getAttachmentType();
                        }
                    }));
            getAttachmentContent().setEntity(obj.getContent());
        }
        else {
            getAttachmentType().setSelectedItem(Linq.firstOrDefault(attachmentTypes.entrySet()));
            getAttachmentContent().setEntity(null);
        }
    }
}
