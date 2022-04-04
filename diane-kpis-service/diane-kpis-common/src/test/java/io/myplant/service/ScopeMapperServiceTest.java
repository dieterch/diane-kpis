package io.myplant.service;

import io.myplant.model.IeeeStates;
import io.myplant.model.ScopeType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ScopeMapperServiceTest {

    private ScopeMapperService scopeMapperService = new ScopeMapperService();

    @Test
    public void testScopeMapperService() {
        assertEquals(ScopeType.INNIO_Genset, scopeMapperService.getScope(1047L, IeeeStates.FORCEDOUTAGE_MTBFO_REL));
        assertEquals(ScopeType.Unclear, scopeMapperService.getScope(1052L, IeeeStates.FORCEDOUTAGE_MTBFO_REL));
        assertEquals(ScopeType.Unclear, scopeMapperService.getScope(1055L, IeeeStates.FORCEDOUTAGE_MTBFO_REL));
    }
}
