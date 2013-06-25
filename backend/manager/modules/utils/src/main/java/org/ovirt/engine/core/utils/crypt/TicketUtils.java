package org.ovirt.engine.core.utils.crypt;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public class TicketUtils {

    private PrivateKey key;
    private final int lifetime;

    /**
     * Creates a TicketUtils instance for
     */
    public static TicketUtils getInstanceForEngineStoreSigning() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        return new TicketUtils(
            EngineEncryptionUtils.getPrivateKeyEntry().getPrivateKey(),
            Config.<Integer> GetValue (ConfigValues.WebSocketProxyTicketValiditySeconds)
        );
    }

    public TicketUtils(PrivateKey key, int lifetime) {
        this.lifetime = lifetime;
        this.key = key;
    }

    public String generateTicket(String data)
    throws
        NoSuchAlgorithmException,
        SignatureException,
        InvalidKeyException
    {
        Base64 base64 = new Base64(0);
        Map<String, String> map = new HashMap<String, String>();

        /*
         * Add signed fields
         */
        byte[] random = new byte[8];
        SecureRandom.getInstance("SHA1PRNG").nextBytes(random);
        map.put("salt", base64.encodeToString(random));

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
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
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(key);
        StringBuilder fields = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (fields.length() > 0) {
                fields.append(",");
            }
            fields.append(entry.getKey());
            signature.update(entry.getValue().getBytes(Charset.forName("UTF-8")));
        }

        /*
         * Add unsigned fields
         */
        map.put("signedFields", fields.toString());
        map.put("signature", base64.encodeToString(signature.sign()));

        /*
         * Create json
         */
        StringBuilder ret = new StringBuilder();
        ret.append("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (first) {
                first = false;
            }
            else {
                ret.append(",");
            }
            ret.append(String.format("\"%s\":\"%s\"", entry.getKey(), entry.getValue()));
        }
        ret.append("}");

        return base64.encodeToString(ret.toString().getBytes(Charset.forName("UTF-8")));
    }

}

