package org.ovirt.engine.api.restapi.utils;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.ovirt.engine.api.restapi.types.SnapshotMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeConversionHelper {

    private static volatile DatatypeFactory datatypeFactory;
    private static final Logger log = LoggerFactory.getLogger(SnapshotMapper.class);
    protected static final String DATATYPE_FACTORY_CREATION_FAILED = "DatatypeFactory creation failed.";

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

    /**
     * Class-level synchronization to avoid potential thread-safety issues
     * with statically shared DatatypeFactory.
     */
    public static synchronized XMLGregorianCalendar toXMLGregorianCalendar(Date date,
                                                                           XMLGregorianCalendar template) {
        GregorianCalendar calendar = template != null ? template.toGregorianCalendar() : new GregorianCalendar();
        calendar.setTime(date);
        DatatypeFactory factory = getDatatypeFactory();
        return factory != null
               ? factory.newXMLGregorianCalendar(calendar)
               : null;
    }
}
