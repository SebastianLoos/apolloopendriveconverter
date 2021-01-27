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
package de.uzl.itm.jaxb4osm.tools;

import de.apollomasterbeuth.logger.Log;
import de.uzl.itm.jaxb4osm.jaxb.NodeElement;
import de.uzl.itm.jaxb4osm.jaxb.OsmElement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a class to provide static methods to unmarshal OSM files.
 *
 * @author Oliver Kleine
 */
public class OsmUnmarshaller {

    private static Log log = new Log();

    private static Unmarshaller unmarshaller;
    static{
        try{
            JAXBContext context = JAXBContext.newInstance(OsmElement.PlainOsmElement.class);
            unmarshaller = context.createUnmarshaller();
        }
        catch (Exception ex){
            log.error("This should never happen!", ex);
        }
    }

    /**
     * Shortcurt for <code>unmarshal(inputStream, WayElementFilter.ANY_WAY, false)</code>
     *
     * @param inputStream the {@link java.io.InputStream} to read the data to be de-serialized from
     *
     * @return the unmarshalled {@link de.uzl.itm.jaxb4osm.jaxb.OsmElement}
     *
     * @throws Exception if some error occurred
     */
    public static OsmElement unmarshal(InputStream inputStream) throws Exception {
        return unmarshal(inputStream, WayElementFilter.ANY_WAY);
    }

    /**
     * Shortcurt for <code>unmarshal(inputStream, filter, false)</code>
     *
     * @param inputStream the {@link java.io.InputStream} to read the data to be de-serialized from
     *
     * @return the unmarshalled {@link de.uzl.itm.jaxb4osm.jaxb.OsmElement}
     *
     * @throws Exception if some error occurred
     */
    public static OsmElement unmarshal(InputStream inputStream, WayElementFilter filter) throws Exception {
        return unmarshal(inputStream, filter, false);
    }

    /**
     * Deserializes the given OSM file into one {@link de.uzl.itm.jaxb4osm.jaxb.OsmElement} instance.
     *
     * @param inputStream the {@link java.io.InputStream} to read the data to be de-serialized from
     * @param filter the {@link WayElementFilter} to be applied
     * @param removeUnreferencedNodes <code>true</code> if the {@link java.util.Map} returned by
     *                                {@link de.uzl.itm.jaxb4osm.jaxb.OsmElement#getNodeElements()}
     *                                is supposed to contain only the
     *                                {@link de.uzl.itm.jaxb4osm.jaxb.NodeElement}s that are referenced at
     *                                least by one {@link de.uzl.itm.jaxb4osm.jaxb.WayElement} contained in
     *                                the {@link java.util.Map} returned by
     *                                {@link de.uzl.itm.jaxb4osm.jaxb.OsmElement#getWayElements()}.
     *
     * @throws Exception if some unexpected error occurred
     */
    public static OsmElement unmarshal(InputStream inputStream, WayElementFilter filter, boolean removeUnreferencedNodes)
            throws Exception{

        //create xml event reader for input stream
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(inputStream);

        //Do the un-marshalling
        OsmElement.PlainOsmElement plainOsmElement =
                unmarshaller.unmarshal(xmlEventReader, OsmElement.PlainOsmElement.class).getValue();

        OsmElement osmElement = new OsmElement.OsmElementAdapter().unmarshal(plainOsmElement, filter);

        if(removeUnreferencedNodes){
            Set<Long> unreferencedNodes = new HashSet<>();
            for(NodeElement nodeElement : osmElement.getNodeElements()){
                if(osmElement.getReferencingWayIDs(nodeElement.getID()).size() == 0){
                    unreferencedNodes.add(nodeElement.getID());
                }
            }

            for(long nodeID : unreferencedNodes){
                osmElement.removeNodeElement(nodeID);
            }
        }

        return osmElement;
    }


    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        String pathToOsmFile = args[0];
        FileInputStream inputStream = new FileInputStream(new File(pathToOsmFile));
        OsmElement osmElement  = OsmUnmarshaller.unmarshal(inputStream, WayElementFilter.STREETS, true);

        System.out.println("Found " + osmElement.getNodeElements().size() + " nodes.");

        System.out.println("Found " + osmElement.getWayElements().size() + " streets.");

        int crossings = 0;
        for(NodeElement nodeElement : osmElement.getNodeElements()){
            if(osmElement.getReferencingWayIDs(nodeElement.getID()).size() > 1) crossings++;
        }

        System.out.println("Found " + crossings + " crossings");

        System.out.println("Time passed: " + (System.currentTimeMillis() - start));

        Thread.sleep(100);
    }
}
