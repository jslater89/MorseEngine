package com.manywords.softworks.morse;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Morse signal.
 */
public class MorseSignal {
    /**
     * If on, this Morse signal is a dot or dash. If off, it's silence.
     */
    public final boolean on;

    /**
     * The length in milliseconds of the Morse signal.
     */
    public final long duration;

    public MorseSignal(boolean on, long duration) {
        this.on = on;
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "[" + (on ? "ON:" : "OFF:") + duration + "]";
    }

    static int[] toDotDashArray(List<MorseSignal> currentSignal, MorseSpeed characterSpeed, MorseStats stats) {
        List<Integer> signals = new ArrayList<>();

        for(MorseSignal signal : currentSignal) {
            if(signal.on) {
                if(signal.duration < characterSpeed.dashMsec * MorseKey.FUDGE_FACTOR) {
                    signals.add(MorseConstants.DOT);
                    if(stats != null) stats.addDot(signal.duration);
                }
                else {
                    signals.add(MorseConstants.DASH);
                    if(stats != null) stats.addDash(signal.duration);
                }
            }
        }

        int[] signalArray = new int[signals.size()];
        for(int i = 0; i < signalArray.length; i++) {
            signalArray[i] = signals.get(i);
        }
        return signalArray;
    }
}
