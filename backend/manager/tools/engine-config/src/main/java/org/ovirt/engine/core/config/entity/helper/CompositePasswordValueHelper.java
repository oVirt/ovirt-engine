package org.ovirt.engine.core.config.entity.helper;

import java.util.StringTokenizer;

import org.ovirt.engine.core.config.entity.ConfigKey;

/**
 * This value helper is for values in the form of key:password. The helper can strip down and encrypt or decrypt the
 * password only. The actual value may be a comma delimited list of key-val pair. e.g: example.com:123456,
 * ovirt.org:0o9i8u, A.B.C:clearTextPass
 */
public class CompositePasswordValueHelper implements ValueHelper {

    private final PasswordValueHelper pwdValueHelper = new PasswordValueHelper();

    private static enum ReformatType {
        ENCRYPT,
        DECRYPT
    }

    @Override
    public String getValue(String value) throws Exception {
        return reformatKeyVal(value, ReformatType.DECRYPT);
    }

    @Override
    public String setValue(String value) throws Exception {
        return reformatKeyVal(value, ReformatType.ENCRYPT);
    }

    private String reformatKeyVal(String value, ReformatType type) throws Exception {
        StringBuilder sb = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        // extract key:val pair format and encrypt/decrypt the val section
        while (tokenizer.hasMoreElements()) {
            String token = (String) tokenizer.nextElement();
            String[] pair = token.split(":", -1);
            sb.append(pair[0]).append(":"); // append key
            // now append the value
            if (type == ReformatType.ENCRYPT) {
                sb.append(pwdValueHelper.encrypt(pair[1]));
            } else {
                try {
                    sb.append(pwdValueHelper.decrypt(pair[1]));
                } catch (Exception e) {
                    // password wasn't encrypted in first place - ignore and append as is.
                    sb.append(pair[1]);
                }
            }
            if (tokenizer.hasMoreElements())
                sb.append(",");
        }
        return sb.toString();
    }

    @Override
    public boolean validate(ConfigKey key, String value) {
        boolean returnValue = true;
        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        while (tokenizer.hasMoreElements()) {
            String token = (String) tokenizer.nextElement();
            String[] pair = token.split(":", -1);
            String password = pair[1];
            if (!pwdValueHelper.validate(null, password)) {
                returnValue = false;
                break;
            }
        }
        return returnValue;
    }
}
