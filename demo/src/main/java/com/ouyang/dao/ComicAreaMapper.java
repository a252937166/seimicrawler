package com.ouyang.dao;

import com.ouyang.model.ComicArea;

public interface ComicAreaMapper {
    int deleteByPrimaryKey(String id);

    int insert(ComicArea record);

    int insertSelective(ComicArea record);

    ComicArea selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ComicArea record);

    int updateByPrimaryKey(ComicArea record);
}