package seedu.address.commons.util;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Writes and reads files
 */
public class FileUtil {

    private static final String CHARSET = "UTF-8";

    /**
     * Creates a file if it does not exist along with its missing parent directories.
     * @throws IOException if the file or directory cannot be created.
     */
    public static void createIfMissing(Path file) throws IOException {
        if (Files.exists(file)) {
            return;
        }

        createParentDirsOfFile(file);

        Files.createFile(file);
    }

    /**
     * Creates parent directories of file if it has a parent directory
     */
    private static void createParentDirsOfFile(Path file) throws IOException {
        Path parentDir = file.getParent();

        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Reads the contents of {@code file}. We assume the file exists.
     *
     * @param file file to read
     * @return file contents decoded using the application charset
     * @throws IOException if the file cannot be read
     */
    public static String readFromFile(Path file) throws IOException {
        return new String(Files.readAllBytes(file), CHARSET);
    }

    /**
     * Replaces {@code file} with {@code content}, using an atomic move when supported.
     *
     * @param file destination file
     * @param content content to write
     * @throws IOException if the content cannot be written or moved into place
     */
    public static void writeToFile(Path file, String content) throws IOException {
        Path target = file.toAbsolutePath();
        Path parent = target.getParent();
        Path temp = Files.createTempFile(parent, ".tmp-", ".tmp");
        try {
            Files.write(temp, content.getBytes(CHARSET));
            try {
                Files.move(temp, target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                // Filesystem without atomic rename (e.g. some network volumes):
                // fall back to a regular replace — still better than truncate-in-place.
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            Files.deleteIfExists(temp);
            throw e;
        }
    }

}
