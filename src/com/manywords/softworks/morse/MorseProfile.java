package com.manywords.softworks.morse;

import java.util.ArrayList;
import java.util.List;

/**
 * A Morse profile represents the particular style of a given Morse sender. At present,
 * a Morse profile encapsulates two variables: a speed (or set of speeds), and a variability.
 * <br /><br />
 * Clients may set a single speed, or may set {@link MorseSpeed}s for three separate components
 * of the Morse signal. See {@link MorseSpeed} for more information on Morse speeds.
 * <br /><br />
 * Clients may also set a variability, given as a double between 0 and 1. Each component of the
 * generated signal will be perturbed by +/- variability. For example, a variability of 0.1 means
 * that each component of the generated signal may be up to 10% shorter or longer than the speed
 * settings specify.
 * <br /><br />
 * {@link #MorseProfile()} creates a standard, 12.5 wpm Morse profile with no variability.
 */
public class MorseProfile {
    private static final double DEFAULT_WPM = 12.5;

    private final MorseSpeed dotSpeed;
    private final MorseSpeed dashSpeed;
    private final MorseSpeed markSpeed;
    private final MorseSpeed charSpeed;
    private final MorseSpeed wordSpeed;

    private final double variability;

    /**
     * Initialize this Morse profile with the default speed (12.5 wpm) and
     * no variability.
     */
    public MorseProfile() {
        this(DEFAULT_WPM, 0);
    }

    /**
     * Initialize this Morse profile with the given speed and no variability.
     * @param wpm The speed in words per minute.
     */
    public MorseProfile(double wpm) {
        this(wpm, 0);
    }

    /**
     * Initialize this Morse profile with the given speed and variability.
     * @param wpm The speed in words per minute.
     * @param variability The variability.
     */
    public MorseProfile(double wpm, double variability) {
        dotSpeed = dashSpeed = markSpeed = charSpeed = wordSpeed = MorseSpeed.getSpeedForWPM(wpm);
        this.variability = variability;
    }

    /**
     * Initialize this Morse profile with the given speeds and no
     * variability.
     *
     * See {@link MorseSpeed} for more information on speeds.
     *
     * @param markWpm The mark speed in words per minute.
     * @param charWpm The character speed in words per minute.
     * @param wordWpm The word speed in words per minute.
     */
    public MorseProfile(double markWpm, double charWpm, double wordWpm) {
        this(markWpm, charWpm, wordWpm, 0);
    }

    /**
     * Initialize this Morse profile with the given speeds and variability.
     *
     * See {@link MorseSpeed} for more information on speeds.
     *
     * @param markWpm The mark speed in words per minute.
     * @param charWpm The character speed in words per minute.
     * @param wordWpm The word speed in words per minute.
     * @param variability The variability.
     */
    public MorseProfile(double markWpm, double charWpm, double wordWpm, double variability) {
        dotSpeed = dashSpeed = markSpeed = MorseSpeed.getSpeedForWPM(markWpm);
        charSpeed = MorseSpeed.getSpeedForWPM(charWpm);
        wordSpeed = MorseSpeed.getSpeedForWPM(wordWpm);
        this.variability = variability;
    }

    /**
     * Initialize this Morse profile with no variability, deriving speeds from the given
     * {@link MorseStats} object.
     *
     * @param stats The Morse statistics to imitate.
     */
    public MorseProfile(MorseStats stats) {
        this(stats, 0);
    }

    /**
     * Initialize this Morse profile with no variability, deriving speeds from the given
     * {@link MorseStats} object.
     *
     * @param stats The Morse statistics to imitate.
     * @param variability The variability.
     */
    public MorseProfile(MorseStats stats, double variability) {
        this((int) stats.getDotAverage(), (int) stats.getDashAverage(), (int) stats.getMarkAverage(),
                (int) stats.getCharAverage(), (int) stats.getWordAverage(), variability);
    }

    MorseProfile(int dotMsec, int dashMsec, int markMsec, int charMsec, int wordMsec) {
        this(dotMsec, dashMsec, markMsec, charMsec, wordMsec, 0);
    }

    MorseProfile(int dotMsec, int dashMsec, int markMsec, int charMsec, int wordMsec, double variability) {
        if(dotMsec == 0 && dashMsec == 0) {
            dotSpeed = MorseSpeed.getSpeedForWPM(12.5);
            dashSpeed = MorseSpeed.getSpeedForWPM(12.5);
            markSpeed = MorseSpeed.getSpeedForWPM(12.5);
        }
        else {
            dotSpeed = new MorseSpeed(dotMsec);
            dashSpeed = new MorseSpeed(dashMsec / MorseConstants.DASH);
            markSpeed = new MorseSpeed(markMsec);
        }

        if(charMsec == 0) {
            charMsec = dashMsec;
        }

        charSpeed = new MorseSpeed(charMsec / MorseConstants.DASH);

        if(wordMsec == 0) {
            wordMsec = charSpeed.dotMsec * MorseConstants.SPACE_LENGTH;
        }
        wordSpeed = new MorseSpeed(wordMsec / MorseConstants.SPACE_LENGTH);

        this.variability = variability;
    }

    /**
     * Generate Morse timings for the given string using this profile.
     *
     * @param characters The string to generate Morse timings for.
     * @return The generated timings.
     */
    public List<MorseSignal> generateSignal(List<MorseCharacter> characters) {
        List<MorseSignal> signal = new ArrayList<>(characters.size());

        for(MorseCharacter character : characters) {
            if(character.character != null) {
                if(character.character.equals(" ")) {
                    // Remove the interchar space at the end of the signal, add a word space
                    signal.remove(signal.size() - 1);
                    int length = variation(wordSpeed.getSpaceLength());
                    signal.add(new MorseSignal(false, length));
                }
                else {
                    int[] charSignal = MorseConstants.lookup(character.character);
                    if(charSignal != null) {
                        for (int dotDash : charSignal) {
                            if (dotDash == MorseConstants.DOT) {
                                int length = variation(dotSpeed.dotMsec);
                                signal.add(new MorseSignal(true, length));
                            }
                            else {
                                int length = variation(dashSpeed.dashMsec);
                                signal.add(new MorseSignal(true, length));
                            }

                            int length = variation(markSpeed.dotMsec);
                            signal.add(new MorseSignal(false, length));
                        }
                    }

                    // Remove the intermark space at the end of the signal, add a char space
                    signal.remove(signal.size() - 1);
                    int length = variation(charSpeed.dashMsec);
                    signal.add(new MorseSignal(false, length));
                }
            }
            else if(character.prosign != null) {
                int[] prosignSignal = MorseConstants.lookupProsign(character.prosign);
                if(prosignSignal != null) {
                    for (int dotDash : prosignSignal) {
                        if(dotDash == MorseConstants.DOT) {
                            int length = variation(dotSpeed.dotMsec);
                            signal.add(new MorseSignal(true, length));
                        }
                        else {
                            int length = variation(dashSpeed.dashMsec);
                            signal.add(new MorseSignal(true, length));
                        }

                        int length = variation(markSpeed.dotMsec);
                        signal.add(new MorseSignal(false, length));
                    }

                    // Remove the intermark space at the end of the signal, add a char space
                    signal.remove(signal.size() - 1);
                    int length = variation(charSpeed.dashMsec);
                    signal.add(new MorseSignal(false, length));
                }
            }
        }
        return signal;
    }

    /**
     * Generate Morse timings for the given string using this profile.
     *
     * @param message The string to generate Morse timings for.
     * @return The generated timings.
     */
    public List<MorseSignal> generateSignal(String message) {
        message = message.toUpperCase();

        List<MorseCharacter> characters = new ArrayList<>();

        for(char c : message.toCharArray()) {
            String s = Character.toString(c);

            int[] charSignal = MorseConstants.lookup(s);
            if(s.equals(" ")) {
                characters.add(new MorseCharacter(s));
            }
            else if(charSignal != null) {
                characters.add(new MorseCharacter(s));
            }
            else {
                int[] prosignSignal = MorseConstants.lookupProsign(s);
                if(prosignSignal != null) {
                    characters.add(new MorseCharacter(MorseConstants.lookupProsign(prosignSignal)));
                }
            }
        }

        return generateSignal(characters);
    }

    private int variation(int length) {
        double actualVariation = 1d + (Math.random() * (2 * variability)) - variability;
        return (int) (length * actualVariation);
    }
}
