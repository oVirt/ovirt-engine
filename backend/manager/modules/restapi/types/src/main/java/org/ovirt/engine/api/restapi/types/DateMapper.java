package org.ovirt.engine.api.restapi.types;

import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateMapper {

    private static final Logger log = LoggerFactory.getLogger(DateMapper.class);
    protected static final String DATATYPE_FACTORY_CREATION_FAILED = "DatatypeFactory creation failed.";

    private static volatile DatatypeFactory datatypeFactory;

    /**
     * Class-level synchronization to avoid potential thread-safety issues
     * with statically shared DatatypeFactory.
     */
    @Mapping(from = Date.class, to = XMLGregorianCalendar.class)
    public static synchronized XMLGregorianCalendar map(Date date, XMLGregorianCalendar template) {
        GregorianCalendar calendar = template != null ? template.toGregorianCalendar() : new GregorianCalendar();
        calendar.setTime(date);
        DatatypeFactory factory = getDatatypeFactory();
        return factory != null
               ? factory.newXMLGregorianCalendar(calendar)
               : null;
    }

    @Mapping(from = Integer.class, to = XMLGregorianCalendar.class)
    public static synchronized XMLGregorianCalendar map(BigDecimal secondsAgo, XMLGregorianCalendar template) {
        GregorianCalendar calendar = template != null
            ? template.toGregorianCalendar()
            : new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        DatatypeFactory factory = getDatatypeFactory();
        XMLGregorianCalendar ret = null;
        if (factory != null) {
            ret = factory.newXMLGregorianCalendar(calendar);
            ret.add(factory.newDuration(false, 0, 0, 0, 0, 0, secondsAgo.intValue()));
        }
        return ret;
    }

    /**
     * called with class-level mutex held
     */
    private static DatatypeFactory getDatatypeFactory() {
        if (datatypeFactory == null) {
            try {
                datatypeFactory = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException dce) {
                log.warn(DATATYPE_FACTORY_CREATION_FAILED, dce);
            }
        }
        return datatypeFactory;
    }
}
