---------- Server-wide settings ----------
-- Settings in this section apply to the whole server and are the default settings
-- for any virtual hosts

admins = { }
modules_enabled = {
	-- Generally required
		"roster"; -- Allow users to have a roster. Recommended ;)
		"saslauth"; -- Authentication for clients and servers. Recommended if you want to log in.
		"tls"; -- Add support for secure TLS on c2s/s2s connections
		--"dialback"; -- s2s dialback support
		"disco"; -- Service discovery

	-- Not essential, but recommended
		"private"; -- Private XML storage (for room bookmarks, etc.)
		"vcard"; -- Allow users to set vCards
		--"privacy"; -- Support privacy lists
		--"compression"; -- Stream compression (Debian: requires lua-zlib module to work)

	-- Nice to have
		"legacyauth"; -- Legacy authentication. Only used by some old clients and bots.
		"version"; -- Replies to server version requests
		"uptime"; -- Report how long server has been running
		"time"; -- Let others know the time here on this server
		"ping"; -- Replies to XMPP pings with pongs
		"pep"; -- Enables users to publish their mood, activity, playing music and more
		"register"; -- Allow users to register on this server using a client and change passwords
		"adhoc"; -- Support for "ad-hoc commands" that can be executed with an XMPP client

	-- Admin interfaces
		"admin_adhoc"; -- Allows administration via an XMPP client that supports ad-hoc commands
		--"admin_telnet"; -- Opens telnet console interface on localhost port 5582

	-- Other specific functionality
		--"bosh"; -- Enable BOSH clients, aka "Jabber over HTTP"
		--"httpserver"; -- Serve static files from a directory over HTTP
		--"groups"; -- Shared roster support
		--"announce"; -- Send announcement to all online users
		--"welcome"; -- Welcome users who register accounts
		--"watchregistrations"; -- Alert admins of registrations
		--"motd"; -- Send a message to users when they log in
	-- Debian: do not remove this module, or you lose syslog
	-- support
		"posix"; -- POSIX functionality, sends server to background, enables syslog, etc.
};

modules_disabled = {
	-- "presence"; -- Route user/contact status information
	-- "message"; -- Route messages
	-- "iq"; -- Route info queries
	-- "offline"; -- Store offline messages
};

allow_registration = true;
daemonize = true;
pidfile = "/var/run/prosody/prosody.pid";

-- These are the SSL/TLS-related settings. If you don't want
-- to use SSL/TLS, you may comment or remove this
--ssl = {
--	key = "/etc/prosody/certs/localhost.key";
--	certificate = "/etc/prosody/certs/localhost.cert";
--}

authentication = "internal_plain"
-- Debian:
--  Logs info and higher to /var/log
--  Logs errors to syslog also
log = {
	-- Log files (change 'info' to 'debug' for debug logs):
	info = "/var/log/prosody/prosody.log";
	error = "/var/log/prosody/prosody.err";
	-- Syslog:
	{ levels = { "error" }; to = "syslog";  };
}

disallow_s2s = true
VirtualHost "arm"
VirtualHost "192.168.204.249"

Component "conference.arm" "muc"
Include "conf.d/*.cfg.lua"
