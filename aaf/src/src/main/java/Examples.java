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
import com.att.rosetta.env.RosettaEnv;

public class Examples {
	public static void main(String[] args) {
		if(args.length<1) {
			System.out.println("Usage: Examples <name> [\"optional\" - will show optional fields]");
		} else {
			boolean options = args.length>1&&"optional".equals(args[1]);
			try {
				RosettaEnv env = new RosettaEnv();
				System.out.println(com.att.cadi.aaf.client.Examples.print(env, args[0], options));
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	

}
