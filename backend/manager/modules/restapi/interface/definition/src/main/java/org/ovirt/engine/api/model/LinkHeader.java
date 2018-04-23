/*
 * From Bill Burke's "RESTful Java with JAX-RS", license unknown
 */
package org.ovirt.engine.api.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkHeader {

    private static Pattern pattern = Pattern.compile("<(.+)>\\s*;\\s*(.+)");

    /**
     * To write as link header
     */
    public static String format(Link link) {
        StringBuilder builder = new StringBuilder("<");
        builder.append(link.getHref()).append(">; rel=").append(link.getRel());
        return builder.toString();
    }

    /**
     * For unmarshalling Link Headers.
     * Its not an efficient or perfect algorithm and does make a few assumptiosn
     */
    public static Link parse(String val) {
        Matcher matcher = pattern.matcher(val);
        if (!matcher.matches()) {
            throw new RuntimeException("Failed to parse link: " + val);
        }

        Link link = new Link();
        link.setHref(matcher.group(1));

        String[] props = matcher.group(2).split(";");
        Map<String, String> map = new HashMap<>();
        for (String prop : props) {
            String[] split = prop.split("=");
            map.put(split[0].trim(), split[1].trim());
        }

        if (map.containsKey("rel")) {
            link.setRel(map.get("rel"));
        }

        return link;
    }
}
