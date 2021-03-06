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
package org.unitils.site;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitils.core.UnitilsException;

/**
 * Utility that post-processes the html files that were generated by maven and adds the piece of javascript
 * that is required by google analytics in order to monitor the site traffic.
 *  
 * @author Filip Neven
 * @author Tim Ducheyne
 */
public class UnitilsSitePostprocessor {

	private static final Log logger = LogFactory.getLog(UnitilsSitePostprocessor.class);
	
	public static final String GOOGLE_ANALYTICS_JAVASCRIPT =
		"<script src=\"http://www.google-analytics.com/urchin.js\" type=\"text/javascript\">\n" +
		"</script>\n" +
		"<script type=\"text/javascript\">\n" +
		"_uacct = \"${userAccountNr}\";\n" +
		"urchinTracker();\n" +
		"</script>\n";

	private String siteDirName;
    private String userAccountNr;

    public UnitilsSitePostprocessor(String siteDirName, String userAccountNr) {
		this.siteDirName = siteDirName;
        this.userAccountNr = userAccountNr;
    }
	
	@SuppressWarnings("unchecked")
	public void postProcessSite() throws IOException {
		File siteDir = new File(siteDirName);
		if (!siteDir.exists() || !siteDir.isDirectory()) {
			throw new UnitilsException("Site directory should be an existing, non-empty directory");
		}
		
		Collection<File> files = FileUtils.listFiles(siteDir, new String[] {"html", "htm"}, true);
		for (File file : files) {
			addGoogleAnalyticsJavascript(file);
		}
	}

	public void addGoogleAnalyticsJavascript(File file) throws IOException {
		logger.info("Adding google analytics javascript to " + file);
		
		String content = FileUtils.readFileToString(file);
		Pattern p = Pattern.compile("</body>", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(content);
		StringBuffer newContentBuffer = new StringBuffer();
		if (m.find()) {
			m.appendReplacement(newContentBuffer, "\n\n" + getGoogleAnalyticsJavaScript() + "\n</body>");
			m.appendTail(newContentBuffer);
			FileUtils.writeStringToFile(file, newContentBuffer.toString());
		} else {
			logger.error("Could not find </body> tag in file " + file.getAbsolutePath());
		}
	}

    private String getGoogleAnalyticsJavaScript() {
        return GOOGLE_ANALYTICS_JAVASCRIPT.replace("${userAccountNr}", userAccountNr);
    }

    public static void main(String[] args) throws Exception {
		String siteDir = args[0];
        String userAccountNr = args[1];
		new UnitilsSitePostprocessor(siteDir, userAccountNr).postProcessSite();
	}
	
}
