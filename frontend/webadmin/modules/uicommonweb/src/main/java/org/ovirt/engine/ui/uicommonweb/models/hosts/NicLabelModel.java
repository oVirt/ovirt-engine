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

public class NicLabelModel extends ListModel<ListModel<String>> {

    private final Collection<VdsNetworkInterface> srcIfaces; // original interfaces composing this interface (more than one in case this is a bond)
    private final Set<String> containedIfaces; // names of the original interfaces
    private final List<String> originalLabels; // union of labels attached originally to original interface(s)
    private final Collection<String> suggestedLabels; // pre-existing DC labels that aren't yet assigned to an interface
    private final Map<String, String> labelToIface; // map from each label to the name of the interface that uses it (possibly null)
    private final Set<String> flushedLabels; // actual labels, as edited in the view

    public Collection<String> getSuggestedLabels() {
        return suggestedLabels;
    }

    public NicLabelModel(Collection<VdsNetworkInterface> srcIfaces,
            Collection<String> suggestedLabels,
            Map<String, String> labelToIface) {

        this.srcIfaces = srcIfaces;
        this.suggestedLabels = suggestedLabels;
        this.labelToIface = labelToIface;

        originalLabels = new ArrayList<String>();
        containedIfaces = new HashSet<String>();
        for (VdsNetworkInterface iface : srcIfaces) {
            Set<String> labels = iface.getLabels();
            if (labels != null) {
                originalLabels.addAll(labels);
            }
            containedIfaces.add(iface.getName());
        }

        Collections.sort(originalLabels, new LexoNumericComparator());
        LinkedList<ListModel<String>> items = new LinkedList<ListModel<String>>();
        for (String label : originalLabels) {
            ListModel<String> labelModel = new ListModel<String>();
            labelModel.setItems(suggestedLabels);
            labelModel.setSelectedItem(label);
            items.add(labelModel);
        }
        setItems(items);

        flushedLabels = new HashSet<String>();
    }

    public boolean validate() {
        Set<String> editedLabels = new HashSet<String>();
        boolean res = true;
        for (ListModel<String> labelModel : getItems()) {
            labelModel.validateSelectedItem(new IValidation[] { new AsciiNameValidation() });

            String label = labelModel.getSelectedItem();
            String usingIface = labelToIface.get(label);
            if (usingIface != null && !containedIfaces.contains(usingIface)) {
                labelModel.getInvalidityReasons().add(ConstantsManager.getInstance()
                        .getMessages()
                        .labelInUse(label, usingIface));
                labelModel.setIsValid(false);
            }

            if (editedLabels.contains(label)) {
                labelModel.getInvalidityReasons().add(ConstantsManager.getInstance().getConstants().duplicateLabel());
                labelModel.setIsValid(false);
            }
            editedLabels.add(label);

            res &= labelModel.getIsValid();
        }
        return res;
    }

    /**
     * Flushes the labels as edited in the view into {@link #flushedLabels}.
     */
    private void flush() {
        flushedLabels.clear();
        for (ListModel<String> labelModel : getItems()) {
            flushedLabels.add(labelModel.getSelectedItem());
        }
    }

    /**
     * Computes which labels have been removed from this interface.
     */
    public Collection<String> getRemovedLabels() {
        flush();
        Set<String> removedLabels = new HashSet<String>(originalLabels);
        removedLabels.removeAll(flushedLabels);
        return removedLabels;
    }

    /**
     * Computes which labels have been added to the interface.
     */
    public Collection<String> getAddedLabels() {
        flush();
        Set<String> addedLabels = new HashSet<String>(flushedLabels);
        addedLabels.removeAll(originalLabels);
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

}
