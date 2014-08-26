/**
 * This class has been derived from ogrinfo.java from gdal/swig/java/apps.  Original copyright notice is below.
 * Changes and new code are Apache License 2.0
 * @author wpalmer
 */

/******************************************************************************
 * $Id: ogrinfo.java 23704 2012-01-06 10:27:08Z rouault $
 *
 * Name:     ogrinfo.java
 * Project:  GDAL SWIG Interface
 * Purpose:  Java port of ogrinfo application, simple client for viewing OGR driver data.
 * Author:   Even Rouault, <even dot rouault at mines dash paris dot org>
 *
 * Port from ogrinfo.cpp by Frank Warmerdam
 *
 ******************************************************************************
 * Copyright (c) 2009, Even Rouault
 * Copyright (c) 1999, Frank Warmerdam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 ****************************************************************************/

/*
 * Portions copyright 2014 The British Library/SCAPE Project Consortium
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

/* http://digimap.edina.ac.uk/webhelp/os/data_information/data_formats/new_data_translators.htm
 * (large) C++ library for NTF: http://trac.osgeo.org/gdal/browser/trunk/gdal/ogr/ogrsf_frmts/ntf
 * A Java wrapper for the above exists: http://trac.osgeo.org/gdal/wiki/GdalOgrInJava
 * http://trac.osgeo.org/gdal/wiki/GdalOgrInJavaBuildInstructions
 * 
 * Compilation tips:
 * - Ensure no spaces or trailing slashes in env vars
 * - JAVA_HOME - ensure no spaces
 * - ANT_HOME is not bin/ directory
 * - "vcvarsall.bat amd64" sets up MSVC for amd64 - ensure this matches the JVM!
 */

import java.io.File;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Driver;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;

import uk.bl.dpt.utils.jni.JNIInit;

/* Note : this is the most direct port of ogrinfo.cpp possible */
/* It could be made much more java'ish ! */

/**
 * Open a file with GDAL/OGR and test it can be opened
 * @author wpalmer
 */
public class GDALWrapper
{
	private static final boolean bReadOnly = true;
    private static final boolean bVerbose = false;

	private static final String OGR_NTF_DRIVER_NAME = "UK .NTF";
	
	////////////////////////////////////////////////////////////
	// JNI constants
	////////////////////////////////////////////////////////////	
	
	private static final String PACKAGE = "gdal";
	private static final String VERSION = "1.10.1";
	private static final String[] LIBS = new String[] { "gdal110", "gdalconstjni", "gdaljni", "osrjni", "ogrjni" };
	
	////////////////////////////////////////////////////////////
	
    static {

    	// Extract and load libraries from the jar
    	JNIInit.init(PACKAGE, VERSION, LIBS);
    	
    	ogr.DontUseExceptions();

    	/* -------------------------------------------------------------------- */
    	/*      Register format(s).                                             */
    	/* -------------------------------------------------------------------- */
    	if( ogr.GetDriverCount() == 0 )
    		ogr.RegisterAll();

    }
    
    /**
     * Process a file 
     * @param pFile file to process
     * @return true if opened ok, false if not
     */
    @SuppressWarnings("unused")
	public static boolean checkValid(File pFile) {

    	if(!pFile.exists()) return false;
    	
    	final String pszWHERE = null;
    	final Geometry poSpatialFilter = null;
    	Driver poDriver = null;
    	final String pszDataSource = pFile.getAbsolutePath();

    	/* -------------------------------------------------------------------- */
    	/*      Open data source.                                               */
    	/* -------------------------------------------------------------------- */

    	DataSource poDS = ogr.Open(pszDataSource, !bReadOnly);
    	if (poDS == null && !bReadOnly)	{
    		poDS = ogr.Open(pszDataSource, false);
    		if (poDS == null && bVerbose) {
    			if(bVerbose) System.out.println( "Had to open data source read-only.");
    		}
    	}

    	/* -------------------------------------------------------------------- */
    	/*      Report failure                                                  */
    	/* -------------------------------------------------------------------- */
    	if( poDS == null ) {
    		if(bVerbose) {
    			System.out.print("FAILURE: Unable to open datasource `"+pszDataSource+"' with the following drivers.\n");
    			for( int iDriver = 0; iDriver < ogr.GetDriverCount(); iDriver++ ) {
    				System.out.println( "  -> " + ogr.GetDriver(iDriver).GetName() );
    			}
    		}
    		return false;
    	}

    	poDriver = poDS.GetDriver();
    	
    	if(!poDriver.GetName().equalsIgnoreCase(OGR_NTF_DRIVER_NAME)) {
    		// i.e. this is not an NTF file
    		return false;
    	}

    	/* -------------------------------------------------------------------- */
    	/*      Some information messages.                                      */
    	/* -------------------------------------------------------------------- */
    	if( bVerbose ) {
    		System.out.println( "INFO: Open of `" + pszDataSource + "'\n" +
    				"      using driver `" + poDriver.GetName() + "' successful." );
    	}

    	/* -------------------------------------------------------------------- */
    	/*      Process each data source layer.                                 */
    	/* -------------------------------------------------------------------- */

    	for(int iLayer = 0; iLayer < poDS.GetLayerCount(); iLayer++) {
    		Layer poLayer = poDS.GetLayer(iLayer);

    		if(poLayer==null) {
    			if(bVerbose) System.out.println( "FAILURE: Couldn't fetch advertised layer " + iLayer + "!");
    			return false;
    		}

    		boolean layerOk = reportOnLayer( poLayer, pszWHERE, poSpatialFilter );
    		if(!layerOk) return false;
    		
    	}

    	// if we get this far then the file must be valid(ish)
    	return true;
    }

    /**
     * 
     * @param poLayer
     * @param pszWHERE
     * @param poSpatialFilter
     * @return
     */
    private static boolean reportOnLayer(Layer poLayer, String pszWHERE, Geometry poSpatialFilter) {
    	FeatureDefn poDefn = poLayer.GetLayerDefn();

    	/* -------------------------------------------------------------------- */
    	/*      Set filters if provided.                                        */
    	/* -------------------------------------------------------------------- */
    	if( pszWHERE != null ) {
    		if( poLayer.SetAttributeFilter( pszWHERE ) != ogr.OGRERR_NONE ) {
    			System.err.println("FAILURE: SetAttributeFilter(" + pszWHERE + ") failed.");
    			return false;
    		}
    	}

    	if( poSpatialFilter != null ) {
    		poLayer.SetSpatialFilter( poSpatialFilter );
    	}

    	/* -------------------------------------------------------------------- */
    	/*      Report various overall information.                             */
    	/* -------------------------------------------------------------------- */

    	if(bVerbose) {
    		System.out.println();
    		System.out.println("Layer name: "+poDefn.GetName());
    		System.out.println("Geometry: "+ogr.GeometryTypeToName(poDefn.GetGeomType()));
    		System.out.println("Feature Count: "+poLayer.GetFeatureCount());
    	}

    	double oExt[] = poLayer.GetExtent(true);
    	if (oExt != null) {
    		if(bVerbose) System.out.println("Extent: (" + oExt[0] + ", " + oExt[2] + ") - (" + oExt[1] + ", " + oExt[3] + ")");
    	}

    	String pszWKT;

    	if( poLayer.GetSpatialRef() == null ) {
    		pszWKT = "(unknown)";
    	} else {
    		pszWKT = poLayer.GetSpatialRef().ExportToPrettyWkt();
    	}            

    	if(bVerbose) System.out.println("Layer SRS WKT:\n"+pszWKT);

    	if( poLayer.GetFIDColumn().length() > 0 ) {
    		String fid = poLayer.GetFIDColumn();
    		if(bVerbose) System.out.println("FID Column = "+fid);
    	}

    	if( poLayer.GetGeometryColumn().length() > 0 ) {
    		String col = poLayer.GetGeometryColumn();
    		if(bVerbose) System.out.println("Geometry Column = "+col);
    	}

    	for( int iAttr = 0; iAttr < poDefn.GetFieldCount(); iAttr++ ) {
    		FieldDefn poField = poDefn.GetFieldDefn(iAttr);
    		String nameRef = poField.GetNameRef();
    		String typeName = poField.GetFieldTypeName(poField.GetFieldType());
    		int width = poField.GetWidth();
    		int precision = poField.GetPrecision();
    		if(bVerbose) System.out.println( nameRef + ": " + typeName + " (" + width + "." + precision + ")");
    	}

    	/* -------------------------------------------------------------------- */
    	/*      Read, and dump features.                                        */
    	/* -------------------------------------------------------------------- */
    	Feature  poFeature;

    	while( (poFeature = poLayer.GetNextFeature()) != null ) {
    		// FIXME: if this is not called does the file still get read?
    		if(bVerbose) { 
    			poFeature.DumpReadable();
    		}
    	}

    	return true;
    }

}
