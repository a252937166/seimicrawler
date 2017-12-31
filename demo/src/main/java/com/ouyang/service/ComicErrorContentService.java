package com.ouyang.service;

import com.ouyang.dao.ComicErrorContentMapper;
import com.ouyang.model.ComicErrorContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComicErrorContentService {
    @Autowired
    ComicErrorContentMapper comicErrorContentMapper;
    public ComicErrorContent insert(ComicErrorContent comicErrorContent) {
        if (comicErrorContentMapper.insertSelective(comicErrorContent) > 0) {
            return comicErrorContent;
        }
        return null;
    }
}
