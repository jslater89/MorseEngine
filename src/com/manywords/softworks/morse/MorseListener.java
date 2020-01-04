package com.manywords.softworks.morse;

/**
 * A callback by which clients receive notification of Morse events.
 */
public interface MorseListener {
    /**
     * The Morse key has received a Morse character.
     * @param c The received character.
     */
    public void morseReceived(MorseCharacter c);
}
