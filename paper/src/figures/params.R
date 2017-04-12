#******************************************
#   Filesystem specific paths to inputs
#******************************************


# Input
israp.counts.file <- "???" # Table of iSRAP estimated counts; rows=miRNAs; columns=samples
mirdeep.counts.file <- "???" # Table of miRDeep2 quantifier estimated counts; rows=miRNAs; columns=samples
mirge.counts.dir <- "???" # Directory with one miRge .csv counts file per sample
mirge.counts.noPermissive.dir <- "???" # Directory with one miRge .csv counts file per sample for modified miRge
magic.counts.file.no.collapse <- "???" # Table of MaGiC estimated counts with no collapsing by functional group; rows=miRNAs; columns=samples
magic.counts.file.collapse.mimat <- "???" # Table of MaGiC estimated counts with collapsing by MIMAT accession; rows=miRNAs; columns=samples
magic.counts.file.collapse.mirbase <- "???" # Table of MaGiC estimated counts with collapsing by miRBase name; rows=miRNAs; columns=samples
metadata.file <- "???" # Sequencing metadata file included in repo

# Output
out.plots.dir <- "???" # Output directory for plots
if(!dir.exists(out.plots.dir)) dir.create(out.plots.dir, recursive = T)

# Misc
mirge.raw.reads.path <- "???" # String to substitute in header of miRge .csv output when read in by read.csv() - see use in import_data.R

