package com.example.ninthhomework.controller;

import com.example.ninthhomework.domain.user.model.Character;
import com.example.ninthhomework.domain.user.service.CharacterServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CharacterListController.class)
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
}
