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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileAsBinaryInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import uk.bl.dpt.qa.geo.CheckResult;
import uk.bl.dpt.qa.geo.GeoLint;
import uk.bl.dpt.qa.geo.Tools;
import uk.bl.dpt.utils.checksum.ChecksumUtil;
import uk.bl.dpt.utils.util.FileUtil;

/**
 * GeoLint MapReduce app - uses data from a sequencefile
 * @author wpalmer
 */
public class GeoLintHadoopSeqFile extends Configured implements Tool {

	/**
	 * Process the pdf/epub
	 * @author wpalmer
	 */
	public static class GeoLintMap extends MapReduceBase implements Mapper<Text, BytesWritable, Text, Text> {

		private static FileSystem gFS = null;
		private static File gTempDir = null;
		private static GeoLint gLint = null;
		
		// length of initial part of path containing server name etc
		private int ROOT_LEN;
		
		@Override
		public void configure(JobConf job) {
			// TODO Auto-generated method stub
			super.configure(job);
			try {
				gFS = FileSystem.get(job);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			gTempDir = Tools.newTempDir();
			
			gLint = new GeoLint();
			
			ROOT_LEN = new Path("/").toString().length()-1;
			
		}

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			super.close();
			
			// clean up temp dir
			FileUtil.deleteDirectory(gTempDir);
			
		}

		@Override
		public void map(Text key, BytesWritable value,
				OutputCollector<Text, Text> collector, Reporter reporter)
						throws IOException {

			try {

				// load file from HDFS
				String sourceFile;
				InputStream in;
				
				// this is a hack to determine if we should load the data from the sequencefile
				// or recover it from HDFS.  This assumes that the non-sequencefile data (i.e. large
				// files > SequenceFileLoader.MAX_SIZE) are loaded into HDFS instead of being added
				// this is because some files are >4GB and access to BytesWritable data is via data array 
				// so >4GB data first buffered into RAM is not ideal.  Therefore - store large files outside
				// the sequencefile and load them via reference
				if(key.toString().equals(SequenceFileLoader.BUFTOOLARGE)) {
					// copy the file over the network
					// use copyBytes to ensure data is correct length
					// FIXME: change path to HDFS path (i.e. get rid of preceding path if necessary)
					sourceFile = String.valueOf(value.copyBytes());
					in = gFS.open(new Path(sourceFile));
				} else {
					// dump the file into a temp dir from the sequence file
					// use copyBytes to ensure data is correct length
					sourceFile = key.toString();
					in = new ByteArrayInputStream(value.copyBytes());
				}

				// copy the data
				boolean overwrite = true;
				Map<String, String> checksums = new HashMap<String, String>();
				final File localFile = new File(gTempDir.getAbsolutePath()+"/"+new File(sourceFile).getName());
				ChecksumUtil.copyAndChecksum(in, checksums, localFile, overwrite);
				
				// will only ever be passed one file
				final CheckResult checkresult = gLint.check(localFile, false).get(0);
				// set the filename to the original one
				// FIXME: sanitise path here??
				checkresult.setFilename(sourceFile);//.toString().substring(ROOT_LEN));
				checkresult.setChecksums(checksums);

				// clean up
				// FIXME: files not getting deleted due to localFile.exists() check failing
				if(!localFile.getAbsoluteFile().delete()) {
					System.err.println("File not deleted: "+localFile);
				}

				collector.collect(new Text(checkresult.toXML()), new Text(""));
				
			} catch(Exception e) {
				collector.collect(new Text("Exception: "+value.toString()+" "+e.getMessage()), new Text(""));
			}

		}

	}

	/* (non-Javadoc)
	 * @see org.apache.hadoop.util.Tool#run(java.lang.String[])
	 */
	@Override
	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		JobConf conf = new JobConf(GeoLintHadoopSeqFile.class);
		
		// String to use for name and output folder in HDFS
		String name = "GeoLintHadoop_"+System.currentTimeMillis();
		
		// set a timeout to 30 mins as we may transfer and checksum ~4gb files
		conf.set("mapred.task.timeout", Integer.toString(30*60*1000));
		
		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(name));
		
		conf.setJobName(name);
		
		//set the mapper to this class' mapper
		conf.setMapperClass(GeoLintMap.class);
		//conf.setReducerClass(GeoLintReduce.class);
		
		conf.setInputFormat(SequenceFileAsBinaryInputFormat.class);
		
		//sets how the output is written cf. OutputFormat
		conf.setOutputFormat(TextOutputFormat.class);

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);
		
		//we only want one reduce task
		conf.setNumReduceTasks(28);
		
		JobClient.runJob(conf);
		
		return 0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ToolRunner.run(new GeoLintHadoopSeqFile(), args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
