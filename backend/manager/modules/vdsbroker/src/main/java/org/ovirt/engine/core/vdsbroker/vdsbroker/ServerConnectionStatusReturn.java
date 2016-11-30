package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.vdsbroker.ObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public final class ServerConnectionStatusReturn extends StatusReturn {
    private static final String STATUS_LIST = "statuslist";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [MissingMapping(MappingAction.Ignore), Member("statuslist")]
    public Map<String, Object>[] statusList;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        ObjectDescriptor.toStringBuilder(statusList, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public ServerConnectionStatusReturn(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = innerMap.get(STATUS_LIST);
        if (temp == null) {
            statusList = null;
        } else {
            Object[] tempArray = (Object[]) temp;
            statusList = new Map[tempArray.length];
            for (int i = 0; i < tempArray.length; i++) {
                statusList[i] = (Map<String, Object>) tempArray[i];
            }
        }
    }

    public Map<String, String> convertToStatusList() {
        HashMap<String, String> result = new HashMap<>();
        for (Map<String, Object> st : this.statusList) {
            String status = st.get("status").toString();
            String id = st.get("id").toString();
            result.put(id, status);
        }
        return result;
    }
}
