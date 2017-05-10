package variant;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import mirmagic.Sequence;


public class FastaWriter {
	
	/**
	 * Write a collection of sequences to a fasta file
	 * @param seqs Sequences to write
	 * @param fileName File to write to
	 * @param basesPerLine Number of sequence bases per line in the output file
	 */
	public static void writeToFile(Collection<Sequence> seqs, String fileName, int basesPerLine) {
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(fileName));
			for(Sequence seq : seqs) {
				write(seq, bw, basesPerLine);
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * Write one sequence to an output stream
	 * @param seq Sequence
	 * @param bw Buffered writer
	 * @param lineLength Bases per line
	 * @throws IOException
	 */
	private static void write(Sequence seq, BufferedWriter bw, int lineLength) throws IOException {
		
		StringBuilder sequenceBuilder = new StringBuilder(seq.getSequenceBases());
		
		if(seq == null || sequenceBuilder.length() == 0) {
			return;
		}
		if(sequenceBuilder.length() == 0) {
			return;
		}

		int currentIndex = 0;
		bw.write(">" + seq.getName());
		bw.newLine();
		while(currentIndex < sequenceBuilder.length()) {
			int toWrite = Math.min(lineLength, sequenceBuilder.length() - currentIndex) - 1;
			bw.write(sequenceBuilder.substring(currentIndex, currentIndex + toWrite + 1));
			bw.newLine();
			currentIndex = currentIndex + toWrite + 1;
		}
	}

	
}