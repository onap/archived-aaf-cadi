/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * 
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package com.att.cadi.routing;

import com.att.inno.env.util.Split;

public class GreatCircle {
	// Note: multiplying by this constant is faster than calling Math equivalent function 
	private static final double DEGREES_2_RADIANS = Math.PI/180.0;
	
	public static final double DEGREES_2_NM = 60;
	public static final double DEGREES_2_KM = DEGREES_2_NM * 1.852; // 1.852 is exact ratio per 1929 Standard Treaty, adopted US 1954
	public static final double DEGREES_2_MI = DEGREES_2_NM * 1.1507795; 
	
	/**
	 * 
	 * Calculate the length of an arc on a perfect sphere based on Latitude and Longitudes of two points
	 *    Parameters are in Degrees (i.e. the coordinate system you get from GPS, Mapping WebSites, Phones, etc)
	 *    
	 * 		L1 = Latitude of point A
	 *      G1 = Longitude of point A
	 * 	    L2 = Latitude of point B
	 *      G2 = Longitude of point B
	 *      
	 *      d  = acos (sin(L1)*sin(L2) + cos(L1)*cos(L2)*cos(G1 - G2))
	 * 
   	 * Returns answer in Degrees
   	 * 
	 * Since there are 60 degrees per nautical miles, you can convert to NM by multiplying by 60
	 * 
	 * Essential formula from a Princeton website, the "Law of Cosines" method.  
	 * 
	 * Refactored cleaned up for speed  3/8/2013
	 * 
	 * @param latA
	 * @param lonA
	 * @param latB
	 * @param lonB
	 * @return
	 */
	public static double calc(double latA, double lonA, double latB, double lonB) {
		// Formula requires Radians.  Expect Params to be Coordinates (Degrees)
		// Simple ratio, quicker than calling Math.toRadians()
		latA *= DEGREES_2_RADIANS;
		lonA *= DEGREES_2_RADIANS;
		latB *= DEGREES_2_RADIANS;
		lonB *= DEGREES_2_RADIANS;

		return Math.acos(
				Math.sin(latA) * Math.sin(latB) + 
				Math.cos(latA) * Math.cos(latB) * Math.cos(lonA-lonB)
			)
			/ DEGREES_2_RADIANS;
	}
	
	/** 
	 * Convert from "Lat,Long Lat,Long" String format
	 *              "Lat,Long,Lat,Long" Format
	 *           or all four entries "Lat Long Lat Long"
	 * 
	 * (Convenience function)
	 * 
	 * Since Distance is positive, a "-1" indicates an error in String formatting
	 */
	public static double calc(String ... coords) {
		try {
			String [] array;
			switch(coords.length) {
			case 1:
				array = Split.split(',',coords[0]);
				if(array.length!=4)return -1;
				return calc(
					Double.parseDouble(array[0]),
					Double.parseDouble(array[1]),
					Double.parseDouble(array[2]),
					Double.parseDouble(array[3])
					);
			case 2:
				array = Split.split(',',coords[0]);
				String [] array2 = Split.split(',',coords[1]);
				if(array.length!=2 || array2.length!=2)return -1;
				return calc(
					Double.parseDouble(array[0]),
					Double.parseDouble(array[1]),
					Double.parseDouble(array2[0]),
					Double.parseDouble(array2[1])
					);
			case 4:
				return calc(
					Double.parseDouble(coords[0]),
					Double.parseDouble(coords[1]),
					Double.parseDouble(coords[2]),
					Double.parseDouble(coords[3])
					);
				
			default:
				return -1;
			}
		} catch (NumberFormatException e) {
			return -1;
		}
	}

}

///**
//* Haverside method, from Princeton
//* 
//* @param alat
//* @param alon
//* @param blat
//* @param blon
//* @return
//*/
//public static double calc3(double alat, double alon, double blat, double blon) {
//	alat *= DEGREES_2_RADIANS;
//	alon *= DEGREES_2_RADIANS;
//	blat *= DEGREES_2_RADIANS;
//	blon *= DEGREES_2_RADIANS;
//	return 2 * Math.asin(
//			Math.min(1, Math.sqrt(
//				Math.pow(Math.sin((blat-alat)/2), 2) +
//				(Math.cos(alat)*Math.cos(blat)*
//					Math.pow(
//						Math.sin((blon-alon)/2),2)
//					)
//				)
//			)
//		)
//	/ DEGREES_2_RADIANS;
//}
//



//This is a MEAN radius.  The Earth is not perfectly spherical
//	public static final double EARTH_RADIUS_KM = 6371.0;
//	public static final double EARTH_RADIUS_NM = 3440.07;
//	public static final double KM_2_MILES_RATIO = 0.621371192;
///**
//* Code on Internet based on Unknown book.  Lat/Long is in Degrees
//* @param alat
//* @param alon
//* @param blat
//* @param blon
//* @return
//*/
//public static double calc1(double alat, double alon, double blat, double blon) {
//	alat *= DEGREES_2_RADIANS;
//	alon *= DEGREES_2_RADIANS;
//	blat *= DEGREES_2_RADIANS;
//	blon *= DEGREES_2_RADIANS;
//	
//	// Reused values
//	double cosAlat,cosBlat;
//	
//	return Math.acos(
//		((cosAlat=Math.cos(alat))*Math.cos(alon)*(cosBlat=Math.cos(blat))*Math.cos(blon)) +
//		(cosAlat*Math.sin(alon)*cosBlat*Math.sin(blon)) +
//		(Math.sin(alat)*Math.sin(blat))
//		)/DEGREES_2_RADIANS;
//	
//}

/*
*  This method was 50% faster than calculation 1, and 75% than the Haverside method
*  Also, since it's based off of Agree standard Degrees of the Earth, etc, the calculations are more exact,
*    at least for Nautical Miles and Kilometers
*/
