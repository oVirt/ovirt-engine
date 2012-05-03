package org.ovirt.engine.ui.uicommonweb.models.hosts;

@SuppressWarnings("unused")
public class HostVLanByNameComparer implements java.util.Comparator<HostVLan>
{
    @Override
    public int compare(HostVLan x, HostVLan y)
    {
        return x.getName().compareTo(y.getName());
    }
}
