#*************************
#   Import the counts
#*************************


# Function to import iSRAP counts
import.counts.iSRAP <- function() {
  counts <- read.table(israp.counts.file, header=T)
  rownames(counts) <- counts$miRNA
  counts <- counts[, -grep("miRNA", colnames(counts))]
  colnames(counts) <- unlist(lapply(colnames(counts), function(x) if(substr(x, 1, 1) == "X") substr(x, 2, nchar(x)) else x))
  counts
}

# Function to import miRDeep counts
char.cols <- c("row.names", "precursor")
import.counts.mirdeep <- function() {
  counts <- read.table(mirdeep.counts.file, row.names=NULL)
  rownames <- paste(counts[,1], "{precursor=", counts[,2], "}", sep="")
  dup.rows <- which(unlist(lapply(rownames, function(x) sum(rownames == x))) > 1)
  lapply(rownames[dup.rows], function(x) warning(paste("Skipping duplicate rows", x)))
  rownames <- rownames[-dup.rows]
  counts <- counts[-dup.rows, -which(colnames(counts) %in% char.cols)]
  colnames(counts) <- unlist(lapply(colnames(counts), function(x) if(substr(x, 1, 1) == "X") substr(x, 2, nchar(x)) else x))
  rownames(counts) <- rownames
  counts
}

# Function to import MaGiC counts
import.counts.magic <- function(file) {
  counts <- read.table(file)
  colnames(counts) <- unlist(lapply(colnames(counts), function(x) if(substr(x, 1, 1) == "X") substr(x, 2, nchar(x)) else x))
  counts
}

# For a list of MaGiC (possibly collapsed) IDs, get a list of the component IDs
expand.magic.ids <- function(ids) {
  unique(unlist(strsplit(unlist(strsplit(ids, split=";")), split="%")))
}

# Function to import miRge counts
import.counts.mirge <- function(dir) {
  files <- list.files(path = dir, pattern = "counts", full.names=T)
  rtrn <- data.frame()
  started <- F
  for(file in files) {
    # sample <- basename(file)
    # sample <- gsub("counts_", "", sample)
    # sample <- gsub(".csv", "", sample)
    data <- read.csv(file)
    colnames(data) <- gsub(".fq", "", colnames(data))
    colnames(data) <- gsub(mirge.raw.reads.path, "", colnames(data))
    data <- data[which(data$miRNA != "miRNAtotal"),]
    data <- data[grep("mmu", data$miRNA),]
    rownames(data) <- data$miRNA
    if(!started) {
      rtrn <- data
      started <- T
    } else rtrn <- merge(rtrn, data, all=T)
  }
  rownames(rtrn) <- rtrn$miRNA
  rtrn <- rtrn[,which(colnames(rtrn) != "miRNA")]
  rtrn
}

# Import the metadata
metadata <- read.table(metadata.file, header=T)
rownames(metadata) <- paste(metadata$Core_ID, "_", metadata$Batch, "_", metadata$Strain, sep="")
counts.clipped.filtered <- read.table(filtered.read.counts.file, header=T)
rownames(counts.clipped.filtered) <- counts.clipped.filtered$ID
metadata$Filtered_read_count <- counts.clipped.filtered[rownames(metadata),]$Count

# Import the counts
counts.israp <- import.counts.iSRAP()
counts.mirdeep <- import.counts.mirdeep()
counts.magic.no.collapse <- import.counts.magic(magic.counts.file.no.collapse)
counts.magic.collapse.mimat <- import.counts.magic(magic.counts.file.collapse.mimat)
counts.magic.collapse.mirbase <- import.counts.magic(magic.counts.file.collapse.mirbase)
counts.mirge <- import.counts.mirge(mirge.counts.dir)
counts.mirge.noPermissive <- import.counts.mirge(mirge.counts.noPermissive.dir)

# Get the sample names
samples <- intersect(intersect(intersect(intersect(intersect(intersect(colnames(counts.magic.no.collapse), colnames(counts.israp)), 
                               colnames(counts.mirdeep)), colnames(counts.mirge)), colnames(counts.mirge.noPermissive)), 
                               colnames(counts.magic.collapse.mimat)), colnames(counts.magic.collapse.mirbase))
samples.without.X <- unlist(lapply(samples, function(x) if(substr(x, 1, 1) == "X") substr(x, 2, nchar(x)) else x))
get.strain <- function(sample) {y <- unlist(strsplit(sample, "_")); y[length(y)]}
strain <- unlist(lapply(samples, function(x) get.strain(x)))
unique.strains <- unique(strain)
parentals.stage.1 <- samples[union(grep("1_ILS", samples), grep("1_ISS", samples))]
parentals <- unlist(lapply(samples.without.X, function(s) {
  if(grepl("ILS", s)) return("ILS")
  else if(grepl("ISS", s)) return("ISS")
  else return(NA)
}))
parentals.no.replicates <- unlist(lapply(samples.without.X, function(s) {
  if(grepl("1_ILS", s)) return("ILS")
  else if(grepl("1_ISS", s)) return("ISS")
  else return(NA)
}))


ils <- which(parentals == "ILS")
iss <- which(parentals == "ISS")

# Arrange columns of counts tables
counts.israp <- counts.israp[,samples]
counts.mirdeep <- counts.mirdeep[,samples]
counts.magic.no.collapse <- counts.magic.no.collapse[,samples]
counts.magic.collapse.mimat <- counts.magic.collapse.mimat[,samples]
counts.magic.collapse.mirbase <- counts.magic.collapse.mirbase[,samples]
counts.mirge <- counts.mirge[,samples]
counts.mirge.noPermissive <- counts.mirge.noPermissive[,samples]


