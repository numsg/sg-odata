/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package com.sg.odata.service.datasource;


public class JPQLStatement {

  public static final class Operator {
    public static final String EQ = "=";
    public static final String NE = "<>";
    public static final String LT = "<";
    public static final String LE = "<=";
    public static final String GT = ">";
    public static final String GE = ">=";
    public static final String AND = "AND";
    public static final String NOT = "NOT";
    public static final String OR = "OR";

  }

  public static final class KEYWORD {
    public static final String SELECT = "SELECT";
    public static final String SELECT_DISTINCT = "SELECT DISTINCT";
    public static final String FROM = "FROM";
    public static final String WHERE = "WHERE";
    public static final String LEFT_OUTER_JOIN = "LEFT OUTER JOIN";
    public static final String OUTER = "OUTER";
    public static final String JOIN = "JOIN";
    public static final String ORDERBY = "ORDER BY";
    public static final String COUNT = "COUNT";
    public static final String OFFSET = ".000";
    public static final String TIMESTAMP = "ts";

  }

  public static final class DELIMITER {
    public static final char SPACE = ' ';
    public static final char COMMA = ',';
    public static final char PERIOD = '.';
    public static final char PARENTHESIS_LEFT = '(';
    public static final char PARENTHESIS_RIGHT = ')';
    public static final char COLON = ':';
    public static final char HYPHEN = '-';
    public static final char LEFT_BRACE = '{';
    public static final char RIGHT_BRACE = '}';
    public static final char LONG = 'L';
  }

}
