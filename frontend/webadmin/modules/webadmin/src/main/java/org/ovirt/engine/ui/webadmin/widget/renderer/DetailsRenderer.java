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
        String formattedStr = ""; //$NON-NLS-1$

        for (int i = 0; i < widgets.size(); i++) {
            formattedStr += widgets.get(i).getElement().getInnerHTML() + " " + delimiters[i]; //$NON-NLS-1$
            if (i < widgets.size() - 1) {
                formattedStr += ", "; //$NON-NLS-1$
            }
        }

        return formattedStr;
    }
}
