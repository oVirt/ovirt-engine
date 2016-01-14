package org.ovirt.engine.core.uutils.crypto.ticket;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.map.ObjectMapper;

public class TicketEncoder {

    private static final String DATE_FORMAT = "yyyyMMddHHmmss";

    private Certificate cert;
    private PrivateKey key;
    private final int lifetime;

    public TicketEncoder(Certificate cert, PrivateKey key, int lifetime) {
        this.lifetime = lifetime;
        this.cert = cert;
        this.key = key;
    }

    public TicketEncoder(Certificate cert, PrivateKey key) {
        this(cert, key, 5);
    }

    public String encode(String data)
    throws GeneralSecurityException, IOException {

        Base64 base64 = new Base64(0);
        Map<String, String> map = new HashMap<>();

        byte[] random = new byte[8];
        SecureRandom.getInstance("SHA1PRNG").nextBytes(random);
        map.put("salt", base64.encodeToString(random));
        map.put("digest", "sha1");

        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        map.put(
            "validFrom",
            df.format(new Date())
        );

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.add(Calendar.SECOND, lifetime);
        map.put(
            "validTo",
            df.format(cal.getTime())
        );

        map.put("data", data);

        /*
         * Calculate signature on fields in map
         */
        Signature signature = Signature.getInstance(String.format("%swith%s", map.get("digest"), key.getAlgorithm()));
        signature.initSign(key);
        StringBuilder fields = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (fields.length() > 0) {
                fields.append(",");
            }
            fields.append(entry.getKey());
            signature.update(entry.getValue().getBytes(StandardCharsets.UTF_8));
        }

        /*
         * Add unsigned fields
         */
        map.put("signedFields", fields.toString());
        map.put("signature", base64.encodeToString(signature.sign()));
        map.put("certificate", String.format(
                "-----BEGIN CERTIFICATE-----\n" +
                "%s" +
                "-----END CERTIFICATE-----\n",
            new Base64(76).encodeToString(cert.getEncoded())
        ));

        return base64.encodeToString(new ObjectMapper().writeValueAsString(map).getBytes(StandardCharsets.UTF_8));
    }
}

