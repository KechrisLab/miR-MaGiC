package variant;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFFileReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.broadinstitute.gatk.utils.GenomeLoc;
import org.broadinstitute.gatk.utils.GenomeLocParser;

import variant.RecombinantInbredHaplotypeWriter;

public class VCFUtils {
	
	public static Logger logger = Logger.getLogger(VCFUtils.class.getName());
	
	/**
	 * Merge a collection of variants that are required to have the same genomic location
	 * @param variants Variants with same location
	 * @param dict SAM sequence dictionary for reference genome
	 * @return Merged variant context
	 */
	public static VariantContext mergeSamePosition(Collection<VariantContext> variants, SAMSequenceDictionary dict) {
		VariantContext first = variants.iterator().next();
		GenomeLocParser glp = new GenomeLocParser(dict);
		GenomeLoc firstLoc = glp.createGenomeLoc(first.getContig(), first.getStart(), first.getEnd(), true); 
		logger.debug("Merging variants with position " + firstLoc.toString());
		Map<String, Genotype> genotypeBySample = new HashMap<String, Genotype>(); // Save genotypes by sample so can check each sample always gets the same genotype
		Collection<Allele> allAlleles = new HashSet<Allele>(); // Keep track of all possible alleles
		String id = first.getID();
		for(VariantContext vc : variants) {
			if(!vc.getID().equals(".") && !vc.emptyID()) {
				id = vc.getID();
			}
			GenomeLoc loc = glp.createGenomeLoc(vc.getContig(), vc.getStart(), vc.getEnd(), true);
			if(!loc.equals(firstLoc)) { // Check all same location
				throw new IllegalArgumentException("All variants must have same genomic location: " + firstLoc.toString() + ", " + loc.toString());
			}
			Iterator<Genotype> iter = vc.getGenotypesOrderedByName().iterator();
			Collection<String> toRemove = new HashSet<String>(); // Samples with problems to remove
			while(iter.hasNext()) {
				Genotype genotype = iter.next();
				if(genotype.isNoCall()) {
					continue;
				}
				String sample = genotype.getSampleName();
				if(genotypeBySample.containsKey(sample)) { // Check sample hasn't been included with different genotype
					if(!genotypeBySample.get(sample).sameGenotype(genotype)) {
						toRemove.add(sample);
					}
				}
				genotypeBySample.put(sample, genotype);
				allAlleles.addAll(genotype.getAlleles());
			}
			String removed = "";
			for(String sample : toRemove) { // Remove problem samples
				removed += sample + " ";
				genotypeBySample.remove(sample);
			}
			if(!toRemove.isEmpty()) logger.warn("Samples included twice with different genotype. Removed. " + loc.toString() + " " + removed);
			allAlleles.add(vc.getReference());
			allAlleles.addAll(vc.getAlternateAlleles());
		}
		// Build variant context
		VariantContextBuilder builder = new VariantContextBuilder();
		builder.id(id);
		builder.chr(first.getContig());
		builder.start(first.getStart());
		builder.stop(first.getEnd());
		builder.alleles(allAlleles);
		builder.source("Unknown");
		builder.genotypes(genotypeBySample.values());
		return builder.make();
	}
	
	/**
	 * Merge sub-collections of variants with same genomic location
	 * @param variants Variants including some with same location
	 * @param dict SAM sequence dictionary for reference genome
	 * @return Merged variant contexts
	 */
	public static Collection<VariantContext> merge(Collection<VariantContext> variants, SAMSequenceDictionary dict) {
		Map<GenomeLoc, List<VariantContext>> duplicateLocs = new TreeMap<GenomeLoc, List<VariantContext>>();
		GenomeLocParser glp = new GenomeLocParser(dict);
		for(VariantContext vc : variants) {
			GenomeLoc loc = glp.createGenomeLoc(vc.getContig(), vc.getStart(), vc.getEnd(), true);
			if(!duplicateLocs.containsKey(loc)) {
				duplicateLocs.put(loc, new ArrayList<VariantContext>());
			}
			duplicateLocs.get(loc).add(vc);
		}
		Collection<VariantContext> rtrn = new ArrayList<VariantContext>();
		for(GenomeLoc loc : duplicateLocs.keySet()) {
			logger.debug("");
			logger.debug(loc.toString());
			for(VariantContext v : duplicateLocs.get(loc)) {
				logger.debug("MERGING\t" + v.toString());
			}
			VariantContext merged = mergeSamePosition(duplicateLocs.get(loc), dict);
			logger.debug("MERGED\t" + duplicateLocs.get(loc).size() + "\t" + loc.toString());
			logger.debug("MERGED_VARIANT_CONTEXT\t" + merged.toStringDecodeGenotypes());
			rtrn.add(merged);
		}
		return rtrn;
	}
	
	public static int vcfToZeroBased(int pos) {
		return pos - 1;
	}
	
	public static int zeroBasedToVcf(int pos) {
		return pos + 1;
	}
	
	
	
	public static void main(String[] args) throws IOException {
		
		logger.setLevel(Level.DEBUG);
		
		String vcf = "/Users/prussell/Documents/smRNA_LXS/output/variants/LXSgeno.mm10.extended_haplotypes.vcf";
		String outVcf = "/Users/prussell/Documents/smRNA_LXS/output/variants/sample_merged.vcf";
		String sizes = "/Users/prussell/Documents/smRNA_LXS/output/variants/sizes";
		SAMSequenceDictionary dict = RecombinantInbredHaplotypeWriter.createDict(sizes);
		VCFFileReader reader = new VCFFileReader(new File(vcf));
		Collection<VariantContext> all = new ArrayList<VariantContext>();
		CloseableIterator<VariantContext> iter = reader.iterator();
		int numDone = 0;
		while(numDone < 11) {
			all.add(iter.next());
			numDone++;
		}
		iter.close();
		reader.close();
		Collection<VariantContext> merged = merge(all, dict);
		
		VariantContextWriterBuilder builder = new VariantContextWriterBuilder();
		builder.setOutputFile(outVcf);
		builder.setReferenceDictionary(RecombinantInbredHaplotypeWriter.createDictFork(sizes));
		VariantContextWriter vcfWriter = builder.build();
		VCFFileReader headerReader = new VCFFileReader(new File(vcf));
		vcfWriter.writeHeader(headerReader.getFileHeader());
		headerReader.close();
		logger.debug("");
		for(VariantContext vc : merged) {
			logger.debug("ADDING\t" + vc.toStringDecodeGenotypes());
			vcfWriter.add(vc);
		}
		vcfWriter.close();
		
		logger.info("");
		logger.info("All done.");
		
	}
	
	

}
