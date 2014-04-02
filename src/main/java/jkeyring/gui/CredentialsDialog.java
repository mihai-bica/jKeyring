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

package jkeyring.gui;

import java.awt.GridLayout;
import java.io.Console;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import jkeyring.gui.UserInputException;

public class CredentialsDialog {
	byte[] password;
	byte[] usermail;
	byte[] secondPasswd;
	String KEY_IDENTIFIER;
	Mode mode;

	public enum Mode {
		GUI, CLI, AUTO
	}

	public CredentialsDialog() {
		mode = Mode.AUTO;
	}

	public CredentialsDialog(Mode mode) {
		if (mode != null)
			this.mode = mode;
		else
			this.mode = Mode.AUTO;
	}

	private void validateCredentials() throws Exception {
		if (usermail == null || usermail.length == 0) {
			throw new UserInputException("The username cannot be empty.");
		}

		if (password == null || password.length == 0 || secondPasswd == null
				|| secondPasswd.length == 0) {
			throw new UserInputException("The password field cannot be empty.");
		}

		if (!Arrays.equals(password, secondPasswd)) {
			throw new UserInputException("Passwordws do not patch. Please try again.");
		}
	}

	private void showGUI() throws Exception {
		JPanel userPanel = new JPanel();
		userPanel.setLayout(new GridLayout(3, 2));
		JLabel usernameLbl = new JLabel("Username:");
		JLabel passwordLbl = new JLabel("Password:");
		JLabel repasswdLbl = new JLabel("Repeat Password:");
		JTextField usernameFLd = new JTextField();
		JPasswordField passwordFld = new JPasswordField();
		JPasswordField rePasswdFld = new JPasswordField();
		userPanel.add(usernameLbl);
		userPanel.add(usernameFLd);
		userPanel.add(passwordLbl);
		userPanel.add(passwordFld);
		userPanel.add(repasswdLbl);
		userPanel.add(rePasswdFld);
		int input = JOptionPane.showConfirmDialog(null, userPanel,
				"Enter your password:", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (input == 0) { // OK Button = 0
			usermail = new String(usernameFLd.getText()).getBytes();
			password = new String(passwordFld.getPassword()).getBytes();
			secondPasswd = new String(rePasswdFld.getPassword()).getBytes();
			validateCredentials();
		} else {
			throw new UserInputException("User cancelled.");
		}
	}

	private void showCLI() throws Exception {
		Console console = System.console();
		if (console == null) {
			throw new Exception("CLI interface is not supported.");
		}
		usermail = console.readLine("\nUsername: ")
				.getBytes();
		password = new String(console.readPassword("Password:")).getBytes();
		secondPasswd = new String(console.readPassword("Repeat Password:"))
				.getBytes();
		validateCredentials();
	}

	public void showUsrPsswdDialog() throws Exception {
		switch (this.mode) {
		case AUTO:
			if (System.console() == null)
				showGUI();
			else
				showCLI();
			break;
		case GUI:
			showGUI();
			break;
		case CLI:
			showCLI();
			break;
		default:
			throw new Exception("Unknown mode.");
		}
	}

	public byte[] getPassword() {
		return this.password;
	}

	public byte[] getUser() {
		return this.usermail;
	}

	public void eraseCredentials() {
		byte zero = 0;
		Arrays.fill(password, zero);
		Arrays.fill(secondPasswd, zero);
		Arrays.fill(usermail, zero);
	}
}
