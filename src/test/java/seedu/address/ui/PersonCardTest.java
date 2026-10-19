package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    @BeforeAll
    public static void startJavaFxToolkit() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
            // The toolkit has already been started by another UI test.
        }
    }

    @Test
    public void constructor_emptyOptionalFields_hiddenAndUnmanaged() throws Exception {
        Person person = new PersonBuilder().withPhone("").withAddress("").build();

        assertFieldVisibility(person, false, false);
    }

    @Test
    public void constructor_presentOptionalFields_visibleAndManaged() throws Exception {
        Person person = new PersonBuilder().withPhone("91234567").withAddress("@amy").build();

        assertFieldVisibility(person, true, true);
    }

    private static void assertFieldVisibility(Person person, boolean phoneVisible, boolean addressVisible)
            throws InterruptedException, ExecutionException {
        FutureTask<Void> task = new FutureTask<>(() -> {
            PersonCard card = new PersonCard(person, 1);
            Label phone = (Label) card.getRoot().lookup("#phone");
            Label address = (Label) card.getRoot().lookup("#address");

            if (phoneVisible) {
                assertTrue(phone.isVisible());
                assertTrue(phone.isManaged());
            } else {
                assertFalse(phone.isVisible());
                assertFalse(phone.isManaged());
            }
            if (addressVisible) {
                assertTrue(address.isVisible());
                assertTrue(address.isManaged());
            } else {
                assertFalse(address.isVisible());
                assertFalse(address.isManaged());
            }
            return null;
        });

        Platform.runLater(task);
        task.get();
    }
}
