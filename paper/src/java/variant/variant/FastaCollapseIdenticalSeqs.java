package variant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import mirmagic.CommandLineParser;
import mirmagic.FastaReader;
import mirmagic.Sequence;

import org.apache.log4j.Logger;

public class FastaCollapseIdenticalSeqs {
	
	private static Logger logger = Logger.getLogger(FastaCollapseIdenticalSeqs.class.getName());
	
	/**
	 * Collapse identical sequences
	 * @param inputSeqs Input sequences
	 * @param upper Convert all sequences to upper case before collapsing
	 * @param separator Separator for combined names of collapsed sequences
	 * @return Collapsed sequences with combined names
	 */
	private static Collection<Sequence> collapseIdentical(Collection<Sequence> inputSeqs, boolean upper, String separator) {
		Map<String, Collection<String>> seqToNames = new HashMap<String, Collection<String>>();
		Collection<Sequence> rtrn = new ArrayList<Sequence>();
		for(Sequence seq : inputSeqs) {
			String id = seq.getName();
			String bases = upper ? seq.getSequenceBases().toUpperCase() : seq.getSequenceBases();
			if(!seqToNames.containsKey(bases)) {
				seqToNames.put(bases, new HashSet<String>());
			}
			seqToNames.get(bases).add(id);
		}
		for(String seq : seqToNames.keySet()) {
			Iterator<String> namesIter = seqToNames.get(seq).iterator();
			namesIter.hasNext();
			String name = namesIter.next();
			while(namesIter.hasNext()) {
				name += separator + namesIter.next();
			}
			Sequence collapsed = new Sequence(name, seq);
			rtrn.add(collapsed);
		}
		return rtrn;
	}

	public static void main(String[] args) {
		
		CommandLineParser p = new CommandLineParser();
		p.addStringArg("-i", "Input fasta file", true);
		p.addStringArg("-o", "Output fasta file", true);
		p.addBooleanArg("-u", "Convert all sequences to upper case before collapsing", false, true);
		p.addStringArg("-s", "Separator for combined names", false, ";");
		p.parse(args);
		String input = p.getStringArg("-i");
		String output = p.getStringArg("-o");
		boolean upper = p.getBooleanArg("-u");
		String separator = p.getStringArg("-s");
		
		Collection<Sequence> inputSeqs = FastaReader.readFromFile(input);
		Collection<Sequence> outputSeqs = collapseIdentical(inputSeqs, upper, separator);
		FastaWriter.writeToFile(outputSeqs, output, 100);
		
		logger.info("");
		logger.info("All done.");

	}

}
