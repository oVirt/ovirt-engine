package org.ovirt.engine.ui.uicommonweb.models.tags;

import org.ovirt.engine.ui.uicompat.EventArgs;

@SuppressWarnings("unused")
public final class TagEventArgs extends EventArgs {
    private TagModel privateTag;

    public TagModel getTag() {
        return privateTag;
    }

    private void setTag(TagModel value) {
        privateTag = value;
    }

    public TagEventArgs(TagModel tag) {
        setTag(tag);
    }
}
