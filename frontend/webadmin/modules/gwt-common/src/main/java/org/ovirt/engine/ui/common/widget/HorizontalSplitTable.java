package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.dialog.ShapedButton;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class HorizontalSplitTable<M extends ListModel<T>, T> extends SplitTable<M, T> {

    interface WidgetUiBinder extends UiBinder<Widget, HorizontalSplitTable<?, ?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private static final CommonApplicationResources resources = AssetProvider.getResources();

    public HorizontalSplitTable(EntityModelCellTable<M> topTable,
            EntityModelCellTable<M> bottomTable,
            String topTitle,
            String bottomTitle) {
        super(topTable, bottomTable, topTitle, bottomTitle);
    }

    @Override
    protected ShapedButton createIncludeButton() {
        return new ShapedButton(resources.arrowDownNormal(),
                resources.arrowDownClick(),
                resources.arrowDownOver(),
                resources.arrowDownDisabled());
    }

    @Override
    protected ShapedButton createExcludeButton() {
        return new ShapedButton(resources.arrowUpNormal(),
                resources.arrowUpClick(),
                resources.arrowUpOver(),
                resources.arrowUpDisabled());
    }

    @Override
    protected void initWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }
}
