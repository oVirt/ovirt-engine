package org.ovirt.engine.ui.userportal.widget;

import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ValueLabel;

public class ToStringEntityModelLabel extends ValueLabel<Object> {

    protected ToStringEntityModelLabel() {
        super(new AbstractRenderer<Object>() {

            @Override
            public String render(Object object) {
                if (object == null) {
                    return ""; //$NON-NLS-1$
                }
                return object.toString();
            }
        });

        // by default, there is a space
        DOM.setInnerHTML(getElement(), "&nbsp;"); //$NON-NLS-1$
    }

}
