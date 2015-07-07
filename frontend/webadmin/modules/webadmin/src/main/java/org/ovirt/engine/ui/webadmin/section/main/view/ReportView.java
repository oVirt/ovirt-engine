package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.webadmin.section.main.presenter.ReportPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.form.PostableFrame;
import com.google.gwt.dom.client.Style.Position;

public class ReportView extends AbstractView implements ReportPresenterWidget.ViewDef {

    private final PostableFrame frame;

    @Inject
    public ReportView() {
        frame = new PostableFrame("_blank"); //$NON-NLS-1$
        frame.setSize("100%", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
        frame.getElement().getStyle().setPosition(Position.ABSOLUTE);
        initWidget(frame);
    }

    @Override
    public void setFrameUrl(String url) {
        frame.setUrl(url);
    }

    @Override
    public void setFrameParams(Map<String, List<String>> params) {
        for (Entry<String, List<String>> entry : params.entrySet()) {
            for (String param : entry.getValue()) {
                frame.addParameter(entry.getKey(), param);
            }
        }
    }

    @Override
    public void postFrame() {
        frame.post();
    }

}
