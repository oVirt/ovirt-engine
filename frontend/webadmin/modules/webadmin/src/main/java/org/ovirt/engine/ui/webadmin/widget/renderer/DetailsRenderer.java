package org.ovirt.engine.ui.webadmin.widget.renderer;

import java.util.ArrayList;

import org.ovirt.engine.ui.common.widget.label.TextBoxLabelBase;
import com.google.gwt.text.shared.AbstractRenderer;

public class DetailsRenderer<V> extends AbstractRenderer<ArrayList<TextBoxLabelBase<V>>> {

    String[] delimiters;

    public DetailsRenderer(String... delimiters) {
        super();
        this.delimiters = delimiters;
    }

    @Override
    public String render(ArrayList<TextBoxLabelBase<V>> widgets) {
        StringBuilder formattedStr = new StringBuilder();

        for (int i = 0; i < widgets.size(); i++) {
            formattedStr.append(widgets.get(i).getText()).append(" ").append(delimiters[i]); //$NON-NLS-1$
            if (i < widgets.size() - 1) {
                formattedStr.append(", "); //$NON-NLS-1$
            }
        }
        return formattedStr.toString();
    }

}
