package com.sg.odata.service.datasource.query;


import com.sg.odata.service.datasource.JPQLStatement;
import org.apache.olingo.server.core.uri.UriInfoImpl;
import org.apache.olingo.server.core.uri.UriResourcePrimitivePropertyImpl;
import org.apache.olingo.server.core.uri.queryoption.FilterOptionImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.BinaryImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.LiteralImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.MemberImpl;

/**
 * Created by gaoqiang on 2017/4/26.
 */
public class JPAFilterBuilder {

    /*
    * buildFromFilter
    * */
    public static  StringBuilder buildFromFilter(StringBuilder queryStringBuilder, FilterOptionImpl filterOptionImpl){
        String queryString = " AND " +  parseFilterOptionImplWhereExpression( (BinaryImpl)((filterOptionImpl).getExpression()),"e1");
        queryStringBuilder.append(queryString);
        return queryStringBuilder;
    }

    /*
    * parseFilterOptionImplWhereExpression
    * */
    private static String parseFilterOptionImplWhereExpression(BinaryImpl binaryImpl, String tableAlias){

        String left = getLeftOperand((MemberImpl)(binaryImpl.getLeftOperand()),tableAlias);
        String right = getRightOperand((LiteralImpl)binaryImpl.getRightOperand());
        switch ( binaryImpl.getOperator()){
            case AND:
                return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left + JPQLStatement.DELIMITER.SPACE
                        + JPQLStatement.Operator.AND + JPQLStatement.DELIMITER.SPACE
                        + right + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
            case OR:
                return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left + JPQLStatement.DELIMITER.SPACE
                        + JPQLStatement.Operator.OR + JPQLStatement.DELIMITER.SPACE + right
                        + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
            case EQ:
                return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left + JPQLStatement.DELIMITER.SPACE
                        + (!"null".equals(right) ? JPQLStatement.Operator.EQ : "IS") + JPQLStatement.DELIMITER.SPACE + right
                        + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
            case NE:
                return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left + JPQLStatement.DELIMITER.SPACE
                        + (!"null".equals(right) ?
                        JPQLStatement.Operator.NE :
                        "IS" + JPQLStatement.DELIMITER.SPACE + JPQLStatement.Operator.NOT)
                        + JPQLStatement.DELIMITER.SPACE + right
                        + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
            case LT:
                return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left + JPQLStatement.DELIMITER.SPACE
                        + JPQLStatement.Operator.LT + JPQLStatement.DELIMITER.SPACE + right
                        + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
            case LE:
                return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left + JPQLStatement.DELIMITER.SPACE
                        + JPQLStatement.Operator.LE + JPQLStatement.DELIMITER.SPACE + right
                        + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
            case GT:
                return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left + JPQLStatement.DELIMITER.SPACE
                        + JPQLStatement.Operator.GT + JPQLStatement.DELIMITER.SPACE + right
                        + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
            case GE:
                return JPQLStatement.DELIMITER.PARENTHESIS_LEFT + left + JPQLStatement.DELIMITER.SPACE
                        + JPQLStatement.Operator.GE + JPQLStatement.DELIMITER.SPACE + right
                        + JPQLStatement.DELIMITER.PARENTHESIS_RIGHT;
            default:
                return "";
        }
    }

    /*
    * getRightOperand
    * */
    private static String getRightOperand(LiteralImpl literalImpl){
        return literalImpl.getText();
    }

    /*
    * getLeftOperand
    * */
    private static String getLeftOperand(MemberImpl memberImpl, String tableAlias){
        return tableAlias + JPQLStatement.DELIMITER.PERIOD +
                ((UriResourcePrimitivePropertyImpl)(((UriInfoImpl)memberImpl.getResourcePath()).getLastResourcePart())).getProperty().getName();
    }

}
