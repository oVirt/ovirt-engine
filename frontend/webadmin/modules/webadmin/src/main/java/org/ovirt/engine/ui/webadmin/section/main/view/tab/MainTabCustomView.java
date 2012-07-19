package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabCustomPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MainTabCustomView extends AbstractView implements MainTabCustomPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, MainTabCustomView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    IFrameElement iframeElement;

    @Inject
    public MainTabCustomView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void setContentUrl(String url) {
        iframeElement.setSrc(url);
    }

}
