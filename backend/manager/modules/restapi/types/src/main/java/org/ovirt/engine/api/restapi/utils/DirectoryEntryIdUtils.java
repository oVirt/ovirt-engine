package org.ovirt.engine.api.restapi.utils;

import java.nio.charset.StandardCharsets;

import javax.xml.bind.DatatypeConverter;

public class DirectoryEntryIdUtils {

    public static String encode(String source) {
        return DatatypeConverter.printHexBinary(source.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String source) {
        return new String(DatatypeConverter.parseHexBinary(source), StandardCharsets.UTF_8);
    }

}
