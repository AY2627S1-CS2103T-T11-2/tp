package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TELEGRAM_HANDLE;

import java.util.List;
import java.util.Set;

import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeleteCommand.FieldsToDelete;
import seedu.address.logic.commands.IndexPersonTarget;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.tag.Tag;

/**
 * Parses whole-contact deletions or optional-field deletions.
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    /**
     * Parses displayed indexes and optional field prefixes. Compulsory and unknown prefixes are rejected.
     *
     * @throws ParseException if the arguments or field values are invalid
     */
    public DeleteCommand parse(String args) throws ParseException {
        Prefix[] prefixes = {PREFIX_PHONE, PREFIX_TELEGRAM_HANDLE, PREFIX_TAG};
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, prefixes);
        IndexPersonTarget target;
        try {
            target = new IndexPersonTarget(IndexRangeParser.parse(arguments.getPreamble()));
        } catch (ParseException | IllegalArgumentException exception) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE), exception);
        }

        if (ArgumentTokenizer.containsUnknownPrefix(args, prefixes)
                || arguments.getValue(PREFIX_PHONE).filter(value -> !value.isEmpty()).isPresent()
                || arguments.getValue(PREFIX_TELEGRAM_HANDLE).filter(value -> !value.isEmpty()).isPresent()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_PHONE, PREFIX_TELEGRAM_HANDLE);

        boolean phone = arguments.getValue(PREFIX_PHONE).isPresent();
        boolean telegram = arguments.getValue(PREFIX_TELEGRAM_HANDLE).isPresent();
        List<String> tagValues = arguments.getAllValues(PREFIX_TAG);
        if (!phone && !telegram && tagValues.isEmpty()) {
            return new DeleteCommand(target);
        }
        boolean allTags = tagValues.contains("");
        Set<Tag> tags = ParserUtil.parseTags(tagValues.stream().filter(value -> !value.isEmpty()).toList());
        return new DeleteCommand(target, new FieldsToDelete(phone, telegram, allTags, tags));
    }
}
