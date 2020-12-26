package Kirrkirr.dictionary;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.panel.ScrollPanel;
import Kirrkirr.ui.panel.SearchPanel;
import Kirrkirr.util.Dbg;

import java.util.*;
import javax.swing.*;

public class HeadwordList extends WordList {

    public HeadwordList(ListCellRenderer r, Kirrkirr parent, ScrollPanel p, int size) {
        super(r, parent, p, size, "HWlist: ");
    }

    @Override
    protected void setWord(String word) {
        this.parent.setCurrentWord(word,false,jWords,Kirrkirr.SCROLLPANEL,1);
    }

    public Vector getFullWordList() {
        if (Dbg.LIST) {
            Dbg.print("Getting headword list of " +
                      parent.cache.headwordList.size() + " words.");
        }
        return parent.cache.headwordList;
    }

    @Override
    public String getFuzzy(String word) {
        return SearchPanel.headwordFuzzy(word);
    }

    @Override
    public void refreshWords(int attributeChanged) {
        if (attributeChanged==ScrollPanel.SEE_SUB) {
            if ( ! attributes[ScrollPanel.SEE_SUB]) {
                // count down to reduce copying and not recalculate size
                for (int i = words.size() - 1; i >= 0; i--) {
                    String ukey = (String) words.elementAt(i);
                    if (this.parent.cache.isSubWord(ukey)) {
                        words.removeElement(ukey);
                    }
                }
            } else {
                this.resetWordsGUI(null);
            }
        }

        //this takes care of cellrenderer, right?
        jWords.repaint();
    }

    @Override
    protected void initAttributes(){
        attributes=new boolean[ScrollPanel.WARLPIRI_ATTRIBUTES];
        attributes[ScrollPanel.SEE_POLY]=false; // item starts unset in menu
        attributes[ScrollPanel.SEE_SUB]=true;
        attributes[ScrollPanel.SEE_FREQ]=false;
        attributes[ScrollPanel.SEE_DIALECT]=false;
        attributes[ScrollPanel.SEE_POS]=false;
    }

}


