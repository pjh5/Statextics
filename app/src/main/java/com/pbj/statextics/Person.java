package com.pbj.statextics;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.Serializable;



/**
 * Created by Hellemn on 1/31/2015.
 */
public class Person implements Serializable {
    public static final int SENT_TO_THEM = 0;
    public static final int RECEIVED_FROM_THEM = 1;

    // for Serialization
    private static final long serialVersionUID = 0l;

    // TAGS
    private static final ArrayList<String> PROFANITY_TAGS = new ArrayList<>();
    private static final ArrayList<String> VANITY_TAGS = new ArrayList<>();
    private static final ArrayList<String> PARTY_TAGS = new ArrayList<>();
    private static final ArrayList<String> LAUGH_TAGS = new ArrayList<>();
    private static final ArrayList<String> KNOW_NOTHING_TAGS = new ArrayList<>();
    private static final ArrayList<String> BASIC_TAGS = new ArrayList<>();
    public static final HashMap<String, ArrayList<String>> tagMap = new HashMap<>();
    static {
        tagMap.put("profanity_tags", PROFANITY_TAGS);
        tagMap.put("vanity_tags", VANITY_TAGS);
        tagMap.put("party_tags", PARTY_TAGS);
        tagMap.put("laugh_tags", LAUGH_TAGS);
        tagMap.put("know_nothing_tags", KNOW_NOTHING_TAGS);
        tagMap.put("basic_tags", BASIC_TAGS);

    }

    // Maps Category Names to Formulas and Indices
    public static enum Category {
        MOST_PROFANE(0, "Most Profane", PROFANITY_TAGS, "swears/1000 texts", new Formula() {
            @Override
            public double calculateFor(Person p) {
                return (1000.0*p.specialCounts[0]) / (p.getTotalMessages(RECEIVED_FROM_THEM));
            }
        }),
        MOST_VAIN(1, "Narcissus", VANITY_TAGS, "vain phrases/1000 texts", new Formula() {
            @Override
            public double calculateFor(Person p) {
                return (1000.0*p.specialCounts[1]) / (p.getTotalMessages(RECEIVED_FROM_THEM));
            }
        }),
        LONGEST_WORD("Sesquipedalian", "letters/word", new Formula() {
            @Override
            public double calculateFor(Person p) {
                return (1.0*p.getTotalChars(RECEIVED_FROM_THEM)) / p.getTotalWords(RECEIVED_FROM_THEM);
            }
        }),
        PARTY_ANIMAL(2, "Kaiser of Krunk", PARTY_TAGS, "party words/1000 texts", new Formula() {
            @Override
            public double calculateFor(Person p) {
                return (1000.0*p.specialCounts[2]) / (p.getTotalMessages(RECEIVED_FROM_THEM));
            }
        }),
        THEY_TEXT_MORE("Stalker", "their texts/your texts", new Formula() {
            @Override
            public double calculateFor(Person p) {
                return (1.0*p.getTotalMessages(RECEIVED_FROM_THEM)) / p.getTotalMessages(SENT_TO_THEM);
            }
        }),
        THEY_TEXT_LESS("Unrequited Love", "your texts/their texts", new Formula() {
            @Override
            public double calculateFor(Person p) {
                return (1.0*p.getTotalMessages(SENT_TO_THEM)) / p.getTotalMessages(RECEIVED_FROM_THEM);
            }
        }),
        LAUGHER(3, "The Laugher", LAUGH_TAGS, "laughs/1000 texts", new Formula() {
            @Override
            public double calculateFor(Person p) {
                return (1000.0*p.specialCounts[3]) / (p.getTotalMessages(RECEIVED_FROM_THEM));
            }
        }),
        KNOW_NOTHING(4, "The Know Nothing", KNOW_NOTHING_TAGS, "not-answers/1000 texts", new Formula() {
            @Override
            public double calculateFor(Person p) {
                return (1000.0*p.specialCounts[4]) / (p.getTotalMessages(RECEIVED_FROM_THEM));
            }
        }),
        MISS_BASIC(5, "Miss Basic", BASIC_TAGS, "basic clues/1000 texts", new Formula() {
            @Override
            public double calculateFor(Person p) {
                return (1000.0*p.specialCounts[5]) / (p.getTotalMessages(RECEIVED_FROM_THEM));
            }
        }),
        LOTS_OF_TEXTS("Trigger Happy", "texts/ 1000 words", new Formula() {
            @Override
            public double calculateFor(Person p) {
                return (1000.0*p.getTotalMessages(RECEIVED_FROM_THEM)) / (p.getTotalWords(RECEIVED_FROM_THEM));
            }
        }),
        LONG_TEXTS("The Novelist", "words/text", new Formula() {
            @Override
            public double calculateFor(Person p) {
                return (1.0*p.getTotalWords(RECEIVED_FROM_THEM)) / p.getTotalMessages(RECEIVED_FROM_THEM);
            }
        }),
        SLOW_TEXTER("The Snail", "minutes to respond", new Formula() {
            @Override
            public double calculateFor(Person p) {
                return -1.0 * p.getAverageResponseTime() / 60;
            }
        });

        public final String name;
        public final String description;
        public final Integer index;
        public final Formula formula;
        public final List<String> tags;

        Category(String name, String description, Formula formula) {
            this(null, name, null, description, formula);
        }

        Category(Integer index, String name, List<String> tags, String description, Formula formula) {
            this.index = index;
            this.name = name;
            this.description = description;
            this.tags = tags;
            this.formula = formula;
        }
    }


    private String ID;
    private String personName;
    private String myContact;
    private long[] specialCounts = new long[6];

    // Counts of stuff
    private long[] countMessages = new long[] {0, 0};
    private long[] countWords = new long[] {0, 0};
    private long[] countChars = new long[] {0, 0};

    // Stats for Response Time
    private boolean meSentLast = false;
    private Long lastSentToThemTime = null;
    private BigInteger totalResponseTime = BigInteger.ZERO;
    private int numResponseInstances = 0;



    public Person(String number, String inputName, String ID) {
        this.myContact = number;
        this.personName = inputName;
        this.ID = ID;
    }

    public String getName() {
        return personName;
    }
    public String getNumber(){ return myContact;}
    public String getID() { return ID; }

    /**
     * Updates all my parameters.
     * @param textMessage the text message
     */
    public void update(SMSData textMessage) {
        boolean meSentToThem = textMessage.getFolderName().equals(SMSData.OUTBOX);
        String lowerCaseText = textMessage.getBody().toLowerCase();

        // Response Time
        // If from me to them, then update my (not their) last sent time
        if (meSentToThem) {
            lastSentToThemTime = Long.parseLong(textMessage.getTime());
            meSentLast = true;
        }
        // Else if I've sent them something before, add the time to the list
        else if (meSentLast) {
            long timeDif = Long.parseLong(textMessage.getTime()) - lastSentToThemTime;
            if (timeDif < 172_800_000) { // milliseconds in a day
                totalResponseTime = totalResponseTime.add(BigInteger.valueOf(timeDif / 1000));
                numResponseInstances++;
            }
            meSentLast = false;
        }

        // Update each category
        if (!meSentToThem) {
            for (Category cat : Category.values()) {
                if (cat.index != null) {
                    specialCounts[cat.index] += countTagInstances(lowerCaseText, cat.tags);
                }
            }
        }

        // Update all counts
        countMessagesAndWordsAndChars(lowerCaseText, meSentToThem? SENT_TO_THEM : RECEIVED_FROM_THEM);
    }


    /**
     * ONLY counts alphabetical characters into totalChars
     * TEXT MUST BE IN LOWER CASE!!
     * @param text the text IN LOWER CASE
     */
    public void countMessagesAndWordsAndChars(final String text, final int sentToThem) {
        char c;

        // Clear starting whitespace
        int i = 0;
        while (i < text.length() && !('a' <= (c = text.charAt(i++)) && c <= 'z'));

        // Loop through the rest of the text
        boolean wasChar = false;
        for (; i < text.length(); i++) {
            c = text.charAt(i);

            // Whitespace. Maybe a word!
            if (c == ' ' || c == '\n') {
                if (wasChar) {
                    countWords[sentToThem]++;
                }
                wasChar = false;
            }

            // Alphabetical Character
            else if ('a' <= c && c <= 'z') {
                countChars[sentToThem]++;
                wasChar = true;
            }

            // Numerical, Punctuation, or Something Much Worse
            else {
                wasChar = false;
            }
        }

        // The final word did not have a space after it
        if (wasChar) {
            countWords[sentToThem]++;
        }

        // And this has only been one message
        countMessages[sentToThem]++;
    }


    /**
     *
     * @param category a constant from Person.Category.<Some_Category>.name  <- NO PARENTHESES
     * @return the current correct ratio for the category given
     */
    public double getRatingFor(String category) {
        for (Category cat : Category.values()) {
            if (cat.name.equals(category)) {
                return cat.formula.calculateFor(this);
            }
        }
        throw new IllegalStateException("Should've used the right constant");
    }

    /**
     *
     * @param sentToThem either Person.SENT_TO_THEM or Person.RECEIVED_FROM_THEM
     * @return the total number of text messages
     */
    public long getTotalMessages(int sentToThem) {
        return countMessages[sentToThem];
    }

    /**
     *
     * @param sentToThem either Person.SENT_TO_THEM or Person.RECEIVED_FROM_THEM
     * @return the total number of words
     */
    public long getTotalWords(int sentToThem) {
        return countWords[sentToThem];
    }

    /**
     *
     * @param sentToThem either Person.SENT_TO_THEM or Person.RECEIVED_FROM_THEM
     * @return the total number of alphabetic characters
     */
    public long getTotalChars(int sentToThem) {
        return countChars[sentToThem];
    }


    public double getAverageResponseTime() {
        if (numResponseInstances == 0) {
            return 0;
        }
        return totalResponseTime.divide(BigInteger.valueOf(numResponseInstances)).longValue();
    }

    /**
     * TEXT BETTER BE IN LOWER CASE BY NOW!
     * @param text MUST BE IN LOWER CASE
     * @param tags MUST ALSO BE IN LOWER CASE
     * @return the number of times text contained a tag
     */
    private static final double countTagInstances(String text, final List<String> tags) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            for (String tag : tags) {
                if (text.startsWith(tag, i)) {
                    count++;
                }
            }
        }
        return count;
    }


    public String getStringRepresentation() {
        String s = "";
        s += ID + ";" + personName + ";" + myContact + ";";
        for (int i = 0; i < 6; i++) {
            s += specialCounts[i] + ";";
        }
        s += countMessages[0] +";" + countMessages[1] + ";";
        s += countWords[0] +";" + countWords[1] + ";";
        s += countChars[0] +";" + countChars[1];

        return s;
    }

    public void setSpecialCounts(long a, long b, long c, long d, long e, long f) {
        specialCounts = new long[] {a, b, c, d, e, f};
    }
    public void setCountMessages(long a, long b) {
        countMessages = new long[] {a, b};
    }
    public void setCountWords(long a, long b) {
        countWords = new long[] {a, b};
    }
    public void setCountChars(long a, long b) {
        countChars = new long[] {a, b};
    }

}
