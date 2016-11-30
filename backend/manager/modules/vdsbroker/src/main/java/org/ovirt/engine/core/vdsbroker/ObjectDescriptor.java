package org.ovirt.engine.core.vdsbroker;

import java.util.Arrays;
import java.util.Map;

public class ObjectDescriptor {
    public static void toStringBuilder(Map<String, ?> map, StringBuilder builder) {
        if (map == null) {
            return;
        }

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                builder.append(String.format("%1$s:", entry.getKey()));
                builder.append("\n");
                toStringBuilder((Map<String, Object>) entry.getValue(), builder);
                builder.append("\n");
            } else if (!(entry.getValue() instanceof String) && entry.getValue() instanceof Iterable) {
                builder.append(String.format("%1$s:", entry.getKey()));
                builder.append("\n");
                toStringBuilder((Iterable) entry.getValue(), builder);
                builder.append("\n");
            } else if (entry.getValue() instanceof Object[]) {
                builder.append(String.format("%1$s:", entry.getKey()));
                builder.append("\n");
                builder.append(Arrays.deepToString((Object[]) entry.getValue()));
                builder.append("\n");
            } else {
                builder.append(String.format("%1$s = %2$s", entry.getKey(), entry.getValue().toString()));
                builder.append("\n");
            }
        }
    }

    public static void toStringBuilder(Map[] map, StringBuilder builder) {
        if (map == null) {
            return;
        }

        for (Map entry : map) {
            toStringBuilder(entry, builder);
        }
    }

    private static void toStringBuilder(Iterable iterable, StringBuilder builder) {
        if (iterable == null) {
            return;
        }

        for (Object value : iterable) {
            if (value instanceof Iterable) {
                toStringBuilder((Iterable) value, builder);
            } else {
                builder.append(value.toString());
                builder.append("\n");
            }
        }
    }

}
