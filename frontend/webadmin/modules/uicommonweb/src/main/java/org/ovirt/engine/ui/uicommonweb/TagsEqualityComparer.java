package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.ui.uicompat.IEqualityComparer;

@SuppressWarnings("unused")
public class TagsEqualityComparer implements IEqualityComparer<Tags> {
    @Override
    public boolean equals(Tags x, Tags y) {
        return x.getTagId().equals(y.getTagId());
    }

    public int hashCode(Tags tag) {
        return tag.getTagId().hashCode();
    }
}
