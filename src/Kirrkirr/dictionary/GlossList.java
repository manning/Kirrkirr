package Kirrkirr.dictionary;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.data.KKListModel;
import Kirrkirr.ui.panel.ScrollPanel;
import Kirrkirr.ui.panel.SearchPanel;
import Kirrkirr.util.*;

import java.util.*;
import javax.swing.*;

public class GlossList extends WordList {

      public GlossList(ListCellRenderer r,Kirrkirr parent,ScrollPanel p, int size) {
	  super(r, parent, p, size, "Glosslist: ");
      }

      /** Set up the Gloss glosses list (at startup).  Previously, mainwords
       *  will have been set to the top level word entries, and the vector
       *  parameter will contain all Gloss gloss entries and individual
       *  words therein.
       *  IndexMaker now sorts mainwords, and we take advantage of that
       *  @param v Vector of gloss items
       */
      public void setGlosses(final Vector<String> v) {
	  /*
          Object[] items=new Object[scrollPanel.mainwords.size()];
	  if (Dbg.K) {
	      Dbg.print("Mainwords has " + scrollPanel.mainwords.size() +
			" items; glosses v has " + v.size() + " items");
	  }
	  scrollPanel.mainwords.copyInto(items);
	  long time=0;
	  if (Dbg.TIMING){
	      time=System.currentTimeMillis();
	  }
	  items=sortGlosses(items, scrollPanel.mainwords.size());
	  if (Dbg.TIMING){
	      time=System.currentTimeMillis()-time;
	      Dbg.print(items.length+" items: "+time+" ms to sort");
	      }*/
	  words = new KKListModel<String>(v);
	  words.trimToSize();
	  if (Dbg.GLOSSES) {
	      Dbg.print("Gloss glosses words has " + words.size() +
			" items");
	      for (int i = 0; i < words.size(); i++) {
		  Dbg.print("  " + i + ": " + words.elementAt(i));
	      }
	  }
	  jWords.setModel(words);
	  sp_jWords.repaint();
      }

      /*public void scrollToWord(String word){
	  currentList.scrollToWord(word);
	  }*/

      /**
       */
      protected void removeWord(String word,int wordsIndex) {
	  words.removeElementAt(wordsIndex);
	  scrollPanel.mainwords.removeElement(word);
      }

      @Override
      public String getFuzzy(String word) {
	  return SearchPanel.glossFuzzy(word);
      }

      /** Return the full word list.  At the moment this is
       *  prebuilt, so we don't have to rebuild it.
       *  @return All the glosses in the dictionary in word, glosses as
       *  	subwords format
       */
      public Vector getFullWordList() {
	  return this.parent.cache.glossList;
	  /*Object[] item=sortGlosses(scrollPanel.mainwords.toArray());
	  Vector v=new Vector();
	  for (int i=0;i<item.length;i++)
	      v.addElement(item[i]);
	      return v;*/
      }

      @Override
      protected void setWord(String word) {
	  this.parent.setCurrentWord(word,true,jWords,Kirrkirr.SCROLLPANEL,1);
      }

      @Override
      public void refreshWords(int attributeChanged) {
	  /*if (attributeChanged==SEE_SUB){
	      if (!attributes[SEE_SUB]){
		  /*  System.err.println("refreshing with no subs");
		  for (int i=0;i<words.size();i++){
		      String pword = (String) words.elementAt(i);
		      if (this.parent.cache.isGlossSubWord(pword)) {
			  words.removeElement(pword);
		      }
		      System.err.println(i);
		  }
		  System.err.println("done");  *
		  //this takes a really long time.
	      } else{
		  this.resetWordsGUI(null);
	      }
	  }*/

	  jWords.repaint();
      }

      @Override
      protected void initAttributes() {
	  attributes = new boolean[ScrollPanel.ENGLISH_ATTRIBUTES];
	  attributes[ScrollPanel.SEE_SUB]=true;
	  attributes[ScrollPanel.SEE_FREQ]=false;
      }

      public void resetWordsGUI(Vector selections) {
	  super.resetWordsGUI(selections);
	  //sortGlosses(words.toArray());
      }

      public String[] sortGlosses(String[] items) {
	  return sortGlosses(items,items.length);
      }

      /** This begins by sorting the passed in array items, which are
       *  the main entries for gloss words.
       *  For each item, it looks it up in the glossIndex
       *  Hashtable, it adds that word and all 'subwords' (i.e., full glosses)
       *  that contain it
       */
      public String[] sortGlosses(String[] items, int length) {
	  Arrays.sort(items, new KAlphaComparator());
	  Vector<String> v = new Vector<String>();
	  for (int i=0; i < length; i++) {
	      String cur = items[i];
	      GlossDictEntry ede = this.parent.cache.getGlossIndexEntry(cur);
	      if (ede != null && ! ede.isSubword) {
		  v.addElement(items[i]);
              } else {
		  continue;
              }
              for (int j=0, sz = ede.numSubwords(); j < sz; j++) {
		  v.addElement(ede.subwords[j]);
	      }
	  }
	  String[] it = new String[v.size()];
	  v.copyInto(it);
	  return it;
      }

      /* public void resetWords(Object[] selections){
	  super.resetWords(selections);

	  // sortGlosses(words.toArray());
	  }*/

} // end of class GlossList




