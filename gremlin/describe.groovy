
// https://gist.githubusercontent.com/robertdale/ad4c63910009dd1118abe67b33ce41e1/raw/27502e0db6916995a44ccfc00b4d1c11d62ba8a2/describe.groovy
// This can be imported via ./bin/gremlin.sh -i describe.groovy
// A variable 'graph' must be defined with a JanusGraph graph
// Run it as a plugin command ':schema'
// :schema describe
// 

import org.janusgraph.graphdb.database.management.MgmtLogType
import org.codehaus.groovy.tools.shell.Groovysh
import org.codehaus.groovy.tools.shell.CommandSupport


class schema extends CommandSupport {
    protected schema(final Groovysh shell) {
        super(shell, 'schema', 'T')
    }
    public Object execute(List args) {
        if (!args) {
            println "Usage: must provide command: describe [ vertex | edge | propertykey | index ]"
        }
        switch(args[0]) {
            case 'describe':
                doDescribe(args.tail())
                break;
            default:
                println "Unknown command: " + args[0]
        }
    }

    public void doDescribe(Object args) {
        try {
            ensureMgmtOpen()

            if (!args) {
                describeAll()
            } else {
                switch (args[0]) {
                    case 'index':
                        printIndex(getIndex(args.tail()))
                        break;
                    case 'vertex':
                        printVertex(getVertex(args.tail()))
                        break;
                    case 'edge':
                        printEdge(getEdge(args.tail()))
                        break;
                    case 'propertykey':
                        printPropertyKey(getPropertyKey(args.tail()))
                        break;
                    default:
                        println "Unknown 'describe' command: " + args
                }
            }
        } finally {
            ensureMgmtClosed()
        }
    }

    public void describeAll() {
        printVertex(getVertex([]))
        printEdge(getEdge([]))
        printPropertyKey(getPropertyKey([]))
        printIndex(getIndex([]))
    }

    public Object getVertex(Object args) {
        if (args) {
            return getManagement().getVertexLabels().findAll {
                args.contains(it.name())
            }
        } else {
            return getManagement().getVertexLabels()
        }
    }

    public void printVertex(Object args) {
        def pattern = "%-30s | %11s | %6s\n"
        printf(pattern, 'Vertex Label', 'Partitioned', 'Static')
        printf(pattern, '------------', '-----------', '------')
        args.each {
            printf(pattern, it.name().take(30), it.isPartitioned(), it.isStatic())
        }
        printf(pattern, '------------', '-----------', '------')
        println ""
    }

    public Object getIndex(Object args) {
        def result = []
        result.addAll(getManagement().getGraphIndexes(Vertex.class))
        result.addAll(getManagement().getGraphIndexes(Edge.class))
        result.addAll(getManagement().getGraphIndexes(PropertyKey.class))
        result.addAll(getRelation(RelationType.class,[]).collect{getManagement().getRelationIndexes(it).flatten()}.flatten())
        if (args) {
            return result.findAll {
                args.contains(it.name())
            }
        }
        return result
    }

    public void printIndex(Object args) {
        def pattern = "%-30s | %9s | %16s | %6s | %13s | %15s | %20s\n"
        printf(pattern, 'Graph Index', 'Type', 'Element', 'Unique', 'Backing', 'PropertyKey', 'Status')
        printf(pattern, '-----------', '----', '-------', '------', '-------', '-----------', '------')
        args.findAll{ it instanceof JanusGraphIndex}.each {
            def idxType = "Unknown"
            if (it.isCompositeIndex()) {
                idxType = "Composite"
            } else if (it.isMixedIndex()) {
                idxType = "Mixed"
            }
            def element = it.getIndexedElement().simpleName.take(16)
            printf(pattern, it.name().take(30), idxType, element, it.isUnique(), it.getBackingIndex().take(13), "", "")
            it.getFieldKeys().each{ fk ->
                printf(pattern, "", "", "", "", "", fk.name().take(15), it.getIndexStatus(fk).name().take(20))
            }
        }
        printf(pattern, '----------', '----', '-------', '------', '-------', '-----------', '------')
        println ""

        pattern = "%-30s | %20s | %10s | %10s | %10s | %20s\n"
        printf(pattern, 'Relation Index', 'Type', 'Direction', 'Sort Key', 'Sort Order', 'Status')
        printf(pattern, '--------------', '----', '---------', '----------', '--------', '------')
        args.findAll{ it instanceof RelationTypeIndex}.each {
            def keys = it.getSortKey()
            printf(pattern, it.name().take(30), it.getType(), it.getDirection(), keys[0], it.getSortOrder(), it.getIndexStatus().name().take(20))
            keys.tail().each{ k ->
                printf(pattern, "", "", "", k, "", "")
            }
        }
        printf(pattern, '--------------', '----', '---------', '----------', '--------', '------')
        println ""
    }

    public Object getPropertyKey(Object args) {
        return getRelation(PropertyKey.class, args)
    }

    public Object getEdge(Object args) {
        return getRelation(EdgeLabel.class, args)
    }

    public Object getRelation(Object type, Object args) {
        def result = []
        result.addAll(getManagement().getRelationTypes(type))
        if (args) {
            return result.findAll {
                args.contains(it.name())
            }
        }
        return result
    }

    public void printEdge(Object args) {
        def pattern = "%-30s | %15s | %15s | %15s | %15s\n"
        printf(pattern, 'Edge Name', 'Type', 'Directed', 'Unidirected', 'Multiplicity')
        printf(pattern, '---------', '----', '--------', '-----------', '------------')
        args.each {
            def relType = "Unknown"
            if (it.isEdgeLabel()) {
                relType ='Edge'
            } else if (it.isPropertyKey()) {
                relType = 'PropertyKey'
            }

            printf(pattern, it.name().take(30), relType, it.isDirected(), it.isUnidirected(), it.multiplicity())
        }
        printf(pattern, '---------', '----', '--------', '-----------', '------------')
        println ""
    }

    public void printPropertyKey(Object args) {
        def pattern = "%-30s | %15s | %15s | %20s\n"
        printf(pattern, 'PropertyKey Name', 'Type', 'Cardinality', 'Data Type')
        printf(pattern, '----------------', '----', '-----------', '---------')
        args.each {
            def relType = "Unknown"
            if (it.isEdgeLabel()) {
                relType ='Edge'
            } else if (it.isPropertyKey()) {
                relType = 'PropertyKey'
            }

            printf(pattern, it.name().take(30), relType, it.cardinality(), it.dataType())
        }
        printf(pattern, '----------------', '----', '-----------', '---------')
        println ""
    }

    private void ensureMgmtOpen() {
        def mgmt = getManagement()
        if (!mgmt.isOpen()) {
            openManagement()
        }
    }

    private void ensureMgmtClosed() {
        def mgmt = getManagement()
        if (mgmt.isOpen()) {
            mgmt.rollback()
        }
    }

    public Object getManagement() {
        if (!variables.get('mgmt')) {
            openManagement()
        }
        return variables.get('mgmt')
    }

    public void openManagement() {
        def graph = getGraph()
        graph.tx().rollback()
        variables.put('mgmt', graph.openManagement())
    }

    public Object getGraph() {
        if (!variables.get('graph')) {
            throw new RuntimeException("'graph' must be configured!")
        }
        return variables.get('graph')
    }
}

:rc schema