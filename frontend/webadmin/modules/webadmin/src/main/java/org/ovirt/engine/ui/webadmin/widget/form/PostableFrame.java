package org.ovirt.engine.ui.webadmin.widget.form;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * A {@link Frame} that has it's contents POSTed
 */
public class PostableFrame extends Frame {

    private final FormPanel form;
    private final Map<String, List<Hidden>> hiddens = new HashMap<>();

    /**
     * Create a new {@link PostableFrame} with the specified name.
     *
     * @param frameName
     *            Frame name: MUST be unique.<BR>
     *            Two {@link PostableFrame}s that operate separately on the same page, must have different names.
     */
    public PostableFrame(String frameName) {
        // Form
        form = new FormPanel(frameName);
        form.setMethod(FormPanel.METHOD_GET);
        form.setSize("0", "0"); //$NON-NLS-1$ //$NON-NLS-2$

        // Frame
        getElement().setAttribute("name", frameName); //$NON-NLS-1$
    }

    /**
     * POST the frame
     */
    public void post() {
        // attach form lazily
        if (!form.isAttached()) {
            attachForm();
        }
        form.submit();
        dettachForm();
    }

    /**
     * Set a POST parameter
     */
    public void setParameter(String name, String value) {
        // get from hiddens map
        if (hiddens.containsKey(name)) {
            for (Hidden hidden : hiddens.get(name)) {
                form.getElement().removeChild(hidden.getElement());
                hiddens.remove(name);
            }
        }

        addParameter(name, value);

    }

    /**
     * Add a POST parameter
     */
    public void addParameter(String name, String value) {
        // add hidden to form
        Hidden hidden = new Hidden(name, value);
        form.getElement().appendChild(hidden.getElement());

        List<Hidden> oldHiddenList = hiddens.get(name);

        if (oldHiddenList == null) {
            hiddens.put(name, new LinkedList<>(Collections.singletonList(hidden)));
        } else {
            oldHiddenList.add(hidden);
        }
    }

    /**
     * Remove old POST parameters
     */
    public void removeOldParams() {
        for (List<Hidden> hiddenList : hiddens.values()) {
            for (Hidden hidden : hiddenList) {
                form.getElement().removeChild(hidden.getElement());
            }
        }
        hiddens.clear();
    }

    @Override
    public void setUrl(String url) {
        form.setAction(url);
    }

    private void attachForm() {
        RootPanel.get().add(form);
    }

    private void dettachForm() {
        RootPanel.get().remove(form);
    }
}
