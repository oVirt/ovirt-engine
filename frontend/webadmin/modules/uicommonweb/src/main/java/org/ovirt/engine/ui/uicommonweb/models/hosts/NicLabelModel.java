package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

// TODO this class will be removed in- 'Change Setup Networks label mechanism' patch
public class NicLabelModel extends ListModel<ListModel<String>> {
    private List<String> originalLabels;
    private Collection<String> suggestedLabels;

    private Collection<VdsNetworkInterface> srcIfaces; // original interfaces composing this interface (more than
    // one in case this is a bond)
    private Set<String> containedIfaces; // names of the original interfaces
    private Map<String, String> labelToIface; // map from each label to the name of the interface that uses it
    // (possibly null)
    private Set<String> flushedLabels; // actual labels, as edited in the view

    public NicLabelModel(Collection<VdsNetworkInterface> srcIfaces,
            Collection<String> suggestedLabels,
            Map<String, String> labelToIface) {

        List<String> tmpOriginalLabels = new ArrayList<String>(); // union of labels attached originally to original
        // interface(s)

        this.srcIfaces = srcIfaces;
        this.labelToIface = labelToIface;

        containedIfaces = new HashSet<String>();
        for (VdsNetworkInterface iface : srcIfaces) {
            Set<String> labels = iface.getLabels();
            if (labels != null) {
                tmpOriginalLabels.addAll(labels);
            }
            containedIfaces.add(iface.getName());
        }

        setSuggestedLabels(suggestedLabels);
        setOriginalLabels(tmpOriginalLabels);
        initLabelModels();

        flushedLabels = new HashSet<String>();
    }

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

    public void validate() {
        boolean res = true;
        Set<String> editedLabels = new HashSet<String>();
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

        for (ListModel<String> labelModel : getItems()) {
            String label = labelModel.getSelectedItem();
            String usingIface = labelToIface.get(label);
            if (usingIface != null && !containedIfaces.contains(usingIface)) {
                labelModel.getInvalidityReasons().add(ConstantsManager.getInstance()
                        .getMessages()
                        .labelInUse(label, usingIface));
                labelModel.setIsValid(false);
            }

            res &= labelModel.getIsValid();
        }
        setIsValid(res);
    }

    /**
     * Flushes the labels as edited in the view into {@link #flushedLabels}.
     */
    private void flush() {
        flushedLabels.clear();
        flushedLabels.addAll(computeSelecetedLabels());
    }

    /**
     * Computes which labels have been removed from this interface.
     */
    public Collection<String> getRemovedLabels() {
        flush();
        Set<String> removedLabels = new HashSet<String>(getOriginalLabels());
        removedLabels.removeAll(flushedLabels);
        return removedLabels;
    }

    /**
     * Computes which labels have been added to the interface.
     */
    public Collection<String> getAddedLabels() {
        flush();
        Set<String> addedLabels = new HashSet<String>(flushedLabels);
        addedLabels.removeAll(getOriginalLabels());
        return addedLabels;
    }

    /**
     * Clears the labels from the original interfaces composing this interface, and committing the actual labels (after
     * removal/addition by the user).
     *
     * @param the
     *            interface to which the actual labels will be committed.
     */
    public void commit(VdsNetworkInterface dstIface) {
        for (VdsNetworkInterface iface : srcIfaces) {
            iface.setLabels(null);
        }
        flush();
        dstIface.setLabels(flushedLabels.isEmpty() ? null : flushedLabels);
    }

    /**
     * @return whether the labels model was changed (new labels were added or old were removed)
     */
    public boolean wasChanged() {
        return !getAddedLabels().isEmpty() || !getRemovedLabels().isEmpty();

    }
}
