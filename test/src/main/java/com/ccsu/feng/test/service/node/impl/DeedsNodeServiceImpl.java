package com.ccsu.feng.test.service.node.impl;

import com.ccsu.feng.test.domain.base.BaseRelationship;
import com.ccsu.feng.test.domain.node.DeedsNode;
import com.ccsu.feng.test.domain.node.PersonNode;
import com.ccsu.feng.test.domain.node.PlaceNode;
import com.ccsu.feng.test.domain.vo.DeedsVO;
import com.ccsu.feng.test.enums.LoginTime;
import com.ccsu.feng.test.enums.RelationsType;
import com.ccsu.feng.test.repository.DeedsNodeRepository;
import com.ccsu.feng.test.repository.PlaceNodeRepository;
import com.ccsu.feng.test.service.node.IBaseRelationshipService;
import com.ccsu.feng.test.service.node.IDeedsNodeService;
import com.ccsu.feng.test.service.node.IPlaceNodeService;
import com.ccsu.feng.test.utils.PageResult;
import com.ccsu.feng.test.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.ListUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author admin
 * @create 2020-02-12-16:53
 */
@Service
@Slf4j
public class DeedsNodeServiceImpl implements IDeedsNodeService {

    @Autowired
    DeedsNodeRepository deedsNodeRepository;

    @Autowired
    PlaceNodeRepository placeNodeRepository;
    @Autowired
    IPlaceNodeService placeNodeService;

    @Autowired
    PersonNodeServiceImpl personNodeService;

    @Autowired
    IBaseRelationshipService relationshipService;
    @Autowired
    RedisUtil redisUtil;


    @Override
    public DeedsNode addDeedsNode(DeedsNode deedsNode) {
        if (deedsNode == null) {
            return null;
        }
        return deedsNodeRepository.save(deedsNode);
    }


    @Override
    public DeedsNode getDeedsNodeByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }

        return deedsNodeRepository.getDeedsNodeByName(name);
    }


    @Override
    public Optional<DeedsNode> findDeedsNodeById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return deedsNodeRepository.findById(id);
    }

    @Override
    public DeedsNode addDeedsNodeByName(String name,String type) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        DeedsNode deedsNode = new DeedsNode();
        deedsNode.setName(name);
        deedsNode.setType(type);
        return deedsNodeRepository.save(deedsNode);

    }

    @Override
    public boolean deleteDeedsNode(Long id) {
        deedsNodeRepository.deleteById(id);
        return true;
    }


    @Override
    public List<BaseRelationship> addDeedsPersonRelationship(String startName, Set<String> names,String type) {
        List<BaseRelationship> list = new ArrayList<>();
        DeedsNode startDeedsNode = getDeedsNodeByName(startName);
        if (startDeedsNode == null) {
            startDeedsNode = addDeedsNodeByName(startName,type);
        }

        for (String name : names) {
            list.add(addaddDeedsPersonRelationship(startDeedsNode, name,type));
        }
        return list;
    }


    @Override
    public List<BaseRelationship> addDeedsPlaceRelationship(String startName, Set<String> names,String type) {
        List<BaseRelationship> list = new ArrayList<>();
        DeedsNode startDeedsNode = getDeedsNodeByName(startName);
        if (startDeedsNode == null) {
            startDeedsNode = addDeedsNodeByName(startName,type);
        }
        for (String name : names) {
            list.add(addaddDeedsPlaceRelationship(startDeedsNode, name,type));
        }

        return list;
    }

    @Override
    public PageResult<DeedsVO> getListDeedsNodeByPage(int pageIndex, int pageSize, String type) {
        int pageIndexNum = 0;
        if (pageIndex > 1) {
            pageIndexNum = (pageIndex - 1) * pageSize;
        }
        List<DeedsNode> page = deedsNodeRepository.getListDeedsNodeByPage(pageIndexNum, pageSize, type);
        List<DeedsVO> list = new ArrayList<>();
        for (DeedsNode deedsNode : page) {
            list.add(new DeedsVO(deedsNode));
        }
        log.info("pageIndex->{},pageSize->{}", pageIndex, pageSize);
        return new PageResult<>(pageIndex, pageSize, deedsNodeRepository.getDeedsNodeCount(type), list);
    }

    @Override
    public Long getDeedsNodeCount(String type) {
        return deedsNodeRepository.getDeedsNodeCount(type);
    }

    @Override
    public PageResult<DeedsVO> getListDeedsNodeByPageAndName(String name, int pageIndex, int pageSize, String type) {
        int pageIndexNum = 0;
        if (pageIndex > 1) {
            pageIndexNum = (pageIndex - 1) * pageSize;
        }
        List<DeedsNode> page = deedsNodeRepository.getListDeedsNodeByPageAndName(name, pageIndexNum, pageSize, type);
        List<DeedsVO> list = new ArrayList<>();
        for (DeedsNode deedsNode : page) {
            list.add(new DeedsVO(deedsNode));
        }
        log.info("pageIndex->{},pageSize->{}", pageIndex, pageSize);
        return new PageResult<>(pageIndex, pageSize, deedsNodeRepository.getDeedsNodeCountByName(type, name), list);
    }


    @Override
    public List<String> getDeedsNodeByType(String type) {

        List<String>  hget = (List<String>) redisUtil.hget("DeedsNode:", type);
        if (!ListUtils.isEmpty(hget)){
            return  hget;
        }
        List<DeedsNode> listDeedsNodeByType = deedsNodeRepository.getListDeedsNodeByType(type);
        if (ListUtils.isEmpty(listDeedsNodeByType)) {
            return null;
        }
        List<String> strings = listDeedsNodeByType.stream().map(x -> x.getName()).collect(Collectors.toList());
        redisUtil.hset("DeedsNode:",type,strings, LoginTime.SAVE_LOGIN_TIME.getTime());
        return null;
    }


    private BaseRelationship addaddDeedsPlaceRelationship(DeedsNode startDeedsNode, String endName,String  type) {
        PlaceNode endPlaceNode = placeNodeService.getPlaceNodeByName(endName);
        if (endPlaceNode != null) { // 1.地点节点存在
            BaseRelationship relationship = relationshipService
                    .findRelationshipByStarNameAndEndName(RelationsType.DEEDS_REF_PLACE.getRelation(), startDeedsNode.getName(), endName);
            if (relationship != null) {
                return relationship;
            }
        } else {
            endPlaceNode = placeNodeService.addPlaceNodeByName(endName,type);
        }
        BaseRelationship relationship = relationshipService
                .addRelationship(RelationsType.DEEDS_REF_PLACE.getRelation(), startDeedsNode, endPlaceNode);
        Set<BaseRelationship> relationships = startDeedsNode.getRelationships();
        if (relationships == null) {
            relationships = new HashSet<>();
        }
        relationships.add(relationship);
        startDeedsNode.setRelationships(relationships);
        deedsNodeRepository.save(startDeedsNode);
        return relationship;
    }


    private BaseRelationship addaddDeedsPersonRelationship(DeedsNode startDeedsNode, String endName,String type) {
        PersonNode endPersonNode = personNodeService.getPersonNodeByName(endName);
        if (endPersonNode != null) { // 1.人物节点存在
            BaseRelationship relationship = relationshipService
                    .findRelationshipByStarNameAndEndName(RelationsType.DEEDS_REF_PERSON.getRelation(), startDeedsNode.getName(), endName);
            if (relationship != null) {
                return relationship;
            }
        } else {
            endPersonNode = personNodeService.addPersonNodeByName(endName, type);
        }
        BaseRelationship relationship = relationshipService
                .addRelationship(RelationsType.DEEDS_REF_PERSON.getRelation(), startDeedsNode, endPersonNode);
        Set<BaseRelationship> relationships = startDeedsNode.getRelationships();
        if (relationships == null) {
            relationships = new HashSet<>();
        }
        relationships.add(relationship);
        startDeedsNode.setRelationships(relationships);
        deedsNodeRepository.save(startDeedsNode);
        return relationship;
    }
}
