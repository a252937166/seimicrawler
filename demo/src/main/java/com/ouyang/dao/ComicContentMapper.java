package com.ouyang.dao;

import com.ouyang.model.ComicContent;

public interface ComicContentMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ComicContent record);

    int insertSelective(ComicContent record);

    ComicContent selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ComicContent record);

    int updateByPrimaryKey(ComicContent record);
}