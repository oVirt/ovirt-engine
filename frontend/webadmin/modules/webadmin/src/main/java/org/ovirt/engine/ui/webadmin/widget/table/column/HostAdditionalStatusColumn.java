package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.table.cell.HostAdditionalStatusCell;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class HostAdditionalStatusColumn<S> extends AbstractColumn<S, VDS> {

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    public HostAdditionalStatusColumn() {
        super(new HostAdditionalStatusCell());
    }

    @Override
    public VDS getValue(S object) {
        if (object instanceof VDS){
            return (VDS) object;
        }

        return null;
    }

    @Override
    public SafeHtml getTooltip(S object) {
        VDS host = getValue(object);

        if (host != null && host.isUpdateAvailable()) {
            String tooltip = constants.updateAvailable();
            return SafeHtmlUtils.fromSafeConstant(tooltip);
        }

        return null;
    }
}
