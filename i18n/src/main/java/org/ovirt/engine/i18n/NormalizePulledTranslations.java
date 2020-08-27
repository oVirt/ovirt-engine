package org.ovirt.engine.i18n;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Document {
    String fileName;
    boolean addTranslationUpdateHeader;
    boolean escapeSingleQuotes;
    boolean removeEmptyKeys;
    boolean removeStaleLocaleKeys;
    boolean removeTrailingSpacesFromNonBlankValues;
    boolean sortKeys;

    String name;
    String baseName;
    String packageName;
    Pattern localizedFilePattern;

    Path baseSourcePath;
    Properties sourceDocument;

    public Document(Path baseSourcePath, String fileName, boolean addTranslationUpdateHeader, boolean escapeSingleQuotes,
                    boolean removeEmptyKeys, boolean removeStaleLocaleKeys, boolean removeTrailingSpacesFromNonBlankValues, boolean sortKeys)
    {
        this.baseSourcePath = baseSourcePath;
        this.fileName = fileName;
        this.addTranslationUpdateHeader = addTranslationUpdateHeader;
        this.escapeSingleQuotes = escapeSingleQuotes;
        this.removeEmptyKeys = removeEmptyKeys;
        this.removeStaleLocaleKeys = removeStaleLocaleKeys;
        this.removeTrailingSpacesFromNonBlankValues = removeTrailingSpacesFromNonBlankValues;
        this.sortKeys = sortKeys;

        name = fileName.substring(fileName.indexOf("src/main/resources/")+19, fileName.indexOf(".properties")).replace("/", ".");
        baseName = name.substring(name.lastIndexOf(".")+1);
        packageName = name.lastIndexOf(".") == -1 ? "" : name.substring(0, name.lastIndexOf("."));

        localizedFilePattern = Pattern.compile("/" + fileName.replace(".properties", "(_..(_..)?)\\.properties$"));
    }

    public String getName() {
        return name;
    }

    public String getBaseName() {
        return baseName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isAddTranslationUpdateHeader() {
        return addTranslationUpdateHeader;
    }

    public boolean isEscapeSingleQuotes() {
        return escapeSingleQuotes;
    }

    public boolean isRemoveEmptyKeys() {
        return removeEmptyKeys;
    }

    public boolean isRemoveStaleLocaleKeys() {
        return removeStaleLocaleKeys;
    }

    public boolean isSortKeys() {
        return sortKeys;
    }

    public boolean isRemoveTrailingSpacesFromNonBlankValues() {
        return removeTrailingSpacesFromNonBlankValues;
    }

    public Pattern getLocalizedFilePattern() {
        return localizedFilePattern;
    }

    public Properties getSourceDocument() throws IOException {
        if (sourceDocument == null) {
            Path sourcePath = baseSourcePath.resolve(fileName);
            sourceDocument = new Properties();
            try (InputStream r = Files.newInputStream(sourcePath)) {
                sourceDocument.load(r);
            }
        }
        return sourceDocument;
    }

    @Override
    public String toString() {
        return "Document{" +
                "baseName='" + baseName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", name='" + name + '\'' +
                ", fileName='" + fileName + '\'' +
                ", addTranslationUpdateHeader=" + addTranslationUpdateHeader +
                ", escapeSingleQuotes=" + escapeSingleQuotes +
                ", removeEmptyKeys=" + removeEmptyKeys +
                ", removeStaleLocaleKeys=" + removeStaleLocaleKeys +
                ", removeTrailingSpacesFromNonBlankValues=" + removeTrailingSpacesFromNonBlankValues +
                ", sortKeys=" + sortKeys +
                '}';
    }
}

public class NormalizePulledTranslations {
    private static final Logger log = LoggerFactory.getLogger(NormalizePulledTranslations.class);

    public static void main(String[] args) {
        String configYml = args[0];

        String baseSourcePathString = args.length >= 2 ? args[1] : "../";
        Path baseSourcePath = Paths.get(baseSourcePathString).toAbsolutePath().normalize();
        log.debug("base source path: {}", baseSourcePath);

        Path baseOutputPath = Paths.get("target/normalized").toAbsolutePath().normalize();
        if (!Files.exists(baseOutputPath)) {
            try {
                Files.createDirectories(baseOutputPath);
            } catch (IOException e) {
                log.error("Can't create output path", e);
                return;
            }
        }
        log.debug("base output path: {}", baseOutputPath);

        for (Document document : loadDocuments(configYml, baseSourcePath)) {
            log.debug("document: {}", document.getName());
            log.debug("\t{}", document);

            try {
                Path documentOutputDirectory = baseOutputPath.resolve(document.getFileName()).getParent();
                if (!Files.exists(documentOutputDirectory)) {
                    try {
                        Files.createDirectories(documentOutputDirectory);
                    } catch (IOException e) {
                        log.error("Can't create document locale specific output path", e);
                        return;
                    }
                }

                 List<Path> localeFiles =
                     Files.find(
                         baseSourcePath,
                         Integer.MAX_VALUE,
                         (path, attrs) -> {
                             String relativePath = path.normalize().toString().substring(baseSourcePath.toString().length());
                             return document.getLocalizedFilePattern().matcher(relativePath).matches();
                         }
                     )
                     .collect(Collectors.toList());
                 log.debug("\tfound {} locale files", localeFiles.size());

                 for (Path localeFile : localeFiles) {
                     String bundleLocale = localeFile.normalize().toString().replaceFirst("^.*?_(..(_..)?).properties$", "$1");
                     log.debug("\t\t{},", bundleLocale);
                     log.debug("\t\t\tfile: {}", localeFile);

                     Path tempNormalized = normalizeLocaleFile(document, bundleLocale, localeFile);

                     Path normalizedFile = documentOutputDirectory.resolve(localeFile.getFileName());
                     Files.copy(tempNormalized, normalizedFile, StandardCopyOption.REPLACE_EXISTING);
                     log.debug("\t\t\tnormalized file: {}", normalizedFile);
                 }
             } catch (IOException e) {
                 log.error("Failed attempting to normalize translations for document: {}", document, e);
             }
        }
    }

    static boolean getConfigKey(Map<String, Object>  d, Map<String, Boolean> defaults, String key) {
        return d.containsKey(key) ? (Boolean)d.get(key) : defaults.get(key);
    }

    static List<Document> loadDocuments(String pathToConfigYaml, Path baseSourcePath) {
        Yaml yaml = new Yaml();

        Map<String, Object> config = null;
        Map<String, Boolean> defaults = null;

        try (InputStream r = Files.newInputStream(Path.of(pathToConfigYaml))) {
          config = (Map<String, Object>)yaml.load(r);
          defaults = config.containsKey("defaults") ? (Map<String, Boolean>)config.get("defaults") : new TreeMap<>();
        } catch (IOException e) {
            log.error("Couldn't read the config yaml file", e);
        }
        log.debug("config file: {}", pathToConfigYaml);

        List<Document> documents = new ArrayList<>();
        for (Map<String, Object> document : (List<Map<String, Object>>)config.get("documents")) {
            String fileName = (String) document.get("file");

            boolean addTranslationUpdateHeader = getConfigKey(document, defaults, "addTranslationUpdateHeader");
            boolean escapeSingleQuotes = getConfigKey(document, defaults, "escapeSingleQuotes");
            boolean removeEmptyKeys = getConfigKey(document, defaults, "removeEmptyKeys");
            boolean removeStaleLocaleKeys = getConfigKey(document, defaults, "removeStaleLocaleKeys");
            boolean removeTrailingSpacesFromNonBlankValues = getConfigKey(document, defaults, "removeTrailingSpacesFromNonBlankValues");
            boolean sortKeys = getConfigKey(document, defaults, "sortKeys");

            documents.add(
                new Document(baseSourcePath, fileName,
                    addTranslationUpdateHeader, escapeSingleQuotes,
                    removeEmptyKeys, removeStaleLocaleKeys, removeTrailingSpacesFromNonBlankValues,
                    sortKeys)
            );
        }

        return documents;
    }

    static Path normalizeLocaleFile(Document document, String locale, Path localeFile) throws IOException {
        // Load the properties, stripping any comments, optionally sorting the keys
        Properties p = document.isSortKeys() ? new OrderedProperties() : new Properties();
        try (InputStream r = Files.newInputStream(localeFile)) {
            p.load(r);
        }

        // remove empty keys
        if (document.isRemoveEmptyKeys()) {
            p.stringPropertyNames().forEach(key -> {
                String value = p.getProperty(key);
                if (StringUtils.isEmpty(value)) {
                    p.remove(key);
                    log.debug("\t\t\t ** removed empty locale key: {}", key);
                }
            });
        }

        // remove keys that don't exist in the current English source document
        if (document.isRemoveStaleLocaleKeys()) {
            Properties source = document.getSourceDocument();
            p.stringPropertyNames().forEach(key -> {
                if (source.getProperty(key) == null) {
                    p.remove(key);
                    log.debug("\t\t\t ** removed stale locale key: {}", key);
                }
            });
        }

        // right trim whitespace from values that have more than just whitespace
        if (document.isRemoveTrailingSpacesFromNonBlankValues()) {
            p.stringPropertyNames().forEach(key -> {
                String value = p.getProperty(key);
                if (StringUtils.isNotBlank(value)) {
                    String value2 = value.replaceAll(" +$", "");
                    if (!value.equals(value2)) {
                        p.setProperty(key, value2);
                        log.debug("\t\t\t ** removed trailing spaces from key: {}", key);
                    }
                }
            });
        }

        // escape single quotes in message format strings, leaving already escaped single quotes in place
        if (document.isEscapeSingleQuotes()) {
            Pattern singleQuote = Pattern.compile("(?<sq>'+)");
            p.stringPropertyNames().forEach(key -> {
                String v1 = p.getProperty(key);

                Matcher m = singleQuote.matcher(v1);
                StringBuffer v2sb = new StringBuffer();
                while (m.find()) {
                    String sq = m.group("sq");
                    if (sq.length() % 2 == 0) {
                        m.appendReplacement(v2sb, m.group(0));
                    } else {
                        m.appendReplacement(v2sb, "'".repeat(sq.length()+1));
                    }
                }
                m.appendTail(v2sb);
                String v2 = v2sb.toString();

                if (!v1.equals(v2)) {
                    p.setProperty(key, v2);
                    log.debug("\t\t\t ** escaped {}: {} -> {}", key, v1, v2);
                }
            });
        }

        // Save the now normalized properties to a temporary file
        Path tempFile = Files.createTempFile(document.getBaseName() + "_" + locale, "properties");
        try (OutputStream w = Files.newOutputStream(tempFile)) {
            if (document.isAddTranslationUpdateHeader()) {
                p.store(w, "\n Bundle name: " + document.getName() + "\n Locale: " + locale + "\n");
            } else {
                p.store(w, null);
            }
        }
        return tempFile;
    }

}
