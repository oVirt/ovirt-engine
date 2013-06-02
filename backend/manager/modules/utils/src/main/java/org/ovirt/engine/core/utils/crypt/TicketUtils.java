package org.ovirt.engine.core.utils.crypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.EngineLocalConfig;

public class TicketUtils {

    PrivateKey key;
    private final int lifetime;
    protected Random random = new Random();

    /**
     * Creates a TicketUtils instance for
     */
    public static TicketUtils getInstanceForEngineStoreSigning() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        File store = EngineLocalConfig.getInstance().getPKIEngineStore();
        String password = EngineLocalConfig.getInstance().getPKIEngineStorePassword();
        String alias = EngineLocalConfig.getInstance().getPKIEngineStoreAlias();
        Integer lifetime = Config.<Integer> GetValue (ConfigValues.WebSocketProxyTicketValiditySeconds);

        return new TicketUtils(store, password, alias, lifetime);
    }

    public TicketUtils(File store, String password, String alias, int lifetime) throws
        KeyStoreException,
        NoSuchAlgorithmException,
        IOException,
        CertificateException,
        UnrecoverableKeyException
    {
        this.lifetime = lifetime;
        InputStream in = null;
        try {
            in = new FileInputStream(store);
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(in, password.toCharArray());
            key = (PrivateKey)keystore.getKey(alias, password.toCharArray());
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {}
            }
        }
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
        this.random.nextBytes(random);
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

    public static void main(String[] args) throws Exception {
        String store = args[0];
        String password = args[1];
        String alias = args[2];
        String data = args[3];
        int lifetime = Integer.parseInt(args[4]);

        TicketUtils tf = new TicketUtils(new File(store), password, alias, lifetime);
        System.out.println(tf.generateTicket(data));
    }
}

