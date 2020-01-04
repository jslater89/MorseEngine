package com.manywords.softworks.morse;

/**
 * A Morse code speed. Morse speeds come in three flavors.
 * <br /><br />
 * A Morse speed used as a <b>mark speed</b> controls or describes the speed at which
 * intra-character components (dots, dashes, and intra-character silences) are sent or received.
 * <br /><br />
 * A Morse speed used as a <b>character speed</b> controls or describes the speed at which
 * inter-character silences are sent or received. Slower character speeds mean that Morse senders
 * have more time before the Morse key continues to the next character.
 * <br /><br />
 * A Morse speed used as a <b>word speed</b> controls or describes the speed at which inter-word
 * silences are sent or received. Slower word speeds mean that Morse senders have more time before
 * the Morse key inserts a space.
 */
public class MorseSpeed implements Comparable<MorseSpeed> {
    public static class Group {
        MorseSpeed markSpeed;
        MorseSpeed charSpeed;
        MorseSpeed wordSpeed;

        public Group(MorseSpeed speed) {
            this.markSpeed = speed;
            this.charSpeed = speed;
            this.wordSpeed = speed;
        }

        public Group(MorseSpeed markSpeed, MorseSpeed charSpeed, MorseSpeed wordSpeed) {
            this.markSpeed = markSpeed;
            this.charSpeed = charSpeed;
            this.wordSpeed = wordSpeed;
        }
    }

    final int dotMsec;
    final int dashMsec;

    MorseSpeed(int dotMsec) {
        this.dotMsec = dotMsec;
        dashMsec = dotMsec * 3;
    }

    MorseSpeed(MorseSpeed other) {
        this.dotMsec = other.dotMsec;
        this.dashMsec = other.dashMsec;
    }

    int getSpaceLength() {
        return dotMsec * 7;
    }

    /**
     *
     * @return This speed, expressed in words per minute (using PARIS as the reference word).
     */
    public double wordsPerMinute() {
        double dotsPerSecond = dotMsecToDotsPerSec(dotMsec);
        return dotsPerSecToWordsPerMin(dotsPerSecond);
    }

    /**
     *
     * @param wordsPerMinute A speed in words per minute (using PARIS as the reference word).
     * @return A Morse speed corresponding to that speed.
     */
    public static MorseSpeed getSpeedForWPM(double wordsPerMinute) {
        double dotsPerMinute = MorseConstants.PARIS_LENGTH * wordsPerMinute;
        double dotsPerSecond = dotsPerMinute / 60;
        return getSpeedForDotsPerSecond(dotsPerSecond);
    }

    static MorseSpeed getSpeedForDotsPerSecond(double dotsPerSecond) {
        double secondsPerDot = 1 / dotsPerSecond;
        int dotMsec = (int)(secondsPerDot * 1000);

        return new MorseSpeed(dotMsec);
    }

    /**
     * Converts dot length in milliseconds to dots per second.
     *
     * @param dotMsec Dot length in milliseconds.
     * @return Dots per second.
     */
    public static double dotMsecToDotsPerSec(double dotMsec) {
        double secondsPerDot = dotMsec / 1000;
        return 1 / secondsPerDot;
    }

    /**
     * Converts dots per second to words per minute (using PARIS as the reference word).
     * @param dotsPerSecond Dots per second.
     * @return The speed in words per minute.
     */
    public static double dotsPerSecToWordsPerMin(double dotsPerSecond) {
        double dotsPerMinute = dotsPerSecond * 60;
        return dotsPerMinute / MorseConstants.PARIS_LENGTH;
    }

    @Override
    public int compareTo(MorseSpeed o) {
        return dotMsec - o.dotMsec;
    }
}
