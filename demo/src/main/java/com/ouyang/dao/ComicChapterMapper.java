package com.ouyang.dao;

import com.ouyang.model.ComicChapter;
import org.apache.ibatis.annotations.Param;

public interface ComicChapterMapper {
    int deleteByPrimaryKey(String id);

    int insert(ComicChapter record);

    int insertSelective(ComicChapter record);

    ComicChapter selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ComicChapter record);

    int updateByPrimaryKey(ComicChapter record);

    ComicChapter select(ComicChapter comicChapter);

    String getIdByName(@Param("name") String name);
}