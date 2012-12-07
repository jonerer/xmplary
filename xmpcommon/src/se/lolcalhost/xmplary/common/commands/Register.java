package se.lolcalhost.xmplary.common.commands;

import java.security.cert.CertificateEncodingException;
import java.sql.SQLException;
import java.util.Arrays;

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
		setPriority(CommandPriority.URGENT);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() throws SQLException, JSONException {
		XMPNode from = msg.getFrom();
		X509CertificateObject cert = (X509CertificateObject) msg.getContents();
		XMPNode origin = msg.getOrigin();
		boolean isIdentical = false;
		
		
		if (origin.getCert() != null) {
			byte[] newc = null;
			byte[] oldc = null;
			try {
				newc = cert.getEncoded();
				oldc = origin.getCert().getEncoded();
				if (Arrays.equals(newc, oldc)) {
					isIdentical = true;
				}
			} catch (CertificateEncodingException e) {
				e.printStackTrace();
			}
		}
		if (!isIdentical) {
			origin.setCert(cert);
			
			if (msg.verify(cert.getPublicKey())) { // TODO: validate cert chain.
				origin.setRegistered(true);
	//			TODO: activate this once I'm sure of it.
	//			ReValidateCommand vld = new ReValidateCommand(main, msg);
	//			vld.schedule();
				
				// TODO: re-send things that have not been deliverable
				// this isn't really used anymore since a failed delivery will schedule itself.
				// but it could be useful for old stuff lying around.
	//			ReTryDelivery rtd = new ReTryDelivery(main, origin);
	//			rtd.schedule();
			} else {
				origin.setRegistered(false);
			}
			origin.save();
		}
	}

}
