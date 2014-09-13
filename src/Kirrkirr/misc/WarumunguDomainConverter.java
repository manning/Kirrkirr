package Kirrkirr.misc;

import Kirrkirr.util.*;
import Kirrkirr.dictionary.*;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.traversal.NodeIterator;
import org.apache.xpath.objects.XObject;


public class WarumunguDomainConverter {

	private static Hashtable conversions;

	/** Usage: java Kirrkirr.misc.WarumunguDomainConverter 
	 *           oldxmlfile newxmlfile
	 */
	public static void main(String argv[]) {
		if(argv.length<2) {
			System.out.println("Usage: java WarumunguDomainConverter oldxmlfile newxmlfile");
			return;
		}
		convertDomains(argv[0], argv[1]);
	}

	private static void convertDomains(String xmlDict, String newfile) {
		buildConversionTable();
		expandDictionary(xmlDict, newfile);
	}

	private static void expandDictionary(String filename, String newfile) {
	        RandomAccessFile raf = null;
		BufferedWriter outWriter = null;
		try {
	    		int total=0;
	    		raf=new RandomAccessFile(filename,"r");
		    	outWriter = new BufferedWriter(new FileWriter(newfile));

			long fpos=0;
			String line=raf.readLine();
	    		String oldline;
			Regex startreg=OroRegex.newRegex("<SEM>");
			while (line!=null){
				//read until you reach the current startentry
				while(line!=null && !startreg.hasMatch(line)){
				    //output line as is
				    outWriter.write(line);
				    outWriter.newLine();
				    fpos=raf.getFilePointer();
				    line=raf.readLine();     			      
				}	
				if (line==null) continue;
				oldline = line;
				line = getConversion(line);
				
				if(line == null){
				    System.out.println("couldn't find conv for: " + oldline);
				}
				else {
				    outWriter.write("<SEM>");
				    outWriter.write(line);
				    outWriter.write("</SEM>");
				    outWriter.newLine();
				}
				fpos=raf.getFilePointer();
				line=raf.readLine();
				total++;	   
	    		}
	    	}
		catch(Exception e){
	    		e.printStackTrace();
	    	} finally {
		    try {
	    		if (raf != null) raf.close();
			if (outWriter != null) outWriter.close();
		    } catch (Exception e) {
		    }
		}
    	}  


	private static String getConversion(String line) {
		if(line == null) return null;
		int startTagEnd = line.indexOf(">");
		int closeTagStart = line.lastIndexOf("<");
		String substr = line.substring(startTagEnd+1,closeTagStart);
		return (String) conversions.get(substr);
	}

	private static void buildConversionTable() {
		conversions = new Hashtable(200);
		conversions.put("A", "<DOMAIN>Topography, soil and natural resources</DOMAIN>");
		conversions.put("A.0", "<DOMAIN>Topography, soil and natural resources</DOMAIN><DOMAIN>place names</DOMAIN>");
		conversions.put("A.1", "<DOMAIN>Topography, soil and natural resources</DOMAIN><DOMAIN>eminences in the landscape</DOMAIN>");
		conversions.put("A.2", "<DOMAIN>Topography, soil and natural resources</DOMAIN><DOMAIN>holes in the earth</DOMAIN>"); 
		conversions.put("A.3", "<DOMAIN>Topography, soil and natural resources</DOMAIN><DOMAIN>types of landscape differing by growth or soil</DOMAIN>");
		conversions.put("A.4", "<DOMAIN>Topography, soil and natural resources</DOMAIN><DOMAIN>soil and natural resources</DOMAIN>");
		conversions.put("B", "<DOMAIN>Environment</DOMAIN>");
		conversions.put("B.1", "<DOMAIN>Environment</DOMAIN><DOMAIN>Sky and heavenly bodies</DOMAIN>"); 
		conversions.put("B.2", "<DOMAIN>Environment</DOMAIN><DOMAIN>Temperature</DOMAIN>"); 
		conversions.put("B.3", "<DOMAIN>Environment</DOMAIN><DOMAIN>Wind</DOMAIN>"); 
		conversions.put("C", "<DOMAIN>Water</DOMAIN>"); 
		conversions.put("C.1", "<DOMAIN>Water</DOMAIN><DOMAIN>precipitation and attending phenomena</DOMAIN>");
		conversions.put("C.2", "<DOMAIN>Water</DOMAIN><DOMAIN>cultural and natural sources of confined water</DOMAIN>");
		conversions.put("C.3", "<DOMAIN>Water</DOMAIN><DOMAIN>water sources which intermittently flow</DOMAIN>");
		conversions.put("C.4", "<DOMAIN>Water</DOMAIN><DOMAIN>types of immersion and application of water</DOMAIN>");
		conversions.put("D", "<DOMAIN>Animals and animal products</DOMAIN>");
		conversions.put("D.0", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>general terms</DOMAIN>");
		conversions.put("D.1", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>mammals</DOMAIN>");
		conversions.put("D.2", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>large birds</DOMAIN>"); 
		conversions.put("D.3", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>snakes</DOMAIN>"); 
		conversions.put("D.4", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>reptiles, amphibians</DOMAIN>");
		conversions.put("D.5", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>birds</DOMAIN>");
		conversions.put("D.5.1", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>birds</DOMAIN><DOMAIN>owls</DOMAIN>");
		conversions.put("D.5.2", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>birds</DOMAIN><DOMAIN>terrestrial hawks</DOMAIN>");
		conversions.put("D.5.3", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>birds</DOMAIN><DOMAIN>ducks, teals and geese</DOMAIN>"); 
		conversions.put("D.5.4", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>birds</DOMAIN><DOMAIN>pigeons and doves</DOMAIN>");
		conversions.put("D.5.5", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>birds</DOMAIN><DOMAIN>parrots</DOMAIN>");
		conversions.put("D.5.6", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>birds</DOMAIN><DOMAIN>bitterns and night-herons</DOMAIN>");
		conversions.put("D.5.7", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>birds</DOMAIN><DOMAIN>herons, stilts, waders etc</DOMAIN>");
		conversions.put("D.5.8", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>birds</DOMAIN><DOMAIN>beach birds</DOMAIN>");
		conversions.put("D.5.9", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>birds</DOMAIN><DOMAIN>sea birds</DOMAIN>");
		conversions.put("D.5.10", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>birds</DOMAIN><DOMAIN>quail</DOMAIN>");
		conversions.put("D.5.11", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>birds</DOMAIN><DOMAIN>other birds</DOMAIN>"); 
		conversions.put("D.6", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>delicacies</DOMAIN>");
		conversions.put("D.7", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>insects</DOMAIN>");
		conversions.put("D.8", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN>");
		conversions.put("D.8.0", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>gropers, codfish</DOMAIN>");
		conversions.put("D.8.1", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>rays</DOMAIN>"); 
		conversions.put("D.8.2", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>sharks</DOMAIN>"); 
		conversions.put("D.8.3", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>catfish</DOMAIN>"); 
		conversions.put("D.8.4", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>mullets</DOMAIN>");
		conversions.put("D.8.5", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>perch</DOMAIN>");
		conversions.put("D.8.6", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>long-toms and garfish</DOMAIN>");
		conversions.put("D.8.7", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>parrotfish and tuskfish</DOMAIN>");
		conversions.put("D.8.8", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>toadfish</DOMAIN>");
		conversions.put("D.8.9", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>emperors, trevallies, darts, sweetlips</DOMAIN>");
		conversions.put("D.8.10", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>rock cods etc</DOMAIN>");
		conversions.put("D.8.11", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>batfish and butterfish</DOMAIN>");
		conversions.put("D.8.12", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>tuna, large jumping fish</DOMAIN>"); 
		conversions.put("D.8.13", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>gudgeons and muskippers</DOMAIN>"); 
		conversions.put("D.8.14", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>small freshwater fish</DOMAIN>");
		conversions.put("D.8.15", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>salmon</DOMAIN>");
		conversions.put("D.8.16", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>herring, anchovy, bony bream</DOMAIN>");
		conversions.put("D.8.17", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>fish, shellfish, crustaceans</DOMAIN><DOMAIN>other fish</DOMAIN>");
		conversions.put("D.9", "<DOMAIN>Animals and animal products</DOMAIN><DOMAIN>introduced animals</DOMAIN>"); 
		conversions.put("E", "<DOMAIN>Plants and plant products</DOMAIN>");
		conversions.put("E.0", "<DOMAIN>Plants and plant products</DOMAIN><DOMAIN>general terms</DOMAIN>");
		conversions.put("E.1", "<DOMAIN>Plants and plant products</DOMAIN><DOMAIN>tree parts</DOMAIN>");
		conversions.put("E.2", "<DOMAIN>Plants and plant products</DOMAIN><DOMAIN>trees and bushes</DOMAIN>"); 
		conversions.put("E.3", "<DOMAIN>Plants and plant products</DOMAIN><DOMAIN>plant foods</DOMAIN>");
		conversions.put("E.4", "<DOMAIN>Plants and plant products</DOMAIN><DOMAIN>tubers</DOMAIN>");
		conversions.put("E.5", "<DOMAIN>Plants and plant products</DOMAIN><DOMAIN>tobaccos</DOMAIN>"); 
		conversions.put("E.6", "<DOMAIN>Plants and plant products</DOMAIN><DOMAIN>grasses</DOMAIN>");
		conversions.put("F", "<DOMAIN>Locations and Directions</DOMAIN>");
		conversions.put("F.1", "<DOMAIN>Locations and Directions</DOMAIN><DOMAIN>cardinal directions</DOMAIN>");
		conversions.put("F.2", "<DOMAIN>Locations and Directions</DOMAIN><DOMAIN>demonstratives</DOMAIN>");
		conversions.put("F.3", "<DOMAIN>Locations and Directions</DOMAIN><DOMAIN>location, distance</DOMAIN>"); 
		conversions.put("G", "<DOMAIN>Time</DOMAIN>"); 
		conversions.put("G.1", "<DOMAIN>Time</DOMAIN><DOMAIN>diurnal</DOMAIN>"); 
		conversions.put("G.2", "<DOMAIN>Time</DOMAIN><DOMAIN>longer than a day, not related to diurnal cycle</DOMAIN>");
		conversions.put("H", "<DOMAIN>Quantification</DOMAIN>");
		conversions.put("H.1", "<DOMAIN>Quantification</DOMAIN><DOMAIN>exact</DOMAIN>"); 
		conversions.put("H.2", "<DOMAIN>Quantification</DOMAIN><DOMAIN>qualitative</DOMAIN>");
		conversions.put("H.3", "<DOMAIN>Quantification</DOMAIN><DOMAIN>size, weight</DOMAIN>");
		conversions.put("H.4", "<DOMAIN>Quantification</DOMAIN><DOMAIN>distance</DOMAIN>");
		conversions.put("H.5", "<DOMAIN>Quantification</DOMAIN><DOMAIN>shape</DOMAIN>");
		conversions.put("H.6", "<DOMAIN>Quantification</DOMAIN><DOMAIN>arrangement</DOMAIN>"); 
		conversions.put("H.7", "<DOMAIN>Quantification</DOMAIN><DOMAIN>surface properties</DOMAIN>"); 
		conversions.put("I", "<DOMAIN>Body parts</DOMAIN>");
		conversions.put("I.1", "<DOMAIN>Body parts</DOMAIN><DOMAIN>head</DOMAIN>"); 
		conversions.put("I.2", "<DOMAIN>Body parts</DOMAIN><DOMAIN>torso, internal organs</DOMAIN>");
		conversions.put("I.3", "<DOMAIN>Body parts</DOMAIN><DOMAIN>limbs</DOMAIN>");
		conversions.put("I.4", "<DOMAIN>Body parts</DOMAIN><DOMAIN>non-localised</DOMAIN>");
		conversions.put("I.5", "<DOMAIN>Body parts</DOMAIN><DOMAIN>hair</DOMAIN>");
		conversions.put("I.6", "<DOMAIN>Body parts</DOMAIN><DOMAIN>secretions and substances</DOMAIN>");
		conversions.put("J", "<DOMAIN>Physiological reactions</DOMAIN>");
		conversions.put("J.1", "<DOMAIN>Physiological reactions</DOMAIN><DOMAIN>semi-voluntary</DOMAIN>");
		conversions.put("J.2", "<DOMAIN>Physiological reactions</DOMAIN><DOMAIN>sleep</DOMAIN>"); 
		conversions.put("J.3", "<DOMAIN>Physiological reactions</DOMAIN><DOMAIN>alimentation</DOMAIN>"); 
		conversions.put("J.4", "<DOMAIN>Physiological reactions</DOMAIN><DOMAIN>sex</DOMAIN>"); 
		conversions.put("J.5", "<DOMAIN>Physiological reactions</DOMAIN><DOMAIN>fitness</DOMAIN>"); 
		conversions.put("K", "<DOMAIN>Disease and bodily afflictions</DOMAIN>");
		conversions.put("K.1", "<DOMAIN>Disease and bodily afflictions</DOMAIN><DOMAIN>aging</DOMAIN>"); 
		conversions.put("K.2", "<DOMAIN>Disease and bodily afflictions</DOMAIN><DOMAIN>physical disability</DOMAIN>");
		conversions.put("K.3", "<DOMAIN>Disease and bodily afflictions</DOMAIN><DOMAIN>mental disability</DOMAIN>"); 
		conversions.put("K.4", "<DOMAIN>Disease and bodily afflictions</DOMAIN><DOMAIN>sickness</DOMAIN>"); 
		conversions.put("K.5", "<DOMAIN>Disease and bodily afflictions</DOMAIN><DOMAIN>medicine</DOMAIN>"); 
		conversions.put("K.6", "<DOMAIN>Disease and bodily afflictions</DOMAIN><DOMAIN>bodily discomfort</DOMAIN>"); 
		conversions.put("L", "<DOMAIN>Senses and perception, attention</DOMAIN>");
		conversions.put("L.1", "<DOMAIN>Senses and perception, attention</DOMAIN><DOMAIN>senses and attention</DOMAIN>"); 
		conversions.put("L.2", "<DOMAIN>Senses and perception, attention</DOMAIN><DOMAIN>colours, light</DOMAIN>"); 
		conversions.put("L.3", "<DOMAIN>Senses and perception, attention</DOMAIN><DOMAIN>Concealment</DOMAIN>");
		conversions.put("M", "<DOMAIN>Speech acts, vocal sounds, noises</DOMAIN>");
		conversions.put("M.1", "<DOMAIN>Speech acts, vocal sounds, noises</DOMAIN><DOMAIN>linguistic, speech acts</DOMAIN>");
		conversions.put("M.2", "<DOMAIN>Speech acts, vocal sounds, noises</DOMAIN><DOMAIN>non-linguistic</DOMAIN>");
		conversions.put("N", "<DOMAIN>Stance, change of stance</DOMAIN>"); 
		conversions.put("O", "<DOMAIN>Motion</DOMAIN>");
		conversions.put("O.0", "<DOMAIN>Motion</DOMAIN><DOMAIN>general terms</DOMAIN>"); 
		conversions.put("O.1", "<DOMAIN>Motion</DOMAIN><DOMAIN>horizontal motion</DOMAIN>"); 
		conversions.put("O.2", "<DOMAIN>Motion</DOMAIN><DOMAIN>vertical motion, falling</DOMAIN>"); 
		conversions.put("O.3", "<DOMAIN>Motion</DOMAIN><DOMAIN>walking</DOMAIN>"); 
		conversions.put("P", "<DOMAIN>Physical transfer and holding</DOMAIN>");
		conversions.put("P.1", "<DOMAIN>Physical transfer and holding</DOMAIN><DOMAIN>transfer</DOMAIN>"); 
		conversions.put("P.2", "<DOMAIN>Physical transfer and holding</DOMAIN><DOMAIN>manipulation, induced motion</DOMAIN>");
		conversions.put("P.3", "<DOMAIN>Physical transfer and holding</DOMAIN><DOMAIN>holding</DOMAIN>");
		conversions.put("Q", "<DOMAIN>Impact and concussion</DOMAIN>");
		conversions.put("Q.1", "<DOMAIN>Impact and concussion</DOMAIN><DOMAIN>(as in Walpiri roots:) pu-, pi-, etc.</DOMAIN>");
		conversions.put("Q.2", "<DOMAIN>Impact and concussion</DOMAIN><DOMAIN>striking, chopping</DOMAIN>");
		conversions.put("Q.3", "<DOMAIN>Impact and concussion</DOMAIN><DOMAIN>cutting</DOMAIN>");
		conversions.put("Q.4", "<DOMAIN>Impact and concussion</DOMAIN><DOMAIN>piercing</DOMAIN>"); 
		conversions.put("Q.5", "<DOMAIN>Impact and concussion</DOMAIN><DOMAIN>throwing</DOMAIN>");
		conversions.put("Q.6", "<DOMAIN>Impact and concussion</DOMAIN><DOMAIN>pressing</DOMAIN>");
		conversions.put("R", "<DOMAIN>Activity tempo, manner</DOMAIN>");
		conversions.put("S", "<DOMAIN>Mentation (incl. possibility, etc.)</DOMAIN>");
		conversions.put("S.1", "<DOMAIN>Mentation (incl. possibility, etc.)</DOMAIN><DOMAIN>sorrow</DOMAIN>");
		conversions.put("S.2", "<DOMAIN>Mentation (incl. possibility, etc.)</DOMAIN><DOMAIN>desire</DOMAIN>");
		conversions.put("S.3", "<DOMAIN>Mentation (incl. possibility, etc.)</DOMAIN><DOMAIN>hate</DOMAIN>"); 
		conversions.put("S.4", "<DOMAIN>Mentation (incl. possibility, etc.)</DOMAIN><DOMAIN>fear</DOMAIN>"); 
		conversions.put("S.5", "<DOMAIN>Mentation (incl. possibility, etc.)</DOMAIN><DOMAIN>anxiety</DOMAIN>");
		conversions.put("T", "<DOMAIN>Values</DOMAIN>"); 
		conversions.put("T.1", "<DOMAIN>Values</DOMAIN><DOMAIN>goodness</DOMAIN>");
		conversions.put("T.2", "<DOMAIN>Values</DOMAIN><DOMAIN>excess</DOMAIN>"); 
		conversions.put("U", "<DOMAIN>Human classification</DOMAIN>");
		conversions.put("U.1", "<DOMAIN>Human classification</DOMAIN><DOMAIN>peoples</DOMAIN>"); 
		conversions.put("U.2", "<DOMAIN>Human classification</DOMAIN><DOMAIN>age-related</DOMAIN>");
		conversions.put("U.2.1", "<DOMAIN>Human classification</DOMAIN><DOMAIN>age-related</DOMAIN><DOMAIN>age-grade terms</DOMAIN>"); 
		conversions.put("U.2.2", "<DOMAIN>Human classification</DOMAIN><DOMAIN>age-related</DOMAIN><DOMAIN>initiation</DOMAIN>");
		conversions.put("U.3", "<DOMAIN>Human classification</DOMAIN><DOMAIN>terms for females</DOMAIN>");
		conversions.put("V", "<DOMAIN>Kinship</DOMAIN>");
		conversions.put("V.1", "<DOMAIN>Kinship</DOMAIN><DOMAIN>family</DOMAIN>"); 
		conversions.put("V.2", "<DOMAIN>Kinship</DOMAIN><DOMAIN>matrilines and respect terms</DOMAIN>");
		conversions.put("V.2.1", "<DOMAIN>Kinship</DOMAIN><DOMAIN>matrilines and respect terms</DOMAIN><DOMAIN>matrilines</DOMAIN>");
		conversions.put("V.2.2", "<DOMAIN>Kinship</DOMAIN><DOMAIN>matrilines and respect terms</DOMAIN><DOMAIN>/yikirrinji/ respect terms</DOMAIN>"); 
		conversions.put("V.3", "<DOMAIN>Kinship</DOMAIN><DOMAIN>subsections</DOMAIN>"); 
		conversions.put("V.4", "<DOMAIN>Kinship</DOMAIN><DOMAIN>age-mate terms</DOMAIN>"); 
		conversions.put("V.5", "<DOMAIN>Kinship</DOMAIN><DOMAIN>Family subsections</DOMAIN>");
		conversions.put("V.5.1", "<DOMAIN>Kinship</DOMAIN><DOMAIN>Family subsections</DOMAIN><DOMAIN>Ego subsection</DOMAIN>"); 
		conversions.put("V.5.2", "<DOMAIN>Kinship</DOMAIN><DOMAIN>Family subsections</DOMAIN><DOMAIN>Fa subsection</DOMAIN>"); 
		conversions.put("V.5.3", "<DOMAIN>Kinship</DOMAIN><DOMAIN>Family subsections</DOMAIN><DOMAIN>Mo subsection</DOMAIN>"); 
		conversions.put("V.5.4", "<DOMAIN>Kinship</DOMAIN><DOMAIN>Family subsections</DOMAIN><DOMAIN>spouse subsection</DOMAIN>"); 
		conversions.put("V.5.5", "<DOMAIN>Kinship</DOMAIN><DOMAIN>Family subsections</DOMAIN><DOMAIN>cross-cousins</DOMAIN>"); 
		conversions.put("V.5.6", "<DOMAIN>Kinship</DOMAIN><DOMAIN>Family subsections</DOMAIN><DOMAIN>MoMo subsection</DOMAIN>"); 
		conversions.put("V.5.7", "<DOMAIN>Kinship</DOMAIN><DOMAIN>Family subsections</DOMAIN><DOMAIN>WiFa subsection</DOMAIN>"); 
		conversions.put("V.5.8", "<DOMAIN>Kinship</DOMAIN><DOMAIN>Family subsections</DOMAIN><DOMAIN>WiMo subsection</DOMAIN>"); 
		conversions.put("V.6", "<DOMAIN>Kinship</DOMAIN><DOMAIN>spouse pairs</DOMAIN>"); 
		conversions.put("V.7", "<DOMAIN>Kinship</DOMAIN><DOMAIN>father-child pairs</DOMAIN>"); 
		conversions.put("V.8", "<DOMAIN>Kinship</DOMAIN><DOMAIN>mother-child pairs</DOMAIN>"); 
		conversions.put("V.9", "<DOMAIN>Kinship</DOMAIN><DOMAIN>other pairs</DOMAIN>");
		conversions.put("V.9.1", "<DOMAIN>Kinship</DOMAIN><DOMAIN>other pairs</DOMAIN><DOMAIN>Fa-MoFa pairs</DOMAIN>"); 
		conversions.put("V.9.2", "<DOMAIN>Kinship</DOMAIN><DOMAIN>other pairs</DOMAIN><DOMAIN>Ego-WiMo pair, Fa-MoMo pair</DOMAIN>"); 
		conversions.put("V.9.3", "<DOMAIN>Kinship</DOMAIN><DOMAIN>other pairs</DOMAIN><DOMAIN>Ego-MoFa,MoMo pair</DOMAIN>"); 
		conversions.put("V.9.4", "<DOMAIN>Kinship</DOMAIN><DOMAIN>other pairs</DOMAIN><DOMAIN>semi-patrimoiety terms</DOMAIN>");
		conversions.put("W", "<DOMAIN>Social satisfactions and displeasures</DOMAIN>"); 
		conversions.put("W.1", "<DOMAIN>Social satisfactions and displeasures</DOMAIN><DOMAIN>play</DOMAIN>");
		conversions.put("W.2", "<DOMAIN>Social satisfactions and displeasures</DOMAIN><DOMAIN>fighting, bystander reactions</DOMAIN>"); 
		conversions.put("W.3", "<DOMAIN>Social satisfactions and displeasures</DOMAIN><DOMAIN>socially prescribed action</DOMAIN>"); 
		conversions.put("X", "<DOMAIN>Material culture</DOMAIN>"); 
		conversions.put("X.1", "<DOMAIN>Material culture</DOMAIN><DOMAIN>tools, weapons</DOMAIN>");
		conversions.put("X.1.1", "<DOMAIN>Material culture</DOMAIN><DOMAIN>tools, weapons</DOMAIN><DOMAIN>tools, weapons esp. for men</DOMAIN>"); 
		conversions.put("X.1.2", "<DOMAIN>Material culture</DOMAIN><DOMAIN>tools, weapons</DOMAIN><DOMAIN>tools, weapons esp. for women</DOMAIN>"); 
		conversions.put("X.2", "<DOMAIN>Material culture</DOMAIN><DOMAIN>clothing, adornments</DOMAIN>"); 
		conversions.put("X.3", "<DOMAIN>Material culture</DOMAIN><DOMAIN>shelter, camp</DOMAIN>"); 
		conversions.put("X.4", "<DOMAIN>Material culture</DOMAIN><DOMAIN>marine, riverine</DOMAIN>"); 
		conversions.put("Y", "<DOMAIN>Cooking, Fire</DOMAIN>"); 
		conversions.put("Z", "<DOMAIN>Ritual, Sorcery</DOMAIN>");
		conversions.put("Z.1", "<DOMAIN>Ritual, Sorcery</DOMAIN><DOMAIN>Mourning</DOMAIN>"); 



	}



}

