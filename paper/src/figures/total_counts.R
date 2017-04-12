#***************************************************
#   Analyze total counts by method and make plots
#***************************************************


# Total estimated counts for each method and each sample
total.count.israp <- unlist(lapply(samples, function(s) colSums(counts.israp)[s]))
total.count.magic.no.collapse <- unlist(lapply(samples, function(s) colSums(counts.magic.no.collapse)[s]))
total.count.magic.collapse.mimat <- unlist(lapply(samples, function(s) colSums(counts.magic.collapse.mimat)[s]))
total.count.magic.collapse.mirbase <- unlist(lapply(samples, function(s) colSums(counts.magic.collapse.mirbase)[s]))
total.count.mirdeep <- unlist(lapply(samples, function(s) colSums(counts.mirdeep)[s]))
total.count.mirge <- unlist(lapply(samples, function(s) colSums(counts.mirge, na.rm=T)[s]))
total.count.mirge.noPermissive <- unlist(lapply(samples, function(s) colSums(counts.mirge.noPermissive, na.rm=T)[s]))
total.count.all <- NULL
total.count.all$iSRAP <- total.count.israp
total.count.all$MaGiC_noCollapse <- total.count.magic.no.collapse
total.count.all$MaGiC_MIMAT <- total.count.magic.collapse.mimat
total.count.all$MaGiC_miRBase <- total.count.magic.collapse.mirbase
total.count.all$miRDeep2 <- total.count.mirdeep
total.count.all$miRge <- total.count.mirge
total.count.all$miRgeModified <- total.count.mirge.noPermissive
total.count.all <- data.frame(total.count.all)

# Total number of filtered reads per sample
filtered.reads <- unlist(lapply(samples.without.X, function(s) metadata[s, "Filtered_read_count"]))

# Transparent colors
transparentColor <- function(color, opacity) {
  rgbVal <- col2rgb(color)
  rgb(rgbVal["red", 1], rgbVal["green", 1], rgbVal["blue", 1], opacity, maxColorValue = 255)
}

# Scatter plot of total count vs input number of filtered reads
scatterCountVsReads <- function(xymax, outPdf) {
  pdf(file=paste(out.plots.dir, outPdf, sep="/"), width=11, height=8.5)
  mar.default <- c(5,4,4,2) + 0.1
  par(mar = mar.default + c(0, 2, 0, 0))
  colors = c("purple3", "forestgreen", "deepskyblue", "blue", "lightsalmon1", "red")
  plot(rep(filtered.reads, 6), 
       c(total.count.israp, total.count.mirdeep, total.count.mirge, total.count.mirge.noPermissive, total.count.magic.no.collapse, 
         total.count.magic.collapse.mirbase),
       xlab="Number of clipped, filtered reads",
       ylab="Estimated total read count from quantification method", 
       col=unlist(lapply(c(rep(colors[1], length(samples)), rep(colors[2], length(samples)), 
                           rep(colors[3], length(samples)), rep(colors[4], length(samples)),
                           rep(colors[5], length(samples)), rep(colors[6], length(samples))), 
                         function(x) transparentColor(x, 80))),
       pch=16, 
       #cex=0.75,
       cex.lab=2,
       xlim = c(0, xymax),
       ylim = c(0, xymax)
  )
  abline(0, 0.6)
  abline(0, 0.8)
  abline(0, 1)
  xlim <- par("usr")[2]
  text(x=0.9*xlim, y=0.95*xlim, labels="1.0", cex=1.5)
  text(x=0.965*xlim, y=0.83*xlim, labels="0.8", cex=1.5)
  text(x=0.965*xlim, y=0.63*xlim, labels="0.6", cex=1.5)
  legend('topleft', 
         c("iSRAP", "miRDeep2", "miRge", "miRgeModified", "MaGiC_noCollapse", "MaGiC_miRBase"), 
         col=colors,
         cex=1.5, pch=16)
  dev.off()
}

scatterCountVsReads(85000000, "total_count_vs_filtered_reads.pdf")
scatterCountVsReads(40000000, "total_count_vs_filtered_reads_zoom.pdf")


# Sample rank for number of input reads and total estimated count
rnk <- data.frame(row.names = samples.without.X)
rnk$Filtered_reads <- rank(filtered.reads)
rnk$MagicNoCollapse <- rank(total.count.magic.no.collapse)
rnk$MagicCollapseMimat <- rank(total.count.magic.collapse.mimat)
rnk$MagicCollapseMirbase <- rank(total.count.magic.collapse.mirbase)
rnk$iSRAP <- rank(total.count.israp)
rnk$miRDeep <- rank(total.count.mirdeep)
rnk$miRge <- rank(total.count.mirge)
rnk$miRgeModified <- rank(total.count.mirge.noPermissive)
rnk.sorted <- rnk[order(-rnk$Filtered_reads), ]

# Heat map of rank shuffling
pdf(file=paste(out.plots.dir, "sample_rank_total_count.pdf", sep="/"), width=11, height=8.5)
image(z=t(as.matrix(-rnk.sorted)[ncol(counts.magic.no.collapse):1,]), col=heat.colors(ncol(counts.magic.no.collapse)), xaxt='n',
      ylab= "Low count                                             High count",
      main="Samples ranked by total count")
text(x=0, y=0.02, labels="Reads")
text(x=1/7, y=0.02, labels="magicNoCollapse")
text(x=2/7, y=0.02, labels="magicMimat")
text(x=3/7, y=0.02, labels="magicMirbase")
text(x=4/7, y=0.02, labels="iSRAP")
text(x=5/7, y=0.02, labels="miRDeep2")
text(x=6/7, y=0.02, labels="miRge")
text(x=1, y=0.02, labels="miRgeModified")
dev.off()

# Squared difference between ranks of input reads and output total count
square.error <- function(total.counts) {
  se <- (rank(filtered.reads) - rank(total.counts))^2
  se <- se[which(!(names(se) %in% c("15_GTGAAA_4_LXS99", "18_GTGGCC_4_LXS99", "23_GTTTCG_4_LXS14",
                                    "171_ACAGTG_5_LXS115", "25_GAGTGG_4_LXS26", "24_CGTACG_4_LXS22")))]
  se
}

# Mean squared difference between ranks of input reads and output total count
mean.square.error <- function(total.counts) {
  se <- square.error(total.counts)
  mse <-(sum(se)/length(se))
  mse
}

# Square root of mean squared difference between ranks of input reads and output total count
sqrt.mean.square.error <- function(total.counts) {sqrt(mean.square.error(total.counts))}

rank.chg <- function(total.counts) {abs(rank(filtered.reads) - rank(total.counts))}

rankChg <- data.frame(rank.chg(total.count.magic.no.collapse))
colnames(rankChg) <- c("magicNoCollapse")
rankChg$magicCollapseMimat <- rank.chg(total.count.magic.collapse.mimat)
rankChg$magicCollapseMirbase <- rank.chg(total.count.magic.collapse.mirbase)
rankChg$israp <- rank.chg(total.count.israp)
rankChg$mirdeep <- rank.chg(total.count.mirdeep)
rankChg$mirge <- rank.chg(total.count.mirge)
rankChg$mirgeModified <- rank.chg(total.count.mirge.noPermissive)

# Make scatter plot for a single method of sample rank before and after quantitation
scttr <- function(total.counts, name, out.name) {
  
  mse <- sqrt.mean.square.error(total.counts)
  pdf(file=paste(out.plots.dir, out.name, sep="/"), width=11, height=8.5)
  mar.default <- c(5,4,4,2) + 0.1
  par(mar = mar.default + c(0, 2, 0, 1))
  plot(rank(filtered.reads), rank(total.counts), 
       xlab="Rank of clipped, filtered reads for sample",
       ylab=paste("Rank of total counts for", name), 
       main=paste("Rank of total counts vs. rank of reads sequenced:", name),
       pch=16, 
       cex=0.75,
       cex.lab=2,
       cex.main=1.5)
  xlim <- par("usr")[2]
  ylim <- par("usr")[4]
  text(x=0.3*xlim, y=0.8*ylim, labels=paste("sqrt(MSE) (without 6 outliers) =", format(mse, digits=4)), cex=1.5)
  abline(0, 1)
  dev.off()
  
}

scttr(total.count.magic.no.collapse, "miR-MaGiC no collapse", "total_count_vs_filtered_reads_rank_magic_no_collapse.pdf")
scttr(total.count.magic.collapse.mimat, "miR-MaGiC collapse MIMAT", "total_count_vs_filtered_reads_rank_magic_collapse_mimat.pdf")
scttr(total.count.magic.collapse.mirbase, "miR-MaGiC collapse miRBase", "total_count_vs_filtered_reads_rank_magic_collapse_mirbase.pdf")
scttr(total.count.israp, "iSRAP", "total_count_vs_filtered_reads_rank_iSRAP.pdf")
scttr(total.count.mirdeep, "miRDeep", "total_count_vs_filtered_reads_miRDeep.pdf")
scttr(total.count.mirge, "miRge", "total_count_vs_filtered_reads_miRge.pdf")
scttr(total.count.mirge.noPermissive, "miRgeModified", "total_count_vs_filtered_reads_miRge_no_permissive.pdf")

# Paired t test of square errors
sq.err.israp <- as.vector(square.error(data.frame(total.count.all[,"iSRAP"], row.names = rownames(total.count.all))))
sq.err.mirdeep <- as.vector(square.error(data.frame(total.count.all[,"miRDeep2"], row.names = rownames(total.count.all))))
sq.err.mirge <- as.vector(square.error(data.frame(total.count.all[,"miRge"], row.names = rownames(total.count.all))))
sq.err.mirgeModified <- as.vector(square.error(data.frame(total.count.all[,"miRgeModified"], row.names = rownames(total.count.all))))
sq.err.magicNoCollapse <- as.vector(square.error(data.frame(total.count.all[,"MaGiC_noCollapse"], row.names = rownames(total.count.all))))
sq.err.magicMimat <- as.vector(square.error(data.frame(total.count.all[,"MaGiC_MIMAT"], row.names = rownames(total.count.all))))
sq.err.magicMirbase <- as.vector(square.error(data.frame(total.count.all[,"MaGiC_miRBase"], row.names = rownames(total.count.all))))

ttest.pval.mirgeModified.mirgeModified <- t.test(sq.err.mirgeModified, sq.err.mirgeModified, paired=T)$p.value
ttest.pval.mirgeModified.israp <- t.test(sq.err.mirgeModified, sq.err.israp, paired=T)$p.value
ttest.pval.mirgeModified.mirdeep <- t.test(sq.err.mirgeModified, sq.err.mirdeep, paired=T)$p.value
ttest.pval.mirgeModified.mirge <- t.test(sq.err.mirgeModified, sq.err.mirge, paired=T)$p.value
ttest.pval.mirgeModified.magicNoCollapse <- t.test(sq.err.mirgeModified, sq.err.magicNoCollapse, paired=T)$p.value
ttest.pval.mirgeModified.magicMimat <- t.test(sq.err.mirgeModified, sq.err.magicMimat, paired=T)$p.value
ttest.pval.mirgeModified.magicMirbase <- t.test(sq.err.mirgeModified, sq.err.magicMirbase, paired=T)$p.value

# Bar plot of mean squared error with error bars
bar_plot_mse <- function(counts, file, title, ymax) {
  mean.all <- apply(counts, 2, mean.square.error)
  se.all <- apply(counts, 2, function(x) sd(square.error(x)) / sqrt(length(x)))
  ord <- order(mean.all)
  mean.all <- mean.all[ord]
  se.all <- se.all[ord]
  
  pdf(file=paste(out.plots.dir, file, sep="/"), width=11, height=8.5)
  op <- par(mar=c(4,5,2,2))
  plt <- barplot(mean.all, 
                 col=c("cornflowerblue"), 
                 ylim=c(0,ymax), 
                 cex.main=1.5,
                 cex.names=0.85,
                 ylab=title,
                 cex.lab=1.5)
  text(x=plt + 0.25, y=mean.all+10, labels=as.character(round(mean.all, digits=1)))
  text(x=plt[6:7] - 0.15, y=mean.all[6:7]+10, labels="*", cex=4, col="red")
  segments(plt, mean.all - se.all, plt, mean.all + se.all, lwd = 1.5)
  arrows(plt, mean.all - se.all, plt, mean.all + se.all, lwd = 1.5, angle = 90, code = 3, length = 0.05)
  dev.off()
  par(op)
}

bar_plot_mse(total.count.all, "mse_all_total_count.pdf", "Mean square error of total estimated count vs. input filtered reads", ymax = 350)


