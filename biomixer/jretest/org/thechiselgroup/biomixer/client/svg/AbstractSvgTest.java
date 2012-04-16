/*******************************************************************************
 * Copyright 2012 Lars Grammel 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.svg;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.server.core.util.IOUtils;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.text_renderer.TextSvgElement;
import org.thechiselgroup.biomixer.shared.svg.text_renderer.TextSvgElementFactory;

public abstract class AbstractSvgTest {

    protected TextSvgElementFactory svgElementFactory;

    public void assertElementEqualsFile(String fileIdentifier, String xmlElement) {
        try {
            XmlTestUtils.assertXmlEquals(getExpectedSvg(fileIdentifier),
                    xmlElement);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void assertElementEqualsFile(String fileIdentifier,
            SvgElement element) {

        TextSvgElement svgElement = svgElementFactory.createElement(Svg.SVG);
        svgElement.setAttribute("xmlns", Svg.NAMESPACE);
        svgElement.setAttribute("version", "1.1");
        svgElement.appendChild(element);

        assertSvgRootElementEqualsFile(fileIdentifier, svgElement);
    }

    public void assertSvgElementEquals(String expectedSvg, SvgElement element) {
        XmlTestUtils.assertXmlEquals(expectedSvg,
                ((TextSvgElement) element).toXML());
    }

    public void assertSvgRootElementEqualsFile(String fileIdentifier,
            SvgElement element) {
        try {
            assertSvgElementEquals(getExpectedSvg(fileIdentifier), element);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getExpectedSvg(String fileIdentifier) throws IOException {
        String fileName = fileIdentifier + ".svg";
        InputStream stream = getClass().getResourceAsStream(fileName);
        assert stream != null : "file " + fileName + " not loaded";
        return IOUtils.readIntoString(stream);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        svgElementFactory = new TextSvgElementFactory();
    }

}