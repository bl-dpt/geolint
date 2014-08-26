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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
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

import uk.bl.dpt.qa.geo.wrappers.TikaWrapper;
import uk.bl.dpt.utils.checksum.ChecksumUtil;

/**
 * This class generates an XML manifest of data about the files
 * It expects to be passed a directory listing of the files to process
 *   
 * @author wpalmer
 */
public class ManifestGenHadoop extends Configured implements Tool {

	private static FileSystem gFS = null;
	
	/**
	 * Process the pdf/epub
	 * @author wpalmer
	 */
	public static class ManifestGenMap extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
		
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
						
		}

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			super.close();
			
		}

		@Override
		public void map(LongWritable pKey, Text pValue,
				OutputCollector<Text, Text> pCollector, Reporter pReporter)
						throws IOException {

			try {
				
				final Path p = new Path(pValue.toString());
				final FileStatus fStatus = gFS.getFileStatus(p);
				BufferedInputStream input = new BufferedInputStream(gFS.open(p));
				input.mark((int)fStatus.getLen());
				
				final String tikaType = TikaWrapper.getMimetype(input);
				
				input.reset();
				
				final Map<String, String> checksums = new HashMap<String, String>();
				ChecksumUtil.calcChecksums(input, checksums);
				
				input.close();
				
				// output a string of XML here
				String out = "";
				out += "<file>";
				out += "<name>"+pValue.toString()+"</name>";
				final File file = new File(pValue.toString());
				if(file.getName().contains(".")) {
					final String ext = file.getName().substring(file.getName().lastIndexOf('.')+1).toLowerCase();
					out += "<ext>"+ext+"</ext>";
				}
				out += "<size>"+fStatus.getLen()+"</size>";
//				out += "<lastModified>"+gFile.lastModified()+"</lastModified>";
				for(String s:checksums.keySet()) {
					out += "<checksum digest=\""+s+"\">"+checksums.get(s)+"</checksum>";
				}
				out += "<tika>"+tikaType+"</tika>";
//				out += "<processingTimeMS>"+runtime+"</processingTimeMS>");
				out += "</file>";
				
				pCollector.collect(new Text(out), new Text(""));
				
			} catch(Exception e) {
				pCollector.collect(new Text("Exception: "+pValue.toString()+" "+e.getMessage()), new Text(""));
			}

		}

	}
	
	/* (non-Javadoc)
	 * @see org.apache.hadoop.util.Tool#run(java.lang.String[])
	 */
	@Override
	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		JobConf conf = new JobConf(ManifestGenHadoop.class);
		
		// String to use for name and output folder in HDFS
		String name = "ManifestGenHadoop_"+System.currentTimeMillis();
		
		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(name));
		
		conf.setJobName(name);
		
		//set the mapper to this class' mapper
		conf.setMapperClass(ManifestGenMap.class);
		
		//this input format should split the input by one line per map by default.
		conf.setInputFormat(NLineInputFormat.class);
		// When this was 200 a job took 22 mins (230k pdfs)
		// When this was 1000 the same job took 16 mins
		conf.setInt("mapred.line.input.format.linespermap", 1000);
		
		//sets how the output is written cf. OutputFormat
		conf.setOutputFormat(TextOutputFormat.class);

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);
		
		//we only want one reduce task
		conf.setNumReduceTasks(1);
		
		JobClient.runJob(conf);
		
		return 0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ToolRunner.run(new ManifestGenHadoop(), args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
