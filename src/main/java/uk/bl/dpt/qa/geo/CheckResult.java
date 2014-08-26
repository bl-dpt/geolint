/*
 * Copyright 2013-2014 The British Library/SCAPE Project Consortium
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

import java.io.PrintWriter;
import java.util.Map;

/**
 * Contains the results from a check
 * @author wpalmer
 *
 */
public class CheckResult {
	private Map<String, Boolean> gValid;
	private String gFilename;
	private long gSize;
	private String gFormat;
	private String gVersion;	
	private long gTime;
	private Map<String, String> gChecksums;
	private String gMimetype;
	
	/**
	 * New check result
	 * @param pFilename filename checked
	 * @param pSize file size
	 * @param pDRM map containing drm checks
	 * @param pValid map containing isValid checks
	 * @param pFormat format used to check file
	 * @param pVersion version of format object used to check
	 * @param pTime time taken to run all checks
	 * @param pChecksums checksums for the file
	 * @param pMimetype mimetype for the file
	 */
	public CheckResult(String pFilename, long pSize, Map<String, Boolean> pValid, String pFormat, 
						String pVersion, long pTime, Map<String, String> pChecksums, String pMimetype) {
		gValid = pValid;
		gFilename = pFilename;
		gSize = pSize;
		gFormat = pFormat;
		gVersion = pVersion;
		gTime = pTime;
		gChecksums = pChecksums;
		gMimetype = pMimetype;
	}
	/**
	 * Is valid?
	 * @return true/false if valid
	 */
	public boolean isValid() {
		return null==gValid?false:gValid.containsValue(true);
	}
	/**
	 * Get filename
	 * @return filename
	 */
	public String getFilename() {
		return gFilename;
	}	
	/**
	 * Allow the filename to be changed (allows GeoLintHadoop to set the correct filename from HDFS)
	 * @param pFilename
	 */
	public void setFilename(String pFilename) {
		gFilename = pFilename;
	}
	/**
	 * Set the checksums that have been calculated externally
	 * @param pChecksums
	 */
	public void setChecksums(Map<String, String> pChecksums) {
		gChecksums = pChecksums;
	}
	/**
	 * Get file size
	 * @return size
	 */
	public long getSize() {
		return gSize;
	}
	/**
	 * Get format object used for the checks
	 * @return name of format object
	 */
	public String getFormat() {
		return gFormat;
	}
	/**
	 * Version of format object used for the checks
	 * @return Version of format object used for the checks
	 */
	public String getVersion() {
		return gVersion;
	}
	/**
	 * Get the time taken to execute tests (in ms)
	 * @return time taken to execute tests (in ms)
	 */
	public long getTimeTaken() {
		return gTime;
	}
	/**
	 * Get list of validity checks that have been run
	 * @return validity checks that have been run
	 */
	public Map<String, Boolean> getIsValidChecks() {
		return gValid;
	}
	/**
	 * Get the checksums for the file
	 * @return checksums for the file (in the format type:digest)
	 */
	public Map<String, String> getChecksums() {
		return gChecksums;
	}
	/**
	 * Get the mimetype for the file
	 * @return mimetype for the file
	 */
	public String getMimetype() {
		return gMimetype;
	}
	
	public String toString() {
		return getFormat()+": v"+getVersion()+", "+getFilename()+", valid: "+(null==gValid?"NA":isValid())+", time: "+getTimeTaken()+"ms";
	}
	
	private String toXML(boolean pPretty) {
		String _start = "";
		String _end = "";
		if(pPretty) {
			_start = "     ";
			_end = System.getProperty("line.separator");
		}
		final String start = _start;
		final String end = _end;
		String ret = "";
		ret += start+"<check format=\""+getFormat()+"\" version=\""+getVersion()+"\" timeMS=\""+getTimeTaken()+"\">"+end;
		ret += start+start+"<file>"+getFilename()+"</file>"+end;
		ret += start+start+"<size>"+getSize()+"</size>"+end;
		if(null!=getIsValidChecks()) {
			ret += start+start+"<isvalid result=\""+isValid()+"\">"+end;
			for(String k:getIsValidChecks().keySet()) {
				ret += start+start+start+"<test name=\""+k+"\" result=\""+getIsValidChecks().get(k)+"\" />"+end;
			}
			ret += start+start+"</isvalid>"+end;
		}
		
		if(null!=getChecksums()) {
			for(String k:getChecksums().keySet()) {
				ret += start+start+"<checksum digest=\""+k+"\">"+getChecksums().get(k)+"</checksum>"+end;
			}
		}
		
		ret += start+start+"<mimetype>"+getMimetype()+"</mimetype>"+end;
		ret += start+"</check>";//+end;
	
		return ret;
	}
	
	/**
	 * Output the CheckResult as a single line of XML
	 * @return this CheckResult as a single line of XML
	 */
	public String toXML() {
		return toXML(false);
	}
	
	/**
	 * Output result to a PrintWriter 
	 * @param pResult
	 * @param pOut
	 */
	public void printXMLResult(PrintWriter pOut) {
		pOut.println(toXML(true));
	}

}