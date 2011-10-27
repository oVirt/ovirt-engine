package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

//C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the methods implemented will need adjustment:
public class VmTemplateComparerByDiskSize implements
        java.util.Comparator<VmTemplate>, Serializable {
    private static final long serialVersionUID = -1620249078971769528L;

    @Override
    public int compare(VmTemplate x, VmTemplate y) {
        return Compare(x, y);
    }

    public int Compare(VmTemplate x, VmTemplate y) {
        return (int) (x.getActualDiskSize() - y.getActualDiskSize());
    }

    public VmTemplateComparerByDiskSize() {
    }
}
