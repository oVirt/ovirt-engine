package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.dialog.ShapedButton;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class VerticalSplitTable<M extends ListModel<T>, T> extends SplitTable<M, T> {

    interface WidgetUiBinder extends UiBinder<Widget, VerticalSplitTable<?, ?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private static final CommonApplicationResources resources = AssetProvider.getResources();

    public VerticalSplitTable(EntityModelCellTable<M> excludedTable,
            EntityModelCellTable<M> includedTable,
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
