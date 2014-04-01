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

package jkeyring.impl.gnome;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jna.Pointer;

import jkeyring.KeyringException;
import jkeyring.intf.IKeyring;
import jkeyring.impl.Base64;
import static jkeyring.impl.gnome.GnomeKeyringLibrary.*;

public class GnomeProvider implements IKeyring {
    private static final String KEY = "key"; // NOI18N

    public @Override boolean enabled() {
        boolean envVarSet = false;
        for (String key : System.getenv().keySet()) {
            if (key.startsWith("GNOME_KEYRING_")) { // NOI18N
                envVarSet = true;
                break;
            }
        }
        if (!envVarSet) {
            return false;
        }
        String appName = "jkeyring";
        try {
            // Need to do this somewhere, or we get warnings on console.
            // Also used by confirmation dialogs to give the app access to the login keyring.
            LIBRARY.g_set_application_name(appName);
            if (!LIBRARY.gnome_keyring_is_available()) {
                return false;
            }
            // Make sure the library is bound by writing, reading and deleting a test key
            save("jKeyring-test", new byte[]{'0'}, null);
            read("jKeyring-test");
            delete("jKeyring-test");
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    public @Override byte[] read(String key) throws KeyringException {
        Pointer[] found = new Pointer[1];
        Pointer attributes = LIBRARY.g_array_new(0, 0, GnomeKeyringAttribute_SIZE);
        try {
            LIBRARY.gnome_keyring_attribute_list_append_string(attributes, KEY, key);
            error(GnomeKeyringLibrary.LIBRARY.gnome_keyring_find_items_sync(GNOME_KEYRING_ITEM_GENERIC_SECRET, attributes, found));
        } finally {
            LIBRARY.gnome_keyring_attribute_list_free(attributes);
        }
        if (found[0] != null) {
            try {
                if (LIBRARY.g_list_length(found[0]) > 0) {
                    GnomeKeyringFound result = LIBRARY.g_list_nth_data(found[0], 0);
                    if (result != null) {
                        if (result.secret != null) {
                            return Base64.decode(result.secret);
                        } else {
                            delete(key);
                        }
                    }
                }
            } catch (IOException e) {
		throw new KeyringException(e);
            } finally {
                LIBRARY.gnome_keyring_found_list_free(found[0]);
            }
        }
        return null;
    }

    public @Override void save(String key, byte[] data, String description) throws KeyringException {
        Pointer attributes = LIBRARY.g_array_new(0, 0, GnomeKeyringAttribute_SIZE);
        try {
            LIBRARY.gnome_keyring_attribute_list_append_string(attributes, KEY, key);
            int[] item_id = new int[1];
            error(GnomeKeyringLibrary.LIBRARY.gnome_keyring_item_create_sync(
                    null, GNOME_KEYRING_ITEM_GENERIC_SECRET, description != null ? description : key, attributes, Base64.encodeBytes(data), true, item_id));
        } finally {
            LIBRARY.gnome_keyring_attribute_list_free(attributes);
        }
    }

    public @Override void delete(String key) throws KeyringException {
        Pointer[] found = new Pointer[1];
        Pointer attributes = LIBRARY.g_array_new(0, 0, GnomeKeyringAttribute_SIZE);
        try {
            LIBRARY.gnome_keyring_attribute_list_append_string(attributes, KEY, key);
            error(GnomeKeyringLibrary.LIBRARY.gnome_keyring_find_items_sync(GNOME_KEYRING_ITEM_GENERIC_SECRET, attributes, found));
        } finally {
            LIBRARY.gnome_keyring_attribute_list_free(attributes);
        }
        if (found[0] == null) {
            return;
        }
        int id;
        try {
            if (LIBRARY.g_list_length(found[0]) > 0) {
                GnomeKeyringFound result = LIBRARY.g_list_nth_data(found[0], 0);
                id = result.item_id;
            } else {
                id = 0;
            }
        } finally {
            LIBRARY.gnome_keyring_found_list_free(found[0]);
        }
        if (id > 0) {
            if ("SunOS".equals(System.getProperty("os.name")) && "5.10".equals(System.getProperty("os.version"))) { // #185698
                save(key, new byte[0], null); // gnome_keyring_item_delete(null, id, null, null, null) does not seem to do anything
            } else {
                error(GnomeKeyringLibrary.LIBRARY.gnome_keyring_item_delete_sync(null, id));
            }
        }
    }

    private static String[] ERRORS = {
        "OK", // NOI18N
        "DENIED", // NOI18N
        "NO_KEYRING_DAEMON", // NOI18N
        "ALREADY_UNLOCKED", // NOI18N
        "NO_SUCH_KEYRING", // NOI18N
        "BAD_ARGUMENTS", // NOI18N
        "IO_ERROR", // NOI18N
        "CANCELLED", // NOI18N
        "KEYRING_ALREADY_EXISTS", // NOI18N
        "NO_MATCH", // NOI18N
    };

    private static void error(int code) throws KeyringException {
	switch(code) {
	  case 0:
	  case 9:
	    break;
	  default:
            throw new KeyringException("gnome-keyring error: " + ERRORS[code]);
        }
    }
}
