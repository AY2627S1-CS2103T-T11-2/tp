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
import seedu.address.model.person.Name;
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
    public void parse_validNamePunctuationInKeyword_returnsFindCommand() {
        assertParseSuccess(parser, "n/Jean-Luc", commandWithName(List.of("Jean-Luc"), List.of()));
        assertParseSuccess(parser, "n/O'Connor", commandWithName(List.of("O'Connor"), List.of()));
        assertParseSuccess(parser, "n/S.", commandWithName(List.of("S."), List.of()));
        assertParseSuccess(parser, "n/S/O", commandWithName(List.of("S/O"), List.of()));
    }

    @Test
    public void parse_validPhraseArgs_returnsFindCommand() {
        FindCommand expectedFindCommand = commandWithName(List.of(), List.of("Alice Chen"));
        assertParseSuccess(parser, "n/\"Alice Chen\"", expectedFindCommand);

        assertParseSuccess(parser, " \n n/\"Alice Chen\"  \t", expectedFindCommand);

        FindCommand expectedPhraseWithRepeatedSpaces = commandWithName(List.of(), List.of("Alice   Chen"));
        assertParseSuccess(parser, "n/\"Alice   Chen\"", expectedPhraseWithRepeatedSpaces);

        assertParseSuccess(parser, "n/\"lex Ye\"", commandWithName(List.of(), List.of("lex Ye")));
        assertParseSuccess(parser, "n/\"Jean-Luc O'Connor\"",
                commandWithName(List.of(), List.of("Jean-Luc O'Connor")));
        assertParseSuccess(parser, "n/\"Jean-\"", commandWithName(List.of(), List.of("Jean-")));
        assertParseSuccess(parser, "n/\"'Connor\"", commandWithName(List.of(), List.of("'Connor")));
        assertParseSuccess(parser, "n/\". Jay\"", commandWithName(List.of(), List.of(". Jay")));
    }

    @Test
    public void parse_phraseWithRelationshipQualifier_returnsFindCommand() {
        assertParseSuccess(parser, "n/\"Ravi S/O Kumar\"", commandWithName(List.of(), List.of("Ravi S/O Kumar")));
        assertParseSuccess(parser, "n/\"ravi s/o kumar\"",
                commandWithName(List.of(), List.of("ravi s/o kumar")));
        assertParseSuccess(parser, "n/s/o", commandWithName(List.of("s/o"), List.of()));
        assertParseSuccess(parser, "n/\"Binte D/O Rahmud\"", commandWithName(List.of(), List.of("Binte D/O Rahmud")));
        assertParseFailure(parser, "g/T11 S/O", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validMixedArgs_returnsFindCommand() {
        FindCommand expectedFindCommand = commandWithName(List.of("Tan"), List.of("Alice Chen"));
        assertParseSuccess(parser, "n/\"Alice Chen\",Tan", expectedFindCommand);
        assertParseSuccess(parser, "n/Tan,\"Alice Chen\"", expectedFindCommand);

        FindCommand substringAndKeywordCommand = commandWithName(List.of("Hans"), List.of("Han"));
        assertParseSuccess(parser, "n/\"Han\",Hans", substringAndKeywordCommand);

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
        assertParseFailure(parser, "n/!!!", expectedMessage);
        assertParseFailure(parser, "n/@", expectedMessage);
        assertParseFailure(parser, "n/-", expectedMessage);
        assertParseFailure(parser, "n/abc$", expectedMessage);
        assertParseFailure(parser, "n/\"!!!\"", expectedMessage);
        assertParseFailure(parser, "n/\"@\"", expectedMessage);
        assertParseFailure(parser, "n/\"-\"", expectedMessage);
        assertParseFailure(parser, "n/\"abc/def\"", expectedMessage);
        assertParseFailure(parser, "n/\"Alice x/typo\"", expectedMessage);
        assertParseFailure(parser, "n/\"a---b\"", expectedMessage);
        assertParseFailure(parser, "n/\"a...b\"", expectedMessage);
        assertParseFailure(parser, "n/\"Alice . Bob\"", expectedMessage);
        assertParseFailure(parser, "n/\"' Connor\"", expectedMessage);
        assertParseFailure(parser, "n/\"Jean -\"", expectedMessage);
        assertParseFailure(parser, "n/\"Alice S/O Bob D/O Carol\"", expectedMessage);
        assertParseFailure(parser, "n/" + "a".repeat(Name.MAX_LENGTH + 1), expectedMessage);
        assertParseFailure(parser, "n/\"" + "a".repeat(Name.MAX_LENGTH + 1) + "\"", expectedMessage);
    }

    @Test
    public void parse_duplicateNamePrefix_throwsParseException() {
        assertParseFailure(parser, "n/Alice n/Bob",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
    }

    @Test
    public void parse_unknownPrefix_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "g/T11 x/typo", expectedMessage);
        assertParseFailure(parser, "g/T11 x/", expectedMessage);
        assertParseFailure(parser, "n/Alice h/@alice_tan s/CS3434 g/T11 t/consultation x/typo",
                expectedMessage);
    }

    @Test
    public void parse_slashesThatAreNotPrefixes_returnsFindCommand() {
        FindCommand slashGroupCommand = new FindCommand(new FindPredicate(
                Optional.empty(), Optional.empty(), Optional.of(new Group("T11/T12")), Set.of()));
        assertParseSuccess(parser, "g/T11/T12", slashGroupCommand);

        FindCommand literalTextCommand = new FindCommand(new FindPredicate(
                Optional.empty(), Optional.empty(), Optional.of(new Group("T11 x / typo")), Set.of()));
        assertParseSuccess(parser, "g/T11 x / typo", literalTextCommand);

        FindCommand urlCommand = new FindCommand(new FindPredicate(
                Optional.empty(), Optional.empty(), Optional.of(new Group("https://example.com/T11")), Set.of()));
        assertParseSuccess(parser, "g/https://example.com/T11", urlCommand);
    }

    private FindCommand commandWithName(List<String> keywords, List<String> phrases) {
        return new FindCommand(new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(keywords, phrases)),
                Optional.empty(), Optional.empty(), Set.of()));
    }

}
