package mirmagic;

import java.io.*;
import java.util.*;


/**
 * Fastq file reader
 * @author prussell
 *
 */
public class FastqParser implements Iterator<FastqSequence>{
	Collection<FastqSequence> sequences;
	File fastqFile;
	BufferedReader reader;
	String nextLine = null;
	
	/**
	 * Empty constructor. Call before setting the file.
	 */
	public FastqParser() {
		super();
	}
	
	/**
	 * Set file and start reader
	 * @param fastqParser The fastq file
	 * @throws IOException
	 */
	public void start(File fastqParser) throws IOException {
		this.fastqFile = fastqParser;
		reader=new BufferedReader(new InputStreamReader(new FileInputStream(fastqFile)));
		nextLine = reader.readLine();
	}
	
	/**
	 * Set reader to the passed reader and start
	 * @param br Reader to set
	 * @throws IOException
	 */
	public void start (BufferedReader br) throws IOException {
		reader=br;
		nextLine = reader.readLine();
	}
	
	public boolean hasNext() {
		return nextLine != null;
	}

	public FastqSequence next() {
		FastqSequence seq = null;
		try{

			
	        if (nextLine  != null) {
        		String firstLine=nextLine;
        		String secondLine=reader.readLine();
				String thirdLine=reader.readLine();
        		String fourthLine=reader.readLine();
        		seq=new FastqSequence(firstLine, secondLine, thirdLine, fourthLine);
	        }
	        nextLine = reader.readLine() ;

		}catch(Exception ex){ 
			System.err.println("Exception thrown while reading fastq file");
		}

		return seq;
	}

	/**
	 * Close the reader
	 * @throws IOException
	 */
	public void close() throws IOException{
		reader.close();
	}

	public void remove() {}
	
}
