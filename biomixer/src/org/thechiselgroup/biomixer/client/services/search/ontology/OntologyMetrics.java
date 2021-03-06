package org.thechiselgroup.biomixer.client.services.search.ontology;

/**
 * Contains statistics for an ontology. First made to support storage of results
 * from query to ontology statistics REST call.
 * 
 * @author everbeek
 * 
 */
public class OntologyMetrics {

    public Integer numberOfClasses;

    public Integer numberOfIndividuals;

    public Integer numberOfProperties;

    public Integer maximumDepth;

    final public String ontologyAcronym;

    public OntologyMetrics(String ontologyAcronym) {
        this.ontologyAcronym = ontologyAcronym;
    }

}
