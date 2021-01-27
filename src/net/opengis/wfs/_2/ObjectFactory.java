//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.11.25 um 03:48:31 PM CET 
//


package net.opengis.wfs._2;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.opengis.wfs._2 package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.opengis.wfs._2
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link FeatureCollection }
     * 
     */
    public FeatureCollection createFeatureCollection() {
        return new FeatureCollection();
    }

    /**
     * Create an instance of {@link FeatureCollection.BoundedBy }
     * 
     */
    public FeatureCollection.BoundedBy createFeatureCollectionBoundedBy() {
        return new FeatureCollection.BoundedBy();
    }

    /**
     * Create an instance of {@link FeatureCollection.Member }
     * 
     */
    public FeatureCollection.Member createFeatureCollectionMember() {
        return new FeatureCollection.Member();
    }

    /**
     * Create an instance of {@link NewDataSet }
     * 
     */
    public NewDataSet createNewDataSet() {
        return new NewDataSet();
    }

}
