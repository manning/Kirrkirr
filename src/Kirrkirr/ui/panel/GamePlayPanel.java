package Kirrkirr.ui.panel;

import Kirrkirr.ui.data.GameWordGenerator;

import javax.swing.*;

public abstract class GamePlayPanel extends JPanel
{
    private final static String ERROR="Kirrkirr error: please override GamePlayPanel.getDesc";
    private int difficulty = GameWordGenerator.EASY;
    
    public abstract void startGame();
    public abstract void restartGame();
    
    public void setDifficulty(int newDiff) {
	difficulty = newDiff;
    }

    /** Returns a difficulty level. The current known difficulty levels are
     *  0, 1, 2 (easy, medium, hard).
     *
     * @return Difficulty level
     */
    public int getDifficulty () {
	return difficulty;
    }

    public static String getDesc() {
	return ERROR;
    }

    public abstract boolean horizontalLayout();

}

