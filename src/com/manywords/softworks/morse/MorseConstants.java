package com.manywords.softworks.morse;

/**
 * Useful constants and lookup functions.
 */
class MorseConstants {
    public static final int DOT = 1;
    public static final int DASH = 3;
    public static final int SPACE_LENGTH = 7;
    public static final int[] SPACE = { SPACE_LENGTH };

    public static final int[] A = { DOT, DASH };
    public static final int[] B = { DASH, DOT, DOT, DOT };
    public static final int[] C = { DASH, DOT, DASH, DOT };
    public static final int[] D = { DASH, DOT, DOT };
    public static final int[] E = { DOT };
    public static final int[] F = { DOT, DOT, DASH, DOT };
    public static final int[] G = { DASH, DASH, DOT} ;
    public static final int[] H = { DOT, DOT, DOT, DOT };
    public static final int[] I = { DOT, DOT };
    public static final int[] J = { DOT, DASH, DASH, DASH };
    public static final int[] K = { DASH, DOT, DASH };
    public static final int[] L = { DOT, DASH, DOT, DOT };
    public static final int[] M = { DASH, DASH };
    public static final int[] N = { DASH, DOT };
    public static final int[] O = { DASH, DASH, DASH };
    public static final int[] P = { DOT, DASH, DASH, DOT };
    public static final int[] Q = { DASH, DASH, DOT, DASH };
    public static final int[] R = { DOT, DASH, DOT };
    public static final int[] S = { DOT, DOT, DOT };
    public static final int[] T = { DASH };
    public static final int[] U = { DOT, DOT, DASH };
    public static final int[] V = { DOT, DOT, DOT, DASH };
    public static final int[] W = { DOT, DASH, DASH };
    public static final int[] X = { DASH, DOT, DOT, DASH};
    public static final int[] Y = { DASH, DOT, DASH, DASH };
    public static final int[] Z = { DASH, DASH, DOT, DOT };

    public static final int[] ZERO = { DASH, DASH, DASH, DASH, DASH };
    public static final int[] ONE = { DOT, DASH, DASH, DASH, DASH };
    public static final int[] TWO = { DOT, DOT, DASH, DASH, DASH };
    public static final int[] THREE = { DOT, DOT, DOT, DASH, DASH };
    public static final int[] FOUR = { DOT, DOT, DOT, DOT, DASH };
    public static final int[] FIVE = { DOT, DOT, DOT, DOT, DOT };
    public static final int[] SIX = { DASH, DOT, DOT, DOT, DOT };
    public static final int[] SEVEN = { DASH, DASH, DOT, DOT, DOT };
    public static final int[] EIGHT = { DASH, DASH, DASH, DOT, DOT };
    public static final int[] NINE = { DASH, DASH, DASH, DASH, DOT };

    public static final int[] STOP = { DOT, DASH, DOT, DASH, DOT, DASH };
    public static final int[] COMMA = { DASH, DASH, DOT, DOT, DASH, DASH };
    public static final int[] COLON = { DASH, DASH, DASH, DOT, DOT, DOT };
    public static final int[] QUERY = { DOT, DOT, DASH, DASH, DOT, DOT };
    public static final int[] APOSTROPHE = { DOT, DASH, DASH, DASH, DASH, DOT };
    public static final int[] HYPHEN = { DASH, DOT, DOT, DOT, DOT, DASH };
    public static final int[] SLASH = { DASH, DOT, DOT, DASH, DOT };
    public static final int[] PAREN = { DASH, DOT, DASH, DASH, DOT, DASH };
    public static final int[] QUOTE = { DOT, DASH, DOT, DOT, DASH, DOT };
    public static final int[] AT = { DOT, DASH, DASH, DOT, DASH, DOT };
    public static final int[] EQUALS = { DASH, DOT, DOT, DOT, DASH };


    public static final int[][] ALL_SIGNALS = {
            A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z,
            ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE,
            STOP, COMMA, COLON, QUERY, APOSTROPHE, HYPHEN, SLASH, PAREN,
            QUOTE, AT, EQUALS
    };
    public static final String SIGNAL_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "0123456789" +
            ".,:?'-/|\"@=";

    public static final int[] NEWLINE = { DOT, DASH, DOT, DASH };
    public static final int[] END_OF_MESSAGE = { DOT, DASH, DOT, DASH, DOT };
    public static final int[] WAIT = { DOT, DASH, DOT, DOT, DOT };
    public static final int[] BREAK = { DASH, DOT, DOT, DOT, DASH, DOT, DASH };
    public static final int[] PARAGRAPH = { DASH, DOT, DOT, DOT, DASH };
    public static final int[] CLEAR = { DASH, DOT, DASH, DOT, DOT, DASH, DOT, DOT };
    public static final int[] START_COPYING = { DASH, DOT, DASH, DOT, DASH };
    public static final int[] SOS = { DOT, DOT, DOT, DASH, DASH, DASH, DOT, DOT, DOT };
    public static final int[] STRIKE = { DOT, DOT, DOT, DOT, DOT, DOT, DOT, DOT };

    public static final int[][] ALL_PROSIGNS = {
            NEWLINE,
            END_OF_MESSAGE,
            WAIT,
            BREAK,
            PARAGRAPH,
            CLEAR,
            START_COPYING,
            SOS,
            STRIKE,
    };

    public static final MorseProsign[] PROSIGN_CONSTANTS = {
            MorseProsign.NEWLINE,
            MorseProsign.END_OF_MESSAGE,
            MorseProsign.WAIT,
            MorseProsign.BREAK,
            MorseProsign.PARAGRAPH,
            MorseProsign.CLEAR,
            MorseProsign.START_COPYING,
            MorseProsign.SOS,
            MorseProsign.STRIKE,
    };

    public static final int PARIS_LENGTH = signalLength(P, A, R, I, S, SPACE);

    public static boolean isSpace(int[] signal) {
        if(signal.length == 1 && signal[0] == SPACE_LENGTH) return true;
        return false;
    }

    private static final MorseTree MORSE_TREE = new MorseTree(ALL_SIGNALS, SIGNAL_CHARS, ALL_PROSIGNS, PROSIGN_CONSTANTS);

    public static MorseProsign lookupProsign(int[] signal) {
        return MORSE_TREE.lookupProsign(signal);
    }

    public static int[] lookupProsign(String c) {
        if(c.equals("\n")) {
            return NEWLINE;
        }
        else if(c.equals("\t")) {
            return PARAGRAPH;
        }
        return null;
    }

    public static int[] lookupProsign(MorseProsign prosign) {
        switch(prosign) {
            case NEWLINE:
                return NEWLINE;
            case END_OF_MESSAGE:
                return END_OF_MESSAGE;
            case WAIT:
                return WAIT;
            case BREAK:
                return BREAK;
            case PARAGRAPH:
                return PARAGRAPH;
            case CLEAR:
                return CLEAR;
            case START_COPYING:
                return START_COPYING;
            case SOS:
                return SOS;
            case STRIKE:
                return STRIKE;
        }
        return null;
    }

    public static String lookup(int[] signal) {
        return MORSE_TREE.lookup(signal);
    }

    public static int[] lookup(String character) {
        int index = SIGNAL_CHARS.indexOf(character);
        if(index < 0) return null;
        else return ALL_SIGNALS[index];
    }

    public static int signalLength(int[]... signal) {
        int length = 0;
        int i = 0;
        for(int[] character : signal) {
            length += charLength(character);
            System.out.println(toString(character) + ": " + charLength(character));

            if(!isSpace(character)) length += DASH; // inter-char length
            //else if(i == signal.length - 1) length -= charLength(character); // if a signal ends in a meaningless space, remove it
            else length -= DASH; // A (dash) A (dash) A (dash) (space): remove extra when we get to a space

            i++;
        }

        return length;
    }

    public static int charLength(int[] character) {
        int length = 0;
        for(int signal : character) {
            length += signal;
            length += DOT; // intra-char length;
        }
        length -= DOT; // remove the extra intra-char space at the end
        return length;
    }

    public static String toString(int[] character) {
        String s = "";
        for(int mark : character) {
            if(mark == DOT) s += ".";
            else s += "-";
        }
        return s;
    }
}
