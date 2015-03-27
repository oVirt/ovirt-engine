package org.ovirt.engine.docs.utils.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.servlet.ServletUtils;

/**
 * This servlet serves the help tag JSON files for easy consumption by GWT. It can merge
 * multiple files to limit server requests and handle overrides (last file wins).
 */
public class ContextSensitiveHelpMappingServlet extends HttpServlet {
    private static final long serialVersionUID = -3938947636590096259L;
    private static final Logger log = Logger.getLogger(ContextSensitiveHelpMappingServlet.class);

    private static final String CONFIG_FILE = "configFile"; //$NON-NLS-1$

    private static ObjectMapper mapper = new ObjectMapper();

    // parse xxxxxx from /some/path/xxxxxx.json
    private static Pattern REQUEST_PATTERN = Pattern.compile(".*?(?<key>[^/]*)\\.json"); //$NON-NLS-1$
    private static String REQUEST_PATTERN_KEY_GROUP = "key"; //$NON-NLS-1$
    private static String HELPTAGS_PREFIX = "helptags."; //$NON-NLS-1$

    private Map<String, String> cachedJson = new HashMap<String, String>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        EngineLocalConfig engineLocalConfig = EngineLocalConfig.getInstance();
        String configFilePath = ServletUtils.getAsAbsoluteContext(
            getServletContext().getContextPath(),
            engineLocalConfig.expandString(
                    config.getInitParameter(CONFIG_FILE).replaceAll("%\\{", "\\${")) //$NON-NLS-1$ //$NON-NLS-2$
        );

        Set<Map.Entry<Object, Object>> properties = new HashSet<Map.Entry<Object, Object>>();
        File configFile = new File(configFilePath);
        if (configFile.exists() && configFile.canRead()) {
            Properties p = new Properties();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                p.load(fis);
                properties = p.entrySet();
            }
            catch (IOException e) {
                log.error("problem parsing Properties file: " + configFile.getAbsolutePath(), e); //$NON-NLS-1$
            }
        }

        // for every HELPTAGS_PREFIX line in the properties file, read all json files, merge them, and put them in cachedJson
        for (Map.Entry<Object, Object> property : properties) {
            String key = (String) property.getKey();
            if (key.startsWith(HELPTAGS_PREFIX)) {
                key = key.substring(HELPTAGS_PREFIX.length());
                String[] jsonFiles = ((String) property.getValue()).trim().split("\\s*,\\s*"); //$NON-NLS-1$

                // read the json files
                List<JsonNode> nodes = new ArrayList<JsonNode>();
                for (String jsonFile : jsonFiles) {

                    // the json files are relative to the configFile's directory
                    File file = new File(configFile.getParent(), jsonFile);
                    if (file.exists() && file.canRead()) {
                        try {
                            BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
                            nodes.add(mapper.readTree(reader));
                        }
                        catch (IOException e) {
                            log.error("problem parsing documentation mapping file: " + file.getAbsolutePath(), e); //$NON-NLS-1$
                        }
                    }
                }

                // merge the json files
                if (nodes.size() > 0) {
                    JsonNode destination = nodes.get(0);
                    for (int i = 1; i < nodes.size(); i++) {
                        destination = merge(destination, nodes.get(i));
                    }

                    this.cachedJson.put(key, destination.toString());
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Matcher m = REQUEST_PATTERN.matcher(request.getRequestURI());
        String content = "{}"; //$NON-NLS-1$
        if (m.matches() && cachedJson.containsKey(m.group(REQUEST_PATTERN_KEY_GROUP))) {
            content = cachedJson.get(m.group(REQUEST_PATTERN_KEY_GROUP));
        }
        response.setContentType("application/json"); //$NON-NLS-1$
        PrintStream printStream = new PrintStream(response.getOutputStream());
        printStream.print(content);
        printStream.flush();
    }

    protected static JsonNode merge(JsonNode destination, JsonNode source) {

        Iterator<String> fieldNames = source.getFieldNames();
        while (fieldNames.hasNext()) {

            String fieldName = fieldNames.next();
            JsonNode jsonNode = destination.get(fieldName);
            // if field is an embedded object, recurse
            if (jsonNode != null && jsonNode.isObject()) {
                merge(jsonNode, source.get(fieldName));
            }
            // else it's a plain field
            else if (destination instanceof ObjectNode) {
                // overwrite field
                JsonNode value = source.get(fieldName);
                ((ObjectNode) destination).put(fieldName, value);
            }
        }

        return destination;
    }
}
