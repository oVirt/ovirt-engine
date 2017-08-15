package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DynamicUrlContentPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DynamicUrlContentView extends AbstractView implements DynamicUrlContentPresenter.ViewDef {

    private static final String GWT_FRAME = "gwt-Frame"; // $NON-NLS-1$

    // For some reason all browsers have some size around the iframe that you cannot hide. But you need to
    // subtract 6 pixels otherwise a second scrollbar will appear. This will result in some white space below the
    // iframe.
    private static final int IFRAME_SUBTRACT = 6;

    interface ViewUiBinder extends UiBinder<Widget, DynamicUrlContentView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    Frame frame;

    @Inject
    public DynamicUrlContentView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Window.addResizeHandler(e -> resizeFrame());
        frame.removeStyleName(GWT_FRAME);
    }

    @Override
    public void onAttach() {
        super.onAttach();
        // Need to do an initial resize after the element gets attached. Note the onAttach comes from ViewImpl which
        // is not a Widget, so I can't use onLoad which one should normally use instead of onAttach.
        resizeFrame();
    }

    private void resizeFrame() {
        int top = frame.getParent().getAbsoluteTop();
        int windowHeight = Window.getClientHeight();
        int newHeight = windowHeight - top;
        // For some reason we need to give some space around the frame or it will cause a second scroll-bar.
        newHeight -= IFRAME_SUBTRACT;
        if (top > 0 && top < windowHeight) {
            frame.getElement().getStyle().setHeight(newHeight, Unit.PX);
        }
    }

    @Override
    public void setContentUrl(String url) {
        frame.setUrl(url);
    }
}
