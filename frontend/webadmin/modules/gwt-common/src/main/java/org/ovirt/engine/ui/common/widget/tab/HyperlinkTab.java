package org.ovirt.engine.ui.common.widget.tab;

import org.ovirt.engine.ui.common.widget.Align;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.TabData;

public class HyperlinkTab extends AbstractTab {

    @UiField
    Label label;

    interface WidgetUiBinder extends UiBinder<Widget, HyperlinkTab> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public HyperlinkTab(TabData tabData, AbstractTabPanel tabPanel) {
        super(tabData, tabPanel);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void setAlign(Align align) {
    }

    @Override
    public void activate() {
        label.getElement().getStyle().setFontWeight(FontWeight.BOLD);
    }

    @Override
    public void deactivate() {
        label.getElement().getStyle().setFontWeight(FontWeight.NORMAL);
    }

    @Override
    public String getText() {
        return label.getText();
    }

    @Override
    public void setText(String text) {
        label.setText(text);
    }

}
