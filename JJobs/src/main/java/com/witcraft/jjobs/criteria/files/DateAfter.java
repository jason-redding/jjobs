package com.witcraft.jjobs.criteria.files;

import com.witcraft.jjobs.actions.Matching;
import java.io.File;
import java.time.LocalDateTime;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public class DateAfter extends FileDateCriterion {

	/**
	 *
	 * @param matching
	 * @param node
	 */
	public DateAfter(Matching<File> matching, Node node) {
		super(matching, node);
	}

	/**
	 *
	 * @param file
	 * @param fileDate
	 *
	 * @return
	 */
	@Override
	protected boolean isMatch(File file, LocalDateTime fileDate) {
		return fileDate.isAfter(getDate(file));
	}

	@Override
	public String expression(File file, LocalDateTime date) {
		StringBuilder sb = new StringBuilder();
		LocalDateTime dt = getDate(file);
		if (dt != null) {
			boolean isMatched = isMatch(file, date);
			sb.append(isMatched ? '+' : '-');
			sb.append('[').append(getFullName()).append("] ");
			sb.append(date);
//			if (isMatched) {
//				sb.append(" is before ");
//			} else {
//				sb.append(" is after ");
//			}
//			sb.append(dt);
		}
		return sb.toString();
	}
}
