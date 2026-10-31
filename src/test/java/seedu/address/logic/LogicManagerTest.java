package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.GROUP_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.SUBJECT_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.TELEGRAM_HANDLE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");
    private static final String LEGACY_DOUBLE_SPACE_DATA = "{ \"persons\" : [ { \"name\" : \"Lee Kim\", "
            + "\"phone\" : \"\", \"email\" : \"lee@example.com\", \"address\" : \"\", \"subject\" : \"CS  2103T\", "
            + "\"group\" : \"T  11\", \"tags\" : [ ] } ] }";

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_clear_requiresPriorConfirmationRequest() throws Exception {
        model.addPerson(AMY);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        assertCommandSuccess("clear " + ClearCommand.CONFIRMATION_KEYWORD,
                ClearCommand.MESSAGE_NO_CONFIRMATION_PENDING, expectedModel);
        assertCommandSuccess("clear", ClearCommand.MESSAGE_CONFIRMATION_REQUIRED, expectedModel);

        expectedModel.setAddressBook(new AddressBook());
        assertCommandSuccess("clear " + ClearCommand.CONFIRMATION_KEYWORD,
                ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_clearConfirmationRequest_isCancelledByAnotherCommand() throws Exception {
        model.addPerson(AMY);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        assertCommandSuccess("clear", ClearCommand.MESSAGE_CONFIRMATION_REQUIRED, expectedModel);
        assertCommandSuccess(ListCommand.COMMAND_WORD, ListCommand.MESSAGE_SUCCESS, expectedModel);
        assertCommandSuccess("clear " + ClearCommand.CONFIRMATION_KEYWORD,
                ClearCommand.MESSAGE_NO_CONFIRMATION_PENDING, expectedModel);
    }

    @Test
    public void execute_addWithTelegramHandleThenEdit_savesChanges() throws Exception {
        Person addedPerson = new PersonBuilder()
                .withName("Alice Tan")
                .withPhone("91234567")
                .withEmail("alice@example.com")
                .withTelegramHandle("@alice_tan")
                .withSubject("Mathematics")
                .withGroup("T11")
                .build();
        Model expectedModel = new ModelManager();
        expectedModel.addPerson(addedPerson);

        assertCommandSuccess("add n/Alice Tan p/91234567 e/alice@example.com h/@alice_tan "
                        + "s/Mathematics g/T11",
                "New student added: Alice Tan; Phone: 9123 4567; Email: alice@example.com; "
                        + "Telegram: @alice_tan; Subject: Mathematics; Group: T11; Tags: ",
                expectedModel);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(expectedModel.getAddressBook(), storage.readAddressBook().get());

        Person editedPerson = new PersonBuilder(addedPerson).withTelegramHandle("@alice_updated").build();
        expectedModel.setPerson(addedPerson, editedPerson);
        assertCommandSuccess("edit 1 h/@alice_updated",
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)), expectedModel);
        assertEquals(expectedModel.getAddressBook(), storage.readAddressBook().get());
    }

    @Test
    public void execute_clearAndRestoreOptionalFields_savesChanges() throws Exception {
        model.addPerson(AMY);
        Person clearedPerson = new PersonBuilder(AMY).withPhone("").withTelegramHandle("").withTags().build();
        Model expectedModel = new ModelManager();
        expectedModel.addPerson(clearedPerson);

        assertCommandSuccess("delete 1 p/ h/ t/",
                String.format(DeleteCommand.MESSAGE_DELETE_FIELDS_SUCCESS, 1, Messages.format(clearedPerson)),
                expectedModel);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(expectedModel.getAddressBook(), storage.readAddressBook().get());

        String telegramHandle = "@alice_123";
        Person restoredPerson = new PersonBuilder(clearedPerson)
                .withPhone("91234567").withTelegramHandle(telegramHandle).withTags("Student", "Online").build();
        expectedModel.setPerson(clearedPerson, restoredPerson);

        assertCommandSuccess("edit 1 p/91234567 h/" + telegramHandle + " t/Student t/Online",
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(restoredPerson)), expectedModel);
        assertEquals(expectedModel.getAddressBook(), storage.readAddressBook().get());
    }

    @Test
    public void execute_editTags_preservesSpellingAndOrderAcrossStorage() throws Exception {
        model.addPerson(new PersonBuilder().withTags("old").build());
        logic.execute("edit 1 t/Student t/Online t/student t/Local t/OLD,New t/online,ONLINE");
        List<String> expectedTags = List.of("New", "Student", "ONLINE", "Local");
        assertEquals(expectedTags,
                model.getFilteredPersonList().getFirst().getTags().stream().map(tag -> tag.tagName).toList());

        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Person savedPerson = storage.readAddressBook().get().getPersonList().getFirst();
        assertEquals(expectedTags, savedPerson.getTags().stream().map(tag -> tag.tagName).toList());
        model.setAddressBook(storage.readAddressBook().get());

        logic.execute("delete 1 t/local t/student");
        assertEquals(List.of("New", "ONLINE"),
                model.getFilteredPersonList().getFirst().getTags().stream().map(tag -> tag.tagName).toList());
        logic.execute("delete 1 t/ t/Missing");
        assertTrue(model.getFilteredPersonList().getFirst().getTags().isEmpty());
        assertTrue(storage.readAddressBook().get().getPersonList().getFirst().getTags().isEmpty());
    }

    @Test
    public void execute_bulkTagDeletion_clearsAllTags() throws Exception {
        model.addPerson(new PersonBuilder().withName("Alice").withTags("First", "Second", "Third").build());
        model.addPerson(new PersonBuilder().withName("Bob").withTags("Only").build());
        model.addPerson(new PersonBuilder().withName("Charlie").withTags().build());
        logic.execute("delete 1-3 t/");
        assertTrue(model.getFilteredPersonList().get(0).getTags().isEmpty());
        assertTrue(model.getFilteredPersonList().get(1).getTags().isEmpty());
        assertTrue(model.getFilteredPersonList().get(2).getTags().isEmpty());
    }

    @Test
    public void execute_eightDigitPhone_storesSpecifiedSpacing() throws Exception {
        model.addPerson(new PersonBuilder().build());
        logic.execute("edit 1 p/9123-4567");
        assertEquals("9123 4567", model.getFilteredPersonList().getFirst().getPhone().value);
        assertTrue(Files.readString(temporaryFolder.resolve("addressBook.json")).contains("\"9123 4567\""));
        for (String phone : List.of("9123456", "912345678", "9123 45678", "+65-9123-4567")) {
            assertThrows(ParseException.class, () -> logic.execute("edit 1 p/" + phone));
            assertEquals("9123 4567", model.getFilteredPersonList().getFirst().getPhone().value);
        }
    }

    @Test
    public void execute_findThenEdit_preservesFilter() throws Exception {
        Person firstAlice = new PersonBuilder().withName("Alice Tan").withGroup("T11").build();
        Person secondAlice = new PersonBuilder().withName("Alice Lee").build();
        Person bob = new PersonBuilder().withName("Bob").build();
        model.addPerson(firstAlice);
        model.addPerson(secondAlice);
        model.addPerson(bob);

        logic.execute("find n/Alice");
        Person editedAlice = new PersonBuilder(firstAlice).withGroup("T12").build();
        logic.execute("edit 1 g/T12");
        assertEquals(List.of(editedAlice, secondAlice), model.getFilteredPersonList());
    }

    @Test
    public void execute_findThenDelete_resetsFilter() throws Exception {
        Person firstAlice = new PersonBuilder().withName("Alice Tan").withGroup("T11").build();
        Person secondAlice = new PersonBuilder().withName("Alice Lee").build();
        Person bob = new PersonBuilder().withName("Bob").build();
        model.addPerson(firstAlice);
        model.addPerson(secondAlice);
        model.addPerson(bob);

        logic.execute("find n/Alice");
        logic.execute("delete 1");
        List<Person> remainingPeople = List.of(secondAlice, bob);

        assertEquals(remainingPeople, model.getFilteredPersonList());
        assertEquals(remainingPeople, model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_invalidFind_keepsDisplayedList() throws Exception {
        Person alice = new PersonBuilder().withName("Alice Tan").build();
        Person bob = new PersonBuilder().withName("Bob").build();
        model.addPerson(alice);
        model.addPerson(bob);
        logic.execute("find n/Alice");
        List<Person> displayedPeople = List.copyOf(model.getFilteredPersonList());

        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () -> logic.execute("find g/T11 x/typo"));
        assertEquals(displayedPeople, model.getFilteredPersonList());
    }

    @Test
    public void execute_miscasedRelationshipQualifier_leavesStudentAndSavedFileUnchanged() throws Exception {
        logic.execute("add n/Ravi S/O Kumar p/91234567 e/ravi@example.com h/@ravi_kumar s/CS2103T g/T11 t/weak");
        Person original = model.getAddressBook().getPersonList().getFirst();
        byte[] savedBytes = Files.readAllBytes(dataFile());

        // #143: each of these used to succeed and silently change the name, the subject, or both.
        for (String command : List.of("edit 1 n/Ravi s/o Kumar", "edit 1 n/Ravi s/O Kumar",
                "edit 1 t/strong n/Ravi s/o Kumar", "edit 1 n/Ravi s/O Level",
                "add n/Ali s/o e/ali@example.com g/T11", "add n/Ali s/o Abu e/ali@example.com s/CS2103T g/T11")) {
            assertThrows(ParseException.class, Name.MESSAGE_INVALID_RELATIONSHIP_QUALIFIER, () ->
                    logic.execute(command));
            assertEquals(List.of(original), model.getAddressBook().getPersonList());
            assertArrayEquals(savedBytes, Files.readAllBytes(dataFile()));
        }

        reopenSavedData();
        assertEquals(List.of(original), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_findSubjectAndGroup_matchesLegacyAndNewSpacingConsistently() throws Exception {
        // A record saved before #145 was fixed keeps its double spaces on disk.
        Files.writeString(dataFile(), LEGACY_DOUBLE_SPACE_DATA);
        reopenSavedData();
        logic.execute("add n/Tan Wei e/tan@example.com s/CS   2103T g/T 11");
        logic.execute("add n/Ng Ann e/ng@example.com s/CS2103T g/T11");

        for (String findCommand : List.of("find s/CS 2103T g/T 11", "find s/cs  2103t g/t   11")) {
            logic.execute(findCommand);
            assertEquals(List.of("Lee Kim", "Tan Wei"), displayedNames());
        }

        // An unrelated edit must not rewrite the legacy subject or group.
        logic.execute("list");
        logic.execute("edit 1 t/legacy");
        reopenSavedData();
        Person legacyPerson = model.getAddressBook().getPersonList().getFirst();
        assertEquals("CS  2103T", legacyPerson.getSubject().subject);
        assertEquals("T  11", legacyPerson.getGroup().group);
        assertEquals("CS 2103T", model.getAddressBook().getPersonList().get(1).getSubject().subject);

        logic.execute("find s/CS 2103T");
        assertEquals(List.of("Lee Kim", "Tan Wei"), displayedNames());
    }

    @Test
    public void execute_findStudentWithRelationshipQualifier_matchesKeywordAndQuotedPhrase() throws Exception {
        logic.execute("add n/Ravi S/O Kumar e/ravi@example.com s/CS2103T g/T11");
        logic.execute("add n/Ravi Shankar e/shankar@example.com s/CS2103T g/T12");

        // The lowercase s/o inside quotes stays part of the phrase instead of starting a subject filter.
        logic.execute("find n/\"ravi s/o kumar\" s/CS2103T");
        assertEquals(List.of("Ravi S/O Kumar"), displayedNames());

        logic.execute("find n/S/O");
        assertEquals(List.of("Ravi S/O Kumar"), displayedNames());

        logic.execute("find n/Ravi g/T12");
        assertEquals(List.of("Ravi Shankar"), displayedNames());
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    private Path dataFile() {
        return temporaryFolder.resolve("addressBook.json");
    }

    /**
     * Replaces the model and logic with ones loaded from the saved data file, as happens when TutorTrack restarts.
     */
    private void reopenSavedData() throws Exception {
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile()),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = new LogicManager(model, storage);
    }

    private List<String> displayedNames() {
        return model.getFilteredPersonList().stream().map(person -> person.getName().fullName).toList();
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + TELEGRAM_HANDLE_DESC_AMY + SUBJECT_DESC_AMY + GROUP_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY)
                .withSubject("CS2100")
                .withGroup("T35")
                .withTags()
                .build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
