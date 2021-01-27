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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Created by olli on 15.06.14.
 */
@XmlTransient
public abstract class AbstractOsmElement{

    protected static final String ATT_VERSION = "version";
    protected static final String ATT_GENERATOR = "generator";
    protected static final String ATT_COPYRIGHT = "copyright";
    protected static final String ATT_ATTRIBUTION = "attribution";
    protected static final String ATT_LICENSE = "license";

    protected static final String ELEM_BOUNDS = "bounds";
    protected static final String ELEM_NODE = "node";
    protected static final String ELEM_WAY = "way";

    protected static final String PROP_VERSION = "version";
    protected static final String PROP_GENERATOR = "generator";
    protected static final String PROP_COPYRIGHT = "copyright";
    protected static final String PROP_ATTRIBUTION = "attribution";
    protected static final String PROP_LICENSE = "license";

    protected static final String PROP_BOUNDS = "boundsElement";
    protected static final String PROP_NODE = "nodeElements";
    protected static final String PROP_WAY = "wayElements";
    

    private String version;
    private String generator;
    private String copyright;
    private String attribution;
    private String license;
    private BoundsElement boundsElement;

    protected AbstractOsmElement(){}
    
    protected AbstractOsmElement(AbstractOsmElement abstractOsmElement){
        this.setVersion(abstractOsmElement.getVersion());
        this.setGenerator(abstractOsmElement.getGenerator());
        this.setCopyright(abstractOsmElement.getCopyright());
        this.setAttribution(abstractOsmElement.getAttribution());
        this.setLicense(abstractOsmElement.getLicense());
    }
    
    
    @XmlAttribute(name = ATT_VERSION)
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @XmlAttribute(name = ATT_GENERATOR)
    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator){
        this.generator = generator;
    }

    @XmlAttribute(name = ATT_COPYRIGHT)
    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    @XmlAttribute(name = ATT_ATTRIBUTION)
    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    @XmlAttribute(name = ATT_LICENSE)
    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public void setBoundsElement(BoundsElement boundsElement){
        this.boundsElement = boundsElement;
    }

    @XmlElement(name = ELEM_BOUNDS)
    public BoundsElement getBoundsElement() {
        return boundsElement;
    }

}
