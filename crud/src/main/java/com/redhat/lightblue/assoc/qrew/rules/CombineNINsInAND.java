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
package com.redhat.lightblue.assoc.qrew.rules;

import com.redhat.lightblue.query.NaryLogicalOperator;
import com.redhat.lightblue.query.NaryRelationalOperator;

import com.redhat.lightblue.assoc.qrew.Rewriter;

/**
 * If 
 * <pre>
 *   q={$and:{...,{$nin:{field:x,values:[v]},..,{$nin:{field:x,values=[w]}...}}
 * </pre>
 * this rewrites q as
 * <pre>
 *   q={$and:{...,{$nin:{field:x,values:[v w]}},...}}
 * </pre>
 */
public class CombineNINsInAND extends CombineInsNotIns {

    public static final Rewriter INSTANCE=new CombineNINsInAND();

    public CombineNINsInAND() {
        super(NaryLogicalOperator._and,NaryRelationalOperator._not_in);
    }
}
