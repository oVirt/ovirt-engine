package org.ovirt.engine.ui.uicommonweb.models.tags;

import org.ovirt.engine.ui.uicompat.EventArgs;

@SuppressWarnings("unused")
public final class TagModelSelectionEventArgs extends EventArgs {
    private boolean privateSelection;

    public boolean getSelection() {
        return privateSelection;
    }

    private void setSelection(boolean value) {
        privateSelection = value;
    }

    public TagModelSelectionEventArgs(boolean selection) {
        setSelection(selection);
    }
}
