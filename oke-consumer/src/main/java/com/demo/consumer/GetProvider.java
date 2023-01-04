/*Copyright (c) 2021 Oracle and/or its affiliates.
The Universal Permissive License (UPL), Version 1.0
Subject to the condition set forth below, permission is hereby granted to any
person obtaining a copy of this software, associated documentation and/or data
(collectively the "Software"), free of charge and under any and all copyright
rights in the Software, and any and all patent rights owned or freely
licensable by each licensor hereunder covering either (i) the unmodified
Software as contributed to or provided by such licensor, or (ii) the Larger
Works (as defined below), to deal in both
(a) the Software, and
(b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
one is included with the Software (each a "Larger Work" to which the Software
is contributed by such licensors),
without restriction, including without limitation the rights to copy, create
derivative works of, display, perform, and distribute the Software and make,
use, sell, offer for sale, import, export, have made, and have sold the
Software and the Larger Work(s), and to sublicense the foregoing rights on
either these or other terms.
This license is subject to the following condition:
The above copyright notice and either this complete permission notice or at
a minimum a reference to the UPL must be included in all copies or
substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.demo.consumer;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.function.Supplier;

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetProvider {

	static BasicAuthenticationDetailsProvider provider;
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static BasicAuthenticationDetailsProvider getProvider(String profile) {

//		BasicAuthenticationDetailsProvider provider = null;
		if(provider!=null)
			return provider;
		if ("LOCAL".equals(profile)) {
			Supplier<InputStream> privateKeySupplier = new SimplePrivateKeySupplier(Environment._PVT_KEY_FILE_PATH);
			provider = SimpleAuthenticationDetailsProvider.builder().tenantId(Environment._TENANT_ID)
					.userId(Environment._USER_ID).fingerprint(Environment._FINGER_PRINT)
					.privateKeySupplier((com.google.common.base.Supplier<InputStream>) privateKeySupplier).build();

		} else if(profile==null | "".equals(profile)) {
			try {
				log.info("Building InstancePrincipals ");
				provider = InstancePrincipalsAuthenticationDetailsProvider.builder().build();
			} catch (Exception e) {
				if (e.getCause() instanceof SocketTimeoutException || e.getCause() instanceof ConnectException) {
					log.info(
							"This sample only works when running on an OCI instance. Are you sure you�re running on an OCI instance? For more info see: https://docs.cloud.oracle.com/Content/Identity/Tasks/callingservicesfrominstances.htm");
					return provider;
				}
				throw e;
			}

		}
		return provider;
	}

}
