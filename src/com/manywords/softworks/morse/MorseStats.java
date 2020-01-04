package com.manywords.softworks.morse;

/**
 * Statistics on recent Morse signal elements.
 */
public class MorseStats {
    private static final double ROLLING_LENGTH = 15;

    private double dotAverage;
    private double dashAverage;
    private double markAverage;
    private double charAverage;
    private double wordAverage;

    private double wordsPerMinute;
    private long charStart;

    private int dotsSeen = 0;
    private int dashesSeen = 0;
    private int markSeen = 0;

    boolean canAdapt() {
        return dotsSeen > 5 && dashesSeen > 5 && markSeen > 5;
    }

    double addDot(long duration) {
        dotsSeen++;
        if(dotAverage == 0) {
            dotAverage = (double) duration;
        }
        else {
            dotAverage = dotAverage * ((ROLLING_LENGTH - 1) / ROLLING_LENGTH) + (double) duration * (1 / ROLLING_LENGTH);
        }
        return dotAverage;
    }

    double addDash(long duration) {
        dashesSeen++;
        if(dashAverage == 0) {
            dashAverage = (double) duration;
        }
        else {
            dashAverage = dashAverage * ((ROLLING_LENGTH - 1) / ROLLING_LENGTH) + (double) duration * (1 / ROLLING_LENGTH);
        }
        return dashAverage;
    }

    double addMarkSilence(long duration) {
        markSeen++;
        if(markAverage == 0) {
            markAverage = duration;
        }
        else {
            markAverage = markAverage * ((ROLLING_LENGTH - 1) / ROLLING_LENGTH) + (double) duration * (1 / ROLLING_LENGTH);
        }
        return markAverage;
    }

    double addCharSilence(long duration) {
        if(charAverage == 0) {
            charAverage = (double) duration;
        }
        else {
            charAverage = charAverage * ((ROLLING_LENGTH - 1) / ROLLING_LENGTH) + (double) duration * (1 / ROLLING_LENGTH);
        }
        return charAverage;
    }

    double addWordSilence(long duration) {
        if(wordAverage == 0) {
            wordAverage = (double) duration;
        }
        else {
            wordAverage = wordAverage * ((ROLLING_LENGTH - 1) / ROLLING_LENGTH) + (double) duration * (1 / ROLLING_LENGTH);
        }
        return wordAverage;
    }

    /**
     *
     * @return The average length of recent dots in milliseconds.
     */
    public double getDotAverage() {
        return dotAverage;
    }

    /**
     *
     * @return The average length of recent dashes in milliseconds.
     */
    public double getDashAverage() {
        return dashAverage;
    }

    /**
     *
     * @return The average length of recent intra-character silences in milliseconds.
     */
    public double getMarkAverage() {
        return markAverage;
    }

    /**
     *
     * @return The average length of recent inter-character silences in milliseconds.
     */
    public double getCharAverage() {
        return charAverage;
    }

    /**
     *
     * @return The average length of recent inter-word silences in milliseconds.
     */
    public double getWordAverage() {
        return wordAverage;
    }

    void reset() {
        reset(new MorseSpeed(0));
    }

    void reset(MorseSpeed speed) {
        reset(speed, speed, speed);
    }

    void reset(MorseSpeed markSpeed, MorseSpeed charSpeed, MorseSpeed wordSpeed) {
        dotsSeen = dashesSeen = markSeen = 0;

        dotAverage = markSpeed.dotMsec;
        dashAverage = markSpeed.dashMsec;
        markAverage = markSpeed.dotMsec;

        charAverage = charSpeed.dashMsec;
        wordAverage = wordSpeed.getSpaceLength();
    }
}
