package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;

public abstract class LunTextColumn extends SafeHtmlColumn<LunModel> {

    public LunTextColumn() {
    }

    @Override
    public final SafeHtml getValue(LunModel object) {
        ScrollableTextCell.Template template = GWT.create(ScrollableTextCell.Template.class);

        String color = "";

        if (object != null) {
            if (object.getIsIncluded() || object.getIsSelected()) {
                color = "black";
            }
            else if (!object.getIsAccessible()) {
                color = "orange";
            }
            else {
                color = "grey";
            }
        }

        return template.input(getRawValue(object), "color:" + color);
    }

    public abstract String getRawValue(LunModel object);
}
