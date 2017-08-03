/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aai
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * Copyright © 2017 Amdocs
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
package com.att.cadi.aaf;

import com.att.inno.env.util.Split;


public class PermEval {
	public static final char START_REGEX_CHAR = '!';
	public static final char START_INST_KEY_CHAR=':';
	public static final char ALT_START_INST_KEY_CHAR='/';
	
	public static final char LIST_SEP = ',';
	public static final String INST_KEY_REGEX = new StringBuilder().append(START_INST_KEY_CHAR).toString();
	public static final String ASTERIX = "*";
	
	/**
	 * Evaluate Instance
	 * 
	 * Instance can be more complex.  It can be a string, a Regular Expression, or a ":" separated Key 
	 * who's parts can also be a String, Regular Expression.
	 * 
	 * sInst = Server's Instance
	 * In order to prevent false matches, keys must be the same length to count as equal
	 * Changing this will break existing users, like Cassandra.  9-4-2015
	 */
	 public static boolean evalInstance(String sInst, String pInst) {
		  if(ASTERIX.equals(sInst)) return true;			// If Server's String is "*", then it accepts every Instance
		  char firstChar = pInst.charAt(0);
		  char startChar = firstChar==ALT_START_INST_KEY_CHAR?ALT_START_INST_KEY_CHAR:START_INST_KEY_CHAR;
		  switch(pInst.charAt(0)) {  						// First char
		  	case START_REGEX_CHAR:							// Evaluate as Regular Expression
		  		String pItem = pInst.substring(1);
  			    for(String sItem : Split.split(LIST_SEP,sInst)) {		// allow for "," definition in Action
  			    	return sItem.matches(pItem);
  			    }
  			 
		  	case START_INST_KEY_CHAR:						// Evaluate a special Key field, i.e.:xyz:*:!df.*
		  	case ALT_START_INST_KEY_CHAR:					// Also allow '/' as special Key Field, i.e. /xyz/*/!.*
		  		if(sInst.charAt(0)==startChar) {  // To compare key-to-key, both strings must be keys
		  			String[] skeys=Split.split(startChar,sInst);
		  			String[] pkeys=Split.split(startChar,pInst);
		  			if(skeys.length!=pkeys.length) return false;
		  			
		  			boolean pass = true;
		  			for(int i=1;pass && i<skeys.length;++i) {  				// We start at 1, because the first one, being ":" is always ""
		  				if(ASTERIX.equals(skeys[i]))continue;   			// Server data accepts all for this key spot
		  				pass = false;
		  			    for(String sItem : Split.split(LIST_SEP,skeys[i])) {		// allow for "," definition in Action
			  				if(pkeys[i].length()==0) {
			  					if(pass=sItem.length()==0) {
			  						break;  								// Both Empty, keep checking
			  					}
//			  				} else if(pkeys[i].charAt(0)==START_REGEX_CHAR) { 
//			  					if(pass=sItem.matches(pkeys[i].substring(1))) {
//			  						break;  								// Matches, keep checking
//			  					}
			  				} else if(sItem.charAt(0)==START_REGEX_CHAR) { // Check Server side when wildcarding like *
			  					if(pass=pkeys[i].matches(sItem.substring(1))) {
			  						break;  								// Matches, keep checking
			  					}
			  				} else if(skeys[i].endsWith(ASTERIX)) {
			  					if(pass=endAsterixCompare(skeys[i],pkeys[i])) {
			  						break;
			  					}
			  				} else {
			  					if(pass=sItem.equals(pkeys[i]))
			  						break;   								// Equal, keep checking
			  				}
		  			    }
		  			}
		  			return pass; 											// return whether passed all key checks
		  		}
		  		return false; 								// if first chars aren't the same, further String compare not necessary
		  	default:										// Evaluate as String Compare
  			    for(String sItem : Split.split(LIST_SEP,sInst)) {	// allow for "," separator //TODO is this only for actions?
  			    	if(sItem.endsWith(ASTERIX)) {
  			    		if(endAsterixCompare(sInst, pInst));
  			    	} else if(sItem.equals(pInst)) {
  			    		return true;
  			    	}
  			    }
  			    return false;
		  }
	 }
	 
	 private static boolean endAsterixCompare(String sInst, String pInst) {
			final int len = sInst.length()-1;
			if(pInst.length()<len) {
				return false;
			}
			for(int j=0;j<len;++j) {
				if(pInst.charAt(j)!=sInst.charAt(j)) {
					return false;
				}
			}
			return true;
	}

	/**
	  * Evaluate Action
	  * 
	  * sAction = Stored Action...
	  * pAction = Present Action... the Permission to validate against.
	  * Action is not quite as complex.  But we write it in this function so it can be consistent
	  */
	  public static boolean evalAction(String sAction,String pAction) {
		  if(ASTERIX.equals(sAction))return true;  		     // If Server's String is "*", then it accepts every Action
		  for(String sItem : Split.split(LIST_SEP,sAction)) {		 // allow for "," definition in Action
			  if (pAction.charAt(0)==START_REGEX_CHAR?       // First char
				      sItem.matches(pAction.substring(1)):   // Evaluate as Regular Expression
				      sItem.equals(pAction))                 // Evaluate as String Compare
				  		return true;
		  }		
		  return false;
	  }
	 
	  /**
	   * Split.split by Char
	   * 
	   * Note: I read the String Split.split and Pattern Split.split code, and we can do this more efficiently for a single Character
	   */

}
