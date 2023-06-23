package com.example.ninthhomework.controller;

import com.example.ninthhomework.domain.user.model.Character;
import com.example.ninthhomework.domain.user.service.CharacterServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CharacterListController.class)
@ExtendWith(MockitoExtension.class)
class CharacterListControllerTest {

    @InjectMocks
    private CharacterListController characterListController;

    @MockBean
    private CharacterServiceImpl characterServiceImpl;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void 指定IDの情報を返すこと() throws Exception {
        Character character = new Character(1, "mei", 5);

        doReturn(character).when(characterServiceImpl).findById(1);
        mockMvc.perform(get("/characters/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                            "id" : 1,
                            "name": "mei",
                            "age" : 5
                        }
                        """));
    }

    @Test
    public void IDなしの全てのデータを返すこと() throws Exception {
        List<Character> characters = new ArrayList<>();
        characters.add(new Character(1, "mei", 5));
        characters.add(new Character(2, "satuki", 10));
        characters.add(new Character(3, "tatuo", 32));

        doReturn(characters).when(characterServiceImpl).getCharacters();
        mockMvc.perform(get("/characters-without-id").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(MockMvcResultMatchers.content().json("""
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
                          }
                        ]
                        """));

    }

    @Test
    public void クエリで指定した年齢以上のデータを返すこと() throws Exception {
        List<Character> characters = new ArrayList<>();
        characters.add(new Character(3, "tatuo", 32));
        doReturn(characters).when(characterServiceImpl).findByAge(30);

        mockMvc.perform(get("/characters?age=30").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(MockMvcResultMatchers.content().json("""
                        [
                          {
                            "id": 3,
                            "name": "tatuo",
                            "age": 32
                          }
                        ]
                        """));
    }

    @Test
    public void 年齢の指定がなければ全件データを返すこと() throws Exception {
        List<Character> characters = new ArrayList<>();
        characters.add(new Character(1, "mei", 5));
        characters.add(new Character(2, "satuki", 10));
        characters.add(new Character(3, "tatuo", 32));

        doReturn(characters).when(characterServiceImpl).findByAge(null);
        mockMvc.perform(get("/characters").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(MockMvcResultMatchers.content().json("""
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
                          }
                        ]
                         """));
    }

    @Test
    public void 新規のデータが登録できること() throws Exception {
        CreateForm inputData = new CreateForm("mei", 5);
        doReturn(new Character(1, "mei", 5))
                .when(characterServiceImpl).createCharacter(inputData.getName(), inputData.getAge());

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
    public void 入力データで更新ができること() throws Exception {
        UpdateForm updateForm = new UpdateForm("satuki", 10);
        //id=1が割り振られると仮定する
        Character character = new Character(1, updateForm.getName(), updateForm.getAge());
        doReturn(character).when(characterServiceImpl).updateCharacter(1, updateForm.getName(), updateForm.getAge());

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String requestBody = ow.writeValueAsString(updateForm);

        String response = mockMvc.perform(patch("/characters/10")
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
    public void 指定されたIDのデータが削除できること() throws Exception {
        doNothing().when(characterServiceImpl).deleteCharacter(1);
        String response = mockMvc.perform(delete("/characters/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JSONAssert.assertEquals("""
                {
                    "message" : "character successfully deleted"
                }
                """, response, JSONCompareMode.STRICT);
    }
}

