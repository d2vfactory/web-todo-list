package com.d2vfactory.webtodolist.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class TodoListDTO extends ErrorDTO {

    private List<TodoDTO> todoList;

}
