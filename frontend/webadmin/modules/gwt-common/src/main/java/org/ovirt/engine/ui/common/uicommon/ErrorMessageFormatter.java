package org.ovirt.engine.ui.common.uicommon;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.frontend.Message;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Utility class used to format Frontend messages into Strings suitable for displaying in UI.
 */
public class ErrorMessageFormatter {
    private static final CommonApplicationTemplates TEMPLATES = GWT.create(CommonApplicationTemplates.class);

    public static String formatMessages(List<Message> values) {
        // If one error message without description no need to format
        if (values.size() == 1) {
            Message msg = values.get(0);
            if (msg.getDescription() == null || "".equals(msg.getDescription())) {
                return msg.getText();
            }
        }

        SafeHtmlBuilder allSb = new SafeHtmlBuilder();

        allSb.append(SafeHtmlUtils.fromTrustedString("</br></br>"));

        Map<String, Set<String>> desc2msgs = getDescription2MsgMap(values);

        for (Map.Entry<String, Set<String>> entry : desc2msgs.entrySet()) {
            SafeHtmlBuilder listSb = new SafeHtmlBuilder();
            String desc = entry.getKey();

            for (String msg : entry.getValue()) {
                listSb.append(TEMPLATES.listItem(SafeHtmlUtils.fromSafeConstant(msg)));
            }

            SafeHtml sh = TEMPLATES.unsignedList(listSb.toSafeHtml());

            if (!desc.equals("")) {
                allSb.append(SafeHtmlUtils.fromString(desc + ":"));
            }

            allSb.append(sh);
        }

        return allSb.toSafeHtml().asString();
    }

    public static String formatMessage(Message value) {
        return formatMessages(Arrays.asList(value));
    }

    public static String formatReturnValues(List<VdcReturnValueBase> values) {
        StringBuilder msg = new StringBuilder();

        for (VdcReturnValueBase val : values)
            msg.append(val.getFault().getMessage());

        return msg.toString();
    }

    public static String formatQueryReturnValues(List<VdcQueryReturnValue> values) {
        StringBuilder msg = new StringBuilder();

        for (VdcQueryReturnValue val : values)
            msg.append(val.getExceptionString());

        return msg.toString();
    }

    private static Map<String, Set<String>> getDescription2MsgMap(List<Message> msgList) {
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
