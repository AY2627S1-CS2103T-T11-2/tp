package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FindPredicate;
import seedu.address.model.person.Group;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.NameContainsPhrasePredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" " + trimmedArgs, PREFIX_SUBJECT, PREFIX_GROUP);

        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        Optional<String> subjectValue = argMultimap.getValue(PREFIX_SUBJECT);
        Optional<String> groupValue = argMultimap.getValue(PREFIX_GROUP);
        boolean hasSubject = subjectValue.isPresent();
        boolean hasGroup = groupValue.isPresent();

        if (hasSubject || hasGroup) {
            return parseCriteria(argMultimap, subjectValue, groupValue);
        }

        return new FindCommand(parseNamePredicate(trimmedArgs));
    }

    /**
     * Parses a name keyword search or quoted phrase search.
     */
    private Predicate<Person> parseNamePredicate(String nameInput) throws ParseException {
        String trimmedNameInput = nameInput.trim();
        if (trimmedNameInput.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        boolean startsWithQuote = trimmedNameInput.startsWith("\"");
        boolean endsWithQuote = trimmedNameInput.endsWith("\"");

        if (startsWithQuote != endsWithQuote
                || (startsWithQuote && trimmedNameInput.length() < 2)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        if (startsWithQuote) {
            String phrase = trimmedNameInput.substring(1, trimmedNameInput.length() - 1).trim();
            if (phrase.isEmpty()) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }
            return new NameContainsPhrasePredicate(phrase);
        }

        String[] nameKeywords = trimmedNameInput.split("\\s+");
        return new NameContainsKeywordsPredicate(List.of(nameKeywords));
    }

    /**
     * Parses optional name, subject, and group filters. A person must match every supplied criterion.
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

        return new FindCommand(new FindPredicate(namePredicate, subject, group));
    }

}
