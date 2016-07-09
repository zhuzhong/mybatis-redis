/**
 * 
 */
package org.mybatis.cache.redis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcUtils;

/**
 * @author sunff
 *
 */
public class RedisCacheFatoryTest {

    @Test
    public void constractTest() {
            RedisCacheFactory f=RedisCacheFactory.instance();
    }

    @Test
    public void tableNameTest() {
        String sql = "select * from dV.Test t left join dv.teSt2 t2 where t.id=t2.id where t.id=?";
        String dbType = "mysql";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        SQLStatement statement = parser.parseStatement();
        SchemaStatVisitor statVisitor = dbType.equalsIgnoreCase(JdbcUtils.MYSQL) ? new MySqlSchemaStatVisitor()
                : new OracleSchemaStatVisitor();
        statement.accept(statVisitor);

        Map<TableStat.Name, TableStat> tables = statVisitor.getTables();
        // statVisitor.getAliasMap()
        Set<String> tableString = new HashSet<String>();
        for (TableStat.Name name : tables.keySet()) {
            // tableString.add(name.toString());
            System.out.println(name.toString());
            System.out.println(name.toString().toUpperCase());
        }

    }
}
