package org.ovirt.engine.ui.uicommonweb.models;

/**
 * Implements this interface if type should be aware of what is selected in system tree. Assignment controlled by a
 * CommonModel.
 */
@SuppressWarnings("unused")
public interface ISupportSystemTreeContext {
    SystemTreeItemModel getSystemTreeSelectedItem();

    void setSystemTreeSelectedItem(SystemTreeItemModel value);
}
