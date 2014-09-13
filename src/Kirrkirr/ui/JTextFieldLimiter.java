package Kirrkirr.ui;
/* Class: JTextFieldLimiter
    Kevin Lim
    This class prevents the user from pasting multiple lines  */

import javax.swing.text.*;

 public class JTextFieldLimiter extends PlainDocument {

    public void insertString(int offset, String  str, AttributeSet attr) throws BadLocationException
    {
      if (str == null) return;

      str = str.replace('\n', ' ');
      super.insertString(offset, str, attr);
    }
 }

