package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.widget.dialog.ShapedButton;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class VerticalSplitTable<T> extends SplitTable<T> {

    interface WidgetUiBinder extends UiBinder<Widget, VerticalSplitTable<?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private static CommonApplicationResources resources = GWT.create(CommonApplicationResources.class);

    public VerticalSplitTable(EntityModelCellTable<ListModel<T>> excludedTable,
            EntityModelCellTable<ListModel<T>> includedTable,
            String excludedTitle,
            String includedTitle) {
        super(excludedTable, includedTable, excludedTitle, includedTitle);
    }

    @Override
    protected ShapedButton createIncludeButton() {
        return new ShapedButton(resources.arrowRightNormal(),
                resources.arrowRightClick(),
                resources.arrowRightOver(),
                resources.arrowRightDisabled());
    }

    @Override
    protected ShapedButton createExcludeButton() {
        return new ShapedButton(resources.arrowLeftNormal(),
                resources.arrowLeftClick(),
                resources.arrowLeftOver(),
                resources.arrowLeftDisabled());
    }

    @Override
    protected void initWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }
}
