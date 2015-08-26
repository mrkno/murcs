package sws.murcs.debug.sampledata;

import sws.murcs.model.Task;
import sws.murcs.model.TaskState;

import java.util.ArrayList;
import java.util.List;

/**
 * A generator that generates tasks for a story.
 */
public class TaskGenerator implements Generator<Task> {

    /**
     * The max number of tasks generated on low stress.
     */
    protected static final int LOW_STRESS_MIN = 1;

    /**
     * The min number of tasks generated on low stress.
     */
    protected static final int LOW_STRESS_MAX = 5;

    /**
     * The max number of tasks generated on medium stress.
     */
    protected static final int MEDIUM_STRESS_MIN = 5;

    /**
     * The min number of tasks generated on medium stress.
     */
    protected static final int MEDIUM_STRESS_MAX = 10;

    /**
     * The max number of tasks generated on high stress.
     */
    protected static final int HIGH_STRESS_MIN = 10;

    /**
     * The min number of tasks generated on high stress.
     */
    protected static final int HIGH_STRESS_MAX = 20;

    /**
     * The number of names that are generated for use in making tasks.
     */
    private static final int NUMBER_OF_NAMES = 100;

    /**
     * The maximum estimate value.
     */
    private static final int MAX_ESTIMATE = 15;

    /**
     * An array of strings used for generated task names.
     */
    private String[] taskNames;

    /**
     * An array of nouns used for creating task names.
     */
    private String[] nouns = {
            "able",
            "achieve",
            "acoustics",
            "action",
            "activity",
            "aftermath",
            "afternoon",
            "afterthought",
            "apparel",
            "appliance",
            "beginner",
            "believe",
            "bomb",
            "border",
            "boundary",
            "breakfast",
            "cabbage",
            "cable",
            "calculator",
            "calendar",
            "caption",
            "carpenter",
            "cemetery",
            "channel",
            "chocolate",
            "circle",
            "computer",
            "creator",
            "creature",
            "Daniel",
            "Dion",
            "education",
            "father",
            "faucet",
            "feather",
            "friction",
            "fruit",
            "fuel",
            "galley",
            "guide",
            "guitar",
            "Haydon",
            "health",
            "heart",
            "idea",
            "James",
            "Jay",
            "kitten",
            "laborer",
            "language",
            "lawyer",
            "linen",
            "locket",
            "lumber",
            "magic",
            "Matt",
            "minister",
            "mitten",
            "money",
            "mountain",
            "music",
            "partner",
            "passenger",
            "pickle",
            "picture",
            "plantation",
            "plastic",
            "pleasure",
            "pocket",
            "police",
            "pollution",
            "railway",
            "recess",
            "reward",
            "route",
            "scene",
            "scent",
            "squirrel",
            "stranger",
            "suit",
            "sweater",
            "temper",
            "territory",
            "texture",
            "thread",
            "treatment",
            "veil",
            "vein",
            "volcano",
            "wealth",
            "weather",
            "wilderness",
            "wren",
            "wrist",
            "writer"
    };

    /**
     * And array of verbs used for creating task names.
     */
    private String[] verbs = {
            "accept",
            "add",
            "admire",
            "admit",
            "advise",
            "afford",
            "agree",
            "alert",
            "allow",
            "amuse",
            "analyse",
            "analyze",
            "announce",
            "annoy",
            "answer",
            "apologise",
            "appear",
            "applaud",
            "appreciate",
            "approve",
            "argue",
            "arrange",
            "arrest",
            "arrive",
            "ask",
            "attach",
            "attack",
            "attempt",
            "attend",
            "attract",
            "avoid",
            "back",
            "bake",
            "balance",
            "ban",
            "bang",
            "bare",
            "bat",
            "bathe",
            "battle",
            "beam",
            "beg",
            "behave",
            "belong",
            "bleach",
            "bless",
            "blind",
            "blink",
            "blot",
            "blush",
            "boast",
            "boil",
            "bolt",
            "bomb",
            "book",
            "bore",
            "borrow",
            "bounce",
            "bow",
            "box",
            "brake",
            "branch",
            "breathe",
            "bruise",
            "brush",
            "bubble",
            "bump",
            "burn",
            "bury",
            "buzz",
            "calculate",
            "call",
            "camp",
            "care",
            "carry",
            "carve",
            "cause",
            "challenge",
            "change",
            "charge",
            "chase",
            "cheat",
            "check",
            "cheer",
            "chew",
            "choke",
            "chop",
            "claim",
            "clap",
            "clean",
            "clear",
            "clip",
            "close",
            "coach",
            "coil",
            "collect",
            "colour",
            "comb",
            "command",
            "communicate",
            "compare",
            "compete",
            "complain",
            "complete",
            "concentrate",
            "concern",
            "confess",
            "confuse",
            "connect",
            "consider",
            "consist",
            "contain",
            "continue",
            "copy",
            "correct",
            "cough",
            "count",
            "cover",
            "crack",
            "crash",
            "crawl",
            "cross",
            "crush",
            "cry",
            "cure",
            "curl",
            "curve",
            "cycle",
            "dam",
            "damage",
            "dance",
            "dare",
            "decay",
            "deceive",
            "decide",
            "decorate",
            "delay",
            "delight",
            "deliver",
            "depend",
            "describe",
            "desert",
            "deserve",
            "destroy",
            "detect",
            "develop",
            "disagree",
            "disappear",
            "disapprove",
            "disarm",
            "discover",
            "dislike",
            "divide",
            "double",
            "doubt",
            "drag",
            "drain",
            "dream",
            "dress",
            "drip",
            "drop",
            "drown",
            "drum",
            "dry",
            "dust",
            "earn",
            "educate",
            "embarrass",
            "employ",
            "empty",
            "encourage",
            "end",
            "enjoy",
            "enter",
            "entertain",
            "escape",
            "examine",
            "excite",
            "excuse",
            "exercise",
            "exist",
            "expand",
            "expect",
            "explain",
            "explode",
            "extend",
            "face",
            "fade",
            "fail",
            "fancy",
            "fasten",
            "fax",
            "fear",
            "fence",
            "fetch",
            "file",
            "fill",
            "film",
            "fire",
            "fit",
            "fix",
            "flap",
            "flash",
            "float",
            "flood",
            "flow",
            "flower",
            "fold",
            "follow",
            "fool",
            "force",
            "form",
            "found",
            "frame",
            "frighten",
            "fry",
            "gather",
            "gaze",
            "glow",
            "glue",
            "grab",
            "grate",
            "grease",
            "greet",
            "grin",
            "grip",
            "groan",
            "guarantee",
            "guard",
            "guess",
            "guide",
            "hammer",
            "hand",
            "handle",
            "hang",
            "happen",
            "harass",
            "harm",
            "hate",
            "haunt",
            "head",
            "heal",
            "heap",
            "heat",
            "help",
            "hook",
            "hop",
            "hope",
            "hover",
            "hug",
            "hum",
            "hunt",
            "hurry",
            "identify",
            "ignore",
            "imagine",
            "impress",
            "improve",
            "include",
            "increase",
            "influence",
            "inform",
            "inject",
            "injure",
            "instruct",
            "intend",
            "interest",
            "interfere",
            "interrupt",
            "introduce",
            "invent",
            "invite",
            "irritate",
            "itch",
            "jail",
            "jam",
            "jog",
            "join",
            "joke",
            "judge",
            "juggle",
            "jump",
            "kick",
            "kill",
            "kiss",
            "kneel",
            "knit",
            "knock",
            "knot",
            "label",
            "land",
            "last",
            "laugh",
            "launch",
            "learn",
            "level",
            "license",
            "lick",
            "lie",
            "lighten",
            "like",
            "list",
            "listen",
            "live",
            "load",
            "lock",
            "long",
            "look",
            "love",
            "man",
            "manage",
            "march",
            "mark",
            "marry",
            "match",
            "mate",
            "matter",
            "measure",
            "meddle",
            "melt",
            "memorise",
            "mend",
            "mess up",
            "milk",
            "mine",
            "miss",
            "mix",
            "moan",
            "moor",
            "mourn",
            "move",
            "muddle",
            "mug",
            "multiply",
            "murder",
            "nail",
            "name",
            "need",
            "nest",
            "nod",
            "note",
            "notice",
            "number",
            "obey",
            "object",
            "observe",
            "obtain",
            "occur",
            "offend",
            "offer",
            "open",
            "order",
            "overflow",
            "owe",
            "own",
            "pack",
            "paddle",
            "paint",
            "park",
            "part",
            "pass",
            "paste",
            "pat",
            "pause",
            "peck",
            "pedal",
            "peel",
            "peep",
            "perform",
            "permit",
            "phone",
            "pick",
            "pinch",
            "pine",
            "place",
            "plan",
            "plant",
            "play",
            "please",
            "plug",
            "point",
            "poke",
            "polish",
            "pop",
            "possess",
            "post",
            "pour",
            "practise",
            "pray",
            "preach",
            "precede",
            "prefer",
            "prepare",
            "present",
            "preserve",
            "press",
            "pretend",
            "prevent",
            "prick",
            "print",
            "produce",
            "program",
            "promise",
            "protect",
            "provide",
            "pull",
            "pump",
            "punch",
            "puncture",
            "punish",
            "push",
            "question",
            "queue ",
            "race",
            "radiate",
            "rain",
            "raise",
            "reach",
            "realise",
            "receive",
            "recognise",
            "record",
            "reduce",
            "reflect",
            "refuse",
            "regret",
            "reign",
            "reject",
            "rejoice",
            "relax",
            "release",
            "rely",
            "remain",
            "remember",
            "remind",
            "remove",
            "repair",
            "repeat",
            "replace",
            "reply",
            "report",
            "reproduce",
            "request",
            "rescue",
            "retire",
            "return",
            "rhyme",
            "rinse",
            "risk",
            "rob",
            "rock",
            "roll",
            "rot",
            "rub",
            "ruin",
            "rule",
            "rush",
            "sack",
            "sail",
            "satisfy",
            "save",
            "saw",
            "scare",
            "scatter",
            "scold",
            "scorch",
            "scrape",
            "scratch",
            "scream",
            "screw",
            "scribble",
            "scrub",
            "seal",
            "search",
            "separate",
            "serve",
            "settle",
            "shade",
            "share",
            "shave",
            "shelter",
            "shiver",
            "shock",
            "shop",
            "shrug",
            "sigh",
            "sign",
            "signal",
            "sin",
            "sip",
            "ski",
            "skip",
            "slap",
            "slip",
            "slow",
            "smash",
            "smell",
            "smile",
            "smoke",
            "snatch",
            "sneeze",
            "sniff",
            "snore",
            "snow",
            "soak",
            "soothe",
            "sound",
            "spare",
            "spark",
            "sparkle",
            "spell",
            "spill",
            "spoil",
            "spot",
            "spray",
            "sprout",
            "squash",
            "squeak",
            "squeal",
            "squeeze",
            "stain",
            "stamp",
            "stare",
            "start",
            "stay",
            "steer",
            "step",
            "stir",
            "stitch",
            "stop",
            "store",
            "strap",
            "strengthen",
            "stretch",
            "strip",
            "stroke",
            "stuff",
            "subtract",
            "succeed",
            "suck",
            "suffer",
            "suggest",
            "suit",
            "supply",
            "support",
            "suppose",
            "surprise",
            "surround",
            "suspect",
            "suspend",
            "switch",
            "talk",
            "tame",
            "tap",
            "taste",
            "tease",
            "telephone",
            "tempt",
            "terrify",
            "test",
            "thank",
            "thaw",
            "tick",
            "tickle",
            "tie",
            "time",
            "tip",
            "tire",
            "touch",
            "tour",
            "tow",
            "trace",
            "trade",
            "train",
            "transport",
            "trap",
            "travel",
            "treat",
            "tremble",
            "trick",
            "trip",
            "trot",
            "trouble",
            "trust",
            "try",
            "tug",
            "tumble",
            "turn",
            "twist",
            "type",
            "undress",
            "unfasten",
            "unite",
            "unlock",
            "unpack",
            "untidy",
            "use",
            "vanish",
            "visit",
            "wail",
            "wait",
            "walk",
            "wander",
            "want",
            "warm",
            "warn",
            "wash",
            "waste",
            "watch",
            "water",
            "wave",
            "weigh",
            "welcome",
            "whine",
            "whip",
            "whirl",
            "whisper",
            "whistle",
            "wink",
            "wipe",
            "wish",
            "wobble",
            "wonder",
            "work",
            "worry",
            "wrap",
            "wreck",
            "wrestle",
            "wriggle"
    };

    /**
     * A list of default task descriptions.
     */
    private String[] defaultDescriptions = {
            "Go into a phone store, look at the sales person, hold out a banana and tell them you want to upgrade to "
                    + "an apple.",
            "On New Years Eve at 11: 55 order a pizza then at 12:01, New Year's day, call and complain I ordered "
                    + "this last year!",
            "Go into a public restroom then after a few seconds, yell 'LET IT GO! LET IT GO! CAN'T HOLD IT "
                    + "BACK ANYMORE!' then drop something heavy into the toilet.",
            "Order a pizza 3 minutes before new year and when it comes say 'I ordered this a darn year ago' "
                    + "and scream in frustration.",
            "When someone knocks at the door, knock back and see how long it goes on for.",
            "Go into a supermarket, and in the produce section, find a pineapple. Grab it and shake it violently, "
                    + "screaming, COME OUT SPONGEBOB, I KNOW YOUR HOME!!",
            "Super glue a coin to the ground and watch people try to pick it up.",
            "Get into a crowded elevator and say, 'I bet your all wondering why I gathered you here today.'",
            "Look at see through glass and when someone is on the other side shout 'OH MY GOD, I.'",
            "Put a Wakie talkie under your friends pillow and then late at night sing scary songs and then the next "
                    + "day ask them if they slept well last night.",
            "Bring a big chair into the elevator facing away from the door and when someone walks in, dramatically "
                    + "turn and say 'we've been expecting you.'",
            "Go to the toilet and scream LET IT GO LET IT GO CANT HOLD IT BACK ANYMORE then throw something in "
                    + "the toilet.",
            "Hide an alarm clock in a locked box and set it to go off at 3 a.m. under your friend or siblings bed. "
                    + "The results, priceless. ; ) XDDDDDDDDDDDD",
            "Bring a swivel chair and two of your friends into an elevator, and when someone comes in, turn slowly "
                    + "and say 'We've been expecting you'.",
            "Put 'try me' stickers on random things at Wal-Mart. (Ex: A carton of ice-cream >:-D)",
            "Go to a grocery store and yell 'I KNOW YOUR PLAN OF TAKING OVER THE WORLD!',at the potato",
            "Buy a donut and complain that there's a hole in it.",
            "Call someone to tell them you can't talk right now.",
            "Put a walkie talkie in your mailbox then yell at people who walk bye,'go away'.",
            "Point at someone and shout 'You're one of them!'' Run and pretend to trip. Crawl away slowly."
    };

    /**
     * Sets up a new task generator.
     */
    public TaskGenerator() {
        setUpNameList();
    }

    /**
     * Sets up the list of names used for task names.
     */
    @SuppressWarnings("checkstyle:avoidinlineconditionals")
    private void setUpNameList() {
        List<String> names = new ArrayList<>();
        String verb;
        String noun;
        for (int i = 0; i < NUMBER_OF_NAMES; i++) {
            verb = GenerationHelper.randomElement(verbs);
            noun = GenerationHelper.randomElement(nouns);
            String taskName = verb;
            taskName += "aeiou".contains(noun.subSequence(0, 1)) ? " an " : " a ";
            taskName += noun;
            names.add(Character.toUpperCase(taskName.charAt(0)) + taskName.substring(1));
        }
        taskNames = new String[names.size()];
        names.toArray(taskNames);
    }

    @Override
    public final Task generate() {
        Task t = new Task();
        t.setDescription(GenerationHelper.randomElement(defaultDescriptions));
        t.setEstimate(GenerationHelper.random(MAX_ESTIMATE));
        t.setState(TaskState.values()[GenerationHelper.random(TaskState.values().length)]);
        t.setName(GenerationHelper.randomElement(taskNames));
        return t;
    }
}
