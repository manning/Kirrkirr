package Kirrkirr.util;

import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xerces.parsers.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.swing.tree.*;


/** This class is parses a Domain Converter XML document and then helps
 * convert domains in different contexts during a Kirrkirr run.
 *
 *  @author Chloe Kiddon 2005
 */
public class DomainConverter extends DefaultHandler {
	
	public static final String DC_ROOT = "conversion";
	public static final String CONVERSION_ENTRY = "domain";
	
	private Document doc;
    private DOMParser parser;
    private String filename;

    
    /**
     * opens the given filename and parses document
     */
    public DomainConverter(String filename) throws Exception {
        this.filename = filename;
        parser = new DOMParser();

        try {
            parser.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
            parser.setErrorHandler(this);
        } catch (Exception ex) {
            if (Dbg.DOMAIN_CONVERT) {
                Dbg.print("DomainConverter Constructor error");
                ex.printStackTrace();
            }
        }
        initConverter();
    }
    
    /** Initialize the domain converter
     */
    private void initConverter() throws Exception {
    	
    	parser.parse(new InputSource(new FileInputStream(filename)));

        doc = parser.getDocument();

        Element root = doc.getDocumentElement();
        if (root.getTagName().equalsIgnoreCase("parseError")) {
        	throw new Exception("XML Parsing Error of Domain Converter File.");
        }
        if ( ! root.getTagName().equals(DC_ROOT)) {
            throw new Exception("Invalid Document; expecting " + DC_ROOT +
            ", found " + root.getTagName());
        }
    }
    
    public DefaultMutableTreeNode getTreeRoot() {
    	if (doc == null) return null;
    	
    	DefaultMutableTreeNode root = new DefaultMutableTreeNode("domains");
    	NodeList nodes = doc.getDocumentElement().getChildNodes();
    	for (int i = 0; i < nodes.getLength(); i++) {
    		Node curr = nodes.item(i);
    		DefaultMutableTreeNode newbie = getTreeRootHelper(curr);
    		if (newbie != null)
    			root.add(newbie);
    	}
    	return root;
    }
    
    public DefaultMutableTreeNode getTreeRootHelper(Node node) {
    		DefaultMutableTreeNode curr = new DefaultMutableTreeNode();
    		if (node.getNodeType() != Node.ELEMENT_NODE) return null;
    		Element enode = (Element)node;
    		curr.setUserObject(new DomainConversionEntry(enode.getAttribute("old"),
					enode.getAttribute("new")));
    		
    		if (node.hasChildNodes()) {
    			NodeList children = node.getChildNodes();
    			Node child;
    			if (children != null) {
    				int numChildren = children.getLength();
    				for( int i = 0; i < numChildren; i++) {
    					child = children.item(i);
    					if (child != null) {
    						DefaultMutableTreeNode newbie = getTreeRootHelper(child);
    						if (newbie != null)
    							curr.add(newbie);
    					}
    				}
    			}
    		}
    		return curr;
    }
    
    public String getConversion(String oldDomain) {
    	
    	Element root = doc.getDocumentElement();

    	NodeList nodes = root.getElementsByTagName(CONVERSION_ENTRY);
    	int len = nodes.getLength();
    	
    	if (len < 1) {
            return oldDomain;
        }
    	Node match = null;
    	for (int i = 0; i <len; i++) {
    		Node currNode = nodes.item(i);
    		Element eCurr = (Element)currNode;
    		if (eCurr.getAttribute("old").compareTo(oldDomain) == 0) {
    			match = currNode;
    			break;
    		}
    	}
    	if (match == null) return oldDomain;
    	Element ematch = (Element) match;
    	//return URLHelper.decode(topElem.getAttribute("name"));
    	return (ematch.getAttribute("new"));
    }
    
    public String[] getConversion(String[] oldDomains) {
    	
    	String[] newDomains = new String[oldDomains.length];
    	Element root = doc.getDocumentElement();
    	NodeList nodes = root.getElementsByTagName(CONVERSION_ENTRY);
    	int len = nodes.getLength();
    	
    	if (len < 1) {
            return oldDomains;
        }
    	Node match = null;
    	int i;
    	for (int dom = 0; dom < oldDomains.length; dom++) {
	    	for (i = 0; i <len; i++) {
	    		Node currNode = nodes.item(i);
	    		Element eCurr = (Element)currNode;
	    		if (eCurr.getAttribute("old").compareTo(oldDomains[dom]) == 0) {
	    			match = currNode;
	    			if (match == null) {
	    				for (int j = dom; j < oldDomains.length; j++)
	    					newDomains[j] = oldDomains[j];
	    				return newDomains;
	    			}
	    			else {
	    				Element ematch = (Element) match;
	    				newDomains[dom] = ematch.getAttribute("new");
	    			}
	    			break;
	    		}
	    		
	    	}
	    	if (i == len) {
	    		for (int k = dom; k < oldDomains.length; k++)
	    			newDomains[k] = oldDomains[k];
	    		return newDomains;
	    	} else {
	    		nodes = match.getChildNodes();
	    		len = nodes.getLength();
	    	}
    	}
    	return newDomains;
    }
    
    public String getConversion(String oldDomain, Node start) {

    	NodeList nodes; 
    	if (start == null) {
    		Element root = doc.getDocumentElement();
        	nodes = root.getElementsByTagName(CONVERSION_ENTRY);
    	} else 
    		nodes = start.getChildNodes();
    	int len = nodes.getLength();
    	
    	if (len < 1) {
            return oldDomain;
        }
    	Node match = null;
    	for (int i = 0; i <len; i++) {
    		Node currNode = nodes.item(i);
    		Element eCurr = (Element)currNode;
    		if (eCurr.getAttribute("old").compareTo(oldDomain) == 0) {
    			match = currNode;
    			break;
    		}
    	}
    	if (match == null) return oldDomain;
    	
    	Element ematch = (Element) match;
        
    	//return URLHelper.decode(topElem.getAttribute("name"));
    	return (ematch.getAttribute("new"));
    }
    
    public Node getConversionNode(String oldDomain, Node start) {
    	NodeList nodes; 
    	if (start == null) {
    		Element root = doc.getDocumentElement();
        	nodes = root.getElementsByTagName(CONVERSION_ENTRY);
    	} else 
    		nodes = start.getChildNodes();
    	int len = nodes.getLength();
    	
    	if (len < 1) {
            return null;
        }
    	Node match = null;
    	for (int i = 0; i <len; i++) {
    		Node currNode = nodes.item(i);
    		Element eCurr = (Element)currNode;
    		if (eCurr.getAttribute("old").compareTo(oldDomain) == 0) {
    			match = currNode;
    			break;
    		}
    	}
    	return match;
    }
}
