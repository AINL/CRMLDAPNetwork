/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.amnesty.crm.ldap.network.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import nl.amnesty.crm.daf.DAF;
import nl.amnesty.crm.daf.DAFConfigSAXHandler;
import nl.amnesty.crm.db.Database;
import nl.amnesty.crm.db.DatabaseConfigSAXHandler;
import nl.amnesty.crm.collection.IdStartdateEnddate;
import nl.amnesty.crm.entity.Country;
import nl.amnesty.crm.entity.Network;
import nl.amnesty.crm.ldap.LDAPConfigSAXHandler;
import nl.amnesty.crm.mail.bean.Message;
import nl.amnesty.crm.mail.bean.MessageConfigSAXHandler;
import nl.amnesty.crm.mail.bean.MessageFacade;
import nl.amnesty.crm.sql.NetworkSQL;
import nl.amnesty.ldap.entity.LDAP;
import nl.amnesty.ldap.entity.LDAPConnection;
import nl.amnesty.sys.controller.CRMLDAPController;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author evelzen
 */
public class Main {

    /*
     * Windows
     */
   
    //private static final String CONFIG_URL = "file:///C://Netbeans72//NLAmnestyConfig//web//network.xml";
    //private static final String CONFIG_CRM = "C://Ldaplocal//crmconfig.xml";
    //private static final String CONFIG_LDAP = "C://My Templates//ldapconfig.xml";
    //private static final String CONFIG_LDAP = "C://My Templates//ldapconfiglof.xml";
    //private static final String CONFIG_LDAP = "C://Ldaplocal//ldapconfiggarnaal.xml";
    //private static final String CONFIG_DAF = "C://Ldaplocal//dafconfig.xml";
    //private static final String CONFIG_MESSAGE = "C://Ldaplocal//messageconfig.xml";
    //private static final String LOG4J_PROPERTIES = "C://Ldaplocal//log4j.properties";
   
    /*
     * Linux development
     */
    //private static final String CONFIG_CRM = "/home/ed/Public/crmconfig.xml";
    //private static final String CONFIG_LDAP = "/home/ed/Public/ldapconfig.xml";
    //private static final String CONFIG_DAF = "/home/ed/Public/dafconfig.xml";
    //private static final String CONFIG_MESSAGE = "/home/ed/Public/messageconfig.xml";
    //private static final String LOG4J_PROPERTIES = "/home/ed/Public/log4j.properties";
    /*
     * Linux live voor garnaal
     */
    
    private static final String CONFIG_URL = "http://localhost:8080/nlamnestyconfig/network.xml";
    private static final String CONFIG_CRM = "/root/crmldapnetwork/garnaal/crmconfig.xml";
    private static final String CONFIG_LDAP = "/root/crmldapnetwork/garnaal/ldapconfig.xml";
    private static final String CONFIG_DAF = "/root/crmldapnetwork/garnaal/dafconfig.xml";
    private static final String CONFIG_MESSAGE = "/root/crmldapnetwork/garnaal/messageconfig.xml";
    private static final String LOG4J_PROPERTIES = "/root/crmldapnetwork/garnaal/log4j.properties";
    
    /*
     * Notification targets
     */
    private static final String NOTIFICATION_TARGET_1 = "e.vanvelzen@amnesty.nl";
    private static final String NOTIFICATION_TARGET_2 = "r.kooijman@amnesty.nl";
    private static final String NOTIFICATION_TARGET_3 = "w.roelofsen@amnesty.nl";
    private static final String NOTIFICATION_TARGET_4 = "b.menting@amnesty.nl";
    /*
     * Resources
     */
    public static Database database;
    public static LDAP ldap;
    public static List<DAF> daflist;
    public static Message message;
    //
    private static final String LDAPGROUP_ACTIVE = "ACT";
    private static final String LDAPGROUP_FAIRTRADESHOP = "FTS";
    private static final String LDAPGROUP_LOCALGROUP = "LOK";
    private static final String LDAPGROUP_COUNCIL = "BST";
    private static final String LDAPGROUP_COUNTRYSPECIALIST = "LND";
    private static final String LDAPGROUP_SCHOOLWORKER = "SCH";
    private static final String LDAPGROUP_MEMBERCOUNCIL = "LDR";
    private static final String LDAPGROUP_DISCOUNT10 = "K10";
    private static final String LDAPGROUP_DISCOUNT20 = "K20";
    private static final String LDAPGROUP_DISCOUNT30 = "K30";
    private static final String LDAPGROUP_DISCOUNT40 = "K40";
    private static final String LDAPGROUP_DISCOUNT50 = "K50";
    private static final String LDAPGROUP_TRAINERS="TRN";
    private static final String LDAPGROUP_ACTIEGROEP_VREEMDELINGENDETENTIE="ATV";
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Get a logger
        PropertyConfigurator.configure(LOG4J_PROPERTIES);
        Logger logger = Logger.getLogger(Main.class);

        // The getConfig methods use XML parsers that update the public static field for database, ldap and daflist.
        getDatabaseConfig();
        getLDAPConfig();
        getDAFConfig();
        getMessageConfig();

        // Database connection
        Connection connection = null;
        // LDAP connection
        LDAPConnection ldapconnection = null;
        // Notification list
        List<String> recipientlist = new ArrayList();
        // Network roleid list
        Collection<IdStartdateEnddate> rolecollection = new ArrayList();
        String filter = "";
        try {
            // Open the CRM Database
            database.setDbvendor(Database.DB_TYPE_MSSQL);
            connection = database.open();

            // Open the LDAP Server
            if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_OPENDS) {
                ldap.setLdapvendor(LDAP.LDAP_TYPE_OPENDS);
            }
            if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_SUNONE) {
                ldap.setLdapvendor(LDAP.LDAP_TYPE_SUNONE);
            }
            if (ldap.getLdapvendor() != LDAP.LDAP_TYPE_NONE) {
                ldapconnection = ldap.open();

                java.net.URL url = new java.net.URL(CONFIG_URL);
                
                // Trainers
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_OPENDS) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "TRN", filter);
                    rolecollection = network.getIdlist();
                }
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_SUNONE) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "TRN", filter);
                    rolecollection = network.getIdlist();
                }
                CRMLDAPController.replicateLDAPGroupCollection(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_TRAINERS, rolecollection);

                //if (true) return;
                // Active Members
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_OPENDS) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    //Network network = NetworkSQL.read(connection, url, "ACT", filter);
                    Network network = networksql.read(connection, url, "LGR", filter);
                    rolecollection = network.getIdlist();
                    // Also add Country specialists to the LDAP group of Active members
                    network = networksql.read(connection, url, "CNT", filter);
                    rolecollection.addAll(network.getIdlist());
                }
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_SUNONE) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    //Network network = NetworkSQL.read(connection, url, "ACT", filter);
                    Network network = networksql.read(connection, url, "LGR", filter);
                    rolecollection = network.getIdlist();
                    // Also add Country specialists to the LDAP group of Active members
                    network = networksql.read(connection, url, "CNT", filter);
                    rolecollection.addAll(network.getIdlist());
                }

                CRMLDAPController.replicateLDAPGroupCollection(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_ACTIVE, rolecollection);
                // Always add the administrative user for this LDAP group
                //CRMLDAPController.addAdminToLDAPGroup(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_ACTIVE);

                // Fairtrade shops
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_OPENDS) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "FTS", filter);
                    rolecollection = network.getIdlist();
                }
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_SUNONE) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "FTS", filter);
                    rolecollection = network.getIdlist();
                }
                CRMLDAPController.replicateLDAPGroupCollection(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_FAIRTRADESHOP, rolecollection);
                // Always add the administrative user for this LDAP group
                //CRMLDAPController.addAdminToLDAPGroup(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_FAIRTRADESHOP);

                // Local groups
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_OPENDS) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "LGR", filter);
                    rolecollection = network.getIdlist();
                }
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_SUNONE) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "LGR", filter);
                    rolecollection = network.getIdlist();
                }
                CRMLDAPController.replicateLDAPGroupCollection(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_LOCALGROUP, rolecollection);
                // Always add the administrative user for this LDAP group
                //CRMLDAPController.addAdminToLDAPGroup(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_LOCALGROUP);

                 // Bestuur
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_OPENDS) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "BST", filter);
                    rolecollection = network.getIdlist();
                }
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_SUNONE) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "BST", filter);
                    rolecollection = network.getIdlist();
                }
                CRMLDAPController.replicateLDAPGroupCollection(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_COUNCIL, rolecollection);
                
                 // Actiegroep vreemdelingendetentie
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_OPENDS) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "ATV", filter);
                    rolecollection = network.getIdlist();
                }
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_SUNONE) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "ATV", filter);
                    rolecollection = network.getIdlist();
                }
                CRMLDAPController.replicateLDAPGroupCollection(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_ACTIEGROEP_VREEMDELINGENDETENTIE, rolecollection);
                
                // Country specialist
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_OPENDS) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "CNT", filter);
                    rolecollection = network.getIdlist();
                }
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_SUNONE) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "CNT", filter);
                    rolecollection = network.getIdlist();
                }
                CRMLDAPController.replicateLDAPGroupCollection(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_COUNTRYSPECIALIST, rolecollection);
                // Always add the administrative user for this LDAP group
                //CRMLDAPController.addAdminToLDAPGroup(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_COUNTRYSPECIALIST);

                // School workers
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_OPENDS) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "EDU", filter);
                    rolecollection = network.getIdlist();
                }
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_SUNONE) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "EDU", filter);
                    rolecollection = network.getIdlist();
                }
                CRMLDAPController.replicateLDAPGroupCollection(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_SCHOOLWORKER, rolecollection);
                // Always add the administrative user for this LDAP group
                //CRMLDAPController.addAdminToLDAPGroup(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_SCHOOLWORKER);

                // Member council
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_OPENDS) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "MBR", filter);
                    rolecollection = network.getIdlist();
                }
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_SUNONE) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "MBR", filter);
                    rolecollection = network.getIdlist();
                }
                CRMLDAPController.replicateLDAPGroupCollection(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_MEMBERCOUNCIL, rolecollection);
                // Always add the administrative user for this LDAP group
                //CRMLDAPController.addAdminToLDAPGroup(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_MEMBERCOUNCIL);

                // Korting 10%
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_OPENDS) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "K10", filter);
                    rolecollection = network.getIdlist();
                }
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_SUNONE) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "K10", filter);
                    rolecollection = network.getIdlist();
                }
                CRMLDAPController.replicateLDAPGroupCollection(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_DISCOUNT10, rolecollection);

                // Korting 20%
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_OPENDS) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "K20", filter);
                    rolecollection = network.getIdlist();
                }
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_SUNONE) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "K20", filter);
                    rolecollection = network.getIdlist();
                }
                CRMLDAPController.replicateLDAPGroupCollection(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_DISCOUNT20, rolecollection);

                // Korting 30%
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_OPENDS) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "K30", filter);
                    rolecollection = network.getIdlist();
                }
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_SUNONE) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "K30", filter);
                    rolecollection = network.getIdlist();
                }
                CRMLDAPController.replicateLDAPGroupCollection(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_DISCOUNT30, rolecollection);

                // Korting 10%
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_OPENDS) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "K40", filter);
                    rolecollection = network.getIdlist();
                }
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_SUNONE) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "K40", filter);
                    rolecollection = network.getIdlist();
                }
                CRMLDAPController.replicateLDAPGroupCollection(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_DISCOUNT40, rolecollection);

                // Korting 50%
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_OPENDS) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "K50", filter);
                    rolecollection = network.getIdlist();
                }
                if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_SUNONE) {
                    filter = "";
                    NetworkSQL networksql = new NetworkSQL();
                    Network network = networksql.read(connection, url, "K50", filter);
                    rolecollection = network.getIdlist();
                }
                CRMLDAPController.replicateLDAPGroupCollection(logger, connection, ldapconnection, ldap.getBasedn(), LDAPGROUP_DISCOUNT50, rolecollection);
                
                //Digital Action Files
                for (DAF daf : daflist) {
                    filter = daf.getCrmcode();
                    if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_OPENDS) {
                        NetworkSQL networksql = new NetworkSQL();
                        Network network = networksql.read(connection, url, "DAF", filter);
                        rolecollection = network.getIdlist();
                    }
                    if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_SUNONE) {
                        NetworkSQL networksql = new NetworkSQL();
                        Network network = networksql.read(connection, url, "DAF", filter);
                        rolecollection = network.getIdlist();
                    }

                    List<Country> countrylist = daf.getCountrylist();
                    for (Country country : countrylist) {
                        CRMLDAPController.replicateLDAPGroupCollection(logger, connection, ldapconnection, ldap.getBasedn(), country.getIsocode(), rolecollection);
                        // Always add the administrative user for this LDAP group
                        //CRMLDAPController.addAdminToLDAPGroup(logger, connection, ldapconnection, ldap.getBasedn(), country.getIsocode());
                    }
                }

                String text = getLogfile();
                MessageFacade messagefacade = new MessageFacade();
                recipientlist.add(NOTIFICATION_TARGET_1);
                recipientlist.add(NOTIFICATION_TARGET_2);
                recipientlist.add(NOTIFICATION_TARGET_3);
                recipientlist.add(NOTIFICATION_TARGET_4);
                messagefacade.sendEmail(message.getSmtphostname(), message.getSmtpdebug(), message.getMimemessagefrom(), recipientlist, "CRMLDAPNetwork synchronized", text);
            }
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            if (ldapconnection != null) {
                ldap.close(ldapconnection);
            }
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private static void getDatabaseConfig() {
        try {
            File file = new File(CONFIG_CRM);
            SAXParserFactory factorySAX = SAXParserFactory.newInstance();
            SAXParser sax = factorySAX.newSAXParser();
            DatabaseConfigSAXHandler saxhandler = new DatabaseConfigSAXHandler();
            sax.parse(file, saxhandler);
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static void getLDAPConfig() {
        try {
            File file = new File(CONFIG_LDAP);
            SAXParserFactory factorySAX = SAXParserFactory.newInstance();
            SAXParser sax = factorySAX.newSAXParser();
            LDAPConfigSAXHandler saxhandler = new LDAPConfigSAXHandler();
            sax.parse(file, saxhandler);
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static void getDAFConfig() {
        try {
            File file = new File(CONFIG_DAF);
            SAXParserFactory factorySAX = SAXParserFactory.newInstance();
            SAXParser sax = factorySAX.newSAXParser();
            DAFConfigSAXHandler saxhandler = new DAFConfigSAXHandler();
            sax.parse(file, saxhandler);
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static void getMessageConfig() {
        try {
            File file = new File(CONFIG_MESSAGE);
            SAXParserFactory factorySAX = SAXParserFactory.newInstance();
            SAXParser sax = factorySAX.newSAXParser();
            MessageConfigSAXHandler saxhandler = new MessageConfigSAXHandler();
            sax.parse(file, saxhandler);
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static String getLogfile() {
        String line = "";
        String text = "";
        FileReader fr = null;
        BufferedReader br = null;
        try {
            Properties log4jproperties = getLOG4JProperties();
            String logfile = log4jproperties.getProperty("log4j.appender.logfile.File");
            fr = new FileReader(logfile);
            br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                text = text.concat(line);
                text += '\n';
            }
        } catch (IOException ioe) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ioe);
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                br.close();
                fr.close();
            } catch (IOException ioe) {
                java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ioe);
            }
            return text;
        }

    }

    private static Properties getLOG4JProperties() {
        try {
            ResourceBundle resourcebundle = new PropertyResourceBundle(new FileInputStream(LOG4J_PROPERTIES));
            //String url = bdl.getString("url");
            Properties properties = new Properties();
            Enumeration enumeration = resourcebundle.getKeys();

            while (enumeration.hasMoreElements()) {
                String property = (String) enumeration.nextElement();
                String value = resourcebundle.getString(property);

                properties.setProperty(property, value);
            }
            return properties;
        } catch (Exception e) {
            return null;
        }

    }
    /*
     * Stukkie code voor resetten van passwords...
     * 
    // Active Members
    if (ldap.getLdapvendor() == LDAP.LDAP_TYPE_OPENDS) {
    Network network = NetworkSQL.read(connection, Networkdefinition.NETWORK_ID_ACTIVEMEMBERS, filter);
    
    // DEBUG
    System.out.println("Network size: " + network.getIdlist().size());
    
    rolecollection = network.getIdlist();
    
    for (IdStartdateEnddate idstartdateenddate : rolecollection) {
    crmcount++;
    Role role = RoleController.readViaEmail(connection, idstartdateenddate.getId());
    if (role != null) {
    LDAPinetOrgPerson ldapinetorgperson = LDAPPersonOpenDS.read(ldapconnection.getLdapconnectionopends(), ldap.getBasedn(), idstartdateenddate.getId());
    if (ldapinetorgperson != null) {
    Address address = role.getAddress();
    if (address != null) {
    ldapcount++;
    String postalcode = String.valueOf(address.getPostalcodenumeric()).concat(address.getPostalcodealpha().toUpperCase());
    System.out.println("Set password " + postalcode + " for id " + idstartdateenddate.getId());
    LDAPPersonOpenDS.setPassword(ldapconnection.getLdapconnectionopends(), ldap.getBasedn(), ldapinetorgperson, postalcode);
    }
    }
    }
    System.out.println("crmcount: " + crmcount + " ldapcount: " + ldapcount);
    }
    // -------------------------------------------------------
    // -------------------------------------------------------
    }
     */
}
