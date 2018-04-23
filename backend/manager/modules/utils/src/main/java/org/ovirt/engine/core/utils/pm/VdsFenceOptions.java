package org.ovirt.engine.core.utils.pm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.IntegerCompat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VdsFenceOptions implements Serializable {

    private static final long serialVersionUID = -8832636627473217232L;
    private static final String COMMA = ",";
    private static final String EQUAL = "=";
    private static final String NEWLINE = "\n";
    private static final String SEMICOLON = ";";
    private static final String COLON = ":";
    private static final String TRUE_STRING = "true";
    private static final String FALSE_STRING = "false";
    private static final String YES = "yes";
    private static final String NO = "no";
    private static final String AGENT_ERROR = "Cannot find fence agent named '{}' in fence option mapping";
    private static final String MAPPING_FORMAT_ERROR = "Illegal fence mapping format '{}'";

    private static final Logger log = LoggerFactory.getLogger(VdsFenceOptions.class);
    private Map<String, Map<String, String>> fenceOptionMapping;
    private static Map<String, String> fenceOptionTypes;

    private String fenceAgent = "";
    private String fenceOptions;
    private static Map<String, String> fenceAgentInstanceOptions;
    private static Set<String> fenceSpecialParams;
    private String version;

    /**
     * Initializes a new instance of the <see cref="VdsFenceOptions"/> class.
     */
    public VdsFenceOptions(String version) {
        this(null, null, version);
    }

    /**
     * Initializes a new instance of the <see cref="VdsFenceOptions"/> class.
     * @param agent
     *            The agent.
     * @param fenceOptions
     *            The fence options.
     */
    public VdsFenceOptions(String agent, String fenceOptions, String version) {
        if (StringUtils.isNotEmpty(agent)) {
            this.fenceAgent = agent;
            this.fenceOptions = fenceOptions;
        }
        this.version = version;
        initCache();
        init();
    }

    public Map<String, Map<String, String>> getFenceOptionMappingMap() {
        return fenceOptionMapping;
    }


    /**
     * Caches the fence agents options mapping. Mapping are stored in the following format <!--
     * <agent>:{var=value}{[,]var=value}*; --> for example :
     * alom:secure=secure,port=ipport;apc:secure=secure,port=ipport,slot=port
     */
    private void cacheFenceAgentsOptionMapping() {
        String localFenceOptionMapping = FenceConfigHelper.getFenceConfigurationValue(ConfigValues.VdsFenceOptionMapping.name(), version);
        String[] agentsOptionsStr = localFenceOptionMapping.split(Pattern.quote(SEMICOLON), -1);
        for (String agentOptionsStr : agentsOptionsStr) {
            String[] parts = agentOptionsStr.split(Pattern.quote(COLON), -1);
            if (parts.length == 2) {
                String agent = parts[0];
                Map<String, String> agentOptions = new HashMap<>();
                // check for empty options
                if (StringUtils.isNotEmpty(parts[1])) {
                    String[] options = parts[1].split(Pattern.quote(COMMA), -1);
                    for (String option : options) {
                        String[] optionKeyVal = option.split(Pattern.quote(EQUAL), -1);
                        agentOptions.put(optionKeyVal[0], optionKeyVal[1]);
                        // add mapped keys to special params
                        fenceSpecialParams.add(optionKeyVal[1]);
                    }
                }
                fenceOptionMapping.put(agent, agentOptions);
            } else {
                log.error(MAPPING_FORMAT_ERROR, agentOptionsStr);
                break;
            }
        }
    }

    /**
     * Caches the fence agents option types. Types are stored in the following format <!-- [key=type][,][key=type]*-->
     * for example : secure=bool,port=int,slot=int
     */
    private void cacheFenceAgentsOptionTypes() {
        String localfenceOptionTypes = Config.getValue(ConfigValues.VdsFenceOptionTypes);
        String[] types = localfenceOptionTypes.split(Pattern.quote(COMMA), -1);
        for (String entry : types) {
            String[] optionKeyVal = entry.split(Pattern.quote(EQUAL), -1);
            fenceOptionTypes.put(optionKeyVal[0], optionKeyVal[1]);
        }
    }

    /**
     * Gets the real key given the displayed key.
     *
     * @param agent
     *            The agent.
     * @param displayedKey
     *            The displayed key.
     */
    private String getRealKey(String agent, String displayedKey) {
        String result = "";
        if (StringUtils.isNotEmpty(agent) && StringUtils.isNotEmpty(displayedKey)) {
            if (fenceOptionMapping.containsKey(agent)) {
                Map<String, String> agentOptions = fenceOptionMapping.get(agent);
                result = agentOptions.getOrDefault(displayedKey, displayedKey);
            } else {
                log.error(AGENT_ERROR, agent);
            }
        }
        return result;
    }

    /**
     * Gets the displayed key given the real key.
     *
     * @param agent
     *            The agent.
     * @param realKey
     *            The real key.
     */
    private String getDisplayedKey(String agent, String realKey) {
        String result = "";
        if (StringUtils.isNotEmpty(agent) && StringUtils.isNotEmpty(realKey)) {
            if (fenceOptionMapping.containsKey(agent)) {
                Map<String, String> agentOptions = fenceOptionMapping.get(agent);
                if (agentOptions.containsValue(realKey)) {
                    for (Map.Entry<String, String> pair : agentOptions.entrySet()) {
                        if (StringUtils.equals(pair.getValue(), realKey)) {
                            result = pair.getKey();
                            break;
                        }
                    }
                } else {
                    // assume that a legal flag that not exists in mapping was
                    // used
                    result = realKey;
                }
            } else {
                log.error(AGENT_ERROR, agent);
            }
        }
        return result;
    }

    /**
     * Gets the type of the key.
     *
     * @param key
     *            The key.
     */
    private String getOptionType(String key) {
        String result = "";
        if (StringUtils.isNotEmpty(key) && fenceOptionTypes.containsKey(key)) {
            result = fenceOptionTypes.get(key);
        }
        return result;
    }

    /**
     * Translates the bool value to yes/no.
     *
     * @param value
     *            The value.
     */
    private static String translateBoolValue(String value) {
        String result;
        if (value.equalsIgnoreCase(TRUE_STRING) || value.equalsIgnoreCase(FALSE_STRING)) {
            if (Boolean.parseBoolean(value)) {
                result = YES;
            } else {
                result = NO;
            }
        } else {
            result = value;
        }
        return result;
    }

    /**
     * Inits this instance.
     */
    private void init() {
        initCache();
        cacheFenceAgentInstanceOptions();
    }

    /**
     * Cleans up.
     */
    private void cleanUp() {

        if (fenceAgentInstanceOptions != null && fenceOptionMapping != null
                && fenceOptionTypes != null) {
            fenceAgentInstanceOptions.clear();
            fenceOptionMapping.clear();
            fenceOptionTypes.clear();
            fenceSpecialParams.clear();
        }
        init();
    }

    /**
     * Inits the cache.
     */
    private void initCache() {
        if (fenceOptionMapping == null) {
            fenceAgentInstanceOptions = new HashMap<>();
            fenceOptionMapping = new HashMap<>();
            fenceOptionTypes = new HashMap<>();
            fenceSpecialParams = new HashSet<>();
            cacheFenceAgentsOptionMapping();
            cacheFenceAgentsOptionTypes();
        }
    }

    /**
     * Caches the fence agent instance options.
     */
    private void cacheFenceAgentInstanceOptions() {
        if (StringUtils.isNotEmpty(getAgent())
                && StringUtils.isNotEmpty(getFenceOptions())) {
            String[] options = getFenceOptions().split(Pattern.quote(COMMA), -1);
            fenceAgentInstanceOptions.clear();
            for (String option : options) {
                String[] optionKeyVal = option.split(Pattern.quote(EQUAL), -1);
                if (optionKeyVal.length == 1) {
                    add(getAgent(), optionKeyVal[0], "");
                } else if (optionKeyVal.length == 2) {
                    add(getAgent(), optionKeyVal[0], optionKeyVal[1]);
                } else if (optionKeyVal.length == 3) {
                    add(getAgent(), optionKeyVal[0], optionKeyVal[1] + EQUAL + optionKeyVal[2]);
                }
            }
        }
    }

    /**
     * handles agent mapping, get the real agent for a given agent name
     *
     * @param agent
     *            the agent name
     * @return string , the agent real name to be used
     */
    public static String getRealAgent(String agent) {
        String agentMapping = FenceConfigHelper.getFenceConfigurationValue(ConfigValues.FenceAgentMapping.name(), ConfigCommon.defaultConfigurationVersion);
        String realAgent = agent;
        // result has the format [<agent>=<real agent>[,]]*
        String[] settings = agentMapping.split(Pattern.quote(COMMA), -1);
        if (settings.length > 0) {
            for (String setting : settings) {
                // get the <agent>=<real agent> pair
                String[] pair = setting.split(Pattern.quote(EQUAL), -1);
                if (pair.length == 2) {
                    if (agent.equalsIgnoreCase(pair[0])) {
                        realAgent = pair[1];
                        break;
                    }
                }
            }
        }
        return realAgent;
    }

    /**
     * handles agent default options
     *
     * @return String the options after adding default agent parameters
     */
    public static String getDefaultAgentOptions(String agent, String fenceOptions, ArchitectureType architectureType) {
        String agentDefaultParams =  (architectureType != null && architectureType == ArchitectureType.ppc64)
                ?
                FenceConfigHelper.getFenceConfigurationValue(ConfigValues.FenceAgentDefaultParamsForPPC.name(), ConfigCommon.defaultConfigurationVersion)
                :
                FenceConfigHelper.getFenceConfigurationValue(ConfigValues.FenceAgentDefaultParams.name(), ConfigCommon.defaultConfigurationVersion);
        StringBuilder realOptions = new StringBuilder(fenceOptions);
        // result has the format [<agent>:param=value[,]...;]*
        String[] params = agentDefaultParams.split(Pattern.quote(SEMICOLON), -1);
        for (String agentOptionsStr : params) {
            String[] parts = agentOptionsStr.split(Pattern.quote(COLON), -1);
            if (parts.length == 2) {
                if (agent.equalsIgnoreCase(parts[0])) {
                    // check for empty options
                    if (StringUtils.isNotEmpty(parts[1])) {
                        String[] options = parts[1].split(Pattern.quote(COMMA), -1);
                        for (String option : options) {
                            String[] optionKeyVal = option.split(Pattern.quote(EQUAL), -1);
                            // if a value is set explicitly for a default param
                            // we respect that value and not use the default value
                            if (!fenceOptions.contains(optionKeyVal[0])) {
                                if (realOptions.length() > 0) {
                                    realOptions.append(COMMA);
                                }
                                realOptions.append(optionKeyVal[0]);
                                if (optionKeyVal.length == 2) {
                                    String val = (optionKeyVal[1] == null) ? "" : optionKeyVal[1];
                                    realOptions.append(EQUAL);
                                    realOptions.append(val);
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
        return realOptions.toString();
    }

    public String getAgent() {
        return fenceAgent;
    }

    public void setAgent(String value) {
        fenceAgent = value;
        cleanUp();
    }

    public String getFenceOptions() {
        return fenceOptions;
    }

    public void setFenceOptions(String value) {
        fenceOptions = value;
        cleanUp();
    }

    /**
     * Adds the specified key.
     *
     * @param key
     *            The key.
     * @param value
     *            The value.
     */
    public void add(String key, String value) {
        add(getAgent(), key, value);
    }

    /**
     * Adds the specified key.
     *
     * @param agent
     *            The agent.
     * @param key
     *            The key.
     * @param value
     *            The value.
     */
    public void add(String agent, String key, String value) {
        key = getRealKey(agent, key);
        fenceAgentInstanceOptions.put(key, value);
    }

    /**
     * Checks if the agent is supported on the version that was set in object constructor
     * @param agent
     *            The agent.
     * @return <c>true</c> if the specified agent is supported; otherwise, <c>false</c>.
     */
    public boolean isAgentSupported(String agent) {
        return fenceOptionMapping.containsKey(agent);
    }

    /**
     * Gets the specified key.
     *
     * @param key
     *            The key.
     * @return The key value, null if key is not exist
     */
    public Object get(String key) {
        final String BOOL = "bool";
        final String INT = "int";
        final String LONG = "long";
        final String DOUBLE = "double";
        Object result = null;
        if (StringUtils.isNotEmpty(key)) {
            String type = getOptionType(key);
            key = getRealKey(getAgent(), key);
            if (fenceAgentInstanceOptions != null
                    && fenceAgentInstanceOptions.containsKey(key)) {
                if (StringUtils.isNotEmpty(type)) {
                    // Convert to the suitable type according to metadata.
                    if (type.equalsIgnoreCase(BOOL)) {
                        result = Boolean.parseBoolean(fenceAgentInstanceOptions.get(key));
                    } else if (type.equalsIgnoreCase(INT)) {
                        Integer intVal = IntegerCompat.tryParse(fenceAgentInstanceOptions
                                .get(key));
                        if (intVal != null) {
                            result = intVal;
                        }
                    } else if (type.equalsIgnoreCase(LONG)) {
                        try {
                            result = Long.parseLong(fenceAgentInstanceOptions.get(key));
                        } catch (NumberFormatException ignore) {
                        }
                    } else if (type.equalsIgnoreCase(DOUBLE)) {
                        try {
                            result = Double.parseDouble(fenceAgentInstanceOptions.get(key));
                        } catch (NumberFormatException ignore) {
                        }
                    } else { // return as string
                        result = fenceAgentInstanceOptions.get(key);
                    }
                } else {
                    // return value as an object
                    result = fenceAgentInstanceOptions.get(key);
                }
            }
        }
        return result;
    }

    /**
     * Returns a <see cref="T:System.String"/> that represents the current <see cref="T:System.Object"/>.
     *
     * @return A <see cref="T:System.String"/> that represents the current <see cref="T:System.Object"/>.
     */

    @Override
    public String toString() {
        StringBuilder value = new StringBuilder();
        String delimiter = "";
        for (Map.Entry<String, String> pair : fenceAgentInstanceOptions.entrySet()) {
            value.append(delimiter)
                    .append(getDisplayedKey(getAgent(), pair.getKey()))
                    .append(pair.getValue().length() > 0 ? EQUAL + pair.getValue() : "");
            delimiter = COMMA;
        }
        return value.toString();
    }

    /**
     * Gets the internal representation of the options.
     */
    public String toInternalString() {
        StringBuilder value = new StringBuilder();
        String delimiter = "";
        for (Map.Entry<String, String> pair : fenceAgentInstanceOptions.entrySet()) {
            if (pair.getValue().trim().length() > 0) {
                value.append(delimiter).append(pair.getKey()).append(EQUAL).append(translateBoolValue(pair.getValue()));
                // special params should not be sent if value is empty
            } else if (!fenceSpecialParams.contains(pair.getKey())) {
                value.append(delimiter).append(pair.getKey());
            }
            delimiter = NEWLINE;
        }
        return value.toString();

    }

}
