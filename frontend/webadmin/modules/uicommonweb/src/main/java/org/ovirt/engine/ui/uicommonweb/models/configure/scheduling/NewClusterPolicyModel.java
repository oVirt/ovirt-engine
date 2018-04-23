package org.ovirt.engine.ui.uicommonweb.models.configure.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.IModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui.RoleListModel.CommandType;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;

public class NewClusterPolicyModel extends Model {
    public static final Guid NONE_POLICY_UNIT = new Guid("38440000-8cf0-14bd-c43e-10b96e4ef00a"); //$NON-NLS-1$

    private static final EventDefinition FILTERS_CHANGED_EVENT_DEFINITION = new EventDefinition("FiltersChanged", //$NON-NLS-1$
            NewClusterPolicyModel.class);
    private static final EventDefinition FUNCTIONS_CHANGED_EVENT_DEFINITION = new EventDefinition("FiltersChanged", //$NON-NLS-1$
            NewClusterPolicyModel.class);

    public static NewClusterPolicyModel createModel(CommandType commandType,
            ClusterPolicy clusterPolicy,
            IModel sourceModel, ArrayList<PolicyUnit> policyUnits) {
        NewClusterPolicyModel clusterPolicyModel =
                new NewClusterPolicyModel(commandType, clusterPolicy, sourceModel, policyUnits);
        clusterPolicyModel.init();
        return clusterPolicyModel;
    }

    private void init() {
        initTitle();
        initModels();
        initCommands();
    }

    private void initModels() {
        getName().setEntity(clusterPolicy.getName());
        getName().setIsChangeable(!clusterPolicy.isLocked());
        getDescription().setEntity(clusterPolicy.getDescription());
        getDescription().setIsChangeable(!clusterPolicy.isLocked());

        initFilters();
        initFunctions();
        initLoadBalance();
        initCustomPropertySheet();
    }

    private void initCustomPropertySheet() {
        setCustomPropertySheet(new KeyValueModel());
        if (clusterPolicy.getParameterMap() != null) {
            getCustomProperties().putAll(clusterPolicy.getParameterMap());
        }
        getCustomPropertySheet().setIsChangeable(!clusterPolicy.isLocked());
        customPropertiesInitialized = true;
        refreshCustomProperties(null, false);

    }

    private void refreshCustomProperties(PolicyUnit toRemove, boolean reset) {
        if (!customPropertiesInitialized) {
            return;
        }
        Map<String, String> policyProperties = new HashMap<>();
        Map<Guid, PolicyUnit> allPolicyUnits = new HashMap<>();
        for (PolicyUnit policyUnit : getUsedFilters()) {
            allPolicyUnits.put(policyUnit.getId(), policyUnit);
        }
        for (Pair<PolicyUnit, Integer> pair : getUsedFunctions()) {
            allPolicyUnits.put(pair.getFirst().getId(), pair.getFirst());
        }
        if (toRemove != null && !allPolicyUnits.containsKey(toRemove.getId())) {
            if (toRemove.getParameterRegExMap() != null) {
                for (Entry<String, String> entry : toRemove.getParameterRegExMap().entrySet()) {
                    getCustomProperties().remove(entry.getKey());
                }
            }
        }

        PolicyUnit selectedItem = loadBalanceList.getSelectedItem();
        allPolicyUnits.put(selectedItem.getId(), selectedItem);
        for (PolicyUnit policyUnit : allPolicyUnits.values()) {
            if (policyUnit.getParameterRegExMap() != null) {
                policyProperties.putAll(policyUnit.getParameterRegExMap());
            }
        }
        Map<String, String> defaultMap = new HashMap<>(getCustomProperties());
        if(!reset) {
            defaultMap.putAll(KeyValueModel.convertProperties(getCustomPropertySheet().serialize()));
        }
        getCustomPropertySheet().setKeyValueMap(policyProperties);
        getCustomPropertySheet().deserialize(KeyValueModel.convertProperties(defaultMap));
    }

    private void initFilters() {
        getUsedFilters().clear();
        getUnusedFilters().clear();
        if (clusterPolicy.getFilters() == null) {
            getUnusedFilters().addAll(getFilterPolicyUnits(policyUnits));
            return;
        }
        Map<Guid, PolicyUnit> map = (HashMap<Guid, PolicyUnit>) ((HashMap<Guid, PolicyUnit>) policyUnitsMap).clone();
        for (Guid policyUnitId : clusterPolicy.getFilters()) {
            map.remove(policyUnitId);
            getUsedFilters().add(policyUnitsMap.get(policyUnitId));
        }
        initFilterPositions();
        getUnusedFilters().addAll(getFilterPolicyUnits(new ArrayList<>(map.values())));

    }

    private void initFilterPositions() {
        if (clusterPolicy.getFilterPositionMap() != null) {
            for (Entry<Guid, Integer> entry : clusterPolicy.getFilterPositionMap().entrySet()) {
                getFilterPositionMap().put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void initFunctions() {
        getUsedFunctions().clear();
        getUnusedFunctions().clear();
        if (clusterPolicy.getFunctions() == null) {
            getUnusedFunctions().addAll(getFunctionPolicyUnits(policyUnits));
            return;
        }
        Map<Guid, PolicyUnit> map = (Map<Guid, PolicyUnit>) ((HashMap<Guid, PolicyUnit>) policyUnitsMap).clone();
        for (Pair<Guid, Integer> pair : clusterPolicy.getFunctions()) {
            map.remove(pair.getFirst());
            getUsedFunctions().add(new Pair<>(policyUnitsMap.get(pair.getFirst()), pair.getSecond()));
        }
        getUnusedFunctions().addAll(getFunctionPolicyUnits(new ArrayList<>(map.values())));
    }

    private void initLoadBalance() {
        ArrayList<PolicyUnit> balancePolicyUnits = getBalancePolicyUnits(policyUnits);
        getLoadBalanceList().setItems(balancePolicyUnits);

        if (clusterPolicy.getBalance() != null) {
            currentLoadBalance = policyUnitsMap.get(clusterPolicy.getBalance());
        } else {
            currentLoadBalance = policyUnitsMap.get(NONE_POLICY_UNIT);
        }
        getLoadBalanceList().setIsChangeable(!clusterPolicy.isLocked());
        getLoadBalanceList().setSelectedItem(currentLoadBalance);
        getLoadBalanceList().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            refreshCustomProperties(currentLoadBalance, false);
            currentLoadBalance = getLoadBalanceList().getSelectedItem();
        });
    }

    private ArrayList<PolicyUnit> getBalancePolicyUnits(ArrayList<PolicyUnit> list) {
        if (list == null || list.size() == 0) {
            return new ArrayList<>();
        }
        ArrayList<PolicyUnit> balancePolicyUnits = new ArrayList<>();
        for (PolicyUnit policyUnit : list) {
            if (policyUnit.getPolicyUnitType() == PolicyUnitType.LOAD_BALANCING) {
                balancePolicyUnits.add(policyUnit);
            }
        }
        return balancePolicyUnits;
    }

    private ArrayList<PolicyUnit> getFilterPolicyUnits(ArrayList<PolicyUnit> list) {
        if (list == null || list.size() == 0) {
            return new ArrayList<>();
        }
        ArrayList<PolicyUnit> filterPolicyUnits = new ArrayList<>();
        for (PolicyUnit policyUnit : list) {
            if (policyUnit.getPolicyUnitType() == PolicyUnitType.FILTER) {
                filterPolicyUnits.add(policyUnit);
            }
        }
        return filterPolicyUnits;
    }

    private ArrayList<PolicyUnit> getFunctionPolicyUnits(ArrayList<PolicyUnit> list) {
        if (list == null || list.size() == 0) {
            return new ArrayList<>();
        }
        ArrayList<PolicyUnit> functionPolicyUnits = new ArrayList<>();
        for (PolicyUnit policyUnit : list) {
            if (policyUnit.getPolicyUnitType() == PolicyUnitType.WEIGHT) {
                functionPolicyUnits.add(policyUnit);
            }
        }
        return functionPolicyUnits;
    }

    private void initCommands() {
        if (!clusterPolicy.isLocked() || commandType == CommandType.Clone) {
            UICommand onSaveCommand = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
            getCommands().add(onSaveCommand);
            UICommand onResetCommand = new UICommand("OnReset", this); //$NON-NLS-1$
            onResetCommand.setTitle(ConstantsManager.getInstance().getConstants().resetTitle());
            getCommands().add(onResetCommand);
        }

        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(!clusterPolicy.isLocked() ? ConstantsManager.getInstance().getConstants().cancel()
                : ConstantsManager.getInstance().getConstants().close());
        cancelCommand.setIsCancel(true);
        getCommands().add(cancelCommand);
    }

    private void initTitle() {
        switch (commandType) {
            case New:
                setTitle(ConstantsManager.getInstance().getConstants().newClusterPolicyTitle());
                setHelpTag(HelpTag.new_cluster_policy);
                setHashName("new_cluster_policy"); //$NON-NLS-1$
                break;
            case Edit:
                setTitle(ConstantsManager.getInstance().getConstants().editClusterPolicyTitle());
                setHelpTag(HelpTag.edit_cluster_policy);
                setHashName("edit_cluster_policy"); //$NON-NLS-1$
                break;
            case Clone:
                setTitle(ConstantsManager.getInstance().getConstants().copyClusterPolicyTitle());
                setHelpTag(HelpTag.copy_cluster_policy);
                setHashName("copy_cluster_policy"); //$NON-NLS-1$
                break;
        }
    }

    private boolean customPropertiesInitialized = false;
    private Event<EventArgs> filtersChangedEvent;
    private Event<EventArgs> functionsChangedEvent;
    private final IModel sourceModel;
    private final ClusterPolicy clusterPolicy;
    private final CommandType commandType;
    private final Map<Guid, PolicyUnit> policyUnitsMap;
    private final ArrayList<PolicyUnit> policyUnits;
    private final ArrayList<PolicyUnit> usedFilters;
    private final ArrayList<PolicyUnit> unusedFilters;
    private final ArrayList<Pair<PolicyUnit, Integer>> usedFunctions;
    private final ArrayList<PolicyUnit> unusedFunctions;
    private final Map<Guid, Integer> filterPositionMap;
    private KeyValueModel customPropertySheet;
    private final Map<String, String> customProperties;
    private EntityModel<String> name;
    private EntityModel<String> description;
    private ListModel filterList;
    private ListModel functionList;
    private ListModel<PolicyUnit> loadBalanceList;
    private PolicyUnit currentLoadBalance;

    public NewClusterPolicyModel(CommandType commandType,
            ClusterPolicy clusterPolicy,
            IModel sourceModel,
            ArrayList<PolicyUnit> policyUnits) {
        this.commandType = commandType;
        this.clusterPolicy = clusterPolicy;
        this.sourceModel = sourceModel;
        this.policyUnits = policyUnits;
        policyUnitsMap = new HashMap<>();
        for (PolicyUnit policyUnit : policyUnits) {
            policyUnitsMap.put(policyUnit.getId(), policyUnit);
        }
        usedFilters = new ArrayList<>();
        unusedFilters = new ArrayList<>();
        usedFunctions = new ArrayList<>();
        unusedFunctions = new ArrayList<>();
        filterPositionMap = new HashMap<>();
        customProperties = new LinkedHashMap<>();
        setName(new EntityModel<String>());
        setDescription(new EntityModel<String>());
        setFilterList(new ListModel());
        setFunctionList(new ListModel());
        setLoadBalanceList(new ListModel<PolicyUnit>());
        setFiltersChangedEvent(new Event<>(FILTERS_CHANGED_EVENT_DEFINITION));
        setFunctionsChangedEvent(new Event<>(FUNCTIONS_CHANGED_EVENT_DEFINITION));
    }

    public EntityModel<String> getName() {
        return name;
    }

    public void setName(EntityModel<String> name) {
        this.name = name;
    }

    public EntityModel<String> getDescription() {
        return description;
    }

    public void setDescription(EntityModel<String> description) {
        this.description = description;
    }

    public ListModel getFilterList() {
        return filterList;
    }

    public void setFilterList(ListModel filterList) {
        this.filterList = filterList;
    }

    public ListModel getFunctionList() {
        return functionList;
    }

    public void setFunctionList(ListModel functionList) {
        this.functionList = functionList;
    }

    public ListModel<PolicyUnit> getLoadBalanceList() {
        return loadBalanceList;
    }

    public void setLoadBalanceList(ListModel<PolicyUnit> loadBalanceList) {
        this.loadBalanceList = loadBalanceList;
    }

    public ClusterPolicy getClusterPolicy() {
        return clusterPolicy;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public ArrayList<PolicyUnit> getPolicyUnits() {
        return policyUnits;
    }

    public Map<Guid, PolicyUnit> getPolicyUnitsMap() {
        return policyUnitsMap;
    }

    public ArrayList<PolicyUnit> getUsedFilters() {
        return usedFilters;
    }

    public ArrayList<PolicyUnit> getUnusedFilters() {
        return unusedFilters;
    }

    public ArrayList<Pair<PolicyUnit, Integer>> getUsedFunctions() {
        return usedFunctions;
    }

    public ArrayList<PolicyUnit> getUnusedFunctions() {
        return unusedFunctions;
    }

    public Map<Guid, Integer> getFilterPositionMap() {
        return filterPositionMap;
    }

    public Event<EventArgs> getFiltersChangedEvent() {
        return filtersChangedEvent;
    }

    public void setFiltersChangedEvent(Event<EventArgs> filtersChangedEvent) {
        this.filtersChangedEvent = filtersChangedEvent;
    }

    public KeyValueModel getCustomPropertySheet() {
        return customPropertySheet;
    }

    public void setCustomPropertySheet(KeyValueModel customPropertySheet) {
        this.customPropertySheet = customPropertySheet;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public Event<EventArgs> getFunctionsChangedEvent() {
        return functionsChangedEvent;
    }

    public void setFunctionsChangedEvent(Event<EventArgs> functionsChangedEvent) {
        this.functionsChangedEvent = functionsChangedEvent;
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command.getName().equals("Cancel")) { // $NON-NLS-1$
            cancel();
        } else if (command.getName().equals("OnSave")) { //$NON-NLS-1$
            save();
        } else if (command.getName().equals("OnReset")) { //$NON-NLS-1$
            reset();
        }
    }

    public void addFilter(PolicyUnit policyUnit, boolean used, int position) {
        if (position != 0) {
            Guid removeEntry = null;
            for (Entry<Guid, Integer> entry : getFilterPositionMap().entrySet()) {
                if (entry.getValue().equals(position)) {
                    removeEntry = entry.getKey();
                    break;
                }
            }
            if (removeEntry != null) {
                getFilterPositionMap().remove(removeEntry);
            }

        }
        getFilterPositionMap().put(policyUnit.getId(), position);
        if (!used) {
            usedFilters.add(policyUnit);
            for (int i = 0; i < unusedFilters.size(); i++) {
                if (unusedFilters.get(i).getId().equals(policyUnit.getId())) {
                    unusedFilters.remove(policyUnit);
                    break;
                }
            }
        }
        refreshCustomProperties(null, false);
        getFiltersChangedEvent().raise(this, EventArgs.EMPTY);
    }

    public void removeFilter(PolicyUnit policyUnit) {
        unusedFilters.add(policyUnit);
        for (int i = 0; i < usedFilters.size(); i++) {
            if (usedFilters.get(i).getId().equals(policyUnit.getId())) {
                usedFilters.remove(i);
                break;
            }
        }
        refreshCustomProperties(policyUnit, false);
        getFiltersChangedEvent().raise(this, EventArgs.EMPTY);
    }

    public void addFunction(PolicyUnit policyUnit) {
        usedFunctions.add(new Pair<>(policyUnit, 1));
        for (int i = 0; i < unusedFunctions.size(); i++) {
            if (unusedFunctions.get(i).getId().equals(policyUnit.getId())) {
                unusedFunctions.remove(policyUnit);
                break;
            }
        }
        refreshCustomProperties(null, false);
        getFunctionsChangedEvent().raise(this, EventArgs.EMPTY);
    }

    public void removeFunction(PolicyUnit policyUnit) {
        unusedFunctions.add(policyUnit);
        for (int i = 0; i < usedFunctions.size(); i++) {
            if (usedFunctions.get(i).getFirst().getId().equals(policyUnit.getId())) {
                usedFunctions.remove(i);
                break;
            }
        }
        refreshCustomProperties(policyUnit, false);
        getFunctionsChangedEvent().raise(this, EventArgs.EMPTY);
    }

    public void updateFactor(PolicyUnit policyUnit, Integer factor) {
        for (int i = 0; i < usedFunctions.size(); i++) {
            if (usedFunctions.get(i).getFirst().getId().equals(policyUnit.getId())) {
                usedFunctions.get(i).setSecond(factor);
                break;
            }
        }
    }

    private void cancel() {
        sourceModel.setWindow(null);
    }

    private void save() {
        if (getProgress() != null) {
            return;
        }
        if (!validate()) {
            return;
        }
        startProgress();
        ClusterPolicy policy = new ClusterPolicy();
        policy.setId(clusterPolicy.getId());
        policy.setName(getName().getEntity());
        policy.setDescription(getDescription().getEntity());
        ArrayList<Guid> keys = new ArrayList<>();
        for (PolicyUnit clusterPolicy : getUsedFilters()) {
            keys.add(clusterPolicy.getId());
        }
        policy.setFilters(keys);
        policy.setFilterPositionMap(getFilterPositionMap());
        ArrayList<Pair<Guid, Integer>> pairs = new ArrayList<>();
        for (Pair<PolicyUnit, Integer> pair : getUsedFunctions()) {
            pairs.add(new Pair<>(pair.getFirst().getId(), pair.getSecond()));
        }
        policy.setFunctions(pairs);
        policy.setBalance(getLoadBalanceList().getSelectedItem().getId());
        policy.setParameterMap(KeyValueModel.convertProperties(getCustomPropertySheet().serialize()));
        Frontend.getInstance().runAction(commandType == CommandType.Edit ? ActionType.EditClusterPolicy
                : ActionType.AddClusterPolicy,
                new ClusterPolicyCRUDParameters(policy.getId(), policy), result -> {
                    NewClusterPolicyModel.this.stopProgress();
                    if (result.getReturnValue().getSucceeded()) {
                        NewClusterPolicyModel.this.cancel();
                    }
                });
    }

    private boolean validate() {
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new LengthValidation(50),
                new AsciiNameValidation() });
        getDescription().validateEntity(new IValidation[] { new LengthValidation(400), new AsciiOrNoneValidation() });
        return getName().getIsValid() && getDescription().getIsValid()
                && getCustomPropertySheet().validate();
    }

    private void reset() {
        getName().setEntity(clusterPolicy.getName());
        getDescription().setEntity(clusterPolicy.getDescription());
        initFilters();
        initFunctions();
        if (clusterPolicy.getBalance() != null) {
            getLoadBalanceList().setSelectedItem(policyUnitsMap.get(clusterPolicy.getBalance()));
        }
        getFiltersChangedEvent().raise(this, EventArgs.EMPTY);
        getFunctionsChangedEvent().raise(this, EventArgs.EMPTY);
        customProperties.clear();
        if (clusterPolicy.getParameterMap() != null) {
            customProperties.putAll(clusterPolicy.getParameterMap());
        }
        refreshCustomProperties(null, true);
    }

    @Override
    public void cleanup() {
        cleanupEvents(getFiltersChangedEvent(),
                getFunctionsChangedEvent());
        super.cleanup();
    }
}
