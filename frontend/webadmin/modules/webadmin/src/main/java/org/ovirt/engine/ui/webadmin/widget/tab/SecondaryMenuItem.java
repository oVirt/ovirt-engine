package org.ovirt.engine.ui.webadmin.widget.tab;

public interface SecondaryMenuItem extends PrimaryMenuItem {
    /**
     * Get the {@code PrimaryMenuItem} this secondary menu item belongs to.
     * @return The primary menu item.
     */
    PrimaryMenuItem getPrimaryMenu();
}
