package com.eems.service;

import com.eems.dto.CompanyProfileUpdateRequest;
import com.eems.vo.CompanyProfileVO;

public interface CompanyProfileService {
    CompanyProfileVO getDefault();
    CompanyProfileVO updateDefault(CompanyProfileUpdateRequest request);
}
