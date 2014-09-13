package Kirrkirr.ui.panel.optionPanel;

import java.io.*;
import javax.swing.*;
 
/** SuperClass for all option panels - defines methods required for notifying them of close
 *  events, etc. in the main KirrkirrOptionsDialog.
 *  (Madhu:'00 added the change and the setxslfilename methods)
 */
public abstract class KirrkirrOptionPanel extends JPanel implements Serializable {

    /** Called when "cancel" is pressed in KirrkirrOptionsDialog
     */
    public void cancel() {
    }

    /** Called when "apply" is pressed in KirrkirrOptionsDialog
     */
    public void apply() {
    }    

    /** Called when "defaults" is pressed in KirrkirrOptionsDialog
     */
    public void defaults() {
    }    
    
    /** Return a string that describes the options panel.  This string will
     *  be internationalized by the caller.
     *  @return Description of option panel
     */
    public String getToolTip() {
	return null;
    }

    /** Do any necessary initialization before displaying each time.
     */
    public void setup() {
    }

    /** Called by profile manager, in case the options
     *  panel wants to save any state. If so, it should also
     *  implement <code>loadState</code>. 
     *  @see #loadState
     */    
    public void saveState(ObjectOutputStream oos) throws IOException {
    }    

    /** Called by profile manager, in case the options
     *  panel wants to save/load state. If so, it should also
     *  implement <code>saveState</code>. 
     *  @see #saveState
     */   
    public void loadState(ObjectInputStream ois) throws IOException, 
						ClassNotFoundException {
    } 

}

