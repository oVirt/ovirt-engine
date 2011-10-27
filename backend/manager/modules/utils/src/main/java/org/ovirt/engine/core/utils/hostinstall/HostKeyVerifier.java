package org.ovirt.engine.core.utils.hostinstall;

import java.net.SocketAddress;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.ClientSession;
import org.apache.sshd.client.ServerKeyVerifier;
import org.apache.sshd.common.util.BufferUtils;

/***
 *
 *
 */
public class HostKeyVerifier implements ServerKeyVerifier {

    public static final ServerKeyVerifier INSTANCE = new HostKeyVerifier();
    private static Log log = LogFactory.getLog(HostKeyVerifier.class);
    private byte[] serverKeyFingerprint;

    HostKeyVerifier() {
        serverKeyFingerprint = null;
    }

    private static byte[] intToDWord(int i) {
        byte[] dword = new byte[4];
        dword[0] = (byte) ((i >> 24));
        dword[1] = (byte) ((i >> 16));
        dword[2] = (byte) ((i >> 8));
        dword[3] = (byte) (i);
        return dword;
    }

    private byte[] getKeyFingerprint(PublicKey serverKey) {
        byte[] baFP = null;
        MessageDigest md5;
        KeyFactory kf = null;
        RSAPublicKeySpec k = null;

        try {
            kf = KeyFactory.getInstance("RSA");
            k = kf.getKeySpec(serverKey, RSAPublicKeySpec.class);

            md5 = MessageDigest.getInstance("MD5");
            md5.reset();

            byte[] bData = "ssh-rsa".getBytes();
            byte[] bLen = intToDWord(bData.length);
            md5.update(bLen, 0, bLen.length);
            md5.update(bData, 0, bData.length);

            bData = k.getPublicExponent().toByteArray();
            bLen = intToDWord(bData.length);
            ;
            md5.update(bLen, 0, bLen.length);
            md5.update(bData, 0, bData.length);

            bData = k.getModulus().toByteArray();
            bLen = intToDWord(bData.length);
            ;
            md5.update(bLen, 0, bLen.length);
            md5.update(bData, 0, bData.length);

            baFP = md5.digest();
            log.debug("Server fingerprint: " + BufferUtils.printHex(baFP));

        } catch (Exception e) {
            log.error("Unable to calculate fingerprint: " + e);
        }

        return baFP;
    }

    @Override
    public boolean verifyServerKey(ClientSession sshClientSession, SocketAddress remoteAddress, PublicKey serverKey) {
        boolean fReturn = true;
        serverKeyFingerprint = getKeyFingerprint(serverKey);
        if (serverKeyFingerprint == null) {
            fReturn = false;
        }

        return fReturn;
    }

    public byte[] getServerFingerprint() {
        return serverKeyFingerprint;
    }
}
