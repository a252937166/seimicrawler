package com.ouyang.model;

public class ComicChapter {
    private String id;

    private String basicId;

    private String name;

    private Integer chapterNo;

    private Integer status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getBasicId() {
        return basicId;
    }

    public void setBasicId(String basicId) {
        this.basicId = basicId == null ? null : basicId.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getChapterNo() {
        return chapterNo;
    }

    public void setChapterNo(Integer chapterNo) {
        this.chapterNo = chapterNo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}