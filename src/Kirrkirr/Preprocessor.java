package Kirrkirr;


import Kirrkirr.util.*;

import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException; 
import org.xml.sax.ext.LexicalHandler;

/**
 * @author Chloe
 * @version Aug 2, 2005
 */
public class Preprocessor extends DefaultHandler implements LexicalHandler {
//public class Preprocessor extends SAXParser implements LexicalHandler {
	
	public static final String SC_SUBENTRIES = "SUBENTRIES";
	public static final String SC_SUBENTRY = "SUBENTRY";
	public static final String SC_MAINENTRY = "MAINENTRY";
	
	public static final int NO_SUB_FIX = -1;
	public static final int NO_LINKS = 0;
	public static final int MAIN_LINK = 1;
	public static final int SUB_LINK = 2;
	public static final int BOTH_LINKS = 3;
	
	private String encoding;
	private static final String DEFAULT_ENCODING = "ISO-8859-1";
	
	private String filename;
	private Hashtable alertTags;
	private String entryPath;
	private String entryTag;
	private String newSubAttr;
	private int subFixType;
	
	private String subLinksTag;
	private String subLinkTag;
	private String mainLinkTag;
	
	private BufferedWriter out = null;
	
	public String currTagPath;
	private boolean inSubTag;
	private boolean inSubEntry;
	//character buffer needed so that special character entities can be taken care of
	private StringBuffer characterBuffer;
	private boolean inEntity = false;
	private static final int MINI_CAPACITY = 25;
	
	private boolean useConv;
	private DomainConverter dc;
	private String domainPath;
	private Vector entryDomainVec;
	
	private StringBuffer subentryBuffer;
	private Vector subentries;
	private static final int INIT_CAPACITY = 50;
	
	public Preprocessor(String newFile, Hashtable parseRegexps, 
			String newSubAttr, String entryPath) {
		this(newFile, parseRegexps, newSubAttr, entryPath, null, null);
	}
	
	public Preprocessor(String newFile, Hashtable parseRegexps, 
			String newSubAttr, String entryPath, String domainConv, String domainPath) {
		super();
		try {
			filename = newFile;
			//FileOutputStream fos = new FileOutputStream(filename);
			out = new BufferedWriter(new FileWriter(filename));
			alertTags = parseRegexps;
			this.entryPath = entryPath;
			inSubTag = false;
			inSubEntry = false;
			entryTag = entryPath.substring(entryPath.lastIndexOf("/")+1);
			if (entryPath != null) {
				this.newSubAttr = newSubAttr;
				this.subFixType = NO_LINKS;
			} else
				this.subFixType = NO_SUB_FIX;
			currTagPath = "";
			subentryBuffer = new StringBuffer(INIT_CAPACITY);
			characterBuffer = new StringBuffer(MINI_CAPACITY);
			subentries = new Vector();
			entryDomainVec = new Vector();			
			if (domainConv == null)
				useConv = false;
			else {
				try {
					dc = new DomainConverter(domainConv);
					this.domainPath = domainPath;
					useConv = true;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (FileNotFoundException ex) {
			//print some sort of debug stuff
			ex.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public void setMainLinkTag(String mlt) {
		if (mlt != null)  {
			mainLinkTag = mlt;
			if (subFixType == NO_LINKS)
				subFixType = MAIN_LINK;
			else if (subFixType == SUB_LINK)
				subFixType = BOTH_LINKS;
		} else {
			mainLinkTag = null;
			if (subFixType == MAIN_LINK)
				subFixType = NO_LINKS;
			else if (subFixType == BOTH_LINKS)
				subFixType = SUB_LINK;
		}
	}
	
	public void setSubLinkTags(String outer, String single) {
		if (outer != null && single != null) {
			subLinksTag = outer;
			subLinkTag = single;
			if (subFixType == NO_LINKS)
				subFixType = SUB_LINK;
			else if (subFixType == MAIN_LINK)
				subFixType = BOTH_LINKS;
		} else {
			subLinksTag = null;
			subLinkTag = null;
			if (subFixType == SUB_LINK)
				subFixType = NO_LINKS;
			else if (subFixType == BOTH_LINKS)
				subFixType = MAIN_LINK;
		}
	}
	
	public void setNewSubAttr(String path) {
		if (path != null) {
			newSubAttr = path;
			if (subFixType == NO_SUB_FIX) {
				if (subLinkTag != null) {
					if (mainLinkTag != null)
						subFixType = BOTH_LINKS;
					else
						subFixType = SUB_LINK;
				} else {
					if (mainLinkTag != null)
						subFixType = MAIN_LINK;
					else
						subFixType = NO_LINKS;
				}
			}
		} else {
			newSubAttr = null;
			subFixType = NO_SUB_FIX;
		}
	}
	
	public void setDomainConv(String domainConv, String domainPath) {
		if (filename == null) {
			useConv = false;
			this.domainPath = null;
		}
		else {
			try {
				dc = new DomainConverter(domainConv);
				this.domainPath = domainPath;
				useConv = true;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void setEncoding(String e) {
		encoding = e;
	}
	
	public int isAttrPath(Attributes attrs, int start) {
		if (attrs == null) return -1;
		int len = attrs.getLength();
        for (int i = start; i < len; i++) {
        	String qName = attrs.getQName(i);
        	Object alert = 
        		alertTags.get(currTagPath+"/@"+qName);
            if (alert != null) return i;
        }
		return -1;
	}
	
	public void startElement(String uri, String name, String raw,
            Attributes attrs) throws SAXException {
		if (characterBuffer.length() != 0) {
			processCharData(characterBuffer.toString().toCharArray(), 0, 
					characterBuffer.length());
			characterBuffer.setLength(0);
		}
		currTagPath+=("/"+name);
		if (currTagPath.equals(entryPath))
			entryDomainVec.clear();
		String[] alert = (String[])alertTags.get(currTagPath);
		if (subFixType != NO_SUB_FIX && alert!=null && alert[0].equals("")) {
			inSubTag = false;
			inSubEntry = true;
			subentries.add(name);
			subentryBuffer.append("<"+entryTag);
			if (attrs != null) {
                int len = attrs.getLength();
                for (int i = 0; i < len; i++) {
                	subentryBuffer.append(' ');
                	subentryBuffer.append(attrs.getQName(i));
                	subentryBuffer.append("=\"");
                	subentryBuffer.append(attrs.getValue(i));
                	subentryBuffer.append('"');
                }
            }
			subentryBuffer.append(' ');
            subentryBuffer.append(newSubAttr);
            subentryBuffer.append("=\"SUB\"");
			subentryBuffer.append(">");
			if (subFixType == MAIN_LINK || subFixType == BOTH_LINKS) {
				subentryBuffer.append('\n');
				subentryBuffer.append("<"+mainLinkTag+">");
				subentryBuffer.append(name);
				subentryBuffer.append("</"+mainLinkTag+">\n");
			}
		}
		else {
			try {
				if (alert != null) inSubTag = true;
				else inSubTag = false;
				int start = isAttrPath(attrs, 0);
				int old = 0;
				if (start != -1) {
					StringBuffer attrParse = new StringBuffer(30);
					if (inSubEntry) {
						subentryBuffer.append("<"+name);
					} else {
						out.write("<"+name);
					}
					while (start != -1) {
						try {
							if (old != start) {
								for (int a = old; a < start; a++) {
									if (inSubEntry) {
										subentryBuffer.append(' ');
										subentryBuffer.append(attrs.getQName(a));
										subentryBuffer.append("=\"");
										subentryBuffer.append(attrs.getValue(a));
										subentryBuffer.append('"');
									} else {
										out.write(' ');
				                        out.write(attrs.getQName(a));
				                        out.write("=\"");
				                        out.write(attrs.getValue(a));
				                        out.write('"');
									}
								}
							}
							String path = currTagPath+"/@"+attrs.getQName(start);
							String[] info = (String[])alertTags.get(path);
							String newTag = info[1];
							String unparsedAttr = attrs.getValue(start);
							Pattern p = Pattern.compile(info[0]);
							String[] parts;
							if (useConv && domainPath.equals(path))
								parts = dc.getConversion(p.split(unparsedAttr));
							else
								parts = p.split(unparsedAttr);
							int pLen = parts.length;
							for (int i = 0; i < pLen; i++) {
								attrParse.append(
									"<"+newTag+">"+parts[i].trim()+"</"+newTag+">");
							}
							old = start+1;
							start = isAttrPath(attrs, start+1);
						} catch (PatternSyntaxException pse) {
							JOptionPane.showMessageDialog(
									Kirrkirr.kk.window, "Pattern Syntax Error in regexp for"
									+ currTagPath + ".");
							//can we throw an saxparsing exception? that would be nice.
						}
					}
					int len = attrs.getLength();
					if (old != len-1) {
						for (int a = old; a < len; a++) {
							if (inSubEntry) {
								subentryBuffer.append(' ');
								subentryBuffer.append(attrs.getQName(a));
								subentryBuffer.append("=\"");
								subentryBuffer.append(attrs.getValue(a));
								subentryBuffer.append('"');
							} else {
								out.write(' ');
		                        out.write(attrs.getQName(a));
		                        out.write("=\"");
		                        out.write(attrs.getValue(a));
		                        out.write('"');
							}
						}
					}
					if (inSubEntry) {
						subentryBuffer.append('>');
						subentryBuffer.append(attrParse.toString()); 
					} else {
						out.write('>');
						out.write(attrParse.toString());
					}
				} else {
					if (!inSubEntry) {
		                out.write("<");
		                out.write(name);
		                if (attrs != null) {
		                    int len = attrs.getLength();
		                    for (int i = 0; i < len; i++) {
		                        out.write(' ');
		                        String qName = attrs.getQName(i);
		                        out.write(qName);
		                        out.write("=\"");
		                        String value = attrs.getValue(i);
		                        if (useConv && domainPath.equals(currTagPath+"/@"+qName)) {
		        					entryDomainVec.add(value);
		        					int size = entryDomainVec.size();
		        					String[] oldDomains = new String[len];
		        					for (int j = 0; j < size; j++)
		        						oldDomains[j] = (String)entryDomainVec.elementAt(j);
		        					String[] newDomains  = dc.getConversion(oldDomains);
		        					value = newDomains[size-1];
		        				}
		                        out.write(value);
		                        out.write('"');
		                    }
		                }
		                out.write('>');
		                if (currTagPath.equals(entryPath))
		                	out.write('\n');
					} else {
			        	subentryBuffer.append("<");
			        	subentryBuffer.append(name);
		                if (attrs != null) {
		                    int len = attrs.getLength();
		                    for (int i = 0; i < len; i++) {
		                    	subentryBuffer.append(' ');
		                    	String qName = attrs.getQName(i);
		                    	subentryBuffer.append(qName);
		                    	subentryBuffer.append("=\"");
		                    	String value = attrs.getValue(i);
		                        if (useConv && domainPath.equals(currTagPath+"/@"+qName)) {
		        					entryDomainVec.add(value);
		        					int size = entryDomainVec.size();
		        					String[] oldDomains = new String[len];
		        					for (int j = 0; j < size; j++)
		        						oldDomains[j] = (String)entryDomainVec.elementAt(j);
		        					String[] newDomains  = dc.getConversion(oldDomains);
		        					value = newDomains[size-1];
		        				}
		                    	subentryBuffer.append(value);
		                    	subentryBuffer.append('"');
		                    }
		                }
		                subentryBuffer.append('>');
		                if (currTagPath.equals(entryPath))
		                	subentryBuffer.append('\n');
			        }
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
	public void endElement(String uri, String name, String raw) 
	throws SAXException {
		if (characterBuffer.length() != 0) {
			processCharData(characterBuffer.toString().toCharArray(), 0, 
					characterBuffer.length());
			characterBuffer.setLength(0);
		}
        String[] alert = (String[])alertTags.get(currTagPath);
		if (subFixType != NO_SUB_FIX && alert!=null && alert[0].equals("")) {
			subentryBuffer.append("</"+entryTag+">\n");
			inSubEntry = false;
		} else {
			if (!inSubEntry) {
				try {
	                out.write("</");
	                out.write(name);
	                out.write(">");
	                if (currTagPath.equals(entryPath) && subentryBuffer.length() != 0){
	                	if (subFixType == SUB_LINK || subFixType == BOTH_LINKS) {
	                		out.write('<');
	                		out.write(subLinksTag);
	                		out.write(">\n");
	                		for (int i = 0; i < subentries.size(); i++) {
	                			out.write("<"+subLinkTag+">");
	                			out.write(((String)subentries.elementAt(i)).trim());
	                			out.write("</"+subLinkTag+">\n");
	                		}
	                		out.write("</");
	                		out.write(subLinksTag);
	                		out.write(">\n");
	                	}
	    				out.write(subentryBuffer.toString());
	    				subentryBuffer.setLength(0);
	    				subentries.clear();
	    			}
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
			} else {
				subentryBuffer.append("</"+name+">");
			}
			
		}
		inSubTag = false;
		currTagPath=currTagPath.substring(0, currTagPath.lastIndexOf(name)-1);
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (!inEntity) {
			for (int i = 0; i < length; i++) {
				int value = ch[start+i];
				if (value > 122)
					characterBuffer.append("&#"+Integer.toString(value)+';');
				else
					characterBuffer.append(ch[start+i]);
			}
		}
	}
	
	public void processCharData(char[] ch, int start, int length) {
		//super.characters(ch, start, length);
		try {
		if (inSubTag) {
			try {
				String[] info = (String[])alertTags.get(currTagPath);
				String newTag = info[1];
				String raw = new String(ch, start, length);
				Pattern p = Pattern.compile(info[0]);
				String[] parts;
				if (useConv && domainPath.equals(currTagPath))
					parts = dc.getConversion(p.split(raw));
				else
					parts = p.split(raw);
				int pLen = parts.length;
				for (int i = 0; i < pLen; i++) {
					if (inSubEntry)
						subentryBuffer.append(
							"<"+newTag+">"+parts[i].trim()+"</"+newTag+">");
					else
						out.write("<"+newTag+">"+parts[i].trim()+"</"+newTag+">");
				}
			} catch (PatternSyntaxException pse) {
				JOptionPane.showMessageDialog(
						Kirrkirr.kk.window, "Pattern Syntax Error in regexp for"
						+ currTagPath + ".");
				//can we throw an saxparsing exception? that would be nice.
			} catch (IOException ioe) {
				ioe.printStackTrace();
				//print some sort of user error reporting
			}
		} else {
			try {
					
				if (useConv && domainPath.equals(currTagPath)) {
					String raw = new String(ch, start, length);
					entryDomainVec.add(raw);
					int len = entryDomainVec.size();
					String[] oldDomains = new String[len];
					for (int i = 0; i < len; i++)
						oldDomains[i] = (String)entryDomainVec.elementAt(i);
					String[] newDomains  = dc.getConversion(oldDomains);
					raw = newDomains[len-1];
					if (inSubEntry)
						subentryBuffer.append(raw);
					else 
						out.write(raw);
				} else {
					if (inSubEntry) {
						System.out.println("b");
						subentryBuffer.append(ch, start, length);
					}
					else {
						out.write(ch, start, length);
					}
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
				//print some sort of user error reporting
			}
		}
		} catch (NullPointerException npe) { 
        	System.out.println("char");
        	npe.printStackTrace(); }
	}
	
    public void processingInstruction (String target, String data) throws SAXException {
		System.out.println("PI!");
		//if (Dbg.PARSE) Dbg.linePrint(null, "Processing Instruction: "
          //      + target + ' ' + escape(data.toCharArray(),0,data.length()));
            try {
            	if (!inSubEntry) {
	                out.write("<?");
	                out.write(target);
	                if (data != null && data.length() > 0) {
	                    out.write(' ');
	                    out.write(data);
	                }
	                out.write("?>\n");
            	} else {
            		subentryBuffer.append("<?");
            		subentryBuffer.append(target);
	                if (data != null && data.length() > 0) {
	                	subentryBuffer.append(' ');
	                	subentryBuffer.append(data);
	                }
	                subentryBuffer.append("?>\n");
            	}
            } catch(Exception e) { e.printStackTrace(); }
	}
    
    public void startDocument() {
    	try {
	    	if (encoding != null)
	    		out.write("<?xml version=\"1.0\" encoding=\"" +
	    				encoding +"\"?>\n");
	    	else
	    		out.write("<?xml version=\"1.0\" encoding=\"" +
	    				DEFAULT_ENCODING +"\"?>\n");
    	} catch (IOException ioe) {
    		ioe.printStackTrace();
    	}
    }
	public void endDocument ()
    {
		try {
     out.close();
		} catch (IOException ioe) {}
    }
	
	/** Normalizes the given string. */
    protected String normalize(String s) {
        StringBuffer str = new StringBuffer();

        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '<': {
                    str.append("&lt;");
                    break;
                }
                case '>': {
                    str.append("&gt;");
                    break;
                }
                case '&': {
                    str.append("&amp;");
                    break;
                }
                case '"': {
                    str.append("&quot;");
                    break;
                }
                case '\r':
                    /* case '\n': {
                    str.append("&#");
                    str.append(Integer.toString(ch));
                    str.append(';');
                    break;
                    // else, default append char
                    }*/
                default: {
                    str.append(ch);
                }
            }
        }
        return str.toString();
    } // normalize(String):String
    
    
    //LEXICAL HANDLER METHODS
    
    public void	comment(char[] ch, int start, int length) {}
    public void endCDATA() {}
    public void startCDATA() {}
    public void endDTD() {}
    public void startDTD(String name, String publicId, String systemId) {}
    
    public void startEntity(String name) {
    	inEntity = true;
    	characterBuffer.append('&');
    	characterBuffer.append(name);
    	characterBuffer.append(';');
    }
    public void endEntity(String name) {
    	inEntity = false;
    }
}
