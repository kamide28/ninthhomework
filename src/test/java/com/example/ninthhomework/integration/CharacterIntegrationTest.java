package com.example.ninthhomework.integration;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DBRider
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CharacterIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DataSet(value = "characters.yml")
    @Transactional
    void IDを指定した時そのIDのデータが取得できること() throws Exception {
        String response = mockMvc.perform(get("/characters/1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSONAssert.assertEquals("""
                   {
                      "id":1,
                      "name":"mei",
                      "age":5
                   }
                """, response, JSONCompareMode.STRICT);
    }

    @Test
    @DataSet(value = "characters.yml")
    @Transactional
    void 存在しないIDで検索した時404エラーとなること() throws Exception {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(
                2023, 6, 26, 11, 11, 11, 111000000,
                ZoneId.of("Asia/Tokyo"));
        try (MockedStatic<ZonedDateTime> zonedDateTimeMockedStatic = Mockito.mockStatic(ZonedDateTime.class)) {
            zonedDateTimeMockedStatic.when(() -> ZonedDateTime.now()).thenReturn(zonedDateTime);

            String response = mockMvc.perform(get("/characters/11"))
                    .andExpect(status().isNotFound())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
            JSONAssert.assertEquals("""
                    {
                        "path": "/characters/11",
                        "error": "Not Found",
                        "timestamp": "2023-06-26T11:11:11.111+09:00[Asia/Tokyo]",
                        "message": "ID:11は見つかりませんでした",
                        "status": "404"
                    }
                    """, response, true);
        }
    }

    @Test
    @DataSet(value = "characters.yml")
    @Transactional
    public void IDを表示しない全件データを取得出来ステータスコードが200であること() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get("/characters-without-id"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSONAssert.assertEquals("""
                [
                  {
                    "name": "mei",
                    "age": 5
                  },
                  {
                    "name": "satuki",
                    "age": 10
                  },
                  {
                    "name": "tatuo",
                    "age": 32
                  }]
                 """, response, JSONCompareMode.STRICT);
    }


    @Test
    @DataSet(value = "characters.yml")
    @Transactional
    public void 全件データを取得出来ステータスコードが200であること() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get("/characters"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSONAssert.assertEquals("""
                [
                  {
                    "id": 1,
                    "name": "mei",
                    "age": 5
                  },
                  {
                    "id": 2,
                    "name": "satuki",
                    "age": 10
                  },
                  {
                    "id": 3,
                    "name": "tatuo",
                    "age": 32
                  }]
                 """, response, JSONCompareMode.STRICT);
    }

    @Test
    @DataSet("characters.yml")
    @Transactional
    public void 年齢指定するとそれより年上のキャラクターデータを取得できステータスコード200であること() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get("/characters?age=20"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSONAssert.assertEquals("""
                 [{
                   "id": 3,
                   "name": "tatuo",
                   "age": 32
                 }]
                """, response, JSONCompareMode.STRICT);
    }

    @Test
    @DataSet("characters.yml")
    @Transactional
    public void 年齢指定より年下のデータしか存在しない時空で返しステータスコード200であること() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get("/characters?age=100"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSONAssert.assertEquals("""
                 [
                 ]
                """, response, JSONCompareMode.STRICT);
    }
}
