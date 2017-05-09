package variant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import mirmagic.CommandLineParser;
import mirmagic.StringParser;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import variant.VCFUtils;
import variant.Haplotype;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.SortingVariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFFileReader;

public class RecombinantInbredHaplotypeWriter {
	
	private String parent1name;
	private String parent2name;
	protected VCFFileReader riVcfReader;
	private VCFFileReader parent1vcfReader;
	private VCFFileReader parent2vcfReader;
	protected Collection<String> sampleNames;
	private htsjdk.samtools.SAMSequenceDictionary dict;
	private htsjdk.samtools.fork.SAMSequenceDictionary dictFork;
	private Map<String, CentiMorganPosition> riMarkerCmPositions;
	private static Logger logger = Logger.getLogger(RecombinantInbredHaplotypeWriter.class.getName());
	private static float MAX_CM_DISTANCE = 1;
	private static int MAX_BP_DISTANCE = 1000000;
	
	private class CentiMorganPosition {
		
		private String chr;
		private float cm;
		
		public CentiMorganPosition(String chrName, float cmPos) {
			chr = chrName;
			cm = cmPos;
		}
		
		public String getChr() {
			return chr;
		}
		
		public float getCm() {
			return cm;
		}
				
	}
	
	/**
	 * @param riVcf VCF file including parental strains and recombinant inbred strains
	 * @param parent1vcf VCF file for parental strain 1
	 * @param parent2vcf VCF file for parental strain 2
	 * @param parent1 Parental strain 1 name
	 * @param parent2 Parental strain 2 name
	 * @param refSizeFile Table of reference sequence sizes (line format: chr   size)
	 * @param markerCmFile Table of centimorgan positions of markers in the RI VCF file, or null if not using (line format: ID   chr   pos)
	 * @throws IOException
	 */
	protected RecombinantInbredHaplotypeWriter(String riVcf, String parent1vcf, String parent2vcf, String parent1, 
			String parent2, String refSizeFile, String markerCmFile) throws IOException {
		logger.info("");
		logger.info("Instantiating...");
		parent1name = parent1;
		parent2name = parent2;
		riVcfReader = new VCFFileReader(new File(riVcf));
		if(parent1vcf != null) parent1vcfReader = new VCFFileReader(new File(parent1vcf));
		if(parent2vcf != null) parent2vcfReader = new VCFFileReader(new File(parent2vcf));
		sampleNames = riVcfReader.iterator().next().getSampleNames();
		dict = createDict(refSizeFile);
		dictFork = createDictFork(refSizeFile);
		riMarkerCmPositions = new HashMap<String, CentiMorganPosition>();
		if(markerCmFile != null) riMarkerCmPositions.putAll(readCmPositionsFromTable(markerCmFile));
		logger.info("Done instantiating.");
	}
	
	private static float distance(CentiMorganPosition cm1, CentiMorganPosition cm2) {
		if(!cm1.getChr().equals(cm2.getChr())) {
			throw new IllegalArgumentException("Not on same chromosome");
		}
		return Math.abs(cm1.getCm() - cm2.getCm());
	}

	private Map<String, CentiMorganPosition> readCmPositionsFromTable(String file) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(file));
		StringParser s = new StringParser();
		Map<String, CentiMorganPosition> rtrn = new HashMap<String, CentiMorganPosition>();
		while(r.ready()) {
			s.parse(r.readLine());
			if(s.getFieldCount() != 3) {
				r.close();
				throw new IllegalArgumentException("Line format: snpID   chr   cM_pos");
			}
			String snpID = s.asString(0);
			if(rtrn.containsKey(snpID)) {
				logger.warn("Map already contains SNP " + snpID + ". Overwriting.");
			}
			String chr = s.asString(1);
			float cm = s.asFloat(2);
			rtrn.put(snpID, new CentiMorganPosition(chr, cm));
		}
		r.close();
		return rtrn;
	}
	
	/**
	 * Check if two markers are within a centimorgan distance of each other
	 * @param marker1 Marker 1
	 * @param marker2 Marker 2
	 * @param cmPositions Map of SNP ID to centimorgan position on chromosome
	 * @param maxCm Max allowable cM distance
	 * @return True iff the two markers are with maxCm of each other. Empty if either marker is not in the map.
	 */
	private static Optional<Boolean> markersWithinMaxCm(VariantContext marker1, VariantContext marker2, Map<String, CentiMorganPosition> cmPositions, float maxCm) {
		String id1 = marker1.getID();
		String id2 = marker2.getID();
		CentiMorganPosition p1 = cmPositions.get(id1);
		CentiMorganPosition p2 = cmPositions.get(id2);
		if(p1 != null && p2 != null) {
			return Optional.of(Boolean.valueOf(distance(cmPositions.get(id1), cmPositions.get(id2)) <= maxCm));
		}
		return Optional.empty();
	}
	
	public static SAMSequenceDictionary createDict(String refSizeFile) throws IOException {
		logger.info("Creating SAM sequence dictionary...");
		BufferedReader reader = new BufferedReader(new FileReader(refSizeFile));
		StringParser sp = new StringParser();
		SAMSequenceDictionary rtrn = new SAMSequenceDictionary();
		while(reader.ready()) {
			sp.parse(reader.readLine());
			if(sp.getFieldCount() != 2) {
				reader.close();
				throw new IllegalArgumentException("Reference sequence file line format: chr   size");
			}
			SAMSequenceRecord sr = new SAMSequenceRecord(sp.asString(0), sp.asInt(1));
			rtrn.addSequence(sr);
		}
		reader.close();
		logger.info("Created dictionary with " + rtrn.size() + " sequences.");
		return rtrn;
	}
	
	public static htsjdk.samtools.fork.SAMSequenceDictionary createDictFork(String refSizeFile) throws IOException {
		logger.info("Creating SAM sequence dictionary...");
		BufferedReader reader = new BufferedReader(new FileReader(refSizeFile));
		StringParser sp = new StringParser();
		htsjdk.samtools.fork.SAMSequenceDictionary rtrn = new htsjdk.samtools.fork.SAMSequenceDictionary();
		while(reader.ready()) {
			sp.parse(reader.readLine());
			if(sp.getFieldCount() != 2) {
				reader.close();
				throw new IllegalArgumentException("Reference sequence file line format: chr   size");
			}
			htsjdk.samtools.fork.SAMSequenceRecord sr = new htsjdk.samtools.fork.SAMSequenceRecord(sp.asString(0), sp.asInt(1));
			rtrn.addSequence(sr);
		}
		reader.close();
		logger.info("Created dictionary with " + rtrn.size() + " sequences.");
		return rtrn;
	}
	
	protected enum Parent {
		PARENT1,
		PARENT2,
		BOTH,
		NEITHER;
		
		public String toString() {
			switch(this) {
			case BOTH:
				return "BOTH";
			case NEITHER:
				return "NEITHER";
			case PARENT1:
				return "PARENT1";
			case PARENT2:
				return "PARENT2";
			default:
				throw new UnsupportedOperationException("Not implemented");
			}
		}
		
	}
	
	protected String parentName(Parent parent) {
		switch(parent) {
		case PARENT1:
			return parent1name;
		case PARENT2:
			return parent2name;
		case BOTH:
			return "both";
		case NEITHER:
			throw new IllegalArgumentException("Specify parent 1 or 2");
		default:
			throw new IllegalArgumentException("Specify parent 1 or 2");
		}
	}
	
	private CloseableIterator<VariantContext> queryParent(Parent parent, String chr, int start, int end) {
		logger.debug("QUERYING_PARENT\t" + getParentName(parent) + "\t" + chr + ":" + start + "-" + end);
		switch(parent) {
		case PARENT1:
			return parent1vcfReader.query(chr, start, end);
		case PARENT2:
			return parent2vcfReader.query(chr, start, end);
		case BOTH:
			throw new IllegalArgumentException("Choose one parent");
		case NEITHER:
			throw new IllegalArgumentException("Choose one parent");
		default:
			throw new IllegalArgumentException("Choose one parent");
		}
	}
	
	private CloseableIterator<VariantContext> queryRI(String chr, int start, int end) {
		return riVcfReader.query(chr, start, end);
	}
	
	/**
	 * Get haplotype for the whole interval
	 * Only return REF or ALT if the parent is homozygous for every variant in the interval in both VCF files
	 * @param parent Parent to get haplotype for
	 * @param chr Chromosome
	 * @param start Interval start
	 * @param end Interval end
	 * @return REF or ALT if homozygous for every variant in the interval, NEITHER otherwise
	 */
	@SuppressWarnings("unused")
	private Haplotype getParentalHaplotype(Parent parent, String chr, int start, int end) {
		CloseableIterator<VariantContext> iter1 = queryParent(parent, chr, start, end);
		CloseableIterator<VariantContext> iter2 = riVcfReader.query(chr, start, end);
		String parentName = parentName(parent);
		Haplotype rtrn = Haplotype.getCommonHaplotype(iter1, iter2, parentName);
		iter1.close();
		iter2.close();
		logger.debug("PARENTAL_HAPLOTYPE\t" + getParentName(parent) + "\t" + chr + ":" + start + "-" + end + "\t" + rtrn.toString());
		return rtrn;
	}
	
	/**
	 * Determine which parent contributed the allele for an RI strain
	 * @param genotypes Genotypes context (all genotype info for the variant)
	 * @param sampleName Sample name to get 
	 * @return Parent 1, Parent 2, or neither
	 */
	protected Parent riVariantOrigin(GenotypesContext genotypes, String sampleName) {
		Genotype sampleGenotype = genotypes.get(sampleName);
		if(sampleGenotype.isNoCall()) {
			//logger.debug("RI_VARIANT_ORIGIN\t" + sampleName + " has no call. Returning neither.");
			return Parent.NEITHER;
		}
		Genotype parent1genotype = genotypes.get(parent1name);
		Genotype parent2genotype = genotypes.get(parent2name);
		boolean parent1 = sampleGenotype.sameGenotype(parent1genotype);
		boolean parent2 = sampleGenotype.sameGenotype(parent2genotype);
		if(parent1) {
			if(parent2) {
				logger.debug("RI_VARIANT_ORIGIN\t" + sampleName + " has same genotype (" + sampleGenotype.getGenotypeString() + ") as " + parent1name + " (" + 
						parent1genotype.getGenotypeString() + ") and parent 2 (" + parent2genotype.getGenotypeString() + ")");
				return Parent.BOTH;
			}
			//logger.debug("RI_VARIANT_ORIGIN\t" + sampleName + " has same genotype (" + sampleGenotype.getGenotypeString() + ") as " + parent1name + " (" + 
					//parent1genotype.getGenotypeString() + ")");
			return Parent.PARENT1;
		}
		if(parent2) {
			//logger.debug("RI_VARIANT_ORIGIN\t" + sampleName + " has same genotype (" + sampleGenotype.getGenotypeString() + ") as " + parent2name + " (" + 
					//parent2genotype.getGenotypeString() + ")");
			return Parent.PARENT2;
		}
		//logger.debug("RI_VARIANT_ORIGIN\t" + sampleName + " has same genotype (" + sampleGenotype.getGenotypeString() + ") as neither parent (" + 
				//parent1genotype.getGenotypeString() + ") and (" + parent2genotype.getGenotypeString() + ")");
		return Parent.NEITHER;	
	}
	
	protected void checkRIvariantsConsecutive(VariantContext riVariant1, VariantContext riVariant2) {
		String chr = riVariant1.getContig();
		if(!riVariant2.getContig().equals(chr)) {
			throw new IllegalArgumentException("Variants must have some contig");
		}
		int var1start = riVariant1.getStart();
		int var2start = riVariant2.getStart();
		CloseableIterator<VariantContext> iter = riVcfReader.query(chr, var1start, var2start);
		while(iter.hasNext()) {
			VariantContext vc = iter.next();
			if(!vc.toString().equals(riVariant1.toString()) && !vc.toString().equals(riVariant2.toString())) { // VariantContext doesn't override equals()
				throw new IllegalArgumentException("RI variants are not consecutive. Variant 1: "
						+ toUCSC(riVariant1) + " Variant 2: " + toUCSC(riVariant2) + " Between: " + toUCSC(vc));
			}
		}
		iter.close();
	}

	private static String toUCSC(VariantContext vc) {
		return vc.getContig() + ":" + vc.getStart() + "-" + vc.getEnd();
	}
	
	/**
	 * Get genotypes for all parental SNPs between these two SNPs, including the two original SNPs
	 * Try to get full haplotype if the sample agrees with one parent at both given SNPs
	 * @param riVariant1
	 * @param riVariant2
	 * @return
	 */
	private Collection<VariantContext> getFullHaplotype(VariantContext riVariant1, VariantContext riVariant2) {
		
		// Make sure we have no variant calls between these two for the RI samples
		checkRIvariantsConsecutive(riVariant1, riVariant2);
		
		Collection<VariantContext> rtrn = new HashSet<VariantContext>();
		
		String chr = riVariant1.getContig();
		int start = riVariant1.getStart();
		int end = riVariant2.getEnd();
		
		logger.debug("GETTING_HAPLOTYPE\t" + chr + ":" + start + "-" + end);
		
		GenotypesContext genotypesVariant1 = riVariant1.getGenotypes(); // All the genotypes for variant 1
		GenotypesContext genotypesVariant2 = riVariant2.getGenotypes(); // All the genotypes for variant 2
		
		/*
		 *  Map of sample to parent
		 *  Parent is PARENT1, PARENT2, or BOTH if the sample agrees with the parent(s) at both endpoints
		 */
		Map<String, Parent> riHaplotypes = new HashMap<String, Parent>();
		for(String sample : sampleNames) {
			Parent variant1origin = riVariantOrigin(genotypesVariant1, sample);
			Parent variant2origin = riVariantOrigin(genotypesVariant2, sample);
			if(!variant1origin.equals(variant2origin)) {
				riHaplotypes.put(sample, Parent.NEITHER);
				logger.debug("PUTTING_VARIANT_ORIGIN\t" + chr + ":" + start + "-" + end + "\t" + sample + "\t" + Parent.NEITHER.toString());
				continue;
			}
			logger.debug("PUTTING_VARIANT_ORIGIN\t" + chr + ":" + start + "-" + end + "\t" + sample + "\t" + getParentName(variant1origin));
			riHaplotypes.put(sample, variant1origin);
		}
		
		Collection<VariantContext> p1 = copyParentGenotypes(Parent.PARENT1, chr, start, end, whichKeys(riHaplotypes, Parent.PARENT1));
		Collection<VariantContext> p2 = copyParentGenotypes(Parent.PARENT2, chr, start, end, whichKeys(riHaplotypes, Parent.PARENT2));
		Collection<VariantContext> b1 = copyParentGenotypes(Parent.PARENT1, chr, start, end, whichKeys(riHaplotypes, Parent.BOTH));
		Collection<VariantContext> b2 = copyParentGenotypes(Parent.PARENT2, chr, start, end, whichKeys(riHaplotypes, Parent.BOTH));
		Collection<VariantContext> n = copyOwnGenotypes(chr, start, end, whichKeys(riHaplotypes, Parent.NEITHER));
		p1.addAll(copyOwnGenotypes(chr, start, end, whichKeys(riHaplotypes, Parent.PARENT1))); // Also include genotypes from RI vcf file
		p2.addAll(copyOwnGenotypes(chr, start, end, whichKeys(riHaplotypes, Parent.PARENT2))); // Also include genotypes from RI vcf file
		b1.addAll(copyOwnGenotypes(chr, start, end, whichKeys(riHaplotypes, Parent.BOTH))); // Also include genotypes from RI vcf file
		logger.debug("ADDING_COPIED_PARENT_GENOTYPES\t" + parent1name + ": " + p1.size());
		logger.debug("ADDING_COPIED_PARENT_GENOTYPES\t" + parent2name + ": " + p2.size());
		logger.debug("ADDING_COPIED_PARENT_GENOTYPES\tboth: " + Integer.valueOf(b1.size() + b2.size()).toString());
		rtrn.addAll(p1);
		rtrn.addAll(p2);
		rtrn.addAll(b1);
		rtrn.addAll(b2);
		rtrn.addAll(n);
		
		try {
			return VCFUtils.merge(rtrn, dict);
		} catch(IllegalArgumentException e) {
			logger.warn("Caught exception between " + riVariant1.getID() + " and " + riVariant2.getID() + ": "
					+ e.getMessage());
			logger.warn("Removing samples that match neither parent over region");
			rtrn.removeAll(n);
			return rtrn;
		}
		
	}
	
	private String getParentName(Parent parent) {
		switch(parent) {
		case BOTH:
			return "BOTH";
		case NEITHER:
			return "NEITHER";
		case PARENT1:
			return parent1name;
		case PARENT2:
			return parent2name;
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	private static Collection<String> whichKeys(Map<String, Parent> map, Parent parent) {
		Collection<String> rtrn = new HashSet<String>();
		for(String str : map.keySet()) {
			if(map.get(str).equals(parent)) {
				rtrn.add(str);
			}
		}
		return rtrn;
	}
	
	
	/**
	 * For all parent variants within the interval, create new variant contexts with the parental genotype copied to a set of RI samples
	 * @param parent The parent to use
	 * @param chr Interval chromosome
	 * @param start Interval start
	 * @param end Interval end
	 * @param riSamples The samples to copy parental genotype to. Other samples will have no genotype for the returned variant contexts.
	 * @return Variant contexts with the parental genotype copied to the requested RI samples
	 */
	private Collection<VariantContext> copyParentGenotypes(Parent parent, String chr, int start, int end, Collection<String> riSamples) {
		Collection<VariantContext> rtrn = new HashSet<VariantContext>();
		CloseableIterator<VariantContext> iter = queryParent(parent, chr, start, end);
		while(iter.hasNext()) {
			VariantContext vc = iter.next();
			Genotype parentGenotype = vc.getGenotype(parentName(parent));
			List<Allele> alleles = parentGenotype.getAlleles();
			logger.debug("GETTING_PARENT_GENOTYPE_TO_COPY\t" + vc.getContig() + "\t" + vc.getStart() + "\t" + parentGenotype.getGenotypeString());
			Collection<Genotype> genotypes = new ArrayList<Genotype>();
			for(String sample : riSamples) {
				GenotypeBuilder gb = new GenotypeBuilder(sample, alleles);
				Genotype genotype = gb.make();
				genotypes.add(genotype);
				logger.debug("ADDED_COPY_GENOTYPE\t" + chr + ":" + start + "-" + end + "\t" + vc.getStart() + "\t" + getParentName(parent) + "\t" + 
				sample + "\t" + genotype.getGenotypeString());
			}
			List<Allele> alleleList = vc.getAlleles();
			//alleleList.add(GATKVariantContextUtils.NON_REF_SYMBOLIC_ALLELE);
			VariantContextBuilder vcBuilder = new VariantContextBuilder("NA", chr, vc.getStart(), vc.getEnd(), alleleList);
			vcBuilder.genotypes(genotypes);
			VariantContext newVc = vcBuilder.make();
			logger.debug("NEW_COPIED_VARIANT_CONTEXT\t" + newVc.toStringDecodeGenotypes());
			rtrn.add(newVc);
		}
		iter.close();
		return rtrn;
	}
	
	/**
	 * Just make new variant contexts with copied genotypes from RI vcf file for the specified samples
	 * @param chr Interval chromosome
	 * @param start Interval start
	 * @param end Interval end
	 * @param samples Samples to copy
	 * @return Variant contexts with these samples' genotypes only
	 */
	private Collection<VariantContext> copyOwnGenotypes(String chr, int start, int end, Collection<String> samples) {
		Collection<VariantContext> rtrn = new HashSet<VariantContext>();
		CloseableIterator<VariantContext> iter = queryRI(chr, start, end);
		while(iter.hasNext()) {
			VariantContext vc = iter.next();
			Collection<Genotype> genotypes = new ArrayList<Genotype>();
			for(String sample : samples) {
				Genotype genotype = vc.getGenotype(sample);
				genotypes.add(genotype);
			}
			List<Allele> alleleList = vc.getAlleles();
			
			VariantContextBuilder vcBuilder = new VariantContextBuilder("NA", chr, vc.getStart(), vc.getEnd(), alleleList);
			vcBuilder.genotypes(genotypes);
			VariantContext newVc = vcBuilder.make();
			rtrn.add(newVc);
		}
		iter.close();
		return rtrn;
	}
	
	private void writeFullHaplotypeFile(String outVcf) {
		logger.info("");
		logger.info("Writing vcf file with extended haplotypes to " + outVcf + "...");
		VariantContextWriterBuilder builder = new VariantContextWriterBuilder();
		builder.setOutputFile(outVcf);
		builder.setReferenceDictionary(dictFork);
		VariantContextWriter vcfWriter = new SortingVariantContextWriter(builder.build(), 50000000);
		vcfWriter.writeHeader(riVcfReader.getFileHeader());
		CloseableIterator<VariantContext> iter = riVcfReader.iterator();
		VariantContext first = null;
		VariantContext second = iter.next();
		int numDone = 0;
		while(iter.hasNext()) {
			first = second;
			second = iter.next();
			numDone++;
			if(numDone % 1000 == 0) {
				logger.info("Finished " + numDone + " pairs of variants from RI file");
			}
			if(!first.getContig().equals(second.getContig())) {
				logger.warn("Moving from " + first.getContig() + " to " + second.getContig());
				continue;
			}
			// Check distance in bp
			if(second.getStart() - first.getStart() > MAX_BP_DISTANCE) {
				logger.debug("Markers not within " + MAX_BP_DISTANCE + " bp of each other. Skipping. " + first.getID() + "  " + second.getID());
				continue;
			}
			// Check distance in cM
			Optional<Boolean> cm = markersWithinMaxCm(first, second, riMarkerCmPositions, MAX_CM_DISTANCE);
			if(cm.isPresent()) {
				if(cm.get().booleanValue()) {
					logger.debug("Markers not within " + MAX_CM_DISTANCE + " cM of each other. Skipping. " + first.getID() + "  " + second.getID());
					continue;
				}
			} 
			Collection<VariantContext> hap = getFullHaplotype(first, second);
			logger.debug("FILLED_IN_HAPLOTYPE\t" + first.getContig() + ":" + first.getStart() + "-" + second.getStart() + "\t" + 
					hap.size() + " positions in between");
			for(VariantContext vc : hap) {
				vcfWriter.add(vc);
			}
		}
		iter.close();
		vcfWriter.close();
		logger.info("Done writing vcf file.");
	}
	
	
	public static void main(String[] args) throws IOException {
		
		CommandLineParser p = new CommandLineParser();
		p.addStringArg("-rv", "VCF file including parental strains and recombinant inbred strains", true);
		p.addStringArg("-pv1", "VCF file for parental strain 1", true);
		p.addStringArg("-pv2", "VCF file for parental strain 2", true);
		p.addStringArg("-r", "Reference sequence table (line format: chr   size)", true);
		p.addStringArg("-p1", "Parental sequence 1 name", true);
		p.addStringArg("-p2", "Parental sequence 2 name", true);
		p.addStringArg("-o", "Output VCF file", true);
		p.addBooleanArg("-d", "Debug logging", false, false);
		p.addStringArg("-cm", "Table of centiMorgan positions for markers in RI VCF file (line format: ID   chr   cm", false, null);
		p.parse(args);
		if(p.getBooleanArg("-d")) {
			logger.setLevel(Level.DEBUG);
			Haplotype.logger.setLevel(Level.DEBUG);
			VCFUtils.logger.setLevel(Level.DEBUG);
		}
		String riVcf = p.getStringArg("-rv");
		String parent1vcf = p.getStringArg("-pv1");
		String parent2vcf = p.getStringArg("-pv2");
		String parent1 = p.getStringArg("-p1");
		String parent2 = p.getStringArg("-p2");
		String refSizeFile = p.getStringArg("-r");
		String outVcf = p.getStringArg("-o");
		String cmTable = p.getStringArg("-cm");
		
		RecombinantInbredHaplotypeWriter r = new RecombinantInbredHaplotypeWriter(riVcf, parent1vcf, parent2vcf, parent1, parent2, refSizeFile, cmTable);
		r.writeFullHaplotypeFile(outVcf);
		
		logger.info("");
		logger.info("All done.");
		
	}
	
	
}
