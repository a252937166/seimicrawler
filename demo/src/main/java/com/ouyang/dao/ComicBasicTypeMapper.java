package com.ouyang.dao;

import com.ouyang.model.ComicBasicType;

public interface ComicBasicTypeMapper {
    int deleteByPrimaryKey(String id);

    int insert(ComicBasicType record);

    int insertSelective(ComicBasicType record);

    ComicBasicType selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ComicBasicType record);

    int updateByPrimaryKey(ComicBasicType record);
}