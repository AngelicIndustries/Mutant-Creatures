package industries.angelic.mutantcreatures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.Test;

/**
 * Every name the game shows comes from the lang files: each registered entity has a name in every language, every
 * lang key used in code exists, and the languages have the same keys.
 */
public class LangFilesTest {

    private static final Path LANG = Paths.get("src/main/resources/assets/mutantcreatures/lang");
    private static final Path SOURCES = Paths.get("src/main/java");

    @Test
    public void everyRegisteredEntityIsNamedInEveryLanguage() throws IOException {
        String registration = new String(
            Files.readAllBytes(SOURCES.resolve("thehippomaster/MutantCreatures/MutantCreatures.java")),
            StandardCharsets.UTF_8);
        Matcher entity = Pattern.compile("registerModEntity\\(\\w+\\.class, \"([^\"]+)\"")
            .matcher(registration);
        Set<String> names = new TreeSet<>();
        while (entity.find()) names.add("entity.MutantCreatures." + entity.group(1) + ".name");
        assertTrue("no entities found", names.size() > 20);
        for (String language : languages()) {
            Set<String> keys = keys(language);
            for (String name : names) assertTrue(name + " missing in " + language, keys.contains(name));
        }
    }

    @Test
    public void everyGuiKeyUsedInCodeExists() throws IOException {
        Set<String> used = new TreeSet<>();
        Pattern key = Pattern.compile("\"(gui\\.mutantcreatures\\.[\\w.]+)\"");
        try (Stream<Path> files = Files.walk(SOURCES)) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                Matcher matcher = key.matcher(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
                while (matcher.find()) used.add(matcher.group(1));
            }
        }
        assertTrue("no gui keys found", !used.isEmpty());
        for (String language : languages()) {
            Set<String> keys = keys(language);
            for (String name : used) assertTrue(name + " missing in " + language, keys.contains(name));
        }
    }

    @Test
    public void languagesHaveTheSameKeys() throws IOException {
        Set<String> english = keys("en_US.lang");
        for (String language : languages()) assertEquals(language, english, keys(language));
    }

    private static Set<String> languages() throws IOException {
        Set<String> languages = new TreeSet<>();
        try (Stream<Path> files = Files.list(LANG)) {
            files.forEach(p -> languages.add(p.getFileName().toString()));
        }
        return languages;
    }

    private static Set<String> keys(String language) throws IOException {
        Set<String> keys = new TreeSet<>();
        for (String line : Files.readAllLines(LANG.resolve(language), StandardCharsets.UTF_8)) {
            int equals = line.indexOf('=');
            if (equals > 0 && !line.startsWith("#")) keys.add(line.substring(0, equals));
        }
        return keys;
    }
}
