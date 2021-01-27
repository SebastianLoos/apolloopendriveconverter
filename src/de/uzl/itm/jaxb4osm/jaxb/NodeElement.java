/**
 * Copyright (c) 2014, Oliver Kleine, Institute of Telematics, University of Luebeck
 * All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *  - Redistributions of source messageCode must retain the above copyright notice, this list of conditions and the following
 *    disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *  - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 *    products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uzl.itm.jaxb4osm.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.apollomasterbeuth.logger.Log;

import java.util.Iterator;
import java.util.Map;

/**
 * JAXB compliant class for the node element in OSM files (currently, only the attributes <code>id</code>,
 * <code>latitude</code>, and <code>longitude</code> are supported.
 *
 * @author Oliver Kleine
 */
@XmlJavaTypeAdapter(NodeElement.OsmNodeElementAdapter.class)
public class NodeElement extends AbstractAdaptedLevel2Element {

    private static Log log = new Log();

    private double latitude;
    private double longitude;

//    private Set<Long> referencingWays;

    private NodeElement(PlainNodeElement plainNodeElement){
        super(plainNodeElement);

        this.latitude = plainNodeElement.getLatitude();
        this.longitude = plainNodeElement.getLongitude();

//        this.referencingWays = new HashSet<Long>();
        log.debug("Instance of {} created!", this.getClass().getName());
    }




    /**
     * Returns the value of the attribute "latitude" (e.g. <code>50.1234</code> if the node element was like
     * <code><node ... latitude="50.1234" ...></code>).
     *
     * @return the value of the attribute "latitude"
     */
    public double getLatitude(){
        return this.latitude;
    }

    /**
     * Returns the value of the attribute "longitude" (e.g. <code>10.987</code> if the node element was like
     * <code><node ... longitude="10.987" ...></code>).
     *
     * @return the value of the attribute "latitude"
     */
    public double getLongitude(){
        return this.longitude;
    }


//    public Set<Long> getReferencingWays(){
//        return this.referencingWays;
//    }


//    /**
//     * Adds a wayID to the set of wayIDs referencing this node, i.e.
//     * @param wayID
//     */
//    public void addReferencingWay(Long wayID){
//        this.referencingWays.add(wayID);
//    }


    /**
     * Returns a {@link String} representation of this {@link NodeElement}.
     *
     * @return a {@link String} representation of this {@link NodeElement}.
     */
    @Override
    public String toString(){
        StringBuilder result = new StringBuilder();
        result.append("osm:node (ID: ").append(this.getID()).append(", Latitude: ").append(this.latitude)
              .append(", Longitude: ").append(this.longitude).append(", Tags: [");

        Iterator<Map.Entry<String, String>> tagIterator = this.getTags().entrySet().iterator();
        while(tagIterator.hasNext()){
            Map.Entry<String, String> entry = tagIterator.next();
            result.append("k=\"").append(entry.getKey()).append("\", v=\"").append(entry.getValue()).append("\"");

            if(tagIterator.hasNext()){
                result.append(" | ");
            }
        }

        result.append("])");

        return result.toString();
    }


    public static class PlainNodeElement extends AbstractPlainLevel2Element {

        @XmlAttribute(name = "lat")
        private double latitude;

        @XmlAttribute(name = "lon")
        private double longitude;

        private PlainNodeElement(){}

        private PlainNodeElement(NodeElement nodeElement){
            super(nodeElement);
            this.latitude = nodeElement.getLatitude();
            this.longitude = nodeElement.getLongitude();
        }

        private double getLatitude(){
            return this.latitude;
        }

        private double getLongitude(){
            return this.longitude;
        }
    }


    public static class OsmNodeElementAdapter extends XmlAdapter<PlainNodeElement, NodeElement>{

        public OsmNodeElementAdapter(){
            log.debug("Instance of {} created!", this.getClass().getName());
        }

        @Override
        public NodeElement unmarshal(PlainNodeElement plainNodeElement) throws Exception {
            return new NodeElement(plainNodeElement);
        }

        @Override
        public PlainNodeElement marshal(NodeElement nodeElement) throws Exception {
            return new PlainNodeElement(nodeElement);
        }
    }
}
