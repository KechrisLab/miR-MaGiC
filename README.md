# What is miR-MaGiC?

miR-MaGiC is a pipeline for miRNA expression quantification from small RNA-seq. miR-MaGiC is distinct from common tools in that it (1)  performs highly stringent mapping to mature miRNA sequences, minimizing the number of ambiguous mappings, and (2) performs collapsing of counts over functional equivalence classes of miRNAs, instead of reporting counts for individual miRNAs. The pipeline is defined in a [Snakemake](https://snakemake.readthedocs.io/en/stable/) workflow and consists of standalone Java programs.

# What isn't miR-MaGiC?

miR-MaGiC reports estimated counts for functional equivalence classes of miRNAs. It does not perform normalization or differential expression analyses.

# Citation

TBA

# Requirements

- [Java](https://www.java.com/en/download/) version 8 (or higher when available)

- [Snakemake](https://snakemake.readthedocs.io/en/stable/index.html) version 3.10.2 or higher

# Installation

There is nothing to install. Just clone or download this repository.

# What's included in the repository?

### Snakemake workflow

The workflow is defined in `pipeline/Snakefile` and `pipeline/config.json`.

### Workflow components

Workflow steps are Java programs provided as runnable .jar files in `pipeline/`.

### Sample functional group tables

miR-MaGiC combines counts at the level of functional groups of miRNAs. The grouping is provided by the user in a table (see Usage below). Recommended tables based on [miRBase](http://www.mirbase.org/) version 21 are provided for several species in `resources/group_tables/`.

### Source code

The Java source code for pipeline steps is in `src/mirmagic/`.

# Usage

[Inputs]

[Running]



