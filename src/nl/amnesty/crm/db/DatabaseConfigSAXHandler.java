/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.amnesty.crm.db;

import nl.amnesty.crm.ldap.network.main.Main;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author evelzen
 */
public class DatabaseConfigSAXHandler extends DefaultHandler {

    private boolean inelement = false;
    private String value;
    //
    private String hostname;
    private String instance;
    private int portnumber;
    private String databasename;
    private String username;
    private String password;

    public DatabaseConfigSAXHandler() throws org.xml.sax.SAXException {
        super();
    }

    /**
     * Receive notification of the start of the document.
     */
    @Override
    public void startDocument() {
    }

    /**
     * Receive notification of the end of the document.
     */
    @Override
    public void endDocument() {
    }

    /**
     * Receive notification of the start of an element.
     * @param uri
     * @param localname
     * @param qname
     * @param attributes
     */
    @Override
    public void startElement(String uri, String localname, String qname, Attributes attributes) {
        inelement = true;
    }

    /**
     * Receive notification of the end of an element.
     * @param uri
     * @param localname
     * @param qname
     */
    @Override
    public void endElement(String uri, String localname, String qname) {

        if (qname.equals("crmconfig")) {

        }
        if (qname.equals("dbserver")) {
            // DEBUG
            //System.out.println("hostname: " + hostname);
            //System.out.println("instance: " + instance);
            //System.out.println("portnumber: " + portnumber);
            //System.out.println("databasename: " + databasename);
            //System.out.println("username: " + username);
            //System.out.println("password: " + password);

            Database database = new Database(Database.DB_TYPE_MSSQL, hostname, instance, portnumber, databasename, username, password);
            Main.database = database;
            //UpdateLDAPFromCRM.database = database;
        }
        if (qname.equals("hostname")) {
            hostname = value;
        }
        if (qname.equals("instance")) {
            instance = value;
        }
        if (qname.equals("portnumber")) {
            portnumber = Integer.parseInt(value);
        }
        if (qname.equals("databasename")) {
            databasename = value;
        }
        if (qname.equals("username")) {
            username = value;
        }
        if (qname.equals("password")) {
            password = value;
        }
        value = "";
        inelement = false;
    }

    /**
     * Receive notification of character data inside an element.
     * @param ch
     * @param start
     * @param length
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        if (inelement) {
            String charactervalue = "";
            for (int i = 0; i < length; i++) {
                char c = ch[start + i];
                if (c != '\r' && c != '\t' && c != '\f' && c != '\n') {
                    charactervalue = charactervalue.concat(String.valueOf(c));
                }
            }
            value = charactervalue;
        }
    }
}
