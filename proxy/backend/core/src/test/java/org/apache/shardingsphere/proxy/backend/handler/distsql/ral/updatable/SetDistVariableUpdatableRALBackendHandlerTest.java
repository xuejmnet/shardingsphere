/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetDistVariableStatement;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.proxy.backend.exception.UnsupportedVariableException;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.UpdatableRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class SetDistVariableUpdatableRALBackendHandlerTest {
    
    private static final String DATABASE_PATTERN = "db_%s";
    
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        connectionSession = new ConnectionSession(mock(MySQLDatabaseType.class), TransactionType.LOCAL, new DefaultAttributeMap());
    }
    
    @Test
    void assertSwitchTransactionTypeXA() throws SQLException {
        connectionSession.setCurrentDatabase(String.format(DATABASE_PATTERN, 0));
        UpdatableRALBackendHandler<?> handler = new UpdatableRALBackendHandler<>(new SetDistVariableStatement("transaction_type", "XA"), connectionSession);
        ResponseHeader actual = handler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(connectionSession.getTransactionStatus().getTransactionType(), is(TransactionType.XA));
    }
    
    @Test
    void assertSwitchTransactionTypeBASE() throws SQLException {
        connectionSession.setCurrentDatabase(String.format(DATABASE_PATTERN, 0));
        UpdatableRALBackendHandler<?> handler = new UpdatableRALBackendHandler<>(new SetDistVariableStatement("transaction_type", "BASE"), connectionSession);
        ResponseHeader actual = handler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(connectionSession.getTransactionStatus().getTransactionType(), is(TransactionType.BASE));
    }
    
    @Test
    void assertSwitchTransactionTypeLOCAL() throws SQLException {
        connectionSession.setCurrentDatabase(String.format(DATABASE_PATTERN, 0));
        UpdatableRALBackendHandler<?> handler = new UpdatableRALBackendHandler<>(new SetDistVariableStatement("transaction_type", "LOCAL"), connectionSession);
        ResponseHeader actual = handler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(connectionSession.getTransactionStatus().getTransactionType(), is(TransactionType.LOCAL));
    }
    
    @Test
    void assertSwitchTransactionTypeFailed() {
        connectionSession.setCurrentDatabase(String.format(DATABASE_PATTERN, 0));
        UpdatableRALBackendHandler<?> handler = new UpdatableRALBackendHandler<>(new SetDistVariableStatement("transaction_type", "XXX"), connectionSession);
        assertThrows(UnsupportedVariableException.class, handler::execute);
    }
    
    @Test
    void assertNotSupportedVariable() {
        UpdatableRALBackendHandler<?> handler = new UpdatableRALBackendHandler<>(new SetDistVariableStatement("@@session", "XXX"), connectionSession);
        assertThrows(UnsupportedVariableException.class, handler::execute);
    }
}
