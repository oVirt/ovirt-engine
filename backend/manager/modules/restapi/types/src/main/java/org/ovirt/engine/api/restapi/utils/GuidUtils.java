package org.ovirt.engine.api.restapi.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.ovirt.engine.core.compat.Guid;

public class GuidUtils {

    private static final String MD5_SECURITY_ALGORITHM = "MD5";

    public static Guid asGuid(String id) {
        try {
            return new Guid(id);
        }catch (IllegalArgumentException e) {
            throw new MalformedIdException(e);
        }
    }

    public static Guid asGuid(byte[] guid) {
        try {
            return new Guid(guid);
        } catch (IllegalArgumentException e) {
            throw new MalformedIdException(e);
        }
    }

    /**
     * There are some business entities in the API, which are not regarded as business entities in the engine, and
     * therefore they don't have IDs. The API generates uniqute GUIDs for them, according to their attributes. This
     * method accepts one or more string attributes, concatenates them, and using Md5 hash to generate a unique Guid for
     * them.
     *
     * @param args
     *            one or more strings, guid will be generated from them
     * @return unique Guid generated from the given strings.
     */
    public static Guid generateGuidUsingMd5(String... args) {
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg);
        }
        byte[] hash;
        try {
            hash = MessageDigest.getInstance(MD5_SECURITY_ALGORITHM).digest(builder.toString().getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e); // never happens, MD5 algorithm exists
        }
        return new Guid(hash);
    }
}
