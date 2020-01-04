package com.manywords.softworks.morse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jay on 2/14/17.
 */
class MorseTree {
    private TreeNode mRoot;
    private List<TreeNode> mNodes;
    public MorseTree(int[][] signalsToInsert, String signalChars, int[][] prosignsToInsert, MorseProsign[] prosigns) {
        mNodes = new ArrayList<>(signalsToInsert.length + 1);

        mRoot = new TreeNode(new int[0], "");
        mNodes.add(mRoot);

        for(int i = 0; i < signalsToInsert.length; i++) {
            int[] signal = signalsToInsert[i];
            String character = signalChars.substring(i, i + 1);

            insert(signal, character);
        }

        for(int i = 0; i < prosignsToInsert.length; i++) {
            int[] signal = prosignsToInsert[i];
            MorseProsign sign = prosigns[i];

            insert(signal, sign);
        }
    }

    public String lookup(int[] signal) {
        TreeNode node = mRoot;

        for(int i = 0; i < signal.length; i++) {
            node = node.getChild(signal[i]);
            if(node == null) {
                return "";
            }
            if(Arrays.equals(node.signal, signal)) return node.value;
        }
        return "";
    }

    public MorseProsign lookupProsign(int[] signal) {
        TreeNode node = mRoot;

        for(int i = 0; i < signal.length; i++) {
            node = node.getChild(signal[i]);
            if(node == null) {
                return null;
            }
            if(Arrays.equals(node.signal, signal)) return node.sign;
        }
        return null;
    }

    private void insert(int[] signal, String character) {
        TreeNode node = mRoot;

        for(int i = 0; i < signal.length; i++) {
            TreeNode child = node.getChild(signal[i]);
            if(child == null) {
                child = createNode(signal, i + 1, node, signal[i]);
            }
            node = child;
            if(Arrays.equals(node.signal, signal)) break;
        }

        if(node == null) {
            throw new IllegalStateException();
        }

        node.value = character;
    }

    private void insert(int[] signal, MorseProsign sign) {
        TreeNode node = mRoot;

        for(int i = 0; i < signal.length; i++) {
            TreeNode child = node.getChild(signal[i]);
            if(child == null) {
                child = createNode(signal, i + 1, node, signal[i]);
            }
            node = child;
            if(Arrays.equals(node.signal, signal)) break;
        }

        if(node == null) {
            throw new IllegalStateException();
        }

        node.sign = sign;
    }

    private TreeNode createNode(int[] signal, int subsignalLength, TreeNode parent, int direction) {
        int[] subsignal = new int[subsignalLength];
        System.arraycopy(signal, 0, subsignal, 0, subsignalLength);

        TreeNode n = new TreeNode(subsignal, "");
        mNodes.add(n);
        parent.setChild(direction, n);
        return n;
    }

    private class TreeNode {
        TreeNode(int[] s, String v) {
            signal = s;
            value = v;
        }

        private int[] signal;
        private String value;
        private MorseProsign sign;

        private TreeNode dotChild;
        private TreeNode dashChild;

        TreeNode getChild(int direction) {
            if(direction == MorseConstants.DOT) return dotChild;
            else if(direction == MorseConstants.DASH) return dashChild;
            else throw new IllegalStateException();
        }

        void setChild(int direction, TreeNode n) {
            if(direction == MorseConstants.DOT) dotChild = n;
            else if(direction == MorseConstants.DASH) dashChild = n;
            else throw new IllegalStateException();
        }
    }
}
