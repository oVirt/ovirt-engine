package org.ovirt.engine.api.restapi.utils;

import java.nio.charset.Charset;

import javax.xml.bind.DatatypeConverter;

public class DirectoryEntryIdUtils {

    public static String encode(String source) {
        return DatatypeConverter.printHexBinary(source.getBytes(Charset.forName("UTF-8")));
    }

    public static String decode(String source) {
        return new String(DatatypeConverter.parseHexBinary(source), Charset.forName("UTF-8"));
    }

}
