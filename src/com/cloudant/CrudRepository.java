package com.cloudant;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;


// Our custon "repo" class which extends the Ektorp base class.  The CouchDbRepositorySupport<T> class
// contains built in CRUD methods.  The Ektorp library as a whole contains more functionality than what
// is represented in this example.
public class CrudRepository extends CouchDbRepositorySupport<CrudDocument> {
	public CrudRepository(CouchDbConnector dbc) {
		super(CrudDocument.class, dbc, true);
	}
}
