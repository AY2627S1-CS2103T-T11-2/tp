package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains non-alphanumeric characters
        assertFalse(Name.isValidName("-Jane")); // starts with a separator
        assertFalse(Name.isValidName("Jane-")); // ends with a separator
        assertFalse(Name.isValidName("O''Connor")); // repeated apostrophes
        assertFalse(Name.isValidName("Mary  Jane")); // repeated spaces
        assertFalse(Name.isValidName("Abdul D/ Rahmud")); // incomplete relationship qualifier
        assertFalse(Name.isValidName("Abdul D/O")); // relationship qualifier must be between words
        assertFalse(Name.isValidName("Abdul X/O Rahmud")); // unsupported slash-containing component
        assertFalse(Name.isValidName("a".repeat(Name.MAX_LENGTH + 1))); // exceeds maximum length

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
        assertTrue(Name.isValidName("Jean-Luc Tan")); // hyphenated name
        assertTrue(Name.isValidName("O'Connor")); // apostrophe in name
        assertTrue(Name.isValidName("S. Jay"));
        assertTrue(Name.isValidName("A. R. Rahman"));
        assertTrue(Name.isValidName("Lee Mei-Ling"));
        assertTrue(Name.isValidName("Abdul Binte D/O Rahmud"));
        assertTrue(Name.isValidName("a".repeat(Name.MAX_LENGTH))); // exactly maximum length
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}
