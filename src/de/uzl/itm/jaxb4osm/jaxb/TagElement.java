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

import de.apollomasterbht.logger.Log;

/**
 * JAXB compliant class for the tag element in OSM files (from OpenStreetMap)
 *
 * @author Oliver Kleine
 */
public class TagElement {

    private static Log log = new Log();

    @XmlAttribute(name = "k")
    private String key;


    @XmlAttribute(name = "v")
    private String value;


    private TagElement(){}

    /**
     * Creates a new instance of {@link TagElement}
     * @param key the value of the tag elements attribute "k"
     * @param value the value of the tag elements attribute "v"
     */
    public TagElement(String key, String value){
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the value of the attribute "k" (e.g. <code>xyz</code> if the tag element was like
     * <code><tag k="xyz" ...></code>).
     *
     * @return the value of the attribute "k"
     */
    public String getKey(){
        return this.key;
    }

    /**
     * Returns the value of the attribute "value" (e.g. <code>xyz</code> if the tag element was like
     * <code><tag ... v="xyz" ...></code>).
     *
     * @return the value of the attribute "v"
     */
    public String getValue(){
        return this.value;
    }


    @Override
    public int hashCode(){
        return this.key.hashCode() + this.value.hashCode();
    }

    /**
     * Returns a {@link String} representation of this {@link TagElement}.
     *
     * @return a {@link String} representation of this {@link TagElement}.
     */
    @Override
    public String toString(){
        return "osm:tag (key=" + this.key + ", value=" + this.value + ")";
    }

    @Override
    public boolean equals(Object object){
        if(!(object instanceof TagElement))
            return false;

        TagElement other = (TagElement) object;
        return this.getKey().equals(other.getKey()) && this.getValue().equals(other.getValue());
    }
}
