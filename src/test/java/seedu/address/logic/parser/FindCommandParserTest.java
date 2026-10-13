package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.FindPredicate;
import seedu.address.model.person.Group;
import seedu.address.model.person.NameContainsSearchTermsPredicate;
import seedu.address.model.person.Subject;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validKeywordArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsSearchTermsPredicate(List.of("Alice", "Bob"), List.of()));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_validPhraseArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsSearchTermsPredicate(List.of(), List.of("Alice Chen")));
        assertParseSuccess(parser, "\"Alice Chen\"", expectedFindCommand);

        // surrounding whitespace is ignored
        assertParseSuccess(parser, " \n \"Alice Chen\"  \t", expectedFindCommand);

        FindCommand expectedPhraseWithRepeatedSpaces =
                new FindCommand(new NameContainsSearchTermsPredicate(List.of(), List.of("Alice   Chen")));
        assertParseSuccess(parser, "\"Alice   Chen\"", expectedPhraseWithRepeatedSpaces);
    }

    @Test
    public void parse_validMixedArgs_returnsFindCommand() {
        FindCommand expectedFindCommand = new FindCommand(
                new NameContainsSearchTermsPredicate(List.of("Tan"), List.of("Alice Chen")));
        assertParseSuccess(parser, "\"Alice Chen\" Tan", expectedFindCommand);
        assertParseSuccess(parser, "Tan \"Alice Chen\"", expectedFindCommand);

        FindCommand expectedMultiplePhrases = new FindCommand(
                new NameContainsSearchTermsPredicate(List.of(), List.of("Alice Chen", "Bob Tan")));
        assertParseSuccess(parser, "\"Alice Chen\" \"Bob Tan\"", expectedMultiplePhrases);
    }

    @Test
    public void parse_subjectAndGroupFilters_returnsFindCommand() {
        FindCommand subjectCommand = new FindCommand(new FindPredicate(
                Optional.empty(), Optional.of(new Subject("Mathematics")), Optional.empty()));
        assertParseSuccess(parser, "s/Mathematics", subjectCommand);

        FindCommand groupCommand = new FindCommand(new FindPredicate(
                Optional.empty(), Optional.empty(), Optional.of(new Group("G01"))));
        assertParseSuccess(parser, "g/G01", groupCommand);

        FindCommand combinedCommand = new FindCommand(new FindPredicate(
                Optional.empty(), Optional.of(new Subject("Computer Science")), Optional.of(new Group("G02"))));
        assertParseSuccess(parser, "s/Computer Science g/G02", combinedCommand);

        FindCommand phraseAndSubjectCommand = new FindCommand(new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(List.of(), List.of("john"))),
                Optional.of(new Subject("CS3434")), Optional.empty()));
        assertParseSuccess(parser, "\"john\" s/CS3434", phraseAndSubjectCommand);

        FindCommand keywordsAndAllFiltersCommand = new FindCommand(new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(List.of("John", "Jane"), List.of())),
                Optional.of(new Subject("CS3434")), Optional.of(new Group("T11"))));
        assertParseSuccess(parser, "John Jane s/CS3434 g/T11", keywordsAndAllFiltersCommand);
    }

    @Test
    public void parse_invalidSubjectAndGroupFilters_throwsParseException() {
        assertParseFailure(parser, "s/", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "g/", Group.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_phraseArgWithoutFullQuotations_throwsParseException() {
        String expectedMessage =
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "\"chloe seow", expectedMessage);
        assertParseFailure(parser, "chloe seow\"", expectedMessage);
        assertParseFailure(parser, "chloe \"seow", expectedMessage);
        assertParseFailure(parser, "chl\"oe", expectedMessage);
        assertParseFailure(parser, "\"\"chloe seow\"\"", expectedMessage);
        assertParseFailure(parser, "\"\"", expectedMessage);
        assertParseFailure(parser, "\"", expectedMessage);
    }

}
