/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.empire.db.sqlserver;

import org.apache.empire.commons.ObjectUtils;
import org.apache.empire.commons.StringUtils;
import org.apache.empire.data.Column;
import org.apache.empire.data.DataType;
import org.apache.empire.db.DBDDLGenerator;
import org.apache.empire.db.DBDatabase;
import org.apache.empire.db.DBDatabaseDriver.DBSeqTable;
import org.apache.empire.db.DBSQLScript;
import org.apache.empire.db.DBTableColumn;

public class MSSqlDDLGenerator extends DBDDLGenerator<DBDatabaseDriverMSSQL>
{
    public MSSqlDDLGenerator(DBDatabaseDriverMSSQL driver)
    {
        super(driver);
        // set Oracle specific data types
        initDataTypes(driver);
        // Alter Column Phrase
        alterColumnPhrase  = " ALTER COLUMN ";
    }

    /**
     * sets Oracle specific data types
     * @param driver
     */
    protected void initDataTypes(DBDatabaseDriverMSSQL driver)
    {   // Override data types
        DATATYPE_CHAR       = "NCHAR";      // Fixed length chars (unicode)
        DATATYPE_VARCHAR    = "NVARCHAR";   // variable length characters (unicode)      
        DATATYPE_DATE       = "DATE";
        DATATYPE_TIMESTAMP  = (driver.isUseDateTime2() ? "DATETIME2" : "DATETIME");
        DATATYPE_CLOB       = "NTEXT";
        DATATYPE_BLOB       = "IMAGE";
        DATATYPE_UNIQUEID   = "UNIQUEIDENTIFIER";  // Globally Unique Identifier
    }

    @Override
    protected boolean appendColumnDataType(DataType type, double size, DBTableColumn c, StringBuilder sql)
    {
        switch (type)
        {
            case AUTOINC:
                super.appendColumnDataType(type, size, c, sql);
                // Check for identity column
                if (driver.isUseSequenceTable()==false)
                {   // Make this column the identity column
                    int minValue = ObjectUtils.getInteger(c.getAttribute(Column.COLATTR_MINVALUE), 1);
                    sql.append(" IDENTITY(");
                    sql.append(String.valueOf(minValue));
                    sql.append(", 1) NOT NULL");
                    return false;
                }
                break;
            case TEXT:
            case CHAR:
            {   // Char or Varchar
                if (type==DataType.CHAR)
                    sql.append((c.isSingleByteChars()) ? DATATYPE_CHAR.substring(1) : DATATYPE_CHAR);
                else
                    sql.append((c.isSingleByteChars()) ? DATATYPE_VARCHAR.substring(1) : DATATYPE_VARCHAR);
                // get length
                int len = Math.abs((int)size);
                if (len == 0)
                    len = (type==DataType.CHAR) ? 1 : 100;
                // Check sign for char (unicode) or bytes (non-unicode) 
                sql.append("(");
                sql.append(String.valueOf(len));
                sql.append(")");
            }
                break;
            case UNIQUEID:
                sql.append(DATATYPE_UNIQUEID);
                if (c.isAutoGenerated())
                    sql.append(" ROWGUIDCOL");
                break;
            case BLOB:
                sql.append(DATATYPE_BLOB);
                break;
                
            default:
                // use default
                return super.appendColumnDataType(type, size, c, sql);
        }
        return true;
    }
 
    @SuppressWarnings("unused")
    @Override
    protected void createDatabase(DBDatabase db, DBSQLScript script)
    {
        // User Master to create Database
        String databaseName = driver.getDatabaseName();
        if (StringUtils.isNotEmpty(databaseName))
        {   // Create Database
            script.addStmt("USE master");
            script.addStmt("IF NOT EXISTS(SELECT * FROM sysdatabases WHERE name = '" + databaseName + "') CREATE DATABASE " + databaseName);
            script.addStmt("USE " + databaseName);
            script.addStmt("SET DATEFORMAT ymd");
            // Sequence Table
            if (driver.isUseSequenceTable() && db.getTable(driver.getSequenceTableName())==null)
                new DBSeqTable(driver.getSequenceTableName(), db);
        }
        // default processing
        super.createDatabase(db, script);
    }
    
}
