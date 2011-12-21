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
        String formattedStr = "";

        for (int i = 0; i < widgets.size(); i++) {
            formattedStr += widgets.get(i).getElement().getInnerHTML() + " " + delimiters[i];
            if (i < widgets.size() - 1) {
                formattedStr += ", ";
            }
        }

        return formattedStr;
    }
}
