package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult;
import org.ovirt.engine.core.common.businessentities.pm.PowerStatus;
import org.ovirt.engine.core.common.queries.GetFenceAgentStatusParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.pm.FenceProxySourceTypeHelper;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.HostAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

public class FenceAgentModel extends EntityModel<FenceAgent> {

    public static final String PM_SECURE_KEY = "secure"; //$NON-NLS-1$
    public static final String PM_PORT_KEY = "port"; //$NON-NLS-1$
    public static final String PM_SLOT_KEY = "slot"; //$NON-NLS-1$
    public static final String PM_ENCRYPT_OPTIONS_KEY = "encrypt_options"; //$NON-NLS-1$
    public static final String CISCO_USC = "cisco_ucs"; //$NON-NLS-1$
    private static final String OK = "Ok"; //$NON-NLS-1$
    private static final String ON_REMOVE = "OnRemove"; //$NON-NLS-1$
    private static final String CANCEL = "Cancel"; //$NON-NLS-1$

    public static final Comparator<FenceAgentModel> orderComparable =
            Comparator.comparing(f -> f.getOrder().getEntity());

    final UIConstants constants = ConstantsManager.getInstance().getConstants();
    final UIMessages messages = ConstantsManager.getInstance().getMessages();

    //Misc fields
    private HostModel hostModel;

    //Agent fields
    private EntityModel<String> userName;
    private EntityModel<String> password;
    private EntityModel<String> managementIp;
    private ListModel<String> pmType;
    private EntityModel<Boolean> secure;
    private EntityModel<Boolean> encryptOptions;
    private EntityModel<Integer> port;
    private EntityModel<String> slot;
    private EntityModel<String> options;
    private ListModel<String> variants;
    //The strings to select which other agents this is concurrent with.
    private ListModel<String> concurrentSelectList;
    //Reference to actual fence agent models this is concurrent with.
    private List<FenceAgentModel> concurrentList;
    private boolean ciscoUcsPrimaryPmTypeSelected;
    private EntityModel<Integer> order;
    private boolean initialized;
    private String originalPmType;
    private String originalManagementIp;

    //UI commands
    private UICommand privateTestCommand;

    public FenceAgentModel() {
        userName = new EntityModel<>();
        password = new EntityModel<>();
        managementIp = new EntityModel<>();
        pmType = new ListModel<>();
        secure = new EntityModel<>();
        encryptOptions = new EntityModel<>();
        port = new EntityModel<>();
        slot = new EntityModel<>();
        options = new EntityModel<>();
        variants = new ListModel<>();
        order = new EntityModel<>();
        concurrentSelectList = new ListModel<>();
        List<String> concurrentSelectValues = new ArrayList<>();
        concurrentSelectValues.add(constants.concurrentFenceAgent());
        concurrentSelectList.setItems(concurrentSelectValues);
        concurrentSelectList.setIsAvailable(false);
        concurrentList = new ArrayList<>();
        setTestCommand(new UICommand("Test", new ICommandTarget() { //$NON-NLS-1$
            @Override
            public void executeCommand(UICommand command) {
                test();
            }

            @Override
            public void executeCommand(UICommand uiCommand, Object... parameters) {
                executeCommand(uiCommand);
            }
        }));
        getPmType().getPropertyChangedEvent().addListener((ev, sender, args) -> updateFormStatus());
    }

    /**
     * Copy constructor.
     * @param original The original to copy from.
     */
    public FenceAgentModel(FenceAgentModel original) {
        this();
        copyModelValues(original);
    }

    /**
     * Copy the values from the passed in {@code FenceAgentModel}
     * @param original The model to copy values from.
     */
    private void copyModelValues(FenceAgentModel original) {
        setHost(original.getHost());
        getConcurrentSelectList().setItems(original.getConcurrentSelectList().getItems());
        getPmVariants().setItems(original.getPmVariants().getItems());
        getPmVariants().setSelectedItem(original.getPmVariants().getSelectedItem());
        getPmVariants().setSelectedItems(original.getPmVariants().getSelectedItems());
        getPmUserName().setEntity(original.getPmUserName().getEntity());
        getPmPassword().setEntity(original.getPmPassword().getEntity());
        getPmType().setItems(original.getPmType().getItems());
        getPmType().setSelectedItem(original.getPmType().getSelectedItem());
        getPmType().setSelectedItems(original.getPmType().getSelectedItems());
        getPmSecure().setEntity(original.getPmSecure().getEntity());
        getPmEncryptOptions().setEntity(original.getPmEncryptOptions().getEntity());
        getPmPort().setEntity(original.getPmPort().getEntity());
        getPmSlot().setEntity(original.getPmSlot().getEntity());
        getOrder().setEntity(original.getOrder().getEntity());
        getPmOptions().setEntity(original.getPmOptions().getEntity());
        setCiscoUcsPrimaryPmTypeSelected(original.isCiscoUcsPrimaryPmTypeSelected());
        //Do this last so any event handlers have the up to date information.
        getManagementIp().setEntity(original.getManagementIp().getEntity());
    }

    /**
     * Update the available fields based on the selection of the Power Management type list. Certain types will have
     * different fields available to select/fill out.
     */
    private void updateFormStatus() {
        String pmType = getPmType().getSelectedItem();
        if (StringHelper.isNotNullOrEmpty(pmType)) {
            String version = AsyncDataProvider.getInstance().getDefaultConfigurationVersion();
            if (getHost().getCluster().getSelectedItem() != null) {
                version = getHost().getCluster().getSelectedItem().getCompatibilityVersion().toString();
            }
            AsyncDataProvider.getInstance().getPmOptions(new AsyncQuery<>(pmOptions -> {

                if (pmOptions != null) {
                    getPmPort().setIsAvailable(pmOptions.contains(PM_PORT_KEY));
                    getPmSlot().setIsAvailable(pmOptions.contains(PM_SLOT_KEY));
                    getPmSecure().setIsAvailable(pmOptions.contains(PM_SECURE_KEY));
                    getPmEncryptOptions().setIsAvailable(pmOptions.contains(PM_ENCRYPT_OPTIONS_KEY));
                } else {
                    getPmPort().setIsAvailable(false);
                    getPmSlot().setIsAvailable(false);
                    getPmSecure().setIsAvailable(false);
                }
            }), pmType, version);
            setCiscoUcsPrimaryPmTypeSelected(pmType.equals(CISCO_USC));
        } else {
            getPmPort().setIsAvailable(false);
            getPmSlot().setIsAvailable(false);
            getPmSecure().setIsAvailable(false);
        }
    }

    public boolean isCiscoUcsPrimaryPmTypeSelected() {
        return ciscoUcsPrimaryPmTypeSelected;
    }

    public void setCiscoUcsPrimaryPmTypeSelected(boolean value) {
        if (ciscoUcsPrimaryPmTypeSelected != value) {
            ciscoUcsPrimaryPmTypeSelected = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsCiscoUcsPrimaryPmTypeSelected")); //$NON-NLS-1$
        }
    }

    public Map<String, String> getPmOptionsMap() {
        return getPmOptionsMapInternal(getPmPort(), getPmSlot(), getPmSecure(), getPmOptions());
    }

    public void setPmOptionsMap(Map<String, String> value) {
        setPmOptionsMapInternal(value, getPmPort(), getPmSlot(), getPmSecure(), getPmOptions());
    }

    private void setPmOptionsMapInternal(Map<String, String> value, EntityModel<Integer> port, EntityModel<String> slot,
            EntityModel<Boolean> secure, EntityModel<String> options) {

        StringBuilder pmOptions = new StringBuilder();

        for (Map.Entry<String, String> pair : value.entrySet()) {
            String k = pair.getKey();
            String v = pair.getValue();

            if (PM_PORT_KEY.equals(k)) {
                try {
                    port.setEntity(StringHelper.isNullOrEmpty(value.get(k)) ? 0 : Integer.parseInt(value.get(k)));
                } catch (NumberFormatException e) {
                    port.setEntity(0);
                }
            } else if (PM_SLOT_KEY.equals(k)) {
                slot.setEntity(StringHelper.isNullOrEmpty(value.get(k)) ? "" : value.get(k)); //$NON-NLS-1$

            } else if (PM_SECURE_KEY.equals(k)) {
                secure.setEntity(Boolean.parseBoolean(value.get(k)));
            } else {
                // Compose custom string from unknown pm options.
                if (StringHelper.isNullOrEmpty(v)) {
                    pmOptions.append(k).append(","); //$NON-NLS-1$
                } else {
                    pmOptions.append(k).append("=").append(v).append(","); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        String pmOptionsValue = pmOptions.toString();
        if (StringHelper.isNotNullOrEmpty(pmOptionsValue)) {
            options.setEntity(pmOptionsValue.substring(0, pmOptionsValue.length() - 1));
        }
    }

    private Map<String, String> getPmOptionsMapInternal(EntityModel<Integer> port, EntityModel<String> slot,
            EntityModel<Boolean> secure, EntityModel<String> options) {

        Map<String, String> dict = new HashMap<>();

        if (port.getIsAvailable() && port.getEntity() != null) {
            dict.put(PM_PORT_KEY, String.valueOf(port.getEntity()));
        }
        // Add well known pm options.
        if (slot.getIsAvailable() && slot.getEntity() != null) {
            dict.put(PM_SLOT_KEY, slot.getEntity());
        }
        if (secure.getIsAvailable() && secure.getEntity() != null) {
            dict.put(PM_SECURE_KEY, secure.getEntity().toString());
        }

        // Add unknown pm options.
        // Assume Validate method was called before this getter.
        String pmOptions = options.getEntity();
        if (StringHelper.isNotNullOrEmpty(pmOptions)) {
            for (String pair : pmOptions.split("[,]", -1)) { //$NON-NLS-1$
                String[] array = pair.split("[=]", -1); //$NON-NLS-1$
                if (array.length == 3) { // key=key=value
                    dict.put(array[0], array[1] + "=" + array[2]); //$NON-NLS-1$
                } else if (array.length == 2) { // key=value
                    dict.put(array[0], array[1]);
                } else if (array.length == 1) {
                    dict.put(array[0], ""); //$NON-NLS-1$
                }
            }
        }

        return dict;
    }

    /**
     * Execute the fence agent test.
     */
    public void test() {
        validatePmModels();

        if (!isValid()) {
            return;
        }

        setMessage(ConstantsManager.getInstance().getConstants().testingInProgressItWillTakeFewSecondsPleaseWaitMsg());
        getTestCommand().setIsExecutionAllowed(false);

        Cluster cluster = getHost().getCluster().getSelectedItem();

        GetFenceAgentStatusParameters param = new GetFenceAgentStatusParameters();
        FenceAgent agent = new FenceAgent();
        if (getHost().getHostId() != null) {
            param.setVdsId(getHost().getHostId());
        }

        agent.setOrder(getOrder().getEntity());
        agent.setIp(getManagementIp().getEntity());
        agent.setType(getPmType().getSelectedItem());
        agent.setUser(getPmUserName().getEntity());
        agent.setPassword(getPmPassword().getEntity());
        agent.setPort(getPmPort().getEntity());
        agent.setOptionsMap(getPmOptionsMap());
        param.setAgent(agent);
        param.setStoragePoolId(cluster.getStoragePoolId() != null ? cluster.getStoragePoolId() : Guid.Empty);
        param.setFenceProxySources(FenceProxySourceTypeHelper.parseFromString(getHost().getPmProxyPreferences()));
        param.setVdsName(getHost().getName().getEntity());
        param.setHostName(getHost().getHost().getEntity());

        param.setClusterId(cluster.getId());

        Frontend.getInstance().runQuery(QueryType.GetFenceAgentStatus, param, new AsyncQuery<QueryReturnValue>(returnValue -> {
            String msg;
            if (returnValue == null) {
                msg = ConstantsManager.getInstance().getConstants().testFailedUnknownErrorMsg();
            } else {
                FenceOperationResult result = returnValue.getReturnValue();
                if (result.getStatus() == FenceOperationResult.Status.SUCCESS) {
                    msg = ConstantsManager.getInstance().getMessages().testSuccessfulWithPowerStatus(
                            result.getPowerStatus() == PowerStatus.ON
                                    ? ConstantsManager.getInstance().getConstants().powerOn()
                                    : ConstantsManager.getInstance().getConstants().powerOff());
                } else {
                    msg = ConstantsManager.getInstance().getMessages().testFailedWithErrorMsg(
                            result.getMessage());
                }
            }
            setMessage(msg);
            getTestCommand().setIsExecutionAllowed(true);
        }, true)
        );
    }

    /**
     * Validate the Power Management related Entity Models.
     */
    public void validatePmModels() {
        EntityModel<String> ip = getManagementIp();
        EntityModel<String> userName = getPmUserName();
        EntityModel<String> password = getPmPassword();
        ListModel<String> type = getPmType();
        EntityModel<Integer> port = getPmPort();

        ip.validateEntity(new IValidation[] {new NotEmptyValidation(), new HostAddressValidation()});
        userName.validateEntity(new IValidation[] {new NotEmptyValidation()});
        password.validateEntity(new IValidation[] {new NotEmptyValidation(), new LengthValidation(50)});
        type.validateSelectedItem(new IValidation[] {new NotEmptyValidation()});
        port.validateEntity(new IValidation[] {new IntegerValidation(1, 65535)});
    }

    /**
     * Determine if the model is valid. validatePmModels() has to be run at some point before calling this otherwise
     * it will always return true.
     * @return true if the model is valid, false otherwise.
     */
    public boolean isValid() {
        return getManagementIp().getIsValid() && getPmUserName().getIsValid() && getPmPassword().getIsValid()
                && getPmType().getIsValid() && getPmPort().getIsValid() && getPmOptions().getIsValid();
    }

    /**
     * Edit the model.
     */
    public void edit() {
        if (getWindow() != null) {
            return;
        }
        FenceAgentModel newModel = new FenceAgentModel(this);
        //If editing a 'new' fence agent model, both type and management ip will be null.
        newModel.setOriginalPmType(getPmType().getSelectedItem());
        newModel.setOriginalManagementIp(getManagementIp().getEntity());
        setWindow(newModel);
        newModel.setTitle(constants.editFenceAgent());
        newModel.getCommands().add(UICommand.createDefaultOkUiCommand(OK, this));
        newModel.getCommands().add(UICommand.createDefaultCancelUiCommand(CANCEL, this));
    }

    /**
     * Confirm the removal of the model.
     */
    public void confirmRemove() {
        if (getWindow() != null) {
            return;
        }
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(constants.areYouSureTitle());
        String confirmMessage = "";
        if (concurrentList.isEmpty()) {
            confirmMessage = messages.confirmDeleteFenceAgent(getDisplayString());
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append(getDisplayString());
            for (FenceAgentModel concurrentModel: concurrentList) {
                builder.append("\n"); //$NON-NLS-1$
                builder.append(concurrentModel.getDisplayString());
            }
            confirmMessage = messages.confirmDeleteAgentGroup(builder.toString());
        }
        model.setMessage(confirmMessage);
        model.getLatch().setIsAvailable(true);
        model.getLatch().setIsChangeable(true);

        UICommand tempVar = UICommand.createDefaultOkUiCommand(ON_REMOVE, this);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand(CANCEL, this);
        model.getCommands().add(tempVar2);
    }

    @Override
    public void executeCommand(UICommand command) {
        if (OK.equals(command.getName())) {
            onOk();
        } else if (CANCEL.equals(command.getName())) {
            cancel();
        } else if (ON_REMOVE.equals(command.getName())) {
            removeFromList();
            setWindow(null);
        } else {
            super.executeCommand(command);
        }
        if (getWindow() == null) {
            getHost().getFenceAgentListModel().notifyItemListeners();
        }
    }

    /**
     * Action to take when user clicked OK in the pop-up.
     */
    private void onOk() {
        FenceAgentModel model = (FenceAgentModel) getWindow();
        model.validatePmModels();
        validateDuplicates(model);
        if (model.isValid()) {
            copyModelValues(model);
            setWindow(null);
        }
    }

    /**
     * Check if the containing list model contains a duplicate management ip address.
     * @param model The model to check against.
     */
    private void validateDuplicates(FenceAgentModel model) {
        //Check for duplicate addresses.
        for (FenceAgentModel existingModel: getHost().getFenceAgentListModel().getItems()) {
            if (!checkIfModelIsDuplicate(model, existingModel)) {
                return;
            }
            for (FenceAgentModel concurrentExistingModel: existingModel.getConcurrentList()) {
                if (!checkIfModelIsDuplicate(model, concurrentExistingModel)) {
                    return;
                }
            }
        }
    }

    private boolean checkIfModelIsDuplicate(FenceAgentModel model, FenceAgentModel concurrentExistingModel) {
        if (model.getManagementIp().getEntity().equals(concurrentExistingModel.getManagementIp().getEntity()) &&
                model.getPmType().getSelectedItem().equals(concurrentExistingModel.getPmType().getSelectedItem()) &&
                !(model.getPmType().getSelectedItem().equals(model.getOriginalPmType()) &&
                model.getManagementIp().getEntity().equals(model.getOriginalManagementIp()))) {
            //Force a change event by setting to true, which will change to false below.
            model.getManagementIp().setIsValid(true);
            model.getPmType().setIsValid(true);
            //Duplicate, need to set the invalidity reason before switching to false to update the widgets properly.
            model.getManagementIp().getInvalidityReasons().add(constants.duplicateFenceAgentManagementIp());
            model.getPmType().getInvalidityReasons().add(constants.duplicateFenceAgentManagementIp());
            model.getManagementIp().setIsValid(false);
            model.getPmType().setIsValid(false);
        }
        return model.getManagementIp().getIsValid();
    }

    /**
     * Action to take when user clicked cancel in the pop-up.
     */
    private void cancel() {
        validatePmModels();
        if (!isValid()) {
            removeFromList();
        }
        setWindow(null);
    }

    /**
     * Remove myself from the items list.
     */
    private void removeFromList() {
        hostModel.getFenceAgentListModel().removeItem(this);
    }

    /**
     * Check if the management ip address is set in this model.
     * @return True if the address is set, false otherwise.
     */
    public boolean hasAddress() {
        return getManagementIp() != null && StringHelper.isNotNullOrEmpty(getManagementIp().getEntity());
    }

    /**
     * Return the first item of the concurrent select list.
     * @return The first item of the list.
     */
    public String getConcurrentListFirstItem() {
        return getConcurrentSelectList().getItems().toArray(new String[getConcurrentSelectList().getItems().size()])[0];
    }

    /**
     * Return the string representation of this model for display purposes.
     * @return The display string.
     */
    public String getDisplayString() {
        return getPmType().getSelectedItem() + " : " //$NON-NLS-1$
                + getManagementIp().getEntity();
    }

    /**
     * Check to see if this model contains any concurrent {@code FenceAgentModel}s. This does NOT check if this model
     * is in any other models concurrent group.
     * @return true if there are any concurrent models, false otherwise.
     */
    public boolean isInConcurrentGroup() {
        return !concurrentList.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ciscoUcsPrimaryPmTypeSelected,
                concurrentSelectList.getItems(), encryptOptions.getEntity(),
                hostModel, initialized, managementIp.getEntity(), options.getEntity(), order.getEntity(),
                password.getEntity(), pmType.getItems(), port.getEntity(), secure.getEntity(),
                slot.getEntity(), userName.getEntity(), variants.getItems());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FenceAgentModel other = (FenceAgentModel) obj;
        return Objects.equals(ciscoUcsPrimaryPmTypeSelected, other.ciscoUcsPrimaryPmTypeSelected)
                && Objects.equals(concurrentSelectList.getItems(), other.concurrentSelectList.getItems())
                && Objects.equals(encryptOptions.getEntity(), other.encryptOptions.getEntity())
                && Objects.equals(hostModel, other.hostModel)
                && Objects.equals(initialized, other.initialized)
                && Objects.equals(managementIp.getEntity(), other.managementIp.getEntity())
                && Objects.equals(options.getEntity(), other.options.getEntity())
                && Objects.equals(order, other.order)
                && Objects.equals(password.getEntity(), other.password.getEntity())
                && Objects.equals(pmType.getItems(), other.pmType.getItems())
                && Objects.equals(port.getEntity(), other.port.getEntity())
                && Objects.equals(secure.getEntity(), other.secure.getEntity())
                && Objects.equals(slot.getEntity(), other.slot.getEntity())
                && Objects.equals(userName.getEntity(), other.userName.getEntity())
                && Objects.equals(variants.getItems(), other.variants.getItems());
    }

    @Override
    public String toString() {
        return getDisplayString();
    }

    //Getters and setters.
    public EntityModel<String> getPmUserName() {
        return userName;
    }

    public void setPmUserName(EntityModel<String> value) {
        if (value != null) {
            userName = value;
        }
    }

    public EntityModel<String> getPmPassword() {
        return password;
    }

    public void setPmPassword(EntityModel<String> value) {
        if (value != null) {
            password = value;
        }
    }

    public EntityModel<String> getManagementIp() {
        return managementIp;
    }

    public void setManagementIp(EntityModel<String> value) {
        if (value != null) {
            managementIp = value;
        }
    }


    public ListModel<String> getPmType() {
        return pmType;
    }

    public EntityModel<Boolean> getPmSecure() {
        return secure;
    }

    public void setPmSecure(EntityModel<Boolean> value) {
        if (value != null) {
            secure = value;
        }
    }

    public EntityModel<Boolean> getPmEncryptOptions() {
        return encryptOptions;
    }

    public void setPmEncryptOptions(EntityModel<Boolean> value) {
        if (value != null) {
            encryptOptions = value;
        }
    }

    public EntityModel<Integer> getPmPort() {
        return port;
    }

    public void setPmPort(EntityModel<Integer> value) {
        if (value != null) {
            port = value;
        }
    }

    public EntityModel<String> getPmSlot() {
        return slot;
    }

    public void setPmSlot(EntityModel<String> value) {
        if (value != null) {
            slot = value;
        }
    }

    public EntityModel<String> getPmOptions() {
        return options;
    }

    public void setPmOptions(EntityModel<String> value) {
        if (value != null) {
            options = value;
        }
    }

    public ListModel<String> getPmVariants() {
        return variants;
    }

    public void setPmVariants(ListModel<String> value) {
        if (value != null) {
            variants = value;
        }
    }

    public ListModel<String> getConcurrentSelectList() {
        return concurrentSelectList;
    }

    public UICommand getTestCommand() {
        return privateTestCommand;
    }

    private void setTestCommand(UICommand value) {
        privateTestCommand = value;
    }

    /**
     * Get the relative order of the {@code FenceAgent}
     * @return The relative order.
     */
    public EntityModel<Integer> getOrder() {
        return order;
    }

    /**
     * Set the relative order of the {@code FenceAgent}
     * @param order The relative order.
     */
    public void setOrder(int order) {
        this.order.setEntity(order);
        for (FenceAgentModel concurrentModel: concurrentList) {
            concurrentModel.setOrder(order);
        }
    }

    private HostModel getHost() {
        return hostModel;
    }

    public void setHost(HostModel value) {
        hostModel = value;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized() {
        initialized = true;
    }

    public List<FenceAgentModel> getConcurrentList() {
        return concurrentList;
    }

    public String getOriginalPmType() {
        return originalPmType;
    }

    public void setOriginalPmType(String originalPmType) {
        this.originalPmType = originalPmType;
    }

    public String getOriginalManagementIp() {
        return originalManagementIp;
    }

    public void setOriginalManagementIp(String originalManagementIp) {
        this.originalManagementIp = originalManagementIp;
    }
}
