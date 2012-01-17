package org.ovirt.engine.core.utils.ovf;

import java.text.DateFormat;
import java.util.Date;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;
import org.ovirt.engine.core.compat.backendcompat.XmlNamespaceManager;
import org.ovirt.engine.core.compat.backendcompat.XmlNode;
import org.ovirt.engine.core.compat.backendcompat.XmlNodeList;

public class OvfParser {

    private static final String utcFallbackDateFormatStr = "yyyy.MM.dd HH:mm:ss";
    private static final String utcDateFormatStr = "yyyy/MM/dd HH:mm:ss";
    protected XmlDocument _document;
    protected XmlNamespaceManager _xmlNS;

    public OvfParser(String ovfstring) {
        _document = new XmlDocument();
        _document.LoadXml(ovfstring);

        _xmlNS = new XmlNamespaceManager(_document.NameTable);
    }

    public boolean IsTemplate() {
        String id1 = "1";
        String id2 = "2";

        XmlNode node = _document.SelectSingleNode("//*/Content/TemplateId");
        if (!StringHelper.isNullOrEmpty(node.InnerText)) {
            id1 = node.InnerText;
        }

        XmlNodeList list = _document.SelectNodes("//*/Content/Section");
        for (XmlNode section : list) {
            String value = section.Attributes.get("xsi:type").getValue();

            if (StringHelper.EqOp(value, "ovf:OperatingSystemSection_Type")) {
                id2 = section.Attributes.get("ovf:id").getValue();
            }
        }

        return StringHelper.EqOp(id1, id2);
    }

    // imageFile is: [image group id]/[image id]
    // 7D1FE0AA-A153-4AAF-95B3-3654A54443BE/7D1FE0AA-A153-4AAF-95B3-3654A54443BE
    public static String CreateImageFile(DiskImage image) {
        String retVal = "";
        if (image.getimage_group_id() != null) {
            retVal += image.getimage_group_id().getValue().toString();
        } else {
            retVal += Guid.Empty;
        }
        retVal += "/" + image.getId().toString();
        return retVal;
    }

    public static Guid GetImageGrupIdFromImageFile(String imageFile) {
        if (!StringHelper.isNullOrEmpty(imageFile)) {
            return new Guid(imageFile.split("[/]", -1)[0]);
        }
        return null;
    }

    public static NGuid GetImageIdFromImageFile(String imageFile) {
        if (!StringHelper.isNullOrEmpty(imageFile)) {
            String[] all = imageFile.split("[/]", -1);
            if (all.length > 1) {
                return new Guid(imageFile.split("[/]", -1)[1]);
            }
        }
        return null;
    }

    public static String LocalDateToUtcDateString(Date date) {
        return getDateFormat(utcDateFormatStr).format(date);
    }

    private static DateFormat getDateFormat(final String format) {
        final DateFormat utcDateTimeFormat = new java.text.SimpleDateFormat(format);
        utcDateTimeFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return utcDateTimeFormat;
    }

    /**
     * Method return false if the format is not yyyy/mm/dd hh:mm:ss
     *
     * @param str
     * @param date
     * @return
     */
    public static boolean UtcDateStringToLocaDate(String str, RefObject<java.util.Date> date) {
        date.argvalue = DateTime.getMinValue();
        if (StringHelper.isNullOrEmpty(str)) {
            return false;
        }

        try {
            date.argvalue = getDateFormat(utcDateFormatStr).parse(str);
            return true;
        } catch (java.text.ParseException e1) {
            try {
                date.argvalue = getDateFormat(utcFallbackDateFormatStr).parse(str);
                return true;
            } catch (java.text.ParseException e) {
                log.error("OVF DateTime format Error, Expected: yyyy/M/dd hh:mm:ss", e);
                return false;
            }
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(OvfParser.class);
}
