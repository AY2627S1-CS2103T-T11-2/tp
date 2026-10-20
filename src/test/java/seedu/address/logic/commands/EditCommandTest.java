package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_GROUP_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_SUBJECT_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person editedPerson = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withTags(VALID_TAG_HUSBAND).build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).withTags(VALID_TAG_HUSBAND).build();
        EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_clearPhone_preservesOtherFields() {
        Person originalPerson = model.getFilteredPersonList().get(0);
        Person editedPerson = new PersonBuilder(originalPerson).withPhone("").build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone("").build();

        assertOptionalFieldsEdited(descriptor, editedPerson);
    }

    @Test
    public void execute_clearTelegramHandle_preservesOtherFields() {
        Person originalPerson = model.getFilteredPersonList().get(0);
        Person editedPerson = new PersonBuilder(originalPerson).withTelegramHandle("").build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTelegramHandle("").build();

        assertOptionalFieldsEdited(descriptor, editedPerson);
    }

    @Test
    public void execute_clearTags_preservesOtherFields() {
        Person originalPerson = model.getFilteredPersonList().get(0);
        Person editedPerson = new PersonBuilder(originalPerson).withTags().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags().build();

        assertOptionalFieldsEdited(descriptor, editedPerson);
    }

    @Test
    public void execute_clearAllOptionalFields_preservesCompulsoryFields() {
        Person originalPerson = model.getFilteredPersonList().get(0);
        Person editedPerson = new PersonBuilder(originalPerson).withPhone("").withTelegramHandle("").withTags().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withPhone("").withTelegramHandle("").withTags().build();

        assertOptionalFieldsEdited(descriptor, editedPerson);
    }

    /**
     * Verifies an optional-field edit against the complete expected person and model.
     */
    private void assertOptionalFieldsEdited(EditPersonDescriptor descriptor, Person editedPerson) {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // edit person in filtered list into a duplicate in address book
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_PERSON, DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_PERSON, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_PERSON, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        // Single Person toString Assertion
        Index index = Index.fromOneBased(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(index, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editPersonDescriptor="
                + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());

        // Multiple Persons toString Assertion
        IndexPersonTarget target = new IndexPersonTarget(List.of(
                new IndexRange(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON)));
        EditCommand bulkEditCommand = new EditCommand(target, editPersonDescriptor);
        String expectedBulk = EditCommand.class.getCanonicalName() + "{target=" + target
                + ", editPersonDescriptor=" + editPersonDescriptor + "}";

        assertEquals(expectedBulk, bulkEditCommand.toString());
    }

    @Test
    public void execute_multipleIndexesAllowedFields_success() {
        List<Person> originalPeople = List.copyOf(
                model.getFilteredPersonList().subList(0, 3));

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withSubject(VALID_SUBJECT_AMY)
                .withGroup(VALID_GROUP_AMY)
                .withTags(VALID_TAG_FRIEND, VALID_TAG_HUSBAND)
                .build();

        IndexPersonTarget target = new IndexPersonTarget(List.of(
                new IndexRange(INDEX_FIRST_PERSON, INDEX_THIRD_PERSON)));
        EditCommand editCommand = new EditCommand(target, descriptor);

        List<Person> editedPeople = originalPeople.stream()
                .map(person -> new PersonBuilder(person)
                        .withSubject(VALID_SUBJECT_AMY)
                        .withGroup(VALID_GROUP_AMY)
                        .withTags(VALID_TAG_FRIEND, VALID_TAG_HUSBAND)
                        .build())
                .toList();

        Model expectedModel = new ModelManager(
                new AddressBook(model.getAddressBook()), new UserPrefs());

        for (int i = 0; i < originalPeople.size(); i++) {
            expectedModel.setPerson(originalPeople.get(i), editedPeople.get(i));
        }

        String editedPeopleSummary = editedPeople.stream()
                .map(Messages::format)
                .collect(Collectors.joining("\n"));
        String expectedMessage = String.format(
                EditCommand.MESSAGE_EDIT_PERSONS_SUCCESS,
                editedPeople.size(),
                editedPeopleSummary);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_multipleIndexesDisallowedField_failure() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(VALID_NAME_BOB)
                .build();

        IndexPersonTarget target = new IndexPersonTarget(List.of(
                new IndexRange(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON)));
        EditCommand editCommand = new EditCommand(target, descriptor);

        assertCommandFailure(
                editCommand,
                model,
                EditCommand.MESSAGE_INVALID_BULK_EDIT);
    }

    @Test
    public void execute_multipleIndexesOneInvalidIndex_failure() {
        Index invalidIndex =
                Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withTags(VALID_TAG_FRIEND)
                .build();

        IndexPersonTarget target = new IndexPersonTarget(List.of(
                IndexRange.single(INDEX_FIRST_PERSON),
                IndexRange.single(invalidIndex)));
        EditCommand editCommand = new EditCommand(target, descriptor);

        assertCommandFailure(
                editCommand,
                model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void hasSinglePersonOnlyFields_variousDescriptors_returnsExpectedResult() {
        assertFalse(new EditPersonDescriptor().hasSinglePersonOnlyFields());

        assertTrue(new EditPersonDescriptorBuilder()
            .withName(VALID_NAME_BOB)
            .build()
            .hasSinglePersonOnlyFields());

        assertTrue(new EditPersonDescriptorBuilder()
            .withPhone(VALID_PHONE_BOB)
            .build()
            .hasSinglePersonOnlyFields());

        assertFalse(new EditPersonDescriptorBuilder()
            .withSubject(VALID_SUBJECT_AMY)
            .withGroup(VALID_GROUP_AMY)
            .withTags(VALID_TAG_FRIEND)
            .build()
            .hasSinglePersonOnlyFields());
    }

    @Test
    public void execute_multipleIndexesEmptyTags_clearsTags() throws CommandException {
        Person unaffectedPerson = model.getFilteredPersonList().get(2);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withTags()
                .build();
        IndexPersonTarget target = new IndexPersonTarget(List.of(
                new IndexRange(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON)));

        CommandResult result = new EditCommand(target, descriptor).execute(model);

        assertTrue(model.getFilteredPersonList().get(0).getTags().isEmpty());
        assertTrue(model.getFilteredPersonList().get(1).getTags().isEmpty());
        assertEquals(unaffectedPerson, model.getFilteredPersonList().get(2));
        assertTrue(result.getFeedbackToUser().startsWith("Edited 2 persons:"));
    }
}
