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
import seedu.address.model.person.Name;
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
    private static final String NAME_WORD_PATTERN = "[\\p{Alnum}]+(?:[-'][\\p{Alnum}]+)*";
    private static final String NAME_ABBREVIATION_PATTERN = "[\\p{Alpha}]{1,3}\\.";
    private static final String NAME_RELATIONSHIP_QUALIFIER_PATTERN = "[DS]/O";
    private static final String NAME_SEARCH_TOKEN_PATTERN = "(?:" + NAME_WORD_PATTERN + "|"
            + NAME_ABBREVIATION_PATTERN + "|" + NAME_RELATIONSHIP_QUALIFIER_PATTERN + ")";
    private static final Pattern NAME_KEYWORD_PATTERN = Pattern.compile(NAME_SEARCH_TOKEN_PATTERN,
            Pattern.CASE_INSENSITIVE);
    private static final Pattern NAME_PHRASE_PATTERN = Pattern.compile(
            NAME_SEARCH_TOKEN_PATTERN + "(?:\\s+" + NAME_SEARCH_TOKEN_PATTERN + ")*",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern NAME_ALPHANUMERIC_CHARACTER_PATTERN = Pattern.compile("\\p{Alnum}");
    private static final Pattern NAME_RELATIONSHIP_QUALIFIER_TOKEN_PATTERN = Pattern.compile(
            "(?<!\\S)" + NAME_RELATIONSHIP_QUALIFIER_PATTERN + "(?!\\S)", Pattern.CASE_INSENSITIVE);
    private static final List<Prefix> FIND_PREFIXES = List.of(PREFIX_NAME, PREFIX_TELEGRAM_HANDLE,
            PREFIX_SUBJECT, PREFIX_GROUP, PREFIX_TAG);

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenizeIgnoringQuotedPrefixes(
                " " + trimmedArgs, FIND_PREFIXES.toArray(Prefix[]::new));

        if (trimmedArgs.isEmpty()
                || ArgumentTokenizer.containsUnknownPrefixIgnoringQuotedPrefixes(
                        trimmedArgs, FIND_PREFIXES.toArray(Prefix[]::new))) {
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
                String normalizedPhrase = phrase.trim().replaceAll("\\s+", " ");
                if (!isValidNamePhrase(normalizedPhrase)) {
                    throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
                }
                phrases.add(phrase);
            } else {
                String keyword = matcher.group(2);
                if (keyword.length() > Name.MAX_LENGTH || !NAME_KEYWORD_PATTERN.matcher(keyword).matches()) {
                    throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
                }
                keywords.add(keyword);
            }
        }

        return new NameContainsSearchTermsPredicate(keywords, phrases);
    }

    /**
     * Returns whether the phrase can occur as a literal substring of a valid name.
     * A leading period or a boundary hyphen or apostrophe can be completed by characters outside the phrase.
     */
    private boolean isValidNamePhrase(String phrase) {
        if (phrase.isEmpty() || phrase.length() > Name.MAX_LENGTH
                || NAME_RELATIONSHIP_QUALIFIER_TOKEN_PATTERN.matcher(phrase).results().count() > 1) {
            return false;
        }

        String phraseCore = phrase;
        char firstCharacter = phrase.charAt(0);
        if (firstCharacter == '.') {
            if (phrase.length() < 2 || !Character.isWhitespace(phrase.charAt(1))) {
                return false;
            }
            phraseCore = phraseCore.substring(1).stripLeading();
        } else if (firstCharacter == '-' || firstCharacter == '\'') {
            if (phrase.length() < 2 || !isNameAlphanumeric(phrase.charAt(1))) {
                return false;
            }
            phraseCore = phraseCore.substring(1);
        }

        char lastCharacter = phrase.charAt(phrase.length() - 1);
        if (lastCharacter == '-' || lastCharacter == '\'') {
            if (phrase.length() < 2 || !isNameAlphanumeric(phrase.charAt(phrase.length() - 2))) {
                return false;
            }
            phraseCore = phraseCore.substring(0, phraseCore.length() - 1);
        }

        return !phraseCore.isEmpty() && NAME_PHRASE_PATTERN.matcher(phraseCore).matches();
    }

    private boolean isNameAlphanumeric(char character) {
        return NAME_ALPHANUMERIC_CHARACTER_PATTERN.matcher(String.valueOf(character)).matches();
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
