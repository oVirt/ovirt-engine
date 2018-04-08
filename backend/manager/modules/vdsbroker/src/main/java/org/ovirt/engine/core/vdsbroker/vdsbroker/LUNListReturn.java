package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.utils.ObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public final class LUNListReturn extends StatusReturn {
    private static final String DEV_LIST = "devList";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [MissingMapping(MappingAction.Ignore), Member("devList")]
    public Map<String, Object>[] lunList;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        ObjectDescriptor.toStringBuilder(lunList, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public LUNListReturn(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = innerMap.get(DEV_LIST);
        if (temp == null) {
            lunList = null;
        } else {
            Object[] tempArray = (Object[]) temp;
            lunList = new Map[tempArray.length];
            for (int i = 0; i < tempArray.length; i++) {
                lunList[i] = (Map<String, Object>) tempArray[i];
            }
        }
    }

}
