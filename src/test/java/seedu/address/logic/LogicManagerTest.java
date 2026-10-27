package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

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
                "New person added: Alice Tan; Phone: 9123 4567; Email: alice@example.com; "
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

        assertCommandSuccess("edit 1 p/ h/ t/",
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(clearedPerson)), expectedModel);

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
    public void execute_editTags_preservesEntryOrderAcrossStorageAndRemovesFromEnd() throws Exception {
        model.addPerson(new PersonBuilder().withTags("old").build());
        logic.execute("edit 1 t/Student t/Online t/student t/Local");
        assertEquals(List.of("Student", "Online", "Local"),
                model.getFilteredPersonList().getFirst().getTags().stream().map(tag -> tag.tagName).toList());

        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Person savedPerson = storage.readAddressBook().get().getPersonList().getFirst();
        assertEquals(List.of("Student", "Online", "Local"),
                savedPerson.getTags().stream().map(tag -> tag.tagName).toList());
        model.setAddressBook(storage.readAddressBook().get());

        logic.execute("edit 1 t/");
        assertEquals(List.of("Student", "Online"),
                model.getFilteredPersonList().getFirst().getTags().stream().map(tag -> tag.tagName).toList());
        logic.execute("edit 1 t/ t/");
        assertTrue(model.getFilteredPersonList().getFirst().getTags().isEmpty());
        logic.execute("edit 1 t/ t/ t/");
        assertTrue(model.getFilteredPersonList().getFirst().getTags().isEmpty());
        assertTrue(storage.readAddressBook().get().getPersonList().getFirst().getTags().isEmpty());
    }

    @Test
    public void execute_bulkTagRemoval_appliesCountToEachPerson() throws Exception {
        model.addPerson(new PersonBuilder().withName("Alice").withTags("First", "Second", "Third").build());
        model.addPerson(new PersonBuilder().withName("Bob").withTags("Only").build());
        model.addPerson(new PersonBuilder().withName("Charlie").withTags().build());
        logic.execute("edit 1-3 t/ t/");
        assertEquals(List.of("First"),
                model.getFilteredPersonList().get(0).getTags().stream().map(tag -> tag.tagName).toList());
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
