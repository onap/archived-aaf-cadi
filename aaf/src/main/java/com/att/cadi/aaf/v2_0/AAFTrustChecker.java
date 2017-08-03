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
package com.att.cadi.aaf.v2_0;

import javax.servlet.http.HttpServletRequest ;

import com.att.cadi.Access;
import com.att.cadi.Lur;
import com.att.cadi.TrustChecker;
import com.att.cadi.aaf.AAFPermission;
import com.att.cadi.config.Config;
import com.att.cadi.principal.TrustPrincipal;
import com.att.cadi.taf.TafResp;
import com.att.cadi.taf.TrustNotTafResp;
import com.att.cadi.taf.TrustTafResp;
import com.att.inno.env.Env;
import com.att.inno.env.util.Split;

public class AAFTrustChecker implements TrustChecker {
	private final String tag, id;
	private final AAFPermission perm;
	private Lur lur;

	/**
	 * 
	 * Instance will be replaced by Identity
	 * @param lur 
	 *    
	 * @param tag
	 * @param perm
	 */
	public AAFTrustChecker(final Env env) {
		tag = env.getProperty(Config.CADI_USER_CHAIN_TAG, Config.CADI_USER_CHAIN);
		id = env.getProperty(Config.CADI_ALIAS,env.getProperty(Config.AAF_MECHID)); // share between components
		String str = env.getProperty(Config.CADI_TRUST_PERM);
		AAFPermission temp=null;
		if(str!=null) {
			String[] sp = Split.splitTrim('|', str);
			if(sp.length==3) {
				temp = new AAFPermission(sp[0],sp[1],sp[2]);
			}
		}
		perm=temp;
	}

	public AAFTrustChecker(final Access access) {
		tag = access.getProperty(Config.CADI_USER_CHAIN_TAG, Config.CADI_USER_CHAIN);
		id = access.getProperty(Config.CADI_ALIAS,access.getProperty(Config.AAF_MECHID,null)); // share between components
		String str = access.getProperty(Config.CADI_TRUST_PERM,null);
		AAFPermission temp=null;
		if(str!=null) {
			String[] sp = Split.splitTrim('|', str);
			if(sp.length==3) {
				temp = new AAFPermission(sp[0],sp[1],sp[2]);
			}
		}
		perm=temp;
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.TrustChecker#setLur(com.att.cadi.Lur)
	 */
	@Override
	public void setLur(Lur lur) {
		this.lur = lur;
	}

	@Override
	public TafResp mayTrust(TafResp tresp, HttpServletRequest req) {
		String user_info = req.getHeader(tag);
		if(user_info !=null ) {
			String[] info = Split.split(',', user_info);
			if(info.length>0) {
				String[] flds = Split.splitTrim(':',info[0]);
				if(flds.length>3 && "AS".equals(flds[3])) { // is it set for "AS"
					String pn = tresp.getPrincipal().getName();
					if(pn.equals(id)  // We do trust our own App Components: if a trust entry is made with self, always accept
					   || lur.fish(tresp.getPrincipal(), perm)) { // Have Perm set by Config.CADI_TRUST_PERM
						return new TrustTafResp(tresp,
								new TrustPrincipal(tresp.getPrincipal(), flds[0]),
								"  " + flds[0] + " validated using " + flds[2] + " by " + flds[1] + ','
							);
					} else if(pn.equals(flds[0])) { // Ignore if same identity 
						return tresp;
					} else {
						return new TrustNotTafResp(tresp, tresp.getPrincipal().getName() + " requested trust as "
								+ flds[0] + ", but does not have Authorization");
					}
				}
			}
		}
		return tresp;
	}

}
