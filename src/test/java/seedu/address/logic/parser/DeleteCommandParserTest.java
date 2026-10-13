package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.IndexDeleteTarget;
import seedu.address.logic.commands.IndexRange;

/**
 * Contains unit tests for {@link DeleteCommandParser}.
 */
public class DeleteCommandParserTest {

    private DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteCommand() {
        assertParseSuccess(parser, "1", new DeleteCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_commaSeparatedIndexes_returnsDeleteCommand() {
        DeleteCommand expected = commandFor(IndexRange.single(Index.fromOneBased(1)),
                IndexRange.single(Index.fromOneBased(3)));

        assertParseSuccess(parser, "1,3", expected);
    }

    @Test
    public void parse_indexRange_returnsDeleteCommand() {
        DeleteCommand expected = commandFor(new IndexRange(Index.fromOneBased(2), Index.fromOneBased(5)));

        assertParseSuccess(parser, "2-5", expected);
    }

    @Test
    public void parse_duplicateAndOverlappingRanges_returnsNormalizedCommand() {
        DeleteCommand expected = commandFor(new IndexRange(Index.fromOneBased(1), Index.fromOneBased(4)));

        assertParseSuccess(parser, "3-4,1,2-3,1", expected);
    }

    @Test
    public void parse_mixedIndexesAndRanges_returnsDeleteCommand() {
        DeleteCommand expected = commandFor(
                IndexRange.single(Index.fromOneBased(1)),
                        new IndexRange(Index.fromOneBased(3), Index.fromOneBased(5)),
                IndexRange.single(Index.fromOneBased(7)));

        assertParseSuccess(parser, " 1,3-5,7 ", expected);
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        String expectedError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);

        String[] invalidInputs = {
            "", "0", "a", "1,", ",1", "1,,2", "1-", "-2",
            "1--2", "3-1", "1 2", "1-a", "2147483648", " 1, 3 - 5, 7 ", "\"\""
        };

        for (String input : invalidInputs) {
            assertParseFailure(parser, input, expectedError);
        }
    }

    @Test
    public void parse_leadingAndTrailingWhitespace_returnsDeleteCommand() {
        DeleteCommand expected = commandFor(IndexRange.single(Index.fromOneBased(1)),
                new IndexRange(
                        Index.fromOneBased(3),
                        Index.fromOneBased(5)));

        assertParseSuccess(parser, "   1,3-5   ", expected);
    }

    @Test
    public void parse_spaceAfterComma_throwsParseException() {
        String expectedMessage = String.format(
                MESSAGE_INVALID_COMMAND_FORMAT,
                DeleteCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "1, 3-5,7", expectedMessage);
    }

    /**
     * Creates a delete command for the supplied index ranges.
     *
     * @param ranges Ranges the command should delete.
     * @return A delete command containing the ranges.
     */
    private DeleteCommand commandFor(IndexRange... ranges) {
        return new DeleteCommand(new IndexDeleteTarget(List.of(ranges)));
    }
}
