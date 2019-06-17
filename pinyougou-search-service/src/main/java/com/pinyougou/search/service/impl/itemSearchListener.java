package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

@Component
public class itemSearchListener implements MessageListener{
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        TextMessage textMessage= (TextMessage) message;
        try {
            String text = textMessage.getText();
            System.out.println("监听接收到的消息");
            List<TbItem> items = JSON.parseArray(text, TbItem.class);

            itemSearchService.importList(items);//导入
            System.out.println("成功导入索引库");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
