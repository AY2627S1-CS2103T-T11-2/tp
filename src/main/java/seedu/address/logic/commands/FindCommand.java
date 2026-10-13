package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Finds and lists all persons in the address book whose name match any argument keyword or quoted phrase.
 * and who match all the supplised subject and group filters.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds all persons whose names match any search term "
            + "(case-insensitive) and match any supplied subject or group filters.\\n"
            + "Parameters: [SEARCH_TERM [MORE_SEARCH_TERMS]...] [s/SUBJECT] [g/GROUP]\\n"
            + "A search term is either a KEYWORD (one word without quotation marks) or a \"PHRASE\" "
            + "(one or more words enclosed in quotation marks).\\n"
            + "Examples:\\n"
            + "  " + COMMAND_WORD + " alice bob\\n"
            + "  " + COMMAND_WORD + " \"Alice Pauline\"\\n"
            + "  " + COMMAND_WORD + " alice \"Bob Chen\"\\n"
            + "  " + COMMAND_WORD + " \"John\" s/Mathematics g/G01";

    private final Predicate<Person> predicate;

    public FindCommand(Predicate<Person> predicate) {
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
