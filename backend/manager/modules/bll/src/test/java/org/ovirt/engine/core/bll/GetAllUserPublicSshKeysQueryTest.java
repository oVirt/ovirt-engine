package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.UserProfileDao;

class GetAllUserPublicSshKeysQueryTest
        extends AbstractQueryTest<QueryParametersBase, GetAllUserPublicSshKeysQuery<QueryParametersBase>> {

    @Mock
    private UserProfileDao userProfileDaoMock;

    @Test
    void executeAsUser() {
        getQuery().executeQueryCommand();

        verifyZeroInteractions(userProfileDaoMock);
    }

    @Test
    void executeInternal() {
        getQuery().setInternalExecution(true);
        when(userProfileDaoMock.getAllPublicSshKeys()).thenReturn(Collections.emptyList());

        getQuery().executeQueryCommand();

        verify(userProfileDaoMock).getAllPublicSshKeys();
        verifyNoMoreInteractions(userProfileDaoMock);
    }

}
