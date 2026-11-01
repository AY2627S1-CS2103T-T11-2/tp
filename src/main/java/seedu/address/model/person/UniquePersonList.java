package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;

/**
 * A list of persons that enforces uniqueness between its elements and does not allow nulls.
 * A person is considered unique by comparing using {@code Person#isSamePerson(Person)}. As such, adding and updating of
 * persons uses Person#isSamePerson(Person) for equality so as to ensure that the person being added or updated is
 * unique in terms of identity in the UniquePersonList. However, the removal of a person uses Person#equals(Object) so
 * as to ensure that the person with exactly the same fields will be removed.
 *
 * Supports a minimal set of list operations.
 *
 * @see Person#isSamePerson(Person)
 */
public class UniquePersonList implements Iterable<Person> {

    private final ObservableList<Person> internalList = FXCollections.observableArrayList();
    private final ObservableList<Person> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /**
     * Returns true if the list contains an equivalent person as the given argument.
     */
    public boolean contains(Person toCheck) {
        requireNonNull(toCheck);
        return internalList.stream().anyMatch(toCheck::isSamePerson);
    }

    /**
     * Adds a person to the list.
     * The person must not already exist in the list.
     */
    public void add(Person toAdd) {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            throw new DuplicatePersonException();
        }
        internalList.add(toAdd);
    }

    /**
     * Replaces the person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the list.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the list.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        int index = internalList.indexOf(target);
        if (index == -1) {
            throw new PersonNotFoundException();
        }

        if (!target.isSamePerson(editedPerson) && contains(editedPerson)) {
            throw new DuplicatePersonException();
        }

        internalList.set(index, editedPerson);
    }

    /**
     * Removes the equivalent person from the list.
     * The person must exist in the list.
     */
    public void remove(Person toRemove) {
        requireNonNull(toRemove);
        if (!internalList.remove(toRemove)) {
            throw new PersonNotFoundException();
        }
    }

    public void setPersons(UniquePersonList replacement) {
        requireNonNull(replacement);
        internalList.setAll(replacement.internalList);
    }

    /**
     * Replaces the contents of this list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        requireAllNonNull(persons);
        if (!personsAreUnique(persons)) {
            throw new DuplicatePersonException();
        }

        internalList.setAll(persons);
    }

    /**
     * Replaces corresponding people using one observable-list update.
     *
     * <p>This operation is identity-preserving: each replacement must represent
     * the same person as its target.
     */
    public void setPersonsInBatch(List<Person> targets, List<Person> editedPersons) {
        requireAllNonNull(targets);
        requireAllNonNull(editedPersons);
        if (targets.size() != editedPersons.size()) {
            throw new IllegalArgumentException("Targets and replacements must have equal sizes");
        }
        if (targets.isEmpty()) {
            return;
        }
        Map<Person, Person> replacementsByTarget = new HashMap<>();
        for (int i = 0; i < targets.size(); i++) {
            Person target = targets.get(i);
            Person replacement = editedPersons.get(i);
            if (!target.isSamePerson(replacement)) {
                throw new IllegalArgumentException("Batch replacement must preserve person identity");
            }
            if (replacementsByTarget.put(target, replacement) != null) {
                throw new IllegalArgumentException("The same target cannot be replaced twice");
            }
        }
        Set<Person> existingPeople = new HashSet<>(internalList);
        if (!existingPeople.containsAll(targets)) {
            throw new PersonNotFoundException();
        }
        List<Person> updatedPeople = internalList.stream()
                .map(person -> replacementsByTarget.getOrDefault(person, person))
                .toList();
        internalList.setAll(updatedPeople);
    }

    /**
     * Removes multiple people using one observable-list update.
     */
    public void removePersonsInBatch(List<Person> targets) {
        requireAllNonNull(targets);
        if (targets.isEmpty()) {
            return;
        }
        Set<Person> targetsToRemove = new HashSet<>(targets);
        Set<Person> existingPeople = new HashSet<>(internalList);
        if (!existingPeople.containsAll(targetsToRemove)) {
            throw new PersonNotFoundException();
        }
        List<Person> remainingPeople = internalList.stream()
                .filter(person -> !targetsToRemove.contains(person))
                .toList();
        internalList.setAll(remainingPeople);
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<Person> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public Iterator<Person> iterator() {
        return internalList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UniquePersonList otherUniquePersonList)) {
            return false;
        }

        return internalList.equals(otherUniquePersonList.internalList);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public String toString() {
        return internalList.toString();
    }

    /**
     * Returns true if {@code persons} contains only unique persons.
     */
    private boolean personsAreUnique(List<Person> persons) {
        for (int i = 0; i < persons.size() - 1; i++) {
            for (int j = i + 1; j < persons.size(); j++) {
                if (persons.get(i).isSamePerson(persons.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }
}
