package com.witcraft.jjobs.criteria.files;

import com.witcraft.jjobs.actions.Matching;
import java.io.File;
import java.math.BigDecimal;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 */
public class MaxSize extends FileSizeCriterion {

	/**
	 *
	 * @param matching
	 * @param node
	 */
	public MaxSize(Matching<File> matching, Node node) {
		super(matching, node);
	}

	/**
	 *
	 * @param size
	 *
	 * @return
	 */
	@Override
	protected boolean isMatch(BigDecimal size) {
		return (size == null || getFileSizeInBytes().compareTo(size) >= 0);
	}

//	@Override
//	public String toString() {
//		StringBuilder sb = new StringBuilder();
//		sb.append("size <= ").append(valueToString());
//		return sb.toString();
//	}
	@Override
	public String expression(BigDecimal size) {
		boolean isMatched = isMatch(size);
		StringBuilder sb = new StringBuilder();
		sb.append(isMatched ? '+' : '-');
		sb.append("[").append(getFullName()).append("] ");
		sb.append(size);
//		if (isMatched) {
//			sb.append(" >= ");
//		} else {
//			sb.append(" < ");
//		}
//		sb.append(getFileSizeInBytes());
		return sb.toString();
	}
}
