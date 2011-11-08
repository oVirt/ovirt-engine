/**
 *
 */
package org.ovirt.engine.core.dns;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.spi.NamingManager;

import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * Utility class to query DNS SRV records, and return results according to the priority/weights algorithm as specified
 * in RFC 2782
 *
 */
public class DnsSRVLocator {

    private static final String DNS_QUERY_PREFIX = "dns:///";
    private static final String SRV_RECORD = "SRV";
    private static Pattern SPACE_PATTERN = Pattern.compile(" ");
    private static SrvRecord invalidRecord = new SrvRecord(false, false, 0, 0, 0, "");
    private int numberOfValidRecords = 0;
    private Random random = new Random(System.currentTimeMillis());

    public static final String TCP = "_tcp";
    public static final String UDP = "_udp";

    /**
     * Holds information on a retrieved SRV record
     *
     */
    public static class SrvRecord implements Comparable<SrvRecord> {
        private boolean valid;
        private int weight;
        private int priority;
        private int sum;
        private String address;
        private boolean used;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("valid: ").append(valid).append(" sum: ").append(sum).append("priority: ").append(priority)
                    .append(" weight: ").append(weight).append(" hostport: ").append(address);
            return sb.toString();
        }

        public SrvRecord(int priority, int weight, String hostPort) {
            this(true, false, 0, priority, weight, hostPort);
        }

        public SrvRecord(boolean valid, boolean used, int sum, int priority, int weight, String address) {
            this.valid = valid;
            this.used = used;
            this.sum = sum;
            this.priority = priority;
            this.weight = weight;
            this.address = address;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean isValid) {
            this.valid = isValid;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public int getSum() {
            return sum;
        }

        public void setSum(int sum) {
            this.sum = sum;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public boolean isUsed() {
            return used;
        }

        public void setUsed(boolean isUsed) {
            this.used = isUsed;
        }

        @Override
        public int compareTo(SrvRecord other) {

            // Sort in ascending order where invalid (non parsable) records are
            // last
            // Records with lower priority value come first
            // For a group of records with same priority, records with weight 0
            // come first
            if (valid && !other.valid) {
                return -1;
            }
            if (!valid && other.valid) {
                return 1;
            }
            if (priority < other.priority) {
                return -1;
            }
            if (priority > other.priority) {
                return 1;
            }
            if (weight == 0 && other.weight != 0) {
                return -1;
            }
            if (weight != 0) {
                return 1;
            }
            return 0;
        }
    }

    public static class DnsSRVResult {
        private int numOfValidAddresses;
        private String[] addresses;

        public DnsSRVResult(int numOfValidAddresses, String[] addresses) {
            this.numOfValidAddresses = numOfValidAddresses;
            this.addresses = addresses;
        }

        public int getNumOfValidAddresses() {
            return numOfValidAddresses;
        }

        public String[] getAddresses() {
            return addresses;
        }
    }

    public DnsSRVResult getService(String service, String protocol, String domain) throws Exception {
        StringBuilder dnsQuery = new StringBuilder();
        dnsQuery.append(service).append(".").append(protocol).append(".").append(domain);

        try {
            return getService(dnsQuery.toString());
        } catch (Exception ex) {
            log.errorFormat("Error: could not find DNS SRV record name: {0}.{1}.{2}.\nException message is: {3}\n" +
                    "Possible causes: missing DNS entries in the DNS server or DNS resolving" +
                    " issues from engine-core machine.\nPlease Ensure correct DNS entries exist in the DNS server" +
                    " and ensure the DNS server is reachable from the engine-core machine.",
                    service,
                    protocol,
                    domain,
                    ex.getMessage());
            throw ex;
        }
    }

    private DnsSRVResult getService(String dnsUrl) throws NamingException {
        Context ctx = NamingManager.getURLContext("dns", new Hashtable(0));

        if (!(ctx instanceof DirContext)) {
            return null; // cannot create a DNS context
        }

        StringBuilder fullDnsURL = new StringBuilder(DNS_QUERY_PREFIX);
        fullDnsURL.append(dnsUrl);
        Attributes attrs = ((DirContext) ctx).getAttributes(fullDnsURL.toString(), new String[] { SRV_RECORD });

        if (attrs == null) {
            return null;
        }

        Attribute attr = attrs.get(SRV_RECORD);
        if (attr == null) {
            return null;
        }

        int numOfRecords = attr.size();
        String[] records = new String[numOfRecords];
        for (int counter = 0; counter < numOfRecords; counter++) {
            records[counter] = (String) attr.get(counter);
        }

        return getSRVResult(records);
    }

    public DnsSRVResult getSRVResult(String[] recordsList) {
        int numOfRecords = recordsList.length;
        if (recordsList == null || numOfRecords == 0) {
            return null;
        }
        // Read records as retrieved from DNS
        SrvRecord[] records = new SrvRecord[numOfRecords];
        int counter = 0;
        for (String recordStr : recordsList) {
            SrvRecord srvRecord = parseSrvRecord(recordStr);
            records[counter++] = srvRecord;
        }
        // Sort the records
        Arrays.sort(records);
        int priority = -1;
        int lastPriorityIndex = -1;
        int priorityIndex = -1;
        int startPriorityIndex = -1;
        String[] addresses = new String[numOfRecords];
        // Total number of service addresses
        int numOfAddreses = -1;
        // Iterates over the records, and calculates for each
        // priority the index of the first SRV record that contains
        // the index of last SRVV record that contains the priority
        // For each group of records with same priorities, gets a list of services
        for (SrvRecord record : records) {
            if (!record.isValid()) {
                break;
            }
            lastPriorityIndex = priorityIndex;
            priorityIndex++;
            int currentPriority = record.getPriority();
            if (currentPriority != priority) {
                if (lastPriorityIndex != -1) {
                    // This means that this is the end of a group of records
                    // with same
                    // priority - get their service addresses
                    numOfAddreses = fillServiceAddress(records, startPriorityIndex, lastPriorityIndex, addresses,
                            numOfAddreses);

                }
                startPriorityIndex = priorityIndex;
                priority = currentPriority;

            }
            lastPriorityIndex = priorityIndex;
        }
        numOfAddreses = fillServiceAddress(records, startPriorityIndex, lastPriorityIndex, addresses, numOfAddreses);

        // numOfAddresses points to the last index of valid address in the
        // addresses array
        // Increase it by 1 in order to truely reflect the number of valid
        // addresses
        return new DnsSRVResult(numOfAddreses + 1, addresses);
    }

    private int fillServiceAddress(SrvRecord[] records,
            int startPriorityIndex,
            int lastPriorityIndex,
            String[] addresses,
            int numOfAddressess) {

        // Run the following algorithm for determining the order of service entries for a
        // group of SRV records with same
        // priority:
        // 1. For each SRV record calculate its sum based on the sum of weights
        // of its weight and
        // the weight of all preceding SRV records
        // 2. Select a random value between 0 and the sum (inclusive)
        // 3. Iterate over the group until a record with sum that is greater or
        // above
        // to the generated random value is encountered
        // 4. This will be the next select SRV record - make it not used for
        // next round of the algorithm
        int numOfRepetitions = (lastPriorityIndex - startPriorityIndex) + 1;
        int totalSum = 0;
        for (int counter = 0; counter < numOfRepetitions; counter++) {
            for (int index = startPriorityIndex; index <= lastPriorityIndex; index++) {
                if (!records[index].isUsed()) {
                    totalSum += records[index].getWeight();
                    records[index].setSum(totalSum);
                }
            }
            int randResult = random.nextInt(totalSum + 1);
            for (int index = startPriorityIndex; index <= lastPriorityIndex; index++) {
                boolean found = false;
                if (!found && !records[index].isUsed()) {
                    if (records[index].getSum() >= randResult) {
                        records[index].setUsed(true);
                        addresses[++numOfAddressess] = records[index].getAddress();
                        found = true;
                    }
                }
            }
        }
        return numOfAddressess;
    }

    private SrvRecord parseSrvRecord(String recordStr) {

        // SRV record looks like: PRIORITY WEIGHT PORT HOST
        Scanner s = new Scanner(recordStr).useDelimiter(SPACE_PATTERN);
        try {
            int priority = s.nextInt();
            int weight = s.nextInt();
            String port = s.next();
            String host = s.next();
            StringBuilder sb = new StringBuilder(host);
            sb.append(":").append(port);
            numberOfValidRecords++;
            return new SrvRecord(priority, weight, sb.toString());
        } catch (InputMismatchException ex) {
            log.errorFormat("the record {0} has invalid format", recordStr);

            // In case there is a parsing error, the invalid record constant is
            // returned
            return invalidRecord;
        }
    }

    public URI constructURI(String protocol, String address) throws URISyntaxException {
        String[] parts = address.split("\\.:");
        if (parts.length != 2) {
            throw new IllegalArgumentException("the address in SRV record should contain host and port");
        }

        StringBuilder uriSB = new StringBuilder(protocol);
        uriSB.append("://").append(parts[0]).append(":").append(parts[1]);
        return new URI(uriSB.toString());
    }

    private static Log log = LogFactory.getLog(DnsSRVLocator.class);

}
