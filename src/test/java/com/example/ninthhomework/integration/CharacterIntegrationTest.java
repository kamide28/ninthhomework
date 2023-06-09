package com.example.ninthhomework.integration;

import com.example.ninthhomework.domain.user.model.Character;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @DataSet("boundary_character.yml")
    @Transactional
    public void 年齢指定より年上のキャラクターデータを取得できステータスコード200であること() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get("/characters?age=10"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSONAssert.assertEquals("""
                 [{
                   "id": 2,
                   "name": "chihiro",
                   "age": 11
                 }]
                """, response, JSONCompareMode.STRICT);
    }

    @Test
    @DataSet("boundary_character.yml")
    @Transactional
    public void 年齢指定以下のデータしか存在しない時空で返しステータスコード200であること() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get("/characters?age=11"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSONAssert.assertEquals("""
                 [
                 ]
                """, response, JSONCompareMode.STRICT);
    }

    @Test
    @Transactional
    public void 入力データを登録できステータスコードが201であること() throws Exception {
        Character inputData = new Character("mei", 5);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String requestBody = ow.writeValueAsString(inputData);

        String response = mockMvc.perform(post("/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSONAssert.assertEquals("""
                {
                    "message" : "character successfully created"
                }
                """, response, JSONCompareMode.STRICT);
    }

    @Test
    @Transactional
    public void 入力データが不正の時登録できずステータスコード400となること() throws Exception {
        mockMvc.perform(post("/characters")
                        .content("""
                                {
                                  "name": " ",
                                  "age": 11
                                }
                                """)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DataSet(value = "characters.yml")
    @Transactional
    public void 指定IDにデータが更新できステータスコード200であること() throws Exception {
        Character updateData = new Character("meityann", 4);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String requestBody = ow.writeValueAsString(updateData);

        String response = mockMvc.perform(patch("/characters/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSONAssert.assertEquals("""
                {
                    "message" : "character successfully updated"
                }
                """, response, JSONCompareMode.STRICT);
    }

    @Test
    @DataSet(value = "characters.yml")
    @Transactional
    public void 指定IDが存在しない時更新せず404エラーとなること() throws Exception {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(
                2023, 6, 26, 11, 11, 11, 111000000,
                ZoneId.of("Asia/Tokyo"));
        try (MockedStatic<ZonedDateTime> zonedDateTimeMockedStatic = Mockito.mockStatic(ZonedDateTime.class)) {
            zonedDateTimeMockedStatic.when(() -> ZonedDateTime.now()).thenReturn(zonedDateTime);

            String response = mockMvc.perform(patch("/characters/11")
                            .content("""
                                    {
                                      "name": "maho",
                                      "age": 11
                                    }
                                    """)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
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
    public void 指定IDのデータを削除しステータスコード200となること() throws Exception {
        String response = mockMvc.perform(delete("/characters/1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSONAssert.assertEquals("""
                {
                    "message" : "character successfully deleted"
                }
                """, response, JSONCompareMode.STRICT);
    }

    @Test
    @DataSet(value = "characters.yml")
    @Transactional
    public void 指定IDが存在しない時削除せず404エラーを返すこと() throws Exception {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(
                2023, 6, 26, 11, 11, 11, 111000000,
                ZoneId.of("Asia/Tokyo"));
        try (MockedStatic<ZonedDateTime> zonedDateTimeMockedStatic = Mockito.mockStatic(ZonedDateTime.class)) {
            zonedDateTimeMockedStatic.when(() -> ZonedDateTime.now()).thenReturn(zonedDateTime);

            String response = mockMvc.perform(delete("/characters/11"))
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
}
