package com.ouyang.service;

import com.ouyang.dao.ComicContentMapper;
import com.ouyang.model.ComicChapter;
import com.ouyang.model.ComicContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Package: com.ouyang.service
 *
 * @Author: Ouyang
 * @Date: 2017/12/27
 */
@Service
public class ComicContentService {
    @Autowired
    ComicContentMapper comicContentMapper;
    public ComicContent insert(ComicContent comicContent) {
        if (comicContentMapper.insertSelective(comicContent)>0) {
            return comicContent;
        }
        return null;
    }
}
