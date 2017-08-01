#**************************************
#   Analyze RPM of individual miRNAs
#**************************************

library(scales)

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

hd <- rpm.head(median.rpm.magic.collapse.mirbase, 40)
in_fig_3 <- c("mmu-let-7f-5p", "mmu-let-7a-5p")
other_gps_not_in_fig_3 <- c("mmu-miR-344d-5p", "mmu-miR-92a-5p", "mmu-miR-92a-3p", "mmu-miR-8099", 
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
                            "mmu-let-7a-3p")



# Make boxplot of RPM values for MaGiC_miRBase
library(ggplot2)
library(reshape2)

rpm_magic_mirbase_tidy <- melt(data.frame(rpm.magic.collapse.mirbase) %>% mutate(mirna = rownames(rpm.magic.collapse.mirbase)), "mirna") %>%
  rename(sample = variable) %>%
  rename(rpm = value)
rpm_magic_mirbase_tidy_top <- filter(rpm_magic_mirbase_tidy, mirna %in% rownames(hd))
rpm_magic_mirbase_tidy_bottom <- filter(rpm_magic_mirbase_tidy, !(mirna %in% rownames(hd)))
sum_bottom <- rpm_magic_mirbase_tidy_bottom %>% 
  group_by(sample) %>% 
  summarize(rpm = sum(rpm)) %>% 
  mutate(mirna = "Sum of 1820 remaining")
sum_bottom <- sum_bottom[, c("mirna", "sample", "rpm")]
top_and_sum_bottom <- rbind(rpm_magic_mirbase_tidy_top, sum_bottom)
top_and_sum_bottom$mirna <- factor(top_and_sum_bottom$mirna, levels = rownames(hd), ordered = T)

gp_color <- function(mir) {
  if(mir %in% in_fig_3) {
    "fig3"
  } else if(mir %in% c(other_gps_not_in_fig_3, "Sum of 1820 remaining")) {
    "other_gp"
  } else {
    "neither"
  }
}

pdf(paste(out.plots.dir, "rpm_magic_mirbase_top.pdf", sep="/"), height=8.5, width=11)
top_and_sum_bottom$gp <- as.factor(unlist(lapply(top_and_sum_bottom$mirna, gp_color)))
ggplot(top_and_sum_bottom, aes(mirna, rpm, color=gp)) + 
  geom_boxplot(outlier.colour = NULL) +
  labs(x = "miRNA", y = "RPM by sample") +
  theme(axis.title = element_text(size = 18),
        axis.text.y = element_text(size = 12),
        axis.text.x = element_text(angle = 45, size = 10, hjust = 1),
        legend.position = "none") +
  scale_y_continuous(labels = comma) +
  scale_color_manual(values=c("firebrick1", "gray39", "darkmagenta"))
dev.off()




