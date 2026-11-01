package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TELEGRAM_HANDLE_BOB;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import javafx.collections.ListChangeListener;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.testutil.PersonBuilder;

public class UniquePersonListTest {

    private final UniquePersonList uniquePersonList = new UniquePersonList();

    @Test
    public void contains_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.contains(null));
    }

    @Test
    public void contains_personNotInList_returnsFalse() {
        assertFalse(uniquePersonList.contains(ALICE));
    }

    @Test
    public void contains_personInList_returnsTrue() {
        uniquePersonList.add(ALICE);
        assertTrue(uniquePersonList.contains(ALICE));
    }

    @Test
    public void contains_personWithSameIdentityFieldsInList_returnsTrue() {
        uniquePersonList.add(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withTelegramHandle(VALID_TELEGRAM_HANDLE_BOB)
                .withTags(VALID_TAG_HUSBAND).build();
        assertTrue(uniquePersonList.contains(editedAlice));
    }

    @Test
    public void contains_personWithNameDifferingOnlyByCase_returnsTrue() {
        uniquePersonList.add(ALICE);
        Person aliceWithUpperCaseName = new PersonBuilder(ALICE)
                .withName(ALICE.getName().toString().toUpperCase()).build();

        assertTrue(uniquePersonList.contains(aliceWithUpperCaseName));
    }

    @Test
    public void add_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.add(null));
    }

    @Test
    public void add_duplicatePerson_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.add(ALICE));
    }

    @Test
    public void add_personWithNameDifferingOnlyByCase_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        Person aliceWithLowerCaseName = new PersonBuilder(ALICE)
                .withName(ALICE.getName().toString().toLowerCase()).build();

        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.add(aliceWithLowerCaseName));
    }

    @Test
    public void setPerson_nullTargetPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPerson(null, ALICE));
    }

    @Test
    public void setPerson_nullEditedPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPerson(ALICE, null));
    }

    @Test
    public void setPerson_targetPersonNotInList_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> uniquePersonList.setPerson(ALICE, ALICE));
    }

    @Test
    public void setPerson_editedPersonIsSamePerson_success() {
        uniquePersonList.add(ALICE);
        uniquePersonList.setPerson(ALICE, ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(ALICE);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonHasSameIdentity_success() {
        uniquePersonList.add(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withTelegramHandle(VALID_TELEGRAM_HANDLE_BOB)
                .withTags(VALID_TAG_HUSBAND).build();
        uniquePersonList.setPerson(ALICE, editedAlice);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(editedAlice);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_capitalizationOnlyNameEdit_success() {
        uniquePersonList.add(ALICE);
        Person aliceWithUpperCaseName = new PersonBuilder(ALICE)
                .withName(ALICE.getName().toString().toUpperCase()).build();

        uniquePersonList.setPerson(ALICE, aliceWithUpperCaseName);

        assertEquals(List.of(aliceWithUpperCaseName), uniquePersonList.asUnmodifiableObservableList());
        assertEquals(ALICE.getName().toString().toUpperCase(),
                uniquePersonList.asUnmodifiableObservableList().getFirst().getName().toString());
    }

    @Test
    public void setPerson_editedPersonHasDifferentIdentity_success() {
        uniquePersonList.add(ALICE);
        uniquePersonList.setPerson(ALICE, BOB);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonHasNonUniqueIdentity_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPerson(ALICE, BOB));
    }

    @Test
    public void setPerson_editedPersonHasNonUniqueNameDifferingOnlyByCase_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        Person aliceWithLowerCaseName = new PersonBuilder(BOB)
                .withName(ALICE.getName().toString().toLowerCase()).build();

        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPerson(BOB, aliceWithLowerCaseName));
    }

    @Test
    public void remove_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.remove(null));
    }

    @Test
    public void remove_personDoesNotExist_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> uniquePersonList.remove(ALICE));
    }

    @Test
    public void remove_existingPerson_removesPerson() {
        uniquePersonList.add(ALICE);
        uniquePersonList.remove(ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_nullUniquePersonList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPersons((UniquePersonList) null));
    }

    @Test
    public void setPersons_uniquePersonList_replacesOwnListWithProvidedUniquePersonList() {
        uniquePersonList.add(ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        uniquePersonList.setPersons(expectedUniquePersonList);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPersons((List<Person>) null));
    }

    @Test
    public void setPersons_list_replacesOwnListWithProvidedList() {
        uniquePersonList.add(ALICE);
        List<Person> personList = List.of(BOB);
        uniquePersonList.setPersons(personList);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_listWithDuplicatePersons_throwsDuplicatePersonException() {
        List<Person> listWithDuplicatePersons = List.of(ALICE, ALICE);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPersons(listWithDuplicatePersons));
    }

    @Test
    public void asUnmodifiableObservableList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, ()
            -> uniquePersonList.asUnmodifiableObservableList().remove(0));
    }

    @Test
    public void setPersonsInBatch_validReplacements_updatesOnce() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);

        Person editedAlice = new PersonBuilder(ALICE)
                .withTags("Batch")
                .build();
        Person editedBob = new PersonBuilder(BOB)
                .withTags("Batch")
                .build();

        AtomicInteger changeCount = new AtomicInteger();
        uniquePersonList.asUnmodifiableObservableList().addListener(
                (ListChangeListener<Person>) change ->
                        changeCount.incrementAndGet());

        uniquePersonList.setPersonsInBatch(
                List.of(ALICE, BOB),
                List.of(editedAlice, editedBob));

        assertEquals(
                List.of(editedAlice, editedBob),
                uniquePersonList.asUnmodifiableObservableList());
        assertEquals(1, changeCount.get());
    }

    @Test
    public void removePersonsInBatch_multiplePersons_removesOnceAndPreservesOrder() {
        Person charlie = new PersonBuilder()
                .withName("Charlie")
                .build();

        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        uniquePersonList.add(charlie);

        AtomicInteger changeCount = new AtomicInteger();
        uniquePersonList.asUnmodifiableObservableList().addListener(
                (ListChangeListener<Person>) change -> changeCount.incrementAndGet());

        uniquePersonList.removePersonsInBatch(
                List.of(ALICE, charlie));

        assertEquals(List.of(BOB), uniquePersonList.asUnmodifiableObservableList());
        assertEquals(1, changeCount.get());
    }

    @Test
    public void removePersonsInBatch_oneTargetMissing_throwsWithoutChanges() {
        uniquePersonList.add(ALICE);

        AtomicInteger changeCount = new AtomicInteger();
        uniquePersonList.asUnmodifiableObservableList().addListener(
                (ListChangeListener<Person>) change -> changeCount.incrementAndGet());

        assertThrows(PersonNotFoundException.class, () -> uniquePersonList.removePersonsInBatch(List.of(ALICE, BOB)));

        assertEquals(List.of(ALICE), uniquePersonList.asUnmodifiableObservableList());
        assertEquals(0, changeCount.get());
    }

    @Test
    public void setPersonsInBatch_oneTargetMissing_throwsWithoutChanges() {
        uniquePersonList.add(ALICE);

        Person editedAlice = new PersonBuilder(ALICE)
                .withTags("Batch")
                .build();
        Person editedBob = new PersonBuilder(BOB)
                .withTags("Batch")
                .build();

        assertThrows(PersonNotFoundException.class, () -> uniquePersonList.setPersonsInBatch(List.of(ALICE, BOB),
                List.of(editedAlice, editedBob)));

        assertEquals(List.of(ALICE), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void setPersonsInBatch_differentListSizes_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> uniquePersonList.setPersonsInBatch(List.of(ALICE),
                List.of()));
    }

    @Test
    public void setPersonsInBatch_changedIdentity_throwsIllegalArgumentException() {
        uniquePersonList.add(ALICE);

        assertThrows(IllegalArgumentException.class, () -> uniquePersonList.setPersonsInBatch(List.of(ALICE),
                List.of(BOB)));

        assertEquals(List.of(ALICE), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void setPersonsInBatch_duplicateTarget_throwsIllegalArgumentException() {
        uniquePersonList.add(ALICE);

        Person editedAlice = new PersonBuilder(ALICE)
                .withTags("Batch")
                .build();

        assertThrows(IllegalArgumentException.class, () -> uniquePersonList.setPersonsInBatch(List.of(ALICE, ALICE),
                List.of(editedAlice, editedAlice)));
    }

    @Test
    public void setPersonsInBatch_nullLists_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPersonsInBatch(null, List.of()));

        assertThrows(NullPointerException.class, () -> uniquePersonList.setPersonsInBatch(List.of(), null));
    }

    @Test
    public void removePersonsInBatch_nullTargets_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.removePersonsInBatch(null));
    }

    @Test
    public void setPersonsInBatch_emptyLists_doesNotNotifyListeners() {
        uniquePersonList.add(ALICE);

        AtomicInteger changeCount = new AtomicInteger();
        uniquePersonList.asUnmodifiableObservableList().addListener(
                (ListChangeListener<Person>) change -> changeCount.incrementAndGet());

        uniquePersonList.setPersonsInBatch(List.of(), List.of());

        assertEquals(List.of(ALICE), uniquePersonList.asUnmodifiableObservableList());
        assertEquals(0, changeCount.get());
    }

    @Test
    public void removePersonsInBatch_emptyList_doesNotNotifyListeners() {
        uniquePersonList.add(ALICE);

        AtomicInteger changeCount = new AtomicInteger();
        uniquePersonList.asUnmodifiableObservableList().addListener(
                (ListChangeListener<Person>) change -> changeCount.incrementAndGet());

        uniquePersonList.removePersonsInBatch(List.of());

        assertEquals(List.of(ALICE), uniquePersonList.asUnmodifiableObservableList());
        assertEquals(0, changeCount.get());
    }

    @Test
    public void toStringMethod() {
        assertEquals(uniquePersonList.asUnmodifiableObservableList().toString(), uniquePersonList.toString());
    }
}
