package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteCommand}.
 */
public class DeleteCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        DeleteCommand deleteSecondCommand = new DeleteCommand(INDEX_SECOND_PERSON);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteCommand deleteFirstCommandCopy = new DeleteCommand(INDEX_FIRST_PERSON);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }

    @Test
    public void toStringMethod() {
        PersonTarget target = new IndexPersonTarget(List.of(IndexRange.single(INDEX_FIRST_PERSON)));
        DeleteCommand deleteCommand = new DeleteCommand(target);
        String expected = DeleteCommand.class.getCanonicalName() + "{target=" + target + "}";
        assertEquals(expected, deleteCommand.toString());
    }

    @Test
    public void execute_multipleIndexesUnfilteredList_success() {
        List<Person> peopleToDelete = List.of(
                model.getFilteredPersonList().get(0),
                model.getFilteredPersonList().get(2),
                model.getFilteredPersonList().get(3));
        DeleteCommand deleteCommand = commandFor(
                IndexRange.single(Index.fromOneBased(1)),
                new IndexRange(
                        Index.fromOneBased(3),
                        Index.fromOneBased(4)));

        Model expectedModel =
                new ModelManager(model.getAddressBook(), new UserPrefs());
        peopleToDelete.forEach(expectedModel::deletePerson);

        assertCommandSuccess(
                deleteCommand,
                model,
                formatMultipleSuccess(peopleToDelete),
                expectedModel);
    }

    @Test
    public void execute_overlappingRanges_deletesEachPersonOnce() {
        List<Person> peopleToDelete = List.copyOf(
                model.getFilteredPersonList().subList(0, 4));
        DeleteCommand deleteCommand = commandFor(
                new IndexRange(
                        Index.fromOneBased(1),
                        Index.fromOneBased(3)),
                new IndexRange(
                        Index.fromOneBased(2),
                        Index.fromOneBased(4)));

        Model expectedModel =
                new ModelManager(model.getAddressBook(), new UserPrefs());
        peopleToDelete.forEach(expectedModel::deletePerson);

        assertCommandSuccess(
                deleteCommand,
                model,
                formatMultipleSuccess(peopleToDelete),
                expectedModel);
    }

    @Test
    public void execute_oneInvalidIndex_doesNotDeleteAnyone() {
        int size = model.getFilteredPersonList().size();
        DeleteCommand deleteCommand = commandFor(
                IndexRange.single(INDEX_FIRST_PERSON),
                IndexRange.single(Index.fromOneBased(size + 1)));

        assertCommandFailure(
                deleteCommand,
                model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    /**
     * Creates a delete command for the supplied index ranges.
     *
     * @param ranges Ranges the command should delete.
     * @return A delete command containing the ranges.
     */
    private DeleteCommand commandFor(IndexRange... ranges) {
        return new DeleteCommand(
                new IndexPersonTarget(List.of(ranges)));
    }

    /**
     * Formats the expected success message for a multiple-person deletion.
     *
     * @param people People included in the message.
     * @return The formatted success message.
     */
    private String formatMultipleSuccess(List<Person> people) {
        String details = people.stream()
                .map(Messages::format)
                .collect(Collectors.joining("\n"));
        return String.format(
                DeleteCommand.MESSAGE_DELETE_PERSONS_SUCCESS,
                people.size(),
                details);
    }
}
