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
package uk.bl.dpt.qa.geo.wrappers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.impl.Constants;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import uk.bl.dpt.utils.util.ResourceUtil;
import uk.bl.dpt.utils.util.StreamUtil;

/**
 * Class to handle the GML GIS file format
 * @author wpalmer
 */
public class GMLXercesWrapper {

	// NOTE: can also use v7 schema (may need to for older products?)
	// Schema: http://www.ordnancesurvey.co.uk/xml/schema/v8/
	private static final String SCHEMA_DIR = "schema/v8/"; // (in resources)
	
	private static final String OSGB_SCHEMA = SCHEMA_DIR+"OSDNFFeatures.xsd";
	
	private static Validator gValidator = null;
	
	/**
	 * Resource resolver for files contained within the jar
	 * @author wpalmer
	 *
	 */
	private static class ResourceResolver implements LSResourceResolver {
		@Override
		public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
			InputStream is = ResourceUtil.loadResource(SCHEMA_DIR+new File(systemId).getName());
			if(is!=null) {
				return new DOMInputImpl(publicId, systemId, baseURI, is, null);
			}
			System.err.println("Can't resolve: "+type+" "+namespaceURI+" "+publicId+" "+systemId+" "+baseURI);
			return null;
		}
	}
	
	private static void initValidator() {

		if(null != gValidator) {
            return;
        }
		
		InputStream osgb = null;
		osgb = ResourceUtil.loadResource(OSGB_SCHEMA);
		
		// We use 1.1 XSD as Xerces complains when using 1.0
		final String W3C_XML_SCHEMA = Constants.W3C_XML_SCHEMA11_NS_URI;
		
		SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA);
		sf.setResourceResolver(new ResourceResolver());
		Schema schema = null;
		try {
			schema = sf.newSchema(new StreamSource(osgb));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		gValidator = schema.newValidator();
		
	}
	
	/**
	 * Validate a GML file according to the schema 
	 * See: http://xerces.apache.org/xerces2-j/faq-xs.html
	 * @param pFile gml file to validate
	 * @return true if valid, false if not
	 */
	public static boolean validateGML(File pFile) {
		
		initValidator();
		
		InputStream xml = StreamUtil.getInputStreamFromFile(pFile);

		try {
			gValidator.validate(new StreamSource(xml));
			return true;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
		
	}

}
