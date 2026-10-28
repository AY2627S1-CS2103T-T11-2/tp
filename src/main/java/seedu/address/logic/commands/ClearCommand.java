package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears the address book.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String CONFIRMATION_KEYWORD = "confirm";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Requests confirmation before deleting all contacts.\n"
            + "Enter `" + COMMAND_WORD + "` followed by `" + COMMAND_WORD + " " + CONFIRMATION_KEYWORD
            + "` to delete all contacts.";
    public static final String MESSAGE_CONFIRMATION_REQUIRED = "This will delete all contacts. Enter `"
            + COMMAND_WORD + " " + CONFIRMATION_KEYWORD + "` to continue.";
    public static final String MESSAGE_NO_CONFIRMATION_PENDING = "No clear confirmation is pending. Enter `"
            + COMMAND_WORD + "` first.";
    public static final String MESSAGE_SUCCESS = "Address book has been cleared!";

    private final boolean isConfirmed;

    public ClearCommand(boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        if (!isConfirmed) {
            return new CommandResult(MESSAGE_CONFIRMATION_REQUIRED);
        }
        model.setAddressBook(new AddressBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
