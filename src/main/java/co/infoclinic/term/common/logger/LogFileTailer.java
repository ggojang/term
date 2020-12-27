package co.infoclinic.term.common.logger;

import java.io.File;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogFileTailer implements Runnable {
	
	Logger log = LoggerFactory.getLogger(LogFileTailer.class);
	
	private int sleepTime = 2000;
	private File logFile = null;
	private long lastLine = 0;
	LogFileTailerListener logFileTailerListener = null;
	
	public LogFileTailer(String path, long lastLine, LogFileTailerListener listener) {
		this.logFile = new File(path);
		this.lastLine = lastLine;
		this.logFileTailerListener = listener;
	}
	
	@Override
	public void run() {
		try {
			
			while(!Thread.currentThread().isInterrupted()) {
				Thread.sleep(sleepTime);
				long fileLength = logFile.length();
				if(fileLength > lastLine) {
					RandomAccessFile readWriteFileAccess = new RandomAccessFile(logFile, "rw");
					readWriteFileAccess.seek(lastLine);
					String line = null;
					while((line = readWriteFileAccess.readLine()) != null) {
						//this.logFileTailerListener.callBack(line);
					}
					lastLine = readWriteFileAccess.getFilePointer();
					readWriteFileAccess.close();
				}
			}
			
		} catch (Exception e) {
			Thread.interrupted();
			log.debug(e.getMessage());
		} finally {
			log.debug("Stop log tail!");
		}
			
	}

}
