/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.amnesty.crm.mail.bean;

/**
 *
 * @author ed
 */
public class Message {

    private String smtphostname;
    private String smtpdebug;
    private String mimemessagefrom;

    public Message(String smtphostname, String smtpdebug, String mimemessagefrom) {
        this.smtphostname = smtphostname;
        this.smtpdebug = smtpdebug;
        this.mimemessagefrom = mimemessagefrom;
    }

    public String getMimemessagefrom() {
        return mimemessagefrom;
    }

    public void setMimemessagefrom(String mimemessagefrom) {
        this.mimemessagefrom = mimemessagefrom;
    }

    public String getSmtpdebug() {
        return smtpdebug;
    }

    public void setSmtpdebug(String smtpdebug) {
        this.smtpdebug = smtpdebug;
    }

    public String getSmtphostname() {
        return smtphostname;
    }

    public void setSmtphostname(String smtphostname) {
        this.smtphostname = smtphostname;
    }
}
