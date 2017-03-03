package com.witcraft.jjobs.criteria.files;

import com.witcraft.jjobs.actions.Matching;
import java.io.File;
import java.time.LocalDateTime;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public class DateBefore extends FileDateCriterion {

	/**
	 *
	 * @param matching
	 * @param node
	 */
	public DateBefore(Matching<File> matching, Node node) {
		super(matching, node);
	}

	/**
	 *
	 * @param fileDate
	 *
	 * @return
	 */
	@Override
	protected boolean isMatch(File file, LocalDateTime fileDate) {
		return fileDate.isBefore(getDate(file));
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
//				sb.append(" is after ");
//			} else {
//				sb.append(" is before ");
//			}
//			sb.append(dt);
		}
		return sb.toString();
	}
}
