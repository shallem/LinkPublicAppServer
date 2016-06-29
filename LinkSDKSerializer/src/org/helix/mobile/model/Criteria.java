/*
 * Copyright 2015 Mobile Helix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.helix.mobile.model;

/**
 * Represents a client side filtering criteria used to narrow a query collection. How this
 * criteria is used depends on the context in which the type is used.
 * 
 * @author Seth
 */
public class Criteria {
    public enum Op {
        EQ,
        NEQ,
        LT,
        LEQ,
        GT,
        GEQ,
        LIKE,
        CLEAR
    };
    
    private final String field;
    private final Op op;
    private final String value;
    
    
    /**
     * Construct a criteria for a string field.
     * 
     * @param field
     * @param op
     * @param value 
     */
    public Criteria(String field, Op op, String value) {
        this.field = field;
        this.op = op;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public Op getOp() {
        return op;
    }

    public String getOpString() {
        switch(this.op) {
            case EQ:
                return "=";
            case NEQ:
                return "!=";
            case LT:
                return "<";
            case GT:
                return ">";
            case LEQ:
                return "<=";
            case GEQ:
                return ">=";
            case LIKE:
                return "LIKE";
            case CLEAR:
                return "CLEAR";
            default:
                return "=";
        }
    }
    
    public String getValue() {
        return value;
    }
}
