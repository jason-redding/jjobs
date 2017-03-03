package com.witcraft.jjobs.helpers;

import com.witcraft.jjobs.Job;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jason Redding
 */
public class LogHandler implements AutoCloseable {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");

	/**
	 *
	 * @param job
	 *
	 * @return
	 */
	public static final LogHandler forJob(Job job) {
		if (job != null) {
			return new LogHandler(job);
		}
		return null;
	}

	private PrintWriter writer;
	private Job job;

	private LogHandler(Job job) {
		this.job = job;
		try {
			writer = new PrintWriter(new FileWriter(new File(this.job.getLoggingDirectory(), "jjobs-" + sdf.format(new Date()) + "-" + this.job.getName().replaceAll("\\s+", "_") + ".log"), true), true);
		} catch (IOException ex) {
			Logger.getLogger(LogHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 *
	 * @param message
	 * @param parameters
	 */
	public void publish(Object message, Object... parameters) {
		writer.format(String.valueOf(message), parameters);
	}

	@Override
	public void close() {
		if (writer != null) {
			writer.close();
		}
	}
}
