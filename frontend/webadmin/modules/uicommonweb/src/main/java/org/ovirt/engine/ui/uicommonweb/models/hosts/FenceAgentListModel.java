package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class FenceAgentListModel extends SortedListModel<FenceAgentModel> {

    /**
     * Constant messages.
     */
    final UIConstants constants = ConstantsManager.getInstance().getConstants();

    /**
     * The available Power Management Types.
     */
    private List<String> pmTypes;

    /**
     * The host this {@code FenceAgentListModel} is associated with.
     */
    private HostModel hostModel;

    /**
     * Update the Power Management types available. This will also update ALL fence agent models part of this
     * {@code FenceAgentListModel}
     * @param pmTypes A list of power management type strings.
     */
    public void setPmTypes(List<String> pmTypes) {
        this.pmTypes = pmTypes;
        if (getItems() != null) {
            for (FenceAgentModel model: getItems()) {
                String selectedItem = model.getPmType().getSelectedItem();
                if (selectedItem != null && pmTypes.contains(selectedItem)) {
                    model.getPmType().setItems(pmTypes, selectedItem);
                } else {
                    model.getPmType().setItems(pmTypes);
                }
            }
        }
    }

    /**
     * Set the associated host, this also updates ALL fence agent models part of this
     * {@code FenceAgentListModel}
     * @param hostModel The {@code HostModel} to set.
     */
    public void setHost(HostModel hostModel) {
        this.hostModel = hostModel;
        if (getItems() != null) {
            for (FenceAgentModel model: getItems()) {
                model.setHost(hostModel);
            }
        }
    }

    /**
     * Validate the {@code FenceAgentListModel}. This will return true only if all the fence agents part of this
     * model validate as true.
     * @return true if validation passes, false otherwise.
     */
    public boolean validate() {
        if (getItems() != null) {
            for (FenceAgentModel model: getItems()) {
                model.validatePmModels();
                if (!model.isValid()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Getter for available Power Management types.
     * @return A list of strings representing the power management types.
     */
    public List<String> getPmTypes() {
        return this.pmTypes;
    }

    /**
     * Getter for the host model
     * @return The current {@code HostModel}
     */
    public HostModel getHostModel() {
        return this.hostModel;
    }

    /**
     * Set the list of {@code FenceAgentModel}s. Since the order of the items matters the setItems takes a {@code List}
     * instead of a collection. setting the items also updates all the {@code FenceAgentModel} concurrentSelectList
     */
    public void setItems(List<FenceAgentModel> items) {
        updateConcurrentList(items);
        super.setItems(items);
    }

    /**
     * Updates the available concurrent select list in all the {@code FenceAgentModel}s in this list model.
     * @see #updateConcurrentList(List) for the algorithm used to update.
     */
    public void updateConcurrentList() {
        if (getItems() != null) {
            updateConcurrentList((List<FenceAgentModel>) getItems());
        }
    }

    /**
     * Iterate over the passed in {@code List<FenceAgentModel>} and for each model calculate the possible other
     * models that can be concurrent with it. This is achieved by looping over the list of FenceAgentModels and
     * calculating the concurrent string. Each string is then added to a list and the list is set for the current
     * FenceAgentModel we are calculating the list for. When generating the list we skip over the current model.
     * @param items The list of {@code FenceAgentModel}s
     */
    public void updateConcurrentList(List<FenceAgentModel> items) {
        for (FenceAgentModel model: items) {
            updateConcurrentList(model, items);
            if (model.getConcurrentSelectList().getItems() == null
                    || model.getConcurrentSelectList().getItems().size() < 2) {
                //There are less than 2 fence agents, can't make them concurrent, so it should not be available.
                model.getConcurrentSelectList().setIsAvailable(false);
            } else {
                model.getConcurrentSelectList().setIsAvailable(true);
            }
        }
    }

    /**
     * Update the concurrent list of the passed in {@code FenceAgentModel} based on the passed in list of models.
     * @param compareModel The model to generate the list for.
     * @param items The list of {@code FenceAgentModel}s.
     */
    private void updateConcurrentList(FenceAgentModel compareModel, Collection<FenceAgentModel> items) {
        List<String> values = new ArrayList<>();
        values.add(constants.concurrentFenceAgent());
        ListModel<String> concurrentList = compareModel.getConcurrentSelectList();
        if (concurrentList != null && concurrentList.getItems() != null && compareModel.hasAddress() && items != null) {
            for (FenceAgentModel model: items) {
                if (!model.equals(compareModel) && model.hasAddress()) {
                    values.add(createConcurrentOptionString(model));
                }
            }
        }
        if (concurrentList != null && concurrentList.getSelectedItem() != null
                && values.indexOf(concurrentList.getSelectedItem()) > -1) {
            concurrentList.setItems(values, concurrentList.getSelectedItem());
        } else if (concurrentList != null ) {
            concurrentList.setItems(values);
        }
    }

    /**
     * Build a string that represents the {@code FenceAgentModel} or a concurrent group of {@code FenceAgentModel}s.
     * It is either the 'displayString' from the model, OR a comma separated string of displayStrings. The comma
     * separated string is generated if the model is in a concurrent group.
     *
     * @param model The model to generate the concurrent option string for.
     * @return A string for the concurrent selection list.
     */
    private String createConcurrentOptionString(FenceAgentModel model) {
        StringBuilder builder = new StringBuilder();
        builder.append(model.getDisplayString());
        for (FenceAgentModel concurrentModel: model.getConcurrentList()) {
            builder.append(", " + concurrentModel.getDisplayString()); //$NON-NLS-1$
        }
        return builder.toString();
    }

    /**
     * Make the passed in {@code FenceAgentModel} concurrent with the model represented by the 'selectedString'. The
     * model (or concurrent group) is looked up using the string. The concurrentModel is then added to the model/group.
     * @param concurrentModel The model to add to the model/group represented by the selectedString.
     * @param selectedString A string representing a model/group.
     */
    public void makeConcurrent(FenceAgentModel concurrentModel, String selectedString) {
        for (FenceAgentModel model : getItems()) {
            if (selectedString != null && selectedString.contains(createConcurrentOptionString(model))) {
                makeConcurrent(concurrentModel, model);
                return;
            }
        }
    }

    /**
     * Make the passed in concurrentModel concurrent with the targetModel. The target model is either a single
     * {@code FenceAgentModel} or a concurrent group. Once this method is done the concurrentModel will be part
     * of the targetModels concurrentList.
     * @param concurrentModel The model to add to the target model concurrent list.
     * @param targetModel The model the concurrent model will be added to.
     */
    private void makeConcurrent(final FenceAgentModel concurrentModel, final FenceAgentModel targetModel) {
        if (concurrentModel.getOrder().getEntity() > targetModel.getOrder().getEntity()) {
            concurrentModel.setOrder(targetModel.getOrder().getEntity());
        } else {
            targetModel.setOrder(concurrentModel.getOrder().getEntity());
        }
        targetModel.getConcurrentList().add(concurrentModel);
        getItems().remove(concurrentModel);
        notifyItemListeners();
        updateConcurrentList();
        targetModel.getConcurrentSelectList().setSelectedItem(createConcurrentOptionString(concurrentModel));
    }

    /**
     * Remove the passed in {@code FenceAgentModel} from the concurrent group it is in. If the model is not in any
     * concurrent group this will do nothing. After the model is removed from the concurrent group it is placed back
     * in the {@code FenceAgentListModel}s item list as a normal fence agent. If the concurrent group the model was
     * part of is reduced to 1 {@code FenceAgentModel} that will also be added to the item list.
     * @param value The {@code FenceAgentModel} to remove from the concurrent group.
     */
    public void removeConcurrent(FenceAgentModel value) {
        for (FenceAgentModel model : getItems()) {
            if (model.equals(value) && !model.getConcurrentList().isEmpty()) {
                //The main concurrent model is being removed, move all the remaining models to the first one in the
                //concurrent list.
                FenceAgentModel newMainConcurrent = model.getConcurrentList().get(0);
                newMainConcurrent.getConcurrentList().addAll(model.getConcurrentList());
                //Remove itself from the list.
                newMainConcurrent.getConcurrentList().remove(newMainConcurrent);
                newMainConcurrent.setOrder(value.getOrder().getEntity());
                getItems().add(newMainConcurrent);
                //Sort to make sure the 'new' is at the end.
                sortAndFixOrder();
                if (newMainConcurrent.getConcurrentList().isEmpty()) {
                    newMainConcurrent.getConcurrentSelectList().setIsAvailable(true);
                    newMainConcurrent.getConcurrentSelectList()
                        .setSelectedItem(newMainConcurrent.getConcurrentListFirstItem());
                }
                model.setOrder(getItems().size());
                model.getConcurrentList().clear();
                model.getConcurrentSelectList().setSelectedItem(model.getConcurrentListFirstItem());
                notifyItemListeners();
                return;
            } else {
                //Check if any of the concurrent models are the one being removed from the group.
                for (FenceAgentModel concurrentModel: model.getConcurrentList()) {
                    if (concurrentModel.equals(value)) {
                        model.getConcurrentList().remove(concurrentModel);
                        concurrentModel.getConcurrentSelectList().setSelectedItem(
                                concurrentModel.getConcurrentListFirstItem());
                        if (model.getConcurrentList().isEmpty()) {
                            model.getConcurrentSelectList().setIsAvailable(true);
                            model.getConcurrentSelectList().setSelectedItem(model.getConcurrentListFirstItem());
                        }
                        getItems().add(concurrentModel);
                        //Sort to make sure the 'new' is at the end.
                        sortAndFixOrder();
                        updateConcurrentList(concurrentModel, getItems());
                        value.setOrder(getItems().size());
                        notifyItemListeners();
                        return;
                    }
                }
            }
        }
    }

    /**
     * Notify any item listeners that the items have been changed.
     */
    public void notifyItemListeners() {
        List<FenceAgentModel> sortedList = sortAndFixOrder();
        setItems(sortedList);
        getItemsChangedEvent().raise(this, EventArgs.EMPTY);
        onPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$
    }

    /**
     * Sort the {@code FenceAgentModel}s based on their order, and fix any gaps in the order count.
     * @return Sorted {@code FenceAgentModel}s that don't have any gaps in the order attribute.
     */
    public List<FenceAgentModel> sortAndFixOrder() {
        List<FenceAgentModel> sortedList = (List<FenceAgentModel>) getItems();
        Collections.sort(sortedList, FenceAgentModel.orderComparable);
        //Fix any gaps in the list.
        for (FenceAgentModel model: sortedList) {
            model.setOrder(sortedList.indexOf(model) + 1);
        }
        return sortedList;
    }

    /**
     * Move the passed in {@code FenceAgentModel} up in the items list. Also change the order value of the passed in
     * model and the model it changed places with. If the model passed in is the first model don't do anything.
     * @param movingModel The model to move up.
     */
    public void moveUp(FenceAgentModel movingModel) {
        List<FenceAgentModel> sortedList = (List<FenceAgentModel>) getItems();
        int movingModelIndex = sortedList.indexOf(movingModel);
        if (movingModelIndex > 0) {
            sortedList.get(movingModelIndex).setOrder(sortedList.get(movingModelIndex).getOrder().getEntity() - 1);
            sortedList.get(movingModelIndex - 1)
                .setOrder(sortedList.get(movingModelIndex - 1).getOrder().getEntity() + 1);
            notifyItemListeners();
        }
    }

    /**
     * Move the passed in {@code FenceAgentModel} down in the items list. Also change the order value of the passed in
     * model and the model it changed places with. If the model passed in is the last model don't do anything.
     * @param movingModel The model to move down.
     */
    public void moveDown(FenceAgentModel movingModel) {
        List<FenceAgentModel> sortedList = (List<FenceAgentModel>) getItems();
        int movingModelIndex = sortedList.indexOf(movingModel);
        if (movingModelIndex < sortedList.size() - 1) {
            sortedList.get(movingModelIndex).setOrder(sortedList.get(movingModelIndex).getOrder().getEntity() + 1);
            sortedList.get(movingModelIndex + 1)
                .setOrder(sortedList.get(movingModelIndex + 1).getOrder().getEntity() - 1);
            notifyItemListeners();
        }
    }

    /**
     * Return a list of {@code FenceAgent}s based on the list of {@code FenceAgentModel}s containing in this list model
     * @return A list of {@code FenceAgent}s
     */
    public List<FenceAgent> getFenceAgents() {
        List<FenceAgent> agents = new LinkedList<>();
        for (FenceAgentModel agentModel: getItems()) {
            FenceAgent agent = createFenceAgentFromModel(agentModel);
            if (!agentModel.getConcurrentList().isEmpty()) {
                for (FenceAgentModel concurrentAgentModel: agentModel.getConcurrentList()) {
                    FenceAgent concurrentAgent = createFenceAgentFromModel(concurrentAgentModel);
                    agents.add(concurrentAgent);
                }
            }
            agents.add(agent);
        }
        return agents;
    }

    /**
     * Create a {@code FenceAgent} from the passed in {@code FenceAgentModel}
     * @param agentModel The model to create the {@code FenceAgent} out of.
     * @return A {@code FenceAgent} based on the passed in model.
     */
    private FenceAgent createFenceAgentFromModel(FenceAgentModel agentModel) {
        FenceAgent agent = new FenceAgent();
        agent.setIp(agentModel.getManagementIp().getEntity());
        agent.setUser(agentModel.getPmUserName().getEntity());
        agent.setPassword(agentModel.getPmPassword().getEntity());
        agent.setType(agentModel.getPmType().getSelectedItem());
        agent.setOptionsMap(agentModel.getPmOptionsMap());
        if (agentModel.getPmEncryptOptions().getEntity() != null) {
            agent.setEncryptOptions(agentModel.getPmEncryptOptions().getEntity());
        } else {
            agent.setEncryptOptions(false);
        }
        if (agentModel.getPmPort() != null && agentModel.getPmPort().getEntity() != null) {
            agent.setPort(agentModel.getPmPort().getEntity());
        }
        agent.setOrder(agentModel.getOrder().getEntity());
        return agent;
    }

    /**
     * Remove the passed in model from the items list and notify listeners of changes.
     * @param fenceAgentModel The model to remove.
     */
    public void removeItem(FenceAgentModel fenceAgentModel) {
        getItems().remove(fenceAgentModel);
        updateConcurrentList();
        sortAndFixOrder();
    }
}

