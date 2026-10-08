package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Guards against inconsistencies between the parameters that are used in
 * stored procedures and the columns of the tables they write to, including
 * length/precision modifiers.
 *
 * The check compares the parameter declarations of every function in
 * the stored procedure sources against the live column types of the
 * DB, normalized the spelling so both sides can be compared as strings.
 *
 * Parameter declarations are parsed from the SQL sources rather
 * than read from the DB: although PostgreSQL accepts length
 * modifiers in functions, it does not retain them - it stores only
 * the base type OID, and format_type() therefore reports a bare character
 * varying for every such parameter. The only place the declared limit still
 * exists is the source file. Column types, in contrast, do keep their
 * types, so they can be read from the database.
 *
 * A parameter is only compared against the columns of tables the function
 * body writes to (INSERT/UPDATE/DELETE targets): an oversized or truncated
 * value can only cause damage when written, and including tables the body
 * merely reads caused parameters to be resolved against unrelated columns
 * with coincidentally matching names. Parameters whose names do not match a
 * column of a written table, or that are ambiguous across written tables,
 * are skipped - name-based resolution is a heuristic, not a contract. Skips
 * are counted by reason and reported with the results, so that a refactor
 * which silently unchecks parameters stays observable.
 */
public class DbTypeConsistencyTest extends BaseDaoTestCase<TagDao> {

    @Inject
    private JdbcTemplate jdbcTemplate;

    /**
     * Helper functions ({@code fn_db_*}) take parameters like {@code v_table}
     * or {@code v_column} that are not bound to table columns, so they are out
     * of scope for this check.
     */
    private static final String HELPER_FUNCTION_PREFIX = "fn_db_";

    /**
     * Tables a stored procedure body writes to, i.e. the targets of
     * INSERT INTO / UPDATE / DELETE FROM statements. Only write targets are
     * considered when resolving a parameter to a column: a value that is
     * too long/too narrow can only corrupt data when it is written, and
     * including tables that the body merely reads (e.g. a
     * {@code SELECT ... FROM cluster} lookup) causes parameters to be
     * resolved against unrelated columns with coincidentally matching
     * names.
     */
    private static final Pattern WRITE_TARGET_PATTERN = Pattern.compile(
            "\\b(?:INSERT\\s+INTO|UPDATE|DELETE\\s+FROM)\\s+([a-z][a-z0-9_]*)",
            Pattern.CASE_INSENSITIVE);

    /**
     * Strips SQL line comments ( -- to end of line) from a function
     * body so that commented-out statements cannot inject phantom table
     * references into the table scan. Only the comment itself is removed,
     * not the whole line: the text before the marker is live code, so e.g.
     * INSERT INTO t -- comment still registers as a write target.
     * Like the rest of the parser this is heuristic: a  -- inside
     * a string literal is treated as a comment start, which can at worst
     * trim the tail of that line's statement.
     */
    private static String stripLineComments(String definition) {
        return definition.replaceAll("(?m)--.*$", "");
    }

    /**
     * Resolves the directory path holding the stored procedure sources.
     */
    private static Path dbscriptsDir() {
        Path dir = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        while (dir != null && !Files.isDirectory(dir.resolve("packaging/dbscripts"))) {
            dir = dir.getParent();
        }
        if (dir == null) {
            throw new IllegalStateException(
                    "Cannot locate packaging/dbscripts above " + System.getProperty("user.dir"));
        }
        return dir.resolve("packaging/dbscripts");
    }

    /**
     * Matches a complete {@code CREATE OR REPLACE FUNCTION} declaration with a
     * dollar-quoted body: group 1 is the function name, group 2 the parameter
     * list, group 3 the dollar-quote tag and group 4 the function body. The
     * parameter-list pattern allows one level of nested parentheses so that
     * modifiers such as {@code numeric(18,9)} do not terminate the list.
     */
    private static final Pattern FUNCTION_PATTERN = Pattern.compile(
            "(?is)\\bcreate\\s+(?:or\\s+replace\\s+)?function\\s+"
            + "([a-z][a-z0-9_]*)\\s*"
            + "\\(([^)]*(?:\\([^)]*\\)[^)]*)*)\\)\\s*"
            + "(?:returns\\s+[^$]*?)?\\s*as\\s*"
            + "(\\$[a-z0-9_]*\\$)(.*?)\\3");

    /**
     * Matches a trailing length/precision modifier, e.g. the {@code (18,9)} of
     * {@code numeric(18,9)}.
     */
    private static final Pattern MODIFIER_AT_END = Pattern.compile("\\(([^)]*)\\)$");

    /**
     * Matches a single parameter declaration, e.g.
     * v_name character varying(128). Accepts the in/inout parameter
     * modes; out-only parameters are not caller-supplied and are left
     * unmatched on purpose. Leading whitespace is tolerated because the
     * parameter list is split on commas from a multi-line signature.
     * An optional DEFAULT clause is swallowed but not captured, so that
     * declarations like v_timezone VARCHAR(300) DEFAULT NULL are still
     * checked.
     */
    private static final Pattern PARAMETER_DECLARATION_PATTERN = Pattern.compile(
            "(?i)\\s*(?:in(?:out)?\\s+)?(v_[a-z0-9_]+)\\s+"
            + "([a-z][a-z0-9_]*(?:\\s+(?!default\\b)[a-z][a-z0-9_]*)*(?:\\([^)]*\\))?)"
            + "(?:\\s+default\\b.*)?"
            + "\\s*");

    /**
     * Normalizes the type spellings used in the SQL sources to the spellings
     * produced, which is how column types are reported
     * by the database.
     */
    private static final Map<String, String> BASE_TYPE_ALIASES = buildBaseTypeAliases();

    /**
     * Ordering within the integer family, used to tell a narrowing
     * (data-loss risk) from a widening (harmless) in the failure report.
     */
    private static final Map<String, Integer> INTEGER_WIDTH = Map.of(
            "smallint", 1,
            "integer", 2,
            "bigint", 3);

    @Test
    public void storedProcedureParameterTypesMatchColumnTypes(TestReporter testReporter) {
        Map<String, Map<String, String>> schema = loadColumnTypes();
        List<Mismatch> mismatches = new ArrayList<>();
        int checkedParameters = 0;
        int skippedUnknownColumn = 0;
        int skippedAmbiguous = 0;

        // fail fast with a specific message when the database is empty,
        // instead of vacuously passing or blaming the parser
        assertFalse(schema.isEmpty(),
                "No base tables found in the database - "
                + "is the schema applied (packaging/dbscripts/schema.sh -c apply)?");

        for (StoredProcedure storedProcedure : loadDeclaredProcedures()) {
            if (storedProcedure.name.startsWith(HELPER_FUNCTION_PREFIX)) {
                continue;
            }
            Set<String> tables = referencedTables(storedProcedure.definition, schema.keySet());
            for (Map.Entry<String, String> argument : storedProcedure.arguments.entrySet()) {
                ColumnResolution resolution =
                        resolveType(columnName(argument.getKey()), tables, schema);
                if (!resolution.isResolved()) {
                    // count skips by reason instead of dropping them
                    // silently, so coverage erosion (e.g. a refactor that
                    // leaves parameters unresolvable) is at least visible
                    if (resolution.status == ResolutionStatus.UNKNOWN_COLUMN) {
                        skippedUnknownColumn++;
                    } else {
                        skippedAmbiguous++;
                    }
                    continue;
                }
                checkedParameters++;
                String found = argument.getValue();
                if (!found.equals(resolution.ref.type)) {
                    mismatches.add(new Mismatch(storedProcedure.name,
                        argument.getKey(),
                        found,
                        resolution.ref));
                }
            }
        }
        // publish the coverage even when everything passes, so the ratio of
        // checked vs skipped parameters is observable on green runs too
        testReporter.publishEntry("parametersChecked", String.valueOf(checkedParameters));
        testReporter.publishEntry("skippedUnknownColumn", String.valueOf(skippedUnknownColumn));
        testReporter.publishEntry("skippedAmbiguous", String.valueOf(skippedAmbiguous));
        assertTrue(checkedParameters > 0, "No parameters were checked");
        assertTrue(mismatches.isEmpty(),
                render(mismatches, checkedParameters, skippedUnknownColumn, skippedAmbiguous));
    }

    /**
     * @return columns of base tables in the schema as
     *         table -> column -> full type with length/precision
     *         modifiers
     */
    private Map<String, Map<String, String>> loadColumnTypes() {
        String sql = "SELECT c.relname, a.attname, format_type(a.atttypid, a.atttypmod)"
                + " FROM pg_attribute a"
                + " JOIN pg_class c ON c.oid = a.attrelid"
                + " WHERE c.relnamespace = 'public'::regnamespace"
                + "   AND c.relkind = 'r'"
                + "   AND a.attnum > 0"
                + "   AND NOT a.attisdropped";
        Map<String, Map<String, String>> schema = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            schema.computeIfAbsent(rs.getString(1).toLowerCase(),
                    table -> new HashMap<>())
                    .put(rs.getString(2).toLowerCase(), rs.getString(3));
        });
        return schema;
    }

    /**
     * Parses the declared parameter types of every function in
     * stored procedures with their length/precision modifiers,
     * and resolves the tables each function body touches so that
     * only checkable parameters are kept.
     */
    private List<StoredProcedure> loadDeclaredProcedures() {
        Path dbscriptsDir = dbscriptsDir();
        List<StoredProcedure> storedProcedures = new ArrayList<>();
        try (Stream<Path> files = Files.list(dbscriptsDir)) {
            files.filter(p -> p.getFileName().toString().endsWith("_sp.sql"))
                    .sorted()
                    .forEach(file -> parseFile(file, storedProcedures));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + dbscriptsDir, e);
        }
        return storedProcedures;
    }

    private void parseFile(Path file, List<StoredProcedure> storedProcedures) {
        String content;
        try {
            content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
        Matcher functions = FUNCTION_PATTERN.matcher(content);
        while (functions.find()) {
            String name = functions.group(1);
            String body = functions.group(4);
            StoredProcedure storedProcedure =
                    new StoredProcedure(name, body);
            for (String declaration : splitParameterList(functions.group(2))) {
                Matcher parameter = PARAMETER_DECLARATION_PATTERN.matcher(declaration);
                if (parameter.matches()) {
                    storedProcedure.addArgument(
                            parameter.group(1),
                            normalizeDeclaredType(parameter.group(2)));
                }
            }
            if (!storedProcedure.arguments.isEmpty()) {
                storedProcedures.add(storedProcedure);
            }
        }
    }

    /**
     * Splits a parameter list on commas that are not inside parentheses, so
     * that modifiers like {@code numeric(18,9)} survive as one declaration.
     */
    private static List<String> splitParameterList(String parameterList) {
        List<String> declarations = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        for (char c : parameterList.toCharArray()) {
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == ',' && depth == 0) {
                declarations.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        if (current.length() > 0) {
            declarations.add(current.toString());
        }
        return declarations;
    }

    /**
     * Rewrites a declared parameter type into the exact spelling used
     * for the equivalent column type, so both sides of the comparison can
     * be compared as strings. Types without a known alias are passed
     * through lower-cased.
     */
    private static String normalizeDeclaredType(String declaredType) {
        String type = declaredType.trim().toLowerCase();
        String base = MODIFIER_AT_END.matcher(type).replaceFirst("").trim();
        String modifier = type.equals(base) ? "" : type.substring(base.length());
        String canonicalBase = BASE_TYPE_ALIASES.getOrDefault(base, base);
        return canonicalBase + modifier;
    }

    private static Map<String, String> buildBaseTypeAliases() {
        Map<String, String> aliases = new HashMap<>();
        // character family
        aliases.put("character varying", "character varying");
        aliases.put("varchar", "character varying");
        aliases.put("character", "character");
        aliases.put("char", "character");
        aliases.put("bpchar", "character");
        // integer family
        aliases.put("smallint", "smallint");
        aliases.put("int2", "smallint");
        aliases.put("integer", "integer");
        aliases.put("int", "integer");
        aliases.put("int4", "integer");
        aliases.put("bigint", "bigint");
        aliases.put("int8", "bigint");
        // numeric / float family
        aliases.put("numeric", "numeric");
        aliases.put("decimal", "numeric");
        aliases.put("real", "real");
        aliases.put("float4", "real");
        aliases.put("double precision", "double precision");
        aliases.put("float8", "double precision");
        return aliases;
    }

    /**
     * Strips the v_ parameter prefix.
     */
    private static String columnName(String parameterName) {
        String name = parameterName.toLowerCase();
        return name.startsWith("v_") ? name.substring(2) : name;
    }

    private static Set<String> referencedTables(String definition, Set<String> knownTables) {
        Set<String> tables = new HashSet<>();
        Matcher matcher = WRITE_TARGET_PATTERN.matcher(stripLineComments(definition));
        while (matcher.find()) {
            String table = matcher.group(1).toLowerCase();
            if (knownTables.contains(table)) {
                tables.add(table);
            }
        }
        return tables;
    }

    /**
     * Why a parameter could not be resolved against a write target.
     */
    private enum ResolutionStatus {
        /** The parameter matches a column of exactly one write target. */
        RESOLVED,
        /** The parameter name matches no column of any write target. */
        UNKNOWN_COLUMN,
        /** The parameter name matches columns of different types across write targets. */
        AMBIGUOUS
    }

    /**
     * The outcome of resolving a parameter name against the columns of the
     * tables a procedure body writes to. {@link #ref} is non-null only for
     * {@link ResolutionStatus#RESOLVED}; the other outcomes mark the
     * parameter as uncheckable, and the status is kept so skipped
     * parameters can be counted by reason instead of disappearing.
     */
    private static final class ColumnResolution {
        private static final ColumnResolution UNKNOWN_COLUMN =
                new ColumnResolution(ResolutionStatus.UNKNOWN_COLUMN, null);
        private static final ColumnResolution AMBIGUOUS =
                new ColumnResolution(ResolutionStatus.AMBIGUOUS, null);

        private final ResolutionStatus status;
        private final ColumnRef ref;

        private ColumnResolution(ResolutionStatus status, ColumnRef ref) {
            this.status = status;
            this.ref = ref;
        }

        private boolean isResolved() {
            return status == ResolutionStatus.RESOLVED;
        }
    }

    /**
     * Resolves a parameter name against the columns of the tables the
     * procedure body writes to. The result carries the skip reason when
     * the column is unknown or its type is ambiguous across the referenced
     * tables, in which case the parameter is not checkable.
     */
    private static ColumnResolution resolveType(String column,
            Set<String> tables,
            Map<String, Map<String, String>> schema) {
        ColumnRef result = null;
        for (String table : tables) {
            String type = schema.get(table).get(column);
            if (type == null) {
                continue;
            }
            if (result != null && !result.type.equals(type)) {
                return ColumnResolution.AMBIGUOUS;
            }
            if (result == null) {
                result = new ColumnRef(table, type);
            }
        }
        return result == null
                ? ColumnResolution.UNKNOWN_COLUMN
                : new ColumnResolution(ResolutionStatus.RESOLVED, result);
    }

    private static String classify(String found, String expected) {
        Integer foundWidth = INTEGER_WIDTH.get(found);
        Integer expectedWidth = INTEGER_WIDTH.get(expected);
        if (foundWidth != null && expectedWidth != null) {
            return foundWidth < expectedWidth ? "NARROWING" : "WIDENING";
        }
        // character types of the same base family: grade by the length
        // modifier, mirroring the integer grading above, e.g. a
        // character varying(30) parameter against a character
        // varying(300) column. Different families (character vs
        // character varying) or a missing modifier stay MISMATCH.
        String foundBase = characterBase(found);
        String expectedBase = characterBase(expected);
        if (foundBase != null && foundBase.equals(expectedBase)) {
            Integer foundLength = characterLength(found);
            Integer expectedLength = characterLength(expected);
            if (foundLength != null && expectedLength != null) {
                return foundLength < expectedLength ? "NARROWING" : "WIDENING";
            }
        }
        return "MISMATCH";
    }

    /**
     * The base of a character type ({@code character varying} or
     * {@code character}) without its length modifier, or null when the
     * type is not a character type.
     */
    private static String characterBase(String type) {
        String base = MODIFIER_AT_END.matcher(type).replaceFirst("").trim();
        return base.equals("character varying") || base.equals("character")
                ? base
                : null;
    }

    /**
     * The length of a character type with a modifier, or null when the type
     * carries no length (e.g. plain {@code character varying}, which is
     * unlimited).
     */
    private static Integer characterLength(String type) {
        Matcher modifier = MODIFIER_AT_END.matcher(type);
        if (!modifier.find()) {
            return null;
        }
        try {
            return Integer.valueOf(modifier.group(1).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String render(List<Mismatch> mismatches,
            int checkedParameters,
            int skippedUnknownColumn,
            int skippedAmbiguous) {
        StringBuilder message = new StringBuilder(
                "\nThe following stored procedure parameters do not match the column types in the database:\n");
        for (Mismatch mismatch : mismatches) {
            message.append(String.format("  %s: parameter %s is declared %s, but column %s.%s is %s (%s)%n",
                    mismatch.functionName,
                    mismatch.parameterName,
                    mismatch.found,
                    mismatch.expected.table,
                    columnName(mismatch.parameterName),
                    mismatch.expected.type,
                    classify(mismatch.found, mismatch.expected.type)));
        }
        message.append(String.format(
                "%nCoverage: %d parameters checked, %d skipped "
                + "(%d without a matching column, %d ambiguous across write targets)%n",
                checkedParameters,
                skippedUnknownColumn + skippedAmbiguous,
                skippedUnknownColumn,
                skippedAmbiguous));
        message.append("\nUpdate the parameter declarations in packaging/dbscripts/*_sp.sql")
                .append(" to match the column types.\n");
        return message.toString();
    }

    private static final class StoredProcedure {
        private final String name;
        private final String definition;
        private final Map<String, String> arguments = new LinkedHashMap<>();

        private StoredProcedure(String name, String definition) {
            this.name = name;
            this.definition = definition;
        }

        private void addArgument(String name, String type) {
            arguments.put(name, type);
        }
    }

    private static final class ColumnRef {
        private final String table;
        private final String type;

        private ColumnRef(String table, String type) {
            this.table = table;
            this.type = type;
        }
    }

    private static final class Mismatch {
        private final String functionName;
        private final String parameterName;
        private final String found;
        private final ColumnRef expected;

        private Mismatch(String functionName, String parameterName, String found, ColumnRef expected) {
            this.functionName = functionName;
            this.parameterName = parameterName;
            this.found = found;
            this.expected = expected;
        }
    }
}
