package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class MainAppTest {

    @TempDir
    public Path testFolder;

    @Test
    public void migrateLegacyDataFileIfNeeded_legacyDataExists_copiesDataAndKeepsLegacyFile() throws Exception {
        Path legacyDataFile = testFolder.resolve("addressbook.json");
        Path dataFile = testFolder.resolve("data").resolve("tutortrack.json");
        String legacyData = "{\"persons\": []}";
        Files.writeString(legacyDataFile, legacyData);

        Path resolvedDataFile = MainApp.migrateLegacyDataFileIfNeeded(dataFile, legacyDataFile);

        assertEquals(dataFile, resolvedDataFile);
        assertTrue(Files.exists(legacyDataFile));
        assertEquals(legacyData, Files.readString(dataFile));
    }

    @Test
    public void migrateLegacyDataFileIfNeeded_dataFolderLegacyDataExists_copiesData() throws Exception {
        Path legacyDataFile = testFolder.resolve("data").resolve("addressbook.json");
        Path dataFile = testFolder.resolve("data").resolve("tutortrack.json");
        Files.createDirectories(legacyDataFile.getParent());
        Files.writeString(legacyDataFile, "{\"persons\": []}");

        Path resolvedDataFile = MainApp.migrateLegacyDataFileIfNeeded(dataFile, legacyDataFile);

        assertEquals(dataFile, resolvedDataFile);
        assertTrue(Files.exists(dataFile));
    }

    @Test
    public void migrateLegacyDataFileIfNeeded_newDataExists_keepsNewData() throws Exception {
        Path legacyDataFile = testFolder.resolve("addressbook.json");
        Path dataFile = testFolder.resolve("tutortrack.json");
        Files.writeString(legacyDataFile, "legacy data");
        Files.writeString(dataFile, "new data");

        Path resolvedDataFile = MainApp.migrateLegacyDataFileIfNeeded(dataFile, legacyDataFile);

        assertEquals(dataFile, resolvedDataFile);
        assertEquals("new data", Files.readString(dataFile));
        assertEquals("legacy data", Files.readString(legacyDataFile));
    }

    @Test
    public void migrateLegacyDataFileIfNeeded_noLegacyData_usesNewDataPath() {
        Path legacyDataFile = testFolder.resolve("addressbook.json");
        Path dataFile = testFolder.resolve("tutortrack.json");

        Path resolvedDataFile = MainApp.migrateLegacyDataFileIfNeeded(dataFile, legacyDataFile);

        assertEquals(dataFile, resolvedDataFile);
        assertFalse(Files.exists(dataFile));
    }
}
