package seedu.address.logic.commands;

import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Selects the people to delete from a model.
 */
public interface DeleteTarget {

    /**
     * Resolves this target against the current model state.
     *
     * @param model Model containing the people that may be selected.
     * @return Distinct people to delete, in display order.
     * @throws CommandException If this target cannot be resolved.
     */
    List<Person> resolve(Model model) throws CommandException;
}
