package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TELEGRAM_HANDLE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_TAG_FILTER;
import static seedu.address.model.person.FindPredicate.TagMatchMode.ALL;
import static seedu.address.model.person.FindPredicate.TagMatchMode.ANY;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.FindPredicate;
import seedu.address.model.person.Group;
import seedu.address.model.person.NameContainsSearchTermsPredicate;
import seedu.address.model.person.Subject;
import seedu.address.model.person.TelegramHandle;
import seedu.address.model.tag.Tag;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validKeywordArgs_returnsFindCommand() {
        FindCommand expectedFindCommand = commandWithName(List.of("Alice", "Bob"), List.of());
        assertParseSuccess(parser, "n/Alice,Bob", expectedFindCommand);

        // Whitespace around commas and the complete criterion is ignored.
        assertParseSuccess(parser, " \n n/Alice, \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_validPhraseArgs_returnsFindCommand() {
        FindCommand expectedFindCommand = commandWithName(List.of(), List.of("Alice Chen"));
        assertParseSuccess(parser, "n/\"Alice Chen\"", expectedFindCommand);

        assertParseSuccess(parser, " \n n/\"Alice Chen\"  \t", expectedFindCommand);

        FindCommand expectedPhraseWithRepeatedSpaces = commandWithName(List.of(), List.of("Alice   Chen"));
        assertParseSuccess(parser, "n/\"Alice   Chen\"", expectedPhraseWithRepeatedSpaces);
    }

    @Test
    public void parse_validMixedArgs_returnsFindCommand() {
        FindCommand expectedFindCommand = commandWithName(List.of("Tan"), List.of("Alice Chen"));
        assertParseSuccess(parser, "n/\"Alice Chen\",Tan", expectedFindCommand);
        assertParseSuccess(parser, "n/Tan,\"Alice Chen\"", expectedFindCommand);

        FindCommand expectedMultiplePhrases = commandWithName(List.of(), List.of("Alice Chen", "Bob Tan"));
        assertParseSuccess(parser, "n/\"Alice Chen\",\"Bob Tan\"", expectedMultiplePhrases);
    }

    @Test
    public void parse_subjectAndGroupFilters_returnsFindCommand() {
        FindCommand subjectCommand = new FindCommand(new FindPredicate(
                Optional.empty(), Optional.of(new Subject("Mathematics")), Optional.empty(), Set.of()));
        assertParseSuccess(parser, "s/Mathematics", subjectCommand);

        FindCommand groupCommand = new FindCommand(new FindPredicate(
                Optional.empty(), Optional.empty(), Optional.of(new Group("G01")), Set.of()));
        assertParseSuccess(parser, "g/G01", groupCommand);

        FindCommand combinedCommand = new FindCommand(new FindPredicate(
                Optional.empty(), Optional.of(new Subject("Computer Science")), Optional.of(new Group("G02")),
                Set.of()));
        assertParseSuccess(parser, "s/Computer Science g/G02", combinedCommand);

        FindCommand phraseAndSubjectCommand = new FindCommand(new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(List.of(), List.of("john"))),
                Optional.of(new Subject("CS3434")), Optional.empty(), Set.of()));
        assertParseSuccess(parser, "n/\"john\" s/CS3434", phraseAndSubjectCommand);

        FindCommand keywordsAndAllFiltersCommand = new FindCommand(new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(List.of("John", "Jane"), List.of())),
                Optional.of(new Subject("CS3434")), Optional.of(new Group("T11")), Set.of()));
        assertParseSuccess(parser, "n/John,Jane s/CS3434 g/T11", keywordsAndAllFiltersCommand);
    }

    @Test
    public void parse_tagFilters_returnsFindCommand() {
        FindCommand tagCommand = new FindCommand(new FindPredicate(
                Optional.empty(), Optional.empty(), Optional.empty(), Set.of(new Tag("highParticipation"))));
        assertParseSuccess(parser, "t/highParticipation", tagCommand);

        FindCommand anyTagsCommand = new FindCommand(new FindPredicate(
                Optional.empty(), Optional.empty(), Optional.empty(),
                Set.of(new Tag("lowAttendance"), new Tag("needsHelp")), ANY));
        assertParseSuccess(parser, "t/lowAttendance,needsHelp", anyTagsCommand);

        FindCommand allTagsCommand = new FindCommand(new FindPredicate(
                Optional.empty(), Optional.empty(), Optional.empty(),
                Set.of(new Tag("lowAttendance"), new Tag("needsHelp")), ALL));
        assertParseSuccess(parser, "t/lowAttendance&needsHelp", allTagsCommand);

        FindCommand allFiltersCommand = new FindCommand(new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(List.of("John"), List.of())),
                Optional.of(new Subject("CS3434")), Optional.of(new Group("T11")),
                Set.of(new Tag("consultation"))));
        assertParseSuccess(parser, "n/John s/CS3434 g/T11 t/consultation", allFiltersCommand);
    }

    @Test
    public void parse_telegramHandleFilter_returnsFindCommand() {
        FindCommand telegramCommand = new FindCommand(new FindPredicate(
                Optional.empty(), Optional.of(new TelegramHandle("@alice_tan")),
                Optional.empty(), Optional.empty(), Set.of()));

        assertParseSuccess(parser, "h/@alice_tan", telegramCommand);
    }

    @Test
    public void parse_telegramHandleCombinedWithOtherFilters_returnsFindCommand() {
        FindCommand combinedCommand = new FindCommand(new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(List.of("Alice"), List.of())),
                Optional.of(new TelegramHandle("@alice_tan")), Optional.of(new Subject("CS3434")),
                Optional.of(new Group("T11")), Set.of(new Tag("consultation"))));

        assertParseSuccess(parser, "n/Alice h/@alice_tan s/CS3434 g/T11 t/consultation", combinedCommand);
    }

    @Test
    public void parse_invalidSubjectGroupAndTagFilters_throwsParseException() {
        assertParseFailure(parser, "s/", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "g/", Group.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "t/", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "t/lowAttendance,needsHelp&consultation", MESSAGE_INVALID_TAG_FILTER);
        assertParseFailure(parser, "t/lowAttendance t/needsHelp",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_TAG));
    }

    @Test
    public void parse_invalidTelegramHandleFilter_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "h/", expectedMessage);
        assertParseFailure(parser, "h/alice_tan", TelegramHandle.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "h/@alice_tan h/@bob_tan",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_TELEGRAM_HANDLE));
    }

    @Test
    public void parse_phraseArgWithoutFullQuotations_throwsParseException() {
        String expectedMessage =
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "n/\"chloe seow", expectedMessage);
        assertParseFailure(parser, "n/chloe seow\"", expectedMessage);
        assertParseFailure(parser, "n/chl\"oe", expectedMessage);
        assertParseFailure(parser, "n/\"\"chloe seow\"\"", expectedMessage);
        assertParseFailure(parser, "n/\"\"", expectedMessage);
        assertParseFailure(parser, "n/\"", expectedMessage);
    }

    @Test
    public void parse_invalidNameExpression_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "n/", expectedMessage);
        assertParseFailure(parser, "n/Alice,", expectedMessage);
        assertParseFailure(parser, "n/,Alice", expectedMessage);
        assertParseFailure(parser, "n/Alice,,Bob", expectedMessage);
        assertParseFailure(parser, "n/Alice Bob", expectedMessage);
        assertParseFailure(parser, "Alice", expectedMessage);
    }

    @Test
    public void parse_duplicateNamePrefix_throwsParseException() {
        assertParseFailure(parser, "n/Alice n/Bob",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
    }

    private FindCommand commandWithName(List<String> keywords, List<String> phrases) {
        return new FindCommand(new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(keywords, phrases)),
                Optional.empty(), Optional.empty(), Set.of()));
    }

}
