package se.lolcalhost.xmplary.common.strategies;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;

import se.lolcalhost.xmplary.common.XMPConfig;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;

public class MUCDispatchStrategy extends AbstractMessageDispatchStrategy {
	public static enum MUCRoomStyle {
		INPUT_OUTPUT, ONLY_INPUT, ONLY_OUTPUT
	}

	private boolean hasJoinedMuc = false;
	private MultiUserChat muc;
	private Logger logger;
	private MUCRoomStyle style;
	private XMPNode room;

	public MUCDispatchStrategy(XMPMain main, MUCRoomStyle style) throws SQLException {
		super(main);
		this.style = style;
		logger = Logger.getLogger(this.getClass());
		joinMuc();
	}

	@Override
	public void DispatchMessage(XMPMessage mess) throws SQLException {
		if (!hasJoinedMuc) {
			joinMuc();
		}
		if ((style == MUCRoomStyle.INPUT_OUTPUT
				|| style == MUCRoomStyle.ONLY_OUTPUT)) {
			try {
				if (mess.getTarget().equals(room)) {
					muc.sendMessage(mess.getRawContents());
				}
			} catch (XMPPException e) {
				logger.error("Couldn't dispatch message to MUC.");
			}
		}
	}

	@Override
	public void DispatchRawMessage(String msg) throws SQLException {
		if (!hasJoinedMuc) {
			joinMuc();
		}
		if (style == MUCRoomStyle.INPUT_OUTPUT
				|| style == MUCRoomStyle.ONLY_OUTPUT) {
			try {
				muc.sendMessage(msg);
			} catch (XMPPException e) {
				logger.error("Couldn't dispatch raw message to MUC.");

			}
		}
	};

	private void joinMuc() throws SQLException {
		final Connection con = main.getConnection();
		room = XMPNode.getRoom();
		if (con != null && con.isConnected() && con.isAuthenticated()) {
			logger.info("Connected to " + con.getServiceName() + " as JID "
					+ con.getUser());
			logger.info("Getting room info: " + room.getJID());
			muc = new MultiUserChat(con, room.getJID());
			try {
				RoomInfo info = MultiUserChat.getRoomInfo(con, room.getJID());
			} catch (XMPPException e) {
				logger.debug("Error 404 room with JID " + room.getJID()
						+ " doesn't exist. Creating it.");
				// probably only the gateway should do this.
				// also docs are at
				// http://www.igniterealtime.org/builds/smack/docs/latest/documentation/extensions/muc.html
				// Create a MultiUserChat using a Connection for a room
				// http://xmpp.org/registrar/formtypes.html för form types.
				try {
					muc.create(con.getUser());

					// Get the the room's configuration form
					Form form = muc.getConfigurationForm();
					// Create a new form to submit based on the original form
					Form submitForm = form.createAnswerForm();
					// Add default answers to the form to submit
					for (Iterator fields = form.getFields(); fields.hasNext();) {
						FormField field = (FormField) fields.next();
						// if (field.)
						if (!FormField.TYPE_HIDDEN.equals(field.getType())
								&& field.getVariable() != null) {
							// Sets the default value as the answer
							submitForm.setDefaultAnswer(field.getVariable());
						}
					}
					// Sets the new owner of the room
					List owners = new ArrayList();
					owners.add(con.getUser());
					submitForm.setAnswer("muc#roomconfig_roomname",
							"XMPlary debug output");
					submitForm.setAnswer("muc#roomconfig_roomdesc",
							" -- # YOLO --");
					// submitForm.setAnswer("muc#roomconfig_roomowners",
					// owners);
					// submitForm.setAnswer("muc#roomconfig_roomowners",
					// owners);
					submitForm.setAnswer("muc#roomconfig_persistentroom", true);
					submitForm.setAnswer("muc#roomconfig_enablelogging", false);
					// submitForm.setAnswer("x-muc#roomconfig_reservednick",
					// true);

					// Send the completed form (with default values) to the
					// server to configure the room
					muc.sendConfigurationForm(submitForm);
				} catch (XMPPException e1) {
					logger.error("Couldn't create chat room " + room.getJID()
							+ ". Exiting.", e1);
					System.exit(1);
				}
			}
			try {
				muc.join(XMPConfig.Name());
				// muc.changeSubject(" -- # YOLO --");
				RoomInfo info2 = MultiUserChat.getRoomInfo(con, room.getJID());
				logger.info("Joining " + room.getJID() + ". "
						+ info2.getOccupantsCount() + " occupant(s).");
				hasJoinedMuc = true;
				muc.addMessageListener(new PacketListener() {

					@Override
					public void processPacket(Packet arg0) {

						Class c = arg0.getClass();
						if (!arg0.getFrom().equals(con.getUser())) {
							if (arg0 instanceof Message) {
								if (style == MUCRoomStyle.INPUT_OUTPUT || style == MUCRoomStyle.ONLY_INPUT) {
									main.receivePacket(arg0);
									// logger.info("Message in the room: " +
									// arg0.getFrom() + ": " + arg0.toString());
								}
							}
						}
					}
				});
			} catch (XMPPException e) {
				logger.debug("Couldn't join room " + room.getJID(), e);
			}

		}
	}

}
