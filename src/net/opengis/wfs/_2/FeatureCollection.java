//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.11.25 um 03:48:31 PM CET 
//


package net.opengis.wfs._2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import de.berlin.broker.SBordstein;
import de.berlin.broker.SFahrbahnmarkierungLinie;
import net.opengis.gml._3.Envelope;


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
 *         &lt;element name="boundedBy" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.opengis.net/gml/3.2}Envelope"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="member" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.berlin.de/broker}s_Fahrbahnmarkierung_Linie"/&gt;
 *                   &lt;element ref="{http://www.berlin.de/broker}s_Bordstein"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="numberMatched" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="numberReturned" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="timeStamp" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "boundedBy",
    "member"
})
@XmlRootElement(name = "FeatureCollection")
public class FeatureCollection {

    protected List<FeatureCollection.BoundedBy> boundedBy;
    protected List<FeatureCollection.Member> member;
    @XmlAttribute(name = "numberMatched")
    protected String numberMatched;
    @XmlAttribute(name = "numberReturned")
    protected String numberReturned;
    @XmlAttribute(name = "timeStamp")
    protected String timeStamp;

    /**
     * Gets the value of the boundedBy property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the boundedBy property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBoundedBy().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FeatureCollection.BoundedBy }
     * 
     * 
     */
    public List<FeatureCollection.BoundedBy> getBoundedBy() {
        if (boundedBy == null) {
            boundedBy = new ArrayList<FeatureCollection.BoundedBy>();
        }
        return this.boundedBy;
    }

    /**
     * Gets the value of the member property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the member property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMember().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FeatureCollection.Member }
     * 
     * 
     */
    public List<FeatureCollection.Member> getMember() {
        if (member == null) {
            member = new ArrayList<FeatureCollection.Member>();
        }
        return this.member;
    }

    /**
     * Ruft den Wert der numberMatched-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumberMatched() {
        return numberMatched;
    }

    /**
     * Legt den Wert der numberMatched-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumberMatched(String value) {
        this.numberMatched = value;
    }

    /**
     * Ruft den Wert der numberReturned-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumberReturned() {
        return numberReturned;
    }

    /**
     * Legt den Wert der numberReturned-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumberReturned(String value) {
        this.numberReturned = value;
    }

    /**
     * Ruft den Wert der timeStamp-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeStamp() {
        return timeStamp;
    }

    /**
     * Legt den Wert der timeStamp-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeStamp(String value) {
        this.timeStamp = value;
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
     *         &lt;element ref="{http://www.opengis.net/gml/3.2}Envelope"/&gt;
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
        "envelope"
    })
    public static class BoundedBy {

        @XmlElement(name = "Envelope", namespace = "http://www.opengis.net/gml/3.2", required = true)
        protected Envelope envelope;

        /**
         * Ruft den Wert der envelope-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Envelope }
         *     
         */
        public Envelope getEnvelope() {
            return envelope;
        }

        /**
         * Legt den Wert der envelope-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Envelope }
         *     
         */
        public void setEnvelope(Envelope value) {
            this.envelope = value;
        }

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
     *         &lt;element ref="{http://www.berlin.de/broker}s_Fahrbahnmarkierung_Linie"/&gt;
     *         &lt;element ref="{http://www.berlin.de/broker}s_Bordstein"/&gt;
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
        "sFahrbahnmarkierungLinie",
        "sBordstein"
    })
    public static class Member {

        @XmlElement(name = "s_Fahrbahnmarkierung_Linie", namespace = "http://www.berlin.de/broker", required = true)
        protected SFahrbahnmarkierungLinie sFahrbahnmarkierungLinie;
        @XmlElement(name = "s_Bordstein", namespace = "http://www.berlin.de/broker", required = true)
        protected SBordstein sBordstein;

        /**
         * Ruft den Wert der sFahrbahnmarkierungLinie-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link SFahrbahnmarkierungLinie }
         *     
         */
        public SFahrbahnmarkierungLinie getSFahrbahnmarkierungLinie() {
            return sFahrbahnmarkierungLinie;
        }

        /**
         * Legt den Wert der sFahrbahnmarkierungLinie-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link SFahrbahnmarkierungLinie }
         *     
         */
        public void setSFahrbahnmarkierungLinie(SFahrbahnmarkierungLinie value) {
            this.sFahrbahnmarkierungLinie = value;
        }

        /**
         * Ruft den Wert der sBordstein-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link SBordstein }
         *     
         */
        public SBordstein getSBordstein() {
            return sBordstein;
        }

        /**
         * Legt den Wert der sBordstein-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link SBordstein }
         *     
         */
        public void setSBordstein(SBordstein value) {
            this.sBordstein = value;
        }

    }

}
