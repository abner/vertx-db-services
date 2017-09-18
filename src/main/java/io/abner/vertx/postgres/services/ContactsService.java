package io.abner.vertx.postgres.services;

import io.abner.vertx.postgres.models.Contact;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.ext.sql.SQLConnection;
import rx.Single;


public class ContactsService extends PersistenceService<Contact> {
    @Override
    public String getTableName() {
        return "contacts";
    }

    @Override
    public Class<Contact> getModelType() {
        return Contact.class;
    }

    public ContactsService(SQLConnection conn) {
        super(conn);
    }
    
    public ContactsService(SQLConnection conn, Boolean autoCommit) {
        super(conn, autoCommit);
    }
    
    public Single<Boolean> insert(JsonObject contact) {
    	JsonArray params = new JsonArray()
    			.add(getUUIDFor(contact))
    			.add(contact.getString("name"))
    			.add(contact.getString("telephone"))
    			.add(contact.getString("email"));
    	return this.executeUpdateWithParams("INSERT INTO contacts(uuid, name, telephone, email) VALUES(?::uuid,?,?,?)", params)
    			.map(rs -> rs.getUpdated() > 0);
    }
    
}
