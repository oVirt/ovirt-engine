package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class PfNicLabelModel extends NicLabelModel {

    private Collection<VdsNetworkInterface> srcIfaces; // original interfaces composing this interface (more than
                                                             // one in case this is a bond)
    private Set<String> containedIfaces; // names of the original interfaces
    private Map<String, String> labelToIface; // map from each label to the name of the interface that uses it
                                                    // (possibly null)
    private Set<String> flushedLabels; // actual labels, as edited in the view

    public PfNicLabelModel() {
    }

    public PfNicLabelModel(Collection<VdsNetworkInterface> srcIfaces,
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

    @Override
    public void validate() {
        super.validate();
        boolean res = getIsValid();
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

}
