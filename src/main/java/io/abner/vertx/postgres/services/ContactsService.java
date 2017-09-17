package io.abner.vertx.postgres.services;

import io.abner.vertx.postgres.models.Contact;
import io.vertx.rxjava.ext.sql.SQLConnection;


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
    
}
