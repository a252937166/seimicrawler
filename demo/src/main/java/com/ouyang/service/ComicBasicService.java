package com.ouyang.service;

import com.ouyang.dao.ComicBasicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComicBasicService {
    @Autowired
    ComicBasicMapper comicBasicMapper;
}
