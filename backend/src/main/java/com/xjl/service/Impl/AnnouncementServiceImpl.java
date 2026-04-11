package com.xjl.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xjl.domain.PageResult;
import com.xjl.domain.dto.request.AnnouncementRequest;
import com.xjl.domain.dto.request.PageQuery;
import com.xjl.domain.dto.response.AnnouncementVO;
import com.xjl.domain.entity.Announcement;
import com.xjl.domain.entity.SysUser;
import com.xjl.mapper.AnnouncementMapper;
import com.xjl.mapper.SysUserMapper;
import com.xjl.service.IAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements IAnnouncementService {

    private final SysUserMapper sysUserMapper;

    @Override
    public PageResult<AnnouncementVO> list(PageQuery query) {
        Page<Announcement> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, "1");
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(Announcement::getTitle, query.getKeyword());
        }
        wrapper.orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getCreateTime);
        Page<Announcement> result = page(page, wrapper);
        List<AnnouncementVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        PageResult<AnnouncementVO> pr = new PageResult<>();
        pr.setRecords(voList);
        pr.setTotal(result.getTotal());
        pr.setPageNum((int) result.getCurrent());
        pr.setPageSize((int) result.getSize());
        pr.setPages(result.getPages());
        return pr;
    }

    @Override
    public AnnouncementVO getDetailById(Long id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            return null;
        }
        return toVO(announcement);
    }

    @Override
    public void add(Long userId, AnnouncementRequest request) {
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setPublisherId(userId);
        announcement.setIsTop(StringUtils.hasText(request.getIsTop()) ? request.getIsTop() : "0");
        announcement.setStatus("1");
        announcement.setDelFlag("0");
        save(announcement);
    }

    @Override
    public void update(Long id, AnnouncementRequest request) {
        Announcement announcement = new Announcement();
        announcement.setId(id);
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setIsTop(request.getIsTop());
        updateById(announcement);
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }

    private AnnouncementVO toVO(Announcement a) {
        AnnouncementVO vo = new AnnouncementVO();
        vo.setId(a.getId());
        vo.setTitle(a.getTitle());
        vo.setContent(a.getContent());
        vo.setPublisherId(a.getPublisherId());
        vo.setIsTop(a.getIsTop());
        vo.setStatus(a.getStatus());
        vo.setCreateTime(a.getCreateTime());
        vo.setUpdateTime(a.getUpdateTime());

        if (a.getPublisherId() != null) {
            SysUser publisher = sysUserMapper.selectById(a.getPublisherId());
            if (publisher != null) {
                vo.setPublisherName(publisher.getNickName() != null ? publisher.getNickName() : publisher.getUserName());
            }
        }
        return vo;
    }
}
