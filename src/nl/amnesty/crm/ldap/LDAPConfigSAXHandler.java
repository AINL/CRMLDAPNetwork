/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.amnesty.crm.ldap;

import nl.amnesty.crm.ldap.network.main.Main;
import nl.amnesty.ldap.entity.LDAP;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author evelzen
 */
public class LDAPConfigSAXHandler extends DefaultHandler {
    private boolean inelement = false;
    private String value;
    //
    private int ldapvendor;
    private String hostname;
    private int portnumber;
    private String basedn;
    private String binddn;
    private String bindpassword;

    public LDAPConfigSAXHandler() throws org.xml.sax.SAXException {
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

        // DEBUG
        //System.out.println("qname: " + qname);
        //System.out.println("value: " + value);
        
        if (qname.equals("ldapconfig")) {
            LDAP ldap = new LDAP();
            ldap.setLdapvendor(ldapvendor);
            ldap.setHostname(hostname);
            ldap.setPortnumber(Integer.valueOf(portnumber));
            ldap.setBasedn(basedn);
            ldap.setBinddn(binddn);
            ldap.setBindpassword(bindpassword);
            Main.ldap = ldap;
            //UpdateLDAPFromCRM.ldap = ldap;
        }
        if (qname.equals("ldapserver")) {
        }
        if (qname.equals("ldapvendor")) {
            if (value.toLowerCase().equals(LDAP.LDAP_TYPE_OPENDS_NAME.toLowerCase())) {
                ldapvendor = LDAP.LDAP_TYPE_OPENDS;
            }
            if (value.toLowerCase().equals(LDAP.LDAP_TYPE_SUNONE_NAME.toLowerCase())) {
                ldapvendor = LDAP.LDAP_TYPE_SUNONE;
            }
        }
        if (qname.equals("hostname")) {
            hostname = value;
        }
        if (qname.equals("portnumber")) {
            portnumber = Integer.parseInt(value);
        }
        if (qname.equals("basedn")) {
            basedn = value;
        }
        if (qname.equals("bind")) {
        }
        if (qname.equals("binddn")) {
            binddn = value;
        }
        if (qname.equals("bindpassword")) {
            bindpassword = value;
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
