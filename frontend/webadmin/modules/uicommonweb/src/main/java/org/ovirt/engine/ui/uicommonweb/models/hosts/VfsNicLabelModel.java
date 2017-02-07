package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VfsNicLabelModel extends ListModel<ListModel<String>> {

    public VfsNicLabelModel(List<String> originalLabels, Collection<String> suggestedLabels) {
        setOriginalLabels(originalLabels);
        setSuggestedLabels(suggestedLabels);
        initLabelModels();
    }

    private List<String> originalLabels;
    private Collection<String> suggestedLabels;
    private Collection<String> suggestedLabelsCache;

    protected void initLabelModels() {
        Collections.sort(originalLabels, new LexoNumericComparator());
        LinkedList<ListModel<String>> items = new LinkedList<>();
        for (String label : originalLabels) {
            ListModel<String> labelModel = new ListModel<>();
            labelModel.setItems(suggestedLabels);
            labelModel.setSelectedItem(label);
            items.add(labelModel);
        }
        setItems(items);
        suggestedLabelsCache = new TreeSet<>(suggestedLabels);
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

    public Set<String> computeSelectedLabels() {
        Set<String> selectedLabels = new HashSet<>();
        for (ListModel<String> labelModel : getItems()) {
            selectedLabels.add(labelModel.getSelectedItem());
        }
        return selectedLabels;
    }

    public void validate() {
        boolean res = true;
        Set<String> editedLabels = new HashSet<>();
        for (ListModel<String> labelModel : getItems()) {
            labelModel.validateSelectedItem(new IValidation[] { new AsciiNameValidation() });

            String label = labelModel.getSelectedItem();

            if (editedLabels.contains(label)) {
                labelModel.getInvalidityReasons().add(ConstantsManager.getInstance().getConstants().duplicateLabel());
                labelModel.setIsValid(false);
            }
            editedLabels.add(label);

            res &= labelModel.getIsValid();
        }
        setIsValid(res);
    }

    public void updateSuggestedLabels() {
        suggestedLabels = new TreeSet<>(suggestedLabelsCache);
        suggestedLabels.removeAll(computeSelectedLabels());
        for (ListModel<String> labelModel : getItems()) {
            labelModel.setItems(suggestedLabels, labelModel.getSelectedItem());
        }
    }
}
