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

import javanet.staxutils.IndentingXMLEventWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import de.apollomasterbht.logger.Log;
import de.uzl.itm.jaxb4osm.jaxb.OsmElement;
/**
 * This is a class to provide a static method to marshal OSM files.
 *
 * @author Oliver Kleine
 */
public class OsmMarshaller {
	
	static Log log = new Log();

    private static Marshaller marshaller;
    static{
        try{
            JAXBContext context = JAXBContext.newInstance(OsmElement.PlainOsmElement.class);
            marshaller = context.createMarshaller();
        }
        catch (Exception ex){
            log.error("This should never happen!", ex);
        }
    }


    /**
     * Marshals the given {@link de.uzl.itm.jaxb4osm.jaxb.OsmElement} and writes the result on the
     * given {@link java.io.OutputStream}.
     *
     * @param osmElement the {@link de.uzl.itm.jaxb4osm.jaxb.OsmElement} to be marshaled
     * @param outputStream the {@link java.io.OutputStream} to write the result on
     *
     * @throws Exception if some error occurred
     */
    public static void marshal(OsmElement osmElement, OutputStream outputStream) throws Exception{

        OsmElement.PlainOsmElement plainOsmElement = new OsmElement.OsmElementAdapter().marshal(osmElement);

        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
        IndentingXMLEventWriter xmlEventWriter =
                new IndentingXMLEventWriter(xmlOutputFactory.createXMLEventWriter(outputStream));

        marshaller.marshal(plainOsmElement, xmlEventWriter);
    }


    public static void main(String[] args) throws Exception{
        String pathToOriginalOsmFile = args[0];
        String pathToNewOsmFile = args[1];

        File newFile = new File(pathToNewOsmFile);
        newFile.createNewFile();
        FileOutputStream outputStream = new FileOutputStream(newFile);

        OsmElement osmElement = OsmUnmarshaller.unmarshal(new FileInputStream(new File(pathToOriginalOsmFile)));
        OsmMarshaller.marshal(osmElement, outputStream);
    }
}
