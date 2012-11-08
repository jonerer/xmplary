xmplary
=======

XMPlary! A couple of applications for my thesis work. It's to be used in embedded connected devices. Uses XMPP as its presentation layer, hence the name.

CERTS
-----
A directory for the certificate files. The xca.xdb file is the database used in the XCA application. With it, certificates are generated. The four CAs are stored in certs/cas/. These are to be put in the trust stores of all nodes in the network. (somehow).

XMPCOMMON
---------
A java project for some parts that are common to all node types.

XMPLaryLeaf
-----------
A java project for the "leaf" node type. The ones put in the welder nodes.

XMPLaryGateway
--------------
A java project for the code running on the "gateway" node.

XMPLaryBackend
--------------
A java project for the code on the backends.


For more info, see the thesis.
