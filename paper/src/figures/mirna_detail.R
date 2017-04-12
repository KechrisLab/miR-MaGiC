#**********************************************************
#   Analyze counts of an individual miRNA by each method
#**********************************************************


find.matches <- function(rpm, mirna) {
  rtrn <- which.name.contains(rpm, mirna)
  rtrn <- rtrn[order(rtrn, decreasing = T)]
  rtrn
}


stacked.bar.sample <- function(sample, mirna) {
  
  fm <- function(rpm) {
    data.frame(find.matches(data.frame(rpm[,sample]), mirna))
  }
  
  rpm <- fm(rpm.israp)
  colnames(rpm) <- c("iSRAP")
  rpm$miRDeep2 <- fm(rpm.mirdeep)[,1]
  rpm$miRge <- c(fm(rpm.mirge)[,1], 0)
  rpm$miRgeModified <- c(fm(rpm.mirge.noPermissive)[,1], 0)
  rpm$MaGiC_noCollapse <- fm(rpm.magic.no.collapse)[,1]
  rpm$MaGiC_MIMAT <- c(fm(rpm.magic.collapse.mimat)[,1], 0)
  rpm$MaGiC_miRBase <- c(fm(rpm.magic.collapse.mirbase)[,1], 0)
  
  rpm <- rpm[, order(unlist(rpm[1, ]), decreasing = T)]

  colors <- c("dodgerblue4", "cadetblue3")
  sp <- getOption('sciPen')
  options(scipen=999)
  bp <- barplot(as.matrix(rpm),
                #beside=T,
                col = colors,
                ylab = paste("Median RPM across libraries for members of the", mirna, "family"),
                axisnames = F)
  text(bp, par("usr")[3], labels = colnames(rpm), srt = 30, adj = c(1.1, 1.1), xpd = T)
  axis(2)
  legend('topleft',
         c(paste("Dominant version of", mirna), "Another version if applicable"),
         fill = colors)
  options(scipen=sp)
}


stacked.bar.median <- function(mirna, ylim) {
  median.rpm <- data.frame(find.matches(median.rpm.israp, mirna))
  colnames(median.rpm) <- c("iSRAP")
  median.rpm$miRDeep2 <- find.matches(median.rpm.mirdeep, mirna)
  median.rpm$miRge <- c(find.matches(median.rpm.mirge, mirna), 0)
  median.rpm$miRgeModified <- c(find.matches(median.rpm.mirge.noPermissive, mirna), 0)
  median.rpm$MaGiC_noCollapse <- find.matches(median.rpm.magic.no.collapse, mirna)
  median.rpm$MaGiC_MIMAT <- c(find.matches(median.rpm.magic.collapse.mimat, mirna), 0)
  median.rpm$MaGiC_miRBase <- c(find.matches(median.rpm.magic.collapse.mirbase, mirna), 0)
  
  median.rpm <- median.rpm[, order(median.rpm[1, ], decreasing = T)]
  
  colors <- c("dodgerblue4", "cadetblue3")
  pdf(file=paste(out.plots.dir, "/median_rpm_", mirna, ".pdf", sep=""), width=11, height=8.5)
  sp <- getOption('sciPen')
  options(scipen=999)
  bp <- barplot(as.matrix(median.rpm), 
                #beside=T, 
                col = colors,
                ylab = paste("Median RPM across libraries for members of the", mirna, "family"),
                axisnames = F,
                ylim = c(0, ylim))
  text(bp, par("usr")[3], labels = colnames(median.rpm), srt = 30, adj = c(1.1, 1.1), xpd = T)
  axis(2)
  legend('topleft', 
         c(paste("Dominant version of", mirna), "Another version if applicable"), 
         fill = colors)
  dev.off()
  options(scipen=sp)
}

stacked.bar.median("mmu-let-7f-5p", 100000)
stacked.bar.median("mmu-miR-7a-5p", 50)
stacked.bar.median("mmu-miR-5615-5p", 4)
stacked.bar.median("mmu-miR-297a-5p", 4)
stacked.bar.median("mmu-miR-1a-3p", 4)
stacked.bar.median("mmu-miR-16-5p", 20000)
stacked.bar.median("mmu-miR-135a-5p", 200)
stacked.bar.median("mmu-miR-133a-3p", 300)
stacked.bar.median("mmu-let-7a-5p", 30000)




