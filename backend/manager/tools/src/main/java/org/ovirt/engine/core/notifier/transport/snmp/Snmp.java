package org.ovirt.engine.core.notifier.transport.snmp;

import java.io.IOException;
import java.net.InetAddress;
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
import org.ovirt.engine.core.notifier.NotificationServiceException;
import org.ovirt.engine.core.notifier.dao.DispatchResult;
import org.ovirt.engine.core.notifier.filter.AuditLogEvent;
import org.ovirt.engine.core.notifier.transport.Transport;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.mp.SnmpConstants;
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

    private final Map<String, Profile> profiles = new HashMap<>();
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
        int auditLogTypeVal = AuditLogType.UNASSIGNED.getValue();
        try {
            // TODO mtayer: what about db? add to audit log type

            auditLogTypeVal = AuditLogType.valueOf(event.getName()).getValue();
        } catch (IllegalArgumentException e) {
            log.warn("Could not find event: " + event.getName() + " in auditLogTypes");
        }
        OID trapOID = SnmpConstants.getTrapOID(profile.oid, ENTERPRISE_SPECIFIC, auditLogTypeVal);
        v2pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, trapOID));
        v2pdu.add(new VariableBinding(SnmpConstants.sysUpTime,
                                      new TimeTicks((System.nanoTime() - nanoStart) / 10000000)));
        v2pdu.add(new VariableBinding(
                new OID(trapOID).append(0),
                new OctetString(event.getMessage())));
        v2pdu.add(new VariableBinding(
                new OID(trapOID).append(1),
                new OctetString(event.getSeverity().name())));
        v2pdu.add(new VariableBinding(
                new OID(trapOID).append(2),
                new OctetString(event.getType().name())));
        v2pdu.add(new VariableBinding(
                new OID(trapOID).append(3),
                new OctetString(event.getLogTime().toString())));
        v2pdu.add(new VariableBinding(
                new OID(trapOID).append(4),
                new OctetString(event.getLogTypeName())));
        if (!StringUtils.isEmpty(event.getUserName())) {
            v2pdu.add(new VariableBinding(
                    new OID(trapOID).append(5),
                    new OctetString(event.getUserName())));
        }
        if (!StringUtils.isEmpty(event.getVmName())) {
            v2pdu.add(new VariableBinding(
                    new OID(trapOID).append(6),
                    new OctetString(event.getVmName())));
        }
        if (!StringUtils.isEmpty(event.getVdsName())) {
            v2pdu.add(new VariableBinding(
                    new OID(trapOID).append(7),
                    new OctetString(event.getVdsName())));
        }
        if (!StringUtils.isEmpty(event.getVmTemplateName())) {
            v2pdu.add(new VariableBinding(
                    new OID(trapOID).append(8),
                    new OctetString(event.getVmTemplateName())));
        }
        if (!StringUtils.isEmpty(event.getStoragePoolName())) {
            v2pdu.add(new VariableBinding(
                    new OID(trapOID).append(9),
                    new OctetString(event.getStoragePoolName())));
        }
        if (!StringUtils.isEmpty(event.getStorageDomainName())) {
            v2pdu.add(new VariableBinding(
                    new OID(trapOID).append(10),
                    new OctetString(event.getStorageDomainName())));
        }
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(profile.community);
        target.setVersion(SnmpConstants.version2c);
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
