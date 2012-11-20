package se.localhost.xmplary.xmpback;

import java.util.HashMap;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;
import se.lolcalhost.xmplary.common.strategies.MessageReceiverStrategy;

public class OperatorInputStrategy extends MessageReceiverStrategy {
	public interface InputCommandStrategy {
		public void HandleCommand(Message m);
	}

	HashMap<OperatorCommand, InputCommandStrategy> handlers = new HashMap<OperatorCommand, InputCommandStrategy>();

	public enum OperatorCommand {
		IsRegistered, 
		Register, 
		Unregister, 
		
		Hello, 
		Echo, 
		
		RequestDataPoints
	}

	public OperatorInputStrategy(XMPMain main) {
		super(main);
		addHandlers();
	}

	@Override
	public void PreparseReceiveMessage(Message p) {
		OperatorCommand c = null;

		if (((Message) p).getBody().charAt(0) == '!') {
			Message m = (Message) p;
			try {
				c = OperatorCommand.valueOf(m.getBody().split(" ")[0]
						.substring(1));
			} catch (IllegalArgumentException e) {
				handleException(e);
			}

			XMPNode operator = XMPNode.getOperator();
			if (operator == null) {
				String opname = p.getFrom().split("@")[0];
				main.dispatchRaw("No operator registered, registering " + opname + ".");
				operator = new XMPNode();
				operator.setType(NodeType.operator);
				operator.setName(opname);
				operator.save();
			}
			if (handlers.get(c) != null) {
				handlers.get(c).HandleCommand(m);
			} else {
				main.dispatchRaw("Unknown command, no handler registered.");
			}
		}
	}

	private void handleException(Exception e) {
		e.printStackTrace();
	}

	@Override
	public void ReceiveMessage(XMPMessage m) {
		switch (m.getType()) {
		case IsRegistered:
		case RegisterBackend:
		case RemoveBackend:
			main.dispatchRaw("Is registered? " + m.getRawContents());
			break;
		default:
			break;
		}
	}

	private void addHandlers() {
		handlers.put(OperatorCommand.Hello, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) {
				main.dispatchRaw("Y halo thar mister " + m.getFrom());
			}
		});
		
		handlers.put(OperatorCommand.IsRegistered, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) {
				main.dispatchRaw("Ok let me find out... ");
				main.dispatch(new XMPMessage(MessageType.IsRegistered));
			}
		});
		handlers.put(OperatorCommand.Register, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) {
				main.dispatchRaw("I'm on it... ");
				main.dispatch(new XMPMessage(MessageType.RegisterBackend));
			}
		});
		handlers.put(OperatorCommand.RequestDataPoints, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) {
				main.dispatchRaw("I'm on it... ");
				main.dispatch(new XMPMessage(MessageType.RequestDataPoints));
			}
		});
		handlers.put(OperatorCommand.Unregister, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) {
				main.dispatchRaw("I'm on it... ");
				main.dispatch(new XMPMessage(MessageType.RemoveBackend));
			}
		});
		handlers.put(OperatorCommand.Unregister, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) {
				main.dispatchRaw("I'm on it... ");
				main.dispatch(new XMPMessage(MessageType.RemoveBackend));
			}
		});
		handlers.put(OperatorCommand.Echo, new InputCommandStrategy() {
			@Override
			public void HandleCommand(Message m) {
				main.dispatchRaw("Echo: " + m.getBody());
			}
		});
		
	}

}
