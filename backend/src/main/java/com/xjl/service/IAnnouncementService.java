package com.xjl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjl.domain.PageResult;
import com.xjl.domain.dto.request.AnnouncementRequest;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.dto.response.AnnouncementVO;
import com.xjl.domain.entity.Announcement;

public interface IAnnouncementService extends IService<Announcement> {

    PageResult<AnnouncementVO> list(PageQuery query);

    AnnouncementVO getDetailById(Long id);

    void add(Long userId, AnnouncementRequest request);

    void update(Long id, AnnouncementRequest request);

    void delete(Long id);
}
