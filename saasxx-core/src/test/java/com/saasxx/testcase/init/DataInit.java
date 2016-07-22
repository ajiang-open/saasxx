package com.saasxx.testcase.init;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import com.saasxx.core.config.MainConfig;
import com.saasxx.core.config.SecurityConfig;
import com.saasxx.core.module.circle.schema.PHobby;
import com.saasxx.core.module.common.schema.PArea;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;
import com.saasxx.framework.test.JUnitInitializer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { MainConfig.class,
		SecurityConfig.class }, initializers = JUnitInitializer.class)
@ActiveProfiles("dev")
@Transactional
public class DataInit {

	static Log log = Logs.getLog();

	@PersistenceContext
	EntityManager em;

	@Test
	@Rollback(false)
	public void initHobby() {
		String hobbies = "生活 玩乐 健身 保养 兴趣 交友 户外 行业 创业";
		String[] hbs = hobbies.split("\\s+");
		for (String hb : hbs) {
			PHobby hobby = new PHobby();
			hobby.setName(hb);
			hobby.setParent(hobby);
			em.persist(hobby);
		}
	}

	@Test
	@Rollback(false)
	public void initArea() throws Exception {
		String text = IOUtils.toString(DataInit.class.getResourceAsStream("/dev/data/area/area.txt"), "UTF-8");
		Stream<String> lines = new BufferedReader(new StringReader(text)).lines();
		Map<String, String> map = new LinkedHashMap<>();
		lines.forEach(new Consumer<String>() {
			public void accept(String line) {
				line = line.trim();
				if (line.length() == 0) {
					return;
				}
				String[] ss = line.split("\\s++");
				if (ss.length != 2) {
					return;
				}
				String code = ss[0].replaceAll("[^\\d]", "");
				String name = ss[1].replaceAll("[^\u4e00-\u9fa5]", "");
				map.put(code, name);
			}
		});
		// 处理国家
		PArea country = new PArea();
		country.setCode("000000");
		country.setName("中华人民共和国");
		country.setParent(country);
		em.persist(country);
		// 处理省份
		Iterator<String> iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			String code = iterator.next();
			if (code.endsWith("0000")) {
				String name = map.get(code);
				PArea province = new PArea();
				province.setCode(code);
				province.setName(name);
				province.setParent(country);
				em.persist(province);
				iterator.remove();
			}
		}
		// 处理市行政区
		iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			String code = iterator.next();
			if (code.endsWith("00")) {
				String name = map.get(code);
				PArea province = em.createQuery("from PArea p where p.code = :code", PArea.class)
						.setParameter("code", code.substring(0, 2).concat("0000")).getSingleResult();
				PArea city = new PArea();
				city.setCode(code);
				city.setName(name);
				city.setParent(province);
				em.persist(city);
				iterator.remove();
			}
		}
		// 处理市行政区
		iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			String code = iterator.next();
			String name = map.get(code);
			PArea city = em.createQuery("from PArea p where p.code = :code", PArea.class)
					.setParameter("code", code.substring(0, 4).concat("00")).getSingleResult();
			PArea region = new PArea();
			region.setCode(code);
			region.setName(name);
			region.setParent(city);
			em.persist(region);
			iterator.remove();
		}
	}
}
