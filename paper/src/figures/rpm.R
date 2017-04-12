#**************************************
#   Analyze RPM of individual miRNAs
#**************************************


rpm <- function(counts) {
  rtrn <- apply(counts, 2, function(x) {
    sc <- sum(x, na.rm=T) / 1000000
    x / sc
  })
  rtrn[is.na(rtrn)] <- 0
  rtrn
}

median.rpm <- function(counts) {
  df <- data.frame(apply(rpm(counts), 1, median))
  ord <- order(df[,1], decreasing = T)
  rn <- rownames(df)[ord]
  df <- data.frame(df[ord,1])
  rownames(df) <- rn
  colnames(df) <- "median_rpm"
  df
}

median.rpm.israp <- median.rpm(counts.israp)
median.rpm.magic.no.collapse <- median.rpm(counts.magic.no.collapse)
median.rpm.magic.collapse.mimat <- median.rpm(counts.magic.collapse.mimat)
median.rpm.magic.collapse.mirbase <- median.rpm(counts.magic.collapse.mirbase)
median.rpm.mirge <- median.rpm(counts.mirge)
median.rpm.mirge.noPermissive <- median.rpm(counts.mirge.noPermissive)
median.rpm.mirdeep <- median.rpm(counts.mirdeep)

rpm.israp <- rpm(counts.israp)
rpm.magic.no.collapse <- rpm(counts.magic.no.collapse)
rpm.magic.collapse.mimat <- rpm(counts.magic.collapse.mimat)
rpm.magic.collapse.mirbase <- rpm(counts.magic.collapse.mirbase)
rpm.mirge <- rpm(counts.mirge)
rpm.mirge.noPermissive <- rpm(counts.mirge.noPermissive)
rpm.mirdeep <- rpm(counts.mirdeep)

which.name.contains <- function(df, name) {
  df[which(grepl(name, rownames(df))), ]
}

sum.name.contains <- function(df, name) {
  sum(which.name.contains(df, name))
}

rpm.head <- function(rpm, n) {
  r <- rpm[1:n, 1]
  nm <- rownames(rpm)[1:n]
  rest <- sum(rpm[n+1:nrow(rpm), 1], na.rm=T)
  r <- c(r, rest)
  nrest <- 1860 - n
  nm <- c(nm, paste("Sum of", nrest, "remaining"))
  df <- data.frame(r)
  rownames(df) <- nm
  colnames(df) <- "median_rpm"
  df
}

pdf(paste(out.plots.dir, "median_rpm_magic_mirbase_top.pdf", sep="/"), height=8.5, width=11)
hd <- rpm.head(median.rpm.magic.collapse.mirbase, 40)
bp <- barplot(
        hd[,1],
        #las=1,
        ylab="Median RPM across libraries by MaGiC_miRBase",
        col="skyblue4"
  )
text(bp, 
     par("usr")[3], 
     labels = rownames(hd), 
     srt = 45, 
     adj = c(1.1, 1.1), 
     xpd = T, 
     cex=0.7
     )
red <- which(rownames(hd) %in% c("mmu-let-7f-5p", "mmu-let-7a-5p"))
black <- which(rownames(hd) %in% c("mmu-miR-344d-5p", "mmu-miR-92a-5p", "mmu-miR-92a-3p", "mmu-miR-8099", 
                                   "mmu-miR-7a-5p", "mmu-miR-7a-3p", "mmu-miR-7676-5p", "mmu-miR-7676-3p", 
                                   "mmu-miR-6967-5p", "mmu-miR-6967-3p", "mmu-miR-692", "mmu-miR-684", "mmu-miR-680", 
                                   "mmu-miR-669d-3p", "mmu-miR-5615-5p", "mmu-miR-5615-3p", "mmu-miR-450a-3p", 
                                   "mmu-miR-365-5p", "mmu-miR-3471", "mmu-miR-3102-5p", "mmu-miR-3102-3p", "mmu-miR-30c-5p", 
                                   "mmu-miR-30c-3p", "mmu-miR-3074-3p", "mmu-miR-3070-3p", "mmu-miR-29b-5p", "mmu-miR-29b-3p", 
                                   "mmu-miR-297a-5p", "mmu-miR-26a-3p", "mmu-miR-24-5p", "mmu-miR-219a-3p", "mmu-miR-218-3p", 
                                   "mmu-miR-1a-5p", "mmu-miR-1a-3p", "mmu-miR-19b-5p", "mmu-miR-19b-3p", "mmu-miR-199a-5p", 
                                   "mmu-miR-196a-3p", "mmu-miR-194-3p", "mmu-miR-1906", "mmu-miR-181b-3p", "mmu-miR-181a-3p", 
                                   "mmu-miR-16-5p", "mmu-miR-16-3p", "mmu-miR-138-3p", "mmu-miR-135a-5p", "mmu-miR-135a-3p", 
                                   "mmu-miR-133a-5p", "mmu-miR-133a-3p", "mmu-miR-129-5p", "mmu-miR-129-3p", "mmu-miR-128-5p", 
                                   "mmu-miR-128-3p", "mmu-miR-125b-3p", "mmu-let-7f-3p", "mmu-let-7c-3p", 
                                   "mmu-let-7a-3p"))
text(x=bp[red]+0.1, y=hd[red,1]+2500, labels="*", cex=2.5, col="red")
last <- 59 - (length(red)+length(black))
black <- c(black, 41)
text(x=bp[black]+0.1, y=hd[black,1]+2500, labels="*", cex=2.5, col="black")
text(x=bp[41]+1.5, y=hd[41,1]+2500, labels=paste("(", last, ")", sep=""), col="black")
dev.off()




