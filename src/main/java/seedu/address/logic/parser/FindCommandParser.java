package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsSearchTermsPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {
    private static final Pattern SEARCH_TERM_PATTERN = Pattern.compile("\\\"([^\\\"]*)\\\"|([^\\s\\\"]+)");

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();

        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        List<String> keywords = new ArrayList<>();
        List<String> phrases = new ArrayList<>();
        Matcher matcher = SEARCH_TERM_PATTERN.matcher(trimmedArgs);
        int previousMatchEnd = 0;

        while (matcher.find()) {
            if (!trimmedArgs.substring(previousMatchEnd, matcher.start()).isBlank()) {
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

        if (!trimmedArgs.substring(previousMatchEnd).isBlank()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        return new FindCommand(new NameContainsSearchTermsPredicate(keywords, phrases));
    }

}
