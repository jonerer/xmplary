package se.lolcalhost.xmplary.common.strategies;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPMessage;

public class ChatDispatchStrategy extends AbstractMessageDispatchStrategy {
	private boolean hasJoinedChat = false;
	private MultiUserChat muc;
	private Logger logger;
	private HashMap<String, Chat> chats = new HashMap<String, Chat>();
	

	public ChatDispatchStrategy(XMPMain main) {
		super(main);
		logger = Logger.getLogger(this.getClass());
	}

	@Override
	public void DispatchMessage(XMPMessage mess) {
		
		String jid = mess.getTarget().getJID();
		if (!chats.containsKey(jid)) {
			joinChat(jid);
		}
		try {
			String asJSON = mess.serialized();
			chats.get(jid).sendMessage(asJSON);
		} catch (XMPPException e) {
			logger.error("Couldn't dispatch message to Chat.");
		}
	}

	private void joinChat(String target) {
		Connection con = main.getConnection();
		ChatManager cm = con.getChatManager();
		Chat c = cm.createChat(target, null);
		chats.put(target, c);
	}

}
