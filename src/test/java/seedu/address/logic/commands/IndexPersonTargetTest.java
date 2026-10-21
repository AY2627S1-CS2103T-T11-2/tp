package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

public class IndexPersonTargetTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_emptyRanges_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new IndexPersonTarget(List.of()));
    }

    @Test
    public void selectsSingleIndex_singleIndex_returnsTrue() {
        IndexPersonTarget target = new IndexPersonTarget(List.of(IndexRange.single(INDEX_FIRST_PERSON)));

        assertTrue(target.selectsSingleIndex());
    }

    @Test
    public void selectsSingleIndex_multipleIndexes_returnsFalse() {
        IndexPersonTarget target = new IndexPersonTarget(List.of(
                new IndexRange(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON)));

        assertFalse(target.selectsSingleIndex());
    }

    @Test
    public void resolve_overlappingUnorderedRanges_returnsDistinctDisplayOrder()
            throws CommandException {
        IndexPersonTarget target = new IndexPersonTarget(List.of(
                IndexRange.single(INDEX_THIRD_PERSON),
                new IndexRange(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON),
                new IndexRange(INDEX_SECOND_PERSON, INDEX_THIRD_PERSON)));

        List<Person> expected = List.copyOf(model.getFilteredPersonList().subList(0, 3));

        assertEquals(expected, target.resolve(model));
    }

    @Test
    public void resolve_separateRanges_returnsSelectedPeopleInDisplayOrder()
            throws CommandException {
        IndexPersonTarget target = new IndexPersonTarget(List.of(
                IndexRange.single(INDEX_THIRD_PERSON),
                IndexRange.single(INDEX_FIRST_PERSON)));

        List<Person> expected = List.of(
                model.getFilteredPersonList().get(0),
                model.getFilteredPersonList().get(2));

        assertEquals(expected, target.resolve(model));
    }

    @Test
    public void resolve_invalidIndex_throwsCommandException() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        IndexPersonTarget target = new IndexPersonTarget(List.of(
                IndexRange.single(INDEX_FIRST_PERSON),
                IndexRange.single(invalidIndex)));

        CommandException exception = assertThrows(CommandException.class, () -> target.resolve(model));

        assertEquals(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, exception.getMessage());
    }

    @Test
    public void equals_normalizedEquivalentRanges_returnsTrue() {
        IndexPersonTarget combined = new IndexPersonTarget(List.of(
                new IndexRange(INDEX_FIRST_PERSON, INDEX_THIRD_PERSON)));

        IndexPersonTarget overlapping = new IndexPersonTarget(List.of(
                new IndexRange(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON),
                new IndexRange(INDEX_SECOND_PERSON, INDEX_THIRD_PERSON)));

        assertEquals(combined, combined);
        assertEquals(combined, overlapping);
        assertEquals(combined.hashCode(), overlapping.hashCode());
    }

    @Test
    public void equals_differentRanges_returnsFalse() {
        IndexPersonTarget first = new IndexPersonTarget(List.of(IndexRange.single(INDEX_FIRST_PERSON)));
        IndexPersonTarget second = new IndexPersonTarget(List.of(IndexRange.single(INDEX_SECOND_PERSON)));

        assertNotEquals(first, second);
        assertNotEquals(first, null);
        assertNotEquals(first, "not a target");
    }

    @Test
    public void toString_returnsNormalizedRanges() {
        IndexRange expectedRange = new IndexRange(INDEX_FIRST_PERSON, INDEX_THIRD_PERSON);
        IndexPersonTarget target = new IndexPersonTarget(List.of(
                IndexRange.single(INDEX_FIRST_PERSON),
                new IndexRange(INDEX_SECOND_PERSON, INDEX_THIRD_PERSON)));

        assertEquals(List.of(expectedRange).toString(), target.toString());
    }

    @Test
    public void constructor_nullRanges_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new IndexPersonTarget(null));
    }

    @Test
    public void resolve_filteredList_usesDisplayedIndexes()
            throws CommandException {
        Person secondPerson =
                model.getFilteredPersonList().get(1);
        Person thirdPerson =
                model.getFilteredPersonList().get(2);

        model.updateFilteredPersonList(person -> person.equals(secondPerson) || person.equals(thirdPerson));

        IndexPersonTarget target = new IndexPersonTarget(List.of(IndexRange.single(INDEX_FIRST_PERSON)));

        assertEquals(List.of(secondPerson), target.resolve(model));
    }

    @Test
    public void resolve_returnedListIsImmutable()
            throws CommandException {
        IndexPersonTarget target = new IndexPersonTarget(List.of(IndexRange.single(INDEX_FIRST_PERSON)));
        List<Person> resolvedPeople = target.resolve(model);

        assertThrows(UnsupportedOperationException.class, () ->
                resolvedPeople.add(model.getFilteredPersonList().get(1)));
    }

    @Test
    public void resolve_nullModel_throwsNullPointerException() {
        IndexPersonTarget target = new IndexPersonTarget(List.of(IndexRange.single(INDEX_FIRST_PERSON)));

        assertThrows(NullPointerException.class, () -> target.resolve(null));
    }
}
