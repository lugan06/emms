package com.eems.service.impl;

import com.eems.dto.CompanyProfileUpdateRequest;
import com.eems.entity.CompanyProfile;
import com.eems.mapper.CompanyProfileMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompanyProfileServiceImplTest {
    @Test
    void updateDefaultShouldUpdateSingletonProfile() {
        CompanyProfileMapper mapper = mock(CompanyProfileMapper.class);
        CompanyProfile profile = new CompanyProfile();
        profile.setId(1L);
        profile.setProfileCode("default");
        when(mapper.selectOne(any())).thenReturn(profile);

        CompanyProfileServiceImpl service = new CompanyProfileServiceImpl(mapper);
        CompanyProfileUpdateRequest request = new CompanyProfileUpdateRequest(
                "EEMS Company", "Address", null, "Alice", null, null, null,
                "test@example.com", null, null, null, null);
        var result = service.updateDefault(request);

        verify(mapper).updateById(profile);
        assertEquals("EEMS Company", result.companyName());
        assertEquals("test@example.com", result.email());
    }
}
