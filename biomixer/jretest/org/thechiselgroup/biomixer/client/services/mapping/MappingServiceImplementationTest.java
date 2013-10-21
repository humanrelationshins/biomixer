/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel, Bo Fu
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
package org.thechiselgroup.biomixer.client.services.mapping;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.biomixer.client.core.util.callbacks.TrackingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.util.url.DefaultUrlBuilder;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class MappingServiceImplementationTest {

    private static final String URL = "test-url-string";

    @Mock
    private UrlBuilderFactory urlBuilderFactory;

    @Mock
    private UrlFetchService urlFetchService;

    private ConceptMappingServiceImplementation underTest;

    @Mock
    private MappingResponseJsonParser responseParser;

    private String conceptUri;

    String ontologyId1 = "1ontologyId1";

    String conceptId1 = "1conceptId1";

    private DefaultUrlBuilder urlBuilder;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ResourceNeighbourhood doGetMappings(List<Resource> parsedMappings)
            throws Exception {

        String xmlResultStub = "xmlResultStub";

        AsyncCallback callback = mock(AsyncCallback.class);

        when(responseParser.parseForConceptMapping(xmlResultStub)).thenReturn(
                parsedMappings);

        ArgumentCaptor<TrackingAsyncCallback> captor = ArgumentCaptor
                .forClass(TrackingAsyncCallback.class);
        doNothing().when(urlFetchService).fetchURL(eq(URL), captor.capture());

        underTest.getMappings(ontologyId1, Concept.getConceptId(conceptUri),
                false, callback);

        AsyncCallback<String> xmlResultCallback = captor.getValue();

        ArgumentCaptor<ResourceNeighbourhood> captor2 = ArgumentCaptor
                .forClass(ResourceNeighbourhood.class);
        doNothing().when(callback).onSuccess(captor2.capture());

        xmlResultCallback.onSuccess(xmlResultStub);

        return captor2.getValue();
    }

    @Test
    public void returnParsedMappings() throws Exception {
        String targetUri = Concept.toConceptURI("o2", "i2");
        String sourceUri = Concept.toConceptURI("o3", "i3");

        List<Resource> mockedMappings = new ArrayList<Resource>();
        mockedMappings.add(Mapping.createMappingResource("mappingId1",
                conceptUri, targetUri, "o2", "o3"));
        mockedMappings.add(Mapping.createMappingResource("mappingId2",
                sourceUri, conceptUri, "o3", "o2"));

        ResourceNeighbourhood result = doGetMappings(mockedMappings);

        assertThat(result.getResources(), equalTo(mockedMappings));
    }

    @Test
    public void returnSourceMappingPartialProperties() throws Exception {
        String sourceUri = Concept.toConceptURI("o3", "i3");
        String mappingId = "mappingId1";

        List<Resource> mockedMappings = new ArrayList<Resource>();
        mockedMappings.add(Mapping.createMappingResource(mappingId, sourceUri,
                conceptUri, "o3", ontologyId1));

        ResourceNeighbourhood result = doGetMappings(mockedMappings);

        UriList targetList = (UriList) result.getPartialProperties().get(
                Concept.INCOMING_MAPPINGS);
        assertThat(targetList.size(), is(1));
        assertThat(targetList.contains(Mapping.toMappingURI(mappingId)),
                is(true));
    }

    @Test
    public void returnTargetMappingPartialProperties() throws Exception {
        String targetUri = Concept.toConceptURI("o2", "i2");
        String mappingId = "mappingId1";

        List<Resource> mockedMappings = new ArrayList<Resource>();
        mockedMappings.add(Mapping.createMappingResource(mappingId, conceptUri,
                targetUri, ontologyId1, "o2"));

        ResourceNeighbourhood result = doGetMappings(mockedMappings);

        UriList targetList = (UriList) result.getPartialProperties().get(
                Concept.OUTGOING_MAPPINGS);
        assertThat(targetList.size(), is(1));
        assertThat(targetList.contains(Mapping.toMappingURI(mappingId)),
                is(true));
    }

    @Before
    public void setUp() {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        conceptUri = Concept.toConceptURI(ontologyId1, conceptId1);

        underTest = new ConceptMappingServiceImplementation(responseParser,
                urlFetchService, urlBuilderFactory);

        this.urlBuilder = Mockito.spy(new DefaultUrlBuilder());

        when(urlBuilderFactory.createUrlBuilder()).thenReturn(urlBuilder);
        when(urlBuilder.toString()).thenReturn(URL);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void urlFetched() {
        String ontologyId = "1ontologyId1";
        String conceptId = "1conceptId1";

        underTest.getMappings(ontologyId, conceptId, false,
                mock(AsyncCallback.class));

        verify(urlFetchService, times(1)).fetchURL(eq(URL),
                any(TrackingAsyncCallback.class));
    }
}