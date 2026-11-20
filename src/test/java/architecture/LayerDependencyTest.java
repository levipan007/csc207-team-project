package architecture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the inward dependency rule, independently of any use-case implementation. */
class LayerDependencyTest {
    @Test
    void entitiesDoNotDependOnOuterLayersOrStorageAndUiLibraries() throws IOException {
        assertForbiddenImports("entity", List.of("use_case.", "interface_adapter.", "view.",
                "data_access.", "app.", "javax.swing.", "java.awt.", "org.json."));
    }

    @Test
    void useCasesOnlyKnowInnerContractsAndEntities() throws IOException {
        assertForbiddenImports("use_case", List.of("interface_adapter.", "view.", "data_access.",
                "app.", "javax.swing.", "java.awt.", "org.json.", "java.nio.file."));
    }

    @Test
    void interfaceAdaptersDoNotDependOnConcreteViewsOrPersistence() throws IOException {
        assertForbiddenImports("interface_adapter", List.of("view.", "data_access.", "app.",
                "javax.swing.", "java.awt.", "org.json."));
    }

    private void assertForbiddenImports(String layer, List<String> prefixes) throws IOException {
        final Path directory = Paths.get("src", "main", "java", layer);
        final List<Path> files;
        try (Stream<Path> paths = Files.walk(directory)) {
            files = paths.filter(path -> path.toString().endsWith(".java")).collect(Collectors.toList());
        }
        assertFalse(files.isEmpty(), "Expected production classes in " + layer);
        for (Path file : files) {
            for (String line : Files.readAllLines(file)) {
                final String trimmed = line.trim();
                if (trimmed.startsWith("import ")) {
                    final String imported = trimmed.substring(7).replaceFirst("^static\\s+", "");
                    assertTrue(prefixes.stream().noneMatch(imported::startsWith),
                            file + " violates the dependency rule: " + trimmed);
                }
            }
        }
    }
}
