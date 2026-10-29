package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.util.SampleDataUtil;

public class MainAppTest {

    private static final String INVALID_NAME_DATA = "{ \"persons\" : [ { \"name\" : \"John  Tan\", \"phone\" : \"\", "
            + "\"email\" : \"john@example.com\", \"address\" : \"\", \"subject\" : \"CS2103T\", \"group\" : \"T11\", "
            + "\"tags\" : [ ] } ] }";
    private static final String MALFORMED_DATA = "{ \"persons\" : [ { \"name\" : ";
    private static final String VALID_DATA = "{ \"persons\" : [ { \"name\" : \"John Tan\", \"phone\" : \"\", "
            + "\"email\" : \"john@example.com\", \"address\" : \"\", \"subject\" : \"CS2103T\", \"group\" : \"T11\", "
            + "\"tags\" : [ ] } ] }";

    @TempDir
    public Path testFolder;

    @Test
    public void initComponents_invalidFieldValue_stopsWithoutChangingFile() throws Exception {
        assertLoadingFailurePreservesFile(INVALID_NAME_DATA);
    }

    @Test
    public void initComponents_malformedJson_stopsWithoutChangingFile() throws Exception {
        assertLoadingFailurePreservesFile(MALFORMED_DATA);
    }

    @Test
    public void initComponents_missingDataFile_usesSampleData() {
        MainApp app = new MainApp();

        app.initComponents(testFolder.resolve("prefs.json"), testFolder.resolve("addressbook.json"));

        assertEquals(new AddressBook(SampleDataUtil.getSampleAddressBook()), app.model.getAddressBook());
        assertNotNull(app.logic);
        assertTrue(app.getDataLoadingErrorMessage().isEmpty());
    }

    @Test
    public void initComponents_validDataFile_loadsData() throws Exception {
        Path dataFile = testFolder.resolve("addressbook.json");
        Files.writeString(dataFile, VALID_DATA);
        MainApp app = new MainApp();

        app.initComponents(testFolder.resolve("prefs.json"), dataFile);

        assertEquals("John Tan", app.model.getAddressBook().getPersonList().getFirst().getName().fullName);
        assertNotNull(app.logic);
        assertTrue(app.getDataLoadingErrorMessage().isEmpty());
    }

    /**
     * Asserts that a data file that cannot be loaded leaves no component able to save, and that stopping the
     * application afterwards leaves the file byte-for-byte unchanged.
     */
    private void assertLoadingFailurePreservesFile(String content) throws Exception {
        Path dataFile = testFolder.resolve("addressbook.json");
        Files.writeString(dataFile, content);
        byte[] originalBytes = Files.readAllBytes(dataFile);
        MainApp app = new MainApp();

        app.initComponents(testFolder.resolve("prefs.json"), dataFile);
        app.stop();

        assertNull(app.model);
        assertNull(app.logic);
        assertNull(app.ui);
        assertTrue(app.getDataLoadingErrorMessage().orElseThrow().contains(dataFile.toAbsolutePath().toString()));
        assertArrayEquals(originalBytes, Files.readAllBytes(dataFile));
    }

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
