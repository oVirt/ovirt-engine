package org.ovirt.engine.ui.common.uicommon;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.frontend.Message;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Utility class used to format Frontend messages into Strings suitable for displaying in UI.
 */
public class ErrorMessageFormatter {

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();

    public static String formatMessages(List<Message> values) {
        // If one error message without description no need to format
        if (values.size() == 1) {
            Message msg = values.get(0);
            if (msg.getDescription() == null || "".equals(msg.getDescription())) { //$NON-NLS-1$
                return msg.getText();
            }
        }

        SafeHtmlBuilder allSb = new SafeHtmlBuilder();
        allSb.append(SafeHtmlUtils.fromTrustedString("<br/><br/>")); //$NON-NLS-1$

        Map<String, Set<String>> desc2msgs = getDescription2MsgMap(values);
        for (Map.Entry<String, Set<String>> entry : desc2msgs.entrySet()) {
            String desc = entry.getKey();
            SafeHtml sh = buildItemList(entry.getValue());

            if (!"".equals(desc)) { //$NON-NLS-1$
                allSb.append(SafeHtmlUtils.fromString(desc + ":")); //$NON-NLS-1$
            }

            allSb.append(sh);
        }

        return allSb.toSafeHtml().asString();
    }

    public static String formatErrorMessages(List<String> values) {
        if (values.size() == 1) {
            return values.get(0);
        }

        SafeHtmlBuilder allSb = new SafeHtmlBuilder();
        allSb.append(SafeHtmlUtils.fromTrustedString("<br/><br/>")); //$NON-NLS-1$

        SafeHtml sh = buildItemList(values);
        allSb.append(sh);

        return allSb.toSafeHtml().asString();
    }

    private static SafeHtml buildItemList(Collection<String> items) {
        SafeHtmlBuilder itemBuilder = new SafeHtmlBuilder();

        for (String i : items) {
            itemBuilder.append(templates.listItem(SafeHtmlUtils.fromSafeConstant(i)));
        }

        return templates.unsignedList(itemBuilder.toSafeHtml());
    }

    public static String formatReturnValues(List<ActionReturnValue> values) {
        StringBuilder msg = new StringBuilder();

        for (ActionReturnValue val : values) {
            msg.append(val.getFault().getMessage());
        }

        return msg.toString();
    }

    public static String formatQueryReturnValues(List<QueryReturnValue> values) {
        StringBuilder msg = new StringBuilder();

        for (QueryReturnValue val : values) {
            msg.append(val.getExceptionString());
        }

        return msg.toString();
    }

    private static Map<String, Set<String>> getDescription2MsgMap(List<Message> msgList) {
        Map<String, Set<String>> desc2Msgs = new HashMap<>();

        for (Message msg : msgList) {
            String desc = msg.getDescription();
            if (desc == null) {
                desc = ""; //$NON-NLS-1$
            }
            desc = desc.trim();

            Set<String> msgs = desc2Msgs.get(desc);

            if (msgs == null) {
                msgs = new HashSet<>();
                desc2Msgs.put(desc, msgs);
            }

            msgs.add(msg.getText());
        }

        return desc2Msgs;
    }

}
