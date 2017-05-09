package variant;

import org.apache.log4j.Logger;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;

public enum Haplotype {
	REF,
	ALT,
	NEITHER;

	public static Logger logger = Logger.getLogger(Haplotype.class.getName());
	
	private static Haplotype getHaplotype(Genotype genotype) {
		if(genotype.isHomRef()) {
			return REF;
		}
		if(genotype.isHomVar()) {
			return ALT;
		}
		return NEITHER;
	}

	/**
	 * Get haplotype for the whole interval represented in the iterator
	 * @param iter Iterator over VCF records
	 * @param sampleName Sample name to get
	 * @return REF or ALT if homozygous for every variant in the interval, NEITHER otherwise, or null if iterator is empty
	 */
	public static Haplotype getHaplotype(CloseableIterator<VariantContext> iter, String sampleName) {
		if(!iter.hasNext()) {
			logger.debug("GETTING_HAPLOTYPE\t" + sampleName + "\titerator empty");
			return null;
		}
		VariantContext vc = iter.next();
		Genotype genotype = vc.getGenotype(sampleName);
		logger.debug("GOT_GENOTYPE_FOR_HAPLOTYPE\t" + sampleName + "\t" + genotype.toString());
		Haplotype startHaplotype = getHaplotype(genotype);
		if(startHaplotype.equals(NEITHER)) {
			logger.debug("GETTING_HAPLOTYPE\t" + sampleName + "\tfirst haplotype is neither");
			return NEITHER;
		}
		while(iter.hasNext()) {
			Haplotype haplotype = getHaplotype(iter.next().getGenotype(sampleName));
			if(haplotype.equals(startHaplotype)) {
				continue;
			}
			logger.debug("GETTING_HAPLOTYPE\t" + sampleName + "\t" + haplotype.toString() + " not equal to " + startHaplotype.toString());
			return NEITHER;
		}
		logger.debug("GETTING_HAPLOTYPE\t" + sampleName + "\treturning " + startHaplotype.toString());
		return startHaplotype;
	}
	
	public String toString() {
		switch(this) {
		case ALT:
			return "ALT";
		case NEITHER:
			return "NEITHER";
		case REF:
			return "REF";
		default:
			throw new UnsupportedOperationException("Not implemented");
		}
	}
	
	/**
	 * Get haplotype for whole interval represented in both iterators
	 * Tries to get haplotype for each iterator using getHaplotype(CloseableIterator<VariantContext> iter, String sampleName) for each
	 * If they are equal, returns the common value
	 * If one of them is null, returns the other one
	 * If they are both non-null but not equal, returns NEITHER
	 * @param iter1 One iterator over VCF records
	 * @param iter2 Another iterator over VCF records
	 * @param sampleName Sample name to get
	 * @return REF or ALT if homozygous for every variant in both intervals, NEITHER otherwise, or null if both iterators are empty
	 */
	public static Haplotype getCommonHaplotype(CloseableIterator<VariantContext> iter1, CloseableIterator<VariantContext> iter2, String sampleName) {
		Haplotype hap1 = getHaplotype(iter1, sampleName);
		Haplotype hap2 = getHaplotype(iter2, sampleName);
		if(hap1 == hap2) {
			logger.debug("GETTING_COMMON_HAPLOTYPE\t" + hap1.toString() + "=" + hap2.toString());
			return hap1;
		}
		if(hap1 == null) {
			logger.debug("GETTING_COMMON_HAPLOTYPE\tnull," + hap2.toString() + "\treturning " + hap2.toString());
			return hap2;
		}
		if(hap2 == null) {
			logger.debug("GETTING_COMMON_HAPLOTYPE\tnull," + hap1.toString() + "\treturning " + hap1.toString());
			return hap1;
		}
		return NEITHER;
	}
	
	
}