package se.lolcalhost.xmplary.xmpback.strategies;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.Alarm;
import se.lolcalhost.xmplary.common.Status;
import se.lolcalhost.xmplary.common.XMPCrypt;
import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPDataPoint.DataPointField;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;
import se.lolcalhost.xmplary.common.strategies.IMessageReceiverStrategy;
import se.lolcalhost.xmplary.xmpback.commands.OperatorInputCommand;
import se.lolcalhost.xmplary.xmpback.dump.DatapointDataset;
import se.lolcalhost.xmplary.xmpback.dump.DatapointGraph;

import com.j256.ormlite.stmt.Where;

public class OperatorInputStrategy implements IMessageReceiverStrategy {
	public interface InputCommandStrategy {
		public void HandleCommand(Message m) throws SQLException;
	}

	HashMap<OperatorCommand, InputCommandStrategy> handlers = new HashMap<OperatorCommand, InputCommandStrategy>();
	private XMPMain main;

	public enum OperatorCommand {
		IsRegistered, Register, Unregister,

		Hello, Echo,

		Help,

		RequestDataPoints, DumpData, ListNodes,
		
		GetWelderConfig, SetWelderConfigVar,
		
		GetDump, GetStatusList, GetAlarmsList
	}

	public OperatorInputStrategy(XMPMain main) {
		this.main = main;
		addHandlers();
	}

	@Override
	public void PreparseReceiveMessage(Message p) throws SQLException {
		OperatorCommand c = null;

		if (((Message) p).getBody().charAt(0) == '!') {
			Message m = (Message) p;
			// c = OperatorCommand.valueOf(m.getBody().split(" ")[0]
			// .substring(1));
			String requestedCmd = m.getBody().split(" ")[0].substring(1)
					.toLowerCase();
			for (OperatorCommand cmd : OperatorCommand.values()) {
				String cmdname = cmd.name().toLowerCase();
				if (cmdname.equals(requestedCmd)) {
					c = cmd;
				}
			}
			if (c == null) {
				return;
			}

			XMPNode operator = XMPNode.getOperator();
			if (operator == null) {
				String opname = p.getFrom().split("@")[0];
				operator = new XMPNode();
				operator.setType(NodeType.operator);
				operator.setName(opname);
				operator.save();
				XMPMessage.tellOperator("No operator registered, registering "
						+ opname + ".");
				main.dispatchRaw("No operator registered, registering "
						+ opname + ".");
			}
			XMPNode n = XMPNode.getByJID(p.getFrom());
			// String operjid = operator.getJID();
			if (handlers.get(c) != null && n.getType() == NodeType.operator) {
				OperatorInputCommand cmd = new OperatorInputCommand(main, m, handlers.get(c));
				cmd.schedule();
//				handlers.get(c).HandleCommand(m);
			} else {
				XMPMessage.tellOperator("Unknown command, or you're not operator.");
			}
		}
	}

	private void handleException(Exception e) {
		e.printStackTrace();
	}

	@Override
	public void ReceiveMessage(XMPMessage msg) throws SQLException {
		switch (msg.getType()) {
		case IsRegistered:
			XMPMessage.tellOperator("Is registered? " + msg.getRawContents());
			break;
		default:
			String format = String.format("Received message of type %s. Verified: " + msg.isVerified() + ". (o->f->t). (%s->%s->%s) Contents: %s", 
					msg.getType().toString(),
					msg.getOrigin().getName(), msg.getFrom().getName(), msg.getTarget().getName(), msg.getRawContents());
			XMPMessage.tellOperator(format);
			break;
		}
	}

	private void addHandlers() {
		handlers.put(OperatorCommand.Help, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) throws SQLException {
				XMPMessage.tellOperator("Here is a list of commands: ");
				for (OperatorCommand node : OperatorCommand.values()) {
					XMPMessage.tellOperator("!" + node.name());
				}
			}
		});
		handlers.put(OperatorCommand.Hello, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) throws SQLException {
				XMPMessage.tellOperator("Y halo thar mister " + m.getFrom());
			}
		});
		handlers.put(OperatorCommand.ListNodes, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) throws SQLException {
				for (XMPNode node : XMPDb.Nodes) {
					XMPMessage.tellOperator("Node: " + node.getName()
							+ " (type: " + node.getType().name() + ")");
				}
			}
		});
		handlers.put(OperatorCommand.DumpData, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) throws SQLException {
				String[] split = m.getBody().split(" ");
				List<XMPNode> nodes = new ArrayList<XMPNode>();
				DataPointField field = null;
				if (split.length > 1) {
					try {
						field = DataPointField.valueOf(split[1]);
					} catch (IllegalArgumentException e) {
					}
				}
				if (field == null) {
					XMPMessage
							.tellOperator("You didn't supply a field name. Valid names are:");
					for (DataPointField tempfield : DataPointField.values()) {
						XMPMessage.tellOperator(tempfield.name());
					}
					return;
				}

				if (split.length > 2) {
					XMPNode byJID = XMPNode.getByJID(split[2]);
					if (byJID != null) {
						nodes.add(byJID);
					} else {
						XMPMessage
						.tellOperator("\""+split[2]+"\" Is not a valid (leaf) node. See valid with !ListNodes.");
						return;
					}
				}
				if (nodes.size() == 0) {
					nodes = XMPNode.getLeaves();
				}
				XMPMessage.tellOperator("Dumping datapoints from "
						+ nodes.size() + " node(s).");
				DatapointGraph g = new DatapointGraph(XMPDataPoint.explanations.get(field), field);
				long numPoints = 0;
				for (XMPNode xmpNode : nodes) {
					Collection<?> datapoints = xmpNode.getDatapoints();
					numPoints += datapoints.size();
					DatapointDataset ds = new DatapointDataset(datapoints);
					ds.setOriginNode(xmpNode);
					g.addDataset(ds);
				}
				XMPMessage.tellOperator("In total " + numPoints + " datapoints.");
				try {
					g.generate();
				} catch (Exception e) {
					System.out.println("is problem: ");
				}
				g.show();
				// does the folder "screens" exist?
				File s = new File("screens");
				if (!s.exists()) {
					if (!s.mkdir()) {
						XMPMessage.tellOperator("Couldn't create screens dir.");
					}
				} else if (s.isFile()) {
					XMPMessage.tellOperator("'screens' already exists but is a file. please move it to save screenshots.");
				}
				s = new File("screens"); // dunno if this is rly neccesary.
				if (s.exists()) {
					File f = new File("screens/" + new Date().toGMTString().replace(":", ".")+".png");
					try {
						g.save(f);
						XMPMessage.tellOperator("Saved file as "
								+ f.getAbsolutePath() + ".");
					} catch (IOException e) {
						XMPMessage.tellOperator("Couldn't save the file to"
								+ f.getAbsolutePath() + ".");
					}
				}
			}
		});
		handlers.put(OperatorCommand.GetStatusList, new InputCommandStrategy() {
			
			@Override
			public void HandleCommand(Message m) throws SQLException {
				String[] split = m.getBody().split(" ");
				List<XMPNode> nodes = new ArrayList<XMPNode>();
				if (split.length > 1) {
					XMPNode byJID = XMPNode.getByJID(split[1]);
					if (byJID != null) {
						nodes.add(byJID);
					} else {
						XMPMessage
						.tellOperator("\""+split[1]+"\" Is not a valid (leaf) node. See valid with !ListNodes. Fill list with RequestDataPoints.");
						return;
					}
				}
				if (nodes.size() == 0) {
					nodes = XMPNode.getLeaves();
				}
				XMPMessage.tellOperator("Dumping status updates from "
						+ nodes.size() + " node(s).");
				
				try {
					Where<XMPMessage, String> eq = XMPDb.Messages.queryBuilder().where().eq(XMPMessage.TYPE, MessageType.WelderStatus).and();
					eq = eq.in(XMPMessage.ORIGIN, nodes);
					for (XMPMessage mess : eq.query()) {
						Status s = (Status) mess.getContents();
						XMPDb.Nodes.refresh(mess.getOrigin());
						XMPMessage.tellOperator(mess.getTime().toLocaleString() + " Status change: " + mess.getOrigin().getName() + ":" + s.getStatus().name() + ": " + s.getStatusMessage());
					}
				} catch (SQLException e) {
					XMPMessage.tellOperator("Problem in lookup: " + e);
				}
				catch (JSONException e) {
					XMPMessage.tellOperator("Problem in lookup: " + e);
				}
			}
		});
		handlers.put(OperatorCommand.GetAlarmsList, new InputCommandStrategy() {
			
			@Override
			public void HandleCommand(Message m) throws SQLException {
				XMPMessage.tellOperator("Ok...");
				String[] split = m.getBody().split(" ");
				List<XMPNode> nodes = new ArrayList<XMPNode>();
				if (split.length > 1) {
					XMPNode byJID = XMPNode.getByJID(split[1]);
					if (byJID != null) {
						nodes.add(byJID);
					} else {
						XMPMessage
						.tellOperator("\""+split[1]+"\" Is not a valid (leaf) node. See valid with !ListNodes. Fill list with RequestDataPoints.");
						return;
					}
				}
				if (nodes.size() == 0) {
					nodes = XMPNode.getLeaves();
				}
				XMPMessage.tellOperator("Dumping alarms from "
						+ nodes.size() + " node(s).");
				
				try {
					Where<XMPMessage, String> eq = XMPDb.Messages.queryBuilder().where().eq(XMPMessage.TYPE, MessageType.Alarm).and();
					eq = eq.in(XMPMessage.ORIGIN, nodes);
					for (XMPMessage mess : eq.query()) {
						Alarm s = (Alarm) mess.getContents();
						XMPDb.Nodes.refresh(mess.getOrigin());
						XMPMessage.tellOperator(mess.getTime().toLocaleString() + " Alarm type: " + mess.getOrigin().getName() + ":" + s.getType().name() + ": " + s.getErrorMessage());
					}
				} catch (SQLException e) {
					XMPMessage.tellOperator("Problem in lookup: " + e);
				}
				catch (JSONException e) {
					XMPMessage.tellOperator("Problem in lookup: " + e);
				}
			}
		});
		handlers.put(OperatorCommand.GetWelderConfig, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) throws SQLException {
				XMPMessage.tellOperator("Ok let me send request... ");
				XMPMessage request = new XMPMessage(MessageType.GetWelderConfig);
				String targetName = m.getBody().split(" ")[1];
				request.setContents("bvlah");
				request.setTarget(XMPNode.getByJID(targetName));
				request.send();
			}
		});
		handlers.put(OperatorCommand.SetWelderConfigVar, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) throws SQLException {
				XMPMessage.tellOperator("Ok let me send request... ");
				XMPMessage request = new XMPMessage(MessageType.SetWelderConfigVar);
				String targetName = m.getBody().split(" ")[1];
				JSONObject json = new JSONObject();
				String varName = m.getBody().split(" ")[2];
				String val = m.getBody().split(" ")[3];
				try {
					json.put("key", varName);
					json.put("value", val);
				} catch (JSONException e) {
					System.out.println("This will never happen. " + e);
				}
				request.setContents(json);
				request.setTarget(XMPNode.getByJID(targetName));
				request.send();
			}
		});
		handlers.put(OperatorCommand.GetDump, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) throws SQLException {
				XMPMessage.tellOperator("Ok let me send request... ");
				XMPMessage request = new XMPMessage(MessageType.DumpRequest);
				String targetName = m.getBody().split(" ")[1];
				request.setTarget(XMPNode.getOrCreateByJID(targetName));
				request.send();
			}
		});
		handlers.put(OperatorCommand.IsRegistered, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) throws SQLException {
				XMPMessage.tellOperator("Ok let me find out... ");
				new XMPMessage(MessageType.IsRegistered).send();
			}
		});
		handlers.put(OperatorCommand.Register, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) throws SQLException {
				XMPMessage.tellOperator("I'm on it... ");
				XMPMessage xmpMessage = new XMPMessage(MessageType.Register);
				xmpMessage.setContents(XMPCrypt.getCertificate());
				xmpMessage.send();
			}
		});
		handlers.put(OperatorCommand.RequestDataPoints,
				new InputCommandStrategy() {
					@Override
					public void HandleCommand(Message m) throws SQLException {
						XMPMessage.tellOperator("I'm on it... ");
						new XMPMessage(MessageType.RequestDataPoints).send();
					}
				});
		handlers.put(OperatorCommand.Unregister, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) throws SQLException {
				XMPMessage.tellOperator("I'm on it... ");
				new XMPMessage(MessageType.Unregister).send();
			}
		});
		handlers.put(OperatorCommand.Echo, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) throws SQLException {
				XMPMessage.tellOperator("Echo: " + m.getBody());
			}
		});

	}

}
