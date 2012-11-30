package se.localhost.xmplary.xmpback.strategies;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jivesoftware.smack.packet.Message;

import se.localhost.xmplary.xmpback.dump.DatapointDataset;
import se.localhost.xmplary.xmpback.dump.DatapointGraph;
import se.lolcalhost.xmplary.common.XMPCrypt;
import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPDataPoint.DataPointField;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;
import se.lolcalhost.xmplary.common.strategies.IMessageReceiverStrategy;

public class OperatorInputStrategy implements IMessageReceiverStrategy {
	public interface InputCommandStrategy {
		public void HandleCommand(Message m);
	}

	HashMap<OperatorCommand, InputCommandStrategy> handlers = new HashMap<OperatorCommand, InputCommandStrategy>();
	private XMPMain main;

	public enum OperatorCommand {
		IsRegistered, Register, Unregister,

		Hello, Echo,

		Help,

		RequestDataPoints, DumpData, ListNodes,
		
		GetDump
	}

	public OperatorInputStrategy(XMPMain main) {
		this.main = main;
		addHandlers();
	}

	@Override
	public void PreparseReceiveMessage(Message p) {
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
				handlers.get(c).HandleCommand(m);
			} else {
				XMPMessage.tellOperator("Unknown command, or you're not operator.");
			}
		}
	}

	private void handleException(Exception e) {
		e.printStackTrace();
	}

	@Override
	public void ReceiveMessage(XMPMessage msg) {
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
			public void HandleCommand(Message m) {
				XMPMessage.tellOperator("Here is a list of commands: ");
				for (OperatorCommand node : OperatorCommand.values()) {
					XMPMessage.tellOperator("!" + node.name());
				}
			}
		});
		handlers.put(OperatorCommand.Hello, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) {
				XMPMessage.tellOperator("Y halo thar mister " + m.getFrom());
			}
		});
		handlers.put(OperatorCommand.ListNodes, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) {
				for (XMPNode node : XMPDb.Nodes) {
					XMPMessage.tellOperator("Node: " + node.getName()
							+ " (type: " + node.getType().name() + ")");
				}
			}
		});
		handlers.put(OperatorCommand.DumpData, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) {
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
				DatapointGraph g = new DatapointGraph("Datapoints", field);
				for (XMPNode xmpNode : nodes) {
					DatapointDataset ds = new DatapointDataset(xmpNode
							.getDatapoints());
					ds.setOriginNode(xmpNode);
					g.addDataset(ds);
				}
				try {
					g.generate();
				} catch (Exception e) {
					System.out.println("is problem: ");
				}
				g.show();
				File f = new File("bleulf.png");
				try {
					g.save(f);
					XMPMessage.tellOperator("Saved file as "
							+ f.getAbsolutePath() + ".");
				} catch (IOException e) {
					XMPMessage.tellOperator("Couldn't save the file to"
							+ f.getAbsolutePath() + ".");
				}
			}
		});
		handlers.put(OperatorCommand.GetDump, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) {
				XMPMessage.tellOperator("Ok let me send request... ");
				XMPMessage request = new XMPMessage(MessageType.DumpRequest);
				String targetName = m.getBody().split(" ")[1];
				request.setTarget(XMPNode.getByJID(targetName));
				request.send();
			}
		});
		handlers.put(OperatorCommand.IsRegistered, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) {
				XMPMessage.tellOperator("Ok let me find out... ");
				new XMPMessage(MessageType.IsRegistered).send();
			}
		});
		handlers.put(OperatorCommand.Register, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) {
				XMPMessage.tellOperator("I'm on it... ");
				XMPMessage xmpMessage = new XMPMessage(MessageType.Register);
				xmpMessage.setContents(XMPCrypt.getCertificate());
				xmpMessage.send();
			}
		});
		handlers.put(OperatorCommand.RequestDataPoints,
				new InputCommandStrategy() {
					@Override
					public void HandleCommand(Message m) {
						XMPMessage.tellOperator("I'm on it... ");
						new XMPMessage(MessageType.RequestDataPoints).send();
					}
				});
		handlers.put(OperatorCommand.Unregister, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) {
				XMPMessage.tellOperator("I'm on it... ");
				new XMPMessage(MessageType.Unregister).send();
			}
		});
		handlers.put(OperatorCommand.Echo, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) {
				XMPMessage.tellOperator("Echo: " + m.getBody());
			}
		});

	}

}