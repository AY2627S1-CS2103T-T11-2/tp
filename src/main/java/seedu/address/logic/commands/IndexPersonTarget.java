package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Selects people using inclusive ranges of displayed indexes.
 */
public final class IndexPersonTarget implements PersonTarget {

    private final List<IndexRange> ranges;

    /**
     * Creates a target from one or more inclusive index ranges.
     *
     * @param ranges displayed index ranges to select
     * @throws IllegalArgumentException if no ranges are supplied
     * @throws NullPointerException if {@code ranges} or one of its elements is null
     */
    public IndexPersonTarget(List<IndexRange> ranges) {
        requireNonNull(ranges);

        if (ranges.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one index range is required");
        }

        this.ranges = IndexRange.normalize(ranges);
    }

    /**
     * Returns whether this target selects exactly one displayed index.
     *
     * @return true if exactly one index is selected
     */
    public boolean selectsSingleIndex() {
        return ranges.size() == 1
                && ranges.getFirst().start()
                        .equals(ranges.getFirst().end());
    }

    @Override
    public List<Person> resolve(Model model)
            throws CommandException {
        requireNonNull(model);

        List<Person> displayedPeople =
                List.copyOf(model.getFilteredPersonList());

        validateRanges(displayedPeople.size());

        Set<Integer> selectedIndexes = new TreeSet<>();

        for (IndexRange range : ranges) {
            for (int index = range.start().getZeroBased();
                    index <= range.end().getZeroBased();
                    index++) {
                selectedIndexes.add(index);
            }
        }

        List<Person> selectedPeople = new ArrayList<>();

        for (int index : selectedIndexes) {
            selectedPeople.add(displayedPeople.get(index));
        }

        return List.copyOf(selectedPeople);
    }

    /**
     * Validates all ranges before any selected people are returned.
     */
    private void validateRanges(int displayedListSize)
            throws CommandException {
        for (IndexRange range : ranges) {
            if (range.end().getZeroBased()
                    >= displayedListSize) {
                throw new CommandException(
                        Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof IndexPersonTarget otherTarget
                && ranges.equals(otherTarget.ranges);
    }

    @Override
    public int hashCode() {
        return ranges.hashCode();
    }

    @Override
    public String toString() {
        return ranges.toString();
    }
}
