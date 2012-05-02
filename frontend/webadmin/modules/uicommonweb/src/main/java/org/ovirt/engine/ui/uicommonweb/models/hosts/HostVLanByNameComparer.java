package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Comparator;

@SuppressWarnings("unused")
public class HostVLanByNameComparer implements Comparator<HostVLan>
{
    @Override
    public int compare(HostVLan x, HostVLan y)
    {
        return x.getName().compareTo(y.getName());
    }
}
