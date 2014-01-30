package nl.amnesty.crm.daf;

import java.util.ArrayList;
import java.util.List;
import nl.amnesty.crm.entity.Country;
import nl.amnesty.crm.ldap.network.main.Main;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author evelzen
 */
public class DAFConfigSAXHandler extends DefaultHandler {

    private List<DAF> daflist = new ArrayList<DAF>();
    //
    private boolean inelement = false;
    private String qnameparent;
    private String qnamechild;
    private String value;
    //
    private String namedaf;
    private String namecountry;
    private String crmcode;
    private String isocode;
    private List<Country> countrylist;

    public DAFConfigSAXHandler() throws org.xml.sax.SAXException {
        super();
    }

    /**
     * Receive notification of the start of the document.
     */
    @Override
    public void startDocument() {
        // DEBUG
        //System.out.println("startDocument()");
        countrylist = new ArrayList(32);
        qnameparent = "";
        qnamechild = "";
    }

    /**
     * Receive notification of the end of the document.
     */
    @Override
    public void endDocument() {
        // DEBUG
        //System.out.println("endDocument()");

        Main.daflist = daflist;
        //UpdateLDAPFromCRM.daflist = daflist;
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
        // DEBUG
        //System.out.println("startElement() qname: " + qname);
        if (qnameparent.length() == 0) {
            qnameparent = qname;
        } else {
            if (qnamechild.length() == 0) {
                qnamechild = qname;
            } else {
                qnameparent = qnamechild;
                qnamechild = qname;
            }
        }
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
        //System.out.println("endElement() qname: " + qname);


        if (qname.equals("daf")) {
            DAF daf = new DAF();
            daf.setName(namedaf);
            daf.setCrmcode(crmcode);
            daf.setCountrylist(countrylist);
            daflist.add(daf);
            // Reset countrylist
            countrylist = new ArrayList(32);
        }
        if (qname.equals("crmcode")) {
            crmcode = value;
        }
        if (qname.equals("name")) {
            if (qnameparent.equals("daf")) {
                namedaf = value;
            }
            if (qnameparent.equals("country")) {
                namecountry = value;
            }
        }
        if (qname.equals("isocode")) {
            isocode = value;
        }
        if (qname.equals("country")) {
            Country country = new Country();
            country.setName(namecountry);
            country.setIsocode(isocode);
            countrylist.add(country);
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
        // DEBUG
        //System.out.println("characters() start: " + start + " lenght: " + length + " value: [" + charactervalue + "]");
        }
    }
}
