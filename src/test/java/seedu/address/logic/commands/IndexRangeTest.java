package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;

/**
 * Contains unit tests for {@link IndexRange}.
 */
public class IndexRangeTest {
    @Test
    public void hashCode_equalRanges_sameHashCode() {
        IndexRange first = new IndexRange(
                Index.fromOneBased(1),
                Index.fromOneBased(3));
        IndexRange second = new IndexRange(
                Index.fromOneBased(1),
                Index.fromOneBased(3));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void normalize_unsortedOverlappingAndAdjacentRanges_returnsCanonicalRanges() {
        List<IndexRange> ranges = List.of(
                range(5, 7),
                range(1, 3),
                range(2, 4),
                single(7),
                single(10));

        assertEquals(
                List.of(range(1, 7), single(10)),
                IndexRange.normalize(ranges));
    }

    @Test
    public void normalize_emptyList_returnsEmptyList() {
        assertEquals(List.of(), IndexRange.normalize(List.of()));
    }

    @Test
    public void normalize_returnedListIsImmutable() {
        List<IndexRange> normalized =
                IndexRange.normalize(List.of(single(1)));

        assertThrows(UnsupportedOperationException.class, () -> normalized.add(single(2)));
    }

    @Test
    public void normalize_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> IndexRange.normalize(null));
    }

    @Test
    public void constructor_descendingRange_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> range(5, 3));
    }

    private static IndexRange single(int index) {
        return IndexRange.single(Index.fromOneBased(index));
    }

    private static IndexRange range(int start, int end) {
        return new IndexRange(
                Index.fromOneBased(start),
                Index.fromOneBased(end));
    }
}
