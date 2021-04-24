package com.maben.springcloudstream.myCommodLineRunner;

import com.maben.springcloudstream.service.SenderService;
import com.maben.springcloudstream.pojo.Foo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

public class CustomRunner implements CommandLineRunner {

		private final String bindingName;

		public CustomRunner(String bindingName) {
			this.bindingName = bindingName;
		}

		@Autowired
		private SenderService senderService;

		@Override
		public void run(String... args) throws Exception {
			if ("output1".equals(this.bindingName)) {
				int count = 5;
				for (int index = 1; index <= count; index++) {
					String msgContent = "msg-" + index;
					if (index % 3 == 0) {
						senderService.send(msgContent);
					}
					else if (index % 3 == 1) {
						senderService.sendWithTags(msgContent, "tagStr");
					}
					else {
						senderService.sendObject(new Foo(index, "foo"), "tagObj");
					}
				}
			}

		}

	}