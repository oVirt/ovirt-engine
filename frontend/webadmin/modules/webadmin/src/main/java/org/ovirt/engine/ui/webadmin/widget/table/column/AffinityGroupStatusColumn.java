package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class AffinityGroupStatusColumn extends AbstractColumn<AffinityGroup, AffinityGroup> {

    public interface CellTemplates extends SafeHtmlTemplates {
        @Template("<div id=\"{1}\" data-status=\"{2}\">{0}</div>")
        SafeHtml statusTemplate(SafeHtml statusHtml, String id, String status);

        @Template("<div id=\"{2}\" data-status=\"{3}\">{1}{0}</div>")
        SafeHtml statusWithIconTemplate(SafeHtml statusHtml, SafeHtml statusIcon, String id, String status);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationResources resources = AssetProvider.getResources();
    private static CellTemplates cellTemplate = GWT.create(CellTemplates.class);

    private static String STATUS_OK = "ok"; //$NON-NLS-1$
    private static String STATUS_BROKEN = "broken"; //$NON-NLS-1$

    public AffinityGroupStatusColumn() {
        super(new AbstractCell<AffinityGroup>() {
            @Override
            public void render(Context context, AffinityGroup group, SafeHtmlBuilder sb, String id) {
                if (group == null) {
                    return;
                }

                if (Boolean.TRUE.equals(group.getBroken())) {
                    sb.append(cellTemplate.statusWithIconTemplate(
                            SafeHtmlUtils.fromSafeConstant(constants.affinityGroupStatusBroken()),
                            AbstractImagePrototype.create(resources.alertImage()).getSafeHtml(),
                            id,
                            STATUS_BROKEN
                    ));
                } else {
                    sb.append(cellTemplate.statusTemplate(
                            SafeHtmlUtils.fromSafeConstant(constants.affinityGroupStatusOk()),
                            id,
                            STATUS_OK
                    ));
                }
            }
        });
    }

    @Override
    public AffinityGroup getValue(AffinityGroup object) {
        return object;
    }

    public void makeSortable() {
        makeSortable(Comparator.nullsFirst(Comparator.comparing(AffinityGroup::getBroken)));
    }


}
