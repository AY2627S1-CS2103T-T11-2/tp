package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "12";
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    public void isValidPhone() {
        // null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // invalid phone numbers
        assertFalse(Phone.isValidPhone(" ")); // spaces only
        assertFalse(Phone.isValidPhone("91")); // fewer than 8 digits
        assertFalse(Phone.isValidPhone("911"));
        assertFalse(Phone.isValidPhone("9123456"));
        assertFalse(Phone.isValidPhone("912345678"));
        assertFalse(Phone.isValidPhone("9123 45678"));
        assertFalse(Phone.isValidPhone("124293842033123"));
        assertFalse(Phone.isValidPhone("+91234567"));
        assertFalse(Phone.isValidPhone("+65-9123-4567"));
        assertFalse(Phone.isValidPhone("phone")); // non-numeric
        assertFalse(Phone.isValidPhone("9011p041")); // alphabets within digits
        assertFalse(Phone.isValidPhone("+91")); // country codes not supported
        assertFalse(Phone.isValidPhone("+65+91234567")); // multiple plus signs
        assertFalse(Phone.isValidPhone("+65-")); // does not end with a digit
        assertFalse(Phone.isValidPhone("1--- --2 3")); // repeated mixed separators
        assertFalse(Phone.isValidPhone("9123--4567"));
        assertFalse(Phone.isValidPhone("9123  4567"));
        assertFalse(Phone.isValidPhone("9123 -4567"));
        assertFalse(Phone.isValidPhone("9123- 4567"));
        assertFalse(Phone.isValidPhone("+ 6591234567"));

        // valid phone numbers
        assertTrue(Phone.isValidPhone("")); // cleared phone number
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("9123 4567")); // spaces as separators
        assertTrue(Phone.isValidPhone("9123-4567"));
        assertTrue(Phone.isValidPhone("91-23 4567")); // single separators between digit groups
        assertTrue(Phone.isValidPhone("00000000")); // leading zeroes retained
    }

    @Test
    public void isEmpty() {
        assertTrue(new Phone("").isEmpty());
        assertFalse(new Phone("99999999").isEmpty());
    }

    @Test
    public void constructor_formattedPhone_canonicalizesValue() {
        for (String input : new String[] {"91234567", "9123 4567", "9123-4567", "91-23 4567"}) {
            assertEquals("9123 4567", new Phone(input).value);
            assertEquals("9123 4567", new Phone(input).toString());
            assertEquals(new Phone("91234567"), new Phone(input));
            assertEquals(new Phone("91234567").hashCode(), new Phone(input).hashCode());
        }
        assertEquals("0000 0001", new Phone("00000001").value);
    }

    @Test
    public void equals() {
        Phone phone = new Phone("99999999");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("99999999")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("99999995")));
    }
}
