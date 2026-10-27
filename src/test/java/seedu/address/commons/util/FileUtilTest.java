package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilTest {

    @TempDir
    public Path testFolder;

    @Test
    void writeToFile_existingTargetPreserved_whenTempCreateFails() throws IOException {
        Path existing = testFolder.resolve("data.json");
        FileUtil.writeToFile(existing, "original-content");

        // Make the parent of the target a path whose parent is NOT a directory:
        // target "data.json/child.json" -> parent is "data.json" (a regular file).
        Path targetInsideFile = existing.resolve("child.json"); // data.json/child.json
        assertThrows(IOException.class, () -> FileUtil.writeToFile(targetInsideFile, "new-content"));

        // The original file must be untouched.
        assertEquals("original-content", Files.readString(existing));
    }
}
