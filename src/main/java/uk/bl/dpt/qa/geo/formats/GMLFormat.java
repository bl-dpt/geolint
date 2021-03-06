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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.bl.dpt.qa.geo.wrappers.GMLXercesWrapper;

/**
 * Class to handle the GML file format
 * @author wpalmer
 */
public class GMLFormat implements Format {

	@SuppressWarnings("unused")
	private static Logger gLogger = Logger.getLogger(GMLFormat.class);
	
	/**
	 * Create a GML handler
	 */
	public GMLFormat() {
		
	}
	
	@Override
	public boolean canCheck(File pFile, String pMimetype) {
		
		//if(pFile.getName().toLowerCase().endsWith(".gz")) {
			if( pMimetype.equals("application/xml")||
				pMimetype.equals("application/gml+xml")||
				pMimetype.equals("application/x-os-gml+xml")) {
				return true;
			}
		//}
		
		return false;
	}

	@Override
	public Map<String, Boolean> isValid(File pFile) {
		Map<String, Boolean> tests = new HashMap<String, Boolean>();
		
		String test = "";
		
		test = "schemaValidate";
		boolean ogr = GMLXercesWrapper.validateGML(pFile);
		tests.put(test, ogr);
		
		return tests;
	}

	@Override
	public String getFormatName() {
		return "GML";
	}

	@Override
	public String getVersion() {
		return "0.0.2";
	}
	
}
