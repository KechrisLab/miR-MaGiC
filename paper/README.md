# Code and resources to reproduce the results in the miR-MaGiC paper

## Raw sequencing data

Fastq files are available for download at https://phenogen.ucdenver.edu/.

## src/figures/

R scripts to produce the figures in the paper from the output of each quantification method

## src/functional_groups/

Script to create the functional groupings of miRNAs by parsing miRBase names. NOTE: some of the more obscure species in miRBase have a different name format for miRNAs. The regular expressions in the script define the names that can be collapsed by this script.

## src/java/variant/

Code to reproduce the strain-specific haplotype calls. Requires [this fork](https://github.com/pamelarussell/htsjdk) of the HTSJDK library to be first on the build path, followed by the libraries in miR-MaGiC/lib/.

Steps:

1. Extract miRNA sequences from genome using the miRBase annotation and [bedtools getfasta](http://bedtools.readthedocs.io/en/latest/content/tools/getfasta.html)
2. Index the miRNA sequences using [samtools faidx](http://www.htslib.org/doc/samtools.html)
3. Collapse identical sequences using miR-MaGiC/paper/src/java/variant/variant/FastaCollapseIdenticalSeqs.java
4. Index the collapsed sequences using samtools faidx
5. Create sequence dictionary using CreateSequenceDictionary in [this fork](https://github.com/pamelarussell/picard)
5. Convert RI VCF file to relative coordinates within miRNAs
6. Sort converted VCF by miRNA coordinates using [vcfsorter](https://gist.github.com/dfjenkins3/b546caa73f9edc4db189)
7. Make strain specific VCFs with miR-MaGiC/paper/src/variant/makeStrainSpecificVCF
8. Incorporate variants into sequences with [GATK FastaAlternateReferenceMaker](https://software.broadinstitute.org/gatk/documentation/tooldocs/current/org_broadinstitute_gatk_tools_walkers_fasta_FastaAlternateReferenceMaker.php)
9. Mark strain specific sequences with miR-MaGiC/paper/src/variant/replaceSequenceNames
10. Collapse identical sequences using miR-MaGiC/paper/src/java/variant/variant/FastaCollapseIdenticalSeqs.java
11. Index the collapsed sequences using samtools faidx

## resources/

Sample and sequencing metadata table

