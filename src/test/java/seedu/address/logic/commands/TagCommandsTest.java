package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests tag command parsing and execution together, including bulk validation and display spelling.
 */
public class TagCommandsTest {

    private final Model model = new ModelManager();
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void add_caseVariants_preservesFirstSpelling() throws Exception {
        execute("add n/Alice e/alice@example.com s/Math g/T11 t/Student t/student t/STUDENT t/Online");
        assertTags(0, "Student", "Online");
    }

    @Test
    public void edit_additions_preserveExistingTagsAndTheirSpelling() throws Exception {
        addPerson("Alice", "Student", "Existing");
        execute("edit 1 t/student t/Online t/STUDENT t/Local");
        assertTags(0, "Student", "Existing", "Online", "Local");
    }

    @Test
    public void edit_rename_trimsCommaWhitespaceAndMatchesWithoutCase() throws Exception {
        addPerson("Alice", "Student", "Local");
        execute("edit 1 t/STUDENT, ONLINE");
        assertTags(0, "ONLINE", "Local");
        execute("edit 1 t/online , Student");
        assertTags(0, "Student", "Local");
    }

    @Test
    public void edit_merge_usesReplacementSpellingRegardlessOfDestinationPosition() throws Exception {
        addPerson("Alice", "Student", "Local", "Online");
        addPerson("Bob", "Online", "Local", "Student");
        execute("edit 1-2 t/student,ONLINE");
        assertTags(0, "ONLINE", "Local");
        assertTags(1, "Local", "ONLINE");
    }

    @Test
    public void edit_caseOnlyRename_updatesDisplay() throws Exception {
        addPerson("Alice", "Student", "Online");
        CommandResult result = execute("edit 1 t/student,STUDENT");
        assertTags(0, "STUDENT", "Online");
        assertTrue(result.getFeedbackToUser().contains("[STUDENT]"));
    }

    @Test
    public void edit_mixedOperations_processLeftToRightWithoutDeduplicating() throws Exception {
        addPerson("Alice", "Existing");
        execute("edit 1 t/New t/New,Updated t/New t/Updated,Final t/Final,FINAL");
        assertTags(0, "Existing", "FINAL", "New");
    }

    @Test
    public void edit_missingSource_rejectsAllChanges() {
        addPerson("Alice", "Student");
        assertRejectedUnchanged("edit 1 g/Changed t/New t/Missing,Updated");
        assertRejectedUnchanged("edit 1 t/Student,New t/Student,Other");
    }

    @Test
    public void edit_bulkMissingSource_rejectsAllSelectedContacts() {
        addPerson("Alice", "Student");
        addPerson("Bob", "STUDENT");
        addPerson("Charlie", "Online");
        assertRejectedUnchanged("edit 1-3 g/Changed t/New t/student,Graduate");
    }

    @Test
    public void edit_invalidTagSyntax_rejectsCommand() {
        addPerson("Alice", "Student");
        for (String value : List.of("", " ", "Online Student", "Student,", ",Online", "Student,,Online",
                "Student,Online,Local", "Student,On line", "Stu dent,Online", "Student,Online!")) {
            assertThrows(ParseException.class, () -> execute("edit 1 t/" + value));
            assertTags(0, "Student");
        }
    }

    @Test
    public void delete_namedTags_matchesWithoutCaseAndPreservesOtherFields() throws Exception {
        Person original = addPerson("Alice", "Student", "Online", "Local");
        execute("delete 1 t/STUDENT t/online t/ONLINE");
        assertEquals(new PersonBuilder(original).withTags("Local").build(), model.getFilteredPersonList().getFirst());
        assertTags(0, "Local");
    }

    @Test
    public void delete_optionalFields_canBeCombined() throws Exception {
        Person original = addPerson("Alice", "Student", "Online", "Local");
        execute("delete 1 p/ h/ t/online t/student");
        Person expected = new PersonBuilder(original).withPhone("").withTelegramHandle("").withTags("Local").build();
        assertEquals(expected, model.getFilteredPersonList().getFirst());
        execute("delete 1 p/ h/"); // Already empty optional fields are valid.
        assertEquals(expected, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void delete_emptyTag_overridesNamedDeletionsInAnyOrder() throws Exception {
        addPerson("Alice", "Student", "Online");
        addPerson("Bob");
        execute("delete 1-2 t/Missing t/ t/OtherMissing");
        assertTags(0);
        assertTags(1);
        execute("delete 1-2 t/ t/Student t/");
        assertEquals(2, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void delete_missingTag_rejectsPhoneAndTagChanges() {
        addPerson("Alice", "Student", "Online");
        assertRejectedUnchanged("delete 1 p/ h/ t/student t/Missing");
    }

    @Test
    public void delete_bulkMissingTag_rejectsAllSelectedContacts() {
        addPerson("Alice", "Student");
        addPerson("Bob", "STUDENT");
        addPerson("Charlie", "Online");
        assertRejectedUnchanged("delete 1-3 p/ t/Student");
    }

    @Test
    public void delete_bulkOptionalFields_supportsOverlappingRanges() throws Exception {
        addPerson("Alice", "Student", "Online");
        addPerson("Bob", "STUDENT");
        addPerson("Charlie", "Student");
        Person unaffected = addPerson("David", "Student");
        execute("delete 1-2,2-3 p/ h/ t/student");
        assertTags(0, "Online");
        assertTags(1);
        assertTags(2);
        for (Person person : model.getFilteredPersonList().subList(0, 3)) {
            assertTrue(person.getPhone().isEmpty());
            assertTrue(person.getTelegramHandle().isEmpty());
        }
        assertEquals(unaffected, model.getFilteredPersonList().get(3));
    }

    @Test
    public void delete_invalidIndex_rejectsAllChanges() {
        addPerson("Alice", "Student");
        assertRejectedUnchanged("delete 1-2 p/ t/");
    }

    @Test
    public void delete_fields_keepsCurrentFilterAndUsesDisplayedIndexes() throws Exception {
        Person unaffected = addPerson("Alice", "Student");
        addPerson("Bob", "Student");
        execute("find n/Bob");
        execute("delete 1 p/ t/student");
        assertEquals(1, model.getFilteredPersonList().size());
        assertTags(0);
        assertTrue(model.getFilteredPersonList().getFirst().getPhone().isEmpty());
        assertEquals(unaffected, model.getAddressBook().getPersonList().getFirst());
    }

    @Test
    public void tagEdit_validatesNamesAndLeavesOriginalTagsUnchanged() throws Exception {
        assertThrows(NullPointerException.class, () -> new TagEdit(null, null));
        assertThrows(IllegalArgumentException.class, () -> new TagEdit(null, "Two words"));
        assertThrows(IllegalArgumentException.class, () -> new TagEdit("", "Valid"));
        Set<Tag> original = Set.of(new Tag("Student"));
        Set<Tag> updated = new TagEdit("student", "ONLINE").applyTo(original);
        assertEquals(Set.of(new Tag("Student")), original);
        assertEquals("ONLINE", updated.iterator().next().tagName);
    }

    @Test
    public void delete_fieldsDescriptor_isImmutableAndDistinguishesWholeDeletion() throws Exception {
        DeleteCommand.FieldsToDelete fields = new DeleteCommand.FieldsToDelete(false, false, false,
                Set.of(new Tag("Student")));
        assertThrows(UnsupportedOperationException.class, () -> fields.tags().clear());
        assertThrows(IllegalArgumentException.class, () -> new DeleteCommand.FieldsToDelete(
                false, false, false, Set.of()));
        Command wholeDelete = parser.parseCommand("delete 1");
        Command tagDelete = parser.parseCommand("delete 1 t/Student");
        assertFalse(wholeDelete.equals(tagDelete));
        assertFalse(tagDelete.equals(wholeDelete));
        assertEquals(tagDelete, parser.parseCommand("delete 1 t/student"));
        assertTrue(tagDelete.toString().contains("fields="));
    }

    private Person addPerson(String name, String... tags) {
        Person person = new PersonBuilder().withName(name).withTags(tags).build();
        model.addPerson(person);
        return person;
    }

    private CommandResult execute(String command) throws ParseException, CommandException {
        return parser.parseCommand(command).execute(model);
    }

    private void assertTags(int index, String... expected) {
        assertEquals(List.of(expected), model.getFilteredPersonList().get(index).getTags().stream()
                .map(tag -> tag.tagName).toList());
    }

    private void assertRejectedUnchanged(String command) {
        AddressBook original = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, () -> execute(command));
        assertEquals(original, model.getAddressBook());
    }
}
