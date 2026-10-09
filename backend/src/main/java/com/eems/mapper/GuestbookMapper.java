package com.eems.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eems.entity.Guestbook;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GuestbookMapper extends BaseMapper<Guestbook> {
}
