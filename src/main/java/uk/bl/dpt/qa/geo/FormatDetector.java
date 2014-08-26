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

import java.io.File;
import uk.bl.dpt.qa.geo.wrappers.TikaWrapper;

/**
 * This class uses Apache Tika to detect the mimetype of a file
 * @author wpalmer
 *
 */
public class FormatDetector {
	
	private FormatDetector() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Get the mimetype of a file
	 * @param pFile file to check
	 * @return the mimetype of the file
	 */
	public static String getMimetype(File pFile) {
		return TikaWrapper.getMimetype(pFile);
	}

}
