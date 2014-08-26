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

import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author wpalmer
 *
 */
public class CheckResultTest {

	@Test
	public void testCheckResultPrintToXML() {
		
		final File file = new File("test.txt");
		final String mimetype = "application/octet-stream";
		final Map<String, String> checksums = new HashMap<String, String>();
		
		CheckResult result = new CheckResult(file.getAbsolutePath(), file.length(), null, null, null, -1, checksums, mimetype);

		try {
			System.out.println(result.toXML());
		} catch(Exception e) {
			fail("error converting result to XML");
		}
		
	}
	@Test
	public void testCheckResultToString() {
		
		final File file = new File("test.txt");
		final String mimetype = "application/octet-stream";
		final Map<String, String> checksums = new HashMap<String, String>();
		
		CheckResult result = new CheckResult(file.getAbsolutePath(), file.length(), null, null, null, -1, checksums, mimetype);

		try {
			System.out.println(result.toString());
		} catch(Exception e) {
			fail("error in toString");
		}
		
	}
	
}
