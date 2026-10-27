package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TELEGRAM_HANDLE;
import static seedu.address.model.person.FindPredicate.TagMatchMode.ALL;
import static seedu.address.model.person.FindPredicate.TagMatchMode.ANY;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FindPredicate;
import seedu.address.model.person.FindPredicate.TagMatchMode;
import seedu.address.model.person.Group;
import seedu.address.model.person.NameContainsSearchTermsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.model.person.TelegramHandle;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {
    private static final Pattern NAME_TERM_PATTERN = Pattern.compile("\"([^\"]*)\"|([^\\s\",]+)");

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(
                " " + trimmedArgs, PREFIX_NAME, PREFIX_TELEGRAM_HANDLE, PREFIX_SUBJECT, PREFIX_GROUP, PREFIX_TAG);

        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        Optional<String> nameValue = argMultimap.getValue(PREFIX_NAME);
        Optional<String> telegramHandleValue = argMultimap.getValue(PREFIX_TELEGRAM_HANDLE);
        Optional<String> subjectValue = argMultimap.getValue(PREFIX_SUBJECT);
        Optional<String> groupValue = argMultimap.getValue(PREFIX_GROUP);
        boolean hasName = nameValue.isPresent();
        boolean hasTelegramHandle = telegramHandleValue.isPresent();
        boolean hasSubject = subjectValue.isPresent();
        boolean hasGroup = groupValue.isPresent();
        boolean hasTag = !argMultimap.getAllValues(PREFIX_TAG).isEmpty();

        if (!hasName && !hasTelegramHandle && !hasSubject && !hasGroup && !hasTag) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        return parseCriteria(argMultimap, nameValue, telegramHandleValue, subjectValue, groupValue);
    }

    /**
     * Parses comma-separated name terms consisting of keywords and quoted phrases.
     */
    private Predicate<Person> parseNamePredicate(String nameInput) throws ParseException {
        List<String> keywords = new ArrayList<>();
        List<String> phrases = new ArrayList<>();
        String[] terms = nameInput.split(",", -1);

        for (String term : terms) {
            Matcher matcher = NAME_TERM_PATTERN.matcher(term.trim());
            if (!matcher.matches()) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }
            String phrase = matcher.group(1);
            if (phrase != null) {
                if (phrase.isBlank()) {
                    throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
                }
                phrases.add(phrase);
            } else {
                keywords.add(matcher.group(2));
            }
        }

        return new NameContainsSearchTermsPredicate(keywords, phrases);
    }

    /**
     * Parses optional name, Telegram handle, subject, group, and tag filters.
     * A person must match every supplied criterion type. Tags separated by commas use OR, while tags separated by
     * ampersands use AND.
     */
    private FindCommand parseCriteria(ArgumentMultimap argMultimap, Optional<String> nameValue,
                                      Optional<String> telegramHandleValue, Optional<String> subjectValue,
                                      Optional<String> groupValue)
            throws ParseException {
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_TELEGRAM_HANDLE, PREFIX_SUBJECT, PREFIX_GROUP,
                PREFIX_TAG);

        Optional<Predicate<Person>> namePredicate = Optional.empty();
        if (nameValue.isPresent()) {
            namePredicate = Optional.of(parseNamePredicate(nameValue.get()));
        }

        Optional<TelegramHandle> telegramHandle = Optional.empty();
        if (telegramHandleValue.isPresent()) {
            String value = telegramHandleValue.get();
            if (value.isBlank()) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }
            telegramHandle = Optional.of(ParserUtil.parseTelegramHandle(value));
        }

        Optional<Subject> subject = Optional.empty();
        if (subjectValue.isPresent()) {
            subject = Optional.of(ParserUtil.parseSubject(subjectValue.get()));
        }

        Optional<Group> group = Optional.empty();
        if (groupValue.isPresent()) {
            group = Optional.of(ParserUtil.parseGroup(groupValue.get()));
        }

        List<String> tagValues = argMultimap.getAllValues(PREFIX_TAG);
        Set<Tag> tags = Set.of();
        TagMatchMode tagMatchMode = ANY;
        if (!tagValues.isEmpty()) {
            String tagFilter = tagValues.getFirst();
            tags = ParserUtil.parseTagsForFind(tagFilter);
            tagMatchMode = tagFilter.contains("&") ? ALL : ANY;
        }

        return new FindCommand(new FindPredicate(namePredicate, telegramHandle, subject, group, tags, tagMatchMode));
    }
}
