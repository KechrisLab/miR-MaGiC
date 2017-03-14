package mirmagic;

/**
 * A nucleotide sequence
 * @author prussell
 *
 */
public class Sequence {
	
	private String sequence;
	private String name;
	
	/**
	 * @param name Sequence name
	 * @param seq Nucleotide sequence
	 */
	public Sequence(String name, String seq){
		this.name=name;
		this.sequence=seq;
	}
	
	/**
	 * @param seq Nucleotide sequence
	 */
	public Sequence(String seq){
		this.sequence=seq;
	}
	
	/**
	 * @return Return the sequence bases
	 */
	public String getSequenceBases() {
		return this.sequence;
	}
	
	/**
	 * @return Sequence name
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * @return Sequence length
	 */
	public int getLength() {
		return sequence.length();
	}
	
	/**
	 * Get subsequence
	 * @param name Name of new sequence to return
	 * @param start Start position of subsequence
	 * @param end Position after last position to include
	 * @return The subsequence
	 */
	public Sequence getSubSequence(String name, int start, int end) {
		String subSeq = sequence.substring(Math.max(start, 0), Math.min(end, sequence.length()));
		Sequence seq = new Sequence(name, subSeq);
		return seq;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!o.getClass().equals(Sequence.class)) {
			return false;
		}
		Sequence otherSeq = (Sequence)o;
		if(!getName().equals(otherSeq.getName()))	{
			return false;
		}
		if(!getSequenceBases().equals(otherSeq.getSequenceBases()))	{
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		String s = getName() + ":" + getSequenceBases();
		return s.hashCode();
	}
	
}
