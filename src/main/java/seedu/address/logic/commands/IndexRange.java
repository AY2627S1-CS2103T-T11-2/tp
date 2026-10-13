package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

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
     */
    public static IndexRange single(Index index) {
        return new IndexRange(index, index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                start.getZeroBased(),
                end.getZeroBased());
    }
}
