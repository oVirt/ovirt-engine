package org.ovirt.engine.core.notifier.transport.snmp;

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.notifier.NotificationServiceException;
import org.ovirt.engine.core.notifier.dao.DispatchResult;
import org.ovirt.engine.core.notifier.filter.AuditLogEvent;
import org.ovirt.engine.core.notifier.transport.Transport;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivAES192;
import org.snmp4j.security.PrivAES256;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class Snmp extends Transport {

    private static final Logger log = LoggerFactory.getLogger(Snmp.class);

    private static final String SNMP_MANAGERS = "SNMP_MANAGERS";
    private static final String SNMP_COMMUNITY = "SNMP_COMMUNITY";
    private static final String SNMP_ENGINE_ID = "SNMP_ENGINE_ID";
    private static final String SNMP_USERNAME = "SNMP_USERNAME";
    private static final String SNMP_AUTH_PROTOCOL = "SNMP_AUTH_PROTOCOL";
    private static final String SNMP_AUTH_PASSPHRASE = "SNMP_AUTH_PASSPHRASE";
    private static final String SNMP_PRIVACY_PROTOCOL = "SNMP_PRIVACY_PROTOCOL";
    private static final String SNMP_PRIVACY_PASSPHRASE = "SNMP_PRIVACY_PASSPHRASE";
    private static final String SNMP_SECURITY_LEVEL = "SNMP_SECURITY_LEVEL";
    private static final String SNMP_OID = "SNMP_OID";
    private static final String SNMP_VERSION = "SNMP_VERSION";
    private static final int ENTERPRISE_SPECIFIC = 6;
    private static final Pattern PROFILE_PATTERN = Pattern.compile(SNMP_MANAGERS + "(|_(?<profile>.*))");

    /** OIDs - See OVIRT-MIB.txt */
    public static final int AUDIT = 1;

    private static final OID OBJECTS_AUDIT = new OID(new int[] {2, 1});

    public static final int INSTANCE_ID = 1;
    public static final int NAME = 2;
    public static final int ID = 3;
    public static final int SEVERITY = 4;
    public static final int MESSAGE = 5;
    public static final int STATUS = 6;
    public static final int DATETIME = 7;
    public static final int USERNAME = 100;
    public static final int USER_ID = 101;
    public static final int VM_NAME = 102;
    public static final int VM_ID = 103;
    public static final int VDS_NAME = 104;
    public static final int VDS_ID = 105;
    public static final int VM_TEMPLATE_NAME = 106;
    public static final int VM_TEMPLATE_ID = 107;
    public static final int STORAGE_POOL_NAME = 108;
    public static final int STORAGE_POOL_ID = 109;
    public static final int STORAGE_DOMAIN_NAME = 110;
    public static final int STORAGE_DOMAIN_ID = 111;


    private final Map<String, Profile> profiles = new HashMap<>();

    private static final String ISO8601 = "yyyy-MM-dd'T'HH:mm'Z'";
    private boolean active = false;
    private static long nanoStart = System.nanoTime();
    private Map<Profile, org.snmp4j.Snmp> profileSnmpMap = new HashMap<>();

    public Snmp(NotificationProperties props) {
        for (Map.Entry<String, String> entry : props.getProperties().entrySet()) {
            Matcher m = PROFILE_PATTERN.matcher(entry.getKey());
            if (m.matches()) {
                String profile = m.group("profile") == null ? "" : m.group("profile");
                String managers = props.getProperty(SNMP_MANAGERS, profile, false);
                if (!StringUtils.isBlank(managers)) {
                    int snmpVersion = getSnmpVersion(props.getProperty(SNMP_VERSION, false));
                    int securityLevel = Integer.parseInt(props.getProperty(SNMP_SECURITY_LEVEL, profile, false));
                    if (snmpVersion == SnmpConstants.version3) {
                        profiles.put(
                                profile,
                                Profile.buildProfile(
                                        managers,
                                        props.getProperty(SNMP_ENGINE_ID, profile, false),
                                        props.getProperty(SNMP_USERNAME, profile, false),
                                        securityLevel == 1 ?
                                                null :
                                                getAuthProtocol(props.getProperty(SNMP_AUTH_PROTOCOL, profile, false)),
                                        props.getProperty(SNMP_AUTH_PASSPHRASE, profile, securityLevel == 1),
                                        securityLevel != 3 ?
                                                null :
                                                getPrivProtocol(props.getProperty(SNMP_PRIVACY_PROTOCOL, profile, false)),
                                        props.getProperty(SNMP_PRIVACY_PASSPHRASE, profile, securityLevel != 3),
                                        securityLevel,
                                        props.getProperty(SNMP_OID, profile, false),
                                        snmpVersion
                                )
                        );
                    } else {
                        profiles.put(
                                profile,
                                Profile.buildProfile(
                                        managers,
                                        props.getProperty(SNMP_COMMUNITY, profile, false),
                                        props.getProperty(SNMP_OID, profile, false),
                                        snmpVersion
                                )
                        );
                    }
                }
            }
        }

        if (!profiles.isEmpty()) {
            active = true;
        }
    }

    private int getSnmpVersion(String ver) {
        int version;
        switch(ver) {
            case "2":
                version = SnmpConstants.version2c;
                break;
            case "3":
                version = SnmpConstants.version3;
                break;
            default:
                throw new NotificationServiceException("Unknown SNMP_VERSION in properties file " + ver);
        }
        return version;
    }

    private OID getAuthProtocol(String authProtocol) {
        switch(authProtocol) {
            case "MD5":
                return AuthMD5.ID;
            case "SHA":
                return AuthSHA.ID;
            default:
                throw new NotificationServiceException("Unknown SNMP_AUTH_PROTOCOL in properties file " + authProtocol);
        }
    }

    private OID getPrivProtocol(String privProtocol) {
        switch(privProtocol) {
            case "AES128":
                return PrivAES128.ID;
            case "AES192":
                return PrivAES192.ID;
            case "AES256":
                return PrivAES256.ID;
            default:
                throw new NotificationServiceException("Unknown SNMP_PRIVACY_PROTOCOL in properties file " + privProtocol);
        }
    }

    @Override
    public String getName() {
        return "snmp";
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void dispatchEvent(AuditLogEvent event, String address) {
        Profile profile = profiles.get(address);
        if (profile == null) {
            profile = profiles.get("");
            if (profile == null) {
                log.warn("Could not find snmp profile: {}", address);
                return;
            }
        }

        PDU pdu;
        org.snmp4j.Snmp snmpForProfile = getSnmpForProfile(profile);
        if (profile.version == SnmpConstants.version3) {
            // Create PDU for V3
            pdu = new ScopedPDU();
            ((ScopedPDU) pdu).setContextEngineID(profile.engineId);
        } else {
            // PDU class is for SNMPv2c units
            pdu = new PDU();
        }
        pdu.setType(PDU.TRAP);

        addPayload(pdu, event, profile);

        Target target = createTarget(profile);
        for (Host host : profile.hosts) {
            try {
                log.info("Generate an snmp trap for event: {} to address: {} ",
                    event, host.name);
                target.setAddress(
                    new UdpAddress(
                        InetAddress.getByName(host.name),
                        host.port
                    )
                );
                snmpForProfile.send(pdu, target);
                notifyObservers(DispatchResult.success(event, address, EventNotificationMethod.SNMP));
            } catch (Exception e) {
                log.error(e.getMessage());
                notifyObservers(DispatchResult.failure(event, address, EventNotificationMethod.SNMP, e.getMessage()));
            }
        }
    }

    private synchronized org.snmp4j.Snmp getSnmpForProfile(Profile profile) {
        if (!profileSnmpMap.containsKey(profile)) {
            profileSnmpMap.put(profile,
                    profile.version == SnmpConstants.version3 ? createSnmp3(profile) : createSnmp());
        }
        return profileSnmpMap.get(profile);
    }

    private org.snmp4j.Snmp createSnmp() {
        try {
            // Create a new session and define it's transport.
            return new org.snmp4j.Snmp(new DefaultUdpTransportMapping());
        } catch (IOException e) {
            throw new NotificationServiceException("error creating " + getClass().getName());
        }
    }

    private org.snmp4j.Snmp createSnmp3(Profile profile) {
        try {
            TransportMapping<?> transport = new DefaultUdpTransportMapping();
            org.snmp4j.Snmp snmp = new org.snmp4j.Snmp(transport);
            SecurityProtocols securityProtocols = SecurityProtocols.getInstance();
            securityProtocols.addDefaultProtocols();
            securityProtocols.addAuthenticationProtocol(new AuthMD5());
            securityProtocols.addAuthenticationProtocol(new AuthSHA());
            securityProtocols.addPrivacyProtocol(new PrivAES128());
            securityProtocols.addPrivacyProtocol(new PrivAES192());
            securityProtocols.addPrivacyProtocol(new PrivAES256());
            USM usm = new USM(securityProtocols, profile.engineId, 0);
            ((org.snmp4j.mp.MPv3) snmp.getMessageProcessingModel(org.snmp4j.mp.MPv3.ID))
                    .setLocalEngineID(profile.engineId.getValue());
            ((org.snmp4j.mp.MPv3) snmp.getMessageProcessingModel(org.snmp4j.mp.MPv3.ID))
                    .getSecurityModels().addSecurityModel(usm);
            SecurityModels.getInstance().addSecurityModel(
                    usm);
            transport.listen();
            snmp.getUSM().addUser(
                    profile.username,
                    getUsmUser(profile));
            return snmp;
        } catch (IOException e) {
            throw new NotificationServiceException("error creating version 3 snmp " + getClass().getName());
        }
    }

    private UsmUser getUsmUser(Profile profile) {
        OID authenticationProtocol = null;
        OctetString authenticationPassphrase = null;
        OID privacyProtocol = null;
        OctetString privacyPassphrase = null;
        switch(profile.securityLevel) {
            case SecurityLevel.AUTH_NOPRIV:
                authenticationProtocol = profile.authProtocol;
                authenticationPassphrase = profile.authPassphrase;
                break;
            case SecurityLevel.AUTH_PRIV:
                authenticationProtocol = profile.authProtocol;
                authenticationPassphrase = profile.authPassphrase;
                privacyProtocol = profile.privProtocol;
                privacyPassphrase = profile.privacyPassphrase;
                break;
        }
        return new UsmUser(profile.username,
                authenticationProtocol,
                authenticationPassphrase,
                privacyProtocol,
                privacyPassphrase);
    }

    private Target createTarget(Profile profile) {
        Target target;
        if (profile.version == SnmpConstants.version2c) {
            target = new CommunityTarget();
            ((CommunityTarget) target).setCommunity(profile.community);
        } else {
            target = new UserTarget();
            ((UserTarget) target).setAuthoritativeEngineID(profile.engineId.getValue());
            target.setSecurityName(profile.username);
            target.setSecurityLevel(profile.securityLevel);
        }
        target.setVersion(profile.version);
        return target;
    }

    private void addPayload(PDU pdu, AuditLogEvent event, Profile profile) {

        pdu.add(new VariableBinding(SnmpConstants.sysUpTime,
                                      new TimeTicks((System.nanoTime() - nanoStart) / 10000000)));
        // { [baseoid] notifications(0) audit(1) }
        pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID,
                                      SnmpConstants.getTrapOID(new OID(profile.oid),
                                                               ENTERPRISE_SPECIFIC,
                                                               AUDIT)));

        int auditLogId = AuditLogType.UNASSIGNED.getValue();
        try {
            auditLogId = AuditLogType.valueOf(event.getName()).getValue();
        } catch (IllegalArgumentException e) {
            log.warn("Could not find event: {} in auditLogTypes", event.getName());
        }
        // { [baseoid] objects(2) audit(1) }
        OID auditObjects = new OID(profile.oid).append(OBJECTS_AUDIT);

        addString(pdu, auditObjects, INSTANCE_ID, Long.toString(event.getId()), true);
        addString(pdu, auditObjects, NAME, event.getName(), true);
        addInt(pdu, auditObjects, ID, auditLogId, true);
        addInt(pdu, auditObjects, SEVERITY, event.getSeverity().getValue(), true);
        addString(pdu, auditObjects, MESSAGE, event.getMessage(), true);
        addInt(pdu, auditObjects, STATUS, event.getType().getValue(), true);
        addString(pdu, auditObjects, DATETIME, new SimpleDateFormat(ISO8601).format(event.getLogTime()), true);

        // Optional pdu:
        addString(pdu, auditObjects, USERNAME, event.getUserName(), false);
        addUuid(pdu, auditObjects, USER_ID, event.getUserId());
        addString(pdu, auditObjects, VM_NAME, event.getVmName(), false);
        addUuid(pdu, auditObjects, VM_ID, event.getVmId());
        addString(pdu, auditObjects, VDS_NAME, event.getVdsName(), false);
        addUuid(pdu, auditObjects, VDS_ID, event.getVdsId());
        addString(pdu, auditObjects, VM_TEMPLATE_NAME, event.getVmTemplateName(), false);
        addUuid(pdu, auditObjects, VM_TEMPLATE_ID, event.getVmTemplateId());
        addString(pdu, auditObjects, STORAGE_POOL_NAME, event.getStoragePoolName(), false);
        addUuid(pdu, auditObjects, STORAGE_POOL_ID, event.getStoragePoolId());
        addString(pdu, auditObjects, STORAGE_DOMAIN_NAME, event.getStorageDomainName(), false);
        addUuid(pdu, auditObjects, STORAGE_DOMAIN_ID, event.getStorageDomainId());
    }

    private void addString(PDU pdu, OID prefix, int suffix, String val, boolean allowEmpty) {
        if (allowEmpty || !StringUtils.isEmpty(val)) {
            pdu.add(new VariableBinding(new OID(prefix).append(suffix), new OctetString(val == null ? "" : val)));
        }
    }

    private void addInt(PDU pdu, OID prefix, int suffix, Integer val, boolean allowEmpty) {
        if (allowEmpty || val != null) {
            pdu.add(new VariableBinding(new OID(prefix).append(suffix), new Integer32(val == null ? 0 : val)));
        }
    }

    private void addUuid(PDU pdu, final OID prefix, int suffix, Guid val) {
        if (!Guid.isNullOrEmpty(val)) {
            addString(pdu, prefix, suffix, val.toString(), false);
        }
    }

    static class Host {
        public String name;
        public int port = 162;

        public Host(String name, String port) {
            this.name = name;
            if (port != null) {
                try {
                    this.port = Integer.parseInt(port);
                    if (this.port <= 0 || this.port > 0xffff) {
                        throw new Exception("Bad port");
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        String.format(
                            "Invalid port %s for snmp host %s",
                            port,
                            name
                        )
                    );
                }
            }
        }
    }

    static class Profile {
        private static final Pattern HOST_PATTERN = Pattern.compile(
            "(?<host>(([^\\[:\\s]+)|(\\[[^\\]]+\\])))(:(?<port>[^\\s]*))?");

        public List<Host> hosts = new LinkedList<>();
        public int version;
        public OID oid;

        // Version 2 specific variables
        public OctetString community;

        // Version 3 specific variables
        public OctetString engineId;
        public OctetString username;
        public OID authProtocol;
        public OctetString authPassphrase;
        public OID privProtocol;
        public OctetString privacyPassphrase;
        public int securityLevel;

        public Profile(String managers, String oid, int version) {
            Matcher m = HOST_PATTERN.matcher(managers);
            while (m.find()) {
                hosts.add(new Host(m.group("host"), m.group("port")));
            }
            this.oid = new OID(oid);
            if (!this.oid.isValid()) {
                throw new IllegalArgumentException(
                        String.format(
                                "Invalid oid '%s'",
                                oid
                        )
                );

            }
            this.version = version;
        }

        public static Profile buildProfile(String managers, String community, String oid, int version) {
            Profile profile = new Profile(managers, oid, version);
            profile.community = new OctetString(community);
            return profile;
        }

        public static Profile buildProfile(String managers,
                                           String engineId,
                                           String username,
                                           OID authProtocol,
                                           String authPassphrase,
                                           OID privProtocol,
                                           String privacyPassphrase,
                                           int securityLevel,
                                           String oid,
                                           int version) {
            Profile profile = new Profile(managers, oid, version);
            profile.engineId = OctetString.fromHexString(engineId);
            profile.username = new OctetString(username);
            profile.authProtocol = authProtocol;
            profile.authPassphrase = new OctetString(authPassphrase);
            profile.privProtocol = privProtocol;
            profile.privacyPassphrase = new OctetString(privacyPassphrase);
            profile.securityLevel = securityLevel;
            return profile;
        }
    }
}
