package mirmagic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

/**
 * Fasta file reader
 * @author prussell
 *
 */
public class FastaReader {
	
	private static Logger logger = Logger.getLogger(FastaReader.class.getName());

	public static Collection<Sequence> readFromFile(String fileName) {
		logger.info("Reading sequences from fasta file " + fileName + "...");
		Collection<Sequence> rtrn = new ArrayList<Sequence>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			boolean started = false;
			String currSeqID = null;
			StringBuilder currSeq = null;
			while(reader.ready()) {
				String line = reader.readLine();
				if(line.startsWith(">")) {
					if(started) {
						rtrn.add(new Sequence(currSeqID, currSeq.toString()));
						logger.info("Added " + currSeqID + " " + currSeq.length());
					}
					currSeqID = line.substring(1);
					currSeq = new StringBuilder();
					continue;
				}
				currSeq.append(line);
				started = true;
			}
			Sequence lastSeq = new Sequence(currSeqID, currSeq.toString());
			rtrn.add(lastSeq);
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		logger.info("Got " + rtrn.size() + " sequences.");
		return rtrn;
	}
	
}
