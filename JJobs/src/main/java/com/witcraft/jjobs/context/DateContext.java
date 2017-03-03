package com.witcraft.jjobs.context;

import com.witcraft.jjobs.actions.Matching;
import static com.witcraft.jjobs.criteria.AbstractCriterion.applyDateRules;
import com.witcraft.jjobs.helpers.DateBias;
import com.witcraft.jjobs.helpers.DatePrecision;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;
import org.w3c.dom.Node;

/**
 *
 * @author Jason Redding
 * @param <T>
 */
public abstract class DateContext<T> extends CriteriaContext<T, LocalDateTime> {

	private static final ZoneOffset zoneOffset;
	private final DatePrecision truncateTo;
	private final DateBias bias;

	static {
		zoneOffset = ZoneOffset.systemDefault().getRules().getOffset(Instant.now());
	}

	/**
	 *
	 * @param matching
	 * @param node
	 */
	public DateContext(Matching<T> matching, Node node) {
		super(matching, node);
		truncateTo = DatePrecision.from(getAttribute("truncate-to"));
		bias = DateBias.valueOf(getAttribute("bias", DateBias.START.name()).toUpperCase());
	}

	/**
	 *
	 * @return
	 */
	public DatePrecision getTruncateTo() {
		return truncateTo;
	}

	/**
	 *
	 * @return
	 */
	public DateBias getBias() {
		return bias;
	}

	/**
	 *
	 * @param context
	 *
	 * @return
	 */
	@Override
	public final LocalDateTime getContextValue(T context) {
		return applyDateRules(getContextValue(context, zoneOffset), getBias(), getTruncateTo());
	}

	/**
	 *
	 * @param context
	 * @param zone
	 *
	 * @return
	 */
	public abstract LocalDateTime getContextValue(T context, ZoneOffset zone);

	/**
	 *
	 * @param file
	 *
	 * @return
	 */
	public static LocalDateTime getFileDateCreated(File file) {
		return getFileDateCreated(file, zoneOffset);
	}

	public static LocalDateTime getFileDateModified(File file) {
		return getFileDateModified(file, zoneOffset);
	}

	/**
	 *
	 * @param file
	 * @param offset
	 *
	 * @return
	 */
	public static LocalDateTime getFileDateCreated(File file, ZoneOffset offset) {
		long fmd;
		try {
			BasicFileAttributeView fav = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class);
			BasicFileAttributes fa = fav.readAttributes();
			fmd = fa.creationTime().to(TimeUnit.SECONDS);
		} catch (IOException ex) {
			fmd = (file.lastModified() / 1000L);
		}
		return LocalDateTime.ofEpochSecond(fmd, 0, offset);
	}

	/**
	 *
	 * @param file
	 * @param offset
	 *
	 * @return
	 */
	public static LocalDateTime getFileDateModified(File file, ZoneOffset offset) {
		long fmd;
		try {
			FileTime ft = Files.getLastModifiedTime(file.toPath());
			fmd = ft.to(TimeUnit.SECONDS);
		} catch (IOException ex) {
			fmd = (file.lastModified() / 1000L);
		}
		return LocalDateTime.ofEpochSecond(fmd, 0, offset);
	}
}
