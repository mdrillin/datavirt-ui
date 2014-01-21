package org.jboss.datavirt.ui.server.services.util;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.jboss.datavirt.ui.client.shared.services.StringUtil;

public class FilterUtil {

	/**
	 * Tests if the given string matches the filter.
	 *
	 * @param inputStr
	 * @param filter
	 * @return true if matches and false if either null or no match
	 */
	public static boolean matchFilter(String inputStr, String filter) {

		if (inputStr == null)
			return false;
		
		if(StringUtil.isEmpty(filter)) {
			filter = "*";
		} else {
			if(!filter.endsWith("*")) {
				filter += "*";
			}
		}

		StringBuffer f = new StringBuffer();

		for (StringTokenizer st = new StringTokenizer(filter, "?*", true); st.hasMoreTokens();) {
			String t = st.nextToken();
			if (t.equals("?"))
				f.append(".");
			else if (t.equals("*"))
				f.append(".*");
			else
				f.append(Pattern.quote(t));
		}
		return inputStr.matches(f.toString());
	}


}
