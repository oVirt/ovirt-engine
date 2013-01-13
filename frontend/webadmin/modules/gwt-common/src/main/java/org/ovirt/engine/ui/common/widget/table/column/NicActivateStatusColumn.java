package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class NicActivateStatusColumn<T> extends SafeHtmlWithSafeHtmlTooltipColumn<T> {
    CommonApplicationResources resources = GWT.create(CommonApplicationResources.class);
    CommonApplicationConstants constants = GWT.create(CommonApplicationConstants.class);
    CommonApplicationTemplates templates = GWT.create(CommonApplicationTemplates.class);
    @Override
    public SafeHtml getValue(T object) {
        VmNetworkInterface vnic = null;
        if (object instanceof VmNetworkInterface) {
            vnic = (VmNetworkInterface) object;
        } else if (object instanceof PairQueryable && ((PairQueryable) object).getFirst() instanceof VmNetworkInterface) {
            vnic = ((PairQueryable<VmNetworkInterface, VM>) object).getFirst();
        }

        if (vnic != null) {
            return vnic.isPlugged() && vnic.isLinked() ?
                    SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.upImage()).getHTML())
                    : SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.downImage()).getHTML());
        }

        return null;
    }

    @Override
    public SafeHtml getTooltip(T object) {
        VmNetworkInterface vnic = null;
        if (object instanceof VmNetworkInterface) {
            vnic = (VmNetworkInterface) object;
        } else if (object instanceof PairQueryable && ((PairQueryable) object).getFirst() instanceof VmNetworkInterface) {
            vnic = ((PairQueryable<VmNetworkInterface, VM>) object).getFirst();
        }

        StringBuilder tooltip = new StringBuilder(""); //$NON-NLS-1$
        boolean vnicExist = vnic != null;
        if (vnicExist && vnic.isPlugged()) {
            tooltip =
                    tooltip.append(templates.cardStatus(constants.pluggedNetworkInteface()).asString());
        } else {
            tooltip =
                    tooltip.append(templates.cardStatus(constants.unpluggedNetworkInteface()).asString());
        }

        tooltip = tooltip.append("<BR>"); //$NON-NLS-1$
        if (vnicExist && vnic.isLinked()) {
            tooltip =
                    tooltip.append(templates.linkState(constants.linkedNetworkInteface())
                            .asString());
        } else {
            tooltip =
                    tooltip.append(templates.linkState(constants.unlinkedNetworkInteface())
                            .asString());
        }

        return SafeHtmlUtils.fromTrustedString(tooltip.toString());
    }
}
