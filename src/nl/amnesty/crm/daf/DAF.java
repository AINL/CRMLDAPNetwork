/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.amnesty.crm.daf;

import java.util.List;
import nl.amnesty.crm.entity.Country;

/**
 *
 * @author evelzen
 */
public class DAF {
    private String name;
    private String crmcode;
    private List<Country> countrylist;

    public DAF() {
    }

    public DAF(String name, String crmcode, List<Country> countrylist) {
        this.name = name;
        this.crmcode = crmcode;
        this.countrylist = countrylist;
    }


    public String getCrmcode() {
        return crmcode;
    }

    public void setCrmcode(String crmcode) {
        this.crmcode = crmcode;
    }

    public List<Country> getCountrylist() {
        return countrylist;
    }

    public void setCountrylist(List<Country> countrylist) {
        this.countrylist = countrylist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
}
