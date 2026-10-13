package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Selects people by inclusive ranges of indexes in the displayed person list.
 */
public final class IndexDeleteTarget implements DeleteTarget {

    private final List<IndexRange> ranges;

    /**
     * Creates a target from one or more inclusive index ranges.
     *
     * @param ranges Ranges of displayed indexes to select.
     * @throws IllegalArgumentException If no ranges are provided.
     */
    public IndexDeleteTarget(List<IndexRange> ranges) {
        requireNonNull(ranges);
        if (ranges.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one index range is required");
        }
        this.ranges = normalize(ranges);
    }

    @Override
    public List<Person> resolve(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedPeople =
                List.copyOf(model.getFilteredPersonList());

        // Validate all ranges before performing any deletion.
        for (IndexRange range : ranges) {
            if (range.end().getZeroBased() >= displayedPeople.size()) {
                throw new CommandException(
                        Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }
        }

        Set<Integer> selectedIndexes = new TreeSet<>();
        for (IndexRange range : ranges) {
            for (int index = range.start().getZeroBased();
                    index <= range.end().getZeroBased(); index++) {
                selectedIndexes.add(index);
            }
        }

        List<Person> selectedPeople = new ArrayList<>();
        for (int index : selectedIndexes) {
            selectedPeople.add(displayedPeople.get(index));
        }
        return List.copyOf(selectedPeople);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof IndexDeleteTarget otherTarget
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

    /**
     * Sorts and merges overlapping or adjacent ranges into a canonical list.
     *
     * @param ranges Ranges to normalize.
     * @return Canonical, non-overlapping ranges in ascending order.
     */
    private static List<IndexRange> normalize(List<IndexRange> ranges) {
        List<IndexRange> sortedRanges = new ArrayList<>(ranges);
        sortedRanges.sort(Comparator.comparingInt(
                range -> range.start().getZeroBased()));

        List<IndexRange> normalizedRanges = new ArrayList<>();
        for (IndexRange next : sortedRanges) {
            if (normalizedRanges.isEmpty()) {
                normalizedRanges.add(next);
                continue;
            }

            int lastPosition = normalizedRanges.size() - 1;
            IndexRange last = normalizedRanges.get(lastPosition);
            if (next.start().getZeroBased()
                    <= last.end().getZeroBased() + 1) {
                IndexRange merged = new IndexRange(
                        last.start(),
                        next.end().getZeroBased()
                                > last.end().getZeroBased()
                                ? next.end() : last.end());
                normalizedRanges.set(lastPosition, merged);
            } else {
                normalizedRanges.add(next);
            }
        }
        return List.copyOf(normalizedRanges);
    }
}
