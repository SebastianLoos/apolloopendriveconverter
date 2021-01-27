//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.11.25 um 03:48:31 PM CET 
//


package de.berlin.broker;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.berlin.broker package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.berlin.broker
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SFahrbahnmarkierungLinie }
     * 
     */
    public SFahrbahnmarkierungLinie createSFahrbahnmarkierungLinie() {
        return new SFahrbahnmarkierungLinie();
    }

    /**
     * Create an instance of {@link SBordstein }
     * 
     */
    public SBordstein createSBordstein() {
        return new SBordstein();
    }

    /**
     * Create an instance of {@link SFahrbahnmarkierungLinie.Geom }
     * 
     */
    public SFahrbahnmarkierungLinie.Geom createSFahrbahnmarkierungLinieGeom() {
        return new SFahrbahnmarkierungLinie.Geom();
    }

    /**
     * Create an instance of {@link SBordstein.Geom }
     * 
     */
    public SBordstein.Geom createSBordsteinGeom() {
        return new SBordstein.Geom();
    }

}
