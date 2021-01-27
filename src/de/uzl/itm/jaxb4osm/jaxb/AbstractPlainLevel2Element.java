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

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for unmarshalled XML elements from OSM files (node, way, relation) before being processed by
 * the appropriate {@link javax.xml.bind.annotation.adapters.XmlAdapter}.
 *
 * @author Oliver Kleine
 */
public abstract class AbstractPlainLevel2Element extends AbstractLevel2Element{

    @XmlElement(name = "tag", type=TagElement.class)
    private List<TagElement> tagElements;

    protected AbstractPlainLevel2Element(){
        this.tagElements = new ArrayList<>();
    }

    protected AbstractPlainLevel2Element(AbstractAdaptedLevel2Element abstractAdaptedLevel2Element){
        super(abstractAdaptedLevel2Element);
        this.tagElements = new ArrayList<>();

        for(Map.Entry<String, String> entry : abstractAdaptedLevel2Element.getTags().entrySet()){
            this.tagElements.add(new TagElement(entry.getKey(), entry.getValue()));
        }
    }

    protected void addTagElement(String key, String value){
        this.tagElements.add(new TagElement(key, value));
    }

    List<TagElement> getTagElements(){
        return this.tagElements;
    }
}
