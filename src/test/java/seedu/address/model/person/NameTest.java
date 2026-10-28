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
        assertFalse(Name.isValidName("John..Tan")); // repeated periods
        assertFalse(Name.isValidName(".John")); // starts with a period
        assertFalse(Name.isValidName("John.")); // ends with an abbreviation-like component
        assertFalse(Name.isValidName("John.Tan")); // period is not followed by a space
        assertFalse(Name.isValidName("Abcd. Tan")); // abbreviations are limited to three letters

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
        assertTrue(Name.isValidName("Jean-Luc Tan")); // hyphenated name
        assertTrue(Name.isValidName("O'Connor")); // apostrophe in name
        assertTrue(Name.isValidName("S. Jay")); // initial
        assertTrue(Name.isValidName("A. R. Rahman")); // multiple initials
        assertTrue(Name.isValidName("M. K. Subramaniam")); // multiple initials with a long surname
        assertTrue(Name.isValidName("John A. Tan")); // middle initial
        assertTrue(Name.isValidName("Aisyah Bte. Ahmad")); // short abbreviation
        assertTrue(Name.isValidName("Tan Wei Ming")); // common unpunctuated name
        assertTrue(Name.isValidName("Lee Mei-Ling")); // common hyphenated name
        assertTrue(Name.isValidName("Nur Aisyah Binte Ahmad")); // common unabbreviated name
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
