package seedu.address.model.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.person.TelegramHandle;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    private static final Subject DEFAULT_SUBJECT = new Subject("General");
    private static final Group DEFAULT_GROUP = new Group("General");

    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Alex Yeoh"), new Phone("87438807"), new Email("alexyeoh@example.com"),
                new TelegramHandle("@alex_yeoh"),
                getTagSet("friends"), DEFAULT_SUBJECT, DEFAULT_GROUP),
            new Person(new Name("Bernice Yu"), new Phone("99272758"), new Email("berniceyu@example.com"),
                new TelegramHandle("@bernice_yu"),
                getTagSet("colleagues", "friends"), DEFAULT_SUBJECT, DEFAULT_GROUP),
            new Person(new Name("Charlotte Oliveiro"), new Phone("93210283"), new Email("charlotte@example.com"),
                new TelegramHandle("@charlotte_oliveiro"),
                getTagSet("neighbours"), DEFAULT_SUBJECT, DEFAULT_GROUP),
            new Person(new Name("David Li"), new Phone("91031282"), new Email("lidavid@example.com"),
                new TelegramHandle("@david_li"),
                getTagSet("family"), DEFAULT_SUBJECT, DEFAULT_GROUP),
            new Person(new Name("Irfan Ibrahim"), new Phone("92492021"), new Email("irfan@example.com"),
                new TelegramHandle("@irfan_ibrahim"),
                getTagSet("classmates"), DEFAULT_SUBJECT, DEFAULT_GROUP),
            new Person(new Name("Roy Balakrishnan"), new Phone("92624417"), new Email("royb@example.com"),
                new TelegramHandle("@roy_balakrishnan"),
                getTagSet("colleagues"), DEFAULT_SUBJECT, DEFAULT_GROUP)
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

}
