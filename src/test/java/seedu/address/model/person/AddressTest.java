package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AddressTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Address(null));
    }

    @Test
    public void constructor_emptyTelegramHandle_success() {
        assertEquals("", new Address("").value);
    }

    @Test
    public void isValidAddress() {
        // null address
        assertThrows(NullPointerException.class, () -> Address.isValidAddress(null));

        // Empty Telegram handles are valid
        assertTrue(Address.isValidAddress(""));

        // Non-empty Telegram handles must begin with @; remaining text is unrestricted
        assertTrue(Address.isValidAddress("@alice_123 / ! Unicode: 用户\nsecond line"));
        assertTrue(Address.isValidAddress("@"));

        // Invalid Telegram handles
        assertFalse(Address.isValidAddress(" "));
        assertFalse(Address.isValidAddress("alice_123"));
        assertFalse(Address.isValidAddress("Blk 456, Den Road, #01-355"));
    }

    @Test
    public void equals() {
        Address address = new Address("@valid_handle");

        // same values -> returns true
        assertTrue(address.equals(new Address("@valid_handle")));

        // same object -> returns true
        assertTrue(address.equals(address));

        // null -> returns false
        assertFalse(address.equals(null));

        // different types -> returns false
        assertFalse(address.equals(5.0f));

        // different values -> returns false
        assertFalse(address.equals(new Address("@other_valid_handle")));
    }
}
