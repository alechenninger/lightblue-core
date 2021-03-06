/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.assoc;

import java.io.Serializable;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.FieldInfo;

import com.redhat.lightblue.metadata.CompositeMetadata;

import com.redhat.lightblue.util.Path;

/**
 * A query clause that cannot be further broken into conjuncts of a
 * conjunctive normal form query. This class also keeps metadata
 * related to that clause, such as the referred nodes of query plan,
 * fieldinfo, etc.
 *
 * Object identify of conjuncts are preserved during query plan
 * processing. That is, a Conjunct object created for a particular
 * query plan is used again for other incarnation of that query plan,
 * only node associations are changed. So, it is possible to keep maps
 * that map a Conjunct to some other piece of data. This is important
 * in query scoring. Scoring can process conjuncts, and keep data
 * internally to prevent recomputing the cost associated with the
 * conjunct for every possible query plan.
 */
public class Conjunct implements Serializable {

    private static final long serialVersionUID=1l;
    
    private static final Logger LOGGER=LoggerFactory.getLogger(Conjunct.class);
    
    /**
     * The query clause
     */
    private final QueryExpression clause;
    
    /**
     * Field info for the fields in the clause
     */
    private final List<ResolvedFieldInfo> fieldInfo=new ArrayList<>();
    
    /**
     * A mapping from absolute field names to the query plan nodes
     * containing that field
     */
    private final Map<Path,QueryPlanNode> fieldNodeMap=new HashMap();

    /**
     * The list of distinct query plan nodes referred by the clause
     */
    private final List<QueryPlanNode> referredNodes=new ArrayList<>();
    
    public Conjunct(QueryExpression q,
                    CompositeMetadata compositeMetadata,
                    QueryPlan qplan) {
        this.clause=q;
        List<FieldInfo> fInfo=clause.getQueryFields();
        LOGGER.debug("Conjunct for query {} with fields {}",q,fieldInfo);
        for(FieldInfo fi:fInfo) {
            ResolvedFieldInfo rfi=new ResolvedFieldInfo(fi,compositeMetadata);
            fieldInfo.add(rfi);
            CompositeMetadata cmd=compositeMetadata.getEntityOfPath(rfi.getAbsFieldName());
            if(cmd==null)
                throw new IllegalArgumentException("Cannot find field in composite metadata "+rfi.getAbsFieldName()); 
            QueryPlanNode qnode=qplan.getNode(cmd);
            if(qnode==null)
                throw new IllegalArgumentException("An entity referenced in a query is not in composite metadata. Query:"+
                                                   clause+" fieldInfo:"+rfi+" Composite metadata:"+cmd);
            
            boolean found=false;
            for(QueryPlanNode n:referredNodes)
                if(n==qnode) {
                    found=true;
                    break;
                }
            if(!found)
                referredNodes.add(qnode);
            fieldNodeMap.put(rfi.getAbsFieldName(),qnode);
        }
    }

    /**
     * Returns the nodes referenced by this clause
     */
    public List<QueryPlanNode> getReferredNodes() {
        return referredNodes;
    }

    /**
     * Returns the query plan node referenced by the field. Null if the field is not in this conjunct
     */
    public QueryPlanNode getFieldNode(Path field) {
        return fieldNodeMap.get(field);
    }


    /**
     * Returns the field information about the fields in the conjunct
     */
    public List<ResolvedFieldInfo> getFieldInfo() {
        return fieldInfo;
    }

    /**
     * Returns the query clause
     */
    public QueryExpression getClause() {
        return clause;
    }

    public String toString() {
        StringBuilder bld=new StringBuilder();
        bld.append("clause=").append(clause.toString()).
            append(" entities=");
        for(QueryPlanNode n:referredNodes)
            bld.append(' ').append(n.getName());
        
        return bld.toString();
    }
}
