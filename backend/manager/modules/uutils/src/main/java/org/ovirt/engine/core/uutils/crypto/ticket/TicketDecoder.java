package org.ovirt.engine.core.uutils.crypto.ticket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;
import org.ovirt.engine.core.uutils.crypto.CertificateChain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class TicketDecoder {

    private static final String DATE_FORMAT = "yyyyMMddHHmmss";

    private final Set<TrustAnchor> trustAnchors;
    private final String eku;
    private final Certificate peer;
    private final int tollerance;

    public TicketDecoder(KeyStore trustStore, String eku, Certificate peer, int tollerance) throws KeyStoreException {
        if (trustStore == null) {
            trustAnchors = null;
        } else {
            trustAnchors = CertificateChain.keyStoreToTrustAnchors(trustStore);
        }
        this.eku = eku;
        this.peer = peer;
        this.tollerance = tollerance;
    }

    public TicketDecoder(KeyStore trustStore, String eku, Certificate peer) throws KeyStoreException {
        this(trustStore, eku, peer, 0);
    }

    public TicketDecoder(KeyStore trustStore, String eku, int tollerance) throws KeyStoreException {
        this(trustStore, eku, null, tollerance);
    }

    public TicketDecoder(Certificate peer, int tollerance) throws KeyStoreException {
        this(null, null, peer, tollerance);
    }

    public TicketDecoder(KeyStore trustStore, String eku) throws KeyStoreException {
        this(trustStore, eku, null);
    }

    public TicketDecoder(Certificate peer) throws KeyStoreException {
        this(null, null, peer);
    }

    public String decode(String ticket) throws GeneralSecurityException, IOException {
        Certificate cert;

        Map<String, String> map = new ObjectMapper().readValue(
                Base64.decodeBase64(ticket),
                TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class));

        if (peer != null) {
            cert = peer;
        } else {
            try (InputStream is = new ByteArrayInputStream(map.get("certificate").getBytes(StandardCharsets.UTF_8))) {
                cert = CertificateFactory.getInstance("X.509").generateCertificate(is);
            }
        }

        if (trustAnchors != null) {
            CertificateChain.buildCertPath(Arrays.asList(cert), trustAnchors);
        }

        if (eku != null) {
            if (!((X509Certificate) cert).getExtendedKeyUsage().contains(eku)) {
                throw new GeneralSecurityException("Certificate is not authorized for action");
            }
        }

        List<String> signedFields = Arrays.asList(map.get("signedFields").trim().split("\\s*,\\s*"));
        if (!signedFields.containsAll(Arrays.asList("salt", "data"))) {
            throw new GeneralSecurityException("Invalid ticket");
        }

        Signature sig;
        if (map.containsKey("v2_signature")) {
            sig = Signature.getInstance("RSASSA-PSS");
            sig.setParameter(new PSSParameterSpec("SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA256,
                    TicketEncoder.getSaltMaxLength((RSAKey) cert.getPublicKey()),
                    PSSParameterSpec.TRAILER_FIELD_BC));
        } else {
            sig = Signature.getInstance(String.format("%swith%s",
                    map.get("digest"),
                    cert.getPublicKey().getAlgorithm()));
        }

        sig.initVerify(cert.getPublicKey());
        for (String field : signedFields) {
            byte[] buf = map.get(field).getBytes(StandardCharsets.UTF_8);
            sig.update(buf);
        }
        if (!sig.verify(Base64
                .decodeBase64(map.containsKey("v2_signature") ? map.get("v2_signature") : map.get("signature")))) {
            throw new GeneralSecurityException("Invalid ticket signature");
        }

        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date validFrom = df.parse(map.get("validFrom"));
            Date validTo = df.parse(map.get("validTo"));
            Date now = new Date();
            if (!(validFrom.getTime() - tollerance <= now.getTime() &&
                    now.getTime() <= validTo.getTime() + tollerance)) {
                throw new GeneralSecurityException("Ticket lifetime expired");
            }
        } catch (ParseException e) {
            throw new GeneralSecurityException(e);
        }

        return map.get("data");
    }
}
