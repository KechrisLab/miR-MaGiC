package mirmagic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

/**
 * Combine counts in a bam file by reference sequence they are mapped to: reference
 * sequences are grouped according to a provided grouping; reads mapping to multiple members
 * of a group are counted once for the group
 * @author prussell
 *
 */
public class CombineCountsByGroup {
	
	private SAMFileReader reader;
	private Map<String, Integer> refLen;
	private Map<String, Set<String>> groupByRep;
	private boolean plusStrandMappingsOnly;
	
	/**
	 * 
	 * @param bam Bam file
	 * @param refFasta Reference fasta file
	 * @param groupTable Table of groups. Line format: ref_name group_name
	 * @param plusStrandMappingsOnly Only count mappings to plus strand
	 */
	private CombineCountsByGroup(String bam, String refFasta, String groupTable, boolean plusStrandMappingsOnly) {
		
		System.out.println("\nCombining counts by group...");
		System.out.println("Bam file:\t" + bam);
		System.out.println("Reference fasta file\t" + refFasta);
		System.out.println("Group table:\t" + groupTable);
		System.out.println("Plus strand mappings only:\t" + plusStrandMappingsOnly);
		
		// Initialize the SAM reader
		reader = new SAMFileReader(new File(bam));
		// Get reference sequence lengths
		Collection<Sequence> refSeqs = FastaReader.readFromFile(refFasta);
		refLen = new HashMap<String, Integer>();
		for(Sequence seq : refSeqs) {
			refLen.put(seq.getName(), Integer.valueOf(seq.getLength()));
		}
		// Establish the groups
		try {
			BufferedReader r = new BufferedReader(new FileReader(new File(groupTable)));
			StringParser p = new StringParser();
			groupByRep = new HashMap<String, Set<String>>();
			while(r.ready()) {
				String line = r.readLine();
				p.parse(line);
				if(p.getFieldCount() != 2) {
					r.close();
					throw new IllegalArgumentException("Group table format: <ref name>   <group name>");
				}
				String refName = p.asString(0);
				String groupName = p.asString(1);
				if(!groupByRep.containsKey(groupName)) {
					groupByRep.put(groupName, new HashSet<String>());
				}
				groupByRep.get(groupName).add(refName);
			}
			r.close();
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		this.plusStrandMappingsOnly = plusStrandMappingsOnly;
	}
	
	private Set<String> getMappedRecordNames(String refName) {
		if(!refLen.containsKey(refName)) {
			throw new IllegalArgumentException("Sequence " + refName + " is not in sequence fasta file. Sets of sequences must match.");
		}
		SAMRecordIterator iter = reader.query(refName, 0, refLen.get(refName).intValue(), false);
		Set<String> rtrn = new HashSet<String>();
		while(iter.hasNext()) {
			SAMRecord rec = iter.next();
			if(plusStrandMappingsOnly && rec.getReadNegativeStrandFlag()) continue;
			rtrn.add(rec.getReadName());
		}
		iter.close();
		return rtrn;
	}
	
	private int combinedCount(Set<String> refNames) {
		Set<String> readNames = new HashSet<String>();
		for(String ref : refNames) {
			readNames.addAll(getMappedRecordNames(ref));
		}
		return readNames.size();
	}
	
	private int combinedCount(String groupName) {
		return combinedCount(groupByRep.get(groupName));
	}
	
	private void writeCombinedCounts(String outFile) {
		try {
			FileWriter w = new FileWriter(outFile);
			for(String group: groupByRep.keySet()) {
				w.write(group + "\t" + combinedCount(group) + "\n");
			}
			w.close();
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) {
		
		CommandLineParser p = new CommandLineParser();
		p.addStringArg("-b", "Bam file", true);
		p.addStringArg("-f", "Reference fasta file", true);
		p.addStringArg("-g", "Group table", true);
		p.addStringArg("-o", "Output counts table", true);
		p.addBooleanArg("-p", "Count plus strand mappings only", true);
		p.parse(args);
		CombineCountsByGroup c = new CombineCountsByGroup(p.getStringArg("-b"), p.getStringArg("-f"), p.getStringArg("-g"), p.getBooleanArg("-p"));
		c.writeCombinedCounts(p.getStringArg("-o"));
		
		System.out.println("");
		System.out.println("Done combining counts by group.");

	}

}
