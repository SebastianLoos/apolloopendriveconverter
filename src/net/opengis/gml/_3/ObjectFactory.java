//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.11.25 um 03:48:31 PM CET 
//


package net.opengis.gml._3;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.opengis.gml._3 package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.opengis.gml._3
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MultiCurve }
     * 
     */
    public MultiCurve createMultiCurve() {
        return new MultiCurve();
    }

    /**
     * Create an instance of {@link MultiCurve.CurveMember }
     * 
     */
    public MultiCurve.CurveMember createMultiCurveCurveMember() {
        return new MultiCurve.CurveMember();
    }

    /**
     * Create an instance of {@link MultiCurve.CurveMember.LineString }
     * 
     */
    public MultiCurve.CurveMember.LineString createMultiCurveCurveMemberLineString() {
        return new MultiCurve.CurveMember.LineString();
    }

    /**
     * Create an instance of {@link Envelope }
     * 
     */
    public Envelope createEnvelope() {
        return new Envelope();
    }

    /**
     * Create an instance of {@link MultiCurve.CurveMember.LineString.PosList }
     * 
     */
    public MultiCurve.CurveMember.LineString.PosList createMultiCurveCurveMemberLineStringPosList() {
        return new MultiCurve.CurveMember.LineString.PosList();
    }

}
