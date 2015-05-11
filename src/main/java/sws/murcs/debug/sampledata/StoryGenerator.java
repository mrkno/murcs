package sws.murcs.debug.sampledata;

import sws.murcs.model.Person;
import sws.murcs.model.Story;

import java.util.List;

/**
 * A generator for stories.
 */
public class StoryGenerator implements Generator<Story> {
    /**
     * The max number of stories to generate at low stress.
     */
    public static final int LOW_STRESS_MAX = 3;
    /**
     * The min number of stories to generate at low stress.
     */
    public static final int LOW_STRESS_MIN = 1;

    /**
     * The max number of stories to generate at medium stress.
     */
    public static final int MEDIUM_STRESS_MAX = 10;
    /**
     * The min number of stories to generate at medium stress.
     */
    public static final int MEDIUM_STRESS_MIN = 3;

    /**
     * The max number of stories to generate at high stress.
     */
    public static final int HIGH_STRESS_MAX = 20;
    /**
     * The min number of stories to generate at high stress.
     */
    public static final int HIGH_STRESS_MIN = 10;

    /**
     * A list of story names for use in generation.
     */
    private String[] storyNames = {
            "A story",
            "Haydons life",
            "Stranger Of Next Year",
            "Angel Of The West",
            "Priests Without Sin",
            "Wives With Pride",
            "Strangers And Foes",
            "Thieves And Invaders",
            "Creation Of The Day",
            "Love Of Fortune",
            "Ending The Champions",
            "Meeting At Myself",
            "Sage Of The North",
            "Hero Without Glory",
            "Turtles Of The Day",
            "Officers Without Duty",
            "Swindlers And Turtles",
            "Officers And Witches",
            "Shield Of The East",
            "Source Of Dread",
            "Smile At The King",
            "Welcome To The Mist",
            "Rebel Of Desire",
            "Savior Of Perfection",
            "Spies Of Destruction",
            "Serpents Of Stone",
            "Priests And Butchers",
            "Horses And Gods",
            "Argument Of Tomorrow",
            "Strife Of The Ocean",
            "Avoiding Eternity",
            "Separated In The Mist",
            "The Black Coffin",
            "The Crooked Stool",
            "Tree Of The Dagger",
            "Woman Without Flaws",
            "Companions Of Hope",
            "Mermen Of Stone",
            "Blacksmiths And Enchanters",
            "Foreigners And Snakes",
            "Country Without Flaws",
            "Planet Of The Eternal",
            "Wrong About History",
            "Shelter In The World",
            "Mouse Prophecy",
            "Buffoon Strategy",
            "Dog Of Parody",
            "Pig During My Travel",
            "Child And Robot",
            "Mime And Friend",
            "Farts Abroad",
            "Stunts Loves Sugar",
            "Greed Of His Laugh",
            "Amused By The Joke",
            "Cook In The River",
            "Friend With Curly Hair",
            "Visitors Of Yearning",
            "Guests In The Night",
            "Angels And Boys",
            "Roommates And Dears",
            "Body Of Romance",
            "Restoration In My Town",
            "Strange Myself",
            "Clinging To Affection",
            "Creator Of Honor",
            "Pilot Of The Orbit",
            "Hunters Of Men's Legacy",
            "Friends Of Death",
            "Girls And Children",
            "Boys And Guardians",
            "Culling Of The Dead",
            "Betrayal From Outer Space",
            "Greed Of The Machines",
            "Secrets Of New Earth",
            "Blacksmith Of History",
            "Blacksmith With Silver Hair",
            "Doctors Of Dusk",
            "Men Of History",
            "Guardians And Builders",
            "Inventors And Angels",
            "Love Of Darkness",
            "Harvest With Sins",
            "Separated By The Mountains",
            "Crying In The Night",
            "Traitor Of The World",
            "Patron Of Gold",
            "Emissaries Of Repentance",
            "Descendants With Debt",
            "Butchers And Servants",
            "Collectors And Widows",
            "Result Of Despair",
            "Dishonor Without Fear",
            "Prepare For The End",
            "Meeting At The End"
    };

    /**
     * A list of descriptions for use in generation.
     */
    private String[] descriptions = {
            "A description",
            "Lorem ipsum ect.",
            "A story",
            "Is anyone actually reading this?",
            "Stories are things you read",
            "I love scrum",
            "Sarcasm is a virtue",
            "A long description",
            "Description",
            "A story that accomplishes nothing",
            "The meaning of life is 42",
            "Monkeys like stories",
            "Implement the hyperdrive and go to the moon",
            "There's just too much to describe here, I'll do it later",
            "I'm tired of describing this",
            "No Luke, I am your father",
            "We're going to need a bigger boat",
            "I'll be back",
            "Your lack of faith disturbs me",
            "What's in the box?",
            "There is no spoon"
    };

    /**
     * A pool of persons.
     */
    private List<Person> personsPool;

    /**
     * A person generator, for use in the event that we don't have a pool.
     */
    private PersonGenerator personGenerator;

    /**
     * Creates a new story generator.
     */
    public StoryGenerator() {
        this(new PersonGenerator());
    }

    /**
     * Creates a new story generator with the specified person generator.
     * @param generator The person generator
     */
    public StoryGenerator(final PersonGenerator generator) {
        this.personGenerator = generator;
    }

    /**
     * Sets the person pool.
     * @param newPersonsPool The pool of persons.
     */
    public final void setPersonsPool(final List<Person> newPersonsPool) {
        personsPool = newPersonsPool;
    }

    @Override
    public final Story generate() {
        String name = storyNames[NameGenerator.random(storyNames.length)];
        String description = descriptions[NameGenerator.random(descriptions.length)];

        Person creator = personsPool == null
                ? personGenerator.generate()
                : personsPool.get(NameGenerator.random(personsPool.size()));

        Story story = new Story();
        try {
            story.setShortName(name);
        } catch (Exception e) {
            //Do nothing this doesn't matter. Ever.
        }
        story.setDescription(description);
        story.setCreator(creator);

        return story;
    }
}
