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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.NLineInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import uk.bl.dpt.qa.geo.CheckResult;
import uk.bl.dpt.qa.geo.GeoLint;
import uk.bl.dpt.qa.geo.Tools;
import uk.bl.dpt.utils.checksum.ChecksumUtil;
import uk.bl.dpt.utils.util.FileUtil;

/**
 * @author wpalmer
 */
public class GeoLintHadoop extends Configured implements Tool {

	/**
	 * Process the pdf/epub
	 * @author wpalmer
	 */
	public static class GeoLintMap extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {

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
		public void map(LongWritable key, Text value,
				OutputCollector<Text, Text> collector, Reporter reporter)
						throws IOException {

			try {
				// load file from HDFS
				final Path hdfsFile = new Path(value.toString());
				final File localFile = new File(gTempDir.getAbsolutePath()+"/"+hdfsFile.getName());
				
				InputStream in = gFS.open(hdfsFile);
				// force overwrite
				final boolean overwrite = true;
				Map<String, String> checksums = new HashMap<String, String>();
				ChecksumUtil.copyAndChecksum(in, checksums, localFile, overwrite);
				//gFS.copyToLocalFile(hdfsFile, new Path(localFile.getAbsolutePath()));
				
				// Close the stream in case it leaves a stale handle - we no longer need it
				in.close();
				
				// will only ever be passed one file
				final CheckResult checkresult = gLint.check(localFile, false).get(0);
				// set the filename to the original one in HDFS (without hdfs server name etc)
				checkresult.setFilename(hdfsFile.toString().substring(ROOT_LEN));
				checkresult.setChecksums(checksums);

				// clean up
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
		
		JobConf conf = new JobConf(GeoLintHadoop.class);
		
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
		
		//this input format should split the input by one line per map by default.
		conf.setInputFormat(NLineInputFormat.class);
		conf.setInt("mapred.line.input.format.linespermap", 2000);
		
		//sets how the output is written cf. OutputFormat
		conf.setOutputFormat(TextOutputFormat.class);

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);
		
		//we only want 28 reduce tasks as we have 28 reduce slots
		conf.setNumReduceTasks(28);
		
		JobClient.runJob(conf);
		
		return 0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ToolRunner.run(new GeoLintHadoop(), args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
