package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;

/**
 * Represents an inclusive range of displayed person indexes.
 */
public record IndexRange(Index start, Index end) {

    /**
     * Creates an inclusive index range.
     *
     * @param start First index in the range.
     * @param end Last index in the range.
     * @throws IllegalArgumentException If {@code end} precedes {@code start}.
     * @throws NullPointerException if {@code start} or {@code end} is null
     */
    public IndexRange {
        requireNonNull(start);
        requireNonNull(end);
        if (end.getZeroBased() < start.getZeroBased()) {
            throw new IllegalArgumentException("Range end must not precede range start");
        }
    }

    /**
     * Creates a range containing only one index.
     *
     * @param index Index to include.
     * @return A single-index range.
     * @throws NullPointerException if {@code index} is null
     */
    public static IndexRange single(Index index) {
        return new IndexRange(index, index);
    }

    /**
     * Returns the supplied ranges in canonical form.
     *
     * <p>The returned ranges are sorted and contain no duplicates,
     * overlaps, or adjacent ranges.
     *
     * @param ranges ranges to normalize
     * @return immutable canonical list of ranges
     * @throws NullPointerException if {@code ranges} or one of its elements is null
     */
    public static List<IndexRange> normalize(List<IndexRange> ranges) {
        requireNonNull(ranges);

        List<IndexRange> sortedRanges = new ArrayList<>(ranges);
        sortedRanges.forEach(Objects::requireNonNull);
        sortedRanges.sort(Comparator.comparingInt(range -> range.start().getZeroBased()));

        List<IndexRange> normalizedRanges = new ArrayList<>();

        for (IndexRange next : sortedRanges) {
            if (normalizedRanges.isEmpty()) {
                normalizedRanges.add(next);
                continue;
            }

            int lastPosition = normalizedRanges.size() - 1;
            IndexRange last = normalizedRanges.get(lastPosition);

            long nextStart = next.start().getZeroBased();
            long lastEnd = last.end().getZeroBased();

            if (nextStart <= lastEnd + 1) {
                Index mergedEnd = next.end().getZeroBased() > last.end().getZeroBased()
                        ? next.end()
                        : last.end();

                normalizedRanges.set(lastPosition, new IndexRange(last.start(), mergedEnd));
            } else {
                normalizedRanges.add(next);
            }
        }

        return List.copyOf(normalizedRanges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                start.getZeroBased(),
                end.getZeroBased());
    }
}
