package se.lolcalhost.xmplary.common.strategies;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPMessage;

public class LeafDispatchStrategy extends MessageDispatchStrategy {
	private boolean hasJoinedChat = false;
	private MultiUserChat muc;
	private Logger logger;
	private HashMap<String, Chat> chats = new HashMap<String, Chat>();
	

	public LeafDispatchStrategy(XMPMain main) {
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
			String asJSON = mess.asJSON();
			chats.get(jid).sendMessage(asJSON);
		} catch (XMPPException e) {
			logger.error("Couldn't dispatch message to Chat.");
		} catch (JSONException e) {
			logger.error("Couldn't dispatch message to Chat.");
		}
	}

	private void joinChat(String target) {
		Connection con = main.getConnection();
		ChatManager cm = con.getChatManager();
		Chat c = cm.createChat(target, null);
		chats.put(target, c);
//		cm.createChat(userJID, listener);
//		cm.addChatListener(new ChatManagerListener() {
//			
//			@Override
//			public void chatCreated(Chat arg0, boolean arg1) {
//				logger.info("Chat created with " + arg0.getParticipant() + " arg1: " + arg1);
//				arg0.addMessageListener(new MessageListener() {
//					@Override
//					public void processMessage(Chat arg0, Message arg1) {
//						logger.info("Got message in chat with " + arg0.getParticipant() + ": " + arg1.getBody());
//						try {
//							arg0.sendMessage("Hej på dig med du!");
//						} catch (XMPPException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//				});
//				
//			}
//		});
	}

}
