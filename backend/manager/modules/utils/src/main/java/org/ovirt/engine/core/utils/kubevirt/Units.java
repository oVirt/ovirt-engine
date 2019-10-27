package org.ovirt.engine.core.utils.kubevirt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Units {
    /**
     * TODO: generalize to other resources and cover the entire list as specified on:
     * https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/
     * @return the given memory resource quantity in MB
     */
    public static Integer parse(String in) {
        if (in == null) {
            return null;
        }
        in = in.trim();
        in = in.replaceAll(",", ".");
        try {
            return Integer.parseInt(in);
        } catch (NumberFormatException e) {
            // proceed
        }
        final Matcher m = Pattern.compile("([\\d.,]+)\\s*(\\w*)").matcher(in);
        m.find();
        double scale = 1.0;
        String unit = m.group(2);
        switch (unit.charAt(0)) {
        case 'G':
            scale *= unit.endsWith("i") ? 1073.74182 : 1000;
            break;
        case 'M':
            scale *= unit.endsWith("i") ? 1.048576 : 1;
            break;
        case 'K':
            scale *= unit.endsWith("i") ? 0.001024 : 0.001;
            break;
        default:
            throw new IllegalArgumentException();
        }
        return (int) Math.round(Double.parseDouble(m.group(1)) * scale);
    }
}
