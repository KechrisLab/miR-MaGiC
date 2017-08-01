#***************************************************
#   Analyze total counts by method and make plots
#***************************************************


# Total estimated counts for each method and each sample
total.count.israp <- unlist(lapply(samples.without.X, function(s) colSums(counts.israp)[s]))
total.count.magic.no.collapse <- unlist(lapply(samples.without.X, function(s) colSums(counts.magic.no.collapse)[s]))
total.count.magic.collapse.mimat <- unlist(lapply(samples.without.X, function(s) colSums(counts.magic.collapse.mimat)[s]))
total.count.magic.collapse.mirbase <- unlist(lapply(samples.without.X, function(s) colSums(counts.magic.collapse.mirbase)[s]))
total.count.mirdeep <- unlist(lapply(samples.without.X, function(s) colSums(counts.mirdeep)[s]))
total.count.mirge <- unlist(lapply(samples.without.X, function(s) colSums(counts.mirge, na.rm=T)[s]))
total.count.mirge.noPermissive <- unlist(lapply(samples.without.X, function(s) colSums(counts.mirge.noPermissive, na.rm=T)[s]))
total.count.all <- NULL
total.count.all$iSRAP <- total.count.israp
total.count.all$MaGiC_noCollapse <- total.count.magic.no.collapse
total.count.all$MaGiC_MIMAT <- total.count.magic.collapse.mimat
total.count.all$MaGiC_miRBase <- total.count.magic.collapse.mirbase
total.count.all$miRDeep2 <- total.count.mirdeep
total.count.all$miRge <- total.count.mirge
total.count.all$miRgeModified <- total.count.mirge.noPermissive
total.count.all <- data.frame(total.count.all)

# Total number of raw reads per sample
raw.reads <- unlist(lapply(samples.without.X, function(s) metadata[s, "Raw_read_count"]))

# Transparent colors
transparentColor <- function(color, opacity) {
  rgbVal <- col2rgb(color)
  rgb(rgbVal["red", 1], rgbVal["green", 1], rgbVal["blue", 1], opacity, maxColorValue = 255)
}

# Scatter plot of total count vs input number of raw reads
scatterCountVsReads <- function(xymax) {
  mar.default <- c(5,4,4,2) + 0.1
  par(mar = mar.default + c(0, 2, 0, 0))
  colors = c("purple3", "forestgreen", "deepskyblue", "blue", "lightsalmon1", "red")
  plot(rep(raw.reads, 6),
       c(total.count.israp, total.count.mirdeep, total.count.mirge, total.count.mirge.noPermissive, total.count.magic.no.collapse,
         total.count.magic.collapse.mirbase),
       xlab="Number of raw reads",
       ylab="Total output read count from method",
       col=unlist(lapply(c(rep(colors[1], length(samples.without.X)), rep(colors[2], length(samples.without.X)),
                           rep(colors[3], length(samples.without.X)), rep(colors[4], length(samples.without.X)),
                           rep(colors[5], length(samples.without.X)), rep(colors[6], length(samples.without.X))),
                         function(x) transparentColor(x, 80))),
       pch=16,
       #cex=0.75,
       cex.lab=1.5,
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
         cex=1.3, pch=16)
}

pdf(file=paste(out.plots.dir, "scatter_counts_vs_total_reads.pdf", sep="/"), width=7, height=11)
par(mfcol=c(2,1))
scatterCountVsReads(85000000)
scatterCountVsReads(40000000)
dev.off()


# MSE
se <- function(v1, v2) {
  (v1 - v2) ^ 2
}
mse <- function(v1, v2) {
  se <- se(v1, v2)
  sum(se) / length(se)
}

# Paired t test of square errors
sq.err.israp <- se(total.count.all$iSRAP, num_19_23)
sq.err.mirdeep <- se(total.count.all$miRDeep2, num_19_23)
sq.err.mirge <- se(total.count.all$miRge, num_19_23)
sq.err.mirgeModified <- se(total.count.all$miRgeModified, num_19_23)
sq.err.magicNoCollapse <- se(total.count.all$MaGiC_noCollapse, num_19_23)
sq.err.magicMimat <- se(total.count.all$MaGiC_MIMAT, num_19_23)
sq.err.magicMirbase <- se(total.count.all$MaGiC_miRBase, num_19_23)

ttest.pval.mirgeModified.mirgeModified <- t.test(sq.err.mirgeModified, sq.err.mirgeModified, paired=T)$p.value
ttest.pval.mirgeModified.israp <- t.test(sq.err.mirgeModified, sq.err.israp, paired=T)$p.value
ttest.pval.mirgeModified.mirdeep <- t.test(sq.err.mirgeModified, sq.err.mirdeep, paired=T)$p.value
ttest.pval.mirgeModified.mirge <- t.test(sq.err.mirgeModified, sq.err.mirge, paired=T)$p.value
ttest.pval.mirgeModified.magicNoCollapse <- t.test(sq.err.mirgeModified, sq.err.magicNoCollapse, paired=T)$p.value
ttest.pval.mirgeModified.magicMimat <- t.test(sq.err.mirgeModified, sq.err.magicMimat, paired=T)$p.value
ttest.pval.mirgeModified.magicMirbase <- t.test(sq.err.mirgeModified, sq.err.magicMirbase, paired=T)$p.value


# Bar plot of mean squared error with error bars
bar_plot_mse <- function(counts, reads, file, title, ymax) {
  mean.all <- apply(counts, 2, function(col) mse(col, reads))
  se.all <- apply(counts, 2, function(col) sd(se(col, reads)) / sqrt(length(col)))
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
  text(x=plt + 0.35, y=mean.all + 0.5e13, labels=format(mean.all, scientific = T, digits = 2))
  segments(plt, mean.all - se.all, plt, mean.all + se.all, lwd = 1.5)
  arrows(plt, mean.all - se.all, plt, mean.all + se.all, lwd = 1.5, angle = 90, code = 3, length = 0.05)
  dev.off()
  par(op)
}

num_19_23 <- num_inside_range(19, 23)
names(num_19_23) <- gsub("^X", "", names(num_19_23))
num_19_23 <- num_19_23[samples.without.X]

bar_plot_mse(total.count.all,
             num_19_23,
             "mse_all_total_count.pdf",
             "MSE of total counts vs. number of input fragments between 19-23nt",
             ymax = 1.6e14)





