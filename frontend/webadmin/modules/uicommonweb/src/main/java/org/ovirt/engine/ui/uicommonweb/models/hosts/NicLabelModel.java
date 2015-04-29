package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public abstract class NicLabelModel extends ListModel<ListModel<String>> {
    private List<String> originalLabels;
    private Collection<String> suggestedLabels;

    protected void initLabelModels() {
        Collections.sort(originalLabels, new LexoNumericComparator());
        LinkedList<ListModel<String>> items = new LinkedList<ListModel<String>>();
        for (String label : originalLabels) {
            ListModel<String> labelModel = new ListModel<String>();
            labelModel.setItems(suggestedLabels);
            labelModel.setSelectedItem(label);
            items.add(labelModel);
        }
        setItems(items);
    }

    public Collection<String> getSuggestedLabels() {
        return suggestedLabels;
    }

    public void setSuggestedLabels(Collection<String> suggestedLabels) {
        this.suggestedLabels = suggestedLabels;
    }

    public List<String> getOriginalLabels() {
        return originalLabels;
    }

    public void setOriginalLabels(List<String> originalLabels) {
        this.originalLabels = originalLabels;
    }

    public Set<String> computeSelecetedLabels() {
        Set<String> selectedLabels = new HashSet<>();
        selectedLabels.clear();
        for (ListModel<String> labelModel : getItems()) {
            selectedLabels.add(labelModel.getSelectedItem());
        }
        return selectedLabels;
    }
}
