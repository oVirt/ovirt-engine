package org.ovirt.engine.core.vdsbroker.libvirt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.ovirt.engine.core.utils.ovf.xml.XmlAttribute;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public class DomainXmlUtils {

    public static final String USER_ALIAS_PREFIX = "ua-";
    private static final List<String> ADDRESS_PROPERTIES = Arrays.asList(
            "type", "slot", "bus", "domain", "function", "controller", "target", "unit", "port", "multifunction", "base");

    public static  String parseMacAddress(XmlNode node) {
        XmlNode macNode = node.selectSingleNode("mac");
        return macNode.attributes.get("address").getValue();
    }

    public static String parseVideoType(XmlNode node) {
        XmlNode videoModelNode = node.selectSingleNode("model");
        return videoModelNode.attributes.get("type").getValue();
    }

    public static String parseAddress(XmlNode node) {
        XmlNode addressNode = node.selectSingleNode("address");
        if (addressNode == null) {
            return "";
        }
        String result = ADDRESS_PROPERTIES.stream()
                .map(property -> {
                    XmlAttribute val = addressNode.attributes.get(property);
                    return val != null ? String.format("%s=%s", property, val.getValue()) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
        return result.isEmpty() ? result : String.format("{%s}", result);
    }

    public static Integer parseIoThreadId(XmlNode node) {
        XmlNode driverNode = node.selectSingleNode("driver");
        if (driverNode == null) {
            return null;
        }

        XmlAttribute val = driverNode.attributes.get("iothread");
        return val != null ? Integer.valueOf(val.getValue()) : null;
    }

    public static String parseAttribute(XmlNode node, String attribute) {
        XmlAttribute xmlAttribute = node.attributes.get(attribute);
        return xmlAttribute != null ? xmlAttribute.getValue() : null;
    }

    public static String parseDiskPath(XmlNode node) {
        XmlNode sourceNode = node.selectSingleNode("source");
        if (sourceNode == null) {
            return "";
        }
        XmlAttribute attr = sourceNode.attributes.get("file");
        if (attr != null) {
            return attr.getValue();
        }
        attr = sourceNode.attributes.get("dev");
        if (attr != null) {
            return attr.getValue();
        }
        attr = sourceNode.attributes.get("name");
        if (attr != null) {
            return attr.getValue();
        }
        return "";
    }

    public static String parseMemSize(XmlNode node) {
        return node != null ?
                Integer.toString(Integer.parseInt(node.innerText) / 1024)
                : "1024"; // should never get here, memory should be set
    }

    public static Map<String, String> parseMaxMemSize(XmlNode node) {
        return node != null ?
                Collections.singletonMap(
                        VdsProperties.maxMemSize,
                        Integer.toString(Integer.parseInt(node.innerText) / 1024))
                : Collections.emptyMap();
    }

    public static String parseNicNetwork(XmlNode node) {
        XmlNode videoModelNode = node.selectSingleNode("source");
        return videoModelNode.attributes.get("bridge").getValue();
    }

    public static String parseNicType(XmlNode node) {
        XmlNode videoModelNode = node.selectSingleNode("model");
        return videoModelNode.attributes.get("type").getValue();
    }

    public static String parseDiskDriver(XmlNode node) {
        XmlNode videoModelNode = node.selectSingleNode("driver");
        return videoModelNode.attributes.get("type").getValue();
    }

    public static String parseDiskBus(XmlNode node) {
        XmlNode videoModelNode = node.selectSingleNode("target");
        return videoModelNode.attributes.get("bus").getValue();
    }

    public static String parseEmulatedMachine(XmlNode node) {
        XmlNode osTypeNode = node.selectSingleNode("type");
        return osTypeNode.attributes.get("machine").getValue();
    }
}
