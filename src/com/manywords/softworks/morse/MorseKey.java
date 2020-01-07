package com.manywords.softworks.morse;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * MorseKey is the entry point into the Morse library.
 *
 */
public class MorseKey {
    static final double FUDGE_FACTOR = 0.75;

    private List<MorseSignal> mCurrentSignal = new ArrayList<>();

    private MorseSpeed mMarkSpeed = MorseSpeed.getSpeedForWPM(12.5);
    private MorseSpeed mCharSpeed = MorseSpeed.getSpeedForWPM(12.5);
    private MorseSpeed mWordSpeed = MorseSpeed.getSpeedForWPM(10);

    private boolean mAdaptiveSpeed = true;

    // Don't change speed if the difference is very small (5%)
    private double mAdaptiveMinMove = 0.05;

    // Don't get more than 20 percent faster or slower at once
    private double mAdaptiveMaxMove = 0.20;

    // Dashes may be at most 1.25x slower than dots
    private double mAdaptiveDifferenceLimit = 1.25;

    private MorseSpeed.Group mSpeedCeiling = new MorseSpeed.Group(MorseSpeed.getSpeedForWPM(25));
    private MorseSpeed.Group mSpeedFloor = new MorseSpeed.Group(MorseSpeed.getSpeedForWPM(7.5));

    private long mMaxInterwordLength = 3000;

    private MorseStats mStats = new MorseStats();

    private Timer mCharTimer = new Timer();
    private Timer mWordTimer = new Timer();

    private boolean mInWord;
    private boolean mInChar;

    private long mLastKeyDown;
    private long mLastKeyUp;
    private String mLastChar = "";
    private MorseProsign mLastProsign;

    private MorseListener mListener;

    private boolean debug = false;

    /**
     *
     * @param listener A {@link MorseListener} to receive events from this Morse key.
     */
    public MorseKey(MorseListener listener) {
        mListener = listener;
    }

    /**
     * Set the speed at which this key expects to receive Morse code.
     * @param speed Set all three controllable speeds to this speed.
     */
    public void setSpeed(MorseSpeed speed) {
        setSpeed(new MorseSpeed.Group(speed));
    }

    /**
     * Set the speed at which this key expects to receive Morse code.
     * @param speed The desired speed group.
     *
     */
    public void setSpeed(MorseSpeed.Group speed) {
        mMarkSpeed = speed.markSpeed;
        mCharSpeed = speed.charSpeed;
        mWordSpeed = speed.wordSpeed;

        capSpeeds();
    }

    /**
     * Set adaptive speed mode.
     * @param adaptive If true, this key will adjust its expected speed based on
     *                 Morse input.
     */
    public void setAdaptiveSpeed(boolean adaptive) {
        mAdaptiveSpeed = adaptive;
    }

    /**
     * Get adaptive speed mode.
     * @return If true, this key is adjusting its expected speed based on Morse input.
     */
    public boolean getAdaptiveSpeed() {
        return mAdaptiveSpeed;
    }


    public MorseSpeed.Group getSpeedCeiling() { return mSpeedCeiling; }
    public void setSpeedCeiling(MorseSpeed.Group ceiling) {
        mSpeedCeiling = ceiling;

        capSpeeds();
    }

    public MorseSpeed.Group getSpeedFloor() { return mSpeedFloor; }
    public void setSpeedFloor(MorseSpeed.Group floor) {
        mSpeedFloor = floor;

        capSpeeds();
    }

    /**
     * Call to signal that this Morse key has been depressed.
     */
    public void down() {
        mLastKeyDown = System.currentTimeMillis();
        long interval = mLastKeyDown - mLastKeyUp;

        if(mLastKeyUp > 0) {
            if (mCurrentSignal.size() > 0) {
                mStats.addMarkSilence(interval);
            }
            else if (!mInChar && mInWord && !mLastChar.isEmpty()) {
                mStats.addCharSilence(interval);
            }
            else if (!mInChar && !mInWord && (interval) < getMaxInterwordLength()) {
                mStats.addWordSilence(interval);

                if(mAdaptiveSpeed && !mLastChar.isEmpty()) {
                    adaptWordSpeed();
                }
            }

            if(interval < getMaxInterwordLength()) {
                mCurrentSignal.add(new MorseSignal(false, interval));
            }
        }

        mInChar = true;
        mInWord = true;

        mCharTimer.cancel();
        mWordTimer.cancel();
    }

    /**
     * Call to signal that this Morse key has been released.
     */
    public void up() {
        mLastKeyUp = System.currentTimeMillis();
        long interval = mLastKeyUp - mLastKeyDown;

        mCurrentSignal.add(new MorseSignal(true, interval));

        mCharTimer = new Timer();
        mWordTimer = new Timer();

        // the char timer is 75% of the char silence length, so we have room to speed up
        mCharTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handleCharDone();
            }
        }, (long) (mCharSpeed.dashMsec * FUDGE_FACTOR));

        // the word timer is 75% of the word silence length, so we have room to speed up
        mWordTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handleWordDone();
            }
        }, (long) (mWordSpeed.dotMsec * MorseConstants.SPACE_LENGTH * FUDGE_FACTOR));
    }

    /**
     *
     * @return Statistics on recent Morse input.
     */
    public MorseStats getStats() { return mStats; }

    /**
     *
     * @return The current mark speed--that is, the speed at which
     * this key expects dots, dashes, and silences to occur.
     */
    public MorseSpeed getMarkSpeed() { return mMarkSpeed; }

    /**
     *
     * @return The current character speed--that is, the speed at
     * which this key expects inter-character silences to occur.
     */
    public MorseSpeed getCharSpeed() { return mCharSpeed; }

    /**
     *
     * @return The current word speed--that is, the speed at which
     * this key expects inter-word silences to occur.
     */
    public MorseSpeed getWordSpeed() { return mWordSpeed; }

    private void handleCharDone() {
        List<MorseSignal> dotDashCopy = new ArrayList<>(mCurrentSignal);
        int[] signalPattern = MorseSignal.toDotDashArray(dotDashCopy, mMarkSpeed, mStats);

        String character = MorseConstants.lookup(signalPattern);
        MorseProsign prosign = MorseConstants.lookupProsign(signalPattern);
        mInChar = false;
        mLastChar = character;

        List<MorseSignal> fallbackCopy = new ArrayList<>(mCurrentSignal);
        MorseFallbackDecoder fallback = new MorseFallbackDecoder(fallbackCopy, getMarkSpeed(), mStats, character, prosign);
        fallback.setDebug(debug);
        List<MorseCharacter> fallbackDecoded = fallback.decode();

        if(fallbackDecoded != null && fallbackDecoded.size() > 0) {
            for(MorseCharacter c : fallbackDecoded)
                mListener.morseReceived(c);

            // TODO: limit to a maxChange change (if needed)
            mMarkSpeed = fallback.getEstimatedMarkSpeed();
            mCharSpeed = fallback.getEstimatedCharSpeed();
            mWordSpeed = fallback.getEstimatedWordSpeed();

            capSpeeds();
        }
        else if(character.isEmpty()) {
            MorseProsign sign = MorseConstants.lookupProsign(signalPattern);
            if(sign != null) {
                mLastChar = "prosign";
                mLastProsign = sign;
                mListener.morseReceived(new MorseCharacter(sign));
            }
        }
        else {
            mListener.morseReceived(new MorseCharacter(character));
        }

        if(mAdaptiveSpeed && !mLastChar.isEmpty()) {
            adaptMarkSpeed();
            adaptCharSpeed();
        }

        mCurrentSignal.clear();
    }

    private void handleWordDone() {
        mInWord = false;
        if(mLastChar.equals("prosign")) {
            if(mLastProsign == MorseProsign.NEWLINE || mLastProsign == MorseProsign.PARAGRAPH || mLastProsign == MorseProsign.STRIKE) {
                return;
            }
        }

        mListener.morseReceived(new MorseCharacter(" "));
    }


    private void adaptMarkSpeed() {
        if(!mStats.canAdapt()) return;

        // --- be adaptive in symbols ---
        double intermarkLength = mStats.getMarkAverage();
        double dotLength = mStats.getDotAverage();
        double dashLength = mStats.getDashAverage() / 3d;

        double markAverage = (intermarkLength + dotLength + dashLength) / 3;

        // The sign of the speed difference is the operation we need to do
        // on current speed.
        // current speed too slow (markAverage > dotMsec): subtract from dotMsec
        // current speed too fast (markAverage < dotMsec): add to dotMsec
        double speedDifference = markAverage - mMarkSpeed.dotMsec;

        // Only adjust if the difference is greater than the fudge factor, and always adjust by
        // the difference
        if(Math.abs(speedDifference) > Math.abs(mMarkSpeed.dotMsec * mAdaptiveMinMove)) {
            int newSpeed = (int) (mMarkSpeed.dotMsec + speedDifference);
            mMarkSpeed = changeSpeedWithCap(mMarkSpeed, newSpeed);
        }

        capSpeeds();
    }

    private void adaptCharSpeed() {
        if(!mStats.canAdapt()) return;

        // --- be adaptive between symbols ---
        // It's a dash, so express it as dots

        if(mCharSpeed.dotMsec > mMarkSpeed.dotMsec * mAdaptiveDifferenceLimit) {
            int speed = (int) (mMarkSpeed.dotMsec * mAdaptiveDifferenceLimit);
            mCharSpeed = new MorseSpeed(speed);
        }
        else if(mCharSpeed.dotMsec < mMarkSpeed.dotMsec) {
            mCharSpeed = new MorseSpeed(mMarkSpeed);
        }
        else {
            double charSilenceLength = mStats.getCharAverage() / 3;
            double speedDifference = charSilenceLength - mCharSpeed.dotMsec;

            // Only adjust if the difference is greater than the fudge factor
            if (Math.abs(speedDifference) > Math.abs(mCharSpeed.dotMsec * mAdaptiveMinMove)) {
                int newSpeed = (int) (mCharSpeed.dotMsec + speedDifference);

                // Dash length no more than 125% dot length
                if (newSpeed < mMarkSpeed.dotMsec * mAdaptiveDifferenceLimit && newSpeed > mMarkSpeed.dotMsec) {
                    mCharSpeed = changeSpeedWithCap(mCharSpeed, newSpeed);
                }
            }
        }

        capSpeeds();
    }

    private void adaptWordSpeed() {
        if(!mStats.canAdapt()) return;

        // --- be adaptive between symbols ---
        if(mWordSpeed.dotMsec < mCharSpeed.dotMsec) {
            // word speed should never be faster than char speed
            int speed = mCharSpeed.dotMsec;
            mWordSpeed = new MorseSpeed(speed);
        }
        else if(mWordSpeed.dotMsec > mCharSpeed.dotMsec * mAdaptiveDifferenceLimit) {
            // word speed should never be too much slower than char speed
            int speed = (int) (mWordSpeed.dotMsec * mAdaptiveDifferenceLimit);
            mWordSpeed = new MorseSpeed(speed);
        }
        else {
            double wordSilenceLength = mStats.getWordAverage() / 7;
            if (wordSilenceLength == 0) return;

            double speedDifference = wordSilenceLength - mWordSpeed.dotMsec;

            // Only adjust if the difference is greater than the min move
            if (Math.abs(speedDifference) > Math.abs(mWordSpeed.dotMsec * mAdaptiveMinMove)) {
                int newSpeed = (int) (mWordSpeed.dotMsec + speedDifference);

                // Dot length no more than (adaptive difference limit)*dot length
                if (newSpeed < mCharSpeed.dotMsec * mAdaptiveDifferenceLimit && newSpeed > mCharSpeed.dotMsec)
                    mWordSpeed = changeSpeedWithCap(mWordSpeed, newSpeed);
            }
        }

        capSpeeds();
    }

    private MorseSpeed changeSpeedWithCap(MorseSpeed speed, int dotMsec) {
        int minSpeed = (int)(speed.dotMsec * (1 - mAdaptiveMaxMove));
        int maxSpeed = (int)(speed.dotMsec * (1 + mAdaptiveMaxMove));

        if(dotMsec > maxSpeed) return new MorseSpeed(maxSpeed);
        if(dotMsec < minSpeed) return new MorseSpeed(minSpeed);
        return new MorseSpeed(dotMsec);
    }

    private void capSpeeds() {
        if(mWordSpeed.wordsPerMinute() > mSpeedCeiling.wordSpeed.wordsPerMinute()) mWordSpeed = mSpeedCeiling.wordSpeed;
        if(mCharSpeed.wordsPerMinute() > mSpeedCeiling.charSpeed.wordsPerMinute()) mCharSpeed = mSpeedCeiling.charSpeed;
        if(mMarkSpeed.wordsPerMinute() > mSpeedCeiling.markSpeed.wordsPerMinute()) mMarkSpeed = mSpeedCeiling.markSpeed;

        if(mWordSpeed.wordsPerMinute() < mSpeedFloor.wordSpeed.wordsPerMinute()) mWordSpeed = mSpeedFloor.wordSpeed;
        if(mCharSpeed.wordsPerMinute() < mSpeedFloor.charSpeed.wordsPerMinute()) mCharSpeed = mSpeedFloor.charSpeed;
        if(mMarkSpeed.wordsPerMinute() < mSpeedFloor.markSpeed.wordsPerMinute()) mMarkSpeed = mSpeedFloor.markSpeed;
    }

    private long getMaxInterwordLength() {
        return (long) Math.max(mMaxInterwordLength, mWordSpeed.dotMsec * 7 * 1.5);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
