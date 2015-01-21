package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.resources.client.CssResource;

public interface DaysOfMonthSelectorCss extends CssResource {
    @ClassName("normalFlexTableCell")
    String normalFlexTableCell();

    @ClassName("selectedFlexTableCell")
    String selectedFlexTableCell();

    @ClassName("daysOfMonthWidget")
    String daysOfMonthWidget();
}
