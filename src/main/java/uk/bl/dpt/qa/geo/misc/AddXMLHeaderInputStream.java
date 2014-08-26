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
package uk.bl.dpt.qa.geo.misc;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("javadoc")
public class AddXMLHeaderInputStream extends InputStream {

	private InputStream gInputStream;
	
	final String gTag = "xml";
	final String gHeader = "<"+gTag+">";
	final String gFooter = "</"+gTag+">";
	
	private int gCountH;
	private int gCountF;
	
	public AddXMLHeaderInputStream(InputStream pInputStream) {
		gInputStream = pInputStream;
		gCountH = 0;
		gCountF = 0;
	}
	
	@Override
	public int read() throws IOException {
		if(gInputStream.available()>0) {
			if(gCountH==gHeader.length()) {
				return gInputStream.read();
			} else {
				return gHeader.charAt(gCountH++);  
			}
		} else {
			if(gCountF==gFooter.length()) {
				return -1;
			} else {
				return gFooter.charAt(gCountF++);  
			}
		}
	}
	

}
