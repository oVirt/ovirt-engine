package org.ovirt.engine.core.vdsbroker.xmlrpc;

import java.util.Map;

public class XmlRpcObjectDescriptor {
    public static void ToStringBuilder(java.util.Map<String, ?> map, StringBuilder builder) {
        if (map == null)
            return;

        for (java.util.Map.Entry<String, ?> entry : map.entrySet()) {
            if (entry.getValue() instanceof java.util.Map) {
                builder.append(String.format("%1$s:", entry.getKey()));
                builder.append("\n");
                ToStringBuilder((java.util.Map<String, Object>) entry.getValue(), builder);
                builder.append("\n");
            } else if (!(entry.getValue() instanceof String) && entry.getValue() instanceof Iterable) {
                builder.append(String.format("%1$s:", entry.getKey()));
                builder.append("\n");
                ToStringBuilder((Iterable) (entry.getValue()), builder);
                builder.append("\n");
            } else {
                builder.append(String.format("%1$s = %2$s", entry.getKey(), entry.getValue().toString()));
                builder.append("\n");
            }
        }
    }

    public static void ToStringBuilder(Map[] xmlRpc, StringBuilder builder) {
        if (xmlRpc == null)
            return;

        for (Map entry : xmlRpc) {
            ToStringBuilder(entry, builder);
        }
    }

    private static void ToStringBuilder(Iterable xmlRpc, StringBuilder builder) {
        if (xmlRpc == null)
            return;

        for (Object value : xmlRpc) {
            if (value instanceof Iterable) {
                ToStringBuilder((Iterable) (value), builder);
            } else {
                builder.append(value.toString());
                builder.append("\n");
            }
        }
    }

}
