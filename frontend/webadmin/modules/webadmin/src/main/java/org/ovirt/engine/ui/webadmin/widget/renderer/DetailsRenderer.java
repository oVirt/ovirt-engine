package org.ovirt.engine.ui.webadmin.widget.renderer;

import java.util.ArrayList;

import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.ui.ValueLabel;

public class DetailsRenderer<V> extends AbstractRenderer<ArrayList<ValueLabel<V>>> {

    String[] delimiters;

    public DetailsRenderer(String... delimiters) {
        super();
        this.delimiters = delimiters;
    }

    @Override
    public String render(ArrayList<ValueLabel<V>> widgets) {
        StringBuilder formattedStr = new StringBuilder();

        for (int i = 0; i < widgets.size(); i++) {
            formattedStr.append(widgets.get(i).getElement().getInnerText()).append(" ").append(delimiters[i]); //$NON-NLS-1$
            if (i < widgets.size() - 1) {
                formattedStr.append(", "); //$NON-NLS-1$
            }
        }
        return formattedStr.toString();
    }

}
