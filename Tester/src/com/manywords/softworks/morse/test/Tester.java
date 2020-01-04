package com.manywords.softworks.morse.test;

import com.manywords.softworks.morse.*;
import layout.TableLayout;
import layout.TableLayoutConstants;
import layout.TableLayoutConstraints;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jay on 2/14/17.
 */
public class Tester {
    public static void main(String[] args) {

        //showUI();
        //Beeper.main(args);

        new Tester();
    }

    private MorseKey mKey;

    private JTextField mDotLengthField;
    private JTextField mDotActualWpmField;
    private JTextField mDotSystemWpmField;

    private JTextField mDashLengthField;
    private JTextField mDashActualWpmField;
    private JTextField mDashSystemWpmField;

    private JTextField mIntermarkLengthField;
    private JTextField mIntermarkActualWpmField;
    private JTextField mIntermarkSystemWpmField;

    private JTextField mIntercharLengthField;
    private JTextField mIntercharActualWpmField;
    private JTextField mIntercharSystemWpmField;

    private JTextField mInterwordLengthField;
    private JTextField mInterwordActualWpmField;
    private JTextField mInterwordSystemWpmField;

    private JButton mCodeKey;
    private JTextArea mInOut;

    private Timer mPlaybackTimer;

    private Tester() {
        mKey = new MorseKey(mMorseListener);
        mKey.setSpeed(new MorseSpeed.Group(MorseSpeed.getSpeedForWPM(5)));
        mKey.setSpeedFloor(new MorseSpeed.Group(MorseSpeed.getSpeedForWPM(5)));

        setupBeep();

        setupUi();
        java.util.Timer t = new java.util.Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                updateStats();
            }
        }, 100, 100);
    }

    private MorseListener mMorseListener = new MorseListener() {
        @Override
        public void morseReceived(MorseCharacter c) {
            if(c.character != null) {
                charReceived(c.character);
            }
            else if(c.prosign != null) {
                prosignReceived(c.prosign);
            }
        }

        private void charReceived(String c) {
            mInOut.setText(mInOut.getText() + c);
        }

        private void prosignReceived(MorseProsign prosign) {
            switch(prosign) {
                case END_OF_MESSAGE:
                    mInOut.setText(mInOut.getText() + "!!EOM!!");
                    break;
                case WAIT:
                    mInOut.setText(mInOut.getText() + "!!WT!!");
                    break;
                case BREAK:
                    mInOut.setText(mInOut.getText() + "!!BRK!!");
                    break;
                case PARAGRAPH:
                    mInOut.setText(mInOut.getText() + "\t");
                    break;
                case NEWLINE:
                    mInOut.setText(mInOut.getText() + "\n");
                    break;
                case CLEAR:
                    mInOut.setText(mInOut.getText() + "!!CLR!!");
                    break;
                case START_COPYING:
                    mInOut.setText(mInOut.getText() + "!!ST!!");
                    break;
                case SOS:
                    mInOut.setText(mInOut.getText() + "!!SOS!!");
                    break;
                case STRIKE:
                    System.out.println("Strike");
                    String text = mInOut.getText();
                    text = text.trim();
                    text = new StringBuffer(text).reverse().toString();

                    boolean charSeen = false;
                    int i;
                    for(i = 0; i < text.length(); i++) {
                        char c = text.charAt(i);

                        if(c != ' ') charSeen = true;

                        if(charSeen && c == ' ') {
                            text = text.substring(i);
                            break;
                        }
                    }

                    if(i == text.length()) {
                        text = "";
                    }
                    else {
                        text = new StringBuffer(text).reverse().toString();
                    }

                    mInOut.setText(text);
                    break;
            }
        }
    };

    private Clip mBeep;

    private void playBeep() {
        if(mBeep != null) {
            mBeep.setFramePosition(0);
            mBeep.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    private void stopBeep() {
        if(mBeep != null) mBeep.stop();
    }

    private void setupBeep() {
        if(mBeep != null) {
            mBeep.stop();
            mBeep.close();
        }
        else {
            try {
                mBeep = AudioSystem.getClip();
            }
            catch (LineUnavailableException e) {
                System.err.println("Unable to make beep!");
                return;
            }
        }

        float sampleRate = 22050f;
        int framesPerWavelength = 28;
        int wavelengths = 100;

        byte[] buffer = new byte[framesPerWavelength * wavelengths * 2];
        int frames = framesPerWavelength * wavelengths;
        for(int i = 0; i < frames; i++) {
            double angleAtPoint = (i * 2d) / (framesPerWavelength) * Math.PI;
            buffer[i * 2] = getByteValue(angleAtPoint);
            buffer[i * 2 + 1] = getByteValue(angleAtPoint * 2);
        }

        try {
            AudioFormat format = new AudioFormat(sampleRate, 8, 2, true, false);
            AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(buffer), format, buffer.length / 2);

            mBeep.open(stream);
        }
        catch (IOException e) {
            System.err.println("Unable to make beep!");
        }
        catch (LineUnavailableException e) {
            System.err.println("Unable to make beep!");
        }
    }

    private byte getByteValue(double angle) {
        int maxVol = 32;
        return new Integer((int) Math.round(Math.sin(angle)*maxVol)).byteValue();
    }

    private void updateStats() {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        MorseStats stats = mKey.getStats();

        double dotAvg = stats.getDotAverage();
        double dashAvg = stats.getDashAverage();
        double markAvg = stats.getMarkAverage();
        double charAvg = stats.getCharAverage();
        double wordAvg = stats.getWordAverage();

        double dotWpm = MorseSpeed.dotsPerSecToWordsPerMin(MorseSpeed.dotMsecToDotsPerSec(dotAvg));
        double dashWpm = MorseSpeed.dotsPerSecToWordsPerMin(MorseSpeed.dotMsecToDotsPerSec(dashAvg / 3d));
        double markWpm = MorseSpeed.dotsPerSecToWordsPerMin(MorseSpeed.dotMsecToDotsPerSec(markAvg));
        double charWpm = MorseSpeed.dotsPerSecToWordsPerMin(MorseSpeed.dotMsecToDotsPerSec(charAvg / 3d));
        double wordWpm = MorseSpeed.dotsPerSecToWordsPerMin(MorseSpeed.dotMsecToDotsPerSec(wordAvg / 7d));

        double trueMarkWpm = mKey.getMarkSpeed().wordsPerMinute();
        double trueCharWpm = mKey.getCharSpeed().wordsPerMinute();
        double trueWordWpm = mKey.getWordSpeed().wordsPerMinute();

        mDotLengthField.setText(decimalFormat.format(dotAvg) + "ms");
        mDashLengthField.setText(decimalFormat.format(dashAvg) + "ms");
        mIntermarkLengthField.setText(decimalFormat.format(markAvg) + "ms");
        mIntercharLengthField.setText(decimalFormat.format(charAvg) + "ms");
        mInterwordLengthField.setText(decimalFormat.format(wordAvg) + "ms");

        mDotActualWpmField.setText(decimalFormat.format(dotWpm) + "wpm");
        mDashActualWpmField.setText(decimalFormat.format(dashWpm) + "wpm");
        mIntermarkActualWpmField.setText(decimalFormat.format(markWpm) + "wpm");
        mIntercharActualWpmField.setText(decimalFormat.format(charWpm) + "wpm");
        mInterwordActualWpmField.setText(decimalFormat.format(wordWpm) + "wpm");

        mDotSystemWpmField.setText(decimalFormat.format(trueMarkWpm) + "wpm");
        mDashSystemWpmField.setText(decimalFormat.format(trueMarkWpm) + "wpm");
        mIntermarkSystemWpmField.setText(decimalFormat.format(trueMarkWpm) + "wpm");
        mIntercharSystemWpmField.setText(decimalFormat.format(trueCharWpm) + "wpm");
        mInterwordSystemWpmField.setText(decimalFormat.format(trueWordWpm) + "wpm");
    }

    private class SignalPlaybackTask extends TimerTask {
        java.util.List<MorseSignal> remainingSignal;

        public SignalPlaybackTask(java.util.List<MorseSignal> remainingSignal) {
            this.remainingSignal = remainingSignal;
        }

        @Override
        public void run() {
            if(remainingSignal.size() > 0) {
                MorseSignal signal = remainingSignal.remove(0);

                if(signal.on) playBeep();
                else stopBeep();

                mPlaybackTimer.schedule(new SignalPlaybackTask(remainingSignal), signal.duration);
            }
            else {
                stopBeep();
                mPlaybackTimer.cancel();
                mPlaybackTimer.purge();
            }
        }
    }

    private void setupUi() {
        final JFrame frame = new JFrame("Morse Tester");

        frame.getContentPane().setPreferredSize(new Dimension(500, 600));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.setLayout(new BorderLayout());
        frame.setFocusable(true);

        frame.addKeyListener(new KeyAdapter() {
            boolean keyDown = false;

            @Override
            public void keyPressed(KeyEvent e) {
                if(!keyDown && e.getKeyCode() == KeyEvent.VK_SPACE) {
                    keyDown = true;
                    mKey.down();
                    playBeep();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                    keyDown = false;
                    mKey.up();
                    stopBeep();
                }
            }
        });

        mCodeKey = new JButton("Code Key");
        mCodeKey.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mKey.down();
                playBeep();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mKey.up();
                stopBeep();
            }
        });
        mCodeKey.addKeyListener(new KeyAdapter() {
            boolean keyDown = false;

            @Override
            public void keyPressed(KeyEvent e) {
                if(!keyDown && e.getKeyCode() == KeyEvent.VK_SPACE) {
                    keyDown = true;
                    mKey.down();
                    playBeep();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                    keyDown = false;
                    mKey.up();
                    stopBeep();
                }
            }
        });

        JPanel inOutPanel = new JPanel();
        double[][] inOutSizes = {
                {
                        0.33, 0.33, 0.33
                },
                {
                        0.8, 0.2
                }
        };

        TableLayout layout = new TableLayout(inOutSizes);
        inOutPanel.setLayout(layout);

        mInOut = new JTextArea("");
        mInOut.setLineWrap(true);
        inOutPanel.add(mInOut, new TableLayoutConstraints(0, 0, 2, 0, TableLayoutConstants.CENTER, TableLayoutConstraints.CENTER));

        JButton transmitButton = new JButton("Xmit (std)");
        JButton transmitButtonMimic = new JButton("Xmit (mimic)");
        JButton clearButton = new JButton("Clear message");

        transmitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                java.util.List<MorseSignal> signal = new MorseProfile(12.5, 0).generateSignal(mInOut.getText());

                mPlaybackTimer = new Timer();
                mPlaybackTimer.schedule(new SignalPlaybackTask(signal), 0);
            }
        });

        transmitButtonMimic.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                java.util.List<MorseSignal> signal = new MorseProfile(mKey.getStats()).generateSignal(mInOut.getText());

                mPlaybackTimer = new Timer();
                mPlaybackTimer.schedule(new SignalPlaybackTask(signal), 0);
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mInOut.setText("");
            }
        });

        inOutPanel.add(transmitButton, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstants.CENTER, TableLayoutConstraints.CENTER));
        inOutPanel.add(transmitButtonMimic, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstants.CENTER, TableLayoutConstraints.CENTER));
        inOutPanel.add(clearButton, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstants.CENTER, TableLayoutConstraints.CENTER));

        final JPanel info = new JPanel();
        info.setLayout(new BorderLayout());
        info.setBorder(new EmptyBorder(5, 5, 5, 5));
        info.add(mCodeKey, BorderLayout.SOUTH);

        info.add(inOutPanel, BorderLayout.CENTER);

        JPanel stats = new JPanel();

        double[][] sizes = {
                {
                        0.25, 0.25, 0.25, 0.25
                },
                {
                        0.1666, 0.1666, 0.1666, 0.1666, 0.1666, 0.1666
                }
        };
        TableLayout table = new TableLayout(sizes);
        stats.setLayout(table);
        info.add(stats, BorderLayout.NORTH);

        info.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                info.grabFocus();
                frame.requestFocus();
            }
        });

        JLabel label = new JLabel("Signal Type");
        TableLayoutConstraints constraints = new TableLayoutConstraints();
        constraints.col1 = 0;
        constraints.col2 = 0;
        constraints.row1 = 0;
        constraints.row2 = 0;
        stats.add(label, constraints);

        label = new JLabel("Actual ms");
        constraints = new TableLayoutConstraints();
        constraints.col1 = 1;
        constraints.col2 = 1;
        constraints.row1 = 0;
        constraints.row2 = 0;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        stats.add(label, constraints);

        label = new JLabel("Actual wpm");
        constraints = new TableLayoutConstraints();
        constraints.col1 = 2;
        constraints.col2 = 2;
        constraints.row1 = 0;
        constraints.row2 = 0;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        stats.add(label, constraints);

        label = new JLabel("System wpm");
        constraints = new TableLayoutConstraints();
        constraints.col1 = 3;
        constraints.col2 = 3;
        constraints.row1 = 0;
        constraints.row2 = 0;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        stats.add(label, constraints);


        // Dot stats -----------
        label = new JLabel("Dot speed");
        constraints = new TableLayoutConstraints();
        constraints.col1 = 0;
        constraints.col2 = 0;
        constraints.row1 = 1;
        constraints.row2 = 1;
        stats.add(label, constraints);

        mDotLengthField = new JTextField("000.00ms");
        constraints.col1 = 1;
        constraints.col2 = 1;
        constraints.row1 = 1;
        constraints.row2 = 1;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        mDotLengthField.setEditable(false);
        mDotLengthField.setFocusable(false);
        stats.add(mDotLengthField, constraints);

        mDotActualWpmField = new JTextField("00.00wpm");
        constraints.col1 = 2;
        constraints.col2 = 2;
        constraints.row1 = 1;
        constraints.row2 = 1;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        mDotActualWpmField.setEditable(false);
        mDotActualWpmField.setFocusable(false);
        stats.add(mDotActualWpmField, constraints);

        mDotSystemWpmField = new JTextField("00.00wpm");
        constraints.col1 = 3;
        constraints.col2 = 3;
        constraints.row1 = 1;
        constraints.row2 = 1;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        mDotSystemWpmField.setEditable(false);
        mDotSystemWpmField.setFocusable(false);
        stats.add(mDotSystemWpmField, constraints);


        // Dash stats ------
        label = new JLabel("Dash speed");
        constraints = new TableLayoutConstraints();
        constraints.col1 = 0;
        constraints.col2 = 0;
        constraints.row1 = 2;
        constraints.row2 = 2;
        stats.add(label, constraints);

        mDashLengthField = new JTextField("000.00ms");
        constraints.col1 = 1;
        constraints.col2 = 1;
        constraints.row1 = 2;
        constraints.row2 = 2;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        mDashLengthField.setEditable(false);
        mDashLengthField.setFocusable(false);
        stats.add(mDashLengthField, constraints);

        mDashActualWpmField = new JTextField("00.00wpm");
        constraints.col1 = 2;
        constraints.col2 = 2;
        constraints.row1 = 2;
        constraints.row2 = 2;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        mDashActualWpmField.setEditable(false);
        mDashActualWpmField.setFocusable(false);
        stats.add(mDashActualWpmField, constraints);

        mDashSystemWpmField = new JTextField("00.00wpm");
        constraints.col1 = 3;
        constraints.col2 = 3;
        constraints.row1 = 2;
        constraints.row2 = 2;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        mDashSystemWpmField.setEditable(false);
        mDashSystemWpmField.setFocusable(false);
        stats.add(mDashSystemWpmField, constraints);


        // Intermark stats -----
        label = new JLabel("Intermark speed");
        constraints = new TableLayoutConstraints();
        constraints.col1 = 0;
        constraints.col2 = 0;
        constraints.row1 = 3;
        constraints.row2 = 3;
        stats.add(label, constraints);

        mIntermarkLengthField = new JTextField("000.00ms");
        constraints.col1 = 1;
        constraints.col2 = 1;
        constraints.row1 = 3;
        constraints.row2 = 3;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        mIntermarkLengthField.setEditable(false);
        mIntermarkLengthField.setFocusable(false);
        stats.add(mIntermarkLengthField, constraints);

        mIntermarkActualWpmField = new JTextField("00.00wpm");
        constraints.col1 = 2;
        constraints.col2 = 2;
        constraints.row1 = 3;
        constraints.row2 = 3;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        mIntermarkActualWpmField.setEditable(false);
        mIntermarkActualWpmField.setFocusable(false);
        stats.add(mIntermarkActualWpmField, constraints);

        mIntermarkSystemWpmField = new JTextField("00.00wpm");
        constraints.col1 = 3;
        constraints.col2 = 3;
        constraints.row1 = 3;
        constraints.row2 = 3;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        mIntermarkSystemWpmField.setEditable(false);
        mIntermarkSystemWpmField.setFocusable(false);
        stats.add(mIntermarkSystemWpmField, constraints);


        // Interchar stats -----
        label = new JLabel("Interchar speed");
        constraints = new TableLayoutConstraints();
        constraints.col1 = 0;
        constraints.col2 = 0;
        constraints.row1 = 4;
        constraints.row2 = 4;
        stats.add(label, constraints);

        mIntercharLengthField = new JTextField("000.00ms");
        constraints.col1 = 1;
        constraints.col2 = 1;
        constraints.row1 = 4;
        constraints.row2 = 4;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        mIntercharLengthField.setEditable(false);
        mIntercharLengthField.setFocusable(false);
        stats.add(mIntercharLengthField, constraints);

        mIntercharActualWpmField = new JTextField("00.00wpm");
        constraints.col1 = 2;
        constraints.col2 = 2;
        constraints.row1 = 4;
        constraints.row2 = 4;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        mIntercharActualWpmField.setEditable(false);
        mIntercharActualWpmField.setFocusable(false);
        stats.add(mIntercharActualWpmField, constraints);

        mIntercharSystemWpmField = new JTextField("00.00wpm");
        constraints.col1 = 3;
        constraints.col2 = 3;
        constraints.row1 = 4;
        constraints.row2 = 4;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        mIntercharSystemWpmField.setEditable(false);
        mIntercharSystemWpmField.setFocusable(false);
        stats.add(mIntercharSystemWpmField, constraints);


        // Interword stats -----
        label = new JLabel("Interword speed");
        constraints = new TableLayoutConstraints();
        constraints.col1 = 0;
        constraints.col2 = 0;
        constraints.row1 = 5;
        constraints.row2 = 5;
        stats.add(label, constraints);

        mInterwordLengthField = new JTextField("000.00ms");
        constraints.col1 = 1;
        constraints.col2 = 1;
        constraints.row1 = 5;
        constraints.row2 = 5;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        mInterwordLengthField.setEditable(false);
        mInterwordLengthField.setFocusable(false);
        stats.add(mInterwordLengthField, constraints);

        mInterwordActualWpmField = new JTextField("00.00wpm");
        constraints.col1 = 2;
        constraints.col2 = 2;
        constraints.row1 = 5;
        constraints.row2 = 5;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        mInterwordActualWpmField.setEditable(false);
        mInterwordActualWpmField.setFocusable(false);
        stats.add(mInterwordActualWpmField, constraints);

        mInterwordSystemWpmField = new JTextField("00.00wpm");
        constraints.col1 = 3;
        constraints.col2 = 3;
        constraints.row1 = 5;
        constraints.row2 = 5;
        constraints.hAlign = TableLayoutConstraints.RIGHT;
        mInterwordSystemWpmField.setEditable(false);
        mInterwordSystemWpmField.setFocusable(false);
        stats.add(mInterwordSystemWpmField, constraints);

        frame.add(info);


        frame.pack();
        frame.setVisible(true);
    }
}
