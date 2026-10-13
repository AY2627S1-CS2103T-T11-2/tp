package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
