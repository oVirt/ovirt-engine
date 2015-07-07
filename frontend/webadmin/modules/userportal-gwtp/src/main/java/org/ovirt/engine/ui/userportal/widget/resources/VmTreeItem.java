package org.ovirt.engine.ui.userportal.widget.resources;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

/**
 * A tree item which is bound to VM and can set up it's state according to the provided list of selected VMs
 */
public class VmTreeItem extends TreeItem {
    private Guid id;

    private VM vm;

    public VmTreeItem(Widget widget, VM vm) {
        super(widget);
        this.id = vm.getId();
        this.vm = vm;
    }

    public void setState(List<VM> selected) {
        setState(isIn(selected));
    }

    public VM getVm() {
        return vm;
    }

    private boolean isIn(List<VM> selected) {
        for (VM other : selected) {
            if (isSame(other)) {
                return true;
            }
        }

        return false;
    }

    private boolean isSame(VM other) {
        if (other == null) {
            return false;
        }

        if (other.getId() == null) {
            return false;
        }

        return id.equals(other.getId());
    }

    public void reset() {
        this.vm = null;
        this.id = null;
    }
}
