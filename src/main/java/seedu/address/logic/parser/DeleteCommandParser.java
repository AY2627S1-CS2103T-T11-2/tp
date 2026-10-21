package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.IndexDeleteTarget;
import seedu.address.logic.commands.IndexRange;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteCommand object
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    private static final String RANGE_SEPARATOR = "-";
    private static final String ITEM_SEPARATOR = ",";
    private static final Pattern INDEX_EXPRESSION_PATTERN =
            Pattern.compile("\\d+(?:-\\d+)?(?:,\\d+(?:-\\d+)?)*");

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand object for execution.
     *
     * @param args User-supplied command arguments
     * @return A command containing the parsed delete target
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteCommand parse(String args) throws ParseException {
        try {
            return new DeleteCommand(parseIndexTarget(args));
        } catch (ParseException | IllegalArgumentException exception) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE), exception);
        }
    }

    /**
     * Parses an index selector. Future selector keywords such as GROUP,
     * SUBJECT, and TAG can be dispatched to their own methods before this
     * method is called.
     *
     * @param args Index selector supplied by the user.
     * @return A target containing the parsed index ranges.
     * @throws ParseException If an index or range has an invalid format.
     */
    private IndexDeleteTarget parseIndexTarget(String args)
            throws ParseException {
        return new IndexDeleteTarget(IndexRangeParser.parse(args));
    }

    /**
     * Parses one index or one inclusive index range.
     *
     * @param item Text containing an index or range.
     * @return The parsed inclusive range.
     * @throws ParseException If the item has an invalid format.
     */
    private IndexRange parseRange(String item) throws ParseException {
        String[] bounds = item.split(RANGE_SEPARATOR, -1);
        if (bounds.length == 1) {
            return IndexRange.single(ParserUtil.parseIndex(bounds[0]));
        }
        if (bounds.length != 2) {
            throw new ParseException(ParserUtil.MESSAGE_INVALID_INDEX);
        }

        Index start = ParserUtil.parseIndex(bounds[0]);
        Index end = ParserUtil.parseIndex(bounds[1]);
        return new IndexRange(start, end);
    }
}
