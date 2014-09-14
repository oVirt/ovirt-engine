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
import org.apache.log4j.Logger;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.notifier.NotificationServiceException;
import org.ovirt.engine.core.notifier.dao.DispatchResult;
import org.ovirt.engine.core.notifier.filter.AuditLogEvent;
import org.ovirt.engine.core.notifier.transport.Transport;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Target;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class Snmp extends Transport {

    private static final Logger log = Logger.getLogger(Snmp.class);

    private static final String SNMP_MANAGERS = "SNMP_MANAGERS";
    private static final String SNMP_COMMUNITY = "SNMP_COMMUNITY";
    private static final String SNMP_OID = "SNMP_OID";
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
    private org.snmp4j.Snmp snmp = null;
    private boolean active = false;
    private static long nanoStart = System.nanoTime();

    public Snmp(NotificationProperties props) {
        for (Map.Entry<String, String> entry : props.getProperties().entrySet()) {
            Matcher m = PROFILE_PATTERN.matcher(entry.getKey());
            if (m.matches()) {
                String profile = m.group("profile") == null ? "" : m.group("profile");
                String managers = props.getProperty(SNMP_MANAGERS, profile, false);
                if (!StringUtils.isBlank(managers)) {
                    profiles.put(
                        profile,
                        new Profile(
                            managers,
                            props.getProperty(SNMP_COMMUNITY, profile, false),
                            props.getProperty(SNMP_OID, profile, false)
                        )
                    );
                }
            }
        }

        if (!profiles.isEmpty()) {
            try {
                // Create a new session and define it's transport.
                snmp = new org.snmp4j.Snmp(new DefaultUdpTransportMapping());
            } catch (IOException e) {
                throw new NotificationServiceException("error creating " + getClass().getName());
            }
            active = true;
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
                log.warn("Could not find snmp profile: " + address);
                return;
            }
        }

        // PDU class is for SNMPv2c units
        PDU v2pdu = new PDU();
        v2pdu.setType(PDU.TRAP);

        addPayload(v2pdu, event, profile);

        Target target = createTarget(profile);
        for (Host host : profile.hosts) {
            try {
                log.info(String.format("Generate an snmp trap for event: %s to address: %s ",
                    event, host.name));
                target.setAddress(
                    new UdpAddress(
                        InetAddress.getByName(host.name),
                        host.port
                    )
                );
                snmp.send(v2pdu, target);
                notifyObservers(DispatchResult.success(event, address, EventNotificationMethod.SNMP));
            } catch (Exception e) {
                log.error(e.getMessage());
                notifyObservers(DispatchResult.failure(event, address, EventNotificationMethod.SNMP, e.getMessage()));
            }
        }
    }

    private CommunityTarget createTarget(Profile profile) {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(profile.community);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }

    private void addPayload(PDU v2pdu, AuditLogEvent event, Profile profile) {

        v2pdu.add(new VariableBinding(SnmpConstants.sysUpTime,
                                      new TimeTicks((System.nanoTime() - nanoStart) / 10000000)));
        // { [baseoid] notifications(0) audit(1) }
        v2pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID,
                                      SnmpConstants.getTrapOID(new OID(profile.oid),
                                                               ENTERPRISE_SPECIFIC,
                                                               AUDIT)));

        int auditLogId = AuditLogType.UNASSIGNED.getValue();
        try {
            auditLogId = AuditLogType.valueOf(event.getName()).getValue();
        } catch (IllegalArgumentException e) {
            log.warn("Could not find event: " + event.getName() + " in auditLogTypes");
        }
        // { [baseoid] objects(2) audit(1) }
        OID auditObjects = new OID(profile.oid).append(OBJECTS_AUDIT);

        addString(v2pdu, auditObjects, INSTANCE_ID, Long.toString(event.getId()), true);
        addString(v2pdu, auditObjects, NAME, event.getName(), true);
        addInt(v2pdu, auditObjects, ID, auditLogId, true);
        addInt(v2pdu, auditObjects, SEVERITY, event.getSeverity().getValue(), true);
        addString(v2pdu, auditObjects, MESSAGE, event.getMessage(), true);
        addInt(v2pdu, auditObjects, STATUS, event.getType().getValue(), true);
        addString(v2pdu, auditObjects, DATETIME, new SimpleDateFormat(ISO8601).format(event.getLogTime()), true);

        // Optional pdu:
        addString(v2pdu, auditObjects, USERNAME, event.getUserName(), false);
        addUuid(v2pdu, auditObjects, USER_ID, event.getUserId());
        addString(v2pdu, auditObjects, VM_NAME, event.getVmName(), false);
        addUuid(v2pdu, auditObjects, VM_ID, event.getVmId());
        addString(v2pdu, auditObjects, VDS_NAME, event.getVdsName(), false);
        addUuid(v2pdu, auditObjects, VDS_ID, event.getVdsId());
        addString(v2pdu, auditObjects, VM_TEMPLATE_NAME, event.getVmTemplateName(), false);
        addUuid(v2pdu, auditObjects, VM_TEMPLATE_ID, event.getVmTemplateId());
        addString(v2pdu, auditObjects, STORAGE_POOL_NAME, event.getStoragePoolName(), false);
        addUuid(v2pdu, auditObjects, STORAGE_POOL_ID, event.getStoragePoolId());
        addString(v2pdu, auditObjects, STORAGE_DOMAIN_NAME, event.getStorageDomainName(), false);
        addUuid(v2pdu, auditObjects, STORAGE_DOMAIN_ID, event.getStorageDomainId());
    }

    private void addString(PDU v2pdu, OID prefix, int suffix, String val, boolean allowEmpty) {
        if (allowEmpty || !StringUtils.isEmpty(val)) {
            v2pdu.add(new VariableBinding(new OID(prefix).append(suffix), new OctetString(val == null ? "" : val)));
        }
    }

    private void addInt(PDU v2pdu, OID prefix, int suffix, Integer val, boolean allowEmpty) {
        if (allowEmpty || val != null) {
            v2pdu.add(new VariableBinding(new OID(prefix).append(suffix), new Integer32(val == null ? 0 : val)));
        }
    }

    private void addUuid(PDU v2pdu, final OID prefix, int suffix, Guid val) {
        if (!Guid.isNullOrEmpty(val)) {
            addString(v2pdu, prefix, suffix, val.toString(), false);
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
        Pattern HOST_PATTERN = Pattern.compile("(?<host>(([^\\[:\\s]+)|(\\[[^\\]]+\\])))(:(?<port>[^\\s]*))?");

        public List<Host> hosts = new LinkedList<>();
        public OctetString community;
        public OID oid;

        public Profile(String managers, String community, String oid) {
            Matcher m = HOST_PATTERN.matcher(managers);
            while (m.find()) {
                hosts.add(new Host(m.group("host"), m.group("port")));
            }
            this.community = new OctetString(community);
            this.oid = new OID(oid);
            if (!this.oid.isValid()) {
                throw new IllegalArgumentException(
                    String.format(
                        "Invalid oid '%s'",
                        oid
                    )
                );

            }
        }
    }
}
