package org.ovirt.engine.ui.webadmin.widget.renderer;

import java.util.ArrayList;

import org.ovirt.engine.ui.common.widget.label.TextBoxLabelBase;

import com.google.gwt.text.shared.AbstractRenderer;

public class DetailsRenderer<T extends ArrayList<TextBoxLabelBase<V>>, V> extends AbstractRenderer<T> {

    String[] delimiters;

    public DetailsRenderer(String... delimiters) {
        super();

        this.delimiters = delimiters;
    }

    @Override
    public String render(T widgets) {
        StringBuilder formattedStr = new StringBuilder();

        for (int i = 0; i < widgets.size(); i++) {
            formattedStr.append(widgets.get(i).getElement().getInnerHTML()).append(" ").append(delimiters[i]); //$NON-NLS-1$
            if (i < widgets.size() - 1) {
                formattedStr.append(", "); //$NON-NLS-1$
            }
        }
        return formattedStr.toString();
    }

}
