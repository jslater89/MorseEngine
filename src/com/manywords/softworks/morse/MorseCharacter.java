package com.manywords.softworks.morse;

/**
 * Represents a single Morse character, either a printing character or a {@link MorseProsign prosign}.
 */
public class MorseCharacter {
    /**
     * A space character.
     */
    public static final MorseCharacter SPACE = new MorseCharacter(" ");
    /**
     * A newline character.
     */
    public static final MorseCharacter NEWLINE = new MorseCharacter(MorseProsign.NEWLINE);
    /**
     * A paragraph character.
     */
    public static final MorseCharacter PARAGRAPH = new MorseCharacter(MorseProsign.PARAGRAPH);

    /**
     * If this Morse character represents a printing character, this field will be equal to
     * that character. If this Morse character represents a prosign, this field will be null.
     */
    public final String character;

    /**
     * If this Morse character represents a prosign, this field will be equal to that
     * prosign. If this Morse character represents a printing character, this field will be
     * null.
     */
    public final MorseProsign prosign;

    public MorseCharacter(String character) {
        this.character = character;
        prosign = null;
    }

    public MorseCharacter(MorseProsign prosign) {
        this.prosign = prosign;
        character = null;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof MorseCharacter)) return false;

        MorseCharacter other = (MorseCharacter) obj;
        if(character == null) return other.character == null && other.prosign == prosign;
        else return other.prosign == null && character.equals(other.character);
    }

    /**
     *
     * @return True if this character is Morse white space (an inter-word space, a newline, or a paragraph).
     */
    public boolean isWhitespace() {
        return equals(SPACE) || equals(NEWLINE) || equals(PARAGRAPH);
    }
}
