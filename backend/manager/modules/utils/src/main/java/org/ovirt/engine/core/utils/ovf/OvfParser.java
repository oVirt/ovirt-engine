package org.ovirt.engine.core.utils.ovf;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;
import org.ovirt.engine.core.utils.ovf.xml.XmlNodeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OvfParser {
    private static final Logger log = LoggerFactory.getLogger(OvfParser.class);
    private static final String utcFallbackDateFormatStr = "yyyy.MM.dd HH:mm:ss";
    private static final String utcDateFormatStr = "yyyy/MM/dd HH:mm:ss";
    public static final String formatStrFromDiskDescription = "EEE MMM d HH:mm:ss zzz yyyy";
    private XmlDocument _document;

    public OvfParser(String ovfstring) throws OvfReaderException {
        try {
            _document = new XmlDocument(ovfstring);
        } catch (Exception e) {
            log.error("Failed Parsing OVF due to {}", e.getMessage());
            log.debug("Exception", e);
            throw new OvfReaderException(e);
        }
    }

    public boolean isTemplate() {
        String id1 = "1";
        String id2 = "2";

        XmlNode node = _document.selectSingleNode("//*/Content/TemplateId");
        if (!StringUtils.isBlank(node.innerText)) {
            id1 = node.innerText;
        }

        XmlNodeList list = _document.selectNodes("//*/Content/Section");
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
    public static String createImageFile(DiskImage image) {
        String retVal = "";
        if (image.getId() != null) {
            retVal += image.getId().toString();
        } else {
            retVal += Guid.Empty;
        }
        retVal += "/" + image.getImageId().toString();
        return retVal;
    }

    /**
     * lunDisk is: /dev/mapper/[lun id]
     *
     * @param lun
     *            - The lun disk which will be used to create the mapper string.
     * @return - Represented guid of the lun mapper.
     */
    public static String createLunFile(LunDisk lun) {
        if (lun.getId() != null) {
            return lun.getId().toString();
        }
        return Guid.Empty.toString();
    }

    public static Guid getImageGroupIdFromImageFile(String imageFile) {
        if (!StringUtils.isBlank(imageFile)) {
            return Guid.createGuidFromStringDefaultEmpty(imageFile.split("[/]", -1)[0]);
        }
        return null;
    }

    public static String localDateToUtcDateString(Date date) {
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
     * @return the date or null if parse failed
     */
    public static Date utcDateStringToLocalDate(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }

        try {
            return getDateFormat(utcDateFormatStr).parse(str);
        } catch (ParseException e1) {
            try {
                return getDateFormat(utcFallbackDateFormatStr).parse(str);
            } catch (ParseException e) {
                log.error("OVF DateTime format error: '{}', Expected: yyyy/M/dd hh:mm:ss", e.getMessage());
                log.debug("Exception", e);
                return null;
            }
        }
    }
}
