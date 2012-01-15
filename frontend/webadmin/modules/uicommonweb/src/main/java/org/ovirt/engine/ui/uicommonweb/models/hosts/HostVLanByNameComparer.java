package org.ovirt.engine.ui.uicommonweb.models.hosts;

//C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the methods implemented will need adjustment:
@SuppressWarnings("unused")
public class HostVLanByNameComparer implements java.util.Comparator<HostVLan>
{
    @Override
    public int compare(HostVLan x, HostVLan y)
    {
        return x.getName().compareTo(y.getName());
    }
}
