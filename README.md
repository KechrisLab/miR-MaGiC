# What is miR-MaGiC?

miR-MaGiC is a pipeline for miRNA expression quantification from small RNA-seq. miR-MaGiC is distinct from common tools in that it (1)  performs highly stringent mapping to mature miRNA sequences, minimizing the number of ambiguous mappings, and (2) performs collapsing of counts over functional equivalence classes of miRNAs, instead of reporting counts for individual miRNAs. The pipeline is defined in a [Snakemake](https://snakemake.readthedocs.io/en/stable/) workflow and consists of standalone Java programs.

# What isn't miR-MaGiC?

miR-MaGiC reports estimated counts for functional equivalence classes of miRNAs. It does not perform normalization or differential expression analysis.

# Citation

TBA

# Requirements

- [Java](https://www.java.com/en/download/) version 8 (or higher when available)
- [Snakemake](https://snakemake.readthedocs.io/en/stable/index.html) version 3.10.2 or higher

# Installation

There is nothing to install. Just clone or download this repository.

# What's included in the repository?

### Snakemake workflow

The workflow is defined in `pipeline/Snakefile`.

### Workflow components

Workflow steps are Java programs provided as runnable .jar files in `pipeline/`.

### Sample functional group tables

miR-MaGiC combines counts at the level of functional groups of miRNAs. The grouping is provided by the user in a table (see Usage below). Recommended tables based on [miRBase](http://www.mirbase.org/) version 21 are provided for several species in `resources/group_tables/`.

### Source code

The Java source code for pipeline steps is in `src/mirmagic/`.

# Usage

## Inputs

*(See explanation of parameters below.)*

- Pipeline components (this repository)
- Fastq file of small RNA-seq reads
- Fasta file of mature miRNA sequences
- Table of functional groups of miRNAs

## Running miR-MaGiC

The pipeline is executed by invoking the `snakemake` command with arguments specifying the parameters.

### Sample command line

```bash
snakemake \ 
--directory /path/to/directory/containing/Snakefile/and/configjson/ \ 
--snakefile /full/path/to/Snakefile \
--config \
outdir=/path/to/output/directory/ \
fastq=/path/to/fastq.fastq \
mirna=/path/to/mirnas.fasta \
mirna_gp=/path/to/functional/groups/table.txt \
jar=/path/to/miR-MaGiC/pipeline/ \
k=20 \
plus_strand_only=True
```

### Explanation of command line parameters

*All parameters are required.*

In the descriptions below, `$MIRMAGIC_DIR` refers to the root directory of the miR-MaGiC repository on your machine.

- `--directory` The directory containing the `Snakefile` and `config.json`. If you leave the repository contents as downloaded, this will be `$MIRMAGIC_DIR/pipeline/`.
- `--snakefile` The full path to the `Snakefile`. If you leave the repository contents as downloaded, this will be `$MIRMAGIC_DIR/pipeline/Snakefile`.
- `--config` The job configuration to pass to Snakemake. This is where you specify run-specific parameters. The value is of the form `[KEY=VALUE [KEY=VALUE ...]]`. All run-specific parameters are required. If a parameter is missing, the workflow will die with an error message specifying the missing parameter. 
  - `outdir` Directory to write output to
  - `fastq` Input fastq file
  - `mirna` Fasta file of mature miRNA sequences with the same nucleotide bases as the fastq file (be careful with T vs. U). Fasta sequence names must contain no whitespace.
  - `mirna_gp` Table specifying functional equivalence classes of miRNAs. These are intended to be groups of miRNAs that are functionally equivalent for the goals of the study. Reads that map to multiple members of a group are only counted once for the group. Final counts are reported at the level of groups. The table should contain one line for each miRNA in the fasta file `mirna`. Each line has two fields separated by whitespace: `<miRNA_id>` and `<group_id>`. Recommended tables derived from miRBase version 21 are provided for several species in `$MIRMAGIC_DIR/resources/group_tables/`.
  - `jar` The directory containing the runnable .jar files for the pipeline. If you leave the repository contents as downloaded, this will be `$MIRMAGIC_DIR/pipeline/`.
  - `k` The length of perfect matches to require between reads and miRNA sequences. A read is matched to a miRNA if they contain identical subsequences of length *k*; the rest of the read and miRNA are ignored in that case. miRNAs shorter than *k* bases are allowed to have a perfect match of their full length instead of requiring a match of length *k*. Recommended: `k=20`.
  - `plus_strand_only` Do not count reverse complement matches. Possible values: `True`, `False`.

### Output

In the provided output directory, miR-MaGiC writes a file whose name begins with `final_counts` and includes the fastq file name. Each line of the file has two fields: the name of a functional group of miRNAs, and the total number of reads matched to that group. If a read matches more than one miRNA in a group, it is only counted once for the group. Groups with zero count are not included in the output.



