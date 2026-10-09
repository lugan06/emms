package com.eems.service;

import com.eems.dto.CompanyProfileUpdateRequest;
import com.eems.vo.PublicCompanyVO;
import com.eems.vo.CompanyProfileVO;

public interface CompanyProfileService {
    CompanyProfileVO getDefault();
    PublicCompanyVO getPublicDefault();
    CompanyProfileVO updateDefault(CompanyProfileUpdateRequest request);

    default CompanyProfileVO updateDefault(CompanyProfileUpdateRequest request, String username) {
        return updateDefault(request);
    }
}
