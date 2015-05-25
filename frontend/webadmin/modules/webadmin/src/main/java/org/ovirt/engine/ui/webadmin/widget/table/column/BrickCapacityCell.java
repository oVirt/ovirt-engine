package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.utils.SizeConverter.SizeUnit;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class BrickCapacityCell<P extends BrickProperties> extends GlusterCapacityCell<P> {

    @Override
    public void render(Context context, BrickProperties value, SafeHtmlBuilder sb, String id) {
        if(value != null) {
            setFreeSize(value.getFreeSize());
            setTotalSize(value.getTotalSize());
            setUsedSize(value.getTotalSize() - value.getFreeSize());
            setInUnit(SizeUnit.MiB);
        }
        super.render(context, value == null ? null : value, sb, id);
    }
}
