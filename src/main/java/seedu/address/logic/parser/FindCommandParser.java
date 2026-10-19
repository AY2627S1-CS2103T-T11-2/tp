package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

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
import seedu.address.model.person.Group;
import seedu.address.model.person.NameContainsSearchTermsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {
    private static final Pattern SEARCH_TERM_PATTERN = Pattern.compile("\\\"([^\\\"]*)\\\"|([^\\s\\\"]+)");

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(
                " " + trimmedArgs, PREFIX_SUBJECT, PREFIX_GROUP, PREFIX_TAG);

        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        Optional<String> subjectValue = argMultimap.getValue(PREFIX_SUBJECT);
        Optional<String> groupValue = argMultimap.getValue(PREFIX_GROUP);
        boolean hasSubject = subjectValue.isPresent();
        boolean hasGroup = groupValue.isPresent();
        boolean hasTag = !argMultimap.getAllValues(PREFIX_TAG).isEmpty();

        if (hasSubject || hasGroup || hasTag) {
            return parseCriteria(argMultimap, subjectValue, groupValue);
        }

        return new FindCommand(parseNamePredicate(trimmedArgs));
    }

    /**
     * Parses name terms consisting of keywords and quoted phrases.
     */
    private Predicate<Person> parseNamePredicate(String nameInput) throws ParseException {
        List<String> keywords = new ArrayList<>();
        List<String> phrases = new ArrayList<>();
        String trimmedNameInput = nameInput.trim();
        Matcher matcher = SEARCH_TERM_PATTERN.matcher(trimmedNameInput);
        int previousMatchEnd = 0;

        while (matcher.find()) {
            if (!trimmedNameInput.substring(previousMatchEnd, matcher.start()).isBlank()) {
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
            previousMatchEnd = matcher.end();
        }
        if (!trimmedNameInput.substring(previousMatchEnd).isBlank()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        return new NameContainsSearchTermsPredicate(keywords, phrases);
    }

    /**
     * Parses optional name, subject, group, and tag filters.
     * A person must match every supplied criterion type; multiple tag filters use OR.
     */
    private FindCommand parseCriteria(ArgumentMultimap argMultimap, Optional<String> subjectValue,
                                      Optional<String> groupValue) throws ParseException {
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_SUBJECT, PREFIX_GROUP);

        Optional<Predicate<Person>> namePredicate = Optional.empty();
        if (!argMultimap.getPreamble().isEmpty()) {
            namePredicate = Optional.of(parseNamePredicate(argMultimap.getPreamble()));
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
        Set<Tag> tags = ParserUtil.parseTags(tagValues);

        return new FindCommand(new FindPredicate(namePredicate, subject, group, tags));
    }
}
