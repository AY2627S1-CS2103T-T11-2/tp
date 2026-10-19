package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.IndexRange;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses expressions containing displayed indexes and inclusive index ranges.
 *
 * <p>The parsed ranges are returned in a canonical form: sorted,
 * non-overlapping, non-adjacent, and free of duplicates.
 */
public final class IndexRangeParser {

    private static final String RANGE_SEPARATOR = "-";
    private static final String ITEM_SEPARATOR = ",";

    private static final Pattern INDEX_EXPRESSION_PATTERN =
            Pattern.compile("\\d+(?:-\\d+)?(?:,\\d+(?:-\\d+)?)*");

    private IndexRangeParser() {
    }

    /**
     * Parses and canonicalizes a displayed-index expression.
     *
     * <p>For example, {@code 3-4,1,2-3,1,6,5} is returned as one
     * inclusive range from 1 to 6.
     *
     * <p>Leading and trailing whitespace around the complete expression is
     * ignored. Whitespace within the expression is not allowed.
     *
     * <p>This method does not determine whether the selected indexes exist in
     * the currently displayed list. That validation must occur when the
     * command executes.
     *
     * @param expression comma-separated indexes and inclusive ranges
     * @return immutable, canonical list of index ranges
     * @throws NullPointerException if {@code expression} is null
     * @throws ParseException if {@code expression} is invalid
     */
    public static List<IndexRange> parse(String expression)
            throws ParseException {
        requireNonNull(expression);

        String trimmedExpression = expression.trim();

        if (trimmedExpression.isEmpty()
                || !INDEX_EXPRESSION_PATTERN
                        .matcher(trimmedExpression)
                        .matches()) {
            throw new ParseException(ParserUtil.MESSAGE_INVALID_INDEX);
        }

        List<IndexRange> parsedRanges = new ArrayList<>();

        for (String item
                : trimmedExpression.split(ITEM_SEPARATOR, -1)) {
            parsedRanges.add(parseRange(item));
        }

        return normalize(parsedRanges);
    }

    /**
     * Parses one individual index or one inclusive range.
     */
    private static IndexRange parseRange(String item)
            throws ParseException {
        int separatorPosition = item.indexOf(RANGE_SEPARATOR);

        if (separatorPosition < 0) {
            return IndexRange.single(ParserUtil.parseIndex(item));
        }

        String startText = item.substring(0, separatorPosition);
        String endText = item.substring(separatorPosition + 1);

        Index start = ParserUtil.parseIndex(startText);
        Index end = ParserUtil.parseIndex(endText);

        try {
            return new IndexRange(start, end);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(
                    ParserUtil.MESSAGE_INVALID_INDEX,
                    exception);
        }
    }

    /**
     * Sorts and merges duplicate, overlapping, and adjacent ranges.
     */
    private static List<IndexRange> normalize(
            List<IndexRange> ranges) {
        List<IndexRange> sortedRanges =
                new ArrayList<>(ranges);

        sortedRanges.sort(Comparator.comparingInt(
                range -> range.start().getZeroBased()));

        List<IndexRange> normalizedRanges =
                new ArrayList<>();

        for (IndexRange next : sortedRanges) {
            if (normalizedRanges.isEmpty()) {
                normalizedRanges.add(next);
                continue;
            }

            int lastPosition = normalizedRanges.size() - 1;
            IndexRange last =
                    normalizedRanges.get(lastPosition);

            long nextStart =
                    next.start().getZeroBased();
            long lastEnd =
                    last.end().getZeroBased();

            if (nextStart <= lastEnd + 1) {
                Index mergedEnd =
                        next.end().getZeroBased()
                                > last.end().getZeroBased()
                                ? next.end()
                                : last.end();

                normalizedRanges.set(
                        lastPosition,
                        new IndexRange(
                                last.start(),
                                mergedEnd));
            } else {
                normalizedRanges.add(next);
            }
        }

        return List.copyOf(normalizedRanges);
    }
}
