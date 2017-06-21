package org.ovirt.engine.ui.common.view;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.presenter.ExpandAllButtonPresenterWidget;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class ExpandAllButtonView extends AbstractView implements ExpandAllButtonPresenterWidget.ViewDef {

    public interface ViewUiBinder extends UiBinder<Widget, ExpandAllButtonView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    Button expandAllButton;

    public ExpandAllButtonView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public HasClickHandlers getButton() {
        return expandAllButton;
    }

    @Override
    public void switchToExpandAll() {
        expandAllButton.setText(constants.expandAll());
        expandAllButton.setIcon(IconType.ANGLE_DOUBLE_DOWN);
    }

    @Override
    public void switchToCollapseAll() {
        expandAllButton.setText(constants.collapseAll());
        expandAllButton.setIcon(IconType.ANGLE_DOUBLE_RIGHT);
    }

}
