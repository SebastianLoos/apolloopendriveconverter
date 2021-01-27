//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.11.25 um 03:48:31 PM CET 
//


package de.berlin.broker;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.opengis.gml._3.MultiCurve;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="elem_nr" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="fbl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="gis_id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="laenge" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="geom" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.opengis.net/gml/3.2}MultiCurve"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute ref="{http://www.opengis.net/gml/3.2}id"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "elemNr",
    "fbl",
    "gisId",
    "laenge",
    "geom"
})
@XmlRootElement(name = "s_Fahrbahnmarkierung_Linie")
public class SFahrbahnmarkierungLinie {

    @XmlElement(name = "elem_nr")
    protected String elemNr;
    protected String fbl;
    @XmlElement(name = "gis_id")
    protected String gisId;
    protected String laenge;
    protected List<SFahrbahnmarkierungLinie.Geom> geom;
    @XmlAttribute(name = "id", namespace = "http://www.opengis.net/gml/3.2")
    protected String id;

    /**
     * Ruft den Wert der elemNr-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getElemNr() {
        return elemNr;
    }

    /**
     * Legt den Wert der elemNr-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setElemNr(String value) {
        this.elemNr = value;
    }

    /**
     * Ruft den Wert der fbl-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFbl() {
        return fbl;
    }

    /**
     * Legt den Wert der fbl-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFbl(String value) {
        this.fbl = value;
    }

    /**
     * Ruft den Wert der gisId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGisId() {
        return gisId;
    }

    /**
     * Legt den Wert der gisId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGisId(String value) {
        this.gisId = value;
    }

    /**
     * Ruft den Wert der laenge-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLaenge() {
        return laenge;
    }

    /**
     * Legt den Wert der laenge-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLaenge(String value) {
        this.laenge = value;
    }

    /**
     * Gets the value of the geom property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the geom property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGeom().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SFahrbahnmarkierungLinie.Geom }
     * 
     * 
     */
    public List<SFahrbahnmarkierungLinie.Geom> getGeom() {
        if (geom == null) {
            geom = new ArrayList<SFahrbahnmarkierungLinie.Geom>();
        }
        return this.geom;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element ref="{http://www.opengis.net/gml/3.2}MultiCurve"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "multiCurve"
    })
    public static class Geom {

        @XmlElement(name = "MultiCurve", namespace = "http://www.opengis.net/gml/3.2", required = true)
        protected MultiCurve multiCurve;

        /**
         * Ruft den Wert der multiCurve-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link MultiCurve }
         *     
         */
        public MultiCurve getMultiCurve() {
            return multiCurve;
        }

        /**
         * Legt den Wert der multiCurve-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link MultiCurve }
         *     
         */
        public void setMultiCurve(MultiCurve value) {
            this.multiCurve = value;
        }

    }

}
