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
package com.att.cadi.util;

/**
 * Split by Char, optional Trim
 *
 * Note: Copied from Inno to avoid linking issues.
 * Note: I read the String split and Pattern split code, and we can do this more efficiently for a single Character
 * 
 * 8/20/2015
 */

public class Split {
	  public static String[] split(char c, String value) {
		  // Count items to preallocate Array (memory alloc is more expensive than counting twice)
		  int count,idx;
		  for(count=1,idx=value.indexOf(c);idx>=0;idx=value.indexOf(c,++idx),++count);
		  String[] rv = new String[count];
		  if(count==1) {
			  rv[0]=value;
		  } else {
			  int last=0;
			  count=-1;
			  for(idx=value.indexOf(c);idx>=0;idx=value.indexOf(c,idx)) {
				  rv[++count]=value.substring(last,idx);
				  last = ++idx;
			  }
			  rv[++count]=value.substring(last);
		  }
		  return rv;
	  }

	  public static String[] splitTrim(char c, String value) {
		  // Count items to preallocate Array (memory alloc is more expensive than counting twice)
		  int count,idx;
		  for(count=1,idx=value.indexOf(c);idx>=0;idx=value.indexOf(c,++idx),++count);
		  String[] rv = new String[count];
		  if(count==1) {
			  rv[0]=value.trim();
		  } else {
			  int last=0;
			  count=-1;
			  for(idx=value.indexOf(c);idx>=0;idx=value.indexOf(c,idx)) {
				  rv[++count]=value.substring(last,idx).trim();
				  last = ++idx;
			  }
			  rv[++count]=value.substring(last).trim();
		  }
		  return rv;
	  }

	  public static String[] splitTrim(char c, String value, int size) {
		  int idx;
		  String[] rv = new String[size];
		  if(size==1) {
			  rv[0]=value.trim();
		  } else {
			  int last=0;
			  int count=-1;
			  size-=2;
			  for(idx=value.indexOf(c);idx>=0 && count<size;idx=value.indexOf(c,idx)) {
				  rv[++count]=value.substring(last,idx).trim();
				  last = ++idx;
			  }
			  rv[++count]=value.substring(last).trim();
		  }
		  return rv;
	  }

}
