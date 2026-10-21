package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.IndexRange;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Contains tests for {@link IndexRangeParser}.
 */
public class IndexRangeParserTest {

    @Test
    public void parse_singleIndex_returnsSingleRange()
            throws Exception {
        assertEquals(List.of(single(1)), IndexRangeParser.parse("1"));
    }

    @Test
    public void parse_singleRange_returnsRange()
            throws Exception {
        assertEquals(List.of(range(2, 5)), IndexRangeParser.parse("2-5"));
    }

    @Test
    public void parse_outerWhitespace_trimsWhitespace()
            throws Exception {
        assertEquals(List.of(single(1), range(3, 5)), IndexRangeParser.parse("   1,3-5   "));
    }

    @Test
    public void parse_unsortedDisjointRanges_sortsRanges()
            throws Exception {
        assertEquals(List.of(single(1), range(3, 5), single(8)), IndexRangeParser.parse("8,3-5,1"));
    }

    @Test
    public void parse_duplicateIndexes_removesDuplicates()
            throws Exception {
        assertEquals(List.of(single(1), single(3)), IndexRangeParser.parse("3,1,3,1"));
    }

    @Test
    public void parse_overlappingRangesExtendingEnd_mergesRanges()
            throws Exception {
        assertEquals(List.of(range(1, 5)), IndexRangeParser.parse("1-3,2-5"));
    }

    @Test
    public void parse_containedRange_keepsLargestEnd()
            throws Exception {
        assertEquals(List.of(range(1, 5)), IndexRangeParser.parse("1-5,2-3"));
    }

    @Test
    public void parse_adjacentRanges_mergesRanges()
            throws Exception {
        assertEquals(List.of(range(1, 6)), IndexRangeParser.parse("4-6,1-3"));
    }

    @Test
    public void parse_complexExpression_returnsCanonicalRanges()
            throws Exception {
        assertEquals(
                List.of(range(1, 8), range(10, 14), single(20)),
                IndexRangeParser.parse("10-12,3-4,1,2-3,8,5-7," + "12-14,1,20"));
    }

    @Test
    public void parse_validExpression_returnsImmutableList()
            throws Exception {
        List<IndexRange> ranges = IndexRangeParser.parse("1,3-5");

        assertThrows(UnsupportedOperationException.class, () -> ranges.add(single(7)));
    }

    @Test
    public void parse_descendingRange_throwsParseException() {
        assertThrows(ParseException.class, () -> IndexRangeParser.parse("5-3"));
    }

    @Test
    public void parse_integerOverflow_throwsParseException() {
        assertThrows(ParseException.class, () -> IndexRangeParser.parse("2147483648"));
    }

    @Test
    public void parse_invalidExpressions_throwParseException() {
        String[] invalidExpressions = {
            "", " ", "0", "a", "1,",
            ",1", "1,,2", "1-", "-2", "1--2",
            "1 2", "1-a", "1, 3-5", "1 - 3", " 1, 3 - 5, 7 ",
            " -1-2", "1..1", "1,-2", "\"\""
        };

        for (String expression : invalidExpressions) {
            assertThrows(ParseException.class, () -> IndexRangeParser.parse(expression),
                    "Expected parsing to fail for: " + expression);
        }
    }

    @Test
    public void parse_nullExpression_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> IndexRangeParser.parse(null));
    }

    /**
     * Creates a single-index range.
     */
    private static IndexRange single(int index) {
        return IndexRange.single(Index.fromOneBased(index));
    }

    /**
     * Creates an inclusive index range.
     */
    private static IndexRange range(int start, int end) {
        return new IndexRange(Index.fromOneBased(start), Index.fromOneBased(end));
    }
}
