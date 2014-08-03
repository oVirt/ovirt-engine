package org.ovirt.engine.core.utils.ovf;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNamespaceManager;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;
import org.ovirt.engine.core.utils.ovf.xml.XmlNodeList;

public class OvfParser {

    private static final String utcFallbackDateFormatStr = "yyyy.MM.dd HH:mm:ss";
    private static final String utcDateFormatStr = "yyyy/MM/dd HH:mm:ss";
    protected XmlDocument _document;
    protected XmlNamespaceManager _xmlNS;

    public OvfParser(String ovfstring) throws OvfReaderException {
        try {
            _document = new XmlDocument(ovfstring);
        } catch (Exception e) {
            log.errorFormat("Failed Parsing OVF due to {0} ", e.getMessage());
            throw new OvfReaderException(e);
        }
        _xmlNS = new XmlNamespaceManager();
    }

    public boolean IsTemplate() {
        String id1 = "1";
        String id2 = "2";

        XmlNode node = _document.SelectSingleNode("//*/Content/TemplateId");
        if (!StringUtils.isBlank(node.innerText)) {
            id1 = node.innerText;
        }

        XmlNodeList list = _document.SelectNodes("//*/Content/Section");
        for (XmlNode section : list) {
            String value = section.attributes.get("xsi:type").getValue();

            if (StringUtils.equals(value, "ovf:OperatingSystemSection_Type")) {
                id2 = section.attributes.get("ovf:id").getValue();
            }
        }

        return StringUtils.equals(id1, id2);
    }

    // imageFile is: [image group id]/[image id]
    // 7D1FE0AA-A153-4AAF-95B3-3654A54443BE/7D1FE0AA-A153-4AAF-95B3-3654A54443BE
    public static String CreateImageFile(DiskImage image) {
        String retVal = "";
        if (image.getId() != null) {
            retVal += image.getId().toString();
        } else {
            retVal += Guid.Empty;
        }
        retVal += "/" + image.getImageId().toString();
        return retVal;
    }

    public static Guid GetImageGrupIdFromImageFile(String imageFile) {
        if (!StringUtils.isBlank(imageFile)) {
            return Guid.createGuidFromStringDefaultEmpty(imageFile.split("[/]", -1)[0]);
        }
        return null;
    }

    public static String LocalDateToUtcDateString(Date date) {
        return getDateFormat(utcDateFormatStr).format(date);
    }

    private static DateFormat getDateFormat(final String format) {
        final DateFormat utcDateTimeFormat = new SimpleDateFormat(format);
        utcDateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return utcDateTimeFormat;
    }

    /**
     * Method return false if the format is not yyyy/mm/dd hh:mm:ss
     *
     * @param str
     * @return the date or null if parse failed
     */
    public static Date UtcDateStringToLocaDate(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }

        try {
            return getDateFormat(utcDateFormatStr).parse(str);
        } catch (ParseException e1) {
            try {
                return getDateFormat(utcFallbackDateFormatStr).parse(str);
            } catch (ParseException e) {
                log.error("OVF DateTime format Error, Expected: yyyy/M/dd hh:mm:ss", e);
                return null;
            }
        }
    }

    private static Log log = LogFactory.getLog(OvfParser.class);
}
