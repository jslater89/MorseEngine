package com.manywords.softworks.morse;

/**
 * Prosigns, or procedural signs, are (generally) non-text Morse characters with a
 * specific meaning.
 */
public enum MorseProsign {
    /**
     * Start a new line.
     */
    NEWLINE,
    /**
     * Message is ending.
     */
    END_OF_MESSAGE,
    /**
     * Wait.
     */
    WAIT,
    /**
     * Done sending for now.
     */
    BREAK,
    /**
     * Start a new paragraph.
     */
    PARAGRAPH,
    /**
     * Station closing.
     */
    CLEAR,
    /**
     * Important transmission commencing.
     */
    START_COPYING,
    /**
     * Distress signal.
     */
    SOS,
    /**
     * The previous word or code group was incorrect. Correct text follows.
     */
    STRIKE,
}
