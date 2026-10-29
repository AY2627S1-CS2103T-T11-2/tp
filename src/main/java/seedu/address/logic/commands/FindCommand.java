package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TELEGRAM_HANDLE;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Finds and lists persons who match the supplied name, Telegram handle, subject, group, and tag criteria.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds students matching the supplied name, Telegram "
            + "handle, subject, group, and tag criteria (case-insensitive).\n"
            + "Parameters: [" + PREFIX_NAME + "NAME_TERM[,NAME_TERM]...] "
            + "[" + PREFIX_TELEGRAM_HANDLE + "TELEGRAM_HANDLE] "
            + "[" + PREFIX_SUBJECT + "SUBJECT] [" + PREFIX_GROUP + "GROUP] [" + PREFIX_TAG + "TAG_EXPRESSION]\n"
            + "A name term is either a KEYWORD (one word without quotation marks) or a \"PHRASE\" "
            + "(one or more words enclosed in quotation marks). Separate name terms with ','. "
            + "Separate tags with ',' for OR or '&' for AND; do not mix the operators or repeat a prefix.\n"
            + "Examples:\n"
            + "  " + COMMAND_WORD + " " + PREFIX_NAME + "alice,bob\n"
            + "  " + COMMAND_WORD + " " + PREFIX_NAME + "\"Alice Pauline\",bob\n"
            + "  " + COMMAND_WORD + " " + PREFIX_TELEGRAM_HANDLE + "@alice_tan\n"
            + "  " + COMMAND_WORD + " t/needsHelp,lowAttendance\n"
            + "  " + COMMAND_WORD + " t/needsHelp&lowAttendance\n"
            + "  " + COMMAND_WORD + " " + PREFIX_NAME
            + "\"John\" s/Mathematics g/G01 t/highParticipation";

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
