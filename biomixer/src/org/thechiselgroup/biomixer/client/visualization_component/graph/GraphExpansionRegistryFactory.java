package org.thechiselgroup.biomixer.client.visualization_component.graph;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.graph.AutomaticConceptExpander;
import org.thechiselgroup.biomixer.client.graph.ConceptConceptNeighbourhoodExpander;
import org.thechiselgroup.biomixer.client.graph.ConceptConceptNeighbourhoodLoader;
import org.thechiselgroup.biomixer.client.graph.ConceptMappingNeighbourhoodExpander;
import org.thechiselgroup.biomixer.client.graph.ConceptMappingNeighbourhoodLoader;
import org.thechiselgroup.biomixer.client.graph.GraphNodeNeighbourhoodRemover;
import org.thechiselgroup.biomixer.client.graph.MappingExpander;
import org.thechiselgroup.biomixer.client.services.mapping.MappingServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.TermServiceAsync;

import com.google.inject.Inject;

public class GraphExpansionRegistryFactory {

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private MappingServiceAsync mappingService;

    @Inject
    private ConceptNeighbourhoodServiceAsync conceptNeighbourhoodService;

    @Inject
    private TermServiceAsync termService;

    public GraphExpansionRegistry createRegistry(ErrorHandler errorHandler) {
        DefaultGraphExpansionRegistry registry = new DefaultGraphExpansionRegistry();

        registry.putAutomaticExpander(Concept.RESOURCE_URI_PREFIX,
                new AutomaticConceptExpander(
                        new ConceptMappingNeighbourhoodLoader(mappingService,
                                resourceManager, errorHandler),
                        new ConceptConceptNeighbourhoodLoader(errorHandler,
                                resourceManager, conceptNeighbourhoodService)));

        registry.putNodeMenuEntry(Concept.RESOURCE_URI_PREFIX, "Concepts",
                new ConceptConceptNeighbourhoodExpander(errorHandler,
                        resourceManager, conceptNeighbourhoodService));

        registry.putNodeMenuEntry(Concept.RESOURCE_URI_PREFIX, "Mappings",
                new ConceptMappingNeighbourhoodExpander(mappingService,
                        errorHandler, resourceManager, termService));

        registry.putNodeMenuEntry(Mapping.RESOURCE_URI_PREFIX, "Concepts",
                new MappingExpander(resourceManager));

        registry.putNodeMenuEntry(
                Concept.RESOURCE_URI_PREFIX,
                "Remove",
                new GraphNodeNeighbourhoodRemover(errorHandler, resourceManager));

        return registry;
    }

}