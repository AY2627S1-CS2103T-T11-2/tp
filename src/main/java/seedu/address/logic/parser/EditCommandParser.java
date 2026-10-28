package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEGACY_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TELEGRAM_HANDLE;

import java.util.List;

import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.IndexPersonTarget;
import seedu.address.logic.commands.IndexRange;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new EditCommand object
 */
public class EditCommandParser implements Parser<EditCommand> {

    private static final List<Prefix> EDIT_PREFIXES = List.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
            PREFIX_TELEGRAM_HANDLE, PREFIX_SUBJECT, PREFIX_GROUP, PREFIX_TAG, PREFIX_LEGACY_ADDRESS);

    /**
     * Parses the given {@code String} of arguments in the context of the EditCommand
     * and returns an EditCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, EDIT_PREFIXES.toArray(Prefix[]::new));

        List<IndexRange> ranges;

        try {
            ranges = IndexRangeParser.parse(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE), pe);
        }

        if (ArgumentTokenizer.containsUnknownPrefix(args, EDIT_PREFIXES.toArray(Prefix[]::new))) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }

        if (argMultimap.getValue(PREFIX_LEGACY_ADDRESS).isPresent()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_TELEGRAM_HANDLE,
                PREFIX_SUBJECT, PREFIX_GROUP);

        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();

        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            editPersonDescriptor.setName(ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get()));
        }
        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            editPersonDescriptor.setPhone(ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get()));
        }
        if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            editPersonDescriptor.setEmail(ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get()));
        }
        if (argMultimap.getValue(PREFIX_TELEGRAM_HANDLE).isPresent()) {
            editPersonDescriptor.setTelegramHandle(
                    ParserUtil.parseTelegramHandle(argMultimap.getValue(PREFIX_TELEGRAM_HANDLE).get()));
        }
        if (argMultimap.getValue(PREFIX_SUBJECT).isPresent()) {
            editPersonDescriptor.setSubject(ParserUtil.parseSubject(argMultimap.getValue(PREFIX_SUBJECT).get()));
        }
        if (argMultimap.getValue(PREFIX_GROUP).isPresent()) {
            editPersonDescriptor.setGroup(ParserUtil.parseGroup(argMultimap.getValue(PREFIX_GROUP).get()));
        }


        parseTagsForEdit(argMultimap.getAllValues(PREFIX_TAG), editPersonDescriptor);

        if (!editPersonDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        IndexPersonTarget target = new IndexPersonTarget(ranges);

        if (target.selectsSingleIndex()) {
            return new EditCommand(ranges.getFirst().start(), editPersonDescriptor);
        }

        if (editPersonDescriptor.hasSinglePersonOnlyFields()) {
            throw new ParseException(EditCommand.MESSAGE_INVALID_BULK_EDIT);
        }

        return new EditCommand(target, editPersonDescriptor);
    }

    /**
     * Parses tags in entry order. Each empty prefix requests removal of one tag from the end.
     * Empty and non-empty values cannot be mixed.
     */
    private void parseTagsForEdit(List<String> tags, EditPersonDescriptor descriptor) throws ParseException {
        assert tags != null;

        if (tags.isEmpty()) {
            return;
        }
        if (tags.stream().allMatch(String::isEmpty)) {
            descriptor.setTagRemovalCount(tags.size());
        } else {
            descriptor.setTags(ParserUtil.parseTags(tags));
        }
    }

}
