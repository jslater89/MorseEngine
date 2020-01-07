package com.manywords.softworks.morse;

import java.util.*;

/**
 * Created by jay on 3/6/17.
 */
class MorseFallbackDecoder {
    private List<MorseSignal> mSignal;

    private MorseSpeed mCurrentMarkSpeed;

    private boolean hasCandidateCharacter = false;
    private String mCandidateCharacter = null;

    private boolean hasCandidateProsign = false;
    private MorseProsign mCandidateProsign = null;

    // Derived from marks
    private MorseSpeed mEstimatedMarkSpeed;

    // Derived from marks, or from silences
    private MorseSpeed mEstimatedCharSpeed;
    private MorseSpeed mEstimatedWordSpeed;

    private MorseStats mStats;

    MorseFallbackDecoder(List<MorseSignal> signal, MorseSpeed currentSpeed, MorseStats stats, String candidateChar, MorseProsign candidateProsign) {
        mSignal = signal;
        mCurrentMarkSpeed = currentSpeed;
        mStats = stats;

        mCandidateCharacter = candidateChar;
        if(candidateChar != null) hasCandidateCharacter = true;

        mCandidateProsign = candidateProsign;
        if(candidateProsign != null) hasCandidateProsign = true;
    }

    private boolean debug = false;

    // Do mean-shift clustering to determine speeds
    private boolean analyze() {
        int min = Integer.MAX_VALUE;
        int max = 0;
        for(MorseSignal s : mSignal) {
            if(s.duration < min) min = (int) s.duration;

            // don't let silences set the max
            if(s.duration > max && s.on) max = (int) s.duration;
        }

        int arrayMin = 0;
        max = (int) (max * 1.1);
        int range = (max - arrayMin);
        int[] histogram = new int[range];

        for(MorseSignal s : mSignal) {
            // we ditch long silences
            if(s.duration < histogram.length) histogram[(int) s.duration]++;
        }

        if(debug) System.out.println("Expected dot speed: " + mCurrentMarkSpeed.dotMsec);
        if(debug) System.out.println("Min/max " + min + "/" + max);
        if(debug) System.out.println("Histogram: " + Arrays.toString(histogram));

        final int windowRadius = Math.max(min, 75);
        final int mergeRadius = windowRadius / 3;
        //System.out.println("Window size: " + windowRadius);
        List<Window> windows = new ArrayList<>();
        for(int i = arrayMin; i < max; i += windowRadius) {
            if(countHistogramPoints(histogram, i, windowRadius) > 0) windows.add(new Window(i, windowRadius));
        }

        int trials = 0;
        while(true) {
            boolean converged = true;

            for(Window w : windows) {
                int oldCenter = w.center;
                w.center = averageHistogramPoints(histogram, w.center, w.radius);

                if(oldCenter != w.center) converged = false;
            }

            Map<Integer, List<Window>> windowMap = new HashMap<>();

            for(Window w : windows) {
                if(windowMap.get(w.center) == null) windowMap.put(w.center, new ArrayList<Window>());

                windowMap.get(w.center).add(w);
            }

            for(List<Window> l : windowMap.values()) {
                for(int i = 0; i < l.size() - 1; i++) {
                    windows.remove(l.get(i));
                }
            }

            trials++;
            if(converged || trials > 100) break;
        }

        List<Window> toMerge = new ArrayList<>();
        List<Window> merged = new ArrayList<>();
        for(int i = 0; i < windows.size(); i++) {
            Window current = windows.get(i);
            Window nextNeighbor;

            boolean finishMerge = false;
            if(windows.size() > i+1) {
                nextNeighbor = windows.get(i+1);
                if(nextNeighbor.center < current.center + mergeRadius) {
                    toMerge.add(nextNeighbor);
                    if(!merged.contains(current)) merged.add(current);
                }
                else {
                    finishMerge = true;
                }
            }
            else finishMerge = true;

            if(finishMerge) {
                if(toMerge.size() == 0 && !merged.contains(current)) merged.add(current);
                else if(toMerge.size() > 0) {
                    merged.add(mergeWindows(toMerge));
                }
            }
        }

        if(debug) System.out.println("After " + trials + " trials: " + merged);

        if(merged.size() == 1) {
            int estimate = merged.get(0).center;
            if(estimate > MorseKey.FUDGE_FACTOR * mCurrentMarkSpeed.dashMsec) {
                estimate /= 3;
            }

            setEstimatedSpeeds(estimate);
        }
        else if(merged.size() == 2) {
            int dotCandidateMsec = merged.get(0).center;
            int dashCandidateMsec = merged.get(1).center;

            // ignore a too-short dash candidate (?)
            if(dashCandidateMsec < 2 * dotCandidateMsec) {
                setEstimatedSpeeds(dotCandidateMsec);
            }
            else {
                int average = (dotCandidateMsec + (dashCandidateMsec / 3)) / 2;
                setEstimatedSpeeds(average);
            }
        }
        else {
            // round up
            int median = merged.get(merged.size() / 2).center;

            int count = 0;
            int estimate = 0;
            for(Window w : merged) {
                if(w.center < median) {
                    estimate += w.center;
                    count++;
                }
            }

            estimate = estimate / count;

            if(merged.get(0).center > 0.66 * median && estimate > MorseKey.FUDGE_FACTOR * mCurrentMarkSpeed.dashMsec) {
                estimate /= 3;
                setEstimatedSpeeds(estimate);
            }
            else {
                setEstimatedSpeeds(estimate);
            }
        }

        if(mEstimatedCharSpeed == null || mEstimatedMarkSpeed == null || mEstimatedWordSpeed == null) {
            throw new IllegalStateException();
        }

        return true;
    }

    private Window mergeWindows(List<Window> toMerge) {
        int total = 0;
        for(Window w : toMerge) {
            total += w.center;
        }

        return new Window(total / toMerge.size(), toMerge.get(0).radius);
    }

    private int countHistogramPoints(int[] histogram, int center, int radius) {
        int start = Math.max(0, center - radius);
        int end = Math.min(histogram.length, center + radius);
        int count = 0;
        for(int i = start; i < end; i++) {
            if(histogram[i] > 0) count++;
        }

        return count;
    }

    private int averageHistogramPoints(int[] histogram, int center, int radius) {
        int start = Math.max(0, center - radius);
        int end = Math.min(histogram.length, center + radius);
        int count = 0;
        int sum = 0;
        for(int i = start; i < end; i++) {
            if(histogram[i] > 0) {
                count++;
                sum += i;
            }
        }

        return (int) Math.round((double) sum / (double) count);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    private static class Window {
        int center;
        int radius;

        Window(int center, int radius) { this.center = center; this.radius = radius; }

        @Override
        public String toString() {
            return "W:" + center;
        }
    }

    private void setEstimatedSpeeds(int dotMsec) {
        if(debug) System.out.println("Estimated dot speed: " + dotMsec);
        mEstimatedMarkSpeed = new MorseSpeed(dotMsec);
        mEstimatedCharSpeed = new MorseSpeed((int) (dotMsec * 1.1));
        mEstimatedWordSpeed = new MorseSpeed((int) (dotMsec * 1.1));
    }

    /**
     *
     * @return A list of MorseCharacters if the decoder thinks the fallback should be used.
     * Null otherwise.
     */
    List<MorseCharacter> decode() {
        if(!analyze()) return null;

        List<List<MorseSignal>> separatedSignal = new ArrayList<>();
        List<MorseCharacter> result = new ArrayList<>();

        List<MorseSignal> character = new ArrayList<>();

        int separationDuration;

        // If we're close to the expected speed, allow shorter character separators.
        if(mEstimatedMarkSpeed.dotMsec > mCurrentMarkSpeed.dotMsec * 0.75 && mCurrentMarkSpeed.dotMsec * 1.25 > mEstimatedMarkSpeed.dotMsec) {
            separationDuration = (int) (mEstimatedCharSpeed.dotMsec * 1.5);
        }
        else {
            separationDuration = (int) (mEstimatedCharSpeed.dashMsec * MorseKey.FUDGE_FACTOR);
        }

        boolean initial = true;
        for(MorseSignal signal : mSignal) {
            if(!initial && !signal.on && signal.duration > separationDuration) {
                separatedSignal.add(character);

                character = new ArrayList<>();
                character.add(signal);

                character = new ArrayList<>();
            }
            else {
                character.add(signal);
            }

            initial = false;
        }

        if(character.size() > 0) {
            separatedSignal.add(character);
        }

        for(List<MorseSignal> c : separatedSignal) {
            if(c.isEmpty() || (c.size() == 1 && !c.get(0).on)) {
                result.add(new MorseCharacter(" "));
                continue;
            }

            int[] dotDashArray = MorseSignal.toDotDashArray(c, mEstimatedMarkSpeed, mStats);
            String lookupResult = MorseConstants.lookup(dotDashArray);
            if(!lookupResult.isEmpty()) {
                result.add(new MorseCharacter(lookupResult));
            }
        }

        // If the fallback decoder decoded the same thing as the ordinary decoder, stay with the ordinary
        // decoder for stats/adaptive speed purposes
        if(result.size() == 1) {
            if(hasCandidateCharacter && mCandidateCharacter.equals(result.get(0).character)) {
                return null;
            }
            if(hasCandidateProsign && mCandidateProsign.equals(result.get(0).prosign)) {
                return null;
            }
        }

        return result;
    }

    public MorseSpeed getEstimatedMarkSpeed() {
        return mEstimatedMarkSpeed;
    }

    public MorseSpeed getEstimatedCharSpeed() {
        return mEstimatedCharSpeed;
    }

    public MorseSpeed getEstimatedWordSpeed() {
        return mEstimatedWordSpeed;
    }
}
