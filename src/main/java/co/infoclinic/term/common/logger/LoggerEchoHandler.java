package co.infoclinic.term.common.logger;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class LoggerEchoHandler  extends TextWebSocketHandler {
	
	@Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        TextMessage echoMessage = new TextMessage("ECHO :" + message.getPayload());
        session.sendMessage(echoMessage);
    }

}
