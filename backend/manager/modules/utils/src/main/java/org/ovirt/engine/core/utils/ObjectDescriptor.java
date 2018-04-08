package org.ovirt.engine.core.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class ObjectDescriptor {

    public static void toStringBuilder(Object object, StringBuilder builder) {
        if (object == null) {
            return;
        }

        if (object instanceof Iterable) {
            toStringBuilder((Iterable<Object>) object, builder);
        } else if (object instanceof Object[]) {
                toStringBuilder(Arrays.asList((Object[]) object), builder);
        } else if (object instanceof Map) {
            toStringBuilder((Map<String, Object>) object, builder);
        } else {
            builder.append(object.toString());
        }
    }

    private static void toStringBuilder(Map<String, ?> map, StringBuilder builder) {
        builder.append("{");
        Iterator<? extends Map.Entry<String, ?>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ?> entry = iterator.next();
            builder.append(String.format("%1$s=", entry.getKey()));
            toStringBuilder(entry.getValue(), builder);
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append("}");
    }

    private static void toStringBuilder(Iterable iterable, StringBuilder builder) {
        builder.append("[");
        Iterator<Object> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            toStringBuilder(iterator.next(), builder);
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append("]");
    }

}
