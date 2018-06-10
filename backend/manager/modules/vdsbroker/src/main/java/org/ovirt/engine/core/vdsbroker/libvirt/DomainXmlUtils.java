package org.ovirt.engine.core.vdsbroker.libvirt;

import org.ovirt.engine.core.utils.ovf.xml.XmlAttribute;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;

public class DomainXmlUtils {

    public static final String USER_ALIAS_PREFIX = "ua-";

    public static  String parseMacAddress(XmlNode node) {
        XmlNode macNode = node.selectSingleNode("mac");
        return macNode.attributes.get("address").getValue();
    }

    public static String parseVideoType(XmlNode node) {
        XmlNode videoModelNode = node.selectSingleNode("model");
        return videoModelNode.attributes.get("type").getValue();
    }

    public static String parseAddress(XmlNode node) {
        String result = "";
        XmlNode addressNode = node.selectSingleNode("address");
        if (addressNode == null) {
            return "";
        }
        XmlAttribute val = addressNode.attributes.get("type");
        result += String.format("%s=%s", "type", val.getValue());
        val = addressNode.attributes.get("slot");
        if (val != null) {
            result += String.format(", %s=%s", "slot", val.getValue());
        }
        val = addressNode.attributes.get("bus");
        if (val != null) {
            result += String.format(", %s=%s", "bus", val.getValue());
        }
        val = addressNode.attributes.get("domain");
        if (val != null) {
            result += String.format(", %s=%s", "domain", val.getValue());
        }
        val = addressNode.attributes.get("function");
        if (val != null) {
            result += String.format(", %s=%s", "function", val.getValue());
        }
        val = addressNode.attributes.get("controller");
        if (val != null) {
            result += String.format(", %s=%s", "controller", val.getValue());
        }
        val = addressNode.attributes.get("target");
        if (val != null) {
            result += String.format(", %s=%s", "target", val.getValue());
        }
        val = addressNode.attributes.get("unit");
        if (val != null) {
            result += String.format(", %s=%s", "unit", val.getValue());
        }
        val = addressNode.attributes.get("port");
        if (val != null) {
            result += String.format(", %s=%s", "port", val.getValue());
        }
        val = addressNode.attributes.get("multifunction");
        if (val != null) {
            result += String.format(", %s=%s", "multifunction", val.getValue());
        }
        val = addressNode.attributes.get("base");
        if (val != null) {
            result += String.format(", %s=%s", "base", val.getValue());
        }

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
}
