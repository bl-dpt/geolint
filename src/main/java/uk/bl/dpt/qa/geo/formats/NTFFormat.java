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
package uk.bl.dpt.qa.geo.formats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.bl.dpt.qa.geo.wrappers.GDALWrapper;

/**
 * Class to handle the NTF GIS file format (BS 7567)
 * @author wpalmer
 */
public class NTFFormat implements Format {

	private static Logger gLogger = Logger.getLogger(NTFFormat.class);
	
	private class BoolRet {
		boolean gBoolean;
		public BoolRet(boolean pBoolean) {
			gBoolean = pBoolean;
		}
		public boolean get() {
			return gBoolean;
		}
		public void set(boolean pBoolean) {
			gBoolean = pBoolean;
		}
	}
	
	/**
	 * Create an NTF handler
	 */
	public NTFFormat() {
		
	}
	
	@Override
	public boolean canCheck(File pFile, String pMimetype) {
		if(pFile.getName().toLowerCase().endsWith(".ntf")) return true;
		if(pMimetype.toLowerCase().equals("application/x-ntf")) return true;
		
		return false;
	}

	@Override
	public Map<String, Boolean> isValid(File pFile) {
		Map<String, Boolean> tests = new HashMap<String, Boolean>();
		
		String test = "";
		BoolRet osgb = new BoolRet(true);
		test = "internalCheck";
		boolean hfcheck = internalCheck(pFile, osgb);
		tests.put(test, hfcheck);
		if(!osgb.get()) System.out.println("Not osgb");
		// if the internal check fails then do not check with gdal as missing % @ EOL etc will crash the library
		// if the file is not osgb then do not check using gdal
		if(osgb.get()&&hfcheck) {
			test = "GDALOGR";
			boolean ogr = GDALWrapper.checkValid(pFile);
			tests.put(test, ogr);
		}
		
		return tests;
	}

	@Override
	public String getFormatName() {
		return "NTF";
	}

	@Override
	public String getVersion() {
		return "0.0.2";
	}
	
	/**
	 * Check the header and footer of the NTF file
	 * @param pFile
	 * @return
	 */
	private boolean internalCheck(File pFile, BoolRet osgb) {

		// Assume ok, unless we don't find the magic 
		boolean ok = true;
		
		final String EOL    = "%";
		final String HEADER = "01ORDNANCE SURVEY";
		final String HEADER2 = "01OSNI";// add to check
		final String FOOTER = "End Of Transfer Set0"+EOL;
		final String FOOTER2 = "End of Volume00"+EOL;
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(pFile));
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			return false;
		}
		
		String line = "";
		boolean end = false;
		try {
			line = reader.readLine().trim();
			if(!(line.startsWith(HEADER)||line.startsWith(HEADER2))) {
				gLogger.trace("NTF header does not match: "+new String(line));
				ok = false;
			} else {
				if(!line.startsWith(HEADER)) {
					// potentially valid ntf file, but not osgb 
					osgb.set(false);
				}
			}
			
			if(!line.endsWith(EOL)) {
				//System.out.println("%eol");
				ok = false;
			}
			
			long currentLine = 1;
			while(reader.ready()) {
				line = reader.readLine().trim();
				currentLine++;
				if(end) {
					if(!line.equals("")) {
						// content after footer (allow whitespace)
						ok = false;
						gLogger.trace("NTF: content after footer");
					}
				} else {
					if(line.endsWith(FOOTER)||line.endsWith(FOOTER2)) {
						end = true;
					} else {
						// all lines should end with %
						if(!line.endsWith(EOL)) {
							gLogger.trace("NTF: line "+currentLine+" incorrectly terminated (no "+EOL+")");
							ok = false;
						}
					}
				}
			}
			
			// no footer encountered
			if(!end) {
				gLogger.trace("NTF: no footer");
				ok = false;
			}
			
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
//			
//			gLogger.trace("Footer does not match: "+new String(footer));
		
		return ok;
	}
	
}
