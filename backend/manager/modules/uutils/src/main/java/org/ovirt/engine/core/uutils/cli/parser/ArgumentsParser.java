package org.ovirt.engine.core.uutils.cli.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * This class parses properties file where user declare structure of command line arguments.
 * Every argument can declare following attributes:
 * <ul>
 *  <li>name - Name of the argument, <b>Must be declared by developer</b></li>
 *  <li>help - Help to be printed to argument, when user request help to be printed (default: empty string)</li>
 *  <li>mandatory - true/false declares if argument have to be specified or not (default: false)</li>
 *  <li>type - one of:
 *  <ul>
 *    <li>required_argument - argument requires value</li>
 *    <li>optional_argument - argument could have value</li>
 *    <li>no_argument - argument doesn't have value (default)</li>
*   </ul>
 *  </li>
 *  <li>convert - Name of the class that arguments value should be converted to (default: java.lang.String)</li>
 *  <li>matcher - Regular expression which value of argument need to fulfill. (default: .*)</li>
 *  <li>metavar - Used in only usage print. Give clue to user what expect as a value. (default: STRING)</li>
 *  <li>multivalue - true/false declares if value is list of values or single value (default: false)</li>
 *  <li>default - If argument name is not found in list of user arguments.
 *            It will add this argument with value of this attribute. (default: null)</li>
 *  <li>value - Value to be set to argument if argument value is not required (default: null)</li>
 * </ul>
 *
 * <p>
 * To be able to ensure program can handle more usages for specific actions we need to specify prefixes for arguments.
 * That means that final declaration of argument in properties file will look like:
 *
 *   <p><i>&lt;prefix&gt;.arg.&lt;argument_name&gt;.&lt;attribute&gt; = value</i></p>
 *
 * ArgumentsParser.parse() will parse only arguments with specified prefix. If parser hit value which don't start with "--",
 * it will exit parsing, validate current arguments. In attribute {@code argMap} you can find parsed arguments.
 * In attribute @{code errors} you can find all errors which were found during parsing. The rest of arguments, which parser
 * didn't parse stay in list, which is paramater of parse() method.
 * </p>
 *
 * <p>
 * If there appears any error while parsing arguments(like, required value missing, mandatory not specified, etc).
 * All these errors will be stored in {@code errors} attribute.
 * </p>
 *
 * <p>
 * Properties file also specify three basic variables that stores usage print, Variables names are as follows:
 * <ul>
 *   <li><i>help.usage</i> - Declares usage of program/module (default: empty)</li>
 *   <li><i>help.header</i> - Declares header of program/module, usually used for description of program/module (default: empty)</li>
 *   <li><i>help.footer</i> - Declares footer of program/module, usually used for additional info for consumer of program/module (default: empty)</li>
 * </ul>
 * </p>
 *
 * The usage is printed in following structure:
 * <pre>
 * """
 *  $help.usage
 *  $help.header
 *
 *  Options:
 *    --&lt;prefix&gt;arg.argument1.name
 *      &lt;prefix&gt;arg.argument1.help
 *
 *    --&lt;prefix&gt;arg.argument2.name
 *      &lt;prefix&gt;arg.argument2.help
 *
 *  $help.footer
 * """
 * </pre>
 * Example:<br/>
 *  We have a program which support two actions - [add, remove]. Both actions accept different arguments and program too.
 *
 * <pre>
 *   ./out [--file] add --message=X [--index]
 *   ./out [--file] remove [--index]
 * </pre>
 *  The properties file of this program can look like:
 * <pre>
 *   module.arg.file.name = file
 *   module.arg.file.help = File where messages are stored
 *   module.arg.file.default = /tmp/X
 *   add.arg.message.name = message
 *   add.arg.message.help = Message to be stored
 *   add.arg.message.mandatory = true
 *   add.arg.message.type = required_argument
 *   add.arg.index.name = index
 *   add.arg.index.help = Index where message should be inserted
 *   add.arg.index.value = 0
 *   add.arg.index.default = 0
 *   add.arg.index.type = optional_argument
 *   remove.arg.index.name = index
 *   remove.arg.index.help = Index where message should be inserted
 *   remove.arg.index.value = 0
 *   remove.arg.index.default = 0
 *   remove.arg.index.type = optional_argument
 *
 *  ArgumentsParser parser = new ArgumentsParser(inputStream)
 *  Map&lt;String, Object&gt; moduleArgMap = parser.parse(args)
 *  print moduleArgMap.get("file")
 *
 *  Listp&lt;Object&gt; others = moduleArgMap.get(PARAMETERS_KEY_OTHERS)
 *  print others.remove(0) // action/remove
 *
 *  Mapp&lt;String, Objectp&gt; actionArgMap = parser.parse(others)
 *  print actionArgMap.get("index")
 * </pre>
 */
public class ArgumentsParser {
    /**
     * Prefix which every argument should use to be considered argument.
     */
    private static final String LONG_PREFIX = "--";

    /**
     * Stores default values of every argument, if user don't override it default from this file will be used.
     */
    private static Properties defaultProperties;
    static {
        try (InputStream is = ArgumentsParser.class.getResourceAsStream("defaults.properties")) {
            defaultProperties = loadProperties(is);
        } catch (IOException e) {
            defaultProperties = null;
        }
    }

    /**
     * Stores user defined arguments in properties file.
     */
    private Properties properties;

    /**
     * The prefix which should be used for parsing of properties file.
     */
    private String prefix;

    /**
     * Map which stores parsed converted arguments.
     */
    private Map<String, Argument> arguments = new HashMap<>();

    /**
     * Set of mandatory arguments to check if user specified all of them.
     */
    private Set<String> mandatory = new HashSet<>();

    /**
     * Map of correctly parsed and converted arguments
     */
    private Map<String, Object> parsedArgs;

    /**
     * List of errors which was found during pasring
     */
    private List<Throwable> errors;

    /**
     * Map of predefined substitutions which should be replaced in usage.
     */
    private Map<String, String> substitutions = new HashMap<>();

    /**
     * Inititilize ArgumentsParser attributes. Parser properties file and create argument map of it.
     *
     * @param properties properties file with defined arguments
     * @param prefix only arguments with this prefix will be parsed
     */
    public ArgumentsParser(Properties properties, String prefix) {
        this.properties = properties;
        this.prefix = prefix;
        parseProperties();
    }

    /**
     * Inititilize ArgumentsParser attributes. Parse properties file and create argument map of it.
     *
     * @param resource Input resource with defined arguments
     * @param prefix only arguments with this prefix will be parsed
     */
    public ArgumentsParser(InputStream resource, String prefix) {
        this(loadProperties(resource), prefix);
    }

    /**
     * Return list of erros
     *
     * @return list of erros
     */
    public List<Throwable> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Return map of validated and parsed arguments
     *
     * @return Map of validated and parsed arguments
     */
    public Map<String, Object> getParsedArgs() {
        return Collections.unmodifiableMap(parsedArgs);
    }

    /**
     * Parse list of arguments based on definition declared in {@link #properties} file with {@link #prefix}.
     * Please note that param {@code args} will be modified and after method finished it will contain rest
     * of arguments(all arguments which follow argument without '--' prefix).
     *
     * @param args list of command line arguments
     * @return true if parsing has no errors, false otherwise
     */
    public boolean parse(List<String> args) {
        parsedArgs = new HashMap<>();
        errors = new ArrayList<>();

        while(!args.isEmpty()) {
            String arg = args.get(0);
            if(!arg.startsWith(LONG_PREFIX)) {
                break;
            }
            arg = args.remove(0);
            if(arg.equals(LONG_PREFIX)) {
                break;
            }

            String[] argVal = parseArgument(arg.substring(2));
            String key = argVal[0];
            String value = argVal[1];

            Argument argument = arguments.get(key);
            if(argument == null) {
                errors.add(
                    new IllegalArgumentException(String.format("Invalid argument '%1$s'", arg))
                );
            } else {
                if (
                    value == null &&
                    (
                        argument.getType() == Argument.Type.OPTIONAL_ARGUMENT ||
                        argument.getType() == Argument.Type.REQUIRED_ARGUMENT
                    )
                ) {
                    if(args.size() > 0) {
                        value = args.get(0);
                        if (value.startsWith(LONG_PREFIX)) {
                            value = null;
                        } else {
                            args.remove(0);
                        }
                    }
                }
                if (argument.getType() == Argument.Type.REQUIRED_ARGUMENT && value == null) {
                    errors.add(
                        new IllegalArgumentException(
                            String.format("Value is required, but missing for argument '%1$s'", key)
                        )
                    );
                }
                if (value == null) {
                    value = argument.getValue();
                }
                Object convertedValue = null;
                if (value != null) {
                    Matcher m = argument.getMatcher().matcher(value);
                    if (!m.matches()) {
                        errors.add(
                            new IllegalArgumentException(
                                String.format(
                                    "Pattern for argument '%1$s' does not match, pattern is '%2$s', value is '%3$s'", key, m.pattern(), value
                                )
                            )
                        );
                    }
                    convertedValue = StringValueConverter.getObjectValueByString(argument.getValueType(), value);
                }
                putValue(parsedArgs, argument, convertedValue);
            }
        }
        fillDefaults(parsedArgs);

        List<String> mandatoryCopy = new ArrayList<>(mandatory);
        mandatoryCopy.removeAll(parsedArgs.keySet());
        if(!mandatoryCopy.isEmpty()) {
            errors.add(
                new IllegalArgumentException(
                    String.format("Argument(s) '%1$s' required", StringUtils.join(mandatoryCopy, ", "))
                )
            );
        }

        return errors.isEmpty();
    }

    /**
     * Print usage of all arguments with {@link #prefix}. In this format:
     *
     * $prefix.help.usage
     * $prefix.help.header
     *
     * Options:
     *   --$prefix.arg.argumentX.name=[$prefix.arg.argumentX.metavar]
     *     $prefix.arg.argumentX.help
     *
     *   --$prefix.arg.argumentY.name=[$prefix.arg.argumentY.metavar]
     *     $prefix.arg.argumentY.help
     *
     * $prefix.help.footer
     *
     * @return formatted string with usage
     */
    public String getUsage() {
        StringBuilder help = new StringBuilder(String.format("Options:%n"));

        for(String arg : getPrefixArguments()) {
            Argument argument = this.arguments.get(arg);
            help.append(
                    String.format(
                            "  --%s%n",
                            arg + (argument.getType() != Argument.Type.NO_ARGUMENT ? "=[" + argument.getMetavar() + "]" : "")
                    )
            );
            for (
                String s : argument.getHelp().replace(
                    "@CLI_PRM_DEFAULT@",
                    StringUtils.defaultString(argument.getDefaultValue())
                ).replace(
                    "@CLI_PRM_PATTERN@",
                    argument.getMatcher().pattern()
                ).split("\n")
            ) {
                help.append(String.format("    %s%n", s));
            }
            help.append(String.format("%n"));
        }
        return doSubstitutions(
                String.format("%1$s%n%2$s%n%n%3$s%4$s%n",
                        properties.getProperty(
                                prefix + ".help.usage",
                                (String) defaultProperties.get("help.usage")
                        ),
                        properties.getProperty(
                                prefix + ".help.header",
                                (String) defaultProperties.get("help.header")
                        ),
                        help.toString(),
                        properties.getProperty(
                                prefix + ".help.footer",
                                (String) defaultProperties.get("help.footer")
                        )
                )
        );
    }

    /**
     * Return all registered substitutions
     * @return registered substitutions
     */
    public Map<String, String> getSubstitutions() {
        return substitutions;
    }

    /**
     * Go through all arguments which has default value. If such argument is not in {@code argMap} put it there.
     *
     * @param argMap map of converted command line arguments
     */
    private void fillDefaults(Map<String, Object> argMap) {
        for(Argument arg : arguments.values()) {
            if (!argMap.containsKey(arg.getName()) && arg.getDefaultValue() != null) {
                putValue(
                    argMap,
                    arg,
                    StringValueConverter.getObjectValueByString(
                        arg.getValueType(),
                        doSubstitutions(arg.getDefaultValue())
                    )
                );
            }
        }
    }

    /**
     * Put {@code value} into {@code argMap}. If {@code arg} is multivalue put a List with {@code value}.
     *
     * @param argMap map of converted command line arguments
     * @param arg argument to be put in map
     * @param value value of argument
     */
    private void putValue(Map<String, Object> argMap, Argument arg, Object value) {
        if (!arg.isMultivalue()) {
            argMap.put(arg.getName(), value);
        } else {
            List<? super Object> c = (List)argMap.get(arg.getName());
            if (c == null) {
                c = new ArrayList<>();
                argMap.put(arg.getName(), c);
            }
            c.add(value);
        }
    }

    /**
     * Substitute entries in string.
     */
    private String doSubstitutions(String s) {
        if (s != null) {
            for (Map.Entry<String, String> substitution : substitutions.entrySet()) {
                s = s.replaceAll(substitution.getKey(), substitution.getValue());
            }
        }
        return s;
    }

    /**
     * Map properties file declaration of arguemnts to {@see org.ovirt.engine.core.uutils.cli.parser.ParserArgument} class.
     * All those mapping classes are stored in map {@link #arguments}. All arguments declared as mandatory are stored
     * in set {@link #mandatory}, so later we can check if all mandatory arguments where specified by user.
     */
    private void parseProperties() {
        for(String arg : getPrefixArguments()) {
            Argument argument = new Argument();
            argument.setName(
                getArgAttrValue(arg, "name")
            );
            if (argument.getName() == null) {
                throw new IllegalArgumentException(
                    String.format("Invalid configuration. Parameter '%1$s' has no name", arg)
                );
            }
            argument.setMetavar(
                getArgAttrValue(arg, "metavar")
            );
            argument.setHelp(
                getArgAttrValue(arg, "help")
            );
            argument.setDefaultValue(
                getArgAttrValue(arg, "default")
            );
            argument.setValue(
                getArgAttrValue(arg, "value")
            );
            argument.setMultivalue(
                Boolean.parseBoolean(
                    getArgAttrValue(arg, "multivalue")
                )
            );
            argument.setMandatory(
                Boolean.parseBoolean(
                    getArgAttrValue(arg, "mandatory")
                )
            );
            argument.setType(
                Argument.Type.valueOfIgnoreCase(
                    getArgAttrValue(arg, "type")
                )
            );
            argument.setMatcher(
                Pattern.compile(
                    getArgAttrValue(arg, "matcher")
                )
            );
            try {
                argument.setValueType(
                    Class.forName(
                        getArgAttrValue(arg, "valuetype"),
                        true,
                        Thread.currentThread().getContextClassLoader()
                    )
                );
            } catch (ClassNotFoundException ex) {
                throw new IllegalArgumentException(
                    String.format("Can't convert argument: '%1$s'. Please check valuetype in properties file.", arg)
                );
            }

            if (argument.isMandatory()) {
                mandatory.add(argument.getName());
            }

            arguments.put(argument.getName(), argument);
        }
    }

    /**
     * Return set of argument from properties file names which starts with {@link #prefix}
     *
     * @return set of argument from properties file names which starts with {@link #prefix}
     */
    private Set<String> getPrefixArguments() {
        Set<String> args = new TreeSet<>();

        for (String argName : this.properties.stringPropertyNames()) {
            String[] param = argName.split("\\.");
            if((param.length > 1 && !param[1].equals("arg")) || !param[0].equals(this.prefix)) {
                continue;
            }
            if (param.length < 4) {
                throw new IllegalArgumentException(
                    String.format("Invalid configuration. Invalid structure for parameter'%1$s'", argName)
                );
            }
            args.add(param[2]);
        }

        return args;
    }

    private String[] parseArgument(String arg) {
        String [] splitArg = arg.split("=", 2);
        if(splitArg.length < 2) {
            return new String[] {splitArg[0],  null};
        }

        return splitArg;
    }

    private String getArgAttrValue(String argument, String key) {
        return properties.getProperty(
            prefix + ".arg." + argument + "." + key,
            defaultProperties.getProperty("arg." + key)
        );
    }

    private static Properties loadProperties(InputStream resource) {
        try (
            Reader is = new InputStreamReader(resource, StandardCharsets.UTF_8);
        ) {
            Properties prop = new Properties();
            prop.load(is);
            return prop;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
