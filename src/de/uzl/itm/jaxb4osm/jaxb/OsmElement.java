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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import de.apollomasterbht.logger.Log;
import de.uzl.itm.jaxb4osm.tools.WayElementFilter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.*;

/**
 * JAXB compliant class for the osm (root) element in OSM files (Open Street Map)
 *
 * @author Oliver Kleine
 */

public class OsmElement extends AbstractOsmElement{

    private static Log log = new Log();

    private Map<Long, NodeElement> nodeElements;

    private Map<Long, WayElement> wayElements;
    private Multimap<Long, Long> nodeReferences;

    /**
     * Creates a new empty instance of {@link OsmElement}
     */
    public OsmElement(){
        initialze();
    }


    private OsmElement(PlainOsmElement plainOsmElement, WayElementFilter filter){
        super(plainOsmElement);
        initialze();

        //Add <node> elements
        this.addNodeElements(plainOsmElement.getNodeElements());

        //Add <way> elements that match the given filter criteria
        for(WayElement wayElement : plainOsmElement.getWayElements()){
            if(filter.matchesCriteria(wayElement)){
                this.addWayElement(wayElement);
            }
        }
    }

    private void initialze(){
        this.nodeElements = new HashMap<>();
        this.nodeReferences = HashMultimap.create();
        this.wayElements = new HashMap<>();
    }


    public void addNodeElements(Collection<NodeElement> nodeElements){
        for(NodeElement nodeElement : nodeElements){
            addNodeElement(nodeElement);
        }
    }


    public void addNodeElement(NodeElement nodeElement){
        this.nodeElements.put(nodeElement.getID(), nodeElement);
    }


    public boolean removeNodeElement(long nodeID){
        return this.nodeElements.remove(nodeID) != null;
    }

    /**
     * Returns a {@link java.util.Map} with the values of the nodes ID attributes as keys and the
     * {@link NodeElement}s as values
     *
     * @return a {@link java.util.Map} with the values of the nodes ID attributes as keys and the
     * {@link NodeElement}s as values
     */
    public ImmutableList<NodeElement> getNodeElements() {
        return new ImmutableList.Builder<NodeElement>().addAll(this.nodeElements.values()).build();
    }


    /**
     * Returns the {@link NodeElement} that has the given ID or
     * <code>null</code> if no such node was found.
     *
     * @param nodeID the ID to lookup the corresponding node for
     *
     * @return the {@link NodeElement} that has the given ID or
     * <code>null</code> if no such node was found.
     */
    public NodeElement getNodeElement(long nodeID){
        return this.nodeElements.get(nodeID);
    }

    /**
     * Adds the given {@link WayElement}s and updates the references returned
     * by {@link #getReferencingWayIDs(long)} properly.
     *
     * @param wayElements the {@link WayElement}s to be added
     *
     * @return the number of added {@link WayElement}s.
     *
     * <b>Note:</b> Elements with {@link WayElement#getID()} returning an ID that is already contained will not be
     * added, i.e. already contained ways are not overwritten! That's why the returned number may vary from the size
     * of the given {@link java.util.Collection}.
     */
    public int addWayElements(Collection<WayElement> wayElements){
        int counter = 0;

        for(WayElement wayElement : wayElements){
            if(this.addWayElement(wayElement)){
                counter++;
            }
        }

        return counter;
    }


    /**
     * Adds the given {@link WayElement} and updates the references returned
     * by {@link #getReferencingWayIDs(long)} properly.
     *
     * @param wayElement the {@link WayElement} to be added
     *
     * @return <code>true</code> if the element was successfully added or <code>false</code> otherwise
     *
     * <b>Note:</b> Elements with {@link WayElement#getID()} returning an ID that is already contained will not be
     * added, i.e. already contained ways are not overwritten!
     */
    public boolean addWayElement(WayElement wayElement){

        if(this.wayElements.containsKey(wayElement.getID()))
            return false;

        this.wayElements.put(wayElement.getID(), wayElement);

        for(NdElement ndElement : wayElement.getNdElements()){
            this.nodeReferences.put(ndElement.getReference(), wayElement.getID());
        }

        return true;
    }


    /**
     * Removes the {@link WayElement} with the given ID and updates the references
     * returned by {@link #getReferencingWayIDs(long)} properly.
     *
     * @param wayID the ID of the {@link WayElement} to be removed
     *
     * @return <code>true</code> if the element was successfully removed or <code>false</code> otherwise, i.e. there
     * was no way with the given ID
     */
    public boolean removeWayElement(long wayID){
        WayElement wayElement = this.wayElements.remove(wayID);

        if(wayElement == null)
            return false;

        for(NdElement ndElement : wayElement.getNdElements()){
            this.nodeReferences.remove(ndElement.getReference(), wayElement.getID());
        }

        return true;
    }

    /**
     * Returns an {@link com.google.common.collect.ImmutableList} containing the already added
     * {@link WayElement}s
     *
     * <b>Note:</b> The reason for making the result immutable is to keep the references returned by
     * {@link #getReferencingWayIDs(long)} properly
     *
     * @return an {@link com.google.common.collect.ImmutableList} containing the already added
     * {@link WayElement}s
     */
    public ImmutableList<WayElement> getWayElements(){
        return new ImmutableList.Builder<WayElement>().addAll(this.wayElements.values()).build();
    }


    /**
     * Returns the {@link WayElement} that has the given ID or
     * <code>null</code> if no such way was found.
     *
     * @return the {@link WayElement} that has the given ID or
     * <code>null</code> if no such way was found.
     */
    public WayElement getWayElement(long wayID){
        return this.wayElements.get(wayID);
    }


    /**
     * Returns an {@link com.google.common.collect.ImmutableSet} containing the IDs of the
     * {@link WayElement}s that were already added and refer to the given
     * nodeID.
     *
     * <b>Note:</b> The reason for making the result immutable is to keep the references returned by
     * {@link #getReferencingWayIDs(long)} properly
     *
     * @return an {@link com.google.common.collect.ImmutableSet} containing the IDs of the
     * {@link WayElement}s that were already added and refer to the given
     * nodeID.
     */
    public ImmutableSet<Long> getReferencingWayIDs(long nodeID){
        if(!this.nodeReferences.containsKey(nodeID))
            return new ImmutableSet.Builder<Long>().build();

        return new ImmutableSet.Builder<Long>().addAll(this.nodeReferences.get(nodeID)).build();
    }

    /**
     * This class is for internal use only and is public due to the restrictions (or bug?) of the
     * {@link javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter} not to be applicable on XML root elements.
     */
    @XmlRootElement(name = "osm")
    @XmlType (propOrder={
            PROP_VERSION, PROP_GENERATOR, PROP_COPYRIGHT, PROP_ATTRIBUTION, PROP_LICENSE,
            PROP_BOUNDS, PROP_NODE, PROP_WAY
    })
    @XmlSeeAlso(AbstractOsmElement.class)
    public static class PlainOsmElement extends AbstractOsmElement{

        @XmlElement(name = ELEM_NODE)
        private List<NodeElement> nodeElements;

        @XmlElement(name = ELEM_WAY)
        private List<WayElement> wayElements;


        private PlainOsmElement(){
            this.initialize();
        }

        private PlainOsmElement(OsmElement osmElement){
            super(osmElement);
            this.initialize();
            this.addNodeElements(osmElement.getNodeElements());
            this.addWayElements(osmElement.getWayElements());
        }


        private void initialize(){
            this.nodeElements = new ArrayList<>();
            this.wayElements = new ArrayList<>();
        }

        private void addNodeElements(Collection<NodeElement> nodeElements){
            this.nodeElements.addAll(nodeElements);
        }

        private List<NodeElement> getNodeElements(){
            return this.nodeElements;
        }

        private void addWayElements(Collection<WayElement> wayElements){
            this.wayElements.addAll(wayElements);
        }

        private List<WayElement> getWayElements(){
            return this.wayElements;
        }
    }


    public static class OsmElementAdapter extends XmlAdapter<PlainOsmElement, OsmElement>{

        public OsmElement unmarshal(PlainOsmElement plainOsmElement, WayElementFilter wayElementFilter){
            return new OsmElement(plainOsmElement, wayElementFilter);
        }

        @Override
        public OsmElement unmarshal(PlainOsmElement plainOsmElement) throws Exception {
            return unmarshal(plainOsmElement, WayElementFilter.ANY_WAY) ;
        }


        @Override
        public PlainOsmElement marshal(OsmElement osmElement) throws Exception {
            return new PlainOsmElement(osmElement);
        }
    }



}
