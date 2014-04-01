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

package jkeyring.intf;

import jkeyring.KeyringException;

/**
 * Provider for a keyring.
 */
public interface IKeyring {
    /**
     * Check whether this provider can be used in the current JVM session.
     * If integrating a native keyring, this should attempt to load related
     * libraries and check whether they can be found.
     *
     * @return true if this provider should be used, false if not
     */
    boolean enabled();

    /**
     * Read a key from the ring.
     * @param key the identifier of the key
     * @return its value if found (elements may be later nulled out), else null if not present
     */
    byte[] read(String key) throws KeyringException;

    /**
     * Save a key to the ring.
     * If it could not be saved, do nothing.
     * If the key already existed, overwrite the password.
     * @param key a key identifier
     * @param data the password or other sensitive information associated with the key
     *             (elements will be later nulled out)
     * @param description a user-visible description of the key (optional - may be null)
     */
    void save(String key, byte[] data, String description) throws KeyringException;

    /**
     * Delete a key from the ring.
     * If the key was not in the ring to begin with, do nothing.
     * @param key a key identifier
     */
    void delete(String key) throws KeyringException;
}
