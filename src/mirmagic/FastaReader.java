package mirmagic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Fasta file reader
 * @author prussell
 *
 */
public class FastaReader {
	

	public static Collection<Sequence> readFromFile(String fileName) {
		System.out.println("Reading sequences from fasta file " + fileName + "...");
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
		System.out.println("Got " + rtrn.size() + " sequences.");
		return rtrn;
	}
	
}
