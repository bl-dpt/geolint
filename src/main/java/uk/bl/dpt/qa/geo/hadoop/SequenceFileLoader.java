/*
 * Copyright 2014 The British Library/SCAPE Project Consortium
 * Author: William Palmer (William.Palmer@bl.uk)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package uk.bl.dpt.qa.geo.hadoop;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.mapred.JobConf;

/**
 * Create a sequence file of data for processing (key: filename, value: data)
 * Note: data will still need to be saved locally (will it?)
 * @author wpalmer
 *
 */
public class SequenceFileLoader {

	private static long gCount = 0;
	final static int MAX_SIZE=1024*1024*384; //384MB
	final static Text BUFTOOLARGE = new Text("BUFTOOLARGEBUFTOOLARGEBUFTOOLARGEBUFTOOLARGEBUFTOOLARGE");
	
	/**
	 * Loads a directory of data into a SequenceFile
	 * Note: references are inserted for large files due to sequencefile limitations  
	 * @param pDir directory containing files to load into the sequencefile (all contents loaded, recursively)
	 * @param pSequenceFileName filename of sequence file that is placed into your HDFS home directory
	 * @throws IOException in case of error
	 */
	public static void loadDir(File pDir, String pSequenceFileName) throws IOException {
		
		JobConf gConf = new JobConf();

		Path seqFile = new Path(pSequenceFileName+".seqfile");
		Writer seq = SequenceFile.createWriter(gConf, 	Writer.compression(CompressionType.BLOCK),
														Writer.file(seqFile),
														Writer.keyClass(Text.class),
														Writer.valueClass(BytesWritable.class));
		
		traverseAdd(seq, pDir);
		
	}
	
	/**
	 * Traverse the directory and add files to the sequencefile
	 * @param seq sequencefile
	 * @param pFile
	 */
	private static void traverseAdd(Writer seq, File pFile) {
		
		if(pFile.isDirectory()) {
			for(File file:pFile.listFiles()) {
				traverseAdd(seq, file);
			}
		} else {
			try {
				addFile(seq, pFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	private static void addFile(Writer seq, File pFile) throws IOException {
		
		gCount++;
		
		final Text textFile = new Text(pFile.getAbsolutePath());
		
		if(pFile.length()>MAX_SIZE) {
			System.out.println();
			System.out.println("File too large: "+pFile+" (added ref to seqfile)");
			
			// TODO: If the files are not already in HDFS then they should be loaded in here
			
			seq.append(BUFTOOLARGE, textFile);
			
		} else {
			
			byte[] buf = IOUtils.toByteArray(new FileInputStream(pFile));
			BytesWritable bw = new BytesWritable(buf);
			seq.append(textFile, bw);
			
		}
		
		if(0==gCount%1000) System.out.print("1k ");
				
	}

	/**
	 * Test main method to load current directory into a sequencefile
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			loadDir(new File("."), "sequencefile");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
