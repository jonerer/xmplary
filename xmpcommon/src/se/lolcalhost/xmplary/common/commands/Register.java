package se.lolcalhost.xmplary.common.commands;

import java.sql.SQLException;

import org.bouncycastle.jce.provider.X509CertificateObject;
import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.XMPCrypt;
import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;

public class Register extends Command {
	public Register(XMPMain main, XMPMessage msg) {
		super(main, msg);
		setPriority(CommandPriority.HIGH);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() throws SQLException, JSONException {
		XMPNode from = msg.getFrom();
		X509CertificateObject cert = (X509CertificateObject) msg.getContents();
		XMPNode origin = msg.getOrigin();
		origin.setCert(cert);
		
		if (msg.verify(cert.getPublicKey())) { // TODO: validate cert chain.
			origin.setRegistered(true);
//			TODO: activate this once I'm sure of it.
//			ReValidateCommand vld = new ReValidateCommand(main, msg);
//			vld.schedule();
		} else {
			origin.setRegistered(false);
		}
		msg.save();
		origin.save();
	}

}
