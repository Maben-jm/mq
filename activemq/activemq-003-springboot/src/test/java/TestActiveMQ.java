import com.maben.activemqSpringboot.ActivemqSpringbootMain;
import com.maben.activemqSpringboot.produce.QueueProduce;
import com.maben.activemqSpringboot.produce.TopicProduce;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * 测试消息MQ发送
 * 注意：测试哪一个在yml中修改相应的配置「pub-sub-domain」参数
 */
@SpringBootTest(classes = ActivemqSpringbootMain.class)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class TestActiveMQ {
    @Autowired
    private QueueProduce queueProduce;
    @Autowired
    private TopicProduce topicProduce;

    @Test
    public void testSendMsgQueue() throws Exception{
        queueProduce.productMessage();
    }
    @Test
    public void testSendMsgTopic()throws Exception{
        topicProduce.sendTopicMsg();
    }

}
