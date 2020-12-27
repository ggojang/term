package co.infoclinic.term.common.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import co.infoclinic.term.common.utils.PropertiesUtil;

//@PropertySource("classpath:use.configure.properties")
public class LogFileTailerListener extends TextWebSocketHandler {
	
	Logger log = LoggerFactory.getLogger(LogFileTailerListener.class);
	
	//@Value("#{useConfig['ws.log.file']}")
	//private String WS_LOG_FILE;
	
	private int sleepTime = 2000;
	private File logFile = null;
	private long lastLine = 0;
	private boolean shouldRun = true;
	private List<WebSocketSession> sessionList = new ArrayList<WebSocketSession>();
	
	@Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
		
		shouldRun = true;
		
		PropertiesUtil prop = new PropertiesUtil();
		String logPath = prop.getPropValue("ws.log.file").toString();
		String fileName = null;
		
		HttpHeaders headers = session.getHandshakeHeaders();
		List<String> xforwards = headers.get("X-FORWARDED-FOR");
		
		log.debug("1 {}", headers.get("X-FORWARDED-FOR"));
		
		log.debug("2 {}", headers.get("x-forwarded-for"));
		
		log.debug("3 {}", headers.get("X-Forwarded-For"));
		
		if (xforwards != null && xforwards.size() > 0) {
			fileName = xforwards.get(0);
		} else {
			fileName = session.getRemoteAddress().getAddress().getHostAddress();
		}
		
		//String path = logPath + File.separatorChar + session..getRemoteAddress().getAddress().getHostAddress() + ".log";
		String path = logPath + File.separatorChar + fileName + ".log";
		
		log.debug("WS START ================>"+path);
		log.debug("Get RemoteAddress:{}", session.getRemoteAddress());
		log.debug("Get LocalAddress:{}", session.getLocalAddress());
		log.debug("Get Headers:{}", session.getHandshakeHeaders());
       logFile = new File(path);
       if(!logFile.exists()) {
    	   FileWriter fw = new FileWriter(logFile, true) ;
    	   fw.write("Concept Editing log Start");
    	   fw.flush();
    	   fw.close();
    	   lastLine = 1;
        }
       else {
    	   lastLine = logFile.length();
       }
		sessionList.add(session);
	}
	
	@Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		log.debug("{}로 부터 {} 받음", session.getId(), message.getPayload());
		log.debug("logFile.length()==============="+logFile.length());
		for(WebSocketSession sess : sessionList) {
			log.debug("Session Data ====> URL:{}       ID:{}       LocalAddr:{}         remoteAddr:{}", session.getUri(), session.getId(), session.getLocalAddress(), session.getRemoteAddress());
			if(sess.getId().equals(session.getId()) && sess.isOpen() && logFile.exists()) {
				try {
					log.debug("Session&Sess Equal!!! ====> URL:{}       ID:{}       LocalAddr:{}         remoteAddr:{}", sess.getUri(), sess.getId(), sess.getLocalAddress(), sess.getRemoteAddress());
					log.debug(shouldRun ? "TRUE":"FALSE");
					while(shouldRun) {
						
						Thread.sleep(sleepTime);
						long fileLength = logFile.length();
						log.debug("Should Run  FileLen:{}, LastLine:{}, LogPath:{}", fileLength, lastLine, logFile.getPath());
						if(fileLength > lastLine) {
							log.debug("Should Run  2");
							RandomAccessFile readWriteFileAccess = new RandomAccessFile(logFile, "rw");
							readWriteFileAccess.seek(lastLine);
							String line = null;
							while((line = readWriteFileAccess.readLine()) != null) {
								String time = line.substring(0, 23);
								String msg = line.substring(line.lastIndexOf("~-")+3);
								sess.sendMessage(new TextMessage("[" + time + "] "+msg));
							}
							lastLine = readWriteFileAccess.getFilePointer();
							readWriteFileAccess.close();
						}
					}
				} catch(Exception e) {
					shouldRun = false;
					log.error("WS ERROR", e);
				} finally {
					log.debug("Stop log tail!");
				}
				break;
			}
		}
	}
	
	@Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		shouldRun = false;
		sessionList.remove(session);
	}
	
}
