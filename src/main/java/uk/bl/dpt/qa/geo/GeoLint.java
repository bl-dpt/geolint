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
package uk.bl.dpt.qa.geo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.bl.dpt.qa.geo.formats.Format;
import uk.bl.dpt.qa.geo.formats.GMLFormat;
import uk.bl.dpt.qa.geo.formats.NTFFormat;
import uk.bl.dpt.utils.checksum.ChecksumUtil;
import uk.bl.dpt.utils.util.FileUtil;

// Sample data download from here: https://www.ordnancesurvey.co.uk/business-and-government/licensing/sample-data/discover-data.html

/**
 * Tool for validating geospatial data
 * @author wpalmer
 */
public class GeoLint {

	private static Logger gLogger = Logger.getLogger(GeoLint.class);
	
	private List<Format> formats = new LinkedList<Format>();
	
	/**
	 * Create a new GeoLint object, adding an instance of all formats to the format list
	 * for use by check()
	 */
	public GeoLint() {
		formats.add(new GMLFormat());
		formats.add(new NTFFormat());
	}
	
	/**
	 * Print a set of results as XML to stdout
	 * @param pResults results to print
	 * @param pOut PrintWriter to send output to
	 */
	public static void printResults(List<CheckResult> pResults, PrintWriter pOut) {
		pOut.println("<geolint>");

		for(CheckResult result:pResults) {
			result.printXMLResult(pOut);
		}
		
		pOut.println("</geolint>");
	}
	
	//TODO: change the return type to DRMFound to allow for yes/no/unknown
	/**
	 * Check a file using relevant Format object(s)
	 * @param pFile file to check
	 * @param pCalcChecksums whether or not to calculate checksums
	 * @return a CheckResult containing the results of the check
	 */
	public List<CheckResult> check(File pFile, boolean pCalcChecksums) {

		boolean checked = false;

		final String mimetype = FormatDetector.getMimetype(pFile);
		
		List<CheckResult> results = new ArrayList<CheckResult>();

		Map<String, String> checksums = null;
		if(pCalcChecksums) {
			checksums = new HashMap<String, String>();
			try {
				ChecksumUtil.calcChecksums(pFile, checksums);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for(Format format:formats) {
			if(format.canCheck(pFile, mimetype)) {
				long time = System.currentTimeMillis();
				
				gLogger.trace("geolint: "+pFile+" "+format.getFormatName()+" "+mimetype);
				gLogger.trace("Checking validity");
				Map<String, Boolean> isValidChecks = format.isValid(pFile);
				
				//calculate time taken and reset time counter
				long timeTaken = (System.currentTimeMillis()-time);
				
				CheckResult result = new CheckResult(pFile.getAbsolutePath(), pFile.length(), 
													 isValidChecks, format.getFormatName(), 
													 format.getVersion(), timeTaken, checksums, mimetype);
				
				System.out.println(result);
				results.add(result);
				checked = true;
			}
		}
		
		if(!checked) {
			System.out.println("Unable to check: "+pFile+", mimetype: "+mimetype);
			CheckResult result = new CheckResult(pFile.getAbsolutePath(), pFile.length(), null, null, null, -1, checksums, mimetype);

			System.out.println(result);
			results.add(result);

		}

		return results;
	}

	private static void test(GeoLint lint) {
		
		String dir = "";
		
		File testfiledir = new File(dir);
		
		List<File> files = new LinkedList<File>();
		FileUtil.traverse(testfiledir, files);
		
		List<CheckResult> results = new ArrayList<CheckResult>();
		
		for(File file:files) {
			System.out.println("Checking: "+file);
			results.addAll(lint.check(file, false));
		}

		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter("test_results.xml"));
			printResults(results, out);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Stub main method
	 * If no arguments will call test() method
	 * @param args command line arguments
	 */
	public static void main(String[] args) {

		GeoLint lint = new GeoLint();
		
		try {
			if(0==args.length) {
				test(lint);
				return;
			} 
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		List<CheckResult> results = new ArrayList<CheckResult>();
		
		for(String file:args) {
			try {
				results.addAll(lint.check(new File(file), true));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter("results.xml"));
			printResults(results, out);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
