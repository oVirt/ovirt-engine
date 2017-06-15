package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DynamicUrlContentTabPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.FrameElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DynamicUrlContentTabView extends AbstractView implements DynamicUrlContentTabPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, DynamicUrlContentTabView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    Frame frame;

    @Inject
    public DynamicUrlContentTabView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        frame.addLoadHandler(new LoadHandler() {

            @Override
            public void onLoad(LoadEvent event) {
                frame.getElement().getStyle().setHeight(getFrameBodyScrollHeight(frame), Unit.PX);
            }
        });
    }

    private int getFrameBodyScrollHeight(Frame frame) {
        return getFrameElement(frame).getContentDocument().getBody().getScrollHeight();
    }

    private FrameElement getFrameElement(Frame frame) {
        return frame.getElement().cast();
    }

    @Override
    public void setContentUrl(String url) {
        frame.setUrl(url);
    }
}
