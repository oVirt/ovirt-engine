package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.ui.uicompat.IEqualityComparer;

@SuppressWarnings("unused")
public class TagsEqualityComparer implements IEqualityComparer<tags>
{
    @Override
    public boolean equals(tags x, tags y)
    {
        return x.gettag_id().equals(y.gettag_id());
    }

    public int hashCode(tags tag)
    {
        return tag.gettag_id().hashCode();
    }
}
