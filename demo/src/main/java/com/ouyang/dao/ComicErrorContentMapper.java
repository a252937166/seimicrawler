package com.ouyang.dao;

import com.ouyang.model.ComicErrorContent;
import org.apache.ibatis.annotations.Options;

public interface ComicErrorContentMapper {
    int deleteByPrimaryKey(Integer id);

    @Options(useGeneratedKeys = true)
    int insert(ComicErrorContent record);

    @Options(useGeneratedKeys = true)
    int insertSelective(ComicErrorContent record);

    ComicErrorContent selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ComicErrorContent record);

    int updateByPrimaryKey(ComicErrorContent record);
}