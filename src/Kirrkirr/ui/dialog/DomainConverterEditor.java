package Kirrkirr.ui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import Kirrkirr.Kirrkirr;
import Kirrkirr.util.*;
import Kirrkirr.ui.KirrkirrButton;


/**
 * The DomainConverterEditor object provides a dialog for a tool
 * that helps the user create a Domain Conversion file to be used
 * with dictionaries that are using abbreviated domain names.
 *
 * @author Chloe Kiddon
 *
 */
public class DomainConverterEditor extends JDialog implements ActionListener {

	public static final String SC_EDIT_DESC = "Kirrkirr_Domain_Conversion_File_Editor";
	public static final String SC_LOAD = "Load_new";
	public static final String SC_SAVE = "Save";
	public static final String SC_SAVE_AS = "Save_As";
	public static final String SC_OK = "OK";
	public static final String SC_CANCEL = "Cancel";
	public static final String SC_CONVERSION = "Conversion";
	public static final String SC_ADD = "Add";
	public static final String SC_CLEAR = "Clear";
	public static final String SC_NEW_CONV = "New_Entry";
	public static final String SC_REMOVE = "Remove";
	public static final String SC_OLD_DNAME = "old_domain_name";
	public static final String SC_NEW_DNAME = "new_domain_name";
	public static final String SC_OLD_PNAME = "old_parent_domain_name";
	public static final String SC_IS_SUBD = "Is_a_Subdomain";
	public static final String SC_DOMAIN_FILE = "Domain_Converter";
	public static final String SC_PROPER_PARENT = "parent_domain_that_has_been_created";
	public static final String SC_CLEAR_PARENT = "unambiguous_parent_domain.\n"+
		"Use @ to denote depth if you need.\n" +
		"(i.e. For a parent that is the B subdomain of A, it is B@A.)";
	public static final String SC_USE_FULL_PATHS = "Show_full_parent_path_names";


	private JTree domainTree;
	private DefaultTreeModel treeModel;
	private DefaultMutableTreeNode root;
	private DefaultTreeSelectionModel selectionModel;
	private String filename;

	private HashMap entries;
	private HashMap oldNames;

	private JPanel mainPanel;
	private EditorPanel editPanel;
	private JPanel filenamePanel;
	private JPanel actionPanel;
	private JPanel centralPanel;

	private JTextField oldDomainName, newDomainName, parentDomain;
	private String oldDName, newDName, parentName;
	private ParentDomainCheckBox isChildDomain;
	private JCheckBox useFullPNames;
	private KirrkirrButton addDomain;

	private KirrkirrButton loadNew, save, saveAs, ok, cancel, add, clear, remove;

	private JScrollPane treePane;

	private JTextField filenameField;

	private boolean dirty;

	private ImportDialog processor;

	//	sizing info
    private static final Dimension minimumSize = new Dimension(500, 450);

    public DomainConverterEditor(Kirrkirr parent, boolean modal) {
    	this(parent, modal, null);
    }

    public DomainConverterEditor(boolean modal,
    		ImportDialog processor) {
    	this(modal, null, processor);
    }

	public DomainConverterEditor(Kirrkirr parent, boolean modal, String fname) {
        //false = non-modal; i.e., other windows can be active
		super(parent.window, Helper.getTranslation(SC_EDIT_DESC), modal);
		if (fname != null) filename = fname;
//		 place and size the dialog
        // setLocationRelativeTo(parent);
        int xLoc = parent.getWidth()/2-250;
        int yLoc = parent.getHeight()/2-200;
        if (xLoc >= 0 && yLoc >= 0) {
            setLocation(xLoc, yLoc);
        }
		setup();
	}

	public DomainConverterEditor(boolean modal, String fname,
			ImportDialog processor) {
		super(processor, Helper.getTranslation(SC_EDIT_DESC), modal);
		if (fname != null) filename = fname;
		if (processor != null) this.processor = processor;
//		 place and size the dialog
        // setLocationRelativeTo(processor);
        int xLoc = processor.getWidth()/2-250;
        int yLoc = processor.getHeight()/2-200;
        if (xLoc >= 0 && yLoc >= 0) {
            setLocation(xLoc, yLoc);
        }
		setup();
	}

	public void setup() {

        setSize(minimumSize);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        centralPanel = new JPanel();
        centralPanel.setLayout(new GridLayout(1, 2));

        entries = new HashMap();
        oldNames = new HashMap();

        //set up the buttons
        setupEditPanel();

        //set up the tree controls
        setupTree();

        mainPanel.add(centralPanel, BorderLayout.CENTER);

        //set up filename panel
        setupFilenamePanel();

        //set up button panel
        setupActionPanel();

        getContentPane().add(mainPanel);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){
                dispose();
            }
        });

        dirty = false;

        setVisible(true);
    }

	/** sets up the jtree
	 *
	 */
	private void setupTree() {
		root = new DefaultMutableTreeNode("domains");

		treeModel = new DefaultTreeModel(root);
		domainTree = new JTree(treeModel);
		domainTree.addTreeSelectionListener(editPanel);
		selectionModel = new DefaultTreeSelectionModel();
		selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		domainTree.setSelectionModel(selectionModel);

		treePane = new JScrollPane(domainTree);

		centralPanel.add(treePane);

		if (filename != null) {
			if (!loadTreeFromFile())
				clearTree();
			else
				domainTree.expandPath(new TreePath(root));
		}
	}

	/** creates an instance of an EditorPanel */
	private void setupEditPanel() {
		editPanel = new EditorPanel();

		centralPanel.add(editPanel);
	}

	/** sets up the top panel where the filename is shown */
	private void setupFilenamePanel() {
		//filenamePanel = new AuxFilePanel(Helper.getTranslation(SC_DOMAIN_FILE),
			//	KirrkirrFileFilter.XML_ENTRY);
		filenamePanel = new JPanel();
		filenamePanel.setLayout(new BoxLayout(filenamePanel, BoxLayout.X_AXIS));
		filenamePanel.setBorder(BorderFactory.createTitledBorder(""));

		JLabel fileLabel = new JLabel(Helper.getTranslation(SC_DOMAIN_FILE));
        fileLabel.setMaximumSize(new Dimension(160,30));
        fileLabel.setPreferredSize(new Dimension(160,30));

        filenamePanel.add(Box.createHorizontalStrut(15));
        filenamePanel.add(fileLabel);
        filenamePanel.add(Box.createHorizontalStrut(5));

		filenameField = new JTextField(80);
		filenameField.setMaximumSize(new Dimension(90,30));
		filenameField.setPreferredSize(new Dimension(90,30));
		filenameField.setEditable(false);

        filenamePanel.add(filenameField);
        filenamePanel.add(Box.createHorizontalStrut(5));

		mainPanel.add(filenamePanel, BorderLayout.NORTH);
	}

	/** sets up the panel of buttons at the bottom of the dialog */
	private void setupActionPanel() {
		loadNew = new KirrkirrButton(Helper.getTranslation(SC_LOAD), this);
		save = new KirrkirrButton(Helper.getTranslation(SC_SAVE), this);
		saveAs = new KirrkirrButton(Helper.getTranslation(SC_SAVE_AS), this);
		ok = new KirrkirrButton(Helper.getTranslation(SC_OK), this);
		cancel = new KirrkirrButton(Helper.getTranslation(SC_CANCEL), this);

		actionPanel = new JPanel();
		actionPanel.setLayout(new GridLayout(1, 5));
		actionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		actionPanel.add(loadNew);
		actionPanel.add(save);
		actionPanel.add(saveAs);
		actionPanel.add(ok);
		actionPanel.add(cancel);

		mainPanel.add(actionPanel, BorderLayout.SOUTH);
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		Object src = e.getSource();
		if (src == add) {
			if (inputIsComplete()) {
				dirty = true;
				DefaultMutableTreeNode parent;
				DefaultMutableTreeNode oldNode;
				if (parentName == null) {
					parent = root;
					parentName = "";
					oldNode =
						(DefaultMutableTreeNode)entries.get(oldDName+"@");
				}
				else {
					parent = (DefaultMutableTreeNode)entries.get(parentName);
					if (parent == null) {
						popupNeedInputDialog(SC_PROPER_PARENT);
						return;
					}
					oldNode =
						(DefaultMutableTreeNode)entries.get(oldDName+"@"+parentName);
				}
				TreePath newPath;
				if (oldNode == null) {
					DomainConversionEntry newEntry =
						new DomainConversionEntry(oldDName, newDName);
					DefaultMutableTreeNode newNode =
						new DefaultMutableTreeNode(newEntry);
					treeModel.insertNodeInto(newNode, parent, parent.getChildCount());
					entries.put(oldDName+"@"+parentName, newNode);
					newPath = new TreePath(treeModel.getPathToRoot(newNode));
					Vector nameList = (Vector)oldNames.get(oldDName);
					if (nameList == null) {
						nameList = new Vector();
						nameList.add(oldDName + "@" + parentName);
						oldNames.put(oldDName, nameList);
					}
					else
						nameList.add(oldDName+"@"+parentName);
				}
				else {
					domainTree.setSelectionPath(new TreePath(root));

					treeModel.removeNodeFromParent(oldNode);

					DomainConversionEntry oldEntry = (DomainConversionEntry)oldNode.getUserObject();
					oldEntry.setNewName(newDName);
					treeModel.insertNodeInto(oldNode, parent, parent.getChildCount());

					newPath = new TreePath(treeModel.getPathToRoot(oldNode));
				}
				domainTree.setSelectionPath(newPath);
				if (parent == root)
					editPanel.setFields(null, null);
				else {
					if (useFullPNames.isSelected()) {
						TreePath selectionPath =
			    			domainTree.getSelectionPath();
						DefaultMutableTreeNode selected =
			    			(DefaultMutableTreeNode)(selectionPath.getLastPathComponent());
						editPanel.setFields(null, getFullParentPath(selected));
					}
					else
						editPanel.setFields(null, parentName.substring(0, parentName.length()-1));
				}
			}
		} else if (src == remove) {
			TreePath selectionPath = domainTree.getSelectionPath();
			if (selectionPath == null) return;
			DefaultMutableTreeNode selected =
    			(DefaultMutableTreeNode)selectionPath.getLastPathComponent();
    		if (selected == root) {
    			 Object[] options = { "OK", "CANCEL" };
    			 int n = JOptionPane.showOptionDialog(null,
    			 		"This action will erase the entire domain tree.\n",
    			 		"Warning",
    			        JOptionPane.DEFAULT_OPTION,
						JOptionPane.WARNING_MESSAGE,
    			             null, options, options[0]);
    			 if (n == 0) {
    			 	dirty = true;
    			 	clearTree();
    			 }
    		}
    		else {
    			dirty = true;
    			domainTree.setSelectionPath(new TreePath(root));
    			DefaultMutableTreeNode parent =
    				(DefaultMutableTreeNode)selected.getParent();
    			if (parent == root) {
    				treeModel.removeNodeFromParent(selected);
    				removeEntries("", selected);
    			}
    			else {
    				String pathname = getFullParentPath(selected);
    				treeModel.removeNodeFromParent(selected);
    				DomainConversionEntry p =
    					(DomainConversionEntry)parent.getUserObject();
    				parentName = p.getOldName();
    				removeEntries(pathname, selected);
    			}
    		}
		} else if (src == clear) {
			editPanel.setFields(null, null);
			domainTree.clearSelection();
		} else if (src == cancel) {
			if (dirty) {
				Object[] options = { "YES", "NO" };
				 int n = JOptionPane.showOptionDialog(null,
				 		"Are you sure you want to exit without saving?\n",
				 		"Closing",
				        JOptionPane.DEFAULT_OPTION,
						JOptionPane.WARNING_MESSAGE,
				             null, options, options[0]);
				 if (n == 1) return;
			}
			dispose();
		} else if ((src == ok) || (src == save) || (src == saveAs)) {
			if (src == ok) {
				if (dirty) {
					do {
						Object[] options = { "YES", "NO" };
						int n = JOptionPane.showOptionDialog(null,
						 		"Do you want to save?\n",
						 		"Closing",
						        JOptionPane.DEFAULT_OPTION,
								JOptionPane.WARNING_MESSAGE,
						             null, options, options[0]);
						if (n == 1) {
							dispose();
							return;
						}
						else if (n == 0) {
							if (filename == null)
								getFilename();
						}
						else
							return;
					} while (filename == null);

					saveTreeToFile();
					if (processor != null)
						processor.setConverter(filename);
					dispose();
					return;

				}
				else {
					if (processor != null)
						processor.setConverter(filename);
					dispose();
					return;
				}
			}
			if ((src == save) && (filename != null) && (!filename.equalsIgnoreCase(""))) {
				saveTreeToFile();
				dirty = false;
				return;
			}
			dirty = false;
			getFilename();
			saveTreeToFile();
		} else if (src == loadNew) {
			getFilename();
			if (!loadTreeFromFile())
				clearTree();
			else
				domainTree.expandPath(new TreePath(root));
		}

	}

	/** helper function. gets the full parent path with @ signs for use
	 *  in processing
	 *
	 */
	public String getFullParentPath(DefaultMutableTreeNode node) {
		String path = "";
		TreeNode[] pathway = treeModel.getPathToRoot(node);
		for (int i = 1; i < pathway.length - 1; i++) {
			DefaultMutableTreeNode curr = (DefaultMutableTreeNode)pathway[i];
			DomainConversionEntry entry = (DomainConversionEntry)curr.getUserObject();
			path = entry.getOldName() + "@" + path;
		}
		return path.substring(0, path.length()-1);
	}

	/** clears the tree and associated hash tables
	 *
	 */
	public void clearTree() {
		domainTree.setSelectionPath(new TreePath(root));
	 	int numChildren = root.getChildCount();
	 	for (int i = 0; i < numChildren; i++)
	 		treeModel.removeNodeFromParent((DefaultMutableTreeNode)(root.getChildAt(0)));
	 	entries.clear();
	 	oldNames.clear();
	 	editPanel.setFields(null, null);
		System.gc();
	}

	/**	creates a tree from an existing xml domain conversion file
	 *
	 */
	public boolean loadTreeFromFile() {
		if ((filename == null) || (filename.equalsIgnoreCase(""))) return false;

		try {
			DomainConverter dc = new DomainConverter(filename);
			clearTree();
			DefaultMutableTreeNode newRoot = dc.getTreeRoot();
			int numChildren = newRoot.getChildCount();
			if (numChildren == 0) return true;
			boolean works = true;
			for (int i = 0; i < numChildren; i++) {
				treeModel.insertNodeInto((DefaultMutableTreeNode)(newRoot.getChildAt(0)),
						root, root.getChildCount());
				if (!addEntries("", (DefaultMutableTreeNode)root.getChildAt(i)))
					works = false;
			}
			return works;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	/** removes entries recursively from the entry hashmap */
	public void removeEntries(String parent, DefaultMutableTreeNode node) {
		String oldName = ((DomainConversionEntry)node.getUserObject()).getOldName();
		entries.remove(oldName +"@" + parent);
		Vector nameList = (Vector)oldNames.get(oldName);
		if (nameList.size() == 0)
			oldNames.remove(oldName);
		else {
			 nameList.remove(oldName+"@"+parent);
		}

		int children = node.getChildCount();
		if (children == 0) return;
		for (int i = 0; i < children; i++)
			removeEntries(oldName +	"@"+parent,
					(DefaultMutableTreeNode)node.getChildAt(i));
	}

	/** add entries recursively to the entry hashmap */
	public boolean addEntries(String parent, DefaultMutableTreeNode node) {
		DomainConversionEntry entry = (DomainConversionEntry)node.getUserObject();
		entries.put(entry.getOldName()+ "@"+ parent, node);
		Vector nameList = (Vector)oldNames.get(entry.getOldName());
		if (nameList == null) {
			nameList = new Vector();
			nameList.add(entry.getOldName()+"@"+parent);
			oldNames.put(entry.getOldName(), nameList);
		}
		else {
			int hopefullyNegative = nameList.indexOf(entry.getOldName()+"@"+parent);
			if (hopefullyNegative == -1) {
				nameList.add(entry.getOldName()+"@"+parent);
			}
			else {
				JOptionPane.showMessageDialog(Kirrkirr.kk.window,
					    "XML has two paths for:\n" +
						entry.getOldName()+"@"+parent +"\n" +
						"Please fix this.",
					    "Distinct Path Error",
					    JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		int numChildren = node.getChildCount();
		if (numChildren == 0) return true;
		boolean works = true;
		for (int i = 0; i < numChildren; i++) {
			if (!addEntries(entry.getOldName()+"@"+parent,
					(DefaultMutableTreeNode)node.getChildAt(i)))
				works = false;
		}
		return works;
	}

	/** saves the jtree to a file */
	public void saveTreeToFile() {
		if ((filename == null) || (filename.equalsIgnoreCase(""))) return;
		try {
            BufferedWriter outWriter = new BufferedWriter(new FileWriter(filename));
            outWriter.write("<?xml version=\"1.0\" encoding=\"" +
                            RelFile.ENCODING + "\"?>");
            outWriter.newLine();
            outWriter.write("<conversion>");
            outWriter.newLine();

            int numFirstGen = root.getChildCount();
            if (numFirstGen != 0) {
            	for (int i = 0; i < numFirstGen; i++) {
            		DomainConversionEntry entry =
            			(DomainConversionEntry)(((DefaultMutableTreeNode)root.getChildAt(i)).getUserObject());
            		entry.recWrite(outWriter, entries, "");
            	}
            }

            outWriter.write("</conversion>");
            outWriter.newLine();
            outWriter.close();
        }
        catch (Exception e) {
            Dbg.print("file writer initialization failed!");
        }
	}


	/** gets a filename to use and puts it in the filename string variable */
	public void getFilename() {
		//open up a file chooser dialog
        JFileChooser chooser = new JFileChooser(RelFile.dictionaryDir);
        chooser.setMultiSelectionEnabled(false);
        KirrkirrFileFilter filter;
        filter = new KirrkirrFileFilter(KirrkirrFileFilter.XML_ENTRY);
        chooser.setFileFilter(filter);
        int returnVal = chooser.showDialog(this, ToolsDialog.SC_OK);

        //if a file is chosen
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            //fill in text field with chosen file name
            filenameField.setText(chooser.getSelectedFile().getAbsolutePath());
            filename = chooser.getSelectedFile().getAbsolutePath();
        }
	}

	public String getOpenFile() {
		return filename;
	}

	 /** Predicate helper method to check the input in the fields for
     * consistency before running tests.  This does not check validity
     * of input, only if something has been supplied where necessary.
     */

    private boolean inputIsComplete() {
        oldDName = null;
        newDName = null;
        parentName = null;

        //check dictionary file
        oldDName = oldDomainName.getText();
        if(oldDName == null || oldDName.length() == 0) {
            //error - no dictionary file specified - pop up dialog saying
            //so
            popupNeedInputDialog(SC_OLD_DNAME);
            return false;
        }

        //check specification file
        newDName = newDomainName.getText();
        if(newDName == null || newDName.length() == 0) {
            //error - no spec file specified - pop up dialog saying so
            popupNeedInputDialog(SC_NEW_DNAME);
            return false;
        }

        //check forward index
        if(isChildDomain.isSelected()) {
            parentName = parentDomain.getText();
            if(parentName == null ||parentName.length() == 0) {
                //error - no fwd index file specified = pop up dialog
                popupNeedInputDialog(SC_OLD_PNAME);
                return false;
            }
            if (parentName.charAt(parentName.length()-1) == '@')
            	parentName = parentName.substring(0, parentName.length()-1);
            if (parentName.length() == 0) {
                //error - no fwd index file specified = pop up dialog
                popupNeedInputDialog(SC_OLD_PNAME);
                return false;
            }
            if (parentName.indexOf('@') == -1) {
            	Vector names = (Vector)oldNames.get(parentName);
            	if (names == null) {
            		popupNeedInputDialog(SC_PROPER_PARENT);
					return false;
            	}
            	else if (names.size() > 1) {
            		int index = names.indexOf(parentName+"@");
            		if (index == -1) {
            			popupNeedInputDialog(SC_CLEAR_PARENT);
            			return false;
            		}
            	}
            	parentName = (String)names.get(0);
            }
            else
            	parentName = parentName+"@";
        }
        return true;
    }

	/* Bring up a dialog box explaining the input that must be entered
     */
    private void popupNeedInputDialog(String inputName) {
        NeedInputDialog nid = new NeedInputDialog(Kirrkirr.window, inputName);
    }

	public static void main(String[] args) {
	}

	/** Inner class that represents a glorified check box - behavior is simply
     * to associate itself with an JTextField, and set that field's
     * enabled bit correspondingly whenever it is checked/un-checked.
     */

    static class ParentDomainCheckBox extends JComponent implements ActionListener {

        JCheckBox checkBox; //the actual control
        JTextField associatedField; //panel to enable/disable on select/deselect
        public ParentDomainCheckBox(String text, JTextField assocField) {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            //add the control
            checkBox = new JCheckBox(text, false);
            checkBox.addActionListener(this);
            add(checkBox);
            add(Box.createHorizontalGlue());

            //set up association with our Aux panel
            associatedField = assocField;
            associatedField.setEnabled(checkBox.isSelected());
        }

        /** When toggled on and off, we simply set our associated
         * AuxFilePanel's enabled bit to our selected status
         */
        public void actionPerformed(ActionEvent e) {
            associatedField.setEnabled(checkBox.isSelected());
        }

        // accessor so that ToolsDialog may query our checkbox's status
        // when determining what tests to run
        public boolean isSelected() { return checkBox.isSelected(); }

        public void setSubdomainStatus(boolean selected) {
        	checkBox.setSelected(selected);
        	associatedField.setEnabled(checkBox.isSelected());
        }

    }


    static class NeedInputDialog extends JDialog implements ActionListener {
        private static final String SC_SPECIFY = "You_must_specify_a";
        private static final String SC_NEED_INPUT = "Need_Input";

        public NeedInputDialog(JFrame owner, String inputName) {
            //create the dialog
            super(owner, Helper.getTranslation(SC_NEED_INPUT), true);


            //add the error message
            JLabel message = new JLabel(Helper.getTranslation(SC_SPECIFY)
                                        + " " +
                                        Helper.getTranslation(inputName) +
                                        ".", SwingConstants.CENTER);
            getContentPane().add(message, BorderLayout.CENTER);

            //add ok button for closing
            JPanel okPanel = new JPanel();
            KirrkirrButton ok = new KirrkirrButton(Helper.getTranslation(SC_OK), null, this);
            okPanel.add(ok);
            getContentPane().add(okPanel, BorderLayout.SOUTH);


            Dimension d = message.getPreferredSize();
            setSize(new Dimension(d.width + 20, 120));

            Rectangle bnds = owner.getBounds();
            //place it
            setLocation(bnds.x + bnds.width/2-d.width/2 - 10,
                        bnds.y + bnds.height/2-35);

            //pop up
            setVisible(true);
        }

        //when user clicks ok, just close
        public void actionPerformed(ActionEvent e) {
            dispose();
        }

    }

    private class EditorPanel extends JPanel implements TreeSelectionListener {

    	public EditorPanel() {
    		super();

    		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    		JPanel conversionPanel = new JPanel();
    		conversionPanel.setLayout(new BoxLayout(conversionPanel, BoxLayout.Y_AXIS));
    		conversionPanel.setBorder(
    				BorderFactory.createTitledBorder(Helper.getTranslation(SC_CONVERSION)));

    		JLabel pLabel =
    			new JLabel(Helper.getTranslation(SC_OLD_PNAME) + ": ");
    		JPanel pTextPanel = new JPanel();
    		parentDomain = new JTextField(20);
    		isChildDomain =
    			new ParentDomainCheckBox(Helper.getTranslation(SC_IS_SUBD), parentDomain);

    		JPanel oldNamePanel = new JPanel();
    		JLabel oldNameLabel =
    			new JLabel(Helper.getTranslation(SC_OLD_DNAME) + ": ");
    		oldDomainName = new JTextField(20);
    		oldDomainName.addFocusListener(new FocusListener() {
    			public void focusLost(FocusEvent evt) {
    				oldDName = oldDomainName.getText();
    				DefaultMutableTreeNode oldNode;
    				if (isChildDomain.isSelected() && !parentDomain.getText().equalsIgnoreCase(""))
    					oldNode = (DefaultMutableTreeNode)entries.get(
    						oldDName+parentDomain.getText()+"@");
    				else
    					oldNode = (DefaultMutableTreeNode)entries.get(oldDName+"@");
    				if (oldNode != null) {
    					DomainConversionEntry oldEntry =
    						(DomainConversionEntry)oldNode.getUserObject();
    					if (oldNode.getParent() == root)
    						setFields(oldEntry, null);
    					else {
    						DefaultMutableTreeNode oldParentNode =
    							(DefaultMutableTreeNode)oldNode.getParent();
    						DomainConversionEntry oldParent =
    							(DomainConversionEntry)oldParentNode.getUserObject();
    						setFields(oldEntry, oldParent.getOldName());
    					}
    				}
    			}
    			public void focusGained(FocusEvent evt) {}
    		});

    		oldNamePanel.add(oldNameLabel);
    		oldNamePanel.add(oldDomainName);
    		conversionPanel.add(oldNamePanel);

    		conversionPanel.add(Box.createVerticalStrut(5));

    		JPanel newNamePanel = new JPanel();
    		JLabel newNameLabel =
    			new JLabel(Helper.getTranslation(SC_NEW_DNAME) + ": ");
    		newDomainName = new JTextField(20);

    		newNamePanel.add(newNameLabel);
    		newNamePanel.add(newDomainName);
    		conversionPanel.add(newNamePanel);

    		add(conversionPanel);

    		JPanel parentPanel = new JPanel();
    		parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.Y_AXIS));
    		parentPanel.setBorder(BorderFactory.createTitledBorder(""));

    		pTextPanel.add(pLabel);
    		pTextPanel.add(parentDomain);

    		parentPanel.add(isChildDomain);
    		parentPanel.add(pTextPanel);

    		JPanel cbPanel = new JPanel();
    		cbPanel.setBorder(BorderFactory.createTitledBorder(""));
    		useFullPNames = new JCheckBox(Helper.getTranslation(SC_USE_FULL_PATHS));
    		cbPanel.add(useFullPNames);

    		JPanel buttonPanel = new JPanel();
    		buttonPanel.setLayout(new GridLayout(1, 3));
    		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));

    		add = new KirrkirrButton(Helper.getTranslation(SC_ADD), DomainConverterEditor.this);
    		clear = new KirrkirrButton(Helper.getTranslation(SC_CLEAR), DomainConverterEditor.this);
    		remove = new KirrkirrButton(Helper.getTranslation(SC_REMOVE), DomainConverterEditor.this);

    		buttonPanel.add(add);
    		buttonPanel.add(remove);
    		buttonPanel.add(clear);

    		add(cbPanel);
    		add(parentPanel);
    		add(buttonPanel);

    	}

    	public void setFields(DomainConversionEntry curr, String parentName) {
    		if (curr == null) {
    			oldDomainName.setText("");
    			newDomainName.setText("");
    		}
    		else {
    			oldDomainName.setText(curr.getOldName());
    			newDomainName.setText(curr.getNewName());
    		}
    		if (parentName == null) {
    			parentDomain.setText("");
    			isChildDomain.setSubdomainStatus(false);
    		}
    		else {
    			parentDomain.setText(parentName);
    			isChildDomain.setSubdomainStatus(true);
    		}
    	}

    	public void valueChanged(TreeSelectionEvent e) {
    		TreePath selectionPath =
    			domainTree.getSelectionPath();
    		if (selectionPath == null) return;
    		DefaultMutableTreeNode selected =
    			(DefaultMutableTreeNode)(selectionPath.getLastPathComponent());
    		if (selected == root) return;

    		DefaultMutableTreeNode parent = (DefaultMutableTreeNode)selected.getParent();
    		if (parent == root)
    			setFields((DomainConversionEntry)(selected.getUserObject()), null);
    		else {
    			if (useFullPNames.isSelected())
    				setFields((DomainConversionEntry)(selected.getUserObject()),
    	    				getFullParentPath(selected));
    			else
    				setFields((DomainConversionEntry)(selected.getUserObject()),
    				((DomainConversionEntry)(parent.getUserObject())).getOldName());
    		}
    	}
    }
}
