package org.ovirt.engine.ui.webadmin.uicommon;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.frontend.FrontendFailureEventArgs;
import org.ovirt.engine.ui.frontend.Message;
import org.ovirt.engine.ui.webadmin.system.ErrorPopupManager;

import com.google.inject.Inject;

public class FrontendFailureEventListener implements IEventListener {

    private final ErrorPopupManager errorPopupManager;

    @Inject
    public FrontendFailureEventListener(ErrorPopupManager errorPopupManager) {
        this.errorPopupManager = errorPopupManager;
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        FrontendFailureEventArgs failureArgs = (FrontendFailureEventArgs) args;

        if (failureArgs.getMessage() != null) {
            errorPopupManager.show(getDescription2MsgMap(Arrays.asList(failureArgs.getMessage())));
        } else if (failureArgs.getMessages() != null) {
            errorPopupManager.show(getDescription2MsgMap(failureArgs.getMessages()));
        }
    }

    private Map<String, Set<String>> getDescription2MsgMap(List<Message> msgList) {
        Map<String, Set<String>> desc2Msgs = new HashMap<String, Set<String>>();

        for (Message msg : msgList) {
            String desc = msg.getDescription();
            if (desc == null) {
                desc = "";
            }
            desc = desc.trim();

            Set<String> msgs = desc2Msgs.get(desc);

            if (msgs == null) {
                msgs = new HashSet<String>();
                desc2Msgs.put(desc, msgs);
            }

            msgs.add(msg.getText());
        }

        return desc2Msgs;
    }

}
