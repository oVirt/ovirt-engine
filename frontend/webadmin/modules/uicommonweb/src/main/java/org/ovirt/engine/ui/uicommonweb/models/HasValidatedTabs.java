package org.ovirt.engine.ui.uicommonweb.models;

import java.util.Set;

public interface HasValidatedTabs {
    /**
     * Returns the set of invalid tab names.
     * @return The {@code Set} of invalid tab names.
     */
    Set<TabName> getInvalidTabs();
}
