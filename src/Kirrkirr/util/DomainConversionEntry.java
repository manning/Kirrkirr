package Kirrkirr.util;

import java.io.*;
import java.util.HashMap;
import javax.swing.tree.*;

//just a wrapper for the old and new names with a nice toString method for the tree
public class DomainConversionEntry {
    
    	private String oldName;
    	private String newName;
    	
    	public DomainConversionEntry(String oldName, String newName) {
    		this.oldName = oldName;
    		this.newName = newName;
    	}
    	
    	//i think this will look nice, but anything will do
    	public String toString() { return oldName; }
    	
    	public String getOldName() { return oldName; }
    	public String getNewName() { return newName; }
    	
    	public void setOldName(String other) { oldName = other; }
    	public void setNewName(String other) { newName = other; }

    	 public boolean recWrite(BufferedWriter writer, HashMap entries, String parent) {
            try {
                //convert any nested quotes into spaces so as to avoid
                //attribute parse errors
                writer.write("<domain old=\"");
                writer.write(oldName);
                writer.write("\" new=\"");
                writer.write(newName);
                writer.write("\"");

                DefaultMutableTreeNode node = 
                	(DefaultMutableTreeNode)(entries.get(oldName+"@"+parent));
                int numChildren = node.getChildCount();
                
                if(numChildren == 0) { //no children - use shorthand
                    writer.write("/>");
                    writer.newLine();
                }
                else {  //children - write out self, ask children to
                        //write, then add ending tag for self
                    writer.write(">");
                    writer.newLine();

                    for(int i = 0; i < numChildren; i++) {
                        DefaultMutableTreeNode childNode = 
                        	(DefaultMutableTreeNode)node.getChildAt(i);
                        DomainConversionEntry child =
                        	(DomainConversionEntry)(childNode.getUserObject());
                        if(!(child.recWrite(writer, entries, oldName+"@"+parent))) {
                            //failure!  quickly fail out and propagate
                            //notification of failure up to top level
                            return false;
                        }
                    }
                    writer.write("</domain>");
                    writer.newLine();
                }
            }
            catch(IOException ioe) {
                Dbg.print("Error while writing xml file");
                return false;
            }
            return true;
        }
}
