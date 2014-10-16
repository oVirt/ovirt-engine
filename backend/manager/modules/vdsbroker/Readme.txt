
	vdsbroker notes:
		1. ssl is not handled for vdsManager
		2. We need xml-rpc client version 3.1.3 (currently not available as distribution - added a patched jar)
		3. removed XML-RPC logger - wait to integrate with slf4j logging
		4. Transactions are not handled