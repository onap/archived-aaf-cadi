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
package com.att.cadi.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

import com.att.cadi.Locator;
import com.att.cadi.LocatorException;

public class PropertyLocator implements Locator {
	private final URI [] orig;
	private PLItem[] current;
	private int end;
	private final Random random;
	
	/**
	 * comma delimited root url list
	 * 
	 * @param locList
	 * @throws LocatorException
	 */
	public PropertyLocator(String locList) throws LocatorException {
		if(locList==null)throw new LocatorException("No Location List given for PropertyLocator");
		String[] locarray = locList.split("\\s*,\\s*");
		orig = new URI[locarray.length];
		
		random = new Random();
		
		for(int i=0;i<locarray.length;++i) {
			try {
				orig[i] = new URI(locarray[i]);
			} catch (URISyntaxException e) {
				throw new LocatorException(e);
			}
		}

		current = new PLItem[orig.length];
		refresh();
	}

	@Override
	public URI get(Item item) throws LocatorException {
		return orig[((PLItem)item).idx];
	}

	@Override
	public Item first() throws LocatorException {
		return end>0?current[0]:null;
	}

	@Override
	public boolean hasItems() {
		return end>0;
	}

	@Override
	public Item next(Item item) throws LocatorException {
		int spot;
		if((spot=(((PLItem)item).order+1))>=end)return null;
		return current[spot];
	}

	@Override
	public synchronized void invalidate(Item item) throws LocatorException {
		if(--end<=0)return;
		PLItem pli = (PLItem)item;
		int i,order;
		for(i=0;i<end;++i) {
			if(pli==current[i])break;
		}
		order = current[i].order;
		for(;i<end;++i) {
			current[i]=current[i+1];
			current[i].order=order++;
		}
		current[end]=pli;
	}

	@Override
	public Item best() throws LocatorException {
		switch(current.length) {
			case 0:
				return null;
			case 1:
				return current[0];
			default:
				return current[Math.abs(random.nextInt())%end];
		}
	}

	@Override
	public synchronized boolean refresh() {
		end = orig.length;
		
		// Build up list
		for(int i = 0; i < end ; ++i) {
			if(current[i]==null)current[i]=new PLItem(i);
			else current[i].idx=current[i].order=i;
		}
		return true;
	}
	
	private class PLItem implements Item {
		public int idx,order;
		
		public PLItem(int i) {
			idx = order =i;
		}
		
		public String toString() {
			return "Item: " + idx + " order: " + order;
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
