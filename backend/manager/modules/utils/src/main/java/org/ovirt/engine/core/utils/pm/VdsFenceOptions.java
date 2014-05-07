package org.ovirt.engine.core.utils.pm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    private HashMap<String, HashMap<String, String>> fenceOptionMapping;
    private static HashMap<String, String> fenceOptionTypes;

    private String fenceAgent = "";
    private String fenceOptions;
    private static HashMap<String, String> fenceAgentInstanceOptions;
    private static HashSet<String> fenceSpecialParams;
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
        InitCache();
        Init();
    }

    public HashMap<String, HashMap<String, String>> getFenceOptionMappingMap() {
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
                HashMap<String, String> agentOptions = new HashMap<String, String>();
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
        String localfenceOptionTypes = Config.<String> getValue(ConfigValues.VdsFenceOptionTypes);
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
     * @return
     */
    private String GetRealKey(String agent, String displayedKey) {
        String result = "";
        if (StringUtils.isNotEmpty(agent) && StringUtils.isNotEmpty(displayedKey)) {
            if (fenceOptionMapping.containsKey(agent)) {
                HashMap<String, String> agentOptions = fenceOptionMapping.get(agent);
                result = agentOptions.containsKey(displayedKey) ? agentOptions.get(displayedKey)
                        : displayedKey;
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
     * @return
     */
    private String GetDisplayedKey(String agent, String realKey) {
        String result = "";
        if (StringUtils.isNotEmpty(agent) && StringUtils.isNotEmpty(realKey)) {
            if (fenceOptionMapping.containsKey(agent)) {
                HashMap<String, String> agentOptions = fenceOptionMapping.get(agent);
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
     * @return
     */
    private String GetOptionType(String key) {
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
     * @return
     */
    private static String TranslateBoolValue(String value) {
        String result;
        if (value.equalsIgnoreCase(TRUE_STRING) || value.equalsIgnoreCase(FALSE_STRING)) {
            if (Boolean.parseBoolean(value)) {
                result = YES;
            }
            else {
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
    private void Init() {
        InitCache();
        cacheFenceAgentInstanceOptions();
    }

    /**
     * Cleans up.
     */
    private void CleanUp() {

        if (fenceAgentInstanceOptions != null && fenceOptionMapping != null
                && fenceOptionTypes != null) {
            fenceAgentInstanceOptions.clear();
            fenceOptionMapping.clear();
            fenceOptionTypes.clear();
            fenceSpecialParams.clear();
        }
        Init();
    }

    /**
     * Inits the cache.
     */
    private void InitCache() {
        if (fenceOptionMapping == null) {
            fenceAgentInstanceOptions = new HashMap<String, String>();
            fenceOptionMapping = new HashMap<String, HashMap<String, String>>();
            fenceOptionTypes = new HashMap<String, String>();
            fenceSpecialParams = new HashSet<String>();
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
                } else {
                    add(getAgent(), optionKeyVal[0], optionKeyVal[1]);
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
     * handles agent power wait parameter mapping
     * @param agent
     * @param powerWait
     * @return
     */
    public static String getAgentPowerWaitParam(String agent, String powerWait) {
        String param = null;
        // result has the format [<agent>=<power wait param name>[,]]*
        String[] settings = powerWait.split(Pattern.quote(COMMA), -1);
        if (settings.length > 0) {
            for (String setting : settings) {
                String[] pair = setting.split(Pattern.quote(EQUAL), -1);
                if (pair.length == 2) {
                    if (agent.equalsIgnoreCase(pair[0])) {
                        param = pair[1];
                        break;
                    }
                }
            }
        }
        return param;
    }

    /**
     * handles agent default options
     *
     * @param agent
     * @param fenceOptions
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
        CleanUp();
    }

    public String getFenceOptions() {
        return fenceOptions;
    }

    public void setFenceOptions(String value) {
        fenceOptions = value;
        CleanUp();
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
        key = GetRealKey(agent, key);
        fenceAgentInstanceOptions.put(key, value);
    }

    /**
     * Determines whether the specified current agent key is supported .
     *
     * @param key
     *            The key.
     * @return <c>true</c> if the specified key is supported; otherwise, <c>false</c>.
     */

    public boolean IsSupported(String key) {

        return IsSupported(getAgent(), key);
    }

    /**
     * Determines whether the specified agent key is supported.
     *
     * @param agent
     *            The agent.
     * @param key
     *            The key.
     * @return <c>true</c> if the specified agent is supported; otherwise, <c>false</c>.
     */

    public boolean IsSupported(String agent, String key) {
        boolean result = false;
        if (StringUtils.isNotEmpty(agent) && StringUtils.isNotEmpty(key)
                && fenceOptionMapping.containsKey(agent)) {
            HashMap<String, String> agentOptions = fenceOptionMapping.get(agent);
            result = (agentOptions == null) ? false : agentOptions.containsKey(key);
        } else {
            log.error(AGENT_ERROR, agent);
        }

        return result;
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
     * Gets the current agent supported options.
     *
     * @return
     */

    public ArrayList<String> GetSupportedOptions() {
        return GetSupportedOptions(getAgent());

    }

    /**
     * Gets the agent supported options.
     *
     * @param agent
     *            The agent.
     * @return
     */

    public ArrayList<String> GetSupportedOptions(String agent) {
        ArrayList<String> agentOptions = new ArrayList<String>();
        if (fenceOptionMapping.containsKey(agent)) {
            HashMap<String, String> options = fenceOptionMapping.get(agent);
            for (Map.Entry<String, String> pair : options.entrySet()) {
                agentOptions.add(pair.getKey());
            }
        } else {
            log.error(AGENT_ERROR, agent);
        }
        return agentOptions;

    }

    /**
     * Gets the specified key.
     *
     * @param key
     *            The key.
     * @return The key value, null if key is not exist
     */

    public Object Get(String key) {
        final String BOOL = "bool";
        final String INT = "int";
        final String LONG = "long";
        final String DOUBLE = "double";
        Object result = null;
        if (StringUtils.isNotEmpty(key)) {
            String type = GetOptionType(key);
            key = GetRealKey(getAgent(), key);
            if (fenceAgentInstanceOptions != null
                    && fenceAgentInstanceOptions.containsKey(key)) {
                if (StringUtils.isNotEmpty(type)) {
                    // Convert to the suitable type according to metadata.
                    if (type.equalsIgnoreCase(BOOL)) {
                        result = Boolean.parseBoolean(fenceAgentInstanceOptions.get(key));
                    }
                    else if (type.equalsIgnoreCase(INT)) {
                        Integer intVal = IntegerCompat.tryParse(fenceAgentInstanceOptions
                                .get(key));
                        if (intVal != null) {
                            result = intVal;
                        }
                    }
                    else if (type.equalsIgnoreCase(LONG)) {
                        try {
                            result = Long.parseLong(fenceAgentInstanceOptions.get(key));
                        } catch (NumberFormatException e) {
                        }
                    }
                    else if (type.equalsIgnoreCase(DOUBLE)) {
                        try {
                            result = Double.parseDouble(fenceAgentInstanceOptions.get(key));
                        } catch (NumberFormatException e) {
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
                    .append(GetDisplayedKey(getAgent(), pair.getKey()))
                    .append(pair.getValue().length() > 0 ? EQUAL + pair.getValue() : "");
            delimiter = COMMA;
        }
        return value.toString();
    }

    /**
     * Gets the unsupported options string.
     *
     * @return
     */
    public String ToUnsupportedOptionsString() {
        String delimiter = "";
        StringBuilder value = new StringBuilder();
        for (Map.Entry<String, String> pair : fenceAgentInstanceOptions.entrySet()) {
            String displayedKey = GetDisplayedKey(getAgent(), pair.getKey());
            if (!IsSupported(displayedKey)) {
                value.append(delimiter)
                        .append(displayedKey)
                        .append((pair.getValue().length() > 0 ? EQUAL + pair.getValue() : ""));
                delimiter = COMMA;
            }
        }
        return value.toString();
    }

    /**
     * Gets the internal representation of the options.
     *
     * @return
     */

    public String ToInternalString() {
        StringBuilder value = new StringBuilder();
        String delimiter = "";
        for (Map.Entry<String, String> pair : fenceAgentInstanceOptions.entrySet()) {
            if (pair.getValue().trim().length() > 0) {
                value.append(delimiter).append(pair.getKey()).append(EQUAL).append(TranslateBoolValue(pair.getValue()));
                // special params should not be sent if value is empty
            } else if (!fenceSpecialParams.contains(pair.getKey())) {
                value.append(delimiter).append(pair.getKey());
            }
            delimiter = NEWLINE;
        }
        return value.toString();

    }

}
