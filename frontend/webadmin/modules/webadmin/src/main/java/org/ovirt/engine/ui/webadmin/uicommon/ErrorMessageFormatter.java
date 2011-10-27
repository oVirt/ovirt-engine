package org.ovirt.engine.ui.webadmin.uicommon;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.ui.frontend.Message;

public abstract class ErrorMessageFormatter {

    public static String formatMessages(List<Message> values) {
        StringBuilder msg = new StringBuilder();

        for (Message val : values)
            msg.append(val.getText());

        return msg.toString();
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

}
