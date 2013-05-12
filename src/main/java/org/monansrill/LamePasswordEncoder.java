package org.monansrill;
/**
 * Copyright Chris Schaefer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.monansrill.password.PasswordEncoder;

public class LamePasswordEncoder implements PasswordEncoder {

	@Override
	public String encodePassword(String rawPassword)
			throws IllegalArgumentException {
		return null;
	}

	@Override
	public boolean isValidPassword(String rawPassword, String encodedPassword)
			throws IllegalArgumentException {
		if(rawPassword.equals("yes")) return true;
		return false;
	}

	@Override
	public boolean canDecodePassword(String encodedPassword) {
		return true;
	}

}
