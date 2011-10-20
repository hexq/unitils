/*
 * Copyright 2008,  Unitils.org
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

package org.unitils.io.conversion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * 
 * @author Jeroen Horemans
 * @author Thomas De Rycke
 * 
 * @since 3.3
 * 
 */
@RunWith(Parameterized.class)
public class PropertiesConversionStrategyTest {

	private PropertiesConversionStrategy conversion = new PropertiesConversionStrategy();

	private String input;
	private String encoding;

	@Parameters
	public static Collection<Object[]> getParameters() {
		List<Object[]> l = new ArrayList<Object[]>();
		l.add(new Object[] { "test=€é*ù¨´ù]:~e;[=+", "utf-8" });
		l.add(new Object[] { "test=€é*ù¨´ù]:~e;[=+", "Cp1252" });
		l.add(new Object[] { "test=€é*ù¨´ù]:~e;[=+", "utf-16" });
		// Apparently the ISO-8859-1 does not support the euro sign.
		l.add(new Object[] { "test=é*ù¨ù]:~e;[=+:", "ISO8859_1" });

		return l;
	}

	public PropertiesConversionStrategyTest(String input, String encoding) {
		this.input = input;
		this.encoding = encoding;
	}

	@Test
	public void testSuccesWithoutEncoding() throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes(encoding));

		Properties result = conversion.readContent(inputStream, encoding);

		Assert.assertEquals("{" + input + "}", result.toString());

	}
}
