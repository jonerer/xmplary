package se.lolcalhost.xmplary.common.commands;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.bouncycastle.jce.provider.X509CertificateObject;
import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;
import se.lolcalhost.xmplary.xmpgate.commands.SendAlarmBacklog;
import se.lolcalhost.xmplary.xmpgate.commands.SendDataPoints;

public class Register extends Command {
	public Register(XMPMain main, XMPMessage msg) {
		super(main, msg);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() throws SQLException, JSONException {
		XMPMessage response = msg.createResponse();
		XMPNode from = msg.getFrom();
		X509CertificateObject cert = (X509CertificateObject) msg.getContents();
		XMPNode origin = msg.getOrigin();
		origin.setRegistered(true);
		origin.setCert(cert);
		
		XMPDb.Nodes.update(origin);
		
		// TODO: try to re-validate previously unvalidated messages, and run their handlers.
		response.setContents(new JSONObject().put("IsRegistered", msg.getFrom().isRegistered()));
		response.send();
	}

}
