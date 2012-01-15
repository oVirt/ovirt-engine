package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.compat.Guid;

@SuppressWarnings("unused")
public class DataBag
{
    private Guid privateDataCenterId;

    public Guid getDataCenterId()
    {
        return privateDataCenterId;
    }

    public void setDataCenterId(Guid value)
    {
        privateDataCenterId = value;
    }

    private Guid privateClusterId;

    public Guid getClusterId()
    {
        return privateClusterId;
    }

    public void setClusterId(Guid value)
    {
        privateClusterId = value;
    }

    private Guid privateOldClusterId;

    public Guid getOldClusterId()
    {
        return privateOldClusterId;
    }

    public void setOldClusterId(Guid value)
    {
        privateOldClusterId = value;
    }
}
