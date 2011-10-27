package org.ovirt.engine.ui.userportal.client.uicommonext;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.ovirt.engine.ui.uicommon.*;

public class LoggerImpl implements ILogger {

	private Logger logger = Logger.getLogger(LoggerImpl.class.getName());

	@Override
	public void Debug(String message) {
		logger.fine(message);
	}

	@Override
	public void Error(String message, RuntimeException ex) {
		logger.log(Level.SEVERE, message, ex);
	}

	@Override
	public void Info(String message) {
		logger.info(message);
	}

	@Override
	public void Warn(String message) {
		logger.log(Level.WARNING,message);
	}

	public void setLogLevel(String level) {
		logger.setLevel(Level.parse(level));
	}
}
