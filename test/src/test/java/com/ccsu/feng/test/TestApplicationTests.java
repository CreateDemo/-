package com.ccsu.feng.test;

import com.ccsu.feng.test.dao.UserBasesMapper;
import com.ccsu.feng.test.domain.vo.NodeRelationsListVO;
import com.ccsu.feng.test.entity.UserBases;
import com.ccsu.feng.test.service.node.IBaseRelationshipService;
import com.ccsu.feng.test.service.node.IPersonNodeService;
import com.ccsu.feng.test.utils.RedisUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestApplicationTests {

    @Autowired
    IPersonNodeService iPersonNodeService;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    UserBasesMapper userBasesMapper;

    @Autowired
    IBaseRelationshipService iBaseRelationshipService;

    @Test
    public void contextLoads() {

    }



}

