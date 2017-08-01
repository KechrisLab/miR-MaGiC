
library(dplyr)

setwd(read.length.dist.dir)
files <- list.files(pattern = "^read_len_")

sample_name <- function(file_name) {
  gsub(".txt", "", gsub("read_len_", "", file_name))
}

read <- function(file_name) {
  read.table(file_name, col.names = c("len", sample_name(file_name)))
}

counts <- read(files[1])

for(i in 2:length(files)) {
  ct <- read(files[i])
  counts <- full_join(counts, ct, by = "len")
}

samples <- names(counts)[2:ncol(counts)]

med <- unlist(lapply(samples, function(sample) {
  with(counts, median(rep.int(len, get(sample))))
}))

avg <- unlist(lapply(samples, function(sample) {
  with(counts, mean(rep.int(len, get(sample))))
}))

stddev <- unlist(lapply(samples, function(sample) {
  with(counts, sd(rep.int(len, get(sample))))
}))

avg_len <- data.frame(sample = samples, median = med, mean = avg, stddev = stddev)
avg_len$sample <- gsub("^X", "", avg_len$sample)

rm(i, files, dir, ct, samples, med, avg, stddev)



num_inside_range <- function(min, max) {
  filtered <- counts %>%
    filter(len >= min) %>%
    filter(len <= max) %>%
    select(-len)
  colSums(filtered)
}

pct_inside_range <- function(min, max) {
  filtered <- counts %>%
    filter(len >= min) %>%
    filter(len <= max) %>%
    select(-len)
  colSums(filtered) / colSums(select(counts, -len))
}


