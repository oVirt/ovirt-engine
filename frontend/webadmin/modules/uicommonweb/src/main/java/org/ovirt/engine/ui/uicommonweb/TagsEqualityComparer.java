package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.ui.uicompat.IEqualityComparer;

@SuppressWarnings("unused")
public class TagsEqualityComparer implements IEqualityComparer<Tags> {
    @Override
    public boolean equals(Tags x, Tags y) {
        return x.gettag_id().equals(y.gettag_id());
    }

    public int hashCode(Tags tag) {
        return tag.gettag_id().hashCode();
    }
}
