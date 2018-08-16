package io.onemfive.core.infovault;

import org.neo4j.graphdb.RelationshipType;

public enum RelTypes implements RelationshipType {
    // Memory Test to DID
    TAKEN_BY,

    // Health Record to DID
    RECORD_OF
}
