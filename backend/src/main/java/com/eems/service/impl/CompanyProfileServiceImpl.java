package com.eems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eems.common.exception.BusinessException;
import com.eems.dto.CompanyProfileUpdateRequest;
import com.eems.entity.CompanyProfile;
import com.eems.mapper.CompanyProfileMapper;
import com.eems.service.CompanyProfileService;
import com.eems.service.OperationLogService;
import com.eems.vo.CompanyProfileVO;
import com.eems.vo.PublicCompanyVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class CompanyProfileServiceImpl implements CompanyProfileService {
    private static final String DEFAULT_CODE = "default";
    private final CompanyProfileMapper mapper;
    private final OperationLogService operationLogService;

    public CompanyProfileServiceImpl(CompanyProfileMapper mapper) {
        this(mapper, null);
    }

    @Autowired
    public CompanyProfileServiceImpl(CompanyProfileMapper mapper, OperationLogService operationLogService) {
        this.mapper = mapper;
        this.operationLogService = operationLogService;
    }

    @Override
    public CompanyProfileVO getDefault() {
        return CompanyProfileVO.from(findDefault());
    }

    @Override
    public PublicCompanyVO getPublicDefault() {
        return PublicCompanyVO.from(findDefault());
    }

    @Override
    @Transactional
    public CompanyProfileVO updateDefault(CompanyProfileUpdateRequest request) {
        return updateDefault(request, null);
    }

    @Override
    @Transactional
    public CompanyProfileVO updateDefault(CompanyProfileUpdateRequest request, String username) {
        CompanyProfile value = findDefault();
        value.setCompanyName(request.companyName());
        value.setAddress(request.address());
        value.setPostalCode(request.postalCode());
        value.setContactName(request.contactName());
        value.setMobile(request.mobile());
        value.setTelephone(request.telephone());
        value.setFax(request.fax());
        value.setEmail(request.email());
        value.setQq(request.qq());
        value.setWechatImageUrl(request.wechatImageUrl());
        value.setBusinessLicenseNo(request.businessLicenseNo());
        value.setOtherInfo(request.otherInfo());
        mapper.updateById(value);
        AuditSupport.success(operationLogService, "COMPANY_PROFILE", username, "修改公司信息", "PUT",
                "/api/admin/company", "{\"profileCode\":\"default\"}");
        return CompanyProfileVO.from(value);
    }

    private CompanyProfile findDefault() {
        CompanyProfile value = mapper.selectOne(new LambdaQueryWrapper<CompanyProfile>()
                .eq(CompanyProfile::getProfileCode, DEFAULT_CODE));
        if (value == null) {
            throw new BusinessException("COMPANY_PROFILE_NOT_FOUND", "默认公司信息不存在");
        }
        return value;
    }
}
