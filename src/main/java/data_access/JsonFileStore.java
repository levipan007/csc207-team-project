package data_access;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import use_case.menu.DataAccessException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Shared JSON validation and atomic file replacement for the local adapters. */
final class JsonFileStore {
    private static final ConcurrentMap<Path, Object> LOCKS = new ConcurrentHashMap<>();

    private JsonFileStore() { }

    static Object lockFor(Path path) {
        return LOCKS.computeIfAbsent(path.toAbsolutePath().normalize(), ignored -> new Object());
    }

    static JSONObject read(Path path, String arrayName) {
        final String text;
        try {
            text = Files.readString(path, StandardCharsets.UTF_8);
        }
        catch (NoSuchFileException exception) {
            return new JSONObject().put("schemaVersion", 1).put(arrayName, new JSONArray());
        }
        catch (IOException | SecurityException exception) {
            throw new DataAccessException("Cannot read " + path.getFileName() + ".", exception);
        }
        try {
            final JSONTokener tokens = new JSONTokener(text);
            final JSONObject document = new JSONObject(tokens);
            if (tokens.nextClean() != 0 || number(document, "schemaVersion") != 1
                    || !(document.get(arrayName) instanceof JSONArray)) {
                throw new IllegalArgumentException("Unsupported schema or trailing content.");
            }
            return document;
        }
        catch (RuntimeException exception) {
            throw malformed(path, exception);
        }
    }

    static void write(Path path, JSONObject document) {
        Path temporary = null;
        try {
            Files.createDirectories(path.getParent());
            temporary = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".tmp");
            final byte[] bytes = (document.toString(2) + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
            try (FileChannel output = FileChannel.open(temporary, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                final ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) {
                    output.write(buffer);
                }
                output.force(true);
            }
            Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException | SecurityException exception) {
            throw new DataAccessException("Cannot save " + path.getFileName()
                    + "; the existing data has not been replaced.", exception);
        }
        finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                }
                catch (IOException | SecurityException ignored) {
                    // A leftover temporary file must not hide the original storage failure.
                }
            }
        }
    }

    static String string(JSONObject object, String key) {
        final Object value = object.get(key);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(key + " must be a string.");
        }
        return (String) value;
    }

    static long number(JSONObject object, String key) {
        final Object value = object.get(key);
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException(key + " must be an integer number.");
        }
        return new BigDecimal(value.toString()).longValueExact();
    }

    static boolean bool(JSONObject object, String key) {
        final Object value = object.get(key);
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException(key + " must be true or false.");
        }
        return (Boolean) value;
    }

    static DataAccessException malformed(Path path, RuntimeException exception) {
        return new DataAccessException("Malformed " + path.getFileName()
                + "; fix the data file before continuing. No data was reset.", exception);
    }
}
