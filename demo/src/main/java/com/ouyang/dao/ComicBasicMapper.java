package com.ouyang.dao;

import com.ouyang.model.ComicBasic;

public interface ComicBasicMapper {
    int deleteByPrimaryKey(String id);

    int insert(ComicBasic record);

    int insertSelective(ComicBasic record);

    ComicBasic selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ComicBasic record);

    int updateByPrimaryKey(ComicBasic record);
}