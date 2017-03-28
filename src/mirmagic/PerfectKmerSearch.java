package mirmagic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import net.sf.samtools.BAMFileWriter;
import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A query is considered to match a target if they share a perfect kmer match of the specified length
 * Case is ignored
 * Ns are treated as wildcards that everything matches
 * Reverse complement matches are NOT included
 * Transcripts shorter than the specified kmer length are allowed to have shorter matches with queries
 * @author prussell
 *
 */
public class PerfectKmerSearch {
	
	/**
	 * A sequence and position on the sequence
	 * E.g. the start position of a match on a target sequence
	 * @author prussell
	 *
	 */
	private class SequencePos implements Comparable<SequencePos> {
		
		private Sequence seq;
		private int pos;
		
		/**
		 * @param seq The sequence
		 * @param pos The position on the sequence
		 */
		public SequencePos(Sequence seq, int pos) {
			this.seq = seq;
			this.pos = pos;
		}
		
		public boolean equals(Object o) {
			if(!o.getClass().equals(SequencePos.class)) return false;
			SequencePos s = (SequencePos)o;
			return seq.equals(s.getSequence()) && pos == s.getPos();
		}
		
		public String toString() {
			return seq.getName() + ":" + pos;
		}
		
		public int hashCode() {
			String s = toString() + ";" + seq.getSequenceBases();
			return s.hashCode();
		}
		
		public Sequence getSequence() {return seq;}
		public int getPos() {return pos;}

		@Override
		public int compareTo(SequencePos o) {
			int n = seq.getName().compareTo(o.getSequence().getName());
			if(n != 0) return n;
			return pos - o.getPos();
		}
		
	}
	
	/**
	 * A match between kmers of query and target sequences
	 * @author prussell
	 *
	 */
	private class IndividualKmerMatch {
		
		private Sequence query; // Query sequence
		private KmerSubsequence queryKmer; // Kmer sequence and start position of kmer on query sequence
		private SequencePos target; // Start position of match on target sequence
		
		/**
		 * @param queryName Query sequence name
		 * @param queryKmer Kmer from query sequence that matches the target, which stores the start position on the query
		 * @param target Target sequence and start position
		 */
		public IndividualKmerMatch(Sequence query, KmerSubsequence queryKmer, SequencePos target) {
			this.query = query;
			this.queryKmer = queryKmer;
			this.target = target;
		}
		
		public int getK() {return queryKmer.getSeq().length();}
		
		/**
		 * @return Query/target pair object for this query and target
		 */
		public QueryTargetPair getQueryTargetPair() {
			return new QueryTargetPair(query, target.getSequence());
		}
		
		public String toString() {
			return query.getName() + ":" + queryKmer.getOrigSeqPos() + "->" + target.toString();
		}
		
		public boolean equals(Object o) {
			if(o.getClass().equals(IndividualKmerMatch.class)) return false;
			IndividualKmerMatch m = (IndividualKmerMatch)o;
			return m.toString().equals(toString());
		}
		
		public int hashCode() {return toString().hashCode();}
		
		public Sequence getQuery() {return query;}
		public Sequence getTarget() {return target.getSequence();}
		public int getQueryStartPos() {return queryKmer.getOrigSeqPos();}		
		public String getQueryName() {return query.getName();}
		public int getTargetStartPos() {return target.getPos();}
		public String getTargetName() {return target.getSequence().getName();}
		
	}
	
	/**
	 * A query and target pair
	 * @author prussell
	 *
	 */
	private class QueryTargetPair {
		
		private Sequence query;
		private Sequence target;
		
		public QueryTargetPair(Sequence query, Sequence target) {
			this.query = query;
			this.target = target;
		}
		
		public Sequence getQuery() {return query;}
		public Sequence getTarget() {return target;}
		
		public String toString() {
			return query.getName() + "->" + target.getName();
		}
		
		public boolean equals(Object o) {
			if(!o.getClass().equals(getClass())) return false;
			QueryTargetPair q = (QueryTargetPair)o;
			return query.equals(q.getQuery()) && target.equals(q.getTarget());
		}
		
		public int hashCode() {
			HashCodeBuilder h = new HashCodeBuilder();
			h.append(query);
			h.append(target);
			return h.toHashCode();
		}
		
	}
	
	/**
	 * A match between a query and target sequence
	 * No gaps are accounted for
	 * @author prussell
	 *
	 */
	private class QueryTargetMatch {
		
		private SequencePos queryMatchStart;
		private SequencePos targetMatchStart;
		private int matchLength;
		
		/**
		 * @param queryMatchStart Query sequence and start position of match
		 * @param targetMatchStart Target sequence and start position of match
		 * @param matchLength Match length
		 */
		public QueryTargetMatch(SequencePos queryMatchStart, SequencePos targetMatchStart, int matchLength) {
			this.queryMatchStart = queryMatchStart;
			this.targetMatchStart = targetMatchStart;
			this.matchLength = matchLength;
		}
		
		/**
		 * Get this match as a SAM record
		 * @return SAM record
		 */
		public SAMRecord toSAMRecord() {
			// Populate cigar
			Cigar cigar = new Cigar();
			int queryStart = queryMatchStart.getPos();
			if(queryStart > 0) {
				// Soft clip beginning of read
				CigarElement s = new CigarElement(queryStart, CigarOperator.S);
				cigar.add(s);
			}
			CigarElement m = new CigarElement(matchLength, CigarOperator.M);
			cigar.add(m);
			int queryLength = queryMatchStart.getSequence().getLength();
			int endSoftClip = queryLength - matchLength - queryStart;
			if(endSoftClip > 0) {
				// Soft clip end of read
				CigarElement s = new CigarElement(endSoftClip, CigarOperator.S);
				cigar.add(s);
			}

			// Set record fields
			SAMRecord rtrn = new SAMRecord(samHeader);
			rtrn.setCigar(cigar);
			rtrn.setAlignmentStart(targetMatchStart.getPos() + 1);
			rtrn.setReadName(queryMatchStart.getSequence().getName().split("\\s+")[0]); // Only take first field before whitespace
			rtrn.setReadPairedFlag(false);
			rtrn.setReferenceName(targetMatchStart.getSequence().getName());
			rtrn.setMappingQuality(255); // mapping quality unknown
			rtrn.setReadBases(queryMatchStart.getSequence().getSequenceBases().getBytes());;
			
			return rtrn;
		}
		
	}
	
	/**
	 * Organize kmer matches by query and target, then get the "first" match for each query/target pair
	 * @param kmerMatches Collection of kmer matches with different queries and targets allowed
	 * @return Map of query/target pair to "first" kmer match from the set wrt query and target coordinates
	 */
	private Map<QueryTargetPair, QueryTargetMatch> firstKmerMatchEachQueryTargetPair(Collection<IndividualKmerMatch> kmerMatches) {
		
		Map<QueryTargetPair, Collection<IndividualKmerMatch>> matchesByPair = new HashMap<QueryTargetPair, Collection<IndividualKmerMatch>>();
		for(IndividualKmerMatch match : kmerMatches) {
			QueryTargetPair qtp = match.getQueryTargetPair();
			if(!matchesByPair.containsKey(qtp)) {
				matchesByPair.put(qtp, new ArrayList<IndividualKmerMatch>());
			}
			matchesByPair.get(qtp).add(match);
		}
		
		Map<QueryTargetPair, QueryTargetMatch> rtrn = new HashMap<QueryTargetPair, QueryTargetMatch>();
		for(QueryTargetPair qtp : matchesByPair.keySet()) {
			rtrn.put(qtp, firstKmerMatch(matchesByPair.get(qtp)));
		}
		
		return rtrn;
		
	}
	
	/**
	 * Get the first kmer match of this query to each of its targets
	 * @param query Query sequence
	 * @return The first match to each target
	 */
	private Collection<QueryTargetMatch> firstKmerMatchEachTarget(Sequence query) {
		Map<QueryTargetPair, QueryTargetMatch> matches = firstKmerMatchEachQueryTargetPair(getIndividualKmerMatches(query));
		return matches.values();
	}
	
	/**
	 * Get the first kmer match of this query to each of its targets as SAM records
	 * @param record Query read
	 * @return The first match to each target as SAM records
	 */	
	private Collection<SAMRecord> samRecordFirstKmerMatchEachTarget(FastqSequence record) {
		String queryName = record.getName();
		String querySeq = record.getSequence();
		return samRecordFirstKmerMatchEachTarget(new Sequence(queryName, querySeq));
	}
	
	/**
	 * Get the first kmer match of this query to each of its targets as SAM records
	 * @param query Query sequence
	 * @return The first match to each target as SAM records
	 */
	private Collection<SAMRecord> samRecordFirstKmerMatchEachTarget(Sequence query) {
		Collection<QueryTargetMatch> matches = firstKmerMatchEachTarget(query);
		Collection<SAMRecord> rtrn = new ArrayList<SAMRecord>();
		for(QueryTargetMatch match : matches) {
			rtrn.add(match.toSAMRecord());
		}
		return rtrn;
	}
	
	/**
	 * Iterate through fastq file and for each query and target, write first kmer match to a bam file
	 * @param queryFastq Query fastq file
	 * @param outputBam Bam file to write
	 * @throws IOException
	 */
	private void writeFirstKmerMatchEachTarget(String queryFastq, String outputBam) throws IOException {
		
		System.out.println("");
		System.out.println("Writing matches for reads in " + queryFastq + " to " + outputBam + "...");
		
		BAMFileWriter writer = new BAMFileWriter(new File(outputBam));
		writer.setSortOrder(SAMFileHeader.SortOrder.unsorted, false);
		writer.setHeader(samHeader);
		FastqParser reader = new FastqParser();
		reader.start(new File(queryFastq));
		int numTooShort = 0;
		int numIllegalChar = 0;
		int numUniquelyMapped = 0;
		int numMultiMapped = 0;
		int numUnmapped = 0;
		int numDone = 0;
		int numTooManyNs = 0;
		while(reader.hasNext()) {
			FastqSequence query = reader.next();
			numDone++;
			if(numDone % 1000000 == 0) {
				System.out.println("Finished " + numDone + " reads");
			}
			try {
				Collection<SAMRecord> alignments = samRecordFirstKmerMatchEachTarget(query);
				if(alignments.size() == 0) numUnmapped++;
				if(alignments.size() == 1) numUniquelyMapped++;
				if(alignments.size() > 1) numMultiMapped++;
				for(SAMRecord alignment : alignments) {
					writer.addAlignment(alignment);
				}
			} catch(SequenceTooShortException e) {
				numTooShort++;
			} catch(IllegalCharacterException e) {
				numIllegalChar++;
			} catch(TooManyNsException e) {
				numTooManyNs++;
			}
		}
		System.out.println("");
		System.out.println("RESULTS");
		System.out.println("Reads mapped uniquely:\t" + numUniquelyMapped);
		System.out.println("Reads mapped to multiple targets:\t" + numMultiMapped);
		System.out.println("Reads unmapped:\t" + numUnmapped);
		if(numTooShort > 0) {
			System.out.println("Reads skipped because they were too short:\t" + numTooShort);
		}
		if(numIllegalChar > 0) {
			System.out.println("Reads skipped because they contain an illegal character:\t" + numIllegalChar);
		}
		if(numTooManyNs > 0) {
			System.out.println("Reads skipped because they contain > " + MAX_PCT_N + " N's:\t" + numTooManyNs);
		}
		System.out.println("");
		reader.close();
		writer.close();
		
	}
	
	/**
	 * Get the "first" kmer match between a query and a target, out of a set of multiple kmer matches between these sequences
	 * @param kmerMatches Set of kmer matches all with same query and target
	 * @return The "first" match, i.e. smallest position on query and target represented in the set of kmer matches
	 */
	private QueryTargetMatch firstKmerMatch(Collection<IndividualKmerMatch> kmerMatches) {
		Iterator<IndividualKmerMatch> iter = kmerMatches.iterator();
		if(!iter.hasNext()) {
			throw new IllegalArgumentException("Iterator empty");
		}
		IndividualKmerMatch match = iter.next();
		Sequence query = match.getQuery();
		Sequence target = match.getTarget();
		String queryName = match.getQueryName();
		String targetName = match.getTargetName();
		int queryStart = match.getQueryStartPos();
		int targetStart = match.getTargetStartPos();
		int matchLen = match.getK();
		while(iter.hasNext()) {
			IndividualKmerMatch next = iter.next();
			String nextQueryName = next.getQueryName();
			if(!nextQueryName.equals(queryName)) {
				throw new IllegalArgumentException("Must have only one query: " + queryName + ", " + nextQueryName);
			}
			String nextTargetName = next.getTargetName();
			if(!nextTargetName.equals(targetName)) {
				throw new IllegalArgumentException("Must have only one target: " + targetName + ", " + nextTargetName);
			}
			int nextQueryStart = next.getQueryStartPos();
			if(nextQueryStart < queryStart) {
				matchLen = next.getK();
				queryStart = nextQueryStart;
			}
			int nextTargetStart = next.getTargetStartPos();
			if(nextTargetStart < targetStart) {
				targetStart = nextTargetStart;
				matchLen = next.getK();
			}
		}
		SequencePos queryMatchPos = new SequencePos(query, queryStart);
		SequencePos targetMatchPos = new SequencePos(target, targetStart);
		return new QueryTargetMatch(queryMatchPos, targetMatchPos, matchLen);
	}
	
	/**
	 * A kmer sequence and the start position of the original sequence it came from
	 * @author prussell
	 *
	 */
	private class KmerSubsequence {
		
		private String seq; // The kmer sequence
		private int origSeqPos; // Start position of the kmer on the original sequence
		
		/**
		 * @param seq Kmer sequence
		 * @param origSeqPos Start position on original sequence
		 */
		public KmerSubsequence(String seq, int origSeqPos) {
			if(seq == null) throw new IllegalArgumentException("Sequence is null");
			this.seq = seq;
			this.origSeqPos = origSeqPos;
		}
		
		public boolean equals(Object o) {
			if(!o.getClass().equals(KmerSubsequence.class)) return false;
			KmerSubsequence k = (KmerSubsequence)o;
			return k.toString().equals(toString());
		}
		
		public int hashCode() {
			return toString().hashCode();
		}
		
		public String toString() {
			return origSeqPos + ":" + seq;
		}
		
		public String getSeq() {return seq;}
		public int getOrigSeqPos() {return origSeqPos;}
		
	}
	
	private int mink; // Minimum kmer length (set to shortest target length when making kmer index for targets, or to maxk, whichever is smaller)
	private int maxk; // Maximum kmer length to search for
	private Map<String, Collection<SequencePos>> targetKmers; // Key is kmer; value is collection of sequences with kmer and the match position
	//private static Logger logger = Logger.getLogger(PerfectKmerSearch.class.getName());
	private SAMFileHeader samHeader; // SAM header for target sequences
	private static double MAX_PCT_N = 0.05; // Max percentage of N's in reads
	
	/**
	 * The legal characters converted to upper case, not including N
	 */
	public static final char[] alphabet = {'A', 'C', 'G', 'T'};
	
	/**
	 * @param k Length of kmers to match. Shorter matches are allowed for shorter target transcripts.
	 * @param fasta Fasta file of target sequences
	 */
	public PerfectKmerSearch(int k, String fasta) {
		this.maxk = k;
		setMinK(fasta);
		createIndex(fasta);
		samHeader = SamtoolsUtils.createSamHeader(fasta);
	}

	
	/**
	 * Check if the char (converted to upper case) is in the alphabet or is N
	 * @param c Char to check
	 * @return True iff upper case version of char is in alphabet or is N
	 */
	private static boolean charIsLegal(char c) {
		char cu = Character.toUpperCase(c);
		if(cu == 'N') return true;
		for(char a : alphabet) {
			if(cu == a) return true;
		}
		return false;
	}
	
	@SuppressWarnings("serial")
	private class SequenceTooShortException extends RuntimeException {
		public SequenceTooShortException(String message) {
			super(message);
		}
	}
	
	@SuppressWarnings("serial")
	private class TooManyNsException extends RuntimeException {
		public TooManyNsException(String message) {
			super(message);
		}
	}
	
	@SuppressWarnings("serial")
	private class IllegalCharacterException extends RuntimeException {
		public IllegalCharacterException(String message) {
			super(message);
		}
	}
	
	private static final int MAX_LEN_TO_CHECK_N_CONTENT = 5000;
	
	/**
	 * Check that a sequence is valid
	 * @param seq Sequence
	 */
	private void validateSequence(Sequence seq) {
		
		String bases = seq.getSequenceBases();
		int len = bases.length();
		
		if(len < mink) {
			throw new SequenceTooShortException("Query shorter than " + mink + ":\t" + seq.getName() + "\t" + bases);
		}
		
		boolean checkN = len <= MAX_LEN_TO_CHECK_N_CONTENT;
		
		int numNs = 0;
		for(int i = 0; i < len; i++) {
			char c = bases.charAt(i);
			// Check that the sequence includes no illegal characters
			if(!charIsLegal(c)) {
				throw new IllegalCharacterException("Illegal char in sequence " + seq.getName() + ": " + c);
			}
			// Make sure there aren't too many N's
			if(checkN) {
				if(Character.toUpperCase(c) == 'N') {numNs++;}
			}
		}
		if((double) numNs / (double) len > MAX_PCT_N) {
			throw new TooManyNsException("Sequence has >" + MAX_PCT_N + " Ns:\t" + seq.getName() + "\t" + bases);
		}
		
	}
	
	/**
	 * Set minimum kmer length field as the length of the shortest sequence in a fasta file
	 * @param fasta Fasta file
	 */
	private void setMinK(String fasta) {
		Collection<Sequence> targets = FastaReader.readFromFile(fasta);
		mink = maxk;
		for(Sequence target : targets) {
			int len = target.getLength();
			if(len < mink) {mink = len;}
		}
	}
	

	/**
	 * Store kmers and their matches to target sequences
	 * @param fasta Fasta file of target sequences
	 */
	private void createIndex(String fasta) {
		System.out.println("");
		System.out.println("Creating index for target fasta " + fasta + "...");
		targetKmers = new HashMap<String, Collection<SequencePos>>();
		Collection<Sequence> targets = FastaReader.readFromFile(fasta);
		int numSkipped = 0;
		for(Sequence target : targets) {
			try {
				validateSequence(target);
			} catch(SequenceTooShortException e) {
				numSkipped++;
				continue;
			}
			int len = target.getLength();
			if(len < mink) {
				throw new IllegalStateException("Target sequence length (" + len + ") is shorter than min kmer length (" + mink + "): " + target.getName());
			}
			/**
			 *  Only index a single length of kmer
			 *  The kmer length is maxk or the transcript length, whichever is shorter
			 */
			int k = Math.min(len, maxk);
			for(KmerSubsequence kmer : getKmers(target.getSequenceBases(), k, k)) {
				String kmerSeq = kmer.getSeq();
				if(!targetKmers.containsKey(kmerSeq)) {
					targetKmers.put(kmerSeq, new TreeSet<SequencePos>());
				}
				targetKmers.get(kmerSeq).add(new SequencePos(target, kmer.getOrigSeqPos()));
			}
		}
		if(numSkipped > 0) {
			System.out.println("");
			System.out.println("Skipped " + numSkipped + " target sequences that did not validate");
			System.out.println("");
		}
		System.out.println("Done creating index. Minimum k is " + mink + ". Maximum k is " + maxk + ".");
	}
	
	/**
	 * Create multiple versions of the sequence for every possible value of N's
	 * @param sequence Sequence to expand
	 * @return Collection of versions with all possible values of N's taken from the alphabet
	 */
	private Collection<String> expandNs(String sequence) {
		for(int i = 0; i < sequence.length(); i++) {
			if(Character.toUpperCase(sequence.charAt(i)) == 'N') {
				Collection<String> expanded = new HashSet<String>();
				StringBuilder sb = new StringBuilder(sequence);
				for(char a : alphabet) {
					sb.replace(i, i+1, Character.toString(a));
					expanded.add(sb.toString());
				}
				return expandNs(expanded);
			}
		}
		Collection<String> rtrn = new HashSet<String>();
		rtrn.add(sequence);
		return rtrn;
	}
		
	/**
	 * Create multiple versions of each sequence for every possible value of N's
	 * @param sequences Sequences to expand
	 * @return Collection of versions with all possible values of N's taken from the alphabet
	 */
	private Collection<String> expandNs(Collection<String> sequences) {
		Collection<String> rtrn = new HashSet<String>();
		for(String seq : sequences) {
			rtrn.addAll(expandNs(seq));
		}
		return rtrn;
	}
	
	/**
	 * Get all kmer substrings of the sequence, converted to upper case
	 * N's are expanded so the method returns multiple versions of each kmer where there is an N
	 * Clients should call validateSequence() before calling this method to get meaningful error messages
	 * If sequence is shorter than the maximum kmer length, only get kmers up to the sequence length
	 * @param sequence Target sequence to break into kmers
	 * @param minK Minimum kmer length to get
	 * @param maxK Maximum kmer length to get
	 * @return Set of kmers converted to upper case with Ns expanded to all possible values
	 */
	private Collection<KmerSubsequence> getKmers(String sequence, int minK, int maxK) {
		int len = sequence.length();
		Collection<KmerSubsequence> rtrn = new ArrayList<KmerSubsequence>();
		for(int i = minK; i <= Math.min(len,maxK); i++) {
			StringBuilder builder = new StringBuilder(sequence.substring(0, i));
			Collection<String> expandedNs = expandNs(builder.toString().toUpperCase());
			for(String s : expandedNs) {
				rtrn.add(new KmerSubsequence(s, 0));
			}
			for(int p = i; p < len; p++) {
				builder.deleteCharAt(0);
				builder.append(sequence.charAt(p));
				Collection<String> expandedNs2 = expandNs(expandNs(builder.toString().toUpperCase()));
				for(String s : expandedNs2) {
					rtrn.add(new KmerSubsequence(s, p - i + 1));
				}
			}
		}
		return rtrn;
	}
	
	/**
	 * Get all kmer matches of this query to the stored targets, based on kmer matches
	 * There can be multiple matches to a given target
	 * @param query Query sequence
	 * @return Set of perfect kmer matches
	 */
	private Collection<IndividualKmerMatch> getIndividualKmerMatches(Sequence query) {
		validateSequence(query);
		Collection<KmerSubsequence> queryKmers = getKmers(query.getSequenceBases(), mink, maxk);
		Collection<IndividualKmerMatch> rtrn = new HashSet<IndividualKmerMatch>();
		for(KmerSubsequence queryKmer : queryKmers) {
			String kmerSeq = queryKmer.getSeq();
			if(!targetKmers.containsKey(kmerSeq)) {continue;}
			for(SequencePos sp : targetKmers.get(kmerSeq)) {
				IndividualKmerMatch match = new IndividualKmerMatch(query, queryKmer, sp);
				rtrn.add(match);
			}
		}
		return rtrn;
	}
	
	/**
	 * Write kmer index out to a file
	 * @param outFile File to write
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private void writeKmerIndex(String outFile) throws IOException {
		FileWriter w = new FileWriter(outFile);
		for(String kmer : targetKmers.keySet()) {
			Iterator<SequencePos> iter = targetKmers.get(kmer).iterator();
			String targets = iter.next().toString();
			while(iter.hasNext()) {
				targets += "," + iter.next().toString();
			}
			w.write(kmer + "\t" + targets + "\n");
		}
		w.close();
	}
	
	public static void main(String[] args) throws IOException {
		
		CommandLineParser p = new CommandLineParser();
		p.addStringArg("-fa", "Reference fasta", true);
		p.addStringArg("-fq", "Query fastq", true);
		p.addStringArg("-b", "Output bam", true);
		p.addIntArg("-k", "Kmer length", true);
		p.addDoubleArg("-mn", "Max proportion of N's in query sequence", false, MAX_PCT_N);
		p.parse(args);
		String fasta = p.getStringArg("-fa");
		String fastq = p.getStringArg("-fq");
		String bam = p.getStringArg("-b");
		int k = p.getIntArg("-k");
		MAX_PCT_N = p.getDoubleArg("-mn");
		if(MAX_PCT_N < 0 || MAX_PCT_N > 1) {
			throw new IllegalArgumentException("Invalid value for max proportion of N's: " + MAX_PCT_N);
		}
		
		PerfectKmerSearch pks = new PerfectKmerSearch(k, fasta);
		pks.writeFirstKmerMatchEachTarget(fastq, bam);
		
		System.out.println("");
		System.out.println("Done with kmer search.");
		
	}
	
	
	

}
