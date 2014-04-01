/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 * jOVAL.org elects to include this software in this distribution
 * under the CDDL license.
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package jkeyring;

import jkeyring.intf.IEncryptionProvider;
import jkeyring.intf.IKeyring;
import jkeyring.impl.crypto.CryptoProvider;
import jkeyring.impl.crypto.MasterPasswordEncryption;
import jkeyring.CredentialsDialog.Mode;

public class Test {
	public static void main(String[] argv) {
		try {
			String key1 = "myUsername";
			String key2 = "myPassword";
			IKeyring keyring = KeyringFactory.getNativeKeyring();
			if (keyring != null && keyring.enabled()) {
				byte[] userBytes = keyring.read(key1);
				byte[] passBytes = keyring.read(key2);
				if (userBytes == null || passBytes == null) {
					CredentialsDialog dialog = new CredentialsDialog(Mode.AUTO);
					dialog.showUsrPsswdDialog();
					byte[] username = dialog.getUser();
					byte[] password = dialog.getPassword();
					keyring.save(key1, username , "this is a password description");
					keyring.save(key2, password, "this is a username descripion");
					dialog.eraseCredentials();
					userBytes = keyring.read(key1);
					passBytes = keyring.read(key2);
				}
				System.out.println(new String(userBytes, "US-ASCII"));
				System.out.println(new String(passBytes, "US-ASCII"));
				//keyring.delete(key1);keyring.delete(key2); //remove the keys
				
			} else {
				System.out.println("Keyring is not enabled.");
			}
		} catch (Exception e) {
			System.out.println("Something went wrong.");
			e.printStackTrace();
		}
		System.exit(0);
	}
}

