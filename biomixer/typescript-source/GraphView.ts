///<reference path="headers/require.d.ts" />

///<reference path="headers/d3.d.ts" />

export class GraphDataForD3<N extends BaseNode, L extends BaseLink<any>> {
    public nodes: Array<N> = [];
    public links: Array<L> = [];
}

export class BaseNode implements D3.Layout.GraphNode {
    id: number;
    index: number;
    name: string;
    px: number;
    py: number;
    size: number;
    weight: number;
    x: number;
    y: number;
    subindex: number;
    startAngle: number;
    endAngle: number;
    value: number;
    fixed: boolean;
    children: D3.Layout.GraphNode[];
    _children: D3.Layout.GraphNode[];
    parent: D3.Layout.GraphNode;
    depth: number;
    
    getEntityId(): string{
        return "Error, must override this method.";
    }
}

export class BaseLink<N extends BaseNode> {
    source: N;
    target: N;
}

export interface Graph {
    
}

// Notes on usage with this pattern:
// If I don't extend and implement both GraphView and BaseGraphView, I have to define things I want implemented in the base class,
// and I won't be forced to define things declared in the interface. Using the interface as the
// type later leads to a full contract of behavior; the doubling up of interface and base class
// here is only important for implementations.
// Example of class using these begins like so:
// export class OntologyMappingOverview extends GraphView.BaseGraphView implements GraphView.GraphView { ...
// Also see "Mixins and Multiple Inheritance" here: http://www.sitepen.com/blog/2013/12/31/definitive-guide-to-typescript/
export interface GraphView<N extends BaseNode, L extends BaseLink<BaseNode>> extends BaseGraphView<N, L> {
    visWidth(): number;
    visHeight(): number;
    linkMaxDesiredLength(): number;
    // needs to contain the onTick listener function
    onLayoutTick(): {()} ;
    
    populateNewGraphElements(data: GraphDataForD3<N, L>, expectingNew: boolean);
    populateNewGraphEdges(links: Array<L>);
    populateNewGraphNodes(nodes: Array<N>);
    removeMissingGraphElements(data: GraphDataForD3<N, L>);
    filterGraphOnMappingCounts();
    updateDataForNodesAndLinks(newDataSubset: GraphDataForD3<N, L>);
    createNodePopupTable(nodeSvg, nodeData);
    sortConceptNodesCentralOntologyName(): Array<N>;
}

export class BaseGraphView<N extends BaseNode, L extends BaseLink<BaseNode>> {
// TODO Review this interface. A lot fo this should probably be made more
// listener orietented rather than direct call. But the system is shallow now,
// so maybe this is what we want.
    
    //var defaultNodeColor = "#496BB0";
    defaultNodeColor = "#000000";
    defaultLinkColor = "#999";
    nodeHighlightColor = "#FC6854";
    
    static nodeSvgClassSansDot = "node";
    static nodeInnerSvgClassSansDot = "inner_node"; // Needed for ontology double-node effect
    static nodeLabelSvgClassSansDot = "nodetext";
    static linkSvgClassSansDot = "link";
    static linkLabelSvgClassSansDot = "linktext";
    
    static ontologyNodeSvgClassSansDot = "ontologyNode";
    static ontologyLinkSvgClassSansDot = "ontologyMappingLink"
    
    static conceptNodeSvgClassSansDot = "conceptNode";
    static conceptLinkSvgClassSansDot = "conceptLink"
    
    static nodeSvgClass = "."+BaseGraphView.nodeSvgClassSansDot;
    static nodeInnerSvgClass = "."+BaseGraphView.nodeInnerSvgClassSansDot;
    static nodeLabelSvgClass = "."+BaseGraphView.nodeLabelSvgClassSansDot;
    static linkSvgClass = "."+BaseGraphView.linkSvgClassSansDot;
    static linkLabelSvgClass = "."+BaseGraphView.linkLabelSvgClassSansDot;
    
    
    
    alphaCutoff: number = 0.01; // used to stop the layout early in the tick() callback
    forceLayout: D3.Layout.ForceLayout = undefined;
    dragging = false;
    
    visWidth(){ return $("#chart").width(); }
    visHeight(){ return $("#chart").height(); }
    linkMaxDesiredLength(){ return Math.min(this.visWidth(), this.visHeight())/2 - 50; }
    
     resizedWindowLambda  = () => {
        d3.select("#graphRect")
        .attr("width", this.visWidth())
        .attr("height", this.visHeight());
        
        d3.select("#graphSvg")
        .attr("width", this.visWidth())
        .attr("height", this.visHeight());
        
        // TODO Layouts not relying on force need additional support here.
         // This might need to call back into an instance method named something like "layoutResized"
        if(this.forceLayout){
            this.forceLayout.size([this.visWidth(), this.visHeight()]).linkDistance(this.linkMaxDesiredLength());
            // If needed, move all the nodes towards the new middle here.
            this.forceLayout.resume();
        }  
    }
    
    updateStartWithoutResume(){
        // When start(0 is called, the last thing it does is to call resume(),
        // which calls alpha(.1). I need this to not occur...
        var resume = this.forceLayout.resume;
        this.forceLayout.resume = ()=>{ return this.forceLayout; };
        this.forceLayout.start();
        this.forceLayout.resume = resume;
    }
    
    // These are needed to do a refresh of popups when new data arrives and the user has the popup open
    lastDisplayedTipsy = null;
    lastDisplayedTipsyData = null;
    lastDisplayedTipsySvg = null;

    /**
     * Set this function to whichever function has most recently been
     * used or is about to be used. Allows refreshing or resetting of layouts.
     */
    runCurrentLayout: (refreshLayout?: boolean) => void ;
    
    getAdjacentLinks(node: N){
        return d3.selectAll(BaseGraphView.linkSvgClass)
        .filter(
            function(d: L, i) {
                    return d.source === node || d.target === node;
            }
        );
    }
    
    highlightHoveredLinkLambda(outerThis: BaseGraphView<N, L>){
        return function(linkLine: L, i){
            if(outerThis.dragging){
                return;
            }
            
            d3.selectAll(BaseGraphView.nodeLabelSvgClass)
                .classed("highlightedNodeLabel", true)
                .filter(function(aText: N, i){return aText.getEntityId() ===linkLine.source.getEntityId() || aText.getEntityId() ===linkLine.target.getEntityId();})
                .classed("dimmedNodeLabel", false)
                .classed("highlightedNodeLabel", true)
                ;
            
            d3.selectAll(BaseGraphView.nodeSvgClass+", "+BaseGraphView.nodeInnerSvgClass)
                .classed("dimmedNode", true)
                .filter(function(aNode: N, i){return aNode.getEntityId() === linkLine.source.getEntityId() || aNode.getEntityId() === linkLine.target.getEntityId();})
                .classed("dimmedNode", false)
                .classed("highlightedNode", true)
                ;
                            
            d3.selectAll(BaseGraphView.linkSvgClass)
                .classed("dimmedLink", true)
                ;
            
            // if we ever use this method attached to anything other than a link hover over, it won't
            // work, because the "this" reference below won't be a line rendered, but whatever we
            // attached the method to.
            // d3.select(this)
            // Defensively, I changed it to grab the correct link via d3.select().filter() instead.
            d3.select(BaseGraphView.linkSvgClass)
                .filter(function(d: L, i){return d === linkLine; })
                .classed("dimmedLink", false)
                .classed("highlightedLink", true)
                ;
        }
    }
    
    highlightHoveredNodeLambda(outerThis: BaseGraphView<N, L>){
        return function(nodeData: N, i){
            if(outerThis.dragging){
                return;
            }
            
            outerThis.beforeNodeHighlight(nodeData);
            
            d3.selectAll(BaseGraphView.linkSvgClass)
            .classed("dimmedLink", true);
            
            d3.selectAll(BaseGraphView.nodeSvgClass+", "+BaseGraphView.nodeInnerSvgClass)
            .classed("dimmedNode", true);
                
            d3.selectAll(BaseGraphView.nodeLabelSvgClass)
                .classed("dimmedNodeLabel", true)
                .filter(function(aText: N, i){return aText.getEntityId() === nodeData.getEntityId();})
                .classed("dimmedNodeLabel", false)
                .classed("highlightedNodeLabel", true);
            
            // D3 doesn't have a way to get from bound data to what it is bound to?
            // Doing it thsi way isntead of d3.select(this) so I can re-use this method with things like
            // checkboxes outside the graph, which will trigger graph behaviors.
            var sourceNode: D3.Selection = d3.select(BaseGraphView.nodeLabelSvgClassSansDot).filter(function(d: N, i){return d === nodeData; });
            
            sourceNode
             .classed("highlightedNode", true);
            
             // There must be a less loopy, data oriented way to achieve this.
             // I recently modified it to *not* use x and y coordinates to identify ndoes and edges, which was heinous.
             // Looping over everything is just as ugly (but fast enough in practice).
             var adjacentLinks = outerThis.getAdjacentLinks(nodeData);
            adjacentLinks
                .classed("dimmedLink", false)
                .classed("highlightedLink", true)
            ;
            
            adjacentLinks.each(function(aLink: L){
                d3.selectAll(BaseGraphView.nodeSvgClass+", "+BaseGraphView.nodeInnerSvgClass)
                .filter(function(otherNode: N, i){return aLink.source.getEntityId() === otherNode.getEntityId() || aLink.target.getEntityId() === otherNode.getEntityId();})
                .classed("dimmedNode", false)
                .classed("highlightedNode", true)
                .each(function(aNode: N){
                    d3.selectAll(BaseGraphView.nodeLabelSvgClass)
                    .filter(function(text: N, i){return aNode.getEntityId() === text.getEntityId()})
                    .classed("dimmedNodeLabel", false)
                    .classed("highlightedNodeLabel", true)
                    ;
                });
            });
        }
    }
    
    unhighlightHoveredLinkLambda(outerThis: BaseGraphView<N, L>){
        return function(linkData, i){
            outerThis.removeAllNodeHighlighting();
            outerThis.removeAllLinkHighlighting();
        }
    }
    
    unhighlightHoveredNodeLambda(outerThis: BaseGraphView<N, L>){
        return function(nodeData, i){
            outerThis.removeAllNodeHighlighting();
            outerThis.removeAllLinkHighlighting();
            outerThis.afterNodeUnhighlight(nodeData);
        }
    }
    
    removeAllNodeHighlighting(){
        d3.selectAll(BaseGraphView.nodeSvgClass+", "+BaseGraphView.nodeInnerSvgClass)
            .classed("dimmedNode", false)
            .classed("highlightedNode", false)
            ;
        d3.selectAll(BaseGraphView.nodeLabelSvgClass)
            .classed("dimmedNodeLabel", false)
            .classed("highlightedNodeLabel", false)
            ;
    }
    
    removeAllLinkHighlighting() {
        d3.selectAll(BaseGraphView.linkSvgClass)
            .classed("dimmedLink", false)
            .classed("highlightedLink", false)
            ;
        
    }
    
    hideNodeLambda(outerThis: BaseGraphView<N, L>){
        return function(nodeData: N, i){
            outerThis.nodeHider(nodeData, true);
        }
    
    }
    
    unhideNodeLambda(outerThis: BaseGraphView<N, L>){
        return function(nodeData: N, i){
            outerThis.nodeHider(nodeData, false);
        }
    
    }
    
    private nodeHider(nodeData: N, hiding: boolean){
        // Hide the node and label away first
        var sourceGNode = d3.selectAll(BaseGraphView.nodeSvgClass)
            .filter(function(d: N, i){ return d === nodeData; })
            .node().parentNode;
        // In order to hide any baggage (like expander menu indicators), we need to grab the parent
        d3.select(sourceGNode)
            .classed("hiddenNode", hiding);
        
        d3.selectAll(BaseGraphView.nodeLabelSvgClass)
            .filter(function(d: N, i){ return d === nodeData;})
            .classed("hiddenNodeLabel", hiding);
        
        // Hide edges too
        var adjacentLinks = this.getAdjacentLinks(nodeData);
        adjacentLinks
            .classed("hiddenBecauseOfNodeLink",
                function(linkData: L, i){
                    // Look at both endpoints of link, see if both are hidden
                    var source: D3.Selection = d3.selectAll(BaseGraphView.nodeSvgClass)
                        .filter(function(d: N, i){ return d === linkData.source; });
                    var target: D3.Selection = d3.selectAll(BaseGraphView.nodeSvgClass)
                        .filter(function(d: N, i){ return d === linkData.target; });
                    // if hiding, we hide the link no matter what
                    // if not hiding, then we pass false if either node is hidden
                    return hiding || source.classed("hiddenNode") || target.classed("hiddenNode");
                })
        ;
    }
    
    hideLinks(links: D3.Selection){
        this.linkHider(links, true);
    }
    
    unhideLinks(links: D3.Selection){
        this.linkHider(links, false);
    }
    
    private linkHider(links: D3.Selection, hiding: boolean){
        // Different style from the node hider() above, but I don't mind much.
        links.classed("hiddenLink", hiding);
    }
    
    beforeNodeHighlight(targetNodeData){
        // Nothing by default
    }
    
    afterNodeUnhighlight(targetNodeData){
        // Nothing by default
    }
    
}