package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class SubjectTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Subject(null));
    }

    @Test
    public void constructor_invalidSubject_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Subject(""));
    }

    @Test
    public void isValidSubject() {
        assertFalse(Subject.isValidSubject(""));
        assertFalse(Subject.isValidSubject(" "));
        assertFalse(Subject.isValidSubject("Computer-Science"));
        assertTrue(Subject.isValidSubject("Mathematics"));
        assertTrue(Subject.isValidSubject("Computer Science 2"));
    }

    @Test
    public void equals() {
        Subject subject = new Subject("Mathematics");
        assertTrue(subject.equals(new Subject("Mathematics")));
        assertTrue(subject.equals(subject));
        assertFalse(subject.equals(null));
        assertFalse(subject.equals("Mathematics"));
        assertFalse(subject.equals(new Subject("Physics")));
    }
}
